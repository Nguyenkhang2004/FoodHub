package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.OrderItemRequest;
import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.entity.*;
import com.example.FoodHub.enums.*;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.RestaurantOrderMapper;
import com.example.FoodHub.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestaurantOrderService {
    RestaurantOrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    RestaurantOrderMapper orderMapper;
    RestaurantTableRepository tableRepository;
    UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    public Page<RestaurantOrderResponse> getAllOrders(Pageable pageable) {
        Page<RestaurantOrder> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toRestaurantOrderResponse);
    }

    public RestaurantOrderResponse getCurrentOrdersByTableId(Integer tableId) {
        log.info("Fetching orders for table ID: {}", tableId);
        List<String> statuses = List.of(OrderStatus.PENDING.name(), OrderStatus.CONFIRMED.name(), OrderStatus.COMPLETED.name());
        RestaurantOrder order = orderRepository.findFirstByTableIdAndStatusInOrderByCreatedAtDesc(tableId, statuses).orElseThrow(
                () -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        return orderMapper.toRestaurantOrderResponse(order);
    }

//    public Page<RestaurantOrderResponse> getCurrentWorkShiftOrders(
//            String area,
//            String status,
//            Integer tableId,
//            BigDecimal minPrice,
//            BigDecimal maxPrice,
//            Instant startTime,
//            Pageable pageable) {
//
//        log.info("Fetching current work shift orders for area: {}, status: {}", area, status);
//
//        // Get current user's area (assuming you have authentication context)
//
//        // Define pending statuses that should be shown from previous shifts
//        List<String> pendingStatuses = List.of(
//                OrderStatus.PENDING.name(),
//                OrderStatus.CONFIRMED.name()
//        );
//
//        // Calculate shift times
//        LocalDateTime previousShiftStart = getPreviousShiftStartTime(); // You need to implement this
//        LocalDateTime previousShiftEnd = getPreviousShiftEndTime(); // You need to implement this
//
//        // Convert Instant to LocalDateTime if provided
//        LocalDateTime startTimeLocal = null;
//        if (startTime != null) {
//            startTimeLocal = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault());
//        }
//
//        // Convert tableId to tableNumber if needed
//        String tableNumber = null;
//        if (tableId != null) {
//            tableNumber = tableId.toString(); // Or get actual table number from tableId
//        }
//
//        // Call repository with correct parameters
//        Page<RestaurantOrder> orders = orderRepository.findCurrentAndPreviousOrdersByArea(
//                area,
//                pendingStatuses,
//                startTime,
//                previousShiftStart,
//                previousShiftEnd,
//                status,
//                tableNumber,
//                minPrice,
//                maxPrice,
//                startTimeLocal,
//                pageable
//        );
//
//        return orders.map(orderMapper::toRestaurantOrderResponse);
//    }


    public RestaurantOrderResponse getOrdersByOrderId(Integer id) {
        log.info("Fetching orders for order ID: {}", id);
        return orderRepository.findById(id)
                .map(orderMapper::toRestaurantOrderResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
    }

    public RestaurantOrderResponse createOrder(RestaurantOrderRequest request) {
        log.info("Creating new order for table: {}", request.getTableId());

        RestaurantOrder order = orderMapper.toRestaurantOrder(request);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        order.setUser(user);
        if(request.getOrderType().equals(OrderType.DINE_IN.name())) {
            RestaurantTable table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
            // Kiểm tra trạng thái bàn trước khi gán
            if (!TableStatus.AVAILABLE.name().equals(table.getStatus())) {
                throw new AppException(ErrorCode.TABLE_NOT_AVAILABLE);
            }
            table.setStatus(TableStatus.OCCUPIED.name()); // Đánh dấu bàn là đang sử dụng
            order.setTable(table);
            tableRepository.save(table); // Lưu lại trạng thái bàn
        }

        Set<OrderItem> orderItems = request.getOrderItems().stream()
                .map(itemReq -> {
                    OrderItem orderItem = orderMapper.toOrderItem(itemReq);
                    // Lấy Menu entity theo menuItemId trong OrderItemRequest
                    MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                            .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_EXISTED));
                    if (!MenuItemStatus.AVAILABLE.name().equals(menuItem.getStatus())) {
                        throw new AppException(ErrorCode.MENU_ITEM_NOT_AVAILABLE);
                    }
                    orderItem.setMenuItem(menuItem);
                    // Gán order cho orderItem (quan hệ hai chiều)
                    orderItem.setOrder(order);
                    // Tính toán giá dựa trên quantity và unit price của MenuItem
                    orderItem.setPrice(menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
                    // Tính tổng giá trị của đơn hàng
                    return orderItem;
                })
                .collect(Collectors.toSet());
        order.setCreatedAt(Instant.now());
        order.setOrderItems(orderItems);
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
        orderItems.forEach(orderItemRepository::save);
        return orderMapper.toRestaurantOrderResponse(order);
    }

    public RestaurantOrderResponse updateOrder(Integer orderId, RestaurantOrderRequest request) {
        log.info("Updating order with ID: {}", orderId);

        // Find existing order
        RestaurantOrder existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // Check if order can be updated (business logic)
        if (OrderStatus.COMPLETED.name().equals(existingOrder.getStatus()) || OrderStatus.CANCELLED.name().equals(existingOrder.getStatus())) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_UPDATED);
        }

        // Update basic fields using mapper
        orderMapper.updateOrder(existingOrder, request);

        // Update user if changed
        if (request.getUserId() != null && !request.getUserId().equals(existingOrder.getUser().getId())) {
            User newUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            existingOrder.setUser(newUser);
        }

        // Update table if changed
        if (request.getTableId() != null && !request.getTableId().equals(existingOrder.getTable().getId())) {
            RestaurantTable newTable = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));

            // Optional: Check if new table is available
            if (!MenuItemStatus.AVAILABLE.name().equals(newTable.getStatus())) {
                throw new AppException(ErrorCode.TABLE_NOT_AVAILABLE);
            }

            // Update table statuses
            RestaurantTable oldTable = existingOrder.getTable();
            oldTable.setStatus(TableStatus.AVAILABLE.name()); // Free up old table
            newTable.setStatus(TableStatus.OCCUPIED.name());   // Occupy new table

            tableRepository.save(oldTable);
            tableRepository.save(newTable);

            existingOrder.setTable(newTable);
        }

        // Update order items - This is the complex part
        updateOrderItems(existingOrder, request.getOrderItems());

        // Recalculate total amount - EXCLUDE CANCELLED ITEMS
        BigDecimal newTotalAmount = calculateTotalAmount(existingOrder);
        existingOrder.setTotalAmount(newTotalAmount);

        // Save updated order
        RestaurantOrder savedOrder = orderRepository.save(existingOrder);

        log.info("Order updated successfully with ID: {}", savedOrder.getId());

        return orderMapper.toRestaurantOrderResponse(savedOrder);
    }

    // Helper method to calculate total amount excluding cancelled items
    private BigDecimal calculateTotalAmount(RestaurantOrder order) {
        return order.getOrderItems().stream()
                .filter(item -> !OrderItemStatus.CANCELLED.name().equals(item.getStatus()))
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updateOrderItems(RestaurantOrder existingOrder, Set<OrderItemRequest> newOrderItemRequests) {
        // Get current order items
        Set<OrderItem> currentOrderItems = existingOrder.getOrderItems();

        Map<Integer, OrderItemRequest> newItemsMap = newOrderItemRequests.stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::getMenuItemId,
                        request -> request
                ));

        // Track items to remove
        Set<OrderItem> itemsToRemove = new HashSet<>();

        // 1. Update existing items or mark for removal
        for (OrderItem currentItem : currentOrderItems) {
            Integer menuItemId = currentItem.getMenuItem().getId();
            OrderItemRequest newItemRequest = newItemsMap.get(menuItemId);

            if (newItemRequest != null) {
                // Only update if item is not cancelled
                if (!OrderItemStatus.CANCELLED.name().equals(currentItem.getStatus())) {
                    // Update existing item
                    orderMapper.updateOrderItem(currentItem, newItemRequest);

                    // Recalculate price
                    BigDecimal newPrice = currentItem.getMenuItem().getPrice()
                            .multiply(BigDecimal.valueOf(newItemRequest.getQuantity()));
                    currentItem.setPrice(newPrice);

                    log.debug("Updated order item for menu item ID: {}", menuItemId);
                } else {
                    log.debug("Skipped updating cancelled order item for menu item ID: {}", menuItemId);
                }
            } else {
                // Only remove if item is not cancelled (keep cancelled items for record)
                if (!OrderItemStatus.CANCELLED.name().equals(currentItem.getStatus())) {
                    itemsToRemove.add(currentItem);
                    log.debug("Marked order item for removal, menu item ID: {}", menuItemId);
                }
            }
        }

        // 2. Remove items that are no longer in the request (except cancelled ones)
        if (!itemsToRemove.isEmpty()) {
            currentOrderItems.removeAll(itemsToRemove);
            orderItemRepository.deleteAll(itemsToRemove);
            log.info("Removed {} order items", itemsToRemove.size());
        }

        // 3. Add new items
        Set<Integer> currentMenuItemIds = currentOrderItems.stream()
                .filter(item -> !OrderItemStatus.CANCELLED.name().equals(item.getStatus()))
                .map(item -> item.getMenuItem().getId())
                .collect(Collectors.toSet());

        for (OrderItemRequest newItemRequest : newOrderItemRequests) {
            if (!currentMenuItemIds.contains(newItemRequest.getMenuItemId())) {
                // Create new order item
                OrderItem newOrderItem = orderMapper.toOrderItem(newItemRequest);

                // Set menu item
                MenuItem menuItem = menuItemRepository.findById(newItemRequest.getMenuItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_EXISTED));

                // Check if menu item is available
                if (!MenuItemStatus.AVAILABLE.name().equals(menuItem.getStatus())) {
                    throw new AppException(ErrorCode.MENU_ITEM_NOT_AVAILABLE);
                }

                newOrderItem.setMenuItem(menuItem);

                // Calculate price
                BigDecimal totalPrice = menuItem.getPrice()
                        .multiply(BigDecimal.valueOf(newItemRequest.getQuantity()));
                newOrderItem.setPrice(totalPrice);

                // Set relationships
                newOrderItem.setOrder(existingOrder);

                // Set status as ACTIVE for new items
                newOrderItem.setStatus(OrderItemStatus.PENDING.name());

                // Add to order
                currentOrderItems.add(newOrderItem);

                log.debug("Added new order item for menu item ID: {}", newItemRequest.getMenuItemId());
            }
        }

        log.info("Order items updated. Total items: {}", currentOrderItems.size());
    }

    // Helper method to update order status
    public RestaurantOrderResponse updateOrderStatus(Integer orderId, String newStatus) {
        log.info("Updating order status. Order ID: {}, New Status: {}", orderId, newStatus);

        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // Validate status transition (optional business logic)
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        if(newStatus.equals(OrderStatus.CANCELLED.name())) {
            // If order is cancelled, set all order items to cancelled
            order.getOrderItems().forEach(item -> item.setStatus(OrderItemStatus.CANCELLED.name()));
            orderItemRepository.saveAll(order.getOrderItems());

            // Recalculate total amount after cancelling items
            BigDecimal newTotalAmount = calculateTotalAmount(order);
            order.setTotalAmount(newTotalAmount);
        }

        RestaurantOrder savedOrder = orderRepository.save(order);

        return orderMapper.toRestaurantOrderResponse(savedOrder);
    }

    // Method to cancel specific order item
    public RestaurantOrderResponse cancelOrderItem(Integer orderId, Integer orderItemId, String reason) {
        log.info("Cancelling order item. Order ID: {}, Item ID: {}", orderId, orderItemId);

        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        OrderItem orderItem = order.getOrderItems().stream()
                .filter(item -> item.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_EXISTED));

        // Cancel the item
        orderItem.setStatus(OrderItemStatus.CANCELLED.name());

        orderItemRepository.save(orderItem);

        // Recalculate total amount
        BigDecimal newTotalAmount = calculateTotalAmount(order);
        order.setTotalAmount(newTotalAmount);

        RestaurantOrder savedOrder = orderRepository.save(order);

        log.info("Order item cancelled successfully. New total amount: {}", newTotalAmount);

        return orderMapper.toRestaurantOrderResponse(savedOrder);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        Map<String, Set<String>> validTransitions = Map.of(
                OrderStatus.PENDING.name(), Set.of(OrderStatus.CONFIRMED.name(), OrderStatus.CANCELLED.name()),
                OrderStatus.CONFIRMED.name(), Set.of(OrderStatus.COMPLETED.name()),
                OrderStatus.COMPLETED.name(), Set.of(), // No transitions from completed
                OrderStatus.CANCELLED.name(), Set.of()  // No transitions from cancelled
        );

        Set<String> allowedTransitions = validTransitions.getOrDefault(currentStatus, Set.of());

        if (!allowedTransitions.contains(newStatus)) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    public List<RestaurantOrderResponse> getOrdersByStatus(String status) {
        log.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(orderMapper::toRestaurantOrderResponse)
                .toList();
    }

}
