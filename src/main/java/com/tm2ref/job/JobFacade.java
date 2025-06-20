package com.tm2ref.job;


import com.tm2ref.entity.job.Job;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;

import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;


import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

//// @ManagedBean
//@Named
@Stateless
public class JobFacade
{
    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;


    public static JobFacade getInstance()
    {
        try
        {
            return (JobFacade) InitialContext.doLookup( "java:module/JobFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JobFacade.getInstance() " );
            return null;
        }
    }

    
    public Job getJob( int jobId ) throws Exception
    {
        try
        {
            return (Job) emmirror.createNamedQuery( "Job.findByJobId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("jobId", jobId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "JobFacade.getJob( " + jobId + " ) " );
            throw new STException( e );
        }
    }
    

}
