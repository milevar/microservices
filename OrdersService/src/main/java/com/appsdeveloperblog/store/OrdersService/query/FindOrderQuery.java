package com.appsdeveloperblog.store.OrdersService.query;

import lombok.Value;

@Value
public class FindOrderQuery {
    private final String orderId;
}
