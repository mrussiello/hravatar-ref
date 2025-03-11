/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.file;

import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import static com.tm2ref.file.BaseFileUploadThread.convertToByteArray;
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
public class RaterFileUploadThread extends BaseFileUploadThread
{
    int orgId = 0;
    int uploadedUserFileTypeId = 0;
    int mediaTypeId = MediaType.VIDEO.getMediaTypeId();
    //OnlineProctoringType onlineProctoringType;
    // int suspiciousActivityThresholdTypeId = 0;
    //RemoteProctorEvent remoteProctorEventFmTestBean = null;

    private static Map<String,Date> rcCheckLastUploadDateMap = null;

    private static final int MIN_SECS_BETWEEN_UPLOADS = 10;
    private static Date lastCleaning = null;


    public RaterFileUploadThread( InputStream strm, String filename, int fileSize, String mime, long rcCheckId, long rcRaterId, int rcItemId, int orgId, int uploadedUserFileTypeId, int mediaTypeId) throws Exception
    {
        if( orgId<=0 || rcCheckId<=0 || fileSize <= 200 )
            throw new Exception( "Parameters invalid. orgId=" + orgId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", rcItemId=" + rcItemId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", fileSize=" + fileSize );

        this.strm=strm;
        this.initialFilename = filename;
        this.initialFileSize=fileSize;
        this.initialMime=mime;
        this.rcCheckId = rcCheckId;
        this.rcRaterId=rcRaterId;
        this.rcItemId=rcItemId;
        this.orgId=orgId;
        this.uploadedUserFileTypeId=uploadedUserFileTypeId;
        this.mediaTypeId=mediaTypeId;

        FileContentType fct = FileContentType.getFileContentTypeFromContentType( initialMime, initialFilename );
        if( fct == null )
            throw new Exception( "RaterFileUploadThread.initRaterFileUploadThread() File ContentType not recognized. " + initialFilename + ", mime=" + initialMime );

        if( !fct.isImage() )
            throw new Exception( "RaterFileUploadThread.initRaterFileUploadThread() FileContentType is not an image. " + initialFilename + ", mime=" + initialMime );
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
            LogService.logIt( e, "RaterFileUploadThread.cleanMap() " );
        }
    }


    public boolean isValid(String filename)
    {
        try
        {
            if( initialMime == null || initialMime.length() == 0 )
                return false;

            UploadedFileMediaType ufmt = UploadedFileMediaType.AUDIOVIDEO_ONLY;

            return ufmt.isValid( initialMime, filename );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RaterFileUploadThread.isValid() " + toString() );
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
            LogService.logIt(e, "RaterFileUploadThread.run() " + toString() );
        }
    }


    public void doFileUpload()
    {
        RcUploadedUserFile uuf = null;
        FileUploadFacade fuf = null;

        try
        {
            if( rcCheckId<=0 || rcRaterId<=0 || rcItemId<=0 || initialFileSize <= 200 )
                throw new Exception( "Parameters invalid. rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", rcItemId=" + rcItemId );

            if( uploadedUserFileTypeId!=UploadedUserFileType.REF_CHECK_RATER_COMMENT.getUploadedUserFileTypeId() )
                throw new Exception( "uploadedUserFileTypeId is invalid. uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", rcItemId=" + rcItemId);

            initMap();

            Date d = rcCheckLastUploadDateMap.get(rcCheckId + "-" + rcRaterId + "-" + rcItemId);

            if( d!=null )
            {
                Calendar cal = new GregorianCalendar();
                cal.add( Calendar.SECOND, -1*MIN_SECS_BETWEEN_UPLOADS );
                if( d.after(cal.getTime() ) )
                    Thread.sleep( 1000*MIN_SECS_BETWEEN_UPLOADS );
            }

            rcCheckLastUploadDateMap.put(rcCheckId + "-" + rcRaterId + "-" + rcItemId, new Date());

            fuf = FileUploadFacade.getInstance();

            uuf = fuf.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(rcCheckId, rcRaterId, rcItemId, uploadedUserFileTypeId );

            FileXferUtils xfer = new FileXferUtils(); //  FileXferUtils.getInstance();
            // String dirBase = RuntimeConstants.getStringValue( "userFileUploadBaseDir" );   // /hra or /ful/hra or locals
            String directory;

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

                // LogService.logIt( "RaterFileUploadThread.run() No existing UUF Found. Creating a new one. " );
                uuf = new RcUploadedUserFile();
                uuf.setRcCheckId(rcCheckId);
                uuf.setRcRaterId(rcRaterId);
                uuf.setRcItemId(rcItemId);
                uuf.setUserId(userId);
                uuf.setCreateDate( new Date() );
                uuf.setR1( orgId );
                uuf.setR2( rcCheckId );
            }

            uuf.setConversionStatusTypeId( ConversionStatusType.NOT_STARTED.getConversionStatusTypeId() );
            uuf.setUploadedUserFileTypeId( uploadedUserFileTypeId );
            uuf.setLastUpload( new Date() );

            FileContentType fct = FileContentType.getFileContentTypeFromContentType( initialMime, initialFilename );

            uuf.setFilename(initialFilename);
            uuf.setFileContentTypeId( fct.getFileContentTypeId() );
            uuf.setInitialMime( initialMime );
            uuf.setMime( initialMime );
            uuf.setInitialFileSize(initialFileSize);
            uuf.setInitialFilename(initialFilename);
            uuf.setInitialFileContentTypeId( fct.getFileContentTypeId() );
            uuf.setInitialFileStatusTypeId( 0 );

            String filenameStub = "rtrcmnt-" + rcCheckId + "-" + rcRaterId + "-" + rcItemId;

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

            String newFilename = filenameStub + "." + fct.getBaseExtension();

            directory = uuf.getDirectory();

            byte[] bytes = null;

            bytes = convertToByteArray( strm );

            if( bytes==null || bytes.length<=0 )
                throw new Exception( "bytes appears missing. bytes.len=" + (bytes==null ? "null" : bytes.length ) );

            // remove temp file.
            strm.close();
            
            uuf.setFileSize( bytes.length );

            strm = new ByteArrayInputStream( bytes );
            
            BucketType bt = RuntimeConstants.getBooleanValue( "useAwsTestFoldersForProctoring" ) ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING;
            // BucketType bt = BucketType.PROCTORRECORDING;

            FileXferUtils.init();

            //if( !FileXferUtils.useAws )
            //    directory = "/proctorrecordings" + directory;
            // now save file. Do this last since it can take some time and can cause some issues if multiple images coming in close together.
            // xfer.saveFileToAws(directory, newFilename, strm, initialFileSize, initialMime, bt.getBucketTypeId(), true );   // long length, String contentType, int bucketTypeId, boolean force2Aws
            xfer.saveFile( directory, newFilename, strm, initialMime, initialFileSize, bt.getBucketTypeId(), true );

            uuf.setOriginalSavedFilename(newFilename);
            uuf.setFilename(newFilename);
            boolean isAudio = mediaTypeId==MediaType.AUDIO.getMediaTypeId();
            uuf.setFileProcessingTypeId(isAudio ? UploadedFileProcessingType.LISTENINGONLY_S2T.getUploadedFileProcessingTypeId() : UploadedFileProcessingType.VIEWING_S2T.getUploadedFileProcessingTypeId() );

            fuf.saveRcUploadedUserFile(uuf);

            // LogService.logIt( "RaterFileUploadThread.run() Completed saving! to " + directory + "/" + newFilename );

            Tracker.addMediaFileUpload();

            cleanMap();
        }
        catch( STException | FileUploadException e )
        {
            String msg = "RaterFileUploadThread.doFileUpload()  " + e.toString() + ", rcCheckId="  + rcCheckId + " filename=" + initialFilename + ", mime=" + initialMime + ", size=" + initialFileSize;
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
                    LogService.logIt( ee, "RaterFileUploadThread.doFileUpload().Exception() Saving " + uuf.toString() );
                }
            }

            if( rcCheckId>0 )
                RcCheckLogUtils.createRcCheckLogEntry(rcCheckId, rcRaterId, 0, msg, null, null );
        }
        catch( Exception e )
        {
            String msg = "RaterFileUploadThread.doFileUpload() Exception caught. rcCheckId="  + rcCheckId + " filename=" + initialFilename + ", mime=" + initialMime + ", size=" + initialFileSize;

            LogService.logIt( e, msg );

            if( uuf!=null && uuf.getRcUploadedUserFileId()>0 && fuf!=null )
            {
                uuf.appendNote( "Exception in FileUploadThread.doFileUpload() " + e.toString() );
                // uuf.setConversionStatusTypeId( ConversionStatusType.NA.getConversionStatusTypeId() );
                try
                {
                    fuf.saveRcUploadedUserFile(uuf);
                }
                catch( Exception ee )
                {
                    LogService.logIt( ee, "RaterFileUploadThread.doFileUpload().Exception() Saving " + uuf.toString() );
                }
            }

            if( rcCheckId>0 )
                RcCheckLogUtils.createRcCheckLogEntry(rcCheckId, rcRaterId, 0, msg, null, null );
        }
        finally
        {
            if( strm!=null )
            {
                try
                {
                    strm.close();
                    strm=null;
                }
                catch( Exception ee )
                {
                    LogService.logIt( ee, "RaterFileUploadThread.doFileUpload() XXX.3BB Error closing file upload input stream. uuf=" + (uuf==null ? "null" : uuf.toString()) );
                }
            }
        }
    }
}
