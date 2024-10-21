package com.tm2ref.event;

import com.tm2ref.entity.event.TestEventLog;
import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.event.TestKeyArchive;
import com.tm2ref.entity.user.OrgAutoTest;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;

import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceContext;
import java.util.Date;



@Stateless
public class EventFacade
{
    @PersistenceContext
    EntityManager em;

    public static EventFacade getInstance()
    {
        try
        {
            return (EventFacade) InitialContext.doLookup( "java:module/EventFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }

    public OrgAutoTest getOrgAutoTest( int orgAutoTestId ) throws Exception
    {
        try
        {
            if( orgAutoTestId <= 0 )
                throw new Exception( "orgAutoTestId is invalid " + orgAutoTestId );
            return (OrgAutoTest) em.createNamedQuery( "OrgAutoTest.findByOrgAutoTestId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "orgAutoTestId", orgAutoTestId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getOrgAutoTest( " + orgAutoTestId + " )" );
            throw e;
        }
    }

    
    

    private TestKeyArchive getTestKeyArchive( long testKeyId ) throws Exception
    {
        try
        {
            if( testKeyId <= 0 )
                throw new Exception( "testKeyId is invalid " + testKeyId );
            return (TestKeyArchive) em.createNamedQuery( "TestKeyArchive.findByTestKeyId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "testKeyId", testKeyId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestKeyArchive( " + testKeyId + " )" );
            throw e;
        }
    }


    public TestKey getTestKey( long testKeyId ) throws Exception
    {      
        TestKey tk = null;
        try
        {
            if( testKeyId <= 0 )
                throw new Exception( "testKeyId is invalid " + testKeyId );
            tk = (TestKey) em.createNamedQuery( "TestKey.findByTestKeyId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "testKeyId", testKeyId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            tk = null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestKey( " + testKeyId + " )" );
            throw new STException( e );
        }
        if( tk!=null )
            return tk;
        
        TestKeyArchive tka = this.getTestKeyArchive(testKeyId);
        return tka!=null ? tka.getTestKey() : null;        
    }
    
    public void saveTestKey( TestKey tk ) throws Exception
    {
        try
        {
            if( tk.getTestKeyId()<=0 )
                throw new Exception( "TestKey.testKeyId is 0" );
                        
            em.merge(tk.getTestKeyArchive()==null ? tk : tk.getTestKeyArchiveToSave() );

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();
        }     
        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.saveTestKey() " + tk.toString() );
            throw new STException( e );
        }
    }    
    

    public void saveTestEventLog( TestEventLog tel )
    {
        try
        {
            if( tel.getLog()==null )
                return; // tel;

            //if( tel.getTestEventId()<=0 )
            //    throw new Exception( "TestEventId=0" );

            if( tel.getLogDate()==null )
                tel.setLogDate( new Date() );

            if( tel.getTestEventLogId() > 0 )
            {
                em.merge( tel );
            }

            else
            {
                // em.detach( tel );

                em.persist( tel );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.saveTestEventLog() " + ( tel == null ? "testEvent is null" : tel.toString() ) );
            // throw new STException( e );
        }
    }
    
    
}
