package com.tonyghouse.restaurant_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "branch")
@Getter
@Setter
public class Branch extends BaseEntity {

    private String name;
    private String location;

    private Instant createdAt;
}
