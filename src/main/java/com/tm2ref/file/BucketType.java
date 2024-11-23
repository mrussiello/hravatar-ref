package com.tm2ref.file;

import com.tm2ref.global.RuntimeConstants;
import software.amazon.awssdk.regions.Region;

public enum BucketType
{
    CFMEDIA(1),
    USERUPLOAD(2),
    LVRECORDING(3),
    LVRECORDING_TEST(4),
    PROCTORRECORDING(5),
    PROCTORRECORDING_TEST(6),
    REFRECORDING(7),
    REFRECORDING_TEST(8),
    OV_PRO_RECORDING(9),
    CT5(10),
    CT5_TEST(11);


    private final int bucketTypeId;


    private BucketType( int p )
    {
        this.bucketTypeId = p;
    }


    public int getBucketTypeId()
    {
        return this.bucketTypeId;
    }




    public static BucketType getValue( int id )
    {
        BucketType[] vals = BucketType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getBucketTypeId() == id )
                return vals[i];
        }

        return null;
    }
    
    public boolean getUsesPublicReadAcl()
    {
        return equals( CFMEDIA );
    }
    

    public String getBucket()
    {
        if( equals( CFMEDIA ) )
            return RuntimeConstants.getStringValue( "awsBucket" );

        else if( equals( PROCTORRECORDING ) || equals( PROCTORRECORDING_TEST )  )
            return RuntimeConstants.getStringValue( "awsBucketProctorRecording" );

        else if( equals( REFRECORDING ) || equals( REFRECORDING_TEST )  )
            return RuntimeConstants.getStringValue( "awsBucketRefRecording" );

        else if( equals( LVRECORDING )  || equals( LVRECORDING_TEST ) )
            return RuntimeConstants.getStringValue( "awsBucketLvRecording" );
        
        else if( equals( CT5 ) || equals( CT5_TEST )  )
            return RuntimeConstants.getStringValue( "awsBucketCt5" );
        
        return RuntimeConstants.getStringValue( "awsBucketFileUpload" );
    }

    public Region getBucketRegion()
    {
        if( getBucketRegionId()==1 )
            return Region.US_EAST_1;
        if( getBucketRegionId()==12 )
            return Region.US_WEST_2;
        return Region.US_EAST_1;
    }
    
    public int getBucketRegionId()
    {
        if( equals( CFMEDIA ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionId" );

        else if( equals( PROCTORRECORDING ) || equals( PROCTORRECORDING_TEST ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionIdProctorRecording" );

        else if( equals( REFRECORDING ) || equals( REFRECORDING_TEST ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionIdRefRecording" );

        else if( equals( LVRECORDING )  || equals( LVRECORDING_TEST ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionIdLvRecording" );
        
        else if( equals( CT5 )  || equals( CT5_TEST ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionIdCt5" );
                
        return RuntimeConstants.getIntValue( "awsBucketRegionIdFileUpload" );
    }



    public String getBaseKey()
    {
        if( equals( CFMEDIA ) )
            return RuntimeConstants.getStringValue( "awsBaseKey" );

        else if( equals( LVRECORDING ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyLvRecording" );
        
        else if( equals( LVRECORDING_TEST ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyLvRecordingTest" );
        
        else if( equals( PROCTORRECORDING ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyProctorRecording" );

        else if( equals( PROCTORRECORDING_TEST ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyProctorRecordingTest" );
        
        else if( equals( REFRECORDING ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyRefRecording" );

        else if( equals( REFRECORDING_TEST ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyRefRecordingTest" );
        
        else if( equals( CT5 ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyCt5" );

        else if( equals( CT5_TEST ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyCt5Test" );
        
        return RuntimeConstants.getStringValue( "awsBaseKeyFileUpload" );
    }



}
