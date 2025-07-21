-- Add updated_at column to inventory_transactions table
ALTER TABLE inventory_transactions ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
