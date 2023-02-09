package com.tm2ref.ref;



/**
 * 
 * @author miker_000
 */
public enum RcReminderType
{
    NEVER(0,"Never", "rccrt.never" ),
    DAYS_1(1,"24 Hours", "rccrt.days1" ),
    DAYS_2(2,"24 and 48 Hours", "rccrt.days2" ),
    DAYS_3(3,"24, 48, 72 Hours", "rccrt.days3" );  

    private final int rcReminderTypeId;

    private final String name;
    private String key;


    private RcReminderType( int s , String n, String k )
    {
        this.rcReminderTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    
    public int getMaxDaysSinceSend()
    {
        if( equals( NEVER ) )
            return 0;
        if( equals( DAYS_1 ) )
            return 1;
        if( equals( DAYS_2 ) )
            return 2;
        if( equals( DAYS_3 ) )
            return 3;
        return 0;
    }
            

    public static RcReminderType getValue( int id )
    {
        RcReminderType[] vals = RcReminderType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcReminderTypeId() == id )
                return vals[i];
        }

        return NEVER;
    }


    public int getRcReminderTypeId()
    {
        return rcReminderTypeId;
    }

    public String getName()
    {
        return name;
    }

}
