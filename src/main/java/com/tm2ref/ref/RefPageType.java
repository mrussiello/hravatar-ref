package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCheck;



/**
 * @author miker_000
 */
public enum RefPageType
{
    ENTRY(0,"Entry", "index.xhtml", "index.xhtml" ),
    CONFIRM(10, "Confirm", "confirm.xhtml", "confirm-r.xhtml" ),
    INTRO(20,"Intro", "intro.xhtml" , "intro-r.xhtml" ),
    RELEASE(30,"Release", "release.xhtml", "release-r.xhtml" ),
    SPECIAL(40,"Special Instructions", "special.xhtml", "special.xhtml" ),
    AVCOMMENTS(41,"AvComments Allowed", "avcommentsallowed.xhtml", "avcommentsallowed.xhtml" ),
    PHOTO(45,"Photo", "photo.xhtml", "photo.xhtml" ),
    ID_PHOTO(46,"Id Photo", "photo-id.xhtml", "photo-id.xhtml" ),
    PRE_QUESTIONS(47,"Pre-Questions", "pre-questions-candidate.xhtml", null ),
    CORE(50,"Core", "question.xhtml" , "item.xhtml" ),
    CORE2(60,"Core2", "item.xhtml", "referrals.xhtml" ),
    CORE3(70,"Core3", "references.xhtml", null ),
    COMPLETE(100,"Complete", "complete.xhtml" , "complete-r.xhtml" ),
    CANCELLED(200,"Cancelled", "cancelled.xhtml", "cancelled-r.xhtml" ),
    ERROR(205,"Error", "error-fatal.xhtml", "error-fatal.xhtml" ),
    EXPIRED(210,"Expired", "expired.xhtml", "expired-r.xhtml" );  

    private final int refPageTypeId;

    private final String name;
    private final String candidatePage;
    private final String raterPage;


    private RefPageType( int s , String n, String p, String pr )
    {
        this.refPageTypeId = s;

        this.name = n;
        this.candidatePage = p;
        this.raterPage = pr;
    }
    
    public boolean getIsCore()
    {
        return equals( CORE );
    }
    public boolean getIsCore2()
    {
        return equals(CORE2 );
    }
    public boolean getIsCore3()
    {
        return equals(CORE3 );
    }

    public boolean getIsAnyCore()
    {
        return equals(CORE ) || equals(CORE2 ) || equals(CORE3 );
    }

    
    public boolean getIsAnyPhotoCapture()
    {
        return equals(PHOTO) || equals(ID_PHOTO);
    }
    
    
    
    public String getPageFull( RefUserType rcUserType )
    {
        return "/ref/" + getPage( rcUserType );
    }

    public String getPage( RefUserType rcUserType )
    {
        return (rcUserType==null || rcUserType.getIsCandidate()) ? candidatePage : raterPage;
    }

    public RefPageType getNextPageTypeNoNull( RefUserType rcUserType )
    {
        RefPageType pt = getNextPageType( this );
        
        while(pt.getPage(rcUserType)==null)
        {
            pt = getNextPageType(pt );
        } 
        return pt;
    }
    
    
    public RefPageType getPreviousPageTypeNoNull( RefUserType rcUserType, RcCheck rc )
    {
        RefPageType pt = getPreviousPageType( this );    
                
        while(pt.getPage(rcUserType) == null)
        {
            pt = getPreviousPageType( pt );
        } 
        
        if( pt.getIsCore2() && rcUserType.getIsCandidate() && ( !rc.getCollectRatingsFmCandidate() || !rc.getRcScript().getHasAnyCandidateRatings()) )
            pt = pt.getPreviousPageTypeNoNull(rcUserType, rc);
        
        if( pt.getIsCore3() && rcUserType.getIsCandidate() && !rc.getCandidateCanAddRaters() )
            pt = pt.getPreviousPageTypeNoNull(rcUserType, rc);
        
        return pt;
    }
    
        
    
    private RefPageType getNextPageType( RefPageType currentPageType )
    {
        switch( currentPageType )
        {
            case ENTRY:
                return ENTRY;
            case CONFIRM:
                return INTRO;
            case INTRO:
                return RELEASE;
            case RELEASE:
                return SPECIAL;
            case SPECIAL:
                return AVCOMMENTS;
            case AVCOMMENTS:
                return PHOTO;
            case PHOTO:
                return ID_PHOTO;
            case ID_PHOTO:
                return PRE_QUESTIONS;
            case PRE_QUESTIONS:
                 return CORE;
            case CORE:
                return CORE2;
            case CORE2:
                return CORE3;
            case CORE3:
                return COMPLETE;
            case COMPLETE:
                return COMPLETE;
            default:
                return ENTRY;
        }
    }


    private RefPageType getPreviousPageType( RefPageType currentPageType )
    {
        switch( currentPageType )
        {
            case ENTRY:
                return ENTRY;
            case CONFIRM:
                return ENTRY;
            case INTRO:
                return CONFIRM;
            case RELEASE:
                return INTRO;
            case SPECIAL:
                return RELEASE;
            case AVCOMMENTS:
                return SPECIAL;
            case PRE_QUESTIONS:
                return AVCOMMENTS;
            case CORE:
                return PRE_QUESTIONS;
            case CORE2:
                return CORE;
            case CORE3:
                return CORE2;
            case COMPLETE:
                return CORE3;
            default:
                return ENTRY;
        }
    }
    
    public static RefPageType getValue( int id )
    {
        RefPageType[] vals = RefPageType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRefPageTypeId() == id )
                return vals[i];
        }

        return null;
    }

    public int getRefPageTypeId()
    {
        return refPageTypeId;
    }

    public String getName()
    {
        return name;
    }

}
