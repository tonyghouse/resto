package com.tonyghouse.restaurant_service.repo;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    @Query("""
        select m from Menu m
        join fetch m.items
        where m.branch.id = :branchId
          and m.menuType = :menuType
          and m.active = true
    """)
    Optional<Menu> findActiveMenu(
        @Param("branchId") UUID branchId,
        @Param("menuType") MenuType menuType
    );
}
