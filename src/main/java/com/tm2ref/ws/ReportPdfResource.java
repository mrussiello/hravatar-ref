/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ws;


import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.ref.RcCheckUtils;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.ref.RcMessageUtils;
import com.tm2ref.ref.RcResultReportingUtils;
import com.tm2ref.ref.RefUserType;
import com.tm2ref.report.ReportManager;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.util.JsonUtils;
import java.util.List;
import java.util.Locale;
import jakarta.enterprise.context.RequestScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.DatatypeConverter;

/**
 *
 * {
 *    rcid:  encrypted test key id
 *    tran:  transaction name
 * }
 * 
 * The purpose of this resource is to start scoring of a completed test key immediately.
 */
@Path("refreportws")
@RequestScoped
public class ReportPdfResource extends BaseApiResource {
    
    
    RcFacade rcFacade;
    
    
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response doPost( @Context HttpServletRequest request , @Context HttpHeaders headers, String jsonContent ) 
    {        
        long rcCheckId = 0;
        String rcid;
        String langStr;
        int reportId;
        
        try
        {
            LogService.logIt( "ReportPdfResource.doPost() START. jsonContent=" + jsonContent );
            // Authenticate
            try
            {
                 authenticateRequest( headers );
            }
            catch( ApiException e )
            {
                LogService.logIt( e, "ReportPdfResource.doPost() AA.1 Authentication Error. jsonContent=" + jsonContent );
                return Response.status( e.getHttpResponseCode(), "Unable to authenticate." ).build();
            }
                        
            if( jsonContent==null || jsonContent.isBlank() )
                throw new ApiException( "Payload is missing.", 150, Response.Status.BAD_REQUEST.getStatusCode() );
                        
            
            // Parse the Json cntent
            JsonObject jo = JsonUtils.getJsonObject(jsonContent);
            
            tran = jo.containsKey( "tran" ) && !jo.isNull("tran") ? jo.getString( "tran" ) : null;
            rcid = jo.containsKey( "rcid" ) && !jo.isNull("rcid") ? jo.getString( "rcid" ) : null;
            
            
            if( tran == null || tran.isBlank() )
                throw new ApiException( "tran is missing.", 155, Response.Status.BAD_REQUEST.getStatusCode() );
                        
            if( rcid == null || rcid.isBlank() )
                throw new ApiException( "rcid is missing.", 180, Response.Status.BAD_REQUEST.getStatusCode() );
                     
            try
            {
                rcCheckId = Long.parseLong( EncryptUtils.urlSafeDecrypt(rcid) );
            }
            catch( NumberFormatException ee )
            {
                LogService.logIt(  "ReportPdfResource.doPost() NumberFormatException parsing " + rcid + ", jsonContent=" + jsonContent );
                throw new ApiException( "Error parsing rcid", 181, Response.Status.BAD_REQUEST.getStatusCode() );
            }
            catch( Exception ee )
            {
                LogService.logIt(  ee, "ReportPdfResource.doPost() Exception parsing " + rcid + ", jsonContent=" + jsonContent );
                throw new ApiException( "Error parsing rcid", 182, Response.Status.BAD_REQUEST.getStatusCode() );                
            }

            if( rcCheckId<=0 )
                throw new ApiException( "rcCheckId is invalid=" + rcCheckId, 185, Response.Status.BAD_REQUEST.getStatusCode() );
                                 
            JsonObjectBuilder outJob = null;
            
            if( tran.equals( "pdfreportgen" ) )
            {
                langStr = jo.containsKey( "langstr" ) && !jo.isNull("langstr") ? jo.getString( "langstr" ) : null;
                reportId = jo.containsKey( "reportid" )  ? jo.getInt("reportid" ) : 0;
                outJob = doGenPdfReport( rcCheckId, langStr, reportId );
            }

            else if( tran.equals( "rcprogressemail" ) )
            {
                langStr = jo.containsKey( "langstr" ) && !jo.isNull("langstr") ? jo.getString( "langstr" ) : null;
                reportId = jo.containsKey( "reportid" )  ? jo.getInt("reportid" ) : 0;
                String destEmails = jo.containsKey( "recipientemails" ) && !jo.isNull("recipientemails") ? jo.getString( "recipientemails" ) : null;
                // String langCode = jo.containsKey( "langcode" ) && !jo.isNull("langcode") ? jo.getString( "langcode" ) : null;
                outJob = doSendReportEmail(rcCheckId, reportId, destEmails, langStr );
            }
            
            else if( tran.equals("rccandfbkrptemail") )
            {
                langStr = jo.containsKey( "langstr" ) && !jo.isNull("langstr") ? jo.getString( "langstr" ) : null;
                reportId = jo.containsKey( "reportid" )  ? jo.getInt("reportid" ) : 0;
                outJob = doSendCandFbkReportEmail( rcCheckId, reportId, langStr );
            }
            
            else
                throw new ApiException( "tran is invalid=" + tran, 160, Response.Status.BAD_REQUEST.getStatusCode() );
                
            
            JsonObject jo2 = outJob.build();            
            String out = JsonUtils.convertJsonObjecttoString(jo2);     
            Tracker.addApiReportPdfRequest();
            // LogService.logIt( "ReportPdfResource.doPost() COMPLETE. output=" + out );
            return Response.ok( out, MediaType.APPLICATION_JSON).status( Response.Status.OK.getStatusCode() ).build();            
        }        
        catch( ApiException e )
        {
            Tracker.addApiError();
            LogService.logIt( "ReportPdfResource.doPost() API ERROR " + e.toString() + ", rcCheckId=" + rcCheckId + ", tran=" + tran + ", orgId=" + getOrgId() );
            String subj = "ScoreResource Exception rcCheckId=" + rcCheckId;     
            sendErrorEmail( subj, "rcCheckId=" + rcCheckId + ", API Exception=" + e.toString() );            
            return Response.status( e.getHttpResponseCode(), "Server Error: ReportPdfResource.ScoreResource() rcCheckId=" + rcCheckId + ", tran=" + tran + ", orgId=" + getOrgId() + ", " + e.toString() ).build();            
        }                
        catch( Exception e )
        {
            Tracker.addApiError();
            LogService.logIt( e, "ReportPdfResource.doPost() jsonContent=" + jsonContent );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "ReportPdfResource.ScoreResource() Unknown Exception rcCheckId=" + rcCheckId + ", tran=" + tran + ", authUserId=" + (authUser==null ? "null" : authUser.getUserId()) + ", " + e.toString() + ", jsonContent=" + jsonContent ).build();            
            // return getGeneralErrorJson( e, "ReportPdfResource.doPost() tran=" + tran + ", rcid=" + rcid + ", jsonContent=" + jsonContent );
        }
    }
    
    private JsonObjectBuilder doGenPdfReport( long rcCheckId, String langStr, int reportId ) throws Exception
    {
        JsonObjectBuilder outJob = Json.createObjectBuilder();             
        
        if( rcFacade==null )
            rcFacade = RcFacade.getInstance();
        
        RcCheck rc = rcFacade.getRcCheck(rcCheckId, true);
        
        if( rc==null )
        {
            LogService.logIt( "ReportPdfResource.doGenPdfReport() ERROR RcCheck not found for rcCheckId=" + rcCheckId + ", reportId=" + reportId + ", langStr=" + langStr );
            throw new ApiException( "ReportPdfResource.doGenPdfReport() ERROR RcCheck not found.", 500, 500 );
            // throw new Exception( "RcCheck not found for rcCheckId=" + rcCheckId );
        }
        
        if( !rc.getRcCheckStatusType().getIsComplete() )
            throw new Exception( "RcCheck is not yet complete. status=" + rc.getRcCheckStatusType().getName() + " rcCheckId=" + rcCheckId );
            
        ReportManager rm = new ReportManager();

        // LogService.logIt( "ReportPdfResource.doGenPdfReport() Starting PDF Report Gen. rcCheckId=" + rcCheckId );
        
        outJob.add( "rccheckid", rcCheckId );        
        
       /* data[0] = rcCheckId
          data[1] = bytes for report
          data[2] = suggested filename
          data[3] = date/time
        */
        List<Object[]> rout = rm.generateReportsForRcCheckAndLanguage(rcCheckId, reportId, langStr);
        if( rout==null || rout.isEmpty() )
        {
            LogService.logIt( "ReportPdfResource.doGenPdfReport() ERROR Call to reportManager did not return any report info. rcCheckId=" + rcCheckId + ", reportId=" + reportId + ", langStr=" + langStr );
            throw new ApiException( "ReportPdfResource.doGenPdfReport() ERROR Call to reportManager did not return any report info.", 500, 500 );
        }
        Object[] out = rout.get(0);

        byte[] bytes = (byte[]) out[1];
        String filename = (String) out[2];

        if( bytes==null || bytes.length==0 )
        {
            LogService.logIt( "ReportPdfResource.doGenPdfReport() ERROR Bytes are missing. rcCheckId=" + rcCheckId + ", reportId=" + reportId + ", langStr=" + langStr );
            throw new ApiException( "ReportPdfResource.doGenPdfReport() ERROR Bytes are missing.", 500, 500 );
            //throw new Exception( "Bytes are missing." );
        }

        // LogService.logIt( "ReportPdfResource.doGenPdfReport() PDF Report Gen. Complete. Bytes=" + bytes.length + ", rcCheckId=" + rcCheckId + ", filename=" + filename );

        String base64Encoded = DatatypeConverter.printBase64Binary(bytes);

        outJob.add( "filename", filename );   
        outJob.add( "bytesbase64", base64Encoded );   
        
        return outJob;        
    }
       
    private JsonObjectBuilder doSendReportEmail( long rcCheckId, int reportId, String destEmails, String langCode) throws Exception
    {
        JsonObjectBuilder outJob = Json.createObjectBuilder();             
        
        if( rcFacade==null )
            rcFacade = RcFacade.getInstance();
        
        RcCheck rc = rcFacade.getRcCheck(rcCheckId, true);
        
        if( rc==null )
            throw new Exception( "RcCheck not found for rcCheckId=" + rcCheckId );

        Locale locale;        
        if( langCode==null || langCode.isBlank() )
            locale = I18nUtils.getLocaleFromCompositeStr( rc.getLangCode()!=null && !rc.getLangCode().isBlank() ? rc.getLangCode() : "en_US" );
        else
            locale = I18nUtils.getLocaleFromCompositeStr( langCode );
        
        // Load for admin
        RcCheckUtils rcCheckUtils = new RcCheckUtils();        
        rcCheckUtils.loadRcCheckForAdmin(rc, RefUserType.CANDIDATE, locale, false );        

        RcMessageUtils rcMessageUtils = new RcMessageUtils();
        
        int[] out = rcMessageUtils.sendProgressUpdateEmailForCurrentStatus(rc, locale, destEmails );
        
        int emailsSent = out[0];
        
        outJob.add( "rccheckid", rcCheckId );        
        outJob.add( "emailssent", emailsSent );        
        
        return outJob;        
    }

    private JsonObjectBuilder doSendCandFbkReportEmail( long rcCheckId, int reportId, String langCode) throws Exception
    {
        JsonObjectBuilder outJob = Json.createObjectBuilder();             
        
        if( rcFacade==null )
            rcFacade = RcFacade.getInstance();
        
        RcCheck rc = rcFacade.getRcCheck(rcCheckId, true);        
        if( rc==null )
            throw new Exception( "RcCheck not found for rcCheckId=" + rcCheckId );

        Locale locale;        
        if( langCode==null || langCode.isBlank() )
            locale = I18nUtils.getLocaleFromCompositeStr( rc.getLangCode()!=null && !rc.getLangCode().isBlank() ? rc.getLangCode() : "en_US" );
        else
            locale = I18nUtils.getLocaleFromCompositeStr( langCode );
        
        // Load for admin
        RcResultReportingUtils rcru = new RcResultReportingUtils();          

        int[] out = rcru.sendCandidateFeedbackReportEmails(rc, reportId, true, locale);
        
        int emailsSent = out[0];
        
        outJob.add( "rccheckid", rcCheckId );        
        outJob.add( "emailssent", emailsSent );        
        
        return outJob;        
    }
}
