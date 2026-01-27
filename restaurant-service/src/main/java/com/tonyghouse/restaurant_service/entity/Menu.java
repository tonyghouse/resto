package com.tonyghouse.restaurant_service.entity;

import com.tonyghouse.restaurant_service.constants.MenuType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "menu",
       uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "menu_type"}))
@Getter
@Setter
public class Menu extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    private MenuType menuType;

    private LocalTime validFrom;
    private LocalTime validTo;

    private boolean active;

    @ManyToMany
    @JoinTable(
        name = "menu_menu_item",
        joinColumns = @JoinColumn(name = "menu_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    private Set<MenuItem> items = new HashSet<>();
}
