<?php

  // shortcode [p_page template="template-name"]
  function p_page_shortcode( $atts ) {
    $a = shortcode_atts( array(
        'page' => 'about',
    ), $atts );
    ob_start();
    $temp_content = file_get_contents("./wp-content/plugins/syncapt/pages/".$a['page'].".html");
    ob_end_clean();
    return $temp_content;
  }
  add_shortcode( 'p_page', 'p_page_shortcode' );

?>
