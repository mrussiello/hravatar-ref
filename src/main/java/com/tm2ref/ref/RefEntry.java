/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.corp.CorpBean;
import com.tm2ref.corp.CorpUtils;
import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcOrgPrefs;
import com.tm2ref.entity.ref.RcScript;
import com.tm2ref.entity.ref.RcSuborgPrefs;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.OrgAutoTest;
import com.tm2ref.entity.user.Suborg;
import com.tm2ref.entity.user.User;
import com.tm2ref.event.EventFacade;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.StringUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Dad
 */
@Named
@RequestScoped
public class RefEntry
{
    String cp,rc,rr,ac,tk,cuid,rcsid;
    int rut,orgAutoTestId;

    @Inject
    RefBean refBean;
    
    @Inject
    RefUtils refUtils;
    
    RcFacade rcFacade;

     protected void init()
     {
     }



     // Called when candidate.references.xhtml unloads to trigger process to ensure that all Raters have been sent.
     //
     public void doCoreExitEntry()
     {
        // LogService.logIt( "RefEntry.doCoreExitEntry() START rc=" + rc + ", rr=" + rr );
        long rcCheckId=0;
        long rcRaterId=0;
        String nextViewId = null;
         
        try
        {
             if( rc!=null && !rc.isBlank() )
                 rcCheckId = Long.parseLong( EncryptUtils.urlSafeDecrypt(rc) );

             if( rr!=null && !rr.isBlank() )
                 rcRaterId = Long.parseLong( EncryptUtils.urlSafeDecrypt(rr) );
        }
        catch( NumberFormatException e )
        {
             LogService.logIt( "RefEntry.doCoreExitEntry() AAA rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", " + e.toString() );
        }
        catch( Exception e )
        {
             LogService.logIt( e, "RefEntry.doCoreExitEntry() AAA.1 rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );
        }
         
        try
        {
            nextViewId = refUtils.performCoreExitEntry( rcCheckId, rcRaterId, rut );

            // LogService.logIt( "RefEntry.doCoreExitEntry() AAA.1 rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId +  ", nextViewId=" + nextViewId );

            //if( nextViewId != null )
            //    navigateTo( nextViewId );         
        }
        catch( Exception e )
        {
             LogService.logIt( e, "RefEntry.doCoreExitEntry() BBB rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );
        }         
     }
     

     public void doTestKeyRefEntry()
     {
        // LogService.logIt( "RefEntry.doTestKeyRefEntry() START tk=" + (tk==null ? "null" : tk )+ ", cuid=" + (cuid==null ? "null" : cuid) + ", rcsid=" + (rcsid==null ? "null" : rcsid) + ", orgAutoTestId=" + orgAutoTestId );
        long testKeyId=0;
        long userId=0;
        int rcScriptId=0;
        String nextViewId = "/index.xhtml";
         
        try
        {
             if( tk!=null && !tk.isBlank() )
                 testKeyId = Long.parseLong( EncryptUtils.urlSafeDecrypt(tk) );

             if( cuid!=null && !cuid.isBlank() )
                 userId = Long.parseLong( EncryptUtils.urlSafeDecrypt(cuid) );

             if( rcsid!=null && !rcsid.isBlank() )
                 rcScriptId = Integer.parseInt( EncryptUtils.urlSafeDecrypt(rcsid) );
             
        }
        catch( NumberFormatException e )
        {
             LogService.logIt( "RefEntry.doTestKeyRefEntry() AAA testKeyId=" + testKeyId + ", userId=" + userId + ", rcScriptId=" + rcScriptId + ", " + e.toString() );
        }
        catch( Exception e )
        {
             LogService.logIt( e, "RefEntry.doTestKeyRefEntry() AAA.1 testKeyId=" + testKeyId + ", userId=" + userId + ", rcScriptId=" + rcScriptId );
        }
         
        try
        {
            if( !refUtils.getNewRefStartsOk() )
            {
                CorpBean corpBean = CorpBean.getInstance();
                
                if( !corpBean.getHasCorp() )
                    corpBean.loadDefaultCorp();
                                
                navigateTo( corpBean.getCorp().getOfflinePage() ); //"/ref/offline.xhtml" );
                return;                
            }
            
            if( testKeyId<=0 && userId<=0 )
            {
                navigateTo( "/index.xhtml" );
                return;
            }
            
            // LogService.logIt( "RefEntry.doTestKeyRefEntry() AAA.1 testKeyId=" + testKeyId + ", userId=" + userId + ", rcScriptId=" + rcScriptId );
            
            Tracker.addTestKeyEntry();

            RcCheck rcc = null;
            
            if( testKeyId>0 )
                rcc = findOrCreateRcCheckFromTestKey( testKeyId );
           
            if( rcc==null && userId>0 && rcScriptId>0 )
                rcc = findOrCreateRcCheckForUserAndScript( userId, rcScriptId );
            
            if( rcc==null )
                throw new Exception( "RefEntry.doTestKeyRefEntry() AAA.0 No RcCheck either found or created for TestKeyId=" + testKeyId + ", userId=" + userId + ", rcScriptId=" + rcScriptId );
            else
                LogService.logIt("RefEntry.doTestKeyRefEntry() AAA.1 RcCheck matching TestKeyId=" + testKeyId + ", userId=" + userId + ", rcScriptId=" + rcScriptId + " is rcCheckId=" + rcc.getRcCheckId() );
             
            nextViewId = refUtils.performSimpleEntry(rcc.getCorpId(), rcc.getRcCheckId(), 0, null, false );
            
            nextViewId = conditionUrlForSessionLossGet(nextViewId);
            // LogService.logIt( "RefEntry.doTestKeyRefEntry() AAA.1c nextViewId=" + nextViewId );

            if( nextViewId != null )
                navigateTo( nextViewId );
         
        }
        catch( Exception e )
        {
             LogService.logIt( e, "RefEntry.doTestKeyRefEntry() BBB testKeyId=" + testKeyId );
        }
     }
     
     private RcCheck findOrCreateRcCheckFromTestKey( long testKeyId ) throws Exception
     {
         
         try
         {
            if( testKeyId<=0 )
                throw new Exception( "TestKey Invalid. TestKeyId=" + testKeyId );

            if( rcFacade==null )
                rcFacade = RcFacade.getInstance();        
            
            RcCheck rcx = rcFacade.getRcCheckForTestKeyId(testKeyId);
            if( rcx!=null )
            {
                LogService.logIt( "RefEntry.findOrCreateRcCheckFromTestKey() Found existing RC Check for testKeyId=" + testKeyId + ", rcCheckId=" + rcx.getRcCheckId() );
                return rcx;
            }
            
            LogService.logIt( "RefEntry.findOrCreateRcCheckFromTestKey() AAA Creating a new RcCheck. testKeyId=" + testKeyId );
            // get the test key
            EventFacade eventFacade = EventFacade.getInstance();
            TestKey tkx = eventFacade.getTestKey(testKeyId);
            if( tkx==null )
                throw new Exception( "TestKey not found for TestKeyId=" + testKeyId );
            UserFacade userFacade = UserFacade.getInstance();
            User user = userFacade.getUser( tkx.getUserId() );
            Org org = userFacade.getOrg( tkx.getOrgId() );
            RcOrgPrefs rcop = rcFacade.getRcOrgPrefsForOrgId( tkx.getOrgId() );
            if( rcop==null )
            {
                LogService.logIt( "RefEntry.findOrCreateRcCheckFromTestKey() Creating a new RcOrgPrefs for OrgId=" + tkx.getOrgId() + ", testKeyId=" + testKeyId );
                rcop = new RcOrgPrefs();
            }
            
            RcSuborgPrefs rsop = null;            
            if( tkx.getSuborgId()>0 )
                rsop = rcFacade.getRcSuborgPrefsForSuborgId(tkx.getSuborgId() );
            
            int rcScriptId = tkx.getRcScriptId();
            if( rcScriptId<=0 )
                throw new Exception( "RcScriptId invalid: " + rcScriptId );

            RcScriptFacade rcScriptFacade = RcScriptFacade.getInstance();
            RcScript rcScript = rcScriptFacade.getRcScript(rcScriptId, true );
            if( rcScript==null )
                throw new Exception( "RcScript not found." );

            
            rcx = new RcCheck();
            rcx.setUserId( tkx.getUserId() );
            rcx.setOrgId( tkx.getOrgId() );
            rcx.setSuborgId( tkx.getSuborgId() );
            rcx.setTestKeyId(testKeyId);
            rcx.setRcScriptId(rcScriptId);
            rcx.setEmailReportsToCandidate(rcScript.getEmailReportsToCandidate());
            rcx.setJobTitle( rcScript.getName() );
            rcx.setLangCode( rcScript.getLangCode() );
            rcx.setCreateDate( new Date() );
            rcx.setAdminUserId( tkx.getAuthorizingUserId() );
            rcx.setRcCheckTypeId( RcCheckType.PREHIRE.getRcCheckTypeId() );
            rcx.setEmailResultsTo( tkx.getEmailResultsTo() );
            rcx.setTextResultsTo( tkx.getTextResultsTo() );
            
            //if( (rc.getEmailResultsTo()!=null && !rc.getEmailResultsTo().isBlank()) || (rc.getTextResultsTo()!=null && !rc.getTextResultsTo().isBlank()) )
            //    rc.setDistributionTypeId( RcDistributionType.EACH_RATER.getRcDistributionTypeId() );
            
            rcx.setReturnUrl( tkx.getReturnUrl() );
            rcx.setCreditId( tkx.getCreditId() );
            rcx.setCreditIndex( tkx.getCreditIndex() );
            rcx.setExtRef( tkx.getExtRef() );
            rcx.setCorpId( rsop!=null && rsop.getCorpId()>=0 ? rsop.getCorpId() : rcop.getCorpId() );
            rcx.setCandidateCannotAddRaters( rsop!=null && rsop.getCandidateCannotAddRaters()>=0 ? rsop.getCandidateCannotAddRaters() : rcop.getCandidateCannotAddRaters() );
            rcx.setCollectCandidateRatings( rsop!=null && rsop.getCollectCandidateRatings()>=0 ? rsop.getCollectCandidateRatings() : rcop.getCollectCandidateRatings() );
            rcx.setCandidateOneRaterNoSend( rcx.getCandidateCannotAddRaters()==1 ? 0 : rcop.getCandidateOneRaterNoSend() );
            
            // LogService.logIt( "RefEntry.findOrCreateRcCheckFromTestKey() rsop=" + (rsop==null ? "null" : "not null, distid=" + rsop.getDistributionTypeId() ) + ", rcop.distid=" + rcop.getDistributionTypeId() );
            
            rcx.setDistributionTypeId( rsop!=null && rsop.getDistributionTypeId()>=0 ? rsop.getDistributionTypeId() : rcop.getDistributionTypeId() );
            rcx.setReminderTypeId( rsop!=null && rsop.getReminderTypeId()>=0 ? rsop.getReminderTypeId() : rcop.getReminderTypeId() );
            rcx.setCandidatePhotoCaptureTypeId(rsop!=null && rsop.getCandidatePhotoCaptureTypeId()>=0 ? rsop.getCandidatePhotoCaptureTypeId() : rcop.getCandidatePhotoCaptureTypeId() );
            rcx.setRaterPhotoCaptureTypeId(rsop!=null && rsop.getRaterPhotoCaptureTypeId()>=0 ? rsop.getRaterPhotoCaptureTypeId() : rcop.getRaterPhotoCaptureTypeId() );
            rcx.setAvCommentsTypeId( rcop.getAvCommentsTypeId() );
            rcx.setDisallowReentry( rcop.getDisallowReentry() );
            rcx.setAskForReferrals( rsop!=null && rsop.getAskForReferrals()>=0 ? rsop.getAskForReferrals() :  rcop.getAskForReferrals() );

            rcx.setMinSupervisors( rsop!=null && rsop.getMinSupervisors()>=0 ? rsop.getMinSupervisors() : rcop.getMinSupervisors());
            rcx.setMaxRaters( rsop!=null && rsop.getMaxRaters()>0 ? rsop.getMaxRaters() : rcop.getMaxRaters() );
            rcx.setMinRaters( rsop!=null && rsop.getMinRaters()>0 ? rsop.getMinRaters() : rcop.getMinRaters() );
            
            if( rcx.getMinRaters()<=0 )
                rcx.setMinRaters(1);
            
            rcx.setEnforceRaterLimits( rcop.getEnforceRaterLimits() );

            rcx.setTopBottomCount(rcop.getTopBottomCount() );
            rcx.setTopBottomSrcTypeId(rcop.getTopBottomSrcTypeId() );
            
            rcx.setSendDate( new Date() );
            rcx.setFirstCandidateSendDate( new Date() );
            rcx.setRcCandidateStatusTypeId( RcCandidateStatusType.STARTED.getRcCandidateStatusTypeId() );
            rcx.setRcCheckStatusTypeId( RcCheckStatusType.STARTED.getRcCheckStatusTypeId() );
            
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.DAY_OF_MONTH, 10 );
            rcx.setExpireDate(cal.getTime() );
            
            rcx.setCandidateAccessCode( Integer.toHexString( rcx.getOrgId() ) + "XC" + StringUtils.generateRandomStringForPin( 10 ) );
            RcCheck rcr = rcFacade.getRcCheckByCandidateAccessCode( rcx.getCandidateAccessCode());                
            while( rcr!=null )
            {
                rcx.setCandidateAccessCode( Integer.toHexString( rcx.getOrgId() ) + "XC" + StringUtils.generateRandomStringForPin( 10 ) );  
                rcr = rcFacade.getRcCheckByCandidateAccessCode( rcx.getCandidateAccessCode() );                
            } 
            
            rcx.setUser(user);
            rcx.setOrg(org);
            rcFacade.saveRcCheck( rcx, false );
            LogService.logIt( "RefEntry.findOrCreateRcCheckFromTestKey() BBB Created new RcCheck for testKeyId=" + testKeyId + ", with rcCheckId=" + rcx.getRcCheckId() );            
            return rcx;
         }
         catch( Exception e )
         {
             LogService.logIt(e, "RefEntry.findOrCreateRcCheckFromTestKey() testKeyId=" + testKeyId );
             return null;
         }
     }

     
     private RcCheck findOrCreateRcCheckForUserAndScript( long candidateUserId, int rcScriptId ) throws Exception
     {
         try
         {
            if( candidateUserId<=0 || rcScriptId<=0 )
                throw new Exception( "Information is Invalid. candidateUserId=" + candidateUserId + ", rcScriptId=" + rcScriptId + ", orgAutoTestId=" + orgAutoTestId );

            UserFacade userFacade = UserFacade.getInstance();
            
            EventFacade eventFacade = EventFacade.getInstance();
            OrgAutoTest oat = orgAutoTestId>0 ? eventFacade.getOrgAutoTest( orgAutoTestId ) : null;
            // if( oat==null )
            //     throw new Exception( "OrgAutoTest not found for orgAutoTestId=" + orgAutoTestId );
            
            User user = userFacade.getUser( candidateUserId );
            if( user==null )
                throw new Exception( "User not found for candidateUserId=" + candidateUserId );

            Org org = userFacade.getOrg( user.getOrgId() );

            long adminUserId = oat==null || oat.getAuthUserId()<=0 ? org.getAdminUserId() : oat.getAuthUserId();

            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.MONTH, -6 );
            Date createdAfterDate = cal.getTime();
            
            if( rcFacade==null )
                rcFacade = RcFacade.getInstance();        
            
            RcCheck rcc = rcFacade.getRcCheckForUserIdAndRcScriptId(candidateUserId, adminUserId, rcScriptId, createdAfterDate );
            if( rcc!=null )
            {
                LogService.logIt( "RefEntry.findOrCreateRcCheckForUserAndScript() Found existing RC Check for candidateUserId=" + candidateUserId + ", rcScriptId=" + rcScriptId + ", orgAutoTestId=" + orgAutoTestId + ", createdAfterDate=" + createdAfterDate.toString() + ", rcCheckId=" + rcc.getRcCheckId() );
                return rcc;
            }
            
            LogService.logIt( "RefEntry.findOrCreateRcCheckForUserAndScript() AAA Creating a new RcCheck. candidateUserId=" + candidateUserId + ", rcScriptId=" + rcScriptId + ", orgAutoTestId=" + orgAutoTestId  );
            // get the test key
            RcScript rcScript = RcScriptFacade.getInstance().getRcScript( rcScriptId , false);
            if( rcScript==null )
                throw new Exception( "RcScript not found for rcScriptId=" + rcScriptId );
            
            RcOrgPrefs rcop = rcFacade.getRcOrgPrefsForOrgId( user.getOrgId() );
            if( rcop==null )
            {
                LogService.logIt( "RefEntry.findOrCreateRcCheckForUserAndScript() Creating a new RcOrgPrefs for OrgId=" + user.getOrgId() + ", userId=" + candidateUserId );
                rcop = new RcOrgPrefs();
            }
            
            RcSuborgPrefs rsop = null; 
            Suborg suborg = null;
            if( oat!=null && oat.getSuborgId()>0 )
                suborg = userFacade.getSuborg( oat.getSuborgId() );
            
            if( user.getSuborgId()>0 )
            {
                rsop = rcFacade.getRcSuborgPrefsForSuborgId(user.getSuborgId() );

                if( suborg==null )
                    suborg = userFacade.getSuborg( user.getSuborgId() );
            }
            
                       
            rcc = new RcCheck();
            rcc.setUserId( candidateUserId );
            rcc.setOrgId( user.getOrgId() );
            rcc.setSuborgId( user.getSuborgId() );
            rcc.setTestKeyId(0);
            rcc.setRcScriptId(rcScriptId);
            rcc.setJobTitle( rcScript.getName() );
            rcc.setLangCode( rcScript.getLangCode() );
            rcc.setCreateDate( new Date() );
            rcc.setAdminUserId( oat==null || oat.getAuthUserId()<=0 ? org.getAdminUserId() : oat.getAuthUserId() );
            rcc.setRcCheckTypeId( rcScript.getRcCheckTypeId() );
            rcc.setEmailResultsTo( oat==null ? ( suborg==null || suborg.getEmailResultsTo()==null || suborg.getEmailResultsTo().isBlank() ? org.getEmailResultsTo() : suborg.getEmailResultsTo() ) : oat.getEmailResultsTo() );
            rcc.setTextResultsTo( oat==null ? ( suborg==null || suborg.getTextResultsTo()==null || suborg.getTextResultsTo().isBlank() ? org.getTextResultsTo() : suborg.getTextResultsTo() ) : oat.getTextResultsTo() );
            
            //if( (rcc.getEmailResultsTo()!=null && !rcc.getEmailResultsTo().isBlank()) || (rcc.getTextResultsTo()!=null && !rcc.getTextResultsTo().isBlank()) )
            //    rcc.setDistributionTypeId( RcDistributionType.EACH_RATER.getRcDistributionTypeId() );
            
            rcc.setReturnUrl( org.getDefaultCorpExitUrl() );
            rcc.setCreditId( 0 );
            rcc.setCreditIndex( 0 );
            rcc.setExtRef( null );
            rcc.setCorpId( rsop!=null && rsop.getCorpId()>=0 ? rsop.getCorpId() : rcop.getCorpId() );
            rcc.setCandidateCannotAddRaters( rsop!=null && rsop.getCandidateCannotAddRaters()>=0 ? rsop.getCandidateCannotAddRaters() : rcop.getCandidateCannotAddRaters() );
            rcc.setCollectCandidateRatings( rsop!=null && rsop.getCollectCandidateRatings()>=0 ? rsop.getCollectCandidateRatings() : rcop.getCollectCandidateRatings() );
            
            // LogService.logIt( "RefEntry.findOrCreateRcCheckFromTestKey() rsop=" + (rsop==null ? "null" : "not null, distid=" + rsop.getDistributionTypeId() ) + ", rcop.distid=" + rcop.getDistributionTypeId() );
            
            rcc.setDistributionTypeId( rsop!=null && rsop.getDistributionTypeId()>=0 ? rsop.getDistributionTypeId() : rcop.getDistributionTypeId() );
            rcc.setReminderTypeId( rsop!=null && rsop.getReminderTypeId()>=0 ? rsop.getReminderTypeId() : rcop.getReminderTypeId() );
            rcc.setCandidatePhotoCaptureTypeId(rsop!=null && rsop.getCandidatePhotoCaptureTypeId()>=0 ? rsop.getCandidatePhotoCaptureTypeId() : rcop.getCandidatePhotoCaptureTypeId() );
            rcc.setRaterPhotoCaptureTypeId(rsop!=null && rsop.getRaterPhotoCaptureTypeId()>=0 ? rsop.getRaterPhotoCaptureTypeId() : rcop.getRaterPhotoCaptureTypeId() );
            rcc.setAvCommentsTypeId( rcop.getAvCommentsTypeId() );
            rcc.setMinSupervisors( rsop!=null && rsop.getMinSupervisors()>=0 ? rsop.getMinSupervisors() : rcop.getMinSupervisors());
            rcc.setMaxRaters( rsop!=null && rsop.getMaxRaters()>=0 ? rsop.getMaxRaters() : rcop.getMaxRaters() );
            rcc.setMinRaters( rsop!=null && rsop.getMinRaters()>0 ? rsop.getMinRaters() : rcop.getMinRaters() );
            
            if( rcc.getMinRaters()<=0 )
                rcc.setMinRaters(1);
            
            rcc.setSendDate( new Date() );
            rcc.setFirstCandidateSendDate( new Date() );
            rcc.setRcCandidateStatusTypeId( RcCandidateStatusType.STARTED.getRcCandidateStatusTypeId() );
            rcc.setRcCheckStatusTypeId( RcCheckStatusType.STARTED.getRcCheckStatusTypeId() );
            
            cal = new GregorianCalendar();
            cal.add( Calendar.DAY_OF_MONTH, 10 );
            rcc.setExpireDate(cal.getTime() );
            
            rcc.setCandidateAccessCode( Integer.toHexString( rcc.getOrgId() ) + "XC" + StringUtils.generateRandomStringForPin( 10 ) );
            RcCheck rcr = rcFacade.getRcCheckByCandidateAccessCode( rcc.getCandidateAccessCode());                
            while( rcr!=null )
            {
                rcc.setCandidateAccessCode( Integer.toHexString( rcc.getOrgId() ) + "XC" + StringUtils.generateRandomStringForPin( 10 ) );  
                rcr = rcFacade.getRcCheckByCandidateAccessCode( rcc.getCandidateAccessCode() );                
            } 
            
            rcc.setUser(user);
            rcc.setOrg(org);
            rcFacade.saveRcCheck( rcc, false );
            LogService.logIt( "RefEntry.findOrCreateRcCheckForUserAndScript() BBB Created new RcCheck for candidateUserId=" + candidateUserId + ", rcScriptId=" + rcScriptId + ", orgAutoTestId=" + orgAutoTestId + ", with rcCheckId=" + rcc.getRcCheckId() );            
            return rcc;
         }
         catch( Exception e )
         {
             LogService.logIt(e, "RefEntry.findOrCreateRcCheckForUserAndScript() candidateUserId=" + candidateUserId + ", rcScriptId=" + rcScriptId + ", orgAutoTestId=" + orgAutoTestId );
             return null;
         }
         
     }
     
     public void doHelpExitEntry()
     {
        LogService.logIt( "RefEntry.doHelpExitEntry() acidx=" + ac + ", refPageTypeId=" + orgAutoTestId );
        // FacesContext fc = FacesContext.getCurrentInstance();
        try
        {
            if( !refUtils.getNewRefStartsOk() )
            {
                CorpBean corpBean = CorpBean.getInstance();                
                if( !corpBean.getHasCorp() )
                    corpBean.loadDefaultCorp();
                                
                navigateTo( corpBean.getCorp().getOfflinePage() ); // "/ref/offline.xhtml" );
                return;                
            }
                        
            CorpUtils cu = CorpUtils.getInstance();
            String nextViewId = cu.processHelpExit();
            
            LogService.logIt( "RefEntry.doHelpExitEntry() AAA.1 after refUtils.doHelpExitEntry() nextViewId=" + nextViewId );

            //if( fc!=null )
            //{
            //    HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
            //    fc.responseComplete();
            //    response.sendRedirect("/tr" + nextViewId);
            //    return;
            //}            
            
            if( nextViewId != null )
                navigateTo( nextViewId );
         
        }
        catch( Exception e )
        {
             LogService.logIt( e, "RefEntry.doHelpExitEntry() XXX.1  acidx=" + ac + ", refPageTypeId=" + orgAutoTestId );
        }         
     }
     
     
     public void doRefReturnEntry()
     {
        LogService.logIt( "RefEntry.doRefReturnEntry() acidx=" + ac + ", refPageTypeId=" + orgAutoTestId );
         
        FacesContext fc = FacesContext.getCurrentInstance();
        try
        {
            if( !refUtils.getNewRefStartsOk() )
            {
                CorpBean corpBean = CorpBean.getInstance();                
                if( !corpBean.getHasCorp() )
                    corpBean.loadDefaultCorp();
                                
                navigateTo( corpBean.getCorp().getOfflinePage() ); // "/ref/offline.xhtml" );
                return;                
            }
                        
            String nextViewId = refUtils.processReturnToRefCheckProcess();
            
            Tracker.addSimpleEntry();

            LogService.logIt( "RefEntry.doRefReturnEntry() AAA.1 after refUtils.processReturnToRefCheckProcess() nextViewId=" + nextViewId );

            if( fc!=null )
            {
                HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
                fc.responseComplete();
                response.sendRedirect("/tr" + nextViewId);
                return;
            }            
            
            if( nextViewId != null )
                navigateTo( nextViewId );
         
        }
        catch( Exception e )
        {
             LogService.logIt( e, "RefEntry.doRefReturnEntry() XXX.1  acidx=" + ac + ", refPageTypeId=" + orgAutoTestId );
        }         
     }
     
     
     
     public void doSimpleRefEntry()
     {
        LogService.logIt( "RefEntry.doSimpleRefEntry() START cp=" + cp + ", rc=" + rc + ", rr=" + rr + ", ac=" + ac );
        int corpId = 0;
        long rcCheckId=0;
        long rcRaterId=0;
        String nextViewId = null;
         
        try
        {
            //if( 1==1 )
            //    throw new Exception( "TESTING ONLY RefEntry.doSimpleRefEntry()");
            
             if( cp!=null && !cp.isBlank() )
                 corpId = Integer.parseInt( EncryptUtils.urlSafeDecrypt(cp) );

             if( rc!=null && !rc.isBlank() )
                 rcCheckId = Long.parseLong( EncryptUtils.urlSafeDecrypt(rc) );

             if( rr!=null && !rr.isBlank() )
                 rcRaterId = Long.parseLong( EncryptUtils.urlSafeDecrypt(rr) );
        }
        catch( NumberFormatException e )
        {
             LogService.logIt( "RefEntry.doSimpleRefEntry() AAA corpId=" + corpId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", accessCode=" + ac + ", " + e.toString() );
        }
        catch( Exception e )
        {
             LogService.logIt( e, "RefEntry.doSimpleRefEntry() AAA.1 corpId=" + corpId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", accessCode=" + ac );
        }
         
        try
        {
            if( !refUtils.getNewRefStartsOk() )
            {
                CorpBean corpBean = CorpBean.getInstance();                
                if( !corpBean.getHasCorp() )
                    corpBean.loadDefaultCorp();
                                
                navigateTo( corpBean.getCorp().getOfflinePage() ); // "/ref/offline.xhtml" );
                return;                
            }
            
            
            nextViewId = refUtils.performSimpleEntry(corpId, rcCheckId, rcRaterId, ac, false );
            
            nextViewId = conditionUrlForSessionLossGet(nextViewId);
            
            Tracker.addSimpleEntry();

            LogService.logIt( "RefEntry.doSimpleRefEntry() AAA.1 nextViewId=" + nextViewId );

            if( nextViewId != null )
                navigateTo( nextViewId );
         
        }
        catch( Exception e )
        {
             LogService.logIt( e, "RefEntry.doSimpleRefEntry() BBB corpId=" + corpId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );
        }
     }

     
    public void navigateTo( String viewId ) throws Exception
    {
        //LogService.logIt( "TestEntry.navigateTo() nextViewId=" + viewId  );
        
        if( viewId.indexOf( "http" )==0  )
            sendRedirect( viewId );

        else
        {
            //LogService.logIt( "RefEntry.navigateTo() req.getRequestURL()=" + req.getRequestURL().toString() );     
            
            ConfigurableNavigationHandler nav  = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
            nav.performNavigation( viewId );
        }
    }
    
    
    public void sendRedirect( String viewId ) throws Exception
    {
        String stub = "";

        FacesContext fc = FacesContext.getCurrentInstance();
        try
        {
                HttpServletRequest req =  (HttpServletRequest) fc.getExternalContext().getRequest();

                if( viewId.toLowerCase().indexOf( "http" )<0 )
                {
                    String ra = req.getRequestURL().toString();

                    //LogService.logIt( "TestEntry.sendRedirect() req.getRequestURL()=" + ra );                    
                    int idx = ra.indexOf( "/" , ra.indexOf( "://" )+4 );

                    stub = idx>0 ? ra.substring(0, idx ) : ra;
                    // stub = req.getScheme() + "://" + req.getLocalAddr();
                }
                
                //else
                //    LogService.logIt( "TestEntry.sendRedirect() req.getRequestURL()=" + req.getRequestURL().toString() );                    
                    
        }

        catch( Exception e )
        {
            LogService.logIt( e, "RefEntry.sendRedirect() Redirecting to " + viewId );
            stub="";
        }

        if( fc!=null )
        {
            fc.responseComplete();
            ((HttpServletResponse)fc.getExternalContext().getResponse()).sendRedirect(stub + viewId);
            LogService.logIt( "RefEntry.sendRedirect() Redirecting to stub=" + stub + ", viewId=" + viewId );
        }
        else
            LogService.logIt( "RefEntry.sendRedirect() Could not redirect to stub=" + stub + ", viewId=" + viewId + " because FacesContext is null!");
        
        //LogService.logIt( "RefEntry.sendRedirect() Redirecting to stub=" + stub + ", viewId=" + viewId );
        //HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        //response.sendRedirect(stub + viewId);
    }
    

    public String conditionUrlForSessionLossGet( String url )
    {
        if( refBean==null || refBean.getActiveAccessCodeX()==null || refBean.getActiveAccessCodeX().isBlank() || url==null || url.isBlank() )
            return url;
        
        if( !url.contains( "acidx=") )
            url += (url.contains("?") ? "&" : "?") + "acidx=" + refBean.getActiveAccessCodeX();
        
        if( !url.contains("refpagex=") && refBean.getRefPageType()!=null )
            url += (url.contains("?") ? "&" : "?") + "refpagex=" + refBean.getRefPageType().getRefPageTypeId();

        return url;
    }
    
    
    
     
    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getRc() {
        return rc;
    }

    public void setRc(String rc) {
        this.rc = rc;
    }

    public String getRr() {
        return rr;
    }

    public void setRr(String rr) {
        this.rr = rr;
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public int getRut() {
        return rut;
    }

    public void setRut(int ru) {
        this.rut = ru;
    }

    public String getTk() {
        return tk;
    }

    public void setTk(String tk) {
        this.tk = tk;
    }

    public String getCuid() {
        return cuid;
    }

    public void setCuid(String cuid) {
        this.cuid = cuid;
    }

    public String getRcsid() {
        return rcsid;
    }

    public void setRcsid(String rcsid) {
        this.rcsid = rcsid;
    }

    public int getOrgAutoTestId() {
        return orgAutoTestId;
    }

    public void setOrgAutoTestId(int orgAutoTestId) {
        this.orgAutoTestId = orgAutoTestId;
    }




}