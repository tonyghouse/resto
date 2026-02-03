package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.constants.PaymentMethod;
import com.tonyghouse.payment_service.constants.PaymentStatus;
import com.tonyghouse.payment_service.entity.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IdempotencyKeyRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private IdempotencyKeyRepository repo;


    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndFetchPaymentId() {
        String key = "order-123";
        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();

        payment.setPaymentId(paymentId);
        payment.setOrderId(UUID.randomUUID());

        payment.setMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.INITIATED);

        payment.setTotalAmount(new BigDecimal("100"));
        payment.setTaxAmount(new BigDecimal("20"));
        payment.setPayableAmount(new BigDecimal("120"));

        payment.setCurrency("INR");
        payment.setRetryCount(0);

        payment.setCreatedAt(Instant.now());

        payment.setRefunds(new ArrayList<>());
        payment.setIdempotencyKeys(new ArrayList<>());

        entityManager.persistAndFlush(payment);

        repo.save(key, payment);

        var result = repo.findPaymentId(key);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(paymentId);
    }

    @Test
    void shouldReturnEmptyWhenMissing() {
        assertThat(repo.findPaymentId("missing")).isEmpty();
    }
}
