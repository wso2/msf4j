
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
    <link href="css/custom.css" rel="stylesheet" type="text/css" />

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="libs/html5shiv_3.7.2/html5shiv.min.js"></script>
    <script src="libs/respond_1.4.2/respond.min.js"></script>
    <![endif]-->

</head>
<body>
<?php
var_dump(getenv('MY_PROJECT_ENV'));
?>
<!-- header -->
<header class="header header-default">
    <div class="container-fluid">
        <div class="pull-left brand float-remove-xs text-center-xs">
            <a href="#">
                <img src="libs/theme-wso2_1.0/images/logo-inverse.svg" alt="wso2" title="wso2" class="logo">
                <h1>UI Component Library</h1>
            </a>
        </div>
        <div class="pull-right auth float-remove-xs text-center-xs">
            <a href="#" class="dropdown" data-toggle="dropdown">
                <span class="hidden-xs add-padding-left-3x">administrator <span class="caret"></span></span>
               <span class="icon fw-stack fw-lg">
                   <i class="fw fw-user fw-stack-1x"></i>
               </span>
            </a>
            <ul class="dropdown-menu float-remove-xs position-static-xs text-center-xs remove-margin-xs slideInDown" role="menu">
                <li class="dropdown-header visible-xs">administrator <span class="caret"></span></li>
                <li class="divider visible-xs"></li>
                <li><a href="#">Sign out</a></li>
            </ul>

        </div>
    </div>
</header>

<!-- navbar -->
<div class="navbar-wrapper">
    <nav class="navbar navbar-default" data-spy="affix" data-offset-top="50" data-offset-bottom="40">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-menu-toggle" data-toggle="sidebar" data-target="#left-sidebar" data-container=".page-content-wrapper" data-container-push="true"  data-push-side="left" aria-controls="left-sidebar" aria-expanded="false">
                        <span class="icon fw-stack">
                            <i class="fw fw-menu fw-stack-1x toggle-icon-left-arrow"></i>
                        </span>
                </a>
                <a class="navbar-menu-toggle" data-toggle="dropdown">
                        <span class="icon fw-stack">
                            <i class="fw fw-tiles fw-stack-1x"></i>
                        </span>
                </a>
                <a class="navbar-menu-toggle collapsed" data-toggle="collapse" data-target="#navbar2"  aria-expanded="false" aria-controls="navbar2">
                        <span class="icon fw-stack">
                            <i class="fw fw-down fw-stack-1x toggle-icon-up"></i>
                        </span>
                </a>
                <ul class="dropdown-menu tiles arrow dark add-margin-1x" role="menu">
                    <li>
                        <a href="#">
                            <i class="icon fw fw-api"></i>
                            <span class="name">API</span>
                        </a>
                    </li>
                    <li>
                        <a href="#">
                            <i class="icon fw fw-wsdl"></i>
                            <span class="name">WSDL</span>
                        </a>
                    </li>
                    <li>
                        <a href="#">
                            <i class="icon fw fw-soap"></i>
                            <span class="name">SOAP Service</span>
                        </a>
                    </li>
                    <li>
                        <a href="#">
                            <i class="icon fw fw-rest-service"></i>
                            <span class="name">REST Service</span>
                        </a>
                    </li>
                </ul>
                <ol class="breadcrumb navbar-brand">
                    <li><a href="index.html"><i class="icon fw fw-home"></i></a></li>
                    <li><a href="#">Library</a></li>
                    <li class="active"><a href="#">Data</a></li>
                </ol>
            </div>
            <div id="navbar" class="collapse navbar-collapse">
                <ul class="nav navbar-nav">
                    <li>
                        <a href="layout.html">
                                <span class="icon fw-stack">
                                    <i class="fw fw-laptop fw-stack-1x"></i>
                                    <i class="fw fw-ring fw-stack-2x"></i>
                                </span>
                            Main Wrapper
                        </a>
                    </li>
                    <li>
                        <a href="details_layout.html">Details Layout</a>
                    </li>
                    <li class="active"><a href="#noiconlink">No Icon Link</a></li>
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Quick Jump <span class="caret"></span></a>
                        <ul class="dropdown-menu" role="menu">
                            <li class="dropdown-header">UI Components</li>
                            <li class="divider"></li>
                            <li><a href="#forms">Forms</a></li>
                            <li class="dropdown-submenu"><a href="#table">Table</a>
                                <ul class="dropdown-menu">
                                    <li><a href="#">Data Table</a></li>
                                    <li><a href="#">Data List Table</a></li>
                                </ul>
                            </li>
                        </ul>
                    </li>
                </ul>
                <ul class="nav navbar-nav navbar-right">
                    <li class="visible-inline-block">
                        <a data-toggle="sidebar" data-target="#right-sidebar" aria-controls="right-sidebar" aria-expanded="false">
                                <span class="icon fw-stack">
                                    <i class="fw fw-notification fw-stack-1x"></i>
                                </span>
                            Notifications
                            <span class="badge">4</span>
                        </a>
                    </li>
                    <li class="visible-inline-block">
                        <a data-toggle="sidebar" data-target="#right-sidebar2" data-container=".page-content-wrapper" data-container-divide="true" data-divide-side="right" aria-controls="right-sidebar2" aria-expanded="false">
                                <span class="icon fw-stack">
                                    <i class="fw fw-settings fw-stack-1x"></i>
                                    <i class="fw fw-ring fw-stack-2x"></i>
                                </span>
                        </a>
                    </li>
                </ul>
            </div><!--/.nav-collapse -->
        </div>
        <div id="navbar2" class="collapse navbar-collapse dropdown tiles"><!-- nav-collapse -->
            <ul class="nav navbar-nav">
                <li><a href="#"><i class="fw fw-user"></i>User Management</a></li>
                <li><a href="#"><i class="fw fw-settings"></i>Profile Management</a></li>
                <li><a href="#"><i class="fw fw-policy"></i>Policy Management</a></li>
            </ul>
        </div><!--/ .nav-collapse -->
    </nav>
