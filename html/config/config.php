<?php
//get security server env varaible
$pet_service = getenv('FE_PET_SERVICE_HOST');
$pet_service_port = getenv('FE_PET_SERVICE_PORT');

define("SECURITY_SERVER", "security");
define("SECURITY_SERVER_PORT", "80");
define("PET_SERVICE", $pet_service);
define("PET_SERVICE_PORT", $pet_service_port);