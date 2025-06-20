package com.tm2ref.entity.job;


import com.tm2ref.ai.MetaScoreType;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table( name = "job" )
@NamedQueries( {
        @NamedQuery( name = "Job.findByJobId", query = "SELECT o FROM Job AS o WHERE o.jobId=:jobId" )
} )
public class Job implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "jobid" )
    private int jobId;

    @Column( name = "name" )
    private String name;

    @Column( name = "orgid" )
    private int orgId;
    
    @Column( name = "suborgid" )
    private int suborgId;

    @Column( name = "jobdescripid" )
    private int jobDescripId;

    @Column( name = "testproductid" )
    private int testProductId;

    @Column( name = "rcscriptid" )
    private int rcScriptId;

    @Column( name = "localestr" )
    private String localeStr;
            
    @Column( name = "metascoretypeids" )
    private String metaScoreTypeIds;

    
    @Transient
    private Set<Integer> metaScoreTypeIdSet;


    public synchronized Set getMetaScoreTypeIdSet()
    {        
        if( metaScoreTypeIdSet==null )
        {
            LogService.logIt( "Job.getMetaScoreTypeIdSet() Creating metaScoreTypeIdSet. metaScoreTypeIds=" + metaScoreTypeIds);
            Set<Integer> ms =new HashSet<>();
            ms.addAll( StringUtils.getIntegerList( metaScoreTypeIds) );            
            metaScoreTypeIdSet = ms;
        }
        // LogService.logIt( "Job.getMetaScoreTypeIdSet() size=" + metaScoreTypeIdSet.size() );
        return metaScoreTypeIdSet;
    }


    @Override
    public String toString()
    {
        return "Job  jobId=" + jobId + ", name=" + name;
    }
    
    public boolean getHasAnyMetaScores()
    {
        getMetaScoreTypeIdSet();
        return !metaScoreTypeIdSet.isEmpty();
    }
    
    public boolean getIncludeOrgTraitsMetaScore()
    {
        if( metaScoreTypeIdSet==null )
            getMetaScoreTypeIdSet();        
        return metaScoreTypeIdSet.contains(MetaScoreType.ORGTRAITS.getMetaScoreTypeId() );
    }
    public boolean getIncludeOrgCompsMetaScore()
    {
        if( metaScoreTypeIdSet==null )
            getMetaScoreTypeIdSet();
        return this.metaScoreTypeIdSet.contains(MetaScoreType.ORGCOMPS.getMetaScoreTypeId() );
    }    
    public boolean getIncludeJobDescripMetaScore()
    {
        if( metaScoreTypeIdSet==null )
            getMetaScoreTypeIdSet();
        return this.metaScoreTypeIdSet.contains(MetaScoreType.JOBDESCRIP.getMetaScoreTypeId() );
    }
    
    public int getJobId()
    {
        return jobId;
    }

    public void setJobId( int jobId )
    {
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(int suborgId) {
        this.suborgId = suborgId;
    }

    public int getJobDescripId() {
        return jobDescripId;
    }

    public void setJobDescripId(int jobDescripId) {
        this.jobDescripId = jobDescripId;
    }

    public int getTestProductId() {
        return testProductId;
    }

    public void setTestProductId(int testProductId) {
        this.testProductId = testProductId;
    }

    public int getRcScriptId() {
        return rcScriptId;
    }

    public void setRcScriptId(int rcScriptId) {
        this.rcScriptId = rcScriptId;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }

    public String getMetaScoreTypeIds() {
        return metaScoreTypeIds;
    }

    public void setMetaScoreTypeIds(String metaScoreTypeIds) {
        this.metaScoreTypeIds = metaScoreTypeIds;
    }


}