</div>

<!-- .left-sidebar -->
<div class="sidebar-wrapper" id="left-sidebar" data-side="left">
    <ul class="sidebar-nav">
        <li class="sidebar-brand">
            <a href="#">
                <i class="fw fw-dashboard"></i>
                Dashboard
            </a>
        </li>
        <li>
            <a href="#">Life Cycle</a>
        </li>
        <li>
            <a href="#">Show Dependency</a>
        </li>
        <li>
            <a href="#">Compare</a>
        </li>
    </ul>
</div>
<!-- /.left-sidebar -->

<!-- .right-sidebar -->
<div class="sidebar-wrapper" id="right-sidebar" data-side="right">
    <ul class="sidebar-messages">
        <li class="message message-success">
            <h4><i class="icon fw fw-ok"></i>Minor Information Message</h4>
            <p>Alerts don't have default classes, only base and modifier classes.</p>
        </li>
        <li class="message message-info">
            <h4><i class="icon fw fw-info"></i>Minor Information Message</h4>
            <p>Alerts don't have default classes, only base and modifier classes.</p>
        </li>
        <li class="message message-warning">
            <h4><i class="icon fw fw-warning"></i>Major Information Message</h4>
            <p>Alerts don't have default classes, only base and modifier classes.</p>
        </li>
        <li class="message message-danger">
            <h4><i class="icon fw fw-error"></i>Critical Information Message</h4>
            <p>Alerts don't have default classes, only base and modifier classes.</p>
        </li>
    </ul>
</div>
<!-- /.right-sidebar -->

