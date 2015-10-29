<?php
include('../API/curl_api.php');
include('../../config/config.php');

$username = htmlspecialchars($_POST["username"]);
$password = htmlspecialchars($_POST["password"]);

$auth_json = json_encode(array('name' => $username, 'password' => $password));
callAuthAPI('http://'.SECURITY_SERVER.':'.SECURITY_SERVER_PORT.'/user/login', $auth_json);
