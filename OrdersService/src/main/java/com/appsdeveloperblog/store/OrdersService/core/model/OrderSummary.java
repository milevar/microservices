package com.appsdeveloperblog.store.OrdersService.core.model;

import com.appsdeveloperblog.store.OrdersService.core.data.OrderEntity;
import lombok.Value;

@Value
public class OrderSummary {

    private final String orderId;
    private final OrderStatus orderStatus;
    private final String reason;
}
