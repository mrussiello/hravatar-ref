package com.tm2ref.reminder;

import com.tm2ref.global.STException;
import com.tm2ref.ref.RcReminderType;
import com.tm2ref.service.LogService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import javax.sql.DataSource;



@Stateless
public class RcReminderFacade
{
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )  // ( unitName = "tm2" )
    EntityManager em;
    
    public static RcReminderFacade getInstance()
    {
        try
        {
            return (RcReminderFacade) InitialContext.doLookup( "java:module/RcReminderFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "RcReminderFacade.getInstance() " );
            return null;
        }
    }
    
    public List<Long> getRcCheckIdsNeedingCandidateReminder() throws Exception
    {
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DAY_OF_MONTH, -4 );
        java.sql.Timestamp minSendDate = new java.sql.Timestamp( cal.getTime().getTime() );

        cal = new GregorianCalendar();
        cal.add( Calendar.HOUR, -24 );
        Date oneDayBefore = cal.getTime();
        java.sql.Timestamp maxLastSendDate = new java.sql.Timestamp( oneDayBefore.getTime() );
        
        cal.add( Calendar.HOUR, -24 );
        Date twoDaysBefore = cal.getTime();
        cal.add( Calendar.HOUR, -24 );
        Date threeDaysBefore = cal.getTime();
        cal.add( Calendar.HOUR, -24 );
        Date fourDaysBefore = cal.getTime();
        
        // at least one day since last send.
        // sent originally
        // needs candidate reminders         
        String sqlStr = "SELECT rc.rccheckid, rc.remindertypeid, rc.senddate, rc.lastcandidatereminderdate FROM rccheck AS rc WHERE rc.rccheckstatustypeid IN (10,20) AND rc.remindertypeid IN (1,2,3) AND rc.rccandidatestatustypeid=10 AND rc.senddate IS NOT NULL AND rc.senddate>='" + minSendDate.toString() + "'  AND rc.senddate<'" + maxLastSendDate.toString() + "' AND ( rc.lastcandidatereminderdate IS NULL OR rc.lastcandidatereminderdate<'" + maxLastSendDate.toString() + "' ) ";
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        
        List<Long> out = new ArrayList<>();
        long rcid;
        RcReminderType rcReminderType;
        Date sendDate;
        Date lastReminderDate;
                
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery( sqlStr );
            
            while( rs.next() )
            {
                rcid = rs.getLong(1);
                rcReminderType = RcReminderType.getValue( rs.getInt(2) );
                sendDate = rs.getDate(3);
                lastReminderDate = rs.getDate(4);
                
                if( sendDate.after( oneDayBefore ) || (lastReminderDate!=null && lastReminderDate.after(oneDayBefore) ))
                    continue;
                
                if( rcReminderType.getMaxDaysSinceSend()==1 && sendDate.after( twoDaysBefore ) )
                    out.add( rcid );
                
                else if( rcReminderType.getMaxDaysSinceSend()==2 && sendDate.after( threeDaysBefore ) )
                    out.add( rcid );
                
                else if( rcReminderType.getMaxDaysSinceSend()==3 && sendDate.after( fourDaysBefore ) )
                    out.add( rcid );
            }
            
            rs.close();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RcReminderFacade.getRcCheckIdsNeedingCandidateReminder()" + sqlStr );
            throw new STException(e);
        }
        return out;
    }
    
    
    public List<Long> getRcRaterIdsNeedingDelayedInvitationSends() throws Exception
    {
        String sqlStr = "SELECT r.rcraterid FROM rccheck AS rc INNER JOIN rcrater AS r ON r.rccheckid=rc.rccheckid WHERE rc.rccheckstatustypeid=20 AND rc.ratersenddelaytypeid=10 AND rc.candidatecompletedate IS NOT NULL AND r.senddate IS NULL AND r.rcraterstatustypeid=0";
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        
        List<Long> out = new ArrayList<>();
                        
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery( sqlStr );            
            while( rs.next() )
            {
                out.add( rs.getLong(1) );
            }            
            rs.close();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RcReminderFacade.getRcRaterIdsNeedingDelayedInvitationSends() " + sqlStr );
            throw new STException(e);
        }
        return out;        
    }
    
    public List<Long> getRcRaterIdsNeedingReminder() throws Exception
    {
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DAY_OF_MONTH, -4 );
        java.sql.Timestamp minSendDate = new java.sql.Timestamp( cal.getTime().getTime() );

        cal = new GregorianCalendar();
        cal.add( Calendar.HOUR, -24 );
        Date oneDayBefore = cal.getTime();
        java.sql.Timestamp maxLastSendDate = new java.sql.Timestamp( oneDayBefore.getTime() );
        
        cal.add( Calendar.HOUR, -24 );
        Date twoDaysBefore = cal.getTime();
        cal.add( Calendar.HOUR, -24 );
        Date threeDaysBefore = cal.getTime();
        cal.add( Calendar.HOUR, -24 );
        Date fourDaysBefore = cal.getTime();
        
        // at least one day since last send.
        // sent originally
        // needs candidate reminders         
        String sqlStr = "SELECT r.rcraterid, rc.remindertypeid, r.senddate, r.lastreminderdate FROM rccheck AS rc INNER JOIN rcrater AS r ON r.rccheckid=rc.rccheckid WHERE rc.rccheckstatustypeid IN (20,101) AND rc.remindertypeid IN (1,2,3) AND r.rcraterstatustypeid=10 AND r.senddate IS NOT NULL AND r.senddate>='" + minSendDate.toString() + "'  AND r.senddate<'" + maxLastSendDate.toString() + "' AND ( r.lastreminderdate IS NULL OR r.lastreminderdate<'" + maxLastSendDate.toString() + "' ) ";
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        
        List<Long> out = new ArrayList<>();
        long rcrid;
        RcReminderType rcReminderType;
        Date sendDate;
        Date lastReminderDate;
                        
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery( sqlStr );
            
            while( rs.next() )
            {
                rcrid = rs.getLong(1);
                rcReminderType = RcReminderType.getValue( rs.getInt(2) );
                sendDate = rs.getDate(3);
                lastReminderDate = rs.getDate(4);
                
                // Nothing within 24 hours.
                if( sendDate.after( oneDayBefore ) || (lastReminderDate!=null && lastReminderDate.after(oneDayBefore) ))
                    continue;
                
                // Only send once - in 24 hours. Send if the send date was between 1 and 2 days ago
                if( rcReminderType.getMaxDaysSinceSend()==1 && sendDate.after( twoDaysBefore ) && (lastReminderDate==null || lastReminderDate.before(oneDayBefore)) )
                    out.add( rcrid );
                
                // Only send twice - 24 hours & 48 hours. Send if the send date was between 1 and 3 days ago
                else if( rcReminderType.getMaxDaysSinceSend()==2 && sendDate.after( threeDaysBefore ) && (lastReminderDate==null || lastReminderDate.before(oneDayBefore)) )
                    out.add( rcrid );
                
                // Only send 3 times - 24 hours, 48, 72 hours hours. Send if the send date was between 1 and 4 days ago
                else if( rcReminderType.getMaxDaysSinceSend()==3 && sendDate.after( fourDaysBefore ) && (lastReminderDate==null || lastReminderDate.before(oneDayBefore)) )
                    out.add( rcrid );
            }
            
            rs.close();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RcReminderFacade.getRcRaterIdsNeedingReminder()" + sqlStr );
            throw new STException(e);
        }
        return out;
    }
    
    
}
