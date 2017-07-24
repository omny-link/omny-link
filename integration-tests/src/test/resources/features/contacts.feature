Feature: Create and manage contacts through a sales funnel

  Scenario: Create a new Firm Gains enquiry from a web form
          Used by the order page
    Given the bot user is logged in
     When an omny.enquiry message including contact info is submitted for tenant 'firmgains'
     Then a 201 response is returned identifying the process instance created
      And the process data includes 'contactId'
      And the call took less than 500ms


#  Scenario: Create a new A-Life enquiry from a web form
#          Used by the order page
#    Given the bot user is logged in
#     When an omny.enquiry message including both contact and account is submitted for tenant 'alife'
#     Then a 201 response is returned identifying the process instance created
#      And the process data includes 'contactId'
#      And the process data includes 'accountId'
#      And the call took less than 500ms
