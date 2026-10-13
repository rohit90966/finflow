# FinFlow Regression Test Report

## Overall Result

FAIL

## Scope

- Database was reset before testing.
- Fresh seed data was created through the live API and the local PostgreSQL database.
- Tested against the Spring Boot app on `localhost:8080`.
- Authentication used HTTP Basic.
- Idempotency keys were generated fresh for payment tests.
- No project code was modified.

## Seed Data

- Users created: `admin@test.com`, `user1@test.com`, `user2@test.com`.
- Accounts created:
  - `900000001` and `900000002` for the admin user.
  - `100000001` and `100000002` for `user1@test.com`.
  - `200000001` and `200000002` for `user2@test.com`.

## Tests Executed

### Account and Authorization

- `GET /account` without authentication.
- `GET /account` with `user1@test.com` authentication.
- `GET /account/{accountNumber}` for own account.
- `GET /account/{accountNumber}` for another user's account.
- `GET /account/{accountNumber}` for an invalid account.
- `GET /account/{accountNumber}` without authentication.
- `GET /transaction/{accountNumber}` for own account.
- `GET /transaction/{accountNumber}` for another user's account.
- `GET /transaction/{accountNumber}` for an invalid account.
- `GET /transaction/{accountNumber}` without authentication.
- `GET /Admin/{accountNumber}` without authentication.
- `GET /Admin/{accountNumber}` as a normal user.
- `GET /Admin/{accountNumber}` as the admin user.
- `GET /user` without authentication.
- `GET /user` with `user1@test.com` authentication.

### Public User Creation Validation

- `POST /public/create-user` with missing password.
- `POST /public/create-user` with missing email.
- `POST /public/create-user` with missing name.
- `POST /public/create-user` with empty JSON.
- `POST /public/create-user` with an invalid email.

### Payment Transfer

- `POST /payment/transfer` without `Idempotency-Key`.
- `POST /payment/transfer` with a fresh idempotency key and valid payload.
- `POST /payment/transfer` replayed with the same key and same payload.
- `POST /payment/transfer` replayed with the same key and a different payload.
- `POST /payment/transfer` with a foreign sender account.
- `POST /payment/transfer` with an invalid receiver account.
- `POST /payment/transfer` with an invalid sender account.
- `POST /payment/transfer` without authentication.

### Concurrency

- 10 concurrent transfers using the same `Idempotency-Key`.
- 10 concurrent transfers using different `Idempotency-Key` values.
- 100 concurrent balance reads during transfers.
- Additional diagnostic same-key concurrency probe to confirm failure pattern.

## Passed Tests

- `GET /account` without authentication returned `401`.
- `GET /account` with authentication returned `200` and a JSON account list.
- `GET /account/{accountNumber}` for own account returned `200`.
- `GET /account/{accountNumber}` for another user's account returned `404` and did not leak data.
- `GET /account/{accountNumber}` for an invalid account returned `404`.
- `GET /account/{accountNumber}` without authentication returned `401`.
- `GET /transaction/{accountNumber}` for own account returned `200`.
- `GET /transaction/{accountNumber}` for another user's account returned `403`.
- `GET /transaction/{accountNumber}` for an invalid account returned `403`.
- `GET /Admin/{accountNumber}` without authentication returned `401`.
- `GET /Admin/{accountNumber}` as a normal user returned `403`.
- `GET /Admin/{accountNumber}` as admin returned `200`.
- `GET /user` without authentication returned `401`.
- `GET /user` with authentication returned `200`.
- `POST /payment/transfer` without `Idempotency-Key` returned `400`.
- `POST /payment/transfer` with a fresh key and valid payload returned `200`.
- `POST /payment/transfer` replayed with the same key and same payload returned `200` with the same transaction ID.
- `POST /payment/transfer` replayed with the same key and a different payload returned `400`.
- `POST /payment/transfer` with an invalid receiver account returned `404`.
- `POST /payment/transfer` with an invalid sender account returned `404`.
- 100 concurrent balance reads returned `200` responses in the tested run.
- Final database checks showed no orphan ledger rows, no orphan idempotency rows, and no negative balances.

## Failed Tests

### 1. `POST /public/create-user` invalid request bodies

