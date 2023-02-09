package com.tm2ref.purchase;

public enum CreditStatusType
{
    ACTIVE(1,"creditstatustype.active"),
    EXPIRED(2,"creditstatustype.expired"),
    EMPTY(3,"creditstatustype.empty"),
    DISABLED(4,"creditstatustype.disabled"),
    OVERAGE(5,"creditstatustype.overage");

    private final int creditStatusTypeId;

    private String key;

    private CreditStatusType( int p , String key )
    {
        this.creditStatusTypeId = p;

        this.key = key;
    }


    public int getCreditStatusTypeId()
    {
        return this.creditStatusTypeId;
    }


    public boolean getIsActive()
    {
        return equals( ACTIVE );
    }


    public static CreditStatusType getValue( int id )
    {
        CreditStatusType[] vals = CreditStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCreditStatusTypeId() == id )
                return vals[i];
        }

        return DISABLED;
    }

}
