-- ids to mail 
select id 
 from contact 
 where tenant_id = 'client1' and id > 1670 and stage not in( 'deleted','On hold','Cold' ) and do_not_email = false;
 
select concat('sleep 2; curl  -u client1:client1 http://USR:PWD@api.knowprocess.com:8082/msg/client1/client1.followUp.json?query=%7B%22contactId%22%3A%22http%3A%2F%2Fapi.knowprocess.com%3A8082%2Fcontacts%2F',id,'%22%2C%22tenantId%22%3A%22client1%22%7D&businessDescription=FG+email') 
 from contact 
 where tenant_id = 'client1' and id > 1670 and stage not in( 'deleted','On hold','Cold' ) and do_not_email = false
 INTO OUTFILE '/var/tmp/followup.sh'
 
 
-- Find notes that hold a valuation but no activity for it 
select contact_id from note 
where contact_id in (select id from contact where tenant_id = 'client1') 
and content like '%Valuation performed%'
and contact_id not in (select id from activity where type = 'valuation');

-- add activity for notes 
 insert into activity (content, occurred, type, contact_id) 
select 'Valuation performed, see notes for details', created, 'valuation', contact_id
from note where contact_id in (
  select id from contact where tenant_id = 'client1') 
and content like '%Valuation performed%'
and contact_id not in (select id from activity where type = 'valuation');

-- set enquiry type = valuation where it was missed
update  contact c, note n set enquiry_type = 'Valuation' where enquiry_type is null and tenant_id = 'client1' and n.contact_id = c.id; 
-- Finding duplicates
select id, concat(first_name, last_name) as 'name' from contact group by name having count(name) >1;

-- Non duplicates 
select id, concat(first_name, last_name) as 'name' from contact group by name having count(name) =1;

 create table tmp_preserve (`id` bigint(20) NOT NULL, name varchar(255));
insert into tmp_preserve 
select max(id), concat(first_name, last_name) as 'name' from contact group by name having count(name) >1;

insert into tmp_preserve 
select id, concat(first_name, last_name) as 'name' from contact group by name having count(name) =1;
 
-- clean up 
 update contact set stage = 'Enquiry' where stage is null;

 insert into activity (content, last_updated, occurred, type, contact_id) 
select 'Valuation, see notes for details', last_updated, first_contact, 'Valuation', id
from contact where enquiry_type like 'Valuation';

insert into activity (content, last_updated, occurred, type, contact_id) 
select 'Registered account', last_updated, first_contact, 'Registration', id
from contact where enquiry_type like 'Registration';

 
-- logical delete all but most recent duplicate 
update contact set stage = 'deleted' 
where  tenant_id = 'client1' and id not in (select id from tmp_preserve) ; 

-- migration 

select value from custom_contact_field where name ='accountName';

  select a.id, a.name, ccf.name
  from account a, custom_contact_field ccf 
  where ccf.name ='accountName',  and ccf.value = a.name


update contact c  set account_id = ( 
  select a.id
  from contact_custom_fields l, custom_contact_field ccf, account a 
  where l.custom_fields_id = ccf.id
  and ccf.name ='accountName',  and ccf.value = a.name
  and l.contact_id = c.id
  group by l.contact_id
);


  select l.contact_id, ccf.value, a.name
  from contact_custom_fields l, custom_contact_field ccf, account a 
  where l.custom_fields_id = ccf.id
  and ccf.name ='accountName',  and ccf.value = a.name
  group by contact_id;

  
  select c.email,c.first_name,c.last_name, f.value
  from contact c, contact_custom_fields l, custom_contact_field f 
  where c.id = l.contact_id 
  and l.custom_fields_id = f.id
  and f.name ='sugarId';
  
  c.email,c.first_name,c.last_name, 
  
  select concat('update wp_usermeta set meta_value = \'http://api.knowprocess.com:8082/contacts/',c.id,'\' where meta_value = \'',f.value,'\';')
  from contact c, contact_custom_fields l, custom_contact_field f 
  where c.id = l.contact_id 
  and l.custom_fields_id = f.id
  and f.name ='sugarId';
  //into outfile '/tmp/migrate-users.sql';
  
  SELECT u.id, meta_value, u.user_login
  FROM wp_usermeta m, wp_users u 
  WHERE m.user_id = u.id
  AND m.meta_key ='user_api_id'
  and u.user_login in ('info@client1.com')

-- convert exponential form
CREATE TEMPORARY TABLE IF NOT EXISTS ol_account_custom_ids AS (select id from ol_account_custom where value like '%.%E%' and name not in ('alreadyContacted', 'propertyInfo', 'reasonForSale','accountName'));

update ol_account_custom set value = cast(value as decimal(38,2))
where id in (select id from ol_account_custom_ids);

  drop table ol_account_custom_ids
  
-- copy memo templates between tenants
insert into ol_memo (
  `created`,
  `last_updated`,
  `owner`,
  `plain_content`,
  `rich_content`,
  `status`,
  `tenant_id`,
  `title`,
  `name`
)
select 
  `created`,
  `last_updated`,
  `owner`,
  `plain_content`,
  `rich_content`,
  `status`,
  "omny",
  `title`,
  `name`
from ol_memo where tenant_id = 'acme';

-- find duplicate accounts. Fix by manual review
select name from ol_account group by name having count(id)>1;
select id, name, tenant_id from ol_account where name in (
  select name from ol_account group by name having count(id)>1
);
select id, first_name, last_name, stage, account_id, tenant_id from ol_contact
where account_id in (
  select id from ol_account where name in (
    select name from ol_account group by name having count(id)>1
  )
);