<!-- .right-sidebar2 -->
<div class="sidebar-wrapper inverse" id="right-sidebar2" data-side="right">

    <!-- notes -->
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="headingNotes">
            <h2 class="sub-title panel-title">
                <a data-toggle="collapse" href="#collapseNotes" aria-expanded="true" aria-controls="collapseNotes" class="collapsed">
                        <span class="fw-stack">
                            <i class="fw fw-ring fw-stack-2x"></i>
                            <i class="fw fw-arrow fw-down fw-stack-1x"></i>
                        </span>
                    Notes
                </a>
            </h2>
        </div>
        <div id="collapseNotes" class="panel-collapse collapse in" aria-labelledby="headingNotes">
            <div class="panel-body wr-panel-notes">

                <!--<a href="#" class="cu-btn-inner add-btn">-->
                <!--<span class="fw-stack">-->
                <!--<i class="fw fw-ring fw-stack-2x"></i>-->
                <!--<i class="fw fw-add fw-stack-1x"></i>-->
                <!--</span>-->
                <!--Add Note-->
                <!--</a>-->

                <a href="#" class="cu-btn-inner add-btn">
                        <span class="fw-stack">
                            <i class="fw fw-ring fw-stack-2x"></i>
                            <i class="fw fw-add fw-stack-1x"></i>
                        </span>
                    New
                </a>

                <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">

                    <div class="panel">

                        <div class="wr-panel-note collapsed" role="tab" data-toggle="collapse" data-parent="#accordion" href="#note1" aria-expanded="false" aria-controls="note1">
                            <div class="wr-panel-desc-icon">
                                <i class="wr-panel-desc-icon fw fw-user"></i>
                            </div>
                            <div class="wr-panel-msg">
                                <div class="open msg-options">
                                    <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                        <i class="fw fw-dots fw-rotate-90"></i>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li><a href="#"><i class="fw fw-edit fa-fw"></i> Edit</a></li>
                                        <li><a href="#"><i class="fw fw-delete fa-fw"></i> Delete</a></li>
                                        <li class="divider"></li>
                                        <li><a href="#"><i class="i"></i> Resolve Thread</a></li>
                                    </ul>
                                </div>
                                <div>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam eu dictum lectus, nec lacinia odio. Fusce eget malesuada lorem, vitae blandit justo. Morbi sed sapien vestibulum ipsum bibendum blandit. Integer magna dui, consequat sed velit ac, pulvinar feugiat nisi. Nulla pharetra viverra risus, quis mattis mauris elementum quis.</div>
                                <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"><i class="fw fw-clock"></i> 11.25am | 12-April-2015</span></div>
                                <!--<button type="button" class="btn btn-default wr-panel-msg-visibility" aria-label="Left Align">-->
                                <!--<i class="glyphicon glyphicon glyphicon-lock" aria-hidden="true"></i>-->
                                <!--</button>-->
                                <a href="#" class="wr-panel-msg-visibility"><i class="glyphicon glyphicon glyphicon-lock"></i></a>
                            </div>
                        </div>
                        <div id="note1" class="panel-collapse collapse" role="tabpanel" aria-labelledby="note1">

                            <a href="#" class="more-link">More ...</a>

                            <div class="wr-panel-sub-note">
                                <div class="wr-panel-desc-icon">
                                    <i class="wr-panel-desc-icon fw fw-user"></i>
                                </div>
                                <div class="wr-panel-msg">
                                    <div class="open msg-options">
                                        <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                            <i class="fw fw-dots fw-rotate-90"></i>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#"><i class="fw fw-edit fa-fw"></i> Edit</a></li>
                                            <li><a href="#"><i class="fw fw-delete fa-fw"></i> Delete</a></li>
                                        </ul>
                                    </div>
                                    <div>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam eu dictum lectus, nec lacinia odio. Fusce eget malesuada lorem, vitae blandit justo.</div>
                                    <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"><i class="fw fw-clock"></i> 11.25am | 12-April-2015</span></div>
                                </div>
                            </div>
                            <div class="wr-panel-sub-note">
                                <div class="wr-panel-desc-icon">
                                    <i class="wr-panel-desc-icon fw fw-user"></i>
                                </div>
                                <div class="wr-panel-msg">
                                    <div class="open msg-options">
                                        <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                            <i class="fw fw-dots fw-rotate-90"></i>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#"><i class="fw fw-edit fa-fw"></i> Edit</a></li>
                                            <li><a href="#"><i class="fw fw-delete fa-fw"></i> Delete</a></li>
                                        </ul>
                                    </div>
                                    <div>Added asset weather.xml</div>
                                    <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"> - 11.25am | 12-April-2015</span></div>
                                </div>
                            </div>

                            <div class="well">
                                <textarea placeholder="reply..."></textarea>
                                <a href="#" class="cu-btn-inner">
                                        <span class="fw-stack">
                                            <i class="fw fw-ring fw-stack-2x"></i>
                                            <i class="fw fw-message fw-stack-1x"></i>
                                        </span>
                                    Reply
                                </a>
                                <!--<a href="#" class="cu-btn-inner">-->
                                <!--<span class="fw-stack">-->
                                <!--<i class="fw fw-ring fw-stack-2x"></i>-->
                                <!--<i class="fw fw-check fw-stack-1x"></i>-->
                                <!--</span>-->
                                <!--Resolve-->
                                <!--</a>-->
                            </div>
                        </div>




                        <div class="wr-panel-note collapsed" role="tab" data-toggle="collapse" data-parent="#accordion" href="#note2" aria-expanded="false" aria-controls="note2">
                            <div class="wr-panel-desc-icon">
                                <i class="wr-panel-desc-icon fw fw-user"></i>
                            </div>
                            <div class="wr-panel-msg">
                                <div class="open msg-options">
                                    <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                        <i class="fw fw-dots fw-rotate-90"></i>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li><a href="#"><i class="fw fw-edit fa-fw"></i> Edit</a></li>
                                        <li><a href="#"><i class="fw fw-delete fa-fw"></i> Delete</a></li>
                                        <li class="divider"></li>
                                        <li><a href="#"><i class="i"></i> Resolve Thread</a></li>
                                    </ul>
                                </div>
                                <div>Added asset weather.xml</div>
                                <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"><i class="fw fw-clock"></i> 11.25am | 12-April-2015</span></div>
                                <a href="#" class="wr-panel-msg-visibility"><i class="glyphicon glyphicon glyphicon-lock"></i></a>
                            </div>
                        </div>
                        <div id="note2" class="panel-collapse collapse" role="tabpanel" aria-labelledby="note2">

                            <div class="wr-panel-sub-note">
                                <div class="wr-panel-desc-icon">
                                    <i class="wr-panel-desc-icon fw fw-user"></i>
                                </div>
                                <div class="wr-panel-msg">
                                    <div class="open msg-options">
                                        <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                            <i class="fw fw-dots fw-rotate-90"></i>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#"><i class="fw fw-edit fa-fw"></i> Edit</a></li>
                                            <li><a href="#"><i class="fw fw-delete fa-fw"></i> Delete</a></li>
                                        </ul>
                                    </div>
                                    <div>Added asset weather.xml</div>
                                    <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"><i class="fw fw-clock"></i> 11.25am | 12-April-2015</span></div>
                                </div>
                            </div>
                            <div class="wr-panel-sub-note">
                                <div class="wr-panel-desc-icon">
                                    <i class="wr-panel-desc-icon fw fw-user"></i>
                                </div>
                                <div class="wr-panel-msg">
                                    <div class="open msg-options">
                                        <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                            <i class="fw fw-dots fw-rotate-90"></i>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#"><i class="fw fw-edit fa-fw"></i> Edit</a></li>
                                            <li><a href="#"><i class="fw fw-delete fa-fw"></i> Delete</a></li>
                                        </ul>
                                    </div>
                                    <div>Added asset weather.xml</div>
                                    <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"><i class="fw fw-clock"></i> 11.25am | 12-April-2015</span></div>
                                </div>
                            </div>

                            <div class="well">
                                <textarea placeholder="reply..."></textarea>
                                <a href="#" class="cu-btn-inner">
                                        <span class="fw-stack">
                                            <i class="fw fw-ring fw-stack-2x"></i>
                                            <i class="fw fw-message fw-stack-1x"></i>
                                        </span>
                                    Reply
                                </a>
                                <!--<a href="#" class="cu-btn-inner">-->
                                <!--<span class="fw-stack">-->
                                <!--<i class="fw fw-ring fw-stack-2x"></i>-->
                                <!--<i class="fw fw-check fw-stack-1x"></i>-->
                                <!--</span>-->
                                <!--Resolve-->
                                <!--</a>-->
                            </div>
                        </div>




                        <div class="wr-panel-note collapsed row" role="tab" data-toggle="collapse" data-parent="#accordion" href="#note3" aria-expanded="false" aria-controls="note3">
                            <div class="wr-panel-desc-icon">
                                <i class="wr-panel-desc-icon fw fw-user"></i>
                            </div>
                            <div class="wr-panel-msg">
                                <div class="open msg-options">
                                    <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                        <i class="fw fw-dots fw-rotate-90"></i>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li><a href="#"><i class="fw fw-edit fa-fw"></i> Edit</a></li>
                                        <li><a href="#"><i class="fw fw-delete fa-fw"></i> Delete</a></li>
                                        <li class="divider"></li>
                                        <li><a href="#"><i class="i"></i> Resolve Thread</a></li>
                                    </ul>
                                </div>
                                <div>Added asset weather.xml</div>
                                <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"><i class="fw fw-clock"></i> 11.25am | 12-April-2015</span></div>
                                <a href="#" class="wr-panel-msg-visibility"><i class="glyphicon glyphicon glyphicon-globe"></i></a>
                            </div>
                        </div>
                        <div id="note3" class="panel-collapse collapse" role="tabpanel" aria-labelledby="note3">

                            <div class="wr-panel-sub-note">
                                <div class="wr-panel-desc-icon">
                                    <i class="wr-panel-desc-icon fw fw-user"></i>
                                </div>
                                <div class="wr-panel-msg">
                                    <div class="open msg-options">
                                        <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                            <i class="fw fw-dots fw-rotate-90"></i>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#"><i class="fw fw-edit fa-fw"></i> Edit</a></li>
                                            <li><a href="#"><i class="fw fw-delete fa-fw"></i> Delete</a></li>
                                        </ul>
                                    </div>
                                    <div>Added asset weather.xml</div>
                                    <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"><i class="fw fw-clock"></i> 11.25am | 12-April-2015</span></div>
                                </div>
                            </div>
                            <div class="wr-panel-sub-note">
                                <div class="wr-panel-desc-icon">
                                    <i class="wr-panel-desc-icon fw fw-user"></i>
                                </div>
                                <div class="wr-panel-msg">
                                    <div class="open msg-options">
                                        <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                            <i class="fw fw-dots fw-rotate-90"></i>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#"><i class="fw fw-edit fa-fw"></i> Edit</a></li>
                                            <li><a href="#"><i class="fw fw-delete fa-fw"></i> Delete</a></li>
                                        </ul>
                                    </div>
                                    <div>Added asset weather.xml</div>
                                    <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"><i class="fw fw-clock"></i> 11.25am | 12-April-2015</span></div>
                                </div>
                            </div>

                            <div class="well">
                                <textarea placeholder="reply..."></textarea>
                                <a href="#" class="cu-btn-inner">
                                        <span class="fw-stack">
                                            <i class="fw fw-ring fw-stack-2x"></i>
                                            <i class="fw fw-message fw-stack-1x"></i>
                                        </span>
                                    Reply
                                </a>
                                <!--<a href="#" class="cu-btn-inner">-->
                                <!--<span class="fw-stack">-->
                                <!--<i class="fw fw-ring fw-stack-2x"></i>-->
                                <!--<i class="fw fw-check fw-stack-1x"></i>-->
                                <!--</span>-->
                                <!--Resolve-->
                                <!--</a>-->
                            </div>
                        </div>




                        <div class="wr-panel-note collapsed resolved" role="tab" data-toggle="collapse" data-parent="#accordion" href="#note4" aria-expanded="false" aria-controls="note4">
                            <div class="wr-panel-desc-icon">
                                <i class="wr-panel-desc-icon fw fw-user"></i>
                            </div>
                            <div class="wr-panel-msg">
                                <div class="open msg-options">
                                    <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">
                                        <i class="fw fw-dots fw-rotate-90"></i>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li><a href="#"><i class="i"></i> Re-open</a></li>
                                    </ul>
                                </div>
                                <div>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam eu dictum lectus, nec lacinia odio.</div>
                                <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"><i class="fw fw-clock"></i> 11.25am | 12-April-2015</span></div>
                                    <span class="resolve-icon">
                                        <span class="fw-stack-md">
                                            <i class="fw fw-ok fw-stack-1-5x"></i>
                                        </span>
                                        Resolved
                                    </span>
                            </div>
                        </div>
                        <div id="note4" class="panel-collapse collapse resolved" role="tabpanel" aria-labelledby="note4">

                            <a href="#" class="more-link">More ...</a>

                            <div class="wr-panel-sub-note">
                                <div class="wr-panel-desc-icon">
                                    <i class="wr-panel-desc-icon fw fw-user"></i>
                                </div>
                                <div class="wr-panel-msg">
                                    <i class="fw fw-dots fw-rotate-90 msg-options"></i>
                                    <div>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam eu dictum lectus, nec lacinia odio. Fusce eget malesuada lorem, vitae blandit justo.</div>
                                    <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"><i class="fw fw-clock"></i> 11.25am | 12-April-2015</span></div>
                                </div>
                            </div>
                            <div class="wr-panel-sub-note">
                                <div class="wr-panel-desc-icon">
                                    <i class="wr-panel-desc-icon fw fw-user"></i>
                                </div>
                                <div class="wr-panel-msg">
                                    <i class="fw fw-dots fw-rotate-90 msg-options"></i>
                                    <div>Added asset weather.xml</div>
                                    <div class="wr-panel-msg-details"><span class="wr-list-username">John Doe</span> <span class="wr-panel-time"> - 11.25am | 12-April-2015</span></div>
                                </div>
                            </div>
                        </div>



                    </div>
                </div>


            </div>
        </div>
    </div>
    <!-- /notes -->
