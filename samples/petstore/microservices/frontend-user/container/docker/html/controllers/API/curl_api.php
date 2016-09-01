<?php

/*
 * method for get API authentication
 */
function callAuthApiLogin($url, $data){

    $curl = curl_init($url);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($curl, CURLOPT_POST, true);
    curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
    curl_setopt($curl, CURLOPT_HTTPHEADER, array(
            'Content-Type: application/json',
            'Content-Length: ' . strlen($data))
    );
    curl_setopt($curl, CURLOPT_HEADER, true);
    $curl_response = curl_exec($curl);
    $info = curl_getinfo($curl);
    if ($curl_response === false) {
        curl_close($curl);
        die('error occured during curl exec. Additioanl info: ' . var_export($info));
    }

    $header_size = curl_getinfo($curl, CURLINFO_HEADER_SIZE);
    $header = substr($curl_response, 0, $header_size);

    if($info['http_code'] === 200){
        $response = explode("\n",$header);
        $searchword = 'X-JWT-Assertion';
        $matches = array_filter($response, function($var) use ($searchword) {
            return preg_match("/\b$searchword\b/i", $var); });

        session_start();
        $_SESSION['username'] = 'user';
        $_SESSION['authtoken'] = trim(explode("X-JWT-Assertion:", array_values($matches)[0])[1]);
        echo json_encode(array('status' => 1));
    }else if($info['http_code'] === 401){
        echo json_encode(array('status' => 'error', 'message' => 'User unauthorized'));
    }else{
        echo json_encode(array('status' => 'error', 'message' => 'Something went wrong'));
    }

}

function callAuthApigetPets($url, $token){
    $curl = curl_init($url);
    $token_assertion = 'X-JWT-Assertion: '.$token;
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($curl, CURLOPT_HTTPHEADER, array(
            'Content-Type: application/json',
            $token_assertion)
    );
    curl_setopt($curl, CURLOPT_HEADER, true);
    $curl_response = curl_exec($curl);
    $info = curl_getinfo($curl);
    if ($curl_response === false) {
        curl_close($curl);
        die('error occured during curl exec. Additioanl info: ' . var_export($info));
    }

    $header_size = curl_getinfo($curl, CURLINFO_HEADER_SIZE);
    $header = substr($curl_response, 0, $header_size);
    $body = substr($curl_response, $header_size);

    if($info['http_code'] === 200) {
        return json_decode($body, true);
    }else if($info['http_code'] === 401){
        echo json_encode(array('status' => 'error', 'message' => 'User unauthorized'));
    }else{
        echo json_encode(array('status' => 'error', 'message' => 'Something went wrong'));
    }
}

function addToCart($pet_id, $pet_price, $pet_image){
    $pet_array = array(
        'id' => $pet_id,
        'price' => $pet_price,
        'image' => $pet_image
    );
    if(isset($_SESSION['cart'])){
        if(!in_array($pet_array, $_SESSION['cart'], true)){
            array_push($_SESSION['cart'], $pet_array);
            echo json_encode(array('status' => 'success', 'message' => 'Pet added to your cart successfully'));
        }else{
            echo json_encode(array('status' => 'warning', 'message' => 'Pet is already in your cart'));
        }

    }else{
        $_SESSION['cart'] = array();
    }

}

function removeFromCart($pet_id, $cart){

    for($i = count($cart)-1; $i >= 0; $i--){
        if(isset($cart[$i]["id"]) && ($cart[$i]["id"] == $pet_id)){
            unset($cart[$i]);
        }
    }
    $_SESSION['cart'] = $cart;
    echo json_encode(array('status' => 'success', 'message' => 'Pet remove from your cart successfully'));
}

function placeOrder($url, $token, $data){

    $curl = curl_init($url);
    $token_assertion = 'X-JWT-Assertion: '.$token;
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($curl, CURLOPT_POST, true);
    curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
    curl_setopt($curl, CURLOPT_HTTPHEADER, array(
            'Content-Type: application/json',
            $token_assertion)
    );
    curl_setopt($curl, CURLOPT_HEADER, true);
    $curl_response = curl_exec($curl);
    $info = curl_getinfo($curl);
    if ($curl_response === false) {
        curl_close($curl);
        die('error occured during curl exec. Additioanl info: ' . var_export($info));
    }

    $header_size = curl_getinfo($curl, CURLINFO_HEADER_SIZE);
    $header = substr($curl_response, 0, $header_size);
    $body = substr($curl_response, $header_size);

    if($info['http_code'] === 200){
        unset($_SESSION['cart']);
        echo json_encode(array('status' => 'success', 'message' => 'Your Order has been placed successfully..! Your transaction ID: '.$body));

    }else if($info['http_code'] === 401) {
        echo json_encode(array('status' => 'error', 'message' => 'User unauthorized'));
    }else{
        echo json_encode(array('status' => 'error', 'message' => 'Something went wrong'));
    }
}
?>