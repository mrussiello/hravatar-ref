package com.tm2ref.util;

import com.tm2ref.global.Constants;
import com.tm2ref.service.LogService;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;


/**
 *
 * <p>
 * supported filters: <code>package</code> and <code>protection</code>.
 * </p>
 */
public class MessageFactory extends Object
{
    private static String defaultBundleName = null;

    //
    // Constructors and Initializers
    //
    private MessageFactory()
    {}

    //
    // Class methods
    //

    //
    // Class methods
    //
    public static String getStringMessage( Locale locale, String messageId )
    {
        return getStringMessage( locale, messageId, null );
    }


    public static String getStringMessage( Locale locale, String messageId, Object[] params )
    {
        return getStringMessage( null, locale, messageId, params ); 
    }

    public static String getStringMessage( String bundleName, Locale locale, String messageId, Object[] params )
    {
        // FacesMessage result = null;
        String summary = null;

        // String bundleName = null;

        ResourceBundle bundle = null;

        if( locale == null )
        {
            FacesContext context = FacesContext.getCurrentInstance();

            // context.getViewRoot() may not have been initialized at this point.
            if( ( context != null ) && ( context.getViewRoot() != null ) )
            {
                locale = context.getViewRoot().getLocale();

                if( locale == null )
                    locale = Locale.getDefault();

            }

            else
                locale = Locale.getDefault();
        }

        if( getApplication() != null && (bundleName==null || bundleName.equals( Constants.DEFAULT_RESOURCE_BUNDLE )))
            bundleName = getApplication().getMessageBundle();

        if( bundleName == null )
            bundleName = defaultBundleName == null || defaultBundleName.length() == 0 ? Constants.DEFAULT_RESOURCE_BUNDLE : defaultBundleName;

        // LogService.logIt( "MessageFactory.getStringMessage() " + bundleName + ", params=" + (params==null ? "params is null" : params.length));

        // see if we have a user-provided bundle
        if( bundleName != null )
        {
            if( null != ( bundle = ResourceBundle.getBundle( bundleName, locale, getCurrentLoader( bundleName ) ) ) )
            {
                // see if we have a hit
                try
                {
                    summary = bundle.getString( messageId );

                    if( summary != null )
                        summary = substituteParams( locale, summary, params );
                }


                catch( MissingResourceException e )
                {
                    LogService.logIt(e, "MessageFactory.getStringMessage() " + e.toString() + ", bundleName=" + bundleName );
                }

                catch( Exception e )
                {
                    LogService.logIt(e, "MessageFactory.getStringMessage() " + messageId );

                    summary = messageId;
                }
            }
        }

        return summary;
    }

    //
    // General Methods
    //

    /**
     * VERY IMPORTANT: Remember that single quotes ' can block substitution of a parameter and are eliminated from the
     * formatted message. Be sure that all single quotes are put in as: '' to avoid this.
     *
     */
    public static String substituteParams( Locale locale, String msgtext, Object[] params )
    {
        String localizedStr = null;

        // LogService.logIt( "MessageFactory.substituteParams() " + msgtext + ", params=" + (params==null ? "params is null" : params.length));

        if( ( params == null ) || ( msgtext == null ) )
        {
            return msgtext;
        }

        // LogService.logIt( "MessageFactory.substituteParams() locale=" + (locale == null ? "null" : locale.toString() ) + " "  + msgtext + ", params=" + (params==null ? "params is null" : params.length));

        StringBuilder b = new StringBuilder( 100 );

        MessageFormat mf = new MessageFormat( msgtext );

        if( locale != null )
        {
            mf.setLocale( locale );

            b.append( mf.format( params ) );

            localizedStr = b.toString();
        }

        return localizedStr;
    }

    /**
     * This version of getMessage() is used in the RI for localizing RI specific messages.
     */
    public static FacesMessage getMessage( String key, Object[] params )
    {
        Locale locale = null;

        FacesContext context = FacesContext.getCurrentInstance();

        // context.getViewRoot() may not have been initialized at this point.
        if( ( context != null ) && ( context.getViewRoot() != null ) )
        {
            locale = context.getViewRoot().getLocale();

            if( locale == null )
            {
                locale = Locale.getDefault();
            }
        }

        else
        {
            locale = Locale.getDefault();
        }

        return getMessage( locale, key, params );
    }

