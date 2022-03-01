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
(function ($, $auth, ractive) {

//  ractive.set('instanceToStart.processDefinitionKeyOverride', 'CreateOrder');
  ractive.set('instanceToStart.variables.contactLocalId', ractive.get('current.contactId'));
//  ractive.set('instanceToStart.variables.order.type', 'po');
//  ractive.set('instanceToStart.variables.order.owner', ractive.get('profile.username'));
//  ractive.set('instanceToStart.variables.order.date', new Date());
//  ractive.set('instanceToStart.variables.order.ref', -1);

}($, $auth, ractive));
