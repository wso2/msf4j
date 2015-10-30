<?php
//get security server env varaible
$pet_service = getenv('PET_SERVICE_HOST');
$pet_service_port = getenv('PET_SERVICE_PORT');
$file_server = getenv('PET_SERVICE_HOST');
$transaction_service = getenv('FE_TXN_SERVICE_HOST');
$transaction_service_port = getenv('FE_TXN_SERVICE_PORT');

define("SECURITY_SERVER", "security");
define("SECURITY_SERVER_PORT", "8080");
define("PET_SERVICE", $pet_service);
define("PET_SERVICE_PORT", $pet_service_port);
define("FILE_SERVER", $file_server);
define("TRANSACTION_SERVICE", $transaction_service);
define("TRANSACTION_SERVICE_PORT", $transaction_service_port);