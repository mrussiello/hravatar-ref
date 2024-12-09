package com.tm2ref.service;

import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import static com.tm2ref.service.EmailConstants.ATTACH_BYTES;
import static com.tm2ref.service.EmailConstants.ATTACH_FN;
import static com.tm2ref.service.EmailConstants.ATTACH_MIME;
import static com.tm2ref.service.EmailConstants.CONTENT;
import static com.tm2ref.service.EmailConstants.FROM;
import static com.tm2ref.service.EmailConstants.MIME_TYPE;
import static com.tm2ref.service.EmailConstants.OVERRIDE_BLOCK;
import static com.tm2ref.service.EmailConstants.SUBJECT;
import static com.tm2ref.service.EmailConstants.TO;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;


/**
 * This is a REQUEST-LEVEL Backing Bean for JSF Actions.
 *
 * This bean contains injected resources, which means that it cannot (and should not) be serialized and restored easily.
 * Do not move to session.
 *
 * @author Mike
 *
 */

import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import java.util.Locale;
import org.apache.commons.validator.routines.EmailValidator;

@Named
@RequestScoped
public class EmailUtils
{

    static EmailUtils emu;

    //@EJB
    private EmailerFacade emailerFacade;




    /**
     * Convenience method to get/create current instance of this bean
     *
     * @return
     */
    public static EmailUtils getInstance()
    {
        //FacesContext fc = FacesContext.getCurrentInstance();

        //if( fc==null )
        //{
            if( emu==null )
                emu = new EmailUtils();
            return emu;
        //}
        
        //return (EmailUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "emailUtils" );
    }


    public void setEmailFacade()
    {
    	if( emailerFacade == null )
    		emailerFacade = EmailerFacade.getInstance();
    }



    public void sendEmailToAdmin( String subj, String msg )
    {
        try
        {
            // prepare to send
            Map<String, Object> emailMap = new HashMap<>();

            emailMap.put( EmailConstants.MIME_TYPE , "text/plain" );

            emailMap.put( EmailConstants.SUBJECT, subj );

            msg = EmailUtils.addNoReplyMessage(msg, false, Locale.US );                        
            emailMap.put( EmailConstants.CONTENT, msg );

            emailMap.put( EmailConstants.TO, RuntimeConstants.getStringValue("system-admin-email") );

            // emailMap.put( EmailUtils.FROM, Constants.SUPPORT_EMAIL + "|" + MessageFactory.getStringMessage( locale , "g.SupportEmailKey", null ) );
            emailMap.put( EmailConstants.FROM, RuntimeConstants.getStringValue("no-reply-email") );

            sendEmail( emailMap );

            Tracker.addEmailSent();
            // Tracker.addEmailToAdmin();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EmailUtils.sendEmailToAdmin(" + subj + ", msg=" + msg + " )" );
        }
    }
    
    
    /*
    public void sendSvcEmail( String toList, String subj, String content )
    {
    	try
    	{
    		LogService.logIt( "EmailUtils.sendSvcEmail( to=" + toList + ", subj=" + subj + ", content=" + content );

    		Map<String,Object> mp = new HashMap<>();


    		mp.put( TO, toList );
    		mp.put( FROM, Constants.SUPPORT_EMAIL ); // new InternetAddress( Constants.SUPPORT_EMAIL ) );
    		mp.put( SUBJECT, subj );
    		mp.put( CONTENT, content );
    		mp.put( MIME_TYPE, "text/plain" );

    		sendEmail( mp );
    	}

    	catch( Exception e )
    	{
    		LogService.logIt( e, "EmailUtils.sendSvcEmail to=" + toList + ", subj=" + subj + ", msg=" + content );
    	}
    }
    */

    public static boolean isNoReplyAddress( String a )
    {
        return a!=null && (a.toLowerCase().startsWith("no-reply") || a.toLowerCase().startsWith("noreply"));
    }

