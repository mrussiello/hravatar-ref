package com.tm2ref.purchase;


public enum CreditType
{
    LEGACY(0,"Legacy"),
    RESULT(1,"Result");

    private final int creditTypeId;

    private String name;

    private CreditType( int p , String key )
    {
        this.creditTypeId = p;

        this.name = key;
    }


    public int getCreditTypeId()
    {
        return this.creditTypeId;
    }


    public boolean getIsLegacy()
    {
        return equals( LEGACY );
    }

    public boolean getIsResult()
    {
        return equals( RESULT );
    }





    public static CreditType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getName()
    {
        return name;
    }
    

    public static CreditType getValue( int id )
    {
        CreditType[] vals = CreditType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCreditTypeId() == id )
                return vals[i];
        }

        return LEGACY;
    }

}
