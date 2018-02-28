DELIMITER $$
DROP PROCEDURE IF EXISTS `cust_db`.`sp_status` $$
CREATE PROCEDURE `cust_db`.`sp_status`(dbname VARCHAR(50))
BEGIN 
-- Obtaining tables and views
(
    SELECT 
     TABLE_NAME AS `Table Name`, 
     ENGINE AS `Engine`,
     TABLE_ROWS AS `Rows`,
     CONCAT(
        (FORMAT((DATA_LENGTH + INDEX_LENGTH) / POWER(1024,2),2))
        , ' Mb')
       AS `Size`,
     TABLE_COLLATION AS `Collation`
    FROM information_schema.TABLES
    WHERE TABLES.TABLE_SCHEMA = dbname
          AND TABLES.TABLE_TYPE = 'BASE TABLE'
)
UNION
(
    SELECT 
     TABLE_NAME AS `Table Name`, 
     '[VIEW]' AS `Engine`,
     '-' AS `Rows`,
     '-' `Size`,
     '-' AS `Collation`
    FROM information_schema.TABLES
    WHERE TABLES.TABLE_SCHEMA = dbname 
          AND TABLES.TABLE_TYPE = 'VIEW'
)
ORDER BY 1;
-- Obtaining functions, procedures and triggers
(
    SELECT ROUTINE_NAME AS `Routine Name`, 
     ROUTINE_TYPE AS `Type`,
     '' AS `Comment`
    FROM information_schema.ROUTINES
    WHERE ROUTINE_SCHEMA = dbname
    ORDER BY ROUTINES.ROUTINE_TYPE, ROUTINES.ROUTINE_NAME
)
UNION
(
    SELECT TRIGGER_NAME,'TRIGGER' AS `Type`, 
    concat('On ',EVENT_MANIPULATION,': ',EVENT_OBJECT_TABLE) AS `Comment`
    FROM information_schema.TRIGGERS
    WHERE EVENT_OBJECT_SCHEMA = dbname
)
ORDER BY 2,1;
END$$
DELIMITER ;