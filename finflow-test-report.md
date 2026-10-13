# FinFlow Test Report

## Overall Result

FAIL

## Scope

- Test environment was reset before each major scenario.
- Database baseline was verified after every reset.
- API tests were run against the app on port 8081.
- Authentication used HTTP Basic with the seeded users.
- Payment transfer tests used the controller's actual `idempotencyKey` binding and also verified the documented `Idempotency-Key` contract.

## APIs Tested

- `POST /public/create-user`
- `GET /user`
- `PUT /user`
- `DELETE /user`
- `GET /account`
- `POST /account`
- `GET /account/{id}`
- `PUT /account/{accountId}`
- `GET /account/{id}/check-balance`
- `GET /transaction/{accountNumber}`
- `GET /Admin/{accountNumber}`
- `POST /payment/transfer`

## Passed Test Cases

- `POST /public/create-user` valid input
- `GET /user` authenticated self lookup
- `GET /user` without authentication
- `PUT /user` valid update
- `DELETE /user` valid delete
- `GET /account` without authentication
- `POST /account` valid create
- `PUT /account/{accountId}` valid update
- `GET /account/{id}` valid lookup
- `GET /account/{id}` missing user
- `GET /account/{id}/check-balance` valid lookup
- `GET /Admin/{accountNumber}` valid admin access
- `GET /Admin/{accountNumber}` forbidden for normal user
- `GET /Admin/{accountNumber}` without authentication
- `POST /payment/transfer` successful transfer with controller-bound header
- `POST /payment/transfer` insufficient balance
- `POST /payment/transfer` invalid sender account
- `POST /payment/transfer` invalid receiver account
- `POST /payment/transfer` self transfer
- `POST /payment/transfer` zero amount
- `POST /payment/transfer` negative amount
- `POST /payment/transfer` invalid request body
- `Same request + same idempotency key` replay
- `Same idempotency key + different request` rejection
- `Different idempotency key + same request` creates a new transaction
- `10 simultaneous transfers` with same idempotency key
- `10 simultaneous transfers` with different idempotency keys
- `100 concurrent balance reads` while transfers were executing
- Final database verification after transfer scenarios

## Failed Test Cases

### Functional - `GET /account`
- Expected Result: `200 OK` with the authenticated user's accounts.
- Actual Result: `500 Internal Server Error`.
- Root Cause: The endpoint returns JPA entities directly and appears to fail during serialization/lazy-loading of the account graph.
- Recommended Fix: Return DTOs instead of managed entities, or explicitly shape the response and test the serialization path.

### Functional - `POST /public/create-user` invalid body
- Expected Result: `4xx` validation failure and no insert.
- Actual Result: `500 Internal Server Error`.
- Root Cause: The controller/service does not validate required fields before calling the password encoder; a missing password can fail with a server error.
- Recommended Fix: Add bean validation to the request model and return `400 Bad Request` for malformed input.

### Authorization - `GET /account/{id}` as another user
- Expected Result: `403 Forbidden`.
- Actual Result: `200 OK` with the foreign user's account list.
- Root Cause: The controller trusts the path `id` and never compares it to the authenticated principal.
- Recommended Fix: Enforce ownership checks before returning account data and reject foreign access.

### Functional / Authorization - `GET /transaction/{accountNumber}` own account
- Expected Result: `200 OK` with the authenticated user's transaction list.
- Actual Result: `500 Internal Server Error`.
- Root Cause: `AccountService.findByAccountNumberAndUser_Id` is recursively implemented and never reaches the repository, which breaks the transaction lookup flow.
- Recommended Fix: Replace the recursive method body with the repository call.

### Functional / Authorization - `GET /transaction/{accountNumber}` foreign account
- Expected Result: `403 Forbidden`.
- Actual Result: `500 Internal Server Error`.
- Root Cause: The same recursive account lookup bug prevents the authorization check from completing.
- Recommended Fix: Fix the account lookup method first, then enforce the ownership check consistently.

### Authorization - `GET /transaction/{accountNumber}` without authentication
- Expected Result: `401 Unauthorized`.
- Actual Result: `500 Internal Server Error`.
- Root Cause: The transaction endpoint reaches the broken account lookup path before producing a proper auth failure.
- Recommended Fix: Fix the recursive lookup and then verify the security response path.

### Functional - `POST /payment/transfer` using the documented `Idempotency-Key` header
- Expected Result: `200 OK` for a valid transfer request.
- Actual Result: `400 Bad Request`.
- Root Cause: The controller binds `@RequestHeader String idempotencyKey`, so it does not honor the documented `Idempotency-Key` header name.
- Recommended Fix: Bind the header explicitly with `@RequestHeader("Idempotency-Key")` or document and standardize the accepted header name.

### Authorization - `POST /payment/transfer` from a foreign sender account
- Expected Result: `403 Forbidden`.
- Actual Result: `500 Internal Server Error`.
- Root Cause: The service throws an access-denied exception for foreign ownership, but the controller falls through to the generic exception handler instead of returning `403`.
- Recommended Fix: Catch access-denied exceptions explicitly and map them to `403 Forbidden`.

## Bugs Found

- Transaction lookup is broken by a recursive account-service method.
- Account list retrieval returns a server error on the collection endpoint.
- Foreign account access is not blocked on `GET /account/{id}`.
- Public user creation does not fail cleanly for malformed input.
- Payment transfer does not accept the documented `Idempotency-Key` header name.
- Payment authorization failures are returned as `500` instead of `403`.

## Security Issues

- `GET /account/{id}` exposes another user's account data with a `200 OK`.
- `POST /payment/transfer` does not map access-denied ownership failures to `403`.
- `POST /public/create-user` can surface server errors on malformed input instead of a controlled validation response.

## Concurrency Issues

- Same-key concurrent transfers collapsed to a single transaction as expected.
- Different-key concurrent transfers completed successfully and preserved balances.
- Concurrent balance reads during transfers returned consistent `200` responses once the correct account was used.
- No lost-update or negative-balance condition was observed in the exercised scenarios.

## Database Issues

- No orphan `ledger` or `idempotency` rows were observed after the exercised transfers.
- No invalid foreign-key state was observed in the verified scenarios.
- Account balances were consistent after successful transfers and after the concurrent scenarios.
- No primary-key duplicate transaction rows were observed; repeated same-payload transfers with different idempotency keys created separate business transactions by design.

## Performance Summary

- The concurrent transfer and read scenarios completed successfully on the seeded database.
- No formal latency benchmark was collected.
- Under the tested load, the app remained responsive enough to complete 10-way transfer bursts and 100 concurrent balance reads.

## Suggested Fixes

- Replace direct entity responses with DTOs for account and transaction endpoints.
- Fix `AccountService.findByAccountNumberAndUser_Id` to call the repository instead of recursing.
- Add explicit ownership checks to `GET /account/{id}`.
- Add request validation to public user creation and payment transfer payloads.
- Bind the payment idempotency header explicitly to `Idempotency-Key`.
- Map access-denied payment failures to `403 Forbidden`.
- Add integration tests for the broken authorization and validation paths.

## Overall Code Quality Assessment

- The core payment flow is functional once the header name matches the current controller binding.
- The codebase has several controller/service contract problems that are visible under invalid input and authorization testing.
- The transfer path is reasonably consistent under concurrent load, but the API layer needs stronger validation, authorization, and response mapping to be production-ready.
