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
(function ($, $env, ractive) {
  /* 
   * Augment ractive with document and notes support
   */
  ractive.addDoc = function() {
    console.log('addDoc');
    if (ractive.uri(ractive.get('current'))==undefined) {
      ractive.showMessage('You must have created your record before adding documents');
      ractive.set('saveObserver', true);
      return;
    }
    ractive.set('saveObserver', false);
    if (ractive.get('current.documents') == undefined) ractive.set('current.documents', []);
    if (ractive.get('current.documents.0')!=undefined && ractive.get('current.documents.0.created') == undefined) return $($('#docsTable input:invalid')[0]).focus();
    ractive.splice('current.documents', 0, 0, {
      author: ractive.get('profile.username'),
      content: '',
      entityName: ractive.uri(ractive.get('current')),
      favorite: true
    });
    ractive.set('saveObserver', true);
    if ($('#docsTable:visible').length==0) ractive.toggleSection($('#docsTable').closest('section'));
    $('#docsTable tr:nth-child(1)').slideDown();
  };
  ractive.addNote = function() {
    console.log('addNote');
    ractive.set('saveObserver', false);
    if (ractive.uri(ractive.get('current'))==undefined) {
      ractive.showMessage('You must have created your record before adding notes');
      ractive.set('saveObserver', true);
      return;
    }
    if (ractive.get('current.notes') == undefined) ractive.set('current.notes', []);
    if (ractive.get('current.notes.0')!=undefined && ractive.get('current.notes.0.created') == undefined) return $($('#notesTable textarea:invalid')[0]).focus();
    ractive.splice('current.notes', 0, 0, {
      author: ractive.get('profile.username'),
      content: '',
      entityName: ractive.uri(ractive.get('current')),
      favorite: true
    });
    ractive.set('saveObserver', true);
    if ($('#notesTable:visible').length==0) ractive.toggleSection($('#notesTable').closest('section'));
    $('#notesTable tr:nth-child(1)').slideDown();
    document.getElementById("note").focus();
  };
  ractive.autolinker = function() {
    if (ractive._autolinker==undefined) ractive._autolinker = new Autolinker({
        email: true,
        hashtag: 'twitter',
        mention: 'twitter',
        newWindow : true,
        stripPrefix: {
          scheme : true,
          www    : true
        },
        truncate  : 30
    });
    return ractive._autolinker;
  };
  ractive.cancelDoc = function() {
    console.info('cancelDoc');
    ractive.splice('current.documents', 0, 1);
    ractive.toggleSection($('#docsTable').closest('section'));
  };
  ractive.cancelNote = function() {
    console.info('cancelNote');
    ractive.splice('current.notes', 0, 1);
    ractive.toggleSection($('#notesTable').closest('section'));
  };
  ractive.fetchDocs = function() {
    $.getJSON(ractive.uri(ractive.get('current'))+'/documents',  function( data ) {
      if ('_embedded' in data) {
        console.log('found docs '+data);
        ractive.merge('current.documents', data._embedded.documents);
        // sort most recent first
        ractive.get('current.documents').sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      }
      ractive.set('saveObserver',true);
    });
  };
  ractive.saveDoc = function() {
    console.log('saveDoc');
    if (ractive.get('current.documents')==undefined || ractive.get('current.documents').length==0) return;
    var n = ractive.get('current.documents.0');
    var url = ractive.tenantUri(ractive.get('current'))+'/documents';
    if (document.getElementById('documentForm').checkValidity() &&
        n.url != undefined && n.url.trim().length > 0) {
      $.ajax({
        url: url,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(n),
        success: function(data) {
          console.log('response: '+ JSON.stringify(data));
          ractive.showMessage('Document link saved successfully');
          ractive.set('saveObserver',false);
          ractive.set('current.documents.0.created',data.created);
          ractive.set('saveObserver',true);
          $('#doc,#docName').val(undefined);
        }
      });
    } else {
      $($('#documentForm :invalid').addClass('field-error')[0]).focus();
    }
  };
  ractive.saveNote = function(n) {
    console.info('saveNote '+JSON.stringify(n)+' ...');
    /// TODO this is temporary for backwards compatibility with older workflow forms
    if (n == undefined) {
      n = ractive.get('current.notes.0');
      n.content = $('#note').val();
    }
    var url = ractive.tenantUri(ractive.get('current'))+'/notes';
    console.log('  url:'+url);
    if (n.content != undefined && n.content.trim().length > 0) {
      $.ajax({
        url: url,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(n),
        success: function(data) {
          console.log('response: '+ JSON.stringify(data));
          ractive.showMessage('Note saved successfully');
          ractive.set('saveObserver',false);
          ractive.set('current.notes.0.id', data.id);
          ractive.set('current.notes.0.created',data.created);
          ractive.set('saveObserver',true);
        }
      });
    }
  };
  ractive.toggleShowConfidentialNotes = function(btn) {
    console.info('toggleShowConfidentialNotes');
    if (ractive.get('current.owner')!=ractive.get('profile.username')) {
      ractive.showMessage('Since you\'re not the owner you cannot view confidential notes', 'alert-warning');
      return;
    }
    $('#notesTable tr.confidential').slideToggle();
    $(btn).toggleClass('kp-icon-lock kp-icon-unlock');
  };
  ractive.toggleShowFavoriteNotes = function(btn) {
    console.info('toggleShowFavoriteNotes');
    $('#notesTable tr.unfavorite').slideToggle();
    $(btn).toggleClass('glyphicon-star glyphicon-star-empty');
  };
  ractive.toggleNoteConfidentiality = function(idx) {
    console.info('toggleNoteConfidentiality: '+idx);
    ractive.set('current.notes.'+idx+'.confidential',!ractive.get('current.notes.'+idx+'.confidential'));
    var n = ractive.get('current.notes.'+idx);
    var url = ractive.getServer()+'/'+ractive.get('tenant.id')+'/notes/'+ractive.localId(n)+'/confidential';

    $.ajax({
      url: url,
      type: 'POST',
      data: { confidential: n.confidential },
      success: function(data) {
        console.log('response: '+ data);
        ractive.showMessage('Note marked as '+(n.confidential ? 'confidential' : 'non-confidential'));
      }
    });
  };
  ractive.toggleNoteFavorite = function(idx) {
    console.info('toggleNoteFavorite: '+idx);
    ractive.set('current.notes.'+idx+'.favorite',!ractive.get('current.notes.'+idx+'.favorite'));
    var n = ractive.get('current.notes.'+idx);
    var url = ractive.getServer()+'/'+ractive.get('tenant.id')+'/notes/'+ractive.localId(n)+'/favorite';

    $.ajax({
      url: url,
      type: 'POST',
      data: { favorite: n.favorite },
      success: function(data) {
        console.log('response: '+ data);
        ractive.showMessage('Note '+(n.favorite ? 'favorited' : 'un-favorited'));
      }
    });
  };

  /*
   * Support ticket support
   */
  ractive.createTicket = function() {
    console.info('createTicket');
    $('#ticketModal').modal({});
  };
  ractive.submit = function() {
    var c = document.getElementById('hdScreenshot');
  //      c.toBlob(function(blob) {
  //        console.log('have blob, submitting...');
      $.ajax({
        url: ractive.getServer()+'/omny/helpdesk',
        type: 'POST',
        contentType: 'application/upload',
        data: JSON.stringify({ message: $('#ticketForm #what').val(), image:c.toDataURL("image/png") }),
        success: function(data,textStatus,jqXHR) {
          console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
          ractive.showMessage('Submitted ticket');
        },
      });
  //      });
    $('#ticketModal').modal('hide');
  };
  ractive.takeScreenshot = function() {
    html2canvas(document.body).then(function(canvas) {
      // check no previous screenshot
      var c = document.getElementById('hdScreenshot');
      if(c != undefined) {
        document.body.removeChild(c);
      }
  
      canvas.id = 'hdScreenshot';
      canvas.setAttribute('style', 'display:none');
      document.body.appendChild(canvas);
  
      ractive.createTicket();
    });
  };
}($, $env, ractive));
