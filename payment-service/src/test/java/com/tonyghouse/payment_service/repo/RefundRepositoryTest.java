package com.tonyghouse.payment_service.repo;

import com.tonyghouse.payment_service.entity.Refund;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RefundRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private RefundRepository repo;


    @Test
    void shouldReturnZeroWhenNoRefundsExist() {
        UUID paymentId = UUID.randomUUID();

        BigDecimal sum = repo.sumRefundedAmount(paymentId);

        assertThat(sum).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void shouldReturnSingleRefundAmount() {
        UUID paymentId = UUID.randomUUID();

        saveRefund(paymentId, "10.50");

        BigDecimal sum = repo.sumRefundedAmount(paymentId);

        assertThat(sum).isEqualByComparingTo("10.50");
    }


    @Test
    void shouldSumMultipleRefunds() {
        UUID paymentId = UUID.randomUUID();

        saveRefund(paymentId, "10.00");
        saveRefund(paymentId, "5.25");
        saveRefund(paymentId, "4.75");

        BigDecimal sum = repo.sumRefundedAmount(paymentId);

        assertThat(sum).isEqualByComparingTo("20.00");
    }


    @Test
    void shouldOnlySumForSamePaymentId() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        saveRefund(p1, "10.00");
        saveRefund(p2, "50.00");

        BigDecimal sum = repo.sumRefundedAmount(p1);

        assertThat(sum).isEqualByComparingTo("10.00");
    }


    // helper
    private void saveRefund(UUID paymentId, String amount) {
        Refund r = new Refund();
        r.setRefundId(UUID.randomUUID());
        r.setPaymentId(paymentId);
        r.setRefundAmount(new BigDecimal(amount));

        repo.save(r);
    }
}
