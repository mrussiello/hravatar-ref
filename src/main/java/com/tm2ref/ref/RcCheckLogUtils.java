/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCheckLog;
import com.tm2ref.service.LogService;

/**
 *
 * @author miker_000
 */
public class RcCheckLogUtils {
       
    
    /**
     *  2 - info
        1 - warning
        0 - error
     */
    public static RcCheckLog createRcCheckLogEntry( long rcCheckId, long rcRaterId, int level, String logEntry, String ipAddress, String userAgent) 
    {
        try
        {
            if( rcCheckId<=0 )
                return null;

            if( level<0 || level>2 )
                level=2;

            if( logEntry==null || logEntry.isBlank())
                return null;

            RcCheckLog tel = new RcCheckLog();            
            tel.setRcCheckId(rcCheckId);
            tel.setRcRaterId(rcRaterId);
            tel.setLevel(level);
            tel.setLog(logEntry);
            tel.setIpAddress(ipAddress);
            tel.setUserAgent(userAgent);
            
            return RcFacade.getInstance().saveRcCheckLog(tel);            
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "RcCheckLogUtils.createRcCheckLogEntry() rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", logEntry=" + logEntry );
            return null;
        }
    }
           
}
