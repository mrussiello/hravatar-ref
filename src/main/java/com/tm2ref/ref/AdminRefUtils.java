/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.faces.FacesUtils;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.ref.results.RcResultEmailFormatter;
import com.tm2ref.ref.results.ResultFormatterFactory;
import com.tm2ref.report.ReportManager;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserFacade;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author miker_000
 */
@Named
@RequestScoped
public class AdminRefUtils extends FacesUtils {
    
    @Inject
    AdminRefBean adminRefBean;
    
    RcFacade rcFacade;
    RcCheckUtils rcCheckUtils;
    
    public static AdminRefUtils getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return (AdminRefUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "adminRefUtils" );
    }

    
    public void toggleNewRefStartsOk()
    {
        RuntimeConstants.setValue("newRefStartsOK", !RuntimeConstants.getBooleanValue("newRefStartsOK") );
    }
    
    
    public String processToggleRefStarts()
    {
        try
        {
            getUserBean();

            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Logon invalid." );

            adminRefBean.setStrParam1(null);
            toggleNewRefStartsOk();

            this.setStringInfoMessage( "New Ref starts are now " + ( getNewRefStartsOk() ? "ON" : "OFF" ) );

            return null;
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.processToggleRefStarts()" );
            setMessage(e);
            return null;
        }
    }

    public String processToggleRemindersBatches()
    {
        try
        {
            getUserBean();

            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Logon invalid." );

            RuntimeConstants.setValue("autoRemindersOk", !RuntimeConstants.getBooleanValue("autoRemindersOk") );

            this.setStringInfoMessage( "Reminders Batches are now " + ( RuntimeConstants.getBooleanValue("autoRemindersOk") ? "ON" : "OFF" ) );

            return null;
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.processToggleRefStarts()" );
            setMessage(e);
            return null;
        }
    }



    public String processClearDmbsCache()
    {
        try
        {
            adminRefBean.setStrParam1(null);
            getUserBean();
            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Logon invalid." );
            
            UserFacade.getInstance().clearSharedCache();
            setStringInfoMessage( "Shared DBMS Cache cleared." );            
            return "/tools/admin/admintools.xhtml";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.processClearDmbsCache() " );
            setMessage(e);
            return null;
        }                
    }    
    
    
    public String processRefAdminTop()
    {
        try
        {
            adminRefBean.setStrParam1(null);
            getUserBean();
            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Logon invalid." );
            adminRefBean.setPdfReportList(null);
            return "/tools/admin/admintools.xhtml";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.processRefAdminTop() " );
            setMessage(e);
            return null;
        }                
    }    

    public String processRunAsAdmin()
    {
        long rcCheckId = adminRefBean.getRcCheckId5();            
        long rcRaterId = adminRefBean.getRcRaterId5();            
            
        try
        {
            getUserBean();
            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Logon invalid." );
            if( rcCheckId<=0 && rcRaterId<=0 )
                throw new Exception( "RcCheckId and/or RcRater are both invalid. Need one of them. rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );
            
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            
            RcCheck rc = null;
            if( rcCheckId>0 )
            {
                rc = rcFacade.getRcCheck(rcCheckId, true );
                if( rc==null )
                {
                    this.setStringErrorMessage("RcCheck not found for RcCheckId=" + rcCheckId );
                    return "StayInSamePlace";
                }
            }
            
            RcRater rater = null;
            if( rcRaterId>0 )
            {
                rater = rcFacade.getRcRater(rcRaterId, true );
                if( rater==null )
                {
                    this.setStringErrorMessage("RcRater not found for rcRaterId=" + rcRaterId );
                    return "StayInSamePlace";
                }
                
                if( rc!=null && rc.getRcCheckId()!=rater.getRcCheckId() )
                {
                    this.setStringErrorMessage("RcRater.rcCheck (" + rater.getRcCheckId() + ") does not match rcCheckId=" + rcCheckId );
                    return "StayInSamePlace";
                } 
                
                rcCheckId=rater.getRcCheckId();
                if(rc==null)
                    rc = rcFacade.getRcCheck(rcCheckId, true );
            }
            
            if( rc==null )
            {
                this.setStringErrorMessage("RcCheck is null. rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );
                return "StayInSamePlace";
            } 
            
            
            LogService.logIt( "AdminRefUtils.processRunAsAdmin() rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId ); 
            
            RefUtils refUtils = RefUtils.getInstance();
            
            return refUtils.performSimpleEntry( rc.getCorpId(), rcCheckId, rcRaterId, null, true );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.processRunAsAdmin() rcCheckId=" + rcCheckId + ", rcCheckId=" + rcCheckId );
            setMessage(e);
            return null;
        }                
    }
    
    public String processCreateRcReportPdf()
    {
        long rcCheckId = adminRefBean.getRcCheckId4();            
        String langStr = adminRefBean.getLangStr4();
        int reportId = adminRefBean.getReportId4();
            
        try
        {
            adminRefBean.setStrParam1(null);
            getUserBean();
            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Logon invalid." );
            if( rcCheckId<=0 )
                throw new Exception( "RcCheckId invalid. " + rcCheckId );
            
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            RcCheck rc = rcFacade.getRcCheck(rcCheckId, true );
            if( rc==null )
                throw new Exception( "RcCheck is null for rcCheckId=" + rcCheckId );
            if( !rc.getRcCheckStatusType().getIsComplete())
                throw new Exception( "RcCheck is not in complete. " + rc.getRcCheckStatusType().getName() + ", rcCheckId=" + rcCheckId );
            
            ReportManager rm = new ReportManager();
            
            LogService.logIt( "AdminRefUtils.processCreateRcReportPdf() START rcCheckId=" + rcCheckId + ", langStr=" + langStr + ", reportId=" + reportId ); 
            List<Object[]> rout = rm.generateReportsForRcCheckAndLanguage(rcCheckId, reportId, langStr);
                     
            if( rout==null )
            {
                this.setStringErrorMessage("ReportList is null" );
                return "StayInSamePLace";
            }

            if( rout.isEmpty() )
            {
                this.setStringErrorMessage("ReportList is empty" );
                return "StayInSamePLace";
            }

            
            byte[] bytes;
            String name;
            for( Object[] out : rout )
            {
                bytes = (byte[]) out[1];
                name = (String) out[2];

                if( bytes==null || bytes.length==0 )
                    throw new Exception( "Bytes are missing." );

                setStringInfoMessage( "Created Report for for rcCheckId=" + rc.getRcCheckId() + " bytes=" + bytes.length );
                adminRefBean.addPdfReport( out );                
                LogService.logIt( "AdminRefUtils.processCreateRcReportPdf() rcCheckId=" + rcCheckId + ", bytes=" + bytes.length + ", filename=" + name );            
            }
                                    
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.processCreateRcReportPdf() rcCheckId=" + rcCheckId + ", langStr=" + langStr  + ", reportId=" + reportId );
            setMessage(e);
            return null;
        }                
    }

    
    public StreamedContent getReportFileToDownload()
    {
        try
        {
            getUserBean();

            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Unauthorized Action." );

            Object[] pdfReport = adminRefBean.getPdfReport();

            LogService.logIt( "AdminRefUtils.getReportFileToDownload() pdfReport=" + (pdfReport==null ? "null" : "Not Null" ) );

            if( pdfReport == null )
                return null;

            byte[] bytes = (byte[]) pdfReport[1];

            LogService.logIt( "AdminRefUtils.getReportFileToDownload() report size is " + bytes.length );

            ByteArrayInputStream baos = new ByteArrayInputStream( bytes );

            DefaultStreamedContent dsc = DefaultStreamedContent.builder().contentType("application/pdf").name((String)pdfReport[2]).stream(() -> baos).build();
            
            return dsc;
            // return new DefaultStreamedContent( baos, "application/pdf",  (String)pdfReport[2] );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.getReportFileToDownload() " );
            return null;
        }
    }
    
    
    
    public String processRecomputeRaterAndOverallScores()
    {
        long rcCheckId = adminRefBean.getRcCheckId3();            
        try
        {
            adminRefBean.setStrParam1(null);
            getUserBean();
            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Logon invalid." );
            if( rcCheckId<=0 )
                throw new Exception( "RcCheckId invalid. " + rcCheckId );
            
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            RcCheck rc = rcFacade.getRcCheck(rcCheckId, true );
            if( rc==null )
                throw new Exception( "RcCheck is null for rcCheckId=" + rcCheckId );
            if( !rc.getRcCheckStatusType().getIsComplete())
                throw new Exception( "RcCheck is not in complete. " + rc.getRcCheckStatusType().getName() + ", rcCheckId=" + rcCheckId );
            
            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();
            rcCheckUtils.loadRcCheckForScoringOrResults(rc);
            
            float score;
            for(RcRater rater : rc.getRcRaterList() )
            {
                if( !rater.getRcRaterStatusType().getIsComplete() )
                    continue;
                
                score = rcCheckUtils.computeRcRaterOverallScore(rc, rater);
                rater.setOverallScore(score);
                rater = rcFacade.saveRcRater(rater, false );   
                this.setStringInfoMessage( "Recalulated Rater " + rater.getUser().getFullname() + " score to " + score );
            }
            
            score = rcCheckUtils.computeRcCheckOverallScore(rc);
            rc.setOverallScore(score);
            rc.setScoreDate( new Date() );
            rc.setRcCheckScoringStatusTypeId( RcCheckScoringStatusType.SCORED.getRcCheckScoringStatusTypeId() );
            rcFacade.saveRcCheck(rc, false );
            
            rcCheckUtils.doCheckForSuspiciousActivity(rc);
            
            setStringInfoMessage( "Recalulated overall score for rcCheckId=" + rc.getRcCheckId() + " to " + score );
            
            LogService.logIt( "AdminRefUtils.processRecomputeRaterAndOverallScores() rcCheckId=" + rcCheckId );            
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.processRecomputeRaterAndOverallScores() rcCheckId=" + rcCheckId );
            setMessage(e);
            return null;
        }        
    }    
    
    
    public String processSendUnsentDelayedRaterInvitations()
    {
        try
        {
            adminRefBean.setStrParam1(null);
            getUserBean();
            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Logon invalid." );
            String rcCheckIdStr = adminRefBean.getRcCheckIds();            
            if( rcCheckIdStr==null || rcCheckIdStr.isBlank() )
                throw new Exception( "RcCheckIds are missing. Should be comma delimited. " );
            
            List<Long> rcCheckIds = new ArrayList<>();
            long rcCheckId = 0;
            for( String rcid : rcCheckIdStr.split(",") )
            {
                if( rcid.isBlank() )
                    continue;
                
                rcCheckId = Long.parseLong(rcid);
                
                if( rcCheckId>0 )
                    rcCheckIds.add( rcCheckId );
            }
            
            if( rcCheckIds.isEmpty() )
                throw new Exception( "Could not parse any rcCheckIds from inStr=" + rcCheckIdStr );
                        
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            RcCheck rc;
            
            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();
            
            CandidateRefUtils cru = CandidateRefUtils.getInstance();
            
            int[] out = new int[2];
            
            int[] tempOut;
                 
            for( Long id : rcCheckIds )
            {
                rc = rcFacade.getRcCheck(id, true );
                
                if( rc==null )
                {
                    this.setStringErrorMessage("Could not find RcCheck for rcCheckId=" + id );
                    continue;
                }

                if( rc.getRcCheckStatusType().getCompleteOrHigher()  )
                {
                    this.setStringErrorMessage("RcCheck is completed or higher. Not sending. rcCheckId=" + id );
                    continue;
                }
                
                rcCheckUtils.loadRcCheckForAdmin(rc, RefUserType.CANDIDATE, getLocale(), true );
                
                if( rc.getCandidateRatingsCompleteDate()==null )
                {
                    RcRater rcr = rc.getRcRaterForUserId( rc.getUserId() );
                    
                    if( !rcr.getRcRaterStatusType().getCompleteOrHigher() )
                        rcCheckUtils.performRcRaterCompletionIfReady(rc, rcr, false );
                    
                    if( rcr.getRcRaterStatusType().getCompleteOrHigher() )
                    {
                        rc.setCandidateRatingsCompleteDate(rcr.getCompleteDate() );
                        rcFacade.saveRcCheck(rc, false );
                    }                        
                    
                    if( rc.getCandidateRatingsCompleteDate()!=null )
                    {
                        setStringInfoMessage( "RcCheckId=" + rc.getRcCheckId() + ", changed to Candidate Ratings Complete." );
                        LogService.logIt( "AdminRefUtils.processSendUnsentDelayedRaterInvitations() RcCheckId=" + rc.getRcCheckId() + ", changed to Candidate Ratings Complete." );            
                    }                    
                }

                if( rc.getCandidateRatingsCompleteDate()==null )
                {
                    setStringInfoMessage( "RcCheckId=" + rc.getRcCheckId() + ", Skipped because Candidate Ratings are not complete." );
                    LogService.logIt( "AdminRefUtils.processSendUnsentDelayedRaterInvitations() Skipped because Candidate Ratings are not complete rcCheckId=" + id );            
                    continue;
                }

                LogService.logIt( "AdminRefUtils.processSendUnsentDelayedRaterInvitations() Sending delayed invites. rcCheckId=" + id + ", " + rc.getCandidateRatingsCompleteDate().toString() );            
                
                tempOut = cru.sendDelayedRaterInvitations(rc);
                
                setStringInfoMessage( "RcCheckId=" + rc.getRcCheckId() + ", emails sent: " + tempOut[0] + ", texts sent: " + tempOut[1] );
                
                out[0]+=tempOut[0];
                out[1]+=tempOut[1];
            }
            
            setStringInfoMessage( "Completion TOTAL emails sent: " + out[0] + ", TOTAL texts sent: " + out[1] );

            LogService.logIt( "AdminRefUtils.processSendUnsentDelayedRaterInvitations() TOTAL emails sent: " + out[0] + ", TOTAL texts sent: " + out[1] + " text messages.  , rcCheckId=" + rcCheckId );            
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.processSendUnsentDelayedRaterInvitations()" );
            setMessage(e);
            return null;
        }        
    }
    
    public String processRedistributeProgress()
    {
        try
        {
            adminRefBean.setStrParam1(null);
            getUserBean();
            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Logon invalid." );
            long rcCheckId = adminRefBean.getRcCheckId2();            
            if( rcCheckId<=0 )
                throw new Exception( "RcCheckId invalid. " + rcCheckId );
            
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            RcCheck rc = rcFacade.getRcCheck(rcCheckId, true );
            if( rc==null )
                throw new Exception( "RcCheck is null for rcCheckId=" + rcCheckId );
            if( !rc.getRcCheckStatusType().getIsStartedOrHigher())
                throw new Exception( "RcCheck is not in proper status for progress reports. " + rc.getRcCheckStatusType().getName() + ", rcCheckId=" + rcCheckId );
            
            Locale locale = null;            
            if( adminRefBean.getLangStr2()!=null && !adminRefBean.getLangStr2().isBlank() )
                locale = I18nUtils.getLocaleFromCompositeStr( adminRefBean.getLangStr2() );
            
            List<RcRater> rcrl = rcFacade.getRcRaterList(rcCheckId);
            RcRater rater = null;
            for( RcRater r : rcrl )
            {
                if( !r.getRcRaterStatusType().getIsComplete() || r.getCompleteDate()==null )
                    continue;
                if( rater!=null && rater.getCompleteDate().after( r.getCompleteDate() ))
                    continue;
                rater = r;
            }
            if( rater==null )
                throw new Exception("Cannot find any raters that are completed for this RcCheck. At least one rater must be complete for a progress report." );
            
            rc.setRcRater(rater);

            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();

            // RaterRefUtils rcru = RaterRefUtils.getInstance();
            rcCheckUtils.loadRcCheckForAdmin(rc, RefUserType.RATER, locale==null ? getLocale() : locale, false );
                        
            if( !rc.getRcCheckStatusType().getCompleteOrHigher() )
            {
                RefBean refBean = RefBean.getInstance();
                refBean.setRcCheck(rc);
                rcCheckUtils.performRcCheckCompletionIfReady(rc, false, false );
            }
            
            int[] sent = rcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete(rc, rater, true );            
            
            this.setStringInfoMessage( "Sent " + sent[0] + " emails and " + sent[1] + " text messages for rcCheckId=" + rcCheckId);
            LogService.logIt( "AdminRefUtils.processRedistributeProgress() sent " + sent[0] + " emails and " + sent[1] + " text messages.  , rcCheckId=" + rcCheckId );            
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.processRedistributeProgress()" );
            setMessage(e);
            return null;
        }        
    }
    
    public String processGenRefResultReportOnline()
    {
        try
        {
            adminRefBean.setStrParam1(null);
            getUserBean();
            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "Logon invalid." );

            long rcCheckId = adminRefBean.getRcCheckId();            
            if( rcCheckId<=0 )
                throw new Exception( "RcCheckId invalid. " + rcCheckId );
            
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            RcCheck rc = rcFacade.getRcCheck(rcCheckId, true );
            if( rc==null )
                throw new Exception( "RcCheck is null for rcCheckId=" + rcCheckId );
            if( !rc.getRcCheckStatusType().getIsComplete() )
                throw new Exception( "RcCheck is not in completed status. " + rc.getRcCheckStatusType().getName() + ", rcCheckId=" + rcCheckId );
            
            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();            
            rcCheckUtils.loadRcCheckForScoringOrResults(rc);
            
            Locale locale = null;
            
            if( adminRefBean.getLangStr()!=null && !adminRefBean.getLangStr().isBlank() )
                locale = I18nUtils.getLocaleFromCompositeStr( adminRefBean.getLangStr() );
            
            RcResultEmailFormatter rf = ResultFormatterFactory.getRcResultEmailFormatter(rc);

            String content = rf.getResultEmailContent(rc, locale );
            LogService.logIt( "AdminRefUtils.processGenRefResultReportOnline() result content string: " + content  + ", rcCheckId=" + rcCheckId);
            
            if( content==null || content.isBlank() )
                this.setStringErrorMessage("AdminRefUtils.processGenRefResultReportOnline() report content is empty." );
            
            adminRefBean.setStrParam1(content);
            return null;            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AdminRefUtils.processGenRefResultReportOnline()" );
            setMessage(e);
            return null;
        }
    }

    
    
}
