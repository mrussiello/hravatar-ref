package com.tm2ref.faces;

import com.tm2ref.service.LogService;



public enum BrowserType
{
    UNKNOWN(0,"bt.unknown"),
    SAFARI_IPHONE(1,"bt.safariiphone" ),   // does scale video
    // IPHONE4(2,"bt.iphone"  ), // does scale video
    ANDROID_NATIVE(3,"bt.droidmobile"  ), // seems to not scale video but need to check
    SAFARI_IPAD(4,"bt.safariipad" ), // does scale video
    BLACKBERRY(5,"bt.blackberry" ), // ??, runs webkit
    SYMBIAN(6,"bt.symbian" ), // ??, runs webkit,
    // SVGA(7,"bt.svga" ),
    // XVGA(8,"bt.xvga"  ),
    MSIE_8_OR_LOWER(9,"bt.msie8"  ),
    SAFARI_DESKTOP(10,"bt.safaripc"  ),
    CHROME_DESKTOP(11,"bt.chromepc"  ),
    FIREFOX_DESKTOP(12,"bt.firefoxpc"  ),
    OPERA_DESKTOP(13,"bt.operapc"  ),
    SAFARI_IPOD(14,"bt.safariipod"  ),
    CHROME_ANDROIDTABLET(15,"bt.droidtablet"  ),
    WINDOWSPHONE7(16,"bt.winphone7"  ),
    MSIE9(17,"bt.windiwsmsie9pc"  ),
    OPERAMINI(18,"bt.operamini"  ),
    WINDOWSPHONE8(19,"bt.winphone8"  ),
    KINDLEFIRE(20,"bt.silk"  ),
    NOOK(21,"bt.nook"  ),
    OPERAMOBILE(22,"bt.operamobile"  ),
    MSIE10_PLUS(23,"bt.msie10pc"  ),
    CHROME_MOBILE(24,"bt.chromemobile"  ),
    FIREFOX_MOBILE(25,"bt.firefoxmobile"  ),
    MSIE10_MOBILE(26,"bt.msie10mobile"  ),
    BLACKBERRY10_MOBILE(27,"bt.blackberry10mobile"  ),
    BLACKBERRY10_TABLET(28,"bt.blackberry10tablet"  ),
    MSIE11_MOBILE(29,"bt.msie11mobile"  ),
    PEARSON_VUE(30,"bt.pearsonvuew"  ),
    EDGE_DESKTOP(31,"bt.edge"  ),
    EDGE_MOBILE(32,"bt.edgemobile"  ),
    SAFARI_MOBILE_IOS10PLUS(33,"bt.safariios10mobile"  ),
    UCBROWSER_MOBILE(34,"bt.ucbrowsermobile"  ),
    UCBROWSER_DESKTOP(35,"bt.ucbrowserdesktop"  ),
    SAFARI_DESKTOP_VER11PLUS(36,"bt.safaripcosx11plus"  ),
    CHROME_BASED_EDGE_MOBILE(37,"bt.edgemobilechrome"  ),
    CHROME_BASED_EDGE_DESKTOP(38,"bt.edgechrome"  ),
    SECURE_EXAM_BROWSER(100,"bt.seb"  ),
    SECURE_EXAM_BROWSER_MAC(101,"bt.sebmac"  );


    private final int browserTypeId;

    private final String key;


    private BrowserType( int p , String k )
    {
        this.browserTypeId = p;

        this.key = k;
    }

    public boolean isMsie()
    {
        return equals( MSIE10_PLUS ) || equals( MSIE11_MOBILE ) || equals( MSIE9 ) || equals( MSIE_8_OR_LOWER  );
    }


