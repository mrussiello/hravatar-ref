/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.faces;

import com.tm2ref.service.LogService;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author miker_000
 */
public class HttpReqUtils {
    
    public static String getClientIpAddress( HttpServletRequest req )
    {
        if( req==null )
            return null;
        
        String h = null;
        
        try
        {
            h = req.getHeader( "X-Forwarded-For" );

            //if( h!=null && !h.isEmpty() )
            //    return h;

            if( h==null || h.isEmpty() )
                h = req.getHeader( "x-forwarded-for" );

            if( h!=null && !h.isEmpty() )
            {
                if( !h.contains(",") )
                    return h.trim();
                
                return h.substring(0,h.indexOf(",")).trim();
            }  

            return req.getRemoteAddr();        
        }
        catch( Exception e )
        {
            LogService.logIt( e, "HttpReqUtils.getClientIpAddress() h=" + h );            
            return null;
        }
        
    }

    public static String getRequestProtocol( HttpServletRequest req )
    {
        if( req==null )
            return null;
        
        String h = null;
        
        try
        {
            h = req.getHeader( "X-Forwarded-Proto" );

            if( h==null || h.isEmpty() )
                h = req.getHeader( "x-forwarded-proto" );

            if( h==null || h.isEmpty() )
                h = req.getScheme();

            if( h==null || h.isEmpty() )
            {
                int port = req.getLocalPort();
                return port == 443 ? "https" : "http";                        
            }

            h = h.toLowerCase();

            return h.contains( "https" ) ? "https" : "http";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "HttpReqUtils.getRequestProtocol() h=" + h );            
            return null;
        }
    }

}
