package com.tm2ref.file;


public enum FileContentType
{
    AUDIO_3GP(  115 ,"filetype.a.3gp" , "3gp,3g2" , "audio/3gpp,audio/3gpp2" , true  , null , null , false),
    AUDIO_AAC( 112 ,"filetype.aac" , "mp4" , "audio/aac,audio/mp4,audio/3gpp," , true , null , null, false  ),
    AUDIO_AIFF( 103 ,"filetype.aiff" , "aiff,aif" , "audio/aiff,audio/x-aiff," , true , null , null, false ),
    AUDIO_AU(   105 ,"filetype.au" , "au" , "audio/basic" , true , null , null, false ),
    AUDIO_MP4(  111 ,"filetype.mp4" , "mp4" , "audio/mp4" , true  , null , null, false ),
    AUDIO_M4A(  110 ,"filetype.m4a" , "m4a" , "audio/x-m4a" , true  , null , null, false ),
    AUDIO_M4P(  109 ,"filetype.m4p" , "m4p" , "application/x-m4p" , true  , null , null, false ),
    AUDIO_MID(  106 ,"filetype.mid" , "mid" , "audio/mid,x-music/x-midi" , true , null , null, false ),
    AUDIO_MP3(  101 ,"filetype.mp3" , "mp3,mpeg3" , "audio/mp3,audio/x-mpeg-2,audio/x-mp3,audio/mpeg3,audio/x-mpeg3" , true  , null , null, false ),
    AUDIO_MPG(  102 ,"filetype.audiompg" , "mpeg" , "audio/mpeg,audio/x-mpeg,audio/mpg,audio/x-mpg,audio/x-mpegaudio,audio/mpeg4,audio/mp4" , true , null , null , false),
    AUDIO_REAL( 107 ,"filetype.ra" , "ra" , "application/x-pn-realaudio" , true  , null , null, false),
    AUDIO_VORBIS( 114 ,"filetype.vorbis" , "ogg,oga" , "audio/ogg, audio/vorbis, audio/vorbis-config" , true  , null , null, false),
    AUDIO_WAV(  108 ,"filetype.wav" , "wav" , "audio/wav,audio/x-wav" , true , null  , null, false),
    AUDIO_WMA(  104 ,"filetype.wma" , "wma" , "audio/x-ms-wma" , true , null  , null, false),
    AUDIO_WEBM(  113 ,"filetype.webmaudio" , "webm" , "audio/webm" , true , null  , null, false),
    AUDIO_FLAC(  116 ,"filetype.flac" , "flac" , "audio/flac" , true  , null , null, false ),
    AUDIO_MKV(  117 ,"filetype.mkv" , "mkv,mk3d,mka,mks" , "audio/x-matroska" , true  , null , "mkv", false ),
    VIDEO_3GP(  209 ,"filetype.3gp" , "3gp,3g2" , "video/3gpp,audio/3gpp2" , true , null , null, false ),
    VIDEO_AVI(  204 ,"filetype.avi" , "avi" , "video/avi,video/x-avi" , true , null , null, false ),
    VIDEO_FLV(  201 ,"filetype.flv" , "flv" , "video/flv,video/x-flv" , true , null  , null, false),
    VIDEO_MP4(  206 ,"filetype.mp4" , "mp4,f4v" , "video/mp4,video/f4v,video/x-flv,application/mp4" , true , null  , null, false),
    VIDEO_MPG(  203 ,"filetype.mpg" , "mpg,m2v" , "video/xmpg2,video/mpeg,video/mpg,video/x-mpg,video/x-mpeg,video/mpeg2" , true , null , null, false ),
    VIDEO_MOV(  200 ,"filetype.mov" , "mov" , "video/quicktime" , true  , null , null, false),
    VIDEO_SWF(  202 ,"filetype.swf" , "swf" , "application/x-shockwave-flash" , false , "application/x-shockwave-flash" , "swf", false ),
    VIDEO_THEORA( 208 ,"filetype.theora" , "ogv" , "video/ogg" , true , null , null, false),
    VIDEO_WEBM(  207 ,"filetype.webm" , "webm" , "video/webm" , true , null , null, false),
    VIDEO_WMV(  205 ,"filetype.wmv" , "wmv" , "video/x-ms-wmv,video/msvideo,video/x-msvideo" , true , null  , null, false),
    VIDEO_MKV(  210 ,"filetype.mkv" , "mkv,mk3d,mka,mks" , "video/x-matroska" , true  , null , "mkv", false ),
    IMAGE_BMP(  304 ,"filetype.bmp" , "bmp" , "image/bmp,image/x-bmp" , false , "image/jpeg"  , "jpg", false),
    IMAGE_GIF(  301 ,"filetype.gif" , "gif" , "image/gif" , false , "image/gif"  , "gif", false ),
    IMAGE_JPEG( 302 ,"filetype.jpeg" , "jpg,jpeg" , "image/jpeg,image/pjpeg" , false, "image/jpeg"  , "jpg" , false),
    IMAGE_PNG(  303 ,"filetype.png" , "png" , "image/png,image/x-png" , false , "image/png" , "png", false),
    IMAGE_TIFF( 305 ,"filetype.tiff" , "tiff" , "image/tiff,image/x-tiff" , false, "image/tiff" , "tif", false ),
    IMAGE_SVG( 306 ,"filetype.svg" , "svg,svgz" , "image/svg+xml" , false, "image/svg+xml" , "svg", false ),
    DOCUMENT_PDF( 500 ,"filetype.pdf" , "pdf" , "application/pdf" , false , "application/pdf"  , "pdf", false),
    DOCUMENT_PPT( 501 ,"filetype.ppt" , "ppt" , "application/vnd.ms-powerpoint" , false , "application/vnd.ms-powerpoint" , "ppt", false ),
    TEXT_PLAIN( 402 ,"filetype.text" , "txt" , "text/plain" , false, "text/plain"  , "txt", false ),
    TEXT_HTML( 403 ,"filetype.html" , "htm,html" , "text/html" , false  , "text/html" , "html", false),
    TEXT_XHTML( 401 ,"filetype.xhtml" , "xhtml" , "text/xhtml" , false , "text/xhtml" , "html", false),
    TEXT_XML( 400 ,"filetype.xml" , "xml" , "text/xml" , false , "text/xml" , "xml", false ),
    DOCUMENT_DOC( 600 ,"filetype.msword" , "doc" , "application/msword" , false , "application/msword" , "doc", false ),
    DOCUMENT_DOCX( 601 ,"filetype.mswordx" , "docx" , "application/vnd.openxmlformats-officedocument.wordprocessingml.document" , false , "application/vnd.openxmlformats-officedocument.wordprocessingml.document" , "docx", false ),
    DOCUMENT_XLS( 602 ,"filetype.xls" , "xls" , "   application/vnd.ms-excel" , false , "   application/vnd.ms-excel" , "xls", false ),
    DOCUMENT_XLSX( 603 ,"filetype.xlsx" , "xlsx" , "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" , false , "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" , "xlsx", false ),
    DOCUMENT_PPTX( 604 ,"filetype.pptx" , "pptx" , "application/vnd.openxmlformats-officedocument.presentationml.presentation" , false , "application/vnd.openxmlformats-officedocument.presentationml.presentation" , "pptx", false ),
    ARCHIVE_ZIP( 700 ,"filetype.zip" , "zip" , "application/zip,application/x-zip,application/x-zip-compressed,application/octet-stream,application/x-compress,application/x-compressed,multipart/x-zip" , false , "application/zip" , "zip", false ),

