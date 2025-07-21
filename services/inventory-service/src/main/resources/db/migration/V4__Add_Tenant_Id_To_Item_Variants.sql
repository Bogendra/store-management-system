-- Add tenant_id column to item_variants table
ALTER TABLE item_variants ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;

-- Add index for tenant_id column
CREATE INDEX idx_item_variants_tenant_id ON item_variants(tenant_id);
