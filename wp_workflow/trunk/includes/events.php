<?php

  // TODO need to have seetings page for this
  //define("P_MSG_NAMESPACE", P_ID.'-message-namespace');
  //define("P_MSG_NAMESPACE", $syncapt_options->get_message_namespace());
//  define("P_API_URL", 'http://api.knowprocess.com/msg/');
  if ($syncapt_options == null) $syncapt_options = new SyncaptOptions();
  define("P_API_URL", $syncapt_options->get_api_url());


  function p_post_published_notification( $ID, $post ) {
    if (P_DEBUG) error_log('Call to p_post_published_notification');
    $author = $post->post_author; /* Post author ID. */
    $name = get_the_author_meta( 'display_name', $author );
    $email = get_the_author_meta( 'user_email', $author );
    $title = $post->post_title;
    $permalink = get_permalink( $ID );
    $edit = get_edit_post_link( $ID, '' );
    $to[] = sprintf( '%s <%s>', $name, $email );
    $subject = sprintf( 'Published: %s', $title );
    $message = sprintf ('Congratulations, %s! Your article “%s” has been published.' . "\n\n", $name, $title );
    $message .= sprintf( 'View: %s', $permalink );
    $headers[] = '';
    //wp_mail( $to, $subject, $message, $headers );

    if ($syncapt_options == null) $syncapt_options = new SyncaptOptions();
    $msg_namespace = $syncapt_options->get_message_namespace();
error_log('msg ns: '.$msg_namespace);
    $msg_name = ''.$msg_namespace.'.postPublicationEvent.json';
    $json = '{"title":"'.$title.'","permalink":"'.$permalink.'"}';
    if (P_DEBUG) {
      error_log('Notifying Syncapt: ');
      error_log('  Message name: '.$msg_name);
      error_log('  JSON: '.$json);
    }
    $fields = array(
      'json' => $json
    );
    //$response = http_post_fields(P_API_URL.$msg_name, array('timeout'=>1), $fields);
    $url = P_API_URL.$msg_name;
    $ch = curl_init($url);

    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $response = curl_exec($ch);
    if (P_DEBUG) error_log('XXXXXXXXXXXXXXXResponse: '.$response);
    $http_status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    if (P_DEBUG || $http_status >=300) error_log('Response from '.$url.': '.$http_status);
    curl_close($ch);
  }
  add_action( 'publish_post', 'p_post_published_notification', 10, 2 );
?>
