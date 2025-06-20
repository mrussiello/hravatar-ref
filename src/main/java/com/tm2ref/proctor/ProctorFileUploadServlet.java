/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.proctor;

import com.tm2ref.corp.CorpBean;
import com.tm2ref.corp.CorpUtils;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.faces.HttpReqUtils;
import com.tm2ref.file.BaseFileUploadServlet;
import com.tm2ref.file.FileContentType;
import com.tm2ref.file.FileUploadErrorType;
import com.tm2ref.file.FileUploadException;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.global.STException;
import com.tm2ref.ref.RcCheckLogUtils;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.ref.RefBean;
import com.tm2ref.ref.RefUtils;
import com.tm2ref.service.AdminEmailUtils;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.UserBean;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Date;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 *
 * @author Mike
 */
@WebServlet(name = "ProctorFileUploadServlet", urlPatterns = {"/ppfupload"})
@MultipartConfig(fileSizeThreshold=1024*1024*2, // 2MB
                 maxFileSize=1024*1024*105,      // 105MB
                 maxRequestSize=1024*1024*105)   // 105MB
public class ProctorFileUploadServlet extends BaseFileUploadServlet
{

    @Inject
    RefBean refBean;
    
    @Inject
    RefUtils refUtils;
    
    @Inject
    CorpBean corpBean;
    
    @Inject
    CorpUtils corpUtils;

    @Inject
    UserBean userBean;
    
