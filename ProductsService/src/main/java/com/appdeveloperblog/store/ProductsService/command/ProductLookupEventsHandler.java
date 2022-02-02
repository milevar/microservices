package com.appdeveloperblog.store.ProductsService.command;

import com.appdeveloperblog.store.ProductsService.core.data.ProductLookupEntity;
import com.appdeveloperblog.store.ProductsService.core.data.ProductLookupRepository;
import com.appdeveloperblog.store.ProductsService.core.events.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductLookupEventsHandler {

    private final ProductLookupRepository productLookupRepository;

    public ProductLookupEventsHandler(ProductLookupRepository productLookupRepository) {
        this.productLookupRepository = productLookupRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) {
        ProductLookupEntity productLookupEntity = new ProductLookupEntity(event.getProductId(),
                event.getTitle());

        productLookupRepository.save(productLookupEntity);
    }

    @ResetHandler
    public void reset() {
        productLookupRepository.deleteAll();
    }
}
