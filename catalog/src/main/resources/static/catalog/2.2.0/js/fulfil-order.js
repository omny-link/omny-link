(function ($, $auth, ractive) {

//  ractive.set('instanceToStart.processDefinitionKeyOverride', 'CreateOrder');
  ractive.set('instanceToStart.processVariables.contactLocalId', ractive.get('current.contactId'));
//  ractive.set('instanceToStart.processVariables.order.type', 'po');
//  ractive.set('instanceToStart.processVariables.order.owner', $auth.getClaim('sub'));
//  ractive.set('instanceToStart.processVariables.order.date', new Date());
//  ractive.set('instanceToStart.processVariables.order.ref', -1);

}($, $auth, ractive));
