/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.ref;

import com.tm2ref.ai.AiCallStatusType;
import com.tm2ref.ai.AiRequestUtils;
import com.tm2ref.entity.user.Resume;
import com.tm2ref.entity.user.User;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.JsonUtils;
import jakarta.json.JsonObject;
import java.util.Date;

/**
 *
 * @author miker
 */
public class ResumeParseThread implements Runnable {
    
    static int MIN_CHARS_FOR_AI_PARSE = 500;
    
    User user;
    Resume resume;
    long rcCheckId;
    
    UserFacade userFacade;
    
    public ResumeParseThread( long rcCheckId, User user, Resume resume )
    {
        this.rcCheckId=rcCheckId;
        this.user=user;
        this.resume=resume;
    }

    @Override
    public void run()
    {
        try
        {
            parseResumeViaAi();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ResumeParseThread.run() rcCheckId=" + rcCheckId + ", userId=" + (user==null ? "null" : user.getUserId()) + ", resumeId=" + (resume==null ? "null" : resume.getResumeId()) );
        }
    }
    
    public boolean parseResumeViaAi() throws Exception
    {
        try
        {
            if( resume.getUploadedText()==null || resume.getUploadedText().isBlank() )
                return false;
            
            if( resume.getUploadedText().trim().length()<MIN_CHARS_FOR_AI_PARSE )
            {
                LogService.logIt("ResumeParseThread.parseResumeViaAi() Resume Uploaded Text is less than the min chars. Saving the uploaded text to Summary and returning. rcCheckId=" + rcCheckId + ",  userId=" + (user==null ? "null" : user.getUserId() ) + ", resumeId=" + (resume==null ? "null" : resume.getResumeId()) );
                
                // See if we have a summary/ If we do and there's very little in the upload, ignore it.
                resume.parseJsonStrNoErrors();
                if( resume.getSummary()!=null && !resume.getSummary().isBlank() && resume.getUploadedText().trim().length()<10 )
                    return false;
                
                resume.setSummary( resume.getUploadedText().trim() );
                resume.setObjective(null);
                resume.setExperience(null);
                resume.setEducation(null);
                resume.setOtherQuals(null);
                resume.setNeedsParse(0);

                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                resume.setLastParseDate(new Date());
                resume.setLastUpdate(new Date() );
                resume.setPlainText( resume.getSummary() );
                resume.packJsonStr();
                userFacade.saveResume(resume);

                return false;
            }
            
            if( !AiRequestUtils.getIsAiSystemAvailable() )
            {
                resume.setPlainText( resume.getUploadedText().trim());
                resume.setSummary( resume.getUploadedText().trim() );
                resume.setObjective(null);
                resume.setExperience(null);
                resume.setEducation(null);
                resume.setOtherQuals(null);
                resume.parseJsonStrNoErrors();                
                resume.setNeedsParse(1);
                resume.packJsonStr();
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                userFacade.saveResume(resume);
                
                LogService.logIt("ResumeParseThread.parseResumeViaAi() AI System is unavailable. rcCheckId=" + rcCheckId + ", userId=" + (user==null ? "null" : user.getUserId() ) + ", resumeId=" + (resume==null ? "null" : resume.getResumeId()) );
                return false;
            }
            
            JsonObject resJo = AiRequestUtils.parseResume(resume, user, true );

            if( resJo==null )
            {
                LogService.logIt("ResumeParseThread.parseResumeViaAi() Resume Parse returned null. rcCheckId=" + rcCheckId + ", userId=" + (user==null ? "null" : user.getUserId() ) + ", resumeId=" + (resume==null ? "null" : resume.getResumeId()) );
                return false;
            }

            int aiCallHistoryId = resJo.containsKey("aicallhistoryid") ? resJo.getInt("aicallhistoryid") : 0;
            // int aiCallTypeId = resJo.containsKey("aicalltypeid") ? resJo.getInt("aicalltypeid") : 0;

            String status = JsonUtils.getStringFmJson(resJo, "status");

            LogService.logIt("ResumeParseThread.parseResumeViaAi() Resume Parse returned status=" + status + ", aiCallHistoryId=" + aiCallHistoryId + ", rcCheckId=" + rcCheckId + ", userId=" + (user==null ? "null" : user.getUserId() ) + ", resumeId=" + (resume==null ? "null" : resume.getResumeId()) );
                       
            if( status.equals( AiCallStatusType.ERROR.getStatusStr() ) )
            {
                String msg = "Call to AI experienced an error. Error code=" + (resJo.containsKey("errorcode") ? resJo.getInt("errorcode") : "none") + ", Error message=" + (resJo.containsKey("errormessage") ? JsonUtils.getStringFmJson( resJo, "errormessage") : "none") + ", aiCallHistoryId=" + aiCallHistoryId;
                LogService.logIt("AdminResumeUtils.parseAndStoreResumeInfo() ERROR: " + msg + ", rcCheckId=" + rcCheckId + ", json returned=" + JsonUtils.convertJsonObjectToString(resJo) );
                return false;
            }

            if( !status.equals( AiCallStatusType.COMPLETED.getStatusStr() ) )
            {
                String msg = "Call to AI is in unexpected status=" + status;
                LogService.logIt("AdminResumeUtils.parseAndStoreResumeInfo() " + msg + ", rcCheckId=" + rcCheckId + ", aiCallHistoryId=" + aiCallHistoryId + ", json returned=" + JsonUtils.convertJsonObjectToString(resJo) );
                return false;
            }            
            
            LogService.logIt("ResumeIactnResp.handleAiCallResult() rcCheckId=" + rcCheckId + ", status=complete, aiCallHistoryId=" + aiCallHistoryId );
            // Update is automatic and performed by the AI Call.
            Thread.sleep(100);
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            resume = userFacade.getResumeForUser( user.getUserId() );
            
            return true;            
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ResumeParseThread.parseResumeViaAi() rcCheckId=" + rcCheckId + ", userId=" + (user==null ? "null" : user.getUserId()) + ", resumeId=" + (resume==null ? "null" : resume.getResumeId()) );
            throw e;
        }
    }
    
}
