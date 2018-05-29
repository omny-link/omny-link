/*******************************************************************************
 * Copyright 2015-2018 Tim Stephenson and contributors
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
