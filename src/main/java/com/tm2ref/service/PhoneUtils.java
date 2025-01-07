/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.service;

import com.tm2ref.email.SmsBlockFacade;
import com.tm2ref.entity.email.SmsBlock;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.util.GooglePhoneUtils;
import com.tm2ref.util.MessageFactory;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.net.URI;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class PhoneUtils
{
    private static Boolean TWILIO_ON = null;
    private static String TWILIO_SID = null;
    private static String TWILIO_AUTH = null;
    private static String MSG_STATUS_CALLBACK = null;


    // returns -3 if blocked full
    //         -2 if phone blocked temp
    //         -1 if phone invalid
    //         0 if not sent
    //         1 if sent
    public static int sendTextMessage( String to, String countryCode, Locale locale, String from, String msg) throws Exception
    {
        if( TWILIO_SID == null )
        {
            TWILIO_ON = RuntimeConstants.getBooleanValue( "twilio.textingon" );
            TWILIO_SID = RuntimeConstants.getStringValue( "twilio.sid" );
            TWILIO_AUTH = RuntimeConstants.getStringValue( "twilio.auhtoken" );
            MSG_STATUS_CALLBACK = RuntimeConstants.getStringValue( "twilio.msgstatuscallbackurl" );
        }

        if( from == null || from.isEmpty() )
        {
            if( RuntimeConstants.getBooleanValue( "twilio.useSandbox" ) )
                from = RuntimeConstants.getStringValue( "twilio.sandboxphonenumber" );

            else
                from = RuntimeConstants.getStringValue( "twilio.fromnumber" );
        }

        to = GooglePhoneUtils.getFormattedPhoneNumberE164(to, countryCode );

        if( to==null )
        {
            LogService.logIt( "PhoneUtils.sendTextMessage() AAA.1 TO phone number is null after E164 conversion. countryCode=" + countryCode + ", from=" + from + ", " + msg );
            return -1;            
        }
        
        try
        {
            if( !GooglePhoneUtils.isNumberValid(to, countryCode ) )
            {
                LogService.logIt("PhoneUtils.sendTextMessage() To number is not valid for country code using E164. Trying Intl. to=" + to + ", countryCode=" + countryCode + ", " + msg );
                to = GooglePhoneUtils.getFormattedPhoneNumberIntl(to, countryCode);
                // LogService.logIt("PhoneUtils.sendTextMessage() to number changed to to=" + to + " using Intl version, countryCode=" + countryCode + ", valid=" + GooglePhoneUtils.isNumberValid(to, countryCode ) );
                if( to==null )
                {
                    LogService.logIt( "PhoneUtils.sendTextMessage() BBB.3 TO phone number is null after E164 conversion. countryCode=" + countryCode + ", from=" + from + ", " + msg );
                    return -1;            
                }        
            }
            
            // Strip leading 0 off phone if needed.
            if( !GooglePhoneUtils.isNumberValid(to, countryCode ) && countryCode!=null && countryCode.equalsIgnoreCase("US") && to.indexOf("0")>=0 )
            {
                if( to.startsWith("+0"))
                    to = to.substring(2, to.length() );
                else if( to.startsWith("0"))
                    to = to.substring(1, to.length() );                
                to = GooglePhoneUtils.getFormattedPhoneNumberE164(to, countryCode );
                // LogService.logIt("PhoneUtils.sendTextMessage() to number changed to to=" + to + " uafter stripping leading numbers, countryCode=" + countryCode + ", valid=" + GooglePhoneUtils.isNumberValid(to, countryCode ) );                
            }
            
            if( !GooglePhoneUtils.isNumberValid(to, countryCode ) )
            {
                LogService.logIt("PhoneUtils.sendTextMessage() To phone number is invalid: " + to );
                return -1;
            }

            if( msg == null || msg.isEmpty() )
                throw new Exception( "No message: " + msg );

            if( locale==null )
                locale = Locale.US;
            
            SmsBlockFacade smsBlockFacade = SmsBlockFacade.getInstance();
            SmsBlock smsBlock = smsBlockFacade.getActiveSmsBlock(to);
            
            if( smsBlock!=null && smsBlock.getIsActiveBlock() )
            {                
                LogService.logIt("PhoneUtils.sendTextMessage() SMS Block found for: " + to +", smsBlock.getSmsBlockReasonId()=" + smsBlock.getSmsBlockReasonId() );
                return smsBlock.getSmsBlockReasonId()==1 ? -3 : -2;
            }
            
            msg += " " + MessageFactory.getStringMessage(locale, "g.SMSSenderId", new String[]{RuntimeConstants.getStringValue("default-site-name")} );
                        
            if( TWILIO_ON )
            {
                Twilio.init( TWILIO_SID, TWILIO_AUTH);
                
                String callbackUrl = MSG_STATUS_CALLBACK + GooglePhoneUtils.cleanPhoneNumberForBlock(to);
                
                Message message = Message.creator( new PhoneNumber(to), new PhoneNumber(from), msg ).setStatusCallback(URI.create(callbackUrl)).create();
                
                // LogService.logIt("PhoneUtils.sendTextMessage() SENT to=" + to + ", from=" + from + ", " + msg );
                
                if( message.getStatus()!=null && (message.getStatus().equals(Message.Status.FAILED) || message.getStatus().equals(Message.Status.UNDELIVERED)) )
                {
                    LogService.logIt("PhoneUtils.sendTextMessage() Message failed or undelivered. Creating block. message.getStatus()=" + message.getStatus().toString() + ", to=" + to + ", from=" + from + ", " + msg );
                    smsBlock = smsBlockFacade.createSmsBlock(to, false);
                    return smsBlock.getSmsBlockReasonId()==1 ? -3 : -2;
                }
                
                return 1;
            }

            else
            {
                LogService.logIt("PhoneUtils.sendTextMessage() to=" + to + ", from=" + from + ", " + msg + " TEXT NOT SENT, TEXTING TURNED OFF." );
                return 0;
            }
        }        
        catch( com.twilio.exception.ApiException e )
        {
            LogService.logIt("PhoneUtils.sendTextMessage() Twilio " + e.toString() + ", to=" + to + ", from=" + from + ", " + msg + ", Twilio Error Code: " + e.getCode() );
            if( e.getCode()==20021 || e.getCode()==20023 || e.getCode()==21202 || e.getCode()==21610 || e.getCode()==21211 || e.getCode()==21216 || e.getCode()==21217 || e.getCode()==21613 || e.getCode()==21614 || e.getCode()==21615 || e.getMessage().contains("Attempt to send to unsubscribed recipient") )
            {
                    createSmsBlock(to, true);
                    return -3;                
            }
            
            else if( e.getCode()==21203 || e.getCode()==21214 || e.getCode()==21215 || e.getCode()==21612 )
            {
                createSmsBlock(to, false);
                return -2;                                
            }
            return 0;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "PhoneUtils.sendTextMessage() to=" + to + ", from=" + from + ", " + msg );
            return 0;
            // throw new STException(e);
        }
    }
    
    public static SmsBlock createSmsBlock( String phoneNumber, boolean fullBlock )
    {
        try
        {
            SmsBlockFacade smsBlockFacade = SmsBlockFacade.getInstance();
            return smsBlockFacade.createSmsBlock(phoneNumber, fullBlock);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "PhoneUtils.createSmsBlock() phoneNumber=" + phoneNumber + ", fullBlock=" + fullBlock );
            return null;
        }
    }
    
    
    public static boolean hasActiveSmsBlock( String phoneNumber )
    {
        try
        {
            SmsBlockFacade smsBlockFacade = SmsBlockFacade.getInstance();
            return smsBlockFacade.getActiveSmsBlock(phoneNumber)!=null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "PhoneUtils.hasActiveSmsBlock phoneNumber=" + phoneNumber  );
            return false;
            
        }
    }

    public static String getSmsErrorLangKey( int errorCode )
    {
        switch (errorCode) {
            case 0:
                return "g.ErrorSmsSendUnknownSys";
            case -1:
                return "g.ErrorSmsSendInvalidPhone";
            case -2:
                return "g.ErrorSmsSendTempBlock";
            case -3:
                return "g.ErrorSmsSendFullBlock";
            default:
                return "g.ErrorSmsSendUnknownSys";
        }
    }
    
    
}
