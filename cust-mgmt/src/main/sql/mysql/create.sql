DROP DATABASE cust_db;
CREATE DATABASE cust_db;
-- CREATE USER 'kp'@'localhost' IDENTIFIED BY 'kp';  
GRANT ALL ON cust_db.* TO 'kp'@'localhost'; 

//DROP INDEX AK_FULL_NAME ON contact ; 
//CREATE UNIQUE INDEX AK_FULL_NAME ON model (name, namespace);
