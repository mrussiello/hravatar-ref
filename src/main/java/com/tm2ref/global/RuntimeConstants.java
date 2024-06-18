/*
 * Created on Dec 12, 2006
 *
 */
package com.tm2ref.global;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.tm2ref.service.LogService;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class RuntimeConstants
{
    // private static char pathSeparator = ' ';

    private static Map<String, Object> cache = null;

    public static boolean DEBUG = false;

    public static SecretKey sealedObjectSecretKey = null;
    /**
     * Init
     */
    static
    {
        TimeZone.setDefault( TimeZone.getTimeZone( "UTC" ) );

        cache = new TreeMap<>();

        cache.put( "services/email/mailon", true);

        cache.put( "services/log/logfilepattern", "/work/tm2ref2/log/tm2ref_%g_%u.log");

        cache.put(  "SupportEmail", "support@hravatar.com" );
        cache.put(  "CSCaseEmails", "sandy@hravatar.com,samantha@hravatar.com,mike@hravatar.com" );
        cache.put(  "SystemErrorEmails", "mike@hravatar.com" );


        cache.put( "propertiesFile", "/work/tm2ref2/zzapplication.conf" );
        cache.put( "secretsFile", "/work/hraconfig/hraglobals-test.conf" );

        cache.put( "httpsOK", true );
        cache.put( "httpsONLY", true );

        cache.put( "refHttpsDomain", "test.hravatar.com" );

        cache.put( "contactFormUrl", "https://www.hravatar.com/ta/help/candidate-contact-request.xhtml" );
        cache.put( "baselogourl", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_8x1715795136855.png" );
        cache.put( "baseiconurl", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_21x1717875839610.png" );

        cache.put( "baseprotocol", "https" );
        cache.put( "basedomain", "test.hravatar.com" );

        cache.put( "baseurl", "https://test.hravatar.com/tr" );
        cache.put( "baseadminurl", "https://www.hravatar.com/ta" );

        cache.put( "corssourceurl", "https://*.hravatar.com");

        cache.put( "filesroot", "/work/tm2ref2/files" );

        cache.put( "no-reply-email", "no-reply@hravatar.com" );        
        cache.put( "support-email", "support@hravatar.com" );        
        cache.put( "system-admin-email", "mike@hravatar.com" );        

        cache.put( "refappbasedomain", "test.hravatar.com" );

        cache.put( "overrideEventIdVerification", false );

        cache.put( "overrideFastDeviceParallelTestChecks", false );

        cache.put( "refappcontextroot", "tr" );

        cache.put( "defaultskinid", ( 1 ) );

        cache.put( "defaultcorpid", 67 );

        cache.put( "defaultorgid", ( 1 ) );

        cache.put( "defaultsuborgid", ( 0 ) );

        cache.put( "public-orgid", 17);
        cache.put( "public-suborgid", 16 );
        
        
        cache.put( "useAwsForUploadedRefMedia", true);

        cache.put( "useAwsMediaServer", true);


        cache.put( "awsBaseUrl", "https://cdn.hravatar.com/web" );
        cache.put( "awsBaseUrlHttps", "https://cdn.hravatar.com/web" );

        cache.put( "defaultMarketingAccountOrgId",  (int)( 24 ) );
        cache.put( "defaultMarketingAccountSuborgId",  (int)( 0 ) );
        cache.put( "defaultMarketingAccountAnonymousUserId",  (long)( 167 ) );


        cache.put( "uploadedUserFileBaseUrlHttps", "https://s3.amazonaws.com/ref-hravatar-com/refrecordings" );
        cache.put( "adminappbasuri", "https://www.hravatar.com/ta" );

        cache.put( "hraCompanyLogoSmall", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_1416868391352.png" );
        cache.put( "translogoimageurl" , "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_1429216573950.png" );
        cache.put( "ivrCustomTestAudioPlayIconUrl", "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_3x1517685008793.png" );
        cache.put( "ivrCustomTestAudioPlayIconUrlEmail", "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_3x1517685008793.png" );
        cache.put( "avCustomTestVideoPlayIconUrl", "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_2x1517685008793.png" );

        cache.put( "AUDIOCOMMENT_ICON_CONV_URL", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_5x1637604549041.png" );
        cache.put( "VIDEOCOMMENT_ICON_CONV_URL", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_3x1637604549021.png" );
        cache.put( "AUDIOCOMMENT_ICON_URL", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_2x1637604549013.png" );
        cache.put( "VIDEOCOMMENT_ICON_URL", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_4x1637604549031.png" );

        // Note - these are the credentials for S3 Administrator. No other services used by this application.
        cache.put( "awsAccessKey", "" );
        cache.put( "awsSecretKey", "" );

        // Note: This is the Amazon user S3AndAIAdministrator who has access to S3 and various AI tools like Polly and Translate and Rekognition
        cache.put( "awsAccessKeyRekognition", "" );
        cache.put( "awsSecretKeyRekognition", "" );
        cache.put( "awsRekognitionRegionId", (Integer)1 );
        
        cache.put( "useAwsTestFoldersForProctoring", false );

        cache.put( "awsS3BaseUrl", "https://s3.amazonaws.com/" );

        cache.put( "awsBucket", "cfmedia-hravatar-com" );
        cache.put( "awsBaseKey", "web/" );
        cache.put( "awsBucketRegionId", (Integer)1 );

        cache.put( "awsBucketFileUpload", "ful-hravatar-com" );
        cache.put( "awsBaseKeyFileUpload", "" );
        cache.put( "awsBucketRegionIdFileUpload", (Integer)1 );

        cache.put( "awsBucketProctorRecording", "rp-hravatar-com" );
        cache.put( "awsBaseKeyProctorRecording", "proctorrecordings/" );
        cache.put( "awsBaseKeyProctorRecordingTest", "proctorrecordingstest/" );
        cache.put( "awsBucketRegionIdProctorRecording", (Integer)1 );

        cache.put( "awsBucketRefRecording", "ref-hravatar-com" );
        cache.put( "awsBaseKeyRefRecording", "refrecordings/" );
        cache.put( "awsBaseKeyRefRecordingTest", "refrecordingstest/" );
        cache.put( "awsBucketRegionIdRefRecording", (Integer)1 );


        cache.put( "awsBucketLvRecording", "lv-hravatar-com" );
        cache.put( "awsBaseKeyLvRecording", "recordings/" );
        cache.put( "awsBaseKeyLvRecordingTest", "recordingstest/" );
        cache.put( "awsBucketRegionIdLvRecording", (Integer)1 );

        cache.put( "useAwsTempUrlsForMedia", true );
        cache.put( "awsTempUrlMinutes", ((Integer)15));
        cache.put( "mediaTempUrlSourcePath", "/suri" );

        cache.put( "mediaServerWebapp", "sm" );
        cache.put( "mediaServerDomain", "media.clicflic.com" );
        cache.put( "primaryMediaServerDomain", "media.clicflic.com" );
        cache.put( "mediaServerPort", (int) 80 );

        cache.put( "applicationSystemId", (int) 1201  );

        cache.put( "stringEncryptorKey",  "" );
        cache.put( "stringEncryptorKeyFileSafe",  "" );
        
               
        cache.put( "newRefStartsOK", true );

        cache.put( "newTwilioCallsOK", true );


        cache.put( "defaultfaviconuri", "/tr/favicon.ico" );
        cache.put( "defaultfaviconuri16", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_21x1717875839610.png" );
        cache.put( "defaultfaviconuri32", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_20x1717875839605.png" );
        cache.put( "defaultfaviconuri60", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_19x1717875839593.png" );

        cache.put( "fileUploadErrorEmails", "mike@hravatar.com" );
        cache.put( "dbmsErrorEmails", "mike@hravatar.com" );
        cache.put( "twilioErrorEmails", "mike@hravatar.com" );

        cache.put( "awsBucketFileUpload", "ful-hravatar-com" );

        cache.put( "localFsRoot", "/work/sm1/web" );

        cache.put( "userFileUploadBaseDir", "/hra" );

        cache.put( "virusScanBeforeSaving" , true );
        cache.put( "virusScanHost" , "localhost" );
        cache.put( "virusScanPort" , ( 3310 ) );

        cache.put( "lowCreditsBccEmails", "mike@hravatar.com,sandy@hravatar.com,samantha@hravatar.com" );

        cache.put( "autoRemindersOk", true );

        // tr/ce/rcidenc/orgid
        cache.put( "RefCheckCandBaseUrl" , "https://test.hravatar.com/tr/rce/" );

        // tr/re/rateridenc/orgid
        cache.put( "RefCheckRaterBaseUrl" , "https://test.hravatar.com/tr/rce/" );

        cache.put( "RefCheckResultsBaseUrl", "https://www.hravatar.com/ta/r-ref.xhtml");

        cache.put( "RefCheckContactPermissionRcItemIds", "178" );
        cache.put( "RefCheckContactRecruitingRcItemIds", "180" );
        cache.put( "RefCheckPriorRoleRcItemIds", "174" );

        cache.put( "RefCheckReferralsRequestRcItemIds", "999999" );


        // ////////////////////////////////////////////////////////////////////////////
        // REPORTING
        // ////////////////////////////////////////////////////////////////////////////

        cache.put( "DefaultRcReportPrehire", 81 );
        cache.put( "DefaultRcReportEmployee", 82 );
        cache.put( "DefaultRcReportEmployeeFbk", 83 );

        cache.put( "DefaultRcReportPrehire_en", 81 );
        cache.put( "DefaultRcReportEmployee_en", 82 );
        cache.put( "DefaultRcReportEmployeeFbk_en", 83 );

        cache.put( "StandardRefCheckReportClassPrehire", "com.tm2ref.custom.ct2.CT2RcReport" );
        cache.put( "StandardRefCheckReportClassEmployee", "com.tm2ref.custom.ct2.CT2RcReport" );

        cache.put( "ReportImagesBaseUrl", "https://cdn.hravatar.com/web/ref" );
        
        cache.put( "RcPdfDownloadUrl", "https://www.hravatar.com/ta/rcpdfdnld.pdf" );

        cache.put( "RcPdfInterQuesStarUrl", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_2x1602882693191.png" );



        // ////////////////////////////////////////////////////////////////////////////
        // TWILIO!!!!!
        // ////////////////////////////////////////////////////////////////////////////

        cache.put( "useTwilioDevelopmentNumber",  false );
        cache.put( "twilioDevelopmentNumber",  "7036353077" );
        cache.put( "twilioDevelopmentNumberFormatted",  "+1 703-635-3077" );

        //////////////////////////////////////////////////////////////////////////////
        // Twilio Texting Params
        //////////////////////////////////////////////////////////////////////////////


        cache.put( "sampleAudioIvrItemResponseId", ((long)85) );


        cache.put( "twilio.textingon", true );
        cache.put( "twilio.sid", "" );
        cache.put( "twilio.auhtoken", "" );
        cache.put( "twilio.fromnumber", "+17036353077" );
        cache.put( "twilio.msgstatuscallbackurl", "https://sim.hravatar.com/tb/msgwh/" );

        cache.put( "twilio.sandboxpin", "7760-3166" );

        cache.put( "twilio.useSandbox", false );
        cache.put( "twilio.sandboxphonenumber", "(415) 599-2671" );

        // ////////////////////////////////////////////////////////////////////////////
        // Org-specific CS Email addresses. Comma-separated.
        // ////////////////////////////////////////////////////////////////////////////
        cache.put( "AdditionalAffiliateSourceEmails_OrgId_107", "support@aseametrics.com" );




        //////////////////////////////////////////////////////////////////////////////
        // Free Geo IP
        //////////////////////////////////////////////////////////////////////////////


        cache.put( "IpStackAccessKey", "" );
        cache.put( "FreeGeoIpURI", "https://api.ipstack.com/" );

        // ////////////////////////////////////////////////////////////////////////////
        // ////////////////////////////////////////////////////////////////////////////
        // ////////////////////////////////////////////////////////////////////////////


        // load properties from file. File overlays everything.
        String propertiesFile = (String) cache.get( "propertiesFile" );
        if( propertiesFile != null && !propertiesFile.isBlank() )
            loadProperties( propertiesFile );

        propertiesFile = (String) cache.get( "secretsFile" );
        if( propertiesFile != null && !propertiesFile.isBlank() )
            loadProperties( propertiesFile );
        convertSecretsToSealedObjects();                
    }
    
    private static synchronized void convertSecretsToSealedObjects()
    {
        if( sealedObjectSecretKey!=null )
            return;
        
        try
        {
            sealedObjectSecretKey = KeyGenerator.getInstance("DES").generateKey();
            Cipher ecipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, sealedObjectSecretKey );

            substituteStringWithSealedObject( "secretsFile", ecipher );
            substituteStringWithSealedObject( "stringEncryptorKey", ecipher );
            substituteStringWithSealedObject( "stringEncryptorKeyFileSafe", ecipher );
            substituteStringWithSealedObject( "awsSecretKey", ecipher );
            substituteStringWithSealedObject( "awsSecretKeyRekognition", ecipher );
            substituteStringWithSealedObject( "twilio.auhtoken", ecipher );
            substituteStringWithSealedObject( "IpStackAccessKey", ecipher );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RuntimeConstants.convertSecretsToSealedObjects()" );
        }
    }
    
    private static String getStringValueFromSealedObject( String cacheKey, SealedObject so )
    {
        if( cacheKey==null || cacheKey.isBlank() )
            return null;
        
        try
        {
            if( so==null )
            {
                Object o = cache.get(cacheKey);
                if( o ==null )
                    return null;
                else if( o instanceof String )
                    return (String)o;
                else if( o instanceof SealedObject )
                    so = (SealedObject)o;
                else
                    throw new Exception( "Cache value for key=" + cacheKey + " is not a String or SealedObject: " + o.getClass().getName() );                
            }
            Cipher dcipher = Cipher.getInstance("DES");
            dcipher.init(Cipher.DECRYPT_MODE, sealedObjectSecretKey);
            return (String) so.getObject(dcipher);
        }
        catch( Exception e )
        {
            LogService.logIt("RuntimeConstants.getStringValueFromSealedObject() NONFATAL " + e.toString() + ", cacheKey=" + cacheKey ); 
        }
        return null;
    }
    
    private static void substituteStringWithSealedObject( String cacheKey, Cipher cipher )
    {
        try
        {
            if( cacheKey==null || cacheKey.isBlank() )
            {
                LogService.logIt( "RuntimeConstants.substituteStringWithSealedObject() cacheKey is invalid (null or empty). Skipping." );
                return;
            }
            
            Object o = cache.get(cacheKey );
            if( o==null )
                throw new Exception( "no entry found for CacheKey " + cacheKey );
            
            if( !(o instanceof String) )
                throw new Exception( "Value for CacheKey " + cacheKey + " is not a String. Class="  + (o.getClass().getName()) );
            
            SealedObject so = new SealedObject((String)o, cipher);
            cache.put( cacheKey, so );
            
        }
        catch( Exception e )
        {
            LogService.logIt("RuntimeConstants.substituteStringWithSealedObject() NONFATAL " + e.toString() + ", cacheKey=" + cacheKey );
        }
    }

    private static void loadProperties( String propertiesFile )
    {
        try
        {
            if( propertiesFile != null && !propertiesFile.isBlank() )
            {
                Properties props = new Properties();

                try (FileInputStream  fis = new FileInputStream( propertiesFile ))
                {
                    props.load( fis );
                }

                catch( Exception e )
                {
                    System.out.println( "ERROR Loading RuntimeConstants: " + e.toString() );
                }

                Enumeration propertyNames = props.propertyNames();

                String name = null;

                String strValue = null;

                Object currentValue = null;

                while( propertyNames.hasMoreElements() )
                {
                    name = (String) propertyNames.nextElement();

                    strValue = props.getProperty( name );

                    if( name != null && name.length() > 0 && strValue != null && strValue.length() > 0 )
                    {
                        currentValue = cache.get( name );

                        //if( currentValue == null )
                        //{
                        //    continue;
                       //     // cache.put( name, strValue );
                        //}

                        if( currentValue!=null )
                        {
                            if( currentValue instanceof Integer )
                                cache.put( name, Integer.parseInt( strValue ) );

                            else if( currentValue instanceof Float )
                                cache.put( name, Float.parseFloat(strValue ) );

                            else if( currentValue instanceof Long )
                                cache.put( name, Long.parseLong(strValue ) );

                            else if( currentValue instanceof Boolean )
                                cache.put( name, Boolean.parseBoolean(strValue ) );

                            else
                                cache.put( name, strValue );
                        }

                        // logIt( "Revised property from file: " + name + " : " + strValue );
                    }
                }
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RuntimeConstants.loadProperties() reading properties file=" + propertiesFile );
        }
    }


    public static String dumpAllValues()
    {
        StringBuilder sb = new StringBuilder( "RuntimeConstants:\n" );

        for( String name : cache.keySet() )
        {
            sb.append( name + "=" + ( cache.get( name ) ).toString() + "\n" );
        }

        try
        {
            LogService.init();
        }

        catch( Exception e )
        {
            System.out.println( "RuntimeConstants.dumpAllValues() " + e.toString() );
        }

        logIt( sb.toString() );

        return sb.toString();
    }

    public static boolean getHttpsOnly()
    {
        return RuntimeConstants.getBooleanValue("httpsONLY");
    }


    public static int[] getIntArray( String key, String delimiter )
    {
        List<Integer> ll = new ArrayList<>();

        int[] out = null;

        try
        {
            String s = getStringValue( key );

            if( s==null || s.isEmpty() )
                return new int[0];

            String[] tks = s.split( delimiter );

            for( String t : tks )
            {
                if( t!=null && !t.trim().isEmpty() )
                    ll.add( Integer.parseInt(t) );
            }

            out = new int[ll.size()];

            for( int i=0; i<ll.size(); i++ )
            {
                out[i] = ll.get(i).intValue();
            }
        }
        catch( NumberFormatException e )
        {
            LogService.logIt( e, "RuntimeConstants.getIntArray() key=" + key + ", delim=" + delimiter );
        }

        return out;
    }


    public static List<Integer> getIntList( String key, String delimiter ) throws Exception
    {
        List<Integer> idl = new ArrayList<>();
        int[] ial = RuntimeConstants.getIntArray(key, delimiter );
        for( int i : ial )
            idl.add( i );
        return idl;
    }


    public static boolean hasValueForKey( String theKey )
    {
        return cache!=null && cache.containsKey( theKey ) && cache.get( theKey )!=null;
    }

    /**
     * Gets a value from the environment. Returns null if not found.
     */
    public static Object getValue( String theKey )
    {

        return cache.get( theKey );
    }

    public static void setValue( String theKey, Object theValue )
    {
        cache.put( theKey, theValue );
    }

    /**
     * Gets a value from the environment. Returns null if not found.
     */
    public static String getStringValue( String theKey )
    {
        Object o = cache.get( theKey );
        
        if( o==null )
            return null;
        if( o instanceof SealedObject )
            return RuntimeConstants.getStringValueFromSealedObject(theKey, (SealedObject)o);
        
        return (String)o;
    }

    public static List<String> getStringList( String theKey )
    {
        String s1 = getStringValue( theKey );

        List<String> out = new ArrayList<>();

        for( String s : s1.split(",") )
        {
            if( s.isBlank() )
                continue;
            out.add(s.trim());
        }

        return out;
    }

    /**
     * Gets a value from the environment. Returns null if not found.
     */
    public static Boolean getBooleanValue( String theKey )
    {
        return (Boolean) cache.get( theKey );
    }

    public static Integer getIntValue( String theKey )
    {
        return (Integer) cache.get( theKey );
    }

    public static Long getLongValue( String theKey )
    {
        return (Long) cache.get( theKey );
    }

    /**
     * logs messages
     */
    private static void logIt( String message )
    {
        LogService.getLogger().fine( message );
    }

}
