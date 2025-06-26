/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.ai;

import com.tm2ref.entity.user.Resume;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.util.JsonUtils;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author miker
 */
public class AiRequestUtils
{
    private static Boolean AI_SYSTEM_AVAILABLE;
    private static Date AI_CHECK_DATE;
    
    public static synchronized void resetAiSystemAvailable()
    {
        AI_CHECK_DATE=null;
        AI_SYSTEM_AVAILABLE=null;
        getIsAiSystemAvailable();
    }

    public static synchronized void checkAiSystemAvailable()
    {
        //if( AI_SYSTEM_AVAILABLE!=null  )
        //    return;

        if( !RuntimeConstants.getBooleanValue( "tm2ai_rest_api_ok") )
        {
            AI_SYSTEM_AVAILABLE = false;
            return;
        }

        AI_CHECK_DATE = new Date();
                        
        AiCallType aiCallType = AiCallType.TEST_CONNECT;
            
        try
        {
            JsonObjectBuilder job = getBasePayloadJsonObjectBuilder(aiCallType, null );

            JsonObject joReq = job.build();

            AiRequestClient client = new AiRequestClient();

            JsonObject jo = client.getJsonObjectFromAiCallRequest( joReq, BaseAiClient.AI_CALL_TIMEOUT_SHORT );
            if( jo==null || !jo.containsKey("status") || jo.isNull("status") )
            {
                LogService.logIt("AiRequestUtils.getIsAiSystemAvailable() response JO is null. " );
                AI_SYSTEM_AVAILABLE = false;
                return;
            }
            if( !jo.containsKey("status") || jo.isNull("status") )
            {
                LogService.logIt("AiRequestUtils.getIsAiSystemAvailable() JO Status is present. " + JsonUtils.convertJsonObjectToString(jo) );
                AI_SYSTEM_AVAILABLE = false;
                return;
            }

            String status = JsonUtils.getStringFmJson(jo, "status" );
            if( status==null ||!status.equalsIgnoreCase("complete") )
            {
                LogService.logIt("AiRequestUtils.getIsAiSystemAvailable() JO Status is not complete. " + JsonUtils.convertJsonObjectToString(jo) );
                AI_SYSTEM_AVAILABLE = false;
                return;
            }
            
            AI_SYSTEM_AVAILABLE = true;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AiRequestUtils.getIsAiSystemAvailable() aiCallType=" +  aiCallType.getName() );
            AI_SYSTEM_AVAILABLE = false;
        }        
    }
    
    public static boolean getIsAiSystemAvailable()
    {
        if( AI_CHECK_DATE!=null )
        {
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.HOUR_OF_DAY, -1 );
            
            if( AI_CHECK_DATE.before( cal.getTime() ) )
                checkAiSystemAvailable();
        }
        
        if( AI_SYSTEM_AVAILABLE==null  )
            checkAiSystemAvailable();
                        
        return AI_SYSTEM_AVAILABLE;
    }
    
    public static JsonObject parseResume( Resume resume, User user, boolean autoUpdate) throws Exception
    {
        return doResumeParsingCall(resume, user, AiCallType.RESUME_PARSE, autoUpdate );
    }
        
    public static JsonObject doResumeParsingCall( Resume resume, User user, AiCallType aiCallType, boolean autoUpdate) throws Exception
    {
        try
        {
            if( resume==null )
                throw new Exception( "Resume is null" );

            if( (resume.getUploadedText()==null || resume.getUploadedText().isBlank()) ) //  && (resume.getPlainText()==null || resume.getPlainText().isBlank()) )
                throw new Exception( "Resume does not have enough info for a Parsing Call" );

            JsonObjectBuilder job = getBasePayloadJsonObjectBuilder(aiCallType, user );

            String textToUse = resume.getUploadedText();
            //if( textToUse==null || textToUse.isBlank() )
            //    textToUse=resume.getPlainText();
            
            if( textToUse==null || textToUse.isBlank() )
                throw new Exception( "No TextToUse to send to Resume Parser." );
            
            job.add("resumeid", resume.getResumeId() );
            job.add("autoupdate", autoUpdate ? 1 : 0 );
            // job.add("strparam1", textToUse );

            JsonObject joReq = job.build();

            AiRequestClient client = new AiRequestClient();

            Tracker.addAiCall();
                        
            return client.getJsonObjectFromAiCallRequest( joReq, BaseAiClient.AI_CALL_TIMEOUT_LONG );
        }
        catch( Exception e )
        {
            Tracker.addAiCallError();
            LogService.logIt(e, "AiRequestUtils.doResumeParsingCall() aiCallType=" +  aiCallType.getName() +", ResumeId=" + (resume==null ? "null" : resume.getResumeId() + ", userId=" + resume.getUserId() + ", orgId=" + resume.getOrgId()) + ", userId=" + (user==null ? "null" : user.getUserId()) );
            throw e;
        }
    }



    private static JsonObjectBuilder getBasePayloadJsonObjectBuilder( AiCallType aiCallType, User user ) throws Exception
    {
        AiCallSourceType aiCallSourceType = AiCallSourceType.BUILDER;

        JsonObjectBuilder job = Json.createObjectBuilder();

        job.add( "tran", aiCallType.getTran() );
        job.add( "sourcetypeid", aiCallSourceType.getAiCallSourceTypeId() );
        
        if( user!=null )
            job.add( "useridenc", user.getUserIdEncrypted() );

        return job;

    }
}
