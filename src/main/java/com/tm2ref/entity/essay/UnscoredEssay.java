/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.entity.essay;

import com.tm2ref.essay.EssayScoreStatusType;
import com.tm2ref.essay.UnscoredEssayType;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Basic;
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
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mike
 */
@Entity
@Table( name = "unscoredessay" )
@XmlRootElement
@NamedQueries({
    @NamedQuery ( name="UnscoredEssay.findByUnscoredEssayId", query="SELECT o FROM UnscoredEssay AS o WHERE o.unscoredEssayId=:unscoredEssayId" ),
    @NamedQuery ( name="UnscoredEssay.findByRcCheckId", query="SELECT o FROM UnscoredEssay AS o WHERE o.rcCheckId=:rcCheckId" ),
    @NamedQuery ( name="UnscoredEssay.findByRcCheckIdAndRatingIdAndTypeId", query="SELECT o FROM UnscoredEssay AS o WHERE o.rcCheckId=:rcCheckId AND o.nodeSequenceId=:nodeSequenceId AND o.subnodeSequenceId=:subnodeSequenceId" )
})
public class UnscoredEssay implements Serializable, Comparable<UnscoredEssay>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="unscoredessayid")
    private int unscoredEssayId;

    @Column(name="unscoredessaytypeid")
    private int unscoredEssayTypeId;

    @Column(name="rccheckid")
    private long rcCheckId;

    @Column(name="scorestatustypeid")
    private int scoreStatusTypeId;

    @Column(name="essaypromptid")
    private int essayPromptId;

    @Column(name="localestr")
    private String localeStr;

    @Column(name="nodesequenceid")
    private long nodeSequenceId;

    @Column(name="subnodesequenceid")
    private int subnodeSequenceId;

    @Column(name="ct5itemid")
    private int ct5ItemId;

    @Column(name="ct5itempartid")
    private int ct5ItemPartId;

    @Column(name="userid")
    private long userId;

    @Column(name="computedscore")
    private float computedScore;

    @Column(name="computedconfidence")
    private float computedConfidence;

    @Column(name="totalwords")
    private int totalWords = -1;

    @Column(name="secondstocompose")
    private int secondsToCompose;

    @Column(name="pctduplicatewords")
    private float pctDuplicateWords;

    @Column(name="pctduplicatelongwords")
    private float pctDuplicateLongWords;

    @Column(name="metascore1")
    private float metaScore1;

    @Column(name="metascoretypeid1")
    private int metaScoreTypeId1;

    @Column(name="metascore2")
    private float metaScore2;

    @Column(name="metascoretypeid2")
    private int metaScoreTypeId2;

    @Column(name="metascore3")
    private float metaScore3;

    @Column(name="metascoretypeid3")
    private int metaScoreTypeId3;

    @Column(name="metascore4")
    private float metaScore4;

    @Column(name="metascoretypeid4")
    private int metaScoreTypeId4;

    @Column(name="metascore5")
    private float metaScore5;

    @Column(name="metascoretypeid5")
    private int metaScoreTypeId5;

    @Column(name="essay")
    private String essay;

    @Column(name="summary")
    private String summary;

    @Column(name="usernote")
    private String userNote;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="scoredate")
    private Date scoreDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="summarydate")
    private Date summaryDate;


    @Override
    public int compareTo(UnscoredEssay o) {
        return createDate.compareTo( o.getCreateDate() );
    }

    @Override
    public String toString() {
        return "UnscoredEssay{" + "unscoredEssayId=" + unscoredEssayId + ", scoreStatusTypeId=" + scoreStatusTypeId + ", rcCheckId=" + rcCheckId + ", nodeSequenceId (rcRatingId)=" + nodeSequenceId + ", subnodeSequenceId=" + subnodeSequenceId + ", computedScore=" + computedScore + ", computedConfidence=" + computedConfidence + ", secondsToCompose=" + this.secondsToCompose + ", essay=" + essay + '}';
    }

    public UnscoredEssayType getUnscoredEssayType()
    {
        return UnscoredEssayType.getValue( this.unscoredEssayTypeId );
    }


    public EssayScoreStatusType getEssayScoreStatusType()
    {
        return EssayScoreStatusType.getValue( scoreStatusTypeId );
    }


    public Map<Integer,Float> getMetaScoreMap()
    {
        Map<Integer,Float> out = new HashMap<>();

        if( this.metaScoreTypeId1>0 )
            out.put( this.metaScoreTypeId1, metaScore1);
        if( this.metaScoreTypeId2>0 )
            out.put( this.metaScoreTypeId2, metaScore2);
        if( this.metaScoreTypeId3>0 )
            out.put( this.metaScoreTypeId3, metaScore3);
        if( this.metaScoreTypeId4>0 )
            out.put( this.metaScoreTypeId4, metaScore4);
        if( this.metaScoreTypeId5>0 )
            out.put( this.metaScoreTypeId5, metaScore5);

        return out;
    }



    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getUnscoredEssayId() {
        return unscoredEssayId;
    }

    public void setUnscoredEssayId(int unscoredEssayId) {
        this.unscoredEssayId = unscoredEssayId;
    }

    public int getUnscoredEssayTypeId()
    {
        return unscoredEssayTypeId;
    }

    public void setUnscoredEssayTypeId(int unscoredEssayTypeId)
    {
        this.unscoredEssayTypeId = unscoredEssayTypeId;
    }

    public long getRcCheckId()
    {
        return rcCheckId;
    }

    public void setRcCheckId(long rcCheckId)
    {
        this.rcCheckId = rcCheckId;
    }

    public int getScoreStatusTypeId()
    {
        return scoreStatusTypeId;
    }

    public void setScoreStatusTypeId(int scoreStatusTypeId)
    {
        this.scoreStatusTypeId = scoreStatusTypeId;
    }

    public String getLocaleStr()
    {
        return localeStr;
    }

    public void setLocaleStr(String localeStr)
    {
        this.localeStr = localeStr;
    }

    public long getNodeSequenceId()
    {
        return nodeSequenceId;
    }

    public void setNodeSequenceId(long nodeSequenceId)
    {
        this.nodeSequenceId = nodeSequenceId;
    }

    public int getSubnodeSequenceId()
    {
        return subnodeSequenceId;
    }

    public void setSubnodeSequenceId(int subnodeSequenceId)
    {
        this.subnodeSequenceId = subnodeSequenceId;
    }

    public int getCt5ItemId()
    {
        return ct5ItemId;
    }

    public void setCt5ItemId(int ct5ItemId)
    {
        this.ct5ItemId = ct5ItemId;
    }

    public int getCt5ItemPartId()
    {
        return ct5ItemPartId;
    }

    public void setCt5ItemPartId(int ct5ItemPartId)
    {
        this.ct5ItemPartId = ct5ItemPartId;
    }

    public float getComputedScore()
    {
        return computedScore;
    }

    public void setComputedScore(float computedScore)
    {
        this.computedScore = computedScore;
    }

    public float getComputedConfidence()
    {
        return computedConfidence;
    }

    public void setComputedConfidence(float computedConfidence)
    {
        this.computedConfidence = computedConfidence;
    }

    public int getTotalWords()
    {
        return totalWords;
    }

    public void setTotalWords(int totalWords)
    {
        this.totalWords = totalWords;
    }

    public int getSecondsToCompose()
    {
        return secondsToCompose;
    }

    public void setSecondsToCompose(int secondsToCompose)
    {
        this.secondsToCompose = secondsToCompose;
    }

    public float getPctDuplicateWords()
    {
        return pctDuplicateWords;
    }

    public void setPctDuplicateWords(float pctDuplicateWords)
    {
        this.pctDuplicateWords = pctDuplicateWords;
    }

    public float getPctDuplicateLongWords()
    {
        return pctDuplicateLongWords;
    }

    public void setPctDuplicateLongWords(float pctDuplicateLongWords)
    {
        this.pctDuplicateLongWords = pctDuplicateLongWords;
    }

    public float getMetaScore1()
    {
        return metaScore1;
    }

    public void setMetaScore1(float metaScore1)
    {
        this.metaScore1 = metaScore1;
    }

    public int getMetaScoreTypeId1()
    {
        return metaScoreTypeId1;
    }

    public void setMetaScoreTypeId1(int metaScoreTypeId1)
    {
        this.metaScoreTypeId1 = metaScoreTypeId1;
    }

    public float getMetaScore2()
    {
        return metaScore2;
    }

    public void setMetaScore2(float metaScore2)
    {
        this.metaScore2 = metaScore2;
    }

    public int getMetaScoreTypeId2()
    {
        return metaScoreTypeId2;
    }

    public void setMetaScoreTypeId2(int metaScoreTypeId2)
    {
        this.metaScoreTypeId2 = metaScoreTypeId2;
    }

    public float getMetaScore3()
    {
        return metaScore3;
    }

    public void setMetaScore3(float metaScore3)
    {
        this.metaScore3 = metaScore3;
    }

    public int getMetaScoreTypeId3()
    {
        return metaScoreTypeId3;
    }

    public void setMetaScoreTypeId3(int metaScoreTypeId3)
    {
        this.metaScoreTypeId3 = metaScoreTypeId3;
    }

    public float getMetaScore4()
    {
        return metaScore4;
    }

    public void setMetaScore4(float metaScore4)
    {
        this.metaScore4 = metaScore4;
    }

    public int getMetaScoreTypeId4()
    {
        return metaScoreTypeId4;
    }

    public void setMetaScoreTypeId4(int metaScoreTypeId4)
    {
        this.metaScoreTypeId4 = metaScoreTypeId4;
    }

    public float getMetaScore5()
    {
        return metaScore5;
    }

    public void setMetaScore5(float metaScore5)
    {
        this.metaScore5 = metaScore5;
    }

    public int getMetaScoreTypeId5()
    {
        return metaScoreTypeId5;
    }

    public void setMetaScoreTypeId5(int metaScoreTypeId5)
    {
        this.metaScoreTypeId5 = metaScoreTypeId5;
    }

    public String getEssay()
    {
        return essay;
    }

    public void setEssay(String essay)
    {
        this.essay = essay;
    }

    public String getUserNote()
    {
        return userNote;
    }

    public void setUserNote(String userNote)
    {
        this.userNote = userNote;
    }

    public Date getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate(Date createDate)
    {
        this.createDate = createDate;
    }

    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }

    public Date getScoreDate()
    {
        return scoreDate;
    }

    public void setScoreDate(Date scoreDate)
    {
        this.scoreDate = scoreDate;
    }

    public int getEssayPromptId()
    {
        return essayPromptId;
    }

    public void setEssayPromptId(int essayPromptId)
    {
        this.essayPromptId = essayPromptId;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Date getSummaryDate()
    {
        return summaryDate;
    }

    public void setSummaryDate(Date summaryDate)
    {
        this.summaryDate = summaryDate;
    }

}
