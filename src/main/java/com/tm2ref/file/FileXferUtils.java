package com.tm2ref.file;


import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;




// @Stateless
public class FileXferUtils
{
    public static Map<Regions, AmazonS3> regionClientMap;
    public static Map<Regions, TransferManager> regionTransferMap;
    
    //public static AmazonS3 amazonS3Client;

    //public static TransferManager transferManager;

    public static Boolean useAws=null;

    public static char PATH_SEPARATOR = '\\';


    public static synchronized TransferManager getAmazonTransferManager( Regions region, boolean force2Aws)
    {
        if( region==null )
            region=Regions.US_EAST_1;

        AmazonS3 client = getAmazonS3Client(region, force2Aws );
        if( client==null )
            return null;
        
        if( regionTransferMap==null )
            regionTransferMap = new HashMap<>();
        
        TransferManager tm = regionTransferMap.get( region );
        if( tm!=null )
            return tm;
        
        tm = TransferManagerBuilder.standard().withS3Client(client).build();        
        regionTransferMap.put( region, tm);
        return tm;
    }
    
    public static synchronized AmazonS3 getAmazonS3Client( Regions region, boolean force2Aws)
    {
        if( useAws==null )
            useAws =  RuntimeConstants.getBooleanValue( "useAwsForUploadedRefMedia" );

        if( useAws != null && !useAws && !force2Aws )
            return null;
        
        if( regionClientMap==null )
            regionClientMap = new HashMap<>();
        
        if( region==null )
            region=Regions.US_EAST_1;
        
        AmazonS3 client = regionClientMap.get(region);
        
        if( client!=null )
            return client;
        
        BasicAWSCredentials bac = new BasicAWSCredentials( RuntimeConstants.getStringValue( "awsAccessKey" ) , RuntimeConstants.getStringValue( "awsSecretKey" ) );

        client = AmazonS3ClientBuilder.standard()
                        .withRegion( region )
                        .withCredentials(new AWSStaticCredentialsProvider(bac))
                        .build();
        regionClientMap.put( region, client);
        return client;
    }
    
   
    public static synchronized void init()
    {
        if( useAws==null )
            useAws =  RuntimeConstants.getBooleanValue( "useAwsForUploadedRefMedia" );        
    }


    
    public static void waitForAwsObject( String directory, String filename, int bucketTypeId ) throws Exception
    {
        try
        {
            if( !FileXferUtils.useAws )
                return;
            
            int count = 0;            
            while( count<200 )
            {
                Thread.sleep(500);
                if( fileExistsAws( directory, filename, bucketTypeId ) )
                    return;
                count++;
                
                if( count>30 && count%5==0 )
                    LogService.logIt( "FileXferUtils.waitForAwsObject() count is high. count=" + count + ", directory=" + directory + ", filename=" + filename  + ", bucketTypeId="+ bucketTypeId +  " )" );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.waitForAwsObject(directory=" + directory + ", filename=" + filename  + ", bucketTypeId="+ bucketTypeId +  " )" );
            throw new STException( e );
        }                
    }
    
    
    
    public static boolean fileExistsAws( String directory, String filename, int bucketTypeId ) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);
        init();
        
