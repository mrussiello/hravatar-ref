/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.ref.ai;

import com.tm2ref.entity.essay.UnscoredEssay;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcItem;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.ref.RcRating;
import com.tm2ref.essay.AiEssayScoringThread;
import com.tm2ref.essay.AiEssayScoringUtils;
import com.tm2ref.essay.EssayFacade;
import com.tm2ref.essay.EssayScoreStatusType;
import com.tm2ref.essay.UnscoredEssayType;
import com.tm2ref.file.FileContentType;
import com.tm2ref.file.FileUploadFacade;
import com.tm2ref.file.UploadedFileHelpUtils;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
import java.util.Date;

/**
 *
 * @author miker
 */
public class RcRatingAiProcessorThread implements Runnable {

    static int MIN_TEXT_LENGTH_FOR_AI_SCORE = 100;
    static int MIN_TEXT_LENGTH_FOR_AI_SUMMARY = 300;
    static final int DUMMY_ESSAY_PROMPT_ID = 999999;


    EssayFacade essayFacade;
    FileUploadFacade fileUploadFacade;
    RcFacade rcFacade;

    RcCheck rcCheck;
    RcRater rcRater;
    RcRating rcRating;
    RcItem rcItem;
    UploadedFileHelpUtils uploadedFileHelpUtils;

    public RcRatingAiProcessorThread(RcCheck rcCheck, RcRater rcRater, RcRating rcRating, RcItem rcItem)
    {
        this.rcCheck = rcCheck;
        this.rcRater = rcRater;
        this.rcRating = rcRating;
        this.rcItem = rcItem;
    }

    @Override
    public void run()
    {
        try
        {
            initiateAiProcessing();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcRatingAiProcessorThread.run() " + toString() );
        }
    }



    private void initiateAiProcessing()
    {
        try
        {
            if( !AiEssayScoringUtils.getAiEssayScoringOn() )
            {
                LogService.logIt( "RcRatingAiProcessorThread.initiateAiProcessing() AI Processing is NOT ON. " + toString());
                return;
            }

            if( !getReadyForAiProcessing() )
            {
                LogService.logIt( "RcRatingAiProcessorThread.initiateAiProcessing() Inputs are not ready for AI Processing is NOT ON. " + toString());
                return;
            }

            LogService.logIt( "RcRatingAiProcessorThread.initiateAiProcessing() START " + toString());

            boolean needsAiScore = rcItem.getAiScoringOk()==1;
            boolean needsAiSummary = rcItem.getAiSummaryOk()==1;

            // IMPORTANT - When an audio or video is converted to SpeechText it overwrites the rcRating.text field. So this field always holds the latest comments.
            String theText = rcRating.getText()==null ? "" : rcRating.getText().trim();

            if( rcRating.getCandidateRcUploadedUserFile()!=null)
            {
                FileContentType fct = rcRating.getCandidateRcUploadedUserFile().getFileContentType();

                if( fct.getIsAnyTextFile() )
                {
                    String uploadedText = rcRating.getCandidateRcUploadedUserFile().getUploadedText();
                    if( uploadedText==null || uploadedText.isBlank() )
                    {
                        if( uploadedFileHelpUtils==null )
                            uploadedFileHelpUtils = new UploadedFileHelpUtils();
                        uploadedText = uploadedFileHelpUtils.parseUploadedUserFileForText(rcRating.getCandidateRcUploadedUserFile());
                    }

                    // uploaded text overwrites any comments.
                    if( uploadedText!=null && !uploadedText.isBlank() )
                    {
                        //if( theText.isBlank() )
                        theText=uploadedText;

                       // else
                       //     theText += " " + uploadedText;
                    }
                }
            }
            
            if( theText==null || theText.isBlank() )
            {
                LogService.logIt( "RcRatingAiProcessorThread.initiateAiProcessing() CCC.0 theText is null or empty. Returning without AI. " + toString() );
                return;
            }

            LogService.logIt( "RcRatingAiProcessorThread.initiateAiProcessing() CCC.1 theText.length=" + (theText==null ? "null" : theText.length()) );

            if( needsAiScore && theText.length()<MIN_TEXT_LENGTH_FOR_AI_SCORE )
            {
                LogService.logIt( "RcRatingAiProcessorThread.initiateAiProcessing() CCC.2 text length too short for AI Scoring. theText.length=" + theText.length() );
                needsAiScore = false;
            }

            if( needsAiSummary && theText.length()<MIN_TEXT_LENGTH_FOR_AI_SUMMARY )
            {
                LogService.logIt( "RcRatingAiProcessorThread.initiateAiProcessing() CCC.2 text length too short for AI Summary. theText.length=" + theText.length() );
                needsAiSummary = false;
            }

            if( !needsAiScore && !needsAiSummary)
            {
                LogService.logIt( "RcRatingAiProcessorThread.initiateAiProcessing() CCC.3 both needsAiScore and needsAiSummary are false. Nothing to do. Returning." );
                return;
            }

            if( essayFacade==null )
                essayFacade=EssayFacade.getInstance();

            UnscoredEssay ue = essayFacade.getUnscoredEssay(rcCheck.getRcCheckId(), rcRating.getRcRatingId(), UnscoredEssayType.RC_COMMENT.getUnscoredEssayTypeId() );

            if( ue==null )
            {
                ue = new UnscoredEssay();
                ue.setEssay(theText);
                ue.setLocaleStr(rcCheck.getLangCode());
                ue.setUserId(rcRater.getUserId());
                ue.setEssayPromptId(DUMMY_ESSAY_PROMPT_ID);
                ue.setUnscoredEssayTypeId(UnscoredEssayType.RC_COMMENT.getUnscoredEssayTypeId());
                ue.setCt5ItemId(rcItem.getRcItemId());
                ue.setRcCheckId( rcRating.getRcCheckId());
                ue.setNodeSequenceId(rcRating.getRcRatingId());
                ue.setScoreStatusTypeId(EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId());
                ue.setSubnodeSequenceId(UnscoredEssayType.RC_COMMENT.getUnscoredEssayTypeId());
                ue.setCreateDate(new Date());

                ue.setTotalWords(StringUtils.numWords(theText));

                essayFacade.saveUnscoredEssay(ue);
            }

            if( ue.getEssayScoreStatusType().unsubmitted() )
            {
                submitForAi( ue, needsAiScore, needsAiSummary );
            }

        }catch (Exception e)
        {
            LogService.logIt(e, "RcRatingAiProcessorThread.initiateAiProcessing() " + toString());
        }
    }

