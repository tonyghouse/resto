package com.tonyghouse.restaurant_service.repo;

import com.tonyghouse.restaurant_service.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderStatusHistoryRepository
        extends JpaRepository<OrderStatusHistory, UUID> {
}
