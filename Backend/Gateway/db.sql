CREATE SCHEMA gateway;

CREATE TABLE `gateway`.`request` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `ip_address` VARCHAR(64) NOT NULL,
    `call_time` TIMESTAMP NOT NULL,
    `path` VARCHAR(2048) NULL,
    PRIMARY KEY (`id`));