-- Fix all users with null institute_id
-- First, ensure Default Institute exists
INSERT IGNORE INTO institute (name, status) 
VALUES ('Default Institute', 'ACTIVE');

-- Update all users with null institute_id to use the Default Institute
UPDATE users 
SET institute_id = (SELECT id FROM institute WHERE name = 'Default Institute' LIMIT 1)
WHERE institute_id IS NULL;

-- Verify the fix
SELECT id, email, full_name, role, institute_id FROM users;
