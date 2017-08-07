(function ($, ractive) {
  var me = {
  };

  ractive.observe('instanceToStart.processVariables.orderItem.stockItemName', function(newValue, oldValue, keypath) {
    console.log('stock item changing from '+oldValue+' to '+newValue);
    if (newValue!=undefined && newValue!='' && newValue.length >0) {
      ractive.set('instanceToStart.processVariables.orderItem.stockItem',{ id: ractive.localId(Array.findBy('name', newValue, ractive.get('stockItems')).selfRef) });
    }
    console.log(ractive.get('instanceToStart.processVariables.orderItem.stockItem'));
  });

//  return me;
}($, ractive));

$(document).ready(function() {
  if (ractive.customActionCallbacks==undefined) ractive.customActionCallbacks = $.Callbacks();
  ractive.customActionCallbacks.add(function() { ractive.select(ractive.get('current')) });
});
