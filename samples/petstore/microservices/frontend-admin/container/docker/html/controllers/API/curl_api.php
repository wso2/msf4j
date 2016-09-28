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

/*
 * method for Add Pet type categories
 */
function callAuthApiAddPetTypes($url, $token, $data){
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
        echo json_encode(array('status' => 'success', 'message' => 'Category added successfully'));
    }else if($info['http_code'] === 401) {
        echo json_encode(array('status' => 'error', 'message' => 'User unauthorized'));
    }else{
        echo json_encode(array('status' => 'error', 'message' => 'Something went wrong'));
    }
}

/*
 * method for delete Pet type categories
 */
function callAuthApiDeletePetType($url, $token){
    $curl = curl_init($url);
    $token_assertion = 'X-JWT-Assertion: '.$token;
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "DELETE");
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
        echo json_encode(array('status' => 'success', 'message' => 'Category deleted successfully'));
    }else if($info['http_code'] === 401) {
        echo json_encode(array('status' => 'error', 'message' => 'User unauthorized'));
    }else{
        echo json_encode(array('status' => 'error', 'message' => 'Something went wrong'));
    }
}

function callAuthApiAddPet($url, $token, $data){
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

    if($info['http_code'] === 200){
        echo json_encode(array('status' => 'success', 'message' => 'Pet added successfully'));
    }else if($info['http_code'] === 401) {
        echo json_encode(array('status' => 'error', 'message' => 'User unauthorized'));
    }else{
        echo json_encode(array('status' => 'error', 'message' => 'Something went wrong'));
    }
}


/*
 * method for delete Pet
 */
function callAuthApiDeletePet($url, $token){
    $curl = curl_init($url);
    $token_assertion = 'X-JWT-Assertion: '.$token;
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "DELETE");
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
        echo json_encode(array('status' => 'success', 'message' => 'Pet deleted successfully'));
    }else if($info['http_code'] === 401) {
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

function callAuthApigetPetTypes($url, $token){
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

function imageUpload($url, $files, $token){
    $filePath = $files['file']['tmp_name'];
    $fileName = $files['file']['name'];
    exec("curl -X POST -H \"Transfer-Encoding: chunked\" -H \"Content-Type: application/octet-stream\" -H \"X-JWT-Assertion: {$token}\" --data-binary @{$filePath} {$url}/".urlencode($fileName));
}
