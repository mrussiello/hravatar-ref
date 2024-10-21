package com.tm2ref.user;


import com.tm2ref.util.MessageFactory;
import java.util.Locale;


public enum RacialCategoryType
{
    WHITE(1,"rcat.white"),
    BLACK(2, "rcat.black" ),
    ASIAN(3, "rcat.asian" ),
    AMERINDIAN(4, "rcat.amerindian" ),
    PACIFIC(5, "rcat.pacific" ),
    OTHER(6, "rcat.other" );

    private int racialCategoryTypeId;

    private String key;

    private RacialCategoryType( int typeId , String key )
    {
        this.racialCategoryTypeId = typeId;

        this.key = key;
    }

    public String getName( Locale locale )
    {
        return MessageFactory.getStringMessage( locale, key , null );
    }

    public int getRacialCategoryTypeId()
    {
        return racialCategoryTypeId;
    }

    public static RacialCategoryType getType( int racialCategoryId )
    {
        for( RacialCategoryType rct : RacialCategoryType.values() )
        {
            if( rct.racialCategoryTypeId == racialCategoryId )
                return rct;
        }

        return null;
    }


    public String getKey()
    {
        return key;
    }
}
