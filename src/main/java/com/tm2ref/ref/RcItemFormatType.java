package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcItem;
import com.tm2ref.entity.ref.RcRating;
import com.tm2ref.service.LogService;
import java.util.Locale;



/**
 * @author miker_000
 */
public enum RcItemFormatType
{
    NONE(0,"Not Set", "rcift.none" ),
    RATING(1,"Rating and/Or Comments", "rcift.rating" ),  // has comments and ratings. This is the standard question type.
    RADIO(2,"Radios Plus Comments", "rcift.radiospluscomments" ),  
    BUTTON(3,"Buttons Only - No Comments", "rcift.button" ),  
    MULTIPLE_CHECKBOX(4,"Multiple Checkboxes - No Comments", "rcift.multicheckbox" ),
    COMMENTS_ONLY(5,"Comments Only", "rcift.commentsonly" );
    //CANDIDATE_FILE_UPLOAD(6,"Candidate File Upload", "rcift.candfileupload" );  

    private final int rcItemFormatTypeId;

    private final String name;
    private final String key;
    // private final String page;


    private RcItemFormatType( int s , String n, String k )
    {
        this.rcItemFormatTypeId = s;

        this.name = n;
        this.key = k;
        // this.page = p;
    }
    
    public boolean getShowPrimaryItemToCandidate( RcItem rcItem, RcRating rating )
    {
        if( rating==null )
            return false;
        
        if( equals( RATING ) )
            return rating.getSelectedResponse()!=null && !rating.getSelectedResponse().isBlank() && !rating.getSelectedResponse().equalsIgnoreCase("0");
        
        if( equals( RADIO ) || equals( BUTTON ) || equals( MULTIPLE_CHECKBOX ))
            return rating.getSelectedResponse()!=null && !rating.getSelectedResponse().isBlank();
               
        return false;

    }

    
    public String getResponseValueForRating( RcItem rcItem, Locale locale, RcRating rating )
    {
        if( rating==null )
            return "";
        
        if( equals( RATING ) )
            return rating.getSelectedResponse();
        
        if( equals( RADIO ) || equals( BUTTON ) || equals( MULTIPLE_CHECKBOX ))
        {
            rating.setRcItem(rcItem);
            return rating.getSelectedChoicesText();
        }
               
        return "";

    }
    
    
    public boolean getIsScoreOk()
    {
        return getIsRating() || getHasChoicePoints();
    }
    
    public boolean getIsRating()
    {
        return equals(RATING);
    }
    
    public boolean getIsCommentsOnly()
    {
        return equals(COMMENTS_ONLY);
    }

    //public boolean getIsCandidateFileUpload()
    //{
    //    return equals(CANDIDATE_FILE_UPLOAD);
    //}

    public boolean getIsRadio()
    {
        return equals(RADIO);
    }
    public boolean getIsButton()
    {
        return equals(BUTTON);
    }
    public boolean getIsMultCheckbox()
    {
        return equals(MULTIPLE_CHECKBOX);
    }
    public boolean getIsCheckbox()
    {
        return equals(MULTIPLE_CHECKBOX);
    }
        
    public boolean getCanHaveComments()
    {
        return equals(RATING) || equals(RADIO) || equals(MULTIPLE_CHECKBOX) || equals(COMMENTS_ONLY); // || equals(CANDIDATE_FILE_UPLOAD);
    }

    
    public boolean getHasChoices()
    {
        return equals(RADIO) || equals(BUTTON) || equals(MULTIPLE_CHECKBOX);
    }

    public boolean getHasChoicePoints()
    {
        return equals(RADIO) || equals(BUTTON) || equals(MULTIPLE_CHECKBOX);
    }
    
    public static RcItemFormatType getValue( int id )
    {
        RcItemFormatType[] vals = RcItemFormatType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcItemFormatTypeId() == id )
                return vals[i];
        }

        return NONE;
    }

    public String getKey() {
        return key;
    }

    //public String getPage() {
    //    return page;
    //}


    public int getRcItemFormatTypeId()
    {
        return rcItemFormatTypeId;
    }

    public String getName()
    {
        return name;
    }

}
