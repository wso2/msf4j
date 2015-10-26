<?php

if (isset($_FILES["file"]["tmp_name"]) && isset($_REQUEST["fileName"]) ){
    move_uploaded_file($_FILES["file"]["tmp_name"], "fs/" . $_REQUEST["fileName"]);
    echo "$_REQUEST["fileName"];
}
?>
