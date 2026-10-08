package com.inventory.service;

import com.inventory.dto.SalesOrderItemRequest;
import com.inventory.dto.SalesOrderRequest;
import com.inventory.dto.SalesOrderResponse;
import com.inventory.entity.*;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.InvalidOperationException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SalesOrderRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ActivityLogService activityLogService;

    public SalesOrderService(SalesOrderRepository salesOrderRepository,
                             ProductRepository productRepository,
                             ProductService productService,
                             ActivityLogService activityLogService) {
        this.salesOrderRepository = salesOrderRepository;
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
    public List<SalesOrderResponse> getAllSalesOrders() {
        return salesOrderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse getSalesOrderById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));
        return mapToResponse(order);
    }

    @Transactional
    public SalesOrderResponse createSalesOrder(SalesOrderRequest request) {
        SalesOrder order = new SalesOrder();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerName(request.getCustomerName().trim());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setStatus(SalesStatus.PENDING);
        order.setNotes(request.getNotes());
        order.setCreatedBy(getCurrentUserEmail());

        BigDecimal total = BigDecimal.ZERO;

        for (SalesOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            // Verify current stock
            if (product.getQuantity() < itemReq.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product '" + product.getName() +
                        "'. Current stock: " + product.getQuantity() + ", Requested: " + itemReq.getQuantity());
            }

            BigDecimal unitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : product.getSellingPrice();
            SalesOrderItem item = new SalesOrderItem(product, itemReq.getQuantity(), unitPrice);
            order.addItem(item);
            total = total.add(item.getSubtotal());
        }

        order.setTotalAmount(total);
        SalesOrder saved = salesOrderRepository.save(order);
        activityLogService.log("SALES_CREATE", "SALES", "Created sales order: " + saved.getOrderNumber() + " for customer: " + saved.getCustomerName());
        return mapToResponse(saved);
    }

    @Transactional
    public SalesOrderResponse processSalesOrder(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));

        if (order.getStatus() == SalesStatus.PROCESSING || order.getStatus() == SalesStatus.SHIPPED || order.getStatus() == SalesStatus.DELIVERED) {
            throw new InvalidOperationException("Sales order " + order.getOrderNumber() + " has already been processed");
        }
        if (order.getStatus() == SalesStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot process a cancelled sales order: " + order.getOrderNumber());
        }

        // Atomically and safely reduce stock for each item
        for (SalesOrderItem item : order.getItems()) {
            productService.reduceStock(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    order.getOrderNumber(),
                    "Fulfillment for order " + order.getOrderNumber()
            );
        }

        order.setStatus(SalesStatus.PROCESSING);
        SalesOrder updated = salesOrderRepository.save(order);
        activityLogService.log("SALES_PROCESS", "SALES", "Processed sales order: " + order.getOrderNumber());
        return mapToResponse(updated);
    }

    @Transactional
    public SalesOrderResponse updateOrderStatus(Long id, SalesStatus newStatus) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));

        if (newStatus == SalesStatus.PROCESSING && order.getStatus() == SalesStatus.PENDING) {
            return processSalesOrder(id);
        }

        if (newStatus == SalesStatus.CANCELLED && (order.getStatus() == SalesStatus.PROCESSING || order.getStatus() == SalesStatus.SHIPPED)) {
            // Restore stock
            for (SalesOrderItem item : order.getItems()) {
                productService.increaseStock(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        MovementType.RETURN_IN,
                        order.getOrderNumber(),
                        "Stock restored due to cancellation of order " + order.getOrderNumber()
                );
            }
            activityLogService.log("SALES_CANCEL", "SALES", "Cancelled sales order and restored stock: " + order.getOrderNumber());
        } else {
            activityLogService.log("SALES_STATUS_UPDATE", "SALES", "Updated sales order " + order.getOrderNumber() + " status to " + newStatus.name());
        }

        order.setStatus(newStatus);
        SalesOrder updated = salesOrderRepository.save(order);
        return mapToResponse(updated);
    }

    public SalesOrderResponse mapToResponse(SalesOrder order) {
        SalesOrderResponse response = new SalesOrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomer(order.getCustomerName());
        response.setCustomerName(order.getCustomerName());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setStatus(order.getStatus().name());
        response.setTotalAmount(order.getTotalAmount());
        response.setNotes(order.getNotes());
        response.setCreatedBy(order.getCreatedBy());
        response.setCreatedAt(order.getCreatedAt());

        if (order.getItems() != null) {
            List<SalesOrderResponse.SalesOrderItemResponse> itemResponses = order.getItems().stream()
                    .map(item -> new SalesOrderResponse.SalesOrderItemResponse(
                            item.getId(),
                            item.getProduct() != null ? item.getProduct().getId() : null,
                            item.getProduct() != null ? item.getProduct().getName() : null,
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getSubtotal()
                    ))
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
            response.setItemsCount(itemResponses.size());
        }

        return response;
    }
}
