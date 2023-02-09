/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.proctor;

import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.file.BaseFileUploadThread;
import static com.tm2ref.file.BaseFileUploadThread.convertToByteArray;
import com.tm2ref.file.BucketType;
import com.tm2ref.file.ConversionStatusType;
import com.tm2ref.file.FileContentType;
import com.tm2ref.file.FileUploadException;
import com.tm2ref.file.FileUploadFacade;
import com.tm2ref.file.FileXferUtils;
import com.tm2ref.file.UploadedFileMediaType;
import com.tm2ref.file.UploadedFileProcessingType;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.ref.RcCheckLogUtils;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Mike
 */
public class ProctorFileUploadThread extends BaseFileUploadThread
{
    int orgId = 0;
    int uploadedUserFileTypeId = 0;
    UploadedUserFileType uploadedUserFileType;
    FileUploadFacade fuf = null;
    FileXferUtils xfer = null;
    //OnlineProctoringType onlineProctoringType;
    // int suspiciousActivityThresholdTypeId = 0;
    //RemoteProctorEvent remoteProctorEventFmTestBean = null;
    
    private static Map<String,Date> rcCheckLastUploadDateMap = null;
    
    private static int MIN_SECS_BETWEEN_UPLOADS = 10;
    private static Date lastCleaning = null;
    private boolean hasValidInfo = false;

    public ProctorFileUploadThread( InputStream strm, String filename, int fileSize, String mime, long rcCheckId, long rcRaterId, int rcItemId, int orgId, int uploadedUserFileTypeId) throws Exception
    {
        if( orgId<=0 || rcCheckId<=0 || fileSize <= 200 )
        {
            LogService.logIt( "ProctorFileUploadThread.initProctorFileUploadThread() Parameters invalid. Ignoring. orgId=" + orgId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", fileSize=" + fileSize );
            return;
            // throw new Exception( "Parameters invalid. orgId=" + orgId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", fileSize=" + fileSize );
        }

        this.strm=strm;
        this.initialFilename = filename;
        this.initialFileSize=fileSize;
        this.initialMime=mime;
        this.rcCheckId = rcCheckId;
        this.rcRaterId=rcRaterId;
        this.rcItemId=rcItemId;
        this.orgId=orgId;
        this.uploadedUserFileTypeId=uploadedUserFileTypeId;

        uploadedUserFileType = UploadedUserFileType.getValue( this.uploadedUserFileTypeId );
        
        FileContentType fct = FileContentType.getFileContentTypeFromContentType( initialMime, initialFilename );
        if( fct == null )
        {
            LogService.logIt( "ProctorFileUploadThread.initProctorFileUploadThread() Parameters invalid. Ignoring. orgId=" + orgId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", fileSize=" + fileSize );
            return;
            //throw new Exception( "ProctorFileUploadThread.initProctorFileUploadThread() File ContentType not recognized. " + initialFilename + ", mime=" + initialMime );
        }
        
        if( !uploadedUserFileType.getIsAnyRc() )
        {
            LogService.logIt( "ProctorFileUploadThread.initProctorFileUploadThread() Parameters invalid. Ignoring. orgId=" + orgId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", fileSize=" + fileSize );
            return;
            //throw new Exception( "ProctorFileUploadThread.initProctorFileUploadThread() UploadedUserFileType is invalid uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", " + initialFilename + ", mime=" + initialMime );
        }
        
        
        if( uploadedUserFileType.getIsRcPhotoOrId() && !fct.isImage()  )
        {
            LogService.logIt( "ProctorFileUploadThread.initProctorFileUploadThread() Parameters invalid. Ignoring. orgId=" + orgId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", fileSize=" + fileSize );
            return;
            //throw new Exception( "ProctorFileUploadThread.initProctorFileUploadThread() FileContentType is not an image but uploadedUserFileType is an image. " + initialFilename + ", mime=" + initialMime );
        }

        if( uploadedUserFileType.getIsRcComment()&& !fct.isAudio() && !fct.isVideo() )
        {
            LogService.logIt( "ProctorFileUploadThread.initProctorFileUploadThread() Parameters invalid. Ignoring. orgId=" + orgId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", fileSize=" + fileSize );
            return;
            //throw new Exception( "ProctorFileUploadThread.initProctorFileUploadThread() UploadedUserFileType is rater comment but FileContentType is not an audio, or video. " + initialFilename + ", mime=" + initialMime );
        }
        
        hasValidInfo = true;
    }

    public boolean getHasValidInfo()
    {
        return hasValidInfo;
    }
    