    public static FacesMessage getMessage( Locale locale, String key, Object[] params )
    {
        // FacesMessage result = null;
        String summary = null;

        String detail = null;

        String bundleName = null;

        ResourceBundle bundle = null;

        if( getApplication() != null )
        {
            bundleName = getApplication().getMessageBundle();
        }

        if( bundleName == null )
            bundleName = defaultBundleName == null || defaultBundleName.length() == 0 ? Constants.DEFAULT_RESOURCE_BUNDLE : defaultBundleName;

        // see if we have a user-provided bundle
        if( bundleName != null )
        {
            if( null != ( bundle = ResourceBundle.getBundle( bundleName, locale, getCurrentLoader( bundleName ) ) ) )
            {
                // see if we have a hit
                try
                {
                    summary = bundle.getString( key );
                }

                catch( MissingResourceException e )
                {}
            }
        }

        // we couldn't find a summary in the user-provided bundle
        if( null == summary )
        {
            // see if we have a summary in the app provided bundle
            bundle = ResourceBundle.getBundle( FacesMessage.FACES_MESSAGES, locale, getCurrentLoader( bundleName ) );

            if( null == bundle )
            {
                LogService.logIt( "Cannot find FACES_MESSAGES ResourceBundle." );

                // summary = "Cannot find FACES_MESSAGES ResourceBundle";
                throw new NullPointerException();
            }

            // see if we have a hit
            else
            {
                try
                {
                    summary = bundle.getString( key );
                }

                catch( MissingResourceException e )
                {}
            }

        }

        // we couldn't find a summary anywhere! Return null
        if( null == summary )
        {
            summary = "KEY " + key + " NOT FOUND! in ResourceBundle " + bundleName + " or in FACES_MESSAGES Bundle";
            // return null;
        }

        summary = substituteParams( locale, summary, params );

        // At this point, we have a summary and a bundle.
        if( null == bundle )
            return new FacesMessage( summary, detail );
        // throw new NullPointerException();

        // summary done. Now look for detail.

        try
        {
            detail = substituteParams( locale, bundle.getString( key + "_detail" ), params );
        }

        catch( MissingResourceException e )
        {}

        return new FacesMessage( summary, detail );
    }

    //
    // Methods from MessageFactory
    //
    public static FacesMessage getMessage( FacesContext context, String messageId )
    {
        return getMessage( context, messageId, null );
    }


    public static FacesMessage getMessage( FacesContext context, String messageId, Object[] params )
    {
        if( ( context == null ) || ( messageId == null ) )
        {
            throw new NullPointerException( "One or more parameters could be null" );
        }

        Locale locale = null;

        // viewRoot may not have been initialized at this point.
        if( ( context != null ) && ( context.getViewRoot() != null ) )
        {
            locale = context.getViewRoot().getLocale();
        }
        else
        {
            locale = Locale.getDefault();
        }

        if( null == locale )
        {
            throw new NullPointerException();
        }

        FacesMessage message = getMessage( locale, messageId, params );

        if( message != null )
        {
            return message;
        }

        locale = Locale.getDefault();

        return ( getMessage( locale, messageId, params ) );
    }

    /*
    public static FacesMessage getMessage( FacesContext context, String messageId, Object param0 )
    {
        return getMessage( context, messageId, new Object[] { param0 } );
    }

    public static FacesMessage getMessage( FacesContext context, String messageId, Object param0, Object param1 )
    {
        return getMessage( context, messageId, new Object[] { param0, param1 } );
    }

    public static FacesMessage getMessage( FacesContext context, String messageId, Object param0, Object param1, Object param2 )
    {
        return getMessage( context, messageId, new Object[] { param0, param1, param2 } );
    }

    public static FacesMessage getMessage( FacesContext context, String messageId, Object param0, Object param1, Object param2, Object param3 )
    {
        return getMessage( context, messageId, new Object[] { param0, param1, param2, param3 } );
    }
    *
    */

    protected static Application getApplication()
    {
        if( FacesContext.getCurrentInstance() == null )
            return null;

        return ( FacesContext.getCurrentInstance().getApplication() );
    }

    protected static ClassLoader getCurrentLoader( Object fallbackClass )
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        if( loader == null )
        {
            loader = fallbackClass.getClass().getClassLoader();
        }

        return loader;
    }

    public static String getDefaultBundleName()
    {
        return defaultBundleName;
    }

    public static void setDefaultBundleName( String defaultBundleName )
    {
        MessageFactory.defaultBundleName = defaultBundleName;
    }

} // end of class MessageFactory
