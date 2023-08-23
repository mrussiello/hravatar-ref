/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.file;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcItem;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.faces.HttpReqUtils;
import com.tm2ref.global.STException;
import com.tm2ref.ref.RcCheckLogUtils;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.ref.RcScriptFacade;
import com.tm2ref.ref.RefBean;
import com.tm2ref.service.AdminEmailUtils;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
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
@WebServlet(name = "RaterUploadServlet", urlPatterns = {"/rtrfupload"})
@MultipartConfig(fileSizeThreshold=1024*1024*2, // 2MB
                 maxFileSize=1024*1024*105,      // 105MB
                 maxRequestSize=1024*1024*105)   // 105MB
public class RaterFileUploadServlet extends BaseFileUploadServlet
{

    @Inject
    RefBean refBean;
    
    

    
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
        String rcitmid = null;
        
        // Uploaded file type. Defaults to 100 (REMOTE_PROCTORING_IMAGES)
        String uft = null;
        String rcavt = null;
        String nthrd = null;
        
        //long actId = 0;
        long rcCheckId = 0;
        long rcRaterId = 0;
        int rcItemId = 0;
        int uploadedUserFileTypeId = 0; // UploadedUserFileType.REF_CHECK_IMAGES.getUploadedUserFileTypeId();
        int mediaTypeId=0;
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
                    LogService.logIt(ee, "RaterFileUploadServlet.doPost() AAA Exception while sending success message. " + info );
                }                
            }
            
            // LogService.logIt( "RaterFileUploadServlet.processRequest() AAA " );
            
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
                LogService.logIt( "RaterFileUploadServlet.doPost() AAA.2 Parsing rcid=" + rcid + ", after decryypt=" +  EncryptUtils.urlSafeDecrypt(rcid));
            }
            catch( Exception e )
            {
                LogService.logIt( e, "RaterFileUploadServlet.doPost() AAA.3 Parsing rcid=" + rcid);
            }
            
            info = "rcCheckId=" + rcCheckId + ", " + info;
            
            if( rcCheckId<=0 )
                throw new FileUploadException( FileUploadErrorType.MISSING_PARAMETER_SERVER,  "Invalid rcCheckId - could not decode rcid " + rcid + "." );
            
            p = getPart(req, "rtrid", true );
            rtrid = readString( p );
            if( rtrid==null )
                rtrid="";
            info="rtrid=" + rtrid;

            try
            {
                rcRaterId = Long.parseLong( EncryptUtils.urlSafeDecrypt(rtrid) );
            }
            catch( NumberFormatException e )
            {
                LogService.logIt( "RaterFileUploadServlet.doPost() AAA.2 Parsing rtrid=" + rtrid + ", after decryypt=" +  EncryptUtils.urlSafeDecrypt(rtrid) + ", rcCheckId=" + rcCheckId);
            }
            catch( Exception e )
            {
                LogService.logIt( e, "RaterFileUploadServlet.doPost() AAA.3 Parsing rtrid=" + rtrid + ", rcCheckId=" + rcCheckId);
            }
            
            if( rcRaterId<=0 )
                throw new FileUploadException( FileUploadErrorType.MISSING_PARAMETER_SERVER,  "Invalid rcRaterId - could not decode rtrid " + rtrid + "." );
            info = "rcRaterId=" + rcRaterId + ", " + info;
                        
            
            p = getPart(req, "rcitmid", true );
            rcitmid = readString( p );
            if( rcitmid==null )
                rcitmid="";
            info="rcitmid=" + rcitmid;
            
            try
            {
                rcItemId = Integer.parseInt( EncryptUtils.urlSafeDecrypt(rcitmid) );
            }
            catch( NumberFormatException e )
            {
                LogService.logIt( "RaterFileUploadServlet.doPost() AAA.2 Parsing rcitmid=" + rcitmid + ", after decryypt=" +  EncryptUtils.urlSafeDecrypt(rcitmid) + ", rcCheckId=" + rcCheckId);
            }
            catch( Exception e )
            {
                LogService.logIt( e, "RaterFileUploadServlet.doPost() AAA.3 Parsing rcitmid=" + rcitmid + ", rcCheckId=" + rcCheckId);
            }
            
            if( rcItemId<=0 )
                throw new FileUploadException( FileUploadErrorType.MISSING_PARAMETER_SERVER,  "Invalid rcItemId - could not decode rcitmid " + rcitmid + "." );
            info = "rcItemId=" + rcItemId + ", " + info;
            
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
            
            p = getPart(req, "rcavt", false );
            if( p!=null )
            {
                rcavt = readString( p );
                info="rcavt=" + (rcavt==null ? "null" : rcavt );
            }

            if( rcavt==null || rcavt.isBlank() )
            {
                LogService.logIt( "rcavt missing. Assuming Video");
                rcavt = Integer.toString( MediaType.VIDEO.getMediaTypeId() );
            }                
            
            try
            {
                mediaTypeId = Integer.parseInt(rcavt);        
                uploadedUserFileTypeId = Integer.parseInt(uft);
            }
            catch( NumberFormatException e )
            {
                LogService.logIt( "RaterFileUploadServlet.doPost() AAA.2 Parsing rcavt=" + rcavt + ", rcCheckId=" + rcCheckId);
            }
            if( uploadedUserFileTypeId!=UploadedUserFileType.REF_CHECK_RATER_COMMENT.getUploadedUserFileTypeId() )
                throw new FileUploadException( FileUploadErrorType.UPLOADEDUSERFILETYPE_INVALID.getFileUploadErrorTypeId(), "Invalid uploadedUserFileTypeId=" + uploadedUserFileTypeId + ", uft=" + uft + ", Expected Rater Comment." );                
                        
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
            
            // rcRaterId = 0;
            
            //if( rcRaterId <= 0 )
            //    throw new FileUploadException( FileUploadErrorType.MISSING_PARAMETER_SERVER,  "Invalid rcRaterId - could not decode " + rtrid + "." );
            
            
            // LogService.logIt( "RaterFileUploadServlet.doPost() " + inspectRequest(req) + ", rcRaterId=" + rcRaterId );
             
            if( blobSize>0 && blobType!=null )
            {                
                blobType = blobType.toLowerCase();
                
                mime = blobType.indexOf(";")>0 ? blobType.substring(0, blobType.indexOf(";") ) : blobType;                
                fct = FileContentType.getFileContentTypeFromContentType(mime, null );                
                if( fct==null )
                    fct = FileContentType.VIDEO_WEBM;
                
                filename = "upblb_" + rcCheckId + "_" + rcRaterId + "_" + rcItemId + "_" + (new Date()).getTime() + "." + fct.getBaseExtension();
                
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
                    LogService.logIt( "RaterFileUploadServlet.doPost() Inputstream for uploaded file. rcCheckId=" + rcCheckId);                    
                    throw new FileUploadException( FileUploadErrorType.BYTES_MISSING_SERVER, "Blob File is too big. FileSize=" + fileSize );
                }
                
                src = "Blob";
            }
            
            else if( blobSize<=0 )
                throw new FileUploadException( FileUploadErrorType.BYTES_MISSING_SERVER, "Blob Size invalid. " + blobSize + ", blobType=" + blobType );

            else if( blobType==null)
                throw new FileUploadException( FileUploadErrorType.BYTES_MISSING_SERVER, "Blob Type is null. " + blobSize + ", blobType=" + blobType );
            
            info += ", Source=" + src;
            
            RcFacade ef = RcFacade.getInstance();

            // OK, everything valid, let's lookup the ActHistory.
            // LogService.logIt( "RaterFileUploadServlet.processRequest() BBB filename=" + filename + ", size=" + fileSize + ", rcRaterId=" + rcRaterId + ", Source=" + src );

            RcCheck tk = null;
            
            try
            {
                tk = ef.getRcCheck(rcCheckId, false );
            }
            catch( STException e )
            {
                LogService.logIt( "ProctorFileUploadServlet.doPost() getting RcCheck rcCheckId=" + rcCheckId);                    
            }

            if( tk == null )
                throw new FileUploadException( FileUploadErrorType.RCCHECK_MISSING, "Cannot find RcCheck. rcCheckId=" + rcCheckId );

            RcRater rcRater = null;
            
            try
            {
                rcRater = ef.getRcRater(rcRaterId, true);
            }
            catch( STException e )
            {
                LogService.logIt( "ProctorFileUploadServlet.doPost() getting rcRater rcRaterId=" + rcRaterId + ", rcCheckId=" + rcCheckId);                    
            }
                
            if( rcRater==null )
                throw new FileUploadException( FileUploadErrorType.RCRATER_MISSING, "Cannot find RcRater rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );
            if( rcRater.getRcCheckId()!=rcCheckId )
                throw new FileUploadException( FileUploadErrorType.RCRATER_MISSING, "RcRater does not match this RcCheck. rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId );                    

            RcScriptFacade rcsf = RcScriptFacade.getInstance();
            RcItem rcItem = null;
            
            try
            {
                rcItem = rcsf.getRcItem(rcItemId, true, true);
            }
            catch( STException e )
            {
                LogService.logIt( "ProctorFileUploadServlet.doPost() getting RcItem rcItemId=" + rcItemId + ", rcRaterId=" + rcRaterId + ", rcCheckId=" + rcCheckId);                    
            }
                
            if( rcItem==null )
            {
                throw new FileUploadException( FileUploadErrorType.RCITEM_MISSING, "Cannot find RcItem. rcItemId=" + rcItemId );
            }                    
                                    
            if( fct == null )
                throw new FileUploadException( FileUploadErrorType.MIME_NOT_RECOGNIZED, "File is not a recognized filetype. " + filename  );
            
            RaterFileUploadThread fuu = new RaterFileUploadThread( inps, filename, fileSize, mime, rcCheckId, rcRaterId, rcItemId, tk.getOrgId(), uploadedUserFileTypeId, mediaTypeId );
            
            if( noThread )
                fuu.performFileUpload();
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
            catch( Exception ee )
            {
                LogService.logIt(ee, "RaterFileUploadServlet.doPost() Exception while sending success message. " + info );
            }
        }

        catch( IOException | STException | FileUploadException e )
        {            
            int typeId = e instanceof FileUploadException ? ((FileUploadException)e).getFileUploadErrorTypeId() : FileUploadErrorType.UNKNOWN_SERVER.getFileUploadErrorTypeId();
            String msg = "RaterFileUploadServlet.doPost() " + e.getMessage() + ", " + info + "\nRequestInfo=" + inspectRequest(req );            
            LogService.logIt( msg );

            Tracker.addMediaFileUploadError();
            
            if( e instanceof IOException && e.getMessage()!=null && e.getMessage().toLowerCase().contains("no space left on device") )
                AdminEmailUtils.sendAdminEmail("IOException Uploading RC Rater Files", msg, "fileUploadErrorEmails");
            
            
            try
            {
                res.setContentType( "text/plain" );
                Writer sw = res.getWriter();
                
                // NOTE: The format of this string is important. Error code is read by IMO. 
                sw.write( "FAILED CODE:" + typeId + " " + e.getMessage()  );
                sw.flush();
                sw.close();
            }
            catch( Exception ee )
            {
                LogService.logIt(ee, "RaterFileUploadServlet.doPost() Sending failure message. " + info );
            }
            
            if( rcCheckId>0 )
                RcCheckLogUtils.createRcCheckLogEntry(rcCheckId, rcRaterId, 0, msg, HttpReqUtils.getClientIpAddress(req), req.getHeader( "User-Agent" ) );
        }
        catch( Exception e )
        {
            Tracker.addMediaFileUploadError();
            
            String reqInsp = inspectRequest(req );
            
            String msg = "RaterFileUploadServlet.doPost() Upload Failure. " + info + "\nRequestInfo=" + reqInsp;
            
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
            catch( Exception ee )
            {
                LogService.logIt(ee, "RaterFileUploadServlet.doPost() Sending failure message. " + info );
            }
            
            if( rcCheckId>0 )
                RcCheckLogUtils.createRcCheckLogEntry(rcCheckId, rcRaterId, 0, msg, HttpReqUtils.getClientIpAddress(req), req.getHeader( "User-Agent" ) );
        }
    }

    
    




}
