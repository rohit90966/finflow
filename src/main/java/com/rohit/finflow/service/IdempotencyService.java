package com.rohit.finflow.service;

import com.rohit.finflow.entity.Idempotency;
import com.rohit.finflow.repository.IdempotencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    public Idempotency findByIdempotencyKey(String key){
        return idempotencyRepository.findByIdempotencyKey(key);
    }

    public void save(Idempotency idempotency1) {
        idempotencyRepository.save(idempotency1);
    }
}
