package com.appdeveloperblog.store.ProductsService.query;

import com.appdeveloperblog.store.ProductsService.core.data.ProductEntity;
import com.appdeveloperblog.store.ProductsService.core.data.ProductsRepository;
import com.appdeveloperblog.store.ProductsService.core.events.ProductCreatedEvent;
import com.appsdeveloperblog.store.core.events.ProductReservedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ProductEventsHandler {
    private final ProductsRepository productsRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventsHandler.class);

    public ProductEventsHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception exception) throws Exception {
        throw exception;
    }

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException exception) {
        // log error message
    }

    @EventHandler
    public void on(ProductCreatedEvent event) throws Exception {

        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event, productEntity);

        try {
            productsRepository.save(productEntity);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        if (true) throw new Exception("An error took place in ProductCreatedEvent @ProductCreatedEvent method");
    }

    @EventHandler
    public void on(ProductReservedEvent event) {
        ProductEntity productEntity = productsRepository.findByProductId(event.getProductId());
        productEntity.setQuantity(productEntity.getQuantity() - event.getQuantity());
        productsRepository.save(productEntity);

        LOGGER.info("ProductReservedEvent handled for orderId: " + event.getOrderId() +
                "and productId: " + event.getProductId());
    }
}
