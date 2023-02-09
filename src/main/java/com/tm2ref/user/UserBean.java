/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.user;

import com.tm2ref.entity.user.User;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.service.LogService;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;

/**
 *
 * @author Mike
 */
@Named
@SessionScoped
public class UserBean implements Serializable
{
    Locale locale = null;

    private User user;

    private User user2;

    private String temp;

    // private String htmlLangCode = null;

    //private int failedLogonAttempts = 0;

    private String tempPassword;
    private String altPassword;

    private long logonHistoryId=0;
    private boolean previousLogout=false;
    private boolean logonRequired = false;

    private int timeZoneOffset = -999999;
    private String timeZoneIdFmBrowser = null;
    
    private String screenDimsStr;
    
    // -1 = unknown and unchecked
    // 0 = unknown but checked.
    // 1 = microphone only
    // 2 = camera (assumes also has a microphone.
    private int recDevs = -1;
    private boolean medRecApi;
    private int hasGetUserMedia;
    private String selCamera;
    private String selMicrophone;
    
    private Boolean msieOrSamsungAndroid;


    /** Creates a new instance of UserBean */
    public UserBean()
    {
    }

    public void clearBean()
    {        
        locale = null;
        user = null;
        user2 = null;
        temp = null;
        // htmlLangCode = null;
        // failedLogonAttempts = 0;
        tempPassword = null;
        altPassword = null;
        logonHistoryId=0;
        previousLogout=false;
        logonRequired = false;
        timeZoneOffset = -999999;
        timeZoneIdFmBrowser = null;
        msieOrSamsungAndroid=null;
    }



