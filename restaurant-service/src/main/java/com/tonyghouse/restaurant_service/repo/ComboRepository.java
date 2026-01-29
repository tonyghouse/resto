package com.tonyghouse.restaurant_service.repo;

import com.tonyghouse.restaurant_service.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ComboRepository extends JpaRepository<Combo, UUID> {

    @Modifying
    @Query("""
        update Combo c
           set c.comboPrice = :price
         where c.id in :comboIds
    """)
    int bulkUpdateComboPrice(
            @Param("comboIds") List<UUID> comboIds,
            @Param("price") BigDecimal price
    );
}