    private static synchronized void initMap()
    {
        if( rcCheckLastUploadDateMap==null )
            rcCheckLastUploadDateMap=new ConcurrentHashMap<>();
    }
    
    private static synchronized void cleanMap()
    {
        if( rcCheckLastUploadDateMap==null )
            return;

        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.MINUTE, -15 );

        if( lastCleaning!=null && lastCleaning.after( cal.getTime() ) )
            return;
        
        lastCleaning = new Date();
        
        cal = new GregorianCalendar();
        cal.add( Calendar.SECOND, -1*MIN_SECS_BETWEEN_UPLOADS );
        Date d;
        Set<String> keys = rcCheckLastUploadDateMap.keySet();
        List<String> keyList = new ArrayList<>();
        keyList.addAll( keys );
        
        try
        {
            for( String k : keyList )
            {            
                d = rcCheckLastUploadDateMap.get(k);

                if( d==null || d.before(cal.getTime()))
                    rcCheckLastUploadDateMap.remove(k);
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorFileUploadThread.cleanMap() " );
        }
    }    
    
    
    public boolean isValid(String filename)
    {
        try
        {
            if( initialMime == null || initialMime.length() == 0 )
                return false;

            UploadedFileMediaType ufmt = uploadedUserFileType.getIsRcPhotoOrId() ? UploadedFileMediaType.IMG_ONLY : UploadedFileMediaType.AUDIOVIDEO_ONLY;
            return ufmt.isValid( initialMime, filename );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ProctorFileUploadThread.isValid() " + toString() );
            errMsg = e.toString() + ", " +e.getMessage();            
            return false;
        }
    }



    public void performFileUpload()
    {
        doFileUpload();
    }


    @Override
    public void run()
    {
        try
        {
            doFileUpload();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ProctorFileUploadThread.run() " + toString() );
        }
    }
    

