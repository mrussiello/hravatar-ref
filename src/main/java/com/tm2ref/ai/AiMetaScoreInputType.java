package com.tm2ref.ai;
import com.tm2ref.util.MessageFactory;
import java.util.Locale;


public enum AiMetaScoreInputType
{
    NONE(0, "msip.none"),
    RESUME(1, "msip.resume"),
    TEST_COMPETENCY(2, "msip.testcomp"),
    RC_COMPETENCY(3, "msip.rccomp"),
    TEXT_RESPONSES(4, "msip.textresp");

    private final int aiMetaScoreInputTypeId;
    private final String key;


    private AiMetaScoreInputType( int p, String key )
    {
        this.aiMetaScoreInputTypeId = p;
        this.key = key;
    }


    public int getAiMetaScoreInputTypeId()
    {
        return this.aiMetaScoreInputTypeId;
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

    public static AiMetaScoreInputType getValue( int id )
    {
        AiMetaScoreInputType[] vals = AiMetaScoreInputType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getAiMetaScoreInputTypeId() == id )
                return vals[i];
        }

        return NONE;
    }
}
