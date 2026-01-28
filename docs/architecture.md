### Architecture Doc



# Payment Service Architectue

**What it does:**
- Creates payment records
- Processes payments (success/fail)
- Handles refunds
- Manages payment state

**What it doesn't do:**
- Calculate prices or discounts
- Manage order items

The order service sends the final price, and we trust it.

---

**Flow:**
1. Order service creates payment (status: INITIATED)
2. Payment processes separately
3. Ends as SUCCESS or FAILED
4. Refunds only work on successful payments

Payment creation and processing are separate steps.

---

**States:**
- INITIATED → PROCESSING → SUCCESS/FAILED
- RETRYING (for timeouts)
- FAILED_PERMANENT (retry limit hit)
- REFUNDED (full refund)

Refunds are stored separately, don't modify original payment.

---

**APIs:**
- `POST /api/payments` - create payment (needs Idempotency-Key header)
- `POST /api/payments/{id}/process` - process it
- `GET /api/payments/{id}` - get details
- `POST /api/payments/{id}/refund` - refund (partial or full)

---

**Idempotency:**
Using `Idempotency-Key` header prevents duplicate charges from retries. Same key = same payment returned, not a new one.

---

**Services:**
- PaymentService - create, process, get payments
- RefundService - handles refunds and validation

Business logic stays in services, not controllers.

---

**Payment Processing:**
Mock processor now (random success/fail/timeout). Easy to swap for real gateway later.

---

**Database:**
Two tables: `payments` and `refunds`. No payment_items table - items belong to order service. Keeps it simple.

---

**Kafka:**
Optional. Only for publishing events (PaymentSucceeded, PaymentFailed, etc). Not used for core operations.

---

**Why this design:**
Simple, testable, matches real payment systems. Focused on payments only.

---

**Future:**
- Real gateway integration
- Async processing
- Scheduled retries
- Better monitoring