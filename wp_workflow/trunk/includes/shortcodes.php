<?php

  // shortcode [p_page page="page" capability="required-capability"]
  function p_page_shortcode( $atts ) {
    $a = shortcode_atts( array(
        'page' => 'about',
    ), $atts );
    $syncapt_options = new SyncaptOptions();
    ob_start();
    if (empty($a['capability']) || current_user_can($a['capability'])) {
      if (is_readable(get_template_directory().'/pages/'.$a['page'].'.html')) {
        $temp_content = file_get_contents(get_template_directory().'/pages/'.$a['page'].'.html');
        $temp_content = str_replace('%message_namespace%',$syncapt_options->get_message_namespace(),$temp_content);
      } else {
        error_log('Cannot read: '.get_template_directory().'/pages/'.$a['page'].'.html. Attempt to fallback to file within plugin');
        error_log('... message namespace found: '.$syncapt_options->get_message_namespace());
        $temp_content = file_get_contents(plugins_url( 'pages/'.$a['page'].'.html', dirname(__FILE__) ));
        $temp_content = str_replace('%message_namespace%',$syncapt_options->get_message_namespace(),$temp_content);
      }
    } else{
      $temp_content = file_get_contents(plugins_url( 'pages/not_allowed.html', dirname(__FILE__) ));
    }
    ob_end_clean();
    return $temp_content;
  }
  add_shortcode( 'p_page', 'p_page_shortcode' );

  // shortcode [p_form id="id of form post" title="Form label"]
  function p_form_shortcode( $atts ) {
    $a = shortcode_atts( array(
      'id' => '',
      'title' => '',
    ), $atts );
    $syncapt_options = new SyncaptOptions();
    ob_start();

    $form = get_post($a['id']);
    $form_content = $form->post_content;
    $form_content = apply_filters('the_content', $form_content);
    $form_content = str_replace(']]>', ']]&gt;', $form_content);

    $temp_content .= '<form class="" id="'.$form->post_name.'">';
    $temp_content .= '<div class="p-messages"></div>';

    $temp_content .= $form_content;

    $temp_content .= '<button data-p-action="$p.sendMessage(\'inOnly\', \''.$syncapt_options->get_message_namespace().'/'.$form->post_name.'\', $p.'.str_replace('-','_',$form->post_name).')" form="'.$form->post_name.'" type="button">Submit</button>';
    $temp_content .= '</form>';

    ob_end_clean();
    return $temp_content;
  }
  add_shortcode( 'p_form', 'p_form_shortcode' );

  // shortcode [p_tasks]
  function p_tasks_shortcode( $atts ) {
    $a = shortcode_atts( array(
        'page' => 'about',
    ), $atts );
    $user_id = get_current_user_id();
    $s_user = get_user_meta($user_id, 'syncapt_user', true);
    $s_pass = get_user_meta($user_id, 'syncapt_pass', true);
    if ($s_user == null || $s_pass == null) {
      return p_page_shortcode( array('page'=>'register') );
    }
    ob_start();
    ?>
      <h2>Welcome <?php echo $s_user ; ?></h2>
      <script type="text/javascript">
        console.debug("Loading tasks...");
      </script>
    <?php
    return ob_get_clean();
  }
  add_shortcode( 'p_tasks', 'p_tasks_shortcode' );

?>