- Missing password returned `500` instead of `400`.
- Missing email returned `500` instead of `400`.
- Missing name returned `500` instead of `400`.
- Empty JSON returned `500` instead of `400`.
- Invalid email returned `204`, and a user row was inserted for `not-an-email`.

### 2. `GET /transaction/{accountNumber}` without authentication

- Returned `500` instead of `401`.
- This endpoint is not safely handling anonymous access.

### 3. `POST /payment/transfer` foreign sender account

- Returned `500` instead of `403`.
- Ownership failures are still not mapped cleanly.

### 4. Concurrency regression

- Same-key burst did not behave cleanly under load.
- In the main concurrency run, the same-key burst returned a mix of `200` and `500` responses while only one transaction row was created for the shared key.
- In the diagnostic same-key probe, 3 of 10 requests returned `200` and 7 returned `500`; all successful responses pointed to the same transaction ID.
- The different-key burst returned `200` for all 10 requests, but account balances did not move in line with the number of successful transfer rows.
- This indicates a race condition or lost-update problem in the transfer path.

## HTTP Status Returned vs Expected

- `POST /public/create-user` invalid bodies: expected `400`, returned `500` for missing fields and `204` for invalid email.
- `GET /transaction/{accountNumber}` without auth: expected `401`, returned `500`.
- `POST /payment/transfer` foreign sender: expected `403`, returned `500`.
- `POST /payment/transfer` missing header: expected `400`, returned `400`.
- `POST /payment/transfer` valid request: expected `200`, returned `200`.
- `POST /payment/transfer` duplicate same key + same payload: expected `200`, returned `200`.
- `POST /payment/transfer` duplicate same key + different payload: expected `400`, returned `400`.
- `POST /payment/transfer` invalid sender/receiver: expected `404`, returned `404`.
- `GET /account/{accountNumber}` foreign access: expected `403` or `404`, returned `404`.

## Security Issues

- `GET /user` and `GET /account` return user objects containing password hashes and internal relationship data.
- `GET /transaction/{accountNumber}` without authentication leaks a `500` instead of a proper `401`.
- `POST /payment/transfer` without authentication leaks a `500` instead of a controlled auth failure.
- `POST /payment/transfer` foreign sender access leaks a `500` instead of a `403`.
- `POST /public/create-user` accepts an invalid email and inserts a row.

## Authorization Issues

- `GET /account/{accountNumber}` correctly blocks foreign access with `404` and does not leak the resource.
- `GET /transaction/{accountNumber}` correctly blocks foreign access with `403`.
- `GET /transaction/{accountNumber}` does not handle unauthenticated access safely.
- `POST /payment/transfer` does not handle unauthorized ownership failures safely.
- `POST /payment/transfer` is not protected cleanly at the security layer for anonymous access.

## Concurrency Issues

- Same-key transfer bursts are not reliable under contention; some requests return `500` while others succeed against the same transaction record.
- Different-key transfer bursts complete, but account balances drift away from the transaction log under concurrent load.
- The final database snapshot after all testing showed:
  - `14` transaction rows.
  - `28` ledger rows.
  - `14` idempotency rows.
  - `0` orphan ledger rows.
  - `0` orphan idempotency rows.
  - `0` negative balances.
  - sender balance `11996` and receiver balance `19004`.
- Those balances do not match the total transfer activity observed during the concurrency runs, which indicates a lost-update or non-serialized write problem.

## Database Consistency Checks

- No orphan `ledger` rows were found.
- No orphan `idempotency` rows were found.
- No account had a negative balance.
- The transaction log and account balances diverged under concurrent transfer load.
- The invalid email test inserted an extra user row, so the database was not unchanged by validation failures.

## Performance Observations

- The app stayed responsive for the read burst and returned `200` for all 100 concurrent balance reads in the tested run.
- No deadlock was observed during the test window.
- The failure mode under concurrency was correctness, not total outage.

## Suggested Fixes

- Add bean validation to the public user creation payload and reject malformed input with `400`.
- Validate email format before persistence.
- Return DTOs for user/account reads so password hashes are not exposed.
- Handle anonymous access in `GET /transaction/{accountNumber}` and `POST /payment/transfer` with proper auth responses.
- Map payment ownership failures to `403` instead of `500`.
- Add locking or atomic update logic to the transfer path to prevent lost updates under concurrent writes.
- Revisit the idempotency flow so same-key concurrent requests cannot observe a partially initialized idempotency record.
