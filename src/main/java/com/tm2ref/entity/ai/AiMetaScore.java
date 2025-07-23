package com.tm2ref.entity.ai;

import com.tm2ref.ai.AiMetaScoreStatusType;
import com.tm2ref.ai.AiMetaScoreType;
import com.tm2ref.util.StringUtils;
import java.io.Serializable;
import jakarta.persistence.Cacheable;

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
import java.util.Date;
import java.util.Locale;

@Cacheable(false)
@Entity
@Table(name = "aimetascore")
@NamedQueries(
{
        @NamedQuery( name = "AiMetaScore.findReportableByRcCheckId", query = "SELECT o FROM AiMetaScore AS o WHERE o.rcCheckId=:rcCheckId AND o.aiMetaScoreStatusTypeId>=110 AND o.aiMetaScoreStatusTypeId<=120" )
})
public class AiMetaScore implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "aimetascoreid" )
    private long aiMetaScoreId;

    @Column( name = "aimetascorestatustypeid" )
    private int aiMetaScoreStatusTypeId;
    
    @Column( name = "aiMetascoretypeid" )
    private int aiMetaScoreTypeId;

    @Column( name = "testkeyid" )
    private long testKeyId;

    @Column( name = "rccheckid" )
    private long rcCheckId;

    @Column( name = "lvcallid" )
    private long lvCallId;

    @Column( name = "userid" )
    private long userId;

    @Column( name = "evalplanid" )
    private int evalPlanId;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "suborgid" )
    private int suborgId;

    @Column( name = "score" )
    private float score;

    @Column( name = "confidence" )
    private float confidence;

    @Column( name = "scoretext" )
    private String scoreText;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="scorecompletedate")
    private Date scoreCompleteDate;

    
    @Transient
    Locale locale;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (int) (this.aiMetaScoreId ^ (this.aiMetaScoreId >>> 32));
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
        final AiMetaScore other = (AiMetaScore) obj;
        return this.aiMetaScoreId == other.aiMetaScoreId;
    }
    
    public String getScoreTextXhtml()
    {
        return StringUtils.replaceStandardEntities(scoreText);
    }

    public AiMetaScoreType getAiMetaScoreType()
    {
        return AiMetaScoreType.getValue(aiMetaScoreTypeId);
    }
    
    public AiMetaScoreStatusType getAiMetaScoreStatusType()
    {
        return AiMetaScoreStatusType.getValue(this.aiMetaScoreStatusTypeId);
    }
    
    public long getAiMetaScoreId()
    {
        return aiMetaScoreId;
    }

    public void setAiMetaScoreId(long aiMetaScoreId)
    {
        this.aiMetaScoreId = aiMetaScoreId;
    }

    public int getAiMetaScoreStatusTypeId()
    {
        return aiMetaScoreStatusTypeId;
    }

    public void setAiMetaScoreStatusTypeId(int aiMetaScoreStatusTypeId)
    {
        this.aiMetaScoreStatusTypeId = aiMetaScoreStatusTypeId;
    }

    public int getAiMetaScoreTypeId()
    {
        return aiMetaScoreTypeId;
    }

    public void setAiMetaScoreTypeId(int aiMetaScoreTypeId)
    {
        this.aiMetaScoreTypeId = aiMetaScoreTypeId;
    }

    public long getTestKeyId()
    {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId)
    {
        this.testKeyId = testKeyId;
    }

    public long getRcCheckId()
    {
        return rcCheckId;
    }

    public void setRcCheckId(long rcCheckId)
    {
        this.rcCheckId = rcCheckId;
    }

    public long getLvCallId()
    {
        return lvCallId;
    }

    public void setLvCallId(long lvCallId)
    {
        this.lvCallId = lvCallId;
    }

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public int getEvalPlanId()
    {
        return evalPlanId;
    }

    public void setEvalPlanId(int evalPlanId)
    {
        this.evalPlanId = evalPlanId;
    }

    public int getOrgId()
    {
        return orgId;
    }

    public void setOrgId(int orgId)
    {
        this.orgId = orgId;
    }

    public int getSuborgId()
    {
        return suborgId;
    }

    public void setSuborgId(int suborgId)
    {
        this.suborgId = suborgId;
    }

    public float getScore()
    {
        return score;
    }

    public void setScore(float score)
    {
        this.score = score;
    }

    public float getConfidence()
    {
        return confidence;
    }

    public void setConfidence(float confidence)
    {
        this.confidence = confidence;
    }

    public String getScoreText()
    {
        return scoreText;
    }

    public void setScoreText(String scoreText)
    {
        this.scoreText = scoreText;
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

    public Date getScoreCompleteDate()
    {
        return scoreCompleteDate;
    }

    public void setScoreCompleteDate(Date scoreCompleteDate)
    {
        this.scoreCompleteDate = scoreCompleteDate;
    }

    public Locale getLocale()
    {
        return locale;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }



}
