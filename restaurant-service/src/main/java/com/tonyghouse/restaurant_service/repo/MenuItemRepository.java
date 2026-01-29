package com.tonyghouse.restaurant_service.repo;

import com.tonyghouse.restaurant_service.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    @Modifying
    @Query("""
        update MenuItem mi
           set mi.available = :available
         where mi.id in :itemIds
    """)
    int bulkUpdateAvailability(
            @Param("itemIds") List<UUID> itemIds,
            @Param("available") boolean available
    );

    @Modifying
    @Query("""
        update MenuItem mi
           set mi.price = :price
         where mi.id in :itemIds
    """)
    int bulkUpdatePrice(
            @Param("itemIds") List<UUID> itemIds,
            @Param("price") BigDecimal price
    );
}
