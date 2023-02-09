/*
 * Created on Dec 31, 2006
 *
 */
package com.tm2ref.util;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.Constants;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.ref.RefUserType;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.RoleType;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import jakarta.servlet.annotation.WebListener;


@WebListener
public class STHttpSessionListener implements HttpSessionListener, Serializable
{
    // Map of sessionId : SessionInfo object. 
    Map<String, SessionInfo> sMap = null;

    private Date startDate = null;

    private boolean initC = false;

    private int totalS = 0;

    public STHttpSessionListener()
    {
        startDate = new Date();

        sMap = new ConcurrentHashMap<>();
    }

    
    @Override
    public void sessionDestroyed(HttpSessionEvent se)
    {
        if( se==null || se.getSession()==null )
            return;
        
        if( sMap != null )
            sMap.remove( se.getSession().getId() );
        
        //if( testKeySessionMap != null )
        //    clearTestKeyForSession( se.getSession().getId() );
    }



    
    public synchronized void init( ServletContext c )
    {
        if( !initC && c != null )
        {
            c.setAttribute( Constants.SYSTEM_SESSION_COUNTER, this );
            initC = true;
        }
    }

    @Override
    public void sessionCreated( HttpSessionEvent se )
    {
        if( sMap == null )
            sMap = new HashMap<>();

        totalS++;

        // clear old stuff every 10 sessions
        if( totalS % 10 == 0 )
            clearOld();

        if( !initC )
        {
            se.getSession().getServletContext().setAttribute( Constants.SYSTEM_SESSION_COUNTER, this );
            initC = true;
        }

        SessionInfo d = new SessionInfo();

        // d[0] = se.getSession().getId();

        d.last = new Date();

        sMap.put( se.getSession().getId(), d );
    }



    public Date getCurrentDateTime()
    {
        return new Date();
    }


    public void userLogout( String sid )
    {
        if( sid == null )
            return;

        SessionInfo d = sMap.get( sid );

        if( d == null )
            d = new SessionInfo();

        d.last = new Date();

        d.user = null;

        d.status = null;

        sMap.put( sid, d );
    }


    public void userLogon( String sid, User u )
    {
        if( sid == null || u == null )
            return;

        SessionInfo d = sMap.get( sid );

        if( d == null )
        {
            d = new SessionInfo();
            sMap.put( sid, d);
        }

        d.last = new Date();

        d.user = ( (RoleType.getValue( u.getRoleId() ).equals( RoleType.NO_LOGON ) || RoleType.getValue( u.getRoleId() ).equals( RoleType.DISABLED_USER ) )? "Gst" : u.getFullname() ) + " [" + u.getUserId() + "] orgId=" + u.getOrgId();

        // sMap.put( sid, d );

    }

    public void updateStatus( String sid, String status, String corp, User user, RcCheck rcCheck, RcRater rcRater, RefUserType refUserType )
    {
        if( sid == null || status == null )
            return;

        SessionInfo d = sMap.get( sid );

        if( d == null )
        {
            d = new SessionInfo();
            sMap.put( sid, d);
        }


        d.last = new Date();

        d.status = status;
        
        if( corp!=null )
            d.corp = corp;
        
        if( user!=null )
        {
            d.user = user.getFullname() + " (" + user.getUserId() + ")";
            d.orgId = user.getOrgId();
        }
        
        if( rcCheck!=null)
        {
            d.rcCheck = rcCheck.getRcCheckId() + "";
            d.orgId = rcCheck.getOrgId();
            
        }
        
        if( rcRater!=null )
            d.rcRater = rcRater.getRcRaterId() + "";

        if( refUserType!=null )
            d.type = refUserType.getName();

    }


