Customer Management System
==========================

A Customer Relationship Management (CRM) system and product catalogue.

Built on [Spring Boot](https://projects.spring.io/spring-boot/) as a series of micro-service REST APIs.

Web user interface built on top of the API as a series of [Ractive](https://ractive.js.org) single page applications supporting a wide range of branding and customisation via a per-tenant configuration file.

Documentation
-------------
- [User / Configuration Guide](https://omnylink.github.io/index.html)

License
-------
Open Source under [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)

Getting started
---------------

1. Install pre-requisites:
  - http://maven.apache.org[Maven]
  - Java
  - MySQL

2. Clone source
  ```
  git clone https://github.com/omny-link/core.git
  ```

3. Build
  ```
  mvn clean install
  ```

4. Create database, admin user, etc.
  ```
  mysql -u root -p < omny-link-server/src/main/sql/mysql/1-create-db.sql
  ```
  If you didn't edit the script above the password below is 'crm'
  ```
  mysql -u crm -p crm_db < omny-link-server/src/main/sql/mysql/2-create-schema.sql
  mysql -u crm -p crm_db < omny-link-server/src/main/sql/mysql/3-create-example-data.sql
  ```

5. Run
  ```
  mvn process-resources
  ```

6. Open browser to http://localhost:8082 using example credentials:
  ```
  user: you@example.com
  pass: secret
  ```
