package com.tonyghouse.restaurant_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "combo")
@Getter
@Setter
public class Combo extends BaseEntity {

    private String name;
    private String description;
    private BigDecimal comboPrice;
    private boolean active;

    @ManyToMany
    @JoinTable(
        name = "combo_item",
        joinColumns = @JoinColumn(name = "combo_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    private Set<MenuItem> items = new HashSet<>();
}
