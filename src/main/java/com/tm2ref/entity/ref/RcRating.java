package com.tm2ref.entity.ref;


import com.tm2ref.entity.essay.UnscoredEssay;
import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.user.User;
import com.tm2ref.ref.RcCheckUtils;
import com.tm2ref.ref.RcRatingStatusType;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
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
@Table( name = "rcrating" )
@NamedQueries({
    
        @NamedQuery( name = "RcRating.findByRcRatingId", query = "SELECT o FROM RcRating AS o WHERE o.rcRatingId=:rcRatingId" ),    
        @NamedQuery( name = "RcRating.findByRcCheckAndRater", query = "SELECT o FROM RcRating AS o WHERE o.rcCheckId=:rcCheckId AND o.rcRaterId=:rcRaterId" ),    
        @NamedQuery( name = "RcRating.findByRcCheck", query = "SELECT o FROM RcRating AS o WHERE o.rcCheckId=:rcCheckId" ),
        @NamedQuery( name = "RcRating.findByRaterIdAndItemId", query = "SELECT o FROM RcRating AS o WHERE o.rcRaterId=:rcRaterId AND o.rcItemId=:rcItemId" )
        
})
public class RcRating implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="rcratingid")
    private long rcRatingId;

    @Column(name="rcraterid")
    private long rcRaterId;
    
    @Column(name="rccheckid")
    private long rcCheckId;
        
    @Column(name="rcitemid")
    private int rcItemId;

    @Column(name="displayorder")
    private int displayorder;

    @Column(name="rcitemformattypeid")
    private int rcItemFormatTypeId;

    @Column(name="selectedresponse")
    private String selectedResponse;

    
    @Column(name="rcratingstatustypeid")
    private int rcRatingStatusTypeId;

    @Column(name="score")
    private float score;

    @Column(name="score2")
    private float score2;

    @Column(name="score3")
    private float score3;
    
    @Column(name="score4")
    private float score4;

    @Column(name="score5")
    private float score5;

    @Column(name="score6")
    private float score6;

    @Column(name="score7")
    private float score7;

    @Column(name="score8")
    private float score8;

    @Column(name="score9")
    private float score9;

    @Column(name="score10")
    private float score10;

    @Column(name="score11")
    private float score11;

    @Column(name="score12")
    private float score12;

    @Column(name="score13")
    private float score13;

    @Column(name="score14")
    private float score14;

    @Column(name="score15")
    private float score15;
    
    @Column(name="aiscoresstatustypeid")
    private int aiScoresStatusTypeId;
    
    @Column(name="aisummarystatustypeid")
    private int aiSummaryStatusTypeId;
    
    
    @Column(name="uploadeduserfileid")
    private long uploadedUserFileId;
    
    @Column(name="candidateuploadeduserfileid")
    private long candidateUploadedUserFileId;

    @Column(name="text")
    private String text;
    
    @Column(name="summary")
    private String summary;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="completedate")
    private Date completeDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="aiscoredate")
    private Date aiScoreDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="aisummarydate")
    private Date aiSummaryDate;
    
    @Transient
    private RcRater rcRater;
    
    
    @Transient
    private User user;
    
    @Transient
    private RcItem rcItem;
    
    @Transient
    private String subtext;
    
    @Transient
    private RcUploadedUserFile rcUploadedUserFile;
    
    @Transient
    private RcUploadedUserFile candidateRcUploadedUserFile;
    
    @Transient
    private RcRating candidateRcRating;
    
    @Transient
    private UnscoredEssay unscoredEssay;
    
    
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString() {
        return "RcRating{" + "rcRatingId=" + rcRatingId + ", rcRaterId=" + rcRaterId + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (int) (this.rcRatingId ^ (this.rcRatingId >>> 32));
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
        final RcRating other = (RcRating) obj;
        if (this.rcRatingId != other.rcRatingId) {
            return false;
        }
        return true;
    }
    
    public float[] getScoresArray()
    {
        float[] out = new float[16];
        out[0]=score;
        out[1]=score;
        out[2]=score2;  // ai score
        out[3]=score3;  // ai confidence
        out[4]=score4;  
        out[5]=score5;
        out[6]=score6;  // total words
        out[7]=score7;
        out[8]=score8;
        out[9]=score9;
        out[10]=score10;
        out[11]=score11;
        out[12]=score12; // clarity
        out[13]=score13; // argument
        out[14]=score14; // mechanics
        out[15]=score15; // ideal match
        return out;
    }
    

    //public boolean getHasValidRecording()
    //{
    //    return rcUploadedUserFile!=null && rcUploadedUserFile.getHasValidInitialFile();
    //}

    public boolean getHasRecordingReadyForPlayback()
    {
        return rcUploadedUserFile!=null && rcUploadedUserFile.getHasRecordingReadyForPlayback();
    }

    public boolean getHasRecordingInConversion()
    {
        return rcUploadedUserFile!=null && rcUploadedUserFile.getHasRecordingInConversion();
    }

    
    
    public String getSelectedChoicesTextXhtml()
    {
        return StringUtils.replaceStandardEntities( getSelectedChoicesText());
    }
    
    public String getSelectedChoicesText()
    {
        //LogService.logIt( "RcRating.getSelectedChoicesText() AAA rcItemId=" + this.rcItemId + ", selectedResponse=" + selectedResponse + ", item: " + (rcItem!=null ? " has choices=" + rcItem.getRcItemFormatType().getHasChoices() : "null" ) + ", complete=" + getRcRatingStatusType().getIsComplete() );
        
        if( rcItem==null || !rcItem.getRcItemFormatType().getHasChoices()  || !getRcRatingStatusType().getIsComplete())
            return "";
        
        //LogService.logIt( "RcRating.getSelectedChoicesText() BBB " );
        if( this.selectedResponse==null || this.selectedResponse.isBlank() )
            return "";
        
        //LogService.logIt( "RcRating.getSelectedChoicesText() CCC " );
        StringBuilder sb = new StringBuilder();

        try
        {
            int idx;
            String chc;
            for( String s : selectedResponse.split(","))
            {
                if( s.isBlank() )
                    continue;
                idx=Integer.parseInt(s);
                chc = rcItem.getChoiceForIndex(idx);
                if( chc!=null && !chc.isBlank() )
                {
                    if( sb.length()>0 )
                        sb.append(",\n");                    
                    sb.append( chc );
                }
            }
        }
        catch(NumberFormatException e)
        {
            LogService.logIt("RcRating.selectedChoicesText() Unable to parse selectedResponse=" + selectedResponse + ", rcRatingId=" + this.rcRatingId + ", rcCheckId=" + rcCheckId );
        }
        return sb.toString();
    }
    
    public void clearAiScores()
    {
        score2=0;
        score3=0;
        score4=0;
        score5=0;
        score6=0;
        score7=0;
        score8=0;
        score9=0;
        score10=0;
        score11=0;
        score12=0;
        score13=0;
        score14=0;
        score15=0;
        aiScoreDate=null;
    }

    public void clearAiSummary()
    {
        summary=null;
        aiSummaryDate=null;
    }
    
    
    public float getFinalScore()
    {
        if( rcItem==null || !rcItem.getRcItemFormatType().getIsRating() || rcItem.getIntParam1()!=1 )
            return score;
        
        return RcCheckUtils.invertRatingScore( score, rcRater!=null && rcRater.getRcCheck()!=null && rcRater.getRcCheck().getRcScript()!=null ? rcRater.getRcCheck().getRcScript().getRcRatingScaleType() : null );
    }
    
    public int getScoreInteger()
    {
        return Math.round(score);
    }
    
    public void setScoreInteger( int s )
    {
        score = s;
    }
        
    public RcRatingStatusType getRcRatingStatusType()
    {
        return RcRatingStatusType.getValue(this.rcRatingStatusTypeId);
    }
    
    public boolean getIsComplete()
    {
        return getRcRatingStatusType().getIsComplete(); 
    }

    public boolean getIsCompleteOrHigher()
    {
        return getRcRatingStatusType().getIsCompleteOrHigher(); 
    }
    public boolean getIsSkipped()
    {
        return getRcRatingStatusType().getIsSkipped(); 
    }
    
    public boolean getHasNumericScore()
    {
        return this.score>0; 
    }
    
    public long getRcRatingId() {
        return rcRatingId;
    }

    public void setRcRatingId(long rcRatingId) {
        this.rcRatingId = rcRatingId;
    }

    public long getRcRaterId() {
        return rcRaterId;
    }

    public void setRcRaterId(long rcRaterId) {
        this.rcRaterId = rcRaterId;
    }

    public long getRcCheckId() {
        return rcCheckId;
    }

    public void setRcCheckId(long rcCheckId) {
        this.rcCheckId = rcCheckId;
    }

    public int getRcItemId() {
        return rcItemId;
    }

    public void setRcItemId(int rcItemId) {
        this.rcItemId = rcItemId;
    }

    public int getDisplayorder() {
        return displayorder;
    }

    public void setDisplayorder(int displayorder) {
        this.displayorder = displayorder;
    }

    public int getRcRatingStatusTypeId() {
        return rcRatingStatusTypeId;
    }

    public void setRcRatingStatusTypeId(int rcRatingStatusTypeId) {
        this.rcRatingStatusTypeId = rcRatingStatusTypeId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getUploadedUserFileId() {
        return uploadedUserFileId;
    }

    public void setUploadedUserFileId(long uploadedUserFileId) {
        this.uploadedUserFileId = uploadedUserFileId;
    }

    public Date getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public int getRcItemFormatTypeId() {
        return rcItemFormatTypeId;
    }

    public void setRcItemFormatTypeId(int rcItemFormatTypeId) {
        this.rcItemFormatTypeId = rcItemFormatTypeId;
    }

    public String getSelectedResponse() {
        return selectedResponse;
    }

    public void setSelectedResponse(String selectedResponse) {
        this.selectedResponse = selectedResponse;
    }

    public RcItem getRcItem() {
        return rcItem;
    }

    public void setRcItem(RcItem rcItem) {
        this.rcItem = rcItem;
    }

    public String getSubtext() {
        return subtext;
    }

    public void setSubtext(String subtext) {
        this.subtext = subtext;
    }

    public RcRater getRcRater() {
        return rcRater;
    }

    public void setRcRater(RcRater rcRater) {
        this.rcRater = rcRater;
    }

    public RcUploadedUserFile getRcUploadedUserFile() {
        return rcUploadedUserFile;
    }

    public void setRcUploadedUserFile(RcUploadedUserFile rcUploadedUserFile) {
        this.rcUploadedUserFile = rcUploadedUserFile;
    }

    public long getCandidateUploadedUserFileId() {
        return candidateUploadedUserFileId;
    }

    public void setCandidateUploadedUserFileId(long candidateUploadedUserFileId) {
        this.candidateUploadedUserFileId = candidateUploadedUserFileId;
    }

    public RcUploadedUserFile getCandidateRcUploadedUserFile() {
        return candidateRcUploadedUserFile;
    }

    public void setCandidateRcUploadedUserFile(RcUploadedUserFile candidateRcUploadedUserFile) {
        this.candidateRcUploadedUserFile = candidateRcUploadedUserFile;
    }

    public RcRating getCandidateRcRating() {
        return candidateRcRating;
    }

    public void setCandidateRcRating(RcRating candidateRcRating) {
        this.candidateRcRating = candidateRcRating;
    }

    public UnscoredEssay getUnscoredEssay()
    {
        return unscoredEssay;
    }

    public void setUnscoredEssay(UnscoredEssay unscoredEssay)
    {
        this.unscoredEssay = unscoredEssay;
    }

    public float getScore2()
    {
        return score2;
    }

    public void setScore2(float score2)
    {
        this.score2 = score2;
    }

    public float getScore3()
    {
        return score3;
    }

    public void setScore3(float score3)
    {
        this.score3 = score3;
    }

    public float getScore4()
    {
        return score4;
    }

    public void setScore4(float score4)
    {
        this.score4 = score4;
    }

    public float getScore5()
    {
        return score5;
    }

    public void setScore5(float score5)
    {
        this.score5 = score5;
    }

    public float getScore6()
    {
        return score6;
    }

    public void setScore6(float score6)
    {
        this.score6 = score6;
    }

    public float getScore7()
    {
        return score7;
    }

    public void setScore7(float score7)
    {
        this.score7 = score7;
    }

    public float getScore8()
    {
        return score8;
    }

    public void setScore8(float score8)
    {
        this.score8 = score8;
    }

    public float getScore9()
    {
        return score9;
    }

    public void setScore9(float score9)
    {
        this.score9 = score9;
    }

    public float getScore10()
    {
        return score10;
    }

    public void setScore10(float score10)
    {
        this.score10 = score10;
    }

    public float getScore11()
    {
        return score11;
    }

    public void setScore11(float score11)
    {
        this.score11 = score11;
    }

    public float getScore12()
    {
        return score12;
    }

    public void setScore12(float score12)
    {
        this.score12 = score12;
    }

    public float getScore13()
    {
        return score13;
    }

    public void setScore13(float score13)
    {
        this.score13 = score13;
    }

    public float getScore14()
    {
        return score14;
    }

    public void setScore14(float score14)
    {
        this.score14 = score14;
    }

    public float getScore15()
    {
        return score15;
    }

    public void setScore15(float score15)
    {
        this.score15 = score15;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Date getAiScoreDate()
    {
        return aiScoreDate;
    }

    public void setAiScoreDate(Date aiScoreDate)
    {
        this.aiScoreDate = aiScoreDate;
    }

    public Date getAiSummaryDate()
    {
        return aiSummaryDate;
    }

    public void setAiSummaryDate(Date aiSummaryDate)
    {
        this.aiSummaryDate = aiSummaryDate;
    }

    public int getAiScoresStatusTypeId()
    {
        return aiScoresStatusTypeId;
    }

    public void setAiScoresStatusTypeId(int aiScoresStatusTypeId)
    {
        this.aiScoresStatusTypeId = aiScoresStatusTypeId;
    }

    public int getAiSummaryStatusTypeId()
    {
        return aiSummaryStatusTypeId;
    }

    public void setAiSummaryStatusTypeId(int aiSummaryStatusTypeId)
    {
        this.aiSummaryStatusTypeId = aiSummaryStatusTypeId;
    }

    
}
