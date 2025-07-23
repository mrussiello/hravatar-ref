package com.tm2ref.entity.job;

import com.tm2ref.ai.AiMetaScoreType;
import com.tm2ref.job.EvalPlanStatusType;
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
import java.util.HashSet;
import java.util.Set;


@Entity
@Table( name = "evalplan" )
@NamedQueries( {
        @NamedQuery( name = "EvalPlan.findByEvalPlanId", query = "SELECT o FROM EvalPlan AS o WHERE o.evalPlanId=:evalPlanId" )
} )
public class EvalPlan implements Serializable, Comparable<EvalPlan>
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "evalPlanid" )
    private int evalPlanId;

    @Column( name = "evalPlantypeid" )
    private int evalPlanTypeId = -1;

    @Column( name = "evalPlanstatustypeid" )
    private int evalPlanStatusTypeId = EvalPlanStatusType.INACTIVE.getEvalPlanStatusTypeId();

    @Column( name = "name" )
    private String name;

    @Column( name = "description" )
    private String description;

    @Column( name = "localestr" )
    private String localeStr;
                
    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "suborgid" )
    private int suborgId;
    
    @Column( name = "userid" )
    private long userId;

    @Column( name = "jobdescripid" )
    private int jobDescripId;

    @Column( name = "testproductid" )
    private int testProductId;

    @Column( name = "rcscriptid" )
    private int rcScriptId;
    
    /*
      Max Wait Days - for evaluation when only partial data is available.
    */
    @Column( name = "maxwaitdays" )
    private int maxWaitDays = 7;
    
    @Column( name = "intparam1" )
    private int intParam1;

    @Column( name = "intparam2" )
    private int intParam2;

    @Column( name = "floatparam1" )
    private float floatParam1;

    @Column( name = "floatparam2" )
    private float floatParam2;
    
    
    @Column( name = "aimetascoretypeids" )
    private String aiMetaScoreTypeIds;
        
    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "createdate" )
    private Date createDate;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "lastupdate" )
    private Date lastUpdate;

    @Transient
    private Set<Integer> aiMetaScoreTypeIdSet;

    
    @Override
    public int compareTo(EvalPlan o)
    {
        if( name!=null && o.getName()!=null )
            return name.compareTo(o.getName());
        
        return Integer.valueOf( this.evalPlanId).compareTo(evalPlanId);
    }

    

    public synchronized Set getAiMetaScoreTypeIdSet()
    {
        if( aiMetaScoreTypeIdSet==null )
        {
            Set<Integer> ms =new HashSet<>();
            ms.addAll( StringUtils.getIntegerList(aiMetaScoreTypeIds) );            
            aiMetaScoreTypeIdSet = ms;
        }
        return aiMetaScoreTypeIdSet;
    }
    public synchronized void packAiMetaScoreTypeIdSet()
    {
        getAiMetaScoreTypeIdSet();
        StringBuilder sb = new StringBuilder();
        for( Integer tid : aiMetaScoreTypeIdSet )
        {
            if( !sb.isEmpty() )
                sb.append(",");
            sb.append(tid);
        }
        this.aiMetaScoreTypeIds=sb.toString();
    }
    
    public boolean getIncludeOrgTraitsMetaScore()
    {
        getAiMetaScoreTypeIdSet();
        return this.aiMetaScoreTypeIdSet.contains(AiMetaScoreType.ORGTRAITS.getAiMetaScoreTypeId() );
    }
    public void setIncludeOrgTraitsMetaScore( boolean b)
    {
        getAiMetaScoreTypeIdSet();
        if( b )
            aiMetaScoreTypeIdSet.add(AiMetaScoreType.ORGTRAITS.getAiMetaScoreTypeId() );
        else
            aiMetaScoreTypeIdSet.remove( AiMetaScoreType.ORGTRAITS.getAiMetaScoreTypeId() );
    }

    public boolean getIncludeOrgCompsMetaScore()
    {
        getAiMetaScoreTypeIdSet();
        return this.aiMetaScoreTypeIdSet.contains(AiMetaScoreType.ORGCOMPS.getAiMetaScoreTypeId() );
    }
    public void setIncludeOrgCompsMetaScore( boolean b)
    {
        getAiMetaScoreTypeIdSet();
        if( b )
            aiMetaScoreTypeIdSet.add(AiMetaScoreType.ORGCOMPS.getAiMetaScoreTypeId() );
        else
            aiMetaScoreTypeIdSet.remove( AiMetaScoreType.ORGCOMPS.getAiMetaScoreTypeId() );
    }
    
    public boolean getIncludeJobDescripMetaScore()
    {
        getAiMetaScoreTypeIdSet();
        return this.aiMetaScoreTypeIdSet.contains(AiMetaScoreType.JOBDESCRIP.getAiMetaScoreTypeId() );
    }
    public void setIncludeJobDescripMetaScore( boolean b)
    {
        getAiMetaScoreTypeIdSet();
        if( b )
            aiMetaScoreTypeIdSet.add(AiMetaScoreType.JOBDESCRIP.getAiMetaScoreTypeId() );
        else
            aiMetaScoreTypeIdSet.remove( AiMetaScoreType.JOBDESCRIP.getAiMetaScoreTypeId() );
    }
    

    public int getEvalPlanId()
    {
        return evalPlanId;
    }

    public void setEvalPlanId(int evalPlanId)
    {
        this.evalPlanId = evalPlanId;
    }

    public int getEvalPlanTypeId()
    {
        return evalPlanTypeId;
    }

    public void setEvalPlanTypeId(int evalPlanTypeId)
    {
        this.evalPlanTypeId = evalPlanTypeId;
    }

    public int getEvalPlanStatusTypeId()
    {
        return evalPlanStatusTypeId;
    }

    public void setEvalPlanStatusTypeId(int evalPlanStatusTypeId)
    {
        this.evalPlanStatusTypeId = evalPlanStatusTypeId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getLocaleStr()
    {
        return localeStr;
    }

    public void setLocaleStr(String localeStr)
    {
        this.localeStr = localeStr;
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

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public int getJobDescripId()
    {
        return jobDescripId;
    }

    public void setJobDescripId(int jobDescripId)
    {
        this.jobDescripId = jobDescripId;
    }

    public int getTestProductId()
    {
        return testProductId;
    }

    public void setTestProductId(int testProductId)
    {
        this.testProductId = testProductId;
    }

    public int getRcScriptId()
    {
        return rcScriptId;
    }

    public void setRcScriptId(int rcScriptId)
    {
        this.rcScriptId = rcScriptId;
    }

    public int getMaxWaitDays()
    {
        return maxWaitDays;
    }

    public void setMaxWaitDays(int maxWaitDays)
    {
        this.maxWaitDays = maxWaitDays;
    }

    public int getIntParam1()
    {
        return intParam1;
    }

    public void setIntParam1(int intParam1)
    {
        this.intParam1 = intParam1;
    }

    public int getIntParam2()
    {
        return intParam2;
    }

    public void setIntParam2(int intParam2)
    {
        this.intParam2 = intParam2;
    }

    public float getFloatParam1()
    {
        return floatParam1;
    }

    public void setFloatParam1(float floatParam1)
    {
        this.floatParam1 = floatParam1;
    }

    public float getFloatParam2()
    {
        return floatParam2;
    }

    public void setFloatParam2(float floatParam2)
    {
        this.floatParam2 = floatParam2;
    }

    public String getAiMetaScoreTypeIds()
    {
        return aiMetaScoreTypeIds;
    }

    public void setAiMetaScoreTypeIds(String aiMetaScoreTypeIds)
    {
        this.aiMetaScoreTypeIds = aiMetaScoreTypeIds;
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
    

}
