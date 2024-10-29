/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.corp.CorpBean;
import com.tm2ref.corp.CorpUtils;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.ref.RcScript;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.STException;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.CountryCodeLister;
import com.tm2ref.user.RoleType;
import com.tm2ref.user.UserFacade;
import com.tm2ref.user.UserType;
import com.tm2ref.util.CookieUtils;
import com.tm2ref.util.GooglePhoneUtils;
import com.tm2ref.util.StringUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ListIterator;

/**
 *
 * @author miker_000
 */

@Named
@RequestScoped
public class CandidateRefUtils extends BaseRefUtils
{

    @Inject
    CandidateRefBean candidateRefBean;


    public static CandidateRefUtils getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return (CandidateRefUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "candidateRefUtils" );
    }

    public List<SelectItem> getRcRaterRoleTypeSelectItemList()
    {
        getRefBean();

        return RcRaterRoleType.getSelectItemList(getLocale(), false, refBean.getRcCheck()!=null && refBean.getRcCheck().getRcOrgPrefs()!=null ? refBean.getRcCheck().getRcOrgPrefs().getOtherRoleTypeNames(refBean.getRcCheck().getRcSuborgPrefs()) : null  );
    }


    public String processGoBackToLastCandidateInputQuestion()
    {
        getCorpBean();
        getRefBean();

        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
                throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );

            if( !rc.getRcScript().getHasAnyCandidateInput() )
                throw new Exception( "RcCheck.script does not require any candidate questions." );

            RefPageType refPageType = refBean.getRefPageType();
            if( !refPageType.getIsCore() )
                refBean.setRefPageType( RefPageType.CORE );

            setLastCandidateInputNumberAndValue();
            candidateRefBean.setBackToCore(true);

            if( candidateRefBean.getCandidateInputNumber()<=0 )
            {
                // move on to Self Ratings
                if( rc.getCollectRatingsFmCandidate() ) // getNeedsCore2() )
                {
                    refBean.setRefPageType(RefPageType.CORE2 );
                    return doEnterSelfRatings();
                }

                // move on to raters.
                else if( getNeedsCore3() )
                {
                    refBean.setRefPageType(RefPageType.CORE3 );
                    if( rc.getRcRaterListCandidate().size()>0 && rc.getNeedsSupervisors() )
                        setInfoMessage( "g.XCAddReferences.belowminsups", new String[]{ getRcCheckRaterNameLc(), getRcCheckRatersNameLc(),Integer.toString(rc.getRcRaterListCandidate().size()),null,null,null,null,Integer.toString(rc.getRcRaterListCandidateSupers().size()), Integer.toString(rc.getMinSupervisors())} );
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
                }
                // exit - we're done.
                else
                    return processExitAllCore();

            }

            // next question.
            return conditionUrlForSessionLossGet("/ref/question.xhtml", true);
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processGoBackToLastCandidiateInputQuestion() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }

    }


    /**
     * Question responses are a CORE1 process.
     * @return
     */
    public String processSaveQuestionResp()
    {
        getCorpBean();
        getRefBean();

        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
                throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );

            RefPageType refPageType = refBean.getRefPageType();
            if( !refPageType.getIsCore() )
                return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );

            // RcScript sc = rc.getRcScript();
            boolean goBack = booleanParam1;
            String val = candidateRefBean.getCandidateInputStr();
            int idx = candidateRefBean.getCandidateInputNumber();

            int reqInput = getCandidateInputNumberFmRequest();
            if( reqInput!=idx )
            {
                // setErrorMessage("g.XCErrGen", new String[]{"Request index does not match index in session."} );
                LogService.logIt( "CandidateRefUtils.processSaveQuestionResp() AAA.0 getCandidateInputNumberFmRequest=" + reqInput + " does not match candidateRefBean.inputNum=" + idx + ", goBack=" + goBack  + ". Resetting to the correct index. rcCheckId=" + rc.getRcCheckId());
                // candidateRefBean.setCandidateInputStr( candidateRefBean.getCandidateInputNumber()<=0 ? null : rc.getCandidateInputStr( candidateRefBean.getCandidateInputNumber() ) );
                candidateRefBean.setCandidateInputStr( reqInput<=0 ? null : rc.getCandidateInputStr( reqInput ) );
                candidateRefBean.setCandidateInputNumber(reqInput);
                // return "/ref/question.xhtml";
            }

            LogService.logIt( "CandidateRefUtils.processSaveQuestionResp() AAA.1 idx=" + idx + ", goBack=" + goBack );
            if( goBack && idx<=1 )
                goBack=false;

            if( !goBack && (val==null || val.isBlank()) )
                throw new STException( "g.XCAnswerRqd" );

            rc.setCandidateInputStr(idx, val);
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            if( !refBean.getAdminOverride() )
                rcFacade.saveRcCheck(rc, true);

            if( !refBean.getAdminOverride() && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
                updateRcCheckAndCandidateStatusAndSendProgressMsgs( rc );

            if( goBack )
            {
                setPreviousCandidateInputNumberAndValue();
                LogService.logIt( "CandidateRefUtils.processSaveQuestionResp() BBB.1 after setPreviousCandidateInputNumber, candidateInputNumber=" + candidateRefBean.getCandidateInputNumber() );
                if( candidateRefBean.getCandidateInputNumber()<=0 )
                {
                    LogService.logIt( "CandidateRefUtils.processSaveQuestionResp() set to go back but there is no question to go back to. rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                    setNextCandidateInputNumberAndValue(false);

                    // This should never happen as long as there are any candidte input questions.
                    if( candidateRefBean.getCandidateInputNumber()<=0 )
                    {
                        // move on to Self Ratings
                        if( rc.getCollectRatingsFmCandidate() ) // getNeedsCore2() )
                        {
                            refBean.setRefPageType(RefPageType.CORE2 );
                            return conditionUrlForSessionLossGet(doEnterSelfRatings(), true);
                        }

                        // move on to raters.
                        else if( getNeedsCore3() )
                        {
                            refBean.setRefPageType(RefPageType.CORE3 );
                            if( rc.getRcRaterListCandidate().size()>0 && rc.getNeedsSupervisors() )
                                setInfoMessage( "g.XCAddReferences.belowminsups", new String[]{ getRcCheckRaterNameLc(), getRcCheckRatersNameLc(),Integer.toString(rc.getRcRaterListCandidate().size()),null,null,null,null,Integer.toString(rc.getRcRaterListCandidateSupers().size()), Integer.toString(rc.getMinSupervisors())} );
                            return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
                        }

                        // exit - we're done.
                        else
                            return processExitAllCore();
                    }
                }
            }

            else
            {
                setNextCandidateInputNumberAndValue(false);
                LogService.logIt( "CandidateRefUtils.processSaveQuestionResp() BBB.2 after setNextCandidateInputNumberAndValue, candidateInputNumber=" + candidateRefBean.getCandidateInputNumber() );

                // No more questions.
                if( candidateRefBean.getCandidateInputNumber()<=0 )
                {
                    // move on to Self Ratings
                    if( rc.getCollectRatingsFmCandidate() ) // getNeedsCore2() )
                    {
                        refBean.setRefPageType(RefPageType.CORE2 );
                        return conditionUrlForSessionLossGet(doEnterSelfRatings(), true);
                    }

                    // move on to raters.
                    else if( getNeedsCore3() )
                    {
                        refBean.setRefPageType(RefPageType.CORE3 );
                        if( rc.getRcRaterListCandidate().size()>0 && rc.getNeedsSupervisors() )
                            setInfoMessage( "g.XCAddReferences.belowminsups", new String[]{ getRcCheckRaterNameLc(), getRcCheckRatersNameLc(),Integer.toString(rc.getRcRaterListCandidate().size()),null,null,null,null,Integer.toString(rc.getRcRaterListCandidateSupers().size()), Integer.toString(rc.getMinSupervisors())} );
                        return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true);
                    }
                    // exit - we're done.
                    else
                        return processExitAllCore();
                }
            }

            // next question.
            return conditionUrlForSessionLossGet("/ref/question.xhtml", true);
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processSaveQuestionResp() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public String doEnterSelfRatings()
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        try
        {
            setInfoMessage( "g.XCNextArePerfQuestions" , new String[] {this.getRcCheckTypeName()} );
            getRefBean();
            RaterRefUtils rru = RaterRefUtils.getInstance();
            rru.doEnterCore( false ); //  !candidateRefBean.isBackToCore() );
            return rru.getNextViewFromRatings();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.doEnterSelfRatings() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) );
            setMessage(e);
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public String doCompleteSelfRatings()
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        try
        {
            //if( rcCheckUtils==null )
            //    rcCheckUtils = new RcCheckUtils();
            //rcCheckUtils.performRcCandidateCompletionIfReady(rc);
            if( !refBean.getAdminOverride() )
                updateRcCheckAndCandidateStatusAndSendProgressMsgs( rc );

            // needs references.
            if( getNeedsCore3() )
            {
                refBean.setRefPageType(RefPageType.CORE3 );
                if( rc.getRcRaterListCandidate().size()>0 && rc.getNeedsSupervisors() )
                    setInfoMessage( "g.XCAddReferences.belowminsups", new String[]{ getRcCheckRaterNameLc(), getRcCheckRatersNameLc(),Integer.toString(rc.getRcRaterListCandidate().size()),null,null,null,null,Integer.toString(rc.getRcRaterListCandidateSupers().size()), Integer.toString(rc.getMinSupervisors())} );
                return getViewFromPageType( refBean.getRefPageType() );
            }

            // exit - we're done.
            else
                return processExitAllCore();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.doCompleteSelfRatings() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) );
            setMessage(e);
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public String getCandidateInputQuestion()
    {
        getRefBean();
        return refBean.getRcCheck().getRcScript().getCandidateStrQuestion( candidateRefBean.getCandidateInputNumber() );
    }


    public String getCandidateInputQuestionXhtml()
    {
        getRefBean();
        return getUserTextXhtml( refBean.getRcCheck().getRcScript().getCandidateStrQuestion( candidateRefBean.getCandidateInputNumber() ) );
    }


    public void doEnterCore() throws Exception
    {
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                throw new Exception( "RcCheck is null in RcBean." );
            if( rc.getRcScript()==null )
            {
                if( rcScriptFacade==null )
                    rcScriptFacade=RcScriptFacade.getInstance();
                RcScript rcs = rcScriptFacade.getRcScript(rc.getRcScriptId(), true );
                rc.setRcScript( (RcScript)rcs.clone() );
            }

            candidateRefBean.setBackToCore(false);
            candidateRefBean.setCandidateInputNumber(0);
            setNextCandidateInputNumberAndValue(true);

            // LogService.logIt( "CandidateRefUtils.doEnterCore() needsCore()=" + this.getNeedsCore() + ", needsCore2()=" + this.getNeedsCore2() + ", candidateInputNumber=" + candidateRefBean.getCandidateInputNumber() + ", rcCheckId="  + rc.getRcCheckId() );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.doEnterCore() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) );
            throw e;

        }
    }


    public void setNextCandidateInputNumberAndValue(boolean goToFirstUnanswered)
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        RcScript sc = rc.getRcScript();

        boolean found = false;
        for( int i=candidateRefBean.getCandidateInputNumber()+1; i<=5; i++ )
        {
            if( sc.getHasCandidateInput(i) )
            {
                if( !goToFirstUnanswered || (rc.getCandidateInputStr(i)==null || rc.getCandidateInputStr(i).isBlank() ) )
                {
                    candidateRefBean.setCandidateInputNumber(i);
                    found=true;
                    break;
                }
            }
        }
        if( !found )
            candidateRefBean.setCandidateInputNumber(0);

        // this is the value the candidate has already entered.
        candidateRefBean.setCandidateInputStr( candidateRefBean.getCandidateInputNumber()<=0 ? null : rc.getCandidateInputStr( candidateRefBean.getCandidateInputNumber() ) );
    }

    public void setLastCandidateInputNumberAndValue()
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        RcScript sc = rc.getRcScript();

        boolean found = false;
        for( int i=5; i>0; i-- )
        {
            if( sc.getHasCandidateInput(i) )
            {
                candidateRefBean.setCandidateInputNumber(i);
                found=true;
                break;
            }
        }
        if( !found )
            candidateRefBean.setCandidateInputNumber(0);

        // this is the value the candidate has already entered.
        candidateRefBean.setCandidateInputStr( candidateRefBean.getCandidateInputNumber()<=0 ? null : rc.getCandidateInputStr( candidateRefBean.getCandidateInputNumber() ) );
    }


    public boolean getAllCandidateQuestionsAnswered()
    {
        return getFirstUnansweredCandidateQuestionNumber()<=0;
    }

    public int getFirstUnansweredCandidateQuestionNumber()
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        RcScript sc = rc.getRcScript();
        for( int i=1;i<=5; i++)
        {
            if( sc.getHasCandidateInput(i)  && ( rc.getCandidateInputStr(i)==null || rc.getCandidateInputStr(i).isBlank() ) )
                return i;
        }
        return 0;
    }


    public void setPreviousCandidateInputNumberAndValue()
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        RcScript sc = rc.getRcScript();

        boolean found = false;
        for( int i=candidateRefBean.getCandidateInputNumber()-1; i>=0; i-- )
        {
            if( sc.getHasCandidateInput(i) )
            {
                candidateRefBean.setCandidateInputNumber(i);
                found=true;
                break;
            }
        }
        if( !found )
            candidateRefBean.setCandidateInputNumber(0);

        candidateRefBean.setCandidateInputStr( candidateRefBean.getCandidateInputNumber()<=0 ? null : rc.getCandidateInputStr( candidateRefBean.getCandidateInputNumber() ) );
    }


    public boolean getNeedsCore() throws Exception
    {
        return candidateRefBean.getCandidateInputNumber()>0;
    }

    public boolean getNeedsCore2() throws Exception
    {
        getRefBean();
        return refBean.getRcCheck().getCollectRatingsFmCandidate() && refBean.getRcCheck().getRcRater()!=null && !refBean.getRcCheck().getRcRater().getRcRaterStatusType().getCompleteOrHigher();
    }
    public boolean getNeedsCore3() throws Exception
    {
        getRefBean();
        return refBean.getRcCheck().getCandidateCannotAddRaters()<=0;
    }


    public int getRaterQuestionNumberForCandidate()
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        RaterRefUtils rrb = RaterRefUtils.getInstance();
        int n = rrb.getRaterQuestionNumber();
        n += getCandidateQuestionCount();
        return n;
    }

    public int getCandidateTotalQuestionCount()
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        RcScript sc = rc.getRcScript();
        int n = getCandidateQuestionCount();
        if( rc.getCollectRatingsFmCandidate() )
            n += sc.getItemCount(true);
        return n;
    }


    public int getCandidateQuestionCount()
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        RcScript sc = rc.getRcScript();
        int n = 0;
        for( int i=1;i<=5; i++)
        {
            if( sc.getHasCandidateInput(i) )
                n++;
        }
        return n;
    }

    public int getCandidateQuestionNumber()
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        RcScript sc = rc.getRcScript();
        int idx = candidateRefBean.getCandidateInputNumber();
        int n = 0;
        for( int i=1;i<=idx; i++)
        {
            if( sc.getHasCandidateInput(i) )
                n++;
        }
        return n;
    }


    public String getReferencesKeyToShow()
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
            List<RcRater> rcrl = rc.getRcRaterListCandidate();
            int sups = 0;
            for( RcRater r : rcrl )
            {
                if( r.getRcRaterRoleType().getIsSupervisorOrManager() )
                    sups ++;
            }
            if( rcrl.isEmpty() )
                return "g.XCAddReferences.none";
            else if( sups<rc.getMinSupervisorsCandidate() && rcrl.size()<rc.getMinRatersCandidate() )
                return rc.getMinSupervisorsCandidate()>0 ? "g.XCAddReferences.belowmins" :  "g.XCAddReferences.belowminsnosups";
            else if( sups<rc.getMinSupervisorsCandidate() && rcrl.size()>=rc.getMinRatersCandidate() )
                return "g.XCAddReferences.belowminsups";
            else if( sups>=rc.getMinSupervisorsCandidate() && rcrl.size()<rc.getMinRatersCandidate() )
                return "g.XCAddReferences.belowminsnosups";
            //else if( rcrl.size()<rc.getMinRatersCandidate() )
            //    return "g.XCAddReferences.belowmin";
            else if( rcrl.size()<rc.getMinRatersOptimum())
                return "g.XCAddReferences.belowoptimium";
            else if( rcrl.size()<rc.getMaxRatersDefault() )
                return "g.XCAddReferences.belowmax";
            else if( rcrl.size()>=rc.getMaxRatersDefault() )
                return "g.XCAddReferences.atmax";
            return "g.XCAddReferences.none";
        }
        catch( STException e )
        {
            setMessage(e);
            return "";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.getReferencesKeyToShow() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) );
            setMessage( e );
            return "";
        }
    }

    public String processBackToViewRaters()
    {
        getRefBean();

        refBean.setRefPageType(RefPageType.CORE3);
        return processViewRaters();
    }


    public String processViewRaters()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            candidateRefBean.clearBean();
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true);
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
                throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );

            RefPageType refPageType = refBean.getRefPageType();
            if( !refPageType.getIsCore3() )
                return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true);

            LogService.logIt( "CandidateRefUtils.processViewRaters() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            RefUserType refUserType = refBean.getRefUserType();
            if( !refUserType.getIsCandidate() )
                throw new Exception( "RefUserType is incorrect: " + refUserType.getName() );

            if( rc.getRcRaterList()==null )
            {
                if( rcCheckUtils==null )
                    rcCheckUtils=new RcCheckUtils();
                rcCheckUtils.loadRcCheckForAdmin(rc, refUserType, getLocale(), refBean.getAdminOverride() );
            }

            return conditionUrlForSessionLossGet("/ref/references.xhtml", true);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processViewRaters() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public int getCandidateInputNumberFmRequest()
    {
        String r = null;
        try
        {
            r = getHttpServletRequest().getParameter("candinputnum");
            if( r==null || r.isBlank() )
                return 0;
            return Integer.parseInt( r );
        }
        catch( NumberFormatException e )
        {
            LogService.logIt( "CandidateRefUtils.getCandidateInputNumberFmRequest() NumberFormatException " + e.toString() + ", r=" + r);
            return 0;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.getCandidateInputNumberFmRequest() r=" + r);
            return 0;
        }
    }


    public long getRcCheckIdFmRequest()
    {
        return getEncryptedIdFmRequest( "rcid" );
    }

    public long getRcRaterIdFmRequest()
    {
        return getEncryptedIdFmRequest( "rcrid" );
    }

    public boolean getShowReferencesGoBack()
    {
        getRefBean();
        RcScript rs = refBean.getRcCheck()==null || refBean.getRcCheck().getRcScript()==null ? null : refBean.getRcCheck().getRcScript();
        if( rs==null )
            return false;

        RcCheck rc = refBean.getRcCheck();

        // No inputs and no self rating. No reason to go back.
        return  rc.getCandidateQuestionsAndSelfRatingCount()>0; // rc.getRequiresAnyCandidateInputOrSelfRating();

        /*
        if( !rc.getRequiresAnyCandidateInputOrSelfRating() )
            return false;

        // needs self ratings. See if complete.
        if( rc.getCollectRatingsFmCandidate() )
        {
            RcRater rtr = rc.getRcRaterForUserId( rc.getUserId() );
            if( rtr==null || !rtr.getRcRaterStatusType().getCompleteOrHigher() )
                return true;
        }

        // needs candidate input. See if complete.
        if( rs.getHasAnyCandidateInput() )
        {
            try
            {
                RefPageType rpt = getPreviousPageTypeForRefProcess();
                if( rpt.getIsCore() )
                    return true;
            }
            catch( Exception e )
            {
                LogService.logIt( e, "CandidateRefUtils.getShowReferencesGoBack() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                return true;
            }
        }
        return false;
        */
    }

    public String processReferencesGoBack()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();


            // LogService.logIt( "CandidateRefUtils.processReferencesGoBack() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));

            refBean.setRefPageType( RefPageType.CORE3 );
            RefPageType rpt = getPreviousPageTypeForRefProcess();
            LogService.logIt( "CandidateRefUtils.processReferencesGoBack() BBB new RefPageType=" + rpt.getName() + ", rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            refBean.setRefPageType(rpt);

            if( rpt.getIsCore() && rc.getRcScript().getHasAnyCandidateInput() )
                return this.processGoBackToLastCandidateInputQuestion();

            return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processReferencesGoBack() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public void doExitCoreBeacon( long rcCheckId )
    {
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                rc = repairRefBeanForCurrentAction(refBean, true );
            // LogService.logIt( "CandidateRefUtils.doExitCoreBeacon() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcCheckId=" + rcCheckId );
            if( rc == null )
                return;

            // clone so no other operation works on this same object.
            rc = (RcCheck) rc.clone();

            Thread.sleep((int) (5000*Math.random()));
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();

            // We can sometimes get multiple simultaneous beacons, so reload and check that not complete.
            Thread.sleep((int) (6000*Math.random()));


            if( !refBean.getAdminOverride() )
                sendUnsentRcRaters( rc );

            if( !refBean.getAdminOverride() )
                updateRcCheckAndCandidateStatusAndSendProgressMsgs( rc );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processExitCore() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    protected void updateRcCheckAndCandidateStatusAndSendProgressMsgs( RcCheck rc ) throws Exception
    {
        if( rcCheckUtils==null )
            rcCheckUtils = new RcCheckUtils();

        getRefBean();

        rcCheckUtils.performRcCandidateCompletionIfReady(rc, refBean.getAdminOverride() );

        if( (rc.getRcRaterList().size()>=1 || rc.getRcCandidateStatusType().getIsCompletedOrHigher()) && !rc.getRcCheckStatusType().getIsStartedOrHigher() )
        {
            rc.setRcCheckStatusTypeId( RcCheckStatusType.STARTED.getRcCheckStatusTypeId() );

            if( !refBean.getAdminOverride() )
            {
                if( rcFacade==null )
                    rcFacade = RcFacade.getInstance();
                rcFacade.saveRcCheck( rc, true );
            }
        }

        if( !refBean.getAdminOverride() && rc.getRcCandidateStatusType().getIsCompletedOrHigher() && rc.getLastCandidateProgressMsgDate()==null )
        {
            rcCheckUtils.loadRcCheckForScoringOrResults(rc);
            rcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete(rc, null, false );
        }

        // rcCheckUtils.performRcCheckCompletionIfReady(rc);


        //if( rc.getRcRaterListCandidate().size()>=rc.getMinRaters() && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
        //{
        //    rc.setRcCandidateStatusTypeId( RcCandidateStatusType.COMPLETED.getRcCandidateStatusTypeId() );
        //    rc.setCandidateCompleteDate( new Date() );
        //    chg=true;
        //}

        //if( rc.getRcRaterListCandidate().size()>=rc.getMinRaters() && rc.getCandidateCompleteDate()==null )
        //{
        //    rc.setCandidateCompleteDate( new Date() );
        //    chg=true;
        //}

        //if( !chg )
        //    return;

        //if( chg )
    }


    protected void sendUnsentRcRaters( RcCheck rc ) throws Exception
    {
        getRefBean();

        if( rc.getCandidateCannotAddRaters()==1 )
            return;

        List<RcRater> rcrl = rc.getRcRaterListCandidate();
        int[]  sendstats = null;

        for( RcRater rcr : rcrl )
        {
            if( !rcr.getCandidateCanSend() )
                continue;

            if( !rcr.getRcRaterStatusType().getSentOrHigher() )
            {
                sendstats = sendRcCheckToRater(rc, rcr, false, false, false );
                if( sendstats[0]>0 || sendstats[1]>0 )
                {
                    rcr.setRcRaterStatusTypeId( RcRaterStatusType.SENT.getRcRaterStatusTypeId() );

                    if( !refBean.getAdminOverride() )
                    {
                        if( rcFacade==null )
                            rcFacade = RcFacade.getInstance();
                        rcFacade.saveRcRater(rcr, false);
                    }
                }
            }
            else if( !refBean.getAdminOverride() && (rcr.getNeedsResendEmail() || rcr.getNeedsResendMobile()) )
                sendRcCheckToRater(rc, rcr, true, false, false );
        }
    }

    public String processExitAllCore()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq>0 && rcChkReq!=rc.getRcCheckId() )
                throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );
            // LogService.logIt( "CandidateRefUtils.processExitAllCore() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));

            if( rc.getCandidateCanAddRaters() )
            {
                int refCt = rc.getRcRaterListCandidate().size();
                if( refCt < rc.getMinRatersCandidate() )
                    throw new STException( "g.XCErrMinRaters", new String[] {Integer.toString( rc.getMinRatersCandidate()), getRcCheckRatersNameLc()});

                if( rc.getMinSupervisorsNeeded()>0  )
                    throw new STException( "g.XCErrMinSupervisors", new String[] {Integer.toString( rc.getMinSupervisorsCandidate()), getRcCheckRatersNameLc(), Integer.toString(rc.getMinSupervisorsNeeded())});


                if( !refBean.getAdminOverride() )
                    sendUnsentRcRaters( rc );
            }

            if( !refBean.getAdminOverride() )
                updateRcCheckAndCandidateStatusAndSendProgressMsgs( rc );

            CookieUtils.removeRcCheckCookie( getHttpServletResponse() );

            refBean.setRefPageType( RefPageType.CORE3 );
            RefPageType rpt = getNextPageTypeForRefProcess();
            refBean.setRefPageType(rpt);
            // LogService.logIt( "CandidateRefUtils.processExitAllCore() next view=" + getViewFromPageType( refBean.getRefPageType() ) );
            return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
        }
        catch( STException e )
        {
            setMessage( e );
            return "StayInSamePlace"; // new systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processExitAllCore() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }




    public String processCreateNewRcRater()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        RcRater rcRater = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
            {
                String msg = "CandidateRefUtils.processCreateNewRcRater() RcCheckId in request does not match. Value in request=" + rcChkReq + ", rcBean.rcCheck.rcCheckId=" + rc.getRcCheckId();
                LogService.logIt( msg );
                return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), msg , null, null, rc, rc==null ? null : rc.getRcRater(), true );
            }

            RefPageType refPageType = refBean.getRefPageType();
            if( !refPageType.getIsCore3() )
                return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );


            //if( rcFacade==null )
           //     rcFacade = RcFacade.getInstance();
            //RcOrgPrefs rcop = rcFacade.getRcOrgPrefsForOrgId( rc.getOrgId() );

            String candRoleResp = candidateRefBean.getStrParam4();

            if( rc.getRcCheckType().getIsPrehire() && ( candRoleResp==null || candRoleResp.isBlank() ) )
            {
                for( RcRater rcr : rc.getRcRaterListCandidate() )
                {
                    if(  rcr.getCandidateRoleResp()!=null && !rcr.getCandidateRoleResp().isBlank() )
                        candRoleResp = rcr.getCandidateRoleResp();
                }
            }

            if( candidateRefBean.getLastObsEndDate()==null )
            {
                Calendar cal = new GregorianCalendar();
                cal.set( Calendar.DAY_OF_MONTH, 15);
                candidateRefBean.setLastObsEndDate( cal.getTime() );
            }


            // candidateRefBean.clearBean();

            // Create a new user
            User ru = new User();
            ru.setCountryCode( rc.getUser().getCountryCode() );

            rcRater = candidateRefBean.getRcRater();

            if( rcRater!=null && rcRater.getRcRaterId()>0 )
            {
                rcRater=partlyCloneRcRaterForNewReference(rcRater);
                candidateRefBean.setRcRater(rcRater);
            }

            if( rcRater==null )
            {
                // create a new rater
                rcRater = new RcRater();
                rcRater.setRcCheckId( rc.getRcCheckId() );
                rcRater.setOrgId( rc.getOrgId() );
                rcRater.setLocale(getLocale() );
                rcRater.setCandidateRoleResp(candRoleResp);
                rcRater.setSourceUserId( rc.getUserId() );
                rcRater.setRcRaterStatusTypeId( RcRaterStatusType.CREATED.getRcRaterStatusTypeId() );
                rcRater.setContactMethodTypeId( RcContactMethodType.BOTH.getRcContactMethodTypeId() );
                // rcRater.setRcRaterSourceType( RcRaterSourceType.CANDIDATE );
                rcRater.setRcRaterSourceTypeId( RcRaterSourceType.CANDIDATE.getRcRaterSourceTypeId() );

                rcRater.setRcRaterRoleTypeId( RcRaterRoleType.PEER.getRcRaterRoleTypeId() );
                rcRater.setRcRaterTypeId( RcRaterType.RATER.getRcRaterTypeId() );
                rcRater.setTempEmail(null);
                rcRater.setTempMobile(null);
                rcRater.setNeedsResendEmail(false);
                rcRater.setNeedsResendMobile(false);
                rcRater.setObservationStartDate( candidateRefBean.getLastObsStartDate());
                rcRater.setObservationEndDate( candidateRefBean.getLastObsEndDate() );
            }

            // place cleared rater and user in rcref bean.
            rcRater.setUser( ru );
            candidateRefBean.setRcRater(rcRater);

            // OK we can edit.
            return "/ref/reference.xhtml";
        }
        catch( STException e )
        {
            setMessage(e);
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processCreateNewRcRater() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ) );
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public String processSendToRater()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        RcRater rcRater = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true);
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
                throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );

            RefPageType refPageType = refBean.getRefPageType();
            if( !refPageType.getIsCore3() )
                return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true);

            rcRater = candidateRefBean.getRcRater2();
            if( rcRater==null )
                throw new Exception( "RcRater is null!" );

            if( !rcRater.getCandidateCanSend() )
                throw new STException( rcRater.getRcRaterStatusType().getCompleteOrHigher() ? "g.XCCannotSendToRater.complete" : "g.XCCannotSendToRater", new String[]{rcRater.getUser().getFullname()} );

            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            RcRater r2 = rcFacade.getRcRater( rcRater.getRcRaterId(), true );
            if( r2!=null && r2.getRcRaterStatusType().getCompleteOrHigher() )
            {
                rc.setRcRaterList(null);
                if( rcCheckUtils==null )
                    rcCheckUtils=new RcCheckUtils();
                rcCheckUtils.loadRcCheckForAdmin(rc, refBean.getRefUserType(), getLocale(), refBean.getAdminOverride() );
                throw new STException( "g.XCCannotSendToRater.complete", new String[]{rcRater.getUser().getFullname()} );
            }

            boolean reminder = rcRater.getSendDate()!=null && rcRater.getRcRaterStatusType().getSentOrHigher();
            int[] out = this.refBean.getAdminOverride() ? new int[2] : sendRcCheckToRater(  rc, rcRater, false, reminder, true); //    sendToRater( rc, rcRater, true, true );
            LogService.logIt( "CandidateRefUtils.processSendToRater() emails sent=" + out[0] + ", text messages sent=" + out[1] + ", rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ) + " reminder=" + reminder );

            // OK we can edit.
            return "StayInSamePlace";
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processSendToRater() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ) );
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public String processEditRater()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        RcRater rcRater = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
                throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );

            RefPageType refPageType = refBean.getRefPageType();
            if( !refPageType.getIsCore3() )
                return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );

            rcRater = candidateRefBean.getRcRater();
            if( rcRater==null )
                throw new Exception( "RcRater is null!" );

            if( rcRater.getUser()==null )
                throw new Exception( "RcRater.user is null" );

            LogService.logIt( "CandidateRefUtils.processEditRater() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ));

            if( rcRater.getRcRaterStatusType().getStartedOrHigher() )
                throw new STException( "g.XCCannotEditRater", new String[]{rcRater.getUser().getFullname()} );
            if( rcRater.getRcRaterSourceType()!=null && rcRater.getRcRaterSourceType().getIsAccountUser() )
                throw new STException( "g.XCCannotEditRaterAcctSource", new String[]{rcRater.getUser().getFullname()} );

            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            RcRater r2 = rcFacade.getRcRater( rcRater.getRcRaterId(), true );
            if( r2!=null && refBean.getAdminOverride() )
                r2 = (RcRater) r2.clone();

            if( r2!=null && r2.getRcRaterStatusType().getStartedOrHigher() )
            {
                rc.setRcRaterList(null);
                if( rcCheckUtils==null )
                    rcCheckUtils=new RcCheckUtils();
                rcCheckUtils.loadRcCheckForAdmin(rc, refBean.getRefUserType(), getLocale(), refBean.getAdminOverride() );
                throw new STException( "g.XCCannotEditRater", new String[]{rcRater.getUser().getFullname()} );
            }

            rcRater.setTempEmail( rcRater.getUser().getEmail() );

            if( rcRater.getUser().getHasMobilePhone() )
            {
                String mp =  GooglePhoneUtils.getFormattedPhoneNumberIntl( rcRater.getUser().getMobilePhone(), rcRater.getUser().getCountryCode() );
                //LogService.logIt( "CandidateRefUtils.processEditRater() EEE.1 user.mobile=" + rcRater.getUser().getMobilePhone() + ", mp=" + mp );
                rcRater.getUser().setMobilePhone( mp );
               //LogService.logIt( "CandidateRefUtils.processEditRater() EEE.2 user.mobile=" + rcRater.getUser().getMobilePhone() );
            }
            rcRater.setTempMobile( rcRater.getUser().getHasMobilePhone() ? rcRater.getUser().getMobilePhone() : null );

            //LogService.logIt( "CandidateRefUtils.processEditRater() user.mobile=" + rcRater.getUser().getMobilePhone() + ", tempMobile=" + rcRater.getTempMobile() );

            rcRater.setNeedsResendEmail(false);
            rcRater.setNeedsResendMobile(false);

            // OK we can edit.
            return "/ref/reference.xhtml";
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processEditRater() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ) );
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public String processRaterGoBack()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        RcRater rcRater = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
                throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );


            RefPageType refPageType = refBean.getRefPageType();
            if( !refPageType.getIsCore3() )
                return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );

            rcRater = candidateRefBean.getRcRater();
            //if( rcRater!=null && rcRater.getRcRaterId()>0 && rcRater.getCandidateCanSend() && !rcRater.getRcRaterStatusType().getSentOrHigher() )
            //    processSendToRater();

            return "/ref/references.xhtml";
        }
        catch( STException e )
        {
            setMessage(e);
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processRaterGoBack() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ) );
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    private RcRater partlyCloneRcRaterForNewReference( RcRater rcRater )
    {
        if( rcRater==null )
            return null;

        RcRater rr = new RcRater();
        rr.setRcCheck(rcRater.getRcCheck());
        rr.setObservationStartDate( rcRater.getObservationStartDate());
        rr.setObservationEndDate( rcRater.getObservationEndDate());
        rr.setLocale( rcRater.getLocale());
        rr.setOrgId( rcRater.getOrgId());
        rr.setRcCheckId(rcRater.getRcCheckId());
        rr.setRcRaterRoleTypeId(rcRater.getRcRaterRoleTypeId());
        rr.setRcRaterSourceTypeId(rr.getRcRaterSourceTypeId());
        rr.setCompanyName(rcRater.getCompanyName());
        rr.setCandidateRoleResp( rcRater.getCandidateRoleResp() );
        rr.setRcRaterStatusTypeId( RcRaterStatusType.CREATED.getRcRaterStatusTypeId() );
        rr.setContactMethodTypeId( RcContactMethodType.BOTH.getRcContactMethodTypeId() );
        rr.setRcRaterSourceTypeId( RcRaterSourceType.CANDIDATE.getRcRaterSourceTypeId() );
        rr.setRcRaterTypeId( RcRaterType.RATER.getRcRaterTypeId() );
        rr.setTempEmail(null);
        rr.setTempMobile(null);
        rr.setNeedsResendEmail(false);
        rr.setNeedsResendMobile(false);

        rr.setUser(new User());
        return rr;
    }

    public String processSaveRater()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        RcRater rcRater = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
                throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );


            RefPageType refPageType = refBean.getRefPageType();
            if( !refPageType.getIsCore3() )
                return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true);

            rcRater = candidateRefBean.getRcRater();
            if( rcRater==null )
                throw new Exception( "RcRater is null!" );

            long rcRtrReq = getRcRaterIdFmRequest();
            if( rcRtrReq!=rcRater.getRcRaterId() )
            {
                LogService.logIt( "RcRaterId in request does not match. Value in request=" + rcRtrReq );
                return processViewRaters();
                // throw new Exception( "RcRaterId in request does not match. Value in request=" + rcRtrReq );
            }

            if( rcRater.getRcRaterRoleType().equals( RcRaterRoleType.UNKNOWN ) )
                throw new STException( rc.getRcCheckType().getIsPrehire() ? "g.XCErrSelectRoleType" : "g.XCErrSelectRoleType.reviewer" );

            if(  rc.getRcCheckType().getIsPrehire() && (rcRater.getCandidateRoleResp()==null || rcRater.getCandidateRoleResp().isBlank()) )
                throw new STException( "g.XCRoleResp4RaterReqd" );

            User user = rcRater.getUser();
            if( user==null )
                throw new Exception( "RcRater.user is null." );

            if( !user.getHasNameEmail() )
                throw new STException( "g.XCErrFullNameEmailReqd" );

            if( user.getFirstName().equalsIgnoreCase( rc.getUser().getFirstName()) && user.getLastName().equalsIgnoreCase( rc.getUser().getLastName()) )
                throw new STException( "g.XCErrSameEmailOrPhoneAsCand" );

            if( user.getEmail().equalsIgnoreCase( rc.getUser().getEmail() ) )
                throw new STException( "g.XCErrSameEmailOrPhoneAsCand" );

            String mobile = user.getMobilePhone();
            boolean mobileValid = mobile==null || mobile.isBlank() ? false : GooglePhoneUtils.isNumberValid( mobile, user.getCountryCode() );
            if( !EmailUtils.validateEmailNoErrors( user.getEmail() ) && !mobileValid )
                throw new STException( "g.XCErrValidEmailOrPhoneRequired" );

            if( mobile!=null && !mobile.isBlank() && !mobileValid )
                setErrorMessage( "g.XCErrPhoneNumberInvalidIgnored", new String[] {mobile} );

            if( mobile!=null && !mobile.isBlank() && rc.getUser().getMobilePhone()!=null && !rc.getUser().getMobilePhone().isBlank() )
            {
                boolean mtch = mobile.equalsIgnoreCase( rc.getUser().getMobilePhone() );
                if( !mtch  )
                    mtch = (GooglePhoneUtils.getFormattedPhoneNumberIntl(mobile, user.getCountryCode() )).equalsIgnoreCase( rc.getUser().getMobilePhone() );
                if( mtch )
                    throw new STException( "g.XCErrSameEmailOrPhoneAsCand" );
            }

            if( rcRater.getObservationStartDate()==null || rcRater.getObservationEndDate()==null )
                throw new STException( "g.RCObservationPeriodFullRqd" );

            if( !RcCheckUtils.isObsDateValid(rcRater.getObservationStartDate()) || !RcCheckUtils.isObsDateValid(rcRater.getObservationEndDate()) )
                throw new STException( "g.RCObservationPeriodInvalid" );

            if( rcRater.getObservationStartDate().after( rcRater.getObservationEndDate()) )
                throw new STException( "g.RCObservationPeriodInverted" );

            if( !EmailUtils.validateEmailNoErrors(user.getEmail()) )
            {
                String revEmail = user.getFirstName() + "-" + user.getLastName() + "-" + StringUtils.generateRandomString(8);
                revEmail = StringUtils.alphaDigitDashCharsOnly(revEmail);
                user.setEmail(revEmail);
            }

            user.setOrgId( rc.getOrgId() );

            if( userFacade==null )
                userFacade = UserFacade.getInstance();
            User u2 = userFacade.getUserByEmailAndOrgId( user.getEmail(), rc.getOrgId() );

            if( u2!=null )
            {
                // First, see if there is already a Rater for this user.
                RcRater r2 = null;
                for( RcRater rx : rc.getRcRaterList() )
                {
                    if( rx.getUserId()==u2.getUserId() )
                    {
                        r2=rx;
                        break;
                    }
                }

                // Found a different rater.
                if( r2!=null && r2.getRcRaterId()!=rcRater.getRcRaterId() )
                {
                    // remove duplicate.
                    if( !refBean.getAdminOverride() && rcRater.getRcRaterId()>0 && !rcRater.getRcRaterStatusType().getSentOrHigher() )
                    {
                        rc.getRcRaterList().remove(rcRater);
                        // delete it.
                        if( rcFacade==null )
                            rcFacade=RcFacade.getInstance();
                        rcFacade.deleteRcRater( rcRater.getRcRaterId() );
                    }
                    else if( rcRater.getRcRaterId()>0  )
                        LogService.logIt( "CandidateRefUtils.processSaveRater() CCC.0 created new Rater rcRaterId=" + rcRater.getRcRaterId() + " but found an existing Rater rcRaterId=" + r2.getRcRaterId() + " for the same email. However, the other one has been sent so cannot delete it.");

                    candidateRefBean.setRcRater(this.partlyCloneRcRaterForNewReference(rcRater));

                    if( r2.getRcRaterSourceType().getIsCandidateOrEmployee() )
                        setErrorMessage("g.XCFoundUserAndExistingRaterX", new String[] { getRcCheckRaterName(), getRcCheckTypeName(), u2.getFullname(), u2.getEmail()} );
                    else
                        setErrorMessage( "g.XCFoundUserAndExistingRaterFmAdminX", new String[] { getRcCheckRaterName(), getRcCheckTypeName(), u2.getFullname(), u2.getEmail(), rc.getOrg().getName(), this.getRcCheckRatersName()} );
                    return "/ref/references.xhtml";
                }

                // found existing user. Use it.
                if( user.getUserId()<=0 )
                {
                    setInfoMessage( "g.XCFoundUserEmailUsingItNoNameChange", new String[] {u2.getFullname(), u2.getEmail()}  );
                    LogService.logIt( "CandidateRefUtils.processSaveRater() CCC.1 create new Rater. Found existing userId=" + u2.getUserId() + ", rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ));
                    user = u2;
                    rcRater.setUserId( user.getUserId() );
                    rcRater.setUser(user);
                }

                // found existing user with same email address but not same as this user.
                if( user.getUserId()>0 && user.getUserId()!=u2.getUserId() )
                {
                    setInfoMessage( "g.XCFoundUserEmailUsingItNoNameChange", new String[] {u2.getFullname(), u2.getEmail()}  );
                    LogService.logIt( "CandidateRefUtils.processSaveRater() CCC.2 Found existing but DIFFERENT user with same email.  Shifting to that user. userId=" + u2.getUserId() + ", rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ));
                    user = u2;
                    rcRater.setUserId( user.getUserId() );
                    rcRater.setUser(user);
                }
            }

            // at this point, we have valid info.
            boolean create = rcRater.getRcRaterId()<=0;
            boolean send = false; // candidateRefBean.isBooleanParam1();
            boolean exit = booleanParam1;


            // need to finish the new user.
            if( user.getUserId()<=0 )
            {
                user.setOrgId( rc.getOrgId() );
                user.setSuborgId( rc.getSuborgId() );
                user.setRoleId( RoleType.NO_LOGON.getRoleTypeId() );
                user.setUserTypeId( UserType.NAMED.getUserTypeId() );
                user.setCountryCode( userBean.getUser().getCountryCode() );
                user.setCreateDate( new Date() );
                user.setLocaleStr( rc.getLangCode());
                user.setTimeZoneId( userBean.getUser().getTimeZoneId() );
                user.setPassword(StringUtils.generateRandomString(12 ));
                user.setUsername(StringUtils.generateRandomString(30 ));
                int count = 0;
                while( count<100 && userFacade.getUserByUsername( user.getUsername())!=null )
                {
                    user.setUsername( StringUtils.generateRandomString(30 ));
                    count++;
                }
            }

            if( user.getHasMobilePhone() )
                user.setMobilePhone( GooglePhoneUtils.getFormattedPhoneNumberIntl(user.getMobilePhone(), user.getCountryCode() ));

            if( !refBean.getAdminOverride() )
                user = userFacade.saveUser(user, false);

            rcRater.setUserId( user.getUserId() );
            rcRater.setUser(user);

            if( create )
            {
                send = exit;
                rcRater.setRcCheckId( rc.getRcCheckId() );
                rcRater.setLocale( getLocale() );
                rcRater.setSourceUserId( rc.getUserId() );
                rcRater.setRcRaterStatusTypeId( RcRaterStatusType.CREATED.getRcRaterStatusTypeId() );
                rcRater.setContactMethodTypeId( RcContactMethodType.BOTH.getRcContactMethodTypeId() );
                // rcRater.setRcRaterSourceType( RcRaterSourceType.CANDIDATE );
                rcRater.setRcRaterSourceTypeId( RcRaterSourceType.CANDIDATE.getRcRaterSourceTypeId() );
                rcRater.setNeedsResendEmail( EmailUtils.validateEmailNoErrors( user.getEmail() ));
                rcRater.setNeedsResendMobile( user.getHasMobilePhone() );

                if( !refBean.getAdminOverride() )
                    Tracker.addRaterCreation();
                // rcRater.setRcRaterRoleTypeId( RcRaterRoleType.UNKNOWN.getRcRaterRoleTypeId() );
            }

            // this is an existing RcRater. Check for changes to email or mobile.
            else
            {
                if( rcRater.getTempEmail()!=null && !rcRater.getTempEmail().isBlank() && !rcRater.getTempEmail().equalsIgnoreCase( user.getEmail() ) && EmailUtils.validateEmailNoErrors( user.getEmail() ))
                    rcRater.setNeedsResendEmail(true);
                if( rcRater.getTempMobile()!=null && !rcRater.getTempMobile().isBlank() && user.getHasMobilePhone() && !rcRater.getTempMobile().equalsIgnoreCase( user.getMobilePhone()) && GooglePhoneUtils.isNumberValid(user.getMobilePhone(), user.getCountryCode()))
                    rcRater.setNeedsResendMobile(true);
                else if( rcRater.getTempMobile()==null  && user.getHasMobilePhone() && GooglePhoneUtils.isNumberValid(user.getMobilePhone(), user.getCountryCode()) )
                    rcRater.setNeedsResendMobile(true);
                if( !refBean.getAdminOverride() && exit )
                {
                    // if exiting, send if need to only.
                    int[] out = sendRcCheckToRater(rc, rcRater, true, false, true);
                    LogService.logIt( "CandidateRefUtils.processSaveRater() CCC.1 sending because not create and exit is true emails sent=" + out[0] + ", text messages sent=" + out[1] + ", rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ) );
                }
                else if( refBean.getAdminOverride() && exit )
                    this.setStringInfoMessage( "Did not send to Rater because of AdminOverride." );
            }

            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();

            if( !refBean.getAdminOverride() )
            {
                // this could return a new copy of this rater if it already exists.
                rcRater = rcFacade.saveRcRater( rcRater , false);

                // replace if existing
                if( create )
                {
                    ListIterator<RcRater> iter = rc.getRcRaterList().listIterator();
                    while( iter.hasNext() )
                    {
                        if( iter.next().getRcRaterId()==rcRater.getRcRaterId() )
                        {
                            iter.remove();
                            iter.add(rcRater);
                            break;
                        }
                    }
                    candidateRefBean.setRcRater(rcRater);
                }
            }

            if( create )
            {
                if( rc.getFirstCandidateReferenceDate()==null )
                    rc.setFirstCandidateReferenceDate( new Date() );
                rc.setLastCandidateReferenceDate( new Date() );
                if( !refBean.getAdminOverride() )
                    rcFacade.saveRcCheck(rc, false );
            }

            if( rc.getRcCheckType().getIsPrehire() && rcRater.getCandidateRoleResp()!=null && !rcRater.getCandidateRoleResp().isBlank()  )
                candidateRefBean.setStrParam4( rcRater.getCandidateRoleResp() );

            // LogService.logIt( "CandidateRefUtils.processSaveRater() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ));

            if( !refBean.getAdminOverride() && send && rcRater.getRaterNoSend()!=1 )
            {
                int[] out = sendRcCheckToRater(rc, rcRater, false, false, true);
                // LogService.logIt( "CandidateRefUtils.processSaveRater() DDD.1 emails sent=" + out[0] + ", text messages sent=" + out[1] + ", rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ) );
            }

            else if( refBean.getAdminOverride() && send && rcRater.getRaterNoSend()==1 )
                this.setStringInfoMessage( "Did not send to Rater because of AdminOverride and/or designated No Send." );

            else if( refBean.getAdminOverride() && send )
                this.setStringInfoMessage( "Did not send to Rater because of AdminOverride." );

            if( send && rcRater.getRaterNoSend()==1 )
                setInfoMessage( "g.XCNoSendNotSent", new String[]{rcRater.getUser().getFullname()} );


            if( exit )
            {
                // Must come after sending.
                rc.setRcRaterList(null);
                if( rcCheckUtils==null )
                    rcCheckUtils=new RcCheckUtils();
                rcCheckUtils.loadRcCheckForAdmin(rc, refBean.getRefUserType(), getLocale(), refBean.getAdminOverride() );

                if( rc.getNeedsSupervisors() )
                    this.setInfoMessage( "g.XCAddReferences.belowminsups", new String[]{ getRcCheckRaterNameLc(), getRcCheckRatersNameLc(),Integer.toString(rc.getRcRaterListCandidate().size()),null,null,null,null,Integer.toString(rc.getRcRaterListCandidateSupers().size()), Integer.toString(rc.getMinSupervisors())} );

                RcRater rr = partlyCloneRcRaterForNewReference(  rcRater );
                candidateRefBean.setRcRater(rr);

                return conditionUrlForSessionLossGet("/ref/references.xhtml", true);
            }

            return "stayInSamePlace";
        }
        catch( STException e )
        {
            setMessage(e);
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processSaveRater() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ) );
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    public boolean getCanExitReferences()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        if( rc==null || rc.getRcRaterListCandidate().size()<rc.getMinRatersCandidate() || rc.getRcRaterListCandidateSupers().size()<rc.getMinSupervisorsCandidate() )
            return false;
        return true;
    }

    public List<SelectItem> getCountryCodeSelectItemList()
    {
        return CountryCodeLister.getInstance().getCountrySelectItemList( getLocale() );
    }


    public String processCancelRater()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = null;
        RcRater rcRater = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
                return CorpUtils.getInstance().processCorpHome();

            long rcChkReq = this.getRcCheckIdFmRequest();
            if( rcChkReq!=rc.getRcCheckId() )
                throw new Exception( "RcCheckId in request does not match. Value in request=" + rcChkReq );


            rcRater = candidateRefBean.getRcRater();
            if( rcRater==null )
                throw new Exception( "RcRater is null!" );

            LogService.logIt( "CandidateRefUtils.processCancelRater() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ));

            if( !rcRater.getCandidateCanCancel() )
                throw new STException( rcRater.getRcRaterStatusType().getSentOrHigher() ? "g.XCCannotCancelToRater.sent" : "g.XCCannotCancelToRater", new String[]{rcRater.getUser().getFullname()} );

            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            RcRater r2 = rcFacade.getRcRater( rcRater.getRcRaterId(), true );
            if( r2!=null && r2.getRcRaterStatusType().getSentOrHigher() )
            {
                rc.setRcRaterList(null);
                if( rcCheckUtils==null )
                    rcCheckUtils=new RcCheckUtils();
                rcCheckUtils.loadRcCheckForAdmin(rc, refBean.getRefUserType(), getLocale(), refBean.getAdminOverride() );
                throw new STException( "g.XCCannotCancelToRater.sent", new String[]{rcRater.getUser().getFullname()} );
            }

            if( refBean.getAdminOverride() )
                throw new Exception( "You cannot Cancel raters in Admin Administration Mode." );

            if( !refBean.getAdminOverride() )
                rcFacade.deleteRcRater( rcRater.getRcRaterId() );
            rc.setRcRaterList(null);
            if( rcCheckUtils==null )
                rcCheckUtils=new RcCheckUtils();
            rcCheckUtils.loadRcCheckForAdmin(rc, refBean.getRefUserType(), getLocale(), refBean.getAdminOverride() );
            return "StayInSamePlace";
        }
        catch( STException e )
        {
            setMessage(e);
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CandidateRefUtils.processCancelRater() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + ( rcRater==null ? "null" : rcRater.getRcRaterId() ) );
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }





    /*
     data[0] = email sent count
     data[1] = text sent count

    */
    protected int[] sendRcCheckToRater( RcCheck rc, RcRater rater, boolean sendIfNeedsOnly, boolean reminder, boolean setWebsiteMessages) throws Exception
    {
        User user = null;
        if( rc==null )
            throw new Exception( "rcCheck is null" );
        if( rater==null )
            throw new Exception( "rater is null");
        if( userFacade == null )
            userFacade = UserFacade.getInstance();
        user = rater.getUser();

        if( user==null )
        {
            user = userFacade.getUser( rater.getUserId());
            rater.setUser(user);
        }

        if( user==null )
            throw new Exception( "CandidateRefUtils.sendRcCheckToRater() user is null. rcCheckId=" + rc.getRcCheckId() );

        if( rc.getAdminUser()==null )
            rc.setAdminUser( userFacade.getUser( rc.getAdminUserId() ) );
        if( rc.getLocale()==null && rc.getLangCode()!=null )
            rc.setLocale( I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() ));
        if( rc.getLocale()==null )
            rc.setLocale( getLocale() );

        if( rc.getRcOrgPrefs()==null )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rc.setRcOrgPrefs( rcFacade.getRcOrgPrefsForOrgId( rc.getOrgId() ));
        }

        if( rc.getSuborgId()>0 && rc.getRcSuborgPrefs()==null )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rc.setRcSuborgPrefs( rcFacade.getRcSuborgPrefsForSuborgId( rc.getSuborgId() ));
        }


        RcMessageUtils rcmu = new RcMessageUtils();
        int[] sent = rcmu.sendRcCheckToRater(rc, rater, rc.getRcOrgPrefs(), 1, rc.getUserId(), sendIfNeedsOnly, reminder);

        if( user.getHasMobilePhone() )
        {
            if( sent[1]>0 && setWebsiteMessages )
                this.setInfoMessage( reminder ? "g.RCReminderTextSent" : "g.RCTextSent" , new String[]{user.getFullname(), user.getMobilePhone()} );
            else if( sent[1]==0 && setWebsiteMessages )
                this.setErrorMessage( reminder ? "g.RCReminderTextNotSentError" : "g.RCTextNotSentError" , new String[]{user.getFullname(), user.getMobilePhone()} );
            else if( sent[1]==-1 && setWebsiteMessages )
                this.setErrorMessage( reminder ? "g.RCReminderTextNotSentErrorInvalid" : "g.RCTextNotSentErrorInvalid" , new String[]{user.getFullname(), user.getMobilePhone()} );
            else if( sent[1]<-1 && setWebsiteMessages )
                this.setErrorMessage( reminder ? "g.RCReminderTextNotSentErrorBlock" : "g.RCTextNotSentErrorBlock" , new String[]{user.getFullname(), user.getMobilePhone()} );
        }

        if( user.getEmail()!=null && !user.getEmail().isBlank() && sent[0]>0 )
            setInfoMessage( reminder ? "g.RCReminderEmailSent" : "g.RCEmailSent" , new String[]{user.getFullname(), user.getEmail()} );

        return sent;
    }

}