    private void submitForAi(UnscoredEssay ue, boolean needsAiScore, boolean needsAiSummary ) throws Exception
    {
        try
        {
            String promptStr = this.rcItem.getAiPrompt();
            if( promptStr==null || promptStr.isBlank() )
                promptStr=rcItem.getQuestionCandidate();
            if( promptStr==null || promptStr.isBlank() )
                promptStr = rcItem.getQuestion();

            LogService.logIt( "RcRatingAiProcessorThread.submitForAi() AAA.1 promptStr=" + promptStr + ", " + toString());

            if( promptStr==null || promptStr.isBlank() )
                throw new Exception( "PromptStr is invalid (null or blank)." );

            int summaryCode = 0;  // score only
            if(needsAiScore && needsAiSummary)
                summaryCode=1; // score and summary
            if( !needsAiScore && needsAiSummary )
                summaryCode = 2;  // summary only

            if( needsAiSummary )
            {
                rcRating.clearAiSummary();
                rcRating.setAiSummaryStatusTypeId( EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId() );
            }

            if( needsAiScore )
            {
                rcRating.clearAiScores();
                rcRating.setAiScoresStatusTypeId( EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId() );
            }

            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rcFacade.saveRcRating(rcRating);

            AiEssayScoringThread aiest = new AiEssayScoringThread(ue, true, true, promptStr, rcItem.getIdealResponse(), rcItem.getAiInstructions(), summaryCode );
            LogService.logIt("RcRatingAiProcessorThread.submitForAi() STARTING AI Scoring inline. unscoredEssayId=" + ue.getUnscoredEssayId() );
            aiest.performEssayScore();
        }
        catch (Exception e)
        {
            boolean chg = false;
            if( rcRating.getAiScoresStatusTypeId()==EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId() )
            {
                rcRating.setAiScoresStatusTypeId(EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId());
                chg=true;
            }
            if( rcRating.getAiSummaryStatusTypeId()==EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId() )
            {
                rcRating.setAiSummaryStatusTypeId(EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId());
                chg=true;
            }
            if( chg )
            {
                try
                {                    
                    if( rcFacade==null )
                        rcFacade=RcFacade.getInstance();
                    rcFacade.saveRcRating(rcRating);                    
                }
                catch( Exception ee )
                {
                    LogService.logIt(ee, "RcRatingAiProcessorThread.submitForAi() WWW.1 " + toString());
                }
            }
            
            LogService.logIt(e, "RcRatingAiProcessorThread.submitForAi() XXX.1 " + toString());
            throw e;
        }
    }

    @Override
    public String toString()
    {
        return "RcRatingAiProcessorThread rcCheckId=" + (rcRating==null ? "null" : rcRating.getRcCheckId()) + ", rcItemId=" + (rcRating==null ? "null" : rcRating.getRcItemId()) + ", rcRaterId=" + (rcRating==null ? "null" : rcRating.getRcRaterId()) + ", rcRatingId=" + (rcRating==null ? "null" : rcRating.getRcRatingId());
    }

