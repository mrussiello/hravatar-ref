package com.tm2ref.skin;


import com.tm2ref.faces.FacesBean;
import com.tm2ref.faces.FacesUtils;
import java.io.Serializable;
import java.util.Date;


import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;

@Named
@SessionScoped
public class SkinInfoBean extends FacesBean implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static Skin defaultSkin = new Skin();
    // private static List<Locale> localeList = null;

    private String htmlLangCode;

    public SkinInfoBean()
    {
    }

    private synchronized void init()
    {
        if( defaultSkin==null )
            defaultSkin = new Skin();
    }


    public static SkinInfoBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        return (SkinInfoBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "skinInfoBean" );
    }

    public String getUniqueStr()
    {
        return Long.toString( new Date().getTime() );
    }

    public String getTextDirection()
    {
        return getIsRTL() ? "RTL" : "LTR";
    }

    public boolean getIsRTL()
    {
        init();

        getHtmlLangCode();

        return ( htmlLangCode.startsWith( "ar" ) || htmlLangCode.startsWith( "fa" ) || htmlLangCode.startsWith( "ur" ) || htmlLangCode.startsWith( "ps" ) || htmlLangCode.startsWith( "syr" ) || htmlLangCode.startsWith( "dv" )
                || htmlLangCode.startsWith( "he" ) || htmlLangCode.startsWith( "yi" ) ) ? true : false;
    }

    public synchronized String getHtmlLangCode()
    {
        init();
        if( htmlLangCode == null )
        {
            FacesUtils facesUtils = FacesUtils.getInstance();
            htmlLangCode = facesUtils.getLocale().toString().replace( '_', '-' ); // .
        }
        return htmlLangCode;
    }


    public Skin getDefaultSkin()
    {
        init();

        if( defaultSkin == null )
            defaultSkin=new Skin();

        return defaultSkin;
    }

    public String getBaseDirectory()
    {
        getDefaultSkin();
        return defaultSkin.getBaseDirectory();
    }

    public String getTitle()
    {
        getDefaultSkin();
        return defaultSkin.getTitle();
    }

    public String getCoreTemplate()
    {
        getDefaultSkin();
        return "/skin/" + defaultSkin.getBaseDirectory() + "/" + defaultSkin.getTemplate();
    }
    
    public String getHomePageHref()
    {
       return "index.xhtml";
    }
}
