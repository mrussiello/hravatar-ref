package com.tm2ref.corp;


import com.tm2ref.entity.corp.Corp;
import com.tm2ref.faces.FacesBean;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.ref.RefBean;
import com.tm2ref.ref.RefUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.util.MessageFactory;
import java.io.Serializable;

import java.util.Locale;

import jakarta.faces.context.FacesContext;

import jakarta.inject.Named; 
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.ConfigurableNavigationHandler;

@Named
@SessionScoped
public class CorpBean extends FacesBean implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static Corp defaultCorp = null;

    private Corp corp;

    private String userAgent = null;
    
    private boolean directCameraHelp;

    public static CorpBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        return (CorpBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "corpBean" );
    }

    public void clearBean()
    {
        defaultCorp = null;
        corp = null;
    }

    @Override
    public String toString() {
        return "CorpBean{" + "corp=" + ( corp ==null ? "null" : corp.toString()  ) + '}';
    }

    
    public String getCompleteText( Locale locale )
    {
        if( corp != null && corp.getCompleteCustomText()!=null && !corp.getCompleteCustomText().trim().isEmpty() )
            return corp.getCompleteCustomText().trim();
        
        return MessageFactory.getStringMessage(locale, "g.RefComplete", null );
    }

    
    public String getContactUsUrl() 
    {
        // UserBean userBean = UserBean.getInstance();
        String out = RuntimeConstants.getStringValue( "contactFormUrl" );        
        out += "?t=21";
        
        RefBean rb = RefBean.getInstance();
        if( rb!=null && rb.getRcCheck()!=null )
        {
            out += "&rcid=" + rb.getRcCheck().getRcCheckIdEncrypted();
            
            if( rb.getRefUserType()!=null && rb.getRefUserType().getIsRater() && rb.getRcCheck().getRcRater()!=null )
                out += "&uid=" + rb.getRcCheck().getRcRater().getUserIdEncrypted();

            else if( rb.getRefUserType()!=null && rb.getRefUserType().getIsCandidate()  )
                out += "&uid=" + rb.getRcCheck().getUserIdEncrypted();
        }
        
        return out;
    }
    
        
    public String getCorpImageIconUrl()
    {
    	if( corp == null )
    		return "";

        return getMediaServerBaseUrl() + corp.getImageIconFilename();
        // return getMediaServerBaseUrl() + Constants.MEDIA_CORPIMAGEICON_DIRECTORY + "/" + corp.getCorpIdEncryptedFileSafe() + "/" + corp.getImageIconFilename();
    }


    public Locale getLocale()
    {
        if( corp != null )
            return I18nUtils.getLocaleFromCompositeStr( corp.getLocaleStr() );

        return Locale.US;
    }


    public Corp getCorp() {

        return corp;
    }


    public void setCorp(Corp corp) {
        this.corp = corp;
    }


    public boolean getHasCorp()
    {
        return corp != null && corp.getCorpId()>0;
    }

    public String getTemplate()
    {
        // LogService.logIt( "CorpBean.getTemplate() Starting. hasCorp=" + getHasCorp() );

        if( !getHasCorp() )
            loadDefaultCorp();
        
        // Always use MobileTemplate if present
        if( corp.getHasMobileTemplate() )
            return "/custom/" + corp.getMobileTemplate();            

        // use template if present and no mobile template.
        //if( corp.getHasTemplate() )
        //    return "/custom/" + corp.getTemplate();            
        
        return "/ref/template.xhtml";
    }    
    

    public String getDirectory()
    {
        if( !getHasCorp() )
            loadDefaultCorp();

        return "ref";        
    }
    
 
    public void sessionMissingCheckEntry(boolean navigate)
    {
        try
        {
            // LogService.logIt( "CorpBean.sessionMissingCheckEntry() Testing for corp . " + (corp != null) );


            //if( corp == null )
            //{
            LogService.logIt( "CorpBean.sessionMissingCheckEntry() Apparent Session timeout. Reloading Corp from cookie or URL." );

            CorpUtils.getInstance().loadCorpIfNeeded( 0, true, null );

            if( corp != null )
            {
                String viewId = "/" + getDirectory() + corp.getHomePage();

                RefBean tb = RefBean.getInstance();
                RefUtils tu = RefUtils.getInstance();

                if( tb.getRcCheck() == null )
                {
                    LogService.logIt( "CorpBean.sessionMissingCheckEntry() Repairing RcCheck." );

                    // LogService.logIt( "CorpBean.sessionMissingCheckEntry() TestBean.TestKey was null.  INFO DUMP: " + (new HttpInfoDumpUtils() ).getRequestInfo( getHttpServletRequest() ) );
                    tu.repairRefBeanForCurrentAction(tb, true, 300 );
                }
                
                    if( tb.getRcCheck() != null )
                        viewId = tu.getNextViewForCorp();

                    if( navigate )
                    {
                        ConfigurableNavigationHandler nav  = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
                        nav.performNavigation( viewId );
                    }
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CorpBean.sessionMissingCheckEntry() " );
        }
    }
    
    
    

    public synchronized void loadDefaultCorp()
    {
        LogService.logIt( "CorpBean.loadDefaultCorp() " + (defaultCorp==null ? "Default corp is null" : "default corp is not null " + defaultCorp.toString() ) );

        if( defaultCorp == null )
        {
            try
            {
                defaultCorp = CorpFacade.getInstance().getCorp( RuntimeConstants.getIntValue( "defaultcorpid" ), true );

                LogService.logIt( "CorpBean.loadDefaultCorp() Default corp loaded defaultcorpid=" + RuntimeConstants.getIntValue( "defaultcorpid" ) + ", null=" + (defaultCorp==null) );
            }

            catch( Exception e )
            {
                LogService.logIt( e, "CorpBean.loadDefaultCorp() " );

                defaultCorp = new Corp();

            }

            defaultCorp.setIsDefault( true );

            // LogService.logIt( "CorpBean.loadDefaultCorp() Default corp loaded defaultcorp=" + defaultCorp.toString() );
        }

        try
        {
            corp = (Corp) defaultCorp.clone();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CorpBean.loadDefaultCorp() Error cloning default corp " + defaultCorp.toString() );

            corp = new Corp();
        }

        // LogService.logIt( "CorpBean.loadDefaultCorp() corp loaded=" + corp.toString() );

        // return defaultCorp;
    }

    public static Corp getDefaultCorp() {
        return defaultCorp;
    }

    public static void setDefaultCorp(Corp defaultCorp) {
        CorpBean.defaultCorp = defaultCorp;
    }

    public boolean getDirectCameraHelp() {
        return directCameraHelp;
    }

    public void setDirectCameraHelp(boolean directCameraHelp) {
        this.directCameraHelp = directCameraHelp;
    }

}

