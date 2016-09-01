<?php
//ajax call base rest call handler
session_start();
include('API/curl_api.php');
include('../config/config.php');

$api_type = htmlspecialchars($_POST["api_type"]);

if(isset($_SESSION['authtoken'])){
    $token = preg_replace('/\s+/', '', $_SESSION['authtoken']);
}else{
    echo json_encode(array('status' => 'error', 'message' => 'User unauthorized'));
    die;
}


switch ($api_type) {
    case "addPetTypes":
        $category_name = htmlspecialchars($_POST["category_name"]);
        $category_name_json = json_encode(array('name' => $category_name));
        $url = 'http://'.PET_SERVICE.':'.PET_SERVICE_PORT.'/category/';
        callAuthApiAddPetTypes($url, $token, $category_name_json);
        break;

    case "addPets":
        $url_image_upload= 'http://'.FILE_SERVER.':'.FILE_SERVER_PORT;
        imageUpload($url_image_upload, $_FILES, $_SESSION['authtoken']);
        $pet_category = htmlspecialchars($_POST["pet-category"]);
        $pet_age_months = htmlspecialchars($_POST["pet-age-months"]);
        $pet_price = htmlspecialchars($_POST["pet-price"]);
        $url = 'http://'.PET_SERVICE.':'.PET_SERVICE_PORT.'/pet/';
        $data_json =json_encode(
            array(
                'category' => array('name'=>$pet_category),
                'ageMonths' => $pet_age_months,
                'price' => $pet_price,
                'image'=> $_FILES['file']['name']
            )
        );
        callAuthApiAddPet($url, $token, $data_json);
        break;

    case "deletePetTypes":
        $category_name = htmlspecialchars($_POST["category_name"]);
        $url = 'http://'.PET_SERVICE.':'.PET_SERVICE_PORT.'/category/'.urlencode($category_name);
        callAuthapiDeletePetType($url, $token);
        break;

    case "deletePet":
        $pet_id = htmlspecialchars($_POST["pet_id"]);
        $url = 'http://'.PET_SERVICE.':'.PET_SERVICE_PORT.'/pet/'.$pet_id;
        callAuthapiDeletePet($url, $token);
        break;

    default:
        echo "Invalid API call";
}