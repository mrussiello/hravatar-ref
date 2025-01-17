/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.user;

import com.tm2ref.affiliate.AffiliateAccountType;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.user.LogonHistory;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.Suborg;
import com.tm2ref.entity.user.User;
import com.tm2ref.entity.user.UserAction;
import com.tm2ref.global.Constants;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import javax.sql.DataSource;

/**
 *
 * @author Mike
 */
@Stateless
public class UserFacade
{
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    public static UserFacade getInstance()
    {
        try
        {
            return (UserFacade) InitialContext.doLookup( "java:module/UserFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }

    public void clearSharedCache()
    {
        try
        {
            em.getEntityManagerFactory().getCache().evictAll();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.clearSharedCache() " );
        }
    }


    public LogonHistory addLogonHistory( User user, int logonTypeId, String userAgent, String ipAddress) throws Exception
    {
        try
        {
            LogonHistory lh = new LogonHistory();

            lh.setUserId( user.getUserId() );

            lh.setLogonDate( new Date() );

            lh.setLogonTypeId( logonTypeId );

            lh.setLogonHistoryId( 0 );

            lh.setOrgId( user.getOrgId() );

            lh.setSuborgId( user.getSuborgId() );

            lh.setSystemId( RuntimeConstants.getIntValue( "applicationSystemId" ) );

            lh.setUserAgent( userAgent );

            lh.setIpAddress( ipAddress );

            // utx.begin();

            em.persist(lh );

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.

            em.flush();

            // utx.commit();

            return lh;
        }

        catch( Exception e )
        {

            // if( utx.isActive() )
                // utx.rollback();

            LogService.logIt(e, "addLogonHistory() " + ( user == null ? "User is null" : user.toString() ) );

            throw new Exception( "addLogonHistory() " + ( user == null ? "User is null" : user.toString() ) + " " + e.toString() );
        }

    }


    public List<Org> getOrgListByAffiliateIdAndExtRef( String affiliateId, String affiliateExtRef ) throws Exception
    {
        try
        {
            Query q = em.createNamedQuery( "Org.findByAffiliateIdAndExtRef" );

            q.setParameter( "affiliateId", affiliateId );

            q.setParameter( "affiliateExtRef", affiliateExtRef );

            return q.getResultList();
        }

        catch( NoResultException e )
        {
            return new ArrayList<>();
        }
    }



    public List<Long> findUserIdsMatchingUser( User user ) throws Exception
    {
        List<Long> out = new ArrayList<>();

        if( user==null )
            return out;

        String sqlStr = "SELECT u.userid FROM xuser u WHERE u.userid<>" + user.getUserId() + " AND u.orgid=" + user.getOrgId() + 
                " AND ( (u.altidentifier IS NOT NULL AND u.altidentifier='" + user.getEmail() + "') OR (u.extref IS NOT NULL AND u.extref='" + user.getEmail() + "') )"; 

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );
        if( pool == null )
            throw new Exception( "RcFacade.findCompleteRcChecksForUser Can not find Datasource" );

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery( sqlStr );
            while( rs.next() )
            {
                out.add(rs.getLong(1) );
            }
            rs.close();

            out.add( user.getUserId() );            
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.findUserIdsMatchingUser() " + sqlStr );
            throw new STException( e );
        }
    }






    public Date getLastLogonDate( long userId, long logonHistoryId ) throws Exception
    {
        String sqlStr = "SELECT MAX(logondate) FROM logonhistory WHERE userid=" + userId + ( logonHistoryId>0 ? " AND logonhistoryid<>" + logonHistoryId : "" ) + " AND systemid=" + RuntimeConstants.getIntValue( "applicationSystemId" );

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery( sqlStr );

            Date d = null;

            Timestamp ts;

            if( rs.next() )
            {
                ts = rs.getTimestamp(1);

                if( ts != null )
                    d = new Date( ts.getTime() );
                else
                    d = null;
            }

            rs.close();
            return d;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.getLastLogonDate() " + sqlStr );

            throw new STException( e );
        }
    }



    public Org getOrg( int orgId ) throws Exception
    {
        try
        {
            if( orgId <= 0 )
                return null;

            TypedQuery<Org> qq = em.createNamedQuery( "Org.findByOrgId", Org.class );

            qq.setParameter( "orgId", orgId );

            qq.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            Org org = (Org) qq.getSingleResult();

            return org;
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.getOrg() orgId=" + orgId );

            return null;
        }
    }


