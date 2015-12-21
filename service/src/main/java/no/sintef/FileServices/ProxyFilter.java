/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sintef.FileServices;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author kjetilo
 */
@WebFilter(filterName = "ProxyFilter", urlPatterns = {"/*"})
public class ProxyFilter implements Filter {
    
    private static final boolean debug = true;

    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;
    
    public ProxyFilter() {
    }    
    
    
    public void doFilter(final ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest req =  ((HttpServletRequest)request);

            String requestInformation = "req = " + req.getRequestURI() + "\n"
                     + "scheme = " +req.getScheme() + "\n"
                     + "hostname = " + req.getRemoteHost() + "\n"
                     + "Server name = " + req.getServerName() + "\n"
                     + "path info" + req.getPathInfo() + "\n"
                     + "Header names: " + req.getHeaderNames();
            log("ProxyFilter - request information: " + requestInformation);
            Enumeration<String> headerNames = req.getHeaderNames();
            String headerInfo = "";
        while (headerNames.hasMoreElements()) {
 
            String headerName = headerNames.nextElement();
            headerInfo = headerInfo + (headerName);
            headerInfo = headerInfo + "\n";
 
            Enumeration<String> headers = req.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String headerValue = headers.nextElement();
                headerInfo = headerInfo + "\t" + headerValue;
                headerInfo = headerInfo + "\n";
            }

        }
        log("headerFields = " + headerInfo + "\n\n");

           
           if (((HttpServletRequest)request).getRequestURI().contains("webresources")) {
                try {
                    chain.doFilter(request, response);
                } catch(Throwable t) {
                    log(t.getMessage());
                }
                return;
            }
        } catch(Throwable t) {
            log(t.getMessage());
        }
        
        // First we set the content type (unfortunately seems we have to do this here)
         log(response.getContentType());
         if (response.getContentType() != null) {
            ((HttpServletResponse)response).setHeader("Content-Type", response.getContentType().replace("ISO-8859-1", "UTF-8"));

        
            response.setContentType(response.getContentType().replace("ISO-8859-1", "UTF-8"));
         }
        ((HttpServletResponse)response).setCharacterEncoding("UTF-8");
        log(response.getContentType());
        
        PrintWriter out = response.getWriter();
        
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper((HttpServletResponse)response) {
            private CharArrayWriter output = new CharArrayWriter();
            
            @Override
            public String toString() {
                return output.toString();
            }
            
            @Override
            public ServletOutputStream getOutputStream()
                                    throws java.io.IOException {
                return new ServletOutputStream() {

                    @Override
                    public boolean isReady() {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void write(int b) throws IOException {
                        output.write(b);
                    }
                };
            }
            @Override
            public PrintWriter getWriter() {
                return new PrintWriter(output);
            }
        };
        
     
        Throwable problem = null;
        try {
            
            chain.doFilter(request, wrapper);
        } catch (Throwable t) {
            log("ProxyFilter: We have a problem");
	    // If an exception is thrown somewhere down the filter chain,
            // we still want to execute our after processing, and then
            // rethrow the problem after that.
            problem = t;
            t.printStackTrace();
        }
       
        // TODO: CloudFlow
        // This is the relevant part that might need to be changed.
        // The generated WSDL might not be aware that your web services are behind a forwarding proxy. 
        // This might cause the links inside your wsdl to be wrong.
        // This ProxyFilter class can modify all requests towards the endpoint of your webservice, and therefore fix the urls in your wsdl.
        
        // How do you detect the need to make changes in your wsdl? 
        //     Deploy your service so that it is reachable behind the proxy server, and access the wsdl through your browser.
        //     Check any urls that belongs to your web service ([...]?xsd=1 typically) and see if these urls are correct.
        // How do I fix them? 
        //     Modify the replace calls below to satisfy your needs.

        
        // Check if header contains "X-Real-IP"
        // If it does contain a X-Real-IP starting with something else than "10", the request is from outside world.
        // If there are no X-Real-IP, or if it starts with "10", the request is internal.
        String requestRealIP = ((HttpServletRequest)request).getHeader("X-Real-IP");
        boolean internalRequest = true;
        if (requestRealIP != null) {
            if ( ! (requestRealIP.startsWith("10") || requestRealIP.startsWith("172")) ) {
                internalRequest = false;
            }
        }
        log("Request header contains the following X-Real-IP field: " + requestRealIP);
        
        
        String newResponse = "";
        
        // If the request comes from internally, your wsdl can contain http://
        if ( internalRequest ) {
            newResponse = wrapper.toString()
		.replace("http://84.88.14.233:8080/", "https://cloudflow.csuc.cat/hpcservice/");
	    
            //      .replace("http://api.eu-cloudflow.eu:80/", "http://api.eu-cloudflow.eu:80/sintef")
	    //      .replace("http://api.eu-cloudflow.eu:443/", "http://api.eu-cloudflow.eu:443/sintef/")
	    //      .replace("http://api.eu-cloudflow.eu/", "http://api.eu-cloudflow.eu/sintef/");
            log("\n\t\t\tINTERNAL REQUEST!!!!\n\n");
	    log("New response: \n" + newResponse);
	    
        // If the request is external, your wsdl needs to use https:// protocol    
        } else {
            newResponse = wrapper.toString()
		.replace("http://84.88.14.233:8080/", "https://cloudflow.csuc.cat/hpcservice/");
                //.replace("http:")
            

            //      .replace("http://api.eu-cloudflow.eu:80/", "http://api.eu-cloudflow.eu:80/sintef/")
	    //      .replace("http://api.eu-cloudflow.eu:80/", "https://api.eu-cloudflow.eu:443/")
	    //      .replace("https://api.eu-cloudflow.eu:443/", "https://api.eu-cloudflow.eu:443/sintef/")
	    //      .replace("https://api.eu-cloudflow.eu/", "https://api.eu-cloudflow.eu/sintef/");
        }
        response.setContentLength(newResponse.getBytes().length);
        out.write(newResponse);
        out.close();

	// If there was a problem, we want to rethrow it if it is
        // a known type, otherwise log it.
        if (problem != null) {
            if (problem instanceof ServletException) {
                throw (ServletException) problem;
            }
            if (problem instanceof IOException) {
                throw (IOException) problem;
            }
            sendProcessingError(problem, response);
        }
    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter
     */
    public void destroy() {        
    }

    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {        
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {                
                log("ProxyFilter:Initializing filter");
            }
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("ProxyFilter()");
        }
        StringBuffer sb = new StringBuffer("ProxyFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }
    
    private void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);        
        
        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);                
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");                
                pw.print(stackTrace);                
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        }
    }
    
    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }
    
    public void log(String msg) {
        filterConfig.getServletContext().log(msg);        
    }
    
}
