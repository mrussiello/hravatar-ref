package com.tm2ref.corp;



import com.tm2ref.entity.corp.Corp;
import com.tm2ref.faces.FacesUtils;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.ref.RefBean;
import com.tm2ref.ref.RefUtils;
import com.tm2ref.util.CookieUtils;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserFacade;
import jakarta.faces.context.FacesContext;


import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Named
@RequestScoped
public class CorpUtils extends FacesUtils
{
    private static Integer defaultCorpId = 0;

    private CorpBean corpBean;

    private CorpFacade corpFacade;

    private UserFacade userFacade;


    public static CorpUtils getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        try
        {
        return (CorpUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "corpUtils" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CorpUtils.getInstance() " );
            return null;
        }
    }

    private static synchronized void init()
    {
        if( defaultCorpId <= 0 )
        {
            defaultCorpId = RuntimeConstants.getIntValue( "defaultcorpid" );
        }
    }


    protected CorpBean getCorpBean()
    {
    	if( corpBean == null )
    		corpBean = CorpBean.getInstance();

        return corpBean;
    }


    protected int getCorpIdFmCookie(HttpServletRequest req)
    {
        try
        {
            Cookie c = CookieUtils.getCookie( req !=null ? req : getHttpServletRequest() , CookieUtils.CORPID_COOKIE_NAME );

            if( c != null && c.getValue() != null && !c.getValue().isEmpty() )
                return Integer.parseInt(  EncryptUtils.urlSafeDecrypt( c.getValue() ) );
        }

    	catch( Exception e )
    	{
            LogService.logIt( e, "CorpUtils.getCorpIdFmCookie() " );
    	}

        return 0;
    }




    public void initFmServlet( CorpBean cb )
    {
        corpBean = cb;
    }

    
    public void loadCorpIfNeeded( int corpId, boolean clear, HttpServletResponse response) throws Exception
    {
        try
        {
            init();

            getCorpBean();
            
            getUserBean();

            Corp c = null;

            HttpServletRequest req = getHttpServletRequest();

            // check cookie
            if( corpId <= 0 && req != null )
                corpId = getCorpIdFmCookie( req );

           // want default but not default loaded.
           if( corpId <= 0 &&
               corpBean.getHasCorp() &&
               corpBean.getCorp().getCorpId() != defaultCorpId )
           {
               corpBean.setCorp(null);
               clear = true;
           }

           // want non-default but no match.
           else if( corpId > 0 && corpBean.getHasCorp() && corpBean.getCorp().getCorpId() != corpId )
           {
               corpBean.setCorp(null);
               clear = true;
           }

           // indicates load the default if no corp present.
           if( corpId <= 0 && !corpBean.getHasCorp() )
           {
               // LogService.logIt( "CorpUtils.loadCorpIfNeeded() corpId=" + corpId + " loading default corp." );
               // corpId = defaultCorpId;
               corpBean.loadDefaultCorp();
           }

           // LogService.logIt( "CorpUtils.loadCorpIfNeeded() 222 corpId=" + corpId + " hasCorp=" + corpBean.getHasCorp() );


           // no corp - need to load it.
           if( !corpBean.getHasCorp() )
           {
               if( corpFacade == null ) 
                   corpFacade = CorpFacade.getInstance();

               if( c == null || c.getCorpId() != corpId )
                   c = corpFacade.getCorp( corpId, true );

               if( c == null )
                   throw new Exception( "Corp not found for corpId=" + corpId );

               // dON'T USE IT IF IT'S NOT A REFERENCE CHECK CORP
               if( !c.getCorpType().getIsReferenceCheck() )
               {
                   corpBean.loadDefaultCorp();
               }
               
               // clone so can store stuff without overwriting others.
               else
                   corpBean.setCorp( c.getIsDefault() ? c : (Corp) c.clone() );
           }


           if( corpBean.getCorp().getCorpId()==defaultCorpId )
               corpBean.getCorp().setIsDefault( true );

           // Force Corp Locale
           if( corpBean.getCorp().getLocaleStr()!=null && !corpBean.getCorp().getLocaleStr().isEmpty() )
           {
               getUserBean();

               if( userBean != null )
                   userBean.setLocale( I18nUtils.getLocaleFromCompositeStr( corpBean.getCorp().getLocaleStr() ) );

               forceLocale( I18nUtils.getLocaleFromCompositeStr( corpBean.getCorp().getLocaleStr() ) );
           }           
        }

        catch( STException e )
        {
            throw e;
        }

    	catch( Exception e )
    	{
            LogService.logIt( e, "CorpUtils.loadCorpIfNeeded() " );
            throw new STException( e );
    	}
    }

    
    public String processViewCameraHelp()
    {
        return viewHelp( 2 );
    }

    
    public String processViewHelp()
    {
        return viewHelp( 1 );
    }
    
    /**
     * pageCode = 1 = help = default
     *            2 = camera help
     * 
     * @param pageCode
     * @return 
     */
    private String viewHelp( int pageCode )
    {
        try
        {
            getCorpBean();

            if( !corpBean.getHasCorp() )
                loadCorpIfNeeded( 0, true, null );

            if( !getNewRefStartsOk() )
                return corpBean.getCorp().getOfflinePage();

            if( pageCode==2 )
                return "/pp/help-camera.xhtml";

            corpBean.setDirectCameraHelp(false);
            
            // LogService.logIt( "CorpUtils.processCorpHome() BBB " + "/" + corpBean.getDirectory() + corpBean.getCorp().getHomePage() );
            return "/" + corpBean.getDirectory() + "/help.xhtml";
        }
        catch( STException e )
        {
            setMessage( e );
            return null;
        }
    	catch( Exception e )
    	{
            LogService.logIt( e, "CorpUtils.processViewHelp() " );
            setMessage( e );
            return "failure";
    	}        
    }
    
    
    public String processHelpExit()
    {
        try
        {
            getCorpBean();

            if( !corpBean.getHasCorp() )
                loadCorpIfNeeded( 0, true, null );

            // RefUtils refUtils = RefUtils.getInstance();
            RefBean refBean = RefBean.getInstance();
            RefUtils refUtils = RefUtils.getInstance();
            
            
            if( refBean.getRcCheck()!=null && refBean.getRefPageType()!=null )
                return refUtils.getViewFromPageType( refBean.getRefPageType() );
                // return refBean.getRefPageType().getPageFull(refBean.getRefUserType());
            
            return this.processCorpHome();
        }
        catch( STException e )
        {
            setMessage( e );
            return null;
        }
    	catch( Exception e )
    	{
            LogService.logIt( e, "CorpUtils.processHelpExit() " );
            setMessage( e );
            return "failure";
    	}                
    }
    


    public String processCorpHome()
    {
        try
        {
            getCorpBean();

            if( !corpBean.getHasCorp() )
                loadCorpIfNeeded( 0, true, null );

            if( !getNewRefStartsOk() )
                return corpBean.getCorp().getOfflinePage();

            // LogService.logIt( "CorpUtils.processCorpHome() BBB " + "/" + corpBean.getDirectory() + corpBean.getCorp().getHomePage() );
            return "/" + corpBean.getDirectory() + corpBean.getCorp().getHomePage();
        }

        catch( STException e )
        {
            setMessage( e );
            return null;
        }

    	catch( Exception e )
    	{
            LogService.logIt( e, "CorpUtils.processCorpHome() " );
            setMessage( e );
            return "failure";
    	}
    }


    public void setCorpBean(CorpBean corpBean) {
        this.corpBean = corpBean;
    }



}
