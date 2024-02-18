/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.faces;

import com.tm2ref.corp.CorpBean;
import com.tm2ref.ref.RefBean;
import com.tm2ref.ref.RefUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import java.util.Iterator;
import java.util.Map;
import jakarta.faces.FacesException;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author miker_000
 */
public class RefExceptionHandler extends ExceptionHandlerWrapper {
    
    private final ExceptionHandler exceptionHandler;

    public RefExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return exceptionHandler;
    }

    @Override
    public void handle() throws FacesException {
        
        final Iterator<ExceptionQueuedEvent> queue = getUnhandledExceptionQueuedEvents().iterator();

        while (queue.hasNext()){
            ExceptionQueuedEvent item = queue.next();
            ExceptionQueuedEventContext errCtxt = (ExceptionQueuedEventContext)item.getSource();

            String nextViewId = null;  
            
            try 
            {
                Throwable t = errCtxt.getException();

                FacesContext fc = FacesContext.getCurrentInstance();

                if( fc==null )
                    LogService.logIt("RefExceptionHandler.handle() FacesContext is null. AAA.1 Exception: " + t.toString() + ", " + t.getMessage() );
                
                if( fc==null  )
                    throw new Exception( "FacesContext was null" );

                String viewId = fc.getViewRoot()!=null ? fc.getViewRoot().getViewId() : "ViewRoot unavailable (FacesContext or FacesContext.viewRoot is null) fc=" + (fc==null ? "null" : "not null");
                
                LogService.logIt("RefExceptionHandler.handle() AAA.2 viewId=" + viewId + ", Exception: " + t.toString() + ", " + t.getMessage() );
                
                RefBean rb = (RefBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "refBean" );
                
                Map<String, Object> rm = fc.getExternalContext().getRequestMap();
                NavigationHandler nav = fc.getApplication().getNavigationHandler();

                //rm.put("error-message", t.getMessage());
                //rm.put("error-stack", t.getStackTrace());
                
                HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();                        
                
                if( viewId==null || !viewId.contains("/tools/admin/") )                
                    LogService.logIt("RefExceptionHandler.handle() viewId=" + viewId + ", Exception: " + t.toString() + ", " + t.getMessage() );
                
                // Recover from errors we can recover from.
                // if( t instanceof IllegalStateException || t instanceof ELException || t instanceof FacesException || t instanceof ViewExpiredException || t instanceof ProtectedViewException || t instanceof IllegalStateException )
                
                if( viewId!=null && viewId.contains("/tools/admin/" ) )
                {
                    nextViewId = "/tools/admin/admintools.xhtml";
                }
                
                else
                {
                    String acidx = rb!=null ? rb.getActiveAccessCodeX(): null;
                    
                    LogService.logIt( "RefExceptionHandler.handle() DDD.1 Attempting to recover from error: " + t.toString() + ", refBean.acidx=" + acidx );
                    
                    if( acidx==null || acidx.isBlank() )
                        acidx = HttpReqUtils.getStringReqParam("acidx", req);
                    
                    if( acidx!=null && !acidx.isBlank() && rb!=null )
                        rb.setActiveAccessCodeX(acidx);

                    String refpagex = rb!=null ? rb.getActiveRefPageTypeIdX() : null;
                    
                    LogService.logIt( "RefExceptionHandler.handle() DDD.2 Attempting to recover from error: " + t.toString() + ", acidx=" + acidx + ", refBean.refpagex=" + refpagex );
                    
                    if( refpagex==null || refpagex.isBlank() )
                        refpagex = HttpReqUtils.getStringReqParam("refpagex", req);
                    
                    if( refpagex!=null && !refpagex.isBlank() && rb!=null )
                    {
                        try
                        {
                            int rbx = Integer.parseInt(refpagex);
                        }
                        catch( NumberFormatException e )
                        {
                            LogService.logIt( "RefExceptionHandler.handle() DDD.2b Unable to parse refpagex from request or refBean, so erasing and ignoring. refpagex=" + refpagex + ", " + t.toString() + ", acidx=" + acidx + ", refBean.refpagex=" + (rb==null ? "null" : rb.getActiveRefPageTypeIdX()) );
                            refpagex = "";
                        }
                        
                        rb.setActiveRefPageTypeIdX(refpagex);
                    }
                    
                    if( refpagex==null )
                        refpagex="";

                    LogService.logIt( "RefExceptionHandler.handle() DDD.3 Attempting to recover from error: " + t.toString() + ", acidx=" + acidx + ", refpagex=" + refpagex );
                    
                    // boolean preview = (rb!=null && rb.getAdminOverride());
                                        
                    if( acidx!=null && !acidx.isBlank() )
                    {
                        try
                        {
                            rm.put("acidx", acidx );
                            rm.put("refpagex", refpagex );
                            if( rb!=null )
                            {
                                rb.setActiveAccessCodeX(acidx);
                                rb.setActiveRefPageTypeIdX(refpagex);
                            }
                            
                            RefUtils tu = (RefUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "refUtils" );
                            
                            if( tu!=null )
                            {
                                nextViewId = tu.checkRepairSession(502, true);
                                
                                // nextViewId = tu.performLoadForwardEntry();
                                LogService.logIt("RefExceptionHandler.handle() EEE.1 Recovering session. After calling TestUtils.checkRepairSession. acidx=" + acidx + ", refpagex=" + refpagex + ", nextViewId=" + nextViewId );
                            }
                        }
                        catch( Exception e )
                        {
                            LogService.logIt( e, "RefExceptionHandler.handle() FFF.2 Unexpected exception recovering from a ViewExpired or ProtectedView exception. acidx=" + acidx );
                        }
                    }
                }

                if( nextViewId==null || nextViewId.isBlank() )
                {
                    nextViewId = "/ref/index.xhtml";
                    
                    if( rb!=null )
                    {
                        rb.setErrorMessage(t.getMessage() );
                        rm.put("acidx", rb.getActiveAccessCodeX());
                        rm.put("refpagex", rb.getActiveRefPageTypeIdX());
                    }                    
                }
                

                // CorpBean cb = (CorpBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "corpBean" );
                
                LogService.logIt( "RefExceptionHandler.handle() GGG.1 Navigating to nextViewId=" + nextViewId );
                nav.handleNavigation(fc, null, nextViewId);
                fc.renderResponse();
            } 
            catch( Exception e )
            {
                LogService.logIt(e, "RefExceptionHandler.handle() XXx.1 Unexpected exception handling errors. nextViewId=" + nextViewId );                
            }
            finally {
                queue.remove();
                Tracker.addFacesError();
            }
            
            if( getWrapped()!=null )
                getWrapped().handle();
        }
    }    
}
