package com.tm2ref.ref;


import com.tm2ref.corp.CorpBean;
import com.tm2ref.corp.CorpUtils;
import com.tm2ref.faces.HttpReqUtils;
import com.tm2ref.proctor.ProctorBean;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserBean;
import jakarta.inject.Inject;
import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(filterName = "RefViewFilter", urlPatterns={"/ref/*"} )
public class RefViewFilter implements Filter
{
    
    @Inject
    RefBean refBean;

    @Inject
    RefUtils refUtils;
    
    @Inject
    CorpBean corpBean;
    
    @Inject
    CorpUtils corpUtils;

    @Inject
    UserBean userBean;
    
    @Inject
    ProctorBean  proctorBean;
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
    {
        // LogService.logIt( "RefViewFilter.doFilter() START testBean.viewAdmin=" + (testBean.getViewAdmin()==null ? "null" : testBean.getViewAdmin().toString()) );

        if( refBean.getRcCheck()==null )
        {
            
            HttpServletRequest hreq = (HttpServletRequest) req;
            
            String p = hreq.getRequestURI();
            if( p.contains("complete.xhtml") || p.contains("complete-r.xhtml") ||
                p.contains("cancelled.xhtml") || p.contains("cancelled-r.xhtml") || 
                p.contains("error-fatal.xhtml")|| 
                p.contains("expired.xhtml") || p.contains("expired-r.xhtml") || 
                p.contains("index.xhtml") || 
                p.contains("initerror.xhtml") || 
                p.contains("offline.xhtml")|| 
                p.contains("thank-you.xhtml") )
            {
                chain.doFilter(req, resp);        
                return;                
            }
            
            String acidx = HttpReqUtils.getStringReqParam("acidx", hreq);
            String refpagex = HttpReqUtils.getStringReqParam("refpagex", hreq);
            int refPageTypeId = 0;
            LogService.logIt( "RefViewFilter.doFilter() refBean.rcCheck is null. checking for recoverability. aicdx=" + acidx + ", refpagex=" + refpagex );
            if( refpagex!=null && !refpagex.isBlank() )
            {
                try
                {
                    refPageTypeId = Integer.parseInt(refpagex);
                }
                catch( NumberFormatException e)
                {
                    LogService.logIt( "RefViewFilter.doFilter() Unable to parse refpagex=" + refpagex );
                    refPageTypeId=0;
                }
            }
            
            if( refPageTypeId>=100 )
            {
                LogService.logIt( "RefViewFilter.doFilter() refPageTypeId is complete or higher, so calling Chain.doFilter() refPageTypeId=" + refPageTypeId );                                
                chain.doFilter(req, resp);        
                return;
            }

            refUtils.setProctorBean(proctorBean);
            refUtils.setRefBean(refBean);
            refUtils.setCorpBean(corpBean);
            refUtils.setCorpUtils(corpUtils);
            refUtils.setUserBean(userBean);
            refUtils.setHttpServletRequest(hreq);
            refUtils.setHttpServletResponse( (HttpServletResponse) resp);

            corpUtils.setCorpBean(corpBean);
            corpUtils.setUserBean(userBean);
            corpUtils.setHttpServletRequest( hreq );
            
            // cannot recover
            if( !requestHasParamsForRecovery( (HttpServletRequest) req ) )
            {
                LogService.logIt( "RefViewFilter.doFilter() Reference Check not appear to be recoverable so going fatal. acidx=" + (acidx==null ? "null" : acidx) );
                String nextViewId = refUtils.systemError(null, null, "Session Error no data and no info for recovery.", null, null, null, null, true);
                redirectToView( (HttpServletResponse)resp, nextViewId );
                return;
            }
            
            // try to recover if GET. If not GET, the system will recover itself.
            else if( ((HttpServletRequest) req).getMethod().equalsIgnoreCase("GET") ) //  || ((HttpServletRequest) req).getMethod().equalsIgnoreCase("POST") )
            {
                LogService.logIt( "RefViewFilter.doFilter() recoverable GET received. Recovering Session. acidx=" + acidx );
                
                try
                {                    
                    String nextViewId = refUtils.checkRepairSession(500, true);
                    LogService.logIt( "RefViewFilter.doFilter() recoverable GET received. Recovering Session. After checkRepair() nextViewId=" + nextViewId );
                    
                    if( nextViewId==null || nextViewId.isBlank())
                    {
                        LogService.logIt( "RefViewFilter.doFilter() Recovering session. Unable to recover. Letting the request flow through. acidx=" + (acidx==null ? "null" : acidx) + ", nextViewId=" + (nextViewId==null ? "null" : nextViewId) );                                
                        // nextViewId = refUtils.systemError(null, null, "Error during session recovery. Unable to recover. acidx=" + acidx, null, null, null, null, true);
                    }
                    
                    else
                    {
                        LogService.logIt( "RefViewFilter.doFilter() Recovering session. After calling TestUtils.processReturnToTestingProcess. acidx=" + acidx + ", nextViewId=" + nextViewId );                                

                        // only advance if GET. Otherwise let the post flow through.
                        //if( ((HttpServletRequest) req).getMethod().equalsIgnoreCase("GET") )
                        //{
                        redirectToView( (HttpServletResponse)resp, nextViewId );
                        return;
                    }
                    //}                    
                }
                catch( Exception e )
                {
                    LogService.logIt( e, "RefViewFilter.doFilter() Error Recovering Session for acidx=" + acidx );
                    String nextViewId = refUtils.systemError(null, null, "Exception caught during session recovery. " + e.toString() + ", acidx=" + acidx, null, null, null, null, true);
                    redirectToView( (HttpServletResponse)resp, nextViewId );
                    return;
                }
            }
            else
            {
                LogService.logIt( "RefViewFilter.doFilter() Request appears to be recoverable but it's not a GET request. Letting the FacesServlet (and RefExceptionHandler) handle session recovery. acidx=" + (acidx==null ? "null" : acidx) );
            }
                
        }
        
        // LogService.logIt( "RefViewFilter.doFilter() Calling Chain.doFilter() " );                                
        chain.doFilter(req, resp);        
    }

    private void redirectToView( HttpServletResponse resp, String nextViewId )
    {
        try
        {
            resp.sendRedirect( "/tr" + nextViewId );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RefViewFilter.redirectToView() Unable to redirect to systemError. " + nextViewId );
        }        
    }
    
    private boolean requestHasParamsForRecovery( HttpServletRequest req )
    {
        if( refBean.getAdminOverride())
            return false;
        
        else if( HttpReqUtils.getStringReqParam("acidx", req)!=null )
            return true;

        return false;
    }
}

