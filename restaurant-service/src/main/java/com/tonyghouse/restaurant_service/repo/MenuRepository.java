package com.tonyghouse.restaurant_service.repo;

import com.tonyghouse.restaurant_service.constants.MenuType;
import com.tonyghouse.restaurant_service.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    boolean existsByBranch_IdAndMenuType(UUID branchId, MenuType menuType);

    Optional<Menu> findByBranch_IdAndMenuTypeAndActiveTrue(
            UUID branchId,
            MenuType menuType
    );

    List<Menu> findAllByBranch_Id(UUID branchId);

    List<Menu> findByBranchIdAndActiveTrue(UUID branchId);

    Optional<Menu> findByBranchIdAndMenuTypeAndActiveTrue(
            UUID branchId,
            MenuType menuType
    );

}
