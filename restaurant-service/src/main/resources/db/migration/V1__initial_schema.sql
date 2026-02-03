CREATE TABLE branch (
    id              UUID PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    location        VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE menu (
    id              UUID PRIMARY KEY,
    branch_id       UUID NOT NULL,
    menu_type       VARCHAR(20) NOT NULL,
    valid_from      TIME NOT NULL,
    valid_to        TIME NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_menu_branch
        FOREIGN KEY (branch_id) REFERENCES branch(id),

    CONSTRAINT uq_branch_menu_type
        UNIQUE (branch_id, menu_type),

    CONSTRAINT chk_menu_type
        CHECK (menu_type IN ('BREAKFAST', 'LUNCH', 'DINNER'))
);

CREATE TABLE menu_item (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(150) NOT NULL,
    description         TEXT,
    price               NUMERIC(10,2) NOT NULL,
    preparation_time    INTEGER NOT NULL, -- minutes
    category            VARCHAR(50) NOT NULL,
    food_type           VARCHAR(20) NOT NULL,
    available           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_food_type
        CHECK (food_type IN ('VEGETARIAN', 'NON_VEGETARIAN', 'VEGAN'))
);

CREATE TABLE menu_menu_item (
    menu_id         UUID NOT NULL,
    menu_item_id    UUID NOT NULL,

    PRIMARY KEY (menu_id, menu_item_id),

    CONSTRAINT fk_mmi_menu
        FOREIGN KEY (menu_id) REFERENCES menu(id),

    CONSTRAINT fk_mmi_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_item(id)
);

CREATE TABLE combo (
    id              UUID PRIMARY KEY,
    branch_id       UUID NOT NULL,
    name            VARCHAR(150) NOT NULL,
    description     TEXT,
    combo_price     NUMERIC(10,2) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_combo_branch
            FOREIGN KEY (branch_id) REFERENCES branch(id)
);

CREATE TABLE combo_item (
    combo_id        UUID NOT NULL,
    menu_item_id    UUID NOT NULL,

    PRIMARY KEY (combo_id, menu_item_id),

    CONSTRAINT fk_combo_item_combo
        FOREIGN KEY (combo_id) REFERENCES combo(id),

    CONSTRAINT fk_combo_item_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_item(id)
);

CREATE TABLE orders (
    id              UUID PRIMARY KEY,
    branch_id       UUID NOT NULL,
    customer_name   VARCHAR(100),
    customer_phone  VARCHAR(20),
    status          VARCHAR(20) NOT NULL,
    total_amount    NUMERIC(12,2) NOT NULL,
    payment_id      UUID,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_branch
        FOREIGN KEY (branch_id) REFERENCES branch(id),

    CONSTRAINT chk_order_status
        CHECK (status IN (
            'CREATED',
            'ACCEPTED',
            'PREPARING',
            'READY',
            'DELIVERED',
            'CANCELLED'
        ))
);

CREATE TABLE order_item (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL,
    item_name       VARCHAR(150) NOT NULL,
    item_type       VARCHAR(50) NOT NULL, -- ITEM or COMBO
    quantity        INTEGER NOT NULL,
    unit_price      NUMERIC(10,2) NOT NULL,
    total_price     NUMERIC(10,2) NOT NULL,
    special_notes   TEXT,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE order_status_history (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL,
    old_status      VARCHAR(20),
    new_status      VARCHAR(20) NOT NULL,
    changed_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_status_order
        FOREIGN KEY (order_id) REFERENCES orders(id),

    CONSTRAINT chk_old_order_status
        CHECK (
            old_status IS NULL OR old_status IN (
                'CREATED',
                'ACCEPTED',
                'PREPARING',
                'READY',
                'DELIVERED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_new_order_status
        CHECK (new_status IN (
            'CREATED',
            'ACCEPTED',
            'PREPARING',
            'READY',
            'DELIVERED',
            'CANCELLED'
        ))
);

-- branch
CREATE INDEX idx_branch_name ON branch(name);

-- menu
CREATE INDEX idx_menu_branch_id ON menu(branch_id);
CREATE INDEX idx_menu_branch_active_type ON menu(branch_id, active, menu_type);

-- menu_item
CREATE INDEX idx_menu_item_category ON menu_item(category);
CREATE INDEX idx_menu_item_food_type ON menu_item(food_type);
CREATE INDEX idx_menu_item_avail_cat ON menu_item(available, category);

-- menu_menu_item
CREATE INDEX idx_mmi_item_id ON menu_menu_item(menu_item_id);

-- combo
CREATE INDEX idx_combo_branch_id ON combo(branch_id);
CREATE INDEX idx_combo_branch_active ON combo(branch_id, active);

-- combo_item
CREATE INDEX idx_combo_item_item_id ON combo_item(menu_item_id);

-- orders
CREATE INDEX idx_orders_branch_id ON orders(branch_id);
CREATE INDEX idx_orders_payment_id ON orders(payment_id);

-- order_item
CREATE INDEX idx_order_item_order_id ON order_item(order_id);

-- order_status_history
CREATE INDEX idx_order_status_order_id ON order_status_history(order_id);
CREATE INDEX idx_order_status_order_time ON order_status_history(order_id, changed_at);
