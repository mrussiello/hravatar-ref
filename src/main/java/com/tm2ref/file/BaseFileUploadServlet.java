/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.file;

import com.tm2ref.service.LogService;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

/**
 *
 * @author miker_000
 */
public abstract class BaseFileUploadServlet extends HttpServlet {
    
    //public long rcCheckId;
    //public long rcRaterId;

    
    protected String inspectRequest( HttpServletRequest req  )
    {
        StringBuilder sb = new StringBuilder();
        
        try
        {
            if( req==null )
                return "request is null.";            
            
            sb.append( "HttpRequest: ContentType=" + req.getContentType() + ", " );
            Collection<Part> parts = req.getParts();
            Collection<String> hdrs;
            String hdr;
            String hdrv;
            Iterator<Part> iter = parts.iterator();
            Iterator<String> iter2;
            Part p;
            
            int count = 0;
            
            while( iter.hasNext() )
            {
                p = iter.next();
                count++;
                                
                sb.append( "\nPart (" + count + "): name=" + p.getName() + ", size=" + p.getSize() + ", " );
                
                hdrs = p.getHeaderNames();                
                iter2 = hdrs.iterator();                
                while( iter2.hasNext() )
                {
                    hdr = iter2.next();                    
                    hdrv = p.getHeader(hdr);
                    sb.append( hdr + "=" + hdrv + ", " );                    
                }                
            }
            
            return sb.toString();
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "BaseBaseFileUploadServlet.inspectRequest() "  );
            return e.toString();
        }
    }
    
    
    
    
    protected Part getPart( HttpServletRequest req, String key, boolean required)
    {
        try
        {
            if( req==null )
                throw new Exception( "request is null!"); 
            
            if( key==null || key.isEmpty() )
                throw new Exception( "Key is invalid"); 
            
            return req.getPart(key);
        }
        catch( ServletException e )
        {
            LogService.logIt("BaseFileUploadServlet.getPart() key=" + key + ", " + e.toString() );
            return null;
        }
        catch( java.io.IOException e )
        {
            if( required )
                LogService.logIt("BaseFileUploadServlet.getPart() java.io.IOException Part not found for key=" + key + ", " + e.toString() );
            return null;
        }
        catch( java.util.concurrent.TimeoutException e )
        {
            if( required )
                LogService.logIt("BaseFileUploadServlet.getPart() java.util.concurrent.TimeoutException Part not found for key=" + key + ", " + e.toString() );
            return null;
        }
        //catch( java.io.EOFException e )
        //{
        //    if( required )
        //        LogService.logIt("BaseFileUploadServlet.getPart() java.io.EOFExceptn Part not found for key=" + key + ", " + e.toString() );
        //    return null;
        //}
        catch( Exception e )
        {
            LogService.logIt(e, "BaseFileUploadServlet.getPart() key=" + key );
            return null;
        }
    }


    protected int readInt( Part p )
    {
        if( p == null || p.getSize()==0 )
            return 0;

        try
        {
            Scanner sc = new Scanner( p.getInputStream() );
            return sc.nextInt();
        }
        catch( IOException e )
        {
            LogService.logIt(e, "BaseFileUploadServlet.readInt() " + p.toString() + " " + p.getName() );
            return 0;
        }
    }

    protected long readLong( Part p )
    {
        if( p == null || p.getSize()==0 )
            return 0;

        try
        {
            Scanner sc = new Scanner( p.getInputStream() );
            return sc.nextLong();
        }
        catch( IOException e )
        {
            LogService.logIt(e, "BaseFileUploadServlet.readLong() " + p.toString() + " " + p.getName() );
            return 0;
        }
    }



    protected String readString( Part p )
    {
        if( p == null || p.getSize()==0 )
            return null;
        try
        {
            Scanner sc = new Scanner( p.getInputStream() );
            return sc.nextLine();
        }
        catch( IOException e )
        {
            LogService.logIt(e, "BaseFileUploadServlet.readLong() " + p.toString() + " " + p.getName() );
            return null;
        }
    }


    protected String extractFileName(Part part)
    {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items)
        {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length()-1);
            }
        }

        return "";
    }

    
    
}
