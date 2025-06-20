package com.tm2ref.entity.ai;

import com.tm2ref.ai.MetaScoreInputType;
import com.tm2ref.ai.MetaScoreStatusType;
import com.tm2ref.ai.MetaScoreType;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Cacheable(false)
@Entity
@Table(name = "metascore")
@NamedQueries(
{
        @NamedQuery( name = "MetaScore.findReportableByRcCheckId", query = "SELECT o FROM MetaScore AS o WHERE o.rcCheckId=:rcCheckId AND o.metaScoreStatusTypeId>=110 AND o.metaScoreStatusTypeId<=120" )
})
public class MetaScore implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metascoreid")
    private long metaScoreId;

    @Column( name = "metascorestatustypeid" )
    private int metaScoreStatusTypeId;
    
    @Column(name = "metascoretypeid")
    private int metaScoreTypeId;

    @Column(name = "testkeyid")
    private long testKeyId;

    @Column(name = "rccheckid")
    private long rcCheckId;

    @Column(name = "userid")
    private long userId;

    @Column(name = "jobid")
    private int jobId;

    @Column(name = "orgid")
    private int orgId;

    @Column(name = "score")
    private float score;

    @Column(name = "confidence")
    private float confidence;

    @Column(name = "scoretext")
    private String scoreText;

    @Column(name = "metascoreinputtypeids")
    private String metaScoreInputTypeIds;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastupdate")
    private Date lastUpdate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="scorecompletedate")
    private Date scoreCompleteDate;

    
    @Transient
    Locale locale;

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + (int) (this.metaScoreId ^ (this.metaScoreId >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final MetaScore other = (MetaScore) obj;
        return this.metaScoreId == other.metaScoreId;
    }

    public MetaScoreStatusType getMetaScoreStatusType()
    {
        return MetaScoreStatusType.getValue(metaScoreStatusTypeId);
    }

    
    public List<Integer> getMetaScoreInputTypeIdList()
    {
        return StringUtils.getIntegerList(this.metaScoreInputTypeIds);
    }

    public List<MetaScoreInputType> getMetaScoreInputTypeList()
    {
        List<MetaScoreInputType> out = new ArrayList<>();
        for (Integer tp : StringUtils.getIntegerList(this.metaScoreInputTypeIds))
        {
            if (tp == null || tp == 0)
                continue;
            out.add(MetaScoreInputType.getValue(tp));
        }
        return out;
    }

    public String getMetaScoreInputTypesStr()
    {
        if (locale == null)
            locale = Locale.US;

        StringBuilder sb = new StringBuilder();
        for (MetaScoreInputType t : this.getMetaScoreInputTypeList())
        {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(t.getName(locale));
        }
        return sb.toString();
    }

    public MetaScoreType getMetaScoreType()
    {
        return MetaScoreType.getValue(metaScoreTypeId);
    }

    public int getMetaScoreTypeId()
    {
        return metaScoreTypeId;
    }

    public void setMetaScoreTypeId(int metaScoreTypeId)
    {
        this.metaScoreTypeId = metaScoreTypeId;
    }

    public float getScore()
    {
        return score;
    }

    public void setScore(float score)
    {
        this.score = score;
    }

    public String getScoreText()
    {
        return scoreText;
    }

    public String getScoreTextXhtml()
    {
        return StringUtils.replaceStandardEntities(scoreText);
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

    public long getMetaScoreId()
    {
        return metaScoreId;
    }

    public void setMetaScoreId(long metaScoreId)
    {
        this.metaScoreId = metaScoreId;
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

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public int getJobId()
    {
        return jobId;
    }

    public void setJobId(int jobId)
    {
        this.jobId = jobId;
    }

    public int getOrgId()
    {
        return orgId;
    }

    public void setOrgId(int orgId)
    {
        this.orgId = orgId;
    }

    public float getConfidence()
    {
        return confidence;
    }

    public void setConfidence(float confidence)
    {
        this.confidence = confidence;
    }

    public String getMetaScoreInputTypeIds()
    {
        return metaScoreInputTypeIds;
    }

    public void setMetaScoreInputTypeIds(String metaScoreInputTypeIds)
    {
        this.metaScoreInputTypeIds = metaScoreInputTypeIds;
    }

    public Locale getLocale()
    {
        return locale;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }
    public int getMetaScoreStatusTypeId()
    {
        return metaScoreStatusTypeId;
    }

    public void setMetaScoreStatusTypeId(int metaScoreStatusTypeId)
    {
        this.metaScoreStatusTypeId = metaScoreStatusTypeId;
    }

    public Date getScoreCompleteDate()
    {
        return scoreCompleteDate;
    }

    public void setScoreCompleteDate(Date scoreCompleteDate)
    {
        this.scoreCompleteDate = scoreCompleteDate;
    }


}
