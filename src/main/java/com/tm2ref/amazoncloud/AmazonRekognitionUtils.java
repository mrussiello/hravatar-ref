/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.amazoncloud;

import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.file.FileXferUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.BoundingBox;
import software.amazon.awssdk.services.rekognition.model.CompareFacesMatch;
import software.amazon.awssdk.services.rekognition.model.CompareFacesRequest;
import software.amazon.awssdk.services.rekognition.model.CompareFacesResponse;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest;
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.FaceDetail;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Instance;
import software.amazon.awssdk.services.rekognition.model.InvalidParameterException;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.rekognition.model.OrientationCorrection;
import static software.amazon.awssdk.services.rekognition.model.OrientationCorrection.ROTATE_180;
import static software.amazon.awssdk.services.rekognition.model.OrientationCorrection.ROTATE_270;
import static software.amazon.awssdk.services.rekognition.model.OrientationCorrection.ROTATE_90;
import software.amazon.awssdk.services.rekognition.model.Parent;
import software.amazon.awssdk.services.rekognition.model.Pose;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.ThrottlingException;

/**
 *
 * @author miker_000
 */
public class AmazonRekognitionUtils {
    
    static float DEFAULT_SIMLILARITY_THRESHOLD = 40f;
    static float MIN_MULTIFACE_WID_AS_RATIO_OF_IMG_SIZE = 0.1f;
    static float MIN_MULTIFACE_HGT_AS_RATIO_OF_IMG_SIZE = 0.1f;
    static float MIN_MULTIFACE_CONFIDENCE = 60f;
    
    RekognitionClient rekognitionClient;
    FileXferUtils fileXfer;
    
    private synchronized void initClient() throws Exception
    {
        if( rekognitionClient!=null )
            return;
        
        try
        {
            AwsBasicCredentials creds = AwsBasicCredentials.builder().accessKeyId(RuntimeConstants.getStringValue( "awsAccessKeyRekognition" )).secretAccessKey(RuntimeConstants.getStringValue( "awsSecretKeyRekognition" )).build();            
            StaticCredentialsProvider bac = StaticCredentialsProvider.create(creds );
            rekognitionClient = RekognitionClient.builder().region(getClientRegion()).credentialsProvider(bac).build();
            
            //BasicAWSCredentials bac = new BasicAWSCredentials( RuntimeConstants.getStringValue( "awsAccessKeyRekognition" ) , RuntimeConstants.getStringValue( "awsSecretKeyRekognition" ) );
            //rekognitionClient = AmazonRekognitionClientBuilder
            //                        .standard()
            //                        .withRegion(getClientRegion() )
            //                        .withCredentials(new AWSStaticCredentialsProvider(bac))
            //                        .build();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonRekognitionUtils.initClient() " );
            throw e;
        }
    }
    