    public UserAction saveMessageAction( User user, String subject, int userActionTypeId, long longParam1, long longParam2, String identifier, String strParam1)
    {
        try
        {
            LogService.logIt( "UserFacade.saveUserAction() " );
            if( userActionTypeId<=0 )
                throw new Exception( "UserActionTypeId invalid: " + userActionTypeId );

            if( user == null )
                throw new Exception( "User is null" );

            if( user.getEmail()==null || user.getEmail().isEmpty() || !EmailUtils.validateEmailNoErrors( user.getEmail()))
                throw new Exception( "Cannot send invalid email: " + user.toString() );

            LogService.logIt("UserFacade.saveUserAction() email=" + user.getEmail() + ", phone=" + user.getMobilePhone() );

            UserAction ua = new UserAction();

            ua.setLongParam1(longParam1);
            ua.setLongParam2(longParam2);

            ua.setStrParam1(strParam1);

            ua.setIdentifier(identifier);

            ua.setCreateDate( new Date());

            ua.setUserId( user.getUserId() );
            ua.setOrgId( user.getOrgId() );

            if( user.getUserId() <= 0 )
            {
                ua.setUserId(RuntimeConstants.getLongValue( "defaultMarketingAccountAnonymousUserId" ));
                ua.setOrgId( RuntimeConstants.getIntValue( "defaultMarketingAccountOrgId" ) );
            }

            ua.setIpCity( user.getIpCity());
            ua.setIpCountry( user.getIpCountry());
            ua.setIpState( user.getIpState() );
            ua.setStrParam5(user.getFullname());
            ua.setStrParam4( user.getEmail());
            ua.setStrParam6( user.getMobilePhone() );

            ua.setStrParam3(subject);
            ua.setUserActionTypeId(userActionTypeId);

            saveUserAction( ua );

            return ua;

        }

        catch( Exception e )
        {
            LogService.logIt(e, "UserFacade.saveUserAction() NONFATAL " + ( user == null ? "null" : user.toString()) + ", subject=" + subject + ", userActionTypeId=" + userActionTypeId );

            return null;
            // throw new Exception( "UserActionFacade.saveUserAction() " + userAction.toString() + " " + e.toString() );
        }
    }


    public UserAction saveMessageAction( long initiatorUserId, User user, String subject, int userActionTypeId, int intParam1, long longParam1, long longParam2, long longParam4, String cpid, String uid, String strParam1)
    {
        try
        {
            // LogService.logIt( "UserFacade.saveUserAction() " );
            if( userActionTypeId<=0 )
                throw new Exception( "UserActionTypeId invalid: " + userActionTypeId );

            if( user == null )
                throw new Exception( "User is null" );

            //if( user.getEmail()==null || user.getEmail().isEmpty() || !EmailUtils.validateEmailNoErrors( user.getEmail()))
            //    throw new Exception( "Cannot send invalid email: " + user.toString() );

            // LogService.logIt("UserFacade.saveUserAction() email=" + user.getEmail() + ", phone=" + user.getMobilePhone() );


            UserAction ua = new UserAction();

            ua.setUid(uid);
            ua.setIntParam1(userActionTypeId);
            ua.setLongParam1(longParam1);
            ua.setLongParam2(longParam2);
            ua.setIntParam1(intParam1);
            ua.setLongParam4(longParam4);
            ua.setLongParam3(initiatorUserId);
            ua.setStrParam1(strParam1);
            ua.setIdentifier(cpid);
            ua.setCreateDate( new Date());

            ua.setUserId( user.getUserId() );
            ua.setOrgId( user.getOrgId() );

            if( user.getUserId() <= 0 )
            {
                ua.setUserId(RuntimeConstants.getLongValue( "defaultMarketingAccountAnonymousUserId" ));
                ua.setOrgId( RuntimeConstants.getIntValue( "defaultMarketingAccountOrgId" ) );
            }

            ua.setIpCity( user.getIpCity());
            ua.setIpCountry( user.getIpCountry());
            ua.setIpState( user.getIpState() );
            ua.setStrParam5(user.getFullname());
            ua.setStrParam4( user.getEmail());
            ua.setStrParam6( user.getMobilePhone() );

            ua.setStrParam3(subject);
            ua.setUserActionTypeId(userActionTypeId);

            saveUserAction( ua );

            return ua;

        }

        catch( Exception e )
        {
            LogService.logIt(e, "UserFacade.saveUserAction() NONFATAL " + ( user == null ? "null" : user.toString()) + ", subject=" + subject + ", userActionTypeId=" + userActionTypeId );

            return null;
            // throw new Exception( "UserActionFacade.saveUserAction() " + userAction.toString() + " " + e.toString() );
        }
    }



