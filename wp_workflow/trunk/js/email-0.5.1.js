        function sendMail() {
          $('.p-messages').empty().append('Sending...').show();
          $p.sendMessage('inOnly','com.knowprocess.mail.MailData.json',JSON.stringify($p.mail));
        }
        $(document).ready(function() {
          console.debug('Document ready handler...');
          loadTemplates();
        });
        function loadTemplates() {
          $.getJSON(
              "/wp-content/plugins/syncapt/emails/templates.php", 
              function(templates) {
                console.debug("Have "+templates.length+" templates: "+templates);
                $('#templates-ctl').empty();
                for (idx in templates) { 
                  $('#templates-ctl').append('<option name="'+templates[idx]+'">'+templates[idx]+'</option>');
                }
              });
        };

