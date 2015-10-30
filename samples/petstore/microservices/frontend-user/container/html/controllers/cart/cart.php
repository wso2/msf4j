<?php
//ajax call base rest call handler
session_start();
include('../API/curl_api.php');
include('../../config/config.php');

$cart_action = htmlspecialchars($_POST["cart_action"]);
$token = preg_replace('/\s+/', '', $_SESSION['authtoken']);

switch ($cart_action) {
    case "addTocart":
        $pet_id = htmlspecialchars($_POST["pet_id"]);
        $pet_price = htmlspecialchars($_POST["pet_price"]);
        $pet_image = htmlspecialchars($_POST["pet_image"]);
        addToCart($pet_id, $pet_price, $pet_image);
        break;

    case "removeFromcart":
        $pet_id = htmlspecialchars($_POST["pet_id"]);
        $cart = $_SESSION['cart'];
        removeFromCart($pet_id, $cart);
        break;

    case "placeOrder":
        $url = 'http://'.TRANSACTION_SERVICE.':'.TRANSACTION_SERVICE_PORT.'/transaction/';
        $cart = $_SESSION['cart'];
        $cart_ids = array();
        foreach($cart as $json ){
            array_push($cart_ids, $json['id']);
        }
        $card_number = htmlspecialchars($_POST["card_number"]);
        $card_holder_name = htmlspecialchars($_POST["card_holder_name"]);
        $card_cvc = htmlspecialchars($_POST["card_cvc"]);
        $cart_total = $_SESSION['carttotal'];
        $data = array(
            "pets" => $cart_ids,
            'total' => $cart_total,
            'creditCard'=>array(
                "number"=> $card_number,
                "name"=>$card_holder_name,
                "cvc"=>$card_cvc
            )
        );
        placeOrder($url, $token, json_encode($data));
        break;

    default:
        echo "Invalid API call";
}
