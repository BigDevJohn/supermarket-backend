-- Development/sample data for Supermarket
-- Login passwords:
-- admin@supermarket.local    -> password
-- customer@supermarket.local -> password
-- employee@supermarket.local -> password

INSERT INTO role (id, name) VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_EMPLOYEE'),
    (3, 'ROLE_CUSTOMER');

INSERT INTO users (id, email, name, password) VALUES
    (1, 'admin@supermarket.local', 'Administrator',
     '$2a$10$QGanebpp/Z/M7L2hAU0H2Ohfk7IUFTau/QiwiRu8tkkdaq73gkl/.'),
    (2, 'customer@supermarket.local', 'John Customer',
     '$2a$10$4m0YL8OULRg7CMOeSiJIb.y97AmFGDQ1VDZlMHYnkBOc3tiea3TMO'),
    (3, 'employee@supermarket.local', 'Jane Employee',
     '$2a$10$CFjxInfjA1G5CwDji3STjeAe1jzfcn8u8Q.8WXYbDMNNK6JCGKhkG');

INSERT INTO category (id, name) VALUES
    (1, 'Beverages'),
    (2, 'Bakery'),
    (3, 'Dairy'),
    (4, 'Meat'),
    (5, 'Fruits'),
    (6, 'Vegetables'),
    (7, 'Cleaning'),
    (8, 'Personal Care');

INSERT INTO product (id, available, brand, description, name, price) VALUES
    (1, 1, 'Coca-Cola', 'Carbonated soft drink 2L', 'Coca-Cola 2L', 9.99),
    (2, 1, 'Nestle', 'Milk chocolate bar 90g', 'Milk Chocolate', 6.49),
    (3, 1, 'Itambé', 'Whole milk 1L', 'Whole Milk', 5.79),
    (4, 1, 'Panco', 'Sliced white bread 500g', 'White Bread', 8.49),
    (5, 1, 'Sadia', 'Frozen chicken breast 1kg', 'Chicken Breast', 18.90),
    (6, 1, 'Qualitá', 'Fresh bananas 1kg', 'Banana', 6.99),
    (7, 1, 'Ypê', 'Multi-purpose cleaning product 500ml', 'Multi-purpose Cleaner', 7.99),
    (8, 0, 'Nivea', 'Body moisturizer 400ml', 'Body Moisturizer', 19.90),
    (9, 1, 'Limpol', 'Dishwashing detergent 500ml', 'Dishwashing Detergent', 3.99),
    (10, 1, 'Omo', 'Washing powder 800g', 'Washing Powder', 12.99);

INSERT INTO stock (id, quantity) VALUES
    (1, 50),
    (2, 35),
    (3, 40),
    (4, 25),
    (5, 20),
    (6, 60),
    (7, 30),
    (8, 0),
    (9, 45),
    (10, 25);

INSERT INTO product_category (product_id, category_id) VALUES
    (1, 1),
    (2, 1),
    (2, 3),
    (3, 3),
    (4, 2),
    (5, 4),
    (6, 5),
    (7, 7),
    (8, 8),
    (9, 7),
    (10, 7);

INSERT INTO user_role (user_id, role_id) VALUES
    (1, 1),
    (2, 3),
    (3, 2);

INSERT INTO cart (user_id) VALUES
    (2);

INSERT INTO orders (id, date, status, user_id) VALUES
    (1, '2026-09-01 10:30:00.000000', 'DELIVERED', 2),
    (2, '2026-09-08 14:15:00.000000', 'WAITING_PAYMENT', 2);

INSERT INTO order_item (id, quantity, order_id, product_id) VALUES
    (1, 2, 1, 1),
    (2, 1, 1, 4),
    (3, 1, 2, 3),
    (4, 2, 2, 6);

INSERT INTO cart_item (id, quantity, cart_id, product_id) VALUES
    (1, 2, 2, 3),
    (2, 1, 2, 6);
