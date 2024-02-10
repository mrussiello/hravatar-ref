/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.corp;

import com.tm2ref.entity.corp.Corp;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.ref.RefBean;
import com.tm2ref.service.LogService;
import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Named;

/**
 *
 * @author Mike
 */
@Named
@SessionScoped
public class JsfCorpBean implements Serializable {

    static String[] NO_RCCHECK_VIEWS = new String[] { "cancelled.xhtml", "cancelled-r.xhtml", "index.xhtml", "offline.xhtml", "help.xhtml", "initerror.xhtml", "complete.xhtml", "complete-r.xhtml", "error-fatal.xhtml", "exit-temp.xhtml", "thank-you.xhtml"};

    public void sessionMissingCheckEntry( ComponentSystemEvent event )
    {
        try
        {
            FacesContext fc = FacesContext.getCurrentInstance();

            String viewId = fc.getViewRoot().getViewId();

            LogService.logIt( "JsfCorpBean.sessionMissingCheckEntry() Testing for corp. viewId=" + viewId );

            //if( 1==1 )
            //    return;
            
            CorpBean cb = CorpBean.getInstance();

            Corp corp = cb.getCorp();

            if( corp == null )
            {
                LogService.logIt( "JsfCorpBean.sessionMissingCheckEntry() AAA Apparent Session timeout. Reloading Corp from cookie or URL." );
                cb.sessionMissingCheckEntry(true);
            }

            else if( viewShouldHaveRcCheck( viewId ) )
            {
                // LogService.logIt( "JsfCorpBean.sessionMissingCheckEntry() Testing for TestKey. viewId=" + viewId );
                RefBean rb = RefBean.getInstance();
                RcCheck rc = rb.getRcCheck();

                if( rc==null )
                {
                    LogService.logIt( "JsfCorpBean.sessionMissingCheckEntryInTest() BBB Apparent Session timeout. Reloading from cookie or URL." );

                    // CorpBean cb = CorpBean.getInstance();

                    cb.sessionMissingCheckEntry(true);
                }

            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JsfCorpBean.sessionMissingCheckEntry() " );
        }
    }


    private boolean viewShouldHaveRcCheck( String viewId )
    {
        if( viewId == null || viewId.isEmpty() )
            return false;

        for( String s : NO_RCCHECK_VIEWS )
        {
            if( viewId.contains( s ) )
                return false;
        }        
        return true;
    }

}
