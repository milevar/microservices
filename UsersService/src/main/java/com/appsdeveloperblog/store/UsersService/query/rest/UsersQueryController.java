package com.appsdeveloperblog.store.UsersService.query.rest;

import com.appsdeveloperblog.store.core.query.FetchUserPaymentDetailsQuery;
import com.appsdeveloperblog.store.core.model.User;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("users")
public class UsersQueryController {
    @Autowired
    QueryGateway queryGateway;

    @GetMapping("/{userId}/payment-details")
    public User getUserPaymentDetails(@PathVariable String userId) {

        //FetchUserPaymentDetailsQuery query = new FetchUserPaymentDetailsQuery(userId);

        FetchUserPaymentDetailsQuery query = FetchUserPaymentDetailsQuery.builder()
                .userId(userId)
                .build();

        return queryGateway.query(query, ResponseTypes.instanceOf(User.class)).join();
    }

}
