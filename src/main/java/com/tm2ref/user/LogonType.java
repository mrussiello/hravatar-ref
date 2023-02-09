package com.tm2ref.user;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public enum LogonType
{
    USER(1,"logontype.userlogon"),
    NEW_REGISTRATION(4,"logontype.newregistration" ),
    SUPERUSER_LOGON(5,"logontype.superuser" ),
    COOKIE_AUTO(6,"logontype.cookieauto" ),
    NEWSLETTER_SUBUNSUB(7,"logontype.newslettersubunsub" ),
    PAYMENT_PROCESSING(8 , "logontype.paymentprocessing" ),
    CORP_LOGON(9 , "logontype.corp" );

    private int logonTypeId;

    private String key;

    private LogonType( int typeId , String key )
    {
        this.logonTypeId = typeId;

        this.key = key;
    }

    public static Map<String,Integer> getMap( Locale locale )
    {
        Map<String,Integer> outMap = new TreeMap<>();

        LogonType[] vals = LogonType.values();

        String name;

        for( int i=0 ; i<vals.length ; i++ )
        {
            name = MessageFactory.getStringMessage( locale, vals[i].getKey() , null );

            outMap.put( name , new Integer( vals[i].getLogonTypeId() ) );
        }

        return outMap;
    }


    public String getName( Locale locale )
    {
        return MessageFactory.getStringMessage( locale, key , null );
    }

    public int getLogonTypeId()
    {
        return logonTypeId;
    }

    public String getKey()
    {
        return key;
    }
}
