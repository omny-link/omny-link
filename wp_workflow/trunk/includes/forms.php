<?php

function my_the_content_filter($content) {
  $scode_pos = strpos($content, '[contact-form-7');
  if ($scode_pos > -1) {
    error_log('YYYYYYYYYYY have found shortcode to replace');
    $id_pos = strpos($content, 'id=',$scode_pos); 
    $id_len = strpos($content, ' ', $id_pos) - ($id_pos+4);
    $id = substr($content, $id_pos+4, $id_len-1);
    error_log('found id: '.$id);

$content_post = get_post($id);
$form = $content_post->post_content;
$form = apply_filters('the_content', $form );
$form = str_replace(']]>', ']]&gt;', $form );

    //$tmp = str_replace('[contact-form-7', '[p-page', $content);
    //$form = '<form>'
    //  .'</form>';
    $tmp = preg_replace('/(\[)contact-form-7(.*)]/', $form, $content);
    return $tmp;
  } else { 
    error_log(strpos($content, '[contact-form-7').'   XXXXXXXXXXXXXXXXXXhave NOT found shortcode to replace');
error_log($content);
  } 
  // otherwise returns the database content
  return $content;
}

add_filter( 'the_content', 'my_the_content_filter' );

?>
