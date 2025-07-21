package com.tm2ref.essay;



public enum EssayScoreStatusType
{
    NOTSUBMITTED(0,"Not Submitted"),
    SUBMITTED(1,"Submitted - pending scoring"),
    SCORECOMPLETE(100,"Score Complete"),
    CANCELLED(200,"Canceled"),
    FAILED(201,"Failed Scoring"),
    FAILED_NOTENABLED(202,"Failed Scoring - System Not Enabled"),
    SKIPPED_DUMMYPROMPT(203,"Skipped Scoring - Dummy Essay Prompt"),
    INVALID_TEXT_FOR_DISCERN(204,"Skipped Scoring - Invalid Text for Discern");


    private final int essayScoreStatusTypeId;

    private final String name;


    private EssayScoreStatusType( int s , String n )
    {
        this.essayScoreStatusTypeId = s;

        this.name = n;
    }

    public boolean getIsComplete()
    {
        return equals(SCORECOMPLETE);
    }
    
    public boolean getIsCancelledOrHigher()
    {
        return essayScoreStatusTypeId>=CANCELLED.getEssayScoreStatusTypeId();
    }
    

    public boolean unsubmitted()
    {
        return equals( NOTSUBMITTED ) ;
    }

    public boolean incomplete()
    {
        return equals( SUBMITTED  );
    }

    public boolean completed()
    {
        return equals( SCORECOMPLETE ) ;
    }

    public boolean failed()
    {
        return equals( FAILED ) ;
    }

    public boolean notEnabled()
    {
        return equals( FAILED_NOTENABLED ) ;
    }
    
    public boolean skipped()
    {
        return equals( SKIPPED_DUMMYPROMPT );
    }

    public boolean invalidTextForDiscern()
    {
        return equals( INVALID_TEXT_FOR_DISCERN );
    }

    public boolean cancelled()
    {
        return equals( CANCELLED );
    }


    public static EssayScoreStatusType getValue( int id )
    {
        EssayScoreStatusType[] vals = EssayScoreStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getEssayScoreStatusTypeId() == id )
                return vals[i];
        }

        return NOTSUBMITTED;
    }


    public int getEssayScoreStatusTypeId()
    {
        return essayScoreStatusTypeId;
    }

    public String getName()
    {
        return name;
    }

}