    public static UserBean getInstance()
    {
        try
        {
            FacesContext fc = FacesContext.getCurrentInstance();

            return (UserBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "userBean" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserBean.getInstance() NON-FATAL " );

            return null;
        }
    }

    @Override
    public String toString() {
        return "UserBean{" + "locale=" + locale + ", user=" + ( user==null ? "null" : user.toString() ) + '}';
    }



    /*
    public String getLanguageName()
    {


        return MessageFactory.getStringMessage( getLocale() , getLocale().toString(), null );
    }
    */

    public String getUserTimeZoneIdNoNull()
    {
        if( user != null && user.getTimeZoneId()!=null )
            return user.getTimeZoneId();

        return TimeZone.getDefault().getID();  //  Constants.DEFAULT_TIMEZONE_ID;
    }

    public TimeZone getUserTimeZoneNoNull()
    {
        if( user != null && user.getTimeZone()!=null )
            return user.getTimeZone();

        return TimeZone.getDefault();
    }


    public User getUser()
    {
        if( user == null )
            user = new User();

        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public Date getCurrentDate()
    {
        return new Date();
    }

    public String getCurrentYear()
    {
        GregorianCalendar c = new GregorianCalendar();

        return Integer.toString( c.get( Calendar.YEAR ) );
    }

    public synchronized String getHtmlLangCode()
    {
        return getLocale().toString().replace( '_', '-' );

        /*
        if( htmlLangCode == null )
        {
            FacesUtils facesUtils = FacesUtils.getInstance();

            htmlLangCode = facesUtils.getLocale().toString().replace( '_', '-' ); // .
        }

        return htmlLangCode;
                */
    }

    public String getTextDirection()
    {
        return I18nUtils.isTextRTL( getLocale() ) ? "rtl" : "ltr";
    }

    public boolean getIsRTL()
    {
        return I18nUtils.isTextRTL( getLocale() );
    }

    public boolean getUserLoggedOn()
    {
        return getUserLoggedOnCorp();
    }


    public boolean getUserLoggedOnCorp()
    {
        if( user != null &&
            user.getUserId() > 0 &&
            user.getRoleId() > RoleType.DISABLED_USER.getRoleTypeId() )
            return true;

        return false;
    }

    public boolean getUserLoggedOnAsAdmin()
    {
        return user != null && user.getUserId()>0 && ( user.getRoleId() >= RoleType.ADMIN.getRoleTypeId() );
    }



    public long getUserId()
    {
        if( user == null )
            return 0;

        return user.getUserId();
    }

    public String getAltPassword() {
        return altPassword;
    }

    public void setAltPassword(String altPassword) {
        this.altPassword = altPassword;
    }

    public String getTempPassword() {
        return tempPassword;
    }

    public void setTempPassword(String tempPassword) {
        this.tempPassword = tempPassword;
    }

    public long getLogonHistoryId() {
        return logonHistoryId;
    }

    public void setLogonHistoryId(long logonHistoryId) {
        this.logonHistoryId = logonHistoryId;
    }

    public boolean getPreviousLogout() {
        return previousLogout;
    }

    public void setPreviousLogout(boolean previousLogout) {
        this.previousLogout = previousLogout;
    }

    public boolean getLogonRequired() {
        return logonRequired;
    }

    public void setLogonRequired(boolean logonRequired) {
        this.logonRequired = logonRequired;
    }

    public User getUser2() {

        if( user2 == null )
            user2 = new User();

        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }


    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public int getTimeZoneOffset()
    {
        //if( 1==1 )
        //    return 5.5f;

        return timeZoneOffset;
    }

    public void setTimeZoneOffset(int timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public Locale getLocaleNoDefault()
    {

        if( locale == null )
        {
            if( FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getViewRoot()!=null )
                locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        }

        return locale;
    }

    public Locale getLocale()
    {
        if( locale == null )
        {
            if( FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getViewRoot()!=null )
                locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();

            if( locale == null )
                locale = Locale.US;
        }

        return locale;
    }

    public void setLocale(Locale l) {
        this.locale = l;
    }

    public int[] getScreenDims()
    {
        if( screenDimsStr==null || screenDimsStr.trim().isEmpty()  )
            return null;
        
        screenDimsStr = screenDimsStr.trim();      
        int idx = screenDimsStr.indexOf(",");
        
        if( idx<=0 || idx==screenDimsStr.length()-1 )
            return null;
        
        return new int[] { Integer.parseInt( screenDimsStr.substring(0, idx)), Integer.parseInt( screenDimsStr.substring(idx+1, screenDimsStr.length() )) };
    }
    
    public String getScreenDimsStr() {
        return screenDimsStr;
    }

    public void setScreenDimsStr(String screenDimsStr) {
        this.screenDimsStr = screenDimsStr;
    }

    public int getRecDevs() {
        return recDevs;
    }

    public void setRecDevs(int r) {
        this.recDevs = r;
    }

    public boolean isMedRecApi() {
        return medRecApi;
    }

    public void setMedRecApi(boolean medRecApi) {
        this.medRecApi = medRecApi;
    }

    public int getHasGetUserMedia() {
        return hasGetUserMedia;
    }

    public void setHasGetUserMedia(int hasGetUserMedia) {
        this.hasGetUserMedia = hasGetUserMedia;
    }

    public String getTimeZoneIdFmBrowser() {
        return timeZoneIdFmBrowser;
    }

    public void setTimeZoneIdFmBrowser(String timeZoneIdFmBrowser) {
        this.timeZoneIdFmBrowser = timeZoneIdFmBrowser;
    }

    public Boolean getMsieOrSamsungAndroid() {
        return msieOrSamsungAndroid;
    }

    public void setMsieOrSamsungAndroid(Boolean msieOrSamsungAndroid) {
        this.msieOrSamsungAndroid = msieOrSamsungAndroid;
    }

    public String getSelCamera() {
        return selCamera;
    }

    public void setSelCamera(String selCamera) {
        this.selCamera = selCamera;
    }

    public String getSelMicrophone() {
        return selMicrophone;
    }

    public void setSelMicrophone(String selMicrophone) {
        this.selMicrophone = selMicrophone;
    }

    
    
}
