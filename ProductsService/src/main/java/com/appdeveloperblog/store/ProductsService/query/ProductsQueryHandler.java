package com.appdeveloperblog.store.ProductsService.query;

import com.appdeveloperblog.store.ProductsService.core.data.ProductEntity;
import com.appdeveloperblog.store.ProductsService.core.data.ProductsRepository;
import com.appdeveloperblog.store.ProductsService.query.rest.ProductRestModel;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductsQueryHandler {

    private final ProductsRepository productsRepository;

    public ProductsQueryHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @QueryHandler
    public List<ProductRestModel> findProducts(FindProductsQuery query) {

        List<ProductRestModel> productRest = new ArrayList<>();
        List<ProductEntity> storeProducts = productsRepository.findAll();

        for (ProductEntity productEntity: storeProducts) {
            ProductRestModel productRestModel = new ProductRestModel();
            BeanUtils.copyProperties(productEntity, productRestModel);
            productRest.add(productRestModel);
        }
        return productRest;
    }
}
