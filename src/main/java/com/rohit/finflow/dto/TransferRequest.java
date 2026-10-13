package com.rohit.finflow.dto;

import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLSession;
import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {

    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;

}