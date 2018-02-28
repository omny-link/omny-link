/*******************************************************************************
 * Copyright 2011-2018 Tim Stephenson and contributors
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
var hd = new HelpDesk();

function HelpDesk() {
  this.createTicket = function() {
    console.info('createTicket');
    $('#ticketModal').modal({});
  }
  this.submit = function() {
    var c = document.getElementById('hdScreenshot');
//    c.toBlob(function(blob) {
//      console.log('have blob, submitting...');
      $.ajax({
        url: ractive.getServer()+'/omny/helpdesk',
        type: 'POST',
        contentType: 'application/upload',
        data: JSON.stringify({ message: $('#ticketForm #what').val(), image:c.toDataURL("image/png") }),
        success: completeHandler = function(data,textStatus,jqXHR) {
          console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
          ractive.showMessage('Submitted ticket');
        },
      });
//    });
    $('#ticketModal').modal('hide');
  };
  this.takeScreenshot = function(obj) {
    html2canvas(document.body).then(function(canvas) {
      // check no previous screenshot
      var c = document.getElementById('hdScreenshot');
      if(c != undefined) {
        document.body.removeChild(c);
      }

      canvas.id = 'hdScreenshot';
      canvas.setAttribute('style', 'display:none');
      document.body.appendChild(canvas);

      hd.createTicket();
    });
  }
}
