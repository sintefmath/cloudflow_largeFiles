/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



var sessionToken = "abc"; // act as both username and token
var gssPath = "csuc://abc/subdir/";

if ( parent ) {
    
    // Look in parent for sessionToken as before...
}

function setToken(token) {
    var xhr = new XMLHttpRequest();
    xhr.onload = function(data) {
        console.log("token sat");
    }
    xhr.open("POST", "server/php/setCFvars.php", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send('sessionToken=' + sessionToken + "&gssPath=" + gssPath);
    console.log("Sent token");
}


setToken(sessionToken);




