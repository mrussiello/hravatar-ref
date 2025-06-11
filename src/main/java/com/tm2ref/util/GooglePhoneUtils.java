/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.ShortNumberInfo;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.Constants;
import com.tm2ref.service.LogService;


public class GooglePhoneUtils {
    
    
    public static boolean getIsCountryAllowedForSms( String countryCode )
    {
        if( countryCode==null || countryCode.isBlank() )
            return false;
        
        countryCode=countryCode.trim();
            
        for( String cc : Constants.SMS_OK_COUNTRIES ) 
        {
            if( cc.equalsIgnoreCase(countryCode))
                return true;
        }
        
        return false;
    }

    public static boolean getIsPhoneNumberAllowedForSms(String numberIn, Org org, User recip, User sender )
    {
        String defCc = null;
        try
        {
            if( org!=null && org.getInternationalSmsOk() )
                return true;
            
            if( recip!=null && recip.getCountryCode()!=null && !recip.getCountryCode().isBlank() )
                defCc = recip.getCountryCode();
            else if( sender!=null && sender.getCountryCode()!=null && !sender.getCountryCode().isBlank() )
                defCc = sender.getCountryCode();
            else if( org!=null && org.getHqCountry()!=null && !org.getHqCountry().isBlank() )
                defCc = org.getHqCountry();
            
            return getIsPhoneNumberAllowedForSms( numberIn, defCc );
        }
        catch( Exception e )
        {
            LogService.logIt( "GooglePhoneUtils.getIsPhoneNumberAllowedForSms() ERROR " + e.toString() + ", numberIn=" + numberIn + ", defCc=" + (defCc==null ? "null" : defCc) + ", orgId==" + (org==null ? "null" : org.getOrgId()) + ", recip=" + (recip==null ? "null" : recip.getUserId()) +  ", sender=" + (sender==null ? "null" : sender.getUserId()) );
            return false;
        }                
    }
    
    
    public static boolean getIsPhoneNumberAllowedForSms(String numberIn, String defaultCountryCode )
    {
        try
        {
            if( numberIn==null || numberIn.isBlank() )
                return getIsCountryAllowedForSms(defaultCountryCode);
            
            int cc = getCountryCodeFromPhoneNumber( numberIn, defaultCountryCode );
            if( cc>0 )
            {
                for( int c : Constants.SMS_OK_COUNTRY_CODES )
                {
                    if( cc==c )
                        return true;
                }
                return false;
            }
            
            return getIsCountryAllowedForSms(defaultCountryCode);
        }
        catch( Exception e )
        {
            LogService.logIt( "GooglePhoneUtils.getIsPhoneNumberAllowedForSms() ERROR " + e.toString() + ", numberIn=" + numberIn + ", defaultCountryCode=" + defaultCountryCode );
            return false;
        }                
    }

    
    
    public static int getCountryCodeFromPhoneNumber(String numberIn, String countryCode )
    {
        try
        {
            if( numberIn==null || numberIn.isBlank() )
                return 0;
            
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            PhoneNumber number = phoneUtil.parseAndKeepRawInput(numberIn, countryCode);
            
            int cc = number.getCountryCode();
            
            return cc;
        }
        catch( NumberParseException e )
        {
            // LogService.logIt( "GooglePhoneUtils.isNumberValid() NumberParseException. numberIn=" + numberIn + ", countryCode=" + countryCode + ", " + e.getMessage() + ", " + e.toString() + ", " + e.getErrorType().toString() );
            return 0;            
        }
        catch( Exception e )
        {
            LogService.logIt( "GooglePhoneUtils.isNumberValid() ERROR " + e.toString() + ", numberIn=" + numberIn + ", countryCode=" + countryCode );
            return 0;
        }        
        
    }
    
    
    public static boolean isNumberValid( String numberIn, String countryCode )
    {
        try
        {
            if( numberIn==null || numberIn.isBlank() )
                return false;
            
            if( countryCode==null || countryCode.isBlank() )
                return isValidPhoneBasic( numberIn );
            
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            PhoneNumber number = phoneUtil.parseAndKeepRawInput(numberIn, countryCode);
            
            boolean isPossible = phoneUtil.isPossibleNumber(number);
            
            if( !isPossible )
            {
                LogService.logIt( "GooglePhoneUtils.isNumberValid() AAA numberIn=" + numberIn + ", countryCode=" + countryCode + ", isPossible=" + isPossible );
                if( countryCode.equalsIgnoreCase("US") && ( numberIn.startsWith("0") || numberIn.startsWith("+0") ) )
                {
                    numberIn = numberIn.substring( numberIn.indexOf("0")+1, numberIn.length() );
                    return isNumberValid( numberIn, countryCode );
                }

                return false; // isValidPhoneBasic( numberIn );
            }
            
            boolean valid = phoneUtil.isValidNumber(number);
            
            return valid;
            
            // if( !valid )
            //     return isValidPhoneBasic( numberIn );
            
            // return true;
        }
        catch( NumberParseException e )
        {
            LogService.logIt( "GooglePhoneUtils.isNumberValid() NumberParseException " + e.toString() + ", numberIn=" + numberIn + ", countryCode=" + countryCode );
            return false;            
        }
        catch( Exception e )
        {
            LogService.logIt( "GooglePhoneUtils.isNumberValid() Error: " + e.toString() + ", numberIn=" + numberIn + ", countryCode=" + countryCode );
            return false;
        }        
    }
    
    
    public static String getFormattedPhoneNumberE164( String numberIn, String countryCode )
    {
        return getFormattedPhoneNumber( numberIn, countryCode, PhoneNumberFormat.E164 );
    }
    
