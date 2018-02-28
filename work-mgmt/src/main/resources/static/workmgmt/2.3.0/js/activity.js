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
/**
 * User Activity library
 */

var ua = new UserActivity();
ua.init();

function UserActivity() {
  this.enabled = false;
  this.server = $env.server;
  // default only, user will override
  this.tenantId = 'omny';
  this.getId = function() {
    var tmp = document.cookie.substring(document.cookie.indexOf('ua=')+3);
    if (tmp.indexOf(';')!=-1) return tmp.substring(0, tmp.indexOf(';'));
    else return tmp;
  };
  this.init = function() {
    if (!ua.enabled) return;
    if (document.cookie.indexOf('ua') >= 0) {
      // user has been here before.
      ua.record('visit',document.location.href);
    } else {
      // new visitor
      expiry = new Date();
      expiry.setTime(expiry.getTime()+(30*60*60*1000)); // 30 days

      var uuid = ua.uuid();
      // Date()'s toGMTSting() method will format the date correctly for a cookie
      document.cookie = 'ua='+uuid+'; expires=' + expiry.toGMTString();
      ua.record('newVisitor',document.location.href);
    }
//    $('a,button,input[type="button"],input[type="submit"]').on('click',function(ev) {
//      console.error('gotcha!');
//      ua.record('click',ev.getTarget.getName());
//      ev.preventDefault();
//    });
  };
  this.linkToKnownContact = function(username) {
    if (!ua.enabled) return;
    console.info('UA:linkToUser: '+username);
    var d = {
        "uuid":ua.getId(),
        "email":username,
        "tenantId":ua.tenantId
      };
    console.log('d: '+JSON.stringify(d));
    return $.ajax({
      url: ua.server+'/msg/'+ua.tenantId+'/omny.userRecognition.json',
      type: 'POST',
      data: { json:JSON.stringify(d) },
      dataType: 'text',
      success: completeHandler = function(data) {
        console.log('Message received:'+data);
      }
    });
  };
  this.login = function(username) {
    if (!ua.enabled) return;
    console.info('UA:login:'+username);
//    ua.record('login',username);
    ua.linkToKnownContact(username);
  };
  this.record = function(type,content) {
    if (!ua.enabled) return;
    console.info('UA:record: '+type+' '+ua.getId()+(content===undefined ? '' : ': '+content));
    var d = {
        "type":type,
        "content":content===undefined ? '' : content,
        "stage": "On hold",
        "uuid":ua.getId(),
        "tenantId":ua.tenantId
      };
    console.log('d: '+JSON.stringify(d));
    return $.ajax({
      url: ua.server+'/msg/'+ua.tenantId+'/omny.userActivity.json',
      type: 'POST',
      data: { json:JSON.stringify(d) },
      dataType: 'text',
      success: completeHandler = function(data) {
        console.log('Message received:'+data);
      }
    });
  };
  this.uuid = function() {
    var d = new Date().getTime();
    if(window.performance && typeof window.performance.now === "function"){
        d += performance.now(); //use high-precision timer if available
    }
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
    });
    return uuid;
  }
}
