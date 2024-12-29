/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.corp.CorpBean;
import com.tm2ref.corp.CorpUtils;
import com.tm2ref.entity.corp.Corp;
import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.Suborg;
import com.tm2ref.entity.user.User;
import com.tm2ref.event.EventFacade;
import com.tm2ref.event.TestKeyStatusType;
import com.tm2ref.faces.FacesUtils;
import com.tm2ref.faces.HttpReqUtils;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.STException;
import com.tm2ref.proctor.ProctorBean;
import com.tm2ref.purchase.ProductType;
import com.tm2ref.purchase.RefCreditUtils;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.CookieUtils;
import com.tm2ref.util.IpUtils;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.StringUtils;
import jakarta.faces.context.FacesContext;
import java.util.Date;
import java.util.List;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author miker_000
 */
public class BaseRefUtils  extends FacesUtils
{
    // private static int defaultOrgId = 0;
    protected boolean booleanParam1;
    protected boolean booleanParam2;

    UserFacade userFacade = null;

    RcFacade rcFacade = null;
    RcScriptFacade rcScriptFacade = null;

    RefBean refBean = null;

    CorpBean corpBean = null;

    ProctorBean proctorBean;

    CorpUtils corpUtils = null;
    RcCheckUtils rcCheckUtils = null;

    public String getRcCheckCandidateName()
    {
        getRefBean();

        if( refBean.getRcCheck()==null || refBean.getRcCheck().getRcCheckType().getIsPrehire() )
            return MessageFactory.getStringMessage( getLocale(), "g.Candidate" );

        return MessageFactory.getStringMessage( getLocale(), "g.Employee" );
    }

    public String getRcCheckTypeName()
    {
        getRefBean();

        if( refBean.getRcCheck()==null || refBean.getRcCheck().getRcCheckType().getIsPrehire() )
            return RcCheckType.PREHIRE.getName( getLocale() );

        return RcCheckType.EMPLOYEE_FBK.getName( getLocale() );
    }

    public String getRcCheckRaterNameLc()
    {
        return getRcCheckRaterName( true, false );
    }

    public String getRcCheckRaterName()
    {
        return getRcCheckRaterName( false, false );
    }
    public String getRcCheckRatersNameLc()
    {
        return getRcCheckRaterName( true, true );
    }

    public String getRcCheckRatersName()
    {
        return getRcCheckRaterName( false, true );
    }

    protected String getRcCheckRaterName( boolean lc, boolean plural )
    {
        getRefBean();

        if( refBean.getRcCheck()==null || refBean.getRcCheck().getRcCheckType().getIsPrehire() )
            return MessageFactory.getStringMessage( getLocale(), "g.Reference" + (plural ? "s" : "" ) + (lc ? "Lc" : "") );

        return MessageFactory.getStringMessage( getLocale(), "g.Reviewer" + (plural ? "s" : "" ) + (lc ? "Lc" : "") );
    }

    protected String getUserTextXhtml( String s )
    {
        getRefBean();

        if( s==null || s.isBlank() )
            return "";

        if( rcCheckUtils==null )
            rcCheckUtils=new RcCheckUtils();
        s = rcCheckUtils.performSubstitutions( s, refBean.getRcCheck(), refBean.getRcCheck().getRcRater(), getLocale() );

        if( s.contains("<") && s.contains( ">" ) )
            return s;
        
        // if( s.contains("<") && s.contains( ">" ) )
        //    return StringUtils.addLineBreaksXhtml( s );
        return StringUtils.replaceStandardEntities(s);
    }

    protected String getUserTextPlain( String s )
    {
        getRefBean();

        if( s==null || s.isBlank() )
            return "";

        if( rcCheckUtils==null )
            rcCheckUtils=new RcCheckUtils();
        s = rcCheckUtils.performSubstitutions( s, refBean.getRcCheck(), refBean.getRcCheck().getRcRater(), getLocale() );

        return StringUtils.convertHtml2PlainText(s, true);
    }
    
    

    public float getUnencryptedFloatFmRequest( String name )
    {
        String r = null;
        try
        {
            r = getHttpServletRequest().getParameter(name);
            if( r==null || r.isBlank() )
                return 0;
            Float f = Float.valueOf( r );
            if( f.isNaN() )
            {
                LogService.logIt( "BaseRefUtils.getUnencryptedFloatFmRequest() value is NaN, r=" + r + ", name=" + name );
                return 0;
            }
            return f;
        }
        catch( NumberFormatException e )
        {
            LogService.logIt( "BaseRefUtils.getUnencryptedFloatFmRequest() NumberFormatException " + e.toString() + ", r=" + r + ", name=" + name );
            return 0;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRefUtils.getUnencryptedFloatFmRequest() r=" + r + ", name=" + name);
            return 0;
        }
    }

    public int getUnencryptedIntFmRequest( String name )
    {
        String r = null;
        try
        {
            r = this.getHttpServletRequest().getParameter(name);
            if( r==null || r.isBlank() )
                return 0;

            return Integer.parseInt( r );
        }
        catch( NumberFormatException e )
        {
            LogService.logIt( "BaseRefUtils.getUnencryptedIntFmRequest() NumberFormatException " + e.toString() + ", r=" + r + ", name=" + name );
            return 0;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRefUtils.getUnencryptedIntFmRequest() r=" + r + ", name=" + name);
            return 0;
        }
    }


    public long getEncryptedIdFmRequest( String name )
    {
        String r = null;
        try
        {
            r = this.getHttpServletRequest().getParameter(name);
            if( r==null || r.isBlank() )
                return 0;

            return Long.parseLong( EncryptUtils.urlSafeDecrypt(r) );
        }
        catch( NumberFormatException e )
        {
            LogService.logIt( "BaseRefUtils.getEncryptedIdFmRequest() NumberFormatException " + e.toString() + ", r=" + r + ", name=" + name );
            return 0;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRefUtils.getEncryptedIdFmRequest() r=" + r + ", name=" + name);
            return 0;
        }
    }

    public String conditionUrlForSessionLossGet( String url, boolean includeRedirect)
    {
        if( refBean==null || refBean.getActiveAccessCodeX()==null || refBean.getActiveAccessCodeX().isBlank() || url==null || url.isBlank() )
            return url;
        
        if( includeRedirect && !url.contains("faces-redirect=") )
            url += (url.contains("?") ? "&" : "?") + "faces-redirect=true";
        
        if( !url.contains( "acidx=") )
            url += (url.contains("?") ? "&" : "?") + "acidx=" + refBean.getActiveAccessCodeX();
        
        if( !url.contains("refpagex=") && refBean.getRefPageType()!=null )
            url += (url.contains("?") ? "&" : "?") + "refpagex=" + refBean.getRefPageType().getRefPageTypeId();

        if( refBean.getRcCheck()!=null  && !url.contains("rcide=") )
            url += (url.contains("?") ? "&" : "?") + "rcide=" + refBean.getRcCheckIdEncrypted();            
        
        if( refBean.getRcRaterIdEncrypted()!=null && !refBean.getRcRaterIdEncrypted().isBlank() && !url.contains("rcride=") )
            url += (url.contains("?") ? "&" : "?") + "rcride=" + refBean.getRcRaterIdEncrypted();            
        
        return url;
    }
    
    
    public String getViewFromPageType( RefPageType refPageType ) throws Exception
    {
        getRefBean();


        RefUserType refUserType = refBean.getRefUserType();
        RcCheck rc = refBean.getRcCheck();

        if( refPageType.getIsAnyPhotoCapture() )
        {
            getProctorBean();
            if( !proctorBean.getCameraOptOut() && refBean.getRecDevs()<2 )
            {
                proctorBean.init( rc, refUserType );
                if( refUserType.getIsCandidate() && rc.getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture())
                {
                    if( rc.getRcCandidatePhotoCaptureType().getIsRequired() )
                        return "/pp/camera-required.xhtml";
                    else
                        return "/pp/camera-optional.xhtml";
                }
                else if( refUserType.getIsRater()&& rc.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture())
                {
                    if( rc.getRcRaterPhotoCaptureType().getIsRequired() )
                        return "/pp/camera-required.xhtml";
                    else
                        return "/pp/camera-optional.xhtml";
                }
            }
        }

        if( refPageType.getIsCore() && refUserType.getIsRater() )
        {
            return RaterRefUtils.getInstance().getNextViewFromRatings();
        }

        if( refPageType.getIsCore2() && refUserType.getIsRater() )
        {
            //return RaterRefUtils.getInstance().getNextView();
            RaterRefUtils rru = RaterRefUtils.getInstance();
            rru.doEnterCore2();

            return refPageType.getPageFull(refUserType);
            // return rru.getNextView();
        }
        
        
        if( refPageType.getIsCore2() && refUserType.getIsCandidate() )
        {
            RaterRefBean rrb = RaterRefBean.getInstance();
            RaterRefUtils rru = RaterRefUtils.getInstance();
            if( rrb.getRcItemWrapper()==null )
                rru.doEnterCore(true);

            return rru.getNextViewFromRatings();
        }

        return refPageType.getPageFull(refUserType);
    }


