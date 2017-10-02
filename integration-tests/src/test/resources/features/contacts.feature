Feature: Create and manage contacts through a sales funnel
         NOTE: these will fail when run together, possibly due to additional
         time required or order of emails varying.

  Scenario: Create a new Firm Gains enquiry from a web form
          Used by http://firmgains.com/register/
    Given the bot user is logged in
      And the test mail server is available
     When an 'omny.enquiry' message including contact info is submitted for tenant 'firmgains'
     Then a 201 response is returned identifying the process instance created
      And the call took less than 500ms
      And the process data includes 'contactId'
    # And (TODO) check de-dupe logic
      And the process has completed within 6000ms
        # no alerts
      And 2 emails were sent
        # initiator (in cucumber.properties) overrides default:john@ergodigital.com
      And email 0 sent was a thanks for your enquiry from 'tim@omny.link' containing 'Thank' in the subject
     Then the test mail server is shut down

  Scenario: Create a new A-Life enquiry from a web form
          Used by http://www.a-life.co.uk/booking-a-life/
    Given the bot user is logged in
      And the test mail server is available
     When an 'omny.enquiry' message including contact, account and order is submitted for tenant 'alife'
     Then a 201 response is returned identifying the process instance created
      And the call took less than 500ms
      And the process data includes 'contactId'
      And the process data includes 'accountId'
      And the process data includes 'orderId'
      And the process has completed within 6000ms
      #And 3 emails were sent
        # initiator (in cucumber.properties) overrides default:hazel@a-life.co.uk
      And email 0 sent was a thanks for your enquiry from 'tim@omny.link' containing 'Thank' in the subject
      And email 2 sent was an enquiry notification from 'info@omny.link'
      And message 2 contains 'accounts.html' in its body
     Then the test mail server is shut down

  Scenario: Create a new Omny enquiry from a web form
          Used by http://omny.link/contact-us/
    Given the bot user is logged in
      And the test mail server is available
     When an 'omny.enquiry' message including contact info is submitted for tenant 'omny'
     Then a 201 response is returned identifying the process instance created
      And the call took less than 500ms
      And the process data includes 'contactId'
      And the process has completed within 6000ms
      #And 2 emails were sent
      And email 0 sent was a thanks for your enquiry from 'tim@omny.link' containing 'Thank' in the subject
      And email 2 sent was an enquiry notification from 'info@omny.link'
      And message 2 contains 'contacts.html' in its body
     Then the test mail server is shut down