    public static String getFormattedPhoneNumberIntl( String numberIn, String countryCode )
    {
        return getFormattedPhoneNumber( numberIn, countryCode, PhoneNumberFormat.INTERNATIONAL );
    }
    
    public static String getFormattedPhoneNumber( String numberIn, String countryCode, PhoneNumberFormat format )
    {
        try
        {
            if( numberIn==null || numberIn.isBlank() )
                return null;
            
            if( countryCode==null || countryCode.isBlank() )
                return cleanPhoneNumberBasic( numberIn );
            
            numberIn = PhoneNumberUtil.convertAlphaCharactersInNumber( numberIn );
            
            
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            PhoneNumber number = phoneUtil.parseAndKeepRawInput(numberIn, countryCode);
            
            boolean isPossible = phoneUtil.isPossibleNumber(number);
            if( !isPossible )
            {
                LogService.logIt( "GooglePhoneUtils.getFormattedPhoneNumber() AA.1A NOT POSSIBLE numberIn=" + numberIn + ", countryCode=" + countryCode + ", isPossible=" + isPossible );
                if( countryCode.equalsIgnoreCase("US") && (numberIn.startsWith("0") || numberIn.startsWith("+0") ) )
                {
                    numberIn = numberIn.substring( numberIn.indexOf("0")+1, numberIn.length() );
                    number = phoneUtil.parseAndKeepRawInput(numberIn, countryCode);            
                    isPossible = phoneUtil.isPossibleNumber(number);
                    LogService.logIt( "GooglePhoneUtils.getFormattedPhoneNumber() AA.1B NOT POSSIBLE numberIn CHANGED to " + numberIn + ", countryCode=" + countryCode + ", isPossible=" + isPossible );                    
                }                
            }
            
            if( !isPossible )
            {
                ShortNumberInfo shortInfo = ShortNumberInfo.getInstance();
                boolean isPossibleShort = shortInfo.isPossibleShortNumber(number);
                LogService.logIt( "GooglePhoneUtils.getFormattedPhoneNumber() DD.3 numberIn=" + numberIn + ", countryCode=" + countryCode + ", isPossible=" + isPossible + ", isPossibleShort=" + isPossibleShort );
                return cleanPhoneNumberBasic( numberIn );
            }
            
            boolean isValid = phoneUtil.isValidNumber(number);
            String region = phoneUtil.getRegionCodeForNumber(number);
            
            String formatted = phoneUtil.format(number, format );
            

            if( isValid )
            {
                //LogService.logIt( "GooglePhoneUtils.getFormattedPhoneNumber() DD.4 returning " + formatted );
                return formatted;
            }
            else
                LogService.logIt( "GooglePhoneUtils.getFormattedPhoneNumber() DD.4 Number is not valid. DD.2 numberIn=" + numberIn + ", formatted=" + formatted + ", countryCode=" + countryCode + ", isPossible=" + isPossible + ", isValid=" + isValid + ", reqion=" + region );
            
        }
        catch( NumberParseException e )
        {
            LogService.logIt( "GooglePhoneUtils.getFormattedPhoneNumber() EE.1 NumberParseException numberIn=" + numberIn + ", countryCode=" + countryCode );
            //return false;            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "GooglePhoneUtils.getFormattedPhoneNumber() EE.2 numberIn=" + numberIn + ", countryCode=" + countryCode );
        }
        return cleanPhoneNumberBasic( numberIn );            
    }    
    
    // A number is only valid if it has a country code
    public static boolean isValidPhoneBasic( String inStr )
    {
        if( inStr == null || inStr.isEmpty() )
            return false;

        inStr = inStr.trim();

        inStr = PhoneNumberUtil.convertAlphaCharactersInNumber( inStr );
        
        char ch;

        for( int i=0;i<inStr.length(); i++ )
        {
            ch = inStr.charAt(i);

            if( i == 0 && ch == '+' )
                continue;

            if( ch == '(' || ch == ')' || ch=='-' || ch==' ' )
                continue;

            if( Character.isDigit( ch ) )
                continue;

            return false;
        }

        String clean = cleanPhoneNumberBasic( inStr );

        if( clean.indexOf( '+' ) < 0 && clean.length() <= 0 )
            return false;

        if( clean.indexOf( '+' ) == 0 && clean.length() <= 1 )
            return false;

        return true;
    }
    
    
    
    
    public static String cleanPhoneNumberBasic( String inStr )
    {
        // LogService.logIt( "GooglePhoneUtils.cleanPhoneNumberBasic() Start " + inStr );
        if( inStr == null || inStr.isEmpty() )
            return "";

        inStr = inStr.trim();

        char ch;

        String out = "";

        if( inStr.startsWith( "+" ) )
            out += "+";

        for( int i=0;i<inStr.length(); i++ )
        {
            ch = inStr.charAt(i);

            if( ch == '(' || ch == ')' )
                continue;

            if( Character.isDigit( ch ) || ch==' ' )
                out += ch;
        }

        return out;
    }
    
    /*
     Numbers only
    */
    public static String cleanPhoneNumberForBlock( String inStr )
    {
        if( inStr == null || inStr.isEmpty() )
            return "";

        inStr = inStr.trim();

        char ch;

        String out = "";

        for( int i=0;i<inStr.length(); i++ )
        {
            ch = inStr.charAt(i);

            if( Character.isDigit( ch ) )
                out += ch;
        }

        return out;
    }
    
    
}
