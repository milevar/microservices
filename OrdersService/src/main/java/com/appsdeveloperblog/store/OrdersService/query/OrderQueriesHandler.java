package com.appsdeveloperblog.store.OrdersService.query;

import com.appsdeveloperblog.store.OrdersService.core.data.OrderEntity;
import com.appsdeveloperblog.store.OrdersService.core.data.OrdersRepository;
import com.appsdeveloperblog.store.OrdersService.core.model.OrderSummary;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderQueriesHandler {

    OrdersRepository ordersRepository;

    public OrderQueriesHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery findOrderQuery) {
       OrderEntity orderEntity = ordersRepository.findByOrderId(findOrderQuery.getOrderId());
       return new OrderSummary(orderEntity.getOrderId(), orderEntity.getOrderStatus(), "");
    }

}
