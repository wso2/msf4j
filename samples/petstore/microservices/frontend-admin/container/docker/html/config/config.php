<?php
//get security server env varaible
$pet_service = getenv('PET_SERVICE_HOST');
$pet_service_port = getenv('PET_SERVICE_PORT');
$pet_file_server_port = getenv('FE_FILE_SERVICE_NODE_PORT');
$pet_file_server = getenv('FE_FILE_SERVICE_HOST');
$security_service_host = getenv('SECURITY_SERVICE_HOST');
$security_service_port = getenv('SECURITY_SERVICE_PORT');

define("SECURITY_SERVER", $security_service_host);
define("SECURITY_SERVER_PORT", $security_service_port);
define("PET_SERVICE", $pet_service);
define("PET_SERVICE_PORT", $pet_service_port);
define("FILE_SERVER", $pet_file_server);
define("FILE_SERVER_HOST", $pet_file_server_port);

?>

