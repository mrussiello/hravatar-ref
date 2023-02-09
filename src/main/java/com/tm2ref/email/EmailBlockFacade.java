/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.email;

import com.tm2ref.entity.email.EmailBlock;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
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
public class EmailBlockFacade
{
    @PersistenceContext
    EntityManager em;

    

    public static EmailBlockFacade getInstance()
    {
        try
        {
            return (EmailBlockFacade) InitialContext.doLookup( "java:module/EmailBlockFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EmailBlockFacade.getInstance() " );

            return null;
        }
    }



    public boolean hasEmailBlock( String email, boolean fullBlock, boolean treatBouncesComplaintsAsFullBlock) throws Exception
    {
        try
        {
            if( email==null || email.trim().isEmpty() )
                return false;
            
            if( treatBouncesComplaintsAsFullBlock && !fullBlock )
                treatBouncesComplaintsAsFullBlock = false;
                        
            Query q = em.createNamedQuery( treatBouncesComplaintsAsFullBlock ? "EmailBlock.findFullBlockOrBounceOrComplainForEmail" : (fullBlock ? "EmailBlock.findFullBlockForEmail" : "EmailBlock.findForEmail") );
            
            // Query q = em.createNamedQuery( fullBlock ? "EmailBlock.findFullBlockForEmail" : "EmailBlock.findForEmail" );

            q.setParameter( "email", email );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            EmailBlock emailBlock = (EmailBlock) q.getSingleResult();

            return emailBlock != null;
        }

        catch( NoResultException e )
        {
            return false;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "hasEmailBlock() email=" + email );

            throw new STException( e );
        }

    }

}
