package com.tm2ref.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;


@Named
@ApplicationScoped
public class TimeZoneLister
{
    private static List<TimeZone> zList = null;

    private static synchronized void init()
    {
        if( zList != null )
            return;
        
        TimeZone tz;
        // so now we have a list of display names and ids. 
        zList = new ArrayList<>();

        for( String id : TimeZone.getAvailableIDs() )
        {
            tz = TimeZone.getTimeZone(id);
            
            if( tz.getID().length()<=3 || ( tz.getID().toLowerCase().startsWith("etc/") && tz.getID().toLowerCase().indexOf("UTC")<0 ) )
                continue;

            zList.add( tz );
        }
        
        
        /*
        String[] ids = TimeZone.getAvailableIDs();

        zList = new ArrayList<>();

        for( String id : ids )
        {
            zList.add( TimeZone.getTimeZone(id) );
        }
        */

        /*
        zList.add( TimeZone.getTimeZone( "Etc/GMT-14" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Tongatapu" ) );
        zList.add( TimeZone.getTimeZone( "NZ-CHAT" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Wallis" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Wake" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Tarawa" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Nauru" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Kwajalein" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Fiji" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Auckland" ) );
        zList.add( TimeZone.getTimeZone( "Kwajalein" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Kamchatka" ) );
        zList.add( TimeZone.getTimeZone( "Antarctica/McMurdo" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Norfolk" ) );
        zList.add( TimeZone.getTimeZone( "SST" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Noumea" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Guadalcanal" ) );
        zList.add( TimeZone.getTimeZone( "Australia/LHI" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Truk" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Port_Moresby" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Guam" ) );
        zList.add( TimeZone.getTimeZone( "Australia/Melbourne" ) );
        zList.add( TimeZone.getTimeZone( "Australia/Hobart" ) );
        zList.add( TimeZone.getTimeZone( "Australia/Brisbane" ) );
        zList.add( TimeZone.getTimeZone( "Australia/ACT" ) );
        zList.add( TimeZone.getTimeZone( "Australia/Yancowinna" ) );
        zList.add( TimeZone.getTimeZone( "Australia/South" ) );
        zList.add( TimeZone.getTimeZone( "Australia/North" ) );
        zList.add( TimeZone.getTimeZone( "Japan" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Yakutsk" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Seoul" ) );
        zList.add( TimeZone.getTimeZone( "Australia/West" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Singapore" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Manila" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Makassar" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Kuala_Lumpur" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Hong_Kong" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Chungking" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Brunei" ) );
        zList.add( TimeZone.getTimeZone( "Indian/Christmas" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Jakarta" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Bangkok" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Rangoon" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Thimbu" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Novosibirsk" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Dacca" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Katmandu" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Calcutta" ) );
        zList.add( TimeZone.getTimeZone( "Indian/Maldives" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Tashkent" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Karachi" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Ashkhabad" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Kabul" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Dubai" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Tehran" ) );
        zList.add( TimeZone.getTimeZone( "Europe/Moscow" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Baghdad" ) );
        zList.add( TimeZone.getTimeZone( "Africa/Nairobi" ) );
        zList.add( TimeZone.getTimeZone( "Europe/Istanbul" ) );
        zList.add( TimeZone.getTimeZone( "Asia/Jerusalem" ) );
        zList.add( TimeZone.getTimeZone( "Africa/Maputo" ) );
        zList.add( TimeZone.getTimeZone( "Africa/Johannesburg" ) );
        zList.add( TimeZone.getTimeZone( "Europe/Paris" ) );
        zList.add( TimeZone.getTimeZone( "Africa/Lagos" ) );
        zList.add( TimeZone.getTimeZone( "Europe/London" ) );
        // zList.add( TimeZone.getTimeZone( "Zulu" ) );
        // zList.add( TimeZone.getTimeZone( "WET" ) );
        zList.add( TimeZone.getTimeZone( "Atlantic/Cape_Verde" ) );
        zList.add( TimeZone.getTimeZone( "Atlantic/Azores" ) );
        zList.add( TimeZone.getTimeZone( "Etc/GMT+2" ) );
        zList.add( TimeZone.getTimeZone( "Atlantic/South_Georgia" ) );
        zList.add( TimeZone.getTimeZone( "Brazil/East" ) );
        zList.add( TimeZone.getTimeZone( "America/Buenos_Aires" ) );
        zList.add( TimeZone.getTimeZone( "America/Puerto_Rico" ) );
        zList.add( TimeZone.getTimeZone( "US/Eastern" ) );
        zList.add( TimeZone.getTimeZone( "US/Central" ) );
        zList.add( TimeZone.getTimeZone( "America/Denver" ) );
        zList.add( TimeZone.getTimeZone( "US/Pacific" ) );
        zList.add( TimeZone.getTimeZone( "US/Alaska" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Tahiti" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Honolulu" ) );
        zList.add( TimeZone.getTimeZone( "America/Adak" ) );
        zList.add( TimeZone.getTimeZone( "Pacific/Samoa" ) );
        zList.add( TimeZone.getTimeZone( "Etc/GMT+12" ) );
         *
         */
    }


