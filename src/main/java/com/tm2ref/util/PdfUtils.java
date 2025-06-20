/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.util;

import com.tm2ref.service.LogService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 *
 * @author miker
 */
public class PdfUtils {
    
    public static String convertPdfToText( File pdfFile )
    {
        try
        {
            RandomAccessReadBufferedFile raf = new RandomAccessReadBufferedFile(pdfFile);

            try(PDDocument document = Loader.loadPDF(raf))   
            {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                LogService.logIt( "PdfUtils.convertPdfToText() text has " + text.length() + " characters. PdfFile=" + (pdfFile==null ? "null" : pdfFile.getName()) );
                return text;
            }
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "PdfUtils.convertPdfToText() " + (pdfFile==null ? "null" : pdfFile.getName() + ", " + pdfFile.getAbsolutePath()) );
            return null;
        }
    }

    public static String convertPdfToText( InputStream ips )
    {
        try
        {
            File file = createTempFileFromInputStream(ips, "jd", "pdf");
            return convertPdfToText( file );
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "PdfUtils.convertPdfToText() From InputStream" );
            return null;
        }
    }

    public static File createTempFileFromInputStream(InputStream inputStream, String prefix, String suffix) throws IOException 
    {
        File tempFile = Files.createTempFile(prefix, suffix).toFile();
        tempFile.deleteOnExit(); // Optional: delete the file when the JVM exits

        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) 
        {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
    
    
    
    
}