    public static String addNoReplyMessage( String content, boolean html, Locale locale )
    {
        if( content==null )
            content="";
        
        if( html )
            return content + "<p style=\"font-family: arial,calibri,sans-serif;width:600px;\">" + MessageFactory.getStringMessage(locale, "g.EmailBoxNotMonitoredRc", new String[]{RuntimeConstants.getStringValue("support-email")}) + "</p>";
        
        else
            return content + "\n\n" + MessageFactory.getStringMessage(locale, "g.EmailBoxNotMonitoredRc", new String[]{RuntimeConstants.getStringValue("support-email")});
    }
    
    
    
    /**
     * Sends an email message fia a JMS queue.
     *
     * @param messageInfoMap must have the following values: <pre>
     *
     *
     *      to          Email|Name,Email|Name,Email|Name
     *      cc          Email|Name,Email|Name,Email|Name
     *      bcc         Email|Name,Email|Name,Email|Name
     *      subject     String
     *      from        Email|Name
     *      mime        mime type. Defaults to text/plain supports text/html
     *      content     String, content of the message
     *      attachments ordered params starting at 0
     *         attach_bytes_0 - bytes    byte[]
     *         attach_mime_0 - mime     String
     *         attach_name_0 - filename String
     *
     *
     *
     * </pre>
     */
    public boolean sendEmail( Map<String,Object> messageInfoMap )
    {
        try
        {
            if( emailerFacade == null )
                emailerFacade = EmailerFacade.getInstance();

            return emailerFacade.sendEmail( messageInfoMap );
        }

        catch( Exception e )
        {
            LogService.logIt( e , "sendEmail()" );
            return false;
        }
    }


    public static String correctNameForSend( String name )
    {
        if( name==null || name.isBlank() )
            return name;
        return StringUtils.replaceStr(name, ",", "" );
    }

    
    public static String cleanEmailAddress( String em )
    {
        if( em==null )
            return em;
        
        return StringUtils.removeWhitespaceAndControlCharsPlusLowercase(em);
    }


    /**
     * This is a faces validation method.
     *
     * @param facesContext
     * @param component
     * @param value
     */
    public static void validateEmail(  FacesContext facesContext ,
                                       UIComponent component ,
                                       Object value )
    {
        try
        {
            if( value == null )
                throw new STException( "l.EmailRequired" );

            String email = (String) value;

            try
            {
                validateEmail( email );
            }

            catch( Exception e )
            {
                throw new STException( "l.EmailRequired" );
            }

            if( email.length() < 5 )
                throw new STException( "l.EmailRequired" );

            int index = email.indexOf( "@" );

            if( index < 1 || index >= email.length() - 3 )
                throw new STException( "l.EmailRequired" );

            int lastdotindex = email.lastIndexOf( "." );

            if( lastdotindex <= index || lastdotindex > email.length() - 2 )
                throw new STException( "l.EmailRequired" );

            // looks OK at this point.
        }

        catch( STException e )
        {
            // flag component as not valid
            ((UIInput) component).setValid( false );

            // create a FacesMessage
            FacesMessage fm = MessageFactory.getMessage( e.getKey() ,  e.getParams() );

            // place in FacesContext
            facesContext.addMessage( component.getClientId( facesContext ), fm );
        }

        catch( Exception e )
        {
            LogService.logIt( e , "validateEmail()" );
        }
    }



    public static boolean validateEmail( InternetAddress iAddr ) throws Exception
    {
        try
        {
            if( 1==1 )
                return EmailValidator.getInstance().isValid( iAddr.getAddress() );
            //LogService.logIt( "EmailUtils.validateEmail() email=" + iAddr.getAddress() );
            
            else 
               iAddr.validate();
        }

        catch( AddressException e )
        {
            LogService.logIt( "EmailUtils.validateEmail() Email found invalid: " + ( iAddr==null ? "null" : iAddr.getAddress()) );

            String[] params = new String[2];

            params[0] = iAddr.getAddress();

            params[1] = e.getMessage();

            throw new STException( "g.InvalidEmailAddress" , params );
        }

        catch( Exception e )
        {
            LogService.logIt( e , "EmailUtils.validateEmail() " + ( iAddr == null ? "address is null" : iAddr.getAddress() ) );

            return false;
        }

        return true;
    }



