package com.tm2ref.ref;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;



/**
 * 
 * @author miker_000
 */
public enum RcCheckStatusType
{
    CREATED(0,"Created", "rccst.created"),
    SENT(10,"Sent", "rccst.sent"),
    STARTED(20,"Started", "rccst.started"),
    //PARTIALLY_COMPLETED(100,"Partially Completed", "rccst.partiallycompleted"),
    COMPLETED(101,"Completed", "rccst.completed"),
    //PARTIALLY_SCORED(110,"Partially Scored", "rccst.partiallyscored"),
    //SCORED(111,"Scored", "rccst.scored"),
    EXPIRED(200,"Expired", "rccst.expired"),
    DEACTIVATED(301,"Deactivated", "rccst.deactivated"),
    ARCHIVED(302,"Archived", "rccst.archived" );

    private final int rcCheckStatusTypeId;

    private final String name;
    private String key;


    private RcCheckStatusType( int s , String n, String k )
    {
        this.rcCheckStatusTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    public boolean getSentOrHigher()
    {
        return rcCheckStatusTypeId>=SENT.getRcCheckStatusTypeId();
    }
    
    public boolean getCompleteOrHigher()
    {
        return rcCheckStatusTypeId>=COMPLETED.getRcCheckStatusTypeId();
    }
    
    public boolean getIsStartedOrHigher()
    {
        return rcCheckStatusTypeId>=STARTED.getRcCheckStatusTypeId();        
    }
    
    //public boolean getIsScoreAvailable()
    //{
    //    return rcCheckStatusTypeId>=PARTIALLY_SCORED.getRcCheckStatusTypeId();
    //}

    //public boolean getIsCandidateInputOrLower()
    //{
    //    return rcCheckStatusTypeId<=SENT.getRcCheckStatusTypeId();        
    //}
    
    public boolean getIsComplete()
    {
        return equals( COMPLETED );
    }
    
    public boolean getIsCandidateInput()
    {
        return equals( SENT );
    }
    
    //public boolean getIsRaterInput()
    //{
    //    return equals( STARTED );
    //}

    public boolean getIsExpired()
    {
        return equals( EXPIRED );    
    }
    
    public boolean getIsCancelled()
    {
        return equals( DEACTIVATED );    
    }
    
    public boolean getCanExpire()
    {
        return rcCheckStatusTypeId<COMPLETED.getRcCheckStatusTypeId();
    }
    
    //public boolean getCanAutoComplete()
    //{
    //    return equals( PARTIALLY_COMPLETED );
    //}
    

    
    public boolean getCanEdit()
    {
        return rcCheckStatusTypeId<STARTED.getRcCheckStatusTypeId();
    }
    
         
    public static RcCheckStatusType getValue( int id )
    {
        RcCheckStatusType[] vals = RcCheckStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcCheckStatusTypeId() == id )
                return vals[i];
        }

        return CREATED;
    }


    public int getRcCheckStatusTypeId()
    {
        return rcCheckStatusTypeId;
    }

    public String getName()
    {
        return name;
    }

    public String getName( Locale locale )
    {
        return MessageFactory.getStringMessage( locale, key );
    }
    
    
}
