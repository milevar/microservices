package com.appdeveloperblog.store.ProductsService.command.rest;

import com.appdeveloperblog.store.ProductsService.command.CreateProductCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductsCommandController {

    private final Environment env;
    private final CommandGateway commandGateway;

    @Autowired
    public ProductsCommandController(Environment env, CommandGateway commandGateway) {
        this.env = env;
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public String createProduct(@Valid @RequestBody CreateProductRestModel createProductRestModel) {
        CreateProductCommand createProductCommand = CreateProductCommand.builder()
                .price(createProductRestModel.getPrice())
                .quantity(createProductRestModel.getQuantity())
                .title(createProductRestModel.getTitle())
                .productId(UUID.randomUUID().toString()).build();

        String returnValue;

        returnValue = commandGateway.sendAndWait(createProductCommand);

//        try{
//            returnValue = commandGateway.sendAndWait(createProductCommand);
//
//        } catch (Exception ex) {
//            returnValue = ex.getLocalizedMessage();
//        }

        return returnValue;
    }

//    @GetMapping
//    public String getProduct(){
//        return "HTTP GET Handled";
//    }
//
//    @PutMapping
//    public String updateProduct(){
//        return "HTTP PUT Handled" + env.getProperty("local.server.port");
//    }
//
//    @DeleteMapping
//    public String deleteProduct(){
//        return "HTTP DELETE Handled";
//    }
}