    FONT_TTF( 901 ,"filetype.fonttruetype" , "ttf" , "application/x-font-ttf" , false , "application/x-font-ttf" , "html", false),
    FONT_OTF( 902 ,"filetype.fontotf" , "otf" , "application/x-font-opentype" , false , "application/x-font-opentype" , "html", false),
    FONT_EOT( 903 ,"filetype.fonteot" , "eot" , "application/vnd.ms-fontobject" , false , "application/vnd.ms-fontobject" , "html", false),
    FONT_WOFF( 904 ,"filetype.fontwoff" , "woff" , "application/x-font-woff" , false , "application/x-font-woff" , "html", false),
    FONT_SVG( 905 ,"filetype.fontsvg" , "svg" , "image/svg+xml" , false , "image/svg+xml" , "html", false);



    private final int fileContentTypeId;

    private String key;

    private String[] extensions;

    private boolean conversionRequired;

    private String[] contentTypes;

    //private String targetContentType;

    //private String targetExtension;

    boolean supportsDirectDl = false;

    private FileContentType( int p ,
                             String key ,
                             String extensions ,
                             String contentTypes ,
                             boolean conversionRequired ,
                             String targetContentType ,
                             String targetExtension ,
                             boolean drctDl
                            )
    {
        this.fileContentTypeId = p;

        this.key = key;

        this.extensions = extensions.split( "," );

        this.conversionRequired = conversionRequired;

        this.contentTypes = contentTypes.split( "," );

        //this.targetContentType = targetContentType;

        //this.targetExtension = targetExtension;

        this.supportsDirectDl = drctDl;
    }


