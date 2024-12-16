package com.tm2ref.file;


public enum UploadedUserFileType
{
    RESPONSE(0,"User Response"),
    REMOTE_PROCTORING(100,"Remote Proctoring Audio/Video (with Image Thumbs)"),
    REMOTE_PROCTORING_IMAGES(101,"Remote Proctoring Image Thumbs (Only)"),
    REMOTE_PROCTORING_ID(102,"Remote Proctoring IDs (Only)"),
    REF_CHECK_IMAGES(201,"Reference Check Candidate Image Thumbs (Only)"),
    REF_CHECK_ID(202,"Reference Check Candidate IDs (Only)"),
    REF_CHECK_RATER(211,"Reference Check Rater Images (Only)"),
    REF_CHECK_RATER_ID(212,"Reference Check Rater IDs (Only)"),
    REF_CHECK_RATER_COMMENT(220,"Reference Check Rater Comment" ),
    REF_CHECK_CANDIDATE_FILE_UPLOAD(221,"Reference Check Candidate File Upload" ),
    CT5(230,"CT5 media" ),
    CT5_GENERAL(231,"CT5 General Uploaded File (for download inside item)" ),
    USER_GENERAL(301,"General Uploaded File Associated with a User" );



    private final int uploadedUserFileTypeId;

    private String key;


    private UploadedUserFileType( int p , String key )
    {
        this.uploadedUserFileTypeId = p;

        this.key = key;
    }
    
    public boolean getIsAnyRc()
    {
        return equals( REF_CHECK_IMAGES ) || equals( REF_CHECK_ID ) || equals( REF_CHECK_RATER ) || equals( REF_CHECK_RATER_ID ) || equals( REF_CHECK_RATER_COMMENT );        
    }
    
    public boolean getIsRcComment()
    {
        return equals( REF_CHECK_RATER_COMMENT );        
    }
    

    public boolean getIsRcPhotoOrId()
    {
        return equals( REF_CHECK_IMAGES ) || equals( REF_CHECK_ID ) || equals( REF_CHECK_RATER ) || equals( REF_CHECK_RATER_ID );        
    }
    
    
    public boolean getIsRcCandidatePhoto()
    {
        return equals( REF_CHECK_IMAGES );        
    }
    
    public boolean getIsRcCandidateId()
    {
        return equals( REF_CHECK_ID );        
    }
    
    public boolean getIsRcRaterPhoto()
    {
        return equals( REF_CHECK_RATER );        
    }
    
    public boolean getIsRcRaterId()
    {
        return equals( REF_CHECK_RATER_ID );        
    }
    

    public boolean getIsResponse()
    {
        return equals( RESPONSE );
    }


    public boolean getIsAnyId()
    {
        return equals( REMOTE_PROCTORING_ID ) || equals(REF_CHECK_ID) || equals( REF_CHECK_RATER_ID );
    }



    public int getUploadedUserFileTypeId()
    {
        return this.uploadedUserFileTypeId;
    }




    public String getName()
    {
        return key;
    }



    public static UploadedUserFileType getValue( int id )
    {
        UploadedUserFileType[] vals = UploadedUserFileType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUploadedUserFileTypeId() == id )
                return vals[i];
        }

        return RESPONSE;
    }

}
