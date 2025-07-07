package com.example.FoodHub.service;

import com.example.FoodHub.dto.response.NotificationResponse;
import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.enums.NotificationType;
import com.example.FoodHub.enums.OrderType;
import com.example.FoodHub.enums.PaymentMethod;
import com.example.FoodHub.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    SimpMessagingTemplate simpMessagingTemplate;

    public void sendNotification(String message, String type, String topic) {
        NotificationResponse notification = NotificationResponse.builder()
                .message(message)
                .timestamp(TimeUtils.getNowInVietNam())
                .type(type)
                .build();
        simpMessagingTemplate.convertAndSend(topic, notification);
    }

    public void notifyOrderEvent(RestaurantOrder order, String notificationType) {
        sendNotificationToChef(order, notificationType);
        sendNotificationToWaiter(order, notificationType);
    }

    private void sendNotificationToWaiter(RestaurantOrder order, String messageType) {
        // Kiểm tra xem order có table không
        String tableInfo = order.getTable() != null ? "bàn " + order.getTable().getTableNumber() : "đơn hàng " + order.getOrderType();
        String baseMessage = "Có đơn hàng mới #" + order.getId() + ", " + tableInfo;

        if (messageType.equals(NotificationType.ORDER_ITEM_ADDED.name())) {
            baseMessage = "Đơn hàng #" + order.getId() + " tại " + tableInfo + " vừa thêm món mới";
        } else if (messageType.equals(NotificationType.ORDER_READY.name())) {
            baseMessage = "Đơn hàng #" + order.getId() + " tại " + tableInfo + " đã sẵn sàng";
        } else if (messageType.equals(NotificationType.ORDER_ITEM_READY.name())) {
            baseMessage = "Một món trong đơn #" + order.getId() + ", " + tableInfo + " đã sẵn sàng, vui lòng phục vụ khách";
        } else if (messageType.equals(NotificationType.BANKING_COMPLETED.name())) {
            baseMessage = tableInfo + " đã thanh toán chuyển khoản thành công, vui lòng đến chụp lại thông tin thanh toán của khách hàng";
        } else if (messageType.equals(NotificationType.CUSTOMER_CALL_WAITER.name())) {
            baseMessage = "Khách hàng tại " + tableInfo + " đã gọi phục vụ";
        }

        // Chỉ gửi thông báo đến waiter cho DINE_IN hoặc các trường hợp phù hợp
        if (order.getTable() != null) {
            String topic = "/topic/waiter/area/" + order.getTable().getArea();
            log.info("topic: {}", topic);
            sendNotification(baseMessage, messageType, topic);
        } else {
            log.info("Skipping waiter notification for order #{} (type: {}), no table assigned", order.getId(), order.getOrderType());
        }
    }

    private void sendNotificationToChef(RestaurantOrder order, String messageType) {
        // Kiểm tra xem order có table không
        String tableInfo = order.getTable() != null ? "bàn " + order.getTable().getTableNumber() : "đơn hàng " + order.getOrderType();

        if (messageType.equals(NotificationType.ORDER_ITEM_ADDED.name())) {
            String message = "Đơn hàng #" + order.getId() + " tại " + tableInfo + " vừa thêm món mới";
            sendNotification(message, messageType, "/topic/kitchen");
            return;
        }

        if (messageType.equals(NotificationType.NEW_ORDER.name())) {
            boolean shouldNotifyChef = order.getOrderType().equals(OrderType.DINE_IN.name()) ||
                    (order.getOrderType().equals(OrderType.DELIVERY.name()) ||
                            order.getOrderType().equals(OrderType.TAKEAWAY.name())) &&
                            order.getPayment() != null &&
                            order.getPayment().getPaymentMethod().equals(PaymentMethod.CASH.name());

            if (shouldNotifyChef) {
                String message = "Có đơn hàng mới: #" + order.getId() + " (" + tableInfo + ")";
                sendNotification(message, messageType, "/topic/kitchen");
            }
        }
    }
}
