package com.tm2ref.ref;



/**
 * @author miker_000
 */
public enum RcContactMethodType
{
    EMAIL(0,"Email", "rccmt.email" ),
    PHONE(1,"Phone", "rccmt.phone" ),
    BOTH(10,"Either email or phone", "rccmt.both" );  

    private final int rcContactMethodTypeId;

    private final String name;
    private String key;


    private RcContactMethodType( int s , String n, String k )
    {
        this.rcContactMethodTypeId = s;
        this.name = n;
        this.key = k;
    }
    
    
            
    public static RcContactMethodType getValue( int id )
    {
        RcContactMethodType[] vals = RcContactMethodType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcContactMethodTypeId() == id )
                return vals[i];
        }

        return EMAIL;
    }


    public int getRcContactMethodTypeId()
    {
        return rcContactMethodTypeId;
    }

    public String getName()
    {
        return name;
    }

}
