select name from ol_account_custom
group by account_id having count(name)>1;

782 rows

delete from ol_account_custom where  value ='';