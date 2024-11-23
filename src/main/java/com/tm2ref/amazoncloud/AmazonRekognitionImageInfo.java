/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.amazoncloud;

import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.file.BucketType;
import com.tm2ref.file.FileXferUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;


import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;

/**
 *
 * @author miker_000
 */
public class AmazonRekognitionImageInfo {
    
    private static Boolean useAwsForFileStorage;
    private static Boolean useAwsForProctoringFileStorage;
    private static String fileUploadDirBase;
    
    RcUploadedUserFile uuf;
    int thumbIndex;
    byte[] bytes;
    // S3Object s3Object;
    boolean useThumb = false;
    boolean forRemoteProctoring = true;
    
    public AmazonRekognitionImageInfo( RcUploadedUserFile uuf, boolean useThumb, int thumbIndex )
    {
        this.uuf = uuf;
        this.useThumb=useThumb;
        this.thumbIndex=thumbIndex;
        // this.forRemoteProctoring=forRemoteProctoring;
    }
    
    private static synchronized void init()
    {
        if( useAwsForFileStorage!=null )
            return;
        
        useAwsForProctoringFileStorage = true ;// RuntimeConstants.getBooleanValue( "useAwsTestFoldersForProctoring" );        
    }
    
    
    public String toString()
    {
        return "AmazonRekognitionImageInfo " + (uuf==null ? "UploadedUserFile is null" : uuf.toString()) + ", useThumb=" + this.useThumb + ", thumbIndex=" + this.thumbIndex + ", forRemoteProctoring=" + this.forRemoteProctoring;
    }

    
    private boolean usesS3()
    {
        init();
        return useAwsForProctoringFileStorage;
    }
    
    
    public Image getImage( FileXferUtils fileXfer )
    {
        init();
            
        S3Object s30 = null;
        try
        {
            if( usesS3() )
            {
                s30 = getS3Object();
                return Image.builder().s3Object(s30).build();
                // return new Image().withS3Object( s30 );
            }
            else
            {
                return Image.builder().bytes( getSdkBytes( fileXfer )).build();
                //return new Image().withBytes( getByteBuffer( fileXfer ));
            }
        }
        catch( Exception e )
        {
            
            LogService.logIt( e, "AmazonRekognitionImageInfo.getImage() " + toString() + ", useS3=" + usesS3() );
            return null;
        }
    }
    
        
    private SdkBytes getSdkBytes( FileXferUtils fileXfer )
    {
        return SdkBytes.fromByteArray(getBytes( fileXfer ) );
    }
    
    private String getFilename()
    {
        //LogService.logIt( "AmazonRecognitionImageInfo.getFilename() " + toString() );
        String fn = useThumb ? uuf.getThumbFilename() : uuf.getFilename();
        if( forRemoteProctoring && useThumb && thumbIndex>0 && fn!=null  )
        {
            if( fn.contains(".IDX.") )
                fn = fn.replace( ".IDX.", "." + thumbIndex + "." );
            //LogService.logIt( "AmazonRecognitionImageInfo.getFilename() corrected fn=" + fn );
        }
        return fn;
    }
    
    
    private byte[] getBytes( FileXferUtils fileXfer )
    {
        try
        {
            init();
            
            // this should never happen.
            if( usesS3() )
                return null;
            
            //String filename = useThumb ? uuf.getThumbFilename() : uuf.getFilename();            
            String directory = fileUploadDirBase + uuf.getDirectory();
            return fileXfer.getFile(directory, getFilename(), getBucketType().getBucketTypeId(), true );
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonRekognitionImageInfo.getBytes() " + toString() );
            return null;
        }
    }
    
    private BucketType getBucketType()
    {
        //if( forRemoteProctoring )
        //    return  RuntimeConstants.getBooleanValue( "useAwsTestFoldersForProctoring" ) ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING;
        return RuntimeConstants.getBooleanValue( "useAwsTestFoldersForProctoring" ) ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING;
        
    }
    
    private S3Object getS3Object()
    {
        init();
        
        if( !usesS3() )
            return null;
        
        BucketType bucketType = getBucketType();
        
        //if( forRemoteProctoring )
         //   bucketType =  RuntimeConstants.getBooleanValue( "useAwsTestFoldersForProctoring" ) ? BucketType.PROCTORRECORDING_TEST : BucketType.PROCTORRECORDING;
        String directory = uuf.getDirectory();
        
        if( directory.startsWith( "/" ) )
             directory = directory.substring( 1, directory.length() );        
         
        String key = bucketType.getBaseKey() + directory  + "/" + getFilename();
        
        if( key.startsWith("/"))
            key = key.substring(1,key.length());
        
        String bucket = bucketType.getBucket();
        
        // LogService.logIt( "AmazonRekognitionImageInfo.getS3Object() bucket=" + bucket + ", key=" + key );
        return S3Object.builder().name(key).bucket(bucket).build();
        
        // return new S3Object().withName( key ).withBucket( bucket );
    }
}
