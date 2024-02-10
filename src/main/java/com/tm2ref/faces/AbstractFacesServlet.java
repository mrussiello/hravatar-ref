/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.faces;

import com.tm2ref.service.LogService;
import java.io.IOException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author miker_000
 */

public abstract class AbstractFacesServlet extends HttpServlet {

    public AbstractFacesServlet() {
        super();
    }
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }    
    protected abstract void processRequest(HttpServletRequest request,
        HttpServletResponse response);

    /** Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse
        response) throws ServletException, IOException {
        processRequest(request, response); 
    }
    protected void log(FacesContext facesContext, String message) {
        facesContext.getExternalContext().log(message);
    }
    /** Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */ 
    protected void doPost(HttpServletRequest request, HttpServletResponse
        response) throws ServletException, IOException {
        processRequest(request, response);
    }
    protected FacesContext getFacesContext(HttpServletRequest request,
        HttpServletResponse response) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {

            FacesContextFactory contextFactory  =
                (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            LifecycleFactory lifecycleFactory =
                (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY); 
            Lifecycle lifecycle =
                lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

            facesContext =
                contextFactory.getFacesContext(request.getSession().getServletContext(),
                    request, response, lifecycle);

            // Set using our inner class

            InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);

						LogService.logIt( "AbstractFacesServlet setting viewRoot to index.xhtml");
            // set a new viewRoot, otherwise context.getViewRoot returns null
            UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "/index.xhtml");
            facesContext.setViewRoot(view);        
        }
        return facesContext;
    }
    public void removeFacesContext() {
        InnerFacesContext.setFacesContextAsCurrentInstance(null);
    }
    protected Application getApplication(FacesContext facesContext) {
        return facesContext.getApplication();        
    }
    protected Object getManagedBean(String beanName, FacesContext
        facesContext) {     
        
        return getApplication(facesContext).getELResolver().getValue(facesContext.getELContext(), null, beanName);
        //return
        //    getApplication(facesContext).getVariableResolver().resolveVariable(facesContext,
        //        beanName);
    }
    // You need an inner class to be able to call FacesContext.setCurrentInstance
    // since it's a protected method
    private abstract static class InnerFacesContext extends FacesContext {
        protected static void setFacesContextAsCurrentInstance(FacesContext
            facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }     
}