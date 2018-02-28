create table OL_PROCESS_MODEL (
  id int4 not null auto_increment, 
  bpmn_string longtext, 
  category varchar(255), 
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
  deployment_id integer, 
  description varchar(255), 
  diagram_resource_name longtext, 
  proc_key varchar(255), 
  last_updated datetime, 
  name varchar(255), 
  resource_name varchar(255), 
  tenant_id varchar(255), 
  version integer not null, 
  primary key (id)
)

create table OL_MODEL_ISSUE (
  id int4 not null auto_increment, 
  name varchar(255), 
  description varchar(255), 
  model_ref varchar(255),
  level varchar(255), 
  model_id int4, 
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
  last_updated datetime, 
  tenant_id varchar(255), 
  version integer not null, 
  primary key (id)
)
