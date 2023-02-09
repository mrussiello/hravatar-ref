package com.tm2ref.ref;



/**
 * 
 * @author miker_000
 */
public enum RcCheckScoringStatusType
{
    NOT_SCORED(0,"Not Scored", "rcsst.notscored"),
    SCORING_STARTED(10,"Scoring Started", "rcsst.scoringstarted"),
    SCORED(20,"Scored", "rcsst.scored"),
    ERROR(100,"Scoring Error", "rcsst.error");

    private final int rcCheckScoringStatusTypeId;

    private final String name;
    private String key;


    private RcCheckScoringStatusType( int s , String n, String k )
    {
        this.rcCheckScoringStatusTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    
         
    public static RcCheckScoringStatusType getValue( int id )
    {
        RcCheckScoringStatusType[] vals = RcCheckScoringStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcCheckScoringStatusTypeId() == id )
                return vals[i];
        }

        return NOT_SCORED;
    }


    public int getRcCheckScoringStatusTypeId()
    {
        return rcCheckScoringStatusTypeId;
    }

    public String getName()
    {
        return name;
    }

}
