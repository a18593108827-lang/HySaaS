USE hysaas;

ALTER TABLE hotel_booking
    ADD COLUMN booking_no VARCHAR(32) NULL AFTER room_type_id,
    ADD COLUMN nights INT NOT NULL DEFAULT 1 AFTER amount,
    ADD COLUMN checked_in_at DATETIME NULL AFTER updated_at;

ALTER TABLE pay_order
    ADD COLUMN order_no VARCHAR(64) NULL AFTER id,
    ADD COLUMN invoice_status VARCHAR(16) NOT NULL DEFAULT 'NONE' AFTER status;
