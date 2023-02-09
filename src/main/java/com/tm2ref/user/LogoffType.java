package com.tm2ref.user;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public enum LogoffType
{
    USER(1,"logofftype.userlogoff"),
    USER_REPLACEMENT(2,"logofftype.userreplacement"),
    SESSION_EXPIRE(3, "logofftype.sessionexpire" ),
    CORP_LOGON(4,"logofftype.corplogon"),
    CORP_LOGOUT(5,"logofftype.corplogoff"),
    AUTOTEST_ENTRY(6,"logofftype.autotestentry");

    private int logoffTypeId;

    private String key;

    private LogoffType( int typeId , String key )
    {
        this.logoffTypeId = typeId;

        this.key = key;
    }

    public static Map<String,Integer> getMap( Locale locale )
    {
        Map<String,Integer> outMap = new TreeMap<String,Integer>();

        LogoffType[] vals = LogoffType.values();

        String name;

        for( int i=0 ; i<vals.length ; i++ )
        {
            name = MessageFactory.getStringMessage( locale, vals[i].getKey() , null );

            outMap.put( name , new Integer( vals[i].getLogoffTypeId() ) );
        }

        return outMap;
    }


    public String getName( Locale locale )
    {
        return MessageFactory.getStringMessage( locale, key , null );
    }

    public int getLogoffTypeId()
    {
        return logoffTypeId;
    }

    public String getKey()
    {
        return key;
    }
}
