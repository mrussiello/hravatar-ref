package com.tm2ref.user;

import com.tm2ref.util.MessageFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import jakarta.faces.model.SelectItem;



public enum RoleType
{
    NO_LOGON(0,"ult.noaccount") , // Account - Test Taker, cs request, whitepaper download, no account privileges, generic org, no login, no password
    PERSONAL_USER(10,"ult.personaluser") , // Personal logons allowed only (uses Default Account)
    DISABLED_USER(12,"ult.disabled") , // DISABLED by Corporate
    PORTAL_USER(15,"ult.portaluser") , // Portal and Personal logons allowed only
    ACCOUNT_LEVEL1(20,"ult.acctlevel1") , // Account - results only, can be org or suborg level (org or suborg level access)
    ACCOUNT_LEVEL1A(21,"ult.acctlevel1a") , // Account Viewer - View Incomplete Test Keys Only, can be org or suborg level (org or suborg level access - if suborg is set, then use is restricted to a specific suborg)
    ACCOUNT_LEVEL1B(22,"ult.acctlevel1b") , // Account Viewer - Create Test Keys and View Incomplete Test Keys Only, can be org or suborg level (org or suborg level access - if suborg is set, then use is restricted to a specific suborg)
    ACCOUNT_LEVEL2(25,"ult.acctlevel2") , // Account - Results, create pins, email test takers, etc. (org or suborg level access)
    ACCOUNT_LEVEL3(30,"ult.acctlevel3") , // Account - Create sub-orgs, Create account users (org access only)
    CSR(90,"ult.hracsr") , // HR Avatar CSR
    ADMIN(100,"ult.admin");

    private final int roleTypeId;

    private String key;


    private RoleType( int level , String key )
    {
        this.roleTypeId = level;

        this.key = key;
    }


    public boolean getIsAdmin()
    {
        return equals( ADMIN );
    }

    public boolean getIsDisabled()
    {
        return equals(  DISABLED_USER );
    }

    public boolean getIsAuthorizedForApi()
    {
        return roleTypeId>=ACCOUNT_LEVEL3.getRoleTypeId();
    }    
    
    public boolean getLogonAllowed()
    {
        return roleTypeId > NO_LOGON.getRoleTypeId() && roleTypeId != DISABLED_USER.roleTypeId;
    }

    public boolean getIsAccountLogon()
    {
        return roleTypeId > PORTAL_USER.getRoleTypeId();
    }


    public int getRoleTypeId()
    {
        return this.roleTypeId;
    }

    /*
    public static List<SelectItem> getAccountUserRoleTypeList( Locale locale )
    {
        List<SelectItem> out = new ArrayList();

        // out.add( new SelectItem( new Integer( PORTAL_PERSONAL_USER.getRoleTypeId() ), MessageFactory.getStringMessage(locale, PORTAL_PERSONAL_USER.getKey(), null ) ) );
        out.add( new SelectItem( new Integer( ACCOUNT_LEVEL1.getRoleTypeId() ), MessageFactory.getStringMessage(locale, ACCOUNT_LEVEL1.getKey(), null ) ) );
        out.add( new SelectItem( new Integer( ACCOUNT_LEVEL2.getRoleTypeId() ), MessageFactory.getStringMessage(locale, ACCOUNT_LEVEL2.getKey(), null ) ) );
        out.add( new SelectItem( new Integer( ACCOUNT_LEVEL3.getRoleTypeId() ), MessageFactory.getStringMessage(locale, ACCOUNT_LEVEL3.getKey(), null ) ) );

        return out;
    }
    */

    /*
    public static Map<String,Integer> getMap( Locale locale )
    {
        Map<String,Integer> outMap = new TreeMap();

        RoleType[] vals = RoleType.values();

        String name;

        for( int i=0 ; i<vals.length ; i++ )
        {
            name = MessageFactory.getStringMessage( locale, vals[i].getKey() , null );

            outMap.put( name , new Integer( vals[i].getRoleTypeId() ) );
        }

        return outMap;
    }
    */



    /*
    public String getName( Locale locale )
    {
        return MessageFactory.getStringMessage( locale, key , null );
    }
    */

    public String getName()
    {
        return MessageFactory.getStringMessage( Locale.US, key , null );
    }



    /**
     * Checks the user's access userLevelTypeId against what's required.
     * @param requiredLevel
     * @return
     */
    public static boolean accessOK( RoleType requiredLevel , RoleType currentLevel )
    {
        if( currentLevel.getRoleTypeId() >= requiredLevel.getRoleTypeId() )
            return true;

        return false;
    }


    /**
     *
     * @param levelIn
     * @return true is UserLevelType is an admin userLevelTypeId, false otherwise
     */
    public static boolean isAdmin( RoleType levelIn )
    {
        switch (levelIn)
        {
        case ADMIN:
            return true;

        default:
            return false;
        }
    }


    /**
     * Returns an enum value for the integer userLevelTypeId provided.
     *
     * @param levelIn
     * @return
     */
    public static RoleType getRoleTypeId( int r )
    {
        return getValue( r );
    }


    public static RoleType getValue( int id )
    {
        RoleType[] vals = RoleType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRoleTypeId() == id )
                return vals[i];
        }

        return ACCOUNT_LEVEL1;
    }



    public String getKey()
    {
        return key;
    }

}
