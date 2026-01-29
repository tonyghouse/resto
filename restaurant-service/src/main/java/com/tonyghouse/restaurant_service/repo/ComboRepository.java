package com.tonyghouse.restaurant_service.repo;

import com.tonyghouse.restaurant_service.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComboRepository extends JpaRepository<Combo, UUID> {

    List<Combo> findByActiveTrue();
}
