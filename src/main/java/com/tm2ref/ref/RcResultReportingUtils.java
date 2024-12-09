/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.report.Report;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.report.ReportFacade;
import com.tm2ref.report.ReportManager;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker
 */
public class RcResultReportingUtils 
{
    RcFacade rcFacade;
    ReportFacade reportFacade;
    UserFacade userFacade;
        
    
    public int[] sendCandidateFeedbackReportEmails( RcCheck rc, int reportId, boolean frcSend, Locale locale)
    {
        int[] out = new int[2];
        try
        {
            if( rc==null )
                throw new Exception( "RcCheck is null. " );
            
            if( !frcSend && rc.getEmailReportsToCandidate()<1)
            {
                LogService.logIt("RcResultReportingUtils.sendCandidateFeedbackReportEmails() RcCheck is not set to send feedback reports and frcSnd is false. Requested reportId=" + reportId + ", " + (rc==null ? "null" : rc.toString() ) );
                return out;
            }
            
            if( !rc.getRcCheckStatusType().getIsComplete() )
            {
                LogService.logIt("RcResultReportingUtils.sendCandidateFeedbackReportEmails() RcCheck is not complete. Cannot generate/send feedback reports. Requested reportId=" + reportId + ", " + (rc==null ? "null" : rc.toString() ) );
                return out;
            }
            
            if( rc.getUser()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                rc.setUser(userFacade.getUser( rc.getUserId() ));
            }
                        
            if( rc.getOrg()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                rc.setOrg(userFacade.getOrg( rc.getOrgId() ));
            }

            if( rc.getAdminUser()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                rc.setAdminUser(userFacade.getUser( rc.getAdminUserId() ));
            }
                        
            
            User u = rc.getUser();            
            if( u == null )
                throw new Exception( "RcCheck.User is null. " );

            if( locale==null )
                locale = rc.getLocale();
            
            if( locale==null && rc.getLangCode()!=null && !rc.getLangCode().isBlank() )
                locale = I18nUtils.getLocaleFromCompositeStr(rc.getLangCode() );

            if( locale==null )
                locale=Locale.US;
            
            if( !EmailUtils.validateEmailNoErrors( u.getEmail() ) )
            {
                LogService.logIt("RcResultReportingUtils.sendCandidateFeedbackReportEmails() RcCheck.User.email is invalid: " + u.getEmail() + "  Requested reportId=" + reportId + ", " + (rc==null ? "null" : rc.toString() ) );
                return out;
            }
            
            int rid = reportId>0 ? reportId : rc.getReportId();
            int rid2 = reportId>0 ? 0 : rc.getReportId2();
            
            if( reportFacade==null )
                reportFacade=ReportFacade.getInstance();
            
            Report r = rid>0 ? reportFacade.getReport(rid) : null;
            Report r2 = rid2>0 ? reportFacade.getReport(rid2) : null;
            
            List<Report> reportList = new ArrayList<>();
            
            if( r!=null && r.getEmailTestTaker()==1 )
                reportList.add(r);
            
            if( r2!=null && r2.getEmailTestTaker()==1 )
                reportList.add(r2);
            
            if( reportList.isEmpty() )                
            {
                LogService.logIt("RcResultReportingUtils.sendCandidateFeedbackReportEmails() No Reports found with emailtesttaker=1. Requested reportId=" + reportId + ", " + (rc==null ? "null" : rc.toString() ) );
                return out;
            }

            if( rc.getRcScript()==null )
            {
                rc.setRcScript( RcScriptFacade.getInstance().getRcScript(rc.getRcScriptId(), true));
            }
            
            
            ReportManager rm = new ReportManager();

            /* data[0] = rcCheckId
               data[1] = bytes for report
               data[2] = suggested filename
               data[3] = date/time
             */
            List<Object[]> rout;
            Object[] reportData;
            byte[] bytes;
            String filename;
            
            String reportTitle = null;
            
            RcMessageUtils rcMessageUtils = new RcMessageUtils();
        
            
            for( Report report : reportList )
            {
                rout = rm.generateReportsForRcCheckAndLanguage(rc.getRcCheckId(), (int) report.getReportId(), locale.toString() );
                if( rout==null || rout.isEmpty() )
                {
                    LogService.logIt("RcResultReportingUtils.sendCandidateFeedbackReportEmails()  ERROR Call to reportManager did not return any report info. rcCheckId=" + rc.getRcCheckId() + ", reportId=" + reportId );
                    continue;
                }
                
                reportTitle = getReportName( rc, report, locale);
                                
                reportData = rout.get(0);

                bytes = (byte[]) reportData[1];
                filename = (String) reportData[2];

                if( bytes==null || bytes.length==0 )
                {
                    LogService.logIt("ReportPdfResource.doGenPdfReport() ERROR Bytes are missing. rcCheckId=" + rc.getRcCheckId() + ", reportId=" + reportId );
                    continue;
                    //throw new Exception( "Bytes are missing." );
                    
                }                

                LogService.logIt("ReportPdfResource.doGenPdfReport() Sending Candidate Feedback Report Email to " + u.getEmail() + ", bytes.length=" + bytes.length + ", filename=" + filename +   ",  rcCheckId=" + rc.getRcCheckId() + ", reportId=" + reportId );
                int sendResults = rcMessageUtils.sendCandFbkReportEmail(rc, bytes, filename, locale, reportTitle );

                out[0] += sendResults;
            } 
            
            if( out[0]>0 )
            {
                rc.setCandidateReportSendDate( new Date() );
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcFacade.saveRcCheck(rc, false);
            }
            
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcResultReportingUtils.sendCandidateFeedbackReportEmails() " + (rc==null ? "null" : rc.toString() ) );
        }
        return out;
    }
    
    public String getReportName( RcCheck rc, Report r, Locale locale) 
    {

        String ttl = "";
        
        if( r!=null && r.getStrParam3()!=null && !r.getStrParam3().isEmpty() )
            ttl = r.getStrParam3();
        
        else if( r!=null && r.getStrParam2()!=null && !r.getStrParam2().isEmpty() )
        {
            String key = r.getStrParam2(); // "g.TestResultsAndInterviewGuide";
        
            ttl = MessageFactory.getStringMessage(locale, key, null );
            
            if( ttl ==null )
                ttl = r.getTitle();
        }
        
        else if( r.getTitle()!=null && !r.getTitle().isEmpty() )
            ttl = r.getTitle();            
        
        else
            ttl = r.getName();
        
        return StringUtils.replaceStr(ttl, "[SCRIPTNAME]" , rc.getRcScript().getName() );
    }
    
    
    
}
