package com.tm2ref.util;

import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

@WebFilter(filterName = "CorsFilter", servletNames = {"Faces Servlet"})
public class CorsFilter implements Filter
{

    private static String CORS_SOURCE_URL = null;
    
    private FilterConfig filterConfig = null;

    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.filterConfig = filterConfig;
    }

    public void destroy()
    {
        this.filterConfig = null;
    }
    
    private static synchronized void initFilter()
    {
        if( CORS_SOURCE_URL!=null )
            return;
        CORS_SOURCE_URL = RuntimeConstants.getStringValue("corssourceurl");
    }


    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
    {
        try
        {
            //LogService.logIt( "SetEndodingFilter.doFilter() " + ((HttpServletRequest)req).getRequestURI() + " req charset=" + req.getCharacterEncoding()  + ", resp charset=" + resp.getCharacterEncoding() );

            // this is REQUIRED otherwise the inbound request doesn't seem to be seen as UTF-8
            //req.setCharacterEncoding( "UTF-8" );

            //resp.setCharacterEncoding( "UTF-8" );

            HttpServletResponse r = (HttpServletResponse) resp;

            //r.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            //r.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            //r.setDateHeader("Expires", 0); // Proxies.

            String s = ((HttpServletRequest)req).getRequestURI();
            String method = ((HttpServletRequest)req).getMethod();
            if( (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("OPTIONS")) && !s.contains("/index.xhtml") && !s.contains("/r.xhtml") && !s.contains("/tools/admin/") )
            {
                if( CORS_SOURCE_URL==null )
                    initFilter();
                
                r.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                r.setHeader("Access-Control-Allow-Origin", CORS_SOURCE_URL);
                r.setHeader("Vary" , "Origin");
            }

            else
                r.setHeader("Access-Control-Allow-Origin", "*");
                
            // LogService.logIt( "SetEndodingFilter.doFilter() BBBBB AFTER req charset=" + req.getCharacterEncoding()  + ", resp charset=" + resp.getCharacterEncoding() );


            /*
            if( req instanceof HttpServletRequest && resp instanceof HttpServletResponse )
            {
                CookieUtils.setDefaultCookie( (HttpServletResponse) resp );
            }
            */

        }

        catch( Exception e )
        {
            LogService.logIt( e , "NONFATAL CorsFilter.doFilter() " );
        }

        chain.doFilter(req, resp);
    }
}

