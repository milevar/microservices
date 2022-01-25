package com.appsdeveloperblog.store.core.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FetchUserPaymentDetailsQuery {

    private String userId;
}
