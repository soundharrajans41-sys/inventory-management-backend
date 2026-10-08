package com.inventory.service;

import com.inventory.dto.ReturnOrderItemRequest;
import com.inventory.dto.ReturnOrderRequest;
import com.inventory.dto.ReturnOrderResponse;
import com.inventory.entity.*;
import com.inventory.exception.InvalidOperationException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.ReturnOrderRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReturnOrderService {

    private final ReturnOrderRepository returnOrderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ActivityLogService activityLogService;

    public ReturnOrderService(ReturnOrderRepository returnOrderRepository,
                              ProductRepository productRepository,
                              ProductService productService,
                              ActivityLogService activityLogService) {
        this.returnOrderRepository = returnOrderRepository;
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
    public List<ReturnOrderResponse> getAllReturnOrders() {
        return returnOrderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReturnOrderResponse getReturnOrderById(Long id) {
        ReturnOrder order = returnOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found with id: " + id));
        return mapToResponse(order);
    }

    @Transactional
    public ReturnOrderResponse createReturnOrder(ReturnOrderRequest request) {
        ReturnType returnType;
        try {
            returnType = ReturnType.valueOf(request.getReturnType().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid return type: " + request.getReturnType() + ". Must be CUSTOMER_RETURN or SUPPLIER_RETURN");
        }

        ReturnOrder order = new ReturnOrder();
        order.setReturnNumber("RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setReturnType(returnType);
        order.setReferenceOrderNumber(request.getReferenceOrderNumber());
        order.setReason(request.getReason());
        order.setStatus(ReturnStatus.PENDING);
        order.setCreatedBy(getCurrentUserEmail());

        for (ReturnOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            ReturnOrderItem item = new ReturnOrderItem(product, itemReq.getQuantity(), itemReq.getRemarks());
            order.addItem(item);
        }

        ReturnOrder saved = returnOrderRepository.save(order);
        activityLogService.log("RETURN_CREATE", "RETURNS", "Created return order: " + saved.getReturnNumber() + " (" + saved.getReturnType() + ")");
        return mapToResponse(saved);
    }

    @Transactional
    public ReturnOrderResponse approveReturnOrder(Long id) {
        ReturnOrder order = returnOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found with id: " + id));

        if (order.getStatus() == ReturnStatus.COMPLETED) {
            throw new InvalidOperationException("Return order is already processed and completed");
        }

        order.setStatus(ReturnStatus.APPROVED);
        ReturnOrder updated = returnOrderRepository.save(order);
        activityLogService.log("RETURN_APPROVE", "RETURNS", "Approved return order: " + order.getReturnNumber());
        return mapToResponse(updated);
    }

    @Transactional
    public ReturnOrderResponse rejectReturnOrder(Long id) {
        ReturnOrder order = returnOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found with id: " + id));

        if (order.getStatus() == ReturnStatus.COMPLETED) {
            throw new InvalidOperationException("Cannot reject a return order that has already been processed and completed");
        }

        order.setStatus(ReturnStatus.REJECTED);
        ReturnOrder updated = returnOrderRepository.save(order);
        activityLogService.log("RETURN_REJECT", "RETURNS", "Rejected return order: " + order.getReturnNumber());
        return mapToResponse(updated);
    }

    @Transactional
    public ReturnOrderResponse processReturnOrder(Long id) {
        ReturnOrder order = returnOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return order not found with id: " + id));

        if (order.getStatus() == ReturnStatus.COMPLETED) {
            throw new InvalidOperationException("Return order " + order.getReturnNumber() + " has already been processed");
        }
        if (order.getStatus() == ReturnStatus.REJECTED) {
            throw new InvalidOperationException("Cannot process a rejected return order: " + order.getReturnNumber());
        }

        for (ReturnOrderItem item : order.getItems()) {
            if (order.getReturnType() == ReturnType.CUSTOMER_RETURN) {
                productService.increaseStock(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        MovementType.RETURN_IN,
                        order.getReturnNumber(),
                        "Customer return: " + (order.getReason() != null ? order.getReason() : "")
                );
            } else if (order.getReturnType() == ReturnType.SUPPLIER_RETURN) {
                productService.reduceStock(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        order.getReturnNumber(),
                        "Supplier return: " + (order.getReason() != null ? order.getReason() : "")
                );
            }
        }

        order.setStatus(ReturnStatus.COMPLETED);
        ReturnOrder updated = returnOrderRepository.save(order);
        activityLogService.log("RETURN_PROCESS", "RETURNS", "Processed return order and updated inventory: " + order.getReturnNumber());
        return mapToResponse(updated);
    }

    public ReturnOrderResponse mapToResponse(ReturnOrder order) {
        ReturnOrderResponse response = new ReturnOrderResponse();
        response.setId(order.getId());
        response.setReturnNumber(order.getReturnNumber());
        response.setReturnType(order.getReturnType().name());
        response.setReferenceOrderNumber(order.getReferenceOrderNumber());
        response.setReason(order.getReason());
        response.setStatus(order.getStatus().name());
        response.setCreatedBy(order.getCreatedBy());
        response.setCreatedAt(order.getCreatedAt());

        if (order.getItems() != null) {
            List<ReturnOrderResponse.ReturnOrderItemResponse> items = order.getItems().stream()
                    .map(i -> new ReturnOrderResponse.ReturnOrderItemResponse(
                            i.getId(),
                            i.getProduct() != null ? i.getProduct().getId() : null,
                            i.getProduct() != null ? i.getProduct().getName() : null,
                            i.getQuantity(),
                            i.getRemarks()
                    ))
                    .collect(Collectors.toList());
            response.setItems(items);
        }

        return response;
    }
}
