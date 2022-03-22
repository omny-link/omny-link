 /*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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
(function (ractive) {
  var me = {
  };

    if ('curMailBody' in CKEDITOR.instances) {
      //
    } else {
      CKEDITOR.replace( 'curMailBody', {
        height: 150,
        toolbarGroups: [
          { name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
          { name: 'paragraph',   groups: [ 'list' ] }
        ]
      });
      CKEDITOR.instances.curMailBody.on('blur', function() {
        ractive.set('instanceToStart.variables.mailBody',
            CKEDITOR.instances.curMailBody.getData().replace(/"/g, '&quot;').replace(/'/g, '&apos;').replace(/[\n\t]/g,''));
      });
    }
    CKEDITOR.instances.curDescription.setData(ractive.get('current.description'));

  return me;
}(ractive));
