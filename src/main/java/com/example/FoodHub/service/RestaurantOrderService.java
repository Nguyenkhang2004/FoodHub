package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.OrderItemRequest;
import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.OrderItemResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.entity.*;
import com.example.FoodHub.enums.*;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.PaymentMapper;
import com.example.FoodHub.mapper.RestaurantOrderMapper;
import com.example.FoodHub.repository.*;
import com.example.FoodHub.specification.OrderSpecifications;
import com.example.FoodHub.utils.PayOSUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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
    PaymentRepository paymentRepository;
    PaymentMapper paymentMapper;
    PayOSUtils payOSUtils;
    private final ErrorPageRegistrar errorPageRegistrar;

    public Page<RestaurantOrderResponse> getAllOrders(
            String status, String tableNumber, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {

        log.info("Fetching all orders with status: {}, tableNumber: {}, minPrice: {}, maxPrice: {}",
                status, tableNumber, minPrice, maxPrice);

        Integer tableId = null;
        if (tableNumber != null && !tableNumber.isEmpty()) {
            RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
            tableId = table.getId();
        }

        Page<RestaurantOrder> orders = orderRepository.findAll(
                OrderSpecifications.filterOrders(status, tableId, minPrice, maxPrice),
                pageable
        );
        return orders.map(orderMapper::toRestaurantOrderResponse);
    }

    public Page<RestaurantOrderResponse> getMyWorkShiftOrders(
            String area,
            String status,
            String tableNumber,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String startTime,
            Pageable pageable) {

        log.info("Fetching orders for area: {}, status: {}, tableId: {}, minPrice: {}, maxPrice: {}, startTime: {}",
                area, status, tableNumber, minPrice, maxPrice, startTime);

        // Convert startTime string "HH:mm" to LocalDateTime of today
        LocalDateTime startTimeLocal = null;
        if (startTime != null && !startTime.trim().isEmpty()) {
            try {
                // Parse time string "08:30" to LocalTime
                LocalTime time = LocalTime.parse(startTime.trim(), DateTimeFormatter.ofPattern("HH:mm"));

                // Combine with today's date to create LocalDateTime
                startTimeLocal = LocalDateTime.of(LocalDate.now(), time);

                log.info("Parsed startTime: {} to LocalDateTime: {}", startTime, startTimeLocal);
            } catch (DateTimeParseException e) {
                log.warn("Invalid startTime format: {}. Expected format: HH:mm (e.g., 08:30)", startTime);
                throw new AppException(ErrorCode.INVALID_TIME_FORMAT);
            }
        }

        // Use OrderSpecifications to filter orders
        Page<RestaurantOrder> orders = orderRepository.findAll(
                OrderSpecifications.filterWorkShiftOrders(
                        area, status, tableNumber, minPrice, maxPrice, startTimeLocal
                ),
                pageable
        );

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
        if (request.getOrderType().equals(OrderType.DINE_IN.name())) {
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

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
        orderItems.forEach(orderItemRepository::save);

        if (OrderType.DELIVERY.name().equals(request.getOrderType())
                || OrderType.TAKEAWAY.name().equals(request.getOrderType())) {

            Payment payment = paymentMapper.toPayment(request.getPayment());
            payment.setOrder(order);
            payment.setAmount(totalAmount);
            payment.setCreatedAt(Instant.now());

            boolean isCash = PaymentMethod.CASH.name()
                    .equals(request.getPayment().getPaymentMethod());
            payment.setStatus(isCash ? PaymentStatus.UNPAID.name()
                    : PaymentStatus.PENDING.name());
            if (!isCash) {
                String paymentUrl = payOSUtils.generatePaymentUrl(payment);
                log.info("Generating payment url: {}", paymentUrl);
                String transactionId = payOSUtils.getTransactionId(paymentUrl);
                log.info("Generating transaction id: {}", transactionId);
                payment.setPaymentUrl(paymentUrl);
                payment.setTransactionId(transactionId);
            } else {
                log.info("Payment method is CASH, no payment URL generated.");
            }
            paymentRepository.save(payment);
            order.setPayment(payment);
        }
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

        // Lock the order to prevent concurrent modifications
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // Validate status transition (optional business logic)
        validateStatusTransition(order.getStatus(), newStatus);

        // Set new status for the order
        order.setStatus(newStatus);

        order.getOrderItems().forEach(item -> item.setStatus(newStatus));
        orderItemRepository.saveAll(order.getOrderItems());

        // Recalculate total amount after cancelling items
        BigDecimal newTotalAmount = calculateTotalAmount(order);
        order.setTotalAmount(newTotalAmount);


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
        order.setUpdatedAt(Instant.now());
        // Update order status based on order items
        updateOrderStatusBasedOnItems(order);

        RestaurantOrder savedOrder = orderRepository.save(order);

        log.info("Order item cancelled successfully. New total amount: {}", newTotalAmount);

        return orderMapper.toRestaurantOrderResponse(savedOrder);
    }

    private void updateOrderStatusBasedOnItems(RestaurantOrder order) {
        Set<OrderItem> items = order.getOrderItems();
        boolean allCancelled = items.stream().allMatch(item -> OrderItemStatus.CANCELLED.name().equals(item.getStatus()));
        boolean allCompleted = items.stream().allMatch(item -> OrderItemStatus.COMPLETED.name().equals(item.getStatus()));

        if (allCancelled) {
            order.setStatus(OrderStatus.CANCELLED.name());
            order.setTotalAmount(BigDecimal.ZERO);
        } else if (allCompleted) {
            order.setStatus(OrderStatus.COMPLETED.name());
        } else {
            // Ensure order status is valid based on items
            Set<String> itemStatuses = items.stream()
                    .map(OrderItem::getStatus)
                    .collect(Collectors.toSet());
            if (itemStatuses.contains(OrderItemStatus.PREPARING.name())) {
                order.setStatus(OrderStatus.PREPARING.name());
            } else if (itemStatuses.contains(OrderItemStatus.READY.name())) {
                order.setStatus(OrderStatus.READY.name());
            } else if (itemStatuses.contains(OrderItemStatus.CONFIRMED.name())) {
                order.setStatus(OrderStatus.CONFIRMED.name());
            }
        }
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

    public Page<RestaurantOrderResponse> getCompletedOrders(
            String period,
            Instant startDate,
            Instant endDate,
            String search,
            Pageable pageable) {
        Instant start = getStartDate(period, startDate);
        Instant end = getEndDate(period, endDate);

        log.info("Fetching completed orders with period: {}, start: {}, end: {}, search: {}", period, start, end, search);

        Page<RestaurantOrder> orders = orderRepository.findByStatusAndCreatedAtBetween(
                "COMPLETED",
                start,
                end,
                search,
                pageable);
        return orders.map(orderMapper::toRestaurantOrderResponsePaidOnly);
    }

    public Page<RestaurantOrderResponse> getCompletedOrdersFiltered(
            String period,
            Instant startDate,
            Instant endDate,
            String orderType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String paymentMethod,
            String search,
            Pageable pageable) {
        Instant start = getStartDate(period, startDate);
        Instant end = getEndDate(period, endDate);

        log.info("Fetching filtered completed orders with period: {}, start: {}, end: {}, orderType: {}, minPrice: {}, maxPrice: {}, paymentMethod: {}, search: {}",
                period, start, end, orderType, minPrice, maxPrice, paymentMethod, search);

        Page<RestaurantOrder> orders = orderRepository.findCompletedOrdersFiltered(
                start,
                end,
                orderType,
                minPrice,
                maxPrice,
                paymentMethod,
                search,
                pageable);
        return orders.map(orderMapper::toRestaurantOrderResponsePaidOnly);
    }

    public Map<String, Object> getOrderSummary(String period, Instant startDate, Instant endDate) {
        Instant start = getStartDate(period, startDate);
        Instant end = getEndDate(period, endDate);

        log.info("Fetching order summary with period: {}, start: {}, end: {}", period, start, end);

        List<Object[]> summary = orderRepository.countOrdersByDateMySQL("COMPLETED", start, end);
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");

        List<String> labels = summary.stream()
                .map(row -> {
                    LocalDate date;
                    if (row[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) row[0]).toLocalDate();
                    } else if (row[0] instanceof LocalDate) {
                        date = (LocalDate) row[0];
                    } else {
                        try {
                            date = LocalDate.parse(row[0].toString(), DateTimeFormatter.ISO_LOCAL_DATE);
                        } catch (Exception e) {
                            log.error("Invalid date format: {}, using fallback parsing", row[0], e);
                            date = LocalDate.parse(row[0].toString());
                        }
                    }
                    return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                })
                .collect(Collectors.toList());

        List<Long> quantities = summary.stream()
                .map(row -> {
                    if (row[1] instanceof Long) {
                        return (Long) row[1];
                    } else if (row[1] instanceof Integer) {
                        return ((Integer) row[1]).longValue();
                    } else {
                        try {
                            return Long.parseLong(row[1].toString());
                        } catch (NumberFormatException e) {
                            log.error("Invalid quantity format: {}, returning 0", row[1], e);
                            return 0L;
                        }
                    }
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("quantities", quantities);
        return result;
    }

    private Instant getStartDate(String period, Instant startDate) {
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDate today = LocalDate.now(zoneId);

        if ("specific".equals(period) && startDate != null) {
            LocalDateTime localDateTime = startDate.atZone(zoneId).toLocalDateTime();
            Instant adjustedStart = localDateTime.toLocalDate().atStartOfDay(utcZone).toInstant();
            log.info("Specific period - Received startDate: {}, Adjusted startDate: {}", startDate, adjustedStart);
            return adjustedStart;
        }

        switch (period != null ? period.toLowerCase() : "month") {
            case "today":
                Instant startToday = today.atStartOfDay(utcZone).toInstant();
                log.info("Today period - Start: {}", startToday);
                return startToday;
            case "week":
                LocalDate mondayOfCurrentWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                Instant startWeek = mondayOfCurrentWeek.atStartOfDay(utcZone).toInstant();
                log.info("Week period - Start: {}", startWeek);
                return startWeek;
            case "month":
                Instant startMonth = today.withDayOfMonth(1).atStartOfDay(utcZone).toInstant();
                log.info("Month period - Start: {}", startMonth);
                return startMonth;
            case "year":
                Instant startYear = today.withDayOfYear(1).atStartOfDay(utcZone).toInstant();
                log.info("Year period - Start: {}", startYear);
                return startYear;
            default:
                Instant defaultStart = today.minusDays(30).atStartOfDay(utcZone).toInstant();
                log.info("Default period - Start: {}", defaultStart);
                return defaultStart;
        }
    }

    private Instant getEndDate(String period, Instant endDate) {
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDate today = LocalDate.now(zoneId);


        if ("specific".equals(period) && endDate != null) {
            LocalDateTime localDateTime = endDate.atZone(zoneId).toLocalDateTime();
            Instant adjustedStart = localDateTime.toLocalDate().atStartOfDay(utcZone).toInstant();
            log.info("Specific period - Received startDate: {}, Adjusted startDate: {}", endDate, adjustedStart);
            return adjustedStart;
        }

        switch (period != null ? period.toLowerCase() : "month") {
            case "today":
                Instant endToday = today.plusDays(1).atStartOfDay(utcZone).toInstant();
                log.info("Today period - End: {}", endToday);
                return endToday;
            case "week":
                LocalDate sundayOfCurrentWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                Instant endWeek = sundayOfCurrentWeek.plusDays(1).atStartOfDay(utcZone).toInstant();
                log.info("Week period - End: {}", endWeek);
                return endWeek;
            case "month":
                Instant endMonth = today.withDayOfMonth(today.lengthOfMonth()).plusDays(1).atStartOfDay(utcZone).toInstant();
                log.info("Month period - End: {}", endMonth);
                return endMonth;
            case "year":
                Instant endYear = today.withDayOfYear(today.lengthOfYear()).plusDays(1).atStartOfDay(utcZone).toInstant();
                log.info("Year period - End: {}", endYear);
                return endYear;
            default:
                Instant defaultEnd = today.plusDays(1).atStartOfDay(utcZone).toInstant();
                log.info("Default period - End: {}", defaultEnd);
                return defaultEnd;
        }
    }
    @Transactional
    public RestaurantOrderResponse addItemsToOrder(Integer orderId, RestaurantOrderRequest request) {
        // 1. Tìm order hiện tại
        RestaurantOrder existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // 2. Kiểm tra trạng thái order có thể thêm món không
        if ("CANCELLED".equals(existingOrder.getStatus()) || "COMPLETED".equals(existingOrder.getStatus())) {
            throw new IllegalStateException("Cannot add items to cancelled or completed order");
        }

        // 3. Update thông tin order nếu có thay đổi (note, orderType...)
//        if (request.getNote() != null) {
//            existingOrder.setNote(request.getNote());
//        }
        if (request.getOrderType() != null) {
            existingOrder.setOrderType(request.getOrderType());
        }
        if (request.getStatus() != null) {
            existingOrder.setStatus(request.getStatus());
        }

        // 4. Lấy danh sách order items hiện tại
        Set<OrderItem> currentOrderItems = existingOrder.getOrderItems();
        Map<Integer, OrderItem> existingItemsMap = currentOrderItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getMenuItem().getId(),
                        item -> item
                ));

        // 5. Xử lý các items mới từ request
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            // Validate menu item exists
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_EXISTED));

            // Kiểm tra menu item có available không
            if (!MenuItemStatus.AVAILABLE.name().equals(menuItem.getStatus())) {
                throw new AppException(ErrorCode.MENU_ITEM_NOT_AVAILABLE);
            }

            // Kiểm tra nếu món đã tồn tại trong order
            if (existingItemsMap.containsKey(itemRequest.getMenuItemId())) {
                // Cộng thêm quantity vào món đã có
                OrderItem existingItem = existingItemsMap.get(itemRequest.getMenuItemId());
                int newQuantity = existingItem.getQuantity() + itemRequest.getQuantity();

                existingItem.setQuantity(newQuantity);
                // Tính lại price cho item hiện tại
                existingItem.setPrice(menuItem.getPrice().multiply(BigDecimal.valueOf(newQuantity)));

                // Update status nếu có
                if (itemRequest.getStatus() != null) {
                    existingItem.setStatus(itemRequest.getStatus());
                }
            } else {
                // Tạo order item mới
                OrderItem newOrderItem = orderMapper.toOrderItem(itemRequest);
                newOrderItem.setOrder(existingOrder);
                newOrderItem.setMenuItem(menuItem);
                // *** QUAN TRỌNG: Set price cho order item mới ***
                newOrderItem.setPrice(menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

                // Thêm vào danh sách order items hiện tại
                currentOrderItems.add(newOrderItem);

                // *** QUAN TRỌNG: Save order item mới vào database ***
                orderItemRepository.save(newOrderItem);
            }
        }

        // 6. Tính lại tổng tiền dựa trên price đã được set cho từng OrderItem
        BigDecimal totalAmount = currentOrderItems.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        existingOrder.setTotalAmount(totalAmount);
        existingOrder.setOrderItems(currentOrderItems);
        existingOrder.setUpdatedAt(Instant.now());

        // 7. Save order
        RestaurantOrder savedOrder = orderRepository.save(existingOrder);

        // 8. Return response
        return orderMapper.toRestaurantOrderResponse(savedOrder);
    }
    @Transactional
    public RestaurantOrderResponse updateOrderItemStatus(Integer orderItemId, String newStatus) {
        log.info("Updating order item status. Order Item ID: {}, New Status: {}", orderItemId, newStatus);

        // Lock the order to prevent concurrent modifications
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_EXISTED));
        RestaurantOrder order = orderRepository.findById(orderItem.getOrder().getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // Validate status transition for the order item
        validateStatusTransition(orderItem.getStatus(), newStatus);
        orderItem.setStatus(newStatus);
        orderItemRepository.save(orderItem);

        // Recalculate total amount if necessary
        BigDecimal newTotalAmount = calculateTotalAmount(order);
        order.setTotalAmount(newTotalAmount);
        order.setUpdatedAt(Instant.now());
        // Update order status based on order items
        updateOrderStatusBasedOnItems(order);

        RestaurantOrder savedOrder = orderRepository.save(order);
        return orderMapper.toRestaurantOrderResponse(savedOrder);
    }
}


