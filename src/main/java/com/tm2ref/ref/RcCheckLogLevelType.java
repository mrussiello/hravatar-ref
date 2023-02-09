package com.tm2ref.ref;


public enum RcCheckLogLevelType
{
    ERROR(0,"Error"),
    WARNING(1,"Warning"),
    INFO(2,"Info");

    private final int rcCheckLogLevelTypeId;

    private String key;


    private RcCheckLogLevelType( int p , String key )
    {
        this.rcCheckLogLevelTypeId = p;

        this.key = key;
    }

    public int getRcCheckLogLevelTypeId()
    {
        return this.rcCheckLogLevelTypeId;
    }

    public String getName()
    {
        return key;
    }

    
    
    public static RcCheckLogLevelType getValue( int id )
    {
        RcCheckLogLevelType[] vals = RcCheckLogLevelType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcCheckLogLevelTypeId() == id )
                return vals[i];
        }

        return ERROR;
    }

}
