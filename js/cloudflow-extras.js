/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



var sessionToken = "abc"; // act as both username and token
var gssPath = "csuc://abc/subdir/";
var soapWFM = "dinada";
var serviceID = "-1";

if ( parent ) {
    if (parent.hasCloudflowVariables !== 'undefined' && parent.hasCloudflowVariables) {
        console.log("Found CloudFlow variables");
        if (parent.sessionToken !== 'undefined' || parent.sessionToken !== "") {
            sessionToken = parent.sessionToken;
            console.log("Found parent.sessionToken");
        }
        if (parent.gssPath !== 'undefined' || parent.gssPath !== "") {
            gssPath = parent.gssPath;
            console.log("Found parent.gssPath: " + gssPath);
        }
        if (parent.soapWFM !== 'undefined' || parent.soapWFM !== "") {
            soapWFM = parent.soapWFM;
            console.log("Found parent.soapWFM: " + soapWFM);
        }
        if (parent.serviceID !== 'undefined' || parent.serviceID !== "") {
            serviceID = parent.serviceID;
            console.log("Found parent.serviceID: " + serviceID);
        }
    }
    else {
        console.log("No CloudFlow variables in sight");
    }
    // Look in parent for sessionToken as before...
}

function setCookies() {
    var xhr = new XMLHttpRequest();
    xhr.onload = function(data) {
        console.log("Cookies sat");
    }
    xhr.open("POST", "server/php/setCFvars.php", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send('sessionToken=' + sessionToken + "&gssPath=" + gssPath);
    console.log("Sent cookies");
}


setCookies();




