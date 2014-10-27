-- DROP database activitidb; 

CREATE DATABASE activitidb;

CREATE USER 'activiti'@'localhost' IDENTIFIED BY 'activiti';  
GRANT ALL ON activitidb.* TO 'activiti'@'localhost'; 

