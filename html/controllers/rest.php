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
        $pet_category = htmlspecialchars($_POST["pet_category"]);
        $pet_age_months = htmlspecialchars($_POST["pet_age_months"]);
        $pet_price = htmlspecialchars($_POST["pet_price"]);
        $url = 'http://'.PET_SERVICE.':'.PET_SERVICE_PORT.'/pet/';
        $data_json =json_encode(
            array(
                'category' => array('name'=>$pet_category),
                'ageMonths' => $pet_age_months,
                'price' => $pet_price,
                'image'=>''
            )
        );
        callAuthApiAddPet($url, $token, $data_json);
        break;
    default:
        echo "Invalid API call";
}