    public Region getClientRegion()
    {
        int rid = RuntimeConstants.getIntValue("awsRekognitionRegionId");
        
        if( rid==1 )
            return Region.US_EAST_1;
        if( rid==12 )
            return Region.US_WEST_2;
        return Region.US_EAST_1;
    }
    
    
    /*
     * Returns Object[]:
     *    data[0] = "SUCCESS" or "ERROR"
     *    data[1] = FLOAT -1 if no matches, 0 if matching error. highest match percentage for Success, 
                    Error Message for Error. 0-100
     *    data[2] = FLOAT confidence of highest match if there is a match. Otherwise 0 (0-1)
                    Null if Error
    */
    public Object[] compareThumbImages( RcUploadedUserFile uuf, int thumbIndex, RcUploadedUserFile uuf2, int thumbIndex2, boolean useThumbs, float similarityThreshold, boolean multipleComparesOk, int minMatches)
    {
        Object[] out = new Object[4];

        try
        {
            // LogService.logIt("AmazonRekognitionUtils.compareImages() uufId1=" + uuf.getUploadedUserFileId() + ", thumbIndex1=" + thumbIndex + ",  uufId2=" + uuf2.getUploadedUserFileId() + ", thumbIndex2=" + thumbIndex2 );

            if( similarityThreshold<=0 )
                similarityThreshold = DEFAULT_SIMLILARITY_THRESHOLD;

            initClient();

            AmazonRekognitionImageInfo arii = new AmazonRekognitionImageInfo( uuf, useThumbs, thumbIndex );

            AmazonRekognitionImageInfo arii2 = new AmazonRekognitionImageInfo( uuf2, useThumbs, thumbIndex2 );

            if( fileXfer == null )
                fileXfer = new FileXferUtils();

            Image src = arii.getImage(fileXfer);

            if( src==null )
                throw new Exception( "Unable to obtain RekognitionClient.Image for Image File 1" );

            Image tgt = arii2.getImage(fileXfer);

            if( tgt==null )
                throw new Exception( "Unable to obtain RekognitionClient.Image for Image File 2" );

            CompareFacesRequest req = CompareFacesRequest.builder().sourceImage(src).targetImage(tgt).similarityThreshold(similarityThreshold).build();
            
            //CompareFacesRequest req = new CompareFacesRequest()
            //   .withSourceImage(src)
            //   .withTargetImage(tgt)
            //   .withSimilarityThreshold(similarityThreshold);

            CompareFacesResponse res=rekognitionClient.compareFaces(req);

            float[] vals = getHighestSimilarity(res, multipleComparesOk, minMatches );

            // LogService.logIt( "AmazonRekognitionUtils.compareImages() highest similary=" + vals[0] + ", conf=" + vals[1] );

            out[0]="SUCCESS";
            out[1]=(float)(vals[0]);
            out[2]=(float)(vals[1]);
            return out;
        }
        catch( InvalidParameterException | ThrottlingException e )
        {
            // This is a weird way Amazon announces that one of the two images did not have a face. So treat it as a successful call that results in a failure.
            LogService.logIt("AmazonRekognitionUtils.compareImages() UNABLE TO PROCESS: Caught: " + e.toString() + " Indicates NO MATCH and that one image had no detectable faces. File1: " + (uuf==null ? "null" : uuf.toString()) + ", file2: " + (uuf2==null ? "null" : uuf2.toString()) );
            out[0]="SUCCESS";
            out[1]=(float)(-1);
            out[2]=(float)(0);
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AmazonRekognitionUtils.compareImages() File1: " + (uuf==null ? "null" : uuf.toString()) + ", file2: " + (uuf2==null ? "null" : uuf2.toString()) );
            out[0]="ERROR";
            out[1]=e.toString();
            return out;
        }

        /*
        Object[] out = new Object[4];
        
        try
        {
            // LogService.logIt("AmazonRekognitionUtils.compareImages() uufId1=" + uuf.getUploadedUserFileId() + ", thumbIndex1=" + thumbIndex + ",  uufId2=" + uuf2.getUploadedUserFileId() + ", thumbIndex2=" + thumbIndex2 );
            
            if( similarityThreshold<=0 )
                similarityThreshold = DEFAULT_SIMLILARITY_THRESHOLD;
            
            //if( uuf==null || !uuf.hasImageFile() )
            //    throw new Exception( "UploadedUserFile1 is invalid" );

            //if( uuf2==null || !uuf2.hasImageFile() )
            //    throw new Exception( "UploadedUserFile2 is invalid" );

            initClient();
            
            // float match = 100;
            
            AmazonRekognitionImageInfo arii = new AmazonRekognitionImageInfo( uuf, useThumbs, thumbIndex );
            
            AmazonRekognitionImageInfo arii2 = new AmazonRekognitionImageInfo( uuf2, useThumbs, thumbIndex2 );
               
            if( fileXfer == null )
                fileXfer = new FileXferUtils();
            
            Image src = arii.getImage(fileXfer);

            if( src==null )
                throw new Exception( "Unable to obtain RekognitionClient.Image for Image File 1" );
            
            Image tgt = arii2.getImage(fileXfer);
            
            if( tgt==null )
                throw new Exception( "Unable to obtain RekognitionClient.Image for Image File 2" );
                        
            CompareFacesRequest req = new CompareFacesRequest()
               .withSourceImage(src)
               .withTargetImage(tgt)
               .withSimilarityThreshold(similarityThreshold);

            CompareFacesResult res=rekognitionClient.compareFaces(req);
            
            float[] vals = getHighestSimilarity(res, multipleComparesOk, minMatches );
            
            // LogService.logIt( "AmazonRekognitionUtils.compareImages() highest similary=" + vals[0] + ", conf=" + vals[1] );
            
            out[0]="SUCCESS";
            out[1]=(float)(vals[0]);
            out[2]=(float)(vals[1]);
            return out;
        }        
        catch( InvalidParameterException e )
        {
            // This is a weird way Amazon announces that one of the two images did not have a face. So treat it as a successful call that results in a failure.
            LogService.logIt("AmazonRekognitionUtils.compareImages() Caught: " + e.toString() + " Indicates NO MATCH and that one image had no detectable faces. File1: " + (uuf==null ? "null" : uuf.toString()) + ", file2: " + (uuf2==null ? "null" : uuf2.toString()) ); 
            out[0]="SUCCESS";
            out[1]=(float)(-1);
            out[2]=(float)(0);
            return out;            
        }        
        catch( Exception e )
        {
            LogService.logIt(e, "AmazonRekognitionUtils.compareImages() File1: " + (uuf==null ? "null" : uuf.toString()) + ", file2: " + (uuf2==null ? "null" : uuf2.toString()) ); 
            out[0]="ERROR";
            out[1]=e.toString();
            return out;
        }
        */
    }
    
