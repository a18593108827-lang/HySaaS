ALTER TABLE evt_event
    ADD COLUMN registration_fee DECIMAL(10, 2) NOT NULL DEFAULT 0 AFTER hotel_enabled;