    public boolean getReadyForAiProcessing()
    {
        try
        {
            if( !AiEssayScoringUtils.getAiEssayScoringOn() )
                return false;

            if (rcItem==null || (rcItem.getAiSummaryOk() != 1 && rcItem.getAiScoringOk()!= 1) || rcRater==null || !rcRater.getRcRaterType().getIsCandidateOrEmployee() || rcRating==null || !rcRating.getIsComplete())
                return false;

            //if (rcItem.getAiSummaryOk() == 1 && rcItem.getAiScoringOk() != 1 && rcRating.getSummary() != null && !rcRating.getSummary().isBlank())
            //    return false;
            
            // CANDIDATE FILE UPLOADS - Dominate if present.

            if (rcItem.getHasCandidateFileUpload() && rcRating.getCandidateRcUploadedUserFile()==null && rcRating.getCandidateUploadedUserFileId()>0)
            {
                if (fileUploadFacade == null)
                    fileUploadFacade = FileUploadFacade.getInstance();
                rcRating.setCandidateRcUploadedUserFile(fileUploadFacade.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(rcRating.getRcCheckId(), rcRating.getRcRaterId(), rcRating.getRcItemId(), UploadedUserFileType.REF_CHECK_CANDIDATE_FILE_UPLOAD.getUploadedUserFileTypeId()));
            }

            // any uploaded Candidate TEXT File. Check dates.  This may not be parsed yet.
            if(rcRating.getCandidateRcUploadedUserFile()!=null && rcRating.getCandidateRcUploadedUserFile().getFileContentType().getIsAnyTextFile())
            {
                // Use it.
                if( rcItem.getAiScoringOk()==1 && (rcRating.getAiScoreDate()==null || rcRating.getAiScoreDate().before( rcRating.getCandidateRcUploadedUserFile().getLastUpload())) )
                    return true;

                if( rcItem.getAiSummaryOk()==1 && (rcRating.getAiSummaryDate()==null || rcRating.getAiSummaryDate().before( rcRating.getCandidateRcUploadedUserFile().getLastUpload())) )
                    return true;

                return false;
            }
            
            // any uploaded Candidate AV File (which is uploaded as an AV Comment. Check SpeechText and dates.
            
            // NO Usable Candidate Uploaded file. So, we look for comments. Comments must be allowed and present.

            // No comments and No AV Candidate upload = No AI.
            if( rcItem.getIncludeComments()<=0 && !rcItem.getHasAvCandidateFileUpload() )
                return false;
            
            // look for audio comments.
            if(rcRating.getUploadedUserFileId()>0 && rcRating.getRcUploadedUserFile()==null )
            {
                if( fileUploadFacade==null )
                    fileUploadFacade=FileUploadFacade.getInstance();
                rcRating.setRcUploadedUserFile( fileUploadFacade.getRcUploadedUserFile( rcRating.getUploadedUserFileId()));
            }

            // No speech text comments - wait if pending, otherwise flow through.
            if( rcRating.getRcUploadedUserFile()!=null && (rcRating.getRcUploadedUserFile().getSpeechText()==null || rcRating.getRcUploadedUserFile().getSpeechText().isBlank()) )
            {
                // still need to wait.
                if( !rcRating.getRcUploadedUserFile().getSpeechTextStatusType().isCompleteOrPermanentError() && !rcRating.getRcUploadedUserFile().getSpeechTextStatusType().isNotRequired())
                    return false;
                
                // Else - wait
            }

            // Converted SpeechText comments. Comment AV File. Use it.
            if( rcRating.getRcUploadedUserFile()!=null && rcRating.getRcUploadedUserFile().getSpeechText()!=null && !rcRating.getRcUploadedUserFile().getSpeechText().isBlank() )
            {
                if( rcItem.getAiScoringOk()==1 && (rcRating.getAiScoreDate()==null || rcRating.getAiScoreDate().before( rcRating.getRcUploadedUserFile().getLastUpload())) )
                    return true;

                if( rcItem.getAiSummaryOk()==1 && (rcRating.getAiSummaryDate()==null || rcRating.getAiSummaryDate().before( rcRating.getRcUploadedUserFile().getLastUpload())) )
                    return true;

                return false;
            }

            // manual comments - Use it.
            if (rcRating.getText()!= null && !rcRating.getText().isBlank())
                return true;

            return false;
        } catch (Exception e)
        {
            LogService.logIt(e, "RcRatingAiProcessorThread.getReadyForAiProcessing() " + toString());
            return false;
        }

    }

}
