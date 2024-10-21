package com.tm2ref.event;




public enum TestKeyStatusType
{
    ACTIVE(0,"Active"),
    STARTED(1,"Started"),
    STOPPED_PROCTOR(90,"Stopped By Proctor"),
    COMPLETED(100,"Completed"),
    COMPLETED_PENDING_EXTERNAL(101,"Completed - Pending External Scoring"),
    SCORING_STARTED(105,"Scoring Started"),
    SCORED(110,"Scored"),
    REPORTS_STARTED(111,"Reports Started"),
    REPORTS_COMPLETE(120,"Reports Complete"),
    DISTRIBUTION_STARTED(129,"Distribution Started"),
    DISTRIBUTION_COMPLETE(130,"Distribution Complete"),
    DISTRIBUTION_ERROR(131,"Distribution Error" ),
    EXPIRED(201,"Expired"),
    DEACTIVATED(202,"Deactivated"),
    SCORE_ERROR(203,"Scoring Error" ),
    REPORT_ERROR(204,"Report Error" );

    private final int testKeyStatusTypeId;

    private String key;

    public boolean getIsActive()
    {
        return equals( ACTIVE );
    }
    
    public boolean getIsStarted()
    {
        return equals( STARTED );
    }

    public boolean getIsScoreComplete()
    {
        return equals( SCORED );
    }

    public boolean getIsScoredOrHigher()
    {
        return  testKeyStatusTypeId>=SCORED.testKeyStatusTypeId && testKeyStatusTypeId<=DISTRIBUTION_COMPLETE.testKeyStatusTypeId;
    }
    
    public boolean getIsScorePending()
    {
        return equals( COMPLETED_PENDING_EXTERNAL );
    }


    public boolean getIsAnyScoreRptDistError()
    {
        return equals(SCORE_ERROR) || equals(REPORT_ERROR) || equals(DISTRIBUTION_ERROR);
    }
    
    public String getName()
    {
        return key;
    }
    
    public String getKey()
    {
        return key;
    }

    private TestKeyStatusType( int p , String key )
    {
        this.testKeyStatusTypeId = p;

        this.key = key;
    }


    public boolean getIsCompleteOrHigher()
    {
        return testKeyStatusTypeId >= COMPLETED.getTestKeyStatusTypeId();
    }
    
    
    public boolean getIsReportGeneratedOrHigher()
    {
        return testKeyStatusTypeId>=REPORTS_COMPLETE.testKeyStatusTypeId && testKeyStatusTypeId<=DISTRIBUTION_COMPLETE.testKeyStatusTypeId;
    }


    public int getTestKeyStatusTypeId()
    {
        return this.testKeyStatusTypeId;
    }


    public static TestKeyStatusType getValue( int id )
    {
        TestKeyStatusType[] vals = TestKeyStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getTestKeyStatusTypeId() == id )
                return vals[i];
        }

        return ACTIVE;
    }

}
