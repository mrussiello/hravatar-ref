package com.tm2ref.ai;



public enum MetaScoreStatusType
{
    NOT_READY(0,"Not Ready"),
    READY(100,"Ready for Scoring"),
    SCORING_STARTED(105,"Scoring Started"),
    PENDING_ASYNC_SCORES(106,"Scoring - Pending External/Async Scores"),
    SCORED(110,"Scored"),
    REPORT_COMPLETE(120,"Report_Revisions Complete"),
    DEACTIVATED(202,"Deactivated"),
    SCORE_ERROR(203,"Scoring Error" );

    private final int metaScoreStatusTypeId;

    private String key;


    private MetaScoreStatusType( int p , String key )
    {
        this.metaScoreStatusTypeId = p;

        this.key = key;
    }




    public int getMetaScoreStatusTypeId()
    {
        return this.metaScoreStatusTypeId;
    }

    
    public boolean getHasResultsToShow()
    {
        return metaScoreStatusTypeId>=SCORED.getMetaScoreStatusTypeId() && metaScoreStatusTypeId<=REPORT_COMPLETE.getMetaScoreStatusTypeId();
    }

    public static MetaScoreStatusType getType( int typeId )
    {
        return getValue( typeId );
    }


    public String getKey()
    {
        return key;
    }



    public static MetaScoreStatusType getValue( int id )
    {
        MetaScoreStatusType[] vals = MetaScoreStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getMetaScoreStatusTypeId() == id )
                return vals[i];
        }
        return NOT_READY;
    }
}
