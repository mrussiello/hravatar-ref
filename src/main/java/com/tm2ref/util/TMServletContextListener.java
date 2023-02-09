/*
 * Created on Dec 31, 2006
 *
 */
package com.tm2ref.util;

import com.tm2ref.global.Constants;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.UserFacade;
import java.util.Date;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import jakarta.servlet.annotation.WebListener;

@WebListener
public class TMServletContextListener implements ServletContextListener
{
    @Override
    public void contextDestroyed( ServletContextEvent arg0 )
    {}

    @Override
    public void contextInitialized( ServletContextEvent arg0 )
    {
        try
        {
            ServletContext servletContext = arg0.getServletContext();

            // init and place Session listener in ServletContext
            if( servletContext.getAttribute( Constants.SYSTEM_SESSION_COUNTER ) == null )
            {
                STHttpSessionListener listener = new STHttpSessionListener();

                listener.init( servletContext );
            }

            MessageFactory.setDefaultBundleName( Constants.DEFAULT_RESOURCE_BUNDLE );

            LogService.logIt( Constants.SERVER_START_LOG_MARKER );
            // RuntimeConstants.dumpAllValues();
            UserFacade userFacade = UserFacade.getInstance();

            userFacade.clearSharedCache();

            Tracker.startDate = new Date();
            
            servletContext.getSessionCookieConfig().setHttpOnly( true );
            servletContext.getSessionCookieConfig().setSecure( RuntimeConstants.getHttpsOnly() );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TMServletContextListener.contextInitialized() creating SessionListener" );
        }

    }

}