    public void doFileUpload()
    {
        if( !hasValidInfo )
        {
            LogService.logIt( "ProctorFileUploadThread.doFileUpload() Object does not have valid info. Ignoring." );            
            return;
        }
        
        // LogService.logIt( "ProctorFileUploadThread.doFileUpload() AAA " );         
        RcUploadedUserFile uuf = null;
        // FileUploadFacade fuf = null;
        int originalMaxThumbIndex = 0;
        try
        {
            if( rcCheckId<=0 || initialFileSize <= 200 )
            {
                LogService.logIt( "ProctorFileUploadThread.doFileUpload() Parameters invalid. rcCheckId=" + rcCheckId );
                return;
                // throw new Exception( "Parameters invalid. rcCheckId=" + rcCheckId );
            }
                        
            initMap();
            
            Date d = rcCheckLastUploadDateMap.get(rcCheckId + "-" + rcRaterId);
            
            if( d!=null )
            {
                Calendar cal = new GregorianCalendar();
                cal.add( Calendar.SECOND, -1*MIN_SECS_BETWEEN_UPLOADS );
                if( d.after(cal.getTime() ) )
                    Thread.sleep( 1000*MIN_SECS_BETWEEN_UPLOADS );
            }
            
            rcCheckLastUploadDateMap.put(rcCheckId + "-" + rcRaterId, new Date());
            
            if( fuf==null )
                fuf = FileUploadFacade.getInstance();

            // LogService.logIt( "ProctorFileUploadThread.doFileUpload() BBB " );         
            uuf = fuf.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(rcCheckId, rcRaterId, rcItemId, uploadedUserFileTypeId );

            if( xfer==null )
                xfer = new FileXferUtils(); //  FileXferUtils.getInstance();
            // String dirBase = RuntimeConstants.getStringValue( "userFileUploadBaseDir" );   // /hra or /ful/hra or locals
            String directory = null;

            boolean create = false;
            
            if( uuf == null )
            {
                RcFacade rcFacade = RcFacade.getInstance();
                long userId = 0;
                if( rcRaterId>0 )
                {
                    RcRater rtr = rcFacade.getRcRater(rcRaterId, false );
                    if( rtr!=null )
                        userId = rtr.getUserId();
                }
                else
                {
                    RcCheck rc = rcFacade.getRcCheck(rcCheckId, false );
                    if( rc!=null )
                        userId = rc.getUserId();
                }
                
                // LogService.logIt( "ProctorFileUploadThread.doFileUpload() No existing UUF Found. Creating a new one. " );
                create = true;
                uuf = new RcUploadedUserFile();
                uuf.setRcCheckId(rcCheckId);
                uuf.setUploadedUserFileTypeId( uploadedUserFileTypeId );
                uuf.setRcRaterId(rcRaterId);
                uuf.setRcItemId(rcItemId);
                uuf.setUserId(userId);
                uuf.setCreateDate( new Date() );
                uuf.setR1( orgId );
                uuf.setR2( rcCheckId );
                //directory = dirBase + uuf.getDirectory();                                
            }
            else
            {
                originalMaxThumbIndex = uuf.getMaxThumbIndex();
                //if( rcRaterId>0 && uuf.getTestEventId()<=0 )
                //    uuf.setTestEventId(rcRaterId);
            }

            // LogService.logIt( "ProctorFileUploadThread.doFileUpload() CCC create=" + create );         
            
            uuf.setUploadedUserFileTypeId( uploadedUserFileTypeId );
            uuf.setLastUpload( new Date() );
            //uuf.setConversionStatusTypeId( ConversionStatusType.NA.getConversionStatusTypeId() );

            FileContentType fct = FileContentType.getFileContentTypeFromContentType( initialMime, initialFilename );

            UploadedUserFileType uuft = uploadedUserFileType; //  UploadedUserFileType.getValue( uploadedUserFileTypeId );
                        
            String filenameStub = null;
            
            if( uuft.getIsRcComment() )
                filenameStub = "cmnt-";
            else if( uuft.getIsAnyId() )
                filenameStub = uuft.getIsRcRaterId() ? "rtrimg-" : "idimg-";
            else
                filenameStub = "img-";

            // Need to check for legacy thumb filename codes
            if( !uuft.getIsRcComment() && !create && uuf.getThumbFilename()!=null && !uuf.getThumbFilename().isBlank() && uuf.getThumbFilename().equalsIgnoreCase(filenameStub + rcCheckId + "-" + rcRaterId + ".IDX." + fct.getBaseExtension()) )
                filenameStub = filenameStub + rcCheckId + "-" + rcRaterId; 
            else
                filenameStub = filenameStub + rcCheckId + "-" + rcRaterId + "-" + rcItemId;
        
            // this is to deal with legacy rc checks
            
            if( !isValid(initialFilename) )
            {
                // uuf.setConversionStatusTypeId( ConversionStatusType.CANCELED.getConversionStatusTypeId() );
                if( uuf.getRcUploadedUserFileId()>0 )
                {
                    uuf.appendNote( "Uploaded file format " + fct.getBaseContentType() + ", " + initialFilename + " not accepted as a valid file. " + (errMsg==null ? "" : errMsg ) );
                    errMsg = null;
                    fuf.saveRcUploadedUserFile(uuf);
                }
                return;
            }
            
            // LogService.logIt( "ProctorFileUploadThread.doFileUpload() DDD " );         
            
            if( uuft.getIsRcComment() )
            {
                doUploadRcComment( filenameStub, uuf, fct );
                return;
            }

            if( maxWidth>0 || maxHeight>0 )
            {
                String pi = maxWidth > 0 ? "maxwidth~" + maxWidth : "";
                
                if( maxHeight>0 )
                    pi += (pi.length()>0 ? "~" : "" ) + "maxheight~" + maxHeight;

                uuf.setProcessingParams(pi);                
            }

            uuf.setMaxThumbIndex( uuf.getMaxThumbIndex()+1 );      
            
            
            String newFilename = filenameStub + "." + uuf.getMaxThumbIndex() + "." + fct.getBaseExtension();
            
            directory = uuf.getDirectory();

            byte[] bytes = null;

            bytes = convertToByteArray( strm );

            if( bytes==null || bytes.length<=0 )
                throw new Exception( "bytes appears missing. bytes.len=" + (bytes==null ? "null" : bytes.length ) );
            
            strm = new ByteArrayInputStream( bytes );

            BucketType bt = RuntimeConstants.getBooleanValue( "useAwsTestFoldersForProctoring" ) ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING;
            // BucketType bt = BucketType.PROCTORRECORDING;
            
            FileXferUtils.init();
            
            // LogService.logIt( "ProctorFileUploadThread.doFileUpload() EEE " );         
            //if( !FileXferUtils.useAws )
            //    directory = "/proctorrecordings" + directory;
            // now save file. Do this last since it can take some time and can cause some issues if multiple images coming in close together. 
            // xfer.saveFileToAws(directory, newFilename, strm, initialFileSize, initialMime, bt.getBucketTypeId(), true );   // long length, String contentType, int bucketTypeId, boolean force2Aws
            xfer.saveFile( directory, newFilename, strm, initialMime, initialFileSize, bt.getBucketTypeId(), true );

            if( create )
            {                                
                uuf.setFileContentTypeId( fct.getFileContentTypeId() );
                // uuf.setLastUpload( new Date() );
                //uuf.setInitialMime( initialMime );
                uuf.setMime( initialMime );
                //uuf.setInitialFileSize(initialFileSize);
                //uuf.setInitialFilename(initialFilename);
                //uuf.setInitialFileContentTypeId( fct.getFileContentTypeId() );                
                //uuf.setOriginalSavedFilename(newFilename);
                uuf.setFilename(newFilename);
                uuf.setThumbFilename( filenameStub + ".IDX." + fct.getBaseExtension() );
            }
            
            //uuf.setConversionStatusTypeId( ConversionStatusType.NA.getConversionStatusTypeId() );
            //uuf.setFileProcessingTypeId( UploadedFileProcessingType.VIEWING_ONLY.getUploadedFileProcessingTypeId() );
            uuf.setLastUpload( new Date() );
            setImageMetadata( bytes, uuf, fct );
            uuf.setThumbWidth( uuf.getWidth() );
            uuf.setThumbHeight( uuf.getHeight() );
            uuf.setFileSize( bytes.length );
                
            // LogService.logIt( "ProctorFileUploadThread.doFileUpload() FFF " );         
            fuf.saveRcUploadedUserFile(uuf);
            
            // LogService.logIt( "ProctorFileUploadThread.doFileUpload() Completed saving! to " + directory + "/" + newFilename );

            Tracker.addImageFileUpload();
            
            cleanMap();            
        }
        catch( STException | FileUploadException e )
        {            
            String msg = "ProctorFileUploadThread.doFileUpload()  " + e.toString() + ", rcCheckId="  + rcCheckId + " filename=" + initialFilename + ", mime=" + initialMime + ", size=" + initialFileSize;            
            LogService.logIt( msg );
            if( uuf!=null && uuf.getRcUploadedUserFileId()>0 && fuf!=null )
            {
                uuf.appendNote( "Exception in FileUploadThread.run() " + msg );
                try
                {
                    fuf.saveRcUploadedUserFile(uuf);
                }
                catch( Exception ee )
                {
                    LogService.logIt( ee, "ProctorFileUploadThread.doFileUpload().Exception() Saving " + uuf.toString() );                    
                }
            }            
            if( rcCheckId>0 )
                RcCheckLogUtils.createRcCheckLogEntry(rcCheckId, rcRaterId, 0, msg, null, null );
        }
        catch( Exception e )
        {
            String msg = "ProctorFileUploadThread.doFileUpload() Exception caught. rcCheckId="  + rcCheckId + " filename=" + initialFilename + ", mime=" + initialMime + ", size=" + initialFileSize;
            
            LogService.logIt( e, msg );
            
            if( uuf!=null && uuf.getRcUploadedUserFileId()>0 && fuf!=null )
            {
                uuf.setMaxThumbIndex(originalMaxThumbIndex);
                uuf.appendNote( "Exception in ProctorFileUploadThread.doFileUpload() " + e.toString() );
                // uuf.setConversionStatusTypeId( ConversionStatusType.NA.getConversionStatusTypeId() );
                try
                {
                    fuf.saveRcUploadedUserFile(uuf);
                }
                catch( Exception ee )
                {
                    LogService.logIt( ee, "ProctorFileUploadThread.doFileUpload().Exception() Saving " + uuf.toString() );
                    
                }
            }
            
            msg += ", " + e.toString();
            
            if( rcCheckId>0 )
                RcCheckLogUtils.createRcCheckLogEntry(rcCheckId, rcRaterId, 0, msg, null, null );
        }
    }

    
    private void doUploadRcComment( String filenameStub, RcUploadedUserFile uuf, FileContentType fct )
    {
        try
        {
            if( maxWidth>0 || maxHeight>0 )
            {
                String pi = maxWidth > 0 ? "maxwidth~" + maxWidth : "";
                
                if( maxHeight>0 )
                    pi += (pi.length()>0 ? "~" : "" ) + "maxheight~" + maxHeight;

                uuf.setProcessingParams(pi);                
            }

            //uuf.setMaxThumbIndex( uuf.getMaxThumbIndex()+1 );             
            String newFilename = filenameStub + "." + fct.getBaseExtension();
            
            String directory = uuf.getDirectory();

            //byte[] bytes = null;

            //bytes = convertToByteArray( strm );

            //if( bytes==null || bytes.length<=0 )
            //    throw new Exception( "bytes appears missing. bytes.len=" + (bytes==null ? "null" : bytes.length ) );
            
            //strm = new ByteArrayInputStream( bytes );

            BucketType bt = RuntimeConstants.getBooleanValue( "useAwsTestFoldersForProctoring" ) ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING;
            // BucketType bt = BucketType.PROCTORRECORDING;
            
            FileXferUtils.init();
            
            //if( !FileXferUtils.useAws )
            //    directory = "/proctorrecordings" + directory;
            // now save file. Do this last since it can take some time and can cause some issues if multiple images coming in close together. 
            // xfer.saveFileToAws(directory, newFilename, strm, initialFileSize, initialMime, bt.getBucketTypeId(), true );   // long length, String contentType, int bucketTypeId, boolean force2Aws
            xfer.saveFile( directory, newFilename, strm, initialMime, initialFileSize, bt.getBucketTypeId(), true );

            if( uuf.getRcUploadedUserFileId()<=0 )
            {                                
                uuf.setOriginalSavedFilename(newFilename);
                //uuf.setThumbFilename( filenameStub + ".IDX." + fct.getBaseExtension() );
            }
            
            uuf.setLastUpload( new Date() );
            uuf.setConversionStatusTypeId( ConversionStatusType.NOT_STARTED.getConversionStatusTypeId() );
            uuf.setFilename(newFilename);
            uuf.setFileContentTypeId( fct.getFileContentTypeId() );
            uuf.setFileProcessingTypeId( fct.isVideo() ? UploadedFileProcessingType.VIEWING_S2T.getUploadedFileProcessingTypeId() : UploadedFileProcessingType.LISTENINGONLY_S2T.getUploadedFileProcessingTypeId() );
            uuf.setInitialMime( initialMime );
            uuf.setMime( initialMime );
            uuf.setInitialFileSize(initialFileSize);
            uuf.setInitialFilename(newFilename);
            uuf.setInitialFileContentTypeId( fct.getFileContentTypeId() );                
            uuf.setLastUpload( new Date() );
                
            fuf.saveRcUploadedUserFile(uuf);
            
            // LogService.logIt( "ProctorFileUploadThread.doFileUpload() Completed saving! to " + directory + "/" + newFilename );

            Tracker.addMediaFileUpload();
            
            cleanMap();            
            
        }
        catch( STException | FileUploadException e )
        {            
            String msg = "ProctorFileUploadThread.doUploadRcComment() " + e.toString() + ", rcCheckId="  + rcCheckId + " filename=" + initialFilename + ", mime=" + initialMime + ", size=" + initialFileSize;            
            LogService.logIt( msg );
            if( uuf!=null && uuf.getRcUploadedUserFileId()>0 && fuf!=null )
            {
                uuf.appendNote( "Exception in FileUploadThread.doUploadRcComment() " + msg );
                try
                {
                    fuf.saveRcUploadedUserFile(uuf);
                }
                catch( Exception ee )
                {
                    LogService.logIt( ee, "ProctorFileUploadThread.doUploadRcComment().Exception() Saving " + uuf.toString() );                    
                }
            }            
            if( rcCheckId>0 )
                RcCheckLogUtils.createRcCheckLogEntry(rcCheckId, rcRaterId, 0, msg, null, null );
        }
        
        catch(Exception e)
        {
            String msg = "ProctorFileUploadThread.doUploadRcComment() Exception caught. rcCheckId="  + rcCheckId + " filename=" + initialFilename + ", mime=" + initialMime + ", size=" + initialFileSize;            
            LogService.logIt( e, msg );            
            if( uuf!=null && uuf.getRcUploadedUserFileId()>0 && fuf!=null )
            {
                uuf.appendNote( "Exception in ProctorFileUploadThread.doUploadRcComment() " + e.toString() );
                // uuf.setConversionStatusTypeId( ConversionStatusType.NA.getConversionStatusTypeId() );
                try
                {
                    fuf.saveRcUploadedUserFile(uuf);
                }
                catch( Exception ee )
                {
                    LogService.logIt( ee, "ProctorFileUploadThread.doUploadRcComment() Exception Saving " + uuf.toString() );                    
                }
            }
            
            msg += ", " + e.toString();
            
            if( rcCheckId>0 )
                RcCheckLogUtils.createRcCheckLogEntry(rcCheckId, rcRaterId, 0, msg, null, null );
            
        }
    }

}
