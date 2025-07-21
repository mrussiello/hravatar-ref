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
import com.tm2ref.essay.EssayFacade;
import com.tm2ref.essay.EssayScoreStatusType;
import com.tm2ref.essay.UnscoredEssayType;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.ref.RcScriptFacade;
import com.tm2ref.score.CaveatScoreType;
import com.tm2ref.service.LogService;
import java.util.Map;

/**
 *
 * @author miker
 */
public class RcCheckAiScoresUpdater implements Runnable {
    
    RcCheck rcCheck;
    
    RcFacade rcFacade;
    RcScriptFacade rcScriptFacade;
    EssayFacade essayFacade;
    
    public RcCheckAiScoresUpdater( RcCheck rcCheck )
    {
        this.rcCheck = rcCheck;
    }

    @Override
    public void run()
    {
        try
        {
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RcCheckAiScoresUpdater.run() rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );
        }
    }
    
    public void updateRcCheckAiScores() throws Exception
    {
        try
        {
            // AI Scores are all for the candidate, so we need to see if there's a Candidate rater. 
            if( rcCheck==null )
            {
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() AAA.1 RcCheck is null!" );
                return;
            }
            
            if( !rcCheck.getCollectRatingsFmCandidate() )
            {
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() AAA.2 RcCheck does not collect ratings from candidate. rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );
                return;                
            }            
            if( rcCheck.getRcRaterList()==null )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcCheck.setRcRaterList( rcFacade.getRcRaterList( rcCheck.getRcCheckId()));
            }
            RcRater candidateRcRater = null;
            for( RcRater rater : rcCheck.getRcRaterList() )
            {
                if( rater.getIsCandidateOrEmployee() )
                {
                    candidateRcRater=rater;
                    break;
                }
            }
            if( candidateRcRater==null )
            {
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() AAA.4  No candidate/employee RcRater found. rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );
                return;                
            }
            if( !candidateRcRater.getRcRaterStatusType().getStartedOrHigher() )
            {
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() AAA.5  No candidate/employee RcRater has not been started. rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );
                return;                
            }
            if( candidateRcRater.getRcRaterAiStatusTypeId()>=RcRaterAiStatusType.COMPLETE.getRcRaterAiStatusTypeId() )
            {
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() AAA.6  RcRater AI Status Type is complete or higher. rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );
                return;                
            }
            
            if( candidateRcRater.getRcRatingList()==null )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                candidateRcRater.setRcRatingList( rcFacade.getRcRatingList( candidateRcRater.getRcCheckId(), candidateRcRater.getRcRaterId()));
            }

            LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() BBB.1  Candidate/employee RcRater has " + candidateRcRater.getRcRatingList().size() + " rcRatings to check. rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );
            
            /*
              count[0] = ratings that include AI.
              count[1] = ratings complete or updated
              count[2] = ratings still pending
              count[3] = ratings with failed permanently.
            */
            int[] counts = new int[4];
            for( RcRating rcRating : candidateRcRater.getRcRatingList() )
            {
                updateRcRatingAiScores( rcRating, counts );
            }
            
            LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() CCC.1 Total Ratings=" + candidateRcRater.getRcRatingList().size() +", include AI=" + counts[0] + ", Completed AI Proc=" + counts[1] + ", pending=" + counts[2] + ", failed permanently=" + counts[3] +", rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );            

            // Do NOT update rcRater.RcRaterAiStatusType unless the rater is complete since there may still be future RcRatings attached to this rater.
            if( !candidateRcRater.getRcRaterStatusType().getCompleteOrHigher() )
            {
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() CCC.2 rcRater is not complete so returning without updating RcRaterAiStatusTypeId.  rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );            
                return;
            }

            // Has pending
            if(counts[2]>0 )
            {
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() CCC.3 rcRater is complete but there are still " + counts[2] + " pending AiScores, so returning without updating RcRaterAiStatusTypeId.  rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );            
                return;
            }
            
            // None needed.
            if(counts[0]<=0 )
            {
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() CCC.4 rcRater has zero ratings requiring AiScores, so updating RcRaterAiStatusTypeId to Not Needed.  rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );            
                candidateRcRater.setRcRaterAiStatusTypeId( RcRaterAiStatusType.NOT_NEEDED.getRcRaterAiStatusTypeId() );
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcFacade.saveRcRater(candidateRcRater, false);
                return;
            }

            LogService.logIt( "RcCheckAiScoresUpdater.updateRcCheckAiScores() CCC.5 rcRater is complete and has zero pending ratings requiring AiScores, so updating RcRaterAiStatusTypeId to Complete.  rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId()) );            
            candidateRcRater.setRcRaterAiStatusTypeId( RcRaterAiStatusType.COMPLETE.getRcRaterAiStatusTypeId() );
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rcFacade.saveRcRater(candidateRcRater, false);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RcCheckAiScoresUpdater.updateRcCheckAiScores() rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId() ) );
            throw e;
        }
    }


    
    /*
      count[0] = ratings that include AI.
      count[1] = ratings complete or updated
      count[2] = ratings still pending
      count[3] = ratings with failed permanently.
    */
    public void updateRcRatingAiScores( RcRating rcRating, int[] counts ) throws Exception
    {
        try
        {
            if( rcRating==null )
                throw new Exception( "RcRating is null" );

            if( rcRating.getRcItem()==null )
            {
                if( rcScriptFacade==null )
                    rcScriptFacade=RcScriptFacade.getInstance();
                rcRating.setRcItem( rcScriptFacade.getRcItem(rcRating.getRcItemId(), false, true) );                
            }
            if( rcRating.getRcItem()==null )
                throw new Exception( "RcItem not found for RcRating" );
            
            RcItem item = rcRating.getRcItem();
            
            // not needed
            if( item.getAiScoringOk()!=1 && item.getAiSummaryOk()!=1 )
            {
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcRatingAiScores() AAA.1 RcItem does not require any AI processing. Not checking. rcRatingId=" + (rcRating==null ? "null" : rcRating.getRcRatingId() + ", rcItemId=" + rcRating.getRcItemId()) + ", rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId() ) );
                return;
            }
            
            // update needs ai count
            counts[0]++;
            
            if( !rcRating.getRcRatingStatusType().getIsComplete() )
            {
                counts[2]++;
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcRatingAiScores() AAA.2 RcRating is not complete. updating pending count. rcRatingId=" + (rcRating==null ? "null" : rcRating.getRcRatingId() + ", rcItemId=" + rcRating.getRcItemId()) + ", rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId() ) );
                return;
            }

            // At this point we have a complete RcRating that DOES require AI.
            if( essayFacade==null )
                essayFacade=EssayFacade.getInstance();
            
            UnscoredEssay ue = essayFacade.getUnscoredEssay( rcRating.getRcCheckId(), rcRating.getRcRatingId(), UnscoredEssayType.RC_COMMENT.getUnscoredEssayTypeId());
            if( ue==null )
            {
                counts[2]++;
                LogService.logIt( "RcCheckAiScoresUpdater.updateRcRatingAiScores() AAA.3 No UnscoredEssay found for RcRating. Updating Pending count. rcRatingId=" + (rcRating==null ? "null" : rcRating.getRcRatingId() + ", rcItemId=" + rcRating.getRcItemId()) + ", rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId() ) );
                return;
            }
            
            boolean update = false;
            boolean pending = false;
            if( item.getAiSummaryOk()==1 )
            {
                if( ue.getSummaryDate()!=null && ue.getSummary()!=null && !ue.getSummary().isBlank() )
                {
                    if( rcRating.getAiSummaryDate()==null || (rcRating.getAiSummaryDate().before( ue.getSummaryDate() )) || rcRating.getAiSummaryStatusTypeId()>EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() )
                    {
                        rcRating.setAiSummaryDate( ue.getSummaryDate());
                        rcRating.setAiSummaryStatusTypeId( EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId());
                        rcRating.setSummary( ue.getSummary());
                        update=true;
                    }
                }
                else if( ue.getSummaryDate()!=null || ue.getEssayScoreStatusType().getIsCancelledOrHigher() )
                {
                    rcRating.setAiSummaryStatusTypeId( ue.getEssayScoreStatusType().getEssayScoreStatusTypeId() );
                    update=true;
                }
                else if( ue.getSummaryDate()==null )
                {
                    LogService.logIt( "RcCheckAiScoresUpdater.updateRcRatingAiScores() BBB.1C AI Summary not available but not failed either. Updating Pending count. rcRatingId=" + (rcRating==null ? "null" : rcRating.getRcRatingId() + ", rcItemId=" + rcRating.getRcItemId()) + ", rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId() ) );
                    pending=true;
                }
            }
            
            if( item.getAiScoringOk()==1 )
            {
                if( ue.getScoreDate()!=null && ue.getEssayScoreStatusType().completed() )
                {
                    if( rcRating.getAiScoreDate()==null || (rcRating.getAiScoreDate().before( ue.getScoreDate() )) || rcRating.getAiScoresStatusTypeId()>EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId())
                    {
                        rcRating.setScore2( ue.getComputedScore());
                        rcRating.setScore3( ue.getComputedConfidence());
                        rcRating.setScore6( ue.getTotalWords());

                        Map<Integer,Float> essayMetaScoreMap = ue.getMetaScoreMap();
                        if (essayMetaScoreMap.containsKey(CaveatScoreType.CLARITY.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId())>0)
                            rcRating.setScore12( essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId()));

                        if (essayMetaScoreMap.containsKey(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId())>0)
                            rcRating.setScore13( essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId()));

                        if (essayMetaScoreMap.containsKey(CaveatScoreType.MECHANICS.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId())>0)
                            rcRating.setScore14( essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId()));

                        if (essayMetaScoreMap.containsKey(CaveatScoreType.IDEAL.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId())>0)
                            rcRating.setScore15( essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId()));
                        rcRating.setAiScoreDate( ue.getSummaryDate());
                        rcRating.setAiScoresStatusTypeId( EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId());
                        update=true;
                    }
                }
                else if( ue.getEssayScoreStatusType().getIsCancelledOrHigher() )
                {
                    rcRating.setAiScoresStatusTypeId( ue.getEssayScoreStatusType().getEssayScoreStatusTypeId() );
                    update=true;
                }
                else if( ue.getScoreDate()==null || ue.getEssayScoreStatusType().unsubmitted() )
                {
                    LogService.logIt( "RcCheckAiScoresUpdater.updateRcRatingAiScores() CCC.1C AI Scores not available but not failed either. Updating Pending count. rcRatingId=" + (rcRating==null ? "null" : rcRating.getRcRatingId() + ", rcItemId=" + rcRating.getRcItemId()) + ", rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId() ) );
                    pending=true;
                }
            }
            
            if( update )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcFacade.saveRcRating(rcRating);                        
            }
            
            if( pending )
            {
                counts[2]++;
                return;
            }
            
            boolean complete=false;
            boolean failed = false;
            
            if( item.getAiSummaryOk()==1 )
            {
                // success.
                if( rcRating.getAiSummaryStatusTypeId()==EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() )
                    complete=true;
                else if( rcRating.getAiSummaryStatusTypeId()>EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() )
                    failed=true;
            }

            if( item.getAiScoringOk()==1 )
            {
                // success.
                if( rcRating.getAiScoresStatusTypeId()==EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() )
                    complete=true;
                else if( rcRating.getAiScoresStatusTypeId()>EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() )
                    failed=true;
            }
            
            // if either is a fail it's a fail.
            if( failed )
                counts[3]++;
            
            // if either complete its a complete
            else if( complete )
                counts[1]++;

        }
        catch( Exception e )
        {
            LogService.logIt( e, "RcCheckAiScoresUpdater.updateRcRatingAiScores() rcRatingId=" + (rcRating==null ? "null" : rcRating.getRcRatingId() + ", rcItemId=" + rcRating.getRcItemId()) + ", rcCheckId=" + (rcCheck==null ? "null" : rcCheck.getRcCheckId() ) );
            throw e;
        }
    }
    
    
    
}