    public String getBaseContentType()
    {
        if( contentTypes == null || contentTypes.length == 0 )
            return null;

        return contentTypes[0];
    }


    public String getBaseExtension()
    {
        if( extensions == null || extensions.length == 0 )
            return null;

        return extensions[0];
    }



    public int getFileContentTypeId()
    {
        return this.fileContentTypeId;
    }


    public boolean isEqualTo( Object o )
    {
        if( o instanceof FileContentType )
        {
            if( fileContentTypeId == ((FileContentType)o).getFileContentTypeId() )
                return true;
        }

        return false;
    }




    public static FileContentType getType( int typeId )
    {
        return getValue( typeId );
    }


    public String getKey()
    {
        return key;
    }



    public static FileContentType getValue( int id )
    {
        FileContentType[] vals = FileContentType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getFileContentTypeId() == id )
                return vals[i];
        }

        return null;
    }


    @Override
    public String toString()
    {
        return "FileContentType: " + key;
    }



    public boolean getConversionRequired()
    {
        return conversionRequired;
    }




    public String[] getExtensions()
    {
        return extensions;
    }


    public boolean matchesContentType( String contentType )
    {
        if( contentType == null )
            return false;

        for( String val : contentTypes )
        {
            if( val.equalsIgnoreCase( contentType ) )
                return true;
        }

        return false;
    }


    public boolean matchesExtension( String extension )
    {
        if( extension == null )
            return false;

        for( String val : extensions )
        {
            if( val.equalsIgnoreCase( extension ) )
                return true;
        }

        return false;
    }

    public boolean isJavascript()
    {
        if( fileContentTypeId >= 1000 && fileContentTypeId < 1100 )
            return true;

        return false;
    }

    public boolean isCss()
    {
        if( fileContentTypeId >= 1100 && fileContentTypeId < 1200 )
            return true;

        return false;
    }
    

    public boolean isAudio()
    {
        if( fileContentTypeId >= 100 && fileContentTypeId < 200 )
            return true;

        return false;
    }


    public boolean isVideo()
    {
        if( fileContentTypeId >= 200 && fileContentTypeId < 300 && fileContentTypeId != 202 )
            return true;

        return false;
    }

    public boolean isFlashSwf()
    {
        return fileContentTypeId == 202;
    }


    public boolean isImage()
    {
        if( fileContentTypeId >= 300 && fileContentTypeId < 400 )
            return true;

        return false;
    }

    public boolean isText()
    {
        if( fileContentTypeId >= 400 && fileContentTypeId < 500 )
            return true;

        return false;
    }

    public boolean isPdf()
    {
        if( fileContentTypeId == 500 )
            return true;

        return false;
    }

    public boolean isPpt()
    {
        if( fileContentTypeId == 501 )
            return true;

        return false;
    }


    public static FileContentType getFromFilename( String fn )
    {
            if( fn == null )
                    return null;

            return getFileContentTypeFromContentType( null , fn );
    }



    public static FileContentType getFileContentTypeFromContentType( String contentType , String filename )
    {
        // try contentType
        if( contentType != null && contentType.length() > 0 )
        {
            contentType = contentType.toLowerCase();

            for( FileContentType val : FileContentType.values() )
            {
                if( val.matchesContentType( contentType ) )
                    return val;
            }
        }

        // try extension
        String ext = getFileExtension( filename );

        if( ext != null && ext.length() > 0 )
        {
            for( FileContentType val : FileContentType.values() )
            {
                if( val.matchesExtension( ext ) )
                {
                	// This is needed so that MP4s and other formats that support audio and/or video are not mismatched.
                	//if( contentType != null && val.isAudio() && contentType.toLowerCase().indexOf( "audio" ) < 0 )
                	//	continue;

                	//
                	//if( contentType != null && val.isVideo() && contentType.toLowerCase().indexOf( "video" ) < 0 )
                	//	continue;

                	return val;
                }
            }

        }

        return null;
    }


    public static String getFileExtension( String filename )
    {
        if( filename == null )
            return null;

        if( filename.lastIndexOf( "." ) <= 0 || filename.endsWith( "." ) )
            return null;

        return filename.substring( filename.lastIndexOf( "." ) + 1, filename.length() ).toLowerCase();
    }


}
