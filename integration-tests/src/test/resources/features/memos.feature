Feature: A repository of memo templates for creating merging into emails, documents etc. 

    Scenario: Maintain the list of memo templates
              Used by {server}/memos.html
        Given the server is available
          And the user is logged in
          And there are 13 memos in the system for the omny tenant 
         When a list of memos is requested
         Then a list of 13 memo _summaries_ is returned
          And the call took less than 300ms

         When a new memo named 'TestMemo' is created including owner,status,subject,richContent and shortContent
          And a list of memos is requested
         Then a list of 14 memo _summaries_ is returned
          And the call took less than 300ms
          
         When an update is made to the memo named 'TestMemo' changing several fields
          And a list of memos is requested
         Then a list of 14 memo _summaries_ is returned
          And the call took less than 300ms
          
         When the memo named 'TestMemo' is deleted
          And a list of memos is requested
         Then a list of 14 memo _summaries_ is returned
          And the call took less than 300ms          