    public void addData( String sid, User user, String status )
    {
        if( sid == null || status == null )
            return;

        SessionInfo d = sMap.get( sid );

        if( d == null )
        {
            d = new SessionInfo();
            sMap.put( sid, d);
        }


        d.last = new Date();
        d.status = status;  
        
        if( user!=null )
        {
            d.user = user.getFullname() + " (" + user.getUserId() + ")";
            d.orgId = user.getOrgId();
        }        
        
    }
    
    public Collection<SessionInfo> getDataList()
    {
        List<SessionInfo> out = new ArrayList<>();

        try
        {
            for( SessionInfo si : sMap.values() )
            {
                out.add( (SessionInfo) si.clone() );

            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "STHttpSessionListener.getDataList() " );
        }
        // out.addAll( sMap.values() );

        Collections.sort( out );

        return out;
    }
    
    


    public int getActiveSessionCount( int maxAgeSecs )
    {
        
        //if( 1==2 )
        //    return 1000;
        
        int count = 0;
        
        SessionInfo info;

        long now = ( new Date() ).getTime();

        long sDate = 0;

        List<String> keys = new ArrayList<>();
        keys.addAll( sMap.keySet() );
        
        for( String key : keys )
        {
            info = sMap.get( key );
            
            if( info==null )
                continue;

            sDate = info.last.getTime();

            // active check
            if( now - sDate <= 1000 * maxAgeSecs )
                count++;
        }
        
        return count;        
    }
    
    
    private void clearOld()
    {
        SessionInfo info;

        long now = ( new Date() ).getTime();

        long sDate = 0;

        List<String> keys = new ArrayList<>();
        keys.addAll( sMap.keySet() );
        
        for( String key : keys )
        {
            info = sMap.get( key );
            
            if( info==null )
                continue;

            sDate = info.last.getTime();

            if( now - sDate > 1000 * Constants.MAX_SESSIONLISTENER_DURATION )
                sMap.remove( key );
        }
    }


    public Map<String, Object> getStatusMap()
    {
        Map<String, Object> outMap = Tracker.getStatusMap();

        // outMap.put( "AA: Test Server Id:", RuntimeConstants.getStringValue( "TestServerInstanceId" ) );        
        
        outMap.put( "USER: Live Sessions", Integer.toString( sMap.size() ) );

        outMap.put( "USER: Active Sessions (60 Sec)", Integer.toString( getActiveSessionCount(60) ) );

        outMap.put( "USER: Sessions Since Startup", (Integer) totalS );

        try
        {
            Date d = new Date(ManagementFactory.getRuntimeMXBean().getStartTime());
            outMap.put( "SYSTEM: JVM Start Time", I18nUtils.getFormattedDateTime(Locale.US, d, TimeZone.getDefault()) );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "STHttpSessionListener.getStatusMap() getting JVM Start Time." );            
        }
                
        outMap.put( "SYSTEM: Start Date", I18nUtils.getFormattedDateTime(Locale.US, startDate, TimeZone.getDefault()) );
        outMap.put( "SYSTEM: Current Date", I18nUtils.getFormattedDateTime(Locale.US, new Date(), TimeZone.getDefault()) );

                
        return outMap;
    }

    public List<NVPair> getStatusList()
    {
        List<NVPair> outList = new ArrayList<>();

        Map<String, Object> valMap = getStatusMap();

        Object val;

        for( String key : valMap.keySet() )
        {
            val = valMap.get( key );

            if(  val==null )
                continue;
            
            if( val instanceof Integer )
            {
                if( ( (Integer) val ).intValue() == 0 )
                    continue;
            }

            if( val instanceof Long )
            {
                if( ( (Long) val ).longValue() == 0 )
                    continue;
            }

            if( val instanceof Integer )
                outList.add( new NVPair( key, (Integer) val ) );

            else if( val instanceof Long )
                outList.add( new NVPair( key, (Long) val ) );

            else if( val instanceof String )
                outList.add( new NVPair( key, (String) val ) );

            else
                outList.add( new NVPair( key, val.toString() ) );
        }

        Collections.sort( outList );

        return outList;
    }

}
