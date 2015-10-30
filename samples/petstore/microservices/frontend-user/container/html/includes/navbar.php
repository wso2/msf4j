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
                <a class="navbar-menu-toggle" data-toggle="dropdown">
                        <span class="icon fw-stack">
                            <i class="fw fw-tiles fw-stack-1x"></i>
                        </span>
                </a>
                <ul class="dropdown-menu tiles arrow dark add-margin-1x" role="menu">
                    <li>
                        <a href="pet-types.php">
                            <img src="images/list-pet-type.png" class="invert-png">
                            <span class="name">Pet Types</span>
                        </a>
                    </li>
                    <li>
                        <a href="pets.php">
                            <img src="images/list-pets.png" class="invert-png">
                            <span class="name">Pets</span>
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
                <?php
                if(isset($action_buttons)){
                ?>
                <ul class="nav navbar-nav">
                    <?php
                    if($action_buttons) {
                        ?>
                        <li>
                            <a href="layout.html">
                                <span class="icon fw-stack">
                                    <i class="fw fw-laptop fw-stack-1x"></i>
                                    <i class="fw fw-ring fw-stack-2x"></i>
                                </span>
                                Main Wrapper
                            </a>
                        </li>
                        <?php
                    }
                    ?>
                </ul>
                <?php
                }
                ?>

            </div><!--/.nav-collapse -->
        </div>
    </nav>
</div>