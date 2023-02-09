package com.tm2ref.ref;


/**
 * 
 * @author miker_000
 */
public enum RcSuspiciousActivityType
{
    UNKNOWN(0,"Unknown", "rcsat.unknown"),
    SAME_IP_UA(10,"Candidate and Rater Same IP Address and User Agent", "rcsat.sameplat"),
    SAME_IP(11,"Candidate and Rater Same IP Address", "rcsat.sameip"),
    RATERS_SAME_IP(12,"Different Raters, Same IP Address", "rcsat.raterssameip");

    private final int rcSuspiciousActivityTypeId;

    private final String name;
    private String key;


    private RcSuspiciousActivityType( int s , String n, String k )
    {
        this.rcSuspiciousActivityTypeId = s;
        this.name = n;
        this.key = k;
    }
    

    public boolean getIsAnyRaterIpMatch()
    {
        return getIsRaterRaterMatch() || getIsCandidateRaterMatch();
    }
    
    public boolean getIsRaterRaterMatch()
    {
        return equals(RATERS_SAME_IP);
    }

    
    public boolean getIsCandidateRaterMatch()
    {
        return equals(SAME_IP_UA) || equals(SAME_IP);
    }

    public static RcSuspiciousActivityType getValue( int id )
    {
        RcSuspiciousActivityType[] vals = RcSuspiciousActivityType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcSuspiciousActivityTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }

    public String getKey() {
        return key;
    }


    public int getRcSuspiciousActivityTypeId()
    {
        return rcSuspiciousActivityTypeId;
    }

    public String getName()
    {
        return name;
    }

}
