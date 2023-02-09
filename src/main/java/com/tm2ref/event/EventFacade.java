package com.tm2ref.event;

import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.event.TestKeyArchive;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;

import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceContext;



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
}
