(function ($, $auth, ractive) {
  var me = {
  };

  ractive.observe('instanceToStart.processVariables.order.contactName', function(newValue, oldValue, keypath) {
    console.log('contact changing from '+oldValue+' to '+newValue);
    if (newValue!=undefined && newValue!='' && newValue.length >0) {
      var contact = Array.findBy('fullName', newValue, ractive.get('contacts'));
      ractive.set('instanceToStart.processVariables.order.contactId', ractive.localId(ractive.uri(contact)));
    }
    console.log('Selected contact '+ractive.get('instanceToStart.processVariables.order.contactId'));
  });

  ractive.set('instanceToStart.processDefinitionKeyOverride', 'CreateOrder');
  ractive.set('instanceToStart.processVariables.order.parent', ractive.get('current'));
  ractive.set('instanceToStart.processVariables.order.type', 'po');
  ractive.set('instanceToStart.processVariables.order.owner', $auth.getClaim('sub'));
  ractive.set('instanceToStart.processVariables.order.date', new Date());
  ractive.set('instanceToStart.processVariables.order.ref', -1);

}($, $auth, ractive));
