-- Create Brands Table
CREATE TABLE brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Categories Table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id)
);

-- Create Locations Table
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'STORE', 'WAREHOUSE', etc.
    address TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Items Table
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    item_code VARCHAR(50) UNIQUE NOT NULL,
    upc_code VARCHAR(50),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id BIGINT,
    brand_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (brand_id) REFERENCES brands(id)
);

-- Create Item Variants Table
CREATE TABLE item_variants (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    variant_name VARCHAR(100),
    sku VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id)
);

-- Create Inventory Levels Table
CREATE TABLE inventory_levels (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT NOT NULL,
    item_variant_id BIGINT NOT NULL,
    quantity_on_hand NUMERIC(15,2) NOT NULL DEFAULT 0,
    quantity_reserved NUMERIC(15,2) NOT NULL DEFAULT 0,
    quantity_available NUMERIC(15,2) GENERATED ALWAYS AS (quantity_on_hand - quantity_reserved) STORED,
    reorder_point NUMERIC(15,2),
    reorder_quantity NUMERIC(15,2),
    last_counted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (location_id) REFERENCES locations(id),
    FOREIGN KEY (item_variant_id) REFERENCES item_variants(id),
    UNIQUE (location_id, item_variant_id)
);

-- Create Inventory Transactions Table
CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT NOT NULL,
    item_variant_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- 'SALE', 'PURCHASE', 'ADJUSTMENT', 'TRANSFER_IN', 'TRANSFER_OUT'
    quantity NUMERIC(15,2) NOT NULL, -- positive for additions, negative for reductions
    reference_type VARCHAR(50), -- 'ORDER', 'PO', 'ADJUSTMENT'
    reference_id VARCHAR(50),
    notes TEXT,
    created_by_user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (location_id) REFERENCES locations(id),
    FOREIGN KEY (item_variant_id) REFERENCES item_variants(id)
);

-- Add indexes for performance
CREATE INDEX idx_items_tenant_id ON items(tenant_id);
CREATE INDEX idx_items_brand_id ON items(brand_id);
CREATE INDEX idx_items_category_id ON items(category_id);
CREATE INDEX idx_item_variants_item_id ON item_variants(item_id);
CREATE INDEX idx_inventory_levels_location_item ON inventory_levels(location_id, item_variant_id);
CREATE INDEX idx_inventory_transactions_item_variant ON inventory_transactions(item_variant_id);
CREATE INDEX idx_inventory_transactions_location ON inventory_transactions(location_id);
CREATE INDEX idx_inventory_transactions_created_at ON inventory_transactions(created_at);
