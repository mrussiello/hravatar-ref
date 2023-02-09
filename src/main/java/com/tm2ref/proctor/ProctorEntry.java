/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.proctor;

import com.tm2ref.corp.CorpBean;
import com.tm2ref.corp.CorpUtils;
import com.tm2ref.ref.RefBean;
import com.tm2ref.ref.RefUtils;
import com.tm2ref.service.LogService;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Dad
 */
@Named
@RequestScoped
public class ProctorEntry
{
    int getusermedia = 0;
    boolean medrecapi=false;
    int recdevs = -1;
    String acidx;

    @Inject
    ProctorBean proctorBean;

    @Inject
    RefBean refBean;


     protected void init()
     {
     }


    public void doUploadSuccessEntry()
    {
        try
        {
           String nextViewId = ProctorUtils.getInstance().doPhotoUpload();
           if( nextViewId != null )
               navigateTo( nextViewId );

        }
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorEntry.doUploadSuccessEntry() " );
        }
    }

    public void doCameraHelpEntry()
    {
       try
       {
           String nextViewId;

           CorpUtils cu = CorpUtils.getInstance();

           CorpBean cb = CorpBean.getInstance();

           // LogService.logIt( "TestEntry.doSurveyExitEntry() testBean.key=" + (tb.getTestKey()==null ? "null" : "true " + tb.getTestKey().getTestKeyId() ) + ", tkidx=" + tkidx  + ", session=" + ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true) ).getId() ) ;

           nextViewId = cu.processViewCameraHelp();
           cb.setDirectCameraHelp(true);

           if( nextViewId != null )
               navigateTo( nextViewId );
       }

       catch( Exception e )
       {
           LogService.logIt( e, "ProctorEntry.doCameraHelpEntry() " );
       }
    }


    public void doBrowserCheckEntry()
    {
       try
       {
           String nextViewId = "/index.xhtml";

           // if( 1==1 || recdevs<0 )
           //     LogService.logIt("ProctorEntry.doBrowserCheckEntry() acidx=" + acidx + ", medrecapi=" + medrecapi + ", recdevs=" + recdevs + ", getusermedia=" + getusermedia ) ;

           if( acidx!=null && !acidx.isBlank() )
           {
               refBean.setRecDevs(recdevs);

               // gotta do this!
               if( refBean.getRecDevs() < 0 )
                   refBean.setRecDevs( 0 );

               refBean.setMedRecApi(medrecapi);
               refBean.setHasGetUserMedia( getusermedia );

               RefUtils refUtils = RefUtils.getInstance();

               nextViewId = refUtils.processMediaRecEntry();

               if( nextViewId == null )
                   nextViewId = refUtils.getNextViewForCorp();
           }

           if( nextViewId == null || nextViewId.isBlank() )
               nextViewId = "/index.xhtml";

           // LogService.logIt( "ProctorEntry.doBrowserCheckEntry() nextViewId=" + nextViewId );

           //if( nextViewId != null )
           navigateTo( nextViewId );
       }

       catch( Exception e )
       {
           LogService.logIt(e, "ProctorEntry.doBrowserCheckEntry() acidx=" + acidx + ", medrecapi=" + medrecapi + ", recdevs=" + recdevs);

           try
           {
           navigateTo( "/index.xhtml" );
           }
           catch ( Exception ee )
           {
               LogService.logIt( ee, "ProctorEntry.doBrowserCheckEntry() CAN't Navigate! to index.xhtml" );
           }
       }
    }


   public void navigateTo( String viewId ) throws Exception
   {
       if( viewId.indexOf( "http" )==0  )
           sendRedirect( viewId );

       else
       {
           ConfigurableNavigationHandler nav  = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
           nav.performNavigation( viewId );
       }
   }


    public void sendRedirect( String viewId ) throws Exception
    {
        String stub = "";

        try
        {
            HttpServletRequest req =  (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

            if( viewId.toLowerCase().indexOf( "http" )<0 )
            {
                String ra = req.getRequestURL().toString();

                //LogService.logIt( "ProctorEntry.sendRedirect() req.getRequestURL()=" + ra );
                int idx = ra.indexOf( "/" , ra.indexOf( "://" )+4 );

                stub = idx>0 ? ra.substring(0, idx ) : ra;
                // stub = req.getScheme() + "://" + req.getLocalAddr();
            }

            //else
            //    LogService.logIt( "ProctorEntry.sendRedirect() req.getRequestURL()=" + req.getRequestURL().toString() );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "ProctorEntry.sendRedirect() Redirecting to " + viewId );
            stub="";
        }


        LogService.logIt( "ProctorEntry.sendRedirect() Redirecting to stub=" + stub + ", viewId=" + viewId );
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.sendRedirect(stub + viewId);
    }


    public boolean isMedrecapi() {
        return medrecapi;
    }

    public void setMedrecapi(boolean medrecapi) {
        this.medrecapi = medrecapi;
    }

    public int getRecdevs() {
        return recdevs;
    }

    public void setRecdevs(int recdevs) {
        this.recdevs = recdevs;
    }

    public String getAcidx() {
        return acidx;
    }

    public void setAcidx(String acidx) {
        this.acidx = acidx;
    }



    public int getGetusermedia() {
        return getusermedia;
    }

    public void setGetusermedia(int getusermedia) {
        this.getusermedia = getusermedia;
    }




}