    public String getNextViewForCorp()
    {
        getCorpBean();
        getRefBean();

        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                if( corpUtils==null )
                    corpUtils = CorpUtils.getInstance();
                return corpUtils.processCorpHome();
            }

            if( !getNewRefStartsOk() && corpBean.getHasCorp() )
                return corpBean.getCorp().getOfflinePage();

            return getViewFromPageType( getNextPageTypeForRefProcess() );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.getNextViewForCorp() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public RefPageType getNextPageTypeForRefProcess() throws Exception
    {
        getRefBean();
        getCorpBean();
        RcCheck rc = refBean.getRcCheck();
        
        try
        {
            if( rc==null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true, 207);                
                if( refBean.getRefPageType()!=null )
                    return refBean.getRefPageType();
            }

            RefPageType currentRefPageType = refBean.getRefPageType();
            RefUserType refUserType = refBean.getRefUserType();

            RefPageType nextRefPageType = currentRefPageType.getNextPageTypeNoNull(refUserType);

            // LogService.logIt( "BaseRefUtils.getNextPageTypeForRefProcess() currentRefPageType=" + currentRefPageType.getName() + ", nextPageType=" + nextRefPageType.getName() +", refUserType=" + refUserType.getName() );

            if( nextRefPageType.equals( RefPageType.RELEASE) && corpBean.getCorp().getReleaseRqd()<=0 )
                nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);

            else if( nextRefPageType.equals( RefPageType.RELEASE)  )
            {
                if( refUserType.getIsCandidate() && rc.getCandidateReleaseDate()!=null )
                    nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
                if( refUserType.getIsRater() && rc.getRcRater().getReleaseDate()!=null )
                    nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
            }

