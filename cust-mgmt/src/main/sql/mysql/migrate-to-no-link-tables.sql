insert into ol_account select * from account where tenant_id='firmgains';
insert into ol_account (
  `id`,
  `aliases`,
  `business_website`,
  `company_number`,
  `description`,
  `incorporation_year`,
  `name`,
  `no_of_employees`,
  `short_desc`,
  `tenant_id`,
  `first_contact`,
  `last_updated`
)
select 
  `id`,
  `aliases`,
  `business_website`,
  `company_number`,
  `description`,
  `incorporation_year`,
  `name`,
  `no_of_employees`,
  `short_desc`,
  `tenant_id`,
  `first_contact`,
  `last_updated`
from account where tenant_id='omny' 
and id in ( select account_id from contact where tenant_id = 'omny' and id not in (select id from ol_contact) and (stage is null)) ;

insert into ol_contact (
  `id`,
  `account_type`,
  `address1`,
  `address2`,
  `campaign`,
  `county_or_city`,
  `do_not_call`,
  `do_not_email`,
  `email`,
  `enquiry_type`,
  `first_contact`,
  `first_name`,
  `keyword`,
  `last_name`,
  `last_updated`,
  `medium`,
  `owner`,
  `phone1`,
  `phone2`,
  `post_code`,
  `source`,
  `stage`,
  `tenant_id`,
  `title`,
  `account_id`,
  `town`,
  `country`,
  `tags`,
  `email_confirmed`
)
select
`id`,
`account_type`,
`address1`,
`address2`,
`campaign`,
`county_or_city`,
`do_not_call`,
`do_not_email`,
`email`,
`enquiry_type`,
`first_contact`,
`first_name`,
`keyword`,
`last_name`,
`last_updated`,
`medium`,
`owner`,
`phone1`,
`phone2`,
`post_code`,
`source`,
`stage`,
`tenant_id`,
`title`,
`account_id`,
`town`,
`country`,
`tags`,
`email_confirmed`
 from contact 
 where tenant_id='omny'
 and id not in (select id from ol_contact) and (stage is null) ;

update ol_contact set stage = 'Cold' where account_id between 1495 and 1506;

update ol_contact set stage = 'Cold' where account_id = 1508;
update ol_contact set stage = 'Cold' where account_id between 1511 and 1521;
update ol_contact set stage = 'Cold' where account_id between 1523 and 1532;
update ol_contact set stage = 'Cold' where account_id between 1534 and 1570;
update ol_contact set stage = 'Cold' where account_id between 1574 and 1787;


insert into ol_contact_custom
   select cf.id,cf.name,cf.value,link.contact_id from custom_contact_field cf, contact_custom_fields link where cf.id = link.custom_fields_id and link.contact_id in (select id from ol_contact);

 insert into ol_account_custom
    select cf.id,cf.name,cf.value,link.account_id from custom_account_field cf, account_custom_fields link where cf.id = link.custom_fields_id and link.account_id in (select id from ol_account);


insert into ol_note ( 
  `id`,
  `author`,
  `content`,
  `created`,
  `contact_id`
 ) 
 select 
  `id`,
  `author`,
  `content`,
  `created`,
  `contact_id`
  from note where contact_id in (select id from ol_contact) and id not in (select id from ol_note);

insert into ol_activity select * from activity where contact_id in (select id from ol_contact) and id not in (select id from ol_activity);

insert into ol_document select * from document where contact_id in (select id from ol_contact) and id not in (select id from ol_document);
