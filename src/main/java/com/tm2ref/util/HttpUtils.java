/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.util;

import com.tm2ref.service.LogService;
import java.net.URI;
import java.net.URL;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;


/**
 *
 * @author miker_000
 */
public class HttpUtils {
    
    private static PoolingHttpClientConnectionManager poolingConnManager;
    private static final int SO_TIMEOUT_SECS = 30;

    
    public static URL getURLFromString( String urlStr )
    {
        if( urlStr==null || urlStr.isBlank() )
            return null;
        
        if( !urlStr.toLowerCase().startsWith("http" ) && !urlStr.toLowerCase().startsWith("file" ))
            LogService.logIt( "HttpUtils.getURLFromString() UrlStr appears to be invalid. trying anyway. " + urlStr );

        try
        {
            return new URI( urlStr ).toURL();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "HttpUtils.getURLFromString() " + urlStr );
            return null;
        }
    }
    
    
    public static CloseableHttpClient getHttpClient( int timeoutSecs )
    {
        try
        {
            if( timeoutSecs< 10 )
                timeoutSecs=10;
            
            // int soTimeoutMs = 1000*timeoutSecs;
            int connectionTimeoutMs = 1000*timeoutSecs;
            // int socketTimeoutMs = 1000*30;

            if( poolingConnManager==null )
                getPoolingConnManager( false );

            // configure the timeouts (socket and connection) for the request
            RequestConfig.Builder config = RequestConfig.copy(RequestConfig.DEFAULT);
            config.setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionTimeoutMs));
            
            // config.setSocketTimeout(Timeout.ofMilliseconds(socketTimeoutMs));
            
            return HttpClients.custom()
                        .setConnectionManager(poolingConnManager)
                        .setConnectionManagerShared(true)
                        .evictExpiredConnections()
                        .evictIdleConnections( TimeValue.ofSeconds(30))
                        .build();
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "HttpUtils.getHttpClient() timeoutSecs=" + timeoutSecs );            
            return null;
        }
    }
    
    /*
     * out[0]=available
       out[1] = leased
       out[2] = pending ??
       out[3] = max
    
    */
    public static synchronized int[] getConnManagerStats()
    {
        int[] out = new int[4];
        
        if( poolingConnManager!=null )
        {
            PoolStats ps = poolingConnManager.getTotalStats();
            out[0] = ps.getAvailable();
            out[1] = ps.getLeased();
            out[2] = ps.getPending();
            out[3] = ps.getMax();
        }
        
        return out;
    }
    
    public static synchronized String getConnManagerStatsStr()
    {
        int[] dat = getConnManagerStats();
        return "Available: " + dat[0] + ", leased: " + dat[1] + ", pending (requests waiting for a free conn): " + dat[2] + ", max: " + dat[3]; 
    }
        
    public static synchronized void resetPooledConnectionManagerIfNeeded()
    {        
        
        int[] dat = getConnManagerStats();
        LogService.logIt( "HttpUtils.resetPooledConnectionManagerIfNeeded() Checking: " + getConnManagerStatsStr() );
        if( dat[0]<=10 )
        {
            LogService.logIt( "HttpUtils.resetPooledConnectionManagerIfNeeded() Resetting. " );
            getPoolingConnManager( true );
        }
    }
        
    public static synchronized void resetPooledConnectionManager()
    {        
        LogService.logIt( "HttpUtils.resetPooledConnectionManager() Resetting. " + getConnManagerStatsStr() );
        getPoolingConnManager( true );
    }
    
    
    private static synchronized PoolingHttpClientConnectionManager getPoolingConnManager(boolean force)
    {
        if( force || poolingConnManager==null )
        {
            PoolingHttpClientConnectionManager poolingConnManager2 = new PoolingHttpClientConnectionManager();
            poolingConnManager2.setMaxTotal(500);
            poolingConnManager2.setDefaultMaxPerRoute(400);

            Timeout t = Timeout.ofMilliseconds(SO_TIMEOUT_SECS*1000);            
            SocketConfig sc = SocketConfig.custom()
                .setSoTimeout(t)
                .build();

            poolingConnManager2.setDefaultSocketConfig(sc);
        
            poolingConnManager = poolingConnManager2;
        }
        
        return poolingConnManager;
    }
    
    
    
    /*
    public static CloseableHttpClient getHttpClient( int timeoutSecs )
    {
        try
        {
            if( timeoutSecs< 10 )
                timeoutSecs=10;
            
            int soTimeoutMs = 1000*30;
            int connectionTimeoutMs = 1000*30;
            int socketTimeoutMs = 1000*30;

            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
            connManager.setMaxTotal(200);
            connManager.setDefaultMaxPerRoute(100);

            SocketConfig sc = SocketConfig.custom()
                .setSoTimeout(soTimeoutMs)
                .build();

            connManager.setDefaultSocketConfig(sc);

            // configure the timeouts (socket and connection) for the request
            RequestConfig.Builder config = RequestConfig.copy(RequestConfig.DEFAULT);
            config.setConnectionRequestTimeout(connectionTimeoutMs);
            config.setSocketTimeout(socketTimeoutMs);
            
            return HttpClients.custom()
                        .setConnectionManager(connManager)
                        .setConnectionManagerShared(true)
                        .build();
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "HttpUtils.getHttpClient() timeoutSecs=" + timeoutSecs );
            
            return null;
        }
    }
    */
            
}
