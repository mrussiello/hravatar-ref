/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.file;


import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import java.io.InputStream;

/**
 *
 * @author Mike
 */
public class VirusUtils
{
    public static Boolean virusScanBeforeSaving = null;

    public static String virusScanHost = null;

    public static Integer virusScanPort = null;



    /**
     * Returns true if OK.
     *
     * @param directory
     * @param filename
     * @param bucketTypeId
     * @return
     * @throws Exception
     */
    public static boolean scanforVirusAndDelete( String directory, String filename, int bucketTypeId ) throws Exception
    {
        InputStream iss = null;
        try
        {
            if( isVirusScanRequired() )
            {
                String pathFn = directory + "/" + filename;

                iss = (InputStream) (new FileXferUtils()).getFileInputStream(directory, filename, bucketTypeId,true);

                //InputStream iss = (InputStream) fileO[0];

                //s3o =  fileO.length>1 ? (S3Object) fileO[1] : null;

                // FileInputStream fis = new FileInputStream( RuntimeConstants.getStringValue( "localFsRoot" ) + pathFn );

                boolean isOk = checkForVirus( iss, pathFn );

                iss.close();

                //if( s3o != null )
                //    s3o.close();

                if( !isOk )
                {
                    LogService.logIt( "FileXferUtils.scanforVirusAndDelete() deleting infected file. " + pathFn );

                    (new  FileXferUtils()).deleteFile( directory, filename, bucketTypeId,true );
                    // throw new Exception( "File contains a virus. Rejected." );
                }
                //else
                //    LogService.logIt( "FileXferUtils.scanforVirusAndDelete() File is OK: " + pathFn );

                return isOk;
            }
        }
        finally
        {
            if( iss!=null )
                iss.close();
        }
        return true;

    }


    private static boolean checkForVirus( InputStream iss, String pathAndFilename ) throws Exception
    {
        if( !isVirusScanRequired() )
            return true;

        if( iss.available()<=0 )
            return true;

        ClamAVChat clamAVChat = new ClamAVChat( virusScanHost , virusScanPort , iss , 0 );

        boolean isOk = clamAVChat.doScan();

        if( !isOk )
            LogService.logIt( "VirusUtils.checkForVirus() File "  + pathAndFilename + " contains the " + clamAVChat.getVirus() + " virus!!" );

        return isOk;
    }

    public static boolean isVirusScanRequired()
    {
        if( virusScanBeforeSaving == null )
            virusScanBeforeSaving = RuntimeConstants.getBooleanValue( "virusScanBeforeSaving" );

        if( virusScanHost == null )
            virusScanHost = RuntimeConstants.getStringValue( "virusScanHost" );

        if( virusScanPort == null )
            virusScanPort = RuntimeConstants.getIntValue( "virusScanPort" );

        return virusScanBeforeSaving;
    }



}
