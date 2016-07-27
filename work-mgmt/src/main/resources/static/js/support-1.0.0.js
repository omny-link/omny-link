
var hd = new HelpDesk();

function HelpDesk() {
  this.submit = function(obj) {
    var c = document.getElementById('hdScreenshot');
    var canvasData = c.toDataURL("image/png");
//    c.toBlob(function(blob) {
//      console.log('have blob, submitting...');
      $.ajax({
        url: ractive.getServer()+'/omny/helpdesk',
        type: 'POST',
        contentType: 'application/upload',
        data: JSON.stringify({ image:canvasData, entity: obj }),
        success: completeHandler = function(data,textStatus,jqXHR) {
          console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
          ractive.showMessage('Submitted screenshot');
        },
      });
//    });
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
      
      hd.submit(obj);
    });
  }
}