/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ai;

import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import com.tm2ref.util.Base64Encoder;
import com.tm2ref.util.HttpUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;


public class BaseAiClient {

    public static int AI_CALL_TIMEOUT_SHORT = 10;
    public static int AI_CALL_TIMEOUT = 36;
    public static int AI_CALL_TIMEOUT_LONG = 180;
    protected static String BASE_URI = null; // "http://localhost:80/ai/webresources";


    protected synchronized void init()
    {
        if( BASE_URI == null )
            BASE_URI = RuntimeConstants.getStringValue( "tm2ai_rest_api_baseuri" );
    }


    protected String[] getBaseCreds()
    {
        return new String[] { RuntimeConstants.getStringValue( "tm2ai_rest_api_username"),  RuntimeConstants.getStringValue( "tm2ai_rest_api_password") };
    }

    public String sendApiPost( String url, String payload, String[] creds, int timeoutSecs) throws Exception
    {
        try
        {
            init();

            return sendApiPostCore(url,  payload, creds, timeoutSecs>0 ? timeoutSecs : AI_CALL_TIMEOUT  );

            //StringBuilder sb = processAPIResponse( r );
            //return sb.toString();
        }
        catch( STException e )
        {
            throw e;
        }
        catch( IOException e )
        {
            LogService.logIt( "BaseAiClient.sendApiPost() " + e.toString() + " url=" + url + ", payload=" + payload );
            throw new STException(e);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseAiClient.sendApiPost() url=" + url + ", payload=" + payload );
            throw new STException(e);
        }
    }



    public String sendApiPostCore( String url, String payload, String[] creds, int timeoutSecs) throws Exception
    {
        // CloseableHttpResponse r = null;
        // int statusCode = 0;

        timeoutSecs = timeoutSecs>0 ? timeoutSecs : AI_CALL_TIMEOUT;
        
        try (CloseableHttpClient client = HttpUtils.getHttpClient(timeoutSecs))
        {
            init();

            HttpPost post;


            // LogService.logIt( "BaseAiClient.sendApiPostCore() Preparing Request. url=" + url );

            post = new HttpPost( url );

            post.setEntity( new StringEntity( payload ) );

            //int length = payload == null ? 0 : payload.length();

            //post.setHeader( "Content-Length", length + "" );
            String[] mimes = getRequestResponseContentType();
            post.addHeader( "Content-Type", mimes[0] );
            post.addHeader( "Accept", mimes[1] );

            Map<String,String> basicAuthCreds = getBasicAuthParmsForResultsPost( creds[0], creds[1] );

            if( basicAuthCreds != null && basicAuthCreds.size()>= 2 )
            {
                String un = basicAuthCreds.get( "username" );
                String pwd = basicAuthCreds.get( "password" );

                String b6 = Base64Encoder.encodeString( un + ":" + pwd );
                post.setHeader( "Authorization", "Basic " + b6  );
                // LogService.logIt( "BaseAiClient.sendApiPostCore() Set basic Auth header: Basic " + b6);
            }

            /*
            if( 1==2 && paramMap!=null && !paramMap.isEmpty() )
            {
                List<NameValuePair> params = new ArrayList<>();

                for( String key : paramMap.keySet() )
                {
                    if( paramMap.get(key) != null && !paramMap.get(key).isEmpty() )
                        params.add(new BasicNameValuePair(key, paramMap.get(key)));
                }

                post.addHeader( "Content-Type", "application/x-www-form-urlencoded; charset=utf-8" );
                post.setEntity(new UrlEncodedFormEntity(params, Charset.forName("UTF-8")));
            }
            */


            // LogService.logIt( "BaseAiClient.sendApiPostCore() Executing Request" );
            String s = client.execute( post, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "BaseAiClient.sendApiPostCore() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "BaseAiClient.sendApiPostCore() statusCode="+ status + ", response=" + ss );
                    return ss;
                    }  );

            return s;
        }
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt("BaseAiClient.sendApiPostCore() STERR " + e.toString() + ", url=" + url + ", payload=" + (payload==null ? "null" : payload ) );
            throw new STException(e);
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseAiClient.sendApiPostCore() " );
            throw new STException(e);
        }

    }

    public String[] getRequestResponseContentType()
    {
        return new String[]{"application/json","application/json"};
    }

    public Map<String,String>  getBasicAuthParmsForResultsPost( String username, String pwd )
    {
        Map<String,String> out = new HashMap<>();
        out.put( "username", username );
        out.put( "password", pwd );
        return out;
    }
        
}
