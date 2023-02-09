package com.tm2ref.affiliate;


public enum AffiliateAccountType
{
    NONE(0, "None"),
    SOURCE(1, "Source Account"),
    RESELLER_MANAGED(2, "Reseller Managed"),
    CUSTOMER_MANAGED(3, "Customer Managed");

    private int affiliateAccountTypeId;

    private String key;

    private AffiliateAccountType( int typeId, String k )
    {
        affiliateAccountTypeId = typeId;
        key = k;
    }

    public int getAffiliateAccountTypeId()
    {
        return affiliateAccountTypeId;
    }

    public boolean getIsSource()
    {
        return equals( SOURCE );
    }


    public static AffiliateAccountType getValue( int id )
    {
        if( id == 1 )
            return SOURCE;

        if( id == 2 )
            return RESELLER_MANAGED;

        if( id == 3 )
            return CUSTOMER_MANAGED;

        return NONE;
    }


    public String getKey()
    {
        return key;
    }
}
