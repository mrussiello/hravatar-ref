package com.tm2ref.ai;
import com.tm2ref.util.MessageFactory;
import jakarta.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public enum MetaScoreType
{
    NONE(0, "mstp.none"),
    ORGTRAITS(1, "mstp.orgtrts"),
    ORGCOMPS(2, "mstp.orgcomps"),
    JOBDESCRIP(3, "mstp.jobdescrip");

    private final int metaScoreTypeId;
    private final String key;


    private MetaScoreType( int p, String key )
    {
        this.metaScoreTypeId = p;
        this.key = key;
    }


    public List<SelectItem> getSelectItemList( Locale locale )
    {
        if( locale==null )
            locale=Locale.US;
        
        List<SelectItem> out = new ArrayList<>();        
        for( MetaScoreType v : MetaScoreType.values() )
        {
            out.add( new SelectItem( v.getMetaScoreTypeId(), v.getName(locale) ));
        }        
        return out;        
    }

    
    public int getMetaScoreTypeId()
    {
        return this.metaScoreTypeId;
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



    public static MetaScoreType getValue( int id )
    {
        MetaScoreType[] vals = MetaScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getMetaScoreTypeId() == id )
                return vals[i];
        }

        return NONE;
    }

}
