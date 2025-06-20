package com.tm2ref.ai;
import com.tm2ref.util.MessageFactory;
import java.util.Locale;


public enum MetaScoreInputType
{
    NONE(0, "msip.none"),
    RESUME(1, "msip.resume"),
    TEST_COMPETENCY(2, "msip.testcomp"),
    RC_COMPETENCY(3, "msip.rccomp"),
    TEXT_RESPONSES(4, "msip.textresp");

    private final int metaScoreInputTypeId;
    private final String key;


    private MetaScoreInputType( int p, String key )
    {
        this.metaScoreInputTypeId = p;
        this.key = key;
    }


    public int getMetaScoreInputTypeId()
    {
        return this.metaScoreInputTypeId;
    }


    public String getName( Locale locale )
    {
        if( locale==null )
            locale=Locale.US;        
        return MessageFactory.getStringMessage(locale, key);
    }

    public String getName()
    {
        return getName(Locale.US);
    }


    public String getKey()
    {
        return key;
    }

    public static MetaScoreInputType getValue( int id )
    {
        MetaScoreInputType[] vals = MetaScoreInputType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getMetaScoreInputTypeId() == id )
                return vals[i];
        }

        return NONE;
    }
}
