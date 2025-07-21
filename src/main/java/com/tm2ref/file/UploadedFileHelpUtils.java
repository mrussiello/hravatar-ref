/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.file;

import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import com.tm2ref.util.MsWordUtils;
import com.tm2ref.util.PdfUtils;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 *
 * @author miker
 */
public class UploadedFileHelpUtils {
    
    FileUploadFacade fileUploadFacade;
    
    public String parseUploadedUserFileForText( RcUploadedUserFile uuf ) throws Exception
    {
        if( uuf==null )
        {
            LogService.logIt( "UploadedFileHelpUtils.parseUploadedUserFileForText() uploadedUserFile is null!" );
            return null;
        }

        if( uuf.getUploadedText()!=null && !uuf.getUploadedText().isBlank() )
            return uuf.getUploadedText();
            
        // String initFilename = null;
        InputStream fis=null;
        
        try
        {
            // initFilename = uuf.getInitialFilename();
            String uploadedFilename = uuf.getFilename();

            if( !uploadedFilename.toLowerCase().endsWith(".pdf") && !uploadedFilename.toLowerCase().endsWith(".doc") && !uploadedFilename.toLowerCase().endsWith(".docx") && !uploadedFilename.toLowerCase().endsWith(".txt") )
                throw new Exception( "UploadedUserFile has an unrecognized file type. uploadedFilename=" + uploadedFilename );

            String cntntHdr = uuf.getMime(); // uf.getHeader( "content-type" );

            FileContentType fct  = FileContentType.getFileContentTypeFromContentType(cntntHdr, uploadedFilename);

            if( !fct.getIsPdf() && !fct.getIsWord() && !fct.equals(FileContentType.TEXT_PLAIN) )
                throw new Exception( "Uploaded File has an unrecognized file type. uploadedFilename=" + uploadedFilename + ", fileContentType=" + fct.toString());
            
            FileXferUtils fxfer = new FileXferUtils();
            
            BucketType bt = RuntimeConstants.getBooleanValue( "useAwsTestFoldersForProctoring" ) ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING;
            
            fis = fxfer.getFileInputStream( uuf.getDirectory(), uuf.getFilename(), bt.getBucketTypeId(), false );            
            if( fis==null )
                throw new Exception( "Could not obtain InputStream for uploaded resume file. uploadedUserFileId=" + (uuf==null ? "null" : uuf.getRcUploadedUserFileId() ));
            
            String text;

            if( fct.getIsPdf() )
            {
                text = PdfUtils.convertPdfToText(fis);
            }
            else if( fct.getIsWord() )
            {
                text = MsWordUtils.convertWordToText(fis, uploadedFilename);
                if( (text==null || text.isBlank()) && fct.getBaseExtension().equalsIgnoreCase("docx") )
                    LogService.logIt( "UploadedFileHelpUtils.parseUploadedUserFileForText() unable to parse Word document. It may be an old .doc version. uploadedFilename=" + uploadedFilename );
            }
            else
            {
                try (Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8))
                {
                    text = scanner.useDelimiter("\\A").next();
                }
            }

            LogService.logIt( "UploadedFileHelpUtils.parseUploadedUserFileForText() text.length=" + (text==null ? "null" : text.length() ) );
            
            if( text!=null && !text.isBlank() )
            {
                uuf.setUploadedText( text );
                if( fileUploadFacade==null )
                    fileUploadFacade=FileUploadFacade.getInstance();
                fileUploadFacade.saveRcUploadedUserFile(uuf);
            }
            
            return text;
            
        }
        catch( Exception e )
        {
            LogService.logIt(e, "UploadedFileHelpUtils.parseUploadedUserFileForText() uploadedUserFileId=" + (uuf==null ? "null" : uuf.getRcUploadedUserFileId() ) );
            return null;
        }
        finally
        {
            if( fis!=null )
                fis.close();
        }
    }

}
