/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.file;

import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.file.image.ImageInfo;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 *
 * @author Mike
 */
public abstract class BaseFileUploadThread extends Thread
{
    private static Random random = null;

    public long rcCheckId;
    public long rcRaterId;
    public int rcItemId;

    public InputStream strm=null;
    public String initialFilename=null;
    public int initialFileSize;
    public String  initialMime = null;
    public int uploadedFileMediaTypeId;
    public int maxHeight;
    public int maxWidth;
    public String errMsg = null;
    


    protected int getNextRandom()
    {
        double d = getUniformRandom();

        return (int) Math.round( d*9999 ) + 1;
    }

    /**
     * Returns a double between 0 (inclusive) - 1.0 (exclusive) on a uniform distribution.
     */
    protected static double getUniformRandom()
    {
        if( random == null )
            random = new Random( new java.util.Date().getTime() );

        return random.nextFloat();
    }


    public void setImageMetadata( byte[] bytes, RcUploadedUserFile uuf, FileContentType fct ) throws Exception
    {
        try
        {

            // now check image info
            ImageInfo imageInfo = new ImageInfo();

            imageInfo.setInput( new ByteArrayInputStream( bytes ) );

            imageInfo.setDetermineImageNumber( true ); // default is false

            imageInfo.setCollectComments( true ); // default is false

            if( !imageInfo.check() )
            {
                LogService.logIt( "BaseFileUploadThread.setImageMetadata() ImageInfo: Not a supported image file format. " + fct.getBaseContentType()  + " , using Toolkit" );

                Image img = Toolkit.getDefaultToolkit().createImage( bytes );

                uuf.setHeight( img.getHeight( null ) );

                uuf.setWidth( img.getWidth( null ) );

                // throw new STException( "g.UploadErrorInvalidFile" );
            }

            else
            {
                uuf.setHeight( imageInfo.getHeight() );

                uuf.setWidth( imageInfo.getWidth() );
            }
        }


        catch( Exception e )
        {
            LogService.logIt( e, "BaseFileUploadThread.setImageMetadata() " + ( uuf == null ? "null" : uuf.toString() ) );

            throw new STException( e );
        }
    }

    public static byte[] convertToByteArray(InputStream input) throws IOException
    {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }




}
