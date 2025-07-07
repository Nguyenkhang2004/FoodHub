package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.OrderItemRequest;
import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.NotificationResponse;
import com.example.FoodHub.dto.response.PaymentResponse;
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
import com.example.FoodHub.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
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
    MenuItemRepository menuItemRepository;
    PaymentRepository paymentRepository;
    PaymentMapper paymentMapper;
    PayOSUtils payOSUtils;
    NotificationService notificationService;

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
                OrderSpecifications.filterOrders(status, tableId),
                pageable
        );
        return orders.map(orderMapper::toRestaurantOrderResponse);
    }

    public Page<RestaurantOrderResponse> getWaiterWorkShiftOrders(
            String area,
            String status,
            String tableNumber,
            Instant startTime,
            Pageable pageable) {

        log.info("Fetching orders for area: {}, status: {}, tableId: {}, startTime: {}",
                area, status, tableNumber, startTime);

        // Use OrderSpecifications to filter orders
        Page<RestaurantOrder> orders = orderRepository.findAll(
                OrderSpecifications.filterWaiterOrders(
                        status, tableNumber, area, startTime
                ),
                pageable
        );

        return orders.map(orderMapper::toRestaurantOrderResponse);
    }


    public Page<RestaurantOrderResponse> getChefWorkShiftOrders(
            String status,
            String tableNumber,
            Instant startTime,
            Pageable pageable) {

        log.info("Fetching orders for status: {}, tableId: {}, startTime: {}",
                status, tableNumber, startTime);


        // Use OrderSpecifications to filter orders
        Page<RestaurantOrder> orders = orderRepository.findAll(
                OrderSpecifications.filterChefOrders(
                        status, tableNumber, startTime
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


    public RestaurantOrderResponse getOrdersByOrderId(Integer id) {
        log.info("Fetching orders for order ID: {}", id);
        return orderRepository.findById(id)
                .map(orderMapper::toRestaurantOrderResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
    }
    @Transactional
    public RestaurantOrderResponse createOrder(RestaurantOrderRequest request) {

        log.info("Creating new order for table: {}", request.getTableId());

        RestaurantOrder order = orderMapper.toRestaurantOrder(request);

        // gán User (nếu có)
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            order.setUser(user);
        }

        // gán bàn nếu DINE_IN
        if (OrderType.DINE_IN.name().equals(request.getOrderType())) {
            RestaurantTable table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
            if (!TableStatus.AVAILABLE.name().equals(table.getStatus())) {
                throw new AppException(ErrorCode.TABLE_NOT_AVAILABLE);
            }
            table.setStatus(TableStatus.OCCUPIED.name());
            order.setTable(table);
        }

        // tạo OrderItems và tính total
        Set<OrderItem> orderItems = request.getOrderItems().stream()
                .map(itemReq -> {
                    MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                            .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_EXISTED));
                    if (!MenuItemStatus.AVAILABLE.name().equals(menuItem.getStatus())) {
                        throw new AppException(ErrorCode.MENU_ITEM_NOT_AVAILABLE);
                    }
                    OrderItem orderItem = orderMapper.toOrderItem(itemReq);
                    orderItem.setOrder(order);
                    orderItem.setMenuItem(menuItem);
                    orderItem.setPrice(menuItem.getPrice()
                            .multiply(BigDecimal.valueOf(itemReq.getQuantity())));
                    return orderItem;
                })
                .collect(Collectors.toSet());

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        order.setCreatedAt(TimeUtils.getNowInVietNam());
        orderRepository.save(order);               // order & id
        orderItems.forEach(orderItemRepository::save);

        if (OrderType.DELIVERY.name().equals(request.getOrderType())
                || OrderType.TAKEAWAY.name().equals(request.getOrderType())) {

            Payment payment = createPaymentForOrder(order, request.getPayment());
            order.setPayment(payment);
        }
        notificationService.notifyOrderEvent(order, NotificationType.NEW_ORDER.name());
        return orderMapper.toRestaurantOrderResponse(order);
    }

    private Payment createPaymentForOrder(RestaurantOrder order, PaymentRequest paymentRequest) {
        Payment payment = paymentMapper.toPayment(paymentRequest);
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setCreatedAt(Instant.now());

        boolean isCash = PaymentMethod.CASH.name().equals(paymentRequest.getPaymentMethod());
        payment.setStatus(isCash ? PaymentStatus.UNPAID.name() : PaymentStatus.PENDING.name());

        if (!isCash) {
            String paymentUrl = payOSUtils.generatePaymentUrl(payment);
            String transactionId = payOSUtils.getTransactionId(paymentUrl);
            payment.setPaymentUrl(paymentUrl);
            payment.setTransactionId(transactionId);
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public RestaurantOrderResponse addItemsToOrder(Integer orderId, RestaurantOrderRequest request) {
        // 1. Tìm order hiện tại
        RestaurantOrder existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // 2. Kiểm tra trạng thái order có thể thêm món không
        if (OrderStatus.CANCELLED.name().equals(existingOrder.getStatus())
                || paymentRepository.existsByOrderIdAndStatus(orderId, PaymentStatus.PAID.name())) {
            throw new AppException(ErrorCode.ORDER_COMPLETED);
        }

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

        notificationService.notifyOrderEvent(savedOrder, NotificationType.ORDER_ITEM_ADDED.name());
        // 8. Return response
        return orderMapper.toRestaurantOrderResponse(savedOrder);
    }

    // Helper method to calculate total amount excluding cancelled items
    private BigDecimal calculateTotalAmount(RestaurantOrder order) {
        return order.getOrderItems().stream()
                .filter(item -> !OrderItemStatus.CANCELLED.name().equals(item.getStatus()))
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Helper method to update order status
    public RestaurantOrderResponse updateOrderStatus(Integer orderId, String newStatus, String note) {
        log.info("Updating order status. Order ID: {}, New Status: {}, note: {}", orderId, newStatus, note);

        // Lock the order to prevent concurrent modifications
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // Validate status transition for the order
        validateStatusTransition(order.getStatus(), newStatus);

        // Set new status for the order
        order.setStatus(newStatus);

        // Nếu là CANCELLED và là TAKEAWAY hoặc DELIVERY thì huỷ luôn payment nếu có
        if (OrderStatus.CANCELLED.name().equals(newStatus)
                && (OrderType.DELIVERY.name().equals(order.getOrderType())
                || OrderType.TAKEAWAY.name().equals(order.getOrderType()))) {

            Payment payment = order.getPayment();
            if (payment != null && !PaymentStatus.PAID.name().equals(payment.getStatus())) {
                log.info("Cancelling payment for order ID: {}", orderId);
                payment.setStatus(PaymentStatus.CANCELLED.name());
                payment.setUpdatedAt(Instant.now());
                paymentRepository.save(payment);
            }
        }


        // Update status of order items, excluding those in CANCELLED or COMPLETED status
        List<OrderItem> updatedItems = order.getOrderItems().stream()
                .filter(item -> !OrderItemStatus.CANCELLED.name().equals(item.getStatus()) &&
                        !OrderItemStatus.COMPLETED.name().equals(item.getStatus()))
                .collect(Collectors.toList());

        updatedItems.forEach(item -> {
            validateStatusTransition(item.getStatus(), newStatus);
            item.setStatus(newStatus);
        });

        orderItemRepository.saveAll(updatedItems);

        // Recalculate total amount
        BigDecimal newTotalAmount = calculateTotalAmount(order);
        order.setTotalAmount(newTotalAmount);
        order.setUpdatedAt(TimeUtils.getNowInVietNam());
        // Update order status based on order items
        updateOrderStatusBasedOnItems(order);
        if(OrderStatus.CANCELLED.name().equals(newStatus) && note != null && !note.isEmpty()) {
            order.setNote(note);
        }
        RestaurantOrder savedOrder = orderRepository.save(order);
        if(newStatus.equals(OrderStatus.READY.name())) {
            notificationService.notifyOrderEvent(savedOrder, NotificationType.ORDER_READY.name());
        }
        return orderMapper.toRestaurantOrderResponse(savedOrder);
    }

    @Transactional
    public RestaurantOrderResponse updateOrderItemStatus(Integer orderItemId, String newStatus, String note) {
        log.info("Updating order item status. Order Item ID: {}, New Status: {}", orderItemId, newStatus);

        // Lock the order to prevent concurrent modifications
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_EXISTED));
        RestaurantOrder order = orderRepository.findById(orderItem.getOrder().getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // Validate status transition for the order item
        validateStatusTransition(orderItem.getStatus(), newStatus);
        orderItem.setStatus(newStatus);
        if(OrderItemStatus.CANCELLED.name().equals(newStatus) && note != null && !note.isEmpty()) {
            orderItem.setNote(note);
        }
        orderItemRepository.save(orderItem);

        // Recalculate total amount if necessary
        BigDecimal newTotalAmount = calculateTotalAmount(order);
        order.setTotalAmount(newTotalAmount);
        order.setUpdatedAt(TimeUtils.getNowInVietNam());
        // Update order status based on order items
        updateOrderStatusBasedOnItems(order);
        RestaurantOrder savedOrder = orderRepository.save(order);
        if(newStatus.equals(OrderStatus.READY.name())) {
            notificationService.notifyOrderEvent(savedOrder, NotificationType.ORDER_ITEM_READY.name());
        }
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
                "PENDING", Set.of("CONFIRMED", "CANCELLED"),
                "CONFIRMED", Set.of("PREPARING", "CANCELLED"),
                "PREPARING", Set.of("READY"),
                "READY", Set.of("COMPLETED"),
                "COMPLETED", Set.of(), // No transitions from completed
                "CANCELLED", Set.of()  // No transitions from cancelled
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


}
