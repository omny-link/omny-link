<?php
$iterator = new GlobIterator("*.html");
$filelist = array();
foreach($iterator as $entry) {
    $filelist[] = substr($entry->getFilename(),1,strpos($entry->getFilename(),".html")-1);
    //$filelist[] = substr($entry->getFilename(),1,5);
}
$i = 0;
echo "[";
foreach($filelist as $entry) {
    echo "\"".$entry."\"";
    $i++;
    if ($i < count($filelist)) echo ",";    
}
echo "]";
?>
