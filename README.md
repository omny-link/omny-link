Customer Management 
===================

Features
--------

    - Web user interface 
      - Add / View / Update / Delete Contacts
      - Add / View / Update / Delete Accounts
      - View Activities related to a Contact or Account
      - Add / View Notes to Contact 
      - Add / View Documents to Contact 
    
    - REST API feature matches Web UI 

    - Bulk import of Contacts, Accounts, Activities and Notes

Roadmap
------- 

     - Merge records 
     - Consider results slide out from side not top 
     
Releases
--------

1.0.0 beta 1 - 21 Oct 15

    #37 FirmGains needs additional reference data 
    support Firm Gains
    
    #36 Personification of Omny 
    enhancement Gardenatics Omny Link
    
    #35 When stage = Cold need an additional 'reason' field 
    enhancement Gardenatics
    
    #34 Gardenatics task forms should include Stage 
    enhancement Gardenatics
    
    #31 'Active' filter in contact management 
    enhancement Gardenatics
    
    #30 Defer Task functionality 
    enhancement Firm Gains Gardenatics
    
    #24 Exclude 'Cold' contacts by default 
    support Firm Gains
    
    #2 Push tasks / events to Slack 
    enhancement Nene
    
    #31 Create ad-hoc task 
    feature Gardenatics

1.0.0 beta 1 - 28 Sep 15

    #18 profile page
    enhancement

    #16 Ensure cannot switch between tenants unless have super-admin role
    bug

    #15 Configure Maven to include Spring Boot executable jars only in omny-link-server
    support

    #14 Resolve issue opening Simple TODO forms
    bug support Firm Gains

    #13 Fix duplicate index.html
    support

    #12 Reorder contact actions and change logo click destination
    enhancement Firm Gains

    #10 Exception in log (no visible impact) on opening Lorenzo Jones
    bug confirmed Firm Gains contact

    #9 Cannot open Lorenzo Jones after upgrade to 0.9.0
    bug confirmed Firm Gains contact

0.9.0 - 20 Sep 15

    #81 New index page providing opportunity for embedded docs / training
    enhancement

    #80 Support for starting Gardenatics enquiry process from Contact
    enhancement Gardenatics

    #78 If no update has taken place lastUpdated is shown as 01 Jan 1970
    bug

    #77 Refactor configuration to prevent duplication of code from work-mgmt
    support

    #76 Per contact custom actions
    feature Gardenatics Firm Gains Omny Link

    #75 Responsive fixes
    enhancement

    #74 Cannot save yet message displayed inappropriately on first loading CRM
    bug

    #73 Firm Gains favicon misnamed so returning 404
    bug

    #72 Embed Wiser for mail server testing during development
    enhancement

    #71 Switch from Omny Link decisions to One Decision
    support Firm Gains

    #69 On creating new contacts it appears to add duplicates
    support Gardenatics Firm Gains

    #64 Highlight active page in Omny Bar
    enhancement Gardenatics

    #59 Make filter drop down wider
    Nile and York

    #57 Replace email follow up decision with One Decision based decision tables
    Firm Gains

    #45 New stages to Gardenatics, also revising process
    Gardenatics
    
    work-mgmt #23 Save message field in notes
    Gardenatics

0.8.0 - 5 Jul 15

    #65 New partners for Firm Gains typeahead
    support Firm Gains

    #63 More Firm Gains layout changes
    support Firm Gains

    #58 Apply Omny branding to Swagger UI
    support CarQuake

    #56 Per-tenant configuration of sections of UI
    enhancement Firm Gains Nile and York

0.7.0 - 4 Jun 15

    - #55 Add REST API documentation via Swagger 
      feature Omny Link
    - #54 Move marketing fields to own section ... 
      enhancement Gardenatics Firm Gains
    - #53 Spurious 'Cannot save yet' message on initial load 
      bug Gardenatics Firm Gains
    - #52 Change icon for 'Jump to notes' to paperclip 
      enhancement Gardenatics
    - #51 Only show migration message on records migrated into Omny 
      Gardenatics
    - #50 Changes to budget 
      enhancement Gardenatics
    - #49 Move 'do not call' and 'do not email' 
      7 days ago
    - #46 Town field not being saved (3rd address field) 
      bug Gardenatics Firm Gains
    - #44 Ensure contact form is hidden when results open 
      bug Gardenatics
    - #42 Search to include Town, County, Country in addition to current 
      support Nile and York
    - #40 Filter on stage 
      Gardenatics Nile and York
    - #39 Load customer data 
      Nile and York
    - #22 Need feedback for adding Note, Document etc. that auto-save 
      enhancement 
    - #11 Need more feedback when Search has no results 
      enhancement Firm Gains

0.6.0 - 19 May 15

    - #43 Better field validation
    enhancement Gardenatics Firm Gains
    - #35 Expose 'do not email' and 'do not call' flags in 
    enhancement Gardenatics
    - #34 Trouble adding new record with space in surname
    bug Gardenatics
    - #32 'Omny Bar' 
    feature
    - #31 Budget (and other?) fields not using the number formatting 
    Gardenatics
    - #25 Need way for 'office' users to start workflow 
    enhancement Gardenatics
    - #24 On adding a Note (tabbing away) focus changes unexpectedly 
    Gardenatics
    - #23 Last updated is too narrowly defined 
    Gardenatics Firm Gains
    - #19 Implement gravatar instead of username
    enhancement Gardenatics Firm Gains
    - #18 Delete needs to be 'logical' not 'physical' 
    Gardenatics Firm Gains    
    - #16 Valuation is not populating AdWords information 
    bug Firm Gains
    - #15 Merge detection is not working
    bug Firm Gains

0.4.0 - 13 Apr 15

    - #3 Powered by Omny logo getting in front of the open/close arrow button
    and making it unclickable
    - #4 Make email optional in gardenatics UI
    - #5 Dates displaying in US format
    - #6 UI and data tweaks from review 9 Apr 15

0.3.0 - 6 Apr 15

    - Gardenatics UI and data migration 
    - Allow URL param to open customer page with pre-filtered list
    - Added Activities to customer mgmt system

0.2.0 - 20 Mar 15

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
