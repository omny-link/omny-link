DROP DATABASE bpm_db;
CREATE DATABASE bpm_db;
CREATE USER 'bpm'@'localhost' IDENTIFIED BY 'bpm';
GRANT ALL ON bpm_db.* TO 'bpm'@'localhost';