            if( nextRefPageType.equals( RefPageType.SPECIAL) && !getHasSpecialInstructions() )
                nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);

            if( nextRefPageType.equals( RefPageType.AVCOMMENTS) && !getHasAvComments() )
                nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);

            if( nextRefPageType.equals( RefPageType.PHOTO) && !getNeedsPhoto() )
                nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);

            if( nextRefPageType.equals( RefPageType.ID_PHOTO) && !getNeedsIdPhoto() )
                nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);

            // Prequestions are only for candidtes that have questions or ratings
            if( nextRefPageType.equals( RefPageType.PRE_QUESTIONS) && (refUserType.getIsRater() || !rc.getRequiresAnyCandidateInputOrSelfRating() ) )
                nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
            
            // skip pre-questions if they are all answered.
            if( nextRefPageType.equals( RefPageType.PRE_QUESTIONS) && refUserType.getIsCandidate() )
            {
                CandidateRefUtils cru = CandidateRefUtils.getInstance();
                cru.doEnterCore();
                if( !cru.getNeedsCore() )
                    nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
            }
            
            // LogService.logIt( "BaseRefUtils.getNextPageTypeForRefProcess() NNN.1 nextRefPageType=" + nextRefPageType.getName() +", refUserType=" + refUserType.getName() );
            
            if( nextRefPageType.getIsCore() )
            {
                if( refUserType.getIsCandidate() )
                {
                    CandidateRefUtils cru = CandidateRefUtils.getInstance();
                    cru.doEnterCore();
                    if( !cru.getNeedsCore() )
                    {
                        refBean.setRefPageType(nextRefPageType);
                        return getNextPageTypeForRefProcess();
                        
                        /*
                        nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);

                        LogService.logIt( "BaseRefUtils.getNextPageTypeForRefProcess() NNN.2 nextRefPageType=" + nextRefPageType.getName() +", refUserType=" + refUserType.getName() + ", needsCore2=" + cru.getNeedsCore2() + ", needsCore3=" + cru.getNeedsCore3() );

                        if( nextRefPageType.getIsCore2() )
                        {
                            if( !cru.getNeedsCore2() )
                            {
                                LogService.logIt( "BaseRefUtils.getNextPageTypeForRefProcess() NNN.3 nextRefPageType=" + nextRefPageType.getName() +", refUserType=" + refUserType.getName() );
                                nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
                            }
                            
                            else
                            {
                                RaterRefUtils rru = RaterRefUtils.getInstance();
                                rru.doEnterCore(true);
                                //if( !rru.getNeedsCore() )
                                //{
                                //    LogService.logIt( "BaseRefUtils.getNextPageTypeForRefProcess() NNN.4 nextRefPageType=" + nextRefPageType.getName() +", refUserType=" + refUserType.getName() );
                                    //nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
                                //}
                            }
                        }

                        if( nextRefPageType.getIsCore3() && !cru.getNeedsCore3() )
                            nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
                        */
                    }

                    // Does need Core1
                    else
                    {
                        // LogService.logIt( "BaseRefUtils.getNextPageTypeForRefProcess() CCC needsCore2=" + cru.getNeedsCore2() + ", needsCore3=" + cru.getNeedsCore3() );
                        if( cru.getAllCandidateQuestionsAnswered() )
                        {
                            // Needs to do ratings (CORE2).
                            if( cru.getNeedsCore2() )
                            {
                                nextRefPageType = RefPageType.CORE2;
                                RaterRefUtils rru = RaterRefUtils.getInstance();
                                rru.doEnterCore(true);
                            }

                            // Needs to enter references (CORE3)
                            else if( cru.getNeedsCore3() )
                            {
                                // refBean.setRefPageType( );
                                nextRefPageType = RefPageType.CORE3;
                                if( !rc.getRcRaterListCandidate().isEmpty() && rc.getNeedsSupervisors() )
                                    setInfoMessage( "g.XCAddReferences.belowminsups", new String[]{ getRcCheckRaterNameLc(), getRcCheckRatersNameLc(),Integer.toString(rc.getRcRaterListCandidate().size()),null,null,null,null,Integer.toString(rc.getRcRaterListCandidateSupers().size()), Integer.toString(rc.getMinSupervisors())} );
                            }
                        }

                        // If we will be asking for raters later (minRaters>=0) ...
                        else if(  cru.getNeedsCore2() || cru.getNeedsCore3() )
                            setInfoMessage( "g.XCStartWithQuestions", null );
                    }
                }

                // rater not candidate
                else
                {
                    RaterRefUtils rru = RaterRefUtils.getInstance();
                    rru.doEnterCore(true);
                    if( !rru.getNeedsCore() )
                    {
                        // this will advance until a non-null page is found.
                        nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
                        
                        // core 2
                        if( nextRefPageType.getIsCore2() )
                        {
                            // doens't need core2, continue.
                            if( !rru.getNeedsCore2() )
                            {
                                refBean.setRefPageType(nextRefPageType);
                                // recurse
                                nextRefPageType = getNextPageTypeForRefProcess();
                                // nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
                            }
                            
                            else
                            {
                                rru.doEnterCore2();
                            }
                        }
                    }

                }
            }
            
            else if( nextRefPageType.getIsCore2() )
            {
                RaterRefUtils rru = RaterRefUtils.getInstance();
                // doens't need core2, continue.
                
                // LogService.logIt( "RefUtils.getNextPageForRefProcess() PPP.1 rru.getNeedsCore2()=" + rru.getNeedsCore2() + ", rcCheck: " + rc.toStringShort() );
                
                if( refUserType.getIsCandidate() )
                {
                    CandidateRefUtils cru = CandidateRefUtils.getInstance();

                    if( !cru.getNeedsCore2() )
                    {
                        // LogService.logIt( "BaseRefUtils.getNextPageTypeForRefProcess() NNN.3 nextRefPageType=" + nextRefPageType.getName() +", refUserType=" + refUserType.getName() );

                        // recurse
                        refBean.setRefPageType(nextRefPageType);
                        return getNextPageTypeForRefProcess();
                    }

                    else
                    {
                        rru.doEnterCore(true);
                        //if( !rru.getNeedsCore() )
                        //{
                        //    LogService.logIt( "BaseRefUtils.getNextPageTypeForRefProcess() NNN.4 nextRefPageType=" + nextRefPageType.getName() +", refUserType=" + refUserType.getName() );
                            //nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
                        //}
                    }
                    
                }
                
                else
                {
                    if( !rru.getNeedsCore2() )
                    {
                        refBean.setRefPageType(nextRefPageType);
                        // recurse
                        nextRefPageType = getNextPageTypeForRefProcess();

                        // nextRefPageType = nextRefPageType.getNextPageTypeNoNull(refUserType);
                    }

                    else
                        rru.doEnterCore2();                
                }                
                
            }
            
            else if( nextRefPageType.getIsCore3() )
            {
                if( refUserType.getIsRater() )
                    throw new Exception( "nextRefPageType is Core 3 but refUserType is Rater. " );
                
                CandidateRefUtils cru = CandidateRefUtils.getInstance();
                if( !cru.getNeedsCore3() )
                {
                    LogService.logIt( "RefUtils.getNextPageForRefProcess() SS.1 nextRefPageType is CORE3 but Candidate/Employee user does not add raters. " + rc.toStringShort() );
                    //performRcCheckStart(refBean.getRcCheck(), refBean.getRefUserType(), true, refBean.getAdminOverride() );
                    // recurse
                    refBean.setRefPageType(nextRefPageType);
                    nextRefPageType = this.getNextPageTypeForRefProcess(); refBean.getRefPageType();
                }
            }

            if( getSessionListener() != null )
                getSessionListener().updateStatus( getHttpSession().getId(), nextRefPageType.getName(),null, null, null, null, null);

            return nextRefPageType;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.getNextPageForRefProcess() " + rc.toStringShort() );
            throw e;
            // return systemError( rc==null ? null : rc.getOrg() , CorpBean.getInstance().getCorp(), e.toString(), null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public boolean getNeedsPhoto()
    {
        getRefBean();
        if( refBean.getAdminOverride() )
            return false;

        getProctorBean();

        if( proctorBean.getSessionPhotoComplete() || proctorBean.getCameraOptOut() )
            return false;

        // For candidates. If no photo and required.
        if( refBean.getRefUserType().getIsCandidate() )
        {
            return refBean.getRcCheck().getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture();
        }

        if( refBean.getRefUserType().getIsRater() )
        {
            if( !refBean.getRcCheck().getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture())
                return false;

            if( !refBean.getRcCheck().getRcRaterPhotoCaptureType().getSameIpOnly() )
                return true;

            RcCheck rc = refBean.getRcCheck();
            if( rc.getIpAddress()==null || rc.getIpAddress().isBlank() )
                return false;
            if( rc.getRcRater().getIpAddress()==null || rc.getRcRater().getIpAddress().isBlank() )
                rc.getRcRater().setIpAddress( HttpReqUtils.getClientIpAddress( getHttpServletRequest() ));

            if( rc.getRcRater().getIpAddress()==null || rc.getRcRater().getIpAddress().isBlank() || !rc.getRcRater().getIpAddress().equals( rc.getIpAddress() ) )
                return false;

            return true;
        }

        return false;
    }

    public boolean getNeedsIdPhoto()
    {
        getRefBean();
        if( refBean.getAdminOverride() )
            return false;

        getProctorBean();

        if( proctorBean.getSessionIdPhotoComplete() || proctorBean.getCameraOptOut() )
            return false;

        if( refBean.getRefUserType().getIsCandidate() )
        {
            return refBean.getRcCheck().getRcCandidatePhotoCaptureType().getRequiresAnyIdCapture();
        }

        if( refBean.getRefUserType().getIsRater() )
        {
            // no id capture set.
            if( !refBean.getRcCheck().getRcRaterPhotoCaptureType().getCapturesAnyId() )
                return false;

            // if not same ID, get it.
            if( !refBean.getRcCheck().getRcRaterPhotoCaptureType().getSameIpOnly() )
                return true;

            // at this point, it's just same ip.
            RcCheck rc = refBean.getRcCheck();
            if( rc.getIpAddress()==null || rc.getIpAddress().isBlank() )
                return false;
            if( rc.getRcRater().getIpAddress()==null || rc.getRcRater().getIpAddress().isBlank() )
                rc.getRcRater().setIpAddress( HttpReqUtils.getClientIpAddress( getHttpServletRequest() ));

            // not same ip address.
            if( rc.getRcRater().getIpAddress()==null || rc.getRcRater().getIpAddress().isBlank() || !rc.getRcRater().getIpAddress().equals( rc.getIpAddress() ) )
                return false;

            return true;
        }

        return false;
    }

    public RefPageType getPreviousPageTypeForRefProcess() throws Exception
    {
        getRefBean();
        getCorpBean();
        RcCheck rc = refBean.getRcCheck();
        try
        {
            if( rc==null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true, 208);                
                if( refBean.getRefPageType()!=null )
                    return refBean.getRefPageType();
            }
            
            RefPageType currentRefPageType = refBean.getRefPageType();
            RefUserType refUserType = refBean.getRefUserType();
            RefPageType pt = currentRefPageType.getPreviousPageTypeNoNull(refUserType, rc);

            if( pt.getIsCore2() && refUserType.getIsCandidate() && !rc.getCollectRatingsFmCandidate() )
                pt = RefPageType.CORE;

            else if( pt.getIsCore2() && refUserType.getIsCandidate() )
            {
                if( rc.getCollectRatingsFmCandidate() && rc.getRcScript().getHasAnyCandidateRatings() )
                {
                    RaterRefUtils rru = RaterRefUtils.getInstance();
                    rru.processGoBackToLastItem();
                    pt = refBean.getRefPageType();
                    //rru.doEnterCore(true);
                    CandidateRefUtils cru = CandidateRefUtils.getInstance();
                    if( !cru.getNeedsCore2() )
                        pt = pt.getPreviousPageTypeNoNull(refUserType, rc);
                }
                else
                    pt = RefPageType.CORE;
            }

            if( pt.getIsCore() )
            {
                if( refUserType.getIsCandidate() )
                {
                    CandidateRefUtils cru = CandidateRefUtils.getInstance();

                    cru.setLastCandidateInputNumberAndValue();
                    //cru.doEnterCore();
                    if( !cru.getNeedsCore() )
                    {
                        pt = pt.getPreviousPageTypeNoNull(refUserType, rc);

                        //if( pt.equals( RefPageType.SPECIAL) && !getHasSpecialInstructions() )
                        //    pt = pt.getPreviousPageTypeNoNull(refUserType, rc);

                        //if( pt.equals( RefPageType.RELEASE) && corpBean.getCorp().getReleaseRqd()<=0 )
                        //    pt = pt.getPreviousPageTypeNoNull(refUserType, rc);

                        //else if( pt.equals( RefPageType.RELEASE)  )
                        //{
                        //    if( refUserType.getIsCandidate() && rc.getCandidateReleaseDate()!=null )
                        //        pt = pt.getPreviousPageTypeNoNull(refUserType, rc);
                       //     if( refUserType.getIsRater() && rc.getRcRater().getReleaseDate()!=null )
                        //        pt = pt.getPreviousPageTypeNoNull(refUserType, rc);
                        //}

                    }
                }
                
                // Not candidate
                else
                {
                    RaterRefUtils rru = RaterRefUtils.getInstance();
                    rru.doEnterCore(true);
                    if( !rru.getNeedsCore() )
                        pt = pt.getPreviousPageTypeNoNull(refUserType, rc);
                }
            }

            // Never go Back to photos
            if( pt.equals( RefPageType.ID_PHOTO)  )
                pt = pt.getPreviousPageTypeNoNull(refUserType, rc);

            // Never go Back to photos
            if( pt.equals( RefPageType.PHOTO)  )
                pt = pt.getPreviousPageTypeNoNull(refUserType, rc);

            // Prequestions are only for candidtes that have questions or ratings
            if( pt.equals( RefPageType.PRE_QUESTIONS) && (refUserType.getIsRater() || !rc.getRequiresAnyCandidateInputOrSelfRating() ) )
                pt = pt.getPreviousPageTypeNoNull(refUserType, rc);
                        
            if( pt.equals( RefPageType.AVCOMMENTS) && !getHasAvComments())
                pt = pt.getPreviousPageTypeNoNull(refUserType, rc);

            if( pt.equals( RefPageType.SPECIAL) && !getHasSpecialInstructions() )
                pt = pt.getPreviousPageTypeNoNull(refUserType, rc);

            if( pt.equals( RefPageType.RELEASE) && corpBean.getCorp().getReleaseRqd()<=0 )
                pt = pt.getPreviousPageTypeNoNull(refUserType, rc);

            else if( pt.equals( RefPageType.RELEASE)  )
            {
                if( refUserType.getIsCandidate() && rc.getCandidateReleaseDate()!=null )
                    pt = pt.getPreviousPageTypeNoNull(refUserType, rc);
                if( refUserType.getIsRater() && rc.getRcRater().getReleaseDate()!=null )
                    pt = pt.getPreviousPageTypeNoNull(refUserType, rc);
            }

            if( getSessionListener() != null )
                getSessionListener().updateStatus(getHttpSession().getId(), pt.getName(),null, null, null, null, null);

            return pt;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefUtils.getPreviousPageTypeForRefProcess() " + rc.toStringShort() );
            throw e;
        }
    }


    public boolean getHasSpecialInstructions()
    {
        getCorpBean();
        getRefBean();


        RcCheck rc = refBean.getRcCheck();
        // LogService.logIt( "BaseRefUtis.getHasSpecialIns() rc.getRcScript().getSpecialInstructionsCandidate()=" + rc.getRcScript().getSpecialInstructionsCandidate() + ", rc.getRcScript().getSpecialInstructionsRaters()=" + rc.getRcScript().getSpecialInstructionsRaters() );
        if( rc!=null )
        {
            if( refBean.getRefUserType().getIsCandidate() && rc.getRcScript()!=null && rc.getRcScript().getSpecialInstructionsCandidate()!=null && !rc.getRcScript().getSpecialInstructionsCandidate().isBlank() )
                return true;

            else if( refBean.getRefUserType().getIsRater() && rc.getRcScript()!=null && rc.getRcScript().getSpecialInstructionsRaters()!=null && !rc.getRcScript().getSpecialInstructionsRaters().isBlank() )
                return true;
        }

        if( !corpBean.getHasCorp() )
            return false;
        Corp c = corpBean.getCorp();
        return c.getHasSpecialInstructions(refBean.getRefUserType() );
    }

    public boolean getHasAvComments()
    {
        getCorpBean();
        getRefBean();

        if( refBean.getAdminOverride() )
            return false;

        if( !refBean.isMedRecApi() )
            return false;

        RcCheck rc = refBean.getRcCheck();
        // LogService.logIt( "BaseRefUtis.getHasSpecialIns() rc.getRcScript().getSpecialInstructionsCandidate()=" + rc.getRcScript().getSpecialInstructionsCandidate() + ", rc.getRcScript().getSpecialInstructionsRaters()=" + rc.getRcScript().getSpecialInstructionsRaters() );
        if( rc!=null )
        {
            if( refBean.getRefUserType().getIsCandidate() && rc.getCollectRatingsFmCandidate() && rc.getRcAvType().getAnyMedia() )
                return true;

            if( refBean.getRefUserType().getIsRater() && rc.getRcAvType().getAnyMedia() )
                return true;
        }

        return false;
    }


    
    

    public RcCheck repairRefBeanForCurrentAction( RefBean rb, boolean useCookie, int sourceCode) throws Exception
    {
        try
        {
            if( rb == null )
                rb = getRefBean();

            refBean = rb;

            if( rb.getAdminOverride() )
                throw new Exception( "Cannot repairRefBean is in Admin Override Mode." );

            HttpServletRequest req = getHttpServletRequest();

            if( req == null )
                return null;

            LogService.logIt("RefUtils.repairRefBeanForCurrentAction() START useCookie=" + useCookie + ", sourceCode=" + sourceCode );

            // RefPageType refPageType = null;
            
            if( rb.getRcCheck()==null ) // || tb.getTestEvent() == null )
            {
                // In case somehow the session is not null
                String acidx = refBean.getActiveAccessCodeX();
                String refpagex = refBean.getActiveRefPageTypeIdX();
                int refPageTypeX;

                //if( tkid> 0 )
                LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() AAA.1 Recovered acidx from RefBean.activeAccessCodeX=" + acidx + ", sourceCode=" + sourceCode);

                // at this point, the session must be null so look for the access code.
                // look at the request.
                if( acidx==null || acidx.isBlank()  )
                {
                    acidx = req.getParameter( "acidx" );
                    if( acidx == null || acidx.isBlank() )
                        LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() AAA.2 No acidx found in request parameter.acidx. sourceCode=" + sourceCode);
                    else
                        LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() AAA.3 acidx Found in request: " + acidx );
                }

                if( refpagex==null || refpagex.isBlank()  )
                {
                    refpagex = req.getParameter( "refpagex" );
                    if( refpagex==null || refpagex.isBlank() )
                        LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() CCC.1 No refpagex found in request parameter.refpagex sourceCode=" + sourceCode);
                    else
                    {
                        LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() CCC.2 refpagex Found in request: " + refpagex + ", sourceCode=" + sourceCode );
                        try
                        {
                            refPageTypeX = Integer.parseInt(refpagex);
                            if( rb.getRefPageType()==null )
                                rb.setRefPageType( RefPageType.getValue(refPageTypeX) );
                            rb.setActiveRefPageTypeIdX( Integer.toString( refPageTypeX ) );
                            // refPageType = RefPageType.getValue(refPageTypeX);
                        }
                        catch( NumberFormatException e )
                        {
                            LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() CCC.3 Error parsing refpagex=" + refpagex + ", sourceCode=" + sourceCode );
                        }
                    }
                }

                // nothing in request, look for cookie.
                if( (acidx==null || acidx.isBlank()) && useCookie )
                {
                    acidx = getAccessCodeFmCookie();
                    LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() DDD.1 Recovered TestKeyId from cookie=" + acidx + ", sourceCode=" + sourceCode );
                }


                // found something
                if( acidx!=null && !acidx.isBlank()  )
                {
                    acidx = RcCheckUtils.conditionAccessCode(acidx);

                    LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() EEE.1 Found an acidx=" + acidx + ", sourceCode=" + sourceCode );
                    
                    // includes clone
                    RcCheck rc = lookupAccessCode(acidx);

                    rb.setRcCheck(rc);

                    if( rb.getRcCheck() == null )
                        LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() EEE.2 AccessCode=" + acidx + " but RcBean.RcCheck is NULL after attempt to load from DBMS. sourceCode=" + sourceCode );
                }

                if( rb.getRcCheck()==null )
                {
                    LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() EEE.3 AccessCode=" + (acidx==null ? "null" : acidx)+ " but RcBean.RcCheck is NULL. sourceCode=" + sourceCode );
                    return null;
                }
            }

            RcCheck rc = rb.getRcCheck();

            RefUserType refUserType = rc.getRcRater()==null ? RefUserType.CANDIDATE : RefUserType.RATER;
            refBean.setRefUserType(refUserType);

            if( refBean.getRecDevs()<0 )
            {
                refBean.setRecDevs(2);
                refBean.setMedRecApi(true);
                refBean.setHasGetUserMedia(1);
            }
                        
            // at this point we have a valid RcCheck / RcRater
            performRcCheckStart(rc, refUserType, true, false );
            
            RcCheckLogUtils.createRcCheckLogEntry( rc.getRcCheckId(), refUserType.getIsCandidate() ? 0 : rc.getRcRater().getRcRaterId(), 2, "RefUtils.repairRefBeanForCurrentAction() Completing process. NEW refPageType=" + (refBean.getRefPageType()==null ? "null" : refBean.getRefPageType().getPageFull(refUserType)), HttpReqUtils.getClientIpAddress(req), req.getHeader("User-Agent"));
            
            return rc;
        }
        catch( STException e )
        {
            LogService.logIt( "RefUtils.repairRefBeanForCurrentAction() " + e.toString() + ", RefBean.rc=" + (refBean!=null && refBean.getRcCheck()==null ? "null" : refBean.getRcCheck().toStringShort()) + ", sourceCode=" + sourceCode );
            throw e;
        }
    }
    
    
    public RcCheck lookupByEmail(String email, boolean completeOk ) throws Exception
    {
        RcCheck rc = null;
        if( email==null || email.isBlank() || !EmailUtils.validateEmailNoErrors(email) )
            return null;

        int type = -1;

        // data[0] = orgId  (Integer)
        // data[1] = AccessCode (String)
        // data[2] = Type (Integer) 0=candidate, 1=rater
        try
        {
            if( rcFacade==null )
                rcFacade = RcFacade.getInstance();

            // first check for an active rater.
            RcRater rcRater = rcFacade.findActiveRcRaterByRaterEmail(email, completeOk);            
            if( rcRater!=null )
            {
                rc = rcFacade.getRcCheck(rcRater.getRcCheckId(), true );
                rc.setRcRater(rcRater);
                return rc;
            }
            
            // no rater? check for an active candidate
            return rcFacade.findActiveRcCheckForCandidateEmail(email, completeOk);
        }
        catch( STException ee )
        {
            throw ee;
            // throw new STException( "g.AccessCodeEnteredInvalid", ee.getParams() );
        }
        catch( Exception ee )
        {
            LogService.logIt( ee, "RefUtils.lookupByEmail() AAA.3 Parse AccessCode." );
            return null;
        }
    }

    
    public RcCheck lookupAccessCode( String accessCode ) throws Exception
    {
        RcCheck rc = null;
        if( accessCode==null || accessCode.isBlank() )
            throw new STException( "g.AccessCodeMissing" );

        int type = -1;

        // data[0] = orgId  (Integer)
        // data[1] = AccessCode (String)
        // data[2] = Type (Integer) 0=candidate, 1=rater
        try
        {
            Object[] oa = RcCheckUtils.parseAccessCode( accessCode );

            if( oa[0]==null || ((Integer) oa[0])<=0 )
            {
                LogService.logIt( "RefUtils.processEnterAccessCode()  OrgId invalid: " + ((Integer) oa[0]) + " accessCode=" + accessCode );
                throw new STException( "g.AccessCodeEnteredInvalid", accessCode );
            }

            if( oa[1]==null || ((String)oa[1]).isBlank() )
            {
                // throw new Exception( "code invalid: " + ((Integer) oa[0]) );
                LogService.logIt( "RefUtils.processEnterAccessCode()  code invalid: " + ((Integer) oa[0]) + " accessCode=" + accessCode );
                throw new STException( "g.AccessCodeEnteredInvalid", accessCode );
            }

            int orgId = ((Integer) oa[0]);

            type = ((Integer) oa[2]);

            if( rcFacade==null )
                rcFacade = RcFacade.getInstance();

            // rater type
            if( type==1 )
            {
                RcRater rcRater = rcFacade.getRcRaterByRaterAccessCode(accessCode);
                if( rcRater==null )
                {
                    LogService.logIt( "RefUtils.processEnterAccessCode() RcRater=null. Rater access code invalid: " + ((Integer) oa[0]) + " accessCode=" + accessCode );
                    throw new STException( "g.AccessCodeEnteredInvalid", accessCode );
                    // throw new Exception( "RcRater record not found for rater access code." );
                }
                rc = rcFacade.getRcCheck( rcRater.getRcCheckId(), true );
                if( rc==null )
                    throw new Exception( "RcCheck referenced by RcRater not found. rcRaterId=" + rcRater.getRcRaterId() + ", rcCheckId=" + rcRater.getRcCheckId() + ", accessCode=" + accessCode );
                rc = (RcCheck) rc.clone();
                rc.setRcRater(null);
                rc.setRcRater(rcRater);
                //rc.setRefUserType( RefUserType.RATER );
            }

            // candidate type
            else
            {
                rc = rcFacade.getRcCheckByCandidateAccessCode(accessCode);
                if( rc==null )
                {
                    LogService.logIt( "RefUtils.processEnterAccessCode() RcCheck=null. Canddiate access code invalid: " + ((Integer) oa[0]) + " accessCode=" + accessCode );
                    throw new STException( "g.AccessCodeEnteredInvalid", accessCode );
                    // throw new Exception( "RcCheck not found for candidate access code." );
                }
                rc = (RcCheck) rc.clone();
                rc.setRcRater(null);
                //rc.setRefUserType( RefUserType.CANDIDATE );
            }

            if( rc.getOrgId()!=orgId )
            {
                // The rc may have been moved, so this could be ok. We will decide it's OK if the originating user is in teh same org as the rc and the candidate or rater is also in the same org
                //
                boolean ok = false;

                if( userFacade==null )
                    userFacade=UserFacade.getInstance();

                if( rc!=null && rc.getRcRater()!=null )
                {
                    User rUser = userFacade.getUser( rc.getRcRater().getUserId() );
                    if( rUser!=null && rUser.getOrgId()==rc.getOrgId() )
                        ok=true;
                }

                else if( rc!=null )
                {
                    User srcUser = userFacade.getUser( rc.getAdminUserId() );
                    if( srcUser!=null && srcUser.getOrgId()==rc.getOrgId() )
                        ok=true;
                }

                if( !ok )
                    throw new Exception( "OrgId mismatch rcCheckId=" + rc.getRcCheckId() + " accessCode points to " + orgId  + " but rcCheck.orgId=" + rc.getOrgId() );
            }

        }
        catch( STException ee )
        {
            throw ee;
            // throw new STException( "g.AccessCodeEnteredInvalid", ee.getParams() );
        }
        catch( Exception ee )
        {
            LogService.logIt( ee, "RefUtils.processEnterAccessCode() AAA.3 Parse AccessCode." );
            throw new STException( "g.AccessCodeEnteredInvalid", accessCode );
        }
        return rc;
    }



    public String performRcCheckStart( RcCheck rc, RefUserType refUserType, boolean sessionRecovery, boolean adminOverride)
    {
        try
        {
            getRefBean();
            getCorpBean();

            if( rc==null )
                throw new Exception( "RcCheck is null." );

            if( refUserType==null )
                throw new Exception( "RefUserType is null." );

            // LogService.logIt("RefUtils.performRcCheckStart() AAA.1 rcCheckId=" + rc.getRcCheckId() + ", refUserType=" + refUserType.getName() + ", sessionRecovery=" + sessionRecovery );

            refBean.clearBean();

            if( !getNewRefStartsOk() && corpBean.getHasCorp() )
                return corpBean.getCorp().getOfflinePage();

            refBean.setAdminOverride( adminOverride );

            refBean.setRefUserType( refUserType );

            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();

            // if status below completed and expire date has come. Expire it.
            if( !adminOverride && rc.getRcCheckStatusType().getCanExpire() && rc.getExpireDate().before( new Date() ) )
            {
                if( rcCheckUtils==null )
                    rcCheckUtils=new RcCheckUtils();
                rcCheckUtils.doExpireOrCompleteRcCheck( rc, adminOverride );
            }

            // Just to be sure.
            rc = (RcCheck)rc.clone();
            // at this point we have a valid RcCheck / RcRater
            rcCheckUtils.loadRcCheckForAdmin(rc, refUserType, getLocale(), refBean.getAdminOverride() );

            refBean.setRcCheck(rc);

            if( rc.getCorpId()>0 && ( !corpBean.getHasCorp() || corpBean.getCorp().getCorpId()!=rc.getCorpId() ) )
            {
                corpBean.clearBean();
                if( corpUtils==null )
                    corpUtils = CorpUtils.getInstance();
                
                corpUtils.loadCorpIfNeeded(rc.getCorpId(), true, getHttpServletResponse() );
            }


            if( rc.getRcScript().getActiveItemCount() <=0 )
            {
                LogService.logIt("RefUtils.performRcCheckStart() RcScript does not have any active items. Cannot conduct check. rcCheckId=" + rc.getRcCheckId() + ", rcScriptId=" + rc.getRcScriptId() + ", returning " + RefPageType.ERROR.getPageFull(refUserType) );
                refBean.setStrParam1( MessageFactory.getStringMessage(getLocale(), "g.RCScriptNoItems", new String[]{rc.getRcScript().getName(), Integer.toString(rc.getRcScriptId())} ));
                if( getSessionListener() != null )
                    getSessionListener().updateStatus( getHttpSession().getId(), "Fatal Error Page",null, null, rc, rc.getRcRater(), refUserType);
                refBean.setRefPageType(RefPageType.ERROR);
                return RefPageType.ERROR.getPageFull(refUserType);
            }

            if( !adminOverride )
            {
                RefCreditUtils rcu = new RefCreditUtils();
                rcu.checkRcPreAuthorization(rc, refUserType.getIsRater() ? rc.getRcRater() : null );
            }

            if( rc.getReportId()<=0 && rc.getRcScript().getReportId()>0 )
            {
                rc.setReportId( rc.getRcScript().getReportId() );
                rc.setReportId2( rc.getRcScript().getReportId2());
                if( !adminOverride )
                {
                    if( rcFacade==null )
                        rcFacade = RcFacade.getInstance();
                    rcFacade.saveRcCheck(rc, false);
                }
            }
            
            
            if( rc.getReportId()<=0 )
            {
                List<Integer> ridl = rcCheckUtils.getReportIdsForRcCheck(rc, rc.getLangCode()!=null && !rc.getLangCode().isBlank() ? rc.getLangCode() : getLocale().toString() );
                rc.setReportId( ridl.get(0) );
                if( ridl.size()>1 )
                    rc.setReportId2( ridl.get(1));
                if( !adminOverride )
                {
                    if( rcFacade==null )
                        rcFacade = RcFacade.getInstance();
                    rcFacade.saveRcCheck(rc, false);
                }
            }

            refBean.setRefUserType( refUserType );
            refBean.setRcCheck(rc);
            refBean.setAccessCode( refUserType.getIsCandidate() ? rc.getCandidateAccessCode() : rc.getRcRater().getRaterAccessCode() );
            refBean.setActiveAccessCodeX( refBean.getAccessCode() );

            if( !adminOverride  && getHttpServletResponse()!=null  )
                CookieUtils.setRcCheckCookie( getHttpServletResponse(), refBean.getAccessCode() );

            // if no input needed, set CandidateStatusTypeId to Completed.
            if( rc.getRcCandidateStatusType().getIsNotStarted() && !rc.getRequiresAnyCandidateInputOrSelfRating() )
            {
                rc.setRcCandidateStatusTypeId( RcCandidateStatusType.COMPLETED.getRcCandidateStatusTypeId() );
                rc.setLastUpdate(new Date() );
                if( !adminOverride )
                {
                    if( rcFacade==null )
                        rcFacade = RcFacade.getInstance();
                    rcFacade.saveRcCheck(rc, false);
                }
            }

            boolean inRaterGracePeriod = false;

            if( !sessionRecovery && refUserType.getIsCandidate() && !rc.getRcCheckStatusType().getCompleteOrHigher() )
            {
                Tracker.addCandidateEntry();
                //if( rc.getCandidateStarts()>0 )
                //{
                //    int secs = 0;
                //    if( rc.getLastUpdate()!=null && rc.getCandidateStartDate()!=null && rc.getCandidateStartDate().before(rc.getLastUpdate()) )
                //        secs = (int) (rc.getLastUpdate().getTime()-rc.getCandidateStartDate().getTime())/1000;
                //    rc.setCandidateSeconds(rc.getCandidateSeconds() + secs);
                //}
                rc.setCandidateStarts(rc.getCandidateStarts()+1 );
                rc.setLastSecondsDate( null );
                if( rc.getCorpId()<=0 )
                    rc.setCorpId( corpBean.getCorp().getCorpId() );

                if( rc.getCandidateStartDate()==null )
                    rc.setCandidateStartDate( new Date() );
                rc.setCandidateLastUpdate( new Date() );
                if( rc.getRcCandidateStatusType().getIsNotStarted() )
                {
                    if( rc.getRequiresAnyCandidateInputOrSelfRating() || rc.getCandidateCanAddRaters() )
                        rc.setRcCandidateStatusTypeId( RcCandidateStatusType.STARTED.getRcCandidateStatusTypeId() );
                    else
                        rc.setRcCandidateStatusTypeId( RcCandidateStatusType.COMPLETED.getRcCandidateStatusTypeId() );
                }

                if( !adminOverride  )
                {
                    rc.setLastUpdate(new Date() );
                    if( rcFacade==null )
                        rcFacade = RcFacade.getInstance();
                    rcFacade.saveRcCheck(rc, false);
                }
            }

            else if( !sessionRecovery &&
                    refUserType.getIsRater() &&
                    !rc.getRcRater().getRcRaterStatusType().getCompleteOrHigher() )
            {
                Tracker.addRaterEntry();

                if( rc.getRcCheckStatusType().getIsComplete() || rc.getRcCheckStatusType().getIsExpired() )
                    inRaterGracePeriod = rc.getRcRater().getInGracePeriod();

                // if not complete or higher and not
                if( !rc.getRcCheckStatusType().getCompleteOrHigher() || inRaterGracePeriod )
                {

                    //if( rc.getRcRater().getRaterStarts()>0 )
                    //{
                    //    int secs = 0;
                    //    if( rc.getRcRater().getLastUpdate()!=null && rc.getRcRater().getStartDate()!=null && rc.getRcRater().getStartDate().before(rc.getRcRater().getLastUpdate()) )
                    //        secs = (int) (rc.getRcRater().getLastUpdate().getTime()-rc.getRcRater().getStartDate().getTime())/1000;
                    //    rc.getRcRater().setRaterSeconds(rc.getRcRater().getRaterSeconds() + secs);
                    //}
                    rc.getRcRater().setRaterStarts(rc.getRcRater().getRaterStarts()+1 );
                    rc.setLastSecondsDate( null );

                    if( rc.getCorpId()<=0 )
                        rc.setCorpId( corpBean.getCorp().getCorpId() );

                    if( rc.getRcRater().getStartDate()==null )
                        rc.getRcRater().setStartDate( new Date() );
                    rc.getRcRater().setLastUpdate( new Date() );

                    if( !rc.getRcRater().getRcRaterStatusType().getStartedOrHigher() )
                        rc.getRcRater().setRcRaterStatusTypeId( RcRaterStatusType.STARTED.getRcRaterStatusTypeId() );

                    if( rcFacade==null )
                        rcFacade = RcFacade.getInstance();
                    if( !adminOverride  )
                    {
                        rcFacade.saveRcRater(rc.getRcRater(), false);
                    }

                    if( !rc.getRcCheckStatusType().getIsStartedOrHigher() )
                        rc.setRcCheckStatusTypeId( RcCheckStatusType.STARTED.getRcCheckStatusTypeId() );
                    rc.setLastUpdate(new Date() );
                    if( !adminOverride  )
                        rcFacade.saveRcCheck(rc, false);
                }
            }

            // updateStatus( String sid, String status, String corp, User user, RcCheck rcCheck, RcRater rcRater, RefUserType refUserType )
            if( !adminOverride && getSessionListener() != null )
                getSessionListener().updateStatus(getHttpSession().getId(), "Start process", "corpId=" + (corpBean.getCorp()==null ? 0 : corpBean.getCorp().getCorpId()), refUserType.getIsCandidate() ? rc.getUser() : ( rc.getRcRater()==null ? null : rc.getRcRater().getUser()), rc, rc.getRcRater(), refUserType);

            //else if( rc.getRcCheckStatusType().getCanAutoComplete() && rc.getExpireDate().before( new Date() ) )
            //{
            //    rc.setRcCheckStatusTypeId( RcCheckStatusType.COMPLETED.getRcCheckStatusTypeId() );
            //    rc.setCompleteDate( new Date() );
            //    rcFacade.saveRcCheck(rc, false);
            //}

            if( !adminOverride && rc.getRcCheckStatusType().getIsExpired() && !inRaterGracePeriod )
            {
                if( getSessionListener() != null )
                    getSessionListener().updateStatus( getHttpSession().getId(), "Expired Page",null, null, null, null, null);
                refBean.setRefPageType(RefPageType.EXPIRED);
                return RefPageType.EXPIRED.getPageFull(refUserType);
            }

            if( rc.getRcCheckStatusType().getIsCancelled() || (refUserType.getIsRater() && rc.getRcRater()!=null && rc.getRcRater().getRcRaterStatusType().getIsDeactivated()) )
            {
                if( getSessionListener() != null )
                    getSessionListener().updateStatus( getHttpSession().getId(), "Cancelled Page",null, null, rc, rc.getRcRater(), refUserType);
                refBean.setRefPageType(RefPageType.CANCELLED);
                return RefPageType.CANCELLED.getPageFull(refUserType);
            }

            if( !adminOverride && rc.getRcCheckStatusType().getCompleteOrHigher() && !inRaterGracePeriod )
            {
                if( getSessionListener() != null )
                    getSessionListener().updateStatus( getHttpSession().getId(), "Already Complete Page",null, null, null, null, null);
                refBean.setRefPageType(RefPageType.COMPLETE);
                return RefPageType.COMPLETE.getPageFull(refUserType);
            }

            if( refUserType.getIsRater() )
            {
                if( !adminOverride && rc.getRcRater().getRcRaterStatusType().getIsExpired() )
                {
                    if( getSessionListener() != null )
                        getSessionListener().updateStatus( getHttpSession().getId(), "Expired Page",null, null, null, null, null);
                    refBean.setRefPageType(RefPageType.EXPIRED);
                    return RefPageType.EXPIRED.getPageFull(refUserType);
                }

                if( rc.getRcRater().getRcRaterStatusType().getIsDeactivated() )
                {
                    if( getSessionListener() != null )
                        getSessionListener().updateStatus( getHttpSession().getId(), "Cancelled Page",null, null, null, null, null);
                    refBean.setRefPageType(RefPageType.CANCELLED);
                    return RefPageType.CANCELLED.getPageFull(refUserType);
                }

                if( !adminOverride && rc.getRcRater().getRcRaterStatusType().getCompleteOrHigher() && !inRaterGracePeriod )
                {
                    if( getSessionListener() != null )
                        getSessionListener().updateStatus( getHttpSession().getId(), "Already Complete Page",null, null, null, null, null);
                    refBean.setRefPageType(RefPageType.COMPLETE);
                    return RefPageType.COMPLETE.getPageFull(refUserType);
                }

            }
            
            if( refUserType.getIsCandidate() && rc.getTestKeyId()>0  )
            {
                EventFacade eventFacade = EventFacade.getInstance();
                TestKey tk = eventFacade.getTestKey(rc.getTestKeyId());
                
                if( tk!=null && tk.getProductTypeId()!=ProductType.REFERENCECHECK.getProductTypeId() )
                    booleanParam1=true;
                
                if( tk!=null && !tk.getTestKeyStatusType().getIsCompleteOrHigher() )
                {
                    tk.setLastAccessDate(new Date());
                    if( tk.getTestKeyStatusType().getIsActive() )
                        tk.setTestKeyStatusTypeId( TestKeyStatusType.STARTED.getTestKeyStatusTypeId() );
                    eventFacade.saveTestKey(tk);                    
                }
            }

            if( refUserType.getIsCandidate() && !rc.getRequiresAnyCandidateInputOrSelfRating() && !rc.getCandidateCanAddRaters() )
            {
                    LogService.logIt("RefUtils.performRcCheckStart() AAA.3 Candidate is Entering an RC Check that is configured to disable direct candidate input (or add raters). Showing complete page. rcCheckId=" + rc.getRcCheckId() + " moving to Confirm stage." );
                    if( getSessionListener() != null )
                        getSessionListener().updateStatus( getHttpSession().getId(), "Error - No Candidate Input Page",null, null, null, null, null);
                    refBean.setRefPageType(RefPageType.COMPLETE);
                    return systemError(rc==null ? null : rc.getOrg() , CorpBean.getInstance().getCorp(), MessageFactory.getStringMessage( getLocale(), "g.XCDirectInputNotAllowed", new String[] {this.getRcCheckCandidateName(), this.getRcCheckTypeName()} ), null, null, rc, rc==null ? null : rc.getRcRater(), true );
                    // return RefPageType.COMPLETE.getPageFull(refUserType);
            }

            if( !adminOverride && rc.getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture() || rc.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture() )
                getProctorBean().clearBean();

            // LogService.logIt("RefUtils.performRcCheckStart() AAA.2 rcCheckId=" + rc.getRcCheckId() + " moving to Confirm stage." );

            if( !adminOverride && getHttpServletRequest()!=null )
            {
                if( refUserType.getIsCandidate() )
                {
                    rc.setUserAgent( getHttpServletRequest().getHeader("User-Agent"));
                    rc.setIpAddress( HttpReqUtils.getClientIpAddress( getHttpServletRequest()));

                    String[] ccd = BaseRefUtils.getIpData( rc.getIpAddress() );
                    if( ccd!=null && ccd[0]!=null && !ccd[0].isBlank() )
                    {
                        // place in user record if empty.
                        if( rc.getUser().getIpCountry()==null || rc.getUser().getIpCountry().isBlank() )
                            setIpDataInUser( rc.getUser(), ccd );

                        rc.setIpCountry( ccd[0] );
                        rc.setIpState( ccd[1] );
                        rc.setIpCity( ccd[2] );
                    }
                }

                if( refUserType.getIsRater())
                {
                    rc.getRcRater().setUserAgent( getHttpServletRequest().getHeader("User-Agent"));
                    rc.getRcRater().setIpAddress( HttpReqUtils.getClientIpAddress( getHttpServletRequest()));

                    // Always get latest IP Location and place in ref records.
                    String[] ccd = BaseRefUtils.getIpData( rc.getRcRater().getIpAddress() );
                    if( ccd!=null && ccd[0]!=null && !ccd[0].isBlank() )
                    {
                        // place in user record if empty.
                        if( rc.getRcRater().getUser().getIpCountry()==null || rc.getRcRater().getUser().getIpCountry().isBlank() )
                            setIpDataInUser( rc.getRcRater().getUser(), ccd );

                        rc.getRcRater().setIpCountry( ccd[0] );
                        rc.getRcRater().setIpState( ccd[1] );
                        rc.getRcRater().setIpCity( ccd[2] );
                    }
                    //if( rc.getRcRater().getUser().getIpCountry()==null || rc.getRcRater().getUser().getIpCountry().isBlank() )
                    //    setIpDataInUser( rc.getRcRater().getUser(), rc.getIpAddress() );
                }
            }

            if( getSessionListener() != null )
                getSessionListener().updateStatus( getHttpSession().getId(), "Confirm Page",null, null, null, null, null);
            refBean.setRefPageType(RefPageType.CONFIRM);
            return RefPageType.CONFIRM.getPageFull(refUserType);
        }
        catch( STException e )
        {
            LogService.logIt( "RefUtils.performRcCheckStart() " + e.toString() );
            setMessage( e );
            if( e.getKey()!=null && e.getKey().equalsIgnoreCase( "g.OrgCreditUsgResultNone" ) )
                return systemError(rc==null ? null : rc.getOrg() , CorpBean.getInstance().getCorp(), MessageFactory.getStringMessage( getLocale(), e.getKey(), e.getParams() ), null, null, rc, rc==null ? null : rc.getRcRater(), true );

            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RefUtils.performRcCheckStart() " + (rc==null ? "" : rc.toStringShort() ) + ", RefUserType=" + (refUserType==null ? "null" : refUserType.getName()) );
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg() , CorpBean.getInstance().getCorp(), e.toString(), null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public static String[] getIpData( String ipAddress )
    {
        try
        {
            return (new IpUtils()).getIPLocationData( ipAddress );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRefUtils.getIpData() ipAddress=" + ipAddress );
            return new String[5];
        }
    }

    public static void setIpDataInUser( User user, String[] ccd )
    {
        try
        {
            if( user==null || ( user.getIpCountry()!=null && !user.getIpCountry().isBlank()) )
            {
                LogService.logIt( "BaseRefUtils.setIpDataInUser() Skipping user=" + (user==null ? "null" : user.getUserId() + ", country=" + user.getIpCountry() ) );
                return;
            }

            // String[] ccd = (new IpUtils()).getIPLocationData( ipAddress );
            if( ccd!=null && ccd[0]!=null && !ccd[0].isBlank() )
            {
                user.setIpCountry( ccd[0] );
                user.setIpState( ccd[1] );
                user.setIpCity( ccd[2] );

                if( user.getCountryCode()==null || user.getCountryCode().isBlank() )
                    user.setCountryCode( user.getIpCountry() );

                if( user.getUserId()>0 )
                    UserFacade.getInstance().saveUser(user, false );
            }

        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRefUtils.setIpDataInUser() user=" + (user==null ? "null" : user.getUserId() + ", country=" + user.getIpCountry() ) );
        }
    }


    public static void setIpDataInUser( User user, String ipAddress )
    {
        try
        {
            if( user==null || ( user.getIpCountry()!=null && !user.getIpCountry().isBlank()) || ipAddress==null || ipAddress.isBlank() )
            {
                LogService.logIt( "BaseRefUtils.setIpDataInUser() Skipping user=" + (user==null ? "null" : user.getUserId() + ", country=" + user.getIpCountry() ) + ", ipAddress=" + ipAddress );
                return;
            }

            String[] ccd = (new IpUtils()).getIPLocationData( ipAddress );
            if( ccd!=null && ccd[0]!=null && !ccd[0].isBlank() )
            {
                user.setIpCountry( ccd[0] );
                user.setIpState( ccd[1] );
                user.setIpCity( ccd[2] );

                if( user.getCountryCode()==null || user.getCountryCode().isBlank() )
                    user.setCountryCode( user.getIpCountry() );

                if( user.getUserId()>0 )
                    UserFacade.getInstance().saveUser(user, false );
            }

        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRefUtils.setIpDataInUser() user=" + (user==null ? "null" : user.getUserId() + ", country=" + user.getIpCountry() ) + ", ipAddress=" + ipAddress );
        }
    }


    protected String getAccessCodeFmCookie()
    {
        try
        {
            HttpServletRequest req = getHttpServletRequest();
            if( req == null )
                return null;

            Cookie c = CookieUtils.getCookie(req , CookieUtils.ACCESSCODE_COOKIE_NAME );
            if( c != null && c.getValue() != null && !c.getValue().isEmpty() )
                return c.getValue();
        }
    	catch( Exception e )
    	{
            LogService.logIt( e, "RefUtils.getAccessCodeFmCookie() " );
    	}
        return null;
    }




    //public boolean getNewRefStartsOk()
    //{
    //    return RuntimeConstants.getBooleanValue( "newRefStartsOK");
    //}


    public String systemError( Org org, Corp corp, String message, String key, String[] params, RcCheck rc, RcRater rcRater, boolean autoforwardToExit )
    {
        try
        {
            getRefBean();

            getCorpBean();

            if( !corpBean.getHasCorp() )
                corpBean.loadDefaultCorp();

            if( corp == null )
                corp = corpBean.getCorp();

            if( key != null && !key.isEmpty() )
                message = MessageFactory.getStringMessage( getLocale() , key , params );

            if( message != null && !message.isEmpty() )
                getRefBean().setErrorMessage(message);

            String exitUrl = null;

            if( !refBean.getAdminOverride() && rc!=null && rc.getRcCheckId()>0 && message!=null && !message.isBlank() )
            {
                HttpServletRequest req=getHttpServletRequest();
                RcCheckLogUtils.createRcCheckLogEntry(rc.getRcCheckId(), rcRater==null ? 0 : rcRater.getRcRaterId(), RcCheckLogLevelType.ERROR.getRcCheckLogLevelTypeId(), message, req==null ? null : HttpReqUtils.getClientIpAddress(req), req==null ? null :  req.getHeader("User-Agent"));
            }

            if( rc != null )
            {
                if( rc.getSuborgId() > 0 )
                {
                        if( userFacade == null )
                            userFacade = UserFacade.getInstance();

                        Suborg suborg = userFacade.getSuborg(rc.getSuborgId() );

                        if( suborg.getDefaultCorpExitUrl()!= null && !suborg.getDefaultCorpExitUrl().isEmpty() )
                            exitUrl = suborg.getDefaultCorpExitUrl();

                }
            }

            if( exitUrl == null || exitUrl.isEmpty() )
            {
                if( org == null && rc != null )
                {
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();

                    org = userFacade.getOrg(rc.getOrgId() );
                }

                if( org != null )
                    exitUrl = org.getDefaultCorpExitUrl();

            }

            if( exitUrl == null || exitUrl.isEmpty() )
            {
                if( corp != null && corp.getDefaultReturnUrl()!= null && !corp.getDefaultReturnUrl().isEmpty() )
                    exitUrl = corp.getDefaultReturnUrl();
            }

            if( exitUrl !=null &&  !exitUrl.isEmpty() )
            {
                if( !exitUrl.toLowerCase().startsWith( "http" ) )
                    exitUrl = "http://" + exitUrl;
            }

            LogService.logIt("RcUtils.systemError() key=" + key + ", message=" + message + ", Exit URL=" + exitUrl + ", rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) + ", rcRaterId=" + (rcRater==null ? "null" : rcRater.getRcRaterId() ) + ", org=" + (org==null ? "null" : org.getName() + " (" + org.getOrgId() + ")") );

            CookieUtils.removeRcCheckCookie( getHttpServletResponse() );

            // if( testKey != null && testKey.getTestKeyStatusType().getIsCompleteOrHigher() && (testKey.getTestKeySourceType().isApi() || (corp==null || corp.getUseDirectExit()==1) )   )
            if( rc != null && rc.getRcCheckStatusType().getCompleteOrHigher() && corp!=null && corp.getUseDirectExit()==1   )
            {
                if( exitUrl !=null &&  !exitUrl.isEmpty() )
                {                    
                    if( getHttpServletResponse()!=null )
                    {
                        FacesContext fc = FacesContext.getCurrentInstance();
                        if( fc!=null )
                            fc.responseComplete();
                        LogService.logIt("RcUtils.systemError() RcCheck is already complete.  RcCheck: " + rc.getRcCheckId() + ", sending to Return URL=" + exitUrl  );
                        getHttpServletResponse().sendRedirect( exitUrl );
                        return "redirect";
                    }

                    else
                        LogService.logIt("RcUtils.systemError() Non-Fatal RcCheck: " + rc.getRcCheckId() + ", RcCheck is already complete. Http Request not available. Error URL=" + exitUrl  );
                }

            } // END Complete or higher with Direct Exit

            getRefBean();

            if( refBean != null )
            {
                refBean.setErrorReturnUrl( exitUrl );
                refBean.setErrorAutoForward( exitUrl==null || exitUrl.isEmpty() ? false : autoforwardToExit );
            }
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RcUtils.systemError() corpId=" + (corp == null ? 0 : corp.getCorpId() ) + ", orgId=" + (org==null ? 0 : org.getOrgId() ) + ", RcCheck: " + (rc == null ? "Null" : rc.toString() )  );
        }

        return "/" + corpBean.getDirectory() + "/initerror.xhtml";
    }


    
    

    protected RefBean getRefBean()
    {
    	if( refBean == null )
    	    refBean = RefBean.getInstance();

        return refBean;
    }

    protected ProctorBean getProctorBean()
    {
    	if( proctorBean == null )
    	    proctorBean = ProctorBean.getInstance();

        return proctorBean;
    }


    protected CorpBean getCorpBean()
    {
    	if( corpBean == null )
            corpBean = CorpBean.getInstance();

        return corpBean;
    }


    public void setCorpBean(CorpBean cb) {
        this.corpBean = cb;
    }

    public void setCorpUtils(CorpUtils corpUtils) {
        this.corpUtils = corpUtils;
    }

    public void setRefBean(RefBean cb) {
        this.refBean = cb;
    }

    public boolean isBooleanParam1() {
        return booleanParam1;
    }

    public void setBooleanParam1(boolean booleanParam1) {
        this.booleanParam1 = booleanParam1;
    }

    public boolean isBooleanParam2() {
        return booleanParam2;
    }

    public void setBooleanParam2(boolean booleanParam2) {
        this.booleanParam2 = booleanParam2;
    }

    public void setProctorBean(ProctorBean proctorBean) {
        this.proctorBean = proctorBean;
    }




}
