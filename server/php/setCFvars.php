<?php

try {
    $token = $_POST["sessionToken"];
    if(setcookie("CF_TOKEN", $token, time()+3600)) {
        error_log("CF_TOKEN cookie sat to " .$_COOKIE["CF_TOKEN"]. ", while token is " .$token);
    }
    else {
        error_log("setCFvars failed for token...");
    }
    
    $gssPath = $_POST["gssPath"];
    if(setcookie("CF_GSS_PATH". $gssPath, time()+3600)) {
        error_log("CF_GSS_PATH cookie is sat to " . $_COOKIE["CF_GSS_PATH"] . ", while gssPath is ". $gssPath);
    }
    else {
        error_log("setCFvars failed for gssPath...");
    }
    
} catch (Exception $e) {
    echo $e;
}


?>