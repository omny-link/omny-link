Feature: A repository of different stock in categories and units
 
  Scenario: List all stock categories
          Used by the maintenance pages
    Given the user is logged in
     When a list of stock categories is requested
     Then a list of 59 stock categories _summaries_ is returned
      And the call took less than 500ms
    
  Scenario: List all stock items
          Used by the maintenance pages
    Given the user is logged in
     When a list of stock items is requested
     Then a list of 3593 stock item _summaries_ is returned
      And the call took less than 6000ms
    
  Scenario: Search stock categories by town
          Used by the WordPress plugin
    Given the user is logged in
     When a search is made for Corsham
     Then a list of 6 stock categories is returned with full details
      And the first category is Melksham, which has the default of 8 image URLs and contains 3 items
      And the call took less than 300ms
    
  Scenario: Search stock categories by postcode
          Used by the WordPress plugin
    Given the user is logged in
     When a search is made for an office near "CF71 7NQ"
     Then a list of 5 stock categories is returned with full details
      And the first category is Caerphilly, which has the default of 8 image URLs and contains 2 items
      And the call took less than 300ms
    
  Scenario: Retrieve a specific stock category by name
          Used by the WordPress plugin
    Given the user is logged in
     When a request is made for Swindon
     Then category Swindon alone is returned including name, description, status, address1, postcode, tags & directions
      And the default of 8 image URLs are included for the specified category
      And 4 units are included each with 4 image urls, name, description, tags, and size
      And the call took less than 300ms

  Scenario: Retrieve details for Borehamwood
          Support case
    Given the user is logged in
     When a request is made for Borehamwood
     Then category Borehamwood alone is returned including name, description, status, address1, postcode, tags & directions
      And the default of 8 image URLs are included for the specified category
      And 3 units are included each with 4 image urls, name, description, tags, and size
      And the call took less than 300ms
      
  Scenario: Fetch and update details of a specific stock item
          Used by maintenance pages
    Given the user is logged in
     When a request is made for Caerphilly
     Then category Caerphilly alone is returned including name, description, status, address1, postcode, tags & directions
      And 2 units are included each with 4 image urls, name, description, tags, and size
     When a request is made for item 59288
     Then Office "2" is returned including all details
      And the stock item's category Caerphilly is available
      And the call took less than 300ms
     When the item status is updated to "Pending Review"
     Then success status is returned
     When a request is made for Caerphilly
     Then category Caerphilly alone is returned including name, description, status, address1, postcode, tags & directions
      And the default of 8 image URLs are included for the specified category
      And 1 units are included each with 4 image urls, name, description, tags, and size
     When a request is made for item 59288
     Then Office "2" is returned including all details
     When the item status is updated to "Published"
     Then success status is returned
     When a request is made for Caerphilly
     Then category Caerphilly alone is returned including name, description, status, address1, postcode, tags & directions
      And 2 units are included each with 4 image urls, name, description, tags, and size
