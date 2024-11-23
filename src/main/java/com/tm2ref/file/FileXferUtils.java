package com.tm2ref.file;


import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;




// @Stateless
public class FileXferUtils
{
    public static Map<Region, S3Client> regionClientMap;
    // public static Map<Regions, TransferManager> regionTransferMap;

    //public static S3Client amazonS3Client;

    //public static TransferManager transferManager;

    public static Boolean useAws=null;

    public static char PATH_SEPARATOR = '\\';


    public static synchronized void init()
    {
        if( useAws==null )
            useAws =  RuntimeConstants.getBooleanValue( "useAwsForUploadedRefMedia" );
    }

    public static synchronized S3Client getS3Client( Region region, boolean force2Aws)
    {
        init();

        if( useAws != null && !useAws && !force2Aws )
            return null;

        if( regionClientMap==null )
            regionClientMap = new HashMap<>();

        if( region==null )
            region=Region.US_EAST_1;

        S3Client client = regionClientMap.get(region);

        if( client!=null )
            return client;

        AwsBasicCredentials creds = AwsBasicCredentials.builder().accessKeyId(RuntimeConstants.getStringValue( "awsAccessKey" )).secretAccessKey(RuntimeConstants.getStringValue( "awsSecretKey" )).build();
        StaticCredentialsProvider bac = StaticCredentialsProvider.create(creds );
        client = S3Client.builder().region(region).credentialsProvider(bac).build();

        //BasicAWSCredentials bac = new BasicAWSCredentials( RuntimeConstants.getStringValue( "awsAccessKey" ) , RuntimeConstants.getStringValue( "awsSecretKey" ) );
        //client = AmazonS3ClientBuilder.standard()
        //                .withRegion( region )
        //                .withCredentials(new AWSStaticCredentialsProvider(bac))
        //                .build();
        regionClientMap.put( region, client);
        return client;
    }




    public static void waitForAwsObject( String directory, String filename, int bucketTypeId ) throws Exception
    {
        try
        {
            init();
            
            if( !useAws )
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



    public static boolean fileExistsAws( String targetDirectory, String targetFilename, int bucketTypeId ) throws Exception
    {
        init();

        if( !useAws )
            return false;
        
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        // initAwsTransferManager();
        String targetKey = null;

        try 
        {
            if (targetDirectory.startsWith("/")) 
            {
                targetDirectory = targetDirectory.substring(1, targetDirectory.length());
            }

            targetKey = bucketType.getBaseKey() + targetDirectory + (targetFilename != null && targetFilename.length() > 0 ? "/" + targetFilename : "");

            HeadObjectRequest hor = HeadObjectRequest.builder().bucket(bucketType.getBucket()).key(targetKey).build();
            
            // LogService.logIt( "FileXferUtils.fileExistsAws " + targetKey + ", bucket=" + bucketType.getBucket() + ", baseKey=" + bucketType.getBaseKey() );
            HeadObjectResponse result = getS3Client(bucketType.getBucketRegion(), true).headObject(hor);
            
            LogService.logIt( "FileXferUtils.fileExistsAws " + targetKey + ", bucket=" + bucketType.getBucket() + ", baseKey=" + bucketType.getBaseKey() + ", result=" + result.toString() );
            return true;
        } 
        catch (S3Exception e) 
        {
            if (e.statusCode()==404) 
            {
                LogService.logIt("FileXferUtils.fileExistsAws() Object does not exist: " + targetKey + ", " + e.toString());
                return false;
            }            
            
            LogService.logIt(e, "FileXferUtils.fileExistsAws() " + targetKey);
            
            throw new STException(e);
        } 
        catch (Exception e) 
        {
            LogService.logIt(e, "FileXferUtils.fileExistsAws() " + targetKey);
            throw new STException(e);
        }

        /*
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

            S3Client cl = getS3Client(bucketType.getBucketRegion(), false );
            return cl.doesObjectExist(bucketType.getBucket(), key );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.fileExistsAws( directory=" + directory + ", filename=" + filename  + ", bucketTypeId="+ bucketTypeId +  " )" );
            throw new STException( e );
        }
        */
    }



    public static String getPresignedUrlAws( String directory, String filename, int bucketTypeId, String frcBaseKey, int minutes ) throws Exception
    {
        String key = null;

        init();

        if( !useAws )
            return null;
        
        try 
        {

            BucketType bucketType = BucketType.getValue(bucketTypeId);


            if (directory.startsWith("/")) {
                directory = directory.substring(1, directory.length());
            }

            // key = bucketType.getBaseKey() + directory + "/" + filename;
            key = ( frcBaseKey==null || frcBaseKey.isBlank() ? bucketType.getBaseKey() : frcBaseKey) + directory  + "/" + filename;

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketType.getBucket())
                .key(key)
                .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(minutes))
                .getObjectRequest(getObjectRequest)
                .build();

            AwsBasicCredentials creds = AwsBasicCredentials.builder().accessKeyId(RuntimeConstants.getStringValue( "awsAccessKey" )).secretAccessKey(RuntimeConstants.getStringValue( "awsSecretKey" )).build();            
            StaticCredentialsProvider bac = StaticCredentialsProvider.create(creds );

            S3Presigner presigner = S3Presigner.builder().credentialsProvider( bac )
                                    .region(bucketType.getBucketRegion())
                                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);
            String theUrl = presignedGetObjectRequest.url().toExternalForm();
            //LogService.logIt("FileXferUtils.getPresignedUrlAws() url=" + theUrl + ", key=" + key);                
            return theUrl;
        } 
        catch (Exception e) 
        {
            LogService.logIt(e, "FileXferUtils.getPresignedUrlAws() " + key);
            throw new STException(e);
        }

