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
(function ($, ractive) {
  var me = {
    dirty: false,
    rtn: undefined
  };
  const DELAY = 500;
  function fetchPdf() {
    console.info('fetchPdf');

    var tmp = JSON.parse(JSON.stringify(ractive.get('instanceToStart')));
    tmp.processDefinitionKey = 'MergeMemoTemplate';
    if (tmp.processVariables.memoId == undefined || tmp.processVariables.memoId.length == 0) {
      ractive.showMessage('Please supply all the required fields');
      showMemoSpec();
      return;
    } else {
      ractive.showMessage('Your download will start shortly...');
    }
    if (ractive.get('resultLocation')==undefined) fetchPreview(fetchPdf);
    else openResponseWindow(ractive.get('resultLocation'));
  }
  function fetchPreview(callback) {
    var tmp = JSON.parse(JSON.stringify(ractive.get('instanceToStart')));
    tmp.processDefinitionKey = 'MergeMemoTemplate';
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(tmp),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        console.log('response: '+ jqXHR.status+", Location: "+location);
        ractive.set('memoHtml','Merging '+ractive.get('instanceToStart.processVariables.memoName')+' template for '+ractive.get('instanceToStart.businessKey'));
        ractive.set('instanceStarted', data);
        setTimeout(fetchResult, DELAY, location);
        if (callback != undefined) setTimeout(callback, DELAY, location);
      }
    });
  }
  function fetchResult(location) {
    ractive.set('resultLocation', ractive.getServer()+'/'+ractive.get('tenant.id')+location+'/variables/mergedMemo');
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+location+'/variables/mergedMemo',
      headers: {
        'Accept': 'text/html'
      },
      success: function( data ) {
        console.log('  result starts: '+data.substring(0,16));
        ractive.set('memoHtml', data);
        if ('Still working...' == data) setTimeout(fetchResult, DELAY, location);
      },
      error: function(jqXHR, textStatus, errorThrown) {
        console.log('  error');
      }
    });
  }
  function showMemoSpec() {
    console.info('showMemoSpec');
    $('#pickMemo .nav-tabs li:nth-child(1)').addClass('active');
    $('#pickMemo .nav-tabs li:nth-child(2)').removeClass('active');
    $('#pickMemo section').toggle();
  }
  function showMemoPreview() {
    console.info('showMemoPreview');
    $('#pickMemo .nav-tabs li:nth-child(2)').addClass('active');
    $('#pickMemo .nav-tabs li:nth-child(1)').removeClass('active');
    $('#pickMemo section').toggle();

    var tmp = JSON.parse(JSON.stringify(ractive.get('instanceToStart')));
    tmp.processDefinitionKey = 'MergeMemoTemplate';
    validateOrder();
    if (!document.forms['customActionForm'].checkValidity()) {
      ractive.showMessage('Please supply all the required fields');
      showMemoSpec();
      return;
    }
    fetchPreview();
  }

  function openResponseWindow(location) {
    console.info('openResponseWindow'+location);
    setTimeout(function() {
      window.open(location);
    }, 2000);
  }
  function initMemosSource() {
    console.info('initMemosSource');
    var memoList = jQuery.map(ractive.get('memos'), function( n, i ) {
      console.log('memo: '+ractive.tenantUri(n)+', '+n.name+', i:'+i);
      var t = Array.findBy('ref', n.name, ractive.get('tenant.templates'));
      if (t == undefined || t.role == undefined || ractive.hasRole(t.role)) {
        if (n['requiredVars']!=undefined) {
          var requiredVarList = n.requiredVars.split(',');
          for(var idx = 0 ; idx < requiredVarList.length ; idx++) {
            console.log('  do we have '+requiredVarList[idx]);
            if (requiredVarList[idx].toLowerCase().indexOf('formatter')==-1
                && requiredVarList[idx].length>0 && ractive.get('instanceToStart.processVariables.'+requiredVarList[idx]) == undefined
                && ractive.get('instanceToStart.processVariables.'+requiredVarList[idx]+'Id') == undefined
                && (requiredVarList[idx].toLowerCase().indexOf('owner')>=0 && ractive.get('current.owner') == undefined)) {
              console.log('  ... not all data available');
              return undefined;
            }
          }
        }
        console.log('  ... allowed');
        return ( {id: ractive.tenantUri(n), name: n.name} );
      } else {
        console.log('  ... disallowed');
        return undefined;
      }
    });
    ractive.set('memoList', memoList);
    return memoList;
  }
  function initOrdersAutocomplete() {
    console.info('initOrdersAutocomplete');
    ractive.set('ordersTypeahead',jQuery.map(ractive.get('orders'), function( n, i ) {
      console.log('n: '+ractive.tenantUri(n)+', i:'+i);
      return ( {
        id: ractive.localId(ractive.tenantUri(n)),
        name: ractive.localId(ractive.tenantUri(n))
      } );
    }));
    ractive.addDataList({ name: "orders" }, ractive.get('ordersTypeahead'));
    $('#curOrderDisplay').blur(function(ev) {
        console.info('afterSelect:'+ev.target);
        var orderId = ev.target.value;
        ractive.set('instanceToStart.processVariables.orderId',orderId);
        ractive.set('instanceToStart.processVariables.orderName',orderId);
        var order = Array.findBy('orderId', orderId, ractive.get('orders'));
        if (ractive.get('currentContact')==undefined && order['contactId']!=undefined) {
          if (order['contactId']!=undefined) {
            ractive.set('instanceToStart.processVariables.contactId','/contacts/'+order['contactId']);
          }
          if (order['stockItem']!=undefined) {
            ractive.set('instanceToStart.processVariables.stockItemId','/stock-items/'+order['stockItem']['id']);
          }
      }
    });
  }
  function fetchMemos() {
    console.log('fetchMemos...');
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memos/findByStatus/Published',
      crossDomain: true,
      success: function( data ) {
        console.log('Found '+data.length+' memos.');
        if (data['_embedded'] == undefined) {
          ractive.set('memos', data);
        } else {
          ractive.set('memos', data['_embedded'].memos);
        }
        ractive.addDataList({ name: "memos" }, initMemosSource());
        $('#curMemoDisplay').blur(function(ev) {
            console.info('afterSelect:'+ev.target);
            var memoName = ev.target.value;
            var memoId = Array.findBy('name',memoName,ractive.get('memoList')).id;
            ractive.set('instanceToStart.processVariables.memoId',memoId.substring(memoId.lastIndexOf('/')+1));
            ractive.set('instanceToStart.processVariables.memoName',memoName);
        });
      }
    });
  }
  function validateOrder() {
    console.info('validateOrder');
    if (ractive.get('instanceToStart.processVariables.memoId')!=undefined) {
      var memo = Array.findBy('selfRef', '/memos/'+ractive.get('instanceToStart.processVariables.memoId'), ractive.get('memos'));
      if (memo!=undefined && memo['requiredVars']!=undefined && memo.requiredVars.indexOf('order')!=-1) {
        if (ractive.get('instanceToStart.processVariables.orderId')=='TBD') {
          $('#curOrderDisplay').attr('required','required').prev().addClass('required');
        } else {
          $('#curOrderDisplay').removeAttr('required').prev().removeClass('required');
        }
      }
    }
  }

  $(document).ready(function() {
    console.info('ready event on pick-memo');

    if ($('#curOrderDisplay').length>0) {
      if (ractive.get('orders') == undefined || ractive.get('orders').length == 0) {
        ractive.showError('You have not created any '+ractive.get('tenant.strings.orders')+' yet');
        return;
      } else {
        ractive.set('instanceToStart.processVariables.orderId','TBD');
        // if there is an order there will be a contact even if we not on contacts page
        if (ractive.get('instanceToStart.processVariables.contact') == undefined && ractive.get('instanceToStart.processVariables.contactId') == undefined) {
          ractive.set('instanceToStart.processVariables.contactId','TBD');
        }
        ractive.set('instanceToStart.processVariables.stockItemId','TBD');
      }
      if (ractive.entityName(ractive.get('current'))=='orders') {
        var order = ractive.get('current');
        if (order['contactId']==undefined || order['stockItem']==undefined) {
          ractive.showError('You must specify both Contact and '+ractive.get('tenant.strings.stockItem')+' first');
        }
        ractive.set('instanceToStart.processVariables.contactId','/contacts/'+order['contactId']);
        ractive.set('instanceToStart.processVariables.stockItemId','/stock-items/'+order['stockItem']['id']);
        ractive.set('instanceToStart.processVariables.orderId',ractive.id(order));
        ractive.set('instanceToStart.processVariables.orderName',ractive.id(order));
        $('#curOrderDisplay').attr('readonly','readonly').attr('disabled','disabled');
      } else {
        initOrdersAutocomplete();
      }
    }
    fetchMemos();
    $('#curMemoDisplay').on('blur', validateOrder);

    $('#pickMemo .nav-tabs li:nth-child(1)').off().on('click',showMemoSpec);
    $('#pickMemo .nav-tabs li:nth-child(2)').off().on('click',showMemoPreview);
    $('#pickMemo .nav-tabs li:nth-child(3)').off().on('click',fetchPdf);
  });

  return me;
}($, ractive));
