package com.tm2ref.purchase;



import com.tm2ref.affiliate.AffiliateAccountType;
import com.tm2ref.email.EmailBlockFacade;
import com.tm2ref.entity.purchase.Credit;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.Constants;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.service.EmailConstants;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.EmailerFacade;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserActionFacade;
import com.tm2ref.user.UserActionType;
import com.tm2ref.user.UserBean;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.MessageFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class RefCreditUtils {

    public Locale locale;

    public UserFacade userFacade;

    public EmailerFacade emailerFacade;
    
    public UserActionFacade userActionFacade;
    
    public PurchaseFacade purchaseFacade;
    
    public RcFacade rcFacade;

    public RefCreditUtils()
    {
        this.locale=Locale.US;
    }

    public RefCreditUtils( Locale l )
    {
        this.locale=l;
    }

    
    public void checkRcPreAuthorization( RcCheck rc ) throws STException
    {
        // already has credits charged.
        if( rc.getCreditId()>0 )
            return;
        
        Org o=rc.getOrg();
        try
        {        
            if( o==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                o = userFacade.getOrg( rc.getOrgId() );
                rc.setOrg(o);
            }

            // unlimited
            if( o.getOrgCreditUsageType().getUnlimited() )
            {
                if( o.getOrgCreditUsageEndDate()!=null && o.getOrgCreditUsageEndDate().before( new Date() ) )
                    throw new STException( "g.OrgCreditUsgExpired" );
                return;
            }
            
            if( purchaseFacade==null )
                purchaseFacade=PurchaseFacade.getInstance();
            
            int[] creditInfo = purchaseFacade.findTestingCreditIdToUseForRef(o.getOrgId(), rc.getUserId(), Constants.MAX_DAYS_PREV_TESTEVENT, o.getOrgCreditUsageType().getCreditType().getCreditTypeId() );
            if( creditInfo[0]>0 )
            {
                rc.setCreditId(creditInfo[0]);
                rc.setCreditIndex( creditInfo[1] );
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rcFacade.saveRcCheck(rc, false );
                return;
            }
            
            // result credit and not pre-used credit available.
            if( o.getOrgCreditUsageType().getAnyResultCredit() )
            {
                boolean ok = false;
                
                //if( purchaseFacade==null )
                //    purchaseFacade = PurchaseFacade.getInstance();
                
                if( purchaseFacade.getTotalRemainingCredits( o.getOrgId(), 0, 1, CreditType.RESULT.getCreditTypeId() )>0 )
                    ok = true;

                if( !ok )
                    throw new STException( "g.OrgCreditUsgResultNone", new String[] {rc.getRcCheckType().getName( rc.getLocale() )} );
            }
        }
        catch( STException e )
        {
            try
            {
                performCreditsNotification(rc.getOrgId(), -1, rc);
            }
            catch( Exception ee )
            {
                LogService.logIt(ee, "RefCreditUtils.checkRcPreAuthorization() Sending Credits Notification. rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) + ", initial STException: " + e.getKey() );
            }
            
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RefCreditUtils.checkRcPreAuthorization() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) );                         
        }
    }


    
    
    public void chargeCreditsIfNeeded( Org o, RcCheck rc ) throws STException
    {
        if( o==null && rc!=null )
            o=rc.getOrg();

        try
        {
        
            if( o==null && (rc!=null ) )
            {
                    if( userFacade==null )
                        userFacade = UserFacade.getInstance();
                    o = userFacade.getOrg(rc.getOrgId() );

                    if( rc!=null )
                        rc.setOrg(o);
            }

            if( o.getOrgCreditUsageType().getUnlimited() )
            {
                if( o.getOrgCreditUsageEndDate()!=null && o.getOrgCreditUsageEndDate().before( new Date() ) )
                    throw new STException( "g.OrgCreditUsgExpired", new String[]{rc.getRcCheckName()} );
                return;
            }

            if( o.getOrgCreditUsageType().getAnyResultCredit() || o.getOrgCreditUsageType().getUsesCredits() )
            {
                // already has credits charged.
                if( rc!=null && rc.getCreditId()>0 )
                    return;

                boolean ok = false;
                
                CreditType creditType = o.getOrgCreditUsageType().getCreditType();
                
                if( purchaseFacade==null )
                    purchaseFacade = PurchaseFacade.getInstance();
                
                int[] creditInfo = purchaseFacade.findTestingCreditIdToUseForRef(rc.getOrgId(), rc.getUserId(), Constants.MAX_DAYS_PREV_TESTEVENT, creditType.getCreditTypeId() );                

                // found already used credit to utilize.
                if( creditInfo[0]>0 )
                {
                    LogService.logIt( "RefCreditUtils.chargeCreditsIfNeeded() BBB.1 found connected TestKey.CreditId=" + creditInfo[0] + ", index=" + creditInfo[1] );
                    rc.setCreditId(creditInfo[0]);
                    rc.setCreditIndex( creditInfo[1] );
                    if( rcFacade==null )
                        rcFacade = RcFacade.getInstance();
                    rcFacade.saveRcCheck(rc, false);
                    return;
                }
                
                int credits = creditType.getIsResult() ? 1 : Constants.REFERENCE_CHECK_LEGACY_CREDITS;                
                if( purchaseFacade.getTotalRemainingCredits( o.getOrgId(), 0, credits, creditType.getCreditTypeId() ) >= credits )
                    ok = true;

                LogService.logIt( "RefCreditUtils.chargeCreditsIfNeeded() CCC.1 OK=" + ok );
                
                if( !ok )
                    throw new STException( "g.OrgCreditUsgResultNone", new String[]{rc.getRcCheckName()} );
                
                Credit credit = purchaseFacade.chargeCredit( o.getOrgId(), 0, credits, creditType.getCreditTypeId() );                
                if( credit==null )
                    throw new Exception( "Error charging credits. Credit record is null." );
                
                LogService.logIt( "RefCreditUtils.chargeCreditsIfNeeded() DDD.1 CreditId=" + credit.getCreditId() );
                
                rc.setCreditId( credit.getCreditId() );
                rc.setCreditIndex( credit.getInitialCount() - credit.getRemainingCount() );
                if( rcFacade==null )
                    rcFacade = RcFacade.getInstance();
                rcFacade.saveRcCheck(rc, false);                
            }
        }
        catch( STException e )
        {
            try
            {
                performCreditsNotification(o.getOrgId(), -1, rc);                    
            }
            catch( Exception ee )
            {
                LogService.logIt(ee, "RefCreditUtils.chargeCreditsIfNeeded() Sending Credits Notification. testKeyId=" + (rc==null ? "null" : rc.getRcCheckId()) + ", initial STException: " + e.getKey() );
            }
            
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RefCreditUtils.chargeCreditsIfNeeded() testKeyId=" + (rc==null ? "null" : rc.getRcCheckId()) );   
            throw new STException( e.getMessage() );
        }
    }
    
    
    
    public void performCreditsNotification( int orgId, int balance, RcCheck rcCheck) throws Exception
    {
         try
         {
             if( orgId <= 0 )
                 throw new Exception( "orgId invalid" );

            // Locale locale = getLocale();

            if( userFacade == null )
                userFacade = UserFacade.getInstance();

            if( emailerFacade==null )
                emailerFacade = EmailerFacade.getInstance();

            Org org = userFacade.getOrg(orgId);

            if( org == null )
                throw new Exception( "org not found " + orgId );

            // LogService.logIt( "RefCreditUtils..performCreditsNotification() AAA balance=" + balance + ", tkId=" + (testKey==null ? "null" : testKey.getRcCheckId() ) + ", org=" + org.getName() + " (" + org.getOrgId() + "), org.AdminUserId=" + org.getAdminUserId() );
            
            if( org.getAdminUserId() <= 0 )
                return;
            
            User adminUser = userFacade.getUser( org.getAdminUserId() );

            User affiliateSrcOrgAdminUser = null;

            Org affiliateSourceOrg = null;
            
            if( org.getAffiliateId()!=null && !org.getAffiliateId().isEmpty() && org.getAffiliateAccountTypeId()!=AffiliateAccountType.SOURCE.getAffiliateAccountTypeId() )
            {
                affiliateSourceOrg = userFacade.getAffiliateSourceAccount( org.getAffiliateId() );

                if( affiliateSourceOrg == null )
                    LogService.logIt( "RefCreditUtils..performCreditsNotification() ERROR Cannot find Affiliate SourceAccount for affiliateId=" + org.getAffiliateId() );

                else
                {
                    if( affiliateSourceOrg.getAdminUserId()>0 )
                        affiliateSrcOrgAdminUser = userFacade.getUser(affiliateSourceOrg.getAdminUserId() );

                    else
                    {
                        List<User> admins = userFacade.getAdminUsersForOrgId(affiliateSourceOrg.getOrgId() );

                        if( admins != null && !admins.isEmpty() )
                            affiliateSrcOrgAdminUser = admins.get(0);
                    }
                }
            }
            
            String extraEmail = null;
            
            if( affiliateSourceOrg!=null && affiliateSourceOrg.getAdminUserId() >0  )
            {
                // affiliateSrcOrgAdminUser = userFacade.getUser(affiliateSourceOrg.getAdminUserId() );

                if( affiliateSrcOrgAdminUser != null )
                    extraEmail = affiliateSrcOrgAdminUser.getEmail();
                
                if( RuntimeConstants.getStringValue( "AdditionalAffiliateSourceEmails_OrgId_" + affiliateSourceOrg.getOrgId() )!=null && 
                    !RuntimeConstants.getStringValue( "AdditionalAffiliateSourceEmails_OrgId_" + affiliateSourceOrg.getOrgId() ).isEmpty())
                {
                    if( extraEmail==null )
                        extraEmail="";
                    
                    extraEmail=extraEmail.trim();
                    
                    if( !extraEmail.isEmpty() )
                        extraEmail+=",";
                    
                    extraEmail += RuntimeConstants.getStringValue( "AdditionalAffiliateSourceEmails_OrgId_" + affiliateSourceOrg.getOrgId() );
                }
            }
            
            

            if( adminUser == null )
            {
                List<User> admins = userFacade.getAdminUsersForOrgId(orgId);

                if( admins != null && !admins.isEmpty() )
                    adminUser = admins.get(0);
            }
            
            User authUser = null;
                        
            if( rcCheck!=null )
            {
                if( rcCheck.getAdminUser()!=null )
                    authUser = rcCheck.getAdminUser();
                
                else if( rcCheck.getAdminUserId()> 0 )
                {                
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();                    
                    rcCheck.setAdminUser(userFacade.getUser(rcCheck.getAdminUserId() ));                    
                    authUser = rcCheck.getAdminUser();
                }                    
            }
                        
            String candidate = "";
            
            if( rcCheck != null && rcCheck.getUser()!=null )
                candidate = rcCheck.getUser().getFullname() + ", " + rcCheck.getUser().getEmail() + " (" + rcCheck.getUser().getUserId() + ") ";
            
            Locale loc = null;
            Map<String, Object> emailMap = new HashMap<>();
            String[] params = new String[10];

            boolean affiliateIsAdmin = false;

            if( adminUser == null )
            {
                String m = "RefCreditUtils..performCreditsNotification() ERROR Admin user not found for org: " + org.toString() + ", adminUserId=" + org.getAdminUserId();

                if( affiliateSrcOrgAdminUser==null )
                    throw new Exception( m );

                else
                {
                    affiliateIsAdmin = true;
                    adminUser = affiliateSrcOrgAdminUser;
                    LogService.logIt( m );
                }
            }

            loc = adminUser.getLocaleToUseDefaultNull();
            if( locale == null )
            {
                UserBean ub = UserBean.getInstance();
                if( ub != null )
                    locale = ub.getLocale();
            }

            if( locale == null )
                locale = Locale.US;

            if( !EmailUtils.validateEmailNoErrors( adminUser.getEmail() ) )
            {
                LogService.logIt( "RefCreditUtils..performCreditsNotification() BBB Error. Email is invalid: " + adminUser.getEmail() );
                return;
            }

            EmailBlockFacade emailBlockFacade = EmailBlockFacade.getInstance();
            if( emailBlockFacade.hasEmailBlock(adminUser.getEmail().trim(), false, false ) )
            {
                LogService.logIt( "RefCreditUtils.performCreditsNotification() Email blocked for " + adminUser.getEmail() );
                return;
            }
            
            
            params[0] = adminUser.getFullname();
            params[1] = Integer.toString( balance );
            params[2] = org.getName() + " (" + org.getOrgId() + ") ";
            
            params[3] = rcCheck==null ? "NF" : "(" + Long.toString(rcCheck.getRcCheckId() ) + ")";

            params[4] = authUser == null ? "" : (authUser.getFullname() + " (" + authUser.getUserId() + ")" );
            params[5] = candidate == null ? "" : candidate;
            params[6] = rcCheck==null ? "Reference Check" : rcCheck.getRcCheckName();
            params[7] =  affiliateSourceOrg==null ? "" : affiliateSourceOrg.getAffiliateId();            
            params[8] = affiliateSrcOrgAdminUser==null ? "" : affiliateSrcOrgAdminUser.getFullname() + ", " + affiliateSrcOrgAdminUser.getEmail();
                                    
            boolean isResultCredit = org.getOrgCreditUsageType().getAnyResultCredit();
            boolean isUnlimited =    org.getOrgCreditUsageType().getUnlimited();         

            String subjKey = null;
            String contentKey = null;
            
            if( isUnlimited )
            {
                // Indicated a test was denied
                if( balance<0 )
                {
                    subjKey = "g.TestDeniedUnlimSubj";
                    contentKey = "g.TestDeniedUnlimContent";
                }                
            }
            
            else if( isResultCredit )
            {                
                // Indicated a test was denied
                if( balance<0 )
                {
                    subjKey = "g.TestDeniedPkgSubj";
                    contentKey = "g.TestDeniedPkgContent";
                }
                
                // Indicated no more ARs
                else if( balance == 0 )
                {
                    subjKey = "g.NoCreditsMsgPkgSubj";
                    contentKey = "g.NoCreditsMsgPkgContent";
                }
                
                // LOW ARs
                else
                {
                    subjKey = "g.LowCreditsMsgPkgSubj";
                    contentKey = "g.LowCreditsMsgPkgContent";                    
                }                
            }
            
            // credits
            else
            {
                subjKey = balance < 0 ? "g.TestDeniedSubj" : (balance==0 ? "g.NoCreditsMsgSubj" : "g.LowCreditsMsgSubj");
                contentKey = balance < 0 ? "g.TestDeniedContent" : (balance==0 ? "g.NoCreditsMsgContent" :  "g.LowCreditsMsgContent" );
            }
            
            String affiliateTxt = affiliateSourceOrg==null ? "" : ("\n" + MessageFactory.getStringMessage( loc , "g.CreditsAffiliateC" , params));
            
            String subj = MessageFactory.getStringMessage( loc , subjKey , params);            
            emailMap.put( EmailConstants.SUBJECT, subj  );
            emailMap.put( EmailConstants.CONTENT, MessageFactory.getStringMessage( loc , contentKey, params) + affiliateTxt );
            emailMap.put( EmailConstants.TO, adminUser.getEmail() );
            emailMap.put( EmailConstants.FROM, Constants.SUPPORT_EMAIL  );
            emailMap.put(EmailConstants.BCC, RuntimeConstants.getStringValue( "lowCreditsBccEmails" ) + ( extraEmail==null ? "" : "," + extraEmail ) );

            boolean sent = emailerFacade.sendEmail( emailMap );            
            if( !sent )
                return;
            
            if( userActionFacade==null)
                userActionFacade = UserActionFacade.getInstance();
            userActionFacade.saveMessageAction(adminUser, subj, UserActionType.SENT_EMAIL.getUserActionTypeId() );
         }

         catch( Exception e )
         {
            LogService.logIt(e, "RefCreditUtils.performNoCreditsNotification() orgId=" + orgId + ", balance=" + balance );
         }
    }

}