    @Inject
    ProctorBean  proctorBean;
    
    

    
    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res )
            throws ServletException, IOException
    {        
        //int ndseq = 0;
        //int snseq = 0;
        String rcid = null;
        String rtrid = null;
        String ritmid = null;
        
        // Uploaded file type. Defaults to 100 (REMOTE_PROCTORING_IMAGES)
        String uft = null;
        String nthrd = null;
        
        //long actId = 0;
        long rcCheckId = 0;
        long rcRaterId = 0;
        int rcItemId = 0;
        int uploadedUserFileTypeId = 0; // UploadedUserFileType.REF_CHECK_IMAGES.getUploadedUserFileTypeId();
        // long rcRaterId = 0;
        int fileSize = 0;
        String filename = null;
        String mime = null;
        
        String blobType = null;
        int blobSize = 0;
        String info = "";
        
        boolean noThread = false;

        try
        {
            if( refBean!=null && refBean.getAdminOverride() )
            {
                try
                {
                    res.setContentType( "text/plain" );
                    Writer sw = res.getWriter();
                    sw.write( "SUCCESS");
                    sw.flush();
                    sw.close();
                    return;
                }
                catch( Exception ee )
                {
                    LogService.logIt(ee, "ProctorFileUploadServlet.doPost() AAA Exception while sending success message. " + info );
                }                
            }
            
            // session issue? Try to recover.
            if( refBean.getRcCheck()==null )
            {
                LogService.logIt( "ProctorFileUploadServlet.doPost() refBean.rcCheck is null. checking for recoverability." );

                String acidx = HttpReqUtils.getStringReqParam("acidx", req);

                refUtils.setProctorBean(proctorBean);
                refUtils.setRefBean(refBean);
                refUtils.setCorpBean(corpBean);
                refUtils.setCorpUtils(corpUtils);
                refUtils.setUserBean(userBean);
                refUtils.setHttpServletRequest(req);
                refUtils.setHttpServletResponse(res);
                corpUtils.setCorpBean(corpBean);
                corpUtils.setUserBean(userBean);
                corpUtils.setHttpServletRequest( req );

                // cannot recover
                if( requestHasParamsForRecovery(req) )
                {
                    LogService.logIt( "ProctorFileUploadServlet.doPost() recoverable session error. Recovering Session. acidx=" + acidx );

                    try
                    {                    
                        String nextViewId = refUtils.checkRepairSession(500, true);
                        LogService.logIt( "ProctorFileUploadServlet.doPost() recoverable Recovering Session. After checkRepair() nextViewId=" + nextViewId );
                    }
                    catch( Exception e )
                    {
                        LogService.logIt( e, "ProctorFileUploadServlet.doPost() Error Recovering Session for acidx=" + acidx );
                    }
                }
                else
                    LogService.logIt( "ProctorFileUploadServlet.doPost() refBean.rcCheck is null. Session appears to not be recoverable." );
            }
            
            
            // LogService.logIt( "ProctorFileUploadServlet.processRequest() AAA " );
            
            Part p = getPart(req, "rcid", true );
            rcid = readString( p );
            info="rcid=" + rcid;

            if( rcid == null || rcid.isEmpty() )
                throw new FileUploadException( FileUploadErrorType.MISSING_PARAMETER_SERVER.getFileUploadErrorTypeId(), "invalid rcid=" + rcid );

            try
            {
                rcCheckId = Long.parseLong( EncryptUtils.urlSafeDecrypt(rcid) );
            }
            catch( NumberFormatException e )
            {
                LogService.logIt( "ProctorFileUploadServlet.doPost() AAA.2 Parsing rcid=" + rcid + ", after decryypt=" +  EncryptUtils.urlSafeDecrypt(rcid));
            }
            catch( Exception e )
            {
                LogService.logIt( e, "ProctorFileUploadServlet.doPost() AAA.3 Parsing rcid=" + rcid);
            }
            

            info = "rcCheckId=" + rcCheckId + ", " + info;
            
            if( rcCheckId <= 0 )
                throw new FileUploadException( FileUploadErrorType.MISSING_PARAMETER_SERVER,  "Invalid rcCheckId - could not decode " + rcid + "." );
            
            p = getPart(req, "rtrid", true );
            rtrid = readString( p );
            if( rtrid==null )
                rtrid="";
            info="rtrid=" + rtrid;

            p = getPart(req, "ritmid", true );
            ritmid = readString( p );
            if( ritmid==null )
                ritmid="";
            info="ritmid=" + ritmid;
            
            //if( rtrid == null || rtrid.isEmpty() )
            //    throw new FileUploadException( FileUploadErrorType.MISSING_PARAMETER_SERVER.getFileUploadErrorTypeId(), "invalid rtrid=" + rtrid );

            p = getPart(req, "nthrd", false );
            if( p!=null )
            {
                nthrd = readString( p );
                info="nthrd=" + (nthrd==null ? "null" : nthrd );
                if( nthrd!=null && nthrd.equalsIgnoreCase("true") )
                    noThread = true;
            }
            
            
            // uft is uploadeduserfiletpyeId. Used here to indicate that a file is a test taker id or a photo.
            // this parameter is NOT required. Defaults to photos.
            p = getPart(req, "uft", false );
            if( p!=null )
            {
                uft = readString( p );
                info="uft=" + (uft==null ? "null" : uft );
            }

            if( uft==null || uft.isBlank() )
                throw new FileUploadException( FileUploadErrorType.UPLOADEDUSERFILETYPE_INVALID.getFileUploadErrorTypeId(), "Invalid uft=" + uft );                
            
            try
            {
                uploadedUserFileTypeId = Integer.parseInt(uft);            
            }
            catch( NumberFormatException e )
            {
                LogService.logIt("ProctorFileUploadServlet.doPost()  Parsing uft=" + uft );
            }
            
            UploadedUserFileType uploadedUserFileType = UploadedUserFileType.getValue(uploadedUserFileTypeId);
            
            if( !uploadedUserFileType.getIsAnyRc() )
                throw new FileUploadException( FileUploadErrorType.UPLOADEDUSERFILETYPE_INVALID.getFileUploadErrorTypeId(), "Invalid uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", uft=" + uft );                
            
            if( rtrid.isBlank() && (uploadedUserFileType.getIsRcComment() || uploadedUserFileType.getIsRcRaterPhoto()) )
                throw new FileUploadException( FileUploadErrorType.UPLOADEDUSERFILETYPE_INVALID.getFileUploadErrorTypeId(), "Invalid uploadedUserFileTypeId for candidate upload. uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", uft=" + uft );                

            //if( !rtrid.isBlank() && 
            //    uploadedUserFileTypeId!=UploadedUserFileType.REF_CHECK_IMAGES.getUploadedUserFileTypeId() && uploadedUserFileTypeId!=UploadedUserFileType.REF_CHECK_ID.getUploadedUserFileTypeId() && uploadedUserFileTypeId!=UploadedUserFileType.REF_CHECK_RATER.getUploadedUserFileTypeId() )
            //    throw new FileUploadException( FileUploadErrorType.UPLOADEDUSERFILETYPE_INVALID.getFileUploadErrorTypeId(), "Invalid uploadedUserFileTypeId for rater or candidate upload. uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", uft=" + uft );                
            
            if( ritmid.isBlank() && uploadedUserFileType.getIsRcComment() ) // ==UploadedUserFileType.REF_CHECK_RATER_COMMENT.getUploadedUserFileTypeId() )
                throw new FileUploadException( FileUploadErrorType.UPLOADEDUSERFILETYPE_INVALID.getFileUploadErrorTypeId(), "UploadedUserFileTypeId for rater comment but no RcItemId provided. uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", uft=" + uft + ", ritmid=" + ritmid );                
            
            Part btp = getPart(req, "blobtype", true );            
            if( btp!=null )
                blobType = readString( btp );
            
            info+=", blobType=" + blobType;            
            Part bsz = getPart(req, "blobsize", true );            
            if( bsz!=null )
                blobSize = readInt( bsz );
            
            info+= ", blobSize=" + blobSize;            
            InputStream inps = null;
            
            // String mimeType = null;
            
            FileContentType fct = null;            
            String src = null;
            

            try
            {
                if( !rtrid.isBlank() )
                    rcRaterId = Long.parseLong( EncryptUtils.urlSafeDecrypt(rtrid) );
                else
                    rcRaterId = 0;
            }
            catch( NumberFormatException e )
            {
                LogService.logIt( "ProctorFileUploadServlet.doPost() AAA.2 Parsing rtrid=" + rtrid + ", after decryypt=" +  EncryptUtils.urlSafeDecrypt(rtrid) + ", rcCheckId=" + rcCheckId);
            }
            catch( Exception e )
            {
                LogService.logIt( e, "ProctorFileUploadServlet.doPost() AAA.3 Parsing rtrid=" + rtrid + ", rcCheckId=" + rcCheckId);
            }

            info = "rcRaterId=" + rcRaterId + ", " + info;
            
            try
            {
                if( !ritmid.isBlank() )
                    rcItemId = Integer.parseInt( EncryptUtils.urlSafeDecrypt(ritmid) );
                else
                    rcItemId = 0;
            }            
            catch( NumberFormatException e )
            {
                LogService.logIt( "ProctorFileUploadServlet.doPost() AAA.2 Parsing ritmid=" + ritmid + ", after decryypt=" +  EncryptUtils.urlSafeDecrypt(ritmid) + ", rcRaterId=" + rcRaterId + ", rcCheckId=" + rcCheckId);
            }
            catch( Exception e )
            {
                LogService.logIt( e, "ProctorFileUploadServlet.doPost() AAA.3 Parsing rtrid=" + ritmid + ", rcRaterId=" + rcRaterId + ", rcCheckId=" + rcCheckId);
            }
            
            info = "rcItemId=" + rcItemId + ", " + info;
            
            // rcRaterId = 0;
            
            //if( rcRaterId <= 0 )
            //    throw new FileUploadException( FileUploadErrorType.MISSING_PARAMETER_SERVER,  "Invalid rcRaterId - could not decode " + rtrid + "." );
            
            
            // LogService.logIt( "ProctorFileUploadServlet.doPost() " + inspectRequest(req) + ", rcRaterId=" + rcRaterId );
             
            if( blobSize>0 && blobType!=null )
            {                
                blobType = blobType.toLowerCase();
                
                mime = blobType.indexOf(";")>0 ? blobType.substring(0, blobType.indexOf(";") ) : blobType;                
                fct = FileContentType.getFileContentTypeFromContentType(mime, null );                
                if( fct==null )
                    fct = rcItemId>0 ? FileContentType.VIDEO_WEBM : FileContentType.IMAGE_JPEG;
                
                filename = "upblb_" + rcRaterId + "_" + rcItemId + "_" + (new Date()).getTime() + "." + fct.getBaseExtension();
                
                fileSize = blobSize;
                
                Part fp = getPart(req, "blobfile", true );

                if( fp == null )
                    throw new FileUploadException( FileUploadErrorType.BLOG_PART_MISSING,  "Blob File Part not found. " );

                fileSize = (int) fp.getSize();
                
                info += ", fileSize=" + fileSize;
                                
                if( fileSize < 10 )
                    throw new FileUploadException( FileUploadErrorType.BYTES_MISSING_SERVER, "Blob File is not present or too small. Filesize=" + fileSize  );

                if( fileSize > 105000000 )
                    throw new FileUploadException( FileUploadErrorType.FILE_TOO_BIG_SERVER, "Blob File is too big. FileSize=" + fileSize );
                
                try
                {
                    inps = fp.getInputStream();                      
                }
                catch( IOException e )
                {
                    LogService.logIt( "ProctorFileUploadServlet.doPost() " + e.toString() + " Getting Inputstream for uploaded file. " + info );                    
                    throw new FileUploadException( FileUploadErrorType.BYTES_MISSING_SERVER, e.toString() + " Getting Inputstream for uploaded file. " + info );
                }
                
                src = "Blob";
            }
            
            else if( blobSize<=0 )
                throw new FileUploadException( FileUploadErrorType.BYTES_MISSING_SERVER, "Blob Size invalid. " + blobSize + ", blobType=" + blobType );

            else if( blobType==null)
                throw new FileUploadException( FileUploadErrorType.BYTES_MISSING_SERVER, "Blob Type is null. " + blobSize + ", blobType=" + blobType );
            
            if( inps==null )
                throw new FileUploadException( FileUploadErrorType.BYTES_MISSING_SERVER, "InputStream is null. BlobSize=" + blobSize + ", blobType=" + blobType );
            
            info += ", Source=" + src;
            
            RcFacade ef = RcFacade.getInstance();

            // OK, everything valid, let's lookup the ActHistory.
            // LogService.logIt( "ProctorFileUploadServlet.processRequest() BBB filename=" + filename + ", size=" + fileSize + ", rcRaterId=" + rcRaterId + ", Source=" + src + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId );

            RcCheck tk = null;
            
            try
            {
                tk = ef.getRcCheck(rcCheckId, false );
            }
            catch( STException e )
            {
                LogService.logIt( "ProctorFileUploadServlet.doPost() getting RcCheck. " + e.toString() + " rcCheckId=" + rcCheckId);                                    
            }
            catch( Exception e )
            {
                LogService.logIt( e, "ProctorFileUploadServlet.doPost() getting RcCheck rcCheckId=" + rcCheckId );                                    
            }
                    
            if( tk == null )
                throw new FileUploadException( FileUploadErrorType.RCCHECK_MISSING, "Cannot find RcCheck. rcCheckId=" + rcCheckId );

            boolean isCandidate = rcRaterId<=0;
            
            if( rcRaterId>0 )
            {
                RcRater rcRater = null;
                try
                {
                    rcRater = ef.getRcRater(rcRaterId, true);
                }                
                catch( STException e )
                {
                    LogService.logIt( "ProctorFileUploadServlet.doPost() getting RcRater. " + e.toString() + " rcCheckId=" + rcCheckId);                                    
                }
                
                if( rcRater==null )
                    throw new FileUploadException( FileUploadErrorType.RCRATER_MISSING, "Cannot find RcRater rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );
                if( rcRater.getRcCheckId()!=rcCheckId )
                    throw new FileUploadException( FileUploadErrorType.RCRATER_MISSING, "RcRater does not match this RcCheck. rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );                    
                if( rcRater.getIsCandidateOrEmployee() )
                    isCandidate=true;
            }
            
            
            // LogService.logIt( "ProctorFileUploadServlet.processRequest() CCC filename=" + filename + ", size=" + fileSize + ", rcRaterId=" + rcRaterId + ", Source=" + src );
            
            if( isCandidate && uploadedUserFileType.getIsRcRaterPhoto() )
                throw new FileUploadException( FileUploadErrorType.UPLOADEDUSERFILETYPE_INVALID, "UploadedUserFileTypeId is not valid for candidate. rcCheckId=" + rcCheckId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId );                

            if( !isCandidate && !uploadedUserFileType.getIsRcRaterPhoto() && !uploadedUserFileType.getIsRcRaterId() && !uploadedUserFileType.getIsRcComment())
                throw new FileUploadException( FileUploadErrorType.UPLOADEDUSERFILETYPE_INVALID, "UploadedUserFileTypeId is not valid for rater. rcCheckId=" + rcCheckId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId );                
                        
            if( isCandidate && uploadedUserFileType.getIsRcCandidatePhoto() && !tk.getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture() )
                throw new FileUploadException( FileUploadErrorType.FILEUPLOAD_NOT_NEEDED, "Candidate Photo Images not required for this rcCheckId=" + rcCheckId );                

            if( isCandidate && uploadedUserFileType.getIsRcCandidateId() && !tk.getRcCandidatePhotoCaptureType().getRequiresAnyIdCapture())
                throw new FileUploadException( FileUploadErrorType.FILEUPLOAD_NOT_NEEDED, "Candidate ID Images not required for this rcCheckId=" + rcCheckId );                

            if( uploadedUserFileType.getIsRcComment() && !tk.getRcAvType().getAnyMedia() )
                throw new FileUploadException( FileUploadErrorType.FILEUPLOAD_NOT_NEEDED, "Comment Media not allowed for this rcCheckId=" + rcCheckId );                
                        
            // no direct images and not id.
            if( !isCandidate && !tk.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture() && !tk.getRcAvType().getAnyMedia() )
                throw new FileUploadException( FileUploadErrorType.FILEUPLOAD_NOT_NEEDED, "Rater Images or comment media not required. rcCheckIdd=" + rcCheckId + ", rcRaterId=" + rcRaterId );                
                        
            if( fct == null )
                throw new FileUploadException( FileUploadErrorType.MIME_NOT_RECOGNIZED, "File is not a recognized filetype. " + filename  );
            
            // LogService.logIt( "ProctorFileUploadServlet.processRequest() DDD filename=" + filename + ", size=" + fileSize + ", rcRaterId=" + rcRaterId + ", Source=" + src );
            
            ProctorFileUploadThread fuu = new ProctorFileUploadThread( inps, filename, fileSize, mime, rcCheckId, rcRaterId, rcItemId, tk.getOrgId(), uploadedUserFileTypeId );
            
            // Failed.
            if( !fuu.getHasValidInfo() )
            {
                try
                {
                    res.setContentType( "text/plain" );
                    Writer sw = res.getWriter();

                    // NOTE: The format of this string is important. Error code is read by IMO. 
                    sw.write( "FAILED CODE:" + FileUploadErrorType.MISSING_PARAMETER_SERVER.getFileUploadErrorTypeId()  );
                    sw.flush();
                    sw.close();
                    return;
                }
                catch( Exception ee )
                {
                    LogService.logIt(ee, "ProctorFileUploadServlet.doPost() Sending failure message. " + info );
                }
                
            }

            // LogService.logIt( "ProctorFileUploadServlet.processRequest() EEE filename=" + filename + ", size=" + fileSize + ", rcRaterId=" + rcRaterId + ", Source=" + src + ", noThread=" + noThread  );
            
            if( noThread )
            {
                fuu.performFileUpload();
            }
            else
                fuu.start();

            try
            {
                res.setContentType( "text/plain" );
                Writer sw = res.getWriter();
                sw.write( "SUCCESS");
                sw.flush();
                sw.close();
            }
            catch( IOException ee )
            {
                LogService.logIt(ee, "ProctorFileUploadServlet.doPost() Exception while sending success message. " + info );
            }
            catch( Exception ee )
            {
                LogService.logIt(ee, "ProctorFileUploadServlet.doPost() Exception while sending success message. " + info );
            }
        }
        catch( IOException | STException | FileUploadException e )
        {            
            String msg = "ProctorFileUploadServlet.doPost() " + e.toString() + ", " + info + "\nRequestInfo=" + inspectRequest(req );            
            LogService.logIt( msg );

            Tracker.addImageFileUploadError();
            
            if( e instanceof IOException && e.getMessage()!=null && e.getMessage().toLowerCase().contains("no space left on device") )
                AdminEmailUtils.sendAdminEmail("IOException Uploading Proctor Files", msg, "fileUploadErrorEmails");
            
            try
            {
                res.setContentType( "text/plain" );
                Writer sw = res.getWriter();
                
                int failedTypeId = e instanceof FileUploadException ? ((FileUploadException)e).getFileUploadErrorTypeId() : FileUploadErrorType.UNKNOWN_SERVER.getFileUploadErrorTypeId();
                // NOTE: The format of this string is important. Error code is read by IMO. 
                sw.write( "FAILED CODE:" + failedTypeId + " " + e.getMessage()  );
                sw.flush();
                sw.close();
            }
            catch( IOException ee )
            {
                LogService.logIt(ee, "ProctorFileUploadServlet.doPost() Sending failure message. " + info );
            }
            catch( Exception ee )
            {
                LogService.logIt(ee, "ProctorFileUploadServlet.doPost() Sending failure message. " + info );
            }
            
            if( rcCheckId>0 )
                RcCheckLogUtils.createRcCheckLogEntry(rcCheckId, rcRaterId, 0, msg, HttpReqUtils.getClientIpAddress(req), req.getHeader( "User-Agent" ) );
        }
        catch( Exception e )
        {
            Tracker.addImageFileUploadError();
            
            String reqInsp = inspectRequest(req );
            
            String msg = "ProctorFileUploadServlet.doPost() Upload Failure. " + info + "\nRequestInfo=" + reqInsp;
            
            if( e.getMessage()!=null && e.getMessage().startsWith( "Blob or Form File Part not found" ) )
                LogService.logIt( msg );
                
            else
                LogService.logIt( e, msg );

            try
            {
                res.setContentType( "text/plain" );
                Writer sw = res.getWriter();

                // NOTE: The format of this string is important. Error code is read by IMO. 
                sw.write( "FAILED CODE:" + FileUploadErrorType.UNKNOWN_SERVER.getFileUploadErrorTypeId() + " " + e.getMessage() );
                sw.flush();
                sw.close();
            }
            catch( IOException ee )
            {
                LogService.logIt(ee, "ProctorFileUploadServlet.doPost() Sending failure message. " + info );
            }
            catch( Exception ee )
            {
                LogService.logIt(ee, "ProctorFileUploadServlet.doPost() Sending failure message. " + info );
            }
            
            if( rcCheckId>0 )
                RcCheckLogUtils.createRcCheckLogEntry(rcCheckId, rcRaterId, 0, msg, HttpReqUtils.getClientIpAddress(req), req.getHeader( "User-Agent" ) );
        }
    }

    
    

    private boolean requestHasParamsForRecovery( HttpServletRequest req )
    {
        if( refBean.getAdminOverride())
            return false;
        
        else if( HttpReqUtils.getStringReqParam("acidx", req)!=null )
            return true;

        return false;
    }



}
