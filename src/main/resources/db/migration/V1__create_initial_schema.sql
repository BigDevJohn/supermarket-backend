-- Initial database schema for Supermarket
-- Generated from the current MySQL schema and cleaned for Flyway.

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    available BIT(1) DEFAULT NULL,
    brand VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(38,2) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cart (
    user_id BIGINT NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    date DATETIME(6) DEFAULT NULL,
    status ENUM('CANCELLED','DELIVERED','PREPARING','SHIPPED','WAITING_PAYMENT') DEFAULT NULL,
    user_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_orders_user_id (user_id),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    download_url VARCHAR(255) DEFAULT NULL,
    file_name VARCHAR(255) DEFAULT NULL,
    file_type VARCHAR(255) DEFAULT NULL,
    image LONGBLOB,
    product_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_image_product_id (product_id),
    CONSTRAINT fk_image_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE stock (
    id BIGINT NOT NULL,
    quantity INT DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_product
        FOREIGN KEY (id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE product_category (
    product_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, category_id),
    KEY idx_product_category_category_id (category_id),
    CONSTRAINT fk_product_category_product
        FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_product_category_category
        FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cart_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    quantity INT NOT NULL,
    cart_id BIGINT DEFAULT NULL,
    product_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_cart_item_cart_id (cart_id),
    KEY idx_cart_item_product_id (product_id),
    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id) REFERENCES cart (user_id),
    CONSTRAINT fk_cart_item_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE order_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    quantity INT NOT NULL,
    order_id BIGINT DEFAULT NULL,
    product_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_order_item_order_id (order_id),
    KEY idx_order_item_product_id (product_id),
    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_role_role_id (role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
