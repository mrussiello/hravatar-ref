package com.tm2ref.entity.ref;


import com.tm2ref.ref.RcImportanceType;
import com.tm2ref.ref.RcItemFormatType;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;


@Entity
@Table( name = "rcitem" )
@NamedQueries({  
    @NamedQuery( name = "RcItem.findByCompetencyId", query = "SELECT o FROM RcItem AS o WHERE o.rcCompetencyId=:rcCompetencyId ORDER BY o.displayOrder" )
})
public class RcItem implements Serializable, Cloneable, Comparable<RcItem>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="rcitemid")
    private int rcItemId;

    @Column(name="orgid")
    private int orgId;
    
    @Column(name="authoruserid")
    private long authorUserId;
                
    @Column(name="rccompetencyid")
    private int rcCompetencyId;

    @Column(name="langcode")
    private String langCode;
        
    @Column(name="displayorder")
    private int displayOrder;
    
    
    //@Column(name="name")
    // private String name;

    @Column(name="itemformattypeid")
    private int itemFormatTypeId;
    
    @Column(name="importancetypeid")
    private int importanceTypeId;
    
    @Column(name="infotext")
    private String infoText;
    
    @Column(name="question")
    private String question;
    
    @Column(name="questioncandidate")
    private String questionCandidate;

    
    @Column(name="commentsplaceholder")
    private String commentsPlaceholder;
    
    @Column(name="skipforcandidate")
    private int skipforCandidate;
    
    
    @Column(name="choice1")
    private String choice1;
    
    @Column(name="choice2")
    private String choice2;
    
    @Column(name="choice3")
    private String choice3;
    
    @Column(name="choice4")
    private String choice4;
    
    @Column(name="choice5")
    private String choice5;
    
    @Column(name="choice6")
    private String choice6;
    
    @Column(name="choice7")
    private String choice7;
    
    @Column(name="choice8")
    private String choice8;
    
    @Column(name="choice9")
    private String choice9;
    
    @Column(name="choice10")
    private String choice10;
    
    @Column(name="choicepoints1")
    private float choicePoints1;

    @Column(name="choicepoints2")
    private float choicePoints2;

    @Column(name="choicepoints3")
    private float choicePoints3;

    @Column(name="choicepoints4")
    private float choicePoints4;
    
    @Column(name="choicepoints5")
    private float choicePoints5;
    
    @Column(name="choicepoints6")
    private float choicePoints6;

    @Column(name="choicepoints7")
    private float choicePoints7;

    @Column(name="choicepoints8")
    private float choicePoints8;

    @Column(name="choicepoints9")
    private float choicePoints9;

    @Column(name="choicepoints10")
    private float choicePoints10;
    
    @Column(name="denymiddle")
    private int denyMiddle;
    
    @Column(name="hideskip")
    private int hideSkip;
    
    @Column(name="skipbuttontext")
    private String skipButtonText;
    
    

    @Column(name="includenumrating")
    private int includeNumRating;

    @Column(name="includecomments")
    private int includeComments;

    @Column(name="commentthresholdlow")
    private float commentThresholdLow;

    @Column(name="commentthresholdhigh")
    private float commentThresholdHigh;
    
    /*
     0=none,
     1=audio
     2=video
    */
    @Column(name="includeavinput")
    private int includeAvInput;

    /*
     * for rating items, indicates invert the numeric scale.
    */
    @Column(name="intparam1")
    private int intParam1;
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;
    
    
    
    
    //@Transient
    //private List<RcRating> rcRatingList;
    
    @Transient
    private RcCompetency rcCompetency;
    
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public int compareTo(RcItem o) {
        return ((Integer)displayOrder).compareTo( o.getDisplayOrder());
    }
    
    
    
    public RcItemFormatType getRcItemFormatType()
    {
        return RcItemFormatType.getValue( itemFormatTypeId );
    }
    
    
    public RcImportanceType getRcImportanceType()
    {
        return RcImportanceType.getValue( importanceTypeId );
    }
    

    @Override
    public String toString() {
        return "RcItem{" + "rcItemId=" + rcItemId + ", orgId=" + orgId + '}';
    }

    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.rcItemId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RcItem other = (RcItem) obj;
        if (this.rcItemId != other.rcItemId) {
            return false;
        }
        return true;
    }

    public int getAnchorwidthPixMsie()
    {
        return anchorwidthPix( 520 );
    }
    public int getAnchorwidthPix()
    {
        return anchorwidthPix( 740 );
    }
    
    private int anchorwidthPix( int total )
    {
        // has a middle value.
        if( getHasChoice3() )
        {
            // 4 or 5 choices
            if( getHasChoice4() || getHasChoice5() )
                return (total - 40)/5;
            
            // 3 choices
            return (total - 20)/3;
        }
        
        // 4 choices, no middle value
        if( getHasChoice4() || getHasChoice5() )
            return (total - 30)/4;

        // 2 choices.
        return (total - 100)/2;
    }
    
    public String getRcItemIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( rcItemId );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcItem.getRcItemIdEncrypted() " + toString()  );
            return "";
        }
    }

    public String getChoiceForIndex( int idx )
    {
        if( idx == 1 )
            return choice1;
        if( idx == 2 )
            return choice2;
        if( idx == 3 )
            return choice3;
        if( idx == 4 )
            return choice4;
        if( idx == 5 )
            return choice5;
        if( idx == 6 )
            return choice6;
        if( idx == 7 )
            return choice7;
        if( idx == 8 )
            return choice8;
        if( idx == 9 )
            return choice9;
        if( idx == 10 )
            return choice10;
        return null;
    }
    
    
    public float getScoreForIndex( int idx )
    {
        if( idx == 1 )
            return choicePoints1;
        if( idx == 2 )
            return choicePoints2;
        if( idx == 3 )
            return choicePoints3;
        if( idx == 4 )
            return choicePoints4;
        if( idx == 5 )
            return choicePoints5;
        if( idx == 6 )
            return choicePoints6;
        if( idx == 7 )
            return choicePoints7;
        if( idx == 8 )
            return choicePoints8;
        if( idx == 9 )
            return choicePoints9;
        if( idx == 10 )
            return choicePoints10;
        return 0;
    }
    
    public boolean getIsItemScored()
    {
        if( getRcItemFormatType().getIsRating() )
            return getIncludeNumRatingB();
        if( !getRcItemFormatType().getHasChoicePoints() )
            return false;
        if( this.getHasChoice1() && this.choicePoints1>0 )
            return true;
        if( this.getHasChoice2() && this.choicePoints2>0 )
            return true;
        if( this.getHasChoice3() && this.choicePoints3>0 )
            return true;
        if( this.getHasChoice4() && this.choicePoints4>0 )
            return true;
        if( this.getHasChoice5() && this.choicePoints5>0 )
            return true;
        if( this.getHasChoice6() && this.choicePoints6>0 )
            return true;
        if( this.getHasChoice7() && this.choicePoints7>0 )
            return true;
        if( this.getHasChoice8() && this.choicePoints8>0 )
            return true;
        if( this.getHasChoice9() && this.choicePoints9>0 )
            return true;
        if( this.getHasChoice10() && this.choicePoints10>0 )
            return true;
        return false;
    }
    
    public boolean getHasInfoText()
    {
        return this.infoText!=null && !this.infoText.isBlank();
    }
        
    public boolean getHasChoice1()
    {
        return choice1!=null && !choice1.isBlank();
    }
    public boolean getHasChoice2()
    {
        return choice2!=null && !choice2.isBlank();
    }
    public boolean getHasChoice3()
    {
        return choice3!=null && !choice3.isBlank();
    }
    public boolean getHasChoice4()
    {
        return choice4!=null && !choice4.isBlank();
    }
    public boolean getHasChoice5()
    {
        return choice5!=null && !choice5.isBlank();
    }
    public boolean getHasChoice6()
    {
        return choice6!=null && !choice6.isBlank();
    }
    public boolean getHasChoice7()
    {
        return choice7!=null && !choice7.isBlank();
    }
    public boolean getHasChoice8()
    {
        return choice8!=null && !choice8.isBlank();
    }
    public boolean getHasChoice9()
    {
        return choice9!=null && !choice9.isBlank();
    }
    public boolean getHasChoice10()
    {
        return choice10!=null && !choice10.isBlank();
    }
    
    public int getRcItemId() {
        return rcItemId;
    }

    public void setRcItemId(int rcItemId) {
        this.rcItemId = rcItemId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public long getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(long authorUserId) {
        this.authorUserId = authorUserId;
    }

    public int getRcCompetencyId() {
        return rcCompetencyId;
    } 

    public void setRcCompetencyId(int rcCompetencyId) {
        this.rcCompetencyId = rcCompetencyId;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getChoice1() {
        return choice1;
    }

    public void setChoice1(String choice1) {
        this.choice1 = choice1;
    }

    public String getChoice2() {
        return choice2;
    }

    public void setChoice2(String choice2) {
        this.choice2 = choice2;
    }

    public int getIncludeNumRating() {
        return includeNumRating;
    }

    public void setIncludeNumRating(int includeNumRating) {
        this.includeNumRating = includeNumRating;
    }

    public boolean getIncludeNumRatingB() {
        return includeNumRating==1;
    }

    public void setIncludeNumRatingB(boolean b) {
        this.includeNumRating = b ? 1 : 0;
    }
    
    
    public int getIncludeComments() {
        return includeComments;
    }

    public void setIncludeComments(int includeComments) {
        this.includeComments = includeComments;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public RcCompetency getRcCompetency() {
        return rcCompetency;
    }

    public void setRcCompetency(RcCompetency rcCompetency) {
        this.rcCompetency = rcCompetency;
    }

    public int getItemFormatTypeId() {
        return itemFormatTypeId;
    }

    public void setItemFormatTypeId(int itemFormatTypeId) {
        this.itemFormatTypeId = itemFormatTypeId;
    }

    public int getImportanceTypeId() {
        return importanceTypeId;
    }

    public void setImportanceTypeId(int importanceTypeId) {
        this.importanceTypeId = importanceTypeId;
    }

    public int getIncludeAvInput() {
        return includeAvInput;
    }

    public void setIncludeAvInput(int includeAvInput) {
        this.includeAvInput = includeAvInput;
    }

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public String getChoice3() {
        return choice3;
    }

    public void setChoice3(String choice3) {
        this.choice3 = choice3;
    }

    public String getChoice4() {
        return choice4;
    }

    public void setChoice4(String choice4) {
        this.choice4 = choice4;
    }

    public String getChoice5() {
        return choice5;
    }

    public void setChoice5(String choice5) {
        this.choice5 = choice5;
    }

    public String getChoice6() {
        return choice6;
    }

    public void setChoice6(String choice6) {
        this.choice6 = choice6;
    }

    public String getChoice7() {
        return choice7;
    }

    public void setChoice7(String choice7) {
        this.choice7 = choice7;
    }

    public String getChoice8() {
        return choice8;
    }

    public void setChoice8(String choice8) {
        this.choice8 = choice8;
    }

    public String getChoice9() {
        return choice9;
    }

    public void setChoice9(String choice9) {
        this.choice9 = choice9;
    }

    public String getChoice10() {
        return choice10;
    }

    public void setChoice10(String choice10) {
        this.choice10 = choice10;
    }

    public String getCommentsPlaceholder() {
        return commentsPlaceholder;
    }

    public void setCommentsPlaceholder(String commentsPlaceholder) {
        this.commentsPlaceholder = commentsPlaceholder;
    }

    public float getChoicePoints1() {
        return choicePoints1;
    }

    public void setChoicePoints1(float choicePoints1) {
        this.choicePoints1 = choicePoints1;
    }

    public float getChoicePoints2() {
        return choicePoints2;
    }

    public void setChoicePoints2(float choicePoints2) {
        this.choicePoints2 = choicePoints2;
    }

    public float getChoicePoints3() {
        return choicePoints3;
    }

    public void setChoicePoints3(float choicePoints3) {
        this.choicePoints3 = choicePoints3;
    }

    public float getChoicePoints4() {
        return choicePoints4;
    }

    public void setChoicePoints4(float choicePoints4) {
        this.choicePoints4 = choicePoints4;
    }

    public float getChoicePoints5() {
        return choicePoints5;
    }

    public void setChoicePoints5(float choicePoints5) {
        this.choicePoints5 = choicePoints5;
    }

    public float getChoicePoints6() {
        return choicePoints6;
    }

    public void setChoicePoints6(float choicePoints6) {
        this.choicePoints6 = choicePoints6;
    }

    public float getChoicePoints7() {
        return choicePoints7;
    }

    public void setChoicePoints7(float choicePoints7) {
        this.choicePoints7 = choicePoints7;
    }

    public float getChoicePoints8() {
        return choicePoints8;
    }

    public void setChoicePoints8(float choicePoints8) {
        this.choicePoints8 = choicePoints8;
    }

    public float getChoicePoints9() {
        return choicePoints9;
    }

    public void setChoicePoints9(float choicePoints9) {
        this.choicePoints9 = choicePoints9;
    }

    public float getChoicePoints10() {
        return choicePoints10;
    }

    public void setChoicePoints10(float choicePoints10) {
        this.choicePoints10 = choicePoints10;
    }

    public int getHideSkip() {
        return hideSkip;
    }

    public void setHideSkip(int hideSkip) {
        this.hideSkip = hideSkip;
    }

    public String getSkipButtonText() {
        return skipButtonText;
    }

    public void setSkipButtonText(String skipButtonText) {
        this.skipButtonText = skipButtonText;
    }

    public int getSkipforCandidate() {
        return skipforCandidate;
    }

    public void setSkipforCandidate(int skipforCandidate) {
        this.skipforCandidate = skipforCandidate;
    }

    public String getQuestionCandidate() {
        return questionCandidate;
    }

    public void setQuestionCandidate(String questionCandidate) {
        this.questionCandidate = questionCandidate;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public float getCommentThresholdLow() {
        return commentThresholdLow;
    }

    public void setCommentThresholdLow(float commentThresholdLow) {
        this.commentThresholdLow = commentThresholdLow;
    }

    public float getCommentThresholdHigh() {
        return commentThresholdHigh;
    }

    public void setCommentThresholdHigh(float commentThresholdHigh) {
        this.commentThresholdHigh = commentThresholdHigh;
    }

    public int getDenyMiddle() {
        return denyMiddle;
    }

    public void setDenyMiddle(int denyMiddle) {
        this.denyMiddle = denyMiddle;
    }

    public boolean getDenyMiddleB() {
        return denyMiddle==1;
    }


    
}
