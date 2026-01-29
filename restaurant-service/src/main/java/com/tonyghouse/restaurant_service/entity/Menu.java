package com.tonyghouse.restaurant_service.entity;

import com.tonyghouse.restaurant_service.constants.MenuType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "menu",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "menu_type"})
)
@Getter
@Setter
public class Menu extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_type", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private MenuType menuType;

    @Column(name = "valid_from")
    private LocalTime validFrom;

    @Column(name = "valid_to")
    private LocalTime validTo;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToMany
    @JoinTable(
            name = "menu_menu_item",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    private Set<MenuItem> items = new HashSet<>();
}
