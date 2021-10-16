# Migrate from MySQL to Postgres

In summary:

 1. record row count in source
 2. export MySQL table data to CSV files
 3. create empty postgres database
 4. create postgres schema with Liquibase
 5. massage CSV files (notably because of the incompatible way null is exported)
 6. bulk load postgres with copy command
 7. check same number of rows in target as source 

## Record row count

  ```
  select count(1) from OL_ACCOUNT;
  select count(1) from OL_ACCOUNT_CUSTOM;
  select count(1) from OL_ACTIVITY;
  select count(1) from OL_CONTACT;
  select count(1) from OL_CONTACT_CUSTOM;
  select count(1) from OL_DOCUMENT;
  select count(1) from OL_FEEDBACK;
  select count(1) from OL_FEEDBACK_CUSTOM;
  select count(1) from OL_MEDIA_RES;
  select count(1) from OL_MEMO;
  select count(1) from OL_MEMO_DIST;
  select count(1) from OL_MEMO_SIG;
  select count(1) from OL_METRIC;
  select count(1) from OL_NOTE;
  select count(1) from OL_ORDER;
  select count(1) from OL_ORDER_CUSTOM;
  select count(1) from OL_ORDER_ITEM;
  select count(1) from OL_ORDER_ITEM_CUSTOM;
  select count(1) from OL_STOCK_CAT;
  select count(1) from OL_STOCK_CAT_CUSTOM;
  select count(1) from OL_STOCK_ITEM;
  select count(1) from OL_STOCK_ITEM_CUSTOM;
  ```

## Export source tables as CSV

  ```
  select * from OL_ACCOUNT INTO OUTFILE '/var/lib/mysql-files/OL_ACCOUNT.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ACCOUNT_CUSTOM INTO OUTFILE '/var/lib/mysql-files/OL_ACCOUNT_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ACTIVITY INTO OUTFILE '/var/lib/mysql-files/OL_ACTIVITY.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_CONTACT INTO OUTFILE '/var/lib/mysql-files/OL_CONTACT.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_CONTACT_CUSTOM INTO OUTFILE '/var/lib/mysql-files/OL_CONTACT_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_DOCUMENT INTO OUTFILE '/var/lib/mysql-files/OL_DOCUMENT.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_FEEDBACK INTO OUTFILE '/var/lib/mysql-files/OL_FEEDBACK.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_FEEDBACK_CUSTOM INTO OUTFILE '/var/lib/mysql-files/OL_FEEDBACK_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_MEDIA_RES INTO OUTFILE '/var/lib/mysql-files/OL_MEDIA_RES.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_MEMO INTO OUTFILE '/var/lib/mysql-files/OL_MEMO.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_MEMO_DIST INTO OUTFILE '/var/lib/mysql-files/OL_MEMO_DIST.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_MEMO_SIG INTO OUTFILE '/var/lib/mysql-files/OL_MEMO_SIG.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_METRIC INTO OUTFILE '/var/lib/mysql-files/OL_METRIC.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_NOTE INTO OUTFILE '/var/lib/mysql-files/OL_NOTE.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ORDER INTO OUTFILE '/var/lib/mysql-files/OL_ORDER.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ORDER_CUSTOM INTO OUTFILE '/var/lib/mysql-files/OL_ORDER_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ORDER_ITEM INTO OUTFILE '/var/lib/mysql-files/OL_ORDER_ITEM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ORDER_ITEM_CUSTOM INTO OUTFILE '/var/lib/mysql-files/OL_ORDER_ITEM_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_STOCK_CAT INTO OUTFILE '/var/lib/mysql-files/OL_STOCK_CAT.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_STOCK_CAT_CUSTOM INTO OUTFILE '/var/lib/mysql-files/OL_STOCK_CAT_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_STOCK_ITEM INTO OUTFILE '/var/lib/mysql-files/OL_STOCK_ITEM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_STOCK_ITEM_CUSTOM INTO OUTFILE '/var/lib/mysql-files/OL_STOCK_ITEM_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  ```

## Massage and load CSV files  

  ```
  export PGPASSWORD=secret
  
  sed -i 's/"N,/,/g' OL_STOCK_CAT.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_STOCK_CAT FROM ~/git/crm/crm-server/src/main/data/OL_STOCK_CAT.csv WITH CSV"
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_STOCK_CAT_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_STOCK_CAT_CUSTOM.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_STOCK_ITEM.csv
  sed -i 's/,"N$/,/g' OL_STOCK_ITEM.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_STOCK_ITEM FROM ~/git/crm/crm-server/src/main/data/OL_STOCK_ITEM.csv WITH CSV"
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_STOCK_ITEM_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_STOCK_ITEM_CUSTOM.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_METRIC.csv
  sed -i 's/,"N$/,/g' OL_METRIC.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_METRIC FROM ~/git/crm/crm-server/src/main/data/OL_METRIC.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_ACCOUNT.csv
  sed -i 's/,"N$/,/g' OL_ACCOUNT.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_ACCOUNT FROM ~/git/crm/crm-server/src/main/data/OL_ACCOUNT.csv WITH CSV"
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_ACCOUNT_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_ACCOUNT_CUSTOM.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_CONTACT.csv
  sed -i 's/,"N$/,/g' OL_CONTACT.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_CONTACT FROM ~/git/crm/crm-server/src/main/data/OL_CONTACT.csv WITH CSV"
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_CONTACT_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_CONTACT_CUSTOM.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_ACTIVITY.csv
  sed -i 's/,"N$/,/g' OL_ACTIVITY.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_ACTIVITY FROM ~/git/crm/crm-server/src/main/data/OL_ACTIVITY.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_NOTE.csv
  sed -i 's/,"N$/,/g' OL_NOTE.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_NOTE FROM ~/git/crm/crm-server/src/main/data/OL_NOTE.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_ORDER.csv
  sed -i 's/,"N$/,/g' OL_ORDER.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_ORDER FROM ~/git/crm/crm-server/src/main/data/OL_ORDER.csv WITH CSV"
  psql --host 126.0.0.1 --username crm --dbname crm -c "\copy OL_ORDER_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_ORDER_CUSTOM.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_ORDER_ITEM.csv
  sed -i 's/,"N$/,/g' OL_ORDER_ITEM.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_ORDER_ITEM FROM ~/git/crm/crm-server/src/main/data/OL_ORDER_ITEM.csv WITH CSV"
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_ORDER_ITEM_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_ORDER_ITEM_CUSTOM.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_FEEDBACK.csv
  sed -i 's/,"N$/,/g' OL_FEEDBACK.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_FEEDBACK FROM ~/git/crm/crm-server/src/main/data/OL_FEEDBACK.csv WITH CSV"
  
  sed -i 's/"N,/,/g' OL_DOCUMENT.csv
  sed -i 's/,"N$/,/g' OL_DOCUMENT.csv
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_DOCUMENT FROM ~/git/crm/crm-server/src/main/data/OL_DOCUMENT.csv WITH CSV"
  psql --host 127.0.0.1 --username crm --dbname crm -c "\copy OL_FEEDBACK_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_FEEDBACK_CUSTOM.csv WITH CSV"
  ```

## Check source and target count match
