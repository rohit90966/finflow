package com.rohit.finflow.dto;

import com.rohit.finflow.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponse {

    private Long transactionId;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime createdAt;

}