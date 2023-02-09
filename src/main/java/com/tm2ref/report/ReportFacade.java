package com.tm2ref.report;

import com.tm2ref.entity.report.Report;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class ReportFacade
{
    @PersistenceContext
    EntityManager em;

    // private static EntityManagerFactory tm2Factory;

    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;


    public static ReportFacade getInstance()
    {
        try
        {
            return (ReportFacade) InitialContext.doLookup( "java:module/ReportFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReportFacade.getInstance() " );
            return null;
        }
    }
     
    
    public Report getReport( long reportId ) throws Exception
    {
        try
        {
            return em.find(Report.class, reportId );
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ReportFacade.getReport( " + reportId + " ) " );
            throw new STException( e );
        }
    }       
    

}
