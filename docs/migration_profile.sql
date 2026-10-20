-- HireHub Profile and Avatar Migration
-- Run on existing hirehub database to enable profile photo (DP), headline, and company contact details.

USE hirehub;

ALTER TABLE users 
    ADD COLUMN IF NOT EXISTS profile_photo VARCHAR(255) DEFAULT NULL;

ALTER TABLE job_seeker_profiles 
    ADD COLUMN IF NOT EXISTS headline VARCHAR(200) DEFAULT NULL;

ALTER TABLE employer_profiles 
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS website VARCHAR(150) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS location VARCHAR(100) DEFAULT NULL;
