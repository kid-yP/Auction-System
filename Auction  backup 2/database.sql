CREATE DATABASE auction_system2;
USE auction_system2;

-- Users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('admin', 'buyer', 'seller') NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Items table (with auction_type)
CREATE TABLE items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    category_id INT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    starting_price DECIMAL(10, 2) NOT NULL,
    current_price DECIMAL(10, 2) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('active', 'ended', 'sold', 'cancelled') DEFAULT 'active',
    image_path VARCHAR(255),
    auction_type ENUM('ascending', 'sealed') NOT NULL DEFAULT 'ascending',
    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- Bids table
CREATE TABLE bids (
    bid_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    buyer_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    bid_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('winning', 'outbid', 'won', 'lost') DEFAULT 'winning',
    FOREIGN KEY (item_id) REFERENCES items(item_id),
    FOREIGN KEY (buyer_id) REFERENCES users(user_id)
);

-- Transactions table
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT DEFAULT 1,
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES items(item_id)
);

-- Populate categories table with broad, high-level categories
INSERT INTO categories (name, description) VALUES
('Electronics', 'Devices, gadgets, and accessories including phones, computers, cameras, and audio equipment.'),
('Home & Garden', 'Furniture, appliances, decor, tools, and gardening supplies.'),
('Fashion', 'Clothing, shoes, accessories, and jewelry for all genders and ages.'),
('Vehicles', 'Cars, motorcycles, bicycles, boats, and vehicle parts.'),
('Collectibles', 'Antiques, coins, stamps, memorabilia, and rare items.'),
('Art', 'Paintings, sculptures, prints, and other artwork.'),
('Sports & Outdoors', 'Sporting goods, fitness equipment, camping, and outdoor gear.'),
('Toys & Games', 'Toys, board games, video games, and puzzles for all ages.'),
('Books, Movies & Music', 'Books, magazines, movies, music, and related media.'),
('Health & Beauty', 'Cosmetics, skincare, personal care, and health products.'),
('Business & Industrial', 'Office equipment, industrial tools, and business supplies.'),
('Baby & Kids', 'Baby gear, kids clothing, toys, and accessories.'),
('Pet Supplies', 'Products for pets including food, toys, and accessories.'),
('Real Estate', 'Residential, commercial, and land properties.'),
('Food & Beverages', 'Gourmet food, wine, and beverages.');