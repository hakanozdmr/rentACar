-- Migration script to add new columns to rentals table
-- These columns are merged from reservations table

-- Add new columns to rentals table
ALTER TABLE rentals 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
ADD COLUMN IF NOT EXISTS total_amount DECIMAL(10,2),
ADD COLUMN IF NOT EXISTS special_requests VARCHAR(500),
ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;

-- Update existing records with default values
UPDATE rentals 
SET status = 'CONFIRMED' 
WHERE status IS NULL;

-- Update total_amount for existing records if they don't have it
-- Calculate days between start and end dates
UPDATE rentals r
SET total_amount = (
    SELECT c.daily_price * (
        CASE 
            WHEN r."end" IS NOT NULL AND r.start IS NOT NULL 
            THEN GREATEST(1, (r."end"::date - r.start::date) + 1)
            ELSE 1
        END
    )
    FROM cars c 
    WHERE c.id = r.car_id
)
WHERE r.total_amount IS NULL;

-- Add index on status for better query performance
CREATE INDEX IF NOT EXISTS idx_rentals_status ON rentals(status);

-- Add index on confirmed_at for date-based queries
CREATE INDEX IF NOT EXISTS idx_rentals_confirmed_at ON rentals(confirmed_at);

-- Add rental_id column to reservation_ratings table for direct rental rating
ALTER TABLE reservation_ratings 
ADD COLUMN IF NOT EXISTS rental_id BIGINT;

-- Add foreign key constraint for rental_id (PostgreSQL doesn't support IF NOT EXISTS for constraints)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_reservation_ratings_rental_id' 
        AND table_name = 'reservation_ratings'
    ) THEN
        ALTER TABLE reservation_ratings 
        ADD CONSTRAINT fk_reservation_ratings_rental_id 
        FOREIGN KEY (rental_id) REFERENCES rentals(id);
    END IF;
END $$;

-- Make reservation_id nullable since we now support direct rental rating
ALTER TABLE reservation_ratings 
ALTER COLUMN reservation_id DROP NOT NULL;

-- Update existing rating records to use rental_id instead of reservation_id
-- This would require manual mapping, so we'll leave reservation_id for now
-- but mark it as deprecated

-- Make rental_id NOT NULL for new records (after existing data is migrated)
-- ALTER TABLE reservation_ratings ALTER COLUMN rental_id SET NOT NULL;

-- Comments for documentation
COMMENT ON COLUMN rentals.status IS 'Rental status: PENDING, CONFIRMED, CANCELLED, COMPLETED, EXPIRED';
COMMENT ON COLUMN rentals.total_amount IS 'Total amount for the rental period';
COMMENT ON COLUMN rentals.special_requests IS 'Special requests from customer';
COMMENT ON COLUMN rentals.confirmed_at IS 'When the rental was confirmed';
COMMENT ON COLUMN rentals.cancelled_at IS 'When the rental was cancelled (if applicable)';
COMMENT ON COLUMN reservation_ratings.rental_id IS 'Direct rental reference for rating (after table merge)';