    public static String getTimeZoneInfoStr()
    {
        StringBuilder sb = new StringBuilder();

        if( zList == null )
            init();

        for( TimeZone tz : zList )
        {
            sb.append( tz.getDisplayName() + ", id=" + tz.getID() + ", offset: " + tz.getRawOffset() + ", " + ((float)tz.getRawOffset()/(60*60*1000)) + "\n" );
        }

        return sb.toString();
    }

    
    public static TimeZone getAvailableTimeZoneForTimeZoneId( String timeZoneId )
    {
        if( zList==null )
            init();
        
        if( timeZoneId==null || timeZoneId.isBlank() )
            return null;
        
        TimeZone tzx = TimeZone.getTimeZone(timeZoneId);
        
        if( tzx==null )
            return null;
                
        // LogService.logIt( "TimeZoneister.getAvailableTimeZoneForTimeZoneId() AAA tzx.id=" + tzx.getID() + ", short displayname=" + tzx.getDisplayName(false, TimeZone.SHORT) );

        // First try to match on Short Disp Name and Offset
        for( TimeZone tz : zList )
        {
            if( tz.getID().equals( tzx.getID() ))
                return tz;
        }
        
        // First try to match on Short Disp Name and Offset
        for( TimeZone tz : zList )
        {
            //if( tz.getOffset(new Date().getTime())==tzx.getOffset(new Date().getTime())  )
             //   LogService.logIt( "TimeZoneister.getAvailableTimeZoneForTimeZoneId() BBB Checking " + tz.getID() + ", short displayname=" + tz.getDisplayName(false, TimeZone.SHORT) + ", equals=" + tz.equals(tzx) );
            
            if( tz.equals(tzx))
                return tzx;
            
            if( !tz.getDisplayName(false, TimeZone.SHORT).equalsIgnoreCase( tzx.getDisplayName(false, TimeZone.SHORT)))
                continue;
            
            // Match within plus minus 5 minutes
            if( tz.getOffset(new Date().getTime())==tzx.getOffset(new Date().getTime())  )
                return tz;
        }

        //LogService.logIt( "TimeZoneister.getAvailableTimeZoneForTimeZoneId() CCC No Match on Short Disp names, just checking offset now" );
        
        // Next just offset
        for( TimeZone tz : zList )
        {
            // Match within plus minus 5 minutes
            if( tz.getOffset(new Date().getTime())==tzx.getOffset(new Date().getTime())  )
                return tz;
        }
        
        // LogService.logIt( "TimeZoneLister.getAvailableTimeZoneForTimeZoneId() could not find a compatible time zone for id=" + timeZoneId );

        return null;
    }
    
    
    public static TimeZone getAvailableTimeZoneForOffsetMins( int offsetMins, String timeZoneIdFmBrowser )
    {
        if( zList == null )
            init();

        if( timeZoneIdFmBrowser!=null && !timeZoneIdFmBrowser.isBlank() )
        {
            TimeZone tzx = getAvailableTimeZoneForTimeZoneId( timeZoneIdFmBrowser );
            if( tzx!=null )
                return tzx;
        }
        
        // convert to MS
        offsetMins = offsetMins*60*1000;

        for( TimeZone tz : zList )
        {
            // Match within plus minus 5 minutes
            if( tz.getOffset(new Date().getTime()) <= (offsetMins + 5*60*1000) &&  tz.getOffset(new Date().getTime()) >= (offsetMins - 5*60*1000) )
                return tz;
        }

        return null;
    }
    
    
    
    /*
    public static TimeZone getAvailableTimeZoneForOffset( float offset )
    {
        if( zList == null )
            init();

        offset = offset*60*60*1000;

        for( TimeZone tz : zList )
        {
            // Match within plus minus two minutes
            if( tz.getRawOffset() <= ((int) offset + 2*60*1000) &&  tz.getRawOffset() >= ((int) offset - 2*60*1000) )
                return tz;
        }

        return null;
    }
    */
}
