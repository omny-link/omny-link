Release Notes
=============

## 3.1.9 - 18 Aug 24

  - chore(deps): bump ws, engine.io-client and socket.io in /crm-ui (#78)

        Bumps [ws](https://github.com/websockets/ws), [engine.io-client](https://github.com/socketio/engine.io-client) and [socket.io](https://github.com/socketio/socket.io). These dependencies needed to be updated together.

        Updates `ws` from 8.11.0 to 8.17.1
        - [Release notes](https://github.com/websockets/ws/releases)
        - [Commits](https://github.com/websockets/ws/compare/8.11.0...8.17.1)

        Updates `engine.io-client` from 6.5.3 to 6.5.4
        - [Release notes](https://github.com/socketio/engine.io-client/releases)
        - [Changelog](https://github.com/socketio/engine.io-client/blob/6.5.4/CHANGELOG.md)
        - [Commits](https://github.com/socketio/engine.io-client/compare/6.5.3...6.5.4)

        Updates `socket.io` from 4.6.1 to 4.7.5
        - [Release notes](https://github.com/socketio/socket.io/releases)
        - [Changelog](https://github.com/socketio/socket.io/blob/4.7.5/CHANGELOG.md)
        - [Commits](https://github.com/socketio/socket.io/compare/4.6.1...4.7.5)

        ---
        updated-dependencies:
        - dependency-name: ws
          dependency-type: indirect
        - dependency-name: engine.io-client
          dependency-type: indirect
        - dependency-name: socket.io
          dependency-type: indirect
        ...

        Signed-off-by: dependabot[bot] <support@github.com>
        Co-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>

  - chore: Bump org.webjars:bootstrap from 3.3.5 to 3.4.1 in /pdf-service (#76)
        Bumps [org.webjars:bootstrap](https://github.com/webjars/bootstrap) from 3.3.5 to 3.4.1.
        - [Commits](https://github.com/webjars/bootstrap/compare/bootstrap-3.3.5...bootstrap-3.4.1)

        ---
        updated-dependencies:
        - dependency-name: org.webjars:bootstrap
          dependency-type: direct:production
        ...

        Signed-off-by: dependabot[bot] <support@github.com>
        Co-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>
  - fix: account export (#70)
  - docs: add namespace to helm upgrade
  - fix: provide resource specs
  - fix: currently necessary to access db by IP
  - fix: data config for springdoc
  - fix: serialisation on contact.account children
  - fix: serialise responses with order item custom fields
  - fix: null order item custom fields
  - chore: test all curl.examples working
  - fix: json serialisation issue
  - fix: update helm chart for springdocs
  - fix: bump spring boot to 3.2.4 and adopt spring openapi tools
  - fix: enhance contact and account test
  - fix: update default process gateway in helm chart

## 3.1.8 - 14 Sep 23

  - chore: update landing page copyright and vsn
  - fix(gh70): csv export lazy init bug
  - fix: test needs to allow 4 letter abbrev. for month
  - chore: Bump socket.io-parser from 4.2.2 to 4.2.4 in /crm-ui (#69)
  - chore: Bump engine.io from 6.4.1 to 6.4.2 in /crm-ui (#67)
  - chore: bump node dependencies
  - chore: bump spring-beans version
  - chore: spring boot update
  - chore: node updates
  - chore: switch github actions to java 17
  - chore: move doc generation to profile

## 3.1.7 - 29 Apr 23

  - fix: rename custom field value to val
  - chore: resolve #66 xalan upgrade
  - chore: Bump itextpdf from 5.5.12 to 5.5.13.3 in /pdf-service (#65)
  - feat: GitHub actions for build and publish (#64)
  - feat: Prototype GitHub actions (#62)
  - fix: update disabled test
  - chore: Bump minimatch from 3.0.4 to 3.0.5 in /crm-ui
  - chore: Bump qs and browser-sync in /crm-ui
  - chore: Bump socket.io-parser and socket.io-client in /crm-ui
  - fix: resolve #57 compute summary stock item
  - fix: preserve messages class on display
  - fix: make date controls smaller
  - chore: Bump engine.io and socket.io in /crm-ui (#52)
  - chore: Bump decode-uri-component from 0.2.0 to 0.2.2 in /crm-ui (#53)
  - chore: Bump json5 from 2.2.0 to 2.2.3 in /crm-ui (#58)
  - doc: example of form endpoint
  - fix: add template handling for empty var, inc. test #28
  - chore: Bump yargs-parser and yargs in /crm-ui (#51)

## 3.1.6 - 01 Oct 22

 - fix: #43 make company numbe varying and remove left padding
 - fix: resolve #50 display order funnel
 - fix: summarise orders by stage
 - fix: disable open stock item when more than one
 - fix: resolve #49 display stock item csv when have order items
 - fix: resolve #47 expand account stage
 - fix: resolve #48 hide deleted order items
 - fix: resolve #45 display of tasks on account page
 - fix: set jshint to use es 6
 - fix: resolve #45 expand account stage
 - fix: Bump jsoup from 1.14.2 to 1.15.3 in /cust-mgmt (#46)
 - fix: Bump itextpdf from 5.5.10 to 5.5.12 in /pdf-service (#44)
 - fix: remove compilation warnings under java 11
 - fix: match version of spring-data-jpa foor liquibase to app (flagged by dependabot)
 - fix: reduce return from findByCustomField to just summary data
 - fix: resolve #42 correct filtering of tasks
 - fix: resolves #39 cannot delete order item
 - fix: add method to eval json as well as url params #28
 - fix: resolves #41 make 'now' available to template engine
 - fix: respect feature flag when initiating editor
 - fix: resolves #20 larger company numbers
 - fix: Bump spring-beans from 4.1.7.RELEASE to 5.3.18 in /crm-server (affects liquibase plugin only)
 - fix: address github security advisory GHSA-673j-qm5f-xpv8 and CVE-2022-21724 on postgres driver

## 3.1.5 - 30 Mar 22

 - apply #34 expliciti order item sort in additional places
 - resolve #32 pdf generation
 - ignore idea files
 - move jasmin spec into resource folder (sometimes note found in java folder)
 - ensure order displayed for super-admins
 - ui to set index from #37
 - resolve #34 explicit order items
 - fix 404 error
 - update copyrights
 - consistency on stock cat logical delete
 - test enquiries with and without account
 - resolves #31 include order feedback when findByContacts
 - add rich editor to free-form mailer
 - additional hide activity indicator
 - resolve #30 search within tasks
 - rich editor to protect against disallowed &
 - added ckeditor to account desc
 - audit fix
 - update package-lock
 - remove unused func
 - resolve #26 refresh order list
 - apply logical delete to stock item as for other entities
 - resolve #24 display stock item name on orders
 - related to #24 set stock item id right on creation
 - resolves #22 unable to preview memo
 - limit length of stage field (noted err in srvr log)
 - resolve #17 now displaying order item data
 - resolve #19 race condition loading bookings

## 3.1.4 - 08 Mar 22

 - remove account augmentations preventing save
 - resolves #15 - short keys code was duplicated
 - validate company and edubase numbers
 - revise enquiry process test to expect process to be complete
 - resolve #17 retrieve order items with orders as in v2

## 3.1.3 - 07 Mar 22

Accumulated fixes and tweaks as we bed down 3.0 UI against the 3.1 API

 - link custom field to its contact (release 3.1.3)
 - reformat task vars for flowable API
 - update copyright dates
 - fix apple touch icon path
 - add banner functionality
 - remove transparency from Google touch icon
 - document task migration
 - support standalone (process-less) tasks and fix bizKey after migration
 - update to svg icons
 - fix task refresh and enlarge # of records fetched
 - actual migration to k8s
 - npm audit fix
 - Bump follow-redirects from 1.14.4 to 1.14.8 in /crm-ui
 - ignore local liquibase config
 - fix broken alert count on contacts
 - fix fail to display on select (first time only)
 - fix activity age display
 - pull up sort method
 - migrate saveDoc to send json
 - fix missing data formatters for orders
 - added js linting
 - implemented paging, though still synchronous
 - optimise list orders
 - optimise find contact by id
 - optimise list contacts and accounts (avoid in-memory processing)
 - permit config url to be set at build time
 - add legacy process gateway support and update REST test
 - exclude spring tool cache in gitignore
 - cleanup config, no ajp connector needed under container/k8s
 - centralise jacoco config in root project
 - parameterise process gateway config
 - branding/links for api server
 - update licence for 2022
 - fix bug in deserialisation of contact
 - add process gateway
 - package crm-ui: exclude jar
 - compensate for diff in flowable task api
 - patch log4j for CVE-2021-44228
 - read liquibase files from classpath

## 3.1.0 - 09 Dec 21

Delivers the containerised backend API including helm chart to ease deployment. UI changes are limited to just those needed. Significant features are:

 - migrate from Spring Boot 1.x to 2.x
 - add Postgres as the preferred data store
 - adopt Liquibase for database migration
 - publish Swagger API docs

Full change list:

 - fix typo in toCsv
 - fix wrong logger
 - fix cartesian product on order/order items
 - configure jacoco with low but passing limits
 - make stock category returns http entity not just simple object
 - fix save of order with order items
 - fix failure to save account custom fields on update
 - reinstate REST API tests and fix feedback one
 - tweak fields included in summary API to suit acct client
 - revised domain model and generate in build
 - tidy up contact repo
 - make order item and stock cat fully auditable
 - add endpoint for evaluating memo template
 - fix add when single search match
 - migrate pick memo to flowable
 - fix link from account to order
 - fix delete contact on acct page
 - fix sort on account name
 - biz key as var (flowable migration)
 - relink to new (keycloak) profile page
 - fix save contact and refresh tasks on accounts
 - ensure complex data sent as json by custom forms
 - add endpoint for evaluating memo template
 - port pick-memo to flowable
 - port work page to flowable
 - dry up fetchTask
 - avoid recursion problem with localId on load
 - remove dup table headers
 - fix search on funnel
 - optimise stock and fix for updated spring
 - optimise order load
 - optimise contacts load
 - switch to flowable task api
 - relocate static assets from boot app
 - upgrade gulp build system
 - remove account->contact navigation
 - helm: disable hibernate update and switch db
 - fix broken Location headers & remove order->feedback
 - impl CORS exposed headers
 - tidy & logging
 - identity -> sequence; optimise col lengths
 - migrate selfRef to id due to diff pk handling in updated spring jpa
 - updated stock items for new spring data
 - temporarily disable api test
 - implemented liquibase for database evolution
 - temporarily disable api test
 - fix contact custom table name
 - align acct summary fields with v2
 - preliminary k8s deployment
 - add mysql to postgres migration
 - ready for next release (3.1.0)
 - standardise on Spring for audit cols
 - fixup REST API tests inc. random port
 - bump jasmine-boot to 1.1.0
 - drop the legacy standalone custmgmt
 - swagger for catalog
 - swagger for notes
 - tests and swagger for memos
 - clean up swagger api
 - created helm chart
 - set up swagger


## 3.0.1 - 03 Jun 21

 - restore role-based edit for power-users
 - disable failing backend tests whilst focus on front end

## 3.0.0 - 27 May 21

This release updates the identity management system to support more modern
features like multi-factor authentication etc. It also separates the UI from
the various backends in readiness to complete the migration to being fully
cloud-native.

 - align roles to keycloak so conditional elements display
 - migrate to kp icons
 - migrate process admin ui
 - migrate work ui
 - Bump browserslist from 4.16.3 to 4.16.6 in /crm-ui
 - git add new contact / acct
 - Bump hosted-git-info from 2.8.8 to 2.8.9 in /crm-ui
 - short keys need to be disabled if focus is on an input field
 - provide author for note
 - Bump hosted-git-info from 2.8.8 to 2.8.9 in /crm-ui
 - migrate status page
 - provide author for doc
 - adjust conditional templates for static tenant config
 - fix autoNumeric & dropdown formatting post refactoring
 - fix missing showX funcs
 - fix order item display after static tenant config change
 - optimise first load
 - fix filters with new user profile
 - move to bootstrap-tagsinput
 - fix small screen layout
 - fix free-form mail
 - tidy css
 - fix delete contact ui
 - fix funnel after migration
 - migrate acct actions
 - refactor standalone crm-ui
 - drop push to dockerhub, handle externally
 - Bump commons-io from 2.4 to 2.7 in /catalog
 - Bump commons-io from 2.4 to 2.7 in /cust-mgmt

## 2.2.4 - 07 Jul 20

 - better account export to csv
 - resolves #2
 - added container to build
 - add accounts.findByCustomField
 - allow selection of order item summary fields instead of using all
 - extend max lengths of memo name and title
 - fix default checkbox implementation
 - search contact by custom field
 - make sorting responsibility of client
 - fix non-externalised strings
 - reduce delete role from 'admin' to 'analyst'
 - resolves omny/platform#631 relax mandatory email
 - resolve puffin/srp#296 sidebar submenu
 - workaround SUREFIRE-1588
 - upgrade to batik 1.9.1
 - resolves #629 add search on invoice ref
 - Add account APIs to set stage, alerts and tags

## 2.2.3 - 27 Aug 2018

 - prevent duplicate contact listing on account page

## 2.2.2 - 19 Jul 2018

 - resolves #626 link to orders
 - resolves #617 limit and sort offered memos
 - resolves #625 ui to save feedback
 - resolves #624 feedback custom fields
 - fix owner gravatar (currently displaying email hash)
 - avoid calc tax on load
 - include account custom fields in contact detail view
 - resolves #621 time since reg'n
 - remove copyright from JSON (correct copyright, invalid JSON)
 - resolves omny/alife#72 caused by #613
 - resolves #618 make boolean fields more resilient
 - remove stuff that doesn't belong with the crm
 - remove stuff that doesn't belong with crm

## 2.2.1 - 28 Feb 2018

- #608 export tasks
- #607 export notes and docs along with the linked contact and account
- #606 export internal ids

## 2.2.0 - 17 Jan 2018

- #613 prevent contacts / accounts relate to the tenant being deleted
- #602 use a 'smart' label to the Orders section of Contacts
- #601 limit users page to the current tenant
- #600 remove case sensitivity of contact tag search
- #598 new sell-side action to be able to add a buy-side record
- #597 hide order id when reference displayed
- #596 fix non-unique ids in HTML
- #588 disallow create document links on orders without a name
- #421 construct 'owners' drop down dynamically from contacts

## 2.1.0 - 08 Dec 2017

- #590 document how to add custom partials
- #585 show cobined (active and historic) instance data on definitions page
- #583 fix dupliacte save of notes on orders page
- #582 fix display of category on stock items
- #581 fix ability to make a note confidential immediately on creation
- #580 fix download of BPMN or image from definitions page
- #579 reduce logging when no default descriptions exist for a given stock item
- #574 enable removal of obsolete process definitions
- #573 new alerts implementation
- #570 add export of Customer Data
- #568 fix exception in 'unique' aggregation of order items
- #567 update Edubase links
- #566 fix scroll bars in task list
- #565 fix intermittent stock item display on contacts page
- #564 add DocuSign integration
- #562 new process to send user a new password without the operator seeing it
- #556 enhance notes with confidential flag
- #553 fix sorting by click on column title
- #546 add map icon to header area alongside email and phone
- #545 set initiator on processes started by message
- #542 improved filter on orders page
- #541 make stock item control on orders page read-only control with link
- #531 retire bootstrap typeahead controls in favour of HTML 5 lists
- #515 add notes and documents to stock items, categories and orders
- #491 new order report
- #490 add 'Related' order grid on order view
- #488 add buy-side orders (new standard field order type)
- #487 hide internal order id and replace with number fountain
- #485 match social fields on account page to contact page
- #478 make stock item optional to order page
- #406 encrypt passwords in the database
- #405 new authentication and authorisation using JSON Web Tokens (JWT)
- #345 new 'easy' config of new tenant
- #486 create number fountain attachable to any field
- #276 process to set stage to inactive after the specified period

## 2.0.0 - 30 May 2017

- #514 show hidden attributes of message events & data store
- #512 enhance stock catalogue
- #510 enhance Merge Memo and Send Memo processes with account
- #508 add'l fields in register new user form & tolerate pre-existing roles
- #506 additional hidden BPMN attributes display
- #502 display service task implementation in def'n page properties section
- #500 additional hidden BPMN attributes display
- #499 Add no of employees to accountCompanyDetails section next to founded, SIC
- #496 fix to ensure task data changes are saved
- #494 harmonise conflicting search and filter terms
- #492 new filter on contact and account views: View Mine (inactive)
- #484 improved BPMN renderer
- #483 fix intermittent rich editor issue in memo templates page
- #482 include stock category custom fields in single stock item response
- #479 optional phone3 field on contacts
- #366 support search on multiple terms
- #462 patch javax.json.JsonObjectBuilder for NPE
- #362 add template preview in new tab
- #476 add search stock items on #tag
- #469 JSON serialisation error during fetchNotes standalone call
- #468 relax need to select order within send mail when not needed by memo
- #348 Fix: apostrophe in name prevents send email dialogue opening
- #467 new 'additional info' field on orders
- #465 improve navigation from orders to account
- #458 fix template editor refresh
- #456 display order ids on account page
- #158 add filter by date range to funnel
- #445 remove spurious template warnings from status page and log
- #439 revised Orders view
- #423 revise login placeholder
- #417 hide 'Add task' on contacts & accounts views when work mgmt disabled
- #382 deep link to a single process definition
- #398 default sort stock (items and categories) alphabetically
- #305 feedback fields need to be custom fields of order not contact
- #377 add Order Item to order
- #410 order price to be read-only unless users have sales_manager role
- #396 #473 performancce optimisation of stock catalog
- #394 use hash to flag out of date processes in status page
- #402 create UI to register users
- #393 allow different de-duplication policies within RecordEnquiry per tenant
- #392 tolerate missing owner within SendMemo process
- #385 ass logical delete for orders
- #384 support aggregation of order item custom fields in account view
- #383 convert Data Input linked to user task to form property on upload
- #379 a help to account page
- #346 convert stock item type to tags
- #349 make order available to SendMemo process
- #369 hide Source,Medium,Campaign fields when not populated
- #363 render of BPMN Pool and Lane names in proper alignment
- #361 add 'Add task' action to account page
- #320 prototype account view
- #306 add duplicate order action
- #295 set business key to contact name when process initiated from contact page
- #314 localisation and en/disable standard sections / fields per tenant
- #302 new 'Add task' action on work page
- #293 define value-chain process from markup
- #298 transpose utm_xxx fields in enquiry into contact as xxx
- #200 list related tasks in contact page
- #232 streamline actions in Work Mgmt in light of 4Ws process
- #239 Helpdesk / Ticket process and UI
- #296 extend account description size
- #122 fix: memo (template) editor does not work properly until saved
- #282 prevent clone of memo creating several copies
- #240 add link to open contact record from work
- #291 fix duplicate display of Activities (due to missing join)
- #228 correct line stype of text annotation associations
- #243 add filter by filterable by today, tomorrow, etc to task list
- #237 allow task due date update if user is initiator

## 1.0.0.0 -  09 Sep 2016

- #288 display potential owners in task list
- #287 trim fields on save
- #286 display process definitions' category
- #275 increase delay between ajax retries
- #272 enlarge offers fields and apply limit in front end to description
- #269 allow service level checks to be run on demand or as cron job
- #268 new option to expose contact id based on tenant or role
- #262 replace help with content from the web site to facilitate editing
- #260 display start and end time of instance without opening
- #259 add sort by column headings to memo and user pages
- #255 default filter on contact page to those owned by current user
- #253 add warning to status page if using defaults for any drop downs
- #251 new standard field job title on contact
- #238 add bulk delete of process instances for admins
- #230 fix: date sorting in Internet Explorer
- #229 add in-contact alerts
- #227 make owner contact available for use in SendMemo as well subect contact
- #223 add name field for document links
- #222 additions to tenant status page
- #220 add feature config to en/disable documents section
- #214 update to Companies House API now requires authentication
- #212 fix Twitter handles length and allowed chars
- #211 show if latest process (in jar) matches the one deployed on status page
- #210 make username case insensitive
- #209 fix user activity recording
- #207 revise registration process to send password directly
- #204 enhance RecordEnquiry process to put message in Notes
- #199 provide default gravatar based on initials
- #196 prevent attempt to save account before contact
- #195 fix: BPMN renderer positioning of sequence flow arrow heads
- #194 add simple TODO process (aka 4Ws, aka GTD)
- #192 more prominent ajax loader
- #187 add import of contacts & accounts from spreadsheet
- #186 add support for Scottish company numbers (UI only) and LLP numbers
- #182 admin function to create tenant bot from the status page
- #180 add failed login message
- #178 fix IE issue with contacts page
- #173 new tenant dashboard
- #172 fix URL used to POST contact
- #166 new standard RecordEnquiry handler
- #165 add deploy latest processes from the status page
- #174 fix contact filter not working
- #162 enhance product catalogue
- #159 add tabular drill down by funnel segment
- #157 fix work mgmt refresh
- #156 refresh notes on contact save
- #154 fix IE issue with date display in the notes section of contact page
- #153 support Scottish company numbers
- #152 add search by location for stock
- #151 fix work page search
- #148 add ability to suspend process defn
- #147 add user message for server unavailable
- #144 fix send memo to contact
- #126 enhance default forms in work mgmt
- #119 allow page sections to be collapsed to save screen real estate
- #109 Fix: de-linking of contact and account
- #69 add tenant status page
- #17 Reset password functionality

## 1.0.0 beta 10 - 31 Mar 16

- #140 fix stack overflow in hashcode preventing update of contact
- #138 new product catalogue
- #136 database schema cleanup
- #135 prototype user activity tracking
- #134 revised funnel design
- #133 fix contact edit button
- #132 manual update to workflow
- #131 render processes directly from bpmn
- #116 fix: add new user fails
- #112 support LLP numbers in addiion to limited companies

## 1.0.0 beta 9 - 12 Feb 2016

- #108 new section embedding company background data
- #106 display contextual help
- #104 fix: hint text following checkboxes is too far from checkbox
- #100 add telephone field validation
- #98 add abilty to open contact address in Google maps
- #94 fix: problems saving accounts
- #93 RSS feed for tasks
- #92 colour code work mgmt list
- #86 upgrade to ractive webjar 0.7.3
- #84 enhance search functionality

## 1.0.0 beta 7 - 23rd Nov 2015

- #70 provide library and authoring of templated memos
- #60 better user hints on purpose of various checkboxes on contact
- #59 fix timeSinceEmail not correctly choosing most recent email
- #58 fix another way can match template when should not be able to
- #57 'dry-run' for email follow up

## 1.0.0 beta 6 - 9th Nov 2015

- #52 mail configuration

## 1.0.0 beta 5 - 6th Nov 2015

- #50 mail configuration
- #49 add Company Number lookup
- #45 fix exception in log (no visible impact) on startup
- #42 fix email follow up for contacts containing ampersands

## 1.0.0 beta 4 - 4th Nov 2015

- #44 Do not post support call for empty JSON messages

## 1.0.0 beta 3  3rd Nov 2015

- #39 fix problem with generating decision script

## 1.0.0 beta 1 - 21 Oct 15

- #36 add personification of Omny
- #35 add additional 'reason' field, display only when stage = Cold
- #34 update task forms to include Stage
- #31 add 'Active' filter in contact management
- #30 add defer task functionality
- #24 exclude 'Cold' contacts from display by default
- #2 push tasks / events to Slack
- #31 create ad-hoc task

## 1.0.0 beta 1 - 28 Sep 15

- #18 profile page
- #16 ensure cannot switch between tenants unless have super-admin role
- #15 Maven config to include Spring Boot executable jars only in server module
- #14 fix issue opening Simple TODO forms
- #13 fix duplicate index.html
- #12 Reorder contact actions and change logo click destination
- #10 fix exception in log (no visible impact)

## 0.9.0 - 20 Sep 15

- #81 New index page providing opportunity for embedded docs / training
- #80 Support for starting enquiry process from Contact
- #78 If no update has taken place lastUpdated is shown as 01 Jan 1970
- #77 Refactor configuration to prevent duplication of code from work-mgmt
- #76 Per contact custom actions
- #75 Responsive fixes
- #74 Cannot save yet message displayed inappropriately on first loading CRM
- #73 favicon misconfigured
- #72 Embed Wiser for mail server testing during development
- #71 Update decision engine
- #69 On creating new contacts it appears to add duplicates
- #64 Highlight active page in Omny Bar
- #59 Make filter drop down wider
- #57 Update email follow-up decision
- #23 Save message field in notes

## 0.8.0 - 05 Jul 15

- #58 Apply Omny branding to Swagger UI
- #56 Per-tenant configuration of sections of UI

## 0.7.0 - 04 Jun 15

- #55 Add REST API documentation via Swagger
- #54 Move marketing fields to own section ...
- #53 Spurious 'Cannot save yet' message on initial load
- #52 Change icon for 'Jump to notes' to paperclip
- #51 Only show migration message on records migrated into Omny
- #50 Changes to budget
- #49 Move 'do not call' and 'do not email'
- #46 Town field not being saved (3rd address field)
- #44 Ensure contact form is hidden when results open
- #42 Search to include Town, County, Country in addition to current
- #40 Filter on stage
- #39 Load customer data
- #22 Need feedback for adding Note, Document etc. that auto-save
- #11 Need more feedback when Search has no results

## 0.6.0 - 19 May 15

- #43 Better field validation
- #35 Expose 'do not email' and 'do not call' flags
- #34 Trouble adding new record with space in surname
- #32 'Omny Bar'
- #31 Budget (and other?) fields not using the number formatting
- #25 Need way for 'office' users to start workflow
- #24 On adding a Note (tabbing away) focus changes unexpectedly
- #23 Last updated is too narrowly defined
- #19 Implement gravatar instead of username
- #18 Delete needs to be 'logical' not 'physical'
- #16 populating AdWords information
- #15 Merge detection is not working

## 0.4.0 - 13 Apr 15

- #3 Powered by Omny logo getting in front of the open/close arrow button
and making it unclickable
- #5 Dates displaying in US format
- #6 UI and data tweaks from review 9 Apr 15

## 0.3.0 - 6 Apr 15

- new tenant config and data migration
- Allow URL param to open customer page with pre-filtered list
- Added Activities to customer mgmt system

## 0.2.0 - 20 Mar 15

- Order contacts by last updated
- Some extra fields including doNotCall/Email and some specific to customer
- REST services to upload Contacts and Accounts plus UI to enable this
- UI polish
- Replace ractive.data. with ractive.get calls (API removed in latest version)
- Switch datasource connection pool in attempt to avoid disconnection errors
- Update drop down list data
- Fix (in service tasks) to REST URL invocation to support spaces

## 0.1.0 - 16 Jan 15

- Basic features release
