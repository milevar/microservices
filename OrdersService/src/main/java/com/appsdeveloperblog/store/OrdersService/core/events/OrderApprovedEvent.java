package com.appsdeveloperblog.store.OrdersService.core.events;

import com.appsdeveloperblog.store.OrdersService.core.model.OrderStatus;
import lombok.Value;

@Value
public class OrderApprovedEvent {

    private final String orderId;
    private final OrderStatus orderStatus = OrderStatus.APPROVED;
}