    public static BrowserType getFmUserAgent( String u )
    {
    	if( u == null || u.length() == 0 )
    		return UNKNOWN;

    	u = u.toLowerCase();

        int ua = 0;
        //boolean flscrnvid=false;

        // SEB
        if( u.indexOf("seb-hravatar-v")>=0 )
        {
            if( u.indexOf("macintosh;")>=0 || u.indexOf("mac os x ")>=0 )
              ua=101;
            else
                ua=100;
        }
        
    	// IOS
        else if( u.indexOf( "iphone;") >= 0 || u.indexOf( "ipod;") >= 0  || u.indexOf( "ipad;") >= 0 )
        {
            //if( u.indexOf( "ipad;" ) <= 0 )
            //    flscrnvid = true;
            // iPhone early versin.
            //if( u.indexOf( "ipad" )<0 && (u.indexOf( "iphone" )>0 || u.indexOf( "ipod;" )>0 ) && u.indexOf( "iphone os 1" )<0 && u.indexOf( "iphone os 2" )<0 )
            //    flscrnvid = true;                    

            // must go ahead of chrome since chrome shows up in UA
            if( u.indexOf( "opera mobi" ) > 0 || u.indexOf( "opr/" ) > 0 )
                ua=22;

            else if( u.indexOf( "ucbrowser" ) > 0 )
                ua=34;
                
            if( u.indexOf( "chrome/" ) > 0 || u.indexOf( "crios/" ) > 0 )
            {
                if( u.indexOf( "edge/" ) > 0 || u.indexOf( "edgios/" ) > 0 )
                    ua=32;
    
                else if( u.indexOf( "edg/" ) > 0 )
                    ua=37;
    
                else
                    ua=24;
            }

            else if( u.indexOf( "edgios/" ) > 0 )
                ua=32;

            else if( u.indexOf( "firefox/" ) > 0 || u.indexOf( "fxios/" ) > 0 )
                ua=25;

            else if( u.indexOf( "ipod;" ) > 0)
                ua=14;

            else if( u.indexOf( "ipad;" ) > 0)
                ua=4;
            
            // iPhone Safari OS 10 or higher 
            else if( u.indexOf( "iphone os 1" )>=0 || u.indexOf( "iphone os 2" )>=0 )
                ua=33;
            
            // it"s not a tablet. Must be an iphone
            else
                ua = 1;
        }

        // opera mini is not useful. So detect it early.
	else if( u.indexOf( "opera mini" ) > 0 )
		 ua = 18;

	else if( u.indexOf( "android" ) > 0 ) // can be Opera, Chrome, Firefox - any of these, plus stock (safari).
        {
            // must go ahead of chrome since chrome shows up in UA
            if( u.indexOf( "opera mobi" ) > 0 || u.indexOf( " opr/" ) > 0 )
            {
                ua = 22;
            }

            else if( u.indexOf( "ucbrowser" ) > 0 )
                ua = 34;
                
            else if( u.indexOf( "chrome/" ) > 0 || u.indexOf( "crios/" ) > 0 )
            {
                if( u.indexOf( "edge/" ) > 0 || u.indexOf( "edga/" ) > 0 )
                    ua=32;
    
                if( u.indexOf( "edg/" ) > 0  )
                    ua=37;
    
                else
                    ua=24;
            }
            
            else if( u.indexOf( "edge/" ) > 0 || u.indexOf( "edga/" ) > 0 )
                ua=32;
            
            else if( u.indexOf( "firefox/" ) > 0 )
                ua = 25;

            // if it says mobile, then it"s not a tablet.
            else
            {
                ua = u.indexOf( "mobile" ) > 0 ? 3 : 15;
                //flscrnvid = true;
            }
        }

	else if( u.indexOf( "windows phone os 7" ) > 0 || u.indexOf( "windows phone 7" ) > 0 )
	{
            if( u.indexOf( "msie 1" ) > 0 || u.indexOf( "trident/7" ) > 0  )
		ua = 26;
            else if( u.indexOf( "edge/" ) > 0)
                ua = 32;
            else if( u.indexOf( "edg/" ) > 0)
                ua = 37;
            else
                ua = 16;

            //flscrnvid = true;
        }

	else if( u.indexOf( "windows phone os 8" ) > 0 || u.indexOf( "windows phone 8" ) > 0 )
	{
            // IE10
            if( u.indexOf( "msie 10" ) > 0 || u.indexOf( "trident/6" ) > 0  )
		ua = 26;

            // IE 11+
            else if( u.indexOf( "msie 1" ) > 0 || u.indexOf( "trident/7" ) > 0 || u.indexOf( "iemobile/1" ) > 0 )
            {
		ua = 29;
                //flscrnvid = false;
            }

            else if( u.indexOf( "edge/" ) > 0)
                ua = 32;
            
            else if( u.indexOf( "edg/" ) > 0)
                ua = 37;
            
            else
                ua = 19;

            //flscrnvid = true;
        }

        // Blackberry Tablet
	else if( u.indexOf( "playbook" ) > 0 && u.indexOf( "tablet" ) > 0 && u.indexOf( "applewebkit" ) > 0 )
	{
            ua = 28;
            // flscrnvid = true;
        }
                
	else if( u.indexOf( "ipad" ) > 0 )
		 ua = 4;

	else if( u.indexOf( "blackberry" ) > 0  )
	{
            ua = 5;
            // flscrnvid = true;
        }

	else if( u.indexOf( "opera mobi" ) > 0 )
	{
            ua = 22;
            // flscrnvid = true;
        }

        // UC Browser
        else if( u.indexOf( "ucbrowser" ) > 0 )
        {
            ua = u.indexOf( "mobile" ) > 0 ? 34 : 35;
        }
        
        // THESE MUST COME BEFORE SAFARI
	else if( u.indexOf( "silk/" ) > 0 )
	{
            ua = 20;
            //flscrnvid = true;
        }

	else if( u.indexOf( "nook browser/" ) > 0 )
	{
            ua = 21;
            //flscrnvid = true;
        }

	else if( u.indexOf( "webkit" ) > 0 && ( u.indexOf( "symbian" ) > 0 || u.indexOf( "series60" ) > 0 ) )
	{
            ua = 6;
            // flscrnvid = true;
        }

	else if( u.indexOf( "msie 9" ) > 0  )
		ua = 17;

        // note - MSIE 11 does not have msie in it"s UA!
        // note - MSIE 11+ does not have msie in it"s UA!  Replaced with rv:
	else if( u.indexOf( "msie 1" ) > 0 || ( u.indexOf( "trident/" ) > 0 && u.indexOf( " rv:1" ) > 0) )
		ua = 23;

	else if( u.indexOf( "msie" ) > 0 )
		ua = 9;

        // must come before chrome or safari since both are in the UA
	else if( u.indexOf( "opera/" ) > 0 || u.indexOf( "opr/" ) > 0 )
		ua = 13;

	// must come before Safari
	else if( u.indexOf( "chrome/" ) > 0 )
	{
            if( u.indexOf( "edge/" ) > 0)
                ua = 31;
            else if( u.indexOf( "edg/" ) > 0)
                ua = 38;
            else
                ua = 11;
        }

        // PVUERendering
	else if( u.indexOf( "pvuerendering" ) > 0  )
	{
                ua = 30;
        }
        
	else if( u.indexOf( "safari/" ) > 0  )
	{
            if( getIsSafariMacVersion11Plus( u ) )
                ua = 36;
            
            else
                ua = 10;
        }

	else if( u.indexOf( "firefox/" ) > 0 )
	{
            ua = 12;
        }

	else if( u.indexOf( "edge/" ) > 0 )
	{
            ua = 31;
        }

	else if( u.indexOf( "edg/" ) > 0 )
	{
            ua = 38;
        }

	else if( u.indexOf( "macintosh;" ) > 0  )
	{
            ua = 10;
        }

        
        return BrowserType.getValue(ua);
    }


    public static boolean getIsSafariMacVersion11Plus( String u )
    {
    	if( u == null || u.length() == 0 )
            return false;

    	u = u.toLowerCase();

        int idx = u.indexOf( " version/" );        
        if( idx<0 )
            return false;
        
        int idx2 = u.indexOf( ".", idx + 9 );        
        if( idx2<0 )
            return false;
        
        String ss = u.substring(idx + 9, idx2);
        
        int vn = 0;
        
        try
        {
            vn = Integer.parseInt( ss );
        }
        catch( Exception e )
        {
            LogService.logIt(e,"BrowserType.getIsSafariMacVersion11Plus() u=" + u );
        }
        return vn>=11;        
    }
    
    

    public int getBrowserTypeId()
    {
        return this.browserTypeId;
    }

    public String getKey()
    {
        return key;
    }



    public static BrowserType getValue( int id )
    {
        BrowserType[] vals = BrowserType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getBrowserTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }


}
