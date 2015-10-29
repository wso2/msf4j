<?php
//ajax call base rest call handler
session_start();
include('API/curl_api.php');
include('../config/config.php');

$api_type = htmlspecialchars($_POST["api_type"]);
$token = preg_replace('/\s+/', '', $_SESSION['authtoken']);

switch ($api_type) {
    case "addPetTypes":
        $category_name = htmlspecialchars($_POST["category_name"]);
        $category_name_json = json_encode(array('name' => $category_name));
        $url = 'http://'.PET_SERVICE.':'.PET_SERVICE_PORT.'/category/';
        callAuthAPIAddPetTypes($url, $token, $category_name_json);
        break;
    case "addPets":
        $url = 'http://'.PET_SERVICE.':'.PET_SERVICE_PORT.'/pet/';
        callAuthAPIAddPets($url);
        break;
    default:
        echo "Invalid API call";
}