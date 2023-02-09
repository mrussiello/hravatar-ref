/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.corp.CorpBean;
import com.tm2ref.corp.CorpUtils;
import com.tm2ref.faces.AbstractFacesServlet;
import com.tm2ref.global.STException;
import com.tm2ref.proctor.ProctorBean;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.UserBean;
import com.tm2ref.util.MessageFactory;
import java.io.IOException;
import java.util.Locale;
import jakarta.inject.Inject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Receives URL with
 *
 * Candidate:
 * /tr/rce/accesscode

 * Candidate:
 * /tr/rce/accesscode
 *
 * Access Code is
*
 * @author miker_000
 */
@WebServlet(name = "RefEntryServlet", urlPatterns = {"/rce/*"})
public class RefEntryServlet  extends AbstractFacesServlet {

    private final String CORE_URL_PATTERN = "rce";

    private ServletContext context;

    @Inject
    RefUtils refUtils;

    @Inject
    RefBean refBean;

    @Inject
    UserBean userBean;

    @Inject
    CorpBean corpBean;

    @Inject
    ProctorBean proctorBean;


    @Inject
    CorpUtils corpUtils;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        // servletConfig = config;
        context = config.getServletContext();
    }


    // Parse and create TestEntryParams
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
    {
        String nextViewId = null;

        try
        {
            // Do this to set the FacesContext for this request.
            getFacesContext( request, response);

            refUtils.setRefBean( refBean );
            refUtils.setUserBean(userBean);
            corpUtils.setCorpBean(corpBean);
            corpUtils.setHttpServletRequest(request);
            corpUtils.setUserBean(userBean);

            if( !corpBean.getHasCorp() )
                corpUtils.loadCorpIfNeeded(0, true, response);

            refUtils.setProctorBean(proctorBean);
            refUtils.setCorpBean(corpBean);
            refUtils.setCorpUtils(corpUtils);
            refUtils.setHttpServletRequest(request);

            String accessCode = getAccessCodeFromUrl( request.getRequestURI() );

            if( accessCode==null || accessCode.isBlank() )
                throw new Exception( "URLFORMAT " + request.getRequestURI() );

            // LogService.logIt( "RefEntryServlet.processRequest() AAA.1 accessCode=" + accessCode );

            nextViewId = refUtils.performSimpleEntry(0, 0, 0, accessCode, false);
            Tracker.addServletEntry();

            // LogService.logIt( "RefEntryServlet.processRequest() AAA.2 nextViewId=" + nextViewId );

            if( nextViewId == null || nextViewId.isEmpty() )
                nextViewId = "/index.xhtml";


            String forwardUrl = correctUrlForForward( nextViewId );
            // LogService.logIt( "TestKeyEntryServlet.processRequest() url=" + request.getRequestURI() + ", nextViewId=" + nextViewId + ", FORWARDING to=" + forwardUrl );
            forward(  forwardUrl,  request,  response );
        }
        catch( Exception e )
        {
            String[] errParams = null;

            if( e instanceof STException )
            {
                STException ste = (STException) e;

                // LogService.logIt( "RefEntryServlet.processRequest() STException url=" + request.getRequestURI() + " " + ste.getKey() );

                //if( testUtils == null )
                //    testUtils = (TestUtils) getManagedBean("testUtils", this.getFacesContext(request, response));

                Locale loc = request.getLocale();

                if( loc==null )
                {
                    //if( testUtils == null )
                    //    testUtils = (TestUtils) getManagedBean("testUtils", this.getFacesContext(request, response));

                    loc = refUtils.getLocale();

                    if( loc==null )
                        loc = Locale.US;
                }

                String msg = MessageFactory.getStringMessage( loc, ste.getKey(), ste.getParams() );
                errParams = new String[] { request.getRequestURI(), msg };
            }

            else
            {
                if( e.getMessage()!=null && e.getMessage().startsWith( "URLFORMAT" ) )
                    LogService.logIt( "RefEntryServlet.processRequest() URLFORMAT Error. AAA.1 url=" + request.getRequestURI() + ", query str=" + request.getQueryString() + ", error=" + e.toString() );
                else
                    LogService.logIt( e, "RefEntryServlet.processRequest() AAA.2 url=" + request.getRequestURI() + ", query str=" + request.getQueryString() + ", error=" + e.toString()  );
                errParams = new String[] { request.getRequestURI(), e.toString() };
            }

            try
            {

                String errKey = "g.UrlFormatErrorProcessingLink";

                nextViewId = refUtils.systemError(null, null, null, errKey, errParams, null, null, true );

                String forwardUrl = correctUrlForForward( nextViewId );
               //  LogService.logIt( "RefEntryServlet.processRequest() Post Exception, Sending to Error Page url=" + request.getRequestURI() + ", nextViewId=" + nextViewId + ", FORWARDING to=" + forwardUrl );

                try
                {
                    forward(  forwardUrl,  request,  response );
                }
                catch( Exception ey )
                {
                    LogService.logIt( ey, "RefEntryServlet.processRequest() XXX.1 Exception forwarding to Error Page url=" + request.getRequestURI() + ", nextViewId=" + nextViewId + ", Redirecting"  );

                    try
                    {
                        response.sendRedirect( "/tr" + nextViewId );
                    }
                    catch( IOException | IllegalStateException ez )
                    {
                        LogService.logIt( ez, "RefEntryServlet.processRequest() XXX.1a Trying redirect after error going to forwardUrl. " + ez.toString() + "  url=" + request.getRequestURI() + ", nextViewId=" + nextViewId + ", Redirecting"  );
                    }
                }
            }
            catch( Exception ex )
            {
                LogService.logIt( ex, "RefEntryServlet.processRequest() XXX.2 url=" + request.getRequestURI() );

                try
                {
                    response.sendRedirect( "/tr/index.xhtml" );
                }
                catch( Exception ez )
                {
                LogService.logIt( ez, "RefEntryServlet.processRequest() XXX.2a redirecting to index.xhtml. " + ez.toString() + "  url=" + request.getRequestURI() );

                }
            }
        }

    }


    private String getAccessCodeFromUrl( String url )
    {
        if( url==null  )
            return null;

        String u=url.trim();

        if( u.indexOf("?")>0 )
            u = u.substring(0, u.indexOf("?") );

        if( u.endsWith("/") )
            u = u.substring(0, u.length()-1 );

        if( u.isBlank() || u.indexOf("/")<0 || (u.indexOf("XC")<0 && u.indexOf("XR")<0) )
            return null;

       String c = u.substring( u.lastIndexOf("/")+1, u.length() );

       // LogService.logIt( "RefEntryServlet.getAccessCodeFromUrl( url=" + url + " ) code=" + c );

       return c;

    }

    protected String correctUrlForForward( String nextViewId )
    {
        if( nextViewId==null )
            nextViewId = "";

        if( nextViewId.toLowerCase().startsWith( "http://" ) )
        {
            int index = nextViewId.indexOf( "/tr/" );

            return nextViewId.substring(index + 3, nextViewId.length() );
        }

        if( nextViewId.startsWith("/tr/") )
            nextViewId = nextViewId.substring( 3, nextViewId.length() );

        else if( !nextViewId.startsWith("/" ) )
            nextViewId = "/" +  nextViewId; // .substring( 1, nextViewId.length() );

        return nextViewId;
    }

    protected void forward( String nextViewId, HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        String forwardUrl = correctUrlForForward( nextViewId );
        // LogService.logIt( "BaseTestEntryServlet.forward() url=" + request.getRequestURI() + ", nextViewId=" + nextViewId + ", FORWARDING to=" + forwardUrl );
        RequestDispatcher dispatcher = request.getRequestDispatcher(forwardUrl);
        dispatcher.forward(request,response);
        return;
    }





}
