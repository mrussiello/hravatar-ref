package com.tm2ref.faces;


import com.tm2ref.global.Constants;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import com.tm2ref.user.TimeZoneLister;
import com.tm2ref.user.UserBean;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.STHttpSessionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import java.util.TimeZone;
import java.util.TreeMap;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Named
@RequestScoped
public class FacesUtils
{
    // @ManagedProperty(value="#{userBean}")
    protected UserBean userBean;

    protected boolean startOverOk=false;
    protected boolean goBackOk=false;

    protected HttpServletRequest httpServletRequest;
    protected HttpServletResponse httpServletResponse;


    /**
     * This value is used strictly by the set language form and change methods.
     */
    // protected String localeStr;
    public static FacesUtils getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        return (FacesUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "facesUtils" );
    }



    public boolean getNewRefStartsOk()
    {
        return RuntimeConstants.getBooleanValue( "newRefStartsOK");
    }


    /**
     * We need to have an exact match with a Skin Locale so that the language change pull-down will work properly. So,
     * get locale, which may be set by browser and may not be an exact match and then find the closest match.
     *
     * @return
     */
    public synchronized Locale getLocale()
    {
        try
        {
            return getUserBean().getLocale();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FacesUtils.getLocale() " );

            return Locale.US;
        }
    }

    public String getLocaleStr()
    {
        return getLocale().toString();
    }

    public void forceLocale( Locale locale )
    {
        if( locale == null )
            return;

        getUserBean();

        if( userBean != null )
            userBean.setLocale(locale);

        if( FacesContext.getCurrentInstance()==null || FacesContext.getCurrentInstance().getViewRoot() == null )
            return;

        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);

    }

    public String getDateStr( Date date )
    {
        getUserBean();

        TimeZone tz = TimeZone.getTimeZone( userBean.getUserTimeZoneIdNoNull() );

        DateFormat dateFormatter = DateFormat.getDateInstance( DateFormat.MEDIUM, getLocale() );

        dateFormatter.setTimeZone( tz );

        return dateFormatter.format( date );
    }

    public String getToday()
    {
        return getDateStr( new Date() );
    }

    public Date getTodayDate()
    {
        return new Date();
    }

    public String getLocalTime( Date date )
    {
        getUserBean();

        TimeZone tz = TimeZone.getTimeZone( userBean.getUserTimeZoneIdNoNull() );

        DateFormat dateFormatter = new SimpleDateFormat( "K:mm a", getLocale() ); // DateFormat.getDateInstance(
        // DateFormat.MEDIUM , getLocale() );

        dateFormatter.setTimeZone( tz );

        String t = dateFormatter.format( date );

        if( t.startsWith( "0:" ) )
            t = "12:" + t.substring( 2, t.length() );

        t += " " + tz.getDisplayName(true, TimeZone.SHORT, getLocale() );

        return t;
    }


    public void getTimeZoneIdFromVtzo()
    {
        try
        {
            HttpServletRequest r = this.getHttpServletRequest();

            String vtzo = r.getParameter( "vtzo" );
            
            if( vtzo == null || vtzo.isEmpty() )
                return;

            String vtzid = r.getParameter( "vtzid" );
            
            int mins = Integer.parseInt(vtzo);
            
            // Shifted
            // float f = Float.parseFloat(vtzo);
            
            if( mins>15*60 || mins<-15*60 )
                return;
            //if( f>14 || f<-12 )
            //    return;
            
            // LogService.logIt( "FacesUtils.getTimeZoneIdFromVtzo() Timezone from vtzo is " + vtzo + ", vtzid=" + vtzid );
            
            getUserBean();
            userBean.setTimeZoneOffset( mins );
            
            // if there is a vtzid and it appears valid, save it. 
            if( vtzid!=null && !vtzid.isBlank() && TimeZone.getTimeZone(vtzid) !=null )
                userBean.setTimeZoneIdFmBrowser(vtzid);
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FacesUtils.getTimeZoneIdFromVtzo()" );
        }
    }

    public void getScrnDimsFromReq()
    {
        HttpServletRequest r = getHttpServletRequest();

        if( r==null )
            return;

        String sd = r.getParameter( "scrdims" );

        if( sd == null || sd.trim().isEmpty() || !sd.contains(",") )
            return;

        if( getUserBean()!=null )
            userBean.setScreenDimsStr(sd);
    }

    public void getMediaRecValsFromReq()
    {
        if( getUserBean()==null )
            return;
        
        HttpServletRequest r = getHttpServletRequest();

        if( r==null )
            return;

        String t = r.getParameter( "recdevs" );
        if( t != null && !t.trim().isEmpty() && Integer.parseInt(t.trim())>=0 )
            userBean.setRecDevs( Integer.parseInt( t.trim() ));

        t = r.getParameter( "medrecapi" );
        if( t != null && !t.trim().isEmpty() )
            userBean.setMedRecApi(Boolean.parseBoolean( t.trim() ));

        t = r.getParameter( "gum" );
        if( t != null && !t.trim().isEmpty() )
            userBean.setHasGetUserMedia(Integer.parseInt( t.trim() ));

    }
    
    
    public String getTimeZoneId()
    {
        try
        {
            getUserBean();

            // LogService.logIt( "FacesUtils.getTimeZoneId() userBean.getTimeZoneOffset()=" + userBean.getTimeZoneOffset() );

            getTimeZoneIdFromVtzo();

            // this means not registered.
            if( userBean.getTimeZoneOffset() < -15*60 || userBean.getTimeZoneOffset()>15*60 )
                return null;

            // LogService.logIt( "FacesUtils.getTimeZoneId() AAA userBean.getTimeZoneOffset()=" + userBean.getTimeZoneOffset() + ", userBean.getTimeZoneIdFmBrowser()=" + userBean.getTimeZoneIdFmBrowser() );
            
            TimeZone tz = TimeZoneLister.getAvailableTimeZoneForOffsetMins( userBean.getTimeZoneOffset(), userBean.getTimeZoneIdFmBrowser() );
            // TimeZone tz = TimeZoneLister.getAvailableTimeZoneForOffset( userBean.getTimeZoneOffset() );

            // LogService.logIt( "FacesUtils.getTimeZoneId() BBB userBean.getTimeZoneOffset()=" + userBean.getTimeZoneOffset() + ", tz=" + (tz==null ? "null" : tz.getID() ) );

            if( tz != null )
                return tz.getID();            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FacesUtils.getTimeZoneId()" );
        }

        return null;
    }



    public String getLocalTime()
    {
        return getLocalTime( new Date() );
    }


    protected void clearSessionData( boolean logonLogoff )
    {

        UserBean.getInstance().clearBean();
    }


    protected STHttpSessionListener getSessionListener()
    {
        try
        {
            if( FacesContext.getCurrentInstance()==null )
                return null;

            ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            STHttpSessionListener sl = (STHttpSessionListener) context.getAttribute( Constants.SYSTEM_SESSION_COUNTER );
            return sl;
        }

        catch( Exception e )
        {
            LogService.logIt( "FacesUtils.getSessionListener() Returing null. Exception: " + e.toString() );
            return null;
        }

    }


    protected HttpServletResponse getHttpServletResponse()
    {
        if( httpServletResponse==null )
        {
            try
            {
               httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            }

            catch( Exception e )
            {
                LogService.logIt( e, "FacesUtils.getHttpServletResponse() NONFatal Error. Returning null." );
            }
        }

        return httpServletResponse;
    }
    
    public boolean getIsMsieOrSamsungAndroid()
    {
        //if(1==1)
        //    return true;
        getUserBean();
        
        if( userBean.getMsieOrSamsungAndroid()!=null )
            return userBean.getMsieOrSamsungAndroid();

        String ua = getUserAgent();
        
        if( ua==null )
            return false;
        
        // return getBrowserType().isSamsung();
        
        userBean.setMsieOrSamsungAndroid( getBrowserType().isMsieOrSamsung() );
        return userBean.getMsieOrSamsungAndroid();
    }
    
    
    public boolean getIsMsie()
    {
        return getBrowserType().isMsie();
    }
    
    public BrowserType getBrowserType()
    {
        return BrowserType.getFmUserAgent( getUserAgent() );
    }
    
    public String getUserAgent()
    {
        getHttpServletRequest();
        return httpServletRequest==null ? null : httpServletRequest.getHeader( "User-Agent" );
    }
    
    
    protected HttpServletRequest getHttpServletRequest()
    {
        if( this.httpServletRequest==null )
        {
            try
            {
               httpServletRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            }

            catch( Exception e )
            {
                LogService.logIt( e, "FacesUtils.httpServletRequest() NONFatal Error. Returning null." );
            }
        }

        return httpServletRequest;
    }


    public Map<String,String> getLocaleMap()
    {
        Locale[] list = SimpleDateFormat.getAvailableLocales();

        Map<String,String> m = new TreeMap<>();

        for( int i=0; i<list.length; i++ )
        {
            if( list[i].toString().length() > 5 )
                continue;

            m.put( list[i].getDisplayName(), list[i].toString() );
        }

        return m;
    }

    public void setErrorMessage( String key, Object[] params )
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // create a FacesMessage
        FacesMessage fm = MessageFactory.getMessage( key, params );

        fm.setSeverity( FacesMessage.SEVERITY_ERROR );

        // place in FacesContext
        facesContext.addMessage( null, fm );
    }

    public void setInfoMessage( String key, Object[] params )
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // create a FacesMessage
        FacesMessage fm = MessageFactory.getMessage( key, params );

        fm.setSeverity( FacesMessage.SEVERITY_INFO );

        // place in FacesContext
        facesContext.addMessage( null, fm );
    }

    public void setStringInfoMessage( String message )
    {
        String[] params;

        params = new String[1];

        params[0] = message;

        setInfoMessage( "g.PassThru", params );
    }

    public void setStringErrorMessage( String message )
    {
        String[] params;

        params = new String[1];

        params[0] = message;

        setErrorMessage( "g.PassThru", params );
    }

    public void setMessage( Exception e )
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // create a FacesMessage
        FacesMessage fm;

        if( e instanceof STException )
        {
            fm = MessageFactory.getMessage( ( (STException) e ).getKey(), ( (STException) e ).getParams() );
        }

        else
        {
            Object[] params = new Object[1];

            params[0] = e.toString();

            // create a FacesMessage
            fm = MessageFactory.getMessage( "g.SystemError", params );
        }

        fm.setSeverity( FacesMessage.SEVERITY_ERROR );

        // place in FacesContext
        facesContext.addMessage( null, fm );
    }

    public void setMessage( String key, Object[] params, Severity severity )
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // create a FacesMessage
        FacesMessage fm = MessageFactory.getMessage( key, params );

        fm.setSeverity( severity );

        // place in FacesContext
        facesContext.addMessage( null, fm );
    }

    protected HttpSession getHttpSession()
    {
        if( getHttpServletRequest() != null )
            return httpServletRequest.getSession( true );

        return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession( true );
    }



    public UserBean getUserBean() {

        if( userBean == null )
            userBean = UserBean.getInstance();

        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public boolean isStartOverOk() {
        return startOverOk;
    }

    public void setStartOverOk(boolean startOverOk) {
        this.startOverOk = startOverOk;
    }

    public boolean isGoBackOk() {
        return goBackOk;
    }

    public void setGoBackOk(boolean goBackOk) {
        this.goBackOk = goBackOk;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public void setHttpServletResponse(HttpServletResponse r) {
        this.httpServletResponse = r;
    }



}
