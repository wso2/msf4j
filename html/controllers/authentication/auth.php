<?php
include('../API/curl_api.php');
$username = htmlspecialchars($_POST["username"]);
$password = htmlspecialchars($_POST["password"]);

$auth_json = json_encode(array('name' => $username, 'password' => $password));
callAuthAPI('http://192.168.58.5:8080/user/login', $auth_json);
