Customer Management 
===================

Features
--------

    - Web user interface 
      - Add / View / Update / Delete Contacts
      - Add / View / Update / Delete Accounts
      - Add / View Notes to Contact 
      - Add / View Documents to Contact 
    
    - REST API feature matches Web UI 

Roadmap
------- 

     - Merge records 
     - Consider results slide out from side not top 
     
Releases
--------

0.2.0 - 20 Jan 15

  - Order contacts by last updated
  - Some extra fields including doNotCall/Email and some specific to customer
  - REST services to upload Contacts and Accounts plus UI to enable this
  - UI polish  
  - Replace ractive.data. with ractive.get calls (API removed in latest version)
  - Switch datasource connection pool in attempt to avoid disconnection errors
  - Fix firm gains drop down list data
  - Fix (in service tasks) to REST URL invocation to support spaces

0.1.0 - 16 Jan 15 

  - Basic features release
