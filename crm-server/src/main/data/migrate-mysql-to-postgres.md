# Migrate from MySQL to Postgres

In summary:

 1. record row count in source
 2. export MySQL table data to CSV files
 3. create empty postgres database
 4. create postgres schema with Liquibase
 5. massage CSV files (notably because of the incompatible way null is exported)
 6. bulk load postgres with copy command
 7. check same number of rows in target as source

NOTE: actual CSV files used are stored in shared drive.

## Record row count

  ```
  select count(1) from OL_ACCOUNT where tenant_id = 'alife';
  select count(1) from OL_ACCOUNT_CUSTOM where account_id in (SELECT id FROM OL_ACCOUNT WHERE tenant_id = 'alife');
  select count(1) from OL_ACTIVITY where tenant_id = 'alife';
  select count(1) from OL_CONTACT where tenant_id = 'alife';
  select count(1) from OL_CONTACT_CUSTOM where contact_id in (SELECT id FROM OL_CONTACT WHERE tenant_id = 'alife');
  select count(1) from OL_DOCUMENT where tenant_id = 'alife';
  select count(1) from OL_FEEDBACK where tenant_id = 'alife';
  select count(1) from OL_FEEDBACK_CUSTOM where feedback_id in (SELECT id FROM OL_FEEDBACK WHERE tenant_id = 'alife');
  select count(1) from OL_MEDIA_RES where tenant_id = 'alife';
  select count(1) from OL_MEMO where tenant_id = 'alife';
  select count(1) from OL_MEMO_DIST where tenant_id = 'alife';
  select count(1) from OL_MEMO_SIG where tenant_id = 'alife';
  select count(1) from OL_METRIC where tenant_id = 'alife';
  select count(1) from OL_NOTE where tenant_id = 'alife';
  select count(1) from OL_ORDER where tenant_id = 'alife';
  select count(1) from OL_ORDER_CUSTOM where order_id in (SELECT id FROM OL_ORDER WHERE tenant_id = 'alife');
  select count(1) from OL_ORDER_ITEM where tenant_id = 'alife';
  select count(1) from OL_ORDER_ITEM_CUSTOM where order_item_id in (SELECT id FROM OL_ORDER_ITEM WHERE tenant_id = 'alife');

  select count(1) from OL_STOCK_CAT where tenant_id = 'alife';
  select count(1) from OL_STOCK_CAT_CUSTOM where stock_cat_id in (SELECT id FROM OL_STOCK_CAT WHERE tenant_id = 'alife');
  select count(1) from OL_STOCK_ITEM where tenant_id = 'alife';
  select count(1) from OL_STOCK_ITEM_CUSTOM where stock_item_id in (SELECT id FROM OL_STOCK_ITEM WHERE tenant_id = 'alife');

  select count(1) from ACT_RU_TASK where tenant_id_ = 'alife';
  select count(1) from ACT_RU_VARIABLE where TASK_ID_ in (SELECT ID_ FROM ACT_RU_TASK WHERE tenant_id_ = 'alife');

  -- filtering on proc def makes no diff.
  select count(1) from ACT_RU_TASK where tenant_id_ = 'alife' and PROC_DEF_ID_ like 'SimpleTo%';
  select * from ACT_RU_VARIABLE where PROC_INST_ID_ in (SELECT PROC_INST_ID_ FROM ACT_RU_TASK WHERE tenant_id_ = 'alife' and PROC_DEF_ID_ like 'SimpleTo%');


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

## Clean data

```
update OL_ACCOUNT set description = REPLACE(IFNULL(description,''), '\r\n' , '\n') where tenant_id  = 'alife';
```

## Export source tables as CSV

  ```
  select * from OL_ACCOUNT where tenant_id = 'alife' INTO OUTFILE '/var/lib/mysql-files/OL_ACCOUNT.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ACCOUNT_CUSTOM where account_id in (select id from OL_ACCOUNT where tenant_id = 'alife') INTO OUTFILE '/var/lib/mysql-files/OL_ACCOUNT_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ACTIVITY where (contact_id is not null and contact_id in (select id from
OL_CONTACT where tenant_id = 'alife')) or (account_id is not null and account_id in (select id from OL_ACCOUNT where tenant_id = 'alife')) INTO OUTFILE '/var/lib/mysql-files/OL_ACTIVITY.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_CONTACT where tenant_id = 'alife' INTO OUTFILE '/var/lib/mysql-files/OL_CONTACT.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_CONTACT_CUSTOM where contact_id in (select id from OL_CONTACT where tenant_id = 'alife') INTO OUTFILE '/var/lib/mysql-files/OL_CONTACT_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_DOCUMENT where (contact_id is not null and contact_id in (select id from OL_CONTACT where tenant_id = 'alife')) or (account_id is not null and account_id in (select id from OL_ACCOUNT
where tenant_id = 'alife')) INTO OUTFILE '/var/lib/mysql-files/OL_DOCUMENT.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_FEEDBACK where tenant_id = 'alife' INTO OUTFILE '/var/lib/mysql-files/OL_FEEDBACK.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_FEEDBACK_CUSTOM where feedback_id in (select id from OL_FEEDBACK where tenant_id = 'alife') INTO OUTFILE '/var/lib/mysql-files/OL_FEEDBACK_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_NOTE where (contact_id is not null and contact_id in (select id from
OL_CONTACT where tenant_id = 'alife')) or (account_id is not null and account_id in (select id from OL_ACCOUNT where tenant_id = 'alife')) INTO OUTFILE '/var/lib/mysql-files/OL_NOTE.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ORDER where tenant_id = 'alife' INTO OUTFILE '/var/lib/mysql-files/OL_ORDER.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ORDER_CUSTOM where order_id in (select id from OL_ORDER where tenant_id = 'alife') INTO OUTFILE '/var/lib/mysql-files/OL_ORDER_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ORDER_ITEM where tenant_id = 'alife' INTO OUTFILE '/var/lib/mysql-files/OL_ORDER_ITEM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_ORDER_ITEM_CUSTOM where order_item_id in (select id from OL_ORDER_ITEM where tenant_id = 'alife') INTO OUTFILE '/var/lib/mysql-files/OL_ORDER_ITEM_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_STOCK_CAT where tenant_id = 'alife' INTO OUTFILE '/var/lib/mysql-files/OL_STOCK_CAT.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_STOCK_CAT_CUSTOM where stock_cat_id in (select id from OL_STOCK_CAT where tenant_id = 'alife') INTO OUTFILE '/var/lib/mysql-files/OL_STOCK_CAT_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_STOCK_ITEM where tenant_id = 'alife' INTO OUTFILE '/var/lib/mysql-files/OL_STOCK_ITEM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_STOCK_ITEM_CUSTOM where stock_item_id in (select id from OL_STOCK_ITEM where tenant_id = 'alife') INTO OUTFILE '/var/lib/mysql-files/OL_STOCK_ITEM_CUSTOM.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

  select * from OL_MEMO where tenant_id = 'alife' INTO OUTFILE '/var/lib/mysql-files/OL_MEMO.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from OL_METRIC INTO OUTFILE '/var/lib/mysql-files/OL_METRIC.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

  select * from ACT_RU_TASK where tenant_id_ = 'alife' and PROC_DEF_ID_ like 'SimpleTo%' INTO OUTFILE '/var/lib/mysql-files/ACT_RU_TASK.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from ACT_RU_VARIABLE where PROC_INST_ID_ in (SELECT PROC_INST_ID_ FROM ACT_RU_TASK WHERE tenant_id_ = 'alife' and PROC_DEF_ID_ like 'SimpleTo%') INTO OUTFILE '/var/lib/mysql-files/ACT_RU_VARIABLE.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
  select * from ACT_RU_IDENTITYLINK where PROC_INST_ID_ in (SELECT PROC_INST_ID_ FROM ACT_RU_TASK WHERE tenant_id_ = 'alife' and PROC_DEF_ID_ like 'SimpleTo%') INTO OUTFILE '/var/lib/mysql-files/ACT_RU_IDENTITYLINK.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

  select v.id_, v.rev_, v.type_, v.name_, '','', t.id_,'','','',bytearray_id_,double_,long_,text_,text2_ from ACT_RU_VARIABLE v INNER JOIN ACT_RU_TASK t on v.proc_inst_id_ = t.proc_inst_id_ where v.PROC_INST_ID_ in (SELECT PROC_INST_ID_ FROM ACT_RU_TASK  WHERE tenant_id_ = 'alife' and PROC_DEF_ID_ like 'SimpleTo%') INTO OUTFILE '/var/lib/mysql-files/ACT_RU_VARIABLE.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ','
