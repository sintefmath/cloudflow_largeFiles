<?php

/**
 * This file provides the API for handling keystone authentication. Mostly you 
 * will use this file indirectly by including portal_authenticate.php
 * 
 * It provides the classes Token and Keystone, as well as the functions
 * keystone_check_login, keystone_logout and keystone_login.
 */
?>
<?php

// Define some constants that we need below. 
define("KEYSTONE_URL", "https://openstack.arctur.si/keystone/");

// Where to store the token id (ie. which cookie key)
define("CF_FC_TOKEN_KEY", "CF_FC_TOKEN");

require_once("keystone_admintoken.php");

/**
 * @brief Token describes the token obtained by Keystone. 
 */
class Token {

    private $token = null;
    private $expires = null;

    /**
     * Construct a new token
     * @param String $token the token id 
     * @param String $expires the expiration time given ( unix time )
     */
    public function __construct($token, $expires) {
        $this->token = $token;
        $this->expires = $expires;
    }

    /**
     * @return String
     */
    public function getToken() {
        return $this->token;
    }

    /**
     * Returns the expire time in unix time
     * @note this is not tested!
     */
    public function getExpireTime() {
        return $this->expires;
    }

}

/**
 * The Keystone class is used for interaction with the (Client) Keystone API.
 * 
 * This class implements a small portion of the API available at:
 * 
 * http://docs.openstack.org/api/openstack-identity-service/2.0/content/POST_authenticate-v2.0__v2.0_tokens_identity-auth-v2.html
 */
class Keystone {

    private $url = null;
    private $tenant = null;
    private $adminUrl = null;

    /**
     * 
     * @param String $url the base URL to the keystone service. Should not include
     *                    version numbers. So should typically look like:
     *                        http://server.com/keystone/
     */
    public function __construct($url) {
        $this->url = $url;
        $this->adminUrl = str_replace("/keystone/", "/keystone_admin/", $url);
        $this->tenant = "cloudflow";
    }

    /**
     * Generates a session token in exchange for username/password
     * @param String $username the username 
     * @param String $password the password
     * 
     * @return Token a token describing the session
     * 
     * @throws Exception if something bad happens (wrong username/password), 
     *                   connection issues, etc. 
     */
    public function login($username, $password) {

        // We use JSON for keystone. First we create a multiline string
        $loginData = <<<EOD
      { "auth": {
	  "tenantName": "$tenant",
	  "passwordCredentials" : {
	    "username" : "$username",
	    "password" : "$password"
	  }
	}
      }
EOD;
        // ^ Note that EOD must NOT be indented!
       
        
        // We decode the results. The 'true' options 
        // See http://www.php.net/manual/en/function.json-decode.php for a 
        // documentation.
        // 
        // The second argument tells json_decode that it should return a associate 
        // array (personal preference...)
        // 
        // $this->seondPostRequest is a utility function defined below. 
        $result = json_decode($this->sendPostRequest($loginData), true);

        // Here we are very picky about what to accept: It's better to authenticate
        // too few users than too many! So we only allow return values that look 
        // right. 
        // 
        // We expect the result to look like: 
        // 
        // {"access" : {
        //      "token": {
        //          "id" : "Some text string",
        //             ... 
        //       },
        //       ...
        //    }
        // }


        if (array_key_exists("access", $result) && array_key_exists("token", $result["access"]) && array_key_exists("id", $result["access"]["token"])) {
            // We set the default expiration time for one day 
            // @todo read the Keystone expiration time instead of hardcoding it
            return new Token($result['access']['token']['id'], time() + 60 * 60 * 24);
        } else {
            // It returned something else than we expected: 
            throw new Exception("Could not login.");
        }
    }


    /**
     * Checks if a user is logged in
     * 
     * @param String $token the token id to validate
     * @return boolean  true if the token is valid, false otherwise
     */
    public function isLoggedIn($token) {
        // Our strategy is to send a GET request to 
        // <keystone admin url>/v2.0/tokens/$token
        // This will generate a 203 og 200 if everything went OK,
        // ie. if it is a valid token.
        
        $curlHandle = curl_init();
        
        // Set the URL
        curl_setopt($curlHandle, CURLOPT_URL, 
		    "{$this->adminUrl}v2.0/tokens/$token");
	
	// We must set the admin token to validate tokens
	// the global variable KEYSTONE_ADMIN_TOKEN comes 
	// from the file keystone_admintoken.php
	curl_setopt($curlHandle, CURLOPT_HTTPHEADER, 
		    array("X-Auth-Token: " . KEYSTONE_ADMIN_TOKEN));
	// Make sure CURL doesn't print the result to stdout:
        curl_setopt($curlHandle, CURLOPT_RETURNTRANSFER, 1);
	// Execute
	curl_exec($curlHandle);
        // Get return code
        $returnCode = curl_getinfo($curlHandle, CURLINFO_HTTP_CODE);
        
        // Close the connection
        curl_close($curlHandle);
        
        // Keystone either returns 200 or 203 when everything is OK
        if ($returnCode != 200 && $returnCode != 203) {
            error_log("Wrong return code = $returnCode");
            return false;
        }
        return true;
    }

    
    
