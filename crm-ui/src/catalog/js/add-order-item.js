/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributorss
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 ******************************************************************************/
(function ($, ractive) {
  ractive.observe('instanceToStart.variables.orderItem.stockItem.name', function(newValue, oldValue, keypath) {
    console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
    if (newValue!=undefined && newValue!='' && newValue.length >0) {
      var stockItem = Array.findBy('name', newValue, ractive.get('stockItems'));
      if (stockItem.id == undefined) stockItem.id = ractive.localId(ractive.uri(stockItem));
      ractive.set('instanceToStart.variables.orderItem.stockItem', stockItem);
    }
    console.log(ractive.get('instanceToStart.variables.orderItem.stockItem'));
  });

  ractive.set('instanceToStart.variables.orderItem.stockItem', ractive.get('current.stockItem'));
}($, ractive));

$(document).ready(function() {
  if (ractive.customActionCallbacks==undefined) ractive.customActionCallbacks = $.Callbacks();
  ractive.customActionCallbacks.add(function() { ractive.select(ractive.get('current')); });
});
