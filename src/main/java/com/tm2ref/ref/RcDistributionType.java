package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;



/**
 * 
 * @author miker_000
 */
public enum RcDistributionType
{
    NEVER(0,"Never", "rccdt.never" ),
    COMPLETE(1,"100%", "rccdt.complete" ),
    HALF_AND_COMPLETE(1,"50 and 100", "rccdt.half_and_complete" ),
    EACH_RATER(0,"Each Rater", "rccdt.everyrating" );  

    private final int rcDistributionTypeId;

    private final String name;
    private String key;


    private RcDistributionType( int s , String n, String k )
    {
        this.rcDistributionTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    public boolean getIsEachRating()
    {
        return equals( EACH_RATER );
    }
    
    public boolean getIsPartialProgress()
    {
        return equals( HALF_AND_COMPLETE );
    }
    
    public boolean sendForRaterCompletion( RcCheck rc, RcRater rater )
    {
        // always send for a new complete.
        if( rc.getRcCheckStatusType().getIsComplete() && rc.getLastProgressMsgDate()==null )
            return true;
        
        if( equals( NEVER ) )
            return false;
        
        if( equals( EACH_RATER ) )
        {
            if( rc.getRcCheckStatusType().getIsComplete() )
                return rc.getLastProgressMsgDate()==null;
            
            if( rater==null )
                return rc.getRcCandidateStatusType().getIsCompletedOrHigher() && rc.getLastCandidateProgressMsgDate()==null;
            
            return rater.getRcRaterStatusType().getIsCompleteOrHigher() && rater.getLastProgressMsgDate()==null;
        }
        
        if( equals( COMPLETE ) )
            return rc.getLastProgressMsgDate()==null && rc.getRcCheckStatusType().getIsComplete();
        
        if( equals( HALF_AND_COMPLETE ) )
        {
            if( rc.getPercentComplete()<100f && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
            {
                if( rater!=null && rater.getLastProgressMsgDate()!=null )
                    return false;

                if( rater==null && rc.getLastCandidateProgressMsgDate()!=null )
                    return false;
                
                return rc.getPercentComplete()>=50f;
            }
            
            // Half done not sent
            //if( rc.getPercentComplete()<100f && rc.getPercentComplete()>=50f )
            //    return true;
            
            if( rc.getPercentComplete()>=100f && rc.getLastProgressMsgDate()==null )
                return true;
        }
        
        return false;
    }
    
            
    public static RcDistributionType getValue( int id )
    {
        RcDistributionType[] vals = RcDistributionType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcDistributionTypeId() == id )
                return vals[i];
        }

        return NEVER;
    }


    public int getRcDistributionTypeId()
    {
        return rcDistributionTypeId;
    }

    public String getName()
    {
        return name;
    }

}
