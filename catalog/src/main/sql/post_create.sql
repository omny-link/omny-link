

ALTER TABLE ol_media_res ADD CONSTRAINT FK_STOCK_ITEM_ID FOREIGN KEY (`stock_item_id`) REFERENCES `ol_stock_item` (`id`);

ALTER TABLE ol_media_res ADD CONSTRAINT FK_STOCK_CAT_ID FOREIGN KEY (`stock_cat_id`) REFERENCES `ol_stock_cat` (`id`);

ALTER TABLE OL_STOCK_CAT
ADD CONSTRAINT UK_NAME_TENANT_ID unique (name,tenant_id);


ALTER TABLE OL_STOCK_ITEM
ADD CONSTRAINT UK_NAME_TENANT_ID unique (name,tenant_id);
