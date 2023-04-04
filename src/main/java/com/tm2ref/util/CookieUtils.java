package com.tm2ref.util;

import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



/**
 * variety of static convenience methods for working with cookies.
 */
public class CookieUtils
{
    public static String CORPID_COOKIE_NAME = "tm2refcorpid";

    public static String ACCESSCODE_COOKIE_NAME = "tm2refcheckid";

    public static boolean getAreCookiesSupported( HttpServletRequest request )
    {

        Cookie[] cookies = request.getCookies();

        if( cookies != null && cookies.length > 0 )
            return true;

        return false;
    }


    public static void setDefaultCookie( HttpServletResponse response, String corpIdEnc )
    {
        if( response==null )
            return;
        
        setCookie(  response ,
                    CORPID_COOKIE_NAME ,
                    corpIdEnc ,
                    "/tr/" ,
                    60*60*24*1     // 1 day.
                   );
    }

    public static void setRcCheckCookie( HttpServletResponse response, String rcCheckIdEnc )
    {
        if( response==null )
            return;
        
        setCookie(response ,
                    ACCESSCODE_COOKIE_NAME ,
                    rcCheckIdEnc ,
                    "/tr/" ,
                    60*60*3     // 180 mins.  // 60*60*24*1     // 1 day.
                   );
    }

    public static void removeRcCheckCookie( HttpServletResponse response )
    {
        if( response==null )
            return;
        
        setCookie(response ,
                    ACCESSCODE_COOKIE_NAME ,
                    "" ,
                    "/tr/" ,
                    0     
                   );
    }


    /**
     * Returns the cookie requested or null
     */
    public static Cookie getCookie( HttpServletRequest request ,
                                    String cookieName )
    {
        try
        {
            if( request==null )
                return null;
            
            // get the cookies
            Cookie[] cookies = request.getCookies();

            // if cookies found
            if( cookies != null )
            {
                for( int i=0; i< cookies.length ; i++ )
                {
                    if( cookies[i].getName().equals( cookieName )  )
                        return cookies[i];
                }
            }
        }
        
        catch( Exception e )
        {
            LogService.logIt( "CookieUtils.getCookie() NONFATAL. Returining null. " + e.toString() );
        }

        return null;
    }



    /**
     * Sets a cookie
     */
    public static void setCookie( HttpServletResponse response ,
                                  String name ,
                                  String value ,
                                  String path ,
                                  int maxAge // seconds
                                 )
    {
        try
        {
            if( response==null )
            {
                LogService.logIt( "CookieUtils.setCookie() NON-FATAL Response not present. Cannot set cookie: name=" + name + ", path=" + path + ", " + (response==null ? "response is null" : " Not null." ) );
                return;
            }
            
            Cookie cookie = new Cookie( name , value );

            if( path != null )
                cookie.setPath( path );

            cookie.setMaxAge( maxAge );

            cookie.setAttribute("SameSite", "STRICT");
            
            if( RuntimeConstants.getHttpsOnly() )
                cookie.setSecure(true);
            
            cookie.setHttpOnly(true);
            
            // place in response
            response.addCookie( cookie );
        }
        catch( Exception e )
        {
            LogService.logIt( "CookieUtils.setCookie() NON-FATAL " + e.toString()  );
        }
    }

}