<?php

try {
    $token = $_POST["sessionToken"];
    if(setcookie("CF_TOKEN", $token, time()+3600)) {
        error_log("CF_TOKEN cookie sat to " .$_COOKIE["CF_TOKEN"]. ", while token is " .$token);
    }
    else {
        error_log("setToken failed...");
    }
} catch (Exception $e) {
    echo $e;
}


?>