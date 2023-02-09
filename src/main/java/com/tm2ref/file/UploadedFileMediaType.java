package com.tm2ref.file;


public enum UploadedFileMediaType
{
    ALL(0,"All File Types", "","" ),
    IMG_ONLY(1,"Image Files Only", "image",""),
    AUDIO_ONLY(2,"Audio Files Only", "audio,video","" ),
    VIDEO_ONLY(3,"Video Files Only", "video",""),
    AUDIOVIDEO_ONLY(4,"Video or Audio Files Only", "audio,video",""),
    AUDVIDIMG_ONLY(5,"Images, Video, or Audio Files Only", "audio,video,image",""),
    WORD_ONLY(6,"MS Word Files Only","application","doc,docx"),
    EXCEL_ONLY(7,"MS Excel Files Only","application","xls,xlsx"),
    PPT_ONLY(8,"MS PowerPoint Files Only","application","ppt,pptx"),
    OFFICE_ONLY(9,"MS Office (Word, Excel, PPT) Files Only","application","doc,docx,xls,xlsx,ppt,pptx");


    private final int uploadedFileMediaTypeId;

    private String key;

    private String validMimePrefix;
    
    private String validExtensions;


    private UploadedFileMediaType( int p , String key, String prefixes, String extensions )
    {
        this.uploadedFileMediaTypeId = p;

        this.key = key;

        this.validMimePrefix = prefixes;
        
        this.validExtensions = extensions;
    }


    private String[] getMimePrefixList()
    {
        return validMimePrefix.split( "," );
    }

    private String[] getValidExtensionList()
    {
        return validExtensions.split( "," );
    }

    
    public boolean isValid( String contentType, String filename )
    {
        return isValidMimePrefix( contentType ) && isValidExtension( filename );
    }
    
    
    public boolean isValidMimePrefix( String contentType )
    {
        // LogService.logIt( "UploadedFileMediaType.isValidMimePrefix() contentType=" + contentType + ", validMimePrefixes=" + validMimePrefix );
        if( validMimePrefix.isEmpty() )
            return true;

        contentType = contentType.toLowerCase();

        for( String p : getMimePrefixList() )
        {
            p = p.trim();

            if( p.isEmpty() )
                continue;

            if( contentType.indexOf( p ) >=0 )
                return true;
        }

        return false;
    }

    public boolean isValidExtension( String filename )
    {
        // LogService.logIt( "UploadedFileMediaType.isValidExtension() filename=" + filename + ", validExtensions=" + validExtensions );
        if( validExtensions.isEmpty() )
            return true;

        filename = filename.toLowerCase();
        
        if( filename.indexOf(".")>=0 && filename.lastIndexOf(".")<filename.length()-1 )
            filename = filename.substring( filename.lastIndexOf(".")+1, filename.length() );

        for( String p : getValidExtensionList() )
        {
            p = p.trim();

            if( p.isEmpty() )
                continue;

            if( p.equalsIgnoreCase(filename) )
                return true;
        }

        return false;
    }



    public int getUploadedFileMediaTypeId()
    {
        return this.uploadedFileMediaTypeId;
    }





    public String getName()
    {
        return key;
    }


    public String getKey()
    {
        return key;
    }



    public static UploadedFileMediaType getValue( int id )
    {
        UploadedFileMediaType[] vals = UploadedFileMediaType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUploadedFileMediaTypeId() == id )
                return vals[i];
        }

        return ALL;
    }

}