    /**
     * Returns 
     *    data[0] = -1 if there is an apparent error.
     *               0 if there are no matches. 
     *               similarity - if there are any matches. 
     * 
     * similarity = highest similarity if there is only one match. 
     *            = lowest similarity if multipleComparesOk is set.  
     * 
     * 
     * @param compareFacesResult
     * @return 
     */
    protected float[] getHighestSimilarity( CompareFacesResponse compareFacesResult, boolean multipleComparesOk, int minMatches)
    {
        float[] out = new float[2];

        if( compareFacesResult==null || compareFacesResult.faceMatches()==null )
        {
            out[0]=-1;
            return out;
        }

        List<CompareFacesMatch> faceDetails = compareFacesResult.faceMatches();

        if( !multipleComparesOk && faceDetails.size()>1 )
        {
            LogService.logIt("AmazonRekognitionUtils.getHighestSimilarity() Something is wrong with the photos - there are " + faceDetails.size() + " different matches. Should only be one. multipleComparesOk=" + multipleComparesOk );
            //out[0]=-1;
            //return out;
        }

        // LogService.logIt( "AmazonRekognitionUtils.getHighestSimilarity() matches size=" + faceDetails.size() );

        if( multipleComparesOk && faceDetails.size()<minMatches )
        {
            LogService.logIt( "AmazonRekognitionUtils.getHighestSimilarity() there are less than the expected number of matches. Matches=" + faceDetails.size() + ", minMatches=" + minMatches );
            out[0]=-1;
            return out;
        }

        boolean useLowestMatch = multipleComparesOk && faceDetails.size()>1;

        float highestMatch = useLowestMatch ? 999 : 0;
        float conf = 0;

        for( CompareFacesMatch m : faceDetails )
        {
            float sim = m.similarity();

            // LogService.logIt( "AmazonRekognitionUtils.getHighestSimilarity() Checking matches. similarity=" + sim );
            if( !useLowestMatch && highestMatch<sim )
            {
                highestMatch = sim;
                conf = m.face()==null ? 1 : m.face().confidence();
            }
            else if( useLowestMatch && highestMatch>sim )
            {
                highestMatch = sim;
                conf = m.face()==null ? 1 : m.face().confidence();
            }
        }

        if( useLowestMatch && highestMatch>100 )
            highestMatch=0;

        out[0]=highestMatch;
        out[1]=conf;

        return out;
        
        /*
        float[] out = new float[2];
        
        if( compareFacesResult==null || compareFacesResult.getFaceMatches()==null )
        {
            out[0]=-1;
            return out;
        }
        
        List<CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches();
        
        if( !multipleComparesOk && faceDetails.size()>1 )
        {
            LogService.logIt("AmazonRekognitionUtils.getHighestSimilarity() Something is wrong with the photos - there are " + faceDetails.size() + " different matches. Should only be one. multipleComparesOk=" + multipleComparesOk );
            //out[0]=-1;
            //return out;
        }
        
        // LogService.logIt( "AmazonRekognitionUtils.getHighestSimilarity() matches size=" + faceDetails.size() );
        
        if( multipleComparesOk && faceDetails.size()<minMatches )
        {
            LogService.logIt( "AmazonRekognitionUtils.getHighestSimilarity() there are less than the expected number of matches. Matches=" + faceDetails.size() + ", minMatches=" + minMatches );            
            out[0]=-1;
            return out;
        }
        
        boolean useLowestMatch = multipleComparesOk && faceDetails.size()>1;
        
        float highestMatch = useLowestMatch ? 999 : 0;
        float conf = 0;
        
        for( CompareFacesMatch m : faceDetails )
        {
            float sim = m.getSimilarity();
            
            LogService.logIt( "AmazonRekognitionUtils.getHighestSimilarity() Checking matches. similarity=" + sim );
            if( !useLowestMatch && highestMatch<sim )
            {
                highestMatch = sim;
                conf = m.getFace()==null ? 1 : m.getFace().getConfidence();
            }
            else if( useLowestMatch && highestMatch>sim )
            {
                highestMatch = sim;
                conf = m.getFace()==null ? 1 : m.getFace().getConfidence();
            }
        }
        
        if( useLowestMatch && highestMatch>100 )
            highestMatch=0;
        
        out[0]=highestMatch;
        out[1]=conf;
        
        return out;
        */
    }

