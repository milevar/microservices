package com.appsdeveloperblog.store.OrdersService.core.events;

import com.appsdeveloperblog.store.OrdersService.core.model.OrderStatus;
import lombok.Value;

@Value
public class OrderRejectedEvent {

    private final String orderId;
    private final String reason;
    private final OrderStatus orderStatus = OrderStatus.REJECTED;
}
