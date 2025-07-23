package com.tm2ref.job;


import com.tm2ref.entity.job.EvalPlan;
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
public class EvalPlanFacade
{
    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;


    public static EvalPlanFacade getInstance()
    {
        try
        {
            return (EvalPlanFacade) InitialContext.doLookup( "java:module/EvalPlanFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EvalPlanFacade.getInstance() " );
            return null;
        }
    }

    
    public EvalPlan getEvalPlan( int evalPlanId ) throws Exception
    {
        try
        {
            return (EvalPlan) emmirror.createNamedQuery( "EvalPlan.findByEvalPlanId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("evalPlanId", evalPlanId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EvalPlanFacade.getEvalPlan( " + evalPlanId + " ) " );
            throw new STException( e );
        }
    }
    

}
