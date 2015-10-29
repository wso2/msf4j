<?php
include('controllers/API/curl_api.php');
include('config/config.php');
session_start();
if(!isset($_SESSION['username'])){
    header("location:login.php");
}
//return page breadcrumbs
$breadcrumbs = array("pets.php"=>'Pets', "add-pets.php"=>'Add');

$url = 'http://'.PET_SERVICE.':'.PET_SERVICE_PORT.'/category/all';
$pet_catogories = callAuthAPIgetPetTypes($url,  preg_replace('/\s+/', '', $_SESSION['authtoken']));

?>
<!--
~   Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
~
~   Licensed under the Apache License, Version 2.0 (the "License");
~   you may not use this file except in compliance with the License.
~   You may obtain a copy of the License at
~
~        http://www.apache.org/licenses/LICENSE-2.0
~
~   Unless required by applicable law or agreed to in writing, software
~   distributed under the License is distributed on an "AS IS" BASIS,
~   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~   See the License for the specific language governing permissions and
~   limitations under the License.
-->

<!DOCTYPE html>
<html lang="en">
<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>WSO2 Pet Store - Administrator</title>

    <link rel="shortcut icon" href="images/favicon.png" />

    <!-- Bootstrap CSS -->
    <link href="libs/bootstrap_3.2.0/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
    <!-- Font Awesome CSS -->
    <link href="libs/font-awesome_4.3.0/css/font-awesome.min.css" rel="stylesheet" type="text/css" />
    <!-- Font WSO2 CSS -->
    <link href="libs/font-wso2_1.2/css/font-wso2.css" rel="stylesheet" type="text/css" />
    <link href="css/custom.css" rel="stylesheet" type="text/css" />

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="libs/html5shiv_3.7.2/html5shiv.min.js"></script>
    <script src="libs/respond_1.4.2/respond.min.js"></script>
    <![endif]-->

</head>
<body>
<!-- header -->
<?php
include('includes/header.php');
?>

<!-- navbar -->
<?php
include('includes/navbar.php');
?>
<!-- #page-content-wrapper -->
<div class="page-content-wrapper">
    <!-- page content -->
    <div class="container-fluid body-wrapper">
        <div class="clearfix"></div>
        <div class="page-header" id="loading">
            <h1>Add Pets</h1>
        </div>
        <div class="form-horizontal col-md-5">
            <div class="form-group">
                <div class="col-sm-12">
                    <label for="pet-category" class="control-label">Type</label>
                    <select class="form-control" id="pet-category">
                        <?php
                        if(is_array($pet_catogories)) {
                            foreach ($pet_catogories as $json) {
                                echo '<option value="' . $json['name'] . '">' . $json['name'] . '</option>';
                            }
                        }
                        ?>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-12">
                    <label for="pet-age-months" class="control-label">Age </label>
                    <input type="text" class="form-control" id="pet-age-months" placeholder="pet age" required="">
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-12">
                    <label for="pet-price" class="control-label">Price </label>
                    <input type="text" class="form-control" id="pet-price" placeholder="pet price" required="">
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-12">
                    <label for="pet-image" class="control-label">Image </label>

                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-12">
                    <button type="button" id="pet-add-btn" class="btn btn-default btn-primary">Add</button>
                </div>
            </div>
        </div>
    </div>

</div><!-- /#page-content-wrapper -->


<!-- footer -->
<footer class="footer">
    <div class="container-fluid">
        <p>&copy; <script>document.write(new Date().getFullYear());</script> <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i> Inc</a>. All Rights Reserved.</p>
    </div>
</footer>

<!-- Jquery/Jquery UI JS -->
<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<!-- Bootstrap JS -->
<script src="libs/bootstrap_3.2.0/js/bootstrap.min.js"></script>

<!-- Noty JS -->
<script src="libs/noty_2.3.5/packaged/jquery.noty.packaged.min.js"></script>
<script>
    $(document).ready(function () {
        var petCategory = $('#pet-category'),
            petAgeMonths = $('#pet-age-months'),
            petPrice = $('#pet-price');

        // handle the add pet type button click event
        $('#pet-add-btn').click(function () {
            var btn = $(this); //get current clicked button
            addPets(btn);
        });

        function addPets(btn){
            btn.button('loading');
            $.ajax({
                type: "POST",
                url:  "controllers/rest.php",
                dataType: 'json',
                data: {
                    api_type: 'addPets',
                    pet_category: petCategory.val(),
                    pet_age_months: petAgeMonths.val(),
                    pet_price: petPrice.val()
                },
                success: function (data) {

                }
            })
                .always(function () {
                    btn.button('reset');
                });
        }

    });
</script>
<script src="js/custom.js"></script>

</body>
</html>