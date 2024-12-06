/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.proctor;

import com.tm2ref.amazoncloud.AmazonRekognitionUtils;
import com.tm2ref.corp.CorpUtils;
import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.user.User;
import com.tm2ref.faces.FacesUtils;
import com.tm2ref.file.BucketType;
import com.tm2ref.file.FileUploadFacade;
import com.tm2ref.file.FileXferUtils;
import com.tm2ref.file.UploadedUserFileFauxSource;
import com.tm2ref.file.UploadedUserFileStatusType;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.global.Constants;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.ref.RcMessageUtils;
import com.tm2ref.ref.RefBean;
import com.tm2ref.ref.RefUtils;
import com.tm2ref.ref.RefUserType;
import com.tm2ref.ref.RefPageType;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.PhoneUtils;
import com.tm2ref.util.GooglePhoneUtils;
import com.tm2ref.util.StringUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import software.amazon.awssdk.services.rekognition.model.FaceDetail;

/**
 *
 * @author miker_000
 */
@Named
@RequestScoped
public class ProctorUtils extends FacesUtils {
    
    @Inject
    private RefBean refBean;

    @Inject
    private ProctorBean proctorBean;
    
    boolean boolean1;
    
    FileUploadFacade fileUploadFacade = null;
    
    
    public static ProctorUtils getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return (ProctorUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "proctorUtils" );
    }
    
    public static List<RcUploadedUserFile> getFauxRcUploadedUserFileListForReportThumbs( List<RcUploadedUserFile> ufl, boolean forceShowAll, int maxImagesToShow ) throws Exception
    {
        if( ufl==null || ufl.isEmpty() )
            return null;
        
        List<RcUploadedUserFile> out = new ArrayList<>();                

        for( RcUploadedUserFile u : ufl )
        {
            if( u==null || !u.getUploadedUserFileType().getIsRcPhotoOrId() || !u.getHasValidThumbs() )
                return out;

            if( u.getThumbFilename()==null || u.getThumbFilename().isBlank() || u.getMaxThumbIndex()<1 )
                return out;

            for( int idx : getIndexesToInclude(u, forceShowAll, maxImagesToShow) )
                out.add(getFauxRcUploadedUserFileForIndex( u, idx ));
        }        
        return out.isEmpty() ? null : out;
    }

    public static RcUploadedUserFile getSingleFauxRcUploadedUserFileForThumb( RcUploadedUserFile u )
    {
        if( u==null || !u.getUploadedUserFileType().getIsRcPhotoOrId() || !u.getHasValidThumbs() )
            return null;
        
        List<Integer> idxl = getIndexesToInclude(u, true, 5);
        
        if( idxl==null || idxl.isEmpty() )
            return null;

        Collections.shuffle(idxl);
        Integer idx = idxl.get(0);
        return getFauxRcUploadedUserFileForIndex( u, idx );
    }
    
    private static RcUploadedUserFile getFauxRcUploadedUserFileForIndex( RcUploadedUserFile u, int idx )
    {
        if( u.getThumbWidth()<=0 && u.getWidth()>0 )
            u.setThumbWidth( u.getWidth() );
        if( u.getThumbHeight()<=0 && u.getHeight()>0 )
            u.setThumbHeight( u.getHeight() );
        RcUploadedUserFile uuf = new RcUploadedUserFile();
        uuf.setRcUploadedUserFileId( u.getRcUploadedUserFileId() );
        uuf.setR1( u.getR1() );
        uuf.setR2( u.getR2() );
        //uuf.setActId( u.getActId() );
        uuf.setRcCheckId(u.getRcCheckId() );
        uuf.setRcRaterId(u.getRcRaterId() );
        uuf.setUserId(u.getUserId() );
        uuf.setUploadedUserFileTypeId( u.getUploadedUserFileTypeId() );
        uuf.setUploadedUserFileStatusTypeId( UploadedUserFileStatusType.AVAILABLE.getUploadedUserFileStatusTypeId() );
        String fn = u.getThumbFilename();
        if( fn.contains(".IDX.") )
            fn = StringUtils.replaceStr(fn, ".IDX." , "." + idx + "." );
        uuf.setThumbFilename( fn );
        // uuf.setThumbFilename( StringUtils.replaceStr( u.getThumbFilename(), ".IDX." , "." + idx + "." ) );
        uuf.setFilename( fn );
        uuf.setThumbWidth( u.getThumbWidth() );
        uuf.setThumbHeight( u.getThumbHeight() );
        uuf.setTempInt1(idx);

        uuf.setOrientation(u.getOrientation());

        // LogService.logIt( "ProctorUtils.getFauxUploadedUserFileListForReportThumbs() uuf.idx=" + idx + ", hasError=" + u.hasFailedIndex(idx) + ", failedThumbIndexes=" + u.getFailedThumbIndices() );      
        return uuf;
        
    }

    
    private static List<Integer> getIndexesToInclude( UploadedUserFileFauxSource u, boolean forceIncludeAll, int maxImagesToInclude)
    {
        int incr = 1;      
        List<Integer> out = new ArrayList<>();
        
        if( u.getFailedIndexMap()==null )
            u.initFailedIndexMap();
        
        Set<Integer> failed =  u.getFailedIndexMap().keySet();
        
        // out.addAll( u.getFailedIndexMap().keySet() );
        
        int remaining = u.getMaxThumbIndex()-failed.size();
        
        if( remaining<=16 || forceIncludeAll )
            incr=1;
        else if( remaining<=32 )
            incr = 2;
        else if( remaining<=48 )
            incr = 3;
        else if( remaining<=64 )
            incr = 4;
        else
            incr = Math.round(((float)remaining)/16f);
        
        for( int i=1; i<=u.getMaxThumbIndex(); i+=incr )
        {
            if( maxImagesToInclude>0 && out.size()>=maxImagesToInclude )
                break;
            
            if( out.contains(i) )
                continue;
            if( failed.contains(i) )
                continue;
            
            out.add(i);
        }        
        Collections.sort(out);
        return out;
    }
    
    
    
    
    public String doPhotoUpload()
    {
        RcCheck rc = refBean.getRcCheck();
        RefUserType refUserType = refBean.getRefUserType();
        RefPageType refPageType = refBean.getRefPageType();            
        long rcRaterId = 0;
        RefUtils refUtils = null;
        
        try
        {
            if( rc==null )
            {
                LogService.logIt( "ProctorUtils.doPhotoUpload() No RcCheck in RefBean. Appears to be a session problem - refBean.rcCheck is null. Tryinig to recover." );
                
                refUtils = RefUtils.getInstance();
                String nextViewId = refUtils.checkRepairSession(503, true );

                
                rc = refBean.getRcCheck();
                refUserType = refBean.getRefUserType();
                refPageType = refBean.getRefPageType();  
                
                LogService.logIt( "ProctorUtils.doPhotoUpload() nextViewId after calling repair is: "  + nextViewId + ", refBean.rcCheck.rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId())  );
                
                if( rc==null )
                {
                    LogService.logIt( "ProctorUtils.doPhotoUpload() Unable to recover session. Starting over."  );
                    return refUtils.processStartOver();
                }
            }
                              
            if( refUserType==null )
                throw new Exception( "RefBean.refUserType is null" );

            if( refUserType.getIsRater() && rc.getRcRater()==null )
                throw new Exception( "RefBean.refUserType is Rater but there is no RcRater in RcCheck." );
            
            if( refUserType.getIsRater() )
                rcRaterId = rc.getRcRater().getRcRaterId(); 
            else
                rcRaterId = rc.getCollectCandidateRatings()==1 && rc.getRcRater()!=null ? rc.getRcRater().getRcRaterId() : 0;
            
            if( refPageType==null )
                throw new Exception( "RefBean.refPageType is null" );

            refUtils = RefUtils.getInstance();
            
            if( !refPageType.equals(RefPageType.PHOTO) && !refPageType.equals(RefPageType.ID_PHOTO) )
            {
                LogService.logIt( "ProctorUtils.doPhotoUpload() ERROR.  RefPageType is not Photo or ID Photo. Returning to current RefPageType=" + refPageType.getName() + ", rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                return refUtils.getViewFromPageType(refPageType);
            }
            
            // need to be sure that the system has had a chance to upload the files.
            Thread.sleep(800);
            

            UploadedUserFileType uploadedUserFileType = null;
            
            if( refPageType.equals(RefPageType.PHOTO))
                uploadedUserFileType = refUserType.getIsCandidate() ? UploadedUserFileType.REF_CHECK_IMAGES : UploadedUserFileType.REF_CHECK_RATER;
            
            if( refPageType.equals( RefPageType.ID_PHOTO) )
                uploadedUserFileType = refUserType.getIsCandidate() ? UploadedUserFileType.REF_CHECK_ID : UploadedUserFileType.REF_CHECK_RATER_ID;

            if( uploadedUserFileType==null )
                throw new Exception( "Cannot determine UploadedUserFileType from request." );
                     
            
            // Get the UploadedUserFile
            if( fileUploadFacade==null )
                fileUploadFacade = FileUploadFacade.getInstance();
            RcUploadedUserFile uuf = fileUploadFacade.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(rc.getRcCheckId(), rcRaterId, 0, uploadedUserFileType.getUploadedUserFileTypeId() );
            
            
            if( uuf == null )
            {
                for( int i=0;i<5;i++ )
                {
                    LogService.logIt( "ProctorUtils.doPhotoUpload() No UploadedUserFile found in dbms. Waiting 2 seconds in case it's a synch error. rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId + ", uploadedUserFileTypeId=" + uploadedUserFileType.getUploadedUserFileTypeId() + " " + uploadedUserFileType.getName() );
                    Thread.sleep(2000);
                    uuf = fileUploadFacade.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(rc.getRcCheckId(), rcRaterId, 0, uploadedUserFileType.getUploadedUserFileTypeId() );
                    if( uuf!=null )
                        break;
                }
            }
            
            String stub = uploadedUserFileType.getIsAnyId() ? "candid" : "candphoto";
            
            if( uuf == null )
            {
                LogService.logIt( "ProctorUtils.doPhotoUpload() No UploadedUserFile found in dbms. rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                setErrorMessage( "g.PPT." + stub + ".nophototryagain", new String[] {"110"} );
                return refUtils.getViewFromPageType(refPageType);
            }
            
            if( uuf.getMaxThumbIndex()<1 )
            {
                LogService.logIt( "ProctorUtils.doPhotoUpload()  UploadedUserFile.maxThumbIndex appears invalid. MaxThumbIndex=" + uuf.getMaxThumbIndex() + ", RefPageType=" + refPageType.getName() + ", rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                setErrorMessage( "g.PPT." + stub + ".nophototryagain", new String[] {"111"} );
                return refUtils.getViewFromPageType(refPageType);
            }

            if( uuf.getLastUpload()==null )
            {
                LogService.logIt( "UploadedUserFile.lastUpload date is null. uploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                setErrorMessage( "g.PPT." + stub + ".nophototryagain", new String[] {"112"} );
                return refUtils.getViewFromPageType(refPageType);
            }
                                    
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.MINUTE, -5 );
            
            if( uuf.getLastUpload().before( cal.getTime() ) )
            {
                LogService.logIt( "ProctorUtils.doPhotoUpload() Last upload date appears to be too far in past. lastUploadDate=" + uuf.getLastUpload().toString() + ", rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                this.setErrorMessage( "g.PPT." + stub + ".nophototryagain", new String[] {"100"} );
                return refUtils.getViewFromPageType(refPageType);
            }
            
            // get the filename to use.
            String fn = uuf.getThumbFilename();
            if( fn!=null && fn.contains(  ".IDX." ) )
                fn = StringUtils.replaceStr( fn, ".IDX." , "." + uuf.getMaxThumbIndex() + "." ); 
            
            // Wait for the file to be uploaded.
            BucketType bt = RuntimeConstants.getBooleanValue("useAwsTestFoldersForProctoring") ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING; 
            FileXferUtils.init();
            FileXferUtils.waitForAwsObject(uuf.getDirectory(), fn, bt.getBucketTypeId() );
            
            // OK, got a valid file. 
            AmazonRekognitionUtils amazonRekognitionUtils = new AmazonRekognitionUtils();
            Object[] output =  amazonRekognitionUtils.getImageFaceDetails(uuf, true, uuf.getMaxThumbIndex(), true);

            if( !uuf.hasPreTestIndex(uuf.getMaxThumbIndex()))
            {
                uuf.addPreTestIndex( uuf.getMaxThumbIndex() );
                uuf.savePreTestIndexSet();
                fileUploadFacade.saveRcUploadedUserFile(uuf);
            }
            
            boolean basicSkip = false;
            //boolean hasValidFace = false;
                        
            if( output[0]==null || !((String)output[0]).equalsIgnoreCase("SUCCESS") )
            {
                // LogService.logIt( "ProctorUtils.doPhotoUpload() AmazonRekognitionUtils.getImageFaceDetails() returned: " + (output[0]==null ? "null" : ((String)output[0])) + ", rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                
                addFailedIndexAndSave(uuf, uuf.getMaxThumbIndex(), ProctorImageErrorType.NO_FACE.getProctorImageErrorTypeId() );
                
                if( !getPhotosOptional() || proctorBean.getPhotoUploadAttempts()<Constants.MAX_PHOTO_UPLOAD_ATTEMPTS )
                {
                    proctorBean.setPhotoUploadAttempts( proctorBean.getPhotoUploadAttempts()+1 );
                    setErrorMessage( "g.PPT." + stub + ".nophototryagain", new String[] {"101"}  ); 
                    return refUtils.getViewFromPageType(refPageType);
                }
                else
                    basicSkip=true;
            }

            // orientation
            if( !basicSkip && output[2]!=null )
            {
                Integer orient = (Integer) output[2];
                
                if( uuf.getOrientation()!=orient )
                {
                    uuf.setOrientation( orient );
                    fileUploadFacade.saveRcUploadedUserFile(uuf);
                }
            }
            
            if( !basicSkip && output[3]==null  )
            {
                addFailedIndexAndSave(uuf, uuf.getMaxThumbIndex(), ProctorImageErrorType.NO_FACE.getProctorImageErrorTypeId() );

                // LogService.logIt( "ProctorUtils.doPhotoUpload() AmazonRekognitionUtils.getImageFaceDetails() Faces Count is null, rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                if( !getPhotosOptional() || proctorBean.getPhotoUploadAttempts()<Constants.MAX_PHOTO_UPLOAD_ATTEMPTS )
                {
                    proctorBean.setPhotoUploadAttempts( proctorBean.getPhotoUploadAttempts()+1 );
                    setErrorMessage( "g.PPT." + stub + ".nophototryagain", new String[] {"103"} ); 
                    return refUtils.getViewFromPageType(refPageType);
                }
                else
                    basicSkip=true;
            }

            if( !basicSkip && output[1]==null  )
            {
                addFailedIndexAndSave(uuf, uuf.getMaxThumbIndex(), ProctorImageErrorType.NO_FACE.getProctorImageErrorTypeId() );
                
                // LogService.logIt( "ProctorUtils.doPhotoUpload() AmazonRekognitionUtils.getImageFaceDetails() FaceDetails is null, uploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                if( !getPhotosOptional() || proctorBean.getPhotoUploadAttempts()<Constants.MAX_PHOTO_UPLOAD_ATTEMPTS )
                {
                    proctorBean.setPhotoUploadAttempts( proctorBean.getPhotoUploadAttempts()+1 );
                    setErrorMessage( "g.PPT." + stub + ".novalidfacetryagain", null );
                    return refUtils.getViewFromPageType(refPageType);
                }
                else
                    basicSkip=true;
            }
            
            FaceDetail faceDetail = (FaceDetail) output[1];            
            if( !basicSkip && faceDetail.confidence()<Constants.PROCTOR_MIN_FACE_CONFIDENCE )
            {
                addFailedIndexAndSave(uuf, uuf.getMaxThumbIndex(), ProctorImageErrorType.NO_FACE.getProctorImageErrorTypeId() );
                
                // LogService.logIt( "ProctorUtils.doPhotoUpload() AmazonRekognitionUtils.getImageFaceDetails() FaceDetail.confidence is too low=" + faceDetail.getConfidence() + ", rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                if( !getPhotosOptional() || proctorBean.getPhotoUploadAttempts()<Constants.MAX_PHOTO_UPLOAD_ATTEMPTS )
                {
                    proctorBean.setPhotoUploadAttempts( proctorBean.getPhotoUploadAttempts()+1 );
                    setErrorMessage( "g.PPT." + stub + ".novalidfacetryagain", null );
                    return refUtils.getViewFromPageType(refPageType);
                }
                else
                    basicSkip=true;
            }
            
            Integer faceCount = output[3]==null || basicSkip ? 0 : (Integer) output[3]; 
            
            if( !basicSkip && uploadedUserFileType.getIsAnyId() && faceCount>2 )
            {
                addFailedIndexAndSave(uuf, uuf.getMaxThumbIndex(), ProctorImageErrorType.ID_TOO_MANY_FACES.getProctorImageErrorTypeId() );
                
                // LogService.logIt( "ProctorUtils.doCandidateIdUpload() AmazonRekognitionUtils.getImageFaceDetails() Faces Count is more than TWO: " + faceCount + " rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                if( !getPhotosOptional() || proctorBean.getPhotoUploadAttempts()<Constants.MAX_PHOTO_UPLOAD_ATTEMPTS )
                {
                    proctorBean.setPhotoUploadAttempts( proctorBean.getPhotoUploadAttempts()+1 );
                    setErrorMessage( "g.PPT.candid.morethan2facetryagain", null ); 
                    return refUtils.getViewFromPageType(refPageType);               
                }
                else
                    basicSkip=true;                    
            }
            
            
            if( !basicSkip && !uploadedUserFileType.getIsAnyId() && faceCount>1 )
            {
                addFailedIndexAndSave(uuf, uuf.getMaxThumbIndex(), ProctorImageErrorType.MULTIPLE_FACES.getProctorImageErrorTypeId() );
                
                // LogService.logIt( "ProctorUtils.doPhotoUpload() AmazonRekognitionUtils.getImageFaceDetails() Faces Count is more than one: " + faceCount + " uploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                if( !getPhotosOptional() || proctorBean.getPhotoUploadAttempts()<Constants.MAX_PHOTO_UPLOAD_ATTEMPTS )
                {
                    proctorBean.setPhotoUploadAttempts( proctorBean.getPhotoUploadAttempts()+1 );
                    setErrorMessage( "g.PPT." + stub + ".morethan1facetryagain", null ); 
                    return refUtils.getViewFromPageType(refPageType);
                }
                else
                    basicSkip=true;
            }
            
            if( !basicSkip && uploadedUserFileType.getIsAnyId() )
            {
                List<Object[]> labelOutput = amazonRekognitionUtils.getImageLabelDetails(uuf, true, uuf.getMaxThumbIndex(), true, Constants.PROCTOR_MIN_IDCARD_CONFIDENCE );

                boolean foundIdCard = false;
                //boolean foundFaceInCard = false;
                String nm;
                for( Object[] label : labelOutput )
                {
                    //if( ((Integer)label[1])<=0 )
                    //continue;
                    nm = ((String)label[0]);

                    // First, is it a potential ID Card? If it is, check for an internal face.
                    if( AmazonRekognitionUtils.isPotentialIdCard(nm))
                        foundIdCard = true;
                }

                // LogService.logIt( "ProctorUtils.doPhotoUpload() foundIdCard=" + foundIdCard + ", basicSkip=" + basicSkip );
                
                if( !basicSkip && !foundIdCard ) // || !foundFaceInCard)
                {
                    addFailedIndexAndSave(uuf, uuf.getMaxThumbIndex(), ProctorImageErrorType.NO_ID_CARD.getProctorImageErrorTypeId() );

                    LogService.logIt( "ProctorUtils.doCandidateIdUpload() AmazonRekognitionUtils.getImageFaceDetails() Cannot find ID Card in the photo . rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                    if( !getPhotosOptional() || proctorBean.getPhotoUploadAttempts()<Constants.MAX_PHOTO_UPLOAD_ATTEMPTS )
                    {
                        proctorBean.setPhotoUploadAttempts( proctorBean.getPhotoUploadAttempts()+1 );
                        setErrorMessage( "g.PPT.candid.cannotfindidcardtryagain", null ); 
                        return refUtils.getViewFromPageType(refPageType);
                    }
                    else
                        basicSkip=true;                    
                }

                // For the ID, accept only if both faces MATCH and are STRONG ENOUGH.
                if( !basicSkip && faceCount==2 )
                {
                    // LogService.logIt( "ProctorUtils.doCandidateIdUpload() BBB.1 AmazonRekognitionUtils.getImageFaceDetails() Faces Count is TWO: " + faceCount + " rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );                
                    for( FaceDetail fd : (List<FaceDetail>) output[4])
                    {
                        if( fd.confidence()<Constants.PROCTOR_MIN_ID_FACE_CONFIDENCE )
                        {
                            addFailedIndexAndSave(uuf, uuf.getMaxThumbIndex(), ProctorImageErrorType.ID_MULTIFACE_MISMATCH.getProctorImageErrorTypeId() );

                            LogService.logIt( "ProctorUtils.doCandidateIdUpload() BBB.1a AmazonRekognitionUtils.getImageFaceDetails() Faces Count is two. However, at least one face has confidence below threshold. rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                            if( !getPhotosOptional() || proctorBean.getPhotoUploadAttempts()<Constants.MAX_PHOTO_UPLOAD_ATTEMPTS )
                            {
                                proctorBean.setPhotoUploadAttempts( proctorBean.getPhotoUploadAttempts()+1 );
                                setErrorMessage( "g.PPT.candid.morethan1facetryagain", null  ); 
                                return refUtils.getViewFromPageType(refPageType);
                            }
                            else
                                basicSkip=true;                    
                        }
                    }

                    // Compare the image to itself and extract the LOWEST match percentage.
                    Object[] output2 = amazonRekognitionUtils.compareThumbImages(uuf, uuf.getMaxThumbIndex(), uuf, uuf.getMaxThumbIndex(), true, Constants.PROCTOR_MIN_FACE_MATCH_CONFIDENCE, true, 2 );
                    if( !basicSkip && (output2[0]==null || !((String)output2[0]).equalsIgnoreCase("SUCCESS")) )
                    {
                        addFailedIndexAndSave(uuf, uuf.getMaxThumbIndex(), ProctorImageErrorType.ID_MULTIFACE_MISMATCH.getProctorImageErrorTypeId() );

                        LogService.logIt( "ProctorUtils.doCandidateIdUpload() BBB.2 AmazonRekognitionUtils.compareThumbImages() for multiple images in same picture. returned: " + (output2[0]==null ? "null" : ((String)output2[0])) + ", rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );
                        if( !getPhotosOptional() || proctorBean.getPhotoUploadAttempts()<Constants.MAX_PHOTO_UPLOAD_ATTEMPTS )
                        {
                            proctorBean.setPhotoUploadAttempts( proctorBean.getPhotoUploadAttempts()+1 );
                            setErrorMessage( "g.PPT.candid.morethan1facetryagain", null  ); 
                            return refUtils.getViewFromPageType(refPageType);
                        }
                        else
                            basicSkip=true;                    

                    }

                    Float matchPct = (Float) output2[1];            
                    Float matchConf = (Float) output2[2];                            
                    // LogService.logIt( "ProctorUtils.doCandidateIdUpload() BBB.3 AmazonRekognitionUtils.compareThumbImages() for multiple images in same picture. returned: matchPct=" + matchPct + ", matchConf=" + matchConf + ", rcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + " rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rcRaterId );

                    if( !basicSkip &&  (matchPct==null || matchConf==null ||  matchPct<Constants.PROCTOR_MIN_FACE_MATCH_PERCENT || matchConf<Constants.PROCTOR_MIN_FACE_MATCH_CONFIDENCE) )
                    {
                        addFailedIndexAndSave(uuf, uuf.getMaxThumbIndex(), ProctorImageErrorType.ID_MULTIFACE_MISMATCH.getProctorImageErrorTypeId() );

                        matchPct = 0f;            
                        matchConf = 0f;                                                
                        if( !getPhotosOptional() || proctorBean.getPhotoUploadAttempts()<Constants.MAX_PHOTO_UPLOAD_ATTEMPTS )
                        {
                            proctorBean.setPhotoUploadAttempts( proctorBean.getPhotoUploadAttempts()+1 );
                            setErrorMessage( "g.PPT.candid.morethan1facetryagain", null ); 
                            return refUtils.getViewFromPageType(refPageType);
                        }
                        else
                            basicSkip=true;                    
                    }
                }                
                
            }
            
            // LogService.logIt( "ProctorUtils.doPhotoUpload() id=" + uploadedUserFileType.getIsAnyId() + ". Face looks good or it's OK to skip. Moving to next step. rcCheckId=" + rc.getRcCheckId() + ", basicSkip=" + basicSkip );
                                    
            if( refPageType.equals(RefPageType.PHOTO ) )
            {
                proctorBean.setPhotoUploadAttempts(0);
                proctorBean.setSessionPhotoComplete( true );
                refPageType = refUtils.getNextPageTypeForRefProcess();
                refBean.setRefPageType(refPageType);
                return refUtils.getViewFromPageType(refPageType);
                // return refUtils.getNextViewForCorp();
            }
            
            if( refPageType.equals(RefPageType.ID_PHOTO ) )
            {
                proctorBean.setPhotoUploadAttempts(0);
                proctorBean.setSessionIdPhotoComplete( true );
                refPageType = refUtils.getNextPageTypeForRefProcess();
                refBean.setRefPageType(refPageType);
                return refUtils.getViewFromPageType(refPageType);
                //return refUtils.getNextViewForCorp();
            }

            throw new Exception( "RefPageType apparently invalid: " + refPageType.getName());
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorUtils.doPhotoUpload() refPageType=" + (refPageType==null ? "null" : refPageType.getName()) + ", rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) + ", rcRaterId=" + rcRaterId );
            setMessage( e );
            try
            {
                if( refUtils==null )
                    refUtils = RefUtils.getInstance();

                if( refPageType!=null )
                    return refUtils.getViewFromPageType(refPageType);
                
                return refUtils.processStartOver();
            }
            catch( Exception ee )
            {
                LogService.logIt( ee, "ProctorUtils.doPhotoUpload() Trying to go to last RefPageType. rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) + ", rcRaterId=" + rcRaterId );
            }
            
        }
        
        return "StayInSamePlace";
    }
       
    
    private void addFailedIndexAndSave( RcUploadedUserFile uuf, int idx, int proctorImageErrorTypeId ) throws Exception
    {
        if( !uuf.hasFailedIndex( idx ))
        {
            uuf.addFailedIndex( idx, proctorImageErrorTypeId );
            uuf.saveFailedIndexMap();
            if( fileUploadFacade==null )
                fileUploadFacade = FileUploadFacade.getInstance();
            fileUploadFacade.saveRcUploadedUserFile(uuf);
        }
    }
    
    
    public boolean getPhotosOptional()
    {
        if( refBean.getRcCheck()==null || refBean.getRefUserType()==null )
            return true;
        
        if( refBean.getRefUserType().getIsCandidate() )
            return refBean.getRcCheck().getRcCandidatePhotoCaptureType().getIsOptional();
        else
            return refBean.getRcCheck().getRcRaterPhotoCaptureType().getIsOptional();
    }
    
    public String getFullRestartUrl()
    {
        if( refBean.getRcCheck()==null || refBean.getRefUserType()==null )
            return null;
        
        if( refBean.getRefUserType().getIsCandidate() )
            return refBean.getRcCheck().getCandidateStartUrl();
        
        else
            return refBean.getRcCheck().getRcRater().getRaterStartUrl();
    }
    
    
    public String processSkipPhotoUpload()
    {
        RcCheck rc = refBean.getRcCheck();        
        try
        {            
            if( rc==null )
            {
                LogService.logIt( "ProctorUtils.processSkipPhotoUpload() refBean.rcCheck is null. Looks like a session issue. Starting over." );
                RefUtils ru = RefUtils.getInstance();
                return ru.processStartOver();                
            }
        
            if( refBean.getRefUserType()==null )
            {
                LogService.logIt( "ProctorUtils.processSkipPhotoUpload() refBean.getRefUserType() is null.  Looks like a session issue. Starting over. rcCheckId=" + rc.getRcCheckId() );
                RefUtils ru = RefUtils.getInstance();
                return ru.processStartOver();                
                // throw new Exception( "RefBean.refUserType is null" );
            }
                        
            if( refBean.getRefUserType().getIsCandidate() && rc.getRcCandidatePhotoCaptureType().getIsRequired() )
                throw new STException( "g.RCPPPhotoRequired", new String[]{rc.getRcCheckName()} );

            if( refBean.getRefUserType().getIsRater() && rc.getRcRaterPhotoCaptureType().getIsRequired() )
                throw new STException( "g.RCPPPhotoRequired", new String[]{rc.getRcCheckName()} );
            
            // proctorBean.setRecDevs(0);
            // proctorBean.setCameraOptOut(true);
            
            RefUtils refUtils = RefUtils.getInstance();
            RefPageType rpt = refUtils.getNextPageTypeForRefProcess();            
            refBean.setRefPageType(rpt);      
            return conditionUrlForSessionLossGet(refUtils.getViewFromPageType( refBean.getRefPageType() ) );
        }        
        catch( STException e )
        {
            setMessage( e );
            return "StayInSamePlace";
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorUtils.processSkipPhotoUpload() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) );
            setMessage( e );
            return "StayInSamePlace";
        }                
    }
    
    public String processChangeDevices()
    {
        RcCheck rc = refBean.getRcCheck();        
        try
        {            
            if( rc==null )
                throw new Exception( "RcCheck is null" );
        
            if( refBean.getRefUserType()==null )
                throw new Exception( "RefBean.refUserType is null" );
                        
            return conditionUrlForSessionLossGet( "/pp/change-devices.xhtml" );
        }        
        catch( STException e )
        {
            setMessage( e );
            return "StayInSamePlace";
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorUtils.processChangeDevices() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) );
            setMessage( e );
            return "StayInSamePlace";
        }                
    }
    
    public String processEmailDeviceChangeRestartUrl()
    {
        return sendDeviceChangeRestartUrl( true );
    }    
    
    public String processSmsDeviceChangeRestartUrl()
    {
        return sendDeviceChangeRestartUrl( false );
    }   
    
    public String sendDeviceChangeRestartUrl( boolean isEmail )
    {
        RcCheck rc = refBean.getRcCheck();
        
        try
        {       
            if( rc==null )
                rc = RefUtils.getInstance().repairRefBeanForCurrentAction(refBean, true );
            
            if( rc==null )
            {
                LogService.logIt( "ProctorUtils.sendDeviceChangeRestartUrl() RcCheck is null and could not be recovered."  );
                return CorpUtils.getInstance().processCorpHome();         
            }
        
            if( refBean.getRefUserType()==null )
            {
                throw new Exception( "RefBean.refUserType is null" );
            }
            
            // String restartUrl = getFullRestartUrl();
            User user = null;
            
            if( refBean.getRefUserType().getIsCandidate() )
                user = rc.getUser();
            else
                user = rc.getRcRater().getUser();

            String[] params = RcMessageUtils.getMessageParams( rc.getAdminUser(), rc, refBean.getRefUserType().getIsRater() ? rc.getRcRater() : null, getLocale(), false );
            
            RcMessageUtils rcmu;
            
            if( isEmail )
            {
                String email = proctorBean.getEmail();

                if( email==null || email.trim().isEmpty() )
                    throw new STException( "g.CorpEmailRequired" );

                if( !EmailUtils.validateEmailNoErrors(email) )
                    throw new STException( "g.EmailInvalid", new String[]{email} );

                rcmu = new RcMessageUtils();
                
                int ct = rcmu.sendRcCheckRestartLinkEmail(rc, refBean.getRefUserType(), user, params, email, getLocale());

                if( ct>0 )
                    setInfoMessage( "g.FulTestLinkEmailSentTo", new String[] { email } ); 
                else
                    setErrorMessage( "g.FulTestLinkEmailNOTSentTo", new String[] { email } );     
            }
            
            else
            {
                String m = proctorBean.getMobileNum();

                if( m==null || m.trim().isEmpty() )
                    throw new STException( "g.MulPhoneNumRequired" );

                if( !GooglePhoneUtils.isNumberValid(m, user.getCountryCode() ) )
                    throw new STException( "g.RestartPhoneNumInvalid" );
                
                rcmu = new RcMessageUtils();
                int ct = rcmu.sendRcCheckRestartLinkSms(rc, refBean.getRefUserType(), user, params, m, user.getCountryCode(), getLocale());

                if( ct>0 )
                    setInfoMessage( "g.MulTestLinkTextSentTo", new String[] { m } ); 
                else
                {
                    setErrorMessage( PhoneUtils.getSmsErrorLangKey(ct), new String[] { user.getFullname(), m } );
                    setErrorMessage( "g.MulTestLinkTextNOTSentTo", new String[] { m } );
                }                     
            }
            
            return "StayInSamePlace";
        }        
        catch( STException e )
        {
            setMessage( e );
            return "StayInSamePlace";
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorUtils.sendDeviceChangeRestartUrl() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) );
            setMessage( e );
            return "StayInSamePlace";
        }        
        
    }
    

    public String processContinueWithoutMicrophone()
    {
        RcCheck rc = refBean.getRcCheck();        
        try
        {            
            if( rc==null )
            {
                LogService.logIt( "ProctorUtils.processContinueWithoutMicrophone() RcCheck is null" );
                return conditionUrlForSessionLossGet("/index.xhtml" );
            }
        
            if( refBean.getRefUserType()==null )
            {
                LogService.logIt( "ProctorUtils.processContinueWithoutMicrophone() RcCheck is null" );
                return conditionUrlForSessionLossGet("/index.xhtml" );
            }
                        
            if( refBean.getRefUserType().getIsCandidate() && refBean.getRequiresAudioVideoCandidateUpload() )
                throw new STException( "g.RCPPPMicrophoneRequired", new String[]{rc.getRcCheckName()} );
            
            refBean.setRecDevs(0);
            proctorBean.setCameraOptOut(true);
            
            RefUtils refUtils = RefUtils.getInstance();
            
            RefPageType rpt = refBean.getRefPageType();
            //RefPageType rpt = refUtils.getNextPageTypeForRefProcess();            
            //refBean.setRefPageType(rpt);      
            return conditionUrlForSessionLossGet( refUtils.getViewFromPageType( refBean.getRefPageType() ) );
        }        
        catch( STException e )
        {
            setMessage( e );
            return "StayInSamePlace";
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorUtils.processContinueWithoutMicrophone() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) );
            setMessage( e );
            return "StayInSamePlace";
        }        
    }
    
    public String processContinueWithoutCamera()
    {
        RcCheck rc = refBean.getRcCheck();        
        try
        {            
            if( rc==null )
            {
                LogService.logIt( "ProctorUtils.processContinueWithoutCamera() RcCheck is null" );
                return conditionUrlForSessionLossGet("/index.xhtml" );
            }
        
            if( refBean.getRefUserType()==null )
            {
                LogService.logIt( "ProctorUtils.processContinueWithoutCamera() RcCheck is null" );
                return conditionUrlForSessionLossGet("/index.xhtml" );
            }
                        
            if( refBean.getRefUserType().getIsCandidate() && rc.getRcCandidatePhotoCaptureType().getIsRequired() )
                throw new STException( "g.RCPPPhotoRequired", new String[]{rc.getRcCheckName()} );

            if( refBean.getRefUserType().getIsRater() && rc.getRcRaterPhotoCaptureType().getIsRequired() )
                throw new STException( "g.RCPPPhotoRequired", new String[]{rc.getRcCheckName()} );
            
            refBean.setRecDevs(0);
            proctorBean.setCameraOptOut(true);
            
            RefUtils refUtils = RefUtils.getInstance();
            
            RefPageType rpt = refBean.getRefPageType();
            if( rpt.getIsAnyPhotoCapture() )
            {
                rpt = refUtils.getNextPageTypeForRefProcess();
                if( rpt.getIsAnyPhotoCapture() )
                {
                    rpt = refUtils.getNextPageTypeForRefProcess();
                }
                refBean.setRefPageType(rpt); 
            }  
            //RefPageType rpt = refUtils.getNextPageTypeForRefProcess();            
            //refBean.setRefPageType(rpt);      
            return conditionUrlForSessionLossGet( refUtils.getViewFromPageType( refBean.getRefPageType() ) );
        }        
        catch( STException e )
        {
            setMessage( e );
            return "StayInSamePlace";
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorUtils.processContinueWithoutCamera() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) );
            setMessage( e );
            return "StayInSamePlace";
        }        
    }

    public String processCheckForCameraAgain()
    {
        RcCheck rc = refBean.getRcCheck();        
        try
        {            
            if( rc==null )
            {
                LogService.logIt( "ProctorUtils.processCheckForCameraAgain() RcCheck is null. Sending back to RC Process." );
                return RefUtils.getInstance().processReturnToRefCheckProcess();
            }
        
            if( refBean.getRefUserType()==null )
            {
                LogService.logIt( "ProctorUtils.processCheckForCameraAgain() RefBean.refUserType is null Sending back to RC Process." );
                return RefUtils.getInstance().processReturnToRefCheckProcess();
            }
            
            proctorBean.clearBean();
            proctorBean.init(rc, refBean.getRefUserType() );

            return conditionUrlForSessionLossGet( "/pp/browser-precheck.xhtml" );
            
        }        
        //catch( STException e )
        //{
        //    setMessage( e );
        //    return "StayInSamePlace";
        //}        
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorUtils.processCheckForCameraAgain() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) );
            setMessage( e );
            return "StayInSamePlace";
        }        
    }

    
    public String processStopProcessForNow()
    {
        RcCheck rc = refBean.getRcCheck();
        LogService.logIt( "ProctorUtils.processStopProcessForNow() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) );
        String fullRestartUrl = getFullRestartUrl();
        String refCheckTypeName = refBean.getRcCheck().getRcCheckName();
        
        refBean.clearBean();
        refBean.setStrParam1(fullRestartUrl);
        refBean.setStrParam2(refCheckTypeName);
        LogService.logIt( "ProctorUtils.processStopProcessForNow() Going to /ref/exit-temp.xhtml" );
        return conditionUrlForSessionLossGet( "/ref/exit-temp.xhtml" );         
    }

    public String conditionUrlForSessionLossGet( String url )
    {
        return conditionUrlForSessionLossGet( url, true );
    }

    public String conditionUrlForSessionLossGet( String url, boolean includeRedirect )
    {
        if( refBean==null || refBean.getActiveAccessCodeX()==null || refBean.getActiveAccessCodeX().isBlank() || url==null || url.isBlank() )
            return url;
        
        if( includeRedirect && !url.contains("faces-redirect=") )
            url += (url.contains("?") ? "&" : "?") + "faces-redirect=true";
        
        if( !url.contains( "acidx=") )
            url += (url.contains("?") ? "&" : "?") + "acidx=" + refBean.getActiveAccessCodeX();
        
        if( !url.contains("refpagex=") && refBean.getRefPageType()!=null )
            url += (url.contains("?") ? "&" : "?") + "refpagex=" + refBean.getRefPageType().getRefPageTypeId();

        if( refBean.getRcCheck()!=null  && !url.contains("rcide=") )
            url += (url.contains("?") ? "&" : "?") + "rcide=" + refBean.getRcCheckIdEncrypted();            
        
        if( refBean.getRcRaterIdEncrypted()!=null && !refBean.getRcRaterIdEncrypted().isBlank() && !url.contains("rcride=") )
            url += (url.contains("?") ? "&" : "?") + "rcride=" + refBean.getRcRaterIdEncrypted();            
        
        return url;
    }
    
    
    public boolean getBoolean1() {
        return boolean1;
    }

    public void setBoolean1(boolean boolean1) {
        this.boolean1 = boolean1;
    }
    
}
