package com.appsdeveloperblog.store.OrdersService.saga;

import com.appsdeveloperblog.store.OrdersService.command.commands.ApproveOrderCommand;
import com.appsdeveloperblog.store.OrdersService.command.commands.RejectOrderCommand;
import com.appsdeveloperblog.store.OrdersService.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.store.OrdersService.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.store.OrdersService.core.events.OrderRejectedEvent;
import com.appsdeveloperblog.store.OrdersService.core.model.OrderSummary;
import com.appsdeveloperblog.store.OrdersService.query.FindOrderQuery;
import com.appsdeveloperblog.store.core.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.store.core.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.store.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.store.core.events.PaymentProcessedEvent;
import com.appsdeveloperblog.store.core.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.store.core.events.ProductReservedEvent;
import com.appsdeveloperblog.store.core.model.User;
import com.appsdeveloperblog.store.core.query.FetchUserPaymentDetailsQuery;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Saga
public class OrderSaga {
    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;


    private transient QueryUpdateEmitter queryUpdateEmitter;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

    private final String PAYMENT_PROCESSING_TIMEOUT_DEADLINE = "payment-processing-deadline";

    private String scheduleId;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {

        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

        LOGGER.info("OrderCreatedEvent handled for orderId 123: " + reserveProductCommand.getOrderId() +
                "and productId 321: " + reserveProductCommand.getProductId());

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {
            @Override
            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage,
                                 CommandResultMessage<? extends Object> commandResultMessage) {
                if(commandResultMessage.isExceptional()) {
                    // Start Compensation
                    RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(orderCreatedEvent.getOrderId(),
                            commandResultMessage.exceptionResult().getMessage());
                }

            }
        });

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {
        //Process user payment

        LOGGER.info("ProductReservedEvent handled for orderId: " + productReservedEvent.getOrderId() +
                "and productId: " + productReservedEvent.getProductId());

        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery =
                new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());

        User userPaymentsDetails = null;
        try {
            userPaymentsDetails  = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception ex){
            LOGGER.error(ex.getMessage());
            // Start Compensation
            cancelProductReservation(productReservedEvent, ex.getMessage());
            return;
        }

        if(userPaymentsDetails == null) {
            // Start Compensation
            cancelProductReservation(productReservedEvent, "Could not fetch user payment details");
            return;
        }
        LOGGER.info("Succecssfully fetched user payment details for user: " +
                userPaymentsDetails.getFirstName());

        scheduleId = deadlineManager.schedule(Duration.of(120, ChronoUnit.SECONDS), PAYMENT_PROCESSING_TIMEOUT_DEADLINE, productReservedEvent);

        // Falla a proposito
        // if(true) return;
        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(userPaymentsDetails.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result = null;
        try {
            result = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            // Start Compensation
            cancelProductReservation(productReservedEvent, ex.getMessage());
        }

        if (result == null) {
            LOGGER.info("The ProcessPaymentCommand resulted in null. Initiating compensating transaction");
            // Start Compensation
            cancelProductReservation(productReservedEvent, "Could not process user payment with provided payment details");
        }
    }


    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {
        cancelDeadline();

        CancelProductReservationCommand publishProductReservationCommand = CancelProductReservationCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .productId(productReservedEvent.getProductId())
                .quantity(productReservedEvent.getQuantity())
                .userId(productReservedEvent.getUserId())
                .reason(reason)
                .build();

        commandGateway.send(publishProductReservationCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {
        cancelDeadline();

        // Send approveOrder
        ApproveOrderCommand approveOrderCommand =
                new ApproveOrderCommand(paymentProcessedEvent.getOrderId());

        commandGateway.send(approveOrderCommand);
    }

    private void cancelDeadline() {
        if (scheduleId != null) {
            deadlineManager.cancelSchedule(PAYMENT_PROCESSING_TIMEOUT_DEADLINE, scheduleId);
            scheduleId = null;
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        LOGGER.info("Order is approved. Order saga is orderId " + orderApprovedEvent.getOrderId());
        //SagaLifecycle.end();

        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true,
                new OrderSummary(orderApprovedEvent.getOrderId(),
                        orderApprovedEvent.getOrderStatus(),
                        ""));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent productReservationCancelledEvent) {
       // create and send a RejectedOrderCommand
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(productReservationCancelledEvent.getOrderId(),
                productReservationCancelledEvent.getReason());

        commandGateway.send(rejectOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent) {
        LOGGER.info("Successfully rejected order with Id: " + orderRejectedEvent.getOrderId());
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true,
                new OrderSummary(orderRejectedEvent.getOrderId(),
                        orderRejectedEvent.getOrderStatus(),
                        orderRejectedEvent.getReason()));


    }

    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_TIMEOUT_DEADLINE)
    public void handlePaymentDeadline(ProductReservedEvent productReservedEvent) {
        LOGGER.info("Payment processing  deadline took place. Sending a compensationn command to cancel the payment");
        cancelProductReservation(productReservedEvent, "Payment timeout");
    }
}
