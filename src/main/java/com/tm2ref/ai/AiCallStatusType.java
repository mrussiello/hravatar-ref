package com.tm2ref.ai;


public enum AiCallStatusType
{
    RECEIVED(0,"Recevied", "inprogress"),
    STARTED(1,"Started", "inprogress"),
    COMPLETED(100,"Completed", "complete"),
    SKIPPED(201,"Skipped", "skipped"),
    ERROR(200,"Error","error");

    private final int aiCallStatusTypeId;
    private final String name;
    private final String statusStr;

    private AiCallStatusType( int typeId , String nm, String statusStr )
    {
        this.aiCallStatusTypeId = typeId;
        this.name = nm;
        this.statusStr = statusStr;
    }

    public boolean getComplete()
    {
        return equals( COMPLETED );
    }

    public boolean getError()
    {
        return equals( ERROR );
    }

    public boolean getInProgress()
    {
        return equals( STARTED );
    }    
    
    public String getName()
    {
        return name;
    }
        
    public String getStatusStr()
    {
        return statusStr;
    }
    
    public int getAiCallStatusTypeId()
    {
        return this.aiCallStatusTypeId;
    }

    public static AiCallStatusType getValue( int id )
    {
        for (AiCallStatusType val : AiCallStatusType.values()) 
        {
            if (val.getAiCallStatusTypeId()==id) 
            {
                return val;
            }
        }
        return RECEIVED;
    }

}