ESCAPED BY '"' LINES TERMINATED BY '\n';

  ```

## Massage and load CSV files

  ```
  export PGPASSWORD=secret

  sed -i 's/"N,/,/g' OL_STOCK_CAT.csv
  sed -i 's/$/,"migration","migration"/g' OL_STOCK_CAT.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_STOCK_CAT FROM ~/git/crm/crm-server/src/main/data/OL_STOCK_CAT.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_STOCK_ITEM.csv
  sed -i 's/,"N$/,/g' OL_STOCK_ITEM.csv
  sed -i 's/$/,"migration","migration"/g' OL_STOCK_ITEM.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_STOCK_ITEM FROM ~/git/crm/crm-server/src/main/data/OL_STOCK_ITEM.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_ACCOUNT.csv
  sed -i 's/,"N$/,/g' OL_ACCOUNT.csv
  sed -i 's/$/,,,"migration","migration"/g' OL_ACCOUNT.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_ACCOUNT FROM ~/git/crm/crm-server/src/main/data/OL_ACCOUNT.csv WITH CSV"
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_ACCOUNT_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_ACCOUNT_CUSTOM.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_CONTACT.csv
  sed -i 's/,"N$/,/g' OL_CONTACT.csv
  sed -i 's/$/,,"migration","migration"/g' OL_CONTACT.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_CONTACT FROM ~/git/crm/crm-server/src/main/data/OL_CONTACT.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_ORDER.csv
  sed -i 's/,"N$/,/g' OL_ORDER.csv
  sed -i 's/$/,"migration","migration",/g' OL_ORDER.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_ORDER FROM ~/git/crm/crm-server/src/main/data/OL_ORDER.csv WITH CSV"
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_ORDER_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_ORDER_CUSTOM.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_ORDER_ITEM.csv
  sed -i 's/,"N$/,/g' OL_ORDER_ITEM.csv
  sed -i 's/$/,"migration","migration"/g' OL_ORDER_ITEM.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_ORDER_ITEM FROM ~/git/crm/crm-server/src/main/data/OL_ORDER_ITEM.csv WITH CSV"
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_ORDER_ITEM_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_ORDER_ITEM_CUSTOM.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_FEEDBACK.csv
  sed -i 's/,"N$/,/g' OL_FEEDBACK.csv
  sed -i 's/$/,"migration","migration"/g' OL_FEEDBACK.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_FEEDBACK FROM ~/git/crm/crm-server/src/main/data/OL_FEEDBACK.csv WITH CSV"
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_FEEDBACK_CUSTOM FROM ~/git/crm/crm-server/src/main/data/OL_FEEDBACK_CUSTOM.csv WITH CSV"

  sed -i 's/"N,/,/g' OL_ACCOUNT2.csv
  sed -i 's/,"N$/,/g' OL_ACCOUNT2.csv
  sed -i 's/$/,,,"migration","migration"/g' OL_ACCOUNT2.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_ACCOUNT FROM ~/git/crm/crm-server/src/main/data/OL_ACCOUNT2.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_ACTIVITY.csv
  sed -i 's/,"N$/,/g' OL_ACTIVITY.csv
  sed -i 's/$/,"migration","migration"/g' OL_ACTIVITY.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_ACTIVITY FROM ~/git/crm/crm-server/src/main/data/OL_ACTIVITY.csv WITH CSV"

  sed -i 's/"N,/,/g' OL_NOTE.csv
  sed -i 's/,"N$/,/g' OL_NOTE.csv
  sed -i 's/$/,"migration",,"migration"/g' OL_NOTE.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_NOTE FROM ~/git/crm/crm-server/src/main/data/OL_NOTE.csv WITH CSV"
  sed -i 's/"N,/,/g' OL_DOCUMENT.csv
  sed -i 's/,"N$/,/g' OL_DOCUMENT.csv
  sed -i 's/$/,"migration",,"migration"/g' OL_DOCUMENT.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_DOCUMENT FROM ~/git/crm/crm-server/src/main/data/OL_DOCUMENT.csv WITH CSV"

  sed -i 's/"N,/,/g' OL_METRIC.csv
  sed -i 's/,"N$/,/g' OL_METRIC.csv
  psql --host 127.0.0.1 --username crm --dbname crm4 -c "\copy OL_METRIC FROM ~/git/crm/crm-server/src/main/data/OL_METRIC.csv WITH CSV"

  sed -i 's/"N,/,/g' ACT_RU_TASK.csv
  sed -i 's/,"N$/,/g' ACT_RU_TASK.csv
  sed -i 's/$/,,,,,,/g' ACT_RU_TASK.csv
  # manually clear exec, proc inst and proc def cols and insert 6 empty ones before name
  psql --host 127.0.0.1 --username flowable --dbname flowable -c "\copy ACT_RU_TASK FROM ~/git/crm/crm-server/src/main/data/ACT_RU_TASK.csv WITH CSV"
  sed -i 's/"N,/,/g' ACT_RU_IDENTITYLINK.csv
  sed -i 's/,"N$/,/g' ACT_RU_IDENTITYLINK.csv
  sed -i 's/$/,,,,/g' ACT_RU_IDENTITYLINK.csv
  # manually clear exec, proc inst and proc def cols
  psql --host 127.0.0.1 --username flowable --dbname flowable -c "\copy ACT_RU_IDENTITYLINK FROM ~/git/crm/crm-server/src/main/data/ACT_RU_IDENTITYLINK.csv WITH CSV"
  sed -i 's/"N,/,/g' ACT_RU_VARIABLE.csv
  sed -i 's/,"N$/,/g' ACT_RU_VARIABLE.csv
  sed -i 's/,"",/,,/g' ACT_RU_VARIABLE.csv
  sed -i 's/,"",/,,/g' ACT_RU_VARIABLE.csv
  psql --host 127.0.0.1 --username flowable --dbname flowable -c "\copy ACT_RU_VARIABLE FROM ~/git/crm/crm-server/src/main/data/ACT_RU_VARIABLE.csv WITH CSV"
  ```

## Check source and target count match

## synthesize business key as task variable
select concat(
              'INSERT INTO act_ru_variable (id_,rev_,type_,name_,task_id_,text_)',
              ' values(uuid_generate_v4(),1,\'string\',\'businessKey\',',
              t.id_,
              ',\'',
               name,
              '\');'
       ) 
from ACT_RU_VARIABLE v
     INNER JOIN ACT_RU_TASK t on v.proc_inst_id_ = t.proc_inst_id_
     inner join OL_ACCOUNT a on a.id = v.text_ 
where v.name_ = 'accountlocalId'
limit 5;

   INNER JOIN ACT_RU_TASK t on v.proc_inst_id_ = t.proc_inst_id_ where v.PROC_INST_ID_ in (SELECT PROC_INST_ID_ FROM ACT_RU_TASK  WHERE tenant_id_ = 'alife' and PROC_DEF_ID_ like 'SimpleTo%') INTO OUTFILE '/var/lib/mysql-files/ACT_RU_VARIABLE.csv' FIELDS ENCLOSED BY '"' TERMINATED BY ','
