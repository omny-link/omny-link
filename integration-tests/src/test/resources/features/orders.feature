Feature: Create and track orders from opportunity to completion
 
  Scenario: List all orders
          Used by the order page
    Given the server is available
      And the user is logged in
     When a list of orders is requested
     Then a list of order _summaries_ is returned
      And the call took less than 500ms
      
  Scenario: List orders only for specific contacts
          Used by the accounts view
    Given the server is available
      And the user is logged in
     When a list of orders is requested for contacts 1 & 2
     Then a list of order _summaries_ is returned
      And the call took less than 500ms
      
  Scenario: Create a new incomplete order (opportunity)
          Used by the order page
    Given the server is available
      And the user is logged in
     When a list of orders is requested
     Then a success response is returned identifying the order created
      And the order is retrievable by the provided identifier
      And the call took less than 500ms
      
  Scenario: Update a specified order with new order items
          Used by the order page
    Given the server is available
      And the user is logged in
     When the order X is retrieved 
      And a list of order items are added to it 
     Then a list of order items are saved and linked to the order
      And the call took less than 500ms
      