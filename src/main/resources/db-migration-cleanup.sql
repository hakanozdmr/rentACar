-- Cleanup migration: Remove reservation_id column from reservation_ratings table
-- This script should be run after all existing ratings are migrated to use rental_id

-- First, verify that all ratings have rental_id populated
-- SELECT COUNT(*) FROM reservation_ratings WHERE rental_id IS NULL AND reservation_id IS NOT NULL;

-- If there are records with null rental_id but existing reservation_id, update them first:
-- UPDATE reservation_ratings rr 
-- SET rental_id = (
--     SELECT r.id FROM rentals r 
--     JOIN reservations res ON res.car_id = r.car_id AND res.customer_id = r.customer_id
--     WHERE res.id = rr.reservation_id
--     LIMIT 1
-- )
-- WHERE rr.rental_id IS NULL AND rr.reservation_id IS NOT NULL;

-- Drop foreign key constraint for reservation_id (if it exists)
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_reservation_ratings_reservation_id' 
        AND table_name = 'reservation_ratings'
    ) THEN
        ALTER TABLE reservation_ratings 
        DROP CONSTRAINT fk_reservation_ratings_reservation_id;
    END IF;
END $$;

-- Drop the reservation_id column completely
ALTER TABLE reservation_ratings 
DROP COLUMN IF EXISTS reservation_id;

-- Make rental_id NOT NULL since it's now the only way to reference rentals
ALTER TABLE reservation_ratings 
ALTER COLUMN rental_id SET NOT NULL;

-- Add comment to document the change
COMMENT ON COLUMN reservation_ratings.rental_id IS 'Required rental reference (replaces deprecated reservation_id)';
