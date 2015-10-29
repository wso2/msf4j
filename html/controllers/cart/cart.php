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

    default:
        echo "Invalid API call";
}
