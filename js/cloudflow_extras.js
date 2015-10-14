/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



var sessionToken = "abc";

if ( parent ) {
    
    // Look in parent for sessionToken as before...
}

function setToken(token) {
    var xhr = new XMLHttpRequest();
    xhr.onload = function(data) {
        console.log("token sat");
    }
    xhr.open("POST", "../server/php/setToken.php" + token, true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send('sessionToken=' + sessionToken);
    console.log("Sent token");
}


setToken(sessionToken);