     /**
     * Obtain a user's username from its token.
     * 
     * @param String $token the token id to validate
     * @return username if valid token
     * 
     * @throws Exception if something bad happens (wrong username/password), 
     *                   connection issues, etc. 
     */
    public function getUsername($token) {
        // Our strategy is to send a GET request to 
        // <keystone admin url>/v2.0/tokens/$token
        // This will generate a 203 og 200 if everything went OK,
        // ie. if it is a valid token.
        
        $curlHandle = curl_init();
        
        // Set the URL
        curl_setopt($curlHandle, CURLOPT_URL, 
		    "{$this->adminUrl}v2.0/tokens/$token");
	
	// We must set the admin token to validate tokens
	// the global variable KEYSTONE_ADMIN_TOKEN comes 
	// from the file keystone_admintoken.php
	curl_setopt($curlHandle, CURLOPT_HTTPHEADER, 
		    array("X-Auth-Token: " . KEYSTONE_ADMIN_TOKEN));
	// Make sure CURL doesn't print the result to stdout:
        curl_setopt($curlHandle, CURLOPT_RETURNTRANSFER, 1);
	// Execute
	$result = curl_exec($curlHandle);
        // Get return code
        $returnCode = curl_getinfo($curlHandle, CURLINFO_HTTP_CODE);
        
        // Close the connection
        curl_close($curlHandle);
        
        // Keystone either returns 200 or 203 when everything is OK
        if ($returnCode != 200 && $returnCode != 203) {
            error_log("Wrong return code = $returnCode");
            throw new Exception("Invalid return code when obtaining username from keystone: " . $returnCode);
        }
        
        if (array_key_exists("access", $result) && array_key_exists("user", $result["access"]) && array_key_exists("username", $result["access"]["user"])) {
            // We set the default expiration time for one day 
            // @todo read the Keystone expiration time instead of hardcoding it
            return $result['access']['user']['username'];
        } else {
            // It returned something else than we expected: 
            throw new Exception("Not valid token.");
        }
    }
    
    /**
     * A small private utility function for issuing HTTP requests to the 
     * Keystone servic
     * 
     * 
     * @param String $data the data to send (must typically be in JSON)
     * @return String the data returned from the server (will be in JSON)
     * @throws Exception if the connection returned a code other than 200 or 203
     */
    private function sendPostRequest($data) {
        // We use the CURL API for this, see 
        // http://www.php.net/manual/en/book.curl.php for a description. 
        // The curl api for PHP is basically a wrapper around a C-api, so 
        // everything is a bit C-ish:
        $curlHandle = curl_init();
        
        // We are only sending requests to the token part for now
        curl_setopt($curlHandle, CURLOPT_URL, "{$this->url}v2.0/tokens");
        
        // We are going to send data to keystone
        curl_setopt($curlHandle, CURLOPT_POST, true);
        
        // Include the data
        curl_setopt($curlHandle, CURLOPT_POSTFIELDS, $data);
        
        // Tell keystone what we send (content-type) and what we expect in 
        // return (accept)
        curl_setopt($curlHandle, CURLOPT_HTTPHEADER, 
                array('Content-Type: application/json', 
                    'Accept: application/json'));
        
        
        // This makes CURL return the output of the request when calling curl_exec
        curl_setopt($curlHandle, CURLOPT_RETURNTRANSFER, 1);

        // Now $result will include the body of the returned data from keystone
        $result = curl_exec($curlHandle);

        // Get return code
        $returnCode = curl_getinfo($curlHandle, CURLINFO_HTTP_CODE);
        
        // Close the connection
        curl_close($curlHandle);
        
        // Keystone either returns 200 or 203 when everything is OK
        if ($returnCode != 200 && $returnCode != 203) {
            error_log("Wrong return code = $returnCode");
            throw new Exception("Bad response " . $returnCode);
        }
        return $result;
    }

}

/**
 * Checks the  token id reciding in CF_FC_TOKEN_KEY against the keystone 
 * service
 * 
 * @return boolean true if the user is logged in, false otherwise
 */
function keystone_check_login() {
    
    // If the token is not in the cookies the user is not logged in:
    if (!array_key_exists(CF_FC_TOKEN_KEY, $_COOKIE)) {
        return false;
    }

    // Get the token 
    $token = $_COOKIE[CF_FC_TOKEN_KEY];
    
    $keystone = new Keystone(KEYSTONE_URL);
    
    // And just call the API:  
    return $keystone->isLoggedIn($token);
}

/**
 * Checks the token id given as input (already read from cookie) against the
 * keystone service
 * 
 * @return username of the user related to the token
 */
function keystone_get_username($token) {
    $keystone = new Keystone(KEYSTONE_URL);
    return $keystone->getUsername($token);
}
 
/**
 * Logs the user in and sets the relevant cookie fields.
 * @param String $username
 * @param String $password
 * @return boolean true if the system was able to log the user in, false 
 *                 otherwise.
 */
function keystone_login($username, $password) {
    $keystone = new Keystone(KEYSTONE_URL);

    try {
        // Try to lgo in
        $token = $keystone->login($username, $password);
    } catch (Exception $e) {
        return false;
    }

    // Set the token id in the cookies for now.
    // @todo Find a better way of storing the CF_TOKEN
    // @todo make this more restrictive (ie. not seen by JS, only on portal-page, 
    //       not subpages. 
    return setcookie(CF_FC_TOKEN_KEY, $token->getToken(), $token->getExpireTime());
}

/**
 * Logs the user out.
 */
function keystone_logout() {
    // Just set the CF_FC_TOKEN_KEY cookie to an empty value 
    // and with a experation time some time in the past: 
    setcookie(CF_FC_TOKEN_KEY, "", time()-3600);
}
?>
