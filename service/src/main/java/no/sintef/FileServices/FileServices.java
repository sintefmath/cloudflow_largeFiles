package no.sintef.FileServices;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;


/**
 *
 * @author havahol
 */


// TODO: Make a pretty name for your web service endpoint
@WebService(serviceName = "FileServices")

public class FileServices {

    // TODO:
    // The namespace should match the package name in line 1. 
    // If package name is a.b.c, the namespace should be "http://c.b.a/" (casae sensitive)
    // WFM will have an easier time recognizing your web service if this is fulfilled
    private final String namespace = "http://FileServices.sintef.no/";
    
    @Resource
    private WebServiceContext context;

    
    // TODO: Make a pretty name for the web service
    @WebMethod(operationName = "largeFile")
    public void largeFile(@WebParam(name="serviceID", targetNamespace=namespace, mode=WebParam.Mode.IN) String serviceID, 
            @WebParam(name="sessionToken", targetNamespace=namespace, mode=WebParam.Mode.IN) String sessionToken,
            @WebParam(name="extraParameters", targetNamespace=namespace, mode=WebParam.Mode.IN) String extraParameters,
            @WebParam(name="folder", targetNamespace=namespace, mode=WebParam.Mode.IN) String folder,
            @WebParam(name="newFiles", targetNamespace=namespace, mode=WebParam.Mode.OUT) Holder<String> newFiles,
            @WebParam(name="status_base64", targetNamespace=namespace, mode=WebParam.Mode.OUT) Holder<String> status_base64
            ) {

        // serviceID, sessionToken and status_base64 is manditory input to a Cloudflow Application.
        
        // TODO: Feel free to add more input parameters   
        // TODO: Feel free to add more output parameters (they must be Holder<T> objects
        
        // TODO: url should point to the web pages you want to include (should be available under "https://api.eu-cloudflow.eu/[...]"
        String url = "https://bigfileservice.csuc.cat/cf-large-files/cloudflow.html";
        
        
        // Parsing of extraParameters needed to contact WFM
        String WFM = getExtraParameter(extraParameters, "WFM");
        WFM = WFM.replace("?wsdl", ""); // This parameter has not yet been fully integrated, but in this service we manage without the "?wsdl" part
        
        String containingHtml = "<html><head><title>Distance viewer</title>\n<style>html, body  {\n" +
"                height: 100%;\n" +
"                overflow: hidden;\n" +
"            }\n" +
"            html, body, iframe {\n" +
"                margin: 0;\n" +
"                padding: 0;\n" +
"            }\n</style>"
                +"<script type='text/javascript'>"
                // Set the variables so that the HTML page in the iframe will have access to them through parent
                // TODO: add your extra input parameters here javascript variables here
                + "sessionToken = \"" + sessionToken + "\";\n"
                + "serviceID = \"" + serviceID + "\";\n"
                + "WFM = \"" + WFM + "\";\n"
                + "hasCloudflowVariables = true;\n"
                + "</script>\n"
                + "</head>\n"
                + "<body>\n"
                +"<iframe src='" + url + "' width='100%' height='100%' seamless='1' style='height: 100%; width: 100%;border-style: none; overflow: hidden;'>\n"
                +"</body>\n"
                + "</html>";
        
        status_base64.value = DatatypeConverter.printBase64Binary(containingHtml.getBytes());
        
        // TODO: If your web application service contains output parameters other than "status_base64", you should make sure they are not null
        // when this method returns. This value will be ignored by WFM, since WFM will expect the proper output values when you call WFM's "serviceExecutionFinish" service
        newFiles.value = "UNSET"; // if outputValue1 is String
        // outputValue2.value = 0;  // if outputValue2 is int/float/double
        
        
        // TODO: Check ProxyFilter.java line 153 ;)
    }

    
    // TODO: Make a pretty name for the web service
    @WebMethod(operationName = "fileChooser")
    public void fileChooser(@WebParam(name="serviceID", targetNamespace=namespace, mode=WebParam.Mode.IN) String serviceID, 
            @WebParam(name="sessionToken", targetNamespace=namespace, mode=WebParam.Mode.IN) String sessionToken,
            @WebParam(name="extraParameters", targetNamespace=namespace, mode=WebParam.Mode.IN) String extraParameters,
            @WebParam(name="description", targetNamespace=namespace, mode=WebParam.Mode.IN) String description,
            @WebParam(name="filter", targetNamespace=namespace, mode=WebParam.Mode.IN) String filter,
            @WebParam(name="fileSelected", targetNamespace=namespace, mode=WebParam.Mode.OUT) Holder<String> fileSelected,
            @WebParam(name="status_base64", targetNamespace=namespace, mode=WebParam.Mode.OUT) Holder<String> status_base64
            ) {
        
        status_base64.value = "UNSET";
        fileSelected.value = "UNSET";
    }
    

    // Function for parsing the extraParameters input 
    private String getExtraParameter(String extraParameters, String key) {
        String[] map = extraParameters.split(",");
        for (String pair : map) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 ) {
                //log("::getExtraParameter - found {" + keyValue[0] + ", " + keyValue[1] + "}");
                if (keyValue[0].equals(key)) {
                    return keyValue[1];
                }
            }
            else {
                String wierd = "Something is wrong with the extraParameters! Found key-value pair of length: " + keyValue.length + "\n{";
                for (String str : keyValue) {
                    wierd += keyValue + str + ", ";
                }
                log(wierd);
            }
        }
        return "notFound";
    }    
    
    // Logging into the GlassFish log 
    // 
    private void log(String message) {
        Logger.getLogger(this.getClass().toString()).log(Level.INFO, this.getClass().getSimpleName() + "::" + message);
    }
    
    
}
