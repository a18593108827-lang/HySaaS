USE hysaas;

ALTER TABLE inv_invoice ADD COLUMN email VARCHAR(128) AFTER tax_no;
