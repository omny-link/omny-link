describe("Customer Management", function() {
  
  it("lists contacts", function() {
    var contacts = [];
    
    $.getJSON('/firmgains/contacts/?projection=complete',  function( data ) {
      contacts = data;
//      if (data._embedded == undefined) {
//        ractive.merge('contacts', data);
//      }else{
//        ractive.merge('contacts', data._embedded.contacts);
//      }
    });
    
    window.request = jasmine.Ajax.requests.mostRecent();
    
//    expect(request).not().toBeUndefined();
    expect(contacts.length).toEqual(5);
  });
});