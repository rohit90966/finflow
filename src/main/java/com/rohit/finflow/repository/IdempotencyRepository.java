package com.rohit.finflow.repository;

import com.rohit.finflow.entity.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<Idempotency,Long> {

    Idempotency findByIdempotencyKey(String key);
}
