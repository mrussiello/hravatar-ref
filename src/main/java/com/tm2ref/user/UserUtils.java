/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.user;

import com.tm2ref.corp.CorpBean;
import com.tm2ref.entity.user.LogonHistory;
import com.tm2ref.entity.user.User;
import com.tm2ref.faces.FacesUtils;
import com.tm2ref.faces.HttpReqUtils;
import com.tm2ref.faces.LocaleNameComparator;
import com.tm2ref.global.Constants;
import com.tm2ref.global.HttpUtils;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.reminder.ReminderStarter;
import com.tm2ref.reminder.ReminderThread;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.STHttpSessionListener;
import com.tm2ref.util.StringUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.model.SelectItem;

/**
 *
 * @author Mike
 */
@Named
@RequestScoped
public class UserUtils extends FacesUtils
{
    private String logonName;

    private String logonKey;

    // private boolean rememberLogon = false;

    private String email2;
    
    private boolean logonLockout = false;

    // @EJB
    private UserFacade userFacade;


    public static UserUtils getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        return (UserUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "userUtils" );
    }



    public List<SelectItem> getLanguageSelectItemListSupportedOnly()
    {
        return getLanguageSelectItemList( true );
    }

    public List<SelectItem> getLanguageSelectItemList()
    {
        return getLanguageSelectItemList( false );
    }


    public List<SelectItem> getLanguageSelectItemList( boolean supportedOnly )
    {
        Set<Locale> locs = new HashSet<>();

        if( !supportedOnly )
        {
            if( userBean.getUserLoggedOn()&& userBean.getUser().getLocaleToUseDefaultNull()!=null )
                locs.add( userBean.getUser().getLocaleToUseDefaultNull() );

            if( getHttpServletRequest()!=null )
                locs.add( HttpUtils.detectLocale( getHttpServletRequest(), false) );
        }

        locs.add( I18nUtils.getLocaleFromCompositeStr( "ar_JO" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "ar_LB" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "da_DK" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "de_DE" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "en_CA" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "en_US" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "en_GB" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "en_AU" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "en_IN" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "es_ES" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "es_MX" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "fr_CA" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "fr_FR" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "in_ID" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "it_IT" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "he_IL" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "ja_JP" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "ko_KR" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "nb_NO" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "nl_NL" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "pl_PL" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "pt_BR" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "pt_PT" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "ro_RO" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "ru_RU" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "sv_SE" ));
        locs.add( I18nUtils.getLocaleFromCompositeStr( "zh_CN" ));

        List<Locale> ll = new ArrayList<>();

        ll.addAll( locs );

        Collections.sort( ll, new LocaleNameComparator() );

        List<SelectItem> out = new ArrayList<>();

        for( Locale l : ll )
            out.add( new SelectItem( l.toString(), l.getDisplayLanguage() + " (" + l.getDisplayCountry() + ")" ) );

        return out;
    }

    public String processLogonAttempt()
    {
        return performLogonAttempt(null, true );
    }

    public String performLogonAttempt( User forceUser, boolean requireAdmin)
    {
        try
        {
            getUserBean();

            if( PasswordUtils.hasTooManyFailedLogons(logonName) ) //  userBean.getFailedLogonAttempts() >= Constants.MAX_FAILED_LOGON_ATTEMPTS )
                throw new STException( "g.TooManyFailedLogonAttempts" );

            String ipAddress = HttpReqUtils.getClientIpAddress( getHttpServletRequest() );            
            if( PasswordUtils.hasTooManyFailedLogons4Ip(ipAddress) )
                throw new STException( "g.TooManyFailedLogonAttempts" );
            
                        
            if( this.logonName == null || this.logonName.isEmpty() )
                 throw new STException( "g.EmailOrUsernameRequired" );

            if( logonKey == null || logonKey.isEmpty() )
                 throw new STException( "g.PasswordRequired" );

            User user = forceUser!=null ? forceUser : performLogon(LogonType.USER.getLogonTypeId(), requireAdmin );

            LogService.logIt( "UserUtils.processLogonAttempt() logonName=" + logonName + ", user=" + ( user==null ? "null" : user.getFullname()) );
                        
            if( user != null )
            {
                performPostLogon(  user );

                if( user.getTimeZoneId() == null || user.getTimeZoneId().isEmpty() )
                {
                    user.setTimeZoneId( getTimeZoneId() );

                    if( user.getTimeZoneId()!= null )
                    {
                        if( userFacade == null ) userFacade = UserFacade.getInstance();

                        user = userFacade.saveUser(user, true);
                    }
                }

                if( getSessionListener() != null )
                    getSessionListener().addData(getHttpSession().getId() , user, "Admin User Logon" );
                else
                    LogService.logIt( "UserUtils.processLogonAttempt() getSessionListener() is null!" );

                return null;
            }
            
            //else
            //{
            PasswordUtils.addFailedLogon(logonName);
            PasswordUtils.addFailedLogon4Ip(ipAddress);
            //}
            
            // userBean.setFailedLogonAttempts( userBean.getFailedLogonAttempts() + 1 );

            // userBean.setFailedLogonAttempts( userBean.getFailedLogonAttempts() + 1 );
            if( logonLockout )
                throw new STException( "g.lockedoutFromLogon" );
                        
            throw new STException( "g.InfoInvalid" );
        }

        catch( STException e )
        {
            setMessage( e );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserUtils.processLogonAttempt()" );

            setMessage( e );
        }

        return null;
    }

    public String processStartReminderBatch()
    {
        try
        {
            getUserBean();
            if( !userBean.getUserLoggedOnAsAdmin() )
                throw new Exception( "User not authorized for this action." );
            
           (new Thread(new ReminderThread(true))).start(); 

           setStringInfoMessage( "Reminder Thread Started." );
        }
        catch( STException e )
        {
            setMessage( e );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "UserUtils.processStartReminderBatch()" );
            setMessage( e );
        }

        return null;
    }
    
    

    public void performPostLogon( User user ) throws Exception
    {
        getUserBean();

        // in case old stuff needs to get cleaned up
        FacesContext fc = FacesContext.getCurrentInstance();

        fc.getExternalContext().getSessionMap().remove( "" );

        userBean.setTempPassword( Constants.DUMMY_PASSWORD );

        userBean.setAltPassword( Constants.DUMMY_PASSWORD );
    }




    /**
     * This method simply tests the logon
     *
     * @param logonTypeId
     * @return
     */
    private User performLogon( int logonTypeId, boolean requireAdmin)
    {
        try
        {
            userBean.setTempPassword( Constants.DUMMY_PASSWORD );

            logonName = StringUtils.sanitizeStringFull( logonName );

            logonKey = StringUtils.sanitizeStringFull( logonKey );

            if( userFacade == null ) userFacade = UserFacade.getInstance();

            // find user by info
            User user = userFacade.getUserByLogonInfo( logonName, logonKey );

            LogService.logIt( "UserUtils.performLogon() " + logonName + ", ****** " + ", user is " + (user == null ? "Null" : "Not Null" ) );

            if( requireAdmin && user!=null && !user.getRoleType().getIsAdmin() )
                user = null;
            
            logonKey = "";

            return logonUser( user, logonTypeId );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserUtils.performLogon()" );

            return null;
        }
    }

    /**
     * This method actually performs the logon
     *
     * @param user
     * @param logonTypeId
     * @return
     */
    public User logonUser( User user, int logonTypeId )
    {
        try
        {
            userBean.setTempPassword( Constants.DUMMY_PASSWORD );

            // Can't do this here!
            //clearSessionData( true );
            if( user!=null && user.getLockoutDate()!=null )
            {
                Calendar cal = new GregorianCalendar();
                cal.add( Calendar.MINUTE, -1*Constants.LOGON_LOCKOUT_MINUTES );
                
                if( cal.getTime().before( user.getLockoutDate() ) )
                {
                    logonLockout=true;
                    user=null;
                    // setStringInfoMessage( "Account is temporarily locked out." );
                }
                
                else
                {
                    user.setLockoutDate(null);
                    
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();

                    userFacade.saveUser( user, false );
                }
            }
            
            else if( user == null )
            {
                // increment.
                PasswordUtils.addFailedLogon( logonName );
                // userBean.setFailedLogonAttempts( userBean.getFailedLogonAttempts() + 1 );
                
                // lockout this user for 30 if too many attempts.
                if( PasswordUtils.hasTooManyFailedLogons(logonName) ) // userBean.getFailedLogonAttempts()>=Constants.MAX_FAILED_LOGON_ATTEMPTS )
                {
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();

                    User u2 = userFacade.getUserByUsername(logonName);

                    if( u2!=null )
                    {
                        logonLockout=true;
                        // setStringInfoMessage( "Account is temporarily locked out." );
                        u2.setLockoutDate( new Date() );
                        userFacade.saveUser(u2, false );
                    }
                }                                
            }

            
            if( user!=null )
            {
                getHttpServletRequest().changeSessionId();

                Calendar cal = new GregorianCalendar();                
                cal.add( Calendar.MONTH, -1*Constants.MAX_PASSWORD_AGE_MONTHS );
                if( user.getResetPwd() != Constants.YES && user.getPasswordStartDate().before( cal.getTime() ) )
                {
                    user.setResetPwd( Constants.YES );                    
                    if( userFacade==null )
                        userFacade=UserFacade.getInstance();
                    userFacade.saveUser(user, false );
                     // if( user.getResetPwd() == 1 )
                    setInfoMessage( "g.PasswordHasExpiredLogonDenied", null );
                    user=null;
                }  
                
                else if( user.getResetPwd()== Constants.YES )
                {
                    setInfoMessage( "g.PasswordHasExpiredLogonDenied", null );
                    user=null;                    
                }
            }
            
            if( user != null )
            {
                userBean.setTempPassword( Constants.DUMMY_PASSWORD );

                userBean.setAltPassword( Constants.DUMMY_PASSWORD );

                // if this user is already logged on, skip
                if( userBean.getUserLoggedOnCorp() && userBean.getUserId() == user.getUserId() )
                    return user;

                // if a user is already logged on, log them out.
                if( userBean.getUserLoggedOnCorp() )
                    processLogout( LogoffType.USER_REPLACEMENT.getLogoffTypeId() );

                if( !user.getLogonAllowed() )
                    throw new Exception( "Logon Not Allowed For User" );

                userBean.setUser( user );

                // reset logonRequired flag
                userBean.setLogonRequired( false );

                String ua = null;
                String ip = null;
                
                
                if( userFacade == null ) 
                    userFacade = UserFacade.getInstance();

                // if( user.getLocaleStr()==null || user.getLocaleStr().isEmpty() )
                // {
                if( getHttpServletRequest()!=null )
                {
                    user.setLocaleStr( HttpUtils.detectLocale( getHttpServletRequest(), false).toString() );
                    user = userFacade.saveUser(user, true);
                    ua = getHttpServletRequest().getHeader( "User-Agent");
                    ip = HttpReqUtils.getClientIpAddress( getHttpServletRequest() );
                }
                    //else
                    //    user.setLocaleStr( userBean.getLocale().toString() );

                // }

                LogonHistory logonHistory = userFacade.addLogonHistory(user, logonTypeId, ua, ip );
                userBean.setLogonHistoryId( logonHistory.getLogonHistoryId() );
                userBean.setPreviousLogout( false );

                Tracker.addLogon();

                // set user in sessionInfoMap
                if( getSessionListener() != null )
                    getSessionListener().userLogon( getHttpSession().getId(), user );
                else
                    LogService.logIt( "UserUtils.logonUser() getSessionListener() is null!" );

                // set values
                logonName = user.getUsername();

                logonKey = null;

                PasswordUtils.clearFailedLogons( user.getUsername() );

                if( user.getLocaleToUseDefaultNull() != null  ) //  && !user.getLocaleToUse().equals( getLocale() ) )
                    changeUserLocale( user.getLocaleToUseDefaultNull() );

                // LogService.logIt( "UserUtils.logonUser() locale is " + getLocale().toString() );
                // user.setAddressList( userFacade.getAddressList( user.getUserId() ) );

                CorpBean corpBean = CorpBean.getInstance();
                
                if( user.getResetPwd() == 1 )
                    setInfoMessage( "g.PleaseResetYourPassword", null );
                
                else if( user.getRoleType().getIsAdmin() )
                {
                    // set the message on last login
                    Date lastLogin = userFacade.getLastLogonDate( user.getUserId(), userBean.getLogonHistoryId() );

                    if( lastLogin != null )
                    {
                       LogService.logIt( "UserUtils.completeLogon: lastLogin=" + lastLogin.toString() );

                       Calendar cal = new GregorianCalendar();
                       cal.setTime( user.getPasswordStartDate()==null ? new Date() : user.getPasswordStartDate() );
                       cal.add( Calendar.MONTH, Constants.MAX_PASSWORD_AGE_MONTHS );
                       Date pwdExpDate = cal.getTime();
                       // setStringInfoMessage( "Last login: " + lastLogin.toString() + ", password expires: " + pwdExpDate.toString() );
                       // setStringInfoMessage( "Last login: " + lastLogin.toString() + ", password expires: " + pwdExpDate.toString() );
                       
                       setInfoMessage( "g.LastLoginInfo", new String[]{ I18nUtils.getFormattedDateTime( userBean.getLocale(), lastLogin, userBean.getUserTimeZoneNoNull()),
                                                                         I18nUtils.getFormattedDate( userBean.getLocale(), pwdExpDate, userBean.getUserTimeZoneNoNull() ) } );
                       
                       
                    }
                    
                }
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserUtils.logonUser()" );

            user = null;
        }

        return user;
    }



    public void selectLanguage( ValueChangeEvent ev )
    {
        try
        {
            getUserBean();

            String newValue = (String) ev.getNewValue();

            Locale newLocale = I18nUtils.getLocaleFromCompositeStr( newValue );

            changeUserLocale( newLocale );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserUtils.selectLanguage() " );
        }
    }



    public User createUserRegistration( User user ) throws STException, Exception
    {
        getUserBean();

        boolean noLogonUser = user.getRoleId() == RoleType.NO_LOGON.getRoleTypeId();

        if( user.getOrgId() <= 0 )
        {
            user.setOrgId( RuntimeConstants.getIntValue( "defaultorgid" ) );

            user.setSuborgId( RuntimeConstants.getIntValue( "defaultsuborgid" ) );
        }

        // unregistered users always have a random password and username.
        if( noLogonUser )
        {
            // force generation later
            user.setUsername( null );

            user.setPassword( StringUtils.generateRandomString( 8 ) );
        }

        if( user.getPassword() == null || user.getPassword().length() < Constants.MIN_PASSWORD_LENGTH )
            user.setPassword( StringUtils.generateRandomString( 8 ) );

        if( userFacade == null )
            userFacade = UserFacade.getInstance();

        // check for username existence
        User tempUser = null;


        // User IP related data is now set in the scoring system.
        //if( user.getCountryCode() == null || user.getCountryCode().length() == 0 )
        //{
        /*
            String ipAddress = ( (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest() ).getRemoteAddr();

            if( ipAddress != null && ipAddress.length() > 0 && !ipAddress.equals("0:0:0:0:0:0:0:1") )
            {
                try
                {
                    //  UserFacade userFacade = UserFacade.getInstance();
                    String[] ipData = userFacade.getIPLocationData( ipAddress );

                    if( ipData[0] != null && !ipData[0].isEmpty() )
                        user.setCountryCode( ipData[0] );
                    user.setIpState( ipData[1] );
                    user.setIpCity( ipData[2] );
                }
                catch( Exception ee )
                {
                    LogService.logIt(ee, "UserUtils.createUserRegistration(() Reading IP locations ip=" + ipAddress );
                }

            }
        //}

        if( user.getGeographicRegionId() == 0 &&
            user.getCountryCode() != null &&
            !user.getCountryCode().isEmpty() )
        {
            Country c = userFacade.getCountryByCode( user.getCountryCode() );

            if( c != null )
                user.setGeographicRegionId( c.getGeographicRegionId() );
        }
        */

        String tzid = getTimeZoneId();

        if( tzid != null && ( user.getTimeZoneId() == null || !user.getTimeZoneId().equalsIgnoreCase( tzid ) ) )
        {
            LogService.logIt( "UserUtils.createUserRegistration() setting time zone to " + tzid );
            user.setTimeZoneId( tzid );
        }

        // Still no username, create a dummy
        if( user.getUsername() == null || user.getUsername().length() == 0 )
        {
            String tempUsername = null;

            do
            {
                tempUsername = StringUtils.generateRandomString( 12 );

                tempUser = userFacade.getUserByUsername( tempUsername );

            }  while( tempUser != null );

            user.setUsername( tempUsername );
        }

        // username provided, check doesn't exist AND that it's not someone
        // else's email
        else
        {
            tempUser = userFacade.getUserByUsername( user.getUsername() );

            // if tempUser exists and is registered
            if( tempUser != null && tempUser.getRoleId() > RoleType.NO_LOGON.getRoleTypeId() )
                throw new STException( "g.UsernameUnavailable" );

            // if username exists but is unregistered, change the username of
            // the unregistered person so can use for this new reg. which is not a no_logon type.
            else if( tempUser != null )
            {
                User tempUser2 = null;

                String tempUsername = null;

                do
                {
                    tempUsername = StringUtils.generateRandomString( 10 );

                    tempUser2 = userFacade.getUserByUsername( tempUsername );

                } while( tempUser2 != null );

                tempUser.setUsername( tempUsername );

                tempUser = userFacade.saveUser(tempUser, true );
            }
        }

        // check if email exists already in a user
        if( user.getEmail() != null && !user.getEmail().isEmpty() )
            tempUser = userFacade.getUserByEmailAndOrgId( user.getEmail(), user.getOrgId() );

        else
            tempUser = null;

        // registered user exists with same email. This should have been caught prior to this point.
        if( tempUser != null && tempUser.getRoleId() > RoleType.NO_LOGON.getRoleTypeId() )
            throw new STException( "g.EmailFound" );

        // if exists but is unregistered - use this account
        else if( tempUser != null )
        {
            LogService.logIt( "UserUtils.createUserRegistration() " + user.toString() + ", setting u seserId to " + tempUser.getUserId() );
            user.setUserId( tempUser.getUserId() );
        }

        //if(user.getLocaleStr()==null || user.getLocaleStr().isEmpty() )
        //{
            if( getHttpServletRequest() != null )
                user.setLocaleStr( HttpUtils.detectLocale( getHttpServletRequest(), false ).toString() );

            //else if( userBean != null )
            //    user.setLocaleStr( userBean.getLocale().toString() );
        //}

        user = userFacade.saveUser( user, true );

        // next, save password (as MD5, must do separately).
        userFacade.updatePassword( user );

        return user;
    }


    public String getHomeView()
    {
        return "/index.xhtml";
    }


    public void changeUserLocale( Locale newLocale ) throws Exception
    {
        try
        {
            // LogService.logIt( "UserUtils.changeUserLocale() " + newLocale.toString() );
            getUserBean();

            userBean.setLocale( newLocale );

            if( FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getViewRoot()!=null )
                FacesContext.getCurrentInstance().getViewRoot().setLocale( newLocale );

            /*
            if( userBean.getUser().getLocaleStr()== null )
            {
                userBean.getUser().setLocaleStr( newLocale.toString() );

                if( userBean.getUserLoggedOnCorp() )
                {
                    if( userFacade == null ) userFacade = UserFacade.getInstance();

                    userFacade.saveUser(userBean.getUser(), true );
                }
            }
            */

        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserUtils.changeUserLocale() " );

            throw new STException( e );
        }
    }



    public String processUserLogOff()
    {
        String s = processLogout( LogoffType.USER.getLogoffTypeId() );

        clearSessionData( true );

        return s;
    }


    public String processLogout( int logoffTypeId )
    {
        try
        {
            getUserBean();

            if( userBean.getUserLoggedOnCorp() && userBean.getLogonHistoryId() > 0 )
            {
                if( userFacade == null ) userFacade = UserFacade.getInstance();

                userFacade.addUserLogout( userBean.getLogonHistoryId(), logoffTypeId );
            }

            userBean.setLogonHistoryId( 0 );

            userBean.setUser( new User() );

            userBean.setAltPassword( Constants.DUMMY_PASSWORD );

            userBean.setTempPassword( Constants.DUMMY_PASSWORD );

            userBean.setPreviousLogout( true );
            
            getHttpServletRequest().changeSessionId();

            Tracker.addLogout();

            if( getSessionListener() != null )
                getSessionListener().userLogout( getHttpSession().getId() );
            else
                LogService.logIt( "UserUtils.processLogout() getSessionListener() is null!" );
        }

        catch( STException e )
        {
            setMessage( e );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "UserUtils.processLogout()" );

            setMessage( e );
        }

        return getHomeView();
    }

    @Override
    public STHttpSessionListener getSessionListener()
    {
        STHttpSessionListener sl = (STHttpSessionListener) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get( Constants.SYSTEM_SESSION_COUNTER );

        return sl;
    }

    public String getLogonKey() {
        return logonKey;
    }

    public void setLogonKey(String logonKey) {
        this.logonKey = logonKey;
    }

    public String getLogonName() {
        return logonName;
    }

    public void setLogonName(String logonName) {
        this.logonName = logonName;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }


}
