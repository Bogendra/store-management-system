-- This migration makes sure any additional columns that might be needed are properly added
-- It's safe to run even if columns already exist, as we use IF NOT EXISTS checks

-- For safety, ensure all BaseEntity columns are present in all tables
-- (these statements will have no effect if columns already exist)

-- Add tenant_id to any table that might need it
DO $$ 
BEGIN
    -- Check if tenant_id exists in inventory_levels
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'inventory_levels' AND column_name = 'tenant_id'
    ) THEN
        ALTER TABLE inventory_levels ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;
    
    -- Check if tenant_id exists in inventory_transactions
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'inventory_transactions' AND column_name = 'tenant_id'
    ) THEN
        ALTER TABLE inventory_transactions ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;
    
    -- Check if tenant_id exists in item_variants
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'item_variants' AND column_name = 'tenant_id'
    ) THEN
        ALTER TABLE item_variants ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;
    
    -- Check if updated_at exists in inventory_transactions
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'inventory_transactions' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE inventory_transactions ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;
