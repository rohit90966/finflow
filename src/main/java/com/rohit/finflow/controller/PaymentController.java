package com.rohit.finflow.controller;

import com.rohit.finflow.dto.TransactionResponse;
import com.rohit.finflow.dto.TransferRequest;
import com.rohit.finflow.entity.Transaction;
import com.rohit.finflow.entity.User;
import com.rohit.finflow.service.AccountService;
import com.rohit.finflow.service.TransactionService;
import com.rohit.finflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;

@RestController
@RequestMapping("payment")
public class PaymentController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key")
            String idempotencyKey

    ) throws Exception {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        User user=userService.findByEmail(email);

        try {
            TransactionResponse response = transactionService.transfer(request, user, idempotencyKey);

            return ResponseEntity.ok(response);

        } catch (InsufficientResourcesException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        }catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
        catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Cant connect to server");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error");
        }
    }
}
