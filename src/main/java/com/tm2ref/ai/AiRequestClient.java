/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ai;

import com.tm2ref.service.LogService;
import com.tm2ref.util.JsonUtils;
import jakarta.json.JsonObject;


public class AiRequestClient extends BaseAiClient 
{
    public JsonObject getJsonObjectFromAiCallRequest( JsonObject joReq ) throws Exception
    {
        String payload = JsonUtils.convertJsonObjectToString(joReq);
        
        return getJsonObjectFromAiCallRequest( payload );        
    }
    
    public JsonObject getJsonObjectFromAiCallRequest( String payload ) throws Exception
    {
        try
        {
            init();
            
            String jsonOut = postAiCallRequest( payload  ); //  sendApiPost(url, payload, paramMap, getBaseCreds() );
            
            if( jsonOut!=null && !jsonOut.isBlank() )
            {
                JsonObject jo = JsonUtils.convertJsonStringToObject(jsonOut);
                return jo;
            }

            LogService.logIt("AiRequestClient.getJsonObjectFromAiCallRequest() BBB.1 No Json Object returned from aiCall. Returning null. Json=" + payload );            
            return null;
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "AiRequestClient.getJsonFromAiCallRequest() XXX.1 Json=" + payload );
            throw e;
        }
    }

    
    public String postAiCallRequest( String jsonStr ) throws Exception
    {
        try
        {
            init();
            
            String url = BASE_URI + "airequest";
            
            //Map<String,String> paramMap = new HashMap<>();
            //paramMap.put( "tran", tran );            
            return sendApiPost(url, jsonStr, getBaseCreds() );
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "AiRequestClient.postAiCallRequest() XXX.1  Json=" + jsonStr );
            throw e;
        }
    }
    
}
