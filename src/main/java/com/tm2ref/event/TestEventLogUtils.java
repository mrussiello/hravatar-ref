/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.event;


import com.tm2ref.entity.event.TestEventLog;
import com.tm2ref.service.LogService;

/**
 *
 * @author miker_000
 */
public class TestEventLogUtils {
   
    
    

    /**
     *  2 - info
        1 - warning
        0 - error
     */
    public static void createTestKeyLogEntry( long testKeyId, int level, String logEntry )
    {
        try
        {
            TestEventLog tel = doTestEventLogEntry( testKeyId, 0, level, logEntry, null, null );

            if( tel!=null )
                EventFacade.getInstance().saveTestEventLog(tel);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TestEventLogUtils.createTestKeyLogEntry() testKeyId=" + testKeyId + ",testEventId=0, logEntry=" + logEntry );
        }        
    }
    
        
    private static TestEventLog doTestEventLogEntry( long testKeyId, long testEventId, int level, String logEntry, String ipAddress, String userAgent)
    {
        if( testEventId<=0 && testKeyId<=0 )
            return null;
        
        if( level<0 || level>2 )
            level=2;
        
        if( logEntry==null || logEntry.isEmpty() )
            return null;
        
        TestEventLog tel = new TestEventLog();

        tel.setTestKeyId( testKeyId );
        tel.setTestEventId(testEventId);
        tel.setLevel(level);
        tel.setLog(logEntry);
        tel.setIpAddress(ipAddress);
        tel.setUserAgent(userAgent);
        
        return tel;
    }
}
