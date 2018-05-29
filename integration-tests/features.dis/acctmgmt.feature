Feature: Manage various aspects of user and tenant accounts

  Scenario: Register a new user for a given tenancy
          Used to on-board new users
    Given the bot user is logged in
      And the test mail server is available
     When an 'omny.registration' message is submitted for tenant 'acme'
     Then a 201 response is returned identifying the process instance created
      And the call took less than 500ms
      And the process data includes 'contactId'
     Then the process has completed within 10000ms
     #And 2 emails were sent
        # message is from initiator (in cucumber.properties)
      And email 0 sent was a welcome email from 'tim@omny.link'
        # TODO add this alert to process
     #And email 2 sent was a new user notification from 'info@omny.link'
      And message 0 contains 'password' in its body
      And user profile exists for 'milhouse.vanhouten@springfieldprimary.com'
     Then the test mail server is shut down

 Scenario: Remove an existing user without changing any contacts or other owned data
          Used when employees leave
    Given the bot user is logged in
     When an 'omny.deregistration' message is submitted for tenant 'acme'
     Then a 201 response is returned identifying the process instance created
      And the call took less than 500ms
      And the process data includes 'contactId'
     Then the process has completed within 7000ms
      And user profile does not exist for 'milhouse.vanhouten@springfieldprimary.com'
