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
    String cp,rc,rr,ac,tk;
    int rut;

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
        LogService.logIt( "RefEntry.doCoreExitEntry() START rc=" + rc + ", rr=" + rr );
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

            LogService.logIt( "RefEntry.doCoreExitEntry() AAA.1 rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId +  ", nextViewId=" + nextViewId );

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
        LogService.logIt( "RefEntry.doTestKeyRefEntry() START tk=" + tk );
        long testKeyId=0;
        String nextViewId = "/index.xhtml";
         
        try
        {
             if( tk!=null && !tk.isBlank() )
                 testKeyId = Long.parseLong( EncryptUtils.urlSafeDecrypt(tk) );
        }
        catch( NumberFormatException e )
        {
             LogService.logIt( "RefEntry.doTestKeyRefEntry() AAA testKeyId=" + testKeyId + ", " + e.toString() );
        }
        catch( Exception e )
        {
             LogService.logIt( e, "RefEntry.doTestKeyRefEntry() AAA.1 testKeyId=" + testKeyId );
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
            
            if( testKeyId<=0 )
            {
                navigateTo( "/index.xhtml" );
                return;
            }
            
            LogService.logIt( "RefEntry.doTestKeyRefEntry() AAA.1 testKeyId=" + testKeyId );
            
            Tracker.addTestKeyEntry();

            RcCheck rc = findOrCreateRcCheckFromTestKey( testKeyId );
            
            if( rc==null )
                throw new Exception( "RefEntry.doTestKeyRefEntry() AAA.0 No RcCheck either found or created for TestKeyId=" + testKeyId );
            else
                LogService.logIt( "RefEntry.doTestKeyRefEntry() AAA.1 RcCheck matching TestKeyId=" + testKeyId + " is rcCheckId=" + rc.getRcCheckId() );
             
            nextViewId = refUtils.performSimpleEntry(rc.getCorpId(), rc.getRcCheckId(), 0, null, false );
            
            LogService.logIt( "RefEntry.doTestKeyRefEntry() AAA.1 nextViewId=" + nextViewId );

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
            RcCheck rc = rcFacade.getRcCheckForTestKeyId(testKeyId);
            if( rc!=null )
                return rc;
            
            LogService.logIt( "RefEntry.findOrCreateRcCheckFromTestKey() AAA Creating a new RcCheck. testKeyId=" + testKeyId );
            // get the test key
            EventFacade eventFacade = EventFacade.getInstance();
            TestKey tk = eventFacade.getTestKey(testKeyId);
            if( tk==null )
                throw new Exception( "TestKey not found for TestKeyId=" + testKeyId );
            UserFacade userFacade = UserFacade.getInstance();
            User user = userFacade.getUser( tk.getUserId() );
            Org org = userFacade.getOrg( tk.getOrgId() );
            RcOrgPrefs rcop = rcFacade.getRcOrgPrefsForOrgId( tk.getOrgId() );
            if( rcop==null )
                rcop = new RcOrgPrefs();
            
            RcSuborgPrefs rsop = null;            
            if( tk.getSuborgId()>0 )
                rsop = rcFacade.getRcSuborgPrefsForSuborgId(tk.getSuborgId() );
            
            int rcScriptId = tk.getRcScriptId();
            if( rcScriptId<=0 )
                throw new Exception( "RcScriptId invalid: " + rcScriptId );

            RcScriptFacade rcScriptFacade = RcScriptFacade.getInstance();
            RcScript rcScript = rcScriptFacade.getRcScript( rcScriptId );
            if( rcScript==null )
                throw new Exception( "RcScript not found." );

            
            rc = new RcCheck();
            rc.setUserId( tk.getUserId() );
            rc.setOrgId( tk.getOrgId() );
            rc.setSuborgId( tk.getSuborgId() );
            rc.setTestKeyId(testKeyId);
            rc.setRcScriptId(rcScriptId);
            rc.setJobTitle( rcScript.getName() );
            rc.setLangCode( rcScript.getLangCode() );
            rc.setCreateDate( new Date() );
            rc.setAdminUserId( tk.getAuthorizingUserId() );
            rc.setRcCheckTypeId( RcCheckType.PREHIRE.getRcCheckTypeId() );
            rc.setEmailResultsTo( tk.getEmailResultsTo() );
            rc.setTextResultsTo( tk.getTextResultsTo() );
            rc.setReturnUrl( tk.getReturnUrl() );
            rc.setCreditId( tk.getCreditId() );
            rc.setCreditIndex( tk.getCreditIndex() );
            rc.setExtRef( tk.getExtRef() );
            rc.setCorpId( rsop!=null && rsop.getCorpId()>=0 ? rsop.getCorpId() : rcop.getCorpId() );
            rc.setCandidateCannotAddRaters( rsop!=null && rsop.getCandidateCannotAddRaters()>=0 ? rsop.getCandidateCannotAddRaters() : rcop.getCandidateCannotAddRaters() );
            rc.setCollectCandidateRatings( rsop!=null && rsop.getCollectCandidateRatings()>=0 ? rsop.getCollectCandidateRatings() : rcop.getCollectCandidateRatings() );
            rc.setDistributionTypeId( rsop!=null && rsop.getDistributionTypeId()>=0 ? rsop.getDistributionTypeId() : rcop.getDistributionTypeId() );
            rc.setReminderTypeId( rsop!=null && rsop.getReminderTypeId()>=0 ? rsop.getReminderTypeId() : rcop.getReminderTypeId() );
            rc.setCandidatePhotoCaptureTypeId(rsop!=null && rsop.getCandidatePhotoCaptureTypeId()>=0 ? rsop.getCandidatePhotoCaptureTypeId() : rcop.getCandidatePhotoCaptureTypeId() );
            rc.setRaterPhotoCaptureTypeId(rsop!=null && rsop.getRaterPhotoCaptureTypeId()>=0 ? rsop.getRaterPhotoCaptureTypeId() : rcop.getRaterPhotoCaptureTypeId() );
            rc.setAvCommentsTypeId( rcop.getAvCommentsTypeId() );
            rc.setMinSupervisors( rsop!=null && rsop.getMinSupervisors()>=0 ? rsop.getMinSupervisors() : rcop.getMinSupervisors());
            rc.setMaxRaters( rsop!=null && rsop.getMaxRaters()>=0 ? rsop.getMaxRaters() : rcop.getMaxRaters() );
            rc.setMinRaters( rsop!=null && rsop.getMinRaters()>=0 ? rsop.getMinRaters() : rcop.getMinRaters() );
            rc.setSendDate( new Date() );
            rc.setFirstCandidateSendDate( new Date() );
            rc.setRcCandidateStatusTypeId( RcCandidateStatusType.STARTED.getRcCandidateStatusTypeId() );
            rc.setRcCheckStatusTypeId( RcCheckStatusType.STARTED.getRcCheckStatusTypeId() );
            
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.DAY_OF_MONTH, 10 );
            rc.setExpireDate(cal.getTime() );
            
            rc.setCandidateAccessCode( Integer.toHexString( rc.getOrgId() ) + "XC" + StringUtils.generateRandomStringForPin( 10 ) );
            RcCheck rcr = rcFacade.getRcCheckByCandidateAccessCode( rc.getCandidateAccessCode());                
            while( rcr!=null )
            {
                rc.setCandidateAccessCode( Integer.toHexString( rc.getOrgId() ) + "XC" + StringUtils.generateRandomStringForPin( 10 ) );  
                rcr = rcFacade.getRcCheckByCandidateAccessCode( rc.getCandidateAccessCode() );                
            } 
            
            rc.setUser(user);
            rc.setOrg(org);
            rcFacade.saveRcCheck( rc, false );
            LogService.logIt( "RefEntry.findOrCreateRcCheckFromTestKey() BBB Created new RcCheck for testKeyId=" + testKeyId + ", with rcCheckId=" + rc.getRcCheckId() );            
            return rc;
         }
         catch( Exception e )
         {
             LogService.logIt(e, "RefEntry.findOrCreateRcCheckFromTestKey() testKeyId=" + testKeyId );
             return null;
         }
     }

     
     public void doSimpleRefEntry()
     {
        LogService.logIt( "RefEntry.doSimpleEntry() START cp=" + cp + ", rc=" + rc + ", rr=" + rr + ", ac=" + ac );
        int corpId = 0;
        long rcCheckId=0;
        long rcRaterId=0;
        String nextViewId = null;
         
        try
        {
             if( cp!=null && !cp.isBlank() )
                 corpId = Integer.parseInt( EncryptUtils.urlSafeDecrypt(cp) );

             if( rc!=null && !rc.isBlank() )
                 rcCheckId = Long.parseLong( EncryptUtils.urlSafeDecrypt(rc) );

             if( rr!=null && !rr.isBlank() )
                 rcRaterId = Long.parseLong( EncryptUtils.urlSafeDecrypt(rr) );
        }
        catch( NumberFormatException e )
        {
             LogService.logIt( "RefEntry.doSimpleEntry() AAA corpId=" + corpId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", accessCode=" + ac + ", " + e.toString() );
        }
        catch( Exception e )
        {
             LogService.logIt( e, "RefEntry.doSimpleEntry() AAA.1 corpId=" + corpId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", accessCode=" + ac );
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
            Tracker.addSimpleEntry();

            LogService.logIt( "RefEntry.doSimpleEntry() AAA.1 nextViewId=" + nextViewId );

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

        try
        {
                HttpServletRequest req =  (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

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

        LogService.logIt( "RefEntry.sendRedirect() Redirecting to stub=" + stub + ", viewId=" + viewId );
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.sendRedirect(stub + viewId);
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




}