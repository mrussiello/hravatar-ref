package com.tm2ref.ref;

import com.tm2ref.corp.CorpBean;
import com.tm2ref.corp.CorpUtils;
import com.tm2ref.cscase.CsCaseUtils;
import com.tm2ref.entity.corp.Corp;
import com.tm2ref.entity.cscase.CSCase;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.User;
import com.tm2ref.faces.HttpReqUtils;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.CookieUtils;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.StringUtils;
import java.util.Date;
import jakarta.faces.context.FacesContext;


import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.HttpServletResponse;


@Named
@RequestScoped
public class RefUtils extends BaseRefUtils
{

    public static RefUtils getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return (RefUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "refUtils" );
    }

    public long getRcCheckIdFmRequest()
    {
        return getEncryptedIdFmRequest( "rcide" );
    }

    public long getRcRaterIdFmRequest()
    {
        return getEncryptedIdFmRequest( "rcride" );
    }



    public String performCoreExitEntry( long rcCheckId, long rcRaterId, int refUserTypeId )
    {
        String nextViewId = "/ref/index.xhtml";
        getRefBean();
        try
        {
            // LogService.logIt( "RefUtils.performCoreExitEntry() AAA.1 rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", refUserTypeId=" + refUserTypeId );
            RcCheck rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            RefUserType refUserType = refBean.getRefUserType();
            if( refUserType==null )
            {
                refUserType = RefUserType.getValue(refUserTypeId);
                refBean.setRefUserType(refUserType);
            }

            if( refUserType.getIsCandidate() )
            {
                CandidateRefUtils candidateRefUtils = CandidateRefUtils.getInstance();
                candidateRefUtils.doExitCoreBeacon( rcCheckId );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( "RefUtils.performCoreExitEntry() rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );
        }

        return nextViewId;
    }


    public String performSimpleEntry( int corpId, long rcCheckId, long rcRaterId, String accessCode, boolean adminOverride )
    {
        String nextViewId = "/ref/index.xhtml";
        try
        {
            // LogService.logIt("RefUtils.performSimpleEntry() AAA.1 corpId=" + corpId + ", rcRaterId=" + rcRaterId + ", accessCode=" + accessCode );
            getRefBean();
            getCorpBean();

            refBean.clearBean();

            if( !getNewRefStartsOk() )
            {
                if( !corpBean.getHasCorp() )
                    corpBean.loadDefaultCorp();

                return corpBean.getCorp().getOfflinePage(); // "/ref/offline.xhtml";
            }


            RcCheck rc = null;
            RcRater rcRater = null;
            RefUserType refUserType = RefUserType.RATER;

            if( rcRaterId>0 )
            {
                if( rcFacade==null )
                    rcFacade = RcFacade.getInstance();
                rcRater = rcFacade.getRcRater(rcRaterId, true );
                if( rcRater==null )
                    throw new Exception( "RcRater not found for RcRaterId=" + rcRaterId + ", rcCheckId=" + rcCheckId );
                if( rcCheckId>0 && rcRater.getRcCheckId()!=rcCheckId )
                    throw new Exception( "RcCheckId mismatch. RcRater.rcCheckId=" + rcRater.getRcCheckId() + ", but rcCheckId=" + rcCheckId );

                // clone to prevent crashes on transient data.
                rcRater = (RcRater) rcRater.clone();
                rcCheckId=rcRater.getRcCheckId();
                refUserType = RefUserType.RATER;

            }

            if( rcCheckId>0 )
            {
                if( rcFacade==null )
                    rcFacade = RcFacade.getInstance();
                rc = rcFacade.getRcCheck(rcCheckId, true );
                if( rc==null )
                    throw new Exception( "RcCheck not found for RcCheckId=" + rcCheckId );

                // clone to prevent crashes on transient data.
                rc = (RcCheck) rc.clone();
                rc.setRcRater(rcRater);
                if( rcRater==null )
                    refUserType = RefUserType.CANDIDATE;
            }

            if( rc==null && accessCode!=null && !accessCode.isBlank() )
            {
                accessCode = RcCheckUtils.conditionAccessCode(accessCode);

                // includes clone
                rc = lookupAccessCode( accessCode );
                if( rc!=null )
                {
                    // clone to prevent crashes on transient data.
                    refUserType = rc.getRcRater()==null ? RefUserType.CANDIDATE : RefUserType.RATER;
                }
            }

            // Load Corp. Do this BEFORE you process the Entry
            if( rc!=null && rc.getCorpId()>0 )
                corpId = rc.getCorpId();
            if( corpUtils==null )
                corpUtils = CorpUtils.getInstance();
            corpBean.clearBean();
            corpUtils.loadCorpIfNeeded(corpId, true, getHttpServletResponse() );

            if( rc!=null )
            {
                nextViewId = performRcCheckStart( rc, refUserType, false, adminOverride );
            }
        }
        catch( Exception e )
        {
            LogService.logIt("RefUtils.performSimpleEntry()  corpId=" + corpId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );
            if( corpBean!=null && !corpBean.getHasCorp() )
                corpBean.loadDefaultCorp();
        }

        return nextViewId;
    }


    public String getViewportWidth()
    {
        return  getViewportMinWidth()>0 && 1==2 ? Integer.toString( getViewportMinWidth() ) : "device-width";
    }

    public int getViewportMinWidth()
    {
        // if in test with an IMO, get the IMO size.
        // return 980;
        getRefBean();
        getCorpBean();

        RefPageType rpt = refBean.getRefPageType();
        if( rpt==null )
            return 0;

        if( !rpt.getIsCore() && !rpt.getIsCore2() && !rpt.getIsCore3() )
            return 0;

        return 0; //  820;
    }




    public String processMediaRecEntry()
    {
        getRefBean();
        getCorpBean();
        getProctorBean();
        RcCheck rc = refBean.getRcCheck(); //

        try
        {
            if( rc==null )
            {
                LogService.logIt( "RefUtils.processMediaRecEntry() No RcCheck found in refBean. Returning to Access page." );
                return "/misc/error/errorsession.xhtml";
            }
            //if( !getNewTestStartsOk() && corpBean.getHasCorp() )
            //    return corpBean.getCorp().getOfflinePage();
            if( refBean.getRecDevs()<2 && !proctorBean.getCameraOptOut() )
            {
                LogService.logIt( "RefUtils.processMediaRecEntry() User device does not have a detectable camera. rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()));
                RefUserType refUserType = refBean.getRefUserType();
                proctorBean.init( rc, refUserType );
                if( refUserType.getIsCandidate() && rc.getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture())
                {
                    if( rc.getRcCandidatePhotoCaptureType().getIsRequired() )
                        return "/pp/camera-required.xhtml";
                    else
                        return "/pp/camera-optional.xhtml";
                }
                else if( refUserType.getIsRater() && rc.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture())
                {
                    if( rc.getRcRaterPhotoCaptureType().getIsRequired() )
                        return "/pp/camera-required.xhtml";
                    else
                        return "/pp/camera-optional.xhtml";
                }
            }

            return getViewFromPageType( refBean.getRefPageType() );
            // return getNextViewForCorp(); // getNextViewForTestingProcess( tk );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processMediaRecEntry() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) );
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString(), null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public String processEnterAccessCode()
    {
        getCorpBean();
        getRefBean();

        String accessCode = refBean==null ? null : refBean.getAccessCode();
        String email = null;
        refBean.clearBean();
        RcCheck rc = null;

        // LogService.logIt( "RefUtils.processEnterAccessCode() accessCode " + accessCode );

        try
        {
            if( accessCode==null || accessCode.isBlank() )
                throw new STException( "g.AccessCodeMissing" );

            if( EmailUtils.validateEmailNoErrors(accessCode))
            {
                email = accessCode.toLowerCase().trim();
                if( email!=null && !email.isBlank() )
                {
                    // see if there's an incomplete.
                    rc = lookupByEmail(email, false);

                    // no incomplete, look for a complete.
                    if( rc==null )
                        rc = lookupByEmail(email, true);
                    
                    if( rc ==null )
                        throw new STException( "g.AccessCodeEmailInvalid", new String[]{email} );
                }
            }
            
            

            // Includes clone
            
            if( rc==null )  
            {
                accessCode = RcCheckUtils.conditionAccessCode(accessCode);
                rc = lookupAccessCode( accessCode );
            }

            if( rc==null )
                throw new STException( "g.AccessCodeEnteredInvalid", new String[]{accessCode} );

            // rc = (RcCheck) rc.clone();

            // Load Corp. Do this before Entry.
            if( rc.getCorpId()>0 && corpBean.getCorp().getCorpId()!=rc.getCorpId() )
            {
                if( corpUtils==null )
                    corpUtils = CorpUtils.getInstance();
                corpBean.clearBean();
                corpUtils.loadCorpIfNeeded(rc.getCorpId(), true, getHttpServletResponse() );
            }

            if( !getNewRefStartsOk() && corpBean.getHasCorp() )
                return corpBean.getCorp().getOfflinePage();

            RefUserType refUserType = rc.getRcRater()!=null && !rc.getRcRater().getIsCandidateOrEmployee() ? RefUserType.RATER : RefUserType.CANDIDATE ;
            return performRcCheckStart(rc, refUserType, false, false );
        }

        catch( STException e )
        {
            setMessage( e );
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processEnterAccessCode() " );
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString(), null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }




    public String processConfirmFailFormSubmit()
    {
        getCorpBean();
        getRefBean();

        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc==null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
                throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );

            if( refBean.getStrParam1()==null || refBean.getStrParam1().isBlank() || refBean.getStrParam2()==null || refBean.getStrParam2().isBlank() )
                throw new STException( "g.ErrNameReqd" );

            if( refBean.getStrParam3()==null || refBean.getStrParam3().isBlank()  )
                throw new STException( "g.ErrEmailReqd" );

            int issueTypeId = refBean.getIntParam1();

            if( issueTypeId==1 )
            {
                User tgtUser = null;
                RcRater tgtRcRater = null;

                if( isUserMatch( rc.getUser(), refBean.getStrParam2(), refBean.getStrParam3() ))
                    tgtUser = rc.getUser();

                else
                {
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();

                    for( RcRater r : rcFacade.getRcRaterList(rc.getRcCheckId()) )
                    {
                        if( userFacade==null )
                            userFacade=UserFacade.getInstance();
                        r.setUser( userFacade.getUser( r.getUserId() ));
                        if( isUserMatch( r.getUser(), refBean.getStrParam2(), refBean.getStrParam3() ) )
                        {
                            tgtUser = rc.getUser();
                            break;
                        }
                    }
                }

                // We found a matching User
                if( tgtUser!=null )
                {
                    String tgtAccessCode = null;
                    String role = null;
                    if( tgtRcRater!=null )
                    {
                        tgtAccessCode = tgtRcRater.getRaterAccessCode();
                        role = MessageFactory.getStringMessage(getLocale(), "g.Rater" );
                    }
                    else
                    {
                        tgtAccessCode = rc.getCandidateAccessCode();
                        role = MessageFactory.getStringMessage(getLocale(), "g.CandidateSubj" + (rc.getRcCheckType().getIsEmployeeFeedback() ? ".employee" : "" ) );
                    }

                    setInfoMessage( "g.ConfFailFoundUserX", new String[] { getRcCheckTypeName(), rc.getUser().getFullname(), tgtUser.getFullname(), role } );
                    refBean.setStrParam6( role );
                    refBean.setTgtUser(tgtUser);
                    refBean.setStrParam5(tgtAccessCode);
                    return "/ref/confirm-found-user.xhtml";
                }
            }

            // At this point, we should create a CS Case and punt.
            if( !refBean.getAdminOverride() )
                sendCsCaseEmailFromForm( "RefUtils.processConfirmFailFormSubmit() ", true );

            setInfoMessage( "g.ErrCsCaseSent", null );

            refBean.clearBean();
            Org o = rc.getOrg();
            if( o==null )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                o=userFacade.getOrg( rc.getOrgId() );
            }
            if( o.getDefaultCorpExitUrl()!=null && !o.getDefaultCorpExitUrl().isBlank() )
            {
                FacesContext fc = FacesContext.getCurrentInstance();
                ((HttpServletResponse)  fc.getExternalContext().getResponse()).sendRedirect( o.getDefaultCorpExitUrlHttp() );
                return null;
            }

            return "/ref/index.xhtml";
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processConfirmFailFormSubmit() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public String processConfirmFailConfirmFound()
    {
        getCorpBean();
        getRefBean();

        if( !booleanParam1 )
        {
            return processExitCheck();
        }

        String accessCode = refBean==null ? null : refBean.getStrParam5();
        refBean.clearBean();
        refBean.setAccessCode(accessCode);
        return processEnterAccessCode();
    }

    private CSCase sendCsCaseEmailFromForm( String systemNote, boolean createRcLogEntry )
    {
        try
        {
            String ipAddress = HttpReqUtils.getClientIpAddress( getHttpServletRequest() );
            String userAgent = getHttpServletRequest()==null ? null : getHttpServletRequest().getHeader("User-Agent" );
            return CsCaseUtils.createCsCaseAndEmailAdmin( refBean.getRcCheck(), refBean.getRefUser(), refBean.getStrParam1() + " " + refBean.getStrParam2(), refBean.getStrParam3(), refBean.getStrParam4(), systemNote, createRcLogEntry, ipAddress, userAgent );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.sendCsCaseEmailFromForm() " );
            return null;
        }
    }

    private boolean isUserMatch( User u, String lastname, String email )
    {
        if( u==null )
            return false;

        if( lastname!=null && u.getLastName().equalsIgnoreCase( lastname ) )
            return true;

        if( email!=null && u.getEmail().equalsIgnoreCase( email )  )
            return true;

        return false;
    }




    public String processExitCheck()
    {
        getCorpBean();
        getRefBean();

        RcCheck rc = null;
        String returnUrl = null; //  refBean!=null && refBean.getRefUserType()!=null && refBean.getRefUserType().getIsCandidate() ? rc.getReturnUrl() : null;

        try
        {
            rc = refBean.getRcCheck();
            if( rc==null )
                rc = repairRefBeanForCurrentAction(refBean, true );

            if( rc==null )
            {
                LogService.logIt( "RefUtils.processExitCheck() NONFATAL Unable to obtain RcCheck even after Repairing. exiting." );
                refBean.clearBean();
                CookieUtils.removeRcCheckCookie( getHttpServletResponse() );
                return CorpUtils.getInstance().processCorpHome();
            }

            Org o = rc.getOrg();
            if( o==null )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                o=userFacade.getOrg( rc.getOrgId() );
            }

            returnUrl = refBean!=null && refBean.getRefUserType()!=null && refBean.getRefUserType().getIsCandidate() ? rc.getReturnUrl() : null;

            if( returnUrl==null || returnUrl.isBlank() )
            {
                if( refBean!=null && refBean.getRefUserType()!=null && refBean.getRefUserType().getIsCandidate() )
                {
                    //if( rc.getOrg().getDefaultCorpExitUrl()!=null && !rc.getOrg().getDefaultCorpExitUrl().isBlank() )
                    //    returnUrl = rc.getOrg().getDefaultCorpExitUrlHttp();

                    CorpBean cb = CorpBean.getInstance();
                    if( cb.getHasCorp() && cb.getCorp().getDefaultReturnUrl()!=null && !cb.getCorp().getDefaultReturnUrl().isBlank() )
                        returnUrl = cb.getCorp().getDefaultReturnUrl();
                }
                // rater
                else
                {
                    CorpBean cb = CorpBean.getInstance();
                    if( cb.getHasCorp() && cb.getCorp().getDefaultReturnUrl()!=null && !cb.getCorp().getDefaultReturnUrl().isBlank() )
                        returnUrl = cb.getCorp().getDefaultReturnUrl();
                    else
                        returnUrl = RuntimeConstants.getStringValue( "baseurl" ) +  "/ref/thank-you.xhtml";
                }
            }

            // LogService.logIt( "RefUtils.processExitCheck() BBB.0 ReturnUrl=" + returnUrl + ", corp=" + (corpBean.getCorp()==null ? "null" : corpBean.getCorp().getName()) );

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
            {
                // if value if real, something odd. Go home.
                if( rcChkReq>0 )
                {
                    LogService.logIt( "RefUtils.processExitCheck() BBB.1 NONFATAL RcCheckId in request does not match. Value in request=" + rcChkReq + ", rcCheckId=" + rc.getRcCheckId() );
                    refBean.clearBean();
                    CookieUtils.removeRcCheckCookie( getHttpServletResponse() );
                    return CorpUtils.getInstance().processCorpHome();
                }

                // if value is 0, they may have sent a get. Log it and use the RC if it's there.
                // LogService.logIt( "RefUtils.processExitCheck() BBB.2 NONFATAL RcCheckId in request does not match. Value in request=" + rcChkReq + ", rcCheckId=" + rc.getRcCheckId() );
                // throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rcCheckId=" + rc.getRcCheckId() );
            }

            if( refBean.getRefUserType().getIsRater() )
            {
                long rcRtrReq = getRcRaterIdFmRequest();
                if( rcRtrReq!=rc.getRcRater().getRcRaterId() )
                {
                    if( rcRtrReq>0 )
                    {
                        LogService.logIt( "RefUtils.processExitCheck() BBB.3 NONFATAL RcRaterId in request does not match. Value in request rcRtrReq=" + rcRtrReq + ", rcRaterId()=" + rc.getRcRater().getRcRaterId() + ", rcCheckId=" + rc.getRcCheckId() );
                        refBean.clearBean();
                        CookieUtils.removeRcCheckCookie( getHttpServletResponse() );
                        return CorpUtils.getInstance().processCorpHome();
                    }

                    // if value is 0, they may have sent a get. Log it and use the RC if it's there.
                    // LogService.logIt( "RefUtils.processExitCheck() BBB.4 NONFATAL RcCheckId in request does not match. Value in request=" + rcChkReq + ", rcRaterId()=" + rc.getRcRater().getRcRaterId()+ ", rcCheckId=" + rc.getRcCheckId() );

                    // throw new Exception( "RcRaterId in request does not match. Value in request=" + rcRtrReq );
                }
            }

            refBean.clearBean();
            // LogService.logIt( "RefUtils.processExitCheck() AAA rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", returnUrl=" + returnUrl );

            CookieUtils.removeRcCheckCookie( getHttpServletResponse() );

            if( returnUrl!=null && !returnUrl.isBlank() )
            {
                // must be absolute.
                returnUrl=returnUrl.trim();
                if( !returnUrl.toLowerCase().startsWith("http") )
                    returnUrl = "http://" + returnUrl;

                FacesContext fc = FacesContext.getCurrentInstance();
                ((HttpServletResponse)  fc.getExternalContext().getResponse()).sendRedirect( returnUrl );
                return null;
            }

            if( o!=null && o.getDefaultCorpExitUrl()!=null && !o.getDefaultCorpExitUrl().isBlank() )
            {
                FacesContext fc = FacesContext.getCurrentInstance();
                // must be absolute.
                ((HttpServletResponse)  fc.getExternalContext().getResponse()).sendRedirect( o.getDefaultCorpExitUrlHttp() );
                return null;
            }

            return CorpUtils.getInstance().processCorpHome();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processExitCheck() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", returnUrl=" + returnUrl );
            setMessage( e );
            String nextView= systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );

            try
            {
                FacesContext fc = FacesContext.getCurrentInstance();

                if( returnUrl!=null && !returnUrl.isBlank() && fc!=null && fc.getExternalContext()!=null )
                {
                    // must be absolute.
                    ((HttpServletResponse)  fc.getExternalContext().getResponse()).sendRedirect( returnUrl );
                    return null;
                }
            }
            catch( Exception ee )
            {
                LogService.logIt( ee, "RefUtils.processExitCheck() Exception within the Exception. rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", returnUrl=" + returnUrl );
            }

            return nextView;
        }
    }

    public String processConfirmFail()
    {
        getCorpBean();
        getRefBean();

        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc==null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId();
                LogService.logIt( "RefUtils.processConfirmFail() " + msg );
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc.getRcRater(), true );
                // throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );
            }

            if( refBean.getRefUserType().getIsRater() )
            {
                long rcRtrReq = getRcRaterIdFmRequest();
                if( rcRtrReq!=rc.getRcRater().getRcRaterId() )
                    throw new Exception( "RcRaterId in request does not match. Value in request=" + rcRtrReq );
            }


            if( booleanParam1 )
            {
                RefPageType pt = getNextPageTypeForRefProcess();
                refBean.setRefPageType(pt);
                return getViewFromPageType( refBean.getRefPageType() );
            }

            // LogService.logIt( "RefUtils.processConfirmFail() Still a problem. rcCheckId=" + rc.getRcCheckId() + ", " + refBean.getRefUserType().getName() + ", " + refBean.getRefUser().getFullname() );
            refBean.setStrParam1( refBean.getRefUser().getFirstName());
            refBean.setStrParam2( refBean.getRefUser().getLastName());
            refBean.setStrParam3( refBean.getRefUser().getEmail());
            refBean.setStrParam4(null);

            return "/ref/confirm-failure-form.xhtml";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processConfirmFail() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public String processConfirmName()
    {
        getCorpBean();
        getRefBean();

        // LogService.logIt( "RefUtils.processConfirmName() AAA Start" );
        
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
            {
                LogService.logIt( "RefUtils.processConfirmName() rc=null after attempt to repair. Going to corp.home." );
                return CorpUtils.getInstance().processCorpHome();
            }

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId();
                LogService.logIt( "RefUtils.processConfirmName() " + msg );
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc.getRcRater(), true );
                // throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId() );
            }

            // Make sure we have the Candidate IP if needed.
            if( !refBean.getAdminOverride() && refBean.getRefUserType().getIsCandidate() && (rc.getIpAddress()==null || rc.getIpAddress().isBlank()) )
            {
                rc.setIpAddress( HttpReqUtils.getClientIpAddress(getHttpServletRequest()));
                if( rc.getIpAddress()!=null && !rc.getIpAddress().isBlank() )
                {
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcCheck(rc, true );
                }
            }

            // ensure that the rater ip address is present.
            if( !refBean.getAdminOverride() && refBean.getRefUserType().getIsRater() && (rc.getRcRater().getIpAddress()==null || rc.getRcRater().getIpAddress().isBlank()) )
            {
                rc.getRcRater().setIpAddress( HttpReqUtils.getClientIpAddress(getHttpServletRequest()));
                if( rc.getRcRater().getIpAddress()!=null && !rc.getRcRater().getIpAddress().isBlank() )
                {
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcRater(rc.getRcRater(), true );
                }
            }

            // LogService.logIt( "RefUtils.processConfirmName() BBB booleanParam1=" + booleanParam1 );
            // BooleanParam1 means confirmed.
            if( booleanParam1 )
            {
                // Now move to next page.



                // set to skip release if we already have it.
                //if( refBean.getRefUserType().getIsCandidate() && rc.getCandidateReleaseDate()!=null )
                //    refBean.setRefPageType( RefPageType.RELEASE );
                //else if( refBean.getRefUserType().getIsRater()&& rc.getRcRater().getReleaseDate()!=null )
                //    refBean.setRefPageType( RefPageType.RELEASE );

                // do not skip release.
                //else
                //    refBean.setRefPageType( RefPageType.CONFIRM );

                // Now move to next page.
                RefPageType rpt = getNextPageTypeForRefProcess();
                refBean.setRefPageType(rpt);

                // Now move to next page.
                //RefPageType rpt = getNextPageTypeForRefProcess();
                //refBean.setRefPageType(rpt);

                // At this point, the system is ready to go to the next page in the process.

                // HOWEVER, we may need to intercept the process to check for a camera.

                // set up proctoring if needed.

                if( !refBean.getAdminOverride() && refBean.getNeedsBrowserPrecheck() )
                {
                    // LogService.logIt( "RefUtils.processConfirmName() Going to Browser Precheck." );
                    return "/pp/browser-precheck.xhtml";
                }

                /*
                if( (refBean.getRefUserType().getIsCandidate() && rc.getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture()) ||
                    (refBean.getRefUserType().getIsRater() && rc.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture()))
                {
                    getProctorBean();
                    proctorBean.init( rc, refBean.getRefUserType() );

                    // go to browser precheck if needed.
                    if( refBean.getRefUserType().getIsCandidate() && rc.getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture() && refBean.getRecDevs()<0 )
                    {
                        LogService.logIt( "RefUtils.processConfirmName() Candidate. Going to Browser Precheck." );
                        return "/pp/browser-precheck.xhtml";
                    }

                    // go to browser precheck if needed.
                    if( refBean.getRefUserType().getIsRater() && rc.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture() && refBean.getRecDevs()<0 )
                    {
                        if( rc.getRcRaterPhotoCaptureType().getSameIpOnly() )
                        {
                            if( rc.getRcRater().getIpAddress()==null || rc.getRcRater().getIpAddress().isBlank() )
                            {
                                rc.getRcRater().setIpAddress( HttpReqUtils.getClientIpAddress(getHttpServletRequest()));
                                if( rc.getRcRater().getIpAddress()!=null && !rc.getRcRater().getIpAddress().isBlank() )
                                {
                                    if( rcFacade==null )
                                        rcFacade=RcFacade.getInstance();
                                    rcFacade.saveRcRater(rc.getRcRater(), true );
                                }
                            }

                            if( rc.getIpAddress()!=null && !rc.getIpAddress().isBlank() &&
                                rc.getRcRater().getIpAddress()!=null &&
                                rc.getRcRater().getIpAddress().equals( rc.getIpAddress() ) )
                            {
                                LogService.logIt( "RefUtils.processConfirmName() Rater Same IP Address as Candidate (" + rc.getIpAddress() + "). Going to Browser Precheck." );
                                return "/pp/browser-precheck.xhtml";
                            }
                        }

                        // Not Same IP but does capture photos. So check.
                        else
                        {
                            LogService.logIt( "RefUtils.processConfirmName() Rater All. Going to Browser Precheck." );
                            return "/pp/browser-precheck.xhtml";
                        }
                    }
                }
                */

                return getViewFromPageType( refBean.getRefPageType() );
            }

            LogService.logIt( "RefUtils.processConfirmName() Confirmation Denied. rcCheckId=" + rc.getRcCheckId() + ", " + refBean.getRefUserType().getName() + ", " + refBean.getRefUser().getFullname() );
            refBean.setRefPageType( RefPageType.CONFIRM );
            return "/ref/confirm-failure.xhtml";
        }

        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processConfirmName() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }




    public String processGoBackOneStep()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            RefPageType cpt = refBean.getRefPageType();
            RefPageType pt = getPreviousPageTypeForRefProcess();
            //LogService.logIt( "RefUtils.processGoBackOneStep() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", current page type=" + cpt.getName() + ", going to PageType=" + pt.getName() );
            refBean.setRefPageType(pt);
            return getViewFromPageType( refBean.getRefPageType() );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processGoBackOneStep() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public String processSubmitIntro()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId();
                LogService.logIt( "RefUtils.processSubmitIntro() " + msg );
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc.getRcRater(), true );
                // throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );
            }

            if( refBean.getRefUserType().getIsRater() )
            {
                long rcRtrReq = getRcRaterIdFmRequest();
                if( rcRtrReq!=rc.getRcRater().getRcRaterId() )
                    throw new Exception( "RcRaterId in request does not match. Value in request=" + rcRtrReq );
            }

            // LogService.logIt( "RefUtils.processSubmitIntro() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));

            if( refBean.getRefUserType().getIsRater() && rc.getRcRater()!=null && rc.getRcRater().getReleaseDate()!=null )
                refBean.setRefPageType( RefPageType.RELEASE );
            else if( refBean.getRefUserType().getIsCandidate()&& rc.getCandidateReleaseDate()!=null )
                refBean.setRefPageType( RefPageType.RELEASE );
            else
                refBean.setRefPageType( RefPageType.INTRO );
            RefPageType rpt = getNextPageTypeForRefProcess();
            refBean.setRefPageType(rpt);
            return getViewFromPageType( refBean.getRefPageType() );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processSubmitIntro() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public String processSubmitAvCommentsAllowed()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId();
                LogService.logIt( "RefUtils.processSubmitAvCommentsAllowed() " + msg );
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc.getRcRater(), true );
                // throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );
            }

            if( refBean.getRefUserType().getIsRater() )
            {
                long rcRtrReq = getRcRaterIdFmRequest();
                if( rcRtrReq!=rc.getRcRater().getRcRaterId() )
                    throw new Exception( "RcRaterId in request does not match. Value in request=" + rcRtrReq );
            }

            // LogService.logIt( "RefUtils.processSubmitAvCommentsAllowed() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            refBean.setRefPageType( RefPageType.AVCOMMENTS );
            RefPageType rpt = getNextPageTypeForRefProcess();
            refBean.setRefPageType(rpt);
            return getViewFromPageType( refBean.getRefPageType() );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processSubmitAvCommentsAllowed() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public String processSubmitSpecialInst()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId();
                LogService.logIt( "RefUtils.processSubmitSpecialInst() " + msg );
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc.getRcRater(), true );
            }

            if( refBean.getRefUserType().getIsRater() )
            {
                long rcRtrReq = getRcRaterIdFmRequest();
                if( rcRtrReq!=rc.getRcRater().getRcRaterId() )
                    throw new Exception( "RcRaterId in request does not match. Value in request=" + rcRtrReq );
            }


            // LogService.logIt( "RefUtils.processSubmitSpecialInst() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            refBean.setRefPageType( RefPageType.SPECIAL );
            RefPageType rpt = getNextPageTypeForRefProcess();
            refBean.setRefPageType(rpt);
            return getViewFromPageType( refBean.getRefPageType() );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processSubmitSpecialInst() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public String processRaterReject()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId();
                LogService.logIt( "RefUtils.processRaterReject() " + msg );
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc.getRcRater(), true );
            }

            if( refBean.getRefUserType().getIsRater() )
            {
                long rcRtrReq = getRcRaterIdFmRequest();
                if( rcRtrReq!=rc.getRcRater().getRcRaterId() )
                    throw new Exception( "RcRaterId in request does not match. Value in request=" + rcRtrReq );
            }


            // LogService.logIt( "RefUtils.processRaterReject() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));

            int rejectType = refBean.getIntParam1();

            RcRaterStatusType rst = RcRaterStatusType.REJECTED_CANDIDATE_NOT_KNOWN;
            if( rejectType==2 )
                rst = RcRaterStatusType.REJECTED_UNFAMILIAR_WITH_PERFORMANCE;
            else if( rejectType==2 )
                rst = RcRaterStatusType.REJECTED_REFUSED;

            RcRater rtr = rc.getRcRater();
            if( rtr==null )
                throw new Exception( "RcRater is null inside RcCheck." );

            if( !refBean.getAdminOverride() && !rtr.getRcRaterStatusType().getCompleteOrHigher() )
            {
                rtr.setRcRaterStatusTypeId( rst.getRcRaterStatusTypeId() );
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcFacade.saveRcRater(rtr, true);

                if( rcCheckUtils==null )
                    rcCheckUtils = new RcCheckUtils();
                rc.setPercentComplete( rcCheckUtils.computeRcCheckPercentComplete(rc) );
                rcCheckUtils.performRcCheckCompletionIfReady(rc, rtr.getInGracePeriod(), false );
                rcFacade.saveRcCheck(rc, false);
                rcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete(rc, rtr, false );
                // LogService.logIt( "RefUtils.processRaterReject() END raterStatusType=" + rtr.getRcRaterStatusTypeName() + ", rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            }

            String logMsg = "RcRaterId=" + rtr.getRcRaterId() + " rejected participation reason: " + rst.getName();
            RcCheckLogUtils.createRcCheckLogEntry( rtr.getRcCheckId(), rtr.getRcRaterId(), RcCheckLogLevelType.INFO.getRcCheckLogLevelTypeId(), logMsg, null, null );

            refBean.setStrParam1( MessageFactory.getStringMessage( getLocale(), rst.getKey() + ".reasontext", new String[]{rc.getUser().getFullname()} ));

            return "/ref/rater-reject-form.xhtml";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processRaterReject() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public String processRejectFormSubmit()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId();
                LogService.logIt( "RefUtils.processRejectFormSubmit() " + msg );
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc.getRcRater(), true );
            }

            // LogService.logIt( "RefUtils.processRejectFormSubmit() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));

            RcRater rtr = rc.getRcRater();
            if( rtr==null )
                throw new Exception( "RcRater is null inside RcCheck." );

            String reason = "Doesn't know subject.";
            if( refBean.getIntParam1()==2 )
                reason = "Not familiar enough with subject's performance.";
            if( refBean.getIntParam1()==3 )
                reason = "Rater refused to participate.";

            String note = refBean.getStrParam1();
            if( !refBean.getAdminOverride() && note!=null && !note.isBlank() )
            {
                rtr.setNote(note);
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcFacade.saveRcRater(rtr, true);

                reason += "\n\nNote:\n\n" + note;
            }

            // At this point, we should create a CS Case and punt.

            if( !refBean.getAdminOverride() )
                sendCsCaseEmailFromForm( "RefUtils.processRejectFormSubmit() RcRater rejected participation. reason=" + reason , true );

            setInfoMessage( "g.ErrCsCaseSent", null );

            if( !refBean.getAdminOverride() && (!rc.getRcCheckStatusType().getCompleteOrHigher() || rtr.getInGracePeriod()) )
            {
                if( rcCheckUtils==null )
                    rcCheckUtils=new RcCheckUtils();
                rcCheckUtils.performRcCheckCompletionIfReady(rc, rtr.getInGracePeriod(), false );

                if( rc.getRcCheckStatusType().getIsComplete() )
                    rcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete( rc, null, false);
            }

            refBean.clearBean();

            Org o = rc.getOrg();
            if( o==null )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                o=userFacade.getOrg( rc.getOrgId() );
            }
            CookieUtils.removeRcCheckCookie( getHttpServletResponse() );

            if( o.getDefaultCorpExitUrl()!=null && !o.getDefaultCorpExitUrl().isBlank() )
            {
                FacesContext fc = FacesContext.getCurrentInstance();
                ((HttpServletResponse)  fc.getExternalContext().getResponse()).sendRedirect( o.getDefaultCorpExitUrlHttp() );
                return null;
            }

            return "/ref/index.xhtml";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processRejectFormSubmit() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public boolean getHasCustomIntro()
    {
        getCorpBean();
        getRefBean();
        if( !corpBean.getHasCorp() )
            return false;
        Corp c = corpBean.getCorp();
        return c.getHasWelcomeMessage( refBean.getRefUserType() );
    }
    public String getCustomIntroXhtml()
    {
        getCorpBean();
        getRefBean();
        if( !corpBean.getHasCorp() )
            return null;
        Corp c = corpBean.getCorp();
        return performSubstitutions( c.getWelcomeMessageXhtml(refBean.getRefUserType() ) );
    }

    public boolean getHasCustomRelease()
    {
        getCorpBean();
        getRefBean();
        if( !corpBean.getHasCorp() )
            return false;
        Corp c = corpBean.getCorp();
        return c.getHasReleaseMessage( refBean.getRefUserType() );
    }
    public String getCustomReleaseXhtml()
    {
        getCorpBean();
        getRefBean();
        if( !corpBean.getHasCorp() )
            return null;
        Corp c = corpBean.getCorp();
        return performSubstitutions( c.getReleaseMessageXhtml(refBean.getRefUserType() ));
    }

    public String getRcCheckExpDateFormatted()
    {
        getRefBean();
        if( refBean.getRcCheck()==null || refBean.getRcCheck().getUser()==null )
            return "";
        return I18nUtils.getFormattedDateTime(getLocale(), refBean.getRcCheck().getExpireDate(), refBean.getRcCheck().getUser().getTimeZone() );
    }

    public String getSpecialInstructionsXhtml()
    {
        getCorpBean();
        getRefBean();

        String s = "";
        RcCheck rc = refBean.getRcCheck();
        if( rc!=null )
        {
            if( refBean.getRefUserType().getIsCandidate() && rc.getRcScript()!=null && rc.getRcScript().getSpecialInstructionsCandidate()!=null && !rc.getRcScript().getSpecialInstructionsCandidate().isBlank() )
                s = rc.getRcScript().getSpecialInstructionsCandidate();
            else if( refBean.getRefUserType().getIsRater()&& rc.getRcScript()!=null && rc.getRcScript().getSpecialInstructionsRaters()!=null && !rc.getRcScript().getSpecialInstructionsRaters().isBlank() )
                s = rc.getRcScript().getSpecialInstructionsRaters();

            // LogService.logIt( "RefUtils.getSpecialInstructionsXhtml() AAA s=" + s );

            if( s!=null && !s.isBlank() )
            {
                s = performSubstitutions( s );
                s = StringUtils.replaceStandardEntities(s) ;
            }
        }

        if( !corpBean.getHasCorp() )
            return s;
        Corp c = corpBean.getCorp();

        String cs = performSubstitutions( c.getSpecialInstructionsXhtml(refBean.getRefUserType() ));

        if( cs!=null && !cs.isBlank() )
            s = s + "<br />" + cs;

        return s;
    }

    String performSubstitutions( String inStr )
    {
        if( inStr==null )
            return inStr;

        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        if( rc==null )
            return inStr;
        String o = StringUtils.replaceStr(inStr, "[TYPE]", this.getRcCheckTypeName() );
        o = StringUtils.replaceStr(o, "[CANDIDATE]", rc.getUser().getFullname() );
        o = StringUtils.replaceStr(o, "[CANDIDATETYPE]", MessageFactory.getStringMessage(getLocale(), refBean.getRefUserType().getIsCandidate() ? "g.Candidate" : "g.Employee" ) );
        o = StringUtils.replaceStr(o, "[REFUSER]", refBean.getRefUser().getFullname() );
        o = StringUtils.replaceStr(o, "[COMPANY]", rc.getOrg().getName() );
        o = StringUtils.replaceStr(o, "[STARTURL]", refBean.getStartUrl() );
        o = StringUtils.replaceStr(o, "[ACCESSCODE]", refBean.getAccessCode());
        return o;

    }


    public String processSubmitRelease()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId();
                LogService.logIt( "RefUtils.processSubmitRelease() " + msg );
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc.getRcRater(), true );
            }

            if( refBean.getRefUserType().getIsRater() )
            {
                long rcRtrReq = getRcRaterIdFmRequest();
                if( rcRtrReq!=rc.getRcRater().getRcRaterId() )
                    throw new Exception( "RcRaterId in request does not match. Value in request=" + rcRtrReq );
            }


            // LogService.logIt( "RefUtils.processSubmitRelease() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            refBean.setRefPageType( RefPageType.RELEASE );

            if( booleanParam1 )
            {
                if( refBean.getRefUserType().getIsCandidate() && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
                {
                    if( rc.getCandidateReleaseDate()==null )
                    {
                        rc.setCandidateReleaseDate( new Date() );
                        rc.setCandidateLastUpdate( new Date() );
                    }
                    if( rc.getRcCandidateStatusType().getIsNotStarted() )
                        rc.setRcCandidateStatusTypeId( RcCandidateStatusType.STARTED.getRcCandidateStatusTypeId() );

                    if( !refBean.getAdminOverride() )
                    {
                        if( rcFacade==null )
                            rcFacade=RcFacade.getInstance();
                        rcFacade.saveRcCheck(rc, true);
                    }
                }

                else if( refBean.getRefUserType().getIsRater() && !rc.getRcRater().getRcRaterStatusType().getCompleteOrHigher() )
                {
                    if( rc.getRcRater().getReleaseDate()==null )
                        rc.getRcRater().setReleaseDate( new Date() );
                    if( !rc.getRcRater().getRcRaterStatusType().getStartedOrHigher() )
                        rc.getRcRater().setRcRaterStatusTypeId( RcRaterStatusType.STARTED.getRcRaterStatusTypeId() );

                    if( !refBean.getAdminOverride() )
                    {
                        if( rcFacade==null )
                            rcFacade=RcFacade.getInstance();
                        rcFacade.saveRcRater( rc.getRcRater(), true );
                        rcFacade.saveRcCheck(rc, false);
                    }
                }

                RefPageType rpt = getNextPageTypeForRefProcess();
                refBean.setRefPageType(rpt);
                return getViewFromPageType( refBean.getRefPageType() );
            }

            // REJECTION!!!!!!
            // Indicates that the release is not OK.
            else
            {
                if( !refBean.getAdminOverride() && refBean.getRefUserType().getIsCandidate() && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
                {
                    rc.setRcCandidateStatusTypeId( RcCandidateStatusType.REJECTED_RELEASE.getRcCandidateStatusTypeId() );
                    if( rcCheckUtils==null )
                        rcCheckUtils=new RcCheckUtils();
                    rc.setPercentComplete( rcCheckUtils.computeRcCheckPercentComplete( rc ) );
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcCheck(rc, true);
                    rcCheckUtils.performRcCheckCompletionIfReady(rc, false, false);
                    rcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete(rc, null, false );
                }

                else if( !refBean.getAdminOverride() && refBean.getRefUserType().getIsRater() && (!rc.getRcCheckStatusType().getCompleteOrHigher() || rc.getRcRater().getInGracePeriod()) && !rc.getRcRater().getRcRaterStatusType().getCompleteOrHigher() )
                {
                    rc.getRcRater().setRcRaterStatusTypeId( RcRaterStatusType.REJECTED_RELEASE.getRcRaterStatusTypeId() );
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcRater( rc.getRcRater(), true );
                    if( rcCheckUtils==null )
                        rcCheckUtils=new RcCheckUtils();
                    rc.setPercentComplete( rcCheckUtils.computeRcCheckPercentComplete( rc ) );
                    rcFacade.saveRcCheck(rc, false);
                    rcCheckUtils.performRcCheckCompletionIfReady(rc, rc.getRcRater().getInGracePeriod(), false );
                    rcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete(rc, rc.getRcRater(), false );
                }

                // LogService.logIt( "RefUtils.processSubmitRelease() RELEASE DENIED. rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                //setInfoMessage( "g.ReleaseDeniedMsg", new String[]{this.getRcCheckTypeName()} );
                //String startUrl = refBean.getStartUrl();
                //refBean.clearBean();
                // refBean.setStrParam1(null);
                refBean.setStrParam1( MessageFactory.getStringMessage( getLocale(), "rcrst.rejectedrelease.reasontext" ));
                //refBean.setStrParam5( startUrl );
                //refBean.setRcCheck(rc);
                return "/ref/release-denied.xhtml";
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processSubmitRelease() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public String processRetractRejection()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId();
                LogService.logIt( "RefUtils.processRetractRejection() " + msg );
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc.getRcRater(), true );
            }

            RefPageType refPageType = RefPageType.CONFIRM;

            if( rc.getRcCheckStatusType().getCompleteOrHigher() )
            {
                rc.setRcCheckStatusTypeId( RcCheckStatusType.STARTED.getRcCheckStatusTypeId() );
                rc.setLastProgressMsgDate( null );
            }

            if( refBean.getRefUserType().getIsCandidate()  )
            {
                RcCandidateStatusType rcst = rc.getRcCandidateStatusType();

                if( !rcst.getIsRejection() )
                    throw new Exception( "Cannot retract candidate rejection when there was no rejection." );

                if( rcst.equals( RcCandidateStatusType.REJECTED_RELEASE) && rc.getCandidateReleaseDate()==null )
                {
                    rc.setCandidateReleaseDate( null );
                    rc.setCandidateLastUpdate( null );
                    refPageType = RefPageType.INTRO;
                }
                rc.setRcCandidateStatusTypeId( RcCandidateStatusType.STARTED.getRcCandidateStatusTypeId() );
                rc.setLastCandidateProgressMsgDate(null);
                rc.setLastProgressMsgDate(null);

                if( !refBean.getAdminOverride() )
                {
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcCheck(rc, true);
                }
            }

            else if( refBean.getRefUserType().getIsRater()  )
            {
                long rcRtrReq = getRcRaterIdFmRequest();
                if( rcRtrReq!=rc.getRcRater().getRcRaterId() )
                    throw new Exception( "RcRaterId in request does not match. Value in request=" + rcRtrReq );

                RcRaterStatusType rcrst = rc.getRcRater().getRcRaterStatusType();

                if( !rcrst.getIsRejection() )
                    throw new Exception( "Cannot retract rater rejection when there was no rejection." );

                if( rcrst.equals( RcRaterStatusType.REJECTED_RELEASE) && rc.getRcRater().getReleaseDate()==null )
                {
                    rcrst = RcRaterStatusType.STARTED;
                    rc.getRcRater().setReleaseDate( null );
                    rc.getRcRater().setCompleteDate( null );
                    refPageType = RefPageType.INTRO;
                }

                else if( rcrst.getIsRejection()  )
                {
                    rcrst = RcRaterStatusType.STARTED;
                    refPageType = RefPageType.CONFIRM;
                    rc.getRcRater().setCompleteDate( null );
                }

                rc.getRcRater().setRcRaterStatusTypeId( rcrst.getRcRaterStatusTypeId() );
                rc.getRcRater().setLastProgressMsgDate(null);
                rc.setLastProgressMsgDate(null);
                if( !refBean.getAdminOverride() )
                {
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcRater( rc.getRcRater(), true );
                    rcFacade.saveRcCheck(rc, false);

                    String logMsg = "RcRaterId=" + rc.getRcRater().getRcRaterId() + " retracted their rejection.";
                    RcCheckLogUtils.createRcCheckLogEntry( rc.getRcCheckId(), rc.getRcRater().getRcRaterId(), RcCheckLogLevelType.INFO.getRcCheckLogLevelTypeId(), logMsg, null, null );
                }
            }

            refBean.setRefPageType( refPageType );
            RefPageType rpt = getNextPageTypeForRefProcess();
            refBean.setRefPageType(rpt);
            return getViewFromPageType( refBean.getRefPageType() );

        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processRetractRejection() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public String processSubmitReleaseDeniedComments()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + ", rc.getRcCheckId()=" + rc.getRcCheckId();
                LogService.logIt( "RefUtils.processSubmitReleaseDeniedComments() " + msg );
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc.getRcRater(), true );
            }

            if( refBean.getRefUserType().getIsRater() )
            {
                long rcRtrReq = getRcRaterIdFmRequest();
                if( rcRtrReq!=rc.getRcRater().getRcRaterId() )
                    throw new Exception( "RcRaterId in request does not match. Value in request=" + rcRtrReq );
            }


            String comments = refBean.getStrParam1();
            User refUser = refBean.getRefUser();

            // record comments if there are any.
            if( !refBean.getAdminOverride() && rc!=null && refUser!=null && comments!=null && !comments.isBlank() )
            {
                String ipAddress = HttpReqUtils.getClientIpAddress( getHttpServletRequest() );
                String userAgent = getHttpServletRequest()==null ? null : getHttpServletRequest().getHeader("User-Agent" );
                CsCaseUtils.createCsCaseAndEmailAdmin( rc, refUser, refUser.getFullname(), refUser.getEmail(), comments, "RefUtils.processSubmitReleaseDeniedComments()", true, ipAddress, userAgent );
            }

            // LogService.logIt( "RefUtils.processSubmitReleaseDeniedComments() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + "ref user=" + (refUser==null ? "null" : refUser.getFullname() + ", userId=" + refUser.getUserId()) + ", comments=" + comments );

            // Create CSCase with release comments.

            if( !booleanParam1 )
                return getViewFromPageType( getNextPageTypeForRefProcess() );

            // Indicates that the release is not OK.
            else
            {
                LogService.logIt( "RefUtils.processSubmitReleaseDeniedComments() RELEASE DENIED. rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                setInfoMessage( "g.ReleaseDeniedMsg", new String[]{this.getRcCheckTypeName()} );
                String startUrl = refBean.getStartUrl();
                refBean.clearBean();
                refBean.setStrParam1(null);
                refBean.setStrParam5( startUrl );
                return "/ref/index.xhtml";
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processSubmitReleaseDeniedComments() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public String processStartOver()
    {
        getRefBean();
        long rcCheckId = 0;
        long rcRaterId = 0;
        String accessCode = rcCheckId<=0 ? refBean.getAccessCode() : null;
        RcCheck rc = null;

        try
        {
            rc = refBean.getRcCheck();
            if( rc==null )
                rc = repairRefBeanForCurrentAction(refBean, true );


            if( refBean.getRcCheck()!=null )
            {
                long rcChkReq = this.getRcCheckIdFmRequest();
                if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
                {
                    String msg = "RcCheckId in request does not match. Value in request=" + rcChkReq + " but rcCheckId in RefBean=" + rc.getRcCheckId();
                    LogService.logIt( "RefUtils.processStartOver() " + msg );
                    return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc==null ? null : rc.getRcRater(), true );
                }

                rcCheckId = refBean.getRcCheck().getRcCheckId();
                if( refBean.getRcCheck().getRcRater()!=null )
                    rcRaterId = refBean.getRcCheck().getRcRater().getRcRaterId();
            }

            boolean adminOverride = refBean.getAdminOverride();

            refBean.clearBean();

            return performSimpleEntry(0, rcCheckId, rcRaterId, accessCode, adminOverride );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.processStartOver() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }






}
