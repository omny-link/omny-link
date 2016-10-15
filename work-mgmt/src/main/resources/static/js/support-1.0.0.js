
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