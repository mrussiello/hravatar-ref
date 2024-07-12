/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ws;

import com.tm2ref.entity.user.User;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.Base64Encoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author miker_000
 */
public class BaseApiResource {
    
    UserFacade userFacade;
    User authUser;
    String tran = null;
    SimpleDateFormat dateFormat;
    
    
    protected String getHeader( String name, HttpHeaders headers )
    {
        return headers.getRequestHeaders().getFirst(name);
    }
    
    protected void authenticateRequest( HttpHeaders headers ) throws ApiException
    {
        try
        {
            // Get the header
            String authHeader = getHeader( "Authorization", headers );

            LogService.logIt( "BaseApiResource.authenticateRequest() Encrypted Header=" + authHeader );

            if( authHeader==null || authHeader.isBlank() )
                throw new ApiException( "No Authorization Header", 100, Response.Status.UNAUTHORIZED.getStatusCode() );

            if( !authHeader.startsWith("Basic " ) )
                throw new ApiException( "Authorization Header format appears invalid. Should start with Basic", 101, Response.Status.UNAUTHORIZED.getStatusCode() );

            String key = authHeader.substring( 6, authHeader.length() );

            if( key==null || key.isBlank() )
                throw new ApiException( "No key found with Basic token" + key, 102, Response.Status.UNAUTHORIZED.getStatusCode() );

            String key2 = Base64Encoder.decodeString(key);

            if( !key2.contains( ":" ) )
            {
                LogService.logIt( "BaseApiResource.authenticateRequest() Unable to parse decoded key: " + key2 );
                throw new ApiException( "Authorization Header Format Invalid.", 103, Response.Status.UNAUTHORIZED.getStatusCode() );
            }

            String username = key2.substring(0, key2.indexOf(":") );
            String pwd = key2.substring( key2.indexOf(":")+1, key2.length() );

            if( username!=null )
                username=username.trim();
            if( pwd!=null )
                pwd=pwd.trim();
            
            if( checkCreds(username, pwd) )
            {
                return;
            }
            
            if( userFacade==null )
                userFacade = UserFacade.getInstance();

            User user = userFacade.getUserByLogonInfo( username, pwd );

            if( user==null )
            {
                User user2 = userFacade.getUserByUsername(username);

                LogService.logIt( "BaseApiResource.authenticateRequest() Unable to find user for username=" + username + ", pwd=" + pwd  + ", username valid=" + (user2!=null) );
                throw new ApiException( "Authentication Failed.", 104, Response.Status.UNAUTHORIZED.getStatusCode() );            
            }

            if( !user.getRoleType().getIsAuthorizedForApi() )
            {
                LogService.logIt( "BaseApiResource.authenticateRequest() user not authorized for API. userId=" + user.getUserId()  );
                throw new ApiException( "Authentication Failed.", 105, Response.Status.UNAUTHORIZED.getStatusCode() );                        
            }

            authUser = user;     
        }
        catch( ApiException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseApiResource.authenticateRequest() ");
            throw new ApiException( "Unknown System Authentication Error: " + e.getMessage(), 0, Response.Status.UNAUTHORIZED.getStatusCode() );
        }
    }
    
    protected boolean checkCreds( String username, String password ) 
    {
        try
        {
            // LogService.logIt( "BaseBuilderRest.checkCreds() un=" + username + ", pw=" + password );
            if( username==null || username.isBlank())
                return false;
            if( password==null || password.isBlank() )
                return false;

            username=username.trim();
            password=password.trim();
            
            if( !username.equalsIgnoreCase( RuntimeConstants.getStringValue("tm2ref_rest_api_username").trim()))
                return false;
            
            if( !password.equals(  RuntimeConstants.getStringValue("tm2ref_rest_api_password").trim() ) )
                return false;
                        
            return true;
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseApiResource.checkCreds() " );            
            return false;
        }
    }
    
    
    
    protected String getJoString( JsonObject j, String key )
    {        
        return j!=null && key!=null && !key.isBlank() && j.containsKey( key ) && !j.isNull( key ) ? j.getString( key ) : null;
    }

    protected int getJoInt( JsonObject j, String key, int defaultVal )
    {        
        return j!=null && key!=null && !key.isBlank() && j.containsKey( key ) ? j.getInt( key ) : defaultVal;
    }


    protected String getDateTimeStr( Date d )
    {
        if( d==null )
            return null;
        
        getDateFormat();
        
        try
        {
            return dateFormat.format(d);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseApiResource.getDateTimeStr() d=" + d );
            return null;
        }
    }
    
    protected Date getJoDateTime( JsonObject j, String key )
    {
        String ds = getJoString( j, key );
        
        if( ds==null || ds.isBlank() )
            return null;
        
        getDateFormat();
        
        try
        {
            return dateFormat.parse(ds);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseApiResource.getJoDateTime() ds=" + ds );
            return null;
        }
    }
    
    protected SimpleDateFormat getDateFormat()
    {
        if( dateFormat==null )
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateFormat;
    }
    
    protected int getOrgId()
    {
        return authUser==null ? 0 : authUser.getOrgId();
    }
    
    protected void sendErrorEmail( String subject, String content )
    {
        String subj = subject!=null && !subject.isBlank() ? subject : "HRA Score REST Error";  
        EmailUtils eu = new EmailUtils();
        eu.sendEmailToAdmin(subj, content );
    }
    
    

    
}
