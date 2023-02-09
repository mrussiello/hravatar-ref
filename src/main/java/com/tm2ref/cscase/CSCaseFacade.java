/*
 * Created on Jan 1, 2007
 *
 */
package com.tm2ref.cscase;

import com.tm2ref.entity.cscase.CSCase;
import com.tm2ref.entity.cscase.CSCaseEntry;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.util.Date;

import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;


import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceContext;

@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class CSCaseFacade
{
    @PersistenceContext
    EntityManager em;

    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;

    public static CSCaseFacade getInstance()
    {
        try
        {
            return (CSCaseFacade) InitialContext.doLookup( "java:module/CSCaseFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }



    public CSCase saveCSCase( CSCase csCase ) throws Exception
    {
        try
        {
            // Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            // EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( csCase.getCsCaseId() > 0 )
            {
                csCase.setLastUpdate( new Date() );

                em.merge( csCase );
            }

            else
            {
                csCase.setCreateDate( new Date() );

                csCase.setLastUpdate( csCase.getCreateDate() );

                em.persist( csCase );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            // utx.commit();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "saveCSCase() " + csCase.toString() );

            throw new Exception( "saveCSCase() " + csCase.toString() + " " + e.toString() );
        }

        return csCase;
    }

    public CSCaseEntry saveCSCaseEntry( CSCaseEntry csCaseEntry ) throws Exception
    {
        try
        {
            if( csCaseEntry.getCsCaseId() == 0 )
                throw new Exception( "CSCaseId must be specified for each CSCaseEntry" );

            // Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            // EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            // utx.begin();

            if( csCaseEntry.getCsCaseEntryId() > 0 )
            {
                em.merge( csCaseEntry );
            }

            else
            {
                csCaseEntry.setCreateDate( new Date() );

                em.persist( csCaseEntry );
            }

            CSCase csCase = em.find( CSCase.class, csCaseEntry.getCsCaseId() );

            csCase.setLastUpdate( csCaseEntry.getCreateDate() );

            em.merge( csCase );

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();
            // utx.commit();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "saveCSCaseEntry() " + csCaseEntry.toString() );

            throw new STException( e );

            // throw new Exception( "saveCSCaseEntry() " + csCaseEntry.toString() + " " + e.toString() );
        }

        return csCaseEntry;
    }





}
