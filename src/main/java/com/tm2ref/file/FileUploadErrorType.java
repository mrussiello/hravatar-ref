package com.tm2ref.file;


public enum FileUploadErrorType
{
    UNKNOWN_IMO(1,"Unknown IMO"),
    UNSUPPORTED_ERR_IMO(2,"Unsupported Error IMO"),
    IOS_UNLOCK_ERR_IMO(3,"IOS Unlock Error IMO"),
    CHK_MUTE_ERR_IMO(4,"Check for Mute Error IMO"),
    BLOB_SEND_ERR_IMO(20,"Blob Send Error IMO"),
    FORM_SEND_ERR_IMO(21,"Form Send Error IMO"),
    XHR_ERR_IMO(22,"XML Http Request Error IMO"),
    MEDCAP_RENDER_ERR_IMO(50,"Media Capture Render Error IMO"),
    MEDCAP_PROMISE_ERR_IMO(51,"Media Capture Promise Error IMO"),
    MEDCAP_STREAM_ERR_IMO(52,"Media Capture Strean Error IMO"),
    UNKNOWN_SERVER(101,"Unknown Server"),    
    SERVLET_ERR(102,"Servlet Error"),    
    MISSING_PARAMETER_SERVER(111,"Missing Parameter"),
    BYTES_MISSING_SERVER(112,"Bytes Missing or Too Small"),
    FILE_TOO_BIG_SERVER(113,"File Too Large"),
    BLOG_PART_MISSING(114,"Blob FilePart Missing"),
    FORMFILE_MISSING_SERVER(122,"FormFile Bytes Missing or Too Small"),
    FORMFILE_PART_MISSING(124,"Form FilePart Missing"),
    RCCHECK_MISSING(149,"RcCheck Missing from DB"),
    RCRATER_MISSING(150,"RcRater Missing from DB"),
    RCITEM_MISSING(151,"RcItem Missing from DB"),
    MIME_NOT_RECOGNIZED(152,"Mime Type not recognized."),
    UPLOADEDUSERFILETYPE_INVALID(160,"UploadedUserFileTypeId Invalid"),
    FILEUPLOAD_NOT_NEEDED(161,"File pload is Not Required");


    private final int fileUploadErrorTypeId;

    private String key;


    private FileUploadErrorType( int p , String key )
    {
        this.fileUploadErrorTypeId = p;

        this.key = key;
    }

    public boolean isRequestError()
    {
        return equals( MISSING_PARAMETER_SERVER ) || equals( BYTES_MISSING_SERVER ) ||
               equals( BLOG_PART_MISSING ) || equals( FORMFILE_MISSING_SERVER ) || 
               equals( FORMFILE_PART_MISSING );
    }

    public int getFileUploadErrorTypeId()
    {
        return this.fileUploadErrorTypeId;
    }






    public static FileUploadErrorType getType( int typeId )
    {
        return getValue( typeId );
    }


    public String getKey()
    {
        return key;
    }



    public static FileUploadErrorType getValue( int id )
    {
        FileUploadErrorType[] vals = FileUploadErrorType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getFileUploadErrorTypeId() == id )
                return vals[i];
        }

        return UNKNOWN_IMO;
    }

}
