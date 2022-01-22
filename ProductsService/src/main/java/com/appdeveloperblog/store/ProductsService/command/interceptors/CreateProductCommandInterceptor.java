package com.appdeveloperblog.store.ProductsService.command.interceptors;

import com.appdeveloperblog.store.ProductsService.command.CreateProductCommand;
import com.appdeveloperblog.store.ProductsService.core.data.ProductLookupEntity;
import com.appdeveloperblog.store.ProductsService.core.data.ProductLookupRepository;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;

@Component
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    private final ProductLookupRepository productLookupRepository;
    private static  final Logger LOGGER = LoggerFactory.getLogger(CreateProductCommandInterceptor.class);

    public CreateProductCommandInterceptor(ProductLookupRepository productLookupRepository) {
        this.productLookupRepository = productLookupRepository;
    }

    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
            List<? extends CommandMessage<?>> messages) {
        return (index, command) -> {

            LOGGER.info("Intercepted command: " + command.getPayloadType());

            if (CreateProductCommand.class.equals(command.getPayloadType())) {
                CreateProductCommand createProductCommand = (CreateProductCommand)command.getPayload();
//                if (createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
//                    throw new IllegalArgumentException("Price cannot be less or equal than zero");
//                }
//                if (createProductCommand.getTitle() == null || createProductCommand.getTitle().isBlank()) {
//                    throw new IllegalArgumentException("Title cannot be empty");
//                }

                ProductLookupEntity productLookupEntity = productLookupRepository.findByProductIdOrTitle(createProductCommand.getProductId(),
                        createProductCommand.getTitle());

                if (productLookupEntity != null) {
                    throw new IllegalStateException(
                            String.format("Product with productId %s or title %s already exist",
                                    createProductCommand.getProductId(), createProductCommand.getTitle())
                    );
                }

            }

            return command;
        };
    }
}
