<?php
//get security server env varaible
$pet_service = getenv('PET_SERVICE_HOST');
$pet_service_port = getenv('PET_SERVICE_PORT');
$pet_file_server = getenv('PET_SERVICE_HOST');
$pet_file_server_port = getenv('FE_FILE_SERVICE_NODE_PORT');
$transaction_service = getenv('FE_TXN_SERVICE_HOST');
$transaction_service_port = getenv('FE_TXN_SERVICE_PORT');

define("SECURITY_SERVER", "security");
define("SECURITY_SERVER_PORT", "80");
define("PET_SERVICE", $pet_service);
define("PET_SERVICE_PORT", $pet_service_port);
define("FILE_SERVER", $pet_file_server);
define("FILE_SERVER_HOST", $pet_file_server_port);
define("TRANSACTION_SERVICE", $transaction_service);
define("TRANSACTION_SERVICE_PORT", $transaction_service_port);