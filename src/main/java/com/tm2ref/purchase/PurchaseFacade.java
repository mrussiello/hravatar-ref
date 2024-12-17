package com.tm2ref.purchase;

import com.tm2ref.entity.purchase.Credit;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.util.List;

import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceContext;
import javax.sql.DataSource;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class PurchaseFacade
{
    @PersistenceContext
    EntityManager em;

    public static PurchaseFacade getInstance()
    {
        try
        {
            return (PurchaseFacade) InitialContext.doLookup( "java:module/PurchaseFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }


    /*
    public int getTotalRemainingCredits( int orgId, int backupOrgId, int minimumNeeded, int creditTypeId) throws Exception
    {
        if( orgId <= 0 )
            return 0;

        int v = getTotalRemainingCredits(orgId, creditTypeId );

        if( v >= minimumNeeded )
            return v;

        if( backupOrgId > 0 )
        {
            int vbu = getTotalRemainingCredits(backupOrgId, creditTypeId );

            if( vbu > v )
                return vbu;
        }

        return v;
    }
    */

    
    public Date findFirstUseResultCreditId( int creditId, int creditIndex, long candidateUserId, int orgId ) throws Exception
    {
        if( creditId<=0 )
            return null;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        
        String sql = "(SELECT lastaccessdate as 'ldt' from testkeyarchive WHERE userid=" + candidateUserId + " AND orgid=" + orgId + " AND creditid=" + creditId + " AND creditindex=" + creditIndex + " ) " + 
                     " UNION ALL " + 
                     "(SELECT lastaccessdate as 'ldt' from testkey WHERE userid=" + candidateUserId + " AND orgid=" + orgId + " AND creditid=" + creditId + " AND creditindex=" + creditIndex + " ) ORDER BY ldt LIMIT 1";
                
        // LogService.logIt( "PurchaseFacade.findFirstUseResultCreditId() sql=" + sql );        
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery( sql );

            Date earlyDate = null;
            Date date2 = rs.next() ? rs.getDate(1) : null;                        
            rs.close();
            if( date2!=null && ( earlyDate==null || date2.before(earlyDate) ) )
                earlyDate = date2;


            // check for another RcCheck
            sql = "SELECT lastupdate FROM rccheck WHERE userid=" + candidateUserId + " AND orgid=" + orgId + " AND creditid=" + creditId + " AND creditindex=" + creditIndex + " ORDER BY lastupdate LIMIT 1";
            rs = stmt.executeQuery( sql );
            date2 = rs.next() ? rs.getDate(1) : null;                        
            rs.close();
            if( date2!=null && ( earlyDate==null || date2.before(earlyDate) ) )
                earlyDate = date2;           

            
            // check Lv Invitation
            sql = "SELECT lastupdate FROM lvinvitation WHERE recipientuserid=" + candidateUserId + " AND orgid=" + orgId + " AND creditid=" + creditId + " AND creditindex=" + creditIndex + " ORDER BY lastupdate LIMIT 1";
            rs = stmt.executeQuery( sql );
            date2 = rs.next() ? rs.getDate(1) : null;                        
            rs.close();
            if( date2!=null && ( earlyDate==null || date2.before(earlyDate) ) )
                earlyDate = date2;           
            
            return earlyDate;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "PurchaseFacade.findFirstUseResultCreditId( creditId=" + creditId + " ) " );
            throw new STException( e );
        }        

    }
    
    public int[] findTestingCreditIdToUseForRef( int orgId, long candidateUserId, int daysPrev, int creditTypeId ) throws Exception
    {
        // out[0] = creditId
        // out[1] = creditIndex
        int[] out = new int[2];
        
        if( orgId<=0 || candidateUserId<=0 )
            return out;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DAY_OF_MONTH, -1*daysPrev - 1 ); // go back 90 days.
        Date startDate = cal.getTime();
        java.sql.Timestamp sDate = new java.sql.Timestamp( startDate.getTime() );
        
        String sql;
        
        if( CreditType.getValue( creditTypeId ).getIsResult() )
            sql = "(SELECT creditid AS 'cid', creditindex as 'cdx' from testkeyarchive WHERE orgid=" + orgId + " AND userid=" + candidateUserId + " AND lastaccessdate>='" + sDate.toString() + "' AND creditid>0 ) " + 
                     " UNION ALL " + 
                     "(SELECT creditid AS 'cid', creditindex as 'cdx' from testkey WHERE orgid=" + orgId + " AND userid=" + candidateUserId + " AND lastaccessdate>='" + sDate.toString() + "' AND creditid>0 ) ORDER BY cid DESC LIMIT 1";
        else
            sql = "(SELECT creditid AS 'cid', 0 as 'cdx' from testeventarchive WHERE orgid=" + orgId + " AND userid=" + candidateUserId + " AND lastaccessdate>='" + sDate.toString() + "' AND creditid>0 ) " + 
                     " UNION ALL " + 
                     "(SELECT creditid AS 'cid', 0 as 'cdx' from testevent WHERE orgid=" + orgId + " AND userid=" + candidateUserId + " AND lastaccessdate>='" + sDate.toString() + "' AND creditid>0 ) ORDER BY cid DESC LIMIT 1";        
                
        // LogService.logIt( "PurchaseFacade.findTestingCreditIdToUseForRef( orgId=" + orgId + ", candidateUserId=" + candidateUserId + " ) sql=" + sql );        
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery( sql );

            //int creditId =0;
            //int creditIndex = 0;
            Date earlyDate;
            if( rs.next() )
            {
                out[0] = rs.getInt(1);
                out[1] = rs.getInt(2);
                rs.close();                 
                if( out[0]>0 )
                {
                    if( !CreditType.getValue( creditTypeId ).getIsResult() )
                        return out;
                    
                    earlyDate = findFirstUseResultCreditId( out[0], out[1], candidateUserId, orgId );
                    if( earlyDate!=null && earlyDate.after( startDate ) )
                        return out;

                    out[0]=0;
                    out[1]=0;
                }
            }
            rs.close();

            if( !CreditType.getValue( creditTypeId ).getIsResult() )
                return out;

            // check for another RcCheck for same candidate
            sql = "SELECT creditid, creditindex FROM rccheck WHERE orgid=" + orgId + " AND userid=" + candidateUserId + " AND lastupdate>='" + sDate.toString() + "' AND creditid>0 ORDER BY creditid DESC LIMIT 1";
            rs = stmt.executeQuery( sql );

            if( rs.next() )
            {
                out[0] = rs.getInt(1);
                out[1] = rs.getInt(2);
                rs.close();                
                if( out[0]>0 )
                {
                    earlyDate = findFirstUseResultCreditId(out[0], out[1], candidateUserId, orgId);
                    if( earlyDate!=null && earlyDate.after( startDate ) )
                        return out;
                    
                    out[0]=0;
                    out[1]=0;
                }
            }
            rs.close();            

            
            // check Lv Invitation for same candidate
            sql = "SELECT creditid, creditindex from lvinvitation WHERE orgid=" + orgId + " AND recipientuserid=" + candidateUserId + " AND lastupdate>='" + sDate.toString() + "' AND creditid>0  ORDER BY creditid DESC LIMIT 1";
            rs = stmt.executeQuery( sql );
            if( rs.next() )
            {
                out[0] = rs.getInt(1);
                out[1] = rs.getInt(2);
                rs.close();                
                if( out[0]>0 )
                {
                    earlyDate = findFirstUseResultCreditId(out[0], out[1], candidateUserId, orgId);
                    if( earlyDate!=null && earlyDate.after( startDate ) )
                        return out;

                    out[0]=0;
                    out[1]=0;
                }
            }
            rs.close();            
            
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "PurchaseFacade.findTestingCreditIdToUseForRef( orgId=" + orgId + ", candidateUserId=" + candidateUserId + " ) " );
            throw new STException( e );
        }        
    }
    
    
    public int getTotalRemainingCredits( int orgId, int creditTypeId) throws Exception
    {
        if( orgId <= 0 )
            return 0;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            java.sql.Timestamp sDate = new java.sql.Timestamp( new Date().getTime() );

            String sql = "SELECT SUM( remainingcount) FROM credit WHERE orgid=" + orgId + " AND creditstatustypeid=" + CreditStatusType.ACTIVE.getCreditStatusTypeId() + " AND credittypeid=" + creditTypeId + " AND expiredate > '" + sDate.toString() + "' ";
            
            ResultSet rs = stmt.executeQuery( sql );

            if( rs.next() )
            {
                if( creditTypeId==CreditType.LEGACY.getCreditTypeId() )            
                    return rs.getInt(1);
                
                int remCt = rs.getInt(1);
                rs.close();
                sql = "SELECT sum(overagecount) FROM credit WHERE orgid=" + orgId + " AND creditstatustypeid IN (" + CreditStatusType.ACTIVE.getCreditStatusTypeId() + "," + CreditStatusType.OVERAGE.getCreditStatusTypeId() + ") AND credittypeid=" + creditTypeId + " AND expiredate > '" + sDate.toString() + "' ";
                rs = stmt.executeQuery( sql );
                if( rs.next() )
                    remCt -= rs.getInt(1);
                return remCt;
            }

            return 0;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "PurchaseFacade.getTotalRemainingCredits( " + orgId + " ) " );

            throw new STException( e );
        }
    }


    /*
    public Credit chargeCredit( int orgId, int qua, int creditTypeId) throws Exception
    {
        int orgIdToUse = orgId;

        //if( backupOrgId > 0 )
        //{
        //    int cv = getTotalRemainingCredits(orgId, creditTypeId );

        //    if( cv < qua )
        //        orgIdToUse = backupOrgId;
        //}

        int charged = 0;

        int chgThisCredit = 0;

        Credit lastCredit = null;

        List<Credit> cl = getNextCreditList( orgIdToUse, creditTypeId );
        
        for( Credit c : cl )
        {            
            if( c.getRemainingCount()>0 )
            {
                lastCredit = c;

                chgThisCredit = Math.min(c.getRemainingCount() , (qua - charged) );

                c.setUsedCount( c.getUsedCount() + chgThisCredit );

                c.setRemainingCount( c.getInitialCount() - c.getUsedCount() );

                if( c.getRemainingCount() < 0 )
                    c.setRemainingCount( 0 );

                if( c.getRemainingCount()==0 )
                {
                    c.setCreditStatusTypeId( CreditStatusType.EMPTY.getCreditStatusTypeId() );
                    c.setCreditZeroDate( new Date() );
                    c.setCreditZeroStatusTypeId(0);
                }


                saveCredit( c );

                charged += chgThisCredit;

                if( charged >= qua )
                    break;
            }
        }

        
        // Set credit zero info if we just used up the last credit for this account.
        if( lastCredit!=null && lastCredit.getRemainingCount()<=0 )
        {
            // check for remaining.
            int remaining = 0;
            for( Credit c : cl )
            {            
                if( c.getRemainingCount()>0 )
                    remaining+=c.getRemainingCount();
            }
            if( remaining<=0 )
            {
                lastCredit.setCreditZeroDate(new Date());
                lastCredit.setCreditZeroStatusTypeId(1);
                saveCredit( lastCredit );
            }
        }
        
        
        if( charged<qua && creditTypeId==CreditType.RESULT.getCreditTypeId() )
        {
            if( lastCredit==null )
                lastCredit = getLatestCreditRecord( orgIdToUse, creditTypeId );
            
            if( lastCredit!=null )
            {
                lastCredit.setOverageCount(lastCredit.getOverageCount() + (qua-charged) );
                
                if( lastCredit.getOverageCount()>0 )
                    lastCredit.setCreditStatusTypeId( CreditStatusType.OVERAGE.getCreditStatusTypeId() );
                
                saveCredit( lastCredit );
            }
            else
                LogService.logIt("PurchaseFacade.chargeCredit( " + orgId + " ) Cannot find a Credit Record to decrement for Result-Credit." );
        }
        
        return lastCredit;
    }
    */



    //public Credit getNextCreditToCharge( int orgId, int creditTypeId) throws Exception
    //{
    //    List<Credit> cl = getNextCreditList(orgId, creditTypeId );

    //    if( cl != null && !cl.isEmpty() )
    //        return cl.get(0);

     //   return null;
    //}

    public List<Credit> getNextCreditList( int orgId, int creditTypeId) throws Exception
    {
        try
        {
            if( orgId <= 0 )
                return null;

            Query q = em.createNamedQuery( "Credit.findAvailEntriesForOrg" );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            q.setParameter( "orgId", orgId );
            q.setParameter( "creditTypeId", creditTypeId );
            q.setParameter( "today", new Date() );

            return q.getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "PurchaseFacade.getNextCreditList( orgId=" + orgId + ", creditTypeId=" + creditTypeId + " ) " );
            throw new STException( e );
        }
    }

    public Credit getLatestCreditRecord( int orgId, int creditTypeId ) throws Exception
    {
        // Credit.findLastEntryForOrg
        try
        {
            if( orgId <= 0 )
                return null;

            Query q = em.createNamedQuery( "Credit.findLastEntryForOrg" );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            q.setParameter( "orgId", orgId );
            q.setParameter( "creditTypeId", creditTypeId );
            q.setMaxResults(1);

            List<Credit> cl = q.getResultList();
            
            if( cl.isEmpty() )
                return null;
            return cl.get(0);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "PurchaseFacade.getLatestCreditRecord( orgId=" + orgId + ", creditTypeId=" + creditTypeId + " ) " );
            throw new STException( e );
        }
        
    }
    

    public Credit saveCredit( Credit credit ) throws Exception
    {
        try
        {
            if( credit.getOrgId() <= 0  )
                 throw new Exception( "Either Credit.orgId is required." );

            if( credit.getCreditStatusTypeId() <= 0 )
                throw new Exception( "credit.creditStatusTypeId invalid: " + credit.getCreditStatusTypeId() );

            if( credit.getCreateDate() == null )
                credit.setCreateDate( new Date() );

            credit.setRemainingCount( credit.getInitialCount() - credit.getUsedCount() );

            if( credit.getRemainingCount() < 0 )
                credit.setRemainingCount( 0 );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( credit.getCreditId() > 0 )
            {
                em.merge( credit );
            }

            else
            {
                // em.detach( credit );

                em.persist( credit );
            }

            em.flush();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "PurchaseFacade.saveCredit() " + credit.toString() );

            throw new STException( e );
        }

        return credit;
    }


    public Credit getCredit( long creditId ) throws Exception
    {
        try
        {
            if( creditId <= 0 )
                throw new Exception( "creditId is invalid " + creditId );

            return (Credit) em.createNamedQuery( "Credit.findByCreditId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "creditId", creditId ).getSingleResult();

        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "PurchaseFacade.getCredit( " + creditId + " )" );

            throw new STException( e );
        }

    }
}
