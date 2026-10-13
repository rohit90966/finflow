package com.rohit.finflow.controller;

import com.rohit.finflow.dto.TransactionResponse;
import com.rohit.finflow.entity.Transaction;
import com.rohit.finflow.entity.User;
import com.rohit.finflow.service.TransactionService;
import com.rohit.finflow.service.UserService;
import net.bytebuddy.build.RepeatedAnnotationPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationNotSupportedException;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("transaction")
public class TransactionController {

    @Autowired
    private UserService userService;
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getTransactions(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                             @RequestParam(defaultValue = "5", required = false) int pageSize,
                                             @PathVariable String accountNumber,
                                             @RequestParam(defaultValue = "createdAt", required = false) String sortBy,
                                             @RequestParam(defaultValue = "DES", required = false) String order,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) BigDecimal minAmount,
                                             @RequestParam(required = false) BigDecimal maxAmount,
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                             LocalDateTime startDate,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                             LocalDateTime endDate
    ) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            List<TransactionResponse> trasanctions = transactionService.getTrasanctions(authentication.getName(), accountNumber, pageNo, pageSize, sortBy, order, status, minAmount, maxAmount, startDate, endDate);
            return new ResponseEntity<>(trasanctions, HttpStatus.OK);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }

}
