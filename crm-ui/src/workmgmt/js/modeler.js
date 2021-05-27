function BpmnModeler(options) {

  /*** PROPERTIES ***/

  var DEFAULT_DATUM_HEIGHT = 60;
  var DEFAULT_DATUM_WIDTH = 48;
  var DEFAULT_EVENT_HEIGHT = 30;
  var DEFAULT_EVENT_WIDTH = 30;
  var DEFAULT_FONT_SIZE = 14;
  var DEFAULT_GATEWAY_HEIGHT = 40;
  var DEFAULT_GATEWAY_WIDTH = 40;
  var DEFAULT_LANE_HEIGHT = 98;
  var DEFAULT_LANE_WIDTH = 899;
  var DEFAULT_POOL_HEIGHT = 100;
  var DEFAULT_POOL_WIDTH = 930;
  var DEFAULT_SUBPROCESS_HEIGHT = 160;
  var DEFAULT_SUBPROCESS_WIDTH = 360;
  var DEFAULT_TASK_HEIGHT = 80;
  var DEFAULT_TASK_WIDTH = 100;
  var FONT_WIDTH_WEIGHTING = 0.2;
  var LABEL_OFFSET = 5;
  var LANE_LABEL_WIDTH = 25;
  var LANE_X_OFFSET = 31;
  var LANE_Y_OFFSET = 1;
  var TASK_LABEL_OFFSET = 20;
  var GAP = 50;

  var BLACK = '#000';
  var GREEN = '#34bb16';
  var RED = '#ff0000';
  var WHITE = '#fff';
  var KP_BG = '#779C95';
  var KP_BORDER = BLACK;
  var KP_NS = 'http://knowprocess.com/'
  var KP_TEXT = '#666';
  var KP_TASK_TEXT = WHITE;

  var me = {
    serializer: new XMLSerializer(),
    transformer: new XSLTProcessor(),

    showBounds: false,
    showIssues: true,
    debug: false,

    BPMN: "http://www.omg.org/spec/BPMN/20100524/MODEL",
    BPMN_DI: "http://www.omg.org/spec/BPMN/20100524/DI",
    DI: "http://www.omg.org/spec/DD/20100524/DI",
    DC: "http://www.omg.org/spec/DD/20100524/DC",
    COLOR: "http://www.omg.org/spec/BPMN/non-normative/color/1.0",
    ACTIVITI: 'http://activiti.org/bpmn'
  };

  me.addDataAssociation = function(bpmn, ids) {
    var src = bpmn.querySelector('#'+ids[0]);
    var trgt = bpmn.querySelector('#'+ids[1]);
    var obj;

    if (src.localName.indexOf('data')==0){
      // input
      type = 'dataInput';
      obj = trgt;
    } else {
      // output
      type = 'dataOutput';
      obj = src;
    }

    // find / create IO specification
    var ioSpec = bpmn.querySelector('#'+obj.id+' ioSpecification');
    if (ioSpec == undefined) {
      ioSpec = bpmn.createElementNS(me.BPMN, "semantic:ioSpecification");
      ioSpec.id = 'ioSpec_'+_uid();
      obj.appendChild(ioSpec);
    }
    // create data in/output
    var dataType = bpmn.createElementNS(me.BPMN, 'semantic:'+type);
    dataType.id = type+'_'+_uid();

    // ensure have in/outputSet (in theory both or neither but you never know)
    var inputSet = bpmn.querySelector('#'+obj.id+' ioSpecification>inputSet');
    if (inputSet == undefined) {
      inputSet = bpmn.createElementNS(me.BPMN, 'semantic:inputSet');
      inputSet.id = 'inputSet_'+_uid();
      ioSpec.appendChild(inputSet);
    }
    var outputSet = bpmn.querySelector('#'+obj.id+' ioSpecification>outputSet');
    if (outputSet == undefined) {
      outputSet = bpmn.createElementNS(me.BPMN, 'semantic:outputSet');
      outputSet.id = 'outputSet_'+_uid();
      ioSpec.appendChild(outputSet);
    }
    // add data type to in/outputSet
    var dataSet = (type == 'dataInput' ? inputSet : outputSet);
    var dataRef = bpmn.createElementNS(me.BPMN, 'semantic:'+type+'Refs');
    dataRef.innerHTML = dataType.id;
    dataSet.appendChild(dataRef);

    // add data in/output
    if (type == 'dataInput') {
      var dataInputs = bpmn.querySelectorAll('#'+obj.id+' ioSpecification>dataInput');
      var dataOutputs = bpmn.querySelectorAll('#'+obj.id+' ioSpecification>dataOutput');
      if (dataInputs != undefined && dataInputs.length > 0) {
        dataInputs[dataInputs.length-1].after(dataType);
      } else if (dataOutputs != undefined && dataOutputs.length > 0) {
        dataOutputs[0].before(dataType);
      } else {
        inputSet.before(dataType);
      }
    } else {
      inputSet.before(dataType);
    }

    // create data association
    var assoc = bpmn.createElementNS(me.BPMN, 'semantic:'+type+'Association');
    assoc.id = _uid();
    var assocSrc = bpmn.createElementNS(me.BPMN, 'semantic:sourceRef');
    assocSrc.innerHTML = (type == 'dataInput' ? src.id : dataType.id);
    assoc.appendChild(assocSrc);
    var assocTrgt = bpmn.createElementNS(me.BPMN, 'semantic:targetRef');
    assocTrgt.innerHTML =(type == 'dataInput' ? dataType.id : trgt.id);
    assoc.appendChild(assocTrgt);

    // append data in/output assoc before first existing of same type
    added = false;
    for (idx in obj.children) {
      if (obj.children[idx].localName == type+'Association') {
        obj.children[idx].before(assoc);
        added = true;
        break;
      }
    }
    if (!added && type == 'dataInput') ioSpec.after(assoc);
    else if (!added) obj.appendChild(assoc);

    var trgtDI = bpmn.querySelector('BPMNShape[bpmnElement="'+trgt.id+'"]');
    var flowDI = _newBpmnEdge(bpmn, assoc.id,
        bpmn.querySelector('BPMNShape[bpmnElement="'+src.id+'"]'), trgtDI, type);
    trgtDI.after(flowDI);

    return bpmn;
  }

  me.addFlowLink = function(bpmn, ids, type) {
    var src = bpmn.querySelector('#'+ids[0]);
    var trgt = bpmn.querySelector('#'+ids[1]);

    var flowObj = _newBpmnFlowObject(bpmn, type, src.id, trgt.id);
    switch (type) {
    case 'messageFlow':
      var collab = bpmn.querySelector('collaboration');
      var msgObj = _newBpmnObject(bpmn, 'message');
      collab.after(msgObj);
      flowObj.setAttribute('messageRef', msgObj.id);
      collab.append(flowObj);
      break;
    default:
      trgt.after(flowObj);
    }

    var trgtDI = bpmn.querySelector('BPMNShape[bpmnElement="'+trgt.id+'"]');
    var flowDI = _newBpmnEdge(bpmn, flowObj.id,
        bpmn.querySelector('BPMNShape[bpmnElement="'+src.id+'"]'), trgtDI, type);
    trgtDI.after(flowDI);

    return bpmn;
  }

  me.addProcessObject = function(bpmn, type, upstreamId, options) {
    console.info('addProcessObject...');

    // add new object
    var obj;
    var upstream = bpmn.querySelector('#'+upstreamId); // beware may be null
    var lane;
    switch (true) {
    case (type == 'lane' && upstream.localName == 'participant' && 'split' != options):
      obj = _newBpmnObject(bpmn, 'process', '');
      var procs = bpmn.querySelectorAll('process');
      procs[procs.length-1].after(obj);

      lane = obj.firstElementChild.firstElementChild;
      break;
    case (type != 'lane' && upstream.localName == 'lane'):
      obj = _newBpmnObject(bpmn, type, '');
      upstream = _findProcess(bpmn, upstream.id);
      upstream.append(obj);
      break;
    default:
      obj = _newBpmnObject(bpmn, type, '');
      upstream.after(obj);
    }

    if (_isVisual(type)) {
      if (type == 'lane' && upstream.localName != 'participant') {
        var newProc = _newBpmnObject(bpmn, 'process');
        var existingProcs = bpmn.querySelectorAll('process');
        existingProcs[existingProcs.length-1].after(newProc);
      }

      // find upstream object di
      var upstreamDI = bpmn.querySelector('BPMNShape[bpmnElement="'+upstreamId+'"]');

      if (lane != undefined) {
      // already set on object creation
      } else if (_isLane(bpmn, upstreamId)) {
        lane = bpmn.getElementById(upstreamId);
      } else {
        // add to same lane as upstream
        lane = _findLane(bpmn, upstreamId);
      }
      if (_isContainedInLane(bpmn, obj)) {
        var flowNodeRef = _newBpmnObject(bpmn, 'flowNodeRef');
        flowNodeRef.innerHTML = obj.id;
        lane.appendChild(flowNodeRef);
      }

      // add new object di
      var x = parseInt(upstreamDI.querySelector('Bounds').getAttribute('x'));
      var y = parseInt(upstreamDI.querySelector('Bounds').getAttribute('y'));
      var height = parseInt(upstreamDI.querySelector('Bounds').getAttribute('height'));
      var width = parseInt(upstreamDI.querySelector('Bounds').getAttribute('width'));
      switch (type) {
      case 'boundaryEvent':
        var di = _newBpmnShape(bpmn, type, obj.id,
            x+3*parseInt(upstreamDI.firstElementChild.getAttribute('width'))/4,
            y+height-(_getDefaultWidth(type)/2));
        upstreamDI.after(di);
        break;
      case 'dataInput':
        var di = _newBpmnShape(bpmn, type, obj.id,
            x+parseInt(upstreamDI.firstElementChild.getAttribute('width'))/4-(_getDefaultWidth(type)/2),
            y+height+GAP);
        upstreamDI.after(di);
        break;
      case 'dataOutput':
      case 'dataObjectReference':
      case 'dataStoreReference':
        var di = _newBpmnShape(bpmn, type, obj.id,
            x+3*parseInt(upstreamDI.firstElementChild.getAttribute('width'))/4-(_getDefaultWidth(type)/2),
            y+height+GAP);
        upstreamDI.after(di);
        break;
      case (type == 'lane' && upstream.localName == 'participant'):
        // 1. upstream is pool and option = split
        // 2. upstream is pool and option != split

        if (options == undefined) {
          var di = _newBpmnShape(bpmn, type, obj.id,x+LANE_X_OFFSET,y+LANE_Y_OFFSET);
        } else { // split existing lane
          upstreamDI.firstElementChild.setAttribute('height', height/2);
          var di = _newBpmnShape(bpmn, type, obj.id, x, y+(height/2));
          di.firstElementChild.setAttribute('height', height/2);
        }
        upstreamDI.after(di);
        break;
      case 'lane': // upstream is NOT pool
        // 3. upstream is NOT pool and option = split
        // 4. upstream is NOT pool and option != split

        switch (options) {
        case 'split':
          // split existing lane
        upstreamDI.firstElementChild.setAttribute('height', height/2);
        var di = _newBpmnShape(bpmn, type, obj.id, x, y+(height/2));
          di.firstElementChild.setAttribute('height', height/2);
          break;
        case 'new-process':
          // stand alone lane (aka process)
          var di = _newBpmnShape(bpmn, 'lane', lane.id, x, y+height+GAP);
            di.firstElementChild.setAttribute('height', DEFAULT_LANE_HEIGHT);
            di.firstElementChild.setAttribute('width', width);
            break;
      default:
            var laneDI = bpmn.querySelector('BPMNShape[bpmnElement="'+lane.id+'"]');
            var laneBounds = laneDI.firstElementChild;
            var di = _newBpmnShape(bpmn, type, obj.id,0,
                parseInt(laneBounds.getAttribute('y'))+parseInt(laneBounds.getAttribute('height'))+GAP);
        }
        upstreamDI.after(di);
        break;
      case 'participant':
        var di = _newBpmnShape(bpmn, type, obj.id,x,y+height+GAP);
        upstreamDI.after(di);
        break;
      case 'subProcess':
        if (upstream.localName == 'participant' || upstream.localName == 'process') {
            var dx = GAP;
            var di = _newBpmnShape(bpmn, type, obj.id, x+dx, y+(height/2)-(_getDefaultHeight(type)/2));
            di.setAttribute('isExpanded', 'true');
            upstreamDI.after(di);
        } else {
            var dx = width+GAP;
            var di = _newBpmnShape(bpmn, type, obj.id, x+dx, y+(height/2)-(_getDefaultHeight(type)/2));
            di.setAttribute('isExpanded', 'true');
            upstreamDI.after(di);
        }
        break;
      default:
        if (upstream.localName == 'lane' || upstream.localName == 'participant' || upstream.localName == 'process' || upstream.localName == 'subProcess') {
          var dx = GAP;
          var di = _newBpmnShape(bpmn, type, obj.id, x+GAP, y+(height/2)-(_getDefaultHeight(type)/2));
        } else {
          var dx = width+GAP;
          var di = _newBpmnShape(bpmn, type, obj.id, x+dx, y+(height/2)-(_getDefaultHeight(type)/2));
        }
        upstreamDI.after(di);
      }

      // join to upstream
      var flowType = 'sequenceFlow';
      switch (type) {
      case 'boundaryEvent':
        obj.setAttributeNode(_newBpmnAttr(bpmn, 'attachedToRef', upstreamId));
        break;
      case 'dataInput':
        me.addDataAssociation(bpmn, [ obj.id, upstreamId ]);
        break;
      case 'dataOutput':
      case 'dataObjectReference':
      case 'dataStoreReference':
        me.addDataAssociation(bpmn, [ upstreamId, obj.id ]);
        break;
      case 'lane':
      case 'participant':
        // do not link at all
        break;
      default:
        if (upstream.localName != 'lane' && upstream.localName != 'participant' && upstream.localName != 'process' && upstream.localName != 'subProcess') {
          me.addFlowLink(bpmn, [ upstreamId, obj.id ], 'sequenceFlow');
        }
      }

      _resizeSwimlanes(bpmn, obj, di);
    } else {
      // figure out where to place non-visual object
      switch (type) {
      case 'dataObject':
        bpmn.getElementById(upstreamId).parentElement.append(obj);
        break;
      default:
        var firstOfType = bpmn.querySelector('definitions '+type);
        if (firstOfType != undefined) firstOfType.prepend(obj);
        else bpmn.querySelector('definitions').prepend(obj);
      }
    }

    return { bpmn: bpmn, obj: obj };
  }

  me.addWaypoint = function(bpmn, selected, idx) {
    console.log('addWaypoint to '+selected+' after '+idx);
    var waypoints = bpmn.querySelectorAll('BPMNEdge[bpmnElement="'+selected+'"] waypoint');

    var x = parseInt(waypoints[0].getAttribute('x'));
    var y = parseInt(waypoints[0].getAttribute('y'));
    var x2 = parseInt(waypoints[waypoints.length-1].getAttribute('x'));
    var y2 = parseInt(waypoints[waypoints.length-1].getAttribute('y'))
    var newWaypoint = _newBpmnWaypoint(bpmn,
        Math.round(x + (x2 - x)/2),
        Math.round(y + (y2 - y)/2));
    waypoints[0].after(newWaypoint);
    return bpmn;
  }

  me.alignBpmn = function(bpmn, ids, strategy) {
    console.info('alignBpmn');

    var trgt = bpmn.querySelector('[id='+ids[0]+']');
    var trgtDI = bpmn.querySelector('BPMNShape[bpmnElement='+ids[0]+']');
    switch (strategy) {
    case 'center':
      var midX = Math.round(parseInt(trgtDI.firstElementChild.getAttribute('x'))+parseInt(trgtDI.firstElementChild.getAttribute('width'))/2);
      for (var i = 0 ; i < ids.length ; i++) {
        var di = bpmn.querySelector('BPMNShape[bpmnElement="'+ids[i]+'"]');
        var x = midX-parseInt(di.firstElementChild.getAttribute('width'))/2;
        di.firstElementChild.setAttribute('x', x);

        try {
          var labelDI = bpmn.querySelector('BPMNShape[bpmnElement="'+ids[i]+'"] BPMNLabel');
          labelDI.firstElementChild.setAttribute('x', x+TASK_LABEL_OFFSET);
        } catch (e) {
          console.warn('unable to move label of '+ids[i]+', probably uses default positioning');
        }

        var flows = bpmn.querySelectorAll('[sourceRef="'+ids[i]+'"]');
        for (var j = 0 ; j < flows.length ; j++) {
          try {
            // deliberately target only the first waypoint
            var waypoint = bpmn.querySelector('BPMNEdge[bpmnElement="'+flows[j].id+'"] waypoint');
            waypoint.setAttribute('x', midX);
          } catch (e) {
            console.warn('unable to move waypoint of '+flows[j].id+', probably uses default positioning');
          }
        }
        flows = bpmn.querySelectorAll('[targetRef="'+ids[i]+'"]');
        for (var j = 0 ; j < flows.length ; j++) {
          var flowDIs = bpmn.querySelectorAll('BPMNEdge[bpmnElement="'+flows[j].id+'"] waypoint');
          try {
            var flowDI = flowDIs[flowDIs.length-1];
            flowDI.setAttribute('x', midX);
          } catch (e) {
            console.warn('unable to move waypoint of '+flows[j].id+', probably uses default positioning');
          }
        }
      }
      break;
    default: /* middle */
      var midY = Math.round(parseInt(trgtDI.firstElementChild.getAttribute('y'))+parseInt(trgtDI.firstElementChild.getAttribute('height'))/2);
      for (var i = 0 ; i < ids.length ; i++) {
        var obj = bpmn.querySelector('[id='+ids[i]+']');
        var di = bpmn.querySelector('BPMNShape[bpmnElement="'+ids[i]+'"]');
        var y = midY-parseInt(di.firstElementChild.getAttribute('height'))/2;
        di.firstElementChild.setAttribute('y', y);

        try {
          var labelDI = bpmn.querySelector('BPMNShape[bpmnElement="'+ids[i]+'"] BPMNLabel');
          switch (true) {
          case (obj.localName.indexOf('Task')!=-1):
            labelDI.firstElementChild.setAttribute('x', parseInt(di.firstElementChild.getAttribute('x'))+TASK_LABEL_OFFSET);
            labelDI.firstElementChild.setAttribute('y', y+TASK_LABEL_OFFSET);
            break;
          default:
            labelDI.firstElementChild.setAttribute('y',
                midY+parseInt(di.firstElementChild.getAttribute('height'))/2);
            break;
          }
        } catch (e) {
          console.warn('unable to move label of '+ids[i]+', probably uses default positioning');
        }

        var flows = bpmn.querySelectorAll('[sourceRef="'+ids[i]+'"]');
        for (var j = 0 ; j < flows.length ; j++) {
          // deliberately target only the first waypoint
          try {
            var flowDI = bpmn.querySelector('BPMNEdge[bpmnElement="'+flows[j].id+'"] waypoint');
            flowDI.setAttribute('y', midY);
          } catch (e) {
            console.warn('unable to move waypoint of '+flows[j].id+', probably uses default positioning');
          }
        }
        flows = bpmn.querySelectorAll('[targetRef="'+ids[i]+'"]');
        for (var j = 0 ; j < flows.length ; j++) {
          var flowDIs = bpmn.querySelectorAll('BPMNEdge[bpmnElement="'+flows[j].id+'"] waypoint');
          try {
            var flowDI = flowDIs[flowDIs.length-1];
            flowDI.setAttribute('y', midY);
          } catch (e) {
            console.warn('unable to move waypoint of '+flows[j].id+', probably uses default positioning');
          }
        }
      }
    }

    return bpmn;
  }

  _changeObjectType = function(doc, obj, newType) {
    var newObj = doc.createElementNS(me.BPMN, 'semantic:'+newType);
    newObj.children.childNodes = obj.childNodes;
    for (i = 0 ; i < obj.getAttributeNames().length ; i++) {
      newObj.setAttributeNode(obj.getAttributeNode(obj.getAttributeNames()[i]).cloneNode());
    }
    obj.after(newObj);
    obj.remove();
    return doc;
  }

  me.deleteCondition = function(bpmn, obj) {
    obj.querySelector('conditionExpression').remove();
    return bpmn;
  }

  me.deleteObjects = function(bpmn, ids) {
    for (var i = 0 ; i < ids.length ; i++) {
      bpmn.querySelector('#'+ids[i]).remove;
      var objs = bpmn.querySelectorAll('[sourceRef="'+ids[i]+'"]');
      for (var j = 0 ; j < objs.length ; j++) {
        bpmn.querySelector('[bpmnElement="'+objs[j].id+'"]').remove();
        objs[j].remove();
      }
      objs = bpmn.querySelectorAll('[targetRef="'+ids[i]+'"]');
      for (var j = 0 ; j < objs.length ; j++) {
        bpmn.querySelector('[bpmnElement="'+objs[j].id+'"]').remove();
        objs[j].remove();
      }
      bpmn.querySelector('[bpmnElement="'+ids[i]+'"]').remove();
      var type = bpmn.querySelector('#'+ids[i]).localName;
      switch (type) {
      case 'dataObjectReference':
        // TODO find dataInputAssociation, dataOutputAssociatio, inputSet,
        // outputSet, dataInput, dataOutput and remove carefully!
      }
    }

    return bpmn;
  }

  me.deleteWaypoint = function(bpmn, id, idx) {
    bpmn.querySelector('[bpmnElement="'+id+'"]').children[idx].remove();

    return bpmn;
  }

  _findLane = function(doc, id) {
    var refs = doc.querySelectorAll('lane>flowNodeRef');
    for(idx in refs) {
      if (id==refs[idx].innerHTML){
        console.log('HIT: found lane for '+id+'='+refs[idx].parentElement);
        return refs[idx].parentElement;
      }
    }
    console.warn('MISS: '+id+' is not in any lane');
  }

  _findProcess = function(doc, id) {
    var procs = doc.querySelectorAll('process');
    for(idx in procs) {
      var lane = procs[idx].querySelector('lane[id="'+id+'"]');
      if (lane != undefined) {
        console.log('HIT: found proc for '+id+'='+procs[idx]);
        return procs[idx];
      }
    }
    console.warn('MISS: '+id+' is not in any proc');
  }

  _getDefaultHeight = function(type) {
    switch (type) {
    case 'dataInput': case 'dataObject': case 'dataObjectReference': case 'dataOutput':
      return DEFAULT_DATUM_HEIGHT;
    case 'boundaryEvent': case 'endEvent': case 'intermediateEvent': case 'startEvent':
      return DEFAULT_EVENT_HEIGHT;
    case 'complexGateway': case 'eventBasedGateway': case 'exclusiveGateway': case 'inclusiveGateway': case 'parallelGateway':
      return DEFAULT_GATEWAY_HEIGHT;
    case 'lane':
      return DEFAULT_LANE_HEIGHT;
    case 'participant':
      return DEFAULT_POOL_HEIGHT;
    case 'subProcess':
      return DEFAULT_SUBPROCESS_HEIGHT;
    default:
      return DEFAULT_TASK_HEIGHT;
    }
  }

  _getDefaultWidth = function(type) {
    switch (type) {
    case 'dataInput': case 'dataObject': case 'dataObjectReference': case 'dataOutput':
      return DEFAULT_DATUM_WIDTH;
    case 'boundaryEvent': case 'endEvent': case 'intermediateEvent': case 'startEvent':
      return DEFAULT_EVENT_WIDTH;
    case 'complexGateway': case 'eventBasedGateway': case 'exclusiveGateway': case 'inclusiveGateway': case 'parallelGateway':
      return DEFAULT_GATEWAY_WIDTH;
    case 'lane':
      return DEFAULT_LANE_WIDTH;
    case 'participant':
      return DEFAULT_POOL_WIDTH;
    case 'subProcess':
      return DEFAULT_SUBPROCESS_WIDTH;
    default:
      return DEFAULT_TASK_WIDTH;
    }
  }

  _isContainedInLane = function(doc, obj) {
    switch(obj.localName) {
    case 'process':
      return false;
    default:
      return _findLane(doc, obj.id) != undefined;
    }
  }

  _isLane = function(bpmn, id) {
    return bpmn.querySelector('lane[id="'+id+'"]') != undefined;
  }

  _isVisual = function(type) {
    switch (type) {
    case 'dataObject':
    case 'dataStore':
    case 'error':
    case 'escalation':
    case 'message':
    case 'resource':
    case 'signal':
      return false;
    default:
      return true;
    }
  }

  _joinHorizontally = function(doc,di,x,y,w,h,x2,y2,w2,h2) {
    di.append(_newBpmnWaypoint(doc, x+w, y+h/2));
    di.append(_newBpmnWaypoint(doc, x2, y2+h2/2));
  }

  _joinDataAssocHorizontally = function(doc,di,x,y,w,h,x2,y2,w2,h2) {
    var y3 = (y+h<y2+h2) ? y+h*.75 : y2+h2*.75;

    di.append(_newBpmnWaypoint(doc, x+w, y3));
    di.append(_newBpmnWaypoint(doc, x2, y3));
  }

  _newBpmnAttr = function(doc, name, value) {
    var attr = doc.createAttribute(name);
    attr.value = value;
    return attr;
  }

  _newBpmnAttrNS = function(doc, ns, name, value) {
    var attr = doc.createAttributeNS(ns, name);
    attr.value = value;
    return attr;
  }

  _newBpmnEdge = function(doc, id, srcDI, trgtDI, type) {
    var di = doc.createElementNS(me.BPMN_DI, "bpmndi:BPMNEdge");
    di.id = id+'_di';
    di.setAttributeNode(_newBpmnAttr(doc, 'bpmnElement', id));

    var x = parseInt(srcDI.firstElementChild.getAttribute('x'));
    var y = parseInt(srcDI.firstElementChild.getAttribute('y'));
    var w = parseInt(srcDI.firstElementChild.getAttribute('width'));
    var h = parseInt(srcDI.firstElementChild.getAttribute('height'));
    var x2 = parseInt(trgtDI.firstElementChild.getAttribute('x'));
    var y2 = parseInt(trgtDI.firstElementChild.getAttribute('y'));
    var w2 = parseInt(trgtDI.firstElementChild.getAttribute('width'));
    var h2 = parseInt(trgtDI.firstElementChild.getAttribute('height'));

    if (x < x2 && x+w < x2 && y2 < y && y2+h2 > y+h) {
      console.log('trgt right AND alongside src: join right src to left of trgt');
      if (type.indexOf('data')==0) _joinDataAssocHorizontally(doc,di,x,y,w,h,x2,y2,w2,h2);
      else _joinHorizontally(doc,di,x,y,w,h,x2,y2,w2,h2);
    } else if (x < x2 && x+w < x2 && y < y2 && y+h < y2) {
      console.log('trgt right AND below src: join right src to top of trgt');

      di.append(_newBpmnWaypoint(doc, x+w, y+h*(type.indexOf('dataOutput') == 0 ? 0.75 : .5)));
      di.append(_newBpmnWaypoint(doc, x2+w2/(type.indexOf('dataInput') == 0 ? 4 : 2),
          y+h*(type.indexOf('dataOutput') == 0 ? 0.75 : .5)));
      di.append(_newBpmnWaypoint(doc, x2+w2/(type.indexOf('dataInput') == 0 ? 4 : 2), y2));

    } else if (x < x2 && x+w < x2 && y > y2 && y+h > y2) {
      console.log('trgt right AND above src: join right of src to bottom of trgt');

      di.append(_newBpmnWaypoint(doc, x+w/2, y+h/(type.indexOf('dataOutput') == 0 ? 4 : 2)));
      di.append(_newBpmnWaypoint(doc, x2+w2/(type.indexOf('dataInput') == 0 ? 4 : 2), y+h/2));
      di.append(_newBpmnWaypoint(doc, x2+w2/(type.indexOf('dataInput') == 0 ? 4 : 2), y2+h2));

    } else if (y < y2 && y+h > y2+h2) {
      console.log('src left of trgt: join right of src to left of target');
      if (type.indexOf('data')==0) _joinDataAssocHorizontally(doc,di,x,y,w,h,x2,y2,w2,h2);
      else _joinHorizontally(doc,di,x,y,w,h,x2,y2,w2,h2);
    } else if (y > y2 && y+h < y2+h2) {
      console.log('src right of trgt: join left of src to right of target');
      if (type.indexOf('data')==0) _joinDataAssocHorizontally(doc,di,x2,y2,w2,h2,x,y,w,h);
      else _joinHorizontally(doc,di,x2,y2,w2,h2,x,y,w,h);
    } else if (y < y2) {
      console.log('src above trgt: join bottom of src to top of target');

      di.append(_newBpmnWaypoint(doc, x+w*(type.indexOf('dataOutput') == 0 ? 0.75 : 0.5), y+h));
      di.append(_newBpmnWaypoint(doc, x+w*(type.indexOf('dataOutput') == 0 ? 0.75 : 0.5), y+h+(y2-y-h)/2));
      di.append(_newBpmnWaypoint(doc, x2+w2/2, y2-(y2-y-h)/(type.indexOf('dataInput') == 0 ? 4 : 2)));
      di.append(_newBpmnWaypoint(doc, x2+w2/(type.indexOf('dataInput') == 0 ? 4 : 2), y2));

    } else if (y > y2) {
      console.log('src below trgt: join top of src to bottom of target');

      di.append(_newBpmnWaypoint(doc, x+w/2, y));
      di.append(_newBpmnWaypoint(doc, x+w/2, y-(y-y2-h2)/2));
      di.append(_newBpmnWaypoint(doc, x2+w2/(type.indexOf('dataInput') == 0 ? 4 : 2), y2+h2+(y-y2-h2)/2));
      di.append(_newBpmnWaypoint(doc, x2+w2/(type.indexOf('dataInput') == 0 ? 4 : 2), y2+h2));

    } else {
      console.log('join right of src to left of target');
      if (type.indexOf('data')==0) _joinDataAssocHorizontally(doc,di,x,y,w,h,x2,y2,w2,h2);
      else _joinHorizontally(doc,di,x,y,w,h,x2,y2,w2,h2);
    }

    di.append(_newDILabel(doc, type, x, y));
    return di;
  }

  _newBpmnFlowObject = function(doc, type, srcBpmnElId, trgtBpmnElId) {
    var obj = doc.createElementNS(me.BPMN, 'semantic:'+type);
    obj.id = srcBpmnElId+'_to_'+trgtBpmnElId+'_di';
    obj.setAttributeNode(_newBpmnAttr(doc, 'sourceRef', srcBpmnElId));
    obj.setAttributeNode(_newBpmnAttr(doc, 'targetRef', trgtBpmnElId));
    return obj;
  }

  _newBpmnObject = function(doc, type, name) {
    var obj = doc.createElementNS(me.BPMN, 'semantic:'+type);
    switch(type) {
    case 'flowNodeRef':
      // flowNodeRefs cannot have an id!
      break;
    case 'process':
      var laneSet = _newBpmnObject(doc, 'laneSet');
      obj.append(laneSet);
      var lane = _newBpmnObject(doc, 'lane', '');
      laneSet.append(lane);
      obj.id = type+_uid();
      break;
    case 'subProcess':
      obj.setAttribute('triggeredByEvent','false');
      obj.setAttribute('completionQuantity','1');
      obj.setAttribute('isForCompensation','false');
      obj.setAttribute('startQuantity','1');
      // no break;
    default:
      obj.id = type+_uid();
    }
    if (name!=undefined) obj.setAttributeNode(_newBpmnAttr(doc, 'name', name));
    return obj;
  }

  _newBpmnShape = function(doc, type, bpmnElId, x, y) {
    var di = doc.createElementNS(me.BPMN_DI, "bpmndi:BPMNShape");
    di.id = bpmnElId+'_di';
    di.setAttributeNode(_newBpmnAttr(doc, 'bpmnElement', bpmnElId));
    di.setAttributeNode(_newBpmnAttrNS(doc, me.COLOR, 'color:border-color', KP_BORDER));
    switch (type) {
    case 'endEvent':
      di.setAttributeNode(_newBpmnAttrNS(doc, me.COLOR, 'color:border-color', RED));
      break;
    case 'boundaryEvent': case 'intermediateEvent':
      break;
    case 'startEvent':
      di.setAttributeNode(_newBpmnAttrNS(doc, me.COLOR, 'color:border-color', GREEN));
      break;
    case 'exclusiveGateway': case 'complexGateway': case 'eventBasedGateway': case 'inclusiveGateway': case 'parallelGateway':
    case 'lane': case 'process': case 'subProcess':
      di.setAttributeNode(_newBpmnAttrNS(doc, me.COLOR, 'color:background-color', WHITE));
      break;
    default:
      di.setAttributeNode(_newBpmnAttrNS(doc, me.COLOR, 'color:background-color', KP_BG));
    }

    var diBounds = doc.createElementNS(me.DC , "dc:Bounds");
    diBounds.setAttributeNode(_newBpmnAttr(doc, 'x', x));
    diBounds.setAttributeNode(_newBpmnAttr(doc, 'y', y));
    switch (type) {
    case 'exclusiveGateway':
      di.setAttributeNode(_newBpmnAttr(doc, 'isMarkerVisible', true));
      // intentionally no break
    case 'boundaryEvent': case 'endEvent': case 'intermediateEvent': case 'startEvent':
    case 'complexGateway': case 'eventBasedGateway': case 'inclusiveGateway': case 'parallelGateway':
      // intentionally no break
    default:
      diBounds.setAttributeNode(_newBpmnAttr(doc, 'height', _getDefaultHeight(type)));
      diBounds.setAttributeNode(_newBpmnAttr(doc, 'width', _getDefaultWidth(type)));
    }
    di.append(diBounds);

    di.append(_newDILabel(doc, type, x, y));

    return di;
  }

  _newBpmnWaypoint = function(doc, x, y) {
    var diWaypoint = doc.createElementNS(me.DI, "di:waypoint");
    diWaypoint.setAttributeNode(_newBpmnAttr(doc, 'x', x));
    diWaypoint.setAttributeNode(_newBpmnAttr(doc, 'y', y));
    return diWaypoint;
  }

  _newDILabel = function(doc, type, x, y) {
    var diLabel = doc.createElementNS(me.BPMN_DI , "bpmndi:BPMNLabel");
    switch (type) {
    case 'complexGateway': case 'eventBasedGateway': case 'inclusiveGateway': case 'parallelGateway':
    case 'dataObjectReference': case 'dataStoreReference':
    case 'endEvent': case 'intermediateEvent': case 'startEvent':
    case 'exclusiveGateway':
    case 'messageFlow': case 'sequenceFlow':
    case 'participant': case 'lane': case 'subProcess':
      diLabel.setAttributeNode(_newBpmnAttrNS(doc, me.COLOR, 'color:color', KP_TEXT));
      break;
    default: // Tasks
      diLabel.setAttributeNode(_newBpmnAttrNS(doc, me.COLOR, 'color:color', KP_TASK_TEXT));
    }

    var diLabelBounds = doc.createElementNS(me.DC , "dc:Bounds");
    diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'x', x));
    diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'y', y));
    switch (type) {
    case 'dataObjectReference':
      // allow for state under single line label
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'height', 2*DEFAULT_FONT_SIZE));
      // intentionally no break;
    case 'complexGateway': case 'eventBasedGateway': case 'inclusiveGateway': case 'parallelGateway':
    case 'endEvent': case 'intermediateEvent': case 'startEvent':
    case 'exclusiveGateway':
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'x', x));
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'y', y+_getDefaultHeight(type)+LABEL_OFFSET));
      // TODO may need multi-line sometimes
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'height', DEFAULT_FONT_SIZE));
      // TODO this is a random guess at length!
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'width', Math.round(DEFAULT_FONT_SIZE*FONT_WIDTH_WEIGHTING*30)));
      break;
    case 'participant':
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'x', x));
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'y', y));
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'height', Math.round(DEFAULT_FONT_SIZE*FONT_WIDTH_WEIGHTING*30)));
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'width', LANE_LABEL_WIDTH));
      break;
    case 'lane':
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'x', x-(2*LABEL_OFFSET)));
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'y', y));
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'height', Math.round(DEFAULT_FONT_SIZE*FONT_WIDTH_WEIGHTING*30)));
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'width', LANE_LABEL_WIDTH));
      break;
    default: // Tasks, Call activities, Sub-Process etc
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'x', x+TASK_LABEL_OFFSET));
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'y', y+TASK_LABEL_OFFSET));
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'height', _getDefaultHeight(type)-TASK_LABEL_OFFSET));
      diLabelBounds.setAttributeNode(_newBpmnAttr(doc, 'width', _getDefaultWidth(type)-TASK_LABEL_OFFSET));
    }
    diLabel.append(diLabelBounds);
    return diLabel;
  }

  me.render = function(bpmn, diagId) {
    me.transformer.clearParameters();
    me.transformer.setParameter(me.BPMN, 'diagramId', diagId);
    me.transformer.setParameter(me.BPMN, 'showBounds', me.showBounds);
    me.transformer.setParameter(me.BPMN, 'showIssue', me.showIssues);
    me.transformer.setParameter(me.BPMN, 'debug', me.debug);
    var result = me.transformer.transformToDocument(bpmn);
    if (result == undefined) {
      ractive.showError("Unable to render image of BPMN");
    } else {
      return me.serializer.serializeToString(result.firstElementChild);
    }
  }

  me.resetIds = function(doc) {
    var defn = doc.querySelector('definitions');
    defn.id = _uid();
    var ns = KP_NS+defn.id;
    defn.setAttribute('targetNamespace', ns);
    defn.setAttribute('xmlns', ns);
    defn.setAttribute('exporter', $env['appName']);
    defn.setAttribute('exporterVersion', $env['appVsn']);

    var processes = doc.querySelectorAll('process');
    for (var i = 0 ; i < processes.length ; i++) {
      var uuid = _uid();
      var bpmnShapes = doc.querySelectorAll('[bpmnElement="'+processes[i].id+'"]');
      for (var j = 0 ; j < bpmnShapes.length ; j++) {
        bpmnShapes[j].setAttribute('bpmnElement', uuid);
      }
      var pool = doc.querySelector('participant[processRef="'+processes[i].id+'"]');
      if (pool != undefined) pool.setAttribute('processRef', uuid);
      processes[i].id = uuid;
    }
    return doc;
  }

  //resize pool & lanes if new object exceeds their width
  _resizeSwimlanes = function(doc, obj, di) {
    var lane = _findLane(doc, obj.id);
    if (lane != undefined) {
      var diWidth = parseInt(di.firstElementChild.getAttribute('x'))+parseInt(di.firstElementChild.getAttribute('width'));
      var procId = lane.parentElement.parentElement.id;
      try {
        var poolDI = doc.querySelector('BPMNShape[bpmnElement='+doc.querySelector('participant[processRef='+procId+']').id+']');
        if (poolDI != undefined && poolDI.firstElementChild.getAttribute('width') < diWidth) poolDI.firstElementChild.setAttribute('width', diWidth+GAP);
      } catch (e) {
        // Unusual but Lane MAY not have pool
      }
      var lanes = doc.querySelectorAll('process[id='+procId+'] lane');
      lanes.forEach(function(lane) {
        var laneDI = doc.querySelector('BPMNShape[bpmnElement='+lane.id+']');
        var laneWidth = parseInt(laneDI.firstElementChild.getAttribute('x'))+parseInt(laneDI.firstElementChild.getAttribute('width'));
        if (laneWidth < diWidth) laneDI.firstElementChild.setAttribute('width', diWidth+GAP);
      });
    }
  }

  _resizeTasks = function(doc, tasks) {
    for (idx in tasks) {
      var di = doc.querySelector('[bpmnElement="'+tasks[idx].id+'"]');
      if (di!=undefined && di.firstElementChild.localName == 'Bounds') {
        var diBounds = di.firstElementChild;
        diBounds.setAttributeNode(_newBpmnAttr(doc, 'height', _getDefaultHeight('task')));
        diBounds.setAttributeNode(_newBpmnAttr(doc, 'width', _getDefaultWidth('task')));
      } else {
        console.warn('Unable to resize user task with id: '+tasks[idx].id);
      }
    }
    return doc;
  }

  me.tidy = function(bpmn) {
    bpmn = _resizeTasks(bpmn, bpmn.querySelectorAll('userTask'));
    bpmn = _resizeSwimlanes(bpmn, )
//    bpmn = ractive.modeler.resetIds(bpmn);
    return bpmn;
  }

  me.reversePoolOrder = function(bpmn) {
    var poolIds = Array.prototype.map.call(bpmn.querySelectorAll('participant'), function(obj) {
      return obj.id;
    }).reverse();

    var currentY = 0;
    for(idx = 0 ; idx < poolIds.length ; idx++) {
      var poolDI = bpmn.querySelector('BPMNShape[bpmnElement="'+poolIds[idx]+'"]');
      me.updateObject(bpmn, poolDI.firstElementChild, 'y', currentY);
      currentY += parseInt(poolDI.firstElementChild.getAttribute('height'));
      currentY += GAP;
    }
    return bpmn;
  }

  me.updateCondition = function(bpmn, obj, value) {
    var cond = bpmn.querySelector('#'+obj.id+' conditionExpression');
    if (cond == undefined) {
      cond = _newBpmnObject(bpmn, 'conditionExpression');
      obj.appendChild(cond);
    }
    cond.firstChild.remove();
    cond.append(bpmn.createTextNode(value));
    return bpmn;
  }

  me.updateObject = function(bpmn, objOrId, key, value, isThrow) {
    if (typeof value == 'string') {
      value = value.trim().replace(/<br>/, '\n').replace(/&lt;br&gt;/, '\n');
    }
    switch(true) {
    case (key == 'dataObjectRef'):
      bpmn.querySelector('#'+objOrId).setAttribute(key, value);
      bpmn.querySelector('#'+objOrId).setAttribute('name', bpmn.querySelector('#'+value).getAttribute('name'));
      break;
    case (key == 'dataState'):
      try {
        bpmn.querySelector('#'+objOrId+' dataState').setAttribute('name', value);
      } catch (e) {
        // Assume no data state present
        var obj = bpmn.querySelector('#'+objOrId);
        var stateObj = _newBpmnObject(bpmn, 'dataState', '');
        stateObj.setAttribute('name', value);
        obj.appendChild(stateObj);
      }
      break;
    case (key == 'serviceType'):
      objOrId.setAttributeNS(this.ACTIVITI, 'activiti:class', value);
      break;
    case (key == 'subType'):
      switch (value) {
      case 'multiple':
        objOrId.setAttribute('parallelMultiple', 'false');
        break;
      case 'parallelMultiple':
        objOrId.setAttribute('parallelMultiple', 'true');
        break;
      case 'participant':
        var existingVal = parseInt(objOrId.getAttribute(key));
        objOrId.setAttribute(key, value);

        // maintain offset of contained lanes (if exist)
        var laneIds = Array.prototype.map.call(bpmn.querySelectorAll('lane'), function(obj) { return obj.id });

        // resize contained lanes
        if (key == 'y' && existingVal < parseInt(value)) {
          // reduce lane size?
        } else {
          // expand last lane, if exists?
        }
        break;
      case 'conditional':
      case 'error':
      case 'escalation':
      case 'message':
      case 'signal':
      case 'timer':
      case 'terminate':
        var response = me.addProcessObject(bpmn, value);
        bpmn = response.bpmn;
        var evtDef = _newBpmnObject(bpmn, value+'EventDefinition');
        evtDef.setAttribute(value+'Ref', response.obj.id);
        objOrId.append(evtDef);
        break;
      default:
        ; // none event
      }
      break;
    case (key == 'targetNamespace'):
      bpmn.documentElement.setAttribute(key, value);
      bpmn.documentElement.setAttribute('xmlns', value);
      break;
    case (key == 'type'):
      bpmn = _changeObjectType(bpmn, objOrId, value);
      break;
    case (typeof objOrId == 'object' && objOrId.localName == 'Bounds' && key == 'x'):
    case (typeof objOrId == 'object' && objOrId.localName == 'Bounds' && key == 'y'):
      var diff = parseInt(value)-parseInt(objOrId.getAttribute(key));
      objOrId.setAttribute(key, value);
      // move label bounds too
      var diLabel = objOrId.nextElementSibling;
      diLabel.firstElementChild.setAttribute(key, parseInt(diLabel.firstElementChild.getAttribute(key))+diff);
      // find attached flows
      var flows = bpmn.querySelectorAll('[sourceRef="'+objOrId.parentElement.getAttribute('bpmnElement')+'"]');
      for (var j = 0 ; j < flows.length ; j++) {
        try {
          // deliberately target only the first waypoint
          var flowDI = bpmn.querySelector('BPMNEdge[bpmnElement="'+flows[j].id+'"] waypoint');
          flowDI.setAttribute(key, parseInt(flowDI.getAttribute(key))+diff);
        } catch (e) {
          console.warn('unable to move waypoint of '+flows[j].id+', probably uses default positioning');
        }
      }
      flows = bpmn.querySelectorAll('[targetRef="'+objOrId.parentElement.getAttribute('bpmnElement')+'"]');
      for (var j = 0 ; j < flows.length ; j++) {
        try {
          var waypointsDI = bpmn.querySelectorAll('BPMNEdge[bpmnElement="'+flows[j].id+'"] waypoint');
          var waypointDI = waypointsDI[waypointsDI.length-1];
          waypointDI.setAttribute(key, parseInt(waypointDI.getAttribute(key))+diff);
        } catch (e) {
          console.warn('unable to move waypoint of '+flows[j].id+', probably uses default positioning');
        }
      }
      break;
    case (typeof objOrId == 'string'):
      bpmn.querySelector('#'+objOrId).setAttribute(key, value);
      break;
    default:
      objOrId.setAttribute(key, value);
    }

    return bpmn;
  }

  me.updateObjectType = function(bpmn, id, newType) {

  }
  /**
   * Generate shorter identifiers, should be unique up to 10k generations.
   * @see https://gist.github.com/gordonbrander/2230317
   */
  _uid = function() {
    return '_' + Math.random().toString(36).substr(2, 9);
  }
  _uuid = function() {
    var d = new Date().getTime();
    if(window.performance && typeof window.performance.now === "function"){
        d += performance.now(); //use high-precision timer if available
    }
    var uuid = '_xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
    });
    return uuid;
  }

  return me;
}