    public UserAction saveUserAction( UserAction userAction ) throws Exception
    {
        try
        {
            if( userAction.getUserId()<=0 )
                throw new Exception( "UserAction.userId is required" );

            if( userAction.getCreateDate()==null )
                userAction.setCreateDate( new Date() );

            if( userAction.getUserActionId() > 0 )
                em.merge( userAction );

            else
                em.persist( userAction );

            em.flush();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.saveUserAction() " + userAction.toString() );
            throw new Exception( "UserFacade.saveUserAction() " + userAction.toString() + " " + e.toString() );
        }

        return userAction;
    }




    public Org saveOrg( Org org ) throws Exception
    {
        try
        {
            if( org.getOrgId() > 0 )
            {
                em.merge( org );
            }

            else
            {
                em.persist( org );
            }

            em.flush();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "saveOrg() " + org.toString() );
            throw new Exception( "saveOrg() " + org.toString() + " " + e.toString() );
        }

        return org;
    }


    public Suborg getSuborg( int suborgId ) throws Exception
    {
        try
        {
            if( suborgId <= 0 )
                return null;

            TypedQuery<Suborg> qq = em.createNamedQuery( "Suborg.findBySuborgId", Suborg.class );

            qq.setParameter( "suborgId", suborgId );

            qq.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (Suborg) qq.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.getSuborg() suborgId=" + suborgId );

            return null;
        }


    }



    public void addUserLogout( long logonHistoryId, int logoffTypeId ) throws Exception
    {
        try
        {
            LogonHistory logonHistory = em.find( LogonHistory.class, new Long( logonHistoryId ) );

            if( logonHistory != null )
            {
                logonHistory.setLogoffDate( new Date() );

                logonHistory.setLogoffTypeId( logoffTypeId );

                try
                {
	                em.merge( logonHistory );

	                em.flush();
                }

                catch( Exception e )
                {
                    // if( utx.isActive() )
	                // utx.rollback();

                    throw e;
                }
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "addUserLogout( logoutHistoryId=" + logonHistoryId + "  ) " );
        }
    }


    public User saveUser( User user, boolean replaceWithExistingOrgUser) throws Exception
    {
        try
        {
            if( user.getCreateDate() == null )
                user.setCreateDate( new Date() );

            if( user.getOrgId() <= 0 )
            {
                user.setOrgId( RuntimeConstants.getIntValue("public-orgid") );

                user.setSuborgId( RuntimeConstants.getIntValue("public-suborgid") );
            }

            user.setLastUpdate( new Date() );

            user.sanitizeUserInput();

            if( user.getUserId() > 0 )
            {
                em.merge( user );
            }

            else
            {
                // em.detach( user );
                em.persist( user );
            }

            em.flush();
        }

        catch( Exception e )
        {
            LogService.logIt(e, "UserFacade.saveUser() " + user.toString() );

            // if( utx.isActive() )
                // utx.rollback();

            throw new Exception( "UserFacade.saveUser() " + user.toString() + " " + e.toString() );

        }

        return user;
    }



    /**
     * Returns null if none found.
     */
    public User getUserByUsername( String username ) throws Exception
    {
        try
        {
            TypedQuery<User> q = em.createNamedQuery( "User.findByUsername", User.class );

            q.setParameter( "uname", username );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }
    }



    public User getUserByEmailAndOrgId( String email, int orgId ) throws Exception
    {
        try
        {
            if( email == null || email.length() == 0 )
                return null;

            Query q = em.createNamedQuery( "User.findUserByEmailAndOrgId" );

            q.setParameter( "uemail", email );
            q.setParameter( "orgId", orgId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (User) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }
    }




    /**
     * Returns null if none found.
     */
    public List<User> getUserByEmail( String email ) throws Exception
    {
        try
        {
            if( email == null || email.length() == 0 )
                return null;

            Query q = em.createNamedQuery( "User.findByEmail" );

            q.setParameter( "uemail", email );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return  q.getResultList(); // User) q.getSingleResult();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.getUserByEmail() " + email );
            return new ArrayList<>();
        }

    }





    public boolean checkPassword( long userId, String password ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        try (Connection con = pool.getConnection() )
        {
            if( password == null || password.length() < Constants.MIN_PASSWORD_LENGTH )
                return false;

            // password is invalid
            if( !password.equals( StringUtils.sanitizeStringFull( password ) ) )
                return false;

            // password is dummy pass
            if( password.equals( Constants.DUMMY_PASSWORD ) )
                return false;

            if( userId <= 0 )
                return false;

            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            password = StringUtils.sanitizeForSqlQuery( password );

            PreparedStatement ps = con.prepareStatement( "SELECT username FROM xuser WHERE zpass IS NOT NULL AND zpass=SHA2( ?, 224 ) AND userid=?" );

            ps.setString( 1, password );

            ps.setLong( 2, userId );

            ResultSet rs = ps.executeQuery();

            boolean recordFound = false;

            if( rs.next() )
                recordFound = true;

            rs.close();

            ps.close();

            if( !recordFound )
            {
                ps = con.prepareStatement( "SELECT username FROM xuser WHERE xpass IS NOT NULL AND xpass=MD5( ? ) AND userid=?" );
                ps.setString( 1, password );
                ps.setLong( 2, userId );
                rs = ps.executeQuery();
                if( rs.next() )
                    recordFound = true;
                rs.close();
                ps.close();

                // if it matched on old password storage, change to new password storage.
                if( recordFound )
                {
                    LogService.logIt( "UserFacade.checkPassword() Converting User to new password storage. userId=" + userId );
                    ps = con.prepareStatement( "UPDATE xuser SET xpass3=null,xpass2=null,xpass1=null,xpass=null,zpass=SHA2( ?, 224 ) WHERE userid=?" );
                    ps.setString( 1, password );
                    ps.setLong( 2, userId );
                    ps.executeUpdate();
                }
            }


            return recordFound;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.checkPassword() userId=" + userId );

            throw new STException( e );
        }
    }



    /**
     * Returns null if either this username (or email) is not found or the password is invalid.
     *
     */
    public User getUserByLogonInfo( String username, String password ) throws Exception
    {
        try
        {

            // both fields are required.
            if( username == null || username.length() == 0 || password == null || password.length() == 0 )
                return null;

            // first look for username
            User user = getUserByUsername( username );

            // try email if not found for username
            //if( user == null ) // && EmailUtils.validateEmailNoErrors(username) )
            //{
            //    List<User> ul = getUserByEmail( username );

            //    if( ul.size() == 1 )
            //        user = ul.get(0); // getUserByEmail( username );

            //    if( ul.size()>1 && password != null && password.length()>0 )
            //    {
            //        for( User tu : ul )
            //        {
            //            if( checkPassword( tu.getUserId(), password ) )
            //            {
            //                user = tu;
            //                break;
            //            }
            //        }
            //    }
            //}

            // if this is a superuser password
            /*
            if( password != null && password.equals( Constants.SUPER_USER_PASSWORD ) )
            {
                // see if username is a userId number
                if( user == null )
                {
                    try
                    {
                        user = getUser( new Long( username ) );
                    }

                    catch( NumberFormatException e )
                    {}
                }

                // return without testing password
                return user;
            }
            */

            // not found?
            if( user == null )
                return null;

            if( !checkPassword( user.getUserId(), password ) )
                return null;

            // passwords don't match?
            // if( user.getPassword() == null || !user.getPassword().equalsIgnoreCase( password ) )
            // return null;

            // found!
            return user;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getUserByLogonInfo( " + username + ", password=" + password + " ) " );

            return null;
        }
    }

    public User getUser( long userId ) throws Exception
    {
        return getUser( userId, true );
    }

    public List<User> getAdminUsersForOrgId( int orgId ) throws Exception
    {
        try
        {
            Query q = em.createNamedQuery( "User.findByMinRoleAndOrgId" );

            q.setParameter( "orgId", orgId );
            q.setParameter( "roleId", RoleType.ACCOUNT_LEVEL3 );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return  q.getResultList();
        }


        catch( Exception e )
        {
            LogService.logIt(e, "UserFacade.getAdminUsersForOrgId()" );
            return new ArrayList<>();
        }

    }


    public Org getAffiliateSourceAccount( String affiliateId ) throws Exception
    {
        try
        {
            if( affiliateId==null || affiliateId.isEmpty() )
                return null;

            Query q = em.createNamedQuery( "Org.findByAffiliateIdAndAffiliateAccountTypeId" );

            q.setParameter( "affiliateId", affiliateId );

            q.setParameter( "affiliateAccountTypeId", AffiliateAccountType.SOURCE.getAffiliateAccountTypeId() );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            List<Org> ol = q.getResultList();

            if( ol.isEmpty() )
                return null;

            return ol.get(0);
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.getAffiliateSourceAccount( " + affiliateId + " ) " );

            throw new STException( e );
        }
    }

    /**
     * Returns null if none found.
     */
    public User getUser( long userId, boolean noCache ) throws Exception
    {
        try
        {
            Query q = em.createNamedQuery( "User.findByUserId" );

            q.setParameter( "userid", userId );

            if( noCache )
                q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (User) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getUser( " + userId + " ) " );

            throw new STException( e );
        }
    }





    public List<String> getCountryCodeList() throws Exception
    {
        // LogService.logIt( "UserFacade.getCountryCodeList() " );

        List<String> out = new ArrayList<>();

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery( "SELECT countrycode FROM countrytype ORDER BY name" );

            while( rs.next() )
            {
                out.add( rs.getString(1));
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.getCountryCodeList() " );
        }

        return out;
    }
}
