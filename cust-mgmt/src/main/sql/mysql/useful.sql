select value from custom_contact_field where name = 'accountName';

  select a.id, a.name, ccf.name
  from account a, custom_contact_field ccf 
  where ccf.name = 'accountName'
  and ccf.value = a.name


update contact c  set account_id = ( 
  select a.id
  from contact_custom_fields l, custom_contact_field ccf, account a 
  where l.custom_fields_id = ccf.id
  and ccf.name = 'accountName'
  and ccf.value = a.name
  and l.contact_id = c.id
  group by l.contact_id
);


  select l.contact_id, ccf.value, a.name
  from contact_custom_fields l, custom_contact_field ccf, account a 
  where l.custom_fields_id = ccf.id
  and ccf.name = 'accountName'
  and ccf.value = a.name
  group by contact_id;
