Feature: Create and track orders from opportunity to completion

  Scenario: List all orders
          Used by the order page
    Given the user is logged in
     When a list of orders is requested
     Then a list of 19 order _summaries_ is returned
      And the call took less than 500ms

  Scenario: List orders only for specific contacts
          Used by the accounts view
    Given the user is logged in
     When a list of orders is requested for contacts 1 & 2
#     Then a list of 2 order _summaries_ is returned
      And the call took less than 500ms

  Scenario: Create and fulfil a simple order, i.e. without order items
          Used by the order page
    Given the user is logged in
     When an order for a "Type A widget" priced at "£10.50" with 2 custom fields is submitted
     Then a success response is returned identifying the order created
      And the order is retrievable by the provided identifier
      And the order includes expected price and custom fields
# TODO, should get around to adding these assertions, just needs time.
#     When a contact is specified for the order
#     Then retrieving the order shows the contact to have been saved
#     When a stock item is specified for the order
#     Then retrieving the order shows the stock item to have been saved
     When an invoice ref is specified for the order
     Then retrieving the order shows the invoice ref to have been saved
# TODO just needs time
#     When feedback is submitted for the order
#     Then the response identifies the newly created feedback
#      And the feedback is retrievable and includes creation date, content and custom field
     When the order is deleted
     Then the order IS retrievable but marked deleted
      And the call took less than 500ms
