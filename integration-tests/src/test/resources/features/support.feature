Feature: Manage user support requests

  Scenario: Raise and resolve a support ticket
          Used to request support within the app
    Given the bot user is logged in
      And the test mail server is available
     When an 'omny.ticket' message including screenshot is submitted for tenant 'omny'
     Then a 201 response is returned identifying the process instance created
      And the call took less than 500ms
      And the process data includes 'contactId'
     When the user task 'resolveTicket' is completed
     Then the process has completed within 6000ms
      And a thanks for the ticket email was sent
      And a support case notification email was sent
     Then the test mail server is shut down
