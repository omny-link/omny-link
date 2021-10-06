LOAD DATA MART
==============

## From a jump box

- Create an SSH tunnel to the MySQL database host (source)
  [Ref](https://hostpresto.com/community/tutorials/how-to-connect-to-a-remote-mysql-server-via-an-ssh-tunnel/)
  ```
  ssh -L 3306:127.0.0.1:3306 user@host -NnT
  ```
- Forward a local port to the postgres pod using kubectl
  [Ref](https://kubernetes.io/docs/tasks/access-application-cluster/port-forward-access-application-cluster/)
  ```
  kubectl port-forward kp-postgres-postgresql-primary-0 5432:5432
  kubectl port-forward pod-name 5432:5432
  ```
- check connectivity
  ```
  # test connection to source
  mysql -u tstephen -p -h 127.0.0.1 kp_db

  # test connection to target
  psql --username=postgres --host 127.0.0.1 --dbname crm
  ```
- use pgloader to ETL in one operation
  ```
  pgloader crm-migration.load
  ```

