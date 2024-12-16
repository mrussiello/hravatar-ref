/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.corp.CorpBean;
import com.tm2ref.corp.CorpUtils;
import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcItem;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.ref.RcRating;
import com.tm2ref.entity.ref.RcReferral;
import com.tm2ref.entity.ref.RcScript;
import com.tm2ref.entity.user.User;
import com.tm2ref.file.BucketType;
import com.tm2ref.file.ConversionStatusType;
import com.tm2ref.file.FileContentType;
import com.tm2ref.file.FileUploadFacade;
import com.tm2ref.file.FileXferUtils;
import com.tm2ref.file.UploadedFileProcessingType;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.global.Constants;
import com.tm2ref.global.NumberUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.RoleType;
import com.tm2ref.user.UserFacade;
import com.tm2ref.user.UserType;
import com.tm2ref.util.GooglePhoneUtils;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.InputStream;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author miker_000
 */

@Named
@RequestScoped
public class RaterRefUtils extends BaseRefUtils
{
    UploadedFile uploadedFile;

    @Inject
    private RaterRefBean raterRefBean;

    public static RaterRefUtils getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return (RaterRefUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "raterRefUtils" );
    }

    public int getRaterQuestionNumber()
    {
        return raterRefBean.getItemDisplayOrder()<9999 ? raterRefBean.getItemDisplayOrder() : 0;
    }

    public int getRaterQuestionCount()
    {
        getRefBean();
        RcScript sc = refBean.getRcCheck().getRcScript();
        return sc.getItemCount( refBean.getRcCheck().getRcRater().getIsCandidateOrEmployee() );
    }

    public String getRcRaterIdEncrypted()
    {
        getRefBean();
        if( refBean.getRcCheck()==null || refBean.getRcCheck().getRcRater()==null )
            return null;
        return refBean.getRcCheck().getRcRater().getRcRaterIdEncrypted();
    }

    public String getFormEncType()
    {
        getRefBean();

        if( refBean.getRefUserType().getIsCandidate() && raterRefBean.getRcItem()!=null && raterRefBean.getRcItem().getHasCandidateFileUpload())
            return "multipart/form-data";

        return "application/x-www-form-urlencoded";
    }

    
    public String getCandidateResponseValueXhtml()
    {        
        if( raterRefBean.getRcItemWrapper()==null )
            return "";
        
        return raterRefBean.getRcItemWrapper().getCandidateResponseValueXhtml(getLocale());
    }



    public String getCommentsPlaceholderStr()
    {
        getRefBean();

        if( refBean.getAudioVideoCommentsOk() && refBean.getRcCheck().getRcAvType().getAnyMedia() && raterRefBean.getRcItemWrapper()!=null && raterRefBean.getRcItemWrapper().getRcRating()!=null && raterRefBean.getRcItemWrapper().getRcRating().getRcUploadedUserFile()!=null )
            return MessageFactory.getStringMessage( getLocale(), "g.XRCommentsPlacehldr.RecordingUploaded" );

        if( raterRefBean.getRcItemWrapper()!=null && raterRefBean.getRcItemWrapper().getRcItem()!=null && raterRefBean.getRcItemWrapper().getRcItem().getCommentsPlaceholder()!=null && !raterRefBean.getRcItemWrapper().getRcItem().getCommentsPlaceholder().isBlank() )
            return raterRefBean.getRcItemWrapper().getRcItem().getCommentsPlaceholder();

        if( refBean.getAudioVideoCommentsOk() && refBean.getRcCheck().getRcAvType().getAnyMedia() && raterRefBean.getRcItemWrapper()!=null && raterRefBean.getRcItemWrapper().getRcRating()!=null && raterRefBean.getRcItemWrapper().getRcRating().getRcUploadedUserFile()==null )
            return MessageFactory.getStringMessage( getLocale(), "g.XRCommentsPlacehldr.RecordingOptional" );

        return MessageFactory.getStringMessage( getLocale(), "g.XRCommentsPlacehldr" );
    }

    public String getItemSkipButtonText()
    {
        if( raterRefBean.getRcItemWrapper()!=null && raterRefBean.getRcItemWrapper().getRcItem()!=null && raterRefBean.getRcItemWrapper().getRcItem().getSkipButtonText()!=null && !raterRefBean.getRcItemWrapper().getRcItem().getSkipButtonText().isBlank() )
            return raterRefBean.getRcItemWrapper().getRcItem().getSkipButtonText();

        return MessageFactory.getStringMessage( getLocale(), "g.XRSkipButn" );
    }


    public boolean getIsCurrentItemCommentRequiredAnyScore()
    {
        getRefBean();

        if( refBean.getRcCheck()!=null && refBean.getRcCheck().getRcScript().getAllCommentsRequiredB() )
            return true;

        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null || !raterRefBean.getRcItemWrapper().getRcItem().getRcItemFormatType().getCanHaveComments() || raterRefBean.getRcItemWrapper().getRcItem().getIncludeComments()<=1 )
            return false;

        if( refBean.getRcCheck().getRcScript().getNoCommentsRatingItemsB() && raterRefBean.getRcItemWrapper().getRcItem().getRcItemFormatType().getIsRating() )
            return false;

        if( raterRefBean.getRcItemWrapper().getRcItem().getIncludeComments()==RcItemCommentsRequiredType.REQUIRED.getRcItemCommentsRequiredTypeId() )
            return true;

        RcItemCommentsRequiredType rt = RcItemCommentsRequiredType.getValue( raterRefBean.getRcItemWrapper().getRcItem().getIncludeComments() );

        if( rt.getAreCommentsRequired( refBean.getRefUserType() ) )
            return true;

        /*
        // Has Thresholds
        if( refBean.getRcCheck().getRcScript()!=null &&
            raterRefBean.getRcItemWrapper().getRcRating()!=null &&
            raterRefBean.getRcItemWrapper().getRcRating().getIsCompleteOrHigher() &&
            raterRefBean.getRcItemWrapper().getRcRating().getHasNumericScore() &&
            (raterRefBean.getRcItem().getCommentThresholdLow()>refBean.getRcCheck().getRcScript().getRcRatingScaleType().getMinScore() || raterRefBean.getRcItem().getCommentThresholdHigh()<refBean.getRcCheck().getRcScript().getRcRatingScaleType().getMaxScore() )
            )
        {
            // has low threshold and the score is below or equal to threshold
            if( raterRefBean.getRcItem().getCommentThresholdLow()>refBean.getRcCheck().getRcScript().getRcRatingScaleType().getMinScore() && raterRefBean.getRcItemWrapper().getRcRating().getScore()<=raterRefBean.getRcItem().getCommentThresholdLow() )
                return true;

            // has high threshold and the score is above or equal to threshold
            if( raterRefBean.getRcItem().getCommentThresholdHigh()<refBean.getRcCheck().getRcScript().getRcRatingScaleType().getMaxScore() && raterRefBean.getRcItemWrapper().getRcRating().getScore()>=raterRefBean.getRcItem().getCommentThresholdHigh() )
                return true;
        }
        */

        return false;
    }

    public boolean getIsCurrentItemCommentRequiredForLowScore( float score )
    {
        return getIsCurrentItemCommentRequiredForScore( false, score );
    }

    public boolean getIsCurrentItemCommentRequiredForHighScore( float score )
    {
        return getIsCurrentItemCommentRequiredForScore( true, score );
    }


    public boolean getIsCurrentItemCommentRequiredForScore( boolean high, float score )
    {
        if( getIsCurrentItemCommentRequiredAnyScore() )
            return true;

        getRefBean();

        if( refBean.getRcCheck().getRcScript().getNoCommentsRatingItemsB() && raterRefBean.getRcItemWrapper().getRcItem().getRcItemFormatType().getIsRating() )
            return false;

        // Has Thresholds
        if( raterRefBean.getRcItem()!=null && refBean.getRcCheck()!=null && refBean.getRcCheck().getRcScript()!=null )
        {
            // LogService.logIt( "RaterRefUtils.getIsCurrentItemCommentRequiredForScore() high=" + high + ", score=" + score + ", raterRefBean.getRcItem().getCommentThresholdLow()=" + raterRefBean.getRcItem().getCommentThresholdLow() +", raterRefBean.getRcItem().getCommentThresholdHigh()=" + raterRefBean.getRcItem().getCommentThresholdHigh() );

            // has low threshold and the score is below or equal to threshold
            if( !high && raterRefBean.getRcItem().getCommentThresholdLow()>refBean.getRcCheck().getRcScript().getRcRatingScaleType().getMinScore() && score<=raterRefBean.getRcItem().getCommentThresholdLow() )
                return true;

            // has high threshold and the score is above or equal to threshold
            if( high && raterRefBean.getRcItem().getCommentThresholdHigh()<refBean.getRcCheck().getRcScript().getRcRatingScaleType().getMaxScore() && score>=raterRefBean.getRcItem().getCommentThresholdHigh() )
                return true;
        }

        return false;
    }



    public void doEnterCore2() throws Exception
    {
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                throw new Exception( "RcCheck is null in RcBean." );
            if( rc.getRcScript()==null )
                throw new Exception( "RcScript is null in RcBean.rcCheck" );
            if( rc.getRcRater()==null )
                throw new Exception( "RcRater is null in RcBean.rcCheck" );

            if( !getNeedsCore2() )
                return;

            if( rc.getRcRater().getRcReferralList()==null )
            {
                if( this.rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rc.getRcRater().setRcReferralList( rcFacade.getRcReferralList(rc.getRcCheckId(), rc.getRcRater().getRcRaterId()));
            }

            for( RcReferral r : rc.getRcRater().getRcReferralList() )
            {
                if( r.getUser()==null )
                {
                    if( userFacade==null )
                        userFacade=UserFacade.getInstance();
                    r.setUser( userFacade.getUser( r.getUserId()));
                }
            }

        }
        catch( Exception e )
        {
            LogService.logIt( e, "RaterRefUtils.doEnterCore2() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) );
            throw e;
        }
    }


    public void doEnterCore(boolean goToFirstUnanswered) throws Exception
    {
        getRefBean();
        RcCheck rc = null;
        try
        {
            rc = refBean.getRcCheck();
            if( rc == null )
                throw new Exception( "RcCheck is null in RcBean." );
            if( rc.getRcScript()==null )
                throw new Exception( "RcScript is null in RcBean.rcCheck" );
            if( rc.getRcRater()==null )
                throw new Exception( "RcRater is null in RcBean.rcCheck" );

            if( !rc.getRcRater().getRcRaterStatusType().getStartedOrHigher() )
            {
                rc.getRcRater().setRcRaterStatusTypeId( RcRaterStatusType.STARTED.getRcRaterStatusTypeId() );
                if( !refBean.getAdminOverride() )
                {
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcRater(rc.getRcRater(), false );
                }
            }
            
            raterRefBean.clearBean();

            RcItemWrapper rciwx = getFirstRcItemWrapper( goToFirstUnanswered);

            //if( rciwx==null && goToFirstUnanswered && refBean.getRefUserType().getIsCandidate() )
            //{
            //    CandidateRefUtils cru = CandidateRefUtils.getInstance();
            //    if( cru.getStartAtEndOfCore2() )
           //     {
            //        rciwx = this.getLastRcItemWrapper();
            //        LogService.logIt( "RaterRefUtils.doEnterCore() BBB.1 rciwx=null and Candidate and start at end of Core2, so going to Last RcItemWrapper()" + (rciwx==null ? "null" : rciwx.getRcItemId() + ", status=" + rciwx.getRcRating().getRcRatingStatusType().getName() ) );
            //    }
            //}
            
            // LogService.logIt( "RaterRefUtils.doEnterCore() rciwx=" + (rciwx==null ? "null" : rciwx.getRcItemId() ) + ", needsCore=" + this.getNeedsCore() + ", rcCheckId="  + rc.getRcCheckId() );
            raterRefBean.setRcItemWrapper(rciwx, rc.getRcRater().getIsCandidateOrEmployee() );
            // all items are complete.
            if( rciwx==null )
                return;

            prepareForItem();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RaterRefUtils.doEnterCore() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) );
            throw e;
        }
    }



    public RcItemWrapper getPreviousRcItemWrapper() throws Exception
    {
        int targetItemDO = raterRefBean.getItemDisplayOrder()-1;
        if( targetItemDO<1 )
            targetItemDO=1;

        return getRcItemWrapper( targetItemDO );
    }


    public RcItemWrapper getNextRcItemWrapper() throws Exception
    {
        getRefBean();
        return getRcItemWrapper( raterRefBean.getItemDisplayOrder()+1 );
    }


    public RcItemWrapper getRcItemWrapper( int iwDO ) throws Exception
    {
        getRefBean();

        if( iwDO<1 )
            iwDO=1;

        boolean isSelf = refBean.getRcCheck().getRcRater().getIsCandidateOrEmployee();
        //LogService.logIt( "RaterRefUtils.getRcItemWrapper() iwDO=" + iwDO + ", total ItemWrappers=" + refBean.getRcCheck().getRcScript().getAllItemWrapperList().size() + ", isSelf=" + isSelf );
        for( RcItemWrapper w : refBean.getRcCheck().getRcScript().getAllItemWrapperList() )
        {
            //LogService.logIt( "RaterRefUtils.getRcItemWrapper() seeking iwDO=" + iwDO + " current DO=" + w.getDisplayOrder() );
            if( !isSelf && w.getRaterDisplayOrder()==iwDO )
            {
                //LogService.logIt( "RaterRefUtils.getRcItemWrapper() Found matching itemWrapper. " + iwDO  );
                return w;
            }
            if( isSelf && w.getCandidateDisplayOrder()==iwDO )
            {
                //LogService.logIt( "RaterRefUtils.getRcItemWrapper() Found matching itemWrapper FOR CANDIDATE. " + iwDO  );
                return w;
            }
        }
        return null;
    }

    //public RcItemWrapper getFirstRcItemWrapper() throws Exception
    //{
    //    return getFirstRcItemWrapper( false );
    //}

    //public RcItemWrapper getFirstUnansweredRcItemWrapper() throws Exception
    //{
    //    return getFirstRcItemWrapper( true );
    //}

    public RcItemWrapper getFirstRcItemWrapper( boolean firstUnanswered ) throws Exception
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        if( rc == null )
            throw new Exception( "RcCheck is null in RcBean." );
        if( rc.getRcScript()==null )
            throw new Exception( "RcScript is null in RcBean.rcCheck" );
        if( rc.getRcRater()==null )
            throw new Exception( "RcRater is null in RcBean.rcCheck" );

        boolean isSelf = rc.getRcRater().getIsCandidateOrEmployee();
        // LogService.logIt( "RaterRefUtils.getFirstRcItemWrapper() START isSelf=" + isSelf + ", size=" + rc.getRcScript().getAllItemWrapperList().size()  );

        for( RcItemWrapper w : rc.getRcScript().getAllItemWrapperList() )
        {
            // LogService.logIt( "RaterRefUtils.getFirstRcItemWrapper() firstUnanswered=" + firstUnanswered + ", isSelf=" + isSelf + ", itemId=" + w.getRcItemId() + ", skipCand=" + w.getRcItem().getSkipforCandidate() + ", complete=" + w.getIsCompleteOrHigher() );
            if( isSelf && w.getRcItem().getSkipforCandidate()>0 )
                continue;

            if( !firstUnanswered || !w.getIsCompleteOrHigher() )
                return w;
        }
        return null;
    }

    public RcItemWrapper getLastRcItemWrapper() throws Exception
    {
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        if( rc == null )
            throw new Exception( "RcCheck is null in RcBean." );
        if( rc.getRcScript()==null )
            throw new Exception( "RcScript is null in RcBean.rcCheck" );
        if( rc.getRcRater()==null )
            throw new Exception( "RcRater is null in RcBean.rcCheck" );

        boolean isSelf = rc.getRcRater().getIsCandidateOrEmployee();
        // LogService.logIt( "RaterRefUtils.getFirstRcItemWrapper() START isSelf=" + isSelf + ", size=" + rc.getRcScript().getAllItemWrapperList().size()  );

        RcItemWrapper w;
        List<RcItemWrapper> iwl = rc.getRcScript().getAllItemWrapperList();

        for( int i=iwl.size()-1; i>=0; i-- )
        {
            w = iwl.get(i);
            if( isSelf && w.getRcItem().getSkipforCandidate()>0 )
                continue;
            return w;
        }
        return null;
    }


    public void prepareForItem() throws Exception
    {
        getRefBean();

        RcItemWrapper rciw = raterRefBean.getRcItemWrapper();
        if( rciw==null )
            return;

        RcItemFormatType rcItemFormatType = raterRefBean.getRcItemFormatType();

        if( rcCheckUtils==null )
            rcCheckUtils=new RcCheckUtils();
        rcCheckUtils.performPreliminarySubstitutions( rciw.getRcItem(), refBean.getRcCheck(), getLocale() );

        RcRating rating = rciw.getRcRating();

        // check if rating missing.
        if( rating==null )
        {
            // Check for rating
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rating = rcFacade.getRcRatingForRcRaterAndRcItem(refBean.getRcCheck().getRcRater().getRcRaterId(), rciw.getRcItemId() );
            if( rating!=null )
            {
                if( refBean.getAdminOverride() )
                    rating=(RcRating) rating.clone();

                rciw.setRcRating(rating);
            }
        }

        // Must always have a rating.
        if( rating==null )
        {
            rating = new RcRating();
            rating.setRcCheckId(refBean.getRcCheck().getRcCheckId());
            rating.setRcRaterId(refBean.getRcCheck().getRcRater().getRcRaterId());
            rating.setRcItemId(rciw.getRcItemId() );
            rating.setRcItemFormatTypeId( rciw.getRcItem().getItemFormatTypeId() );
            rating.setRcRatingStatusTypeId( RcRatingStatusType.INCOMPLETE.getRcRatingStatusTypeId() );
            rciw.setRcRating(rating);

            if( !refBean.getAdminOverride() && refBean.getRcCheck().getRcAvType().getAnyMedia() )
            {
                //if( rcFacade==null )
                //    rcFacade=RcFacade.getInstance();
                saveRcRatingWithCheck(rating);
            }
        }

        FileUploadFacade fuf = null;
        
        if( !refBean.getRefUserType().getIsCandidate() &&
            rciw.getRcItem().getShowCandRespToRater()>0 &&
            refBean.getRcCheck().getCandidateRcRaterId()>0 )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            RcRating crtg = rcFacade.getRcRatingForRcRaterAndRcItem(refBean.getRcCheck().getCandidateRcRaterId(), rciw.getRcItemId() );

            if( crtg==null )
                LogService.logIt( "RaterRefUtils.prepareForItem() Cannot find Candidate Rating for this item. rcCheckId=" + refBean.getRcCheck().getRcCheckId() + ", candidateRcRaterId=" + refBean.getRcCheck().getCandidateRcRaterId() + ( rciw.getRcItem()==null ? " item is null, itemId=" + rciw.getRcItemId() : rciw.getRcItem().toString()) );

            rating.setCandidateRcRating(crtg);
            
            if( fuf == null )
                fuf = FileUploadFacade.getInstance();

            if( crtg!=null  )
            {
                if( crtg.getCandidateRcUploadedUserFile()==null )
                    crtg.setCandidateRcUploadedUserFile( fuf.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(crtg.getRcCheckId(), crtg.getRcRaterId(), crtg.getRcItemId(), UploadedUserFileType.REF_CHECK_CANDIDATE_FILE_UPLOAD.getUploadedUserFileTypeId()));
            
                if( crtg.getCandidateRcUploadedUserFile()!=null && crtg.getCandidateUploadedUserFileId()!=crtg.getCandidateRcUploadedUserFile().getRcUploadedUserFileId())
                {
                    crtg.setCandidateUploadedUserFileId( crtg.getCandidateRcUploadedUserFile().getRcUploadedUserFileId() );
                    rcFacade.saveRcRating(crtg);
                }                    

                if( crtg.getCandidateRcUploadedUserFile()==null && crtg.getCandidateUploadedUserFileId()>0 )
                    crtg.setCandidateRcUploadedUserFile( fuf.getRcUploadedUserFile( crtg.getCandidateUploadedUserFileId()));
                
                if( crtg.getCandidateRcUploadedUserFile()!=null )
                {
                    crtg.getCandidateRcUploadedUserFile().setUploadedFileUrl( RcCheckUtils.getUploadedFileUrl(crtg.getCandidateRcUploadedUserFile()));
                    crtg.getCandidateRcUploadedUserFile().setUploadedFileIconFilename( RcCheckUtils.getUploadedFileIconFilename( crtg.getCandidateRcUploadedUserFile()));
                    crtg.getCandidateRcUploadedUserFile().setUploadedFileTypeName( RcCheckUtils.getUploadedFileTypeName( getLocale(), crtg.getCandidateRcUploadedUserFile()));
                }
            }

            if( crtg!=null &&
                rciw.getRcItem().getHasAvCandidateFileUpload() && 
                rciw.getRcItem().getShowCandRespToRater()>0 )
            {            
                if( fuf == null )
                    fuf = FileUploadFacade.getInstance();

                if( crtg.getRcUploadedUserFile()==null )
                    crtg.setRcUploadedUserFile( fuf.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(crtg.getRcCheckId(), crtg.getRcRaterId(), crtg.getRcItemId(), UploadedUserFileType.REF_CHECK_RATER_COMMENT.getUploadedUserFileTypeId()));

                if( crtg.getUploadedUserFileId()>0 && crtg.getRcUploadedUserFile()==null )
                    crtg.setRcUploadedUserFile(fuf.getRcUploadedUserFile( crtg.getUploadedUserFileId() ));

                if( crtg.getRcUploadedUserFile()!=null && crtg.getUploadedUserFileId()!=crtg.getRcUploadedUserFile().getRcUploadedUserFileId() )
                {
                    crtg.setUploadedUserFileId( crtg.getRcUploadedUserFile().getRcUploadedUserFileId() );
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcRating(crtg);
                }                                     
                
                if( crtg.getRcUploadedUserFile()!=null && crtg.getRcUploadedUserFile().getHasRecordingReadyForPlayback() )
                    crtg.getRcUploadedUserFile().setUploadedFileUrl( RcCheckUtils.getUploadedFileUrl(crtg.getRcUploadedUserFile()));                    
            }


        }

        if( refBean.getRefUserType().getIsCandidate() &&
            rciw.getRcItem().getHasCandidateFileUpload() &&
            !rciw.getRcItem().getHasAvCandidateFileUpload() )
        {            
            if( fuf == null )
                fuf = FileUploadFacade.getInstance();

            if( rating.getCandidateUploadedUserFileId()>0 && rating.getCandidateRcUploadedUserFile()==null )
                rating.setCandidateRcUploadedUserFile(fuf.getRcUploadedUserFile( rating.getCandidateUploadedUserFileId() ));
            
            if( rating.getCandidateRcUploadedUserFile()==null )
                rating.setCandidateRcUploadedUserFile( fuf.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(rating.getRcCheckId(), rating.getRcRaterId(), rating.getRcItemId(), UploadedUserFileType.REF_CHECK_CANDIDATE_FILE_UPLOAD.getUploadedUserFileTypeId()));

            if( rating.getCandidateRcUploadedUserFile()!=null && rating.getCandidateUploadedUserFileId()!=rating.getCandidateRcUploadedUserFile().getRcUploadedUserFileId() )
            {
                rating.setCandidateUploadedUserFileId( rating.getCandidateRcUploadedUserFile().getRcUploadedUserFileId() );
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcFacade.saveRcRating(rating);
            }                                            
        }


        
        
        // at this point we always have a rating though it may not have been saved.
        try
        {
            raterRefBean.setSelectedRadioIndex( 0 );
            if( rcItemFormatType.getIsRadio() )
            {
                int idx=0;
                if( rating.getIsComplete() && rating.getSelectedResponse()!=null && !rating.getSelectedResponse().isBlank() )
                    idx = Integer.parseInt( rating.getSelectedResponse() );

                raterRefBean.setSelectedRadioIndex( idx );
            }

            else if( rcItemFormatType.getIsCheckbox() )
            {
                if( !rating.getIsComplete() )
                {
                    // raterRefBean.setSelectedCheckboxes(new int[0] );
                    raterRefBean.setSelectedCheckboxesStr( new String[0] );
                }
                else
                {
                    List<Integer> il = new ArrayList<>();
                    String svs = rating.getSelectedResponse();
                    if( svs==null )
                        svs = "";
                    String[] vals = svs.split(",");
                    int idx;
                    for( String s : vals )
                    {
                        s = s.trim();
                        if( s.isBlank() )
                            continue;
                        idx=Integer.parseInt(s);
                        il.add(idx);
                    }
                    String[] ia = new String[il.size()];
                    for( int i=0;i<il.size();i++ )
                    {
                        ia[i] = Integer.toString( il.get(i) );
                    }
                    raterRefBean.setSelectedCheckboxesStr(ia);
                }
            }
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RaterRefUtils.prepareForItem() " + ( rciw.getRcItem()==null ? " item is null, itemId=" + rciw.getRcItemId() : rciw.getRcItem().toString()) );
            throw e;
        }
    }


    public List<SelectItem> getRcItemRadioSelectItemListForRating()
    {
        getRefBean();

        boolean discrete = refBean!=null && refBean.getRcCheck().getRcScript()!=null && refBean.getRcCheck().getRcScript().getUseDiscreteRatingsB();

        List<SelectItem> out = new ArrayList<>();

        RcItemWrapper rciw = raterRefBean.getRcItemWrapper();
        if( rciw==null || rciw.getRcItem()==null || !rciw.getRcItem().getRcItemFormatType().getIsRating() || !rciw.getRcItem().getIncludeNumRatingB() )
            return out;

        boolean noMiddle = rciw.getRcItem().getDenyMiddleB();

        SelectItem ss;

        RcRatingScaleType ratingScale = refBean.getRcCheck().getRcScript().getRcRatingScaleType();

        // If Discrete, use five choices.
        if(discrete )
        {
            float[] vals = ratingScale.getDiscreteValues();
            SelectItem si;
            for( int i=0;i<5;i++ )
            {
                si= new SelectItem( ((float)(vals[i])), "   " );
                if( i==2 && noMiddle )
                    si.setDisabled(true);

                out.add( si );
            }
            //out.add( new SelectItem( ((float)(1.0)), "   " ) );
            //out.add( new SelectItem( ((float)(3.25)), "   " ) );

            //ss = new SelectItem( ((float)(5.5)), "   " );
            //if( noMiddle )
            //    ss.setDisabled(true);
            //out.add( ss );

            //out.add( new SelectItem( ((float)(7.75)), "   " ) );
            //out.add( new SelectItem( ((float)(10.0)), "   " ) );
        }

        // Not discrete, this must be MSIE or Accessible
        else
        {
            float[] vals = ratingScale.getDiscreteValuesMsie();
            SelectItem si;
            for( int i=0;i<vals.length;i++ )
            {
                si= new SelectItem( ((float)(vals[i])), " " );

                if( noMiddle && ratingScale.getIsDefault() && (i==4 || i==5) )
                    si.setDisabled(true);

                if( noMiddle && ratingScale.getIsOneToFive() && i==2 )
                    si.setDisabled(true);

                out.add( si );
            }

        }

        // LogService.logIt( "RaterRefUtils.getRcItemRadioSelectItemListForRating() selectItemList.size=" + out.size() );
        return out;
    }

    public List<Object[]> getRcItemRadioInfoArrayList()
    {
        List<Object[]> out = new ArrayList<>();
        RcItemWrapper rciw = raterRefBean.getRcItemWrapper();
        if( rciw==null || rciw.getRcItem()==null || ( !raterRefBean.getRcItemFormatType().getIsRadio() && !raterRefBean.getRcItemFormatType().getIsCheckbox()) )
            return out;

        String[] data = null;

        RcItem itm = rciw.getRcItem();
        if( rciw.getRcItem().getHasChoice1() )
            out.add(  new Object[] {(int)1, itm.getChoice1()} );
        if( rciw.getRcItem().getHasChoice2() )
            out.add(  new Object[] {(int)2, itm.getChoice2()} );
        if( rciw.getRcItem().getHasChoice3() )
            out.add(  new Object[] {(int)3, itm.getChoice3()} );
        if( rciw.getRcItem().getHasChoice4() )
            out.add(  new Object[] {(int)4, itm.getChoice4()} );
        if( rciw.getRcItem().getHasChoice5() )
            out.add(  new Object[] {(int)5, itm.getChoice5()} );
        if( rciw.getRcItem().getHasChoice6() )
            out.add(  new Object[] {(int)6, itm.getChoice6()} );
        if( rciw.getRcItem().getHasChoice7() )
            out.add(  new Object[] {(int)7, itm.getChoice7()} );
        if( rciw.getRcItem().getHasChoice8() )
            out.add(  new Object[] {(int)8, itm.getChoice8()} );
        if( rciw.getRcItem().getHasChoice9() )
            out.add(  new Object[] {(int)9, itm.getChoice9()} );
        if( rciw.getRcItem().getHasChoice10() )
            out.add(  new Object[] {(int)10, itm.getChoice10()} );
        return out;
    }


    public int getDiscreteRadioDivMinWidth()
    {
        if( 1==1 )
        return 30;
        return 62;
        /*
        RcItemWrapper rciw = raterRefBean.getRcItemWrapper();
        if( rciw==null || rciw.getRcItem()==null || ( !raterRefBean.getRcItemFormatType().getIsRadio() && !raterRefBean.getRcItemFormatType().getIsCheckbox()) )
            return 40;
        int wid = 0;
        int incr = 40;
        RcItem itm = rciw.getRcItem();
        if( rciw.getRcItem().getHasChoice1() )
            wid += incr;
        else
            return wid;
        if( rciw.getRcItem().getHasChoice2() )
            wid += incr;
        else
            return wid;
        if( rciw.getRcItem().getHasChoice3() )
            wid += incr;
        else
            return wid;
        if( rciw.getRcItem().getHasChoice4() )
            wid += incr;
        else
            return wid;
        if( rciw.getRcItem().getHasChoice5() )
            wid += incr;
        else
            return wid;
        if( rciw.getRcItem().getHasChoice6() )
            wid += incr;
        else
            return wid;
        if( rciw.getRcItem().getHasChoice7() )
            wid += incr;
        else
            return wid;
        if( rciw.getRcItem().getHasChoice8() )
            wid += incr;
        else
            return wid;
        if( rciw.getRcItem().getHasChoice9() )
            wid += incr;
        else
            return wid;
        if( rciw.getRcItem().getHasChoice10() )
            wid += incr;
        return wid;
        */
    }



    public List<SelectItem> getRcItemRadioSelectItemList()
    {
        List<SelectItem> out = new ArrayList<>();
        RcItemWrapper rciw = raterRefBean.getRcItemWrapper();
        if( rciw==null || rciw.getRcItem()==null || ( !raterRefBean.getRcItemFormatType().getIsRadio() && !raterRefBean.getRcItemFormatType().getIsCheckbox()) )
            return out;

        RcItem itm = rciw.getRcItem();
        if( rciw.getRcItem().getHasChoice1() )
            out.add( new SelectItem( ((int)(1)), itm.getChoice1() ) );
        if( rciw.getRcItem().getHasChoice2() )
            out.add( new SelectItem( ((int)(2)), itm.getChoice2() ) );
        if( rciw.getRcItem().getHasChoice3() )
            out.add( new SelectItem( ((int)(3)), itm.getChoice3() ) );
        if( rciw.getRcItem().getHasChoice4() )
            out.add( new SelectItem( ((int)(4)), itm.getChoice4() ) );
        if( rciw.getRcItem().getHasChoice5() )
            out.add( new SelectItem( ((int)(5)), itm.getChoice5() ) );
        if( rciw.getRcItem().getHasChoice6() )
            out.add( new SelectItem( ((int)(6)), itm.getChoice6() ) );
        if( rciw.getRcItem().getHasChoice7() )
            out.add( new SelectItem( ((int)(7)), itm.getChoice7() ) );
        if( rciw.getRcItem().getHasChoice8() )
            out.add( new SelectItem( ((int)(8)), itm.getChoice8() ) );
        if( rciw.getRcItem().getHasChoice9() )
            out.add( new SelectItem( ((int)(9)), itm.getChoice9() ) );
        if( rciw.getRcItem().getHasChoice10() )
            out.add( new SelectItem( ((int)(10)), itm.getChoice10() ) );
        return out;
    }




    public String getNextViewFromRatings() throws Exception
    {
        getRefBean();

        if( refBean.getRcCheck()==null )
        {
            RcCheck rcx = repairRefBeanForCurrentAction(refBean, true, 206 );
            if( rcx!=null )
                return getViewFromPageType( refBean.getRefPageType() );
        }
        
        if( raterRefBean.getRcItemWrapper()==null )
        {
            getRefBean();

            // check completion of Rater
            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();
            // complete the RcRater
            boolean isComplete = rcCheckUtils.performRcRaterCompletionIfReady(refBean.getRcCheck(), refBean.getRcCheck().getRcRater(), refBean.getAdminOverride() );
            
            if( refBean.getRcCheck().getRcRater().getIsCandidateOrEmployee() )
            {
                if( !isComplete )
                {
                    LogService.logIt( "RaterRefUtils.getNextViewFromRatings() AAA.1 NONFATAL ERROR. next RcItemWrapper is null, but Candidate/Employee RcRater is not complete. Restarting. rcCheck: "  + (refBean.getRcCheck()==null ? "null" : refBean.getRcCheck().toStringShort() + ", rcRaterId=" + (refBean.getRcCheck().getRcRater()==null ? "null" : refBean.getRcCheck().getRcRater().getRcRaterId())));
                
                    RefUtils ru = RefUtils.getInstance();
                    return ru.performRcCheckStart( refBean.getRcCheck(), refBean.getRefUserType(), true, refBean.getAdminOverride());
                }
                 
                else
                {
                    CandidateRefUtils cru = CandidateRefUtils.getInstance();
                    return cru.doCompleteSelfRatings();
                }
            }
            
            else if( !isComplete )
            {
                LogService.logIt( "RaterRefUtils.getNextViewFromRatings() AAA.2 Next RcItemWrapper is null, but Non-candidate RcRater is not complete. rcCheck: "  + (refBean.getRcCheck()==null ? "null" : refBean.getRcCheck().toStringShort() + ", rcRaterId=" + (refBean.getRcCheck().getRcRater()==null ? "null" : refBean.getRcCheck().getRcRater().getRcRaterId())));
                RefUtils ru = RefUtils.getInstance();
                return ru.performRcCheckStart( refBean.getRcCheck(), refBean.getRefUserType(), true, refBean.getAdminOverride());
            }

            refBean.setRefPageType(RefPageType.CORE );
            RefPageType pt = getNextPageTypeForRefProcess();

            if( pt.getIsCore2()  )
            {

            }

            refBean.setRefPageType(pt);
            return getViewFromPageType(pt);
        }

        prepareForItem();
        return "/ref/item.xhtml"; // + raterRefBean.getRcItemFormatType().getPage();
    }


    public float computeRaterPercentComplete()
    {
        getRefBean();
        boolean isSelf = refBean.getRcCheck().getRcRater().getIsCandidateOrEmployee();


        RcScript rcs = refBean.getRcCheck().getRcScript();

        if( isSelf && rcs.getItemCount(isSelf)==0 )
        {
            LogService.logIt( "RaterRefUtils.computeRaterPercentComplete() isSelf is true but RcScrit.getItemCount() for self ratings is 0. Setting percent complete to 100. rcCheck: " + refBean.getRcCheck().toString());
            return 100f;
        }

        return (float) NumberUtils.roundIt( 100f* ((float)rcs.getItemsAnswered(isSelf)) / ((float)rcs.getItemCount(isSelf)), 0 );
    }

    public boolean getNeedsCore() throws Exception
    {
        
        
        return raterRefBean.getRcItemWrapper()!=null;
    }

    public boolean getNeedsCore2() throws Exception
    {
        getRefBean();

        if( refBean.getRcCheck()==null )
            return false;

        if( refBean.getRefUserType().getIsRater() )
            return refBean.getRcCheck().getRcCheckType().getIsPrehire() && refBean.getRcCheck().getAskForReferrals()==1 && refBean.getRefUserType().getIsRater() && refBean.getRcCheck().getRcRater()!=null && !refBean.getRcCheck().getRcRater().getRcRaterStatusType().getCompleteOrHigher();
        
        // Candidate
        else
        {
            // need ratings from candidate and have rater in RcCheck for Candidate
            if( refBean.getRcCheck().getCollectRatingsFmCandidate() )
            {
                if( refBean.getRcCheck().getRcRater()==null || !refBean.getRcCheck().getRcRater().getIsCandidateOrEmployee() )
                    return false;
                
                // Not complete, or no expiration, or not expired.
                return !refBean.getRcCheck().getRcRater().getRcRaterStatusType().getCompleteOrHigher() || refBean.getRcCheck().getExpireDate()==null || refBean.getRcCheck().getExpireDate().after(new Date());
            }
            
            // No ratings from candidate
            return false;
        }
    }


    public String processGoBackToCore2()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        try
        {
            if( !getNeedsCore2() )
                return processGoBackToLastItem();

            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true, 200 );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true);
            }
            if( rc == null )
            {
                if( corpUtils==null )
                    corpUtils = CorpUtils.getInstance();
                return corpUtils.processCorpHome();
            }
            if( rc.getRcRater()==null )
                throw new Exception( "RcCheck.RcRater is null" );



            refBean.setRefPageType( RefPageType.CORE2 );
            doEnterCore2();
            return conditionUrlForSessionLossGet(getViewFromPageType(refBean.getRefPageType()), true);
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RaterRefUtils.processGoBackToLastItem() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }

    }

    public String processToggleAccessible()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        try
        {
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true, 201 );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
            {
                if( corpUtils==null )
                    corpUtils = CorpUtils.getInstance();
                return corpUtils.processCorpHome();
            }
            if( rc.getRcRater()==null )
                throw new Exception( "RcCheck.RcRater is null" );

            refBean.setAccessibleActive( !refBean.getAccessibleActive() );

            RcItemWrapper rciw = raterRefBean.getRcItemWrapper();

            // LogService.logIt( "RaterRefUtils.processToggleAccessible()" );

            if( rciw == null )
            {
                // If this is the candidate. It's OK and can happen when RcCheck is set to capture ratings from candidate but all items are set to skip for candidate.
                if( rc.getRcRater().getIsCandidateOrEmployee() && !rc.getRcScript().getHasAnyCandidateRatings()  )
                {
                    // throw new Exception( "RaterRefUtils.processGoBackToLastItem() Candidate. rcCheck is set to collect ratings from candidate, but there are no candidate ratings to collect for the associated script.  rcCheckId="  + rc.getRcCheckId() );
                    throw new Exception( "Candidate. rcCheck is set to collect ratings from candidate, but there are no candidate ratings to collect for the associated script.  rcCheckId="  + rc.getRcCheckId() );

                }
                else
                    throw new Exception( "Rater Cannot find RcItemWrapper()!" );
            }

            raterRefBean.setRcItemWrapper(rciw, rc.getRcRater().getIsCandidateOrEmployee() );
            refBean.setRefPageType( rc.getRcRater().getIsCandidateOrEmployee() ? RefPageType.CORE2 : RefPageType.CORE );
            // refBean.setRefPageType( rc.getRcRater().getIsCandidateOrEmployee() ? RefPageType.CORE3 : RefPageType.CORE );
            return conditionUrlForSessionLossGet(getNextViewFromRatings(), true);
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RaterRefUtils.processToggleAccessible() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public String processGoBackToLastItem()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        try
        {
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true, 202 );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
            {
                if( corpUtils==null )
                    corpUtils = CorpUtils.getInstance();
                return corpUtils.processCorpHome();
            }
            if( rc.getRcRater()==null )
                throw new Exception( "RcCheck.RcRater is null" );

            RcItemWrapper rciwNew = getLastRcItemWrapper();

            // LogService.logIt( "RaterRefUtils.doSaveItemResp() goBack=" + goBack + ", rciwNew=" + (rciwNew==null ? "null" : "not null, rcItemId=" + rciwNew.getRcItemId()) );

            if( rciwNew == null )
            {
                // If this is the candidate. It's OK and can happen when RcCheck is set to capture ratings from candidate but all items are set to skip for candidate.
                if( rc.getRcRater().getIsCandidateOrEmployee() && !rc.getRcScript().getHasAnyCandidateRatings()  )
                {
                    // throw new Exception( "RaterRefUtils.processGoBackToLastItem() Candidate. rcCheck is set to collect ratings from candidate, but there are no candidate ratings to collect for the associated script.  rcCheckId="  + rc.getRcCheckId() );
                    throw new Exception( "RaterRefUtils.processGoBackToLastItem() Candidate. rcCheck is set to collect ratings from candidate, but there are no candidate ratings to collect for the associated script.  rcCheckId="  + rc.getRcCheckId() );

                }
                else
                    throw new Exception( "Rater Cannot find last RcItemWrapper()!" );
            }

            raterRefBean.setRcItemWrapper(rciwNew, rc.getRcRater().getIsCandidateOrEmployee() );
            refBean.setRefPageType( rc.getRcRater().getIsCandidateOrEmployee() ? RefPageType.CORE2 : RefPageType.CORE );
            // refBean.setRefPageType( rc.getRcRater().getIsCandidateOrEmployee() ? RefPageType.CORE3 : RefPageType.CORE );
            return conditionUrlForSessionLossGet(getNextViewFromRatings(), true);
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RaterRefUtils.processGoBackToLastItem() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    public String processMarkCompleteAndExit()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        try
        {
            if( rc == null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true, 203 );
                if( rc!=null )
                    return conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc == null )
            {
                if( corpUtils==null )
                    corpUtils = CorpUtils.getInstance();
                return corpUtils.processCorpHome();
            }
            if( rc.getRcRater()==null )
                throw new Exception( "RcCheck.RcRater is null" );

            RcItemWrapper rciwNew = null;

            rciwNew = getFirstRcItemWrapper( true );
            // LogService.logIt( "RaterRefUtils.doSaveItemResp()Target (Next) RcItemWrapper is null. First unanswered RcItemWrapper is " + (rciwNew==null ? "null" : "rcItemId=" + rciwNew.getRcItemId() + " displayOrder=" + rciwNew.getDisplayOrder()) );

            raterRefBean.setRcItemWrapper(rciwNew, rc.getRcRater().getIsCandidateOrEmployee() );

            // complete!
            if( rciwNew==null )
            {
                if( rcCheckUtils==null )
                    rcCheckUtils = new RcCheckUtils();

                // complete the RcRater
                boolean isComplete = rcCheckUtils.performRcRaterCompletionIfReady(rc, rc.getRcRater(), refBean.getAdminOverride()  );

                if( !isComplete )
                    LogService.logIt( "RaterRefUtils.processMarkCompleteAndExit()  NONFATAL ERROR. rciwNew is null, but Rater is not complete after checking ratings. Will return to nextViewFromRatings. rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + rc.getRcRater().getRcRaterId());

                // should not happen but just in case.
                else if( rc.getRcRater().getIsCandidateOrEmployee() )
                {
                    CandidateRefUtils cru = CandidateRefUtils.getInstance();
                    return cru.doCompleteSelfRatings();                        
                }
                
                if( !refBean.getAdminOverride() && !rc.getRcRater().getIsCandidateOrEmployee() && (rc.getRcRater().getRcRaterStatusType().getIsComplete() ) )
                    rcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete( rc, rc.getRcRater(), false);
            }

            return conditionUrlForSessionLossGet(getNextViewFromRatings(), true);
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RaterRefUtils.processMarkCompleteAndExit() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }


    private void resetItemDOForBackFwdButton( RcCheck rc, boolean isCandidate, int itemDO, int tgtRcItemId ) throws Exception
    {
        getRefBean();
        RcItemWrapper rciw = raterRefBean.getRcItemWrapper();
        if( rciw==null )
            throw new Exception( "rcItemWrapper is null." );

        RcItemWrapper rciwx = getRcItemWrapper( itemDO );

        LogService.logIt( "RaterRefUtils.resetItemDOForBackFwdButton() itemDO=" + itemDO + ", current rciw.displayOrder=" + rciw.getDisplayOrder() + ", current rciw.rcItemId=" + rciw.getRcItemId() + ", rciwx=" + (rciwx==null ? "null" : rciwx.getRcItemId() ) + ", rcCheckId="  + rc.getRcCheckId() );

        raterRefBean.setRcItemWrapper(rciwx, isCandidate );

        // all items are complete.
        if( rciwx==null )
        {
            LogService.logIt( "RaterRefUtils.resetItemDOForBackFwdButton() No matching itemwrapper found for itemDO=" + itemDO + ", returning." );
            return;
        }

        if( rciwx.getRcItem().getRcItemId()!=tgtRcItemId )
        {
            LogService.logIt( "RaterRefUtils.resetItemDOForBackFwdButton() rciwx.rcItemId=" + rciwx.getRcItemId() + " for itemDO=" + itemDO + " does not match targetRcItemId=" + tgtRcItemId );
            throw new STException( "g.ErrorItemIdMismatch", new String[]{} );
            // throw new Exception( "RaterRefUtils.resetItemDOForBackFwdButton() rciwx for rcItemId=" + rciwx.getRcItemId() + " does not match targetRcItemId=" + tgtRcItemId );
        }

        RcRating rating = rciwx.getRcRating();

        // check if rating missing.
        if( rating==null )
        {
            // Check for rating
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rating = rcFacade.getRcRatingForRcRaterAndRcItem(refBean.getRcCheck().getRcRater().getRcRaterId(), rciwx.getRcItemId() );
            if( rating!=null )
            {
                if( refBean.getAdminOverride() )
                    rating = (RcRating) rating.clone();

                rciwx.setRcRating(rating);
            }
        }

        // Must always have a rating.
        if( rating==null )
        {
            rating = new RcRating();
            rating.setRcCheckId(refBean.getRcCheck().getRcCheckId());
            rating.setRcRaterId(refBean.getRcCheck().getRcRater().getRcRaterId());
            rating.setRcItemId(rciwx.getRcItemId() );
            rating.setRcItemFormatTypeId( rciwx.getRcItem().getItemFormatTypeId() );
            rating.setRcRatingStatusTypeId( RcRatingStatusType.INCOMPLETE.getRcRatingStatusTypeId() );
            rciwx.setRcRating(rating);
        }

        FileUploadFacade fuf = null;
        
        if( !refBean.getRefUserType().getIsCandidate() &&
            rciwx.getRcItem().getShowCandRespToRater()>0 &&
            refBean.getRcCheck().getCandidateRcRaterId()>0 )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            RcRating crtg = rcFacade.getRcRatingForRcRaterAndRcItem(refBean.getRcCheck().getCandidateRcRaterId(), rciwx.getRcItemId() );

            if( crtg==null )
                LogService.logIt( "RaterRefUtils.resetItemDOForBackFwdButton() Cannot find Candidate Rating for this item. rcCheckId=" + refBean.getRcCheck().getRcCheckId() + ", candidateRcRaterId=" + refBean.getRcCheck().getCandidateRcRaterId() + ( rciwx.getRcItem()==null ? " item is null, itemId=" + rciwx.getRcItemId() : rciwx.getRcItem().toString()) );

            rating.setCandidateRcRating(crtg);

            if( fuf==null )
                fuf = FileUploadFacade.getInstance();

            if( crtg!=null )
            {
                if( crtg.getCandidateRcUploadedUserFile()==null )
                    crtg.setCandidateRcUploadedUserFile( fuf.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(crtg.getRcCheckId(), crtg.getRcRaterId(), crtg.getRcItemId(), UploadedUserFileType.REF_CHECK_CANDIDATE_FILE_UPLOAD.getUploadedUserFileTypeId()));
            
                if( crtg.getCandidateRcUploadedUserFile()!=null && crtg.getCandidateUploadedUserFileId()!=crtg.getCandidateRcUploadedUserFile().getRcUploadedUserFileId() )
                {
                    crtg.setCandidateUploadedUserFileId( crtg.getCandidateRcUploadedUserFile().getRcUploadedUserFileId() );
                    rcFacade.saveRcRating(crtg);
                }                    

                if( crtg.getCandidateRcUploadedUserFile()==null && crtg.getCandidateUploadedUserFileId()>0 )
                    crtg.setCandidateRcUploadedUserFile( fuf.getRcUploadedUserFile( crtg.getCandidateUploadedUserFileId()));
                
                if( crtg.getCandidateRcUploadedUserFile()!=null )
                {
                    crtg.getCandidateRcUploadedUserFile().setUploadedFileUrl( RcCheckUtils.getUploadedFileUrl(crtg.getCandidateRcUploadedUserFile()));
                    crtg.getCandidateRcUploadedUserFile().setUploadedFileIconFilename( RcCheckUtils.getUploadedFileIconFilename( crtg.getCandidateRcUploadedUserFile()));
                    crtg.getCandidateRcUploadedUserFile().setUploadedFileTypeName( RcCheckUtils.getUploadedFileTypeName( getLocale(), crtg.getCandidateRcUploadedUserFile()));
                }
            }
            
            if( crtg!=null &&
                rciw.getRcItem().getHasAvCandidateFileUpload() && 
                rciw.getRcItem().getShowCandRespToRater()>0 )
            {            
                if( fuf == null )
                    fuf = FileUploadFacade.getInstance();

                if( crtg.getUploadedUserFileId()>0 && crtg.getRcUploadedUserFile()==null )
                    crtg.setRcUploadedUserFile(fuf.getRcUploadedUserFile( crtg.getUploadedUserFileId() ));

                if( crtg.getRcUploadedUserFile()==null )
                    crtg.setRcUploadedUserFile( fuf.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(crtg.getRcCheckId(), crtg.getRcRaterId(), crtg.getRcItemId(), UploadedUserFileType.REF_CHECK_RATER_COMMENT.getUploadedUserFileTypeId()));

                if( crtg.getRcUploadedUserFile()!=null && crtg.getUploadedUserFileId()!=crtg.getRcUploadedUserFile().getRcUploadedUserFileId() )
                {
                    crtg.setUploadedUserFileId( crtg.getRcUploadedUserFile().getRcUploadedUserFileId() );
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcRating(crtg);
                }                                     
                
                if( crtg.getRcUploadedUserFile()!=null && crtg.getRcUploadedUserFile().getHasRecordingReadyForPlayback() )
                {
                    crtg.getRcUploadedUserFile().setUploadedFileUrl( RcCheckUtils.getUploadedFileUrl(crtg.getRcUploadedUserFile()));                    
                }
            }
            
        }
        
        if( refBean.getRefUserType().getIsCandidate() &&
            rciw.getRcItem().getHasCandidateFileUpload() &&
            !rciw.getRcItem().getHasAvCandidateFileUpload() )
        {
            
            if( fuf==null )
                fuf = FileUploadFacade.getInstance();

            if( rating.getCandidateUploadedUserFileId()>0 && rating.getCandidateRcUploadedUserFile()==null )
                rating.setCandidateRcUploadedUserFile(fuf.getRcUploadedUserFile( rating.getCandidateUploadedUserFileId() ));
            
            if( rating.getCandidateRcUploadedUserFile()==null )
                rating.setCandidateRcUploadedUserFile( fuf.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(rating.getRcCheckId(), rating.getRcRaterId(), rating.getRcItemId(), UploadedUserFileType.REF_CHECK_CANDIDATE_FILE_UPLOAD.getUploadedUserFileTypeId()));

            if( rating.getCandidateRcUploadedUserFile()!=null && rating.getCandidateUploadedUserFileId()!=rating.getCandidateRcUploadedUserFile().getRcUploadedUserFileId() )
            {
                rating.setCandidateUploadedUserFileId( rating.getCandidateRcUploadedUserFile().getRcUploadedUserFileId() );
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcFacade.saveRcRating(rating);
            }                                            
        }
        

    }

    
    
    
    
    public String processExitCore2()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        try
        {
            LogService.logIt( "RaterRefUtils.processExitCore2() START rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            if( rc==null )
            {
                LogService.logIt( "RaterRefUtils.processExitCore2() Fatal Error refBean.rcCheck is null." );
                return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), "RefBean.rcCheck is null" , null, null, rc, rc==null ? null : rc.getRcRater(), true );
            }

            // use the completed survey as a reason to check for candidate completion if this is not the candidate.
            if( !rc.getRcRater().getRcRaterStatusType().getCompleteOrHigher() )
            {
                if( rcCheckUtils==null )
                    rcCheckUtils = new RcCheckUtils();

                boolean isRaterComplete = rcCheckUtils.performRcRaterCompletionIfReady(rc, rc.getRcRater(), refBean.getAdminOverride() );

                if( !isRaterComplete )
                {
                    LogService.logIt( "RaterRefUtils.processExitCore2()  NONFATAL ERROR. Rater ratings not completed (or skipped). rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + " Rater is not completed sending to next view from ratings. rcRaterId=" + rc.getRcRater().getRcRaterId());
                    return getNextViewFromRatings();
                }
                
                // should not happen but just in case
                else if( rc.getRcRater().getIsCandidateOrEmployee() )
                {
                    CandidateRefUtils cru = CandidateRefUtils.getInstance();
                    return cru.doCompleteSelfRatings();                        
                }
            }

            refBean.setRefPageType(RefPageType.CORE2 );
            RefPageType pt = getNextPageTypeForRefProcess();

            LogService.logIt( "RaterRefUtils.processExitCore2() going to pageType=" + pt.getName() );
            
            refBean.setRefPageType(pt);
            return conditionUrlForSessionLossGet(getViewFromPageType(pt), true);
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RaterRefUtils.processExitCore2() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }

    }


    public String processCreateReferral()
    {
        getCorpBean();
        getRefBean();
        RcCheck rc = refBean.getRcCheck();
        try
        {
            if( rc==null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true, 204 );
                if( rc!=null )
                    return this.conditionUrlForSessionLossGet(getViewFromPageType( refBean.getRefPageType() ), true );
            }
            if( rc==null )
            {
                if( corpUtils==null )
                    corpUtils = CorpUtils.getInstance();
                return corpUtils.processCorpHome();
            }
            if( rc.getRcRater()==null )
            {
                // throw new Exception( "RcCheck.RcRater is null" );
                LogService.logIt( "RaterRefUtils.processCreateReferral() Error  RcCheck.RcRater is null. Likely session error or user backtracked. rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), "RaterRefUtils.processCreateReferral() Error  RcCheck.RcRater is null. Likely session error or user backtracked." , null, null, rc, rc==null ? null : rc.getRcRater(), true );
            }

            User refUser = raterRefBean.getReferralUser();

            if( !refUser.getHasNameEmailOrPhone() )
                throw new STException( "g.XRErrFullNameEmailOrPhoneReqd" );

            String mobile = refUser.getMobilePhone();
            boolean mobileValid = mobile==null || mobile.isBlank() ? false : GooglePhoneUtils.isNumberValid( mobile, rc.getRcRater().getUser().getCountryCode() );
            if( mobile!=null && !mobile.isBlank() && !mobileValid )
                throw new STException( "g.XCErrPhoneNumberInvalidIgnored", new String[] {mobile} );

            boolean emailValid = EmailUtils.validateEmailNoErrors( refUser.getEmail() );

            if( !emailValid && !mobileValid )
                throw new STException( "g.XCErrValidEmailOrPhoneRequired" );

            if( mobile!=null && !mobile.isBlank() && mobileValid )
                mobile = GooglePhoneUtils.getFormattedPhoneNumberIntl(mobile, rc.getRcRater().getUser().getCountryCode() );

            String referralNotes = raterRefBean.getReferralNotes();
            if( referralNotes!=null && referralNotes.isBlank() )
                referralNotes=null;

            boolean created = createReferral(rc, rc.getRcRater(), refUser.getFirstName(), refUser.getLastName(), refUser.getEmail(), mobile, referralNotes);

            if( created )
            {
                setInfoMessage("g.XRReferralCreatedForX", new String[]{refUser.getFullname()} );
                raterRefBean.setReferralNotes(null);
                raterRefBean.setReferralUser(new User() );
            }
            else
                setErrorMessage("g.XRReferralExistsForX", new String[]{refUser.getFullname()} );

            return conditionUrlForSessionLossGet("/ref/referrals.xhtml", true);
            // return "StayInSamePlace";
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RaterRefUtils.processCreateReferral() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }

    }


    public String processSaveItemResp()
    {
        return conditionUrlForSessionLossGet(doSaveItemResp( false ), true);
    }
    public String processSkipItem()
    {
        return conditionUrlForSessionLossGet(doSaveItemResp( true ), true);
    }

    public String doSaveItemResp( boolean skip )
    {
        getCorpBean();
        getRefBean();
        RcItemWrapper rciw = raterRefBean.getRcItemWrapper();
        RcCheck rc = refBean.getRcCheck();
        try
        {
            if( rc==null )
            {
                rc = repairRefBeanForCurrentAction(refBean, true, 205 );
                if( rc!=null )
                    return getViewFromPageType( refBean.getRefPageType() );
            }
            if( rc==null )
            {
                if( corpUtils==null )
                    corpUtils = CorpUtils.getInstance();
                return corpUtils.processCorpHome();
            }
            if( rc.getRcRater()==null )
            {
                // throw new Exception( "RcCheck.RcRater is null" );
                LogService.logIt( "RaterRefUtils.doSaveItemResp() Error  RcCheck.RcRater is null. Likely session error or user backtracked. rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), "RaterRefUtils.doSaveItemResp() Error  RcCheck.RcRater is null. Likely session error or user backtracked." , null, null, rc, rc==null ? null : rc.getRcRater(), true );
            }

            if( rciw==null )
            {
                // throw new Exception( "RcItemWrapper is null in RaterRefBean." );
                LogService.logIt( "RaterRefUtils.doSaveItemResp() Error  RcItemWrapper is null in RaterRefBean. Likely session error or user backtracked. rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), "RaterRefUtils.doSaveItemResp() Error  RcItemWrapper is null in RaterRefBean. Likely session error or user backtracked. " , null, null, rc, rc==null ? null : rc.getRcRater(), true );
            }

            RcItem itm = rciw.getRcItem();
            if( itm==null )
            {
                // throw new Exception( "RcItemWrapper.RcItem is null" );
                LogService.logIt( "RaterRefUtils.doSaveItemResp() Error  RcItemWrapper.RcItem is null. Likely session error or user backtracked.  rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), "RaterRefUtils.doSaveItemResp() Error  RcItemWrapper.RcItem is null. Likely session error or user backtracked." , null, null, rc, rc==null ? null : rc.getRcRater(), true );
            }

            RcRating rating = rciw.getRcRating();
            if( rating==null )
            {
                // throw new Exception( "RcItemWrapper.RcRating is null" );
                LogService.logIt( "RaterRefUtils.doSaveItemResp() Error  RcItemWrapper.RcItem is null. Likely session error or user backtracked.  rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), "RaterRefUtils.doSaveItemResp() Error  RcItemWrapper.RcItem is null. Likely session error or user backtracked." , null, null, rc, rc==null ? null : rc.getRcRater(), true );
            }

            long rcCheckId = getEncryptedIdFmRequest( "rcid" );
            if( rcCheckId!=rc.getRcCheckId() )
            {
                // throw new Exception( "Request.rcCheckId=" + rcCheckId + " does not match session.rcCheckId=" + rc.getRcCheckId() );
                LogService.logIt( "RaterRefUtils.doSaveItemResp() Error Request.rcCheckId=" + rcCheckId + " does not match session.rcCheckId=" + rc.getRcCheckId() + " rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), "RaterRefUtils.doSaveItemResp() Error Request.rcCheckId=" + rcCheckId + " does not match session.rcCheckId=" + rc.getRcCheckId() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
            }

            long rcRaterId = getEncryptedIdFmRequest( "rcrid" );
            if( rc.getRcRater().getRcRaterId()!=rcRaterId )
            {
                // throw new Exception( "Request.rcRaterId=" + rcRaterId + " does not match session.rcRaterId=" + rc.getRcRater().getRcRaterId() );
                LogService.logIt( "RaterRefUtils.doSaveItemResp() Error  Request.rcRaterId=" + rcRaterId + " does not match session.rcRaterId=" + rc.getRcRater().getRcRaterId() + " rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
                return systemError(rc.getOrg(), CorpBean.getInstance().getCorp(), "Request.rcRaterId=" + rcRaterId + " does not match session.rcRaterId=" + rc.getRcRater().getRcRaterId() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
            }

            // get values from request
            int itemDO = getUnencryptedIntFmRequest( "rcitemdo" );
            int itemId = (int) getEncryptedIdFmRequest( "rcitemid" );

            // Seems out of order - rater
            if( !rc.getRcRater().getIsCandidateOrEmployee() && itemDO!=rciw.getRaterDisplayOrder() )
            {
                if( itemDO>=1 && (itemDO==rciw.getRaterDisplayOrder()-1 || itemDO==rciw.getRaterDisplayOrder()+1 ) )
                {
                    // this is ok. Can happen on a reload.
                    return "StayInSamePlace";
                }

                LogService.logIt( "RaterRefUtils.doSaveItemResp() BBB.1 Rater. Request.ItemDO=" + itemDO + " does not match session.raterItemWrapperDO=" + rciw.getRaterDisplayOrder() + ", wrapper.itemId=" + rciw.getRcItem().getRcItemId() + " request.itemId=" + itemId + ", attempting to move to correct item DO." );
                resetItemDOForBackFwdButton( rc, false,  itemDO, itemId );
                return doSaveItemResp( skip );

                // throw new Exception( "Request.ItemDO=" + itemDO + " does not match session.itemWrapperRaterDO=" + rciw.getRaterDisplayOrder() + ", wrapper.itemId=" + rciw.getRcItem().getRcItemId() + " request.itemId=" + itemId );
            }

            // seems out of order - candidate
            if( rc.getRcRater().getIsCandidateOrEmployee() && itemDO!=rciw.getCandidateDisplayOrder())
            {
                if( itemDO>=1 && (itemDO==rciw.getCandidateDisplayOrder()-1 || itemDO==rciw.getCandidateDisplayOrder()+1 ) )
                {
                    // this is ok. Can happen on a reload.
                    return "StayInSamePlace";
                }

                //if( itemDO<rciw.getCandidateDisplayOrder() )
                //{
                    LogService.logIt( "RaterRefUtils.doSaveItemResp() BBB.2 Candidate. Request.ItemDO=" + itemDO + " does not match session.candidateItemWrapperDO=" + rciw.getCandidateDisplayOrder() + ", wrapper.itemId=" + rciw.getRcItem().getRcItemId() + " request.itemId=" + itemId );
                    resetItemDOForBackFwdButton( rc, true,  itemDO, itemId );
                    return doSaveItemResp( skip );
                //}

                //throw new Exception( "Request.ItemDO=" + itemDO + " does not match session.candidateItemWrapperDO=" + rciw.getCandidateDisplayOrder() + ", wrapper.itemId=" + rciw.getRcItem().getRcItemId() + " request.itemId=" + itemId );
            }

            if( itemId!=rciw.getRcItemId())
                throw new Exception( "Request.ItemId=" + itemId + " does not match session.itemWrapper.itemId=" + rciw.getRcItemId() );

            // OK at this point it looks like we have a valid response.
            boolean goBack = booleanParam1;

            if( goBack && itemDO<=1 )
            {
                if( !rc.getRcRater().getIsCandidateOrEmployee() || !rc.getRcScript().getHasAnyCandidateInput() )
                {
                    LogService.logIt( "RaterRefUtils.doSaveItemResp() Go Back selected but itemDO=" + itemDO + ". Ignoring." );
                    goBack=false;
                }
            }

            rating.setText( StringUtils.removeAllControlChars( rating.getText() ) );
            
            RcItemFormatType itmFmt = itm.getRcItemFormatType();

            String validMessage = null;

            String infoMessage = null;

            boolean complete = true;

            float score = -1;

            RcRatingScaleType ratingScale = rc.getRcScript().getRcRatingScaleType();

            FileUploadFacade fileUploadFacade = null;

            if( !refBean.getAdminOverride() && (refBean.getAudioVideoCommentsOk() || refBean.getAudioVideoCandidateUpload()) && !getIsMsie() )
            {
                if( fileUploadFacade==null )
                    fileUploadFacade = FileUploadFacade.getInstance();
                rating.setRcUploadedUserFile( fileUploadFacade.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType( rcCheckId, rcRaterId, itemId, UploadedUserFileType.REF_CHECK_RATER_COMMENT.getUploadedUserFileTypeId() ) );
            }

            if( uploadedFile !=null )
            {
                // LogService.logIt( "Wait a few seconds for upload
                Thread.sleep(1500);
            }
            
            // candidate uploaded file
            if( refBean.getRefUserType().getIsCandidate() && itm.getHasCandidateFileUpload() )
            {
                if( fileUploadFacade==null )
                    fileUploadFacade = FileUploadFacade.getInstance();

                if( rating.getCandidateUploadedUserFileId()>0 && rating.getCandidateRcUploadedUserFile()==null )
                    rating.setCandidateRcUploadedUserFile(fileUploadFacade.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType( rcCheckId, rcRaterId, itemId, UploadedUserFileType.REF_CHECK_CANDIDATE_FILE_UPLOAD.getUploadedUserFileTypeId() ) );

                if( rating.getCandidateUploadedUserFileId()>0 && rating.getCandidateRcUploadedUserFile()==null )
                {
                    Tracker.addCandidateFileUploadError();
                    throw new Exception( "Cannot find CandidateUploadedUserFileId=" + rating.getCandidateUploadedUserFileId() + ", for rcRatingId=" + rating.getRcRatingId() + ", rcItemId=" + itm.getRcItemId() + ", rcCheckId=" + rating.getRcCheckId() + ", rcRaterId=" + rating.getRcRaterId());
                }

                getProctorBean();
                
                if( !skip )
                {
                    if( !itm.getRcCandidateUploadType().getAnyAudioVideo() )
                    {
                        FileContentType fct = uploadedFile==null ? null : FileContentType.getFileContentTypeFromContentType( uploadedFile.getContentType(), uploadedFile.getFileName() );
                        if( uploadedFile==null )
                        {
                            // no existing file. 
                            if( rating.getCandidateRcUploadedUserFile()==null )
                            {
                                validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), "g.UploadedCandFileRqd" );
                                complete = false;
                            }
                            // otherwise OK!
                            else
                            {}
                        }
                        else if( uploadedFile.getSize()<=1 )
                        {
                            // no existing file. 
                            if( rating.getCandidateRcUploadedUserFile()==null )
                            {
                                validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), "g.UploadedCandFileTooSmall", new String[]{ Long.toString( uploadedFile.getSize())} );
                                complete = false;
                                uploadedFile = null;
                            }
                            // otherwise OK!
                            else
                            {}
                        }
                        else if( uploadedFile.getSize()>Constants.MAX_FILE_UPLOAD_SIZE )
                        {
                            validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), "g.UploadedCandFileTooBig", new String[]{ Long.toString( uploadedFile.getSize()), Long.toString(Constants.MAX_FILE_UPLOAD_SIZE)} );
                            complete = false;
                            uploadedFile = null;
                        }
                        else if( fct==null )
                        {
                            validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), "g.UploadedCandFileUnrecFileType", new String[]{ Long.toString( uploadedFile.getSize())} );
                            complete = false;
                            uploadedFile = null;
                        }
                        else if( !itm.getRcCandidateUploadType().getIsUploadedFileContentTypeValid( fct ))
                        {
                            validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), "g.UploadedCandFileInvalidXY", new String[]{ fct.getBaseExtension().toLowerCase(), itm.getRcCandidateUploadType().getName(getLocale())} );
                            complete = false;
                            uploadedFile = null;
                        }
                        //else if( !fct.isValidForCandidateUploadedFile())
                        //{
                        //    validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), "g.UploadedCandFileInvalidFileType", new String[]{ Long.toString( uploadedFile.getSize())} );
                        //    complete = false;
                        //    uploadedFile = null;
                        //}

                        // OK to save.
                        if( uploadedFile!=null )
                        {
                            RcUploadedUserFile uuf = saveUploadedUserFile( rc, rc.getRcRater(), rating, fct, fileUploadFacade );
                            if( uuf!=null )
                                setInfoMessage("g.UploadedCandFileSaved", new String[]{uploadedFile.getFileName(), Long.toString(uploadedFile.getSize())});
                        }
                    }
                    
                    // Required response and not no set to opt out of audio / video
                    else if( itm.getHideSkip()==1 && !proctorBean.getCameraOptOut() )
                    {
                        if( rating.getRcUploadedUserFile()==null && rating.getUploadedUserFileId()>0 )
                            rating.setRcUploadedUserFile(fileUploadFacade.getRcUploadedUserFile( rating.getUploadedUserFileId() ) );
                                                
                        if( rating.getRcUploadedUserFile()==null )
                            rating.setRcUploadedUserFile(fileUploadFacade.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType( rcCheckId, rcRaterId, itemId, UploadedUserFileType.REF_CHECK_RATER_COMMENT.getUploadedUserFileTypeId() ) );

                        RcUploadedUserFile uuf = rating.getRcUploadedUserFile();
                        
                        // LogService.logIt( "RaterRefUtils.doSaveItemResp() DDD.1 uuf=" + (uuf==null ? "null" : "Not Null. rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + ", filename=" + uuf.getFilename() + ", conv status=" + uuf.getConversionStatusTypeId() ) );
                        
                        if( uuf==null )
                        {
                            String errKey = itm.getRcCandidateUploadType().getAudio() ? "g.UploadedCandAudioFileMissing" : "g.UploadedCandVideoFileMissing";
                            
                            if(itm.getRcCandidateUploadType().getAudioVideo() )
                                errKey = "g.UploadedCandAudioVideoFileMissing";
                            
                            validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), errKey );
                            complete = false;
                        }
                    }
                    else
                        LogService.logIt( "RaterRefUtils.doSaveItemResp() DDD.2 itm.getHideSkip()=" + itm.getHideSkip() + ", proctorBean.getCameraOptOut()=" + proctorBean.getCameraOptOut() );
                    
                }
            }


            if( skip )
            {
                if( !rating.getIsComplete() )
                {
                    complete=false;
                    rating.setScore( -2 );
                    setStringInfoMessage( MessageFactory.getStringMessage(getLocale(), "g.XRQuestionSkipped" ) );
                }
            }

            else if( itmFmt.getIsRating() )
            {
                if( itm.getIncludeNumRatingB() && (!refBean.getRefUserType().getIsCandidate() || itm.getIntParam2()<=0) )
                {
                    score = getIsMsieOrSamsungAndroid() || refBean.getRcCheck().getRcScript().getUseDiscreteRatingsB() || refBean.getAccessibleActive() ? rating.getScore() : getUnencryptedFloatFmRequest( "ratingvalue");
                    // LogService.logIt( "RaterRefUtils.doSaveItemResp() rating item. rating value selected=" + score + " comments=" + rating.getText() );
                    if( score<ratingScale.getMinScore() )
                    {
                        score=0;
                        validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), itmFmt.getKey()+".rqdmsg" );
                        complete = false;
                    }

                    if( score>ratingScale.getMaxScore()*0.985f ) // score>9.85f )
                        score=ratingScale.getMaxScore();

                    if( itm.getDenyMiddle()==1 && (score>=ratingScale.getDenyMiddleLow() && score<=ratingScale.getDenyMiddleHigh()) )
                    {
                        LogService.logIt( "RaterRefUtils.doSaveItemResp() CCCx Rater. item.denyMiddle=true and in range. score=" + score );
                        throw new STException( "g.NeutralDisallowed");
                    }

                    rating.setSelectedResponse( Float.toString( score ) );
                }

                // score=-3 indicates primary scoring system skipped because it's a candidate and primary is hidden for candidates
                else if( itm.getIncludeNumRatingB() && refBean.getRefUserType().getIsCandidate() && itm.getIntParam2()>0)
                {
                    score = -3;
                    rating.setSelectedResponse( "" );                    
                }
                
                else
                    score = -1;

                rating.setScore(score);

                if( itm.getIncludeComments()>0 )
                {
                    if( !itm.getIncludeNumRatingB() || (refBean.getRefUserType().getIsCandidate() && itm.getIntParam2()>0) )
                    {
                        rating.setScore(refBean.getRefUserType().getIsCandidate() && itm.getIntParam2()>0 ? -3 : -1);
                        rating.setSelectedResponse( "" );
                    }

                    // LogService.logIt( "RaterRefUtils.doSaveItemResp() DDD.1 Rater.rating item.  score=" + rating.getScore() );
                    // No comments in submission
                    if( rating.getRcUploadedUserFile()==null && (rating.getText()==null || rating.getText().isBlank()) && (!refBean.getRefUserType().getIsCandidate() || itm.getIntParam2()<=0) )
                    {
                        // LogService.logIt( "RaterRefUtils.doSaveItemResp() DDD.2 Rater.rating item.  ");
                        rating.setText( null );

                        if( getIsCurrentItemCommentRequiredAnyScore() ) //   !rc.getRcScript().getNoCommentsRatingItemsB() && (itm.getIncludeComments()==2 || rc.getRcScript().getAllCommentsRequiredB()) )
                        {
                            // LogService.logIt( "RaterRefUtils.doSaveItemResp() DDD.3 Rater.rating item.  ");
                            validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), itmFmt.getKey()+".commentsrqdmsg" );
                            complete = false;
                        }

                        else if( (validMessage==null || validMessage.isBlank()) && getIsCurrentItemCommentRequiredForLowScore( rating.getScore() ) )
                        {
                            // LogService.logIt( "RaterRefUtils.doSaveItemResp() DDD.4 Rater.rating item. score=" + rating.getScore() + ", raterRefBean.getRcItem().getCommentThresholdLow()=" + raterRefBean.getRcItem().getCommentThresholdLow());
                            infoMessage = MessageFactory.getStringMessage(getLocale(), itmFmt.getKey()+".commentsrqdforlowscoremsg" );
                            complete = false;
                        }
                        else if( (validMessage==null || validMessage.isBlank()) && getIsCurrentItemCommentRequiredForHighScore( rating.getScore() ) )
                        {
                            // LogService.logIt( "RaterRefUtils.doSaveItemResp() DDD.5 Rater.rating item. score=" + rating.getScore()  + ", raterRefBean.getRcItem().getCommentThresholdHigh()=" + raterRefBean.getRcItem().getCommentThresholdHigh());
                            infoMessage = MessageFactory.getStringMessage(getLocale(), itmFmt.getKey()+".commentsrqdforhighscoremsg" );
                            complete = false;
                        }
                    }
                }
            }

            else if( itmFmt.getIsCommentsOnly())
            {
                rating.setScore(refBean.getRefUserType().getIsCandidate() && itm.getIntParam2()>0 ? -3 : -1);
                rating.setSelectedResponse( "" );

                if( rating.getRcUploadedUserFile()==null && (rating.getText()==null || rating.getText().isBlank()) )
                {
                    rating.setText( null );
                    // if( !rc.getRcScript().getNoCommentsRatingItemsB() && (itm.getIncludeComments()==2 || rc.getRcScript().getAllCommentsRequiredB()) )
                    if( getIsCurrentItemCommentRequiredAnyScore() )
                    {
                        validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), itmFmt.getKey()+".commentsrqdmsg" );
                        complete = false;
                    }
                }
            }

            else if( itmFmt.getIsRadio() )
            {
                int selIdx = 0;
                // if file upload and we need to hide the radio.
                if( refBean.getRefUserType().getIsCandidate() && itm.getIntParam2()>0 )
                {
                    score = -3;
                    rating.setSelectedResponse( "" );
                }

                else
                {
                    selIdx = raterRefBean.getSelectedRadioIndex();
                    if( selIdx<1 )
                    {
                        validMessage = MessageFactory.getStringMessage(getLocale(), itmFmt.getKey()+ ".rqdmsg" );
                        score = itm.getIsItemScored() ? 0 : -1;
                        complete = false;
                    }
                    else
                        score = itm.getIsItemScored() ? itm.getScoreForIndex(selIdx) : -1;

                    // LogService.logIt( "RaterRefUtils.doSaveItemResp() radio item. selected radio index=" + selIdx );
                    rating.setSelectedResponse( selIdx>0 ? Integer.toString( selIdx ) : "" );
                }

                rating.setScore(score);

                if( RcCheckUtils.isContactPermissionItem(itm.getRcItemId()) )
                {
                    rc.getRcRater().setContactPermissionTypeId( selIdx==1 ? RcContactPermissionType.YES.getRcContactPermissionTypeId() : RcContactPermissionType.NO.getRcContactPermissionTypeId() );

                    if( !refBean.getAdminOverride() )
                    {
                        if( rcFacade==null )
                            rcFacade=RcFacade.getInstance();
                        rcFacade.saveRcRater(rc.getRcRater(), true);
                    }
                }

                else if( RcCheckUtils.isRecruitingPermissionItem(itm.getRcItemId()) )
                {
                    rc.getRcRater().setRecruitingPermissionTypeId( selIdx==1 ? RcContactPermissionType.YES.getRcContactPermissionTypeId() : RcContactPermissionType.NO.getRcContactPermissionTypeId() );
                    if( !refBean.getAdminOverride() )
                    {
                        if( rcFacade==null )
                            rcFacade=RcFacade.getInstance();
                        rcFacade.saveRcRater(rc.getRcRater(), true);

                        // OK to contact as a referral
                        if( selIdx==1 )
                            createReferralForRater(rc, rc.getRcRater(), rc.getRcRater().getUser(), rating.getText() );
                    }
                }

                if( itm.getIncludeComments()>0 )
                {
                    if( rating.getRcUploadedUserFile()==null && (rating.getText()==null || rating.getText().isBlank()) && (!refBean.getRefUserType().getIsCandidate() || itm.getIntParam2()<=0) )
                    {
                        rating.setText( null );

                        if( getIsCurrentItemCommentRequiredAnyScore() ) //   !rc.getRcScript().getNoCommentsRatingItemsB() && (itm.getIncludeComments()==2 || rc.getRcScript().getAllCommentsRequiredB()) )
                        {
                            validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), itmFmt.getKey()+".commentsrqdmsg" );
                            complete = false;
                        }
                        else if(  (validMessage==null || validMessage.isBlank()) && getIsCurrentItemCommentRequiredForLowScore( rating.getScore() ) )
                        {
                            infoMessage = MessageFactory.getStringMessage(getLocale(), itmFmt.getKey()+".commentsrqdforlowscoremsg" );
                            complete = false;
                        }
                        else if( (validMessage==null || validMessage.isBlank()) && getIsCurrentItemCommentRequiredForHighScore( rating.getScore() ) )
                        {
                            infoMessage = MessageFactory.getStringMessage(getLocale(), itmFmt.getKey()+".commentsrqdforhighscoremsg" );
                            complete = false;
                        }

                    }
                }

            }

            else if( itmFmt.getIsButton())
            {
                int selIdx = raterRefBean.getSelectedRadioIndex();
                score = itm.getIsItemScored() ? itm.getScoreForIndex(selIdx) : -1;
                // LogService.logIt( "RaterRefUtils.doSaveItemResp() button item. selected index=" + selIdx );
                rating.setSelectedResponse( Integer.toString( selIdx ) );
                rating.setScore(score);

                // always complete
                // complete = true;
            }

            else if( itmFmt.getIsCheckbox())
            {
                int[] selVals = null;
                
                String s;

                if( refBean.getRefUserType().getIsCandidate() && itm.getIntParam2()>0 )
                {
                    score = -3;
                }

                else
                {
                    String[] slvs = raterRefBean.getSelectedCheckboxesStr();
                    // LogService.logIt( "RaterRefUtils.doSaveItemResp() multicheckbox found " + (slvs==null ? "null" : slvs.length ) + " selected values." );
                    selVals = new int[slvs.length];
                    score = itm.getIsItemScored() ? 0 : -1;

                    for( int i=0; i<slvs.length;i++ )
                    {
                        s = slvs[i];
                        if( s==null || s.isBlank() )
                            continue;
                        selVals[i] = Integer.parseInt(s);
                    }
                }

                if( selVals==null || selVals.length<=0 )
                {
                    rating.setSelectedResponse("");
                }
                else
                {
                    StringBuilder sb = new StringBuilder();
                    for( int v : selVals )
                    {
                        if( sb.length()>0 )
                            sb.append(",");
                        sb.append( v );

                        if( itm.getIsItemScored() )
                            score += itm.getScoreForIndex( v );

                    }
                    rating.setSelectedResponse(sb.toString());
                    if( score>ratingScale.getMaxScore() )
                        score=ratingScale.getMaxScore();
                    rating.setScore(score);
                }

                // always complete unless comments required below
                // complete = true;

                if( itm.getIncludeComments()>0 )
                {
                    if( rating.getRcUploadedUserFile()==null && (rating.getText()==null || rating.getText().isBlank()) && (!refBean.getRefUserType().getIsCandidate() || itm.getIntParam2()<=0) )
                    {
                        rating.setText( null );

                        // if( (itm.getIncludeComments()==2 || rc.getRcScript().getAllCommentsRequiredB()) )
                        if( getIsCurrentItemCommentRequiredAnyScore() )
                        {
                            validMessage = (validMessage==null || validMessage.isBlank() ? "" : validMessage + " ") +  MessageFactory.getStringMessage(getLocale(), itmFmt.getKey()+".commentsrqdmsg" );
                            complete = false;
                        }
                    }
                }
            }


            // Validation issue.
            if( !goBack && !skip && validMessage!=null && !validMessage.isBlank() )
            {
                //if( !goBack )
                //{
                this.setStringErrorMessage(validMessage);
                return "StayInSamePlace";
                //}
            }

            // Info message
            else if( !goBack && !skip && infoMessage!=null && !infoMessage.isBlank() )
            {
                    this.setStringInfoMessage(infoMessage);
                    return "StayInSamePlace";
            }

            // Can save.
            else
            {
                if( complete || skip )
                {
                    if( rating.getCompleteDate()==null )
                        rating.setCompleteDate(new Date() );

                    rating.setRcRatingStatusTypeId( skip ? RcRatingStatusType.SKIPPED.getRcRatingStatusTypeId() : RcRatingStatusType.COMPLETE.getRcRatingStatusTypeId() );
                }
                else
                    rating.setRcRatingStatusTypeId( RcRatingStatusType.INCOMPLETE.getRcRatingStatusTypeId() );

                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();

                // Save it
                if( !refBean.getAdminOverride() )
                {
                    // if new, check for existing rater id.
                    //if( rating.getRcRatingId()<=0 )
                    //{
                    //    RcRating r2 = rcFacade.getRcRatingForRcRaterAndRcItem( rating.getRcRaterId(), rating.getRcItemId() );
                    //    if( r2!=null )
                    //    {
                    //        LogService.logIt( "RaterRefUtils.doSaveItemResp() Saving a new RcRating but found existing RcRating with rcRatingId=" + r2.getRcRatingId() + " for this RcRater and RcItem. ir.getRcRaterId()=" + rating.getRcRaterId() + ", ir.getRcItemId()=" + rating.getRcItemId() + ", overWriting." );
                    //        rating.setRcRatingId( r2.getRcRatingId() );
                    //     }
                    // }

                    saveRcRatingWithCheck(rating);
                }

                // Update if moving on
                if( complete || skip )
                {
                    rc.getRcRater().setPercentComplete( computeRaterPercentComplete() );

                    if( rc.getRcRater().getPercentComplete()>=100 )
                    {
                        if( rcCheckUtils==null )
                            rcCheckUtils = new RcCheckUtils();
                        // complete the RcRater
                        boolean isComplete = rcCheckUtils.performRcRaterCompletionIfReady(rc, rc.getRcRater(), refBean.getAdminOverride() );
                        
                        if( !isComplete )
                        {
                            LogService.logIt( "RaterRefUtils.doSaveItemResp() WWW.1 NONFATAL ERROR. percentcomplete is 100 but RcRater is not complete. Will send to next view from ratings.  rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + (rc.getRcRater()==null ? "null" : rc.getRcRater().getRcRaterId()) );                    
                        }
                    }
                    
                    if( !refBean.getAdminOverride() )
                        rcFacade.saveRcRater(rc.getRcRater(), true );

                    if( rcCheckUtils==null )
                        rcCheckUtils = new RcCheckUtils();

                    if( !refBean.getAdminOverride() )
                    {
                        rc.setPercentComplete(rcCheckUtils.computeRcCheckPercentComplete( rc ));
                        rcFacade.saveRcCheck(rc, false);
                    }

                    Tracker.addItemResponse();
                    if( skip )
                        Tracker.addItemSkip();
                    else
                        Tracker.addItemAnswer();
                }
            }

            RcItemWrapper rciwNew;

            if( goBack )
                rciwNew = getPreviousRcItemWrapper();
            else
            {
                rciwNew = getNextRcItemWrapper();
            }

            // LogService.logIt( "RaterRefUtils.doSaveItemResp() goBack=" + goBack + ", rciwNew=" + (rciwNew==null ? "null" : "not null, rcItemId=" + rciwNew.getRcItemId()) );

            if( goBack && (rciwNew==null || rciwNew.getRcItemId()==itm.getRcItemId() ) )
            {

                if( rc.getRcRater().getIsCandidateOrEmployee() && rc.getRcScript().getHasAnyCandidateInput() )
                {
                    CandidateRefUtils cru = CandidateRefUtils.getInstance();
                    return cru.processGoBackToLastCandidateInputQuestion();
                }
                else
                    throw new Exception( "goBack=true but Previous RcItemWrapper is null or new rcItemWrapper is the same as the current rcItemWrapper. rcItemWrapper=" + (rciwNew==null ? "null" : rciwNew.toString()) );
            }

            // if rciwNew is null, we think it should be done.
            else if( rciwNew==null )
            {
                rciwNew = getFirstRcItemWrapper( true );
                // LogService.logIt( "RaterRefUtils.doSaveItemResp()Target (Next) RcItemWrapper is null. First unanswered RcItemWrapper is " + (rciwNew==null ? "null" : "rcItemId=" + rciwNew.getRcItemId() + " displayOrder=" + rciwNew.getDisplayOrder()) );

                // complete! // 11-9-2020 Moved to MarkCompleteAndExit method so person can go back if not candidte or employee.
               if( rciwNew==null && rc.getRcRater().getIsCandidateOrEmployee() )
                {
                    if( rcCheckUtils==null )
                        rcCheckUtils = new RcCheckUtils();
                    // complete the RcRater
                    boolean isComplete = rcCheckUtils.performRcRaterCompletionIfReady(rc, rc.getRcRater(), refBean.getAdminOverride() );

                    if( !isComplete )
                        LogService.logIt( "RaterRefUtils.doSaveItemResp() XXX.1 NONFATAL ERROR. next RcItemWrapper is null, but Candidate/Employee RcRater is not complete. Will send to next view from ratings.  rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + (rc.getRcRater()==null ? "null" : rc.getRcRater().getRcRaterId() + ", rcRater.percentComplete=" + rc.getRcRater().getPercentComplete()) );                    
                    else
                    {
                        CandidateRefUtils cru = CandidateRefUtils.getInstance();
                        return cru.doCompleteSelfRatings();                        
                    }
                }

               else if( rciwNew==null && !rc.getRcRater().getIsCandidateOrEmployee() )
                {
                    if( rcCheckUtils==null )
                        rcCheckUtils = new RcCheckUtils();
                    // complete the RcRater
                    boolean isComplete = rcCheckUtils.performRcRaterCompletionIfReady(rc, rc.getRcRater(), refBean.getAdminOverride() );

                    if( !isComplete )
                        LogService.logIt( "RaterRefUtils.doSaveItemResp() XXX.2 NONFATAL ERROR. next RcItemWrapper is null, but Candidate/Employee RcRater is not complete. Will send to next view from ratings.  rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ) + ", rcRaterId=" + (rc.getRcRater()==null ? "null" : rc.getRcRater().getRcRaterId()) );

                    if( !refBean.getAdminOverride() && (rc.getRcRater().getRcRaterStatusType().getIsComplete() ) )
                    {
                        rcCheckUtils.loadRcCheckForScoringOrResults(rc);
                        rcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete( rc, rc.getRcRater(), false);
                    }
                }
               
                // use the completed survey as a reason to check for candidate completion if this is not the candidate.
                else if( rciwNew==null && !rc.getRcRater().getIsCandidateOrEmployee() && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
                {
                    rcCheckUtils.performRcCandidateCompletionIfReady(rc, refBean.getAdminOverride() );
                }
            }

            raterRefBean.setRcItemWrapper(rciwNew, rc.getRcRater().getIsCandidateOrEmployee() );

            return getNextViewFromRatings();
        }
        catch( STException e )
        {
            setMessage(e);
            return "StayInSamePlace";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RaterRefUtils.doSaveItemResp() rcCheckId="  + (rc==null ? "null" : rc.toStringShort() ));
            setMessage( e );
            return systemError(rc==null ? null : rc.getOrg(), CorpBean.getInstance().getCorp(), e.toString() , null, null, rc, rc==null ? null : rc.getRcRater(), true );
        }
    }

    private RcUploadedUserFile saveUploadedUserFile( RcCheck rc, RcRater rater, RcRating rating, FileContentType fct, FileUploadFacade fuf ) throws Exception
    {
        try
        {
            if( rc==null )
                throw new Exception( "RcCheck is null" );

            if( rating==null )
                throw new Exception( "RcRating is null" );

            if( rating==null )
                throw new Exception( "RcRating is null" );

            if( uploadedFile==null )
                throw new Exception( "UploadedFile is null" );

            if( uploadedFile.getSize()<=1 )
                throw new Exception( "UploadedFile.fileSize too small: " + uploadedFile.getSize() );

            if( uploadedFile.getSize()>Constants.MAX_FILE_UPLOAD_SIZE  )
                throw new Exception( "UploadedFile.fileSize too big: " + uploadedFile.getSize() );

            if( fct==null )
                throw new Exception( "FileContentType is null" );

            InputStream strm = uploadedFile.getInputStream();
            if( strm==null )
                throw new Exception( "UploadedFile.inputStream is null. File size=" + uploadedFile.getSize() );

            if( fuf==null )
                fuf = FileUploadFacade.getInstance();

            FileXferUtils xfer = new FileXferUtils(); //  FileXferUtils.getInstance();

            RcUploadedUserFile uuf = rating.getCandidateRcUploadedUserFile();
            if( uuf==null && rating.getCandidateUploadedUserFileId()>0 )
            {
                rating.setCandidateRcUploadedUserFile( fuf.getRcUploadedUserFile(rating.getCandidateUploadedUserFileId()));
                uuf = rating.getCandidateRcUploadedUserFile();
            }

            if( uuf==null )
            {
                uuf = new RcUploadedUserFile();
                uuf.setRcCheckId( rating.getRcCheckId() );
                uuf.setRcItemId( rating.getRcItemId() );
                uuf.setRcRaterId( rating.getRcRaterId() );
                uuf.setRcRatingId( rating.getRcRatingId());
                uuf.setUserId( rater.getUserId() );
                uuf.setUploadedUserFileTypeId( UploadedUserFileType.REF_CHECK_CANDIDATE_FILE_UPLOAD.getUploadedUserFileTypeId());
                uuf.setCreateDate(new Date());
                uuf.setConversionStatusTypeId( ConversionStatusType.NA.getConversionStatusTypeId() );
                uuf.setInitialFileStatusTypeId( 0 );
                uuf.setFileProcessingTypeId(UploadedFileProcessingType.NONE.getUploadedFileProcessingTypeId() );
            }

            String ext = fct.getBaseExtension();
            String filename = "candidate-upload-" + rating.getRcCheckId() + "-" + rating.getRcRaterId() + "-" + rating.getRcItemId() + "." + ext;
            uuf.setInitialFilename( uploadedFile.getFileName());
            uuf.setInitialMime(uploadedFile.getContentType());
            uuf.setInitialFileContentTypeId( fct.getFileContentTypeId());
            uuf.setInitialFileSize( (int) uploadedFile.getSize() );
            uuf.setFilename(filename);
            uuf.setFileSize((int) uploadedFile.getSize());
            uuf.setFileContentTypeId(fct.getFileContentTypeId());
            uuf.setR1( rc.getOrgId() );
            uuf.setR2(rating.getRcCheckId());
            uuf.setLastUpload( new Date() );

            String directory = uuf.getDirectory();
            BucketType bt = RuntimeConstants.getBooleanValue( "useAwsTestFoldersForProctoring" ) ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING;
            // BucketType bt = BucketType.PROCTORRECORDING;

            if( !refBean.getAdminOverride() )
            {
                FileXferUtils.init();
                xfer.saveFile( directory, filename, strm, fct.getBaseContentType(), (int)uploadedFile.getSize(), bt.getBucketTypeId(), true );

                fuf.saveRcUploadedUserFile(uuf);
            }

            rating.setCandidateUploadedUserFileId( uuf.getRcUploadedUserFileId() );
            rating.setCandidateRcUploadedUserFile(uuf);
            
            if( rating.getRcRatingId()>0 )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcFacade.saveRcRating(rating);
            }

            LogService.logIt("RaterRefUtils.saveUploadedUserFile() Successfully saved Candidate Uploaded File initial filename=" + uploadedFile.getFileName() + ", mime=" + fct.getBaseContentType() +", size=" + uuf.getFileSize() + ", saved filename=" + uuf.getFilename() +", rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + "rcItemId=" + rating.getRcItemId() + ", rcRaterId=" + rating.getRcRaterId() + ", rcCheckId=" + rating.getRcCheckId() );
            Tracker.addCandidateFileUpload();
            return uuf;
        }
        catch( Exception e )
        {
            Tracker.addCandidateFileUploadError();
            LogService.logIt(e, "RaterRefUtils.saveUploadedUserFile() " + (rating==null ? "RcRating is null" : "rcItemId=" + rating.getRcItemId() + ", rcRaterId=" + rating.getRcRaterId() + ", rcCheckId=" + rating.getRcCheckId()) );
            throw e;
        }
    }

    public RcRating saveRcRatingWithCheck( RcRating r ) throws Exception
    {
        if( rcFacade==null )
            rcFacade=RcFacade.getInstance();

        // neeed to move this outside of the transaction.
        if( r.getRcRatingId()<=0 )
        {
            RcRating r2 = rcFacade.getRcRatingForRcRaterAndRcItem(r.getRcRaterId(), r.getRcItemId() );
            if( r2!=null )
            {
                LogService.logIt( "RaterRefUtils.saveRcRating() Saving a new RcRating but found existing RcRating with rcRatingId=" + r2.getRcRatingId() + " for this RcRater and RcItem. ir.getRcRaterId()=" + r.getRcRaterId() + ", ir.getRcItemId()=" + r.getRcItemId() + ", overWriting." );
                r.setRcRatingId(r2.getRcRatingId() );
            }
        }

        return rcFacade.saveRcRating(r);
    }



    public String getItemInfoTextXhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getInfoText() );
    }

    public String getItemQuestionCandidateXhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        if( raterRefBean.getRcItemWrapper().getRcItem().getQuestionCandidate()==null || raterRefBean.getRcItemWrapper().getRcItem().getQuestionCandidate().isBlank() )
            return getItemQuestionXhtml();
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getQuestionCandidate() );
    }
    public String getItemQuestionXhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getQuestion() );
    }
    public String getItemChoice1Xhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getChoice1());
    }
    public String getItemChoice2Xhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getChoice2());
    }
    public String getItemChoice3Xhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getChoice3());
    }
    public String getItemChoice4Xhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getChoice4());
    }
    public String getItemChoice5Xhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getChoice5());
    }
    public String getItemChoice6Xhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getChoice6());
    }
    public String getItemChoice7Xhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getChoice7());
    }
    public String getItemChoice8Xhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getChoice8());
    }
    public String getItemChoice9Xhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getChoice9());
    }
    public String getItemChoice10Xhtml()
    {
        if( raterRefBean.getRcItemWrapper()==null || raterRefBean.getRcItemWrapper().getRcItem()==null )
            return "";
        return getUserTextXhtml( raterRefBean.getRcItemWrapper().getRcItem().getChoice10());
    }


    private boolean createReferralForRater( RcCheck rc, RcRater rater, User u, String textNotes) throws Exception
    {
        try
        {

            //if( rater.getRcReferralList()==null )
           // {

            // always reload referrals for rater.

            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();

            rater.setRcReferralList(rcFacade.getRcReferralList( rater.getRcCheckId(), rater.getRcRaterId() ));

            for( RcReferral r : rater.getRcReferralList() )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                r.setUser( userFacade.getUser( r.getUserId() ));
            }
            //}

            for( RcReferral r : rater.getRcReferralList() )
            {
                if(r.getUserId()==u.getUserId())
                    return false;
            }

            RcReferral rfrl = new RcReferral();
            rfrl.setRcCheckId(rater.getRcCheckId());
            rfrl.setRcRaterId(rater.getRcRaterId());
            rfrl.setUserId(u.getUserId());
            rfrl.setReferrerUserId(rater.getUserId());
            rfrl.setRcReferralTypeId(u.getUserId()==rater.getUserId() ? 0 : 1);
            rfrl.setOrgId(rater.getOrgId());
            rfrl.setCreateDate( new Date() );
            rfrl.setLastUpdate( rfrl.getCreateDate() );
            rfrl.setRcScriptId( rc.getRcScriptId());
            if( rc.getRcScript()!=null )
                rfrl.setTargetRole(rc.getRcScript().getName() );
            if( textNotes!=null && !textNotes.isBlank() )
                rfrl.setReferrerNotes(StringUtils.removeAllControlChars(textNotes));
            else
                rfrl.setReferrerNotes(null);


            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();

            rcFacade.saveRcReferral( rfrl );

            rfrl.setUser(u);

            rater.getRcReferralList().add(rfrl );

            if( u.getUserId()==rater.getUserId() )
                Tracker.addSelfReferral();

            else
                Tracker.addExtraReferral();

            return true;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RaterRefUtils.createReferralForRater() " + rater.toString() );
            throw e;
        }
    }

    private boolean createReferral( RcCheck rc, RcRater rater, String firstName, String lastName, String email, String phone, String textNotes) throws Exception
    {
        try
        {
            if( (lastName==null || lastName.isBlank()) || (firstName==null || firstName.isBlank()) )
            {
                LogService.logIt( "RaterRefUtils.createReferral() Referral has no name info. Ignoring." );
                return false;
            }

            if( phone!=null && !phone.isBlank() && !GooglePhoneUtils.isNumberValid(phone, rater.getUser().getCountryCode() ) )
            {
                LogService.logIt("RaterRefUtils.createReferral() Phone number (" + phone + ") not valid for country code=" + rater.getUser().getCountryCode() + ".  Ignoring phone." );
                phone=null;
            }


            if( (email==null || email.isBlank()) && (phone==null || phone.isBlank()) )
            {
                LogService.logIt( "RaterRefUtils.createReferral() Referral has no email or phone. Ignoring." );
                return false;
            }

            if( userFacade==null )
                userFacade = UserFacade.getInstance();

            if( email==null || email.isBlank() )
            {
                email = "{" + StringUtils.generateRandomString(28) + "}";

                while( userFacade.getUserByEmailAndOrgId(email, rc.getOrgId() )!=null  )
                {
                    email = "{" + StringUtils.generateRandomString(28) + "}";
                }
            }

            User u = userFacade.getUserByEmailAndOrgId(email, rc.getOrgId() );
            if( u!=null )
            {
                boolean save = false;
                if( EmailUtils.validateEmailNoErrors(email) && !EmailUtils.validateEmailNoErrors(u.getEmail()) )
                {
                      u.setEmail(email);
                      save=true;
                }

                if( phone!=null && !phone.isBlank() && (u.getMobilePhone()==null || u.getMobilePhone().isBlank()) )
                {
                      u.setMobilePhone(phone);
                      save=true;
                }

                if( save )
                     userFacade.saveUser(u,false );
            }
            else
            {
                u = new User();
                u.setOrgId( rater.getOrgId() );
                u.setSuborgId( rc.getSuborgId() );
                u.setRoleId( RoleType.NO_LOGON.getRoleTypeId() );
                u.setUserTypeId( UserType.NAMED.getUserTypeId() );
                u.setCountryCode( rater.getUser().getCountryCode() );
                u.setCreateDate( new Date() );
                u.setLocaleStr( rc.getLangCode());
                u.setTimeZoneId( rater.getUser().getTimeZoneId() );
                u.setPassword(StringUtils.generateRandomString(12 ));
                u.setUsername(StringUtils.generateRandomString(30 ));
                u.setFirstName( firstName );
                u.setLastName( lastName );
                u.setEmail(email);

                if( phone!=null && !phone.isBlank() )
                    u.setMobilePhone(GooglePhoneUtils.getFormattedPhoneNumberIntl(phone, u.getCountryCode() ));

                int count = 0;
                while( count<100 && userFacade.getUserByUsername( u.getUsername())!=null )
                {
                    u.setUsername( StringUtils.generateRandomString(30 ));
                    count++;
                }

                u = userFacade.saveUser(u, true );
            }

            return createReferralForRater(rc, rater, u, textNotes );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RaterRefUtils.createReferralForRater() " + rc.toString() + ", " + rater.toString() );
            throw e;
        }
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }




}
