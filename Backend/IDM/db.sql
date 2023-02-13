CREATE SCHEMA idm;


CREATE TABLE `idm`.`token_status` (
                                      `id` INT NOT NULL,
                                      `value` VARCHAR(32) NOT NULL,
                                      PRIMARY KEY (`id`));

CREATE TABLE `idm`.`user_status` (
                                     `id` INT NOT NULL,
                                     `value` VARCHAR(32) NOT NULL,
                                     PRIMARY KEY (`id`));


CREATE TABLE `idm`.`role` (
                              `id` INT NOT NULL,
                              `name` VARCHAR(32) NOT NULL,
                              `description` VARCHAR(128) NOT NULL,
                              `precedence` INT NOT NULL,
                              PRIMARY KEY (`id`));

CREATE TABLE `idm`.`user` (
                              `id` INT NOT NULL AUTO_INCREMENT,
                              `email` VARCHAR(32) NOT NULL,
                              `user_status_id` INT NOT NULL,
                              `salt` CHAR(8) NOT NULL,
                              `hashed_password` CHAR(88) NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE,
                              FOREIGN KEY (`user_status_id`)
                                  REFERENCES `idm`.`user_status` (`id`)
                                  ON DELETE RESTRICT
                                  ON UPDATE CASCADE);


CREATE TABLE `idm`.`refresh_token` (
                                       `id` INT NOT NULL AUTO_INCREMENT,
                                       `token` CHAR(36) NOT NULL,
                                       `user_id` INT NOT NULL,
                                       `token_status_id` INT NOT NULL,
                                       `expire_time` TIMESTAMP NOT NULL,
                                       `max_life_time` TIMESTAMP NOT NULL,
                                       PRIMARY KEY (`id`),
                                       UNIQUE INDEX `token_UNIQUE` (`token` ASC) VISIBLE,
                                       FOREIGN KEY (`user_id`)
                                           REFERENCES `idm`.`user` (`id`)
                                           ON DELETE CASCADE
                                           ON UPDATE CASCADE,
                                       FOREIGN KEY (`token_status_id`)
                                           REFERENCES `idm`.`token_status` (`id`)
                                           ON DELETE RESTRICT
                                           ON UPDATE CASCADE);


CREATE TABLE `idm`.`user_role` (
                                   `user_id` INT NOT NULL,
                                   `role_id` INT NOT NULL,
                                   PRIMARY KEY (`user_id`, `role_id`),
                                   FOREIGN KEY (`user_id`)
                                       REFERENCES `idm`.`user` (`id`)
                                       ON DELETE CASCADE
                                       ON UPDATE CASCADE,
                                   FOREIGN KEY (`role_id`)
                                       REFERENCES `idm`.`role` (`id`)
                                       ON DELETE RESTRICT
                                       ON UPDATE CASCADE);