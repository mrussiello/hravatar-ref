package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcCheckLog;
import com.tm2ref.entity.ref.RcOrgPrefs;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.ref.RcRating;
import com.tm2ref.entity.ref.RcReferral;
import com.tm2ref.entity.ref.RcSuborgPrefs;
import com.tm2ref.entity.ref.RcSuspiciousActivity;
import com.tm2ref.global.STException;
import com.tm2ref.purchase.CreditType;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.sql.DataSource;



@Stateless
public class RcFacade
{
    @PersistenceContext
    EntityManager em;

    public static RcFacade getInstance()
    {
        try
        {
            return (RcFacade) InitialContext.doLookup( "java:module/RcFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "RcFacade.getInstance() " );
            return null;
        }
    }
    
    
    public RcCheck getRcCheckForUserIdAndRcScriptId( long userId, long adminUserId, int rcScriptId, Date createdAfterDate ) throws Exception
    {
        try
        {
            return (RcCheck) em.createNamedQuery( "RcCheck.findRecentByUserIdAndScriptId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("userId", userId ).setParameter("adminUserId", adminUserId ).setParameter("rcScriptId", rcScriptId ).setParameter("createdAfterDate", createdAfterDate ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getActiveRcCheckForUserIdAndRcScriptId( userId=" + userId + ", adminUserId=" + adminUserId + ", rcScriptId=" + rcScriptId + " ) " );
            throw new STException( e );
        }
    }
    
    public RcCheck getRcCheckForTestKeyId( long testKeyId ) throws STException
    {
        try
        {
            return (RcCheck) em.createNamedQuery( "RcCheck.findByTestKeyId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("testKeyId", testKeyId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcCheckForTestKeyId( " + testKeyId + " ) " );
            throw new STException( e );
        }
    }       
    
    
    public RcCheck getRcCheck( long rcCheckId, boolean refresh ) throws STException
    {
        try
        {
            if( refresh )
                return (RcCheck) em.createNamedQuery( "RcCheck.findByRcCheckId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcCheckId", rcCheckId ).getSingleResult();

            return em.find(RcCheck.class, rcCheckId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcCheck( " + rcCheckId + ", " + refresh + " ) " );
            throw new STException( e );
        }
    }       

    public RcOrgPrefs getRcOrgPrefsForOrgId( int orgId ) throws STException
    {
        try
        {
            Query q = em.createNamedQuery( "RcOrgPrefs.findByOrgId", RcOrgPrefs.class );
            q.setParameter( "orgId", orgId );            
            return (RcOrgPrefs) q.getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcOrgPrefs( " + orgId + " ) " );
            throw new STException( e );
        }
    }   
    
    public RcSuborgPrefs getRcSuborgPrefsForSuborgId( int suborgId ) throws STException
    {
        try
        {
            Query q = em.createNamedQuery( "RcSuborgPrefs.findBySuborgId", RcSuborgPrefs.class );
            q.setParameter( "suborgId", suborgId );            
            return (RcSuborgPrefs) q.getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcSuborgPrefsForSuborgId( " + suborgId + " ) " );
            throw new STException( e );
        }
    }   
    
    
    public RcRater getRcRater( long rcRaterId, boolean refresh ) throws STException
    {
        try
        {
            if( refresh )
                return (RcRater) em.createNamedQuery( "RcRater.findByRcRaterId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcRaterId", rcRaterId ).getSingleResult();

            return em.find(RcRater.class, rcRaterId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcRater( " + rcRaterId + ", " + refresh + " ) " );
            throw new STException( e );
        }
    }       

    public RcRating getRcRating( long rcRatingId, boolean refresh ) throws STException
    {
        try
        {
            if( refresh )
                return (RcRating) em.createNamedQuery( "RcRating.findByRcRatingId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcRatingId", rcRatingId ).getSingleResult();

            return em.find(RcRating.class, rcRatingId );
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcRating( " + rcRatingId + ", " + refresh + " ) " );
            throw new STException( e );
        }
    }       
    
    
    public void deleteRcRater( long rcRaterId ) throws Exception
    {
        try
        {      
            RcRater r = this.getRcRater( rcRaterId, true);            
            if( r==null )
                return;
            if( r.getRcRaterStatusType().getSentOrHigher() )
                throw new Exception( "RcRater has been sent. Cannot delete." );
            em.remove(r);
            em.flush();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AvEventFacade.deleteRcRater() rcRaterId=" + rcRaterId );
            throw e;
        }
    }


    public RcReferral saveRcReferral( RcReferral ir) throws STException
    {
        try
        {
            if( ir.getRcCheckId()<=0 )
                throw new Exception( "RcCheckId is 0" );
            if( ir.getRcRaterId()<=0 )
                throw new Exception( "RcRaterId is 0" );
            if( ir.getReferrerUserId()<=0 )
                throw new Exception( "ReferrerUserId is 0" );
            if( ir.getUserId()<=0 )
                throw new Exception( "UserId is 0" );            
            if( ir.getOrgId()<=0 )
                throw new Exception( "OrgId is 0" );            
            if( ir.getRcScriptId()<=0 )
                throw new Exception( "RcScriptId is 0" );            
            if( ir.getCreateDate()==null )
                ir.setCreateDate( new Date() );
                        
            ir.setLastUpdate( new Date() );
            
            if( ir.getRcCheckId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                em.detach( ir );
                em.persist( ir );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            return ir;
        }
        catch( STException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.saveRcReferral() " + ir.toString() );
            throw new STException( e );
        }
    }    


    
    
    public RcCheck saveRcCheck( RcCheck ir, boolean updateSeconds) throws STException
    {
        try
        {
            if( ir.getUserId()<=0 )
                throw new Exception( "UserId is 0" );
            
            if( ir.getOrgId()<=0 )
                throw new Exception( "OrgId is 0" );
            
            if( ir.getCreateDate()==null )
                ir.setCreateDate( new Date() );
            
            if( ir.getRcCheckId()<=0 || ir.getCandidateAccessCode()==null || ir.getCandidateAccessCode().isBlank() )
            {
                ir.setCandidateAccessCode(Integer.toHexString( ir.getOrgId() ) + "XC" + StringUtils.generateRandomStringForPin( 10 ) );
                
                RcCheck rcr = getRcCheckByCandidateAccessCode( ir.getCandidateAccessCode());
                while( rcr!=null )
                {
                    ir.setCandidateAccessCode(Integer.toHexString( ir.getOrgId() ) + "XC" + StringUtils.generateRandomStringForPin( 10 ) );  
                    rcr = getRcCheckByCandidateAccessCode( ir.getCandidateAccessCode() );
                    if( rcr!=null && rcr.getRcCheckId()==ir.getRcCheckId() )
                        rcr=null;
                }                
            }
            
            // always check for creditId if none is there.
            if( ir.getRcCheckId()>0 && ir.getCreditId()<=0 )
            {
                int[] d = this.getRcCheckCreditInfo( ir.getRcCheckId() );
                if( d!=null && d[0]>0 )
                {
                    ir.setCreditId(d[0]);
                    ir.setCreditIndex(d[1]);
                }
            }
                
            
            ir.setLastUpdate( new Date() );
            
            if( updateSeconds && ir.getLastSecondsDate()!=null )
            {
                long ms = (new Date()).getTime() - ir.getLastSecondsDate().getTime();
                int secs = (int) (ms/1000);
                ir.setCandidateSeconds(ir.getCandidateSeconds()+secs);                
            }
            ir.setLastSecondsDate( new Date() );
            
            
            if( ir.getRcCheckId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                em.detach( ir );
                em.persist( ir );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            return ir;
        }
        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.saveRcCheck() " + ir.toString() );
            throw new STException( e );
        }
    }    

    /*
    public boolean getAreAllRcRatersCompleteOrHigherOLD(long rcCheckId ) throws Exception
    {
        try
        {
            List<RcRater> rcrl = this.getRcRaterList(rcCheckId);
            for( RcRater rcr : rcrl )
            {
                if( !rcr.getRcRaterStatusType().getCompleteOrHigher() )
                    return false;
            }
            return true;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getAreAllRcRatersCompleteOrHigher( " + rcCheckId + " ) " );
            throw new STException( e );
        }                
    }
    */

    
    public RcRater findActiveRcRaterByRaterEmail( String email, boolean completeOk) throws Exception
    {
        if( email==null || email.isBlank() )
            return null;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            // search for incomplete.
            String sql =  "SELECT rcraterid FROM rcrater r INNER JOIN xuser u on u.userid=r.userid WHERE u.email='" + email.trim() + "' AND r.rcraterstatustypeid<" + (completeOk ? "101" : "100") + " ORDER BY r.rcraterid ";            
            ResultSet rs = stmt.executeQuery( sql );
            long rcRaterId = 0;
            if( rs.next() )
                rcRaterId = rs.getLong(1);
            rs.close();
            
            return rcRaterId<=0 ? null : this.getRcRater(rcRaterId, true);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.findActiveRcRaterByRaterEmail() email=" + email );
            return null;
        }        
    }

    public RcCheck findActiveRcCheckForCandidateEmail( String email, boolean completeOk) throws Exception
    {
        if( email==null || email.isBlank() )
            return null;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            // search for incomplete.
            String sql = "SELECT rccheckid FROM rccheck r INNER JOIN xuser u on u.userid=r.userid WHERE u.email='" + email.trim() + "' AND r.rccheckstatustypeid<" + (completeOk ? "102" : "101") + " AND r.rccandidatestatustypeid<" + (completeOk ? "101" : "100") + " ORDER BY r.rccheckid ";            
            ResultSet rs = stmt.executeQuery( sql );
            long rccheckId = 0;
            if( rs.next() )
                rccheckId = rs.getLong(1);
            rs.close();
            
            return rccheckId<=0 ? null : getRcCheck(rccheckId, true);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.findActiveRcCheckForCandidateEmail() email=" + email );
            return null;
        }        
    }

    
    
    /*
    
    */
    public boolean getAreAllRcRatersCompleteOrHigher( long rcCheckId ) throws Exception
    {
        if( rcCheckId<=0 )
            return false;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            // search for incomplete.
            String sql = "SELECT count(1) FROM rcrater r WHERE r.rccheckid=" + rcCheckId + " AND r.rcraterstatustypeid<100 ";            
            ResultSet rs = stmt.executeQuery( sql );
            int incomplete = 0;
            if( rs.next() )
                incomplete = rs.getInt(1);
            rs.close();
            return incomplete<=0;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getAreAllRcRatersCompleteOrHigher() rcCheckId=" + rcCheckId );
            throw new STException( e );
        }
    }

    
    /*
    public float computeRcCheckPercentComplete( RcCheck rc ) throws Exception
    {
        if( rc==null )
            return 0;

        // boolean stillNeedCandidateInput = rc.getRcCandidateStatusType().getIsCompletedOrHigher();
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            String sql = "SELECT count(1), AVG(r.percentcomplete) FROM rcrater r WHERE r.rccheckid=" + rc.getRcCheckId() + " AND r.rcraterstatustypeid<=100";            
            ResultSet rs = stmt.executeQuery( sql );
            float pc=0;
            if( rs.next() )
            {
                float ct = rs.getInt(1);
                if( ct>0 )
                {
                    // average pct complete.
                    float z = rs.getFloat(2);
                    
                    // adjust if still need a candidate to complete. No ratings needed, but candidate still has stuff to do.
                    if( !rc.getCollectRatingsFmCandidate() && rc.getRequiresAnyCandidateInputOrSelfRating() && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
                        pc = (z*ct)/(ct + 1);
                                        
                    // no adjust needed
                    else
                        pc = z;
                }
                
                if( rc.getCollectRatingsFmCandidate() && ct<=1 )
                    pc = Math.min( 50f, pc );
            }
            
            rs.close();
            return (float) NumberUtils.roundIt(pc, 0);
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.computeRcCheckPercentComplete() rcCheckId=" + rc.getRcCheckId() );
            throw new STException( e );
        }
    }
    */
    
    
    public List<RcReferral> getRcReferralList( long rcCheckId ) throws Exception
    {
        try
        {
            return em.createNamedQuery( "RcReferral.findByRcCheckId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcCheckId", rcCheckId ).getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcReferralList( " + rcCheckId + " ) " );
            throw new STException( e );
        }        
    }

    public List<RcReferral> getRcReferralList( long rcCheckId, long rcRaterId ) throws Exception
    {
        try
        {
            if( rcRaterId<=0 )
                return getRcReferralList( rcCheckId );
            
            return em.createNamedQuery( "RcReferral.findByRcCheckIdAndRcRaterId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcCheckId", rcCheckId ).setParameter("rcRaterId", rcRaterId ).getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcReferralList( rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + " ) " );
            throw new STException( e );
        }        
    }

    
            
    
    public List<RcRater> getRcRaterList( long rcCheckId ) throws Exception
    {
        try
        {
            return em.createNamedQuery( "RcRater.findByRcCheckId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcCheckId", rcCheckId ).getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcRaterList( " + rcCheckId + " ) " );
            throw new STException( e );
        }        
    }
    
    public RcRater getRcRaterByRcCheckIdAndUserId( long rcCheckId,  long userId ) throws Exception
    {
        try
        {
            return(RcRater) em.createNamedQuery( "RcRater.findByRcCheckIdAndUserId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcCheckId", rcCheckId).setParameter("userId", userId).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcRaterByRcCheckIdAndUserId( rcCheckId=" + rcCheckId + ", userId=" + userId + ") " );
            throw new STException( e );
        }                
        
    }
    
    public RcRater getRcRaterByRaterAccessCode( String raterAccessCode ) throws Exception
    {
        try
        {
            return(RcRater) em.createNamedQuery( "RcRater.findByRaterAccessCode" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("raterAccessCode", raterAccessCode).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcRaterByRaterAccessCode( raterAccessCode=" + raterAccessCode+ " ) " );
            throw new STException( e );
        }                
    }
    
    public RcCheck getRcCheckByCandidateAccessCode( String candidateAccessCode ) throws Exception
    {
        try
        {
            return(RcCheck)  em.createNamedQuery( "RcCheck.findByCandidateAccessCode" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("candidateAccessCode", candidateAccessCode).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcCheckByCandidateAccessCode( candidateAccessCode=" + candidateAccessCode+ " ) " );
            throw new STException( e );
        }                
    }
    
    
    
    
    public RcRater saveRcRater(RcRater ir, boolean updateSeconds) throws Exception
    {
        try
        {
            if( ir.getRcCheckId()<=0 )
                throw new Exception( "RcCheck is 0" );
            
            if( ir.getUserId()<=0 )
                throw new Exception( "UserId is 0" );
            
            if( ir.getCreateDate()==null )
                ir.setCreateDate( new Date() );
                        
            ir.setLastUpdate( new Date() );

            if( ir.getRcRaterId()<=0 && ir.getUserId()>0 )
            {
                for( RcRater r : getRcRaterList( ir.getRcCheckId() ))
                {
                    if( r.getUserId()==ir.getUserId() )
                    {
                        LogService.logIt("RcFacade.saveRcRater() Found existing RcRater for rcCheckId=" + ir.getRcCheckId() + " and userId=" + r.getUserId() + " returning." );
                        ir.setRcRaterId( r.getRcRaterId() );
                        ir.setRaterAccessCode(r.getRaterAccessCode());                        
                        r.setUser(ir.getUser());
                        r.setRcCheck(ir.getRcCheck());
                        r.setLocale( ir.getLocale());
                        return saveRcRater(r, updateSeconds);
                    }
                }
            }
            
            if( ir.getRcRaterId()<=0 || ir.getRaterAccessCode()==null || ir.getRaterAccessCode().isBlank() )
            {
                ir.setRaterAccessCode(Integer.toHexString( ir.getOrgId() ) + "XR" + StringUtils.generateRandomStringForPin( 12 ) );                
                RcRater rcr = getRcRaterByRaterAccessCode( ir.getRaterAccessCode() );
                while( rcr!=null )
                {
                    ir.setRaterAccessCode(Integer.toHexString( ir.getOrgId() ) + "XR" + StringUtils.generateRandomStringForPin( 12 ) );  
                    rcr = getRcRaterByRaterAccessCode( ir.getRaterAccessCode() );
                    if( rcr!=null && rcr.getRcRaterId()==ir.getRcRaterId() )
                        rcr=null;
                }
            }
            
            if( updateSeconds && ir.getLastSecondsDate()!=null )
            {
                long ms = (new Date()).getTime() - ir.getLastSecondsDate().getTime();
                int secs = (int) (ms/1000);
                ir.setRaterSeconds(ir.getRaterSeconds()+secs);                
            }
            ir.setLastSecondsDate( new Date() );
                //        secs = (int) (rc.getRcRater().getLastUpdate().getTime()-rc.getRcRater().getStartDate().getTime())/1000;
                //    rc.getRcRater().setRaterSeconds(rc.getRcRater().getRaterSeconds() + secs);

                                   
            if( ir.getRcRaterId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                em.detach( ir );
                em.persist( ir );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            return ir;
        }
        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.saveRcRater() " + ir.toString() );
            throw new STException( e );
        }
    }    

    
    public RcRating saveRcRating( RcRating ir) throws Exception
    {
        try
        {
            if( ir.getRcCheckId()<=0 )
                throw new Exception( "RcCheck is 0" );
            
            if( ir.getRcRaterId()<=0 )
                throw new Exception( "RcRaterId is 0" );
            
            if( ir.getCreateDate()==null )
                ir.setCreateDate( new Date() );
                        
            ir.setLastUpdate( new Date() );
                    
            // if new, check.
            //if( ir.getRcRatingId()<=0 )
            //{
            //    RcRating r2 = getRcRatingForRcRaterAndRcItem( ir.getRcRaterId(), ir.getRcItemId() );
            //    if( r2!=null )
            //    {
            //        LogService.logIt( "RcFacade.saveRcRating() Saving a new RcRating but found existing RcRating with rcRatingId=" + r2.getRcRatingId() + " for this RcRater and RcItem. ir.getRcRaterId()=" + ir.getRcRaterId() + ", ir.getRcItemId()=" + ir.getRcItemId() + ", overWriting." );
            //        ir.setRcRatingId( r2.getRcRatingId() );
            //    }
            //}
            
            if( ir.getRcRatingId()>0 )
            {
                em.merge( ir );
            }

            else
            {
                em.detach( ir );
                em.persist( ir );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            return ir;
        }     
        catch( PersistenceException e )
        {
                
            LogService.logIt( "RcFacade.saveRcRating() XXX.1 " + e.toString() + ", " + ir.toString() + ", rcCheckId=" + ir.getRcCheckId() +", rcItemId=" + ir.getRcItemId() );
            try
            {
                // let the constrain violation clear itself if necessary
                Thread.sleep(1000);
                
                long rcRatingId = getRcRatingIdForRcRaterAndRcItem( ir.getRcRaterId(), ir.getRcItemId() );   
                // RcRating r2 = this.getRcRatingForRcRaterAndRcItem( ir.getRcRaterId(), ir.getRcItemId() );                
                LogService.logIt("RcFacade.saveRcRating() XXX.2 Checking for overlapping rating: rcRatingId=" + rcRatingId );
                if( rcRatingId>0 )
                {
                    LogService.logIt("RcFacade.saveRcRating() XXX.3 Changing RcRatingId from " + ir.getRcRatingId() + " to " + rcRatingId + " and saving." );
                    ir.setRcRatingId( rcRatingId );
                    em.persist(ir);
                    em.flush();
                }
                
                else
                    LogService.logIt(e, "RcFacade.saveRcRating() XXX.4 Unexplained Persistence exception after short wait and duplicate check. " + ir.toString() + ", rcCheckId=" + ir.getRcCheckId() +", rcItemId=" + ir.getRcItemId() );                    
            }
            catch( Exception ee )
            {
                LogService.logIt( ee, "RcFacade.saveRcRating() Checking for duplicate after error. XXX.5 " + ir.toString() );
            }
            throw new STException( e );            
        }
        catch( STException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.saveRcRating() " + ir.toString() );
            throw new STException( e );
        }
    }    
    
    public long getRcRatingIdForRcRaterAndRcItem( long rcRaterId, int rcItemId ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            // search for incomplete.
            String sql =  "SELECT rcratingid FROM rcrating r WHERE r.rcraterid=" + rcRaterId + " AND r.rcitemid=" + rcItemId;    
            long rcRatingId = 0;
            ResultSet rs = stmt.executeQuery( sql );
            if( rs.next() )
                rcRatingId = rs.getLong(1);
            rs.close();
            
            return rcRatingId;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.findActiveRcRaterByRaterEmail() rcRaterId=" + rcRaterId + ", rcItemId=" + rcItemId);
            return 0;
        }        
    }
    
    
    
    public RcRating getRcRatingForRcRaterAndRcItem( long rcRaterId, int rcItemId ) throws Exception
    {
        try
        {
            return(RcRating) em.createNamedQuery( "RcRating.findByRaterIdAndItemId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcRaterId", rcRaterId).setParameter("rcItemId", rcItemId).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcRatingForRcRaterAndRcItem( rcRaterId=" + rcRaterId + ", rcItemId=" + rcItemId + " ) " );
            throw new STException( e );
        }                
        
    }
    
    
    public List<RcRating> getRcRatingList( long rcCheckId, long rcRaterId ) throws Exception
    {
        try
        {
            
           Query q =  em.createNamedQuery( rcRaterId>0 ? "RcRating.findByRcCheckAndRater" : "RcRating.findByRcCheck" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcCheckId", rcCheckId );
           
           if( rcRaterId>0 )
               q.setParameter("rcRaterId", rcRaterId );
           
           return q.getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcRatingList( rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + " ) " );
            throw new STException( e );
        }        
    }
    
    public List<RcSuspiciousActivity> getRcSuspiciousActivityList( long rcCheckId ) throws Exception
    {
        try
        {
            return em.createNamedQuery( "RcSuspiciousActivity.findByRcCheckId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcCheckId", rcCheckId ).getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcSuspiciousActivityList( " + rcCheckId + " ) " );
            throw new STException( e );
        }        
    }
    
    
    public RcSuspiciousActivity saveRcSuspiciousActivity( RcSuspiciousActivity ir) throws Exception
    {
        try
        {
            if( ir.getRcCheckId()<=0 )
                throw new Exception( "RcCheck is 0" );
            
            if( ir.getSuspiciousActivityTypeId()==RcSuspiciousActivityType.UNKNOWN.getRcSuspiciousActivityTypeId() )
                throw new Exception( "SuspiciousActivityTypeId is invalid: " + ir.getSuspiciousActivityTypeId() );
            
            if( ir.getCreateDate()==null )
                ir.setCreateDate( new Date() );
                                   
            if( ir.getRcSuspiciousActivityId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                em.detach( ir );
                em.persist( ir );
            }

            return ir;
        }
        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.saveRcSuspiciousActivity() " + ir.toString() );
            throw new STException( e );
        }
    }    

    

    public RcCheckLog saveRcCheckLog( RcCheckLog ir) throws Exception
    {
        try
        {
            if( ir.getRcCheckId()<=0 )
                throw new Exception( "RcCheck is 0" );
            
            if( ir.getLog()==null || ir.getLog().isBlank() )
                throw new Exception( "Log Entry is missing." );
            
            if( ir.getLogDate()==null )
                ir.setLogDate( new Date() );
                                   
            if( ir.getRcCheckLogId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                em.detach( ir );
                em.persist( ir );
            }

            return ir;
        }
        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.saveRcCheckLog() " + ir.toString() );
            throw new STException( e );
        }
    }    

    
    
    
    public int[] getRcCheckCreditInfo( long rcCheckId ) throws Exception
    {
        // out[0] = creditId
        // out[1] = creditIndex
        int[] out = new int[2];
        
        if( rcCheckId<=0 )
            return out;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        
        String sql = "SELECT creditid,creditindex FROM rccheck WHERE rccheckid=" + rcCheckId;
        
        // LogService.logIt( "PurchaseFacade.getRcCheckCreditInfo( orgId=" + orgId + ", candidateUserId=" + candidateUserId + " ) sql=" + sql );        
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            try (ResultSet rs = stmt.executeQuery( sql )) 
            {
                if( rs.next() )
                {
                    out[0] = rs.getInt(1);
                    out[1] = rs.getInt(2);
                }
            }
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RcFacade.getRcCheckCreditInfo( rcCheckId=" + rcCheckId + " ) " );
            throw new STException( e );
        }        
    }
    
    
}
