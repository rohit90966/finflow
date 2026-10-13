package com.rohit.finflow.dto;

import com.rohit.finflow.enums.EntryType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Component
@Getter
@Setter
public class LedgerResponse {

    private String accountNumber;
    private Long transactionId;
    private EntryType entryType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
    private String remark;
}