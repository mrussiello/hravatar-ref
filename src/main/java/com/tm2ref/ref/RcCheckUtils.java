/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.api.ResultPoster;
import com.tm2ref.api.ResultPosterFactory;
import com.tm2ref.entity.event.TestEvent;
import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcItem;
import com.tm2ref.entity.ref.RcOrgPrefs;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.ref.RcRating;
import com.tm2ref.entity.ref.RcScript;
import com.tm2ref.entity.ref.RcSuspiciousActivity;
import com.tm2ref.entity.user.User;
import com.tm2ref.event.EventFacade;
import com.tm2ref.event.TestKeyStatusType;
import com.tm2ref.file.BucketType;
import com.tm2ref.file.FileContentType;
import com.tm2ref.file.FileUploadFacade;
import com.tm2ref.file.FileXferUtils;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.NumberUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.previousresult.PreviousResult;
import com.tm2ref.previousresult.PreviousResultDateComparator;
import com.tm2ref.proctor.ProctorUtils;
import com.tm2ref.purchase.ProductType;
import com.tm2ref.purchase.RefCreditUtils;
import com.tm2ref.ref.ai.RcCheckAiScoresUpdater;
import com.tm2ref.ref.ai.RcRaterAiStatusType;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.ResumeHelpUtils;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.StringUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 *
 * @author miker_000
 */
public class RcCheckUtils {
    
    private static List<Integer> rcItemIdsForContactPermission;
    private static List<Integer> rcItemIdsForRecruiting;
    private static List<Integer> rcItemIdsForPriorRole;
    private static List<Integer> rcItemIdsForReferralsRequest;
    
    UserFacade userFacade;
    RcFacade rcFacade;
    RcScriptFacade rcScriptFacade;
    EventFacade eventFacade;
        
    private static String[] ALPHABET = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    
    private static synchronized void init()
    {
        if( rcItemIdsForRecruiting!=null )
            return;
        try
        {
            rcItemIdsForContactPermission = RuntimeConstants.getIntList("RefCheckContactPermissionRcItemIds", ",");
            rcItemIdsForRecruiting = RuntimeConstants.getIntList("RefCheckContactRecruitingRcItemIds", ","); 
            rcItemIdsForPriorRole = RuntimeConstants.getIntList("RefCheckPriorRoleRcItemIds", ","); 
            rcItemIdsForReferralsRequest = RuntimeConstants.getIntList("RefCheckReferralsRequestRcItemIds", ","); 
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RcCheckUtils.init()" );            
        }
    }
    
    
    
    
    
    
    public static boolean hasUnconvertedCandidateMediaForRaterReview( RcCheck rc, RcFacade rcFacade ) throws Exception
    {
        try
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            
            if( rc.getRcRaterList()==null )
                rc.setRcRaterList( rcFacade.getRcRaterList(rc.getRcCheckId()));
                        
            List<RcRating> rcrl = rcFacade.getRcRatingList(rc.getRcCheckId(), rc.getCandidateRcRaterId() );

            if( rcrl==null || rcrl.isEmpty() )
                return false;
            
            RcScriptFacade rcScriptFacade=RcScriptFacade.getInstance();
            RcItem rci;
            FileUploadFacade fuf = null;
            for( RcRating rcRating : rcrl )
            {
                rci = rcScriptFacade.getRcItem( rcRating.getRcItemId(), false, false);
                if( rci.getShowCandRespToRater()<=0 || !rci.getHasAvCandidateFileUpload() )
                    continue;
                if( rcRating.getUploadedUserFileId()>0 )
                {
                    if( fuf==null )
                        fuf=FileUploadFacade.getInstance();
                    rcRating.setRcUploadedUserFile( fuf.getRcUploadedUserFile( rcRating.getUploadedUserFileId()));
                    if( rcRating.getRcUploadedUserFile()!=null && rcRating.getRcUploadedUserFile().getHasRecordingInConversion() )
                    {
                        return true;
                    }
                }
            }
            return false;
            
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcCheckUtils.hasUnconvertedCandidateMediaForRaterReview() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) );
            throw e;
        }
    }
    
    public static String getUploadedFileUrl( RcUploadedUserFile uploadedUserFile )
    {
        // String thumbUrl = null;
        UploadedUserFileType uft = uploadedUserFile.getUploadedUserFileType();
        BucketType bt = RuntimeConstants.getBooleanValue("useAwsTestFoldersForRefUploads") ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING;;
        boolean aws = RuntimeConstants.getBooleanValue("useAwsForUploadedRefMedia");

        String dir = uploadedUserFile.getDirectory();
        if( dir.startsWith("/") )
            dir = dir.substring(1, dir.length() );
        
        if( aws )
        {
            if( RuntimeConstants.getBooleanValue("useAwsTempUrlsForMedia") )
            {
                try
                {
                    return FileXferUtils.getPresignedUrlAws( dir, uploadedUserFile.getFilename(), bt.getBucketTypeId(), null, RuntimeConstants.getIntValue( "awsTempUrlMinutes") );
                }
                catch( Exception e )
                {
                    LogService.logIt( e, "RcCheckUtils.getUploadedFileUrl() dir=" + dir + ", filename=" + uploadedUserFile.getFilename()  );
                    return "";
                }
            }

            // Normal Method.
            return RuntimeConstants.getStringValue( "awsS3BaseUrl") + bt.getBucket() + "/" + bt.getBaseKey() + dir + "/" + uploadedUserFile.getFilename();
        }
        
        // Not AWS
        return RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps") + "/" + dir + "/" + uploadedUserFile.getFilename();           
    }
    
    public static String getUploadedFileIconFilename( RcUploadedUserFile uploadedUserFile)
    {
        if( uploadedUserFile==null )
            return null;
        
        FileContentType fct = uploadedUserFile.getFileContentType();
        
        if( fct.equals( FileContentType.DOCUMENT_DOC) || fct.equals( FileContentType.DOCUMENT_DOCX ) )
            return "word_icon_ta_70.png";
        
        if( fct.equals( FileContentType.DOCUMENT_XLS) || fct.equals( FileContentType.DOCUMENT_XLSX ) || fct.equals( FileContentType.TEXT_CSV ) )
            return "excel_Icon_ta_70.png";
        
        if( fct.equals( FileContentType.DOCUMENT_PPT) || fct.equals( FileContentType.DOCUMENT_PPTX ) )
            return "ppt-icon_ta_70.png";
        
        if( fct.equals( FileContentType.DOCUMENT_PDF)  )
            return "pdf_icon_ta_70.png";

        if( fct.equals( FileContentType.ARCHIVE_ZIP)  )
            return "zip_icon_ta_70.png";
               
        return "document_icon_ta_70.png";
    }
    
    public static String getUploadedFileTypeName( Locale locale, RcUploadedUserFile uploadedUserFile )
    {
        if( uploadedUserFile==null )
            return null;
        
        FileContentType fct = uploadedUserFile.getFileContentType();
        
        return fct==null ? "" : fct.getBaseExtension().toUpperCase();        
    }

        
    
    
    public static boolean isObsDateValid( Date d )
    {
        if( d==null )
            return false;
        
        Calendar cal = new GregorianCalendar();
        int currentYear = cal.get(Calendar.YEAR);
        cal.setTime(d);
        int dateYear = cal.get(Calendar.YEAR);
        if( dateYear>currentYear || dateYear<1900 )
            return false;
        int month = cal.get(Calendar.MONTH );
        
        if( month>=12 )
            return false;
        
        return true;
    }
    
    private static String getLetterCode( int idx )
    {
        if( idx<0 )
            idx=0;
        
        if( idx<ALPHABET.length )
            return ALPHABET[idx];
        
        int m = (idx / ALPHABET.length) - 1;
        int n = (idx % ALPHABET.length);
        
        return ALPHABET[m] + ALPHABET[n];        
    }
    
    /**
     * /ta/rcpdfdnld.pdf?rc=rcCheckIdEncrypted&r=reportId&l=langCode
     * @param rc
     * @param reportId
     * @param langStr
     * @return 
     */
    public String getPdfDownloadUrl( RcCheck rc, int reportId, String langStr )
    {
        if( rc==null )
            return "";
        
        String u = RuntimeConstants.getStringValue( "RcPdfDownloadUrl" ) + "?rc=" + rc.getRcCheckIdEncrypted();
        if( reportId>0 )
            u += "&r=" + reportId;
        if( langStr!=null && !langStr.isBlank() )
            u += "&l=" + langStr;
        return u;
    }

    public List<RcItemWrapper> getLowScoringRcItems( RcCheck rc, int maxQuestions ) throws Exception
    {
        RcRatingScaleType rst = rc.getRcScript().getRcRatingScaleType();
        return getRcItemsSubList( rc, false, rc.getRcCheckType().getIsPrehire() ? rst.getMaxLowRatedItemScore() : rst.getMaxLowRatedItemScore(), maxQuestions );
    }
    
    public List<RcItemWrapper> getHighScoringRcItems( RcCheck rc, int maxQuestions ) throws Exception
    {
        RcRatingScaleType rst = rc.getRcScript().getRcRatingScaleType();
        return getRcItemsSubList( rc, true, rc.getRcCheckType().getIsPrehire() ? rst.getMinHighRatedItemScore() : rst.getMinHighRatedItemScore(), maxQuestions );
    }
    
    
    public List<RcItemWrapper> getRcItemsSubList( RcCheck rc, boolean high, float cutoff, int maxQuestions ) throws Exception
    {
        List<RcItemWrapper> out = new ArrayList<>();
        
        if( !rc.getRcCheckStatusType().getIsComplete() )
            return out;
        
        if( rc.getRcScript()==null || rc.getRcRaterList()==null )
            loadRcCheckForScoringOrResults(rc);
        
        RcTopBottomSrcType srcTyp = RcTopBottomSrcType.getValue( rc.getTopBottomSrcTypeId() );
        
        float scoreToUse;
        
        for( RcCompetencyWrapper rcw : rc.getRcScript().getRcCompetencyWrapperList() )
        {
            for( RcItemWrapper rciw : rcw.getRcItemWrapperList() )
            {
                if( !rciw.getHasRatingInfoToShow() )
                    continue;

                if( srcTyp.getIsAll() )
                    scoreToUse = rciw.getAverageScore(null);

                else if( srcTyp.getIsOthers() )
                    scoreToUse = rciw.getScoreAvgNoCandidate();

                else
                    scoreToUse = rciw.getScoreCandidate( rc.getCandidateRcRaterId() );
                
                // not scored.
                if( scoreToUse<=0 )
                    continue;

                // above
                if( high && scoreToUse<cutoff )
                    continue;


                // below
                if( !high && scoreToUse>cutoff )
                    continue;

                rciw.setQuestionWithSubs( StringUtils.replaceStr( rciw.getRcItem().getQuestion(), "[CANDIDATENAME]", rc.getUser().getFullname()));
                
                out.add( rciw );                                
            }        
        }
        
        // ALWAYS sort high-to-low
        Collections.sort( out, new RcItemWrapperScoreComparator(true, srcTyp, rc.getCandidateRcRaterId()) );
        
        if( maxQuestions>0 && out.size()>maxQuestions )
        {
            // if we want the top, take the top X values.
            if( high )
                out = out.subList(0, maxQuestions);
            
            // not high. 
            // since we want the bottom, take the bottom X values
            else
                out = out.subList( out.size()-maxQuestions, out.size() );
        }
        
        return out;
    }
    
    
    
    public List<RcCompetencyWrapper> getLowScoringRcCompetencies( RcCheck rc, int maxQuestions ) throws Exception
    {
        RcRatingScaleType rst = rc.getRcScript().getRcRatingScaleType();
        return getRcCompetenciesSubList( rc, false, rc.getRcCheckType().getIsPrehire() ? rst.getMaxLowRatedCompScore() : rst.getMaxLowRatedCompScore360(), maxQuestions, false );
    }
    
    public List<RcCompetencyWrapper> getHighScoringRcCompetencies( RcCheck rc, int maxQuestions ) throws Exception
    {
        RcRatingScaleType rst = rc.getRcScript().getRcRatingScaleType();
        return getRcCompetenciesSubList( rc, true, rc.getRcCheckType().getIsPrehire() ? rst.getMinHighRatedCompScore() : rst.getMinHighRatedCompScore360(), maxQuestions, false );
    }
        
    public List<RcCompetencyWrapper> getRcCompetenciesForInterviewQuestions( RcCheck rc, int maxQuestions ) throws Exception
    {
        return getRcCompetenciesSubList( rc, false, rc.getRcScript().getRcRatingScaleType().getMaxLowRatedCompScore(), maxQuestions, true );
    }
    
    public List<RcCompetencyWrapper> getRcCompetenciesSubList( RcCheck rc, boolean high, float cutoff, int maxQuestions, boolean hasInterviewOnly ) throws Exception
    {
        List<RcCompetencyWrapper> out = new ArrayList<>();
        
        if( !rc.getRcCheckStatusType().getIsComplete() )
            return out;
        
        if( rc.getRcScript()==null || rc.getRcRaterList()==null )
            this.loadRcCheckForScoringOrResults(rc);
        
        RcTopBottomSrcType srcTyp = RcTopBottomSrcType.getValue( rc.getTopBottomSrcTypeId() );
        float scoreToUse;
        
        for( RcCompetencyWrapper rcw : rc.getRcScript().getRcCompetencyWrapperList() )
        {
            // LogService.logIt( "RcCheckUtils.getRcCompetenciesForInterviewQuestions() competency=" + rcw.getRcCompetency().getName() + ", has interview=" + rcw.getRcCompetency().getHasInterviewQuestion() + ", score=" + rcw.getScoreAvgNoCandidate() );

            // no interview question
            if( hasInterviewOnly && !rcw.getRcCompetency().getHasInterviewQuestion() )
                continue;
            
            if( srcTyp.getIsAll() )
                scoreToUse = rcw.getAverageScore(null);
            
            else if( srcTyp.getIsOthers() )
                scoreToUse = rcw.getScoreAvgNoCandidate();
            
            else
                scoreToUse = rcw.getAvgScoreCandidate( rc.getCandidateRcRaterId() );
            
            
            // not scored.
            if( scoreToUse<=0 )
                continue;
            
            // above
            if( high && scoreToUse<cutoff )
                continue;
            
            // below
            if( !high && scoreToUse>cutoff )
                continue;
            
            out.add( rcw );            
        }
        
        // ALWAYS sort high-to-low
        Collections.sort( out, new RcCompetencyWrapperScoreComparator(true, srcTyp, rc.getCandidateRcRaterId()) );
        
        if( maxQuestions>0 && out.size()>maxQuestions )
        {
            // if we want the top, take the top X values.
            if( high )
                out = out.subList(0, maxQuestions);
            
            // not high. 
            // since we want the bottom, take the bottom X values
            else
                out = out.subList( out.size()-maxQuestions, out.size() );
        }
        
        // reverse order if not high.
        //if( !high )
        //    Collections.reverse(out);
        
        return out;
    }
    
    public static float invertRatingScore( float score, RcRatingScaleType rcRatingScaleType )
    {
        if( rcRatingScaleType==null )
            rcRatingScaleType = RcRatingScaleType.DEFAULT;
        
        if( score>=rcRatingScaleType.getMinScore() && score<=rcRatingScaleType.getMaxScore() )
        {
            return rcRatingScaleType.getMaxScore() - score + rcRatingScaleType.getMinScore();
            // return 11f - score;
        }
        
        return score;
    }
    
    public static boolean isContactPermissionItem( int rcItemId )
    {
        if( rcItemIdsForContactPermission==null )
            init();
        
        return rcItemIdsForContactPermission.contains(rcItemId);
    }
                
    public static boolean isRecruitingPermissionItem(int rcItemId )
    {
        if( rcItemIdsForRecruiting==null )
            init();
        return rcItemIdsForRecruiting.contains(rcItemId);
    }  

    public static boolean isReferralsRequestItem(int rcItemId )
    {
        if( rcItemIdsForReferralsRequest==null )
            init();
        return rcItemIdsForReferralsRequest.contains(rcItemId);
    }  

    
    public static String conditionAccessCode( String accessCode )
    {
        if( accessCode==null || accessCode.isBlank() )
            return accessCode;
        
        if( accessCode.contains("?") )
            accessCode = accessCode.substring(0, accessCode.indexOf("?") );
        
        if( accessCode.contains("/") )
            accessCode = accessCode.substring(accessCode.lastIndexOf("/")+1, accessCode.length() );
        
        return accessCode;
    }
    
    /**
     * AccessCode is orgId Hex + XC or XR + Random String
     * 
     * returns:
     *    data[0] = orgId  (Integer)
     *    data[1] = AccessCode (String)
     *    data[2] = Type (Integer) 0=candidate, 1=rater
     * 
     * @param accessCode
     * @return
     * @throws Exception 
     */
    public static Object[] parseAccessCode( String accessCode ) throws Exception
    {
        Object[] out = new Object[3];
        
        if( accessCode==null || accessCode.isBlank()  )
            throw new STException( "g.AccessCodeInvalid", new String[]{(accessCode==null ? "null" : "empty" )} );
        
        int idx = accessCode.indexOf("X");
        
        if( idx<=0 || accessCode.length() < idx+8 )
            throw new STException( "g.AccessCodeInvalid", new String[]{accessCode} );
        
        String orgIdStr = accessCode.substring(0, idx );
        String typeStr = accessCode.substring( idx, idx+2 );
        
        out[1] = accessCode.substring( idx+2, accessCode.length() );
        int orgId = 0;
        out[2]=typeStr.toUpperCase().contains("XR") ? 1 : 0;
        
        try
        {
            orgId = Integer.parseInt( orgIdStr, 16 );   
            out[0] = orgId; 
        }
        catch( NumberFormatException e ) 
        {
            LogService.logIt( "RcCheckUtils.parseAccessCode() parsing OrgIdStr. Should be a Hex value. " + e.toString() + " accessCode=" + accessCode + ", orgIdStr=" + orgIdStr + ", typeStr=" + typeStr + ", codeStr=" + out[1] + ", orgId=" + orgId + ", type=" + out[2]  );
            throw new STException( "g.AccessCodeInvalid", new String[]{accessCode} );
        }
        catch( Exception e ) 
        {
            LogService.logIt( e, "RcCheckUtils.parseAccessCode() accessCode=" + accessCode + ", orgIdStr=" + orgIdStr + ", typeStr=" + typeStr + ", codeStr=" + out[1] + ", orgId=" + orgId + ", type=" + out[2]  );
            throw new STException( "g.AccessCodeInvalid", new String[]{accessCode} );
        }
        
        // LogService.logIt( "RcCheckUtils.parseAccessCode() accessCode=" + accessCode + ", orgId=" + out[0] + ", code=" + out[1] + ", type=" + out[2] );        
        return out;        
    }
    
    
    public void loadRcCheckForAdmin( RcCheck rc, RefUserType refUserType, Locale locale, boolean adminOverride) throws Exception
    {
        if( rc==null )
            return;
        
        if( userFacade==null )
            userFacade=UserFacade.getInstance();

        if( rc.getUser()==null )
            rc.setUser( userFacade.getUser( rc.getUserId() ));
        
        if( rc.getOrg()==null )
            rc.setOrg( userFacade.getOrg( rc.getOrgId() ));
        
        if( rc.getAdminUser()==null && rc.getAdminUserId()>0 )
            rc.setAdminUser( userFacade.getUser( rc.getAdminUserId() ));
        
        rc.setLocale( locale );
        
        if( rcFacade==null )
            rcFacade = RcFacade.getInstance();
        
        if( rc.getRcOrgPrefs()==null )
            rc.setRcOrgPrefs( rcFacade.getRcOrgPrefsForOrgId( rc.getOrgId( )));

        if( rc.getSuborgId()>0 && rc.getRcSuborgPrefs()==null )
            rc.setRcSuborgPrefs( rcFacade.getRcSuborgPrefsForSuborgId( rc.getSuborgId() ));            
        
        // always create an RcRater for the Candidate if needed, regardless of refUserType.
        // this is important to ensure that percent complete calculations are done right.
        createRcRaterForCandidate( rc, adminOverride );
        
        if( refUserType.getIsCandidate() )
        {
            if( rc.getRcScript()==null && rc.getRcScriptId()>0 )
            {
                if( rcScriptFacade==null )
                    rcScriptFacade = RcScriptFacade.getInstance();
                RcScript rcs = rcScriptFacade.getRcScript(rc.getRcScriptId(), true );
                rc.setRcScript( (RcScript)rcs.clone() );
                // rc.getRcScript().parseScriptJson();
                rcScriptFacade.loadScriptObjects(rc.getRcScript(), true);
            }
            
            // load existing resume if needed.
            if( rc.getRcScript()!=null && rc.getRcScript().getCollectResume()==1 && rc.getUser().getResume()==null )
            {
                rc.getUser().setResume( userFacade.getResumeForUser(rc.getUserId()));
            }
            
            if( rc.getRcRaterList()==null )
            {
                rc.setRcRaterList( rcFacade.getRcRaterList( rc.getRcCheckId() ));
                if( adminOverride )
                {
                    List<RcRater> rl = new ArrayList<>();
                    for( RcRater rtr : rc.getRcRaterList() )
                    {
                        rl.add( (RcRater)rtr.clone() );
                    }
                    rc.setRcRaterList(rl);
                }
            }                

            RcRater cRtr = null;
            for( RcRater r : rc.getRcRaterList() )
            {
                r.setRcCheck(rc);
                
                if( r.getIsCandidateOrEmployee() )
                    cRtr = r;
                
                if( r.getUser()==null )
                    r.setUser( userFacade.getUser( r.getUserId() ));
                
                r.setNeedsResendEmail(false);
                r.setNeedsResendMobile(false);
                
                r.setLocale( locale );      
                if( r.getRcRaterSourceTypeId()==RcRaterSourceType.UNKNOWN.getRcRaterSourceTypeId() )
                    r.setRcRaterSourceTypeId( RcRaterSourceType.getForRcRater(rc, r).getRcRaterSourceTypeId() );
                // r.setRcRaterSourceType( RcRaterSourceType.getForRcRater(rc, r));

            }
            
            if( rc.getCollectRatingsFmCandidate() )
            {
                if( cRtr==null )
                    throw new Exception( "No RcRater for candidate found." );
                
                cRtr.setUser( rc.getUser() );                
                rc.setRcRater(cRtr);
                
                if( cRtr.getStartDate()==null )
                    cRtr.setStartDate( new Date() );
                
                cRtr.setRaterStarts( cRtr.getRaterStarts()+1);
                
                if( !adminOverride )
                    rcFacade.saveRcRater(cRtr, false );
                
                if( rc.getRcScript()!=null )
                {
                    //if( rcScriptFacade==null )
                    //    rcScriptFacade = RcScriptFacade.getInstance();
                    //rcScriptFacade.loadScriptObjects(rc.getRcScript(), true );

                    rc.getRcScript().clearRatings();
                    List<RcRating> rcrl = rcFacade.getRcRatingList( rc.getRcCheckId(), cRtr.getRcRaterId() );
                    
                    if( adminOverride )
                    {
                        List<RcRating> rl = new ArrayList<>();
                        for( RcRating rg : rcrl )
                        {
                            rl.add( (RcRating) rg.clone() );
                        }
                        rcrl = rl;
                    }
                    
                    cRtr.setRcRatingList(rcrl);
                    rc.setRcRatingsInScript(rcrl, false );
                }                
            }
            
            if( !adminOverride && (rc.getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture() || ( (rc.getRcAvType().getAnyMedia() || rc.getRcScript().getHasCandidateFileUploads()) && rc.getCollectRatingsFmCandidate())) )
            {
                FileUploadFacade fuf = FileUploadFacade.getInstance();
                rc.setRcUploadedUserFileList( fuf.getRcUploadedUserFilesForRcCheckAndRater(rc.getRcCheckId(), cRtr==null ? 0 : cRtr.getRcRaterId() ) );
                // rc.setPhotoRcUploadedUserFileList( fuf.getRcUploadedUserFilesForRcCheckAndRater(rc.getRcCheckId(), cRtr==null ? 0 : cRtr.getRcRaterId() ) );
                
                if( cRtr!=null && (rc.getRcAvType().getAnyMedia() || rc.getRcScript().getHasCandidateFileUploads()) )
                {
                    rc.setRcUploadedUserFilesInCandidateRatings( cRtr.getRcRatingList(), cRtr.getRcRaterId());
                }
            }
        }
        
        // Not the candidate.
        else if( refUserType.getIsRater() )
        {
            if( rc.getRcRater().getUser()==null )
                rc.getRcRater().setUser( userFacade.getUser( rc.getRcRater().getUserId() ) );
            
            rc.getRcRater().setLocale( locale );
            //rc.getRcRater().setRcRaterSourceType( RcRaterSourceType.getForRcRater(rc, rc.getRcRater()));
            if( rc.getRcRater().getRcRaterSourceTypeId()==RcRaterSourceType.UNKNOWN.getRcRaterSourceTypeId() )
                rc.getRcRater().setRcRaterSourceTypeId( RcRaterSourceType.getForRcRater(rc, rc.getRcRater()).getRcRaterSourceTypeId() );

            // must come before loading Script.
            if( !adminOverride && (rc.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture() || rc.getRcAvType().getAnyMedia()) )
            {
                FileUploadFacade fuf = FileUploadFacade.getInstance();
                rc.getRcRater().setRcUploadedUserFileList( fuf.getRcUploadedUserFilesForRcCheckAndRater(rc.getRcCheckId(), rc.getRcRater().getRcRaterId()) );
                // rc.getRcRater().setRcUploadedUserFile( rufl!=null && !rufl.isEmpty() ? rufl.get(0) : null );
            }            
            
            if( rc.getRcScript()==null && rc.getRcScriptId()>0 )
            {
                if( rcScriptFacade==null )
                    rcScriptFacade = RcScriptFacade.getInstance();
                RcScript rcs = rcScriptFacade.getRcScript(rc.getRcScriptId(), true );
                rc.setRcScript( (RcScript)rcs.clone() );
                rcScriptFacade.loadScriptObjects(rc.getRcScript(), true );
            }
            
            if( rc.getRcScript()!=null )
            {
                //if( rcScriptFacade==null )
                //    rcScriptFacade = RcScriptFacade.getInstance();
                //rcScriptFacade.loadScriptObjects(rc.getRcScript(), true );
                
                rc.getRcScript().clearRatings();
                List<RcRating> rcrl = rcFacade.getRcRatingList( rc.getRcCheckId(), rc.getRcRater().getRcRaterId() );
                if( adminOverride )
                {
                    List<RcRating> rl = new ArrayList<>();
                    for( RcRating rg : rcrl )
                    {
                        rl.add( (RcRating) rg.clone() );
                    }
                    rcrl = rl;
                }
                
                rc.getRcRater().setRcRatingList(rcrl);
                rc.setRcRatingsInScript(rcrl, false );
                
                if( !adminOverride && rc.getRcAvType().getAnyMedia() )
                {
                    rc.getRcRater().setRcUploadedUserFilesInRatings();
                } 
                
                if( rc.getRcScript().getHasShowCandRespToRater() && rc.getRcRaterList()==null )
                {
                    rc.setRcRaterList( rcFacade.getRcRaterList( rc.getRcCheckId() ));
                    if( adminOverride )
                    {
                        List<RcRater> rl = new ArrayList<>();
                        for( RcRater rtr : rc.getRcRaterList() )
                        {
                            rl.add( (RcRater)rtr.clone() );
                        }
                        rc.setRcRaterList(rl);
                    }
                }
                
                if( !rc.getRcRater().getIsCandidateOrEmployee() && rc.getRcScript().getSharePrevResultsWithRater()>0 )
                {
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();
                    List<Long> userIdList = userFacade.findUserIdsMatchingUser(rc.getUser());
                    List<PreviousResult> prl = new ArrayList<>();
                    
                    prl.addAll( eventFacade.findCompleteTestEventsForUser(userIdList));
                    prl.addAll( rcFacade.findCompleteRcChecksForUser(userIdList));
                    
                    for( PreviousResult pr : prl )
                    {
                        pr.setLocale( locale );
                        
                        if( pr instanceof TestEvent testEvent )
                            testEvent.setProduct(eventFacade.getProduct(testEvent.getProductId() ));
                    }
                    
                    Collections.sort( prl, new PreviousResultDateComparator() ); 
                    Collections.reverse(prl);
                    rc.setPreviousResultList(prl);
                }
            }
            
            if( rc.getRcCheckStatusType().getIsComplete() || rc.getRcCheckStatusType().getIsExpired() )
            {
                int graceDays = rc.getRcOrgPrefs().getRaterGracePeriod();
                Calendar cal = new GregorianCalendar();
                cal.add( Calendar.DAY_OF_MONTH, -1*graceDays );

                if( rc.getCompleteDate()!=null && cal.getTime().before( rc.getCompleteDate()) )
                    rc.getRcRater().setInGracePeriod(true);
                else if( rc.getExpireDate()!=null && cal.getTime().before( rc.getExpireDate()) )
                    rc.getRcRater().setInGracePeriod(true);
                else
                    rc.getRcRater().setInGracePeriod(false);
            }  
            
        }
    }
    
    
    public RcRater createRcRaterForCandidate( RcCheck rc, boolean adminOverride ) throws Exception
    {
        if( rc==null )
            throw new Exception( "RcCheckUtils.createRcRaterForCandidate() RcCheck is null" );
        
        // LogService.logIt( "RcCheckUtils.createRcRaterForCandidate() rcCheckId=" + rc.getRcCheckId() + ", rc.getCollectRatingsFmCandidate()=" + rc.getCollectRatingsFmCandidate() );
        if( !rc.getCollectRatingsFmCandidate() )
            return null;
        
        if( rc.getRcRaterList()==null )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rc.setRcRaterList( rcFacade.getRcRaterList( rc.getRcCheckId() ));
            if( adminOverride )
            {
                List<RcRater> rl = new ArrayList<>();
                for( RcRater rtr : rc.getRcRaterList() )
                {
                    rl.add( (RcRater)rtr.clone() );
                }
                rc.setRcRaterList(rl);
            }
        }                

        // see if has one already
        for( RcRater r : rc.getRcRaterList() )
        {
            if( r.getRcRaterType().getIsCandidateOrEmployee() )
                return r;
            
            if( r.getUserId()==rc.getUserId() )
            {
                LogService.logIt("RccheckUtils.createRcRaterForCandidate() ERROR Found a Rater with the same userId as rcCheck but not set to self. Correcting. rcCheckId=" + r.getRcCheckId() + ", rcRaterId=" + r.getRcRaterId() + ", correcting." );
                r.setRcRaterTypeId(RcRaterType.SELF.getRcRaterTypeId() );
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                r = rcFacade.saveRcRater(r, false);
                return r;
            }
        }
        
        if( rcFacade==null )
            rcFacade=RcFacade.getInstance();
                
        RcRater cRtr = rcFacade.getRcRaterByRcCheckIdAndUserId(rc.getRcCheckId(), rc.getUserId());
        if( cRtr!=null )
        {
            if( !rc.getRcRaterList().contains(cRtr ))
                rc.getRcRaterList().add(cRtr );
            return cRtr;
        }
                          
        try
        {
            cRtr = new RcRater();
            cRtr.setRcCheckId( rc.getRcCheckId() );
            cRtr.setUserId( rc.getUserId() );
            cRtr.setCompanyName( MessageFactory.getStringMessage(rc.getLocale(), "g.RCSelfRatings" ));
            cRtr.setOrgId( rc.getOrgId() );
            cRtr.setSourceUserId( rc.getAdminUserId() );
            cRtr.setRcRaterTypeId( RcRaterType.SELF.getRcRaterTypeId() );
            // cRtr.setRcRaterSourceType(RcRaterSourceType.CANDIDATE);
            cRtr.setRcRaterSourceTypeId(RcRaterSourceType.CANDIDATE.getRcRaterSourceTypeId());
            cRtr.setLocale( rc.getLocale() );
            cRtr.setUser( rc.getUser() );

            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.YEAR, -1 );
            cal.add( Calendar.DAY_OF_MONTH, -1 );
            cRtr.setObservationStartDate( cal.getTime() );
            cRtr.setObservationEndDate( new Date() );


            if( !adminOverride )
                cRtr = rcFacade.saveRcRater(cRtr, false );

            //if( adminOverride )
            //    cRtr = (RcRater) cRtr.clone();

            if( !rc.getRcRaterList().contains(cRtr ));            
                rc.getRcRaterList().add( cRtr );
            
            return cRtr;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RccheckUtils.createRcRaterForCandidate() ZZZ.1 rcCheckId=" + rc.getRcCheckId() );
            throw e;
        }
    }
    
        
    
    
    public List<Integer> getReportIdsForRcCheck( RcCheck rc, String langStr ) throws Exception
    {
        List<Integer> out = new ArrayList<>();
        
        if( rc.getReportId()>0 )
        {
            out.add( rc.getReportId() );
            if( rc.getReportId2()>0 )
                out.add( rc.getReportId2() );            
        }
        if( !out.isEmpty() )
            return out;
        
        int reportId = 0;
        int reportId2 = 0;
        if( rcFacade==null )
            rcFacade=RcFacade.getInstance();
        RcOrgPrefs rcop = rcFacade.getRcOrgPrefsForOrgId( rc.getOrgId() );
        if( rcop!=null )
        {
            reportId = rc.getRcCheckType().getIsPrehire() ? rcop.getReportIdPrehire() : rcop.getReportIdEmployee();
            if( reportId>0 )
            {
                reportId2 = rc.getRcCheckType().getIsPrehire() ? 0 : rcop.getReportIdEmployeeFbk();
                out.add( reportId );
                if( reportId2>0 )
                    out.add( reportId2 );
            }
            if( !out.isEmpty() )
                return out;
        }

        if( langStr==null || langStr.isBlank() )
            langStr = rc.getLangCode();
        if( langStr==null || langStr.isBlank() )
            langStr = "en_US";

        Locale locale = I18nUtils.getLocaleFromCompositeStr(langStr);
        String lang = locale.getLanguage();

        String langKey = rc.getRcCheckType().getIsPrehire() ? "DefaultRcReportPrehire" + "_" + lang : "DefaultRcReportEmployee" + "_" + lang;
        
        if( !lang.equalsIgnoreCase( "en" ) )
            LogService.logIt("RcCheckUtils.getReportIdForRcCheck() langStr=" + langStr + ", lang=" + lang + ", comp=" + ("DefaultRcReportPrehire" + "_" + lang)  );
        
        if( RuntimeConstants.hasValueForKey( langKey ) )
            reportId = RuntimeConstants.getIntValue( langKey ); 
        
        if( reportId<=0 )
            reportId = RuntimeConstants.getIntValue( rc.getRcCheckType().getIsPrehire() ? "DefaultRcReportPrehire" : "DefaultRcReportEmployee" ); 
        
        langKey = "DefaultRcReportEmployeeFbk" + "_" + lang;
        
        
        if( !rc.getRcCheckType().getIsPrehire() && RuntimeConstants.hasValueForKey( langKey ) )
            reportId2 = RuntimeConstants.getIntValue( langKey );
        
        if( !rc.getRcCheckType().getIsPrehire() && reportId2<=0 )
            reportId2 = RuntimeConstants.getIntValue( "DefaultRcReportEmployeeFbk" ); 
        
        if( reportId>0 )
        {
            out.add( reportId );
            if( reportId2>0 )
                out.add( reportId2 );            
        }
        return out;
    }
    
    
    public void loadRcCheckForScoringOrResults( RcCheck rc) throws Exception
    {
        if( rc==null )
            return;
        
        if( userFacade==null )
            userFacade=UserFacade.getInstance();

        if( rc.getUser()==null )
            rc.setUser( userFacade.getUser( rc.getUserId() ));
        
        if( rc.getUser()!=null && rc.getUser().getResume()==null )
        {
            rc.getUser().setResume( userFacade.getResumeForUser( rc.getUserId() ));
        }

        if( rc.getUser()!=null && rc.getUser().getResume()!=null && rc.getUser().getResume().getNeedsParse()==1 )
        {
            boolean success = ResumeHelpUtils.parseResumeByAiNoErrors( rc.getRcCheckId(), rc.getUser(), rc.getUser().getResume());
            if( success )
            {
                Thread.sleep(100);
                rc.getUser().setResume( userFacade.getResumeForUser(rc.getUserId()));
            }
        }        
        
        if( rc.getOrg()==null )
            rc.setOrg( userFacade.getOrg( rc.getOrgId() ));
        
        if( rc.getAdminUser()==null && rc.getAdminUserId()>0 )
            rc.setAdminUser( userFacade.getUser( rc.getAdminUserId() ));
        
        // rc.setLocale( getLocale() );
        
        if( rcFacade==null )
            rcFacade = RcFacade.getInstance();
        
        if( rc.getRcOrgPrefs()==null )
            rc.setRcOrgPrefs( rcFacade.getRcOrgPrefsForOrgId( rc.getOrgId( )));

        if( rc.getSuborgId()>0 && rc.getRcSuborgPrefs()==null )
            rc.setRcSuborgPrefs( rcFacade.getRcSuborgPrefsForSuborgId( rc.getSuborgId() ));            

        //if( refUserType.getIsCandidate() )
        //{
        if( rc.getRcScript()==null )
        {
            if( rcScriptFacade==null )
                rcScriptFacade = RcScriptFacade.getInstance();
            RcScript rcs = rcScriptFacade.getRcScript(rc.getRcScriptId(), true );
            rc.setRcScript( (RcScript)rcs.clone() );            
            rcScriptFacade.loadScriptObjects(rc.getRcScript(), true );
        }
            
        // if( rc.getRcRaterList()==null )
        // always load raters. 
        rc.setRcRaterList( rcFacade.getRcRaterList( rc.getRcCheckId() ));                
        
        // Sort raters by name.
        RcRater cRtr = null;
        if( !rc.getRcRaterList().isEmpty() )
        {
            // int count = 0;
            for( RcRater r : rc.getRcRaterList() )
            {
                r.setRcCheck(rc);
                
                if( r.getRcRaterType().getIsCandidateOrEmployee() )
                    cRtr = r;
                
                if( r.getUser()==null )
                    r.setUser( userFacade.getUser( r.getUserId() ));
                // r.setLocale( getLocale() );                
                // r.setRcRaterSourceType( RcRaterSourceType.getForRcRater(rc, r));
                if( r.getRcRaterSourceTypeId()==RcRaterSourceType.UNKNOWN.getRcRaterSourceTypeId() )
                    r.setRcRaterSourceTypeId( RcRaterSourceType.getForRcRater(rc, r).getRcRaterSourceTypeId());

                r.setRcRatingList( rcFacade.getRcRatingList( rc.getRcCheckId(), r.getRcRaterId() ) );
            } 
            
            Collections.sort( rc.getRcRaterList(), new RcRaterNameComparator() );
            
            // Move candidate rater record to the top slot after sorting.
            if( cRtr!=null )
            {
                rc.getRcRaterList().remove( cRtr );
                rc.getRcRaterList().add( 0, cRtr);
            }            
        }
        
        FileUploadFacade fileUploadFacade = null;
        
        // Candidate photos and Videos
        if( rc.getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture() || ((rc.getRcAvType().getAnyMedia() || rc.getRcScript().getHasCandidateFileUploads()) && rc.getCollectRatingsFmCandidate()) )
        {
            if( fileUploadFacade==null )
                fileUploadFacade = FileUploadFacade.getInstance();
            rc.setRcUploadedUserFileList(fileUploadFacade.getRcUploadedUserFilesForRcCheckAndRater(rc.getRcCheckId(),cRtr==null ? 0 : cRtr.getRcRaterId()));
            if( rc.getRcUploadedUserFileList()!=null && !rc.getRcUploadedUserFileList().isEmpty() )
            {
                rc.setFauxRcUploadedUserFileList( ProctorUtils.getFauxRcUploadedUserFileListForReportThumbs(rc.getRcUploadedUserFileList(), true, 20));
            }
            // rc.setRcUploadedUserFileList(fileUploadFacade.getRcUploadedUserFilesForRcCheckAndRater(rc.getRcCheckId(),cRtr==null ? 0 : cRtr.getRcRaterId()));

            // Candidate rating comment videos are stored in the RcCheck
            if( cRtr!=null && (rc.getRcAvType().getAnyMedia() || rc.getRcScript().getHasCandidateFileUploads()) )
                rc.setRcUploadedUserFilesInCandidateRatings( cRtr.getRcRatingList(), cRtr.getRcRaterId() );
        }
        
        rc.getRcScript().clearRatings();
        // List<RcUploadedUserFile> rufl = null;
        for( RcRater r : rc.getRcRaterList() )
        {
            if( !r.getIsCandidateOrEmployee() && (rc.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture() || rc.getRcAvType().getAnyMedia()) )
            {
                if( fileUploadFacade==null )
                    fileUploadFacade = FileUploadFacade.getInstance();
                r.setRcUploadedUserFileList( fileUploadFacade.getRcUploadedUserFilesForRcCheckAndRater(rc.getRcCheckId(),r.getRcRaterId()) );
                
                // r.setRcUploadedUserFile( rufl!=null && !rufl.isEmpty() ? rufl.get(0) : null );
                if( rc.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture() && r.getRcUploadedUserFileList()!=null && !r.getRcUploadedUserFileList().isEmpty() )
                    r.setFauxRcUploadedUserFileList( ProctorUtils.getFauxRcUploadedUserFileListForReportThumbs( r.getRcUploadedUserFileList(), true, 20 ));
                //r.setRcUploadedUserFileList( rufl!=null && !rufl.isEmpty() ? rufl : null );

                if( rc.getRcAvType().getAnyMedia() )
                    r.setRcUploadedUserFilesInRatings();
            }
                        
            rc.setRcRatingsInScript(r.getRcRatingList(), true );              
        }
        
        if( cRtr!=null && cRtr.getRcRaterAiStatusTypeId()<RcRaterAiStatusType.COMPLETE.getRcRaterAiStatusTypeId() && rc.getRcScript().getHasAnyAiProcessing())
        {
            RcCheckAiScoresUpdater rcCheckAiScoresUpdater = new RcCheckAiScoresUpdater(rc);
            rcCheckAiScoresUpdater.updateRcCheckAiScores();
        }
                
        rc.setRcSuspiciousActivityList( rcFacade.getRcSuspiciousActivityList( rc.getRcCheckId() ));
        for( RcSuspiciousActivity sa : rc.getRcSuspiciousActivityList() )
        {
            if( sa.getRcSuspiciousActivityType().getIsRaterRaterMatch() || sa.getRcRaterId()<=0 )
                continue;
            if( sa.getUser()==null )
            {
                RcRater r = rc.getRcRaterForRcRaterId( sa.getRcRaterId() );
                sa.setUser( r.getUser() );
            }
        }
        
        long candidateRaterId = rc.getCandidateRcRaterId();
        List<Long> rcRaterIdsToSkip = null;        
        if( candidateRaterId>0 && (rc.getCandidateCanAddRaters() || rc.getRcRaterList().size()>1 ) )
        {
            rcRaterIdsToSkip = new ArrayList<>();
            rcRaterIdsToSkip.add(candidateRaterId);
        }
        for( RcCompetencyWrapper rcw : rc.getRcScript().getRcCompetencyWrapperList() )
        {
            for( RcItemWrapper rciw : rcw.getRcItemWrapperList() )
            {
                if( !rciw.getRcItem().getIsItemScored() )
                    continue;
                rciw.setScoreAvgNoCandidate( rciw.getAverageScore(rcRaterIdsToSkip));
            }
        }
        
        if( rc.getMetaScoreList()==null )
            rc.setAiMetaScoreList( rcFacade.getReportableAiMetaScoreListForRcCheck( rc.getRcCheckId()));
                
    }
    
    public static void addAnonymousNames(RcCheck rc, Locale locale )
    {
        if( rc.getRcRaterList()==null || rc.getRcScript()==null)
            return;

        int anonymous = rc.getForceAllAnonymous()>=0 ? rc.getForceAllAnonymous() : rc.getRcScript().getForceAllAnonymous();

        int ct = 0;
        for( RcRater rtr : rc.getRcRaterList() )
        {
            if( anonymous>=1 )
            {
                rtr.setAnonymousName( MessageFactory.getStringMessage(locale, "g.AnonyNameX" , new String[]{getLetterCode( ct )}));
                ct++;
            }
            else
                rtr.setAnonymousName(null);
            
        }
        Collections.sort( rc.getRcRaterList(), new RcRaterNameComparator() );
        
    }
    
    
    public float computeRcRaterOverallScore( RcCheck rc, RcRater rater )
    {
        RcScript rcs = rc.getRcScript();        
        
        boolean hasWeights = false;
        for( RcItemWrapper rciw : rcs.getAllItemWrapperList() )
        {
            if( rciw.getWeight()>0 )
            {
                hasWeights=true;
                break;
            }
        }
        float ct = 0;
        float total = 0;
        RcRating rating;
        for( RcItemWrapper rciw : rcs.getAllItemWrapperList() )
        {
            if( !rciw.getRcItem().getIsItemScored() )
                continue;
            
            rating = rciw.getRcRating( rater.getRcRaterId() );
            
            if( rating==null )
                continue;
            
            if( !rating.getIsComplete() )
                continue;
            
            if( hasWeights )
            {
                ct += rciw.getWeight();
                total += rating.getFinalScore()*rciw.getWeight();                
            }
            else
            {
                ct++;
                total += rating.getFinalScore();
            }
        }
        
        if( ct>0 )
            total = total/ct;
        
        return (float) NumberUtils.roundIt(total, 2);
    }


    /*
    public float computeRcCheckPercentComplete( RcCheck rc ) throws Exception
    {
        // always reload.
        if( rcFacade==null )
            rcFacade = RcFacade.getInstance();
        return rcFacade.computeRcCheckPercentComplete(rc);
    }
    */
    
    public float computeRcCheckPercentComplete( RcCheck rc ) throws Exception
    {
        
        if( rcFacade==null )
            rcFacade = RcFacade.getInstance();
        List<RcRater> rcrl = rcFacade.getRcRaterList( rc.getRcCheckId() );
        float ct = 0;
        float total = 0;
        int minRaters = rc.getMinRaters();
        int minSups = rc.getMinSupervisors();
        int rtrs = 0;
        int sups = 0;
        for( RcRater rcr : rcrl )
        {
            rtrs++;            
            if( rcr.getRcRaterRoleType().getIsSupervisorOrManager() )
                sups++;
            
            if( rcr.getRcRaterStatusType().getIsDeactivated() || rcr.getRcRaterStatusType().getIsExpired() || rcr.getRcRaterStatusType().getIsRejected() ) 
                continue;
            
            ct++;
            total += rcr.getPercentComplete();
        }
        
        if( ct==0 )
            return 0;
        
        int missingRtrs = Math.max(minRaters - rtrs,0);
        int missingSups = Math.max(minSups - sups,0);
        
        // The number of missing raters is higher of these. 
        missingRtrs = Math.max(missingRtrs, missingSups);
        
        if( missingRtrs>0 )
            ct += missingRtrs;
        
        // adjust if still need a candidate to complete something and there is no candidate rater.
        if( rc.getCollectCandidateRatings()!=1 && rc.getRequiresResumeOrAnyCandidateInputOrSelfRating() && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
            ct++;
        
        total = total/ct;

        if( rc.getCollectCandidateRatings()==1 && ct<=1 )
            total = Math.min( 50f, total );
        
        return (float) NumberUtils.roundIt(total, 0);
    }
    
    
    
    
    
    public float computeRcCheckOverallScore( RcCheck rc ) throws Exception
    {
        //if( rc.getPercentComplete()<100 )
        //    return 0;
        
        if( rcFacade==null )
            rcFacade=RcFacade.getInstance();
        List<RcRater> rcrl = rcFacade.getRcRaterList( rc.getRcCheckId() );
        float ct = 0;
        float total = 0;
        for( RcRater rcr : rcrl )
        {
            // this is the candidate and they can add raters and there is more than one rater. 
            if( rcr.getIsCandidateOrEmployee() && (rc.getCandidateCanAddRaters() || rcrl.size()>1 ) )
                continue;
            
            
            if( !rcr.getRcRaterStatusType().getIsComplete() ) 
                continue;
            
            ct++;
            total += rcr.getOverallScore();
        }
        if( ct>0 )
            total = total/ct;

        return (float) NumberUtils.roundIt(total, 2);
    }

    
    public void performRcCandidateCompletionIfReady( RcCheck rc, boolean adminOverride) throws Exception
    {
        try
        {
            if( rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
                return;
            
            // Needs nothing.
            if( !rc.getRequiresResumeOrAnyCandidateInputOrSelfRating() )
            {
                rc.setRcCandidateStatusTypeId( RcCandidateStatusType.COMPLETED.getRcCandidateStatusTypeId() );
                rc.setCandidateCompleteDate( new Date() );
                if( !adminOverride )
                {
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcCheck(rc, false );
                }
                
                performRcCheckCompletionIfReady(rc, false, adminOverride);
                return;
            }

            if( !adminOverride && rc.getRcRaterList()==null )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rc.setRcRaterList( rcFacade.getRcRaterList( rc.getRcCheckId()));
            }
             
            if( rc.getRcRaterList()!=null && !rc.getRcRaterList().isEmpty() )
            {
                int nonCandRaters = 0;
                for( RcRater r : rc.getRcRaterList() )
                {
                    if( r.getIsCandidateOrEmployee() )
                         continue;
                    nonCandRaters++;
                }
                
                if( !adminOverride && nonCandRaters>0 )
                    doCheckForSuspiciousActivity(rc);                
            }

            
            // needs to have candidate raters
            if( rc.getCandidateCanAddRaters() && rc.getMinRatersCandidate()>0 && rc.getCandidateCannotAddRaters()<=0 )
            {                
                // Not enough supervisors
                if( rc.getMinSupervisorsNeeded()>0 )
                    return;
                
                // not enough raters
                if( rc.getRcRaterListCandidate().size()<rc.getMinRatersCandidate())
                    return;
            }
            
            // needs self ratings
            if( rc.getCollectRatingsFmCandidate() )
            {
                boolean hasComp = false;
                for( RcRater r : rc.getRcRaterList() )
                {
                     if( r.getIsCandidateOrEmployee() )
                     {
                        hasComp = r.getRcRaterStatusType().getCompleteOrHigher();
                        
                        if( hasComp )
                        {
                            if( r.getCompleteDate()==null )
                            {
                                r.setCompleteDate( new Date() );
                                if( !adminOverride )
                                {
                                    if( rcFacade==null )
                                        rcFacade=RcFacade.getInstance();
                                    rcFacade.saveRcRater(r, false );
                                }
                            }
                            if( rc.getCandidateRatingsCompleteDate()==null )
                            {
                                rc.setCandidateRatingsCompleteDate( r.getCompleteDate() );
                                if( !adminOverride )
                                {
                                    if( rcFacade==null )
                                        rcFacade=RcFacade.getInstance();
                                    rcFacade.saveRcCheck(rc, false );  

                                    if( rc.getRaterSendDelayTypeId()>0 )
                                        sendDelayedRaterInvitations(rc, null, adminOverride);
                                }
                            }
                        }
                        break;
                     }
                }
                
                // No complete ratings.
                if( !hasComp )
                    return;                
            }
            
            if( rc.getRcScript()==null )
            {
                if( rcScriptFacade==null )
                    rcScriptFacade=RcScriptFacade.getInstance();
                rc.setRcScript( (RcScript) rcScriptFacade.getRcScript(rc.getRcScriptId(), true ).clone() );
            }
            
            if( rc.getRcScript().getCollectResumeB() )
            {
                // has not done the resume.
                if( rc.getResumeComplete()<=0 )
                    return;
                // else, completed.
            }
            
            // needs questions answered
            if( rc.getRcScript().getHasAnyCandidateInput() )
            {
                boolean hasUnans = false;
                String inpt;
                for( int i=1;i<=5;i++ )
                {
                    if( !rc.getRcScript().getHasCandidateInput(i) )
                        continue;
                    
                    inpt = rc.getCandidateInputStr(i);
                    if( inpt==null || inpt.isBlank() )
                    {
                        hasUnans = true;
                        break;
                    }
                }
                if( hasUnans )
                    return;
            }
            
            // at this point, it's complete!
            rc.setRcCandidateStatusTypeId( RcCandidateStatusType.COMPLETED.getRcCandidateStatusTypeId() );
            rc.setCandidateCompleteDate( new Date() );
            
            if( rc.getCollectRatingsFmCandidate() && rc.getCandidateRatingsCompleteDate()==null )
                rc.setCandidateRatingsCompleteDate(rc.getCandidateCompleteDate() );
            
            if( !adminOverride )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcFacade.saveRcCheck(rc, false );
                
                if( rc.getRaterSendDelayTypeId()>0 )
                    sendDelayedRaterInvitations(rc, null, adminOverride);                
            }
            
            // useful data from candidate - charge credit.
            if( !adminOverride &&  (rc.getCollectRatingsFmCandidate() || rc.getRcScript().getHasAnyCandidateInput() || rc.getRcScript().getCollectResumeB()) )
                chargeCreditIfNeeded(rc, null );

            performRcCheckCompletionIfReady(rc, RcCheckUtils.getIsRaterOrCandidateCompleteButBeforeExpireDateAndCanReenter(rc, null, RefUserType.CANDIDATE), adminOverride );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcCheckUtils.performRcCandidateCompletionIfReady() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) );
            throw e;
        }
    }

    public boolean performRcRaterCompletionIfReady( RcCheck rc, RcRater rater, boolean adminOverride) throws Exception
    {
        try
        {
            
            if( rater==null )
                throw new Exception( "RcRater is null" );
            
            if( rater.getRcRaterStatusType().getCompleteOrHigher() )
            {
                // LogService.logIt("RcCheckUtils.performRcRaterCompletionIfReady() RcRater is already complete or higher. status=" + rc.getRcCheckStatusType().getName() + ", rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rater.getRcRaterId() );
                return true;                
            }
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();

            RcScript rcs = rc.getRcScript();
            RcRating rating;
            for( RcItemWrapper rciw : rcs.getAllItemWrapperList() )
            {
                if( rater.getIsCandidateOrEmployee() && rciw.getRcItem().getSkipforCandidate()>0 )
                    continue;
                
                rating = rciw.getRcRating( rater.getRcRaterId() );                
                if( rating==null )
                {
                    // throw new Exception( "RcRater is not complete or skipped. At least one question was not answered: rcItemId=" + rciw.getRcItem().getRcItemId() + ", question=" + rciw.getRcItem().getQuestion() );
                    LogService.logIt( "RcCheckUtils.performRcRaterCompletionIfReady() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) + " RcRater is not complete or skipped. At least one question was not answered: rcItemId=" + rciw.getRcItem().getRcItemId() + ", question=" + rciw.getRcItem().getQuestion() );
                    return false;
                }
                    
                if( !rating.getIsCompleteOrHigher() )
                {
                    // throw new Exception( "RcRater is not complete or skipped. At least one question was not answered: rcItemId=" + rciw.getRcItem().getRcItemId() + ", question=" + rciw.getRcItem().getQuestion() + ", rating status is " + rating.getRcRatingStatusType().getName() );
                    LogService.logIt( "RcCheckUtils.performRcRaterCompletionIfReady() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) + " RcRater is not complete or skipped. At least one question was not answered: rcItemId=" + rciw.getRcItem().getRcItemId() + ", question=" + rciw.getRcItem().getQuestion() + ", rating status is " + rating.getRcRatingStatusType().getName() );
                    return false;
                }
            }
            
            // At this point the rater is complete.
            rater.setRcRaterStatusTypeId( RcRaterStatusType.COMPLETED.getRcRaterStatusTypeId() );
            rater.setPercentComplete(100);
            rater.setOverallScore( computeRcRaterOverallScore( rc, rater ) );
            rater.setCompleteDate( new Date() );
            if( !adminOverride )
                rcFacade.saveRcRater( rater, true );  
            Tracker.addRaterComplete();     
            
            if( !adminOverride )
                chargeCreditIfNeeded(rc, rater );
            
            // see if candidate is now complete.
            if( rater.getIsCandidateOrEmployee() )
            {
                performRcCandidateCompletionIfReady(rc, adminOverride);
            }
            
            // check for suspicious
            else
            {
                if( !adminOverride )
                    doCheckForSuspiciousActivity(rc);
                
                // Sometimes, candidates exited without getting marked complete, even if there are enough raters and all questions are answered. 
                // So, check now since we have a newly completed rater.
                // not necessary for adminOverride
                if( !adminOverride && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
                    performRcCandidateCompletionIfReady(rc, adminOverride);
            }
            
            // Check for RcCheck completion if not already complete and candidate is complete.
            if( !rc.getRcCheckStatusType().getCompleteOrHigher() || rater.getInGracePeriod() )
                performRcCheckCompletionIfReady(rc, rater.getInGracePeriod() || RcCheckUtils.getIsRaterOrCandidateCompleteButBeforeExpireDateAndCanReenter(rc, rater, rater.getIsCandidateOrEmployee() ? RefUserType.CANDIDATE : RefUserType.RATER), adminOverride);
            
            return true;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcCheckUtils.performRcRaterCompletionIfReady() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) );
            throw e;
        }
        
    }
    
    
    public int[] sendDelayedRaterInvitations( RcCheck rc, Boolean hasUnconvertedMedia, boolean adminOverride)
    {
        int[] out = new int[2];
        
        try
        {
            if( rc.getRaterSendDelayTypeId()<=0 )
                return out;
            
            if( rc.getCandidateRatingsCompleteDate()==null )
            {
                LogService.logIt("CandidateRefUtils.sendDelayedRaterInvitations() candidateRatingsCompleteDate is null. Cannot send delayed invitations. " + rc.toString() );
                return out;
            }

            if( hasUnconvertedMedia==null )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                hasUnconvertedMedia = RcCheckUtils.hasUnconvertedCandidateMediaForRaterReview(rc, rcFacade);
            }
                     
            if( rc.getRaterSendDelayTypeId()==1 && rc.getCandidateRcRaterId()>0 && hasUnconvertedMedia )
            {
                LogService.logIt("CandidateRefUtils.doCompleteSelfRatings() rcCheck is complete but there is unconverted media for review. rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) );
                // indicates must wait and check media.
                rc.setRaterSendDelayTypeId(10);
                
                if( !adminOverride )
                {
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcCheck( rc, true );                                                    
                }
            }                    
            
            if( hasUnconvertedMedia )
            {
                LogService.logIt("CandidateRefUtils.sendDelayedRaterInvitations() Candidate Ratings has uncoverted av media for rater review. Cannot send delayed invitations. " + rc.toString() );
                return out;
            }
            
            return sendUnsentRcRaters(rc, false, adminOverride);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "CandidateRefUtils.sendDelayedRaterInvitations() " + (rc==null ? "rcCheck is null" : rc.toString() ) );
            return out;
        }
    }

    protected int[] sendUnsentRcRaters( RcCheck rc, boolean candidateRatersOnly, boolean adminOverride ) throws Exception
    {
        int[] out = new int[2];
        
        if( candidateRatersOnly && rc.getCandidateCannotAddRaters()==1 )
            return out;

        List<RcRater> rcrl = candidateRatersOnly ? rc.getRcRaterListCandidate() : rc.getRcRaterList();
        int[]  sendstats;

        for( RcRater rcr : rcrl )
        {
            if( candidateRatersOnly && !rcr.getCandidateCanSend() )
                continue;
            
            if( rcr.getRaterNoSend()==1 )
                continue;

            if( !rcr.getRcRaterStatusType().getSentOrHigher() )
            {
                sendstats = adminOverride ? new int[2] : sendRcCheckToRater(rc, rcr, false, false, false );
                
                if( sendstats!=null )
                {
                    out[0] += sendstats[0];
                    out[1] += sendstats[1];
                }
                
                if( sendstats[0]>0 || sendstats[1]>0 )
                {
                    rcr.setRcRaterStatusTypeId( RcRaterStatusType.SENT.getRcRaterStatusTypeId() );

                    if( !adminOverride )
                    {
                        if( rcFacade==null )
                            rcFacade = RcFacade.getInstance();
                        rcFacade.saveRcRater(rcr, false);
                    }
                }
            }
            
            else if( !adminOverride && (rcr.getNeedsResendEmail() || rcr.getNeedsResendMobile()) )
                sendRcCheckToRater(rc, rcr, true, false, false );
        }
        
        return out;        
    }
    
    

    /*
     data[0] = email sent count
     data[1] = text sent count

    */
    public int[] sendRcCheckToRater( RcCheck rc, RcRater rater, boolean sendIfNeedsOnly, boolean reminder, boolean setWebsiteMessages) throws Exception
    {
        if( rc==null )
            throw new Exception( "rcCheck is null" );
        if( rater==null )
            throw new Exception( "rater is null");
        if( userFacade == null )
            userFacade = UserFacade.getInstance();
        User user = rater.getUser();

        if( user==null )
        {
            user = userFacade.getUser( rater.getUserId());
            rater.setUser(user);
        }

        if( user==null )
            throw new Exception( "BaseRefUtils.sendRcCheckToRater() user is null. rcCheckId=" + rc.getRcCheckId() );

        if( rc.getAdminUser()==null )
            rc.setAdminUser( userFacade.getUser( rc.getAdminUserId() ) );
        if( rc.getLocale()==null && rc.getLangCode()!=null )
            rc.setLocale( I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() ));
        if( rc.getLocale()==null )
            rc.setLocale( Locale.US );

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
            //if( sent[1]>0 && setWebsiteMessages )
            //    setInfoMessage( reminder ? "g.RCReminderTextSent" : "g.RCTextSent" , new String[]{user.getFullname(), user.getMobilePhone()} );
            if( sent[1]==0 && setWebsiteMessages )
                throw new STException( reminder ? "g.RCReminderTextNotSentError" : "g.RCTextNotSentError" , new String[]{user.getFullname(), user.getMobilePhone()} );
            else if( sent[1]==-1 && setWebsiteMessages )
                throw new STException( reminder ? "g.RCReminderTextNotSentErrorInvalid" : "g.RCTextNotSentErrorInvalid" , new String[]{user.getFullname(), user.getMobilePhone()} );
            else if( sent[1]<-1 && setWebsiteMessages )
               throw new STException( reminder ? "g.RCReminderTextNotSentErrorBlock" : "g.RCTextNotSentErrorBlock" , new String[]{user.getFullname(), user.getMobilePhone()} );
        }

        //if( user.getEmail()!=null && !user.getEmail().isBlank() && sent[0]>0 )
        //    setInfoMessage( reminder ? "g.RCReminderEmailSent" : "g.RCEmailSent" , new String[]{user.getFullname(), user.getEmail()} );

        return sent;
    }
    
    
    

    public void chargeCreditIfNeeded( RcCheck rc, RcRater rater ) throws Exception
    {
        if( rc.getCreditId()>0 )
            return;
        
        if( rc.getOrg()!=null && rc.getOrg().getOrgCreditUsageType().getUnlimited() && rc.getOrg().getOrgCreditUsageEndDate()!=null && rc.getOrg().getOrgCreditUsageEndDate().after( new Date() ) )
            return;
        
        // First check.
        if( rcFacade!=null )
            rcFacade = RcFacade.getInstance();
        
        // wait a bit then check.
        Thread.sleep(300);
        int[] d = rcFacade.getRcCheckCreditInfo( rc.getRcCheckId() );
        if( d!=null && d[0]>0 )
        {
            rc.setCreditId(d[0]);
            rc.setCreditIndex(d[1]);
            rcFacade.saveRcCheck(rc, false );
            return;
        }
        
        RefCreditUtils rcu = new RefCreditUtils();
        rcu.chargeCreditsIfNeeded(rc.getOrg(), rc, rater );
    }
    
    
    
    
    public void performRcCheckCompletionIfReady( RcCheck rc, boolean updateIfAlreadyComplete, boolean adminOverride) throws Exception
    {
        try
        {
            // No need to do this in AdminOverride
            if( adminOverride )
                return;

            if( rc.getRcCheckStatusType().getIsComplete() && rc.getEmailReportsToCandidate()==1 && rc.getCandidateReportSendDate()==null )
            {
                RcResultReportingUtils rrru = new RcResultReportingUtils();
                rrru.sendCandidateFeedbackReportEmails(rc, 0, false, rc.getLocale() );
            }
            
            // already complete.
            if( !updateIfAlreadyComplete && rc.getRcCheckStatusType().getCompleteOrHigher() )
            {
                // LogService.logIt("RcCheckUtils.performRcCheckCompletionIfReady() RcCheck is already complete or higher. status=" + rc.getRcCheckStatusType().getName() + ", rcCheckId=" + rc.getRcCheckId() );
                return;                
            }

            // check for expiration.
            boolean pastExpireDate = rc.getExpireDate()!=null && rc.getExpireDate().before(new Date());

            //if( !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
            //    performRcCandidateCompletionIfReady( rc );
            boolean candComplete = rc.getRcCandidateStatusType().getIsCompletedOrHigher();

            if( !pastExpireDate && !candComplete )
            {
                boolean skipCandidate = !rc.getRequiresResumeOrAnyCandidateInputOrSelfRating();
                
                // need input but it's only raters (no resume and no candidate input and no candidate self-ratings). So check to see if there are enough raters.
                if( !skipCandidate && rc.getMinRaters()>0 && 
                        rc.getRcScript()!=null && !rc.getRcScript().getHasAnyCandidateInput() && 
                        (!rc.getRcScript().getCollectResumeB() || rc.getResumeComplete()>0) &&
                        !rc.getCollectRatingsFmCandidate()  )
                {
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance(); 
                    int raterCt = 0;
                    int superCt = 0;
                    for( RcRater r : rcFacade.getRcRaterList( rc.getRcCheckId() ))
                    {
                        if( r.getIsCandidateOrEmployee() )
                            continue;

                        if( r.getRcRaterStatusType().getIsDeactivated() )
                            continue;
                        
                        raterCt++;
                        if( r.getRcRaterRoleType().getIsSupervisorOrManager() )
                            superCt++;
                    }
                    if( raterCt>=rc.getMinRaters() && superCt>=rc.getMinSupervisors() )
                        skipCandidate = true;                    
                }                                
                
                if( skipCandidate )
                {
                    // LogService.logIt("RcCheckUtils.performRcCheckCompletionIfReady() AAA.3 Candidate is not complete but no other input than raters and we have enough raters. So setting Candidate to complete. rcCheckId=" + rc.getRcCheckId() );                    
                    rc.setCandidateCompleteDate( new Date() );
                    rc.setRcCandidateStatusTypeId( RcCandidateStatusType.COMPLETED.getRcCandidateStatusTypeId() );
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance(); 
                    
                    if( !adminOverride )
                    {   
                        rc.setPercentComplete( computeRcCheckPercentComplete(rc));
                        rcFacade.saveRcCheck(rc, false );
                    }
                    candComplete = true;
                }                    
            }
            
            // Check Percent Complete            
             float pc = adminOverride ? rc.getPercentComplete() : computeRcCheckPercentComplete(rc);
            if( !adminOverride && pc!=rc.getPercentComplete() )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance(); 
                rc.setPercentComplete( pc );
                if( !adminOverride )
                    rcFacade.saveRcCheck(rc, false );            
            }
            
            if( !pastExpireDate && !candComplete )
            {
                // LogService.logIt("RcCheckUtils.performRcCheckCompletionIfReady() Candidate not yet complete and rcCheck is not expired. rcCheckId=" + rc.getRcCheckId() );
                return;                                
            }
            
            // not expired and not all raters are complete and candidate is complete.
            if( !pastExpireDate && candComplete )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();                
                if( !rcFacade.getAreAllRcRatersCompleteOrHigher(rc.getRcCheckId()) )
                {                    
                    // LogService.logIt("RcCheckUtils.performRcCheckCompletionIfReady() All current RcRaters are not yet complete and not expired. rcCheckId=" + rc.getRcCheckId() );
                    return;      
                }
            }
            
            if( pastExpireDate )
            {
                boolean anyCompleteRaters = false;
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();                
                for( RcRater r : rcFacade.getRcRaterList( rc.getRcCheckId() ))
                {
                    //if( r.getIsCandidateOrEmployee() )
                    //    continue;
                    if( r.getRcRaterStatusType().getIsComplete() )
                        anyCompleteRaters=true;
                }
                // no completed raters, so mark it as expired.
                if( !anyCompleteRaters )
                {
                    // mark as expired
                    rc.setRcCheckStatusTypeId( RcCheckStatusType.EXPIRED.getRcCheckStatusTypeId() );
                    if( !adminOverride )
                        rcFacade.saveRcCheck(rc, false);
                    return;
                }
            }
            
            //int minReqRaters = rc.getMinRaters();
           // if( rc.getCollectRatingsFmCandidate() )
           //     minReqRaters++;
            //List<RcRater> rcrl = rcFacade.getRcRaterList( rc.getRcCheckId() );
            //if( rcrl.size()<minReqRaters )
            //{
            //    LogService.logIt( "RcCheckUtils.performRcCheckCompletionIfReady() There are not enough raters. rcCheckId=" + rc.getRcCheckId() + ", minReqRaters=" + minReqRaters );
            //    return;                   
            //}
            
            // looks like it's complete. Check for susp.  
            if( !adminOverride )
                doCheckForSuspiciousActivity( rc );
                        
            // LogService.logIt( "RcCheckUtils.performRcCheckCompletionIfReady() Setting RcCheck to complete!" );
            rc.setRcCheckStatusTypeId( RcCheckStatusType.COMPLETED.getRcCheckStatusTypeId() );
            rc.setCompleteDate( new Date() );
            rc.setPercentComplete(100);
            rc.setOverallScore( computeRcCheckOverallScore( rc ));
            rc.setScoreDate( new Date() );
            rc.setRcCheckScoringStatusTypeId( RcCheckScoringStatusType.SCORED.getRcCheckScoringStatusTypeId() );
            
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();   
            if( !adminOverride )
                rcFacade.saveRcCheck(rc, false);
            Tracker.addRcCheckComplete(); 
            
            if( rc.getEmailReportsToCandidate()==1 && rc.getCandidateReportSendDate()==null )
            {
                RcResultReportingUtils rrru = new RcResultReportingUtils();
                rrru.sendCandidateFeedbackReportEmails(rc, 0, false, rc.getLocale() );
            }
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcCheckUtils.performRcCheckCompletionIfReady() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) );
            throw e;
        }
    }
    
    public void prepSuspiciusActivityForReporting( RcCheck rc, Locale locale ) throws Exception
    {
        if( rc.getRcSuspiciousActivityList()==null )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rc.setRcSuspiciousActivityList( rcFacade.getRcSuspiciousActivityList(rc.getRcCheckId()));            
        }
        if( rc.getRcRaterList()==null )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rc.setRcRaterList( rcFacade.getRcRaterList( rc.getRcCheckId() ));
        }

        for( RcRater rcr : rc.getRcRaterList() )
        {
            if( rcr.getUser()==null )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                rcr.setUser( userFacade.getUser( rcr.getUserId() ));
            }
        }
        
        List<RcSuspiciousActivity> raterRaterSa = new ArrayList<>();
        for( RcSuspiciousActivity sa : rc.getRcSuspiciousActivityList() )
        {
            if( sa.getRcSuspiciousActivityType().getIsAnyRaterIpMatch() )
                raterRaterSa.add( sa );
                        
            // Make sure the user is there. 
            if( sa.getUser()==null && sa.getRcRaterId()>0 )
            {
                for( RcRater rcr : rc.getRcRaterList() )
                {
                    if( rcr.getRcRaterId()==sa.getRcRaterId() )
                        sa.setUser( rcr.getUser() );
                }
            }
        }

        if( locale==null && rc.getLangCode()!=null && !rc.getLangCode().isBlank() )
            locale = I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() );
        if( locale==null )
            locale = Locale.US;
        
        
        List<Long> ridl = new ArrayList<>();
        
        for( RcSuspiciousActivity rca : raterRaterSa )
        {
            StringBuilder sb = new StringBuilder();
            RcRater rcr;
            
            if( rca.getRcSuspiciousActivityType().getIsCandidateRaterMatch() )
            {
                sb.append( "\n");
                sb.append( rc.getUser().getFullname() );

                if( rc.getIpAddress()!=null && !rc.getIpAddress().isBlank() )
                    sb.append( ", " + rc.getIpAddress() );
                if( rc.getUserAgent()!=null && !rc.getUserAgent().isBlank() )
                    sb.append( ", " + rc.getUserAgent() );                
            }
            
            ridl.clear();
            if( rca.getRcSuspiciousActivityType().getIsCandidateRaterMatch() )
                ridl.add( rca.getRcRaterId() );
            else
                ridl.addAll( getLongSet( rca.getNote() ) );
            
            for( Long rid : ridl )
            {
                rcr = null;
                for( RcRater r : rc.getRcRaterList() )
                {
                    if( r.getRcRaterId()==rid )
                    {
                        rcr=r;
                        break;
                    }
                }
                
                if( rcr!=null && !rcr.getIsCandidateOrEmployee() )
                {
                        // if( sb.length()>0 )
                    sb.append( "\n");
                    sb.append( rcr.getUser().getFullname() );
                    
                    if( rcr.getIpAddress()!=null && !rcr.getIpAddress().isBlank() )
                        sb.append( ", " + rcr.getIpAddress() );
                    if( rcr.getUserAgent()!=null && !rcr.getUserAgent().isBlank() )
                        sb.append( ", " + rcr.getUserAgent() );                        
                }
            }
            if( sb.length()>0 )
                rca.setSpecialNote( MessageFactory.getStringMessage( locale, "g.RCMultRatersSaX", new String[]{sb.toString()}) );                        
        }
    }
    
    public void doCheckForSuspiciousActivity( RcCheck rc ) throws Exception
    {
        if( rcFacade==null )
            rcFacade = RcFacade.getInstance();
        
        List<RcRater> rcrl = rcFacade.getRcRaterList( rc.getRcCheckId() );
        List<RcSuspiciousActivity> sal = rcFacade.getRcSuspiciousActivityList( rc.getRcCheckId() );

        RcSuspiciousActivity raterRaterSa = null;
        for( RcSuspiciousActivity sa : sal )
        {
            if( sa.getRcSuspiciousActivityType().getIsRaterRaterMatch() )
                raterRaterSa = sa;
        }

        // Check for Rater Rater match.
        Set<Long> ll;
        for( RcRater rcr : rcrl )
        {
            if( rcr.getIsCandidateOrEmployee() )
                continue;
            
            for( RcRater rcr2 : rcrl )
            {
                if( rcr.getRcRaterId()==rcr2.getRcRaterId() )
                    continue;
                
                // Rater / Rater Match
                if( rcr.getIpAddress()!=null && rcr2.getIpAddress()!=null && rcr.getIpAddress().equalsIgnoreCase( rcr2.getIpAddress() ))
                {
                    if( raterRaterSa ==null )
                    {
                        raterRaterSa = new RcSuspiciousActivity();
                        raterRaterSa.setRcCheckId( rc.getRcCheckId() );
                        raterRaterSa.setRcRaterId(rcr.getRcRaterId() );
                        raterRaterSa.setCreateDate(new Date());
                        raterRaterSa.setSuspiciousActivityTypeId( RcSuspiciousActivityType.RATERS_SAME_IP.getRcSuspiciousActivityTypeId() );
                        sal.add( raterRaterSa );
                    }
                    ll = getLongSet( raterRaterSa.getNote() );
                    ll.add( rcr2.getRcRaterId() );
                    ll.add( rcr.getRcRaterId() );
                    raterRaterSa.setNote( longSetToStr( ll ) );
                    rcFacade.saveRcSuspiciousActivity(raterRaterSa);
                }
            }
        }
        
        RcSuspiciousActivity sa = null;
        for( RcRater rcr : rcrl )
        {
            if( rcr.getIsCandidateOrEmployee() )
                continue;
            
            // Rater Candidate Match
            if( rcr.getIpAddress()!=null && rc.getIpAddress()!=null && !rc.getIpAddress().isBlank() && rcr.getIpAddress().equalsIgnoreCase( rc.getIpAddress() ))
            {
                boolean hasSameUa = rcr.getUserAgent()!=null && !rcr.getUserAgent().isBlank() && rc.getUserAgent()!=null && rcr.getUserAgent().equalsIgnoreCase( rc.getUserAgent());

                sa = null;
                for( RcSuspiciousActivity sx : sal )
                {
                    if( sx.getRcSuspiciousActivityType().getIsCandidateRaterMatch() && sx.getRcRaterId()==rcr.getRcRaterId() )
                    {
                        sa = sx;
                        break;
                    }
                }
                
                if( sa == null )          
                {
                    sa = new RcSuspiciousActivity();
                    sa.setRcCheckId( rc.getRcCheckId() );
                    sa.setRcRaterId(rcr.getRcRaterId() );                            
                    sa.setCreateDate(new Date());
                    sa.setSuspiciousActivityTypeId( hasSameUa ? RcSuspiciousActivityType.SAME_IP_UA.getRcSuspiciousActivityTypeId() : RcSuspiciousActivityType.SAME_IP.getRcSuspiciousActivityTypeId() );
                    sa.setNote( Long.toString(rcr.getRcRaterId()) );
                    rcFacade.saveRcSuspiciousActivity(sa);
                    sal.add(sa);
                }
                else
                {
                    sa.setSuspiciousActivityTypeId( hasSameUa ? RcSuspiciousActivityType.SAME_IP.getRcSuspiciousActivityTypeId() : RcSuspiciousActivityType.SAME_IP.getRcSuspiciousActivityTypeId() );
                    sa.setNote( Long.toString(rcr.getRcRaterId()) );
                    rcFacade.saveRcSuspiciousActivity(sa);
                }                            
            }            
        }
        
    }
    
    public String longSetToStr( Set<Long> l )
    {
        StringBuilder sb = new StringBuilder();
        for( Long val : l )
        {
            if( sb.length()>0 )
                sb.append(",");            
            sb.append( val.toString() );
        }
        return sb.toString();
    }
    
    public Set<Long> getLongSet( String inStr )
    {
        Set<Long> out = new HashSet<>();
        
        if( inStr==null || inStr.isBlank() )
            return out;
        
        for( String s : inStr.split(","))
        {
            if( s.isBlank() )
                continue;
            out.add(Long.valueOf(s) );
        }
        return out;
    }

    public static boolean getIsRaterOrCandidateCompleteOrLowerButBeforeExpireDateAndCanAccess( RcCheck rc, RcRater rater, RefUserType refUserType )
    {
        if( rc==null || rc.getExpireDate()==null || rc.getExpireDate().before( new Date() ) )
            return false;
        
        if( refUserType.getIsCandidate() )
        {
            return rc.getRcCandidateStatusTypeId()<RcCandidateStatusType.COMPLETED.getRcCandidateStatusTypeId() || (rc.getDisallowReentry()==0 && rc.getRcCandidateStatusType().getIsComplete());
        }
        
        return rater==null ? false : rater.getRcRaterStatusTypeId()<RcRaterStatusType.COMPLETED.getRcRaterStatusTypeId() || (rc.getDisallowReentry()==0 && rater.getRcRaterStatusType().getIsComplete());
    }
    
    
    public static boolean getIsRaterOrCandidateCompleteButBeforeExpireDateAndCanReenter( RcCheck rc, RcRater rater, RefUserType refUserType )
    {
        if( rc==null || rc.getExpireDate()==null || rc.getExpireDate().before( new Date() ) || rc.getDisallowReentry()==1 )
            return false;
        
        if( refUserType.getIsCandidate() )
            return rc.getRcCandidateStatusType().getIsComplete();
        
        return rater==null ? false : rater.getRcRaterStatusType().getIsComplete();
    }
    
    
    public void doExpireOrCompleteRcCheck( RcCheck rc, boolean adminOverride ) throws Exception
    {
        if( rc.getRcCheckStatusType().getCompleteOrHigher() )
            return;
        
        if( rc.getExpireDate()!=null && rc.getExpireDate().after( new Date() ) )
            return;

        if( rcFacade==null )
            rcFacade=RcFacade.getInstance();
        boolean hasCompleteRater = false;
        //boolean candidateComplete = rc.getRcCandidateStatusType().getIsCompletedOrHigher();
        
        //if( !candidateComplete )
        //{
        //    performRcCandidateCompletionIfReady( rc );
        //    candidateComplete = rc.getRcCandidateStatusType().getIsCompletedOrHigher();
        //}
        for( RcRater r : rcFacade.getRcRaterList( rc.getRcCheckId() ) )
        {
            if( r.getRcRaterStatusType().getIsComplete() && !r.getIsCandidateOrEmployee() )
                hasCompleteRater=true;
        }
        if( hasCompleteRater )
        {
            //if( !rc.getRcCandidateStatusType().getIsCompletedOrHigher() && candidateComplete )
            //    rc.setRcCandidateStatusTypeId( RcCandidateStatusType.COMPLETED.getRcCandidateStatusTypeId() );            
            performRcCheckCompletionIfReady(rc, false, adminOverride);
            
        }
        else
        {
            rc.setRcCheckStatusTypeId( RcCheckStatusType.EXPIRED.getRcCheckStatusTypeId() );
            if( !adminOverride )
                rcFacade.saveRcCheck(rc, false);            
        }
    }
    
    
    /*
     returns
        int[0] = 
    */
    public int[] sendProgressUpdateForRaterOrCandidateComplete( RcCheck rc, RcRater rater, boolean forceSend) throws Exception
    {
        // No progress report for candidate completion unless the whole thing is complete.
        if( rater!=null && !rc.getRcCheckStatusType().getIsComplete() && rater.getIsCandidateOrEmployee() )
            return new int[2];
        
        if( rc.getRcCheckStatusType().getIsComplete() && rc.getTestKeyId()>0 )
        {
            try
            {
                if( eventFacade==null )
                    eventFacade=EventFacade.getInstance();
                TestKey tk = eventFacade.getTestKey(rc.getTestKeyId() );
                if( tk==null )
                    throw new Exception( "Could not find TestKey for testKeyId=" + rc.getTestKeyId() );
                
                if( tk.getResultPostUrl()!=null && !tk.getResultPostUrl().isBlank() && tk.getProductTypeId()==ProductType.REFERENCECHECK.getProductTypeId() ) // && tk.getApiTypeId()>=0 )
                {
                    ResultPoster resultPoster = ResultPosterFactory.getResultPosterInstance(tk, rc);
                    if( resultPoster!=null )
                    {
                        LogService.logIt( "RcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete() BBB.1 Posting API Test Results. testKeyId=" + rc.getTestKeyId() +", rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) + " rcRaterId=" + (rater==null ? "null" : rater.getRcRaterId() ) + ", forceSend=" + forceSend  );
                        resultPoster.postTestResults();
                    }
                }

                if( !tk.getTestKeyStatusType().getIsCompleteOrHigher() )
                {
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() );
                    eventFacade.saveTestKey(tk);
                }
                
            }
            catch( Exception e )
            {
                LogService.logIt( e, "RcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete() XXX.2 rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) + " rcRaterId=" + (rater==null ? "null" : rater.getRcRaterId() ) + ", forceSend=" + forceSend  );
                
            }
            
        }
        
        try
        {
            // if( rc.getLocale()==null && rc.getLangCode()!=null && !rc.getLangCode().isBlank() )
            // force admin locale.
            Locale messageLocale = I18nUtils.getLocaleFromCompositeStr( rc.getLangCode());

            RcMessageUtils rcmu = new RcMessageUtils();
            return rcmu.sendProgressUpdateForRaterOrCandidateComplete(rc, rater, messageLocale, forceSend);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RcCheckUtils.sendProgressUpdateForRaterOrCandidateComplete() ZZZ.2 rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) + " rcRaterId=" + (rater==null ? "null" : rater.getRcRaterId() ) + ", forceSend=" + forceSend  );
        }
        return new int[2];        
    }

    
    public void performPreliminarySubstitutions( RcItem rcItem, RcCheck rc, Locale locale )
    {
        if( locale==null )
            locale = I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() );
        if( locale==null )
           locale=rc.getLocale();
        if( locale==null )
            locale=Locale.US;
        
        String[] otherNames = rc.getRcOrgPrefs()==null ? null : rc.getRcOrgPrefs().getOtherRoleTypeNames(rc.getRcSuborgPrefs());
        rcItem.setChoice1( doPreliminarySubstitutions( rcItem.getChoice1(), otherNames ) );
        rcItem.setChoice2( doPreliminarySubstitutions( rcItem.getChoice2(), otherNames ) );
        rcItem.setChoice3( doPreliminarySubstitutions( rcItem.getChoice3(), otherNames ) );
        rcItem.setChoice4( doPreliminarySubstitutions( rcItem.getChoice4(), otherNames ) );
        rcItem.setChoice5( doPreliminarySubstitutions( rcItem.getChoice5(), otherNames ) );
        rcItem.setChoice6( doPreliminarySubstitutions( rcItem.getChoice6(), otherNames ) );
        rcItem.setChoice7( doPreliminarySubstitutions( rcItem.getChoice7(), otherNames ) );
        rcItem.setChoice8( doPreliminarySubstitutions( rcItem.getChoice8(), otherNames ) );
        rcItem.setChoice9( doPreliminarySubstitutions( rcItem.getChoice9(), otherNames ) );
        rcItem.setChoice10( doPreliminarySubstitutions( rcItem.getChoice10(), otherNames ) );
    }
    
    public String doPreliminarySubstitutions( String s, String[] otherNames )
    {
        if( s==null || s.isBlank() )
            return s;
        if( s.contains( "[OTHERROLENAME1]" ) )
        {
            if( otherNames==null || otherNames[0]==null )
                return null;
            return otherNames[0];
        }
        if( s.contains( "[OTHERROLENAME2]" ) )
        {
            if( otherNames==null || otherNames[1]==null )
                return null;
            return otherNames[1];
        }
        if( s.contains( "[OTHERROLENAME3]" ) )
        {
            if( otherNames==null || otherNames[2]==null )
                return null;
            return otherNames[2];
        }
        return s;        
    }
    
    
    /**
[REFCHECKTYPENAME]
[CURRENTCOMPANY]
[PASTCOMPANY]
[CANDIDATENAME]
[CANDIDATETYPENAME]
[CANDIDATEROLERESP]
[CANDIDATEINPUTSTR1]
[CANDIDATEINPUTSTR2]
[CANDIDATEINPUTSTR3]
[CANDIDATEINPUTSTR4]
[CANDIDATEINPUTSTR5]     * 
     * @param s
     * @return 
     */
    public String performSubstitutions( String s, RcCheck rc, RcRater rater, Locale locale )
    {
        if( s==null || s.isBlank() )
            return s;
        
        if( locale==null )
            locale=rc.getLocale();
        if( locale==null )
            locale = I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() );
        if( locale==null )
            locale=Locale.US;
        
        s = StringUtils.replaceStr( s, "[REFCHECKTYPENAME]", rc.getRcCheckType().getName(locale) );
        s = StringUtils.replaceStr( s, "[CURRENTCOMPANY]", rc.getOrg().getName() );
        
        s = StringUtils.replaceStr( s, "[PASTCOMPANY]", rater!=null && rater.getCompanyName()!=null && !rater.getCompanyName().isBlank() ? rater.getCompanyName() : MessageFactory.getStringMessage(locale, "g.XRNoCandPastCompanyFnd" ) );
        
        // s = StringUtils.replaceStr( s, "[CANDIDATENAME]", rater.getRcRaterType().getIsRater() ? rc.getUser().getFullname() : MessageFactory.getStringMessage(locale, "g.RCSelfReference1" ) );
        s = StringUtils.replaceStr( s, "[CANDIDATENAME]", rc.getUser().getFullname() );
        s = StringUtils.replaceStr( s, "[CANDIDATE]", rc.getUser().getFullname() );
        s = StringUtils.replaceStr( s, "[CANDIDATENAMEABSOLUTE]", rc.getUser().getFullname() );
        s = StringUtils.replaceStr( s, "[CANDIDATETYPENAME]", MessageFactory.getStringMessage( locale, rc.getRcCheckType().getIsPrehire() ? "g.Candidate" : "g.Employee" ));
        s = StringUtils.replaceStr( s, "[CANDIDATEROLERESP]", rater!=null && rater.getCandidateRoleResp()!=null && !rater.getCandidateRoleResp().isBlank() ? rater.getCandidateRoleResp() : MessageFactory.getStringMessage(locale, "g.XRNoCandRoleRespFnd" ) );
        s = StringUtils.replaceStr( s, "[CANDIDATEINPUTSTR1]", rc.getCandidateInputStr1() );
        s = StringUtils.replaceStr( s, "[CANDIDATEINPUTSTR2]", rc.getCandidateInputStr2() );
        s = StringUtils.replaceStr( s, "[CANDIDATEINPUTSTR3]", rc.getCandidateInputStr3() );
        s = StringUtils.replaceStr( s, "[CANDIDATEINPUTSTR4]", rc.getCandidateInputStr4() );
        s = StringUtils.replaceStr( s, "[CANDIDATEINPUTSTR5]", rc.getCandidateInputStr5() );
        
        if( rater!=null )
            s = StringUtils.replaceStr( s, "[OBSERVATIONPERIOD]", getRaterObservationPeriod( rater, locale ) );

        return s;
    }
    

    public String performPostXhtmlSubstitutions( String s, RcCheck rc, RcRater rater, Locale locale )
    {
        if( s==null || s.isBlank() )
            return s;
        
        if( locale==null )
            locale=rc.getLocale();
        if( locale==null )
            locale = I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() );
        if( locale==null )
            locale=Locale.US;
        
        s = StringUtils.replaceStr( s, "[CANDIDATEROLERESP]", rater!=null && rater.getCandidateRoleResp()!=null && !rater.getCandidateRoleResp().isBlank() ? rater.getCandidateRoleResp() : MessageFactory.getStringMessage(locale, "g.XRNoCandRoleRespFnd" ) );
        s = StringUtils.replaceStr( s, "[CANDIDATEINPUTSTR1]", rc.getCandidateInputStr1() );
        s = StringUtils.replaceStr( s, "[CANDIDATEINPUTSTR2]", rc.getCandidateInputStr2() );
        s = StringUtils.replaceStr( s, "[CANDIDATEINPUTSTR3]", rc.getCandidateInputStr3() );
        s = StringUtils.replaceStr( s, "[CANDIDATEINPUTSTR4]", rc.getCandidateInputStr4() );
        s = StringUtils.replaceStr( s, "[CANDIDATEINPUTSTR5]", rc.getCandidateInputStr5() );
        
        if( rater!=null )
            s = StringUtils.replaceStr( s, "[OBSERVATIONPERIOD]", getRaterObservationPeriod( rater, locale ) );

        return s;
    }

    
    
    public String getRaterObservationPeriod( RcRater rater, Locale locale )
    {
        if( rater==null || rater.getObservationStartDate()==null || rater.getObservationEndDate()==null )
            return "";
        
        Calendar start = new GregorianCalendar();
        start.setTime( rater.getObservationStartDate() );
        Calendar end = new GregorianCalendar();
        end.setTime( rater.getObservationEndDate() );        
        String[] params = new String[] { Integer.toString( start.get(Calendar.MONTH)+1),Integer.toString( start.get(Calendar.YEAR)),Integer.toString( end.get(Calendar.MONTH)+1),Integer.toString( end.get(Calendar.YEAR)) };         
        return MessageFactory.getStringMessage( locale, "g.RCObservationPdXY", params);
    }
    
    public static void correctRcRaterListForReporting( RcCheck rc )
    {
        if( rc.getRcRaterList()==null || !rc.getCollectRatingsFmCandidate() )
            return;
        
        List<RcRater> rlst = new ArrayList<>();
        for( RcRater rater : rc.getRcRaterList() )
        {
            if( !rater.getIsCandidateOrEmployee() )
                continue;
            rlst.add(rater);
        }
        for( RcRater rater : rc.getRcRaterList() )
        {
            if( rater.getIsCandidateOrEmployee() )
                continue;
            rlst.add(rater);
        }
        rc.setRcRaterList(rlst);
    }

    
    public static void addCandidateRoleRespToRaterRoleRespItemResponses( RcCheck rc, Locale loc ) throws Exception
    {
        // No raters, or not prehire
        if( rc.getRcRaterList()==null )
            return;
        
        init();
        RcRater rater;
        for( RcItemWrapper rciw : rc.getRcScript().getAllItemWrapperList() )            
        {
            // Not a "Prior Role Item"
            if( !rcItemIdsForPriorRole.contains( (int) rciw.getRcItemId() ) )
                    continue;
            
            if( !rciw.getHasRatingInfoToShow() )
                continue;
            
            for( RcRating rcr : rciw.getRcRatingList() )
            {
                // skip ratings from candidate
                if( rcr.getRcRaterId()==rc.getCandidateRcRaterId() )
                    continue;
                
                // get rater for this rating.
                rater = rc.getRcRaterForRcRaterId( rcr.getRcRaterId() );
                if( rater==null || 
                    rater.getCandidateRoleResp()==null || 
                    rater.getCandidateRoleResp().isBlank() || 
                    !rater.getRcRaterSourceType().getIsCandidateOrEmployee() )
                    continue;
                
                // set subtext.
                rcr.setSubtext( MessageFactory.getStringMessage( loc, rc.getRcCheckType().getIsPrehire() ? "g.RCFmCandidate" : "g.RCFmEmployee" , new String[]{rater.getCandidateRoleResp()}));
            }
        }        
    }
    
    

    
}
