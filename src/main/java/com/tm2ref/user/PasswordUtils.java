/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.user;

import com.tm2ref.service.LogService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class PasswordUtils {
    
    public static final int MIN_PASSWORD_STRENGTH = 6;
    
    private static Map<String,List<Date>> failedLogonMap = null;
    private static Map<String,List<Date>> failedLogonMapIp = null;


    private static final int MAX_FAILED_LOGONS_IN_PD = 6;
    private static final int MAX_FAILED_LOGONS_IN_PD_4IP = 20;
    
    private static final int MAX_FAILED_LOGON_MINUTES = 20;
    
    public synchronized static void addFailedLogon4Ip( String ipAddress )
    {
        cleanFailedLogonCache();
        
        if( ipAddress==null || ipAddress.trim().isEmpty() )
            return;
        
        ipAddress=ipAddress.trim().toLowerCase();
        
        if( failedLogonMapIp==null )
            failedLogonMapIp = new HashMap<>();
        
        List<Date> dl = failedLogonMapIp.get( ipAddress );
        
        if( dl==null )
            dl = new ArrayList<>();
        
        dl.add(0, new Date() );
        
        failedLogonMapIp.put( ipAddress, dl);
    }

    public synchronized static boolean hasTooManyFailedLogons4Ip( String ipAddress )
    {
        cleanFailedLogonCache();
        
        if( ipAddress==null || ipAddress.trim().isEmpty() || failedLogonMapIp==null )
            return false;
        
        ipAddress=ipAddress.trim().toLowerCase();
                
        List<Date> dl = failedLogonMapIp.get( ipAddress );
        
        if( dl==null || dl.size()<MAX_FAILED_LOGONS_IN_PD )
        {
            LogService.logIt( "PasswordUtils.hasTooManyFailedLogons4Ip() AAA number of previous attempts=" + (dl==null ? "0" : dl.size() ) );
            return false;
        }
        
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.MINUTE, -1*MAX_FAILED_LOGON_MINUTES );
        Date start = cal.getTime();
        
        int ct = 0;
        
        for( Date d : dl )
        {
            if( d.after(start) )
                ct++;
        }
        
        LogService.logIt( "PasswordUtils.hasTooManyFailedLogons4Ip() BBB number of previous attempts=" + ct );
        return ct >= MAX_FAILED_LOGONS_IN_PD_4IP;
    }
    
    public synchronized static void clearFailedLogons( String username )
    {
        if( username==null || username.trim().isEmpty() )
            return;
        
        username=username.trim().toLowerCase();
        
        if( failedLogonMap==null )
            return;
        
        failedLogonMap.remove(username);
        
        cleanFailedLogonCache();
    }
    
    public synchronized static void addFailedLogon( String username )
    {
        cleanFailedLogonCache();
        
        if( username==null || username.trim().isEmpty() )
            return;
        
        username=username.trim().toLowerCase();
        
        if( failedLogonMap==null )
            failedLogonMap = new HashMap<>();
        
        List<Date> dl = failedLogonMap.get( username );
        
        if( dl==null )
            dl = new ArrayList<>();
        
        dl.add(0, new Date() );
        
        failedLogonMap.put( username, dl);
    }
    
    public synchronized static boolean hasTooManyFailedLogons( String username )
    {
        cleanFailedLogonCache();
        
        if( username==null || username.trim().isEmpty() || failedLogonMap==null )
            return false;
        
        username=username.trim().toLowerCase();
                
        List<Date> dl = failedLogonMap.get( username );
        
        if( dl==null || dl.size()<MAX_FAILED_LOGONS_IN_PD )
            return false;
        
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.MINUTE, -1*MAX_FAILED_LOGON_MINUTES );
        Date start = cal.getTime();
        
        int ct = 0;
        
        for( Date d : dl )
        {
            if( d.after(start) )
                ct++;
        }
        
        return ct >= MAX_FAILED_LOGONS_IN_PD;
    }
    
    public synchronized static void cleanFailedLogonCache()
    {
        cleanFailedLogonCache4Map( PasswordUtils.failedLogonMap );
        cleanFailedLogonCache4Map( PasswordUtils.failedLogonMapIp );
    }

    
    public synchronized static void cleanFailedLogonCache4Map( Map<String,List<Date>> theMap )
    {
        if( theMap==null || theMap.isEmpty() )
            return;
        
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.MINUTE, -1*MAX_FAILED_LOGON_MINUTES );
        Date start = cal.getTime();
        
        List<Date> dl;
        
        List<String> keys = new ArrayList<>();
        
        keys.addAll(theMap.keySet());
        
        ListIterator<Date> li;
        
        for( String key : keys )
        {
            dl = theMap.get( key );
            
            if( dl==null )
                continue;
            
            if( dl.isEmpty() )
            {
                theMap.remove(key);
                continue;
            }
            
            li = dl.listIterator();
            
            while( li.hasNext() )
            {
                if( li.next().before(start) )
                    li.remove();
            }
            
            if( dl.isEmpty() )
                theMap.remove(key);
        }        
    }
       
}
