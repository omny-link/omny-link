var dc = new DomainController();

jQuery(document).ready(function() {
  window.$ = jQuery;
  dc.init('http://localhost/wp-content/plugins/trunk/js/fg-domain.json');
});

function DomainController() {
  this.domainFields = [];
  this.formFields = [];
  this.init = function(url) {
    $.ajax({
      type: 'GET',
      url: url,
      success: function(response) {
        console.log('Received domain model containing '+response.length+' entities.');
        dc.domain = response;
        dc.renderDomain();
      },
      error: function(jqXHR, textStatus, errorThrown) {
        console.log('error:'+textStatus+':'+errorThrown);
        console.log('  headers: '+JSON.stringify(jqXHR.getAllResponseHeaders()));
        //$('html, body').css("cursor", "auto");
      }
    });
  }
  this.dragHelper = function( event ) {
    return '<div class="draggableHelper">Drag onto the form to bind a '+event.target.id+' control</div>';
  }
  this.handleDragStop = function( event, ui ) {
    var offsetXPos = parseInt( ui.offset.left );
    var offsetYPos = parseInt( ui.offset.top );
    console.log( "Drag stopped!\n\nOffset: (" + offsetXPos + ", " + offsetYPos + ")\n");
  }
  this.handleDropEvent = function( event, ui ) {
    var draggable = ui.draggable;
    console.log( 'The model element with ID "' + draggable.attr('id') + '" was dropped onto me!' );
    dc.formFields.push(draggable.attr('id'));
    dc.renderForm();
  }
  this.renderDomain = function() {
    $('#domainPalette').empty().append('<ul class="entities">');
    $.each(dc.domain, function(i,d) {
      $('#domainPalette .entities').append('<li class="draggable entity" id="'+$(d).attr('id')+'">'+$(d).attr('name')+'</li>');
      $('#domainPalette .entities').append('<ul class="'+$(d).attr('id')+' fields">');
      $.each(d.fields, function(j,e) {
        var id = $(d).attr('id')+'.'+$(e).attr('id');
        console.log('Adding to domain palette: '+id);
        dc.domainFields[id] = e;
        $('#domainPalette .'+$(d).attr('id')+'.fields').append('<li class="draggable field" id="'+id+'">'+$(e).attr('name')+'</li>');
      });
    });
    $('.draggable').draggable( {
      containment: 'body',
      cursor: 'move',
      helper: dc.dragHelper,
      snap: '#formCanvas',
      stop: dc.handleDragStop
    } );
    $('.droppable').droppable( {
      drop: dc.handleDropEvent
    } );
  }
  this.renderField = function(id) {
    var field = dc.domainFields[id];
    var hint = field.description === undefined ? '' : field.description;
    switch (field.type) {
    case 'email':
      var ctrl = '<input class="decorate" data-p-bind="$p.'+id+'" id="'+field.id+'" name="'+field.name
          +'" placeholder="'+(field.placeholder === undefined ? '' : field.placeholder)
          +'" '+(field.required === true ? 'required ' : '')
          +' title="'+hint+'" type="email"/>';
      break;
    case 'number':
      var ctrl = '<input class="decorate" data-p-bind="$p.'+id+'" id="'+field.id+'" name="'+field.name
          +'" placeholder="'+(field.placeholder === undefined ? '' : field.placeholder)
          +'" '+(field.required === true ? 'required ' : '')
          +'" title="'+hint+'" type="number"/>';
      break;
    case 'text':
      var ctrl = '<textarea class="decorate" data-p-bind="$p.'+id+'" id="'+field.id+'" name="'+field.name
          +'" placeholder="'+(field.placeholder === undefined ? '' : field.placeholder)
          +'" '+(field.required === true ? 'required ' : '')
          +'" rows="3" title="'+hint+'"></textarea>';
      break;
    case 'string':
    default:
      var ctrl = '<input class="decorate" data-p-bind="$p.'+id+'" id="'+field.id+'" name="'+field.name+'" placeholder="'+(field.placeholder === undefined ? '' : field.placeholder)+'" title="'+hint+'"/>';
    }
    return ctrl;
  }
  this.renderForm = function() {
    $('#formCanvas').empty();
    $.each(dc.formFields, function(i,d) {
      $('#formCanvas').append(dc.renderField(d));
    })
    $('#formCanvas').append('<button class="btn" id="submit">Submit</button>');
    $p.bindControls();
  }
}
