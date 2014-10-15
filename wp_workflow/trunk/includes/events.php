<?php

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
    
    $msg_namespace = get_option(P_MSG_NAMESPACE, P_ID);
error_log('msg ns: '.$msg_namespace);
    $msg_name = ''.$msg_namespace.'.postPublicationEvent.json';
    $json = '{"title":"'.$title.'","permalink":"'.$permalink.'"}';
    if (P_DEBUG) { 
      error_log('Notifying Syncapt: ');
      error_log('  Message name: '.$msg_name);
      error_log('  JSON: '.$json);
    }
    
  }
  add_action( 'publish_post', 'p_post_published_notification', 10, 2 );
?>
