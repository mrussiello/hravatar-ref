/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.util;

import com.tm2ref.service.LogService;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 *
 * @author miker_000
 */
public class HttpUtils {
    
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
            
}
