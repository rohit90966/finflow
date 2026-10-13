package com.rohit.finflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohit.finflow.dto.TransactionResponse;
import com.rohit.finflow.dto.TransferRequest;
import com.rohit.finflow.entity.*;
import com.rohit.finflow.enums.EntryType;
import com.rohit.finflow.enums.TransactionStatus;
import com.rohit.finflow.kafka.TransferProducer;
import com.rohit.finflow.kafka.event.TransferEvent;
import com.rohit.finflow.repository.AccountRepository;
import com.rohit.finflow.repository.TransactionRepository;
import com.rohit.finflow.repository.UserRepository;
import com.rohit.finflow.specification.TransactionSpecification;
import jdk.jfr.Frequency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    @Autowired
    private UserService userService;
    @Autowired
    private
    AccountService accountService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private LedgerService ledgerService;
    @Autowired
    private IdempotencyService idempotencyService;
    @Autowired
    private TransferProducer transferProducer;

    @Transactional
    public TransactionResponse transfer(
            TransferRequest request,
            User user,
            String idempotencyKey
    ) throws Exception {

        Long userId = user.getId();

        String requestHash = buildStringRequestFromRequestAndHashedIt(request);


        Idempotency existing =
                idempotencyService.findByIdempotencyKey(idempotencyKey);

        if (existing != null) {

            if (!existing.getRequestHash().equals(requestHash)) {
                throw new IllegalArgumentException(
                        "Same Idempotency-Key used with different request"
                );
            }

            return buildTrasactionResponseFromTransaction(
                    existing.getTransaction()
            );
        }

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }


        Account senderAccount =
                accountService.findByAccountNumber(
                        request.getSenderAccountNumber()
                );

        if (senderAccount == null) {
            throw new AccountNotFoundException(
                    "Sender account not found"
            );
        }

        Account ownedAccount =
                accountRepository.findByIdAndUser_Id(
                        senderAccount.getId(),
                        userId
                );

        if (ownedAccount == null) {
            throw new AccessDeniedException(
                    "You don't own this account"
            );
        }

        Account receiverAccount =
                accountService.findByAccountNumber(
                        request.getReceiverAccountNumber()
                );

        if (receiverAccount == null) {
            throw new AccountNotFoundException(
                    "Receiver account not found"
            );
        }

        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new IllegalArgumentException(
                    "Self transfer is not allowed"
            );
        }

        if (request.getAmount().compareTo(
                senderAccount.getBalance()
        ) > 0) {

            throw new InsufficientResourcesException(
                    "Insufficient balance"
            );
        }

        User receiverUser =
                userRepository.findByAccountNumber(
                        request.getReceiverAccountNumber()
                ).orElseThrow(() ->
                        new AccountNotFoundException(
                                "Receiver user not found"
                        )
                );

        String receiverEmail = receiverUser.getEmail();

        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = new Transaction();

        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(now);
        transaction.setSenderAccount(senderAccount);
        transaction.setReceiverAccount(receiverAccount);
        transaction.setAmount(request.getAmount());

        transaction = transactionRepository.save(transaction);


        Idempotency idempotency = new Idempotency();

        idempotency.setIdempotencyKey(idempotencyKey);
        idempotency.setRequestHash(requestHash);
        idempotency.setTransactionStatus(TransactionStatus.PENDING);
        idempotency.setCreatedAt(now);
        idempotency.setTransaction(transaction);

        try {

            idempotencyService.save(idempotency);

        } catch (DataIntegrityViolationException e) {

            Idempotency concurrentRequest =
                    idempotencyService.findByIdempotencyKey(
                            idempotencyKey
                    );

            if (concurrentRequest != null) {
                return buildTrasactionResponseFromTransaction(
                        concurrentRequest.getTransaction()
                );
            }

            throw e;
        }

        TransferEvent transferEvent =
                buildTransferEvent(
                        transaction,
                        idempotencyKey,
                        user,
                        receiverEmail,
                        senderAccount,
                        receiverAccount
                );


        try {

            transferProducer.publishTransferEvent(
                    transferEvent
            );

        } catch (Exception e) {

            transaction.setStatus(
                    TransactionStatus.FAILED
            );

            throw new RuntimeException(
                    "Failed to publish transfer event",
                    e
            );
        }

        return buildTrasactionResponseFromTransaction(
                transaction
        );
    }


    public List<TransactionResponse> getTrasanctions(String email, String accountNumber, int pageNo, int pageSize,String sortBy,String order,String status,BigDecimal minAmount,BigDecimal maxAmount,LocalDateTime startDate,LocalDateTime endDate) throws AccessDeniedException {
        User user=userService.findByEmail(email);

        Account account=accountService.findByAccountNumberAndUser_Id(accountNumber,user.getId());
        if(account==null){
            throw new AccessDeniedException("This account does not belongs to you");
        }
        Sort sort=null;
        if(order.equalsIgnoreCase("DES")){
            sort=Sort.by(sortBy).descending();
        }
        else{
            sort=Sort.by(sortBy).ascending();
        }


        Pageable pageable = PageRequest.of(pageNo, pageSize,sort);

        Page<Transaction>a=transactionRepository.findBySenderAccount_IdOrReceiverAccount_Id(
                account.getId(),
                account.getId(),
                pageable);

        List<TransactionResponse>responses=new ArrayList<>();
        for(Transaction transaction:a){
            TransactionResponse transactionResponse=new TransactionResponse();
            transactionResponse.setSenderAccountNumber(transaction.getSenderAccount().getAccountNumber());
            transactionResponse.setReceiverAccountNumber(transaction.getReceiverAccount().getAccountNumber());
            transactionResponse.setAmount(transaction.getAmount());
            transactionResponse.setStatus(transaction.getStatus());
            transactionResponse.setCreatedAt(transaction.getCreatedAt());
            transactionResponse.setTransactionId(transaction.getId());
            responses.add(transactionResponse);
        }

        return responses;
    }

    public TransferEvent buildTransferEvent(Transaction transaction,String idempotency,User user,String receiverEmail,Account senderAccount,Account receiverAccount){
        TransferEvent transferEvent =new TransferEvent();
        transferEvent.setTransactionId(transaction.getId());
        transferEvent.setSenderAccountNumber(transaction.getSenderAccount().getAccountNumber());
        transferEvent.setReceiverAccountNumber(transaction.getReceiverAccount().getAccountNumber());
        transferEvent.setAmount(transaction.getAmount());
        transferEvent.setStatus(transaction.getStatus());
        transferEvent.setIdempotencyKey(idempotency);
        transferEvent.setCreatedAt(transaction.getCreatedAt());

        transferEvent.setUserEmail(user.getEmail());
        transferEvent.setReceiverEmail(receiverEmail);
        return  transferEvent;
    }

    public Optional<Transaction> findById(Long trasactionId) {
        Optional<Transaction>t= transactionRepository.findById(trasactionId);
        return t;
    }

    private TransactionResponse buildTrasactionResponseFromTransaction(Transaction transaction) {
        TransactionResponse transactionResponse=new TransactionResponse();

        transactionResponse.setTransactionId(transaction.getId());
        transactionResponse.setSenderAccountNumber(transaction.getSenderAccount().getAccountNumber());
        transactionResponse.setReceiverAccountNumber(transaction.getReceiverAccount().getAccountNumber());
        transactionResponse.setAmount(transaction.getAmount());
        transactionResponse.setStatus(transaction.getStatus());
        transactionResponse.setCreatedAt(transaction.getCreatedAt());
        return transactionResponse;
    }

    private String buildStringRequestFromRequestAndHashedIt(TransferRequest request) throws JsonProcessingException, NoSuchAlgorithmException {
        ObjectMapper objectMapper=new ObjectMapper();
        String stringRequest=objectMapper.writeValueAsString(request);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(stringRequest.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb=new StringBuilder();
        for(byte b:hash){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();

    }

}
