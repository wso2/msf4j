<?php
include('controllers/API/curl_api.php');
include('config/config.php');
session_start();
if(!isset($_SESSION['username'])){
    header("location:login.php");
}
//return page breadcrumbs
$breadcrumbs = array("checkout.php"=>"checkout");
if(isset($_SESSION['cart'])){
    $cart = $_SESSION['cart'];
}
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

    <title>WSO2 Pet Store - Checkout</title>

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
<div class="navbar-wrapper">
    <nav class="navbar navbar-default" data-offset-top="50" data-offset-bottom="40">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-menu-toggle" data-toggle="dropdown">
                        <span class="icon fw-stack">
                            <i class="fw fw-tiles fw-stack-1x"></i>
                        </span>
                </a>
                <ul class="dropdown-menu tiles arrow dark add-margin-1x" role="menu">
                    <li>
                        <a href="index.php">
                            <img src="images/pets.png" class="invert-png">
                            <span class="name">Pets</span>
                        </a>
                    </li>
                    <li>
                        <a href="cart.php">
                            <img src="images/cart.png" class="invert-png">
                            <span class="name">My Cart</span>
                        </a>
                    </li>
                </ul>
                <ol class="breadcrumb navbar-brand">
                    <li><a href="index.php"><i class="icon fw fw-home"></i></a></li>
                    <?php
                    if($breadcrumbs){
                        foreach ($breadcrumbs as $key => $value) {
                            echo '<li><a href="'.$key.'">'.$value.'</a></li>';
                        }
                    }
                    ?>
                </ol>
            </div>
            <div id="navbar" class="collapse navbar-collapse">
                <ul class="nav navbar-nav navbar-right">
                    <li class="visible-inline-block">
                        <a href="cart.php">
                                <span class="icon fw-stack">
                                    <i class="fa fa-shopping-cart fa-stack-1x"></i>
                                </span>
                            Cart
                            <span class="badge"><?php
                                if(isset($_SESSION['cart'])) {
                                    echo count($_SESSION['cart']);
                                }
                                ?></span>
                        </a>
                    </li>
                </ul>
            </div><!--/.nav-collapse -->
        </div>
    </nav>
</div>

<!-- #page-content-wrapper -->
<div class="page-content-wrapper">
    <!-- page content -->
    <div class="container-fluid body-wrapper">
        <div class="clearfix"></div>
        <div class="page-header" id="loading">
            <h1>Checkout</h1>
        </div>
        <div class="row">
            <div class="form-horizontal col-md-5"
                <div class="form-group">
                    <div class="col-sm-12">
                        <label for="card-number" class="control-label">Cart Number </label>
                        <input type="text" class="form-control" id="card-number" min="1" max="1200"
                               placeholder="Card Number" required="" inputmode="numeric" autocomplete="cc-number">
                    </div>
                    <div class="col-sm-12">
                        <label for="card-holder-name" class="control-label">Cart Holder Name </label>
                        <input type="text" class="form-control" id="card-holder-name" min="1" max="1200"
                               placeholder="Card Holder Name" required="">
                    </div>
                    <div class="col-sm-6">
                        <label for="card-holder-cvc" class="control-label">Cart Holder CVC </label>
                        <input type="text" class="form-control" id="card-holder-cvc" min="1" max="1200"
                               placeholder="Card Holder CVC" required="">
                    </div>
                    <div class="clearfix"></div>
                    <div class="col-sm-6">
                        <br/>
                        <a type="button" id="place-order-btn" class="btn btn-default btn-primary">Place Order</a>
                    </div>
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
    $('#place-order-btn').click(function(){
        var cardNumber = $('#card-number').val(),
            cardHolderName = $('#card-holder-name').val(),
            cardCvc = $('#card-holder-cvc').val();

        $.ajax({
            type: "POST",
            url:  "controllers/cart/cart.php",
            dataType: 'json',
            data: {
                cart_action: 'placeOrder',
                card_number: cardNumber,
                card_holder_name: cardHolderName,
                card_cvc: cardCvc
            },
            success: function (data, textStatus, jqXHR) {
                if (data.status == 'error') {
                    var n = noty({text: data.message, layout: 'bottomRight', type: 'error'});
                } else if (data.status == 'warning') {
                    var n = noty({text: data.message, layout: 'bottomRight', type: 'warning'});
                } else {
                    var n = noty({text: data.message, layout: 'bottomRight', type: 'success'});
                    window.setTimeout(function(){
                        window.location.href = 'index.php';
                    }, 1500);
                }
            }
        });
    });
</script>
<script src="js/custom.js"></script>

</body>
</html>