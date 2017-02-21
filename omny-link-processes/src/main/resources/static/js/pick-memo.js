function fetchPdf() {
  console.info('fetchPdf');

  var tmp = JSON.parse(JSON.stringify(ractive.get('instanceToStart')));
  tmp.processDefinitionId = 'MergeMemoTemplate';
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
  tmp.processDefinitionId = 'MergeMemoTemplate';
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
      setTimeout(fetchResult, 1000, location);
      if (callback != undefined) setTimeout(callback, 1000, location);
    }
  });
}
function fetchResult(location) {
  ractive.set('resultLocation', ractive.getServer()+'/'+ractive.get('tenant.id')+location+'/variables/result');
  $.ajax({
    url: ractive.getServer()+'/'+ractive.get('tenant.id')+location+'/variables/result',
    headers: {
      'Accept': 'text/html'
    },
    success: function( data ) {
      console.log('  retrieved merged memo');
      ractive.set('memoHtml', data);
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
  tmp.processDefinitionId = 'MergeMemoTemplate';
  if (tmp.processVariables.memoId == undefined || tmp.processVariables.memoId.length == 0) {
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
    console.log('memo: '+n.selfRef+', '+n.name+', i:'+i);
    var t = Array.findBy('ref', n.name, ractive.get('tenant.templates'));
    if (t == undefined || t.role == undefined || ractive.hasRole(t.role)) {
      if (n['requiredVars']!=undefined) {
        var requiredVarList = n.requiredVars.split(',');
        for(idx in requiredVarList) {
          console.log('  do we have '+requiredVarList[idx]);
          if (requiredVarList[idx].length>0 && ractive.get('instanceToStart.processVariables.'+requiredVarList[idx]) == undefined 
              && ractive.get('instanceToStart.processVariables.'+requiredVarList[idx]+'Id') == undefined) {
            console.log('  ... not all data available');
            return undefined;
          }
        }
      }
      console.log('  ... allowed');
      return ( {id: n.selfRef, name: n.name} );
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
    //if (n.orderItems == undefined || n.orderItems.length == 0) return;
    console.log('n: '+n.selfRef+', '+n.id+', i:'+i);
    return ( {id: ractive.shortId(n.selfRef), name: n['name']} );
    //return ( {id: ractive.shortId(n.selfRef), name: Array.findBy('selfRef','/contacts/'+n.contactId,ractive.get('current.contacts')).fullName+' '+n.orderItems[0].customFields['date']} );
  }));
  
  $('#curOrderDisplay').typeahead({ 
    items:'all',
    minLength:0,
    source: ractive.get('ordersTypeahead'),
    afterSelect:function(d) {
      console.info('afterSelect:'+d);
      ractive.set('instanceToStart.processVariables.orderId',d.id);
      ractive.set('instanceToStart.processVariables.orderName',d.name);
      var order = Array.findBy('orderId', d.id, ractive.get('orders'));
      if (order['contactId']!=undefined) {
        ractive.set('instanceToStart.processVariables.contactId','/contacts/'+order['contactId']);
      }
    }
  });
  $('#curOrderDisplay').on("click", function (ev) {
    newEv = $.Event("keydown");
    newEv.keyCode = newEv.which = 40;
    $(ev.target).trigger(newEv);
    return true;
  });
}
function fetchMemos() {
  console.log('fetchMemos...');
  $.ajax({
    dataType: "json",
    url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memos/',
    crossDomain: true,
    success: function( data ) {
      console.log('Found '+data.length+' memos.');
      if (data['_embedded'] == undefined) {
        ractive.set('memos', data);
      }else{
        ractive.set('memos', data['_embedded'].memos);
      }
      $('#curMemoDisplay').typeahead({ 
        items:'all',
        minLength:0,
        source: initMemosSource(),
        afterSelect:function(d) {
          console.info('afterSelect:'+d);
          ractive.set('instanceToStart.processVariables.memoId',d.id.substring(d.id.lastIndexOf('/')+1));
          ractive.set('instanceToStart.processVariables.memoName',d.name);
        }
      });
      $('#curMemoDisplay').on("click", function (ev) {
        newEv = $.Event("keydown");
        newEv.keyCode = newEv.which = 40;
        $(ev.target).trigger(newEv);
        return true;
      });
    }     
  });
}
$(document).ready(function() {
  console.info('ready event on pick-memo');
  $('#curMemoDisplay').val(undefined);
//  ractive.customActionCallbacks = $.Callbacks();
//  ractive.customActionCallbacks.add(openResponseWindow);
  
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
    }
    initOrdersAutocomplete();
  }
  fetchMemos();
});