package com.tm2ref.user;

import java.util.Locale;

public enum UserActionType
{
    SEARCH_PRODUCTS(1,"Product Search"),
    SEARCH_BLOG(2,"Product Blog"),
    SEARCH_NEWS(3,"Product News"),
    DOWNLOAD_WP(10,"Download White Paper" ),
    NEWSLETTER_SIGNUP(11,"Signup for Newsletter" ),
    VIEW_PROMOTION(12,"View Promotion" ),
    VISIT_WEBPAGE(13,"Visit a Misc Webpage" ),
    VIEW_BLOG(14,"View Blog Entry"),
    VIEW_NEWS(15,"View News Entry"),
    VIEW_PRODUCT(16,"View Product Detail" ),
    REGISTER_ACCOUNT(17,"Register for Account" ),
    SENT_EMAIL(18,"Sent Email Message" ),
    SENT_TEXT(19,"Sent Text Message" ),
    ACCOUNT_ACTIVATION(20,"Account Activation" ),
    TEST_RECONSTRUCT_VIA_REVIEW(21,"Test Reconstruct Via Review" ),
    START_ORGAUTOTEST(22,"Start OrgAutoTest" ),
    CUSTOM_PAGE(23,"Show Custom Page" ),
    USER_REFERRAL(24,"User Referral Submit" ),
    AUTO_EMAIL_ACTION(30,"AutoEmail Action" ),
    SUPPORT_CALL(50,"Support Phone conversation"),
    SALES_CALL(51,"Support Phone conversation" ),
    ONLINE_ORDER(70,"Online Order Completed" ),
    ACCOUNT_USER_ROLE_CHANGE(80,"Account User Role Change" ),
    ACCOUNT_USER_CREATE(81,"Account User Creation" ),
    SENT_EMAIL_BLOCKED(82,"Sent Email Blocked" ),
    UNFINISHED_PURCHASE(83,"Unfinished Purchase" ),
    UNFINISHED_REGISTRATION(84,"Unfinished Registration" ),
    EMAIL_UNBLOCK(85,"Email Unblock" ),
    ACCOUNT_USER_CHANGE(86,"Account User Infro Change" );

    private final int userActionTypeId;

    private final String name;

    private UserActionType( int typeId , String key )
    {
        this.userActionTypeId = typeId;

        this.name = key;
    }


    public static UserActionType getValue( int id )
    {
    	UserActionType[] vals = UserActionType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUserActionTypeId() == id )
                return vals[i];
        }

        return null;
    }



    public String getName( Locale locale )
    {
        return name;
    }

    public int getUserActionTypeId()
    {
        return userActionTypeId;
    }

    public String getName()
    {
        return name;
    }


}
