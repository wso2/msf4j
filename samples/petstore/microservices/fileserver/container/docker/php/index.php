<?php
# Fileserver IP
$fileserver="fileserver";
# File saving location
# need write access to apache user 
$file_location="upload";

if (isset($_FILES["file"]["tmp_name"]) && isset($_REQUEST["fileName"]) ){
    move_uploaded_file($_FILES["file"]["tmp_name"], "upload/" . $_REQUEST["fileName"]);
    echo "http://$fileserver/$file_location/".$_REQUEST["fileName"];
}
?>