        String key = null;
        try
        {
            if( !FileXferUtils.useAws )
                throw new Exception( "Aws is off!" );

            if( directory.startsWith( "/" ) )
                directory = directory.substring( 1, directory.length() );

            key = bucketType.getBaseKey() + directory  + "/" + filename;

            AmazonS3 cl = getAmazonS3Client(bucketType.getBucketRegion(), false );
            return cl.doesObjectExist(bucketType.getBucket(), key );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.fileExistsAws( directory=" + directory + ", filename=" + filename  + ", bucketTypeId="+ bucketTypeId +  " )" );
            throw new STException( e );
        }                
    }

    

    public static String getPresignedUrlAws( String targetDirectory, String targetFilename, int bucketTypeId, String frcBaseKey, int minutes ) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        // initAwsTransferManager();

        String targetKey = null;

        try
        {
            if( targetDirectory.startsWith( "/" ) )
                targetDirectory = targetDirectory.substring( 1, targetDirectory.length() );

            targetKey = ( frcBaseKey==null || frcBaseKey.isBlank() ? bucketType.getBaseKey() : frcBaseKey) + targetDirectory  + "/" + targetFilename;

            LogService.logIt( "FileXferUtils.getPresignedUrlAws() " + bucketType.getBucket() + "/" + targetKey );
            
            // LogService.logIt( "FileXferUtils.fileExistsAws " + targetKey + ", bucket=" + bucketType.getBucket() + ", baseKey=" + bucketType.getBaseKey() );

            AmazonS3 amazonS3 = getAmazonS3Client(bucketType.getBucketRegion(), true ); // getAmazonS3Client(bucketType, bucketType.getBucketRegion());
            
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.MINUTE, minutes );
            java.util.Date expiration = cal.getTime();
            
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest( bucketType.getBucket(), targetKey)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);            
            
            LogService.logIt("FileXferUtils.getPresignedUrlAws() " + targetKey + ", minutes=" + minutes + ", " + url.toString() );            
            return url.toString();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getPresignedUrlAws() " + targetKey );
            throw new STException(e);
        }

    }
    



    private void saveFileToAws( String directory, String filename, InputStream iss, long length, String contentType, int bucketTypeId ) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        init();

        String key = null;

        try
        {
            if( directory.startsWith( "/" ) )
                directory = directory.substring( 1, directory.length() );

            key = bucketType.getBaseKey() + directory  + "/" + filename;

            ObjectMetadata omd = new ObjectMetadata();

            omd.setContentLength(length);

            FileContentType fct = FileContentType.getFileContentTypeFromContentType(contentType, filename);

            if( contentType == null || contentType.length()==0 )
            {
                if( fct != null )
                    contentType = fct.getBaseContentType();
            }

            if( contentType != null && contentType.length()> 0 )
                omd.setContentType( contentType );

            int maxAge=345600;
            if( fct!=null )
            {
                if( fct.isJavascript() || fct.isCss() )
                    maxAge=0;
                else if( fct.isVideo() || fct.isAudio() )
                    maxAge=345600;
                else if( fct.isImage())
                    maxAge=345600;
            }

            //omd.setCacheControl("no-cache");
            //omd.setHeader("Expires", 0 );
            omd.setCacheControl( "max-age=" + maxAge );

            PutObjectRequest por = new PutObjectRequest( bucketType.getBucket(), key , iss , omd );

            if( bucketType.getUsesPublicReadAcl() ) 
                por.setCannedAcl( CannedAccessControlList.PublicRead );

            Upload myUpload = getAmazonTransferManager(bucketType.getBucketRegion(), true ).upload( por );

            // LogService.logIt( "FileXferUtils.saveFileToAws() Saving file " + key + " to bucket " + RuntimeConstants.getStringValue( "awsBucket" ) );

            // force wait.
            while(myUpload.isDone() == false)
            {
                 Thread.sleep(500);
            }
        }
        catch( Exception e )
        {
                LogService.logIt( e, "FileXferUtils.saveFileToAws() " + key );
                throw new STException(e);
        }
    }



    private void deleteFileAws( String targetDirectory, String targetFilename, int bucketTypeId) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        init();
        String targetKey = null;

        try
        {
            if( targetDirectory.startsWith( "/" ) )
                targetDirectory = targetDirectory.substring( 1, targetDirectory.length() );

            targetKey = bucketType.getBaseKey() + targetDirectory   + (targetFilename != null && targetFilename.length()>0 ? "/" + targetFilename : "" );

            // LogService.logIt( "FileXferUtils.deleteFileAws " + targetKey );

            getAmazonS3Client(bucketType.getBucketRegion(), true ).deleteObject( bucketType.getBucket(), targetKey);

            Thread.sleep(500);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.deleteFileAws() " + targetKey );
            throw new STException(e);
        }
    }



    //private void saveFileToAws( String directory, String filename, InputStream iss, long length, String contentType, int bucketTypeId ) throws Exception
    public void saveFile( String directory, String filename, InputStream iss, String contentType, int fileSize, int bucketTypeId, boolean force2Aws ) throws Exception
    {
        init();

        try
        {
            if( useAws || force2Aws )
            {
                saveFileToAws(directory , filename, iss, fileSize, contentType, bucketTypeId );

                if( VirusUtils.isVirusScanRequired() )
                {
                    Thread.sleep( 200 );

                    waitForAwsObject( directory, filename,bucketTypeId);
                
                    boolean isOk = VirusUtils.scanforVirusAndDelete( directory, filename, bucketTypeId );
                    if( !isOk )
                    {
                        LogService.logIt( "FileXferUtils.saveFile - Via AWS -( directory=" + directory + ", filename=" + filename + " ) ClamAV Not OK. File contains a virus. Rejected." );
                        throw new STException( "", "ClamAV Not OK. File appears to contain a virus.");
                        // throw new Exception( "File contains a virus. Rejected." );
                    }
                }

                return;
            }
            
            throw new STException( "", "Local file storage not allowed for this app." );
        }
        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.saveFile( directory=" + directory + ", filename=" + filename + " )" );
            throw new STException( e );
        }
    }


    
    public void deleteFile( String directory, String filename, int bucketTypeId, boolean force2Aws ) throws Exception
    {
        // LogService.logIt( "FileXferUtils.deleteFile() " + directory + "/" + filename );

        init();

        try
        {
            if( useAws || force2Aws )
            {
                deleteFileAws( directory, filename, bucketTypeId );
                return;
            }
            
            throw new Exception( "Local File Delete not allowed for this App." );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.deleteFile( directory=" + directory + ", filename=" + filename + " )" );
            throw new STException( e );
        }
    }



    private S3Object getFileAwsIs( String directory, String filename, int bucketTypeId ) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        init();

        // LogService.logIt( "AwsFileUtils.getFileAwsIs() bucket=" + bucket + ", " + directory + ", " + filename + " useAws=" + useAws.booleanValue() );

        String key = null;

        try
        {
            if( directory.startsWith( "/" ) )
                directory = directory.substring( 1, directory.length() );

            key = bucketType.getBaseKey() + directory  + "/" + filename;

            // LogService.logIt( "AwsFileUtils.getFileAws() " + key );

            S3Object s3o = getAmazonS3Client(bucketType.getBucketRegion(), true ).getObject( bucketType.getBucket(), key );

            return s3o; // .getObjectContent();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getFileAwsIs() " + key );
            throw new STException(e);
        }
    }


    public Object[] getFileInputStream( String directory, String filename, int bucketTypeId, boolean force2Aws ) throws Exception
    {
        init();

        // LogService.logIt( "AwsFileUtils.getFileAwsIs() bucket=" + bucket + ", " + directory + ", " + filename + " useAws=" + useAws.booleanValue() );

        String key = null;

        if( useAws || force2Aws )
        {
            S3Object s3o = getFileAwsIs(  directory,  filename,  bucketTypeId );
            return new Object[] { s3o.getObjectContent() , s3o };
        }

        throw new Exception( "Locale file storage not allowed for this app." );
    }



    
    public byte[] getFile( String directory, String filename, int bucketTypeId, boolean force2Aws ) throws Exception
    {
        init();

        try
        {

            if( useAws || force2Aws )
            {
                return getFileAws( directory, filename, bucketTypeId );
            }

            throw new Exception( "Local file storage not allowed for this app." );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getFile( directory=" + directory + ", filename=" + filename + " )" );
            throw new STException( e );
        }
    }
    


    private byte[] getFileAws( String directory, String filename, int bucketTypeId ) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        init();

        String key = null;

        try
        {
            if( directory.startsWith( "/" ) )
                directory = directory.substring( 1, directory.length() );

            key = bucketType.getBaseKey() + directory  + "/" + filename;
            ByteArrayOutputStream baos;

            try (S3Object s3o = getAmazonS3Client(bucketType.getBucketRegion(), true ).getObject( bucketType.getBucket(), key) )
            {
                InputStream iss = s3o.getObjectContent();
                baos = new ByteArrayOutputStream();
                BufferedOutputStream bout = new BufferedOutputStream (baos);
                BufferedInputStream bin = new BufferedInputStream(iss );
                int byte_;
                while ((byte_=bin.read()) != -1)
                {
                    bout.write(byte_);
                }
                bout.close();

                bin.close();
                iss.close();
                return baos.toByteArray();
            }
            catch( IllegalArgumentException ee )
            {
                LogService.logIt( "FileXferUtils.getFileAws() AAA.1 IllegalArgumentException Direct Key=" + key );
            }
            catch( Exception ee )
            {
                LogService.logIt(ee, "FileXferUtils.getFileAws() AAA.2 Direct Key=" + key );
            }
        }

        catch( IllegalArgumentException e )
        {
            LogService.logIt( "FileXferUtils.getFileAws() BBB.1 IllegalArgumentException Direct Key=" + key );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getFileAws() BBB.2 Upper Level. Key=" + key );

            throw new STException(e);
        }

        return new byte[0];
    }




    public static String removePathFromFilename( String filename )
    {
        if( filename == null || filename.length() == 0 )
            return filename;

        if( filename.indexOf( "/" ) >= 0 )
            filename = filename.substring( filename.lastIndexOf( "/" ) + 1, filename.length() );

        if( filename.indexOf( "\\" ) >= 0 )
            filename = filename.substring( filename.lastIndexOf( "\\" ) + 1, filename.length() );

        return filename;
    }

    public static String getFileExtension( String filename )
    {
        if( filename == null )
            return null;

        if( filename.lastIndexOf( "." ) <= 0 || filename.endsWith( "." ) )
            return null;

        return filename.substring( filename.lastIndexOf( "." ) + 1, filename.length() ).toLowerCase();
    }



}
