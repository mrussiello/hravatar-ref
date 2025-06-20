package com.tm2ref.user;

import com.tm2ref.ai.AiCallType;
import com.tm2ref.ai.AiRequestUtils;
import com.tm2ref.entity.user.Resume;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.STException;
import com.tm2ref.ref.ResumeParseThread;
import com.tm2ref.service.LogService;
import jakarta.json.JsonObject;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author miker
 */
public class ResumeHelpUtils {

    public static boolean parseResumeByAiNoErrors(  long rcCheckId, User user, Resume resume)
    {
        try
        {
            return parseResumeByAi( rcCheckId, user, resume);
        }
        catch (STException e)
        {
            return false;
        }
        catch (Exception e)
        {
            LogService.logIt(e, "ResumeUtilsHelp.parseResumeByAiNoErrors() rcCheckId=" + rcCheckId + ", Resume=" + (resume==null ? "null" : resume.toString()) );
            return false;
        }
                
    }
    
    public static boolean parseResumeByAi( long rcCheckId, User user, Resume resume) throws STException
    {
        try
        {
            if( !AiRequestUtils.getIsAiSystemAvailable() )
                return false;
            
            if( resume==null )
                throw new Exception( "Resume is null.");

            if( resume.getUploadedText()==null || resume.getUploadedText().isBlank() )
                throw new Exception( "Resume.uploadedText is null or empty." );
            
            if( !resume.getHasAnyDataForAiParseCall())
                throw new STException( "g.ResumeNoDataForAi", new String[]{} );

            ResumeParseThread rpt = new ResumeParseThread( rcCheckId,  user, resume);
            return rpt.parseResumeViaAi();            
        }
        catch (STException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            LogService.logIt(e, "ResumeUtilsHelp.ResumeHelpUtils() rcCheckId=" + rcCheckId + ", Resume=" + (resume==null ? "null" : resume.toString()) );
            throw new STException(e);            
        }
    }
    
    
    
    
}
