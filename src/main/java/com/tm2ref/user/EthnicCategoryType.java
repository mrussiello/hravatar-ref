package com.tm2ref.user;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;


public enum EthnicCategoryType
{
    HISPANIC(1,"ecat.hispanic"),
    NOTHISPANIC(2, "ecat.nothispanic" );

    private int ethnicCategoryTypeId;

    private String key;

    private EthnicCategoryType( int typeId , String key )
    {
        this.ethnicCategoryTypeId = typeId;

        this.key = key;
    }

    public String getName( Locale locale )
    {
        return MessageFactory.getStringMessage( locale, key , null );
    }

    public int getEthnicCategoryTypeId()
    {
        return ethnicCategoryTypeId;
    }

    public static EthnicCategoryType getType( int t )
    {
        for( EthnicCategoryType ect : EthnicCategoryType.values() )
        {
            if( ect.ethnicCategoryTypeId == t )
                return ect;
        }

        return null;
    }



    public String getKey()
    {
        return key;
    }
}
