package com.tm2ref.ref;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;



/**
 * 
 * @author miker_000
 */
public enum RcRaterStatusType
{
    CREATED(0,"Created", "rcrst.created"),
    SENT(10,"Sent", "rcrst.sent"),
    STARTED(20,"Started", "rcrst.started"),
    COMPLETED(100,"Completed", "rcrst.completed"),
    REJECTED_CANDIDATE_NOT_KNOWN(180,"Rejected - not known by rater", "rcrst.rejectednotknown"),
    REJECTED_UNFAMILIAR_WITH_PERFORMANCE(181,"Rejected - unfamiliar", "rcrst.rejectedcannotrate"),
    REJECTED_REFUSED(182,"Rejected - refused", "rcrst.rejectedrefused"),
    REJECTED_RELEASE(183,"Rejected - release not accepted", "rcrst.rejectedrelease"),
    EXPIRED(201,"Expired", "rcrst.expired"),
    DEACTIVATED(202,"Deactivated", "rcrst.deactivated");

    private final int rcRaterStatusTypeId;

    private final String name;
    private String key;


    private RcRaterStatusType( int s , String n, String k )
    {
        this.rcRaterStatusTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    public boolean getIsRejection()
    {
        return equals(REJECTED_REFUSED) || equals(REJECTED_RELEASE) || equals(REJECTED_CANDIDATE_NOT_KNOWN) || equals(REJECTED_UNFAMILIAR_WITH_PERFORMANCE);
    }

    
    public boolean getIsCompleteOrHigher()
    {
        return rcRaterStatusTypeId>=COMPLETED.getRcRaterStatusTypeId();
    }
    
    public boolean getStartedOrHigher()
    {
        return rcRaterStatusTypeId>SENT.getRcRaterStatusTypeId();
    }
        
    public boolean getCandidateCanEdit()
    {
        return equals(CREATED) || equals(SENT);
    }

    public boolean getSentOrHigher()
    {
        return rcRaterStatusTypeId>=SENT.getRcRaterStatusTypeId();
    }
    
    public boolean getCompleteOrHigher()
    {
        return rcRaterStatusTypeId>=COMPLETED.getRcRaterStatusTypeId();
    }
    public boolean getIsComplete()
    {
        return equals( COMPLETED );
    }
    
    public boolean getIsRejected()
    {
        return equals(REJECTED_CANDIDATE_NOT_KNOWN) || equals(REJECTED_UNFAMILIAR_WITH_PERFORMANCE) || equals(REJECTED_REFUSED) || equals(REJECTED_RELEASE);
    }
        
    
    
    public boolean getIsExpired()
    {
        return equals( EXPIRED );    
    }
    
    public boolean getIsDeactivated()
    {
        return equals( DEACTIVATED );    
    }
    

    

    public static RcRaterStatusType getValue( int id )
    {
        RcRaterStatusType[] vals = RcRaterStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcRaterStatusTypeId() == id )
                return vals[i];
        }

        return CREATED;
    }


    public int getRcRaterStatusTypeId()
    {
        return rcRaterStatusTypeId;
    }

    public String getName()
    {
        return name;
    }
    
    public String getName( Locale l )
    {
        if( l==null )
            l = Locale.US;
        
        return MessageFactory.getStringMessage(l, key );
    }

    public String getKey() {
        return key;
    }
    
}
