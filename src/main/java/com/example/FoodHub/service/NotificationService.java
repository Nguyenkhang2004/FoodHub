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
        String baseMessage = "Có đơn hàng mới #" + order.getId() + ", bàn " + order.getTable().getTableNumber();
        if (messageType.equals(NotificationType.ORDER_ITEM_ADDED.name())) {
            baseMessage = "Đơn hàng #" + order.getId() + " tại bàn " + order.getTable().getTableNumber() + " vừa thêm món mới";
        } else if (messageType.equals(NotificationType.ORDER_READY.name())) {
            baseMessage = "Đơn hàng #" + order.getId() + " tại bàn " + order.getTable().getTableNumber() + " đã sẵn sàng";
        } else if (messageType.equals(NotificationType.ORDER_ITEM_READY.name())) {
            baseMessage = "Một món trong đơn #" + order.getId()
                    + ", bàn " + order.getTable().getTableNumber() + " đã sẵn sàng, vui lòng phục vụ khách";
        } else if(messageType.equals(NotificationType.BANKING_COMPLETED.name())) {
            baseMessage = "Bàn " + order.getTable().getTableNumber()
                    + " đã thanh toán chuyển khoản thành công, vui lòng đến chụp lại thông tin thanh toán của khách hàng";
        } else if (messageType.equals(NotificationType.CUSTOMER_CALL_WAITER.name())) {
            baseMessage = "Khách hàng tại bàn " + order.getTable().getTableNumber() + " đã gọi phục vụ";
        }
        String topic = "/topic/waiter/area/" + order.getTable().getArea();
        log.info("topic: {}", topic);
        sendNotification(baseMessage, messageType, topic);
    }

    private void sendNotificationToChef(RestaurantOrder order, String messageType) {
        if (messageType.equals(NotificationType.ORDER_ITEM_ADDED.name())) {
            String message = "Đơn hàng #" + order.getId() + " tại bàn " + order.getTable().getTableNumber() + " vừa thêm món mới";
            sendNotification(message, messageType, "/topic/kitchen");
            return;
        }
        if(messageType.equals(NotificationType.NEW_ORDER.name())){
            boolean shouldNotifyChef = order.getOrderType().equals(OrderType.DINE_IN.name()) ||
                    (order.getOrderType().equals(OrderType.DELIVERY.name()) ||
                            order.getOrderType().equals(OrderType.TAKEAWAY.name())) &&
                            order.getPayment().getPaymentMethod().equals(PaymentMethod.CASH.name());

            if (shouldNotifyChef) {
                String message = "Có đơn hàng mới: #" + order.getId(); // hoặc "Đơn hàng #" + id + " vừa thêm món mới"
                sendNotification(message, messageType, "/topic/kitchen");
            }
        }
    }


}
