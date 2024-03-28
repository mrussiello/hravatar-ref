/*
 * Created on Jan 19, 2007
 *
 */
package com.tm2ref.service;

import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;



public class EncryptUtils
{
    public static StringEncrypter ENCRYPTER;
    public static StringEncrypter FILE_ENCRYPTER;

    public static synchronized void init()
    {
        if( ENCRYPTER!=null )
            return;
        
        try
        {
            ENCRYPTER = new StringEncrypter( StringEncrypter.DES_ENCRYPTION_SCHEME , RuntimeConstants.getStringValue("stringEncryptorKey") );
            FILE_ENCRYPTER = new StringEncrypter( StringEncrypter.DES_ENCRYPTION_SCHEME , RuntimeConstants.getStringValue("stringEncryptorKeyFileSafe") );            
        }
        catch( Exception e )
        {
            LogService.logIt(e, "EncryptUtils.init()" );
        }
    }

    public static String urlSafeEncrypt( long i ) throws Exception
    {
       if( EncryptUtils.ENCRYPTER==null )
           init();
       
        try
        {
            return urlSafeEncrypt( Long.toString( i ), EncryptUtils.ENCRYPTER );
        }
        catch( Exception e )
        {
            LogService.logIt( e ,  "EncryptUtils.urlSafeEncrypt( value=" + i + " ) "  );
            throw new STException( e );
        }
    }




   public static String urlSafeEncrypt( String s ) throws Exception
   {
       if( EncryptUtils.ENCRYPTER==null )
           init();
       
       try
       {
           return urlSafeEncrypt( s , EncryptUtils.ENCRYPTER );
       }
       catch( Exception e )
       {
           LogService.logIt( e ,  "EncryptUtils.urlSafeEncrypt( " + s + " ) "  );
           throw new STException( e );
       }
   }

    public static String urlSafeEncrypt( long i , StringEncrypter encrypter ) throws Exception
    {
        try
        {
            return urlSafeEncrypt( Long.toString( i ) , encrypter );
        }
        catch( Exception e )
        {
            LogService.logIt( e ,  "EncryptUtils.urlSafeEncrypt( value=" + i + " ) "  );
            throw new STException( e );
        }
    }

    /**

     * Encodes a string into a url-friendly base64 encoded string

     */
    public static String urlSafeEncrypt( String s , StringEncrypter encrypter ) throws Exception
    {
       if( EncryptUtils.ENCRYPTER==null )
           init();
       
        if( encrypter==null )
            encrypter = EncryptUtils.ENCRYPTER;
       
        try
        {
            String newStr = null;

            if( s != null )
                s = s.trim();

            if (s != null) {

                newStr = encryptString(s , encrypter );

                newStr = newStr.replace( '+', '_');
                newStr = newStr.replace( '/', '-');
                newStr = newStr.replace( '=', '*');
                newStr = newStr.replaceAll( ">", "");
                newStr = newStr.replaceAll( "<", "");
                newStr = newStr.replaceAll( "\n", "");
                newStr = newStr.replaceAll( "\r", "");
            }

            return newStr;
        }
        catch( Exception e )
        {
            LogService.logIt( e ,  "EncryptUtils.urlSafeEncrypt( value=" + s + " ) "  );
            throw new STException( e );
        }
    }




    /**
     * Encodes in a way that is filename / filesystem safe
     *
     * @param s
     * @return
     * @throws Exception
     */
    public static String fileSafeEncrypt( String s ) throws Exception
    {
        try
        {
            if( EncryptUtils.ENCRYPTER==null )
                init();
            
            if( s != null )
                s = s.trim();

            String newStr = urlSafeEncrypt( s , EncryptUtils.FILE_ENCRYPTER );
            newStr = newStr.replace( '*', '-');
            return newStr;
        }

        catch( STException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e ,  "EncryptUtils.fileSafeEncrypt( value=" + s + " ) "  );
            throw new STException( e );
        }
    }

    public static String fileSafeDecrypt( String s ) throws Exception
    {
        if( EncryptUtils.ENCRYPTER==null )
            init();
            
        try
        {
            if( s != null )
                s = s.trim();

            if( s != null )
            {
                s = s.replace( '-', '=');
                s = s.replace('_', '+');
                s = decryptString(s , EncryptUtils.FILE_ENCRYPTER );
            }

            return s;
        }
        catch( STException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e ,  "EncryptUtils.fileSafeDecrypt( value=" + s + " ) "  );
            throw new STException( e );
        }
    }



    /**
     * Encodes in a way that result can be used in a Javascript variable.
     *
     * @param s
     * @return encrypted string
     *
     * @throws Exception
     */
    public static String javascriptSafeEncrypt( String s ) throws Exception
    {
        if( EncryptUtils.ENCRYPTER==null )
            init();
        
        try
        {
            if( s != null )
                s = s.trim();

            String newStr = urlSafeEncrypt( s , EncryptUtils.FILE_ENCRYPTER );

            newStr = newStr.replace( '*', '_');

            newStr = newStr.replace( '-', '_');

            return newStr;
        }
        catch( STException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e ,  "fileSafeEncrypt( value=" + s + " ) "  );
            throw new STException( e );
        }
    }



    public static String urlSafeDecrypt( String s ) throws Exception
    {

        if( EncryptUtils.ENCRYPTER==null )
            init();
        
        //try
        //{
            if( s == null || s.length() == 0 )
                return s;

            return urlSafeDecrypt( s , EncryptUtils.ENCRYPTER );
        //}

        //catch( Exception e )
        //{
        //    LogService.logIt( e ,  "urlSafeDecrypt( " + s + " ) " + e.toString()  );

       //     throw new STException( e );
        //}
    }



    /**

     * Returns the original string given a url-converted base64 encoded string

     */

    public static String urlSafeDecrypt(String s , StringEncrypter encrypter ) throws Exception
    {
       if( EncryptUtils.ENCRYPTER==null )
           init();
       
        if( encrypter==null )
            encrypter = EncryptUtils.ENCRYPTER;
        
        String newStr = s;

        if (newStr != null)
        {
            newStr = newStr.trim();

            if( newStr.length() > 0 && newStr.length() % 4 == 3 )
                newStr += "*";

            newStr = newStr.replaceAll( "%2a", "*" );
            newStr = newStr.replaceAll( "%2A", "*" );
            newStr = newStr.replace('_', '+');
            newStr = newStr.replace('-', '/');
            newStr = newStr.replace('*', '=');
            newStr = decryptString(newStr , encrypter );
        }

        return newStr;
    }







    protected static String decryptString( String inStr , StringEncrypter encrypter ) throws Exception
    {
        if( inStr == null )
            return null;

        if( inStr.length() == 0 )
            return inStr;

        return encrypter.decrypt( inStr );
    }



    protected static String encryptString( String inStr , StringEncrypter encrypter ) throws Exception
    {
        try
        {
            if( inStr == null )
                return null;

            if( inStr.length() == 0 )
                return inStr;
            
            return encrypter.encrypt( inStr );
        }
        catch( Exception e )
        {
            LogService.logIt( e ,  "encryptString( " + inStr + " ) " + e.toString()  );
            throw new STException( e );
        }

    }


    public void LogIt( String message )
    {
        LogService.logIt( message );
    }
}
