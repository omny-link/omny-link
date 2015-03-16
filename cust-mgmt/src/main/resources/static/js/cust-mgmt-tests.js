var noOfContacts = 0;
var testContactId;
var tenantId = 'firmgains';
var contact = {
    email:'george@omny.link',
    firstName:'George',
    lastName:'Orwell',
    tenantId:tenantId
};
var contactPhone = '+44 77988 67607' ; 

asyncTest('GET existing Contacts as baseline', function(){
  console.log('starting test 1');
  //  expect(2); // no. of async test to run
  var xhr = $.ajax({
    type: 'GET',
    url:  '/'+tenantId+'/contacts/?projection=complete'
  })
  .always(function(data, status){
    console.log('completed xhr 1');
    var $data = $(data);
    equal(status, 'success', 'Get contacts should return success');
    //equal(data.length, 6, 'Expected 6 contacts returned');
    noOfContacts = data.length;
    start(); // we have our answer for this assertion, continue testing other assertions
  });
});

asyncTest('Create a new contact', function(){
  console.log('starting test 2');
  var xhr = $.ajax({
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(contact),
    url:  '/contacts'
  })
  .always(function(data, status, jqxhr){
    console.log('completed xhr 2');
    var $data = $(data);
    equal(status, 'success', 'Create new contact should return success');
    console.log('data:'+data);
    var location = jqxhr.getResponseHeader('Location');
    testContactId = location.substring(location.lastIndexOf('/')+1);
    start(); // we have our answer for this assertion, continue testing other assertions
  });

});

asyncTest('Check new contact exists', function(){
  console.log('starting test 3');
  //  expect(2); // no. of async test to run
  var xhr = $.ajax({
    type: 'GET',
    url:  '/'+tenantId+'/contacts/?projection=complete'
  })
  .always(function(data, status){
    console.log('completed xhr 3');
    var $data = $(data);
    equal(status, 'success', 'Get contacts should return success');
    console.log('data:'+data);
    console.log('data.length:'+data.length);
    equal(data.length, (noOfContacts+1), 'Additional contact should be found now');
    start(); // we have our answer for this assertion, continue testing other assertions
  });
});

asyncTest('Update test contact', function() {
  console.log('starting test 4');
  //  expect(2); // no. of async test to run
  
  contact.phone = contactPhone;
  var xhr = cm.saveContact(contact)
  .always(function(data, status){
    console.log('completed xhr 4');
    var $data = $(data);
    equal(status, 'success', 'Update contacts should return success');
    start(); // we have our answer for this assertion, continue testing other assertions
  });
});

asyncTest('Check contact updated ok', function(){
  console.log('starting test 4.1');
  //  expect(2); // no. of async test to run
  var xhr = cm.getContact(testContactId)
  .always(function(data, status){
    console.log('completed xhr 4.1');
    var $data = $(data);
    equal(status, 'success', 'Get test contact should return success');
    console.log('data:'+data);
    data.phone = contactPhone; 
    
    start(); // we have our answer for this assertion, continue testing other assertions
  });
});


// TODO This reports success, even see the Hibernate delete statement in the 
// server log but the record remains. 
asyncTest('Remove test contact', function() {
  console.log('starting test 5');
  //  expect(2); // no. of async test to run
  var xhr = $.ajax({
    type: 'DELETE',
    url:  '/contacts/'+testContactId
  })
  .always(function(data, status){
    console.log('completed xhr 5');
    var $data = $(data);
    equal(status, 'nocontent', 'Delete test contact should return nocontent');
    start(); // we have our answer for this assertion, continue testing other assertions
  });
});