package com.tm2ref.event;

import com.tm2ref.entity.event.TestEvent;
import com.tm2ref.entity.event.TestEventArchive;
import com.tm2ref.entity.event.TestEventLog;
import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.event.TestKeyArchive;
import com.tm2ref.entity.purchase.Product;
import com.tm2ref.entity.user.OrgAutoTest;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;

import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.sql.DataSource;



@Stateless
public class EventFacade
{
    //@PersistenceContext
    //EntityManager em;

    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )  // ( unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;


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

    public TestKey getTestKeyForOrgAndEventRef( int orgId, String extRef ) throws Exception
    {
        try
        {
            if( orgId <= 0 || extRef == null || extRef.isBlank() )
                return null;

            TypedQuery<TestKey> q = em.createNamedQuery( "TestKey.findByOrgAndExtRef", TestKey.class );

            q.setParameter( "orgId", orgId );
            q.setParameter( "extRef", extRef );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            List<TestKey> ul = q.getResultList();

            if( ul.isEmpty() )
            {
                TypedQuery<TestKeyArchive> qq = em.createNamedQuery( "TestKeyArchive.findByOrgAndExtRef", TestKeyArchive.class );

                qq.setParameter( "orgId", orgId );

                qq.setParameter( "extRef", extRef );

                qq.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

                List<TestKeyArchive> ula = qq.getResultList();

                if( ula.isEmpty() )
                    return null;

                if( ula.size()> 1 )
                    throw new Exception( "ERROR: there is more than one row with this orgId/eventRef combination." );

                return ula.get( 0 ).getTestKey();
            }

            if( ul.size()> 1 )
                throw new Exception( "ERROR: there is more than one row with this orgId/eventRef combination." );

            return ul.get( 0 );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestKeyForOrgAndEventRef( orgId=" + orgId + " , extRef=" +  extRef + " )" );
            return null;
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


    public List<TestEvent> findCompleteTestEventsForUser( List<Long> userIdList ) throws Exception
    {
        List<TestEvent> out = new ArrayList<>();

        if( userIdList==null || userIdList.isEmpty() )
            return out;

        StringBuilder sb = new StringBuilder();
        for( Long id : userIdList )
        {
            if( !sb.isEmpty() )
                sb.append(",");
            sb.append( id.toString() );
        }

        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.MONTH, -12);
        cal.add( Calendar.DAY_OF_YEAR, -1 );
        java.sql.Date sdate = new java.sql.Date( cal.getTime().getTime() );
        
        String sqlStr = "SELECT tea.testeventarchiveid FROM testeventarchive tea WHERE tea.userid in (" + sb.toString() + ") AND tea.testeventstatustypeid=120 AND tea.lastaccessdate>='" + sdate.toString() + "' ";

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );
        if( pool == null )
            throw new Exception( "EventFacade.findCompleteTestEventsForUser Can not find Datasource" );

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            TestEventArchive tea;
            long testEventArchiveId;

            ResultSet rs = stmt.executeQuery( sqlStr );
            while( rs.next() )
            {
                testEventArchiveId = rs.getLong(1);
                tea = this.getTestEventArchive(testEventArchiveId);
                if( tea!=null )
                    out.add(tea.getTestEvent() );
            }
            rs.close();

            TestEvent te;
            sqlStr = "SELECT te.testeventid FROM testevent te WHERE te.userid in (" + sb.toString() + ") AND te.testeventstatustypeid=120 AND te.lastaccessdate>='" + sdate.toString() + "' ";
            rs = stmt.executeQuery( sqlStr );
            while( rs.next() )
            {
                testEventArchiveId = rs.getLong(1);
                te = getTestEvent(testEventArchiveId);
                if( te!=null )
                    out.add(te );
            }
            rs.close();

            return out;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.findCompleteTestEventsForUser() " + sqlStr );
            throw new STException( e );
        }
    }


    public TestEvent getTestEvent( long testEventId ) throws Exception
    {
        try
        {
            TestEvent te =  emmirror.find(TestEvent.class, testEventId);
            if( te != null )
                return te;

            TestEventArchive tea = getTestEventArchiveForTestEventId( testEventId );
            return tea != null ? tea.getTestEvent() : null;
        }
        catch( NoResultException e )
        {
            TestEventArchive tea = getTestEventArchiveForTestEventId( testEventId );
            if( tea != null )
                return tea.getTestEvent();
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestEvent( " + testEventId + " ) " );
            throw new STException( e );
        }
    }

    public TestEventArchive getTestEventArchive( long testEventArchiveId ) throws Exception
    {
        try
        {
            return emmirror.find(TestEventArchive.class, testEventArchiveId);
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestEventArchive( testEventArchiveId=" + testEventArchiveId + " ) " );
            throw new STException( e );
        }
    }


    public TestEventArchive getTestEventArchiveForTestEventId( long testEventId ) throws Exception
    {
        try
        {
            Query q = emmirror.createNamedQuery( "TestEventArchive.findByTestEventId" );

            q.setParameter( "testEventId", testEventId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (TestEventArchive) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestEventArchiveForTestEventId( " + testEventId + " ) " );
            throw new STException( e );
        }
    }


    public Product getProduct( int productId ) throws Exception
    {
        try
        {
            if( productId <= 0 )
                throw new Exception( "productId is invalid " + productId );

            Query q = emmirror.createNamedQuery( "Product.findByProductId" );
            q.setParameter( "productId", productId );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (Product) q.getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getProduct( " + productId + " )" );
            throw new STException( e );
        }
    }



}
