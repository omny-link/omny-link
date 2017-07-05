-- RENAME TABLES REQUIRED WITH MOVE TO BOOT 1.4; HIBERNATE 5 ETC AND JWT
RENAME TABLE ol_account         TO OL_ACCOUNT;
RENAME TABLE ol_account_custom  TO OL_ACCOUNT_CUSTOM;
RENAME TABLE ol_activity        TO OL_ACTIVITY;
RENAME TABLE ol_contact         TO OL_CONTACT;
RENAME TABLE ol_contact_custom  TO OL_CONTACT_CUSTOM;
RENAME TABLE ol_dmn_model       TO OL_DMN_MODEL;
RENAME TABLE ol_document        TO OL_DOCUMENT;
RENAME TABLE ol_memo            TO OL_MEMO;
RENAME TABLE ol_memo_dist       TO OL_MEMO_DIST;
RENAME TABLE ol_metric          TO OL_METRIC;
RENAME TABLE ol_model_issue     TO OL_MODEL_ISSUE;
RENAME TABLE ol_media_res       TO OL_MEDIA_RES;
RENAME TABLE ol_note            TO OL_NOTE;
RENAME TABLE ol_process_model   TO OL_PROCESS_MODEL;
RENAME TABLE ol_stock_cat       TO OL_STOCK_CAT;
RENAME TABLE ol_stock_item      TO OL_STOCK_ITEM;
RENAME TABLE ol_stock_cat_custom  TO OL_STOCK_CAT_CUSTOM;
RENAME TABLE ol_stock_item_custom TO OL_STOCK_ITEM_CUSTOM;
RENAME TABLE ol_tenant            TO OL_TENANT;

RENAME TABLE ol_order             TO OL_ORDER;
RENAME TABLE ol_order_item        TO OL_ORDER_ITEM;
RENAME TABLE ol_order_custom      TO OL_ORDER_CUSTOM;
RENAME TABLE ol_order_item_custom TO OL_ORDER_ITEM_CUSTOM;
RENAME TABLE ol_feedback          TO OL_FEEDBACK;
RENAME TABLE ol_feedback_custom   TO OL_FEEDBACK_CUSTOM;

ALTER TABLE OL_PROCESS_MODEL change column diagram_resource_name diag_resource_name longtext;

insert into ACT_ID_INFO
  select UUID(), 1, user_id_, type_, 'jwt-login-url',
      'https://api.omny.link/auth/login', null, null
  from ACT_ID_INFO where key_ = 'cust-mgmt-url';

update ACT_ID_GROUP set name_ = id_ where name_ is null;

| ol_stock_item | CREATE TABLE `ol_stock_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` text,
  `map_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `size` varchar(255) DEFAULT NULL,
  `tenant_id` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` datetime DEFAULT NULL,
  `stock_cat_id` bigint(20) DEFAULT NULL,
  `price` decimal(19,2) DEFAULT NULL,
  `status` varchar(255) DEFAU,LT NULL,
  `tags` varchar(255) DEFAULT NULL,
  `offer_call_to_action` varchar(30) DEFAULT NULL,
  `offer_description` varchar(80) DEFAULT NULL,
  `offer_status` varchar(20) DEFAULT NULL,
  `offer_title` varchar(35) DEFAULT NULL,
  `offer_url` varchar(255) DEFAULT NULL,
  `video_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_4t99x5u33cb2hiaoaqg5wipd4` (`stock_cat_id`),
  CONSTRAINT `ol_stock_item_ibfk_1` FOREIGN KEY (`stock_cat_id`) REFERENCES `ol_stock_cat` (`id`)
)

ALTER TABLE `OL_STOCK_ITEM` add column `last_updated` datetime DEFAULT NULL;
ALTER TABLE `OL_STOCK_ITEM` add column  `offer_call_to_action` varchar(30) DEFAULT NULL;
ALTER TABLE `OL_STOCK_ITEM` add column  `offer_description` varchar(80) DEFAULT NULL;
ALTER TABLE `OL_STOCK_ITEM` add column  `offer_status` varchar(20) DEFAULT NULL;
ALTER TABLE `OL_STOCK_ITEM` add column  `offer_title` varchar(35) DEFAULT NULL;
ALTER TABLE `OL_STOCK_ITEM` add column  `offer_url` varchar(255) DEFAULT NULL;
ALTER TABLE `OL_STOCK_ITEM` add column  `video_code` varchar(255) DEFAULT NULL;

ALTER TABLE `OL_STOCK_ITEM` change column `offer_call_to_action` `offer_cta` varchar(30) DEFAULT NULL;
ALTER TABLE `OL_STOCK_ITEM` change column `offer_description` `offer_desc` varchar(80) DEFAULT NULL;

ALTER TABLE `OL_STOCK_CAT_CUSTOM` add column `stock_cat_id` bigint(20) DEFAULT NULL;
update OL_STOCK_CAT_CUSTOM set stock_cat_id = stock_category_id;
ALTER TABLE `OL_STOCK_CAT_CUSTOM` drop foreign key `FK_5b80v07xl4bms0s2cfwe46k2r`;


ALTER TABLE OL_ACCOUNT add column `parent_org` varchar(100) DEFAULT NULL;
