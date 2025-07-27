package com.tm2ref.ref.ai;


/**
 * 
 * @author miker_000
 */
public enum RcRaterAiStatusType
{
    INCOMPLETE(0,"Incomplete"),
    COMPLETE(100,"COMPLETE"),
    NOT_NEEDED(200,"NOT_NEEDED");

    private final int rcRaterAiStatusTypeId;

    private final String name;


    private RcRaterAiStatusType( int s , String n )
    {
        this.rcRaterAiStatusTypeId = s;

        this.name = n;
    }
    

    public boolean getIsCompleteOrHigher()
    {
        return rcRaterAiStatusTypeId>=COMPLETE.getRcRaterAiStatusTypeId();
    }
    
    public static RcRaterAiStatusType getValue( int id )
    {
        RcRaterAiStatusType[] vals = RcRaterAiStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcRaterAiStatusTypeId() == id )
                return vals[i];
        }

        return INCOMPLETE;
    }


    public int getRcRaterAiStatusTypeId()
    {
        return rcRaterAiStatusTypeId;
    }

    public String getName()
    {
        return name;
    }
        
}
