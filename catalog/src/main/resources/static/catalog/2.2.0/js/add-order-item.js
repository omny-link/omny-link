(function ($, ractive) {
  var me = {
  };

  ractive.observe('instanceToStart.processVariables.orderItem.stockItem.name', function(newValue, oldValue, keypath) {
    console.log('stock item changing from '+oldValue+' to '+newValue);
    if (newValue!=undefined && newValue!='' && newValue.length >0) {
      var stockItem = Array.findBy('name', newValue, ractive.get('stockItems'));
      if (stockItem.id == undefined) stockItem.id = ractive.localId(ractive.uri(stockItem));
      ractive.set('instanceToStart.processVariables.orderItem.stockItem', stockItem);
    }
    console.log(ractive.get('instanceToStart.processVariables.orderItem.stockItem'));
  });

  ractive.set('instanceToStart.processVariables.orderItem.stockItem', ractive.get('current.stockItem'));

//  return me;
}($, ractive));

$(document).ready(function() {
  if (ractive.customActionCallbacks==undefined) ractive.customActionCallbacks = $.Callbacks();
  ractive.customActionCallbacks.add(function() { ractive.select(ractive.get('current')) });
});