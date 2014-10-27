<?php
define('BASE_URL','http://wp.knowprocess.com/wp-content/plugins/syncapt/emails/');
$iterator = new GlobIterator("*.html");
$filelist = array();
foreach($iterator as $entry) {
    $filelist[] = substr($entry->getFilename(),1,strpos($entry->getFilename(),".html")-1);
    //$filelist[] = substr($entry->getFilename(),1,5);
}
$i = 0;
echo "[\" --- please select template ---\",";
foreach($filelist as $entry) {
    echo "\"".BASE_URL.$entry."\"";
    $i++;
    if ($i < count($filelist)) echo ",";    
}
echo "]";
?>