    public static boolean validateEmailNoErrors( String email )
    {
        try
        {
            return validateEmail( new InternetAddress( email ) );
        }

        catch( Exception e )
        {
            return false;
        }
    }



    public static boolean validateEmail( String email ) throws Exception
    {
        try
        {
            if( email==null )
                return false;
            
            return validateEmail( new InternetAddress( email ) );
        }

        catch( AddressException e )
        {
            String[] params = new String[2];

            params[0] = email;

            params[1] = e.getMessage();

            throw new STException( "g.InvalidEmailAddress" , params );
        }
    }


    public static boolean isValidSupportAddress( String addr )
    {
       try
       {
           if( addr == null || addr.length() == 0 )
               return false;

           if( addr.indexOf( "@" ) >= 0 )
               addr = addr.substring( 0 , addr.indexOf( "@" ) );

           if( addr.length() == 0 )
               return false;

           for( String t : EmailConstants.VALID_SUPPORT_ADDRESSES )
           {
               if( addr.equalsIgnoreCase( t ) )
                   return true;
           }

           return false;
       }

       catch( Exception e )
       {
           LogService.logIt( e , "EmailUtils.isValidSupportAddress() " + addr );

           return false;
       }
    }

    public static String getEmailAddressName( String emailAddress ) throws Exception
    {
        try
        {
            if( emailAddress == null || emailAddress.length() == 0 || emailAddress.indexOf( "@" ) <= 0 )
                return "";

            return emailAddress.substring( 0 , emailAddress.indexOf( "@" ) );
        }

        catch( Exception e )
        {
            LogService.logIt( e , "EmailUtils.getEmailAddressName( " + emailAddress + " ) " );

            throw new STException( e );
        }
    }

    
    public int sendEmailWithSingleAttachment( String subject, String content, String toEmailsCommaDelim, String mimeType, String attachMimeType, String attachFilename, byte[] attachBytes  )
    {
        try
        {
            if( attachBytes==null || attachBytes.length<=0)
                throw new Exception( "Attachment bytes is missing." );

            if( attachMimeType == null || attachMimeType.isEmpty() )
                throw new Exception( "Attachment MimeType is missing." );
            
            if( attachFilename == null || attachFilename.isEmpty() )
                throw new Exception( "Attachment Filename is missing." );
            
            
            if( content == null || content.isEmpty() )
                throw new Exception( "Content is missing." );
            
            if( subject==null || subject.isEmpty() )
                subject = "HR Avatar Internal Message ";
            
            Map<String, Object> emailMap = new HashMap<>();

            emailMap.put( SUBJECT, subject );
            emailMap.put( CONTENT, content );            
            emailMap.put( MIME_TYPE, mimeType==null || mimeType.isBlank() ? "text/plain" : mimeType );

            StringBuilder sb; //  = new StringBuilder();

            emailMap.put( TO, toEmailsCommaDelim  );

            sb = new StringBuilder();
            sb.append( RuntimeConstants.getStringValue("no-reply-email") );
            emailMap.put( FROM, sb.toString() );
            emailMap.put( OVERRIDE_BLOCK, "true" );

            emailMap.put( ATTACH_MIME + "0", attachMimeType );
            emailMap.put( ATTACH_FN + "0", attachFilename );
            emailMap.put( ATTACH_BYTES + "0", attachBytes );
            
            LogService.logIt("EmailUtils.sendEmailWithSingleAttachment() content=" + content + ", bytes attached.length=" + attachBytes.length );
            
            // EmailerFacade  emailerFacade = EmailerFacade.getInstance();

            sendEmail( emailMap );
            
            return 1;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "EmailUtils.sendEmailWithSingleAttachment() " );
            return 0;
        }

    }
    
    
    
}
