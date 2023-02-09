package com.tm2ref.corp;

import com.tm2ref.entity.corp.Corp;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.util.List;

import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;


import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceContext;



@Stateless
//@PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class CorpFacade
{
    @PersistenceContext
    EntityManager em;


    public static CorpFacade getInstance()
    {
        try
        {
            return (CorpFacade) InitialContext.doLookup( "java:module/CorpFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }


    public CorpFacade()
    {}

    public CorpFacade( UserTransaction utx )
    {
    }


    public Corp getCorp( int corpId, boolean refresh ) throws Exception
    {
        try
        {
            Corp c = null;

            if( refresh || 1==1 )
            {
                // done this way to avoid cache
                c = (Corp) em.createNamedQuery( "Corp.findByCorpId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "corpId", corpId ).getSingleResult();
            }

            else
                c = em.find( Corp.class, corpId );

            // ClassLoader cl = ;

            // LogService.logIt( "Classloader 1=" + com.tm2test.entity.corp.Corp.class.getClassLoader().toString()  + " CL2=" + c.getClass().getClassLoader().toString() );

            return c;
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CorpFacade.getCorp( " + corpId + ", " + refresh + " ) " );
            throw new STException( e );
        }
    }


    public Corp findCorpIdByDomain( String domain ) throws Exception
    {
        if( domain == null || domain.isEmpty() )
            return null;

        try
        {
            Query q = em.createNamedQuery( "Corp.findByBaseDomain" );

            q.setParameter( "domain", domain );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            List<Corp> corpList = q.getResultList();

            return corpList.size()>0 ? corpList.get( 0 ) : null;
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "findCorpIdByDomain() domain=" + domain );
            throw new STException( e );
        }

    }

}
