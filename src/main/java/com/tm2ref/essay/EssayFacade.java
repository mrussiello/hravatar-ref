/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.essay;

import com.tm2ref.entity.essay.UnscoredEssay;
import com.tm2ref.global.Constants;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;


/**
 *
 * @author Mike
 */
@Stateless
public class EssayFacade
{
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;
    
    public static EssayFacade getInstance()
    {
        try
        {
            return (EssayFacade) InitialContext.doLookup( "java:module/EssayFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EssayFacade.getInstance() " );

            return null;
        }
    }

    
    public UnscoredEssay getUnscoredEssay( long rcCheckId, long rcRatingId, int unscoredEssayTypeId ) throws Exception
    {
        try
        {
            // if( factory == null )
            //     factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = factory.createEntityManager();

            Query q = em.createNamedQuery( "UnscoredEssay.findByRcCheckIdAndRatingIdAndTypeId" );

            q.setParameter("rcCheckId", rcCheckId );
            q.setParameter("nodeSequenceId", rcRatingId );
            q.setParameter("subnodeSequenceId", unscoredEssayTypeId );
            
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            List<UnscoredEssay> uel = q.getResultList();

            Collections.sort( uel );

            return uel.isEmpty() ? null : uel.get( uel.size()-1 );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "EssayFacade.getUnscoredEssay( rcCheckId=" + rcCheckId + ", nodeSeq (rcRatingId)=" + rcRatingId + ", subnodeSeq (unscoredEssayTypeId)=" + unscoredEssayTypeId + " ) " );
            throw new STException( e );
        }
    }
    
    public UnscoredEssay getUnscoredEssay( int unscoredEssayId ) throws Exception
    {
        try
        {
            //if( factory == null )
            //    factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = factory.createEntityManager();
            
            return (UnscoredEssay) em.createNamedQuery( "UnscoredEssay.findByUnscoredEssayId", UnscoredEssay.class ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "unscoredEssayId", unscoredEssayId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EssayFacade.getUnscoredEssay( " + unscoredEssayId + " )" );

            throw new STException( e );
        }
    }
    


    public UnscoredEssay saveUnscoredEssay( UnscoredEssay r) throws Exception
    {
        try
        {
            if( r.getEssayPromptId()<=0 )
                r.setEssayPromptId( Constants.DUMMY_ESSAY_PROMPT_ID);

            if( r.getEssay()==null || r.getEssay().length()== 0 )
                throw new Exception( "UnscoredEssay.essay is required" );

            if( r.getSecondsToCompose()<0 )
                r.setSecondsToCompose(0);
            else if( r.getSecondsToCompose()> 1000000 )
                r.setSecondsToCompose( 1000000 );
                        
            if( r.getRcCheckId()<=0 )
                throw new Exception( "UnscoredEssay.rcCheckId invalid " + r.getRcCheckId() );

            if( r.getNodeSequenceId()<=0 )
                throw new Exception( "UnscoredEssay.getNodeSequenceId (rcRatingId) invalid " + r.getNodeSequenceId() );

            if( r.getSubnodeSequenceId()<=0 )
                throw new Exception( "UnscoredEssay.getSubnodeSequenceId (UnscoredEssayTypeId - for Ref Checks should be 30 or 40) invalid " + r.getSubnodeSequenceId() );

            r.setLastUpdate( new Date() );

            if( r.getCreateDate() == null )
                r.setCreateDate( new Date() );

            // utx.begin();
            if( r.getUnscoredEssayId() > 0 )
            {
                em.merge( r );
            }

            else
            {
                em.detach( r );
                em.persist( r );
            }
        }

        catch( Exception e )
        {
            LogService.logIt(e, "EssayFacade.saveUnscoredEssay() " + r.toString() );
            // if( utx.isActive() )
                // utx.rollback();
            throw new STException( e );
        }

        return r;
    }
}
