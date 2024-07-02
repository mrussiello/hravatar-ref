/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.util;

import com.tm2ref.service.LogService;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;


/**
 *
 * @author miker_000
 */
public class HttpUtils {
    
    private static PoolingHttpClientConnectionManager poolingConnManager;
    private static final int SO_TIMEOUT_SECS = 30;

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
                getPoolingConnManager();

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
    
    
    private static synchronized PoolingHttpClientConnectionManager getPoolingConnManager()
    {
        if( poolingConnManager==null )
        {
            poolingConnManager = new PoolingHttpClientConnectionManager();
            poolingConnManager.setMaxTotal(400);
            poolingConnManager.setDefaultMaxPerRoute(200);

            Timeout t = Timeout.ofMilliseconds(SO_TIMEOUT_SECS*1000);            
            SocketConfig sc = SocketConfig.custom()
                .setSoTimeout(t)
                .build();

            poolingConnManager.setDefaultSocketConfig(sc);
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