</div>
<!-- /.right-sidebar2 -->

<!-- #page-content-wrapper -->
<div class="page-content-wrapper">

    <!-- page content -->
    <div class="container-fluid body-wrapper">

        <div id="demohbs"></div>

        <!-- ******************************************************************
        Wrapper
        ******************************************************************* -->
        <!--<div class="page-header">-->
        <!--<h1>WSO2 Bootstrap Theme</h1>-->
        <!--<p class="lead">Starter Templates</p>-->
        <!--</div>-->
        <!--<p>Use this document as a way to quickly start any new project. All you get is this text and a mostly barebones HTML document.</p>-->

        <!-- ******************************************************************
        Message Templates
        ******************************************************************* -->
        <div class="page-header" id="Message">
            <h1>Custom Messages</h1>
            <p class="lead">Alert Templates</p>
        </div>
        <div class="alert alert-success" role="alert">
            <i class="icon fw fw-ok"></i><strong>Success!</strong>
            You successfully read this important alert message.
            <button type="button" class="close" aria-label="close" data-dismiss="alert"><span aria-hidden="true"><i class="fw fw-cancel"></i></span></button>
        </div>
        <div class="alert alert-info" role="alert">
            <i class="icon fw fw-warning"></i><strong>Info!</strong>
            This alert needs your attention, but it's not super important.
            <button type="button" class="close" aria-label="close" data-dismiss="alert"><span aria-hidden="true"><i class="fw fw-cancel"></i></span></button>
        </div>
        <div class="alert alert-warning" role="alert">
            <i class="icon fw fw-info"></i><strong>Warning!</strong>
            Better check yourself, you're not looking too good.
            <button type="button" class="close" aria-label="close" data-dismiss="alert"><span aria-hidden="true"><i class="fw fw-cancel"></i></span></button>
        </div>
        <div class="alert alert-danger" role="alert">
            <i class="icon fw fw-error"></i><strong>Error!</strong>
            Change a few things up and try submitting again.
            <button type="button" class="close" aria-label="close" data-dismiss="alert"><span aria-hidden="true"><i class="fw fw-cancel"></i></span></button>
        </div>

        <br class="spacer">

        <div class="page-header">
            <p class="lead">Message Status Templates</p>
        </div>
        <div class="message message-success">
            <h4><i class="icon fw fw-ok"></i>Message</h4>
            <p>Alerts don't have default classes, only base and modifier classes. A default gray alert
                doesn't make too much sense, so you're required to specify a type via contextual class.
                Choose from success, info, warning, or danger.</p>
        </div>
        <div class="message message-info">
            <h4><i class="icon fw fw-info"></i>Minor Information Message</h4>
            <p>Alerts don't have default classes, only base and modifier classes. A default gray alert
                doesn't make too much sense, so you're required to specify a type via contextual class.
                Choose from success, info, warning, or danger.</p>
        </div>
        <div class="message message-warning">
            <h4><i class="icon fw fw-warning"></i>Major Information Message</h4>
            <p>Alerts don't have default classes, only base and modifier classes. A default gray alert
                doesn't make too much sense, so you're required to specify a type via contextual class.
                Choose from success, info, warning, or danger.</p>
        </div>
        <div class="message message-danger">
            <h4><i class="icon fw fw-error"></i>Critical Information Message</h4>
            <p>Alerts don't have default classes, only base and modifier classes. A default gray alert
                doesn't make too much sense, so you're required to specify a type via contextual class.
                Choose from success, info, warning, or danger.</p>
        </div>

        <br class="spacer">

        <!-- ******************************************************************
        Form Elements Templates
        ******************************************************************* -->
        <div class="page-header" id="forms">
            <h1>Forms</h1>
            <p class="lead">Form Elements Templates</p>
        </div>
        <form class="form-horizontal">
            <div class="form-group">
                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                    <label class="control-label">Search</label>
                    <div class="input-group input-wrap">
                        <input type="text" class="form-control" placeholder="Search for...">
                        <div class="input-group-btn">
                            <button class="btn btn-default wrap-input-right" type="button" title="Search">
                                <i class="fw fw-search" aria-hidden="true"></i>
                            </button>
                            <button class="btn" type="button" title="Advance Search">
                                <i class="fw fw-settings" aria-hidden="true"></i>
                                <span class="hidden-xs">Advanced Search</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <form class="form-horizontal">
                <div class="form-group">
                    <div class="col-sm-12">
                        <label class="control-label">Filter</label>
                        <div class="input-group input-wrap">
                            <div class="input-group-btn" data-toggle="dropdown">
                                <button class="btn btn-default btn-primary" type="button" title="Advance Filter">
                                    <i class="fw fw-filter" aria-hidden="true"></i>
                                </button>
                            </div>
                            <ul class="dropdown-menu tiles arrow dark" role="menu">
                                <li>
                                    <a href="#">
                                        <i class="icon fw fw-android"></i>
                                        <span class="name">Android</span>
                                    </a>
                                </li>
                                <li>
                                    <a href="#">
                                        <i class="icon fw fw-apple"></i>
                                        <span class="name">iOS</span>
                                    </a>
                                </li>
                                <li class="divider"></li>
                                <li>
                                    <a href="#">
                                        <i class="icon fw fw-user"></i>
                                        <span class="name">User</span>
                                    </a>
                                </li>
                                <li>
                                    <a href="#">
                                        <i class="icon fw fw-users"></i>
                                        <span class="name">User Roles</span>
                                    </a>
                                </li>
                            </ul>
                            <div class="input-group-btn">
                                <button class="btn" type="button" title="Android">
                                    <i class="fw fw-android" aria-hidden="true"></i>
                                    <span class="hidden-xs">Android</span>
                                </button>
                            </div>
                            <div class="input-group-btn">
                                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                    All Users
                                    <i class="icon-caret fw fw-down icon-caret"></i>
                                </button>
                                <ul class="dropdown-menu">
                                    <li>
                                        <input type="text" class="form-control form-control-lg" placeholder="Search by user...">
                                        <div class="input-group-btn btn-input-right">
                                            <button class="btn btn-default" type="button" title="Search">
                                                <i class="fw fw-search" aria-hidden="true"></i>
                                            </button>
                                        </div>
                                    </li>
                                    <li><a href="#">All Users</a></li>
                                    <li role="separator" class="divider"></li>
                                    <li><a href="#">Denver</a></li>
                                    <li><a href="#">Jack</a></li>
                                    <li><a href="#">Cooper</a></li>
                                    <li><a href="#">Nick</a></li>
                                </ul>
                            </div>
                            <input type="text" class="form-control" placeholder="Search for...">
                            <div class="input-group-btn">
                                <button class="btn btn-default wrap-input-right" type="button" title="Search">
                                    <i class="fw fw-search" aria-hidden="true"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <label class="control-label">Name *</label>
                        <div class="input-group input-wrap res-wrap">
                            <input type="text" class="form-control" placeholder="First Name" required>
                            <div class="input-group-btn">
                                <label class="control-label">Surname</label>
                            </div>
                            <div class="input-group-btn">
                                <input type="text" class="form-control form-control-lg" placeholder="Last Name">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <label for="input-name" class="control-label">Email <span class="">*</span></label>
                        <input type="email" class="form-control" id="input-name" placeholder="email@example.com" required>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <label class="control-label">File Upload [Supports Multi Select]</label>
                        <div class="input-group input-wrap file-upload-control">
                            <input type="text" class="form-control" readonly>
                            <input type="file" class="form-control" multiple>
                            <div class="input-group-btn">
                                <button class="btn browse" type="button" title="Browse File">
                                    <i class="fw fw-file-browse" aria-hidden="true"></i>
                                    <span class="hidden-xs">Browse</span>
                                </button>
                            </div>
                            <div class="input-group-btn"></div>
                            <div class="input-group-btn">
                                <button class="btn btn-default btn-primary" type="button" title="Upload">
                                    <i class="fw fw-upload" aria-hidden="true"></i>
                                    <span class="hidden-xs">Upload</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <label for="input-name" class="control-label">Transfer</label>
                        <div class="form-inline">
                            <div class="form-group">
                                <div class="col-sm-12">
                                    <label class="sr-only" for="exampleInputAmount">Amount (in dollars)</label>
                                    <div class="input-group">
                                        <div class="input-group-addon">$</div>
                                        <input type="number" min="1" max="100000" class="form-control form-control-md" placeholder="Amount">
                                        <div class="input-group-addon">.00</div>
                                    </div>
                                </div>
                            </div>
                            <button type="submit" class="btn">Transfer cash</button>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <label class="control-label">Message</label>
                        <textarea class="form-control" rows="3"></textarea>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <label class="control-label">Choose</label>
                        <select class="form-control">
                            <option>1</option>
                            <option>2</option>
                            <option>3</option>
                            <option>4</option>
                            <option>5</option>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <label class="control-label">Read</label>
                        <p class="form-control-static">
                            Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                            Mauris egestas ligula eu aliquam sollicitudin.
                        </p>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <div class="checkbox">
                            <label>
                                <input type="checkbox"> Remember me
                            </label>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <button type="submit" class="btn btn-default btn-primary">Sign in</button>
                    </div>
                </div>
            </form>

            <br class="spacer">

            <!-- ******************************************************************
            Loading Style
            ******************************************************************* -->

            <div class="page-header" id="loading">
                <h1>Pre-loading Animation</h1>
            </div>

            <form class="form-horizontal" data-state="loading" data-loading-style="overlay">
                <div class="form-group">
                    <div class="col-sm-12">
                        <label for="input-name" class="control-label">Transfer</label>
                        <div class="form-inline">
                            <div class="form-group">
                                <div class="col-sm-12">
                                    <label class="sr-only" for="exampleInputAmount">Amount (in dollars)</label>
                                    <div class="input-group">
                                        <div class="input-group-addon">$</div>
                                        <input type="number" min="1" max="100000" class="form-control form-control-md" id="exampleInputAmount" placeholder="Amount">
                                        <div class="input-group-addon">.00</div>
                                    </div>
                                </div>
                            </div>
                            <button type="submit" class="btn">Transfer cash</button>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <label class="control-label">Message</label>
                        <textarea class="form-control" rows="3"></textarea>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-12">
                        <label class="control-label">Choose</label>
                        <select class="form-control">
                            <option>1</option>
                            <option>2</option>
                            <option>3</option>
                            <option>4</option>
                            <option>5</option>
                        </select>
                    </div>
                </div>
            </form>

            <div style="height:200px" data-state="loading" data-loading-text="Processing" data-loading-style="icon-only" data-loading-inverse="true"></div>


    </div>

</div><!-- /#page-content-wrapper -->


<!-- footer -->
<footer class="footer">
    <div class="container-fluid">
        <p>Theme WSO2 | &copy; <script>document.write(new Date().getFullYear());</script> <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i> Inc</a>. All Rights Reserved. | Powered by Twitter Bootstrap 3.3.5</p>
    </div>
</footer>

<!-- Jquery/Jquery UI JS -->
<script src="libs/jquery_1.11.0/jquery-1.11.3.min.js"></script>
<!-- Bootstrap JS -->
<script src="libs/bootstrap_3.2.0/js/bootstrap.min.js"></script>

<!-- Noty JS -->
<script src="libs/noty_2.3.5/packaged/jquery.noty.packaged.min.js"></script>

<script src="js/custom.js"></script>

</body>
</html>