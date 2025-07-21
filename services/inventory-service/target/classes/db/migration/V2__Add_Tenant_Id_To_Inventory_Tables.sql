-- Add tenant_id column to inventory_levels table
ALTER TABLE inventory_levels ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;

-- Add tenant_id column to inventory_transactions table if it doesn't exist already
ALTER TABLE inventory_transactions ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;

-- Add indexes for tenant_id columns
CREATE INDEX idx_inventory_levels_tenant_id ON inventory_levels(tenant_id);
CREATE INDEX idx_inventory_transactions_tenant_id ON inventory_transactions(tenant_id);
