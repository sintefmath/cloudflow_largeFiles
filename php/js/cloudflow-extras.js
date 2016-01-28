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
    if (typeof parent.hasCloudflowVariables !== 'undefined' && parent.hasCloudflowVariables) {
        console.log("Found CloudFlow variables");
        if (typeof parent.sessionToken !== 'undefined' && parent.sessionToken !== "") {
            sessionToken = parent.sessionToken;
            console.log("Found parent.sessionToken");
        }
        if (typeof parent.gssPath !== 'undefined' && parent.gssPath !== "") {
            gssPath = parent.gssPath;
            console.log("Found parent.gssPath: " + gssPath);
        }
        if (typeof parent.soapWFM !== 'undefined' && parent.soapWFM !== "") {
            soapWFM = parent.soapWFM;
            console.log("Found parent.soapWFM: " + soapWFM);
        }
        if (typeof parent.serviceID !== 'undefined' && parent.serviceID !== "") {
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


function done() {
    console.log("We should proceed to the next step now");

    var xmlOutputs_base64 = "";
    xmlOutputs = "<newFiles>Please close this tab and reload folder in the File Chooser</newFiles>";
    xmlOutputs_base64 = btoa(xmlOutputs);

    var namespace = "http://www.eu-cloudflow.eu/dfki/WorkflowManager2/";
    var messageName ="serviceExecutionFinished";
    var xmlToSend=  '<soapenv:Envelope '
            + 'xmlns:ns="' +  namespace + '" '
            + 'xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">' 
            + ' <soapenv:Header />'
            + ' <soapenv:Body>'
            // We assume we only have one relevant namespace...
            + "<ns:" + messageName + '>'
            + "<serviceID>" + serviceID + "</serviceID>"
            + "<sessionToken>" + sessionToken + "</sessionToken>"
            + "<serviceOutputs_base64>" + xmlOutputs_base64 + "</serviceOutputs_base64>"
            // ARGUMENTER HER <ns:serviceID>....</ns:serviceID>;
            + "</ns:" + messageName + ">"
            + ' </soapenv:Body>'
            + '</soapenv:Envelope>';
    var endpointURL = soapWFM.replace("?wsdl", "");
    var xhr = new XMLHttpRequest();
    xhr.onload = function(data) {
        console.log("Succesfully received a response from WFM");
    };
    xhr.open("POST", endpointURL, true);
    xhr.setRequestHeader("Content-Type", "text/xml");
    xhr.setRequestHeader("Accept", "text/xml");
    xhr.setRequestHeader("SOAPAction", "http://www.eu-cloudflow.eu/dfki/WorkflowManager2/serviceExecutionFinished");
    xhr.send(xmlToSend);
}