        /*
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

            S3Client amazonS3 = getS3Client(bucketType.getBucketRegion(), true ); // getAmazonS3Client(bucketType, bucketType.getBucketRegion());

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
        */

    }




    private void saveFileToAws( String directory, String filename, InputStream iss, long length, String contentType, int bucketTypeId ) throws Exception
    {
        init();

        if( !useAws )
            return;
        
        
        BucketType bucketType = bucketTypeId <= 0 ? BucketType.CFMEDIA : BucketType.getValue(bucketTypeId);

        String key = null;

        try 
        {
            if (directory.startsWith("/")) {
                directory = directory.substring(1, directory.length());
            }

            key = bucketType.getBaseKey() + directory + "/" + filename;
            // key = RuntimeConstants.getStringValue( "awsBaseKey" ) + directory  + "/" + filename;

            Map<String, String> metadata = new HashMap<>();

            // ObjectMetadata omd = new ObjectMetadata();
            PutObjectRequest.Builder porb = PutObjectRequest.builder().bucket(bucketType.getBucket()).key(key).metadata(metadata); //bucketType.getBucket(), key, iss, omd);

            if (length > 0) 
            {
                porb = porb.contentLength(length);
                // metadata.put("length", Integer.toString((int)length) );
                // omd.setContentLength(length);
            }

            FileContentType fct = FileContentType.getFileContentTypeFromContentType(contentType, filename );

            if(fct != null && (contentType == null || contentType.length() == 0) ) 
                contentType = fct.getBaseContentType();

            if (contentType != null && contentType.length() > 0) 
                porb = porb.contentType(contentType);

            if( fct!=null && fct.isText() )
                porb = porb.contentEncoding("UTF8");

            int maxAge = 0;
            if (fct != null) 
            {
                if (fct.isJavascript() || fct.isCss()) {
                    maxAge = 0;
                } else if (fct.isVideo() || fct.isAudio()) {
                    maxAge = 86400;
                } else if (fct.isImage()) {
                    maxAge = 3600;
                }
            }

            //omd.setCacheControl("no-cache");
            //omd.setHeader("Expires", 0 );
            porb = porb.cacheControl("max-age=" + maxAge);
            // omd.setCacheControl("max-age=" + maxAge);

            if (bucketType.getUsesPublicReadAcl()) 
            {
                porb.acl( ObjectCannedACL.PUBLIC_READ);
                // por.setCannedAcl(CannedAccessControlList.PublicRead);
            }


            PutObjectRequest por = porb.build();  // new PutObjectRequest(bucketType.getBucket(), key, iss, omd);

            PutObjectResponse response = getS3Client(bucketType.getBucketRegion(), false).putObject(por, RequestBody.fromInputStream(iss, length));

            LogService.logIt("FileXferUtils.saveFileToAws() with inputStream. Saving file " + key + " to bucket " + bucketType.getBucket() + ", response=" + response.toString() + ", contentType=" + contentType + ", length=" + length );
        } 
        catch (Exception e) 
        {
            LogService.logIt(e, "FileXferUtils.saveFileToAws() " + key);
            throw new STException(e);
        }
        
        /*
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
        */
    }


    public void deleteFileAws(String targetDirectory, String targetFilename, BucketType bucketType) throws Exception 
    {
        if (bucketType == null)
            bucketType = BucketType.CFMEDIA;
            
        deleteFileAws(targetDirectory, targetFilename, bucketType.getBucketTypeId(), bucketType.getBaseKey() );
    }

    public void deleteFileAws(String targetDirectory, String targetFilename, int bucketTypeId, String forceBaseKey ) throws Exception 
    {
        init();

        if( !useAws )
            return;
        
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        String targetKey = null;

        try 
        {
            if (targetDirectory.startsWith("/")) 
                targetDirectory = targetDirectory.substring(1, targetDirectory.length());

            if (forceBaseKey==null) 
                targetKey = bucketType.getBaseKey() + targetDirectory + (targetFilename != null && targetFilename.length() > 0 ? "/" + targetFilename : "");
            else 
                targetKey = forceBaseKey + targetDirectory + (targetFilename != null && targetFilename.length() > 0 ? "/" + targetFilename : "");

            DeleteObjectRequest dor = DeleteObjectRequest.builder().bucket(bucketType.getBucket()).key(targetKey).build();                            

            DeleteObjectResponse delResp = getS3Client(bucketType.getBucketRegion(), false).deleteObject(dor);

            LogService.logIt( "FileXferUtils.deleteFileAws " + targetKey + ", bucket=" + bucketType.getBucket() + ", baseKey=" + bucketType.getBaseKey() + ", result=" + delResp.toString() );
            // LogService.logIt( "FileXferUtils.deleteFileAws " + targetKey + ", bucket=" + bucketType.getBucket() + ", baseKey=" + bucketType.getBaseKey() );

            Thread.sleep(500);
        } 
        catch (Exception e) 
        {
            LogService.logIt(e, "FileXferUtils.deleteFileAws() WWW.1 Target key=" + targetKey + ", bucketTypeId=" + bucketTypeId);
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
                deleteFileAws( directory, filename, bucketTypeId, null );
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



    public InputStream getFileAwsIs(String directory, String filename, int bucketTypeId) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        return getFileAwsIs( directory,  filename, bucketType, bucketType.getBaseKey() );
    }

    public InputStream getFileAwsIs(String directory, String filename, BucketType bucketType, String forceBaseKey) throws Exception 
    {
        init();

        if( !useAws )
            return null;
        
        String key = null;

        try 
        {
            if (directory.startsWith("/")) 
            {
                directory = directory.substring(1, directory.length());
            }

            if (forceBaseKey == null) 
            {
                key = bucketType.getBaseKey() + directory + "/" + filename;
            } else {
                key = forceBaseKey + directory + "/" + filename;
            }

            S3Client client = getS3Client(bucketType.getBucketRegion(), false);

            GetObjectRequest objectRequest = GetObjectRequest.builder().key(key).bucket(bucketType.getBucket()).build();

            InputStream iss = client.getObject(objectRequest, ResponseTransformer.toInputStream() );
            return iss;
        } 
        catch (Exception e) 
        {
            LogService.logIt(e, "FileXferUtils.getFileAwsIs() " + key);
            throw new STException(e);
        }
    }
    
    /*
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

            S3Object s3o = getS3Client(bucketType.getBucketRegion(), true ).getObject( bucketType.getBucket(), key );

            return s3o; // .getObjectContent();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getFileAwsIs() " + key );
            throw new STException(e);
        }
    }
    */


    public InputStream getFileInputStream( String directory, String filename, int bucketTypeId, boolean force2Aws ) throws Exception
    {
        init();
        // LogService.logIt( "AwsFileUtils.getFileAwsIs() bucket=" + bucket + ", " + directory + ", " + filename + " useAws=" + useAws.booleanValue() );

        //String key = null;
        if( useAws || force2Aws )
        {
            return getFileAwsIs( directory,  filename,  bucketTypeId);
            //S3Object s3o = getFileAwsIs(  directory,  filename,  bucketTypeId );
            //return new Object[] { s3o.getObjectContent() , s3o };
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
        init();

        String key = null;

        BucketType bucketType = BucketType.getValue(bucketTypeId);

        try
        {
            if( useAws )
            {
                key = bucketType.getBaseKey() + directory  + "/" + filename;
                InputStream iss = getFileAwsIs( directory,  filename,  bucketType, null );
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedInputStream bin;
                try (BufferedOutputStream bout = new BufferedOutputStream (baos)) {
                    bin = new BufferedInputStream(iss );
                    int byte_;
                    while ((byte_=bin.read()) != -1)
                    {
                        bout.write(byte_);
                    }
                }
                bin.close();
                return baos.toByteArray();
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
        
        /*
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        init();

        String key = null;

        try
        {
            if( directory.startsWith( "/" ) )
                directory = directory.substring( 1, directory.length() );

            key = bucketType.getBaseKey() + directory  + "/" + filename;
            ByteArrayOutputStream baos;

            try (S3Object s3o = getS3Client(bucketType.getBucketRegion(), true ).getObject( bucketType.getBucket(), key) )
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
        */
    }
}
