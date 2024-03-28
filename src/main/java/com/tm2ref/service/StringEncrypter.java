/*
 * Created on May 26, 2006
 *
 */
package com.tm2ref.service;

import com.tm2ref.global.RuntimeConstants;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;

import com.tm2ref.util.Base64Encoder;
import java.net.URLDecoder;


public class StringEncrypter
{
    public static final String  DESEDE_ENCRYPTION_SCHEME = "DESede";

    public static final String  DES_ENCRYPTION_SCHEME    = "DES";

    private KeySpec             keySpec;

    private SecretKeyFactory    keyFactory;

    private Cipher              cipher;

    private static final String UNICODE_FORMAT           = "UTF8";

    public StringEncrypter(String encryptionScheme) throws EncryptionException
    {
        this(encryptionScheme, RuntimeConstants.getStringValue("stringEncryptorKey"));
    }

    public StringEncrypter(String encryptionScheme, String encryptionKey)
            throws EncryptionException
    {
        if (encryptionKey == null)
            throw new IllegalArgumentException("encryption key was null");
        if (encryptionKey.trim().length() < 24)
            throw new IllegalArgumentException(
                    "encryption key was less than 24 characters");
        try
        {
            byte[] keyAsBytes = encryptionKey.getBytes(UNICODE_FORMAT);
            if (encryptionScheme.equals(DESEDE_ENCRYPTION_SCHEME))
            {
                keySpec = new DESedeKeySpec(keyAsBytes);
            } else if (encryptionScheme.equals(DES_ENCRYPTION_SCHEME))
            {
                keySpec = new DESKeySpec(keyAsBytes);
            } else
            {
                throw new IllegalArgumentException(
                        "Encryption scheme not supported: " + encryptionScheme);
            }
            keyFactory = SecretKeyFactory.getInstance(encryptionScheme);
            cipher = Cipher.getInstance(encryptionScheme);
        } catch (InvalidKeyException e)
        {
            throw new EncryptionException(e);
        } catch (UnsupportedEncodingException e)
        {
            throw new EncryptionException(e);
        } catch (NoSuchAlgorithmException e)
        {
            throw new EncryptionException(e);
        } catch (NoSuchPaddingException e)
        {
            throw new EncryptionException(e);
        }
    }

    public String encrypt(String unencryptedString) throws EncryptionException
    {
        if (unencryptedString == null || unencryptedString.trim().length() == 0)
            throw new IllegalArgumentException(
                    "unencrypted string was null or empty");
        try
        {
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cleartext = unencryptedString.getBytes(UNICODE_FORMAT);
            byte[] ciphertext = cipher.doFinal(cleartext);
            // Base64Encoder base64encoder = new Base64Encoder();
            return new String( Base64Encoder.encode( ciphertext ) );
        } catch (Exception e)
        {
            throw new EncryptionException(e);
        }
    }


    /*
    public String decrypt_OLD(String encryptedString) throws EncryptionException
    {
        if (encryptedString == null || encryptedString.trim().length() <= 0)
            throw new IllegalArgumentException(
                    "encrypted string was null or empty");
        try
        {
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] cleartext = Base64Encoder.decode( encryptedString );
            byte[] ciphertext = cipher.doFinal(cleartext);
            return bytes2String(ciphertext);
        } catch (Exception e)
        {
            throw new EncryptionException(e);
        }
    }
    */



    public String decrypt(String encryptedString) throws EncryptionException
    {
        if( encryptedString==null || encryptedString.isEmpty() )
            return null;

        String es = encryptedString;

        try
        {

            String out = decryptNoError( es );

            if( out!=null && !out.isEmpty() )
                return out;

            es = URLDecoder.decode(es,"UTF8" );

            es = es.replace('_', '+');

            es = es.replace('-', '/');

            es = es.replace('*', '=');

            out = decryptNoError( es );

            if( out!=null && !out.isEmpty() )
                return out;

            es = encryptedString + "*";

            out = decryptNoError( es );

            if( out!=null && !out.isEmpty() )
                return out;


            es = encryptedString + "=";

            out = decryptNoError( es );

            if( out!=null && !out.isEmpty() )
                return out;

            return null;

        }
        catch( Exception e )
        {
            LogService.logIt( e, "StringEncryptor.decrypt() encryptedString=" + encryptedString + ", es=" + es );

            return null;
        }

    }


    public String decryptNoError(String encryptedString) throws EncryptionException
    {
        if (encryptedString == null || encryptedString.trim().length() <= 0)
            return null;
         //   throw new IllegalArgumentException(
         //           "encrypted string was null or empty");
        try
        {
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] cleartext = Base64Encoder.decode( encryptedString );
            byte[] ciphertext = cipher.doFinal(cleartext);
            return bytes2String(ciphertext);
        } catch (Exception e)
        {
            LogService.logIt( "StringEncryptor.decryptNoError() NONFATAL ERROR " + encryptedString + ", " + e.toString() );
            return null;
            // throw new EncryptionException(e);
        }
    }

    /*
    public String decrypt(String encryptedString) throws EncryptionException
    {
        if (encryptedString == null || encryptedString.trim().length() <= 0)
            throw new IllegalArgumentException(
                    "encrypted string was null or empty");
        try
        {
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] cleartext = Base64Encoder.decode( encryptedString );
            byte[] ciphertext = cipher.doFinal(cleartext);
            return bytes2String(ciphertext);
        }

        catch (Exception e)
        {
                throw new EncryptionException(e);
        }
    }
    */

    private static String bytes2String(byte[] bytes)
    {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
        {
            stringBuffer.append((char) bytes[i]);
        }
        return stringBuffer.toString();
    }

    public static class EncryptionException extends Exception
    {
        public EncryptionException(Throwable t)
        {
            super(t);
        }
    }
}
