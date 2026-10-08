package com.inventory.service;

import com.inventory.dto.PurchaseOrderItemRequest;
import com.inventory.dto.PurchaseOrderRequest;
import com.inventory.dto.PurchaseOrderResponse;
import com.inventory.entity.*;
import com.inventory.exception.InvalidOperationException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseOrderRepository;
import com.inventory.repository.SupplierRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ActivityLogService activityLogService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                SupplierRepository supplierRepository,
                                ProductRepository productRepository,
                                ProductService productService,
                                ActivityLogService activityLogService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.activityLogService = activityLogService;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "system";
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        return mapToResponse(order);
    }

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNumber("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setSupplier(supplier);
        order.setStatus(PurchaseStatus.PENDING);
        order.setNotes(request.getNotes());
        order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        order.setCreatedBy(getCurrentUserEmail());

        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            BigDecimal unitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : product.getPurchasePrice();
            PurchaseOrderItem item = new PurchaseOrderItem(product, itemReq.getQuantity(), unitPrice);
            order.addItem(item);
            total = total.add(item.getSubtotal());
        }

        order.setTotalAmount(total);
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        activityLogService.log("PURCHASE_CREATE", "PURCHASES", "Created purchase order: " + saved.getOrderNumber() + " with supplier: " + supplier.getName());
        return mapToResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse updateOrderStatus(Long id, PurchaseStatus status) {
        if (status == PurchaseStatus.RECEIVED) {
            return receivePurchaseOrder(id);
        }
        if (status == PurchaseStatus.CANCELLED) {
            return cancelPurchaseOrder(id);
        }

        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        if (order.getStatus() == PurchaseStatus.RECEIVED) {
            throw new InvalidOperationException("Cannot modify status of an already received purchase order");
        }

        order.setStatus(status);
        PurchaseOrder updated = purchaseOrderRepository.save(order);
        activityLogService.log("PURCHASE_STATUS_UPDATE", "PURCHASES", "Updated purchase order " + order.getOrderNumber() + " status to " + status.name());
        return mapToResponse(updated);
    }

    @Transactional
    public PurchaseOrderResponse receivePurchaseOrder(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        if (order.getStatus() == PurchaseStatus.RECEIVED) {
            throw new InvalidOperationException("Purchase order " + order.getOrderNumber() + " has already been received");
        }
        if (order.getStatus() == PurchaseStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot receive cancelled purchase order: " + order.getOrderNumber());
        }

        for (PurchaseOrderItem item : order.getItems()) {
            productService.increaseStock(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    MovementType.PURCHASE,
                    order.getOrderNumber(),
                    "Received stock from Purchase Order " + order.getOrderNumber()
            );
        }

        order.setStatus(PurchaseStatus.RECEIVED);
        PurchaseOrder updated = purchaseOrderRepository.save(order);
        activityLogService.log("PURCHASE_RECEIVE", "PURCHASES", "Received items for purchase order: " + order.getOrderNumber());
        return mapToResponse(updated);
    }

    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        if (order.getStatus() == PurchaseStatus.RECEIVED) {
            throw new InvalidOperationException("Cannot cancel an already received purchase order");
        }

        order.setStatus(PurchaseStatus.CANCELLED);
        PurchaseOrder updated = purchaseOrderRepository.save(order);
        activityLogService.log("PURCHASE_CANCEL", "PURCHASES", "Cancelled purchase order: " + order.getOrderNumber());
        return mapToResponse(updated);
    }

    public PurchaseOrderResponse mapToResponse(PurchaseOrder order) {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        if (order.getSupplier() != null) {
            response.setSupplierId(order.getSupplier().getId());
            response.setSupplierName(order.getSupplier().getName());
        }
        response.setStatus(order.getStatus().name());
        response.setTotalAmount(order.getTotalAmount());
        response.setNotes(order.getNotes());
        response.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        response.setCreatedBy(order.getCreatedBy());
        response.setCreatedAt(order.getCreatedAt());

        if (order.getItems() != null) {
            List<PurchaseOrderResponse.PurchaseOrderItemResponse> itemResponses = order.getItems().stream()
                    .map(item -> new PurchaseOrderResponse.PurchaseOrderItemResponse(
                            item.getId(),
                            item.getProduct() != null ? item.getProduct().getId() : null,
                            item.getProduct() != null ? item.getProduct().getName() : null,
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getSubtotal()
                    ))
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }

        return response;
    }
}