    /*
     Data[0] == SUCCESS or ERROR
     data[1] = null or FaceDetail for success and only one face present, message for Error
     data[2]= null, 0, or orientation. Orientation=0 good, XX=XX degrees counterclockwise (must rotate XX clockwise)
     data[3]= null or number of faces detected. If there's someone standing behind you there would be two faces. If there is an ID Card and your face is in the picture that is two faces.
     data[4]= List<FaceDetail>
    */
    public Object[] getImageFaceDetails( RcUploadedUserFile uuf, boolean useThumbImage, int thumbIndex, boolean forRemoteProctoring)
    {
        Object[] out = new Object[5];

        out[2] = ((int) 0);
        out[3] = ((int) 0);

        try
        {
            if( uuf==null ||
                //(!useThumbImage && !uuf.hasImageFile()) ||
                (useThumbImage && ( uuf.getThumbFilename()==null || uuf.getThumbFilename().isBlank())) )
                throw new Exception( "UploadedUserFile1 is invalid" );

            initClient();

            AmazonRekognitionImageInfo arii = new AmazonRekognitionImageInfo( uuf, useThumbImage, thumbIndex );

            if( fileXfer == null )
                fileXfer = new FileXferUtils();

            Image src = arii.getImage(fileXfer);

            if( src==null )
                throw new Exception( "Unable to obtain RekognitionClient.Image for Image File 1" );

            DetectFacesRequest  req = DetectFacesRequest.builder().image(src).attributes( Attribute.ALL ).build();

            DetectFacesResponse  res=rekognitionClient.detectFaces(req);

            OrientationCorrection ocor = res.orientationCorrection();
            String ocorStr = res.orientationCorrectionAsString();
            
            FaceDetail faceDetail=null;
            if( !res.faceDetails().isEmpty() )
            {
                faceDetail=res.faceDetails().get(0);                
            }
            
            Float roll = null;
            if( faceDetail!=null )
            {
                Pose pose = faceDetail.pose();
                if( pose!=null )
                    roll = pose.roll();                
            }
            
            // LogService.logIt( "AmazonRekognitionUtils.getSingleFaceDetails() orientation String=" + (ocor==null ? "null" : ocor.toString()) + ", uploadedUserFileId=" + (arii.uuf==null ? "null" : arii.uuf.getUploadedUserFileId()) );
            
            int oVal = 0;
            if( ocor!=null )
            {
                switch (ocor) {
                    case ROTATE_180:
                        oVal = 180;
                        break;
                    case ROTATE_270:
                        oVal = 270;
                        break;
                    case ROTATE_90:
                        oVal = 90;
                        break;
                    default:
                        break;
                }
            } 
            
            if( oVal==0 && roll!=null )
            {
                // LogService.logIt( "AmazonRekognitionUtils.getSingleFaceDetails() Using roll=" + roll + " for oVal, uploadedUserFileId=" + (arii.uuf==null ? "null" : arii.uuf.getUploadedUserFileId()) );
                
                // Roll is -180 to +180
                if( roll>=-224 && roll<-135 )
                    oVal=180;
                
                // rotated counter 90 degrees. correct by going clockwise 90.
                else if( roll>=-135 && roll<-45 )
                    oVal=90;
                
                else if( roll>135 && roll<=225 )
                    oVal=180;
                
                // rotated clockwise 90. Correct by going clockwise 270
                else if( roll>45 && roll<=135 )
                    oVal=270;                 
            }            
            out[2]= oVal;

            if( res.faceDetails().size() < 1)
            {
                //LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() No faceDetails found for photo. Returing null" );
                out[0]="SUCCESS";
                out[1]=null;
                return out;
            }

            BoundingBox bb;
            //float area;
            //float confidence;

            // Multi face. Make sure they are all big enough.
            if( res.faceDetails().size()>1)
            {
                List<FaceDetail> fdl = res.faceDetails();
                List<FaceDetail> fdl2 = new ArrayList<>();
                fdl2.addAll( fdl );

                // Sorts with largest / strongest confidence at the top.
                Collections.sort( fdl2, new FaceDetailComparator() );

                ListIterator<FaceDetail> iter = fdl2.listIterator();

                FaceDetail fd;
                int ct = 0;
                while( iter.hasNext() )
                {
                    fd = iter.next();
                    ct++;

                    // skip first (best and biggest)
                    if( ct<=1 )
                        continue;

                    bb = fd.boundingBox();

                    // LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() multiface bounding box=" + ( bb==null ? "null" : "width=" + bb.getWidth() + ", height=" + bb.getHeight() + ", left=" + bb.getLeft() + ", top=" + bb.getTop()) );

                    if( bb!=null )
                    {
                        if( bb.width()<MIN_MULTIFACE_WID_AS_RATIO_OF_IMG_SIZE && bb.height()<MIN_MULTIFACE_HGT_AS_RATIO_OF_IMG_SIZE )
                        {
                            // LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() Analyzing multiface. Ignoring additional face that appears to be too small. width as ratio=" + bb.getWidth() + ", height as ratio=" + bb.getHeight() + ", confidence=" + fd.getConfidence() + ", uploadedUserFileId=" + uuf.getUploadedUserFileId() + ", index=" + thumbIndex );
                            iter.remove();
                            continue;
                        }
                    }

                    // confidence = fd.getConfidence();
                    if( fd.confidence()<MIN_MULTIFACE_CONFIDENCE )
                    {
                        // LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() Analyzing multiface. Ignoring additional face low confidence=" + fd.getConfidence() );
                        iter.remove();
                        //continue;
                    }
                }

                // LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() Analyzing Multifaces. Initial size=" + fdl.size() + " revised size=" + fdl2.size() );
                out[0]="SUCCESS";
                out[1]=fdl2.get(0);
                out[3]=(int)( fdl2.size() );
                out[4]=fdl2;
                return out;
            }

            //FaceDetail fdx = res.getFaceDetails().get(0);
            //bb = fdx.getBoundingBox();
            // LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() single face bounding box=" + ( bb==null ? "null" : "width=" + bb.getWidth() + ", height=" + bb.getHeight() + ", left=" + bb.getLeft() + ", top=" + bb.getTop()) );

            //if( bb!=null )
            //    LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() YYY.1 Analyzing single face. width as ratio=" + bb.getWidth() + ", height as ratio=" + bb.getHeight() + ", uploadedUserFileId=" + uuf.getUploadedUserFileId() + ", index=" + thumbIndex );

            // LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() YYY.2 Analyzing single face. confidence=" + fdx.getConfidence() + ", uploadedUserFileId=" + uuf.getUploadedUserFileId() + ", index=" + thumbIndex );


            out[0]="SUCCESS";
            out[1]=res.faceDetails().get(0);
            out[3]=(int)( res.faceDetails().size() );
            out[4]=res.faceDetails();

            return out;
        }

        catch( ThrottlingException e )
        {
            LogService.logIt("AmazonRekognitionUtils.getImageFaceDetails() ERROR Amazon was unable to process photo. " + e.toString() + " File1: " + (uuf==null ? "null" : uuf.toString()) );
            out[0]="ERROR";
            out[1]=e.toString();
            return out;
        }
        catch( RekognitionException e )
        {
            LogService.logIt("AmazonRekognitionUtils.getImageFaceDetails() ERROR Amazon was unable to process photo. " + e.toString() + " File1: " + (uuf==null ? "null" : uuf.toString()) );
            out[0]="ERROR";
            out[1]=e.toString();
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "AmazonRekognitionUtils.getImageFaceDetails() File1: " + (uuf==null ? "null" : uuf.toString()) );
            out[0]="ERROR";
            out[1]=e.toString();
            return out;
        }
        
