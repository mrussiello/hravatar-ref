package com.tm2ref.ai;
import com.tm2ref.util.MessageFactory;
import jakarta.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public enum AiMetaScoreType
{
    NONE(0, "mstp.none"),
    ORGTRAITS(1, "mstp.orgtrts"),
    ORGCOMPS(2, "mstp.orgcomps"),
    JOBDESCRIP(3, "mstp.jobdescrip");

    private final int aiMetaScoreTypeId;
    private final String key;


    private AiMetaScoreType( int p, String key )
    {
        this.aiMetaScoreTypeId = p;
        this.key = key;
    }


    public List<SelectItem> getSelectItemList( Locale locale )
    {
        if( locale==null )
            locale=Locale.US;
        
        List<SelectItem> out = new ArrayList<>();        
        for( AiMetaScoreType v : AiMetaScoreType.values() )
        {
            out.add( new SelectItem( v.getAiMetaScoreTypeId(), v.getName(locale) ));
        }        
        return out;        
    }

    
    public int getAiMetaScoreTypeId()
    {
        return this.aiMetaScoreTypeId;
    }


    public String getName( Locale locale )
    {
        if( locale==null )
            locale=Locale.US;
        
        return MessageFactory.getStringMessage(locale, key);
    }

    public String getDescription( Locale locale )
    {
        if( locale==null )
            locale=Locale.US;
        
        return MessageFactory.getStringMessage(locale, key + ".descrip");
    }

    
    

    public String getName()
    {
        return getName(Locale.US);
    }


    public String getKey()
    {
        return key;
    }



    public static AiMetaScoreType getValue( int id )
    {
        AiMetaScoreType[] vals = AiMetaScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getAiMetaScoreTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


}
