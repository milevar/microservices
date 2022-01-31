package com.appsdeveloperblog.store.PaymentsService.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "payments")
public class PaymentEntity implements Serializable {

    private static final long serialVersionUID = 4270620825942752834L;

    @Id
    private String paymentId;
    @Column
    public String orderId;
}