        /*
        Object[] out = new Object[5];
        
        out[2] = ((int) 0);
        out[3] = ((int) 0);
        
        try
        {
            if( uuf==null || 
                //(!useThumbImage && !uuf.hasImageFile()) || 
                (useThumbImage && ( uuf.getThumbFilename()==null || uuf.getThumbFilename().isEmpty())) )
                throw new Exception( "UploadedUserFile1 is invalid" );

            initClient();
            
            // float match = 100;
            
            AmazonRekognitionImageInfo arii = new AmazonRekognitionImageInfo( uuf, useThumbImage, thumbIndex );
               
            if( fileXfer == null )
                fileXfer = new FileXferUtils();
            
            Image src = arii.getImage(fileXfer);

            if( src==null )
                throw new Exception( "Unable to obtain RekognitionClient.Image for Image File 1" );
            
                        
            DetectFacesRequest  req = new DetectFacesRequest().withImage(src).withAttributes( Attribute.ALL );

            DetectFacesResult  res=rekognitionClient.detectFaces(req);

            String oStr = res.getOrientationCorrection();
            
            // LogService.logIt( "AmazonRekognitionUtils.getSingleFaceDetails() orientation String=" + oStr );
            
            int oVal = 0;
            
            if( oStr!=null && oStr.indexOf("_")>0 )
            {
                String r = null;
                try
                {
                    r = oStr.substring( oStr.indexOf("_")+1, oStr.length() );
                    if( !r.trim().isEmpty() )
                    {   
                        oVal = Integer.parseInt( r );
                        out[2]= ((int) oVal);
                    }
                }
                catch( Exception e )
                {
                    LogService.logIt(e,"AmazonRekognitionUtils.getImageFaceDetails() Parsing orientation String=" + oStr  + ", r=" + r );
                }
            }
            
            //if( !multipleFacesOk && res.getFaceDetails().size() > 1)
            //    LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() Found more than one face: " + res.getFaceDetails().size() );
            
            if( res.getFaceDetails().size() < 1)
            {
                //LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() No faceDetails found for photo. Returing null" );
                out[0]="SUCCESS";
                out[1]=null;
                return out;
            }

            BoundingBox bb;
            float area;
            float confidence;
            
            // Multi face. Make sure they are all big enough.
            if( res.getFaceDetails().size()>1)
            {
                List<FaceDetail> fdl = res.getFaceDetails();                
                List<FaceDetail> fdl2 = new ArrayList<>();
                fdl2.addAll( fdl );
                
                // Sorts with largest / strongest confidence at the top. 
                Collections.sort( fdl2, new FaceDetailComparator() );
                

                
                ListIterator<FaceDetail> iter = fdl2.listIterator();
                
                FaceDetail fd;
                int ct = 0;
                while( iter.hasNext() )
                {
                    fd = iter.next();
                    ct++;

                    // skip first (best and biggest)
                    if( ct<=1 )
                        continue;

                    bb = fd.getBoundingBox();
                    if( bb!=null )
                    {
                        if( bb.getWidth()<MIN_MULTIFACE_WID_AS_RATIO_OF_IMG_SIZE && bb.getHeight()<MIN_MULTIFACE_HGT_AS_RATIO_OF_IMG_SIZE )
                        {
                            LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() Analyzing multiface. Ignoring additional face that appears to be too small. width as ratio=" + bb.getWidth() + ", height as ratio=" + bb.getHeight() + ", confidence=" + fd.getConfidence() + ", uploadedUserFileId=" + uuf.getRcUploadedUserFileId() + ", index=" + thumbIndex );
                            iter.remove();
                            continue;
                        }
                    }
                    
                    // confidence = fd.getConfidence();
                    if( fd.getConfidence()<MIN_MULTIFACE_CONFIDENCE )
                    {
                        LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() Analyzing multiface. Ignoring additional face low confidence=" + fd.getConfidence() );
                        iter.remove();
                        continue;
                    }                    
                }
                
                LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() Analyzing Multifaces. Initial size=" + fdl.size() + " revised size=" + fdl2.size() );
                out[0]="SUCCESS";
                out[1]=fdl2.get(0);
                out[3]=(int)( fdl2.size() );
                out[4]=fdl2;
                return out;
            }
            
            //FaceDetail fdx = res.getFaceDetails().get(0);
            //bb = fdx.getBoundingBox();
            //if( bb!=null )
            //    LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() YYY.1 Analyzing single face. width as ratio=" + bb.getWidth() + ", height as ratio=" + bb.getHeight() + ", uploadedUserFileId=" + uuf.getUploadedUserFileId() + ", index=" + thumbIndex );

            // LogService.logIt( "AmazonRekognitionUtils.getImageFaceDetails() YYY.2 Analyzing single face. confidence=" + fdx.getConfidence() + ", uploadedUserFileId=" + uuf.getUploadedUserFileId() + ", index=" + thumbIndex );
            
            
            out[0]="SUCCESS";
            out[1]=res.getFaceDetails().get(0);
            out[3]=(int)( res.getFaceDetails().size() );
            out[4]=res.getFaceDetails();
            
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AmazonRekognitionUtils.getImageFaceDetails() File1: " + (uuf==null ? "null" : uuf.toString()) ); 
            out[0]="ERROR";
            out[1]=e.toString();
            return out;
        }
        */
    }

    /*
     Data[0] == SUCCESS or ERROR
     data[1] = null or List<Object[]> name : confidence : count
     data[2] = null, 0, number of labels
     data[3] = null, or List<BoundingBox> boundingBox, if any.
    */
    public List<Object[]> getImageLabelDetails( RcUploadedUserFile uuf, boolean useThumbImage, int thumbIndex, boolean forRemoteProctoring, float minConfidence )
    {
        List<Object[]> out = new ArrayList<>();

        try
        {
            if( uuf==null ||
                //(!useThumbImage && !uuf.hasImageFile()) ||
                (useThumbImage && ( uuf.getThumbFilename()==null || uuf.getThumbFilename().isBlank())) )
                throw new Exception( "UploadedUserFile1 is invalid" );

            initClient();

            // float match = 100;

            AmazonRekognitionImageInfo arii = new AmazonRekognitionImageInfo( uuf, useThumbImage, thumbIndex );

            if( fileXfer == null )
                fileXfer = new FileXferUtils();

            Image src = arii.getImage(fileXfer);

            if( src==null )
                throw new Exception( "Unable to obtain RekognitionClient.Image for Image File 1" );

            DetectLabelsRequest request = DetectLabelsRequest.builder().image(src).maxLabels(200).minConfidence(minConfidence).build();

            DetectLabelsResponse result = rekognitionClient.detectLabels(request);

            List<Label> labels = result.labels();
            // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() labels found=" + labels.size() );

            Object[] lout; // = new Object[4];

            float confidence;
            int count = 0;
            //  BoundingBox boundingBox;
            List<BoundingBox> bbl;
            //String res = "";
            for (Label label : labels)
            {
                // res += "\nLABEL=" + label.name() + ", confidence=" + label.confidence();
                confidence = label.confidence();
                // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() LABEL=" + label.getName() + ", confidence=" + label.getConfidence() );

                if( confidence<minConfidence )
                    continue;

                if( !isPotentialIdCard( label.name() ))
                    continue;
                
                count = 0;

                bbl = null;

                if( label.instances()==null || label.instances().isEmpty() )
                {
                    // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Label " +  label.getName() + ", confidence=" + confidence + ", has no instances. This means it is probably a parent and another label has the child." );
                    if( confidence>1 )
                        count = 1;
                }
                else
                {
                    // res += ", instances=" + label.instances().size();
                    // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Label " +  label.getName() + " has " + label.getInstances().size() + " instances. confidence=" + label.getConfidence() );
                    count = label.instances().size();
                    bbl = new ArrayList<>();
                    for( Instance inst : label.instances() )
                    {
                        // res += ", instance.confidence=" + inst.confidence();
                        if( inst.boundingBox()==null )
                        {
                            LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Bounding Box is null Label=" + label.name() );
                        }
                        else
                        {
                            // res += ", instance.bounding=" + inst.boundingBox().toString();
                            // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() ADDING Bounding Box " + inst.getBoundingBox().toString() + ", Label=" + label.getName() );
                            bbl.add( inst.boundingBox() );
                        }
                    }
                }
                // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Found label  name=" + label.getName() + ", count=" + count + ", confidence=" + confidence );

                if( label.parents()!=null && !label.parents().isEmpty() )
                {
                    String p = "";
                    for( Parent pp : label.parents() )
                        p += ", " + pp.name();
                    // res += ", parents=" + p;
                    // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Found Parents: " + p );
                }
                lout = new Object[4];
                lout[0] = label.name();
                lout[1] = count;
                lout[2] = confidence;
                lout[3] = bbl;

                out.add(lout);
            }

            // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Result Summary: " + res );

            return out;
        }
        catch( ThrottlingException e )
        {
            LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() ERROR " + e.toString() + " File1: " + (uuf==null ? "null" : uuf.toString()) );
            return out;
        }
        catch( RekognitionException e )
        {
            LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() ERROR " + e.toString() + " File1: " + (uuf==null ? "null" : uuf.toString()) );
            return out;
        }                
        catch( Exception e )
        {
            LogService.logIt(e, "AmazonRekognitionUtils.getImageLabelDetails() File1: " + (uuf==null ? "null" : uuf.toString()) );
            return out;
        }
        
        /*
        List<Object[]> out = new ArrayList<>();
        
        try
        {
            if( uuf==null || 
                //(!useThumbImage && !uuf.hasImageFile()) || 
                (useThumbImage && ( uuf.getThumbFilename()==null || uuf.getThumbFilename().isEmpty())) )
                throw new Exception( "UploadedUserFile1 is invalid" );

            initClient();
            
            // float match = 100;
            
            AmazonRekognitionImageInfo arii = new AmazonRekognitionImageInfo( uuf, useThumbImage, thumbIndex );
               
            if( fileXfer == null )
                fileXfer = new FileXferUtils();
            
            Image src = arii.getImage(fileXfer);

            if( src==null )
                throw new Exception( "Unable to obtain RekognitionClient.Image for Image File 1" );
            
            DetectLabelsRequest request = new DetectLabelsRequest()
              .withImage(src)
              .withMaxLabels(200).withMinConfidence(minConfidence);           
                        
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();
            
            // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() labels found=" + labels.size() );
            
            Object[] lout; // = new Object[4];
            
            float confidence; 
            int count = 0;
            BoundingBox boundingBox;
            List<BoundingBox> bbl;
            String res = "";
            for (Label label : labels) 
            {
                res += "\nLABEL=" + label.getName() + ", confidence=" + label.getConfidence();
                confidence = label.getConfidence();
                
                if( confidence<minConfidence )
                    continue;
                
                if( !isPotentialIdCard( label.getName() ))
                    continue;
                
                count = 0;
                
                bbl = null;
                
                if( label.getInstances()==null || label.getInstances().isEmpty() )
                {
                    // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Label " +  label.getName() + " has no instances. This means it is probably a parent and another label has the child." );
                    // count = 0;
                }
                else
                {
                    res += ", instances=" + label.getInstances().size();
                    // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Label " +  label.getName() + " has " + label.getInstances().size() + " instances. confidence=" + label.getConfidence() );
                    count = label.getInstances().size();
                    bbl = new ArrayList<>();
                    for( Instance inst : label.getInstances() )
                    {
                        res += ", instance.confidence=" + inst.getConfidence();
                        if( inst.getBoundingBox()==null )
                        {
                            LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Bounding Box is null Label=" + label.getName() );
                        }
                        else
                        {
                            res += ", instance.bounding=" + inst.getBoundingBox().toString();                        
                            LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() ADDING Bounding Box " + inst.getBoundingBox().toString() + ", Label=" + label.getName() );
                            bbl.add( inst.getBoundingBox() );
                        }
                    }
                }
                
                
                //for( Instance inst : label.getInstances() )
                //{
                //    if( inst.getConfidence()>confidence )
                //        confidence=inst.getConfidence();
                //}
                
                // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Found label  name=" + label.getName() + ", count=" + count + ", confidence=" + confidence );
                
                if( label.getParents()!=null && !label.getParents().isEmpty() )
                {
                    String p = "";
                    
                    for( Parent pp : label.getParents() )
                        p += ", " + pp.getName();
                    res += ", parents=" + p;                    
                    // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Found Parents: " + p );
                }
                lout = new Object[4];
                lout[0] = label.getName();
                lout[1] = count;
                lout[2] = confidence;
                lout[3] = bbl;                
                                
                out.add(lout);
            }
            
            // LogService.logIt( "AmazonRekognitionUtils.getImageLabelDetails() Result Summary: " + res );
            
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AmazonRekognitionUtils.getImageLabelDetails() File1: " + (uuf==null ? "null" : uuf.toString()) ); 
            return out;
        }
        */
    }
    
    
    
    /**
     * Returns the index number of the face that is inside the bounding box or -1 if none found.
     * 
     * @param faceDetailList
     * @param data
     * @return 
     *
    public static int getIndexForFaceInsideObject( List<FaceDetail> faceDetailList, Object[] label, float minFaceConfidence )
    {
        if( label==null || faceDetailList==null || faceDetailList.isEmpty() )
            return -1;

        BoundingBox faceBb;
        List<BoundingBox> cardBbl;
        FaceDetail fd;

        for( int idx=0;idx<faceDetailList.size(); idx++ )
        {
            fd = faceDetailList.get(idx);

            if( fd.confidence()<minFaceConfidence )
            {
                // LogService.logIt( "AmazonRekognitionUtils.getIndexForFaceInsideObject() checking " + ((String)label[0]) + ", Skipping Face because face has low confidence=" + fd.getConfidence() + ", threshold=" + minFaceConfidence );
                continue;
            }

            faceBb = fd.boundingBox();

            //for( Object[] label : data )
            //{
            cardBbl = (List<BoundingBox>)label[3];
            // LogService.logIt( "AmazonRekognitionUtils.getIndexForFaceInsideObject() checking " + ((String)label[0]) + ", bounding box list size=" + cardBbl.size() );
            for( BoundingBox cbb : cardBbl )
            {
                if( containsBoundingBox( cbb, faceBb ) )
                    return idx;
            }
            //}
        }
        return -1;

    }
    */



    public static boolean isPotentialIdCard( String nm )
    {
        if( nm.equalsIgnoreCase( "ID Cards" ) || 
            nm.equalsIgnoreCase( "Document" ) || 
            nm.equalsIgnoreCase( "Paper" ) || 
            nm.equalsIgnoreCase( "Photo" ) || 
            nm.equalsIgnoreCase( "Advertisement" ) || 
            nm.equalsIgnoreCase( "Photography" ) || 
            nm.equalsIgnoreCase( "Portrait" ) || 
            // nm.equalsIgnoreCase( "Finger" ) || 
            nm.equalsIgnoreCase( "Box" ) || 
            nm.equalsIgnoreCase( "Poster" ) )   
            return true;
        return false;
    }
    
    
    /*
    public static boolean containsBoundingBox( BoundingBox outer, BoundingBox inner )
    {
        if( outer==null || inner==null )
            return false;

        // upper left
        if( outer.left() > inner.left() )
            return false;

        if( outer.top() > inner.top() )
            return false;

        // bleeds right
        if( (outer.left()+outer.width()) < (inner.left() + inner.width()) )
            return false;

        // bleeds bottom
        if( (outer.top() + outer.height()) < (inner.top()+inner.height()))
            return false;

        // looks good!
        return true;
    }
    */
    

}
