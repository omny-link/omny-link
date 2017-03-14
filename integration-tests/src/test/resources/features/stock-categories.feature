Feature: A repository of different stock categories
 
  Scenario: List all stock categories
      Used by the maintenance pages
    Given the server is available
    And the user is logged in
    When a list of stock categories is requested
    Then a list of stock categories _summaries_ is returned
    And the call took less than 500ms
    
  Scenario: List all stock items
      Used by the maintenance pages
    Given the server is available
    And the user is logged in
    When a list of stock items is requested
    Then a list of stock item _summaries_ is returned
    And the call took less than 4000ms
    
  Scenario: Search stock categories by town
      Used by the WordPress plugin
    Given the server is available
    And the user is logged in
    When a search is made for Corsham
    #Then a list of 6 stock categories including all details is returned starting with Melksham
    And the default of 8 image URLs are included for the nearest category
    And the call took less than 300ms
    
  Scenario: Search stock categories by postcode
          Used by the WordPress plugin
    Given the server is available
      And the user is logged in
     When a search is made for an office near "CF71 7NQ"
     Then a list of 1 stock category is returned with full details
      And the default of 8 image URLs are included for the nearest category
      And the call took less than 300ms
    
  Scenario: Retrieve a specific stock category
          Used by both maintenance pages and the WordPress plugin
    Given the server is available
      And the user is logged in
     When a request is made for Swindon
     Then a single stock category is returned including all details
      And the default of 8 image URLs are included for the specified category
      And the call took less than 300ms
      
  Scenario: Fetch details of a specific stock item
          Used by both maintenance pages and the WordPress plugin
    Given the server is available
      And the user is logged in
     When a request is made for item 59287
     Then Office "1" is returned including all details
      And the stock item's category is available
      And the call took less than 300ms      