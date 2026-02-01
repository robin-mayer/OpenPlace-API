-- liquibase formatted sql

-- changeset Robin.Mayer:1769089624-1
ALTER TABLE addresses
    ADD CONSTRAINT addresses_unique
        UNIQUE (street, house_number, post_code, city);