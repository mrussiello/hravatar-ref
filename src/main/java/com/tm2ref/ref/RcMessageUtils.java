/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.email.EmailBlockFacade;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcOrgPrefs;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.User;
import com.tm2ref.entity.user.UserAction;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.ref.results.RcResultEmailFormatter;
import com.tm2ref.ref.results.ResultFormatterFactory;
import com.tm2ref.service.EmailConstants;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.PhoneUtils;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.UserActionType;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.GooglePhoneUtils;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 * @author miker_000
 */
public class RcMessageUtils {
    
    RcFacade rcFacade;
    UserFacade userFacade;
    // UserActionFacade userActionFacade;
    EmailBlockFacade emailBlockFacade;
    
    
    
    /*
     data[0] = email sent count
     data[1] = text sent count  
    
    sourceCode 1 = Ref Candidate Ref Utils
    sourceCode 3 = Ref AutoReminder Batch
    */
    public int[] sendRcCheckReminderToCandidate( RcCheck rc, RcOrgPrefs rcOrgPrefs, int sourceCode) throws Exception
    {
        User user = null;
        if( rc==null )
            throw new Exception( "rcCheck is null" );
        
        if( rc.getUser()==null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setUser( userFacade.getUser( rc.getUserId() ));
        }
        if( rc.getOrg()==null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setOrg( userFacade.getOrg( rc.getOrgId() ));
        }
        user = rc.getUser();     
        
        if( rc.getAdminUser()==null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setAdminUser( userFacade.getUser( rc.getAdminUserId() ));
        }
        User adminUser = rc.getAdminUser();
        
        Locale locale = rc.getLocale();
        if( user.getLocaleStr()!=null && !user.getLocaleStr().isBlank() )
            locale = I18nUtils.getLocaleFromCompositeStr( user.getLocaleStr() );
        
        String[] params = getMessageParams( adminUser, rc, null, locale, true );        
        int[] sent = new int[2];
        
        if( user.getHasMobilePhone() )
        {
            sent[1] = sendRcCheckSmsToRaterOrCandi( rc, params, rc.getUser(), true, true, locale, 0 );
            if( sent[1]>0 )
            {
                String identifier = "RC_SMS_" + rc.getRcCheckId() + "_reminder_refx_" + (new Date()).getTime();
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();
                userFacade.saveMessageAction( 0, // rc.getAdminUserId(), 
                                                user, 
                                                "RcCheck SMS", 
                                                UserActionType.SENT_TEXT.getUserActionTypeId(), 
                                                0, // intParam1
                                                rc.getRcCheckId(), // longparam1
                                                sourceCode, // longparam2 ()
                                                0, // longparam4
                                                identifier, 
                                                null, 
                                                null );
            }  
            //else
            //{
            //    setErrorMessage( PhoneUtils.getSmsErrorLangKey(sent[1]) , new String[] { rc.getUser().getFullname(), rc.getUser().getMobilePhone() });                                                
            //}
        }
        
        if( EmailUtils.validateEmailNoErrors( user.getEmail() ) )
        {
            sent[0] = sendRcCheckEmailToCandidate(rc, rcOrgPrefs, params, locale );
            if( sent[0]>0 )
            {
                String identifier = "RC_" + rc.getRcCheckId() + "_reminder_refx_" + (new Date()).getTime();
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();
                userFacade.saveMessageAction( 0, // rc.getAdminUserId(), 
                                                user, 
                                                "RcCheck Email", 
                                                UserActionType.SENT_EMAIL.getUserActionTypeId(), 
                                                0, // intParam1
                                                rc.getRcCheckId(), // longparam1
                                                sourceCode, // longparam2 (sourceCode)
                                                0, // longparam4 
                                                identifier, 
                                                null, 
                                                "html" );
            }                        
        }    

        if( sent[0]>0 || sent[1]>0 )
        {
            if( rc.getSendDate()==null )
                rc.setSendDate(new Date() );
            if( rc.getFirstCandidateSendDate()==null )
                rc.setFirstCandidateSendDate(new Date() );
            if( rc.getLastCandidateSendDate()==null )
                rc.setLastCandidateSendDate(new Date() );
            
            rc.setLastCandidateReminderDate( new Date() );

            if( !rc.getRcCandidateStatusType().getSentOrHigher() )
                rc.setRcCandidateStatusTypeId( RcCandidateStatusType.SENT.getRcCandidateStatusTypeId() );
                
            if( !rc.getRcCheckStatusType().getSentOrHigher() )
                rc.setRcCheckStatusTypeId( RcCheckStatusType.STARTED.getRcCheckStatusTypeId() );

            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rcFacade.saveRcCheck(rc, false);            
        }        
        return sent;
    }
    
    
    public int sendRcCheckEmailToCandidate( RcCheck rc, RcOrgPrefs rcOrgPrefs, String[] params, Locale l)
    {
        try
        {
            User user = rc.getUser();
            
            if( user==null )
                return 0;
            
            if( !EmailUtils.validateEmailNoErrors( user.getEmail() ) )
            {
                LogService.logIt("RcMessageUtils.sendRcCheckEmailToCandidate() To address is not a valid email address: " + user.getEmail() );
                return 0;
            }

            if( rc.getOrg()==null )
                rc.setOrg( UserFacade.getInstance().getOrg( rc.getOrgId() ));                
            
            if( l==null )
                throw new Exception( "Locale is null" );
            
            String subject = null;
            String content = null;
            
            if( rc.getSuborgId()>0 && rc.getRcSuborgPrefs()==null )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rc.setRcSuborgPrefs(rcFacade.getRcSuborgPrefsForSuborgId( rc.getSuborgId()));
            }                

            if( rc.getRcSuborgPrefs()!=null && rc.getRcSuborgPrefs().getInvitationSubj()!=null && !rc.getRcSuborgPrefs().getInvitationSubj().isBlank() )
            {
                subject = performMessageSubstitutions( rc.getRcSuborgPrefs().getInvitationSubj(), rc, user, rc.getOrg(), l, user.getTimeZone(), params[20] );                    

                subject = MessageFactory.getStringMessage( rc.getLocale(), "g.REMINDERC" ) + " " + subject;
            }

            if( rc.getRcSuborgPrefs()!=null && rc.getRcSuborgPrefs().getInvitation()!=null && !rc.getRcSuborgPrefs().getInvitation().isBlank() )
            {
                content = performMessageSubstitutions( rc.getRcSuborgPrefs().getInvitation(), rc, user, rc.getOrg(), l, user.getTimeZone(), params[20] );
                content = StringUtils.addLineBreaksXhtml(content);
            }                
                        
            if( subject==null && rcOrgPrefs!=null && rcOrgPrefs.getInvitationSubj()!=null && ! rcOrgPrefs.getInvitationSubj().isBlank() )
            {
                subject = performMessageSubstitutions( rcOrgPrefs.getInvitationSubj(), rc, user, rc.getOrg(), l, user.getTimeZone(), params[20] );                    

                subject = MessageFactory.getStringMessage( rc.getLocale(), "g.REMINDERC" ) + " " + subject;
            }
            
            if( emailBlockFacade==null )
                emailBlockFacade=EmailBlockFacade.getInstance();
            if( emailBlockFacade.hasEmailBlock(user.getEmail(), true, true))
            {
                    String identifier = "RC_" + rc.getRcCheckId() + "_CANDIDATE_REMINDER_" + (new Date()).getTime();
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();
                    userFacade.saveMessageAction(rc.getAdminUserId(), // rc.getAdminUserId(), 
                                                    user, 
                                                    subject, 
                                                    UserActionType.SENT_EMAIL_BLOCKED.getUserActionTypeId(), 
                                                    0, // intParam1
                                                    rc.getRcCheckId(), // longparam1
                                                    0, // longparam2 (sourceCode)
                                                    0, // longparam4 
                                                    identifier, 
                                                    null, 
                                                    user.getEmail() );                    
                    return 0;                
            }                
            

            if( content==null && rcOrgPrefs!=null && rcOrgPrefs.getInvitation()!=null && !rcOrgPrefs.getInvitation().isBlank() )
            {
                content = performMessageSubstitutions( rcOrgPrefs.getInvitation(), rc, user, rc.getOrg(), l, user.getTimeZone(), params[20] );
                content = StringUtils.addLineBreaksXhtml(content);
            }                
            
            String key = null;
            if( subject==null || subject.isBlank() )
            {
                key = rc.getRcCheckType().getIsPrehire() ?  "g.RCEmailHireCandSubj" : "g.RCEmail360CandSubj";
                subject = MessageFactory.getStringMessage(l, key, params );
            }
            
            if( content==null || content.isBlank() )
            {
                key = rc.getRcCheckType().getIsPrehire() ?  "g.RCEmailHireCandContent" : "g.RCEmail360CandContent";                        
                content = MessageFactory.getStringMessage(l, key, params );
            }

            String fromAddr = rc.getOrg().getHasCustomSupportSendEmail() ? rc.getOrg().getSupportSendEmail() : RuntimeConstants.getStringValue("no-reply-email");
            
            boolean includeVia = rc.getOrg()==null || !rc.getOrg().getHasCustomSupportSendEmail();
            
            StringBuilder sb = new StringBuilder();
            sb.append( fromAddr ); // + "|" + MessageFactory.getStringMessage( getLocale(), "g.SupportEmailKey", null ) );
            if( includeVia && rc.getOrg()!=null )
            {
                boolean useAdminName = rc.getAdminUser()!=null && includeVia && rc.getOrg().getUseInitiatorNameInEmails();
                String om = useAdminName ? rc.getAdminUser().getFullname() :  rc.getOrg().getName();
                om = StringUtils.replaceStr(om, "\"", "" );
                om = StringUtils.truncateString(om, 60 );
                sb.append( "|" + MessageFactory.getStringMessage(l, "g.TestInviteOrgName" , new String[]{ om, RuntimeConstants.getStringValue("default-site-name") } ));
            }
            else if( rc.getOrg()!=null )
                sb.append( "|" + rc.getOrg().getName() );
            
            if( EmailUtils.isNoReplyAddress(fromAddr ) )
                content = EmailUtils.addNoReplyMessage(content, true, l );
            
            // wrap content
            content = wrapEmailContent( content, l );
            
            Map<String, Object> emailMap = new HashMap<>();
            emailMap.put( EmailConstants.SUBJECT, subject );
            emailMap.put( EmailConstants.CONTENT, content );
            emailMap.put( EmailConstants.MIME_TYPE, "text/html" );            
            emailMap.put( EmailConstants.OVERRIDE_BLOCK, "true" );                        
            emailMap.put( EmailConstants.TO, user.getEmail() );            
            emailMap.put( EmailConstants.FROM, sb.toString() );            
            EmailUtils emailUtils = EmailUtils.getInstance();
            boolean sent = emailUtils.sendEmail( emailMap );
            
            
            
            return sent ? 1 : 0;                
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcMessageUtils.sendRcCheckEmailToRater()  rcCheckId=" + rc.getRcCheckId()  );
            return 0;
        }
        
    }
    
    
    public String performMessageSubstitutions( String inStr, RcCheck rc, User raterUser, Org org, Locale locale, TimeZone timeZone, String url )
    {
        if( inStr==null || inStr.isBlank() )
            return inStr;
        String s = StringUtils.replaceStr(inStr, "[CANDIDATENAME]" , rc.getUser().getFullname() );
        if( raterUser!=null  )
            s = StringUtils.replaceStr( s, "[RATERNAME]", raterUser.getFullname() );
        s = StringUtils.replaceStr( s, "[TYPE]", rc.getRcCheckType().getName(locale) );
        s = StringUtils.replaceStr( s, "[COMPANY]", org.getName() );        
        s = StringUtils.replaceStr( s, "[TEMPLATE]", rc.getRcScript().getName() );        
        s = StringUtils.replaceStr( s, "[EXPIRE]", I18nUtils.getFormattedDateTime(locale, rc.getExpireDate(), timeZone) );        
        s = StringUtils.replaceStr( s, "[URL]", url );
        return s;
    }
    
    
    
    
    /*
     data[0] = email sent count
     data[1] = text sent count    
    
    sourceCode 1 = Ref Candidate Ref Utils
    sourceCode 3 = Ref AutoReminder Batch
    
    */
    public int[] sendRcCheckToRater( RcCheck rc, RcRater rater, RcOrgPrefs rcOrgPrefs, int sourceCode, long userId, boolean sendIfNeedsOnly, boolean reminder) throws Exception
    {
        User user = null;
        if( rc==null )
            throw new Exception( "rcCheck is null" );
        if( rater==null )
            throw new Exception( "rater is null"); 
        user = rater.getUser();        
        if( user==null )
            throw new Exception( "RcMessageUtils.sendRcCheckToRater() user is null. rcCheckId=" + rc.getRcCheckId() );
        
        // Never sent to the target.
        if( rater.getIsCandidateOrEmployee() )
            return new int[2];
        
        if( userFacade == null )
            userFacade = UserFacade.getInstance();
        User adminUser = userFacade.getUser( rc.getAdminUserId() );
        
        if( rc.getUser()==null )
            rc.setUser( userFacade.getUser( rc.getUserId() ));
        
        Locale locale = rc.getLocale();
        if( rater.getLocale()!=null )
            locale = rater.getLocale();
        else if( rater.getUser().getLocaleStr()!=null && !rater.getUser().getLocaleStr().isBlank() )
            locale = I18nUtils.getLocaleFromCompositeStr( rater.getUser().getLocaleStr() );
        if( locale==null )
            locale=Locale.US;
        
        String[] params = getMessageParams( adminUser, rc, rater, locale, reminder );        
        int[] sent = new int[2];
        UserAction ua;
        if( user.getHasMobilePhone() && (!sendIfNeedsOnly || rater.getNeedsResendMobile() ) )
        {
            sent[1] = sendRcCheckSmsToRaterOrCandi(rc, params, user, false, reminder, locale, rater==null ? 0 : rater.getSourceUserId()==rc.getUserId() ? RcRaterSourceType.CANDIDATE.getRcRaterSourceTypeId() : RcRaterSourceType.ACCT_USER.getRcRaterSourceTypeId() );
            if( sent[1]>0 )
            {
                String identifier = "RC_SMS_" + rc.getRcCheckId() + "_" + (reminder ? "reminder_ref" : "initial_ref") + "_" + (new Date()).getTime();
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();
                userFacade.saveMessageAction( userId, // rc.getAdminUserId(), 
                                                user, 
                                                "RcCheck Text", 
                                                UserActionType.SENT_TEXT.getUserActionTypeId(), 
                                                0, // intParam1
                                                rc.getRcCheckId(), // longparam1
                                                sourceCode, // longparam2 (sourceCode)
                                                rater==null ? 0 : rater.getRcRaterId(), // longparam4
                                                identifier, 
                                                null, 
                                                null );

                //if( setWebsiteMessages )
                //    this.setInfoMessage( reminder ? "g.RCReminderTextSent" : "g.RCTextSent" , new String[]{user.getFullname(), user.getMobilePhone()} );
            }   
            rater.setNeedsResendMobile(false);
            rater.setTempMobile( user.getMobilePhone() );
        }
        
        if( EmailUtils.validateEmailNoErrors( user.getEmail() ) && (!sendIfNeedsOnly || rater.getNeedsResendEmail() ) )
        {
            sent[0] = sendRcCheckEmailToRater(rc, rcOrgPrefs, params, user, reminder, locale, rater==null ? 0 : rater.getSourceUserId()==rc.getUserId() ? RcRaterSourceType.CANDIDATE.getRcRaterSourceTypeId() : RcRaterSourceType.ACCT_USER.getRcRaterSourceTypeId() );
            if( sent[0]>0 )
            {
                String identifier = "RC_" + rc.getRcCheckId() + "_" + (reminder ? "reminder_ref" : "initial_ref") + "_" + (new Date()).getTime();
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();
                userFacade.saveMessageAction(userId, // rc.getAdminUserId(), 
                                                user, 
                                                "RcCheck Email", 
                                                UserActionType.SENT_EMAIL.getUserActionTypeId(), 
                                                0, // intParam1
                                                rc.getRcCheckId(), // longparam1
                                                sourceCode, // longparam2 (sourceCode)
                                                rater==null ? 0 : rater.getRcRaterId(), // longparam4 
                                                identifier, 
                                                null, 
                                                "html" );
                
                //if( setWebsiteMessages )
                //    setInfoMessage( reminder ? "g.RCReminderEmailSent" : "g.RCEmailSent" , new String[]{user.getFullname(), user.getEmail()} );                                
                rater.setNeedsResendEmail(false);
                rater.setTempEmail( user.getEmail() );
            }                        
        }    

        if( sent[0]>0 || sent[1]>0 )
        {
            if( rater.getSendDate()==null )
                rater.setSendDate(new Date() );
            if( reminder )
                rater.setLastReminderDate( new Date() );
            
            if( !rater.getRcRaterStatusType().getSentOrHigher() )
            {
                rater.setRcRaterStatusTypeId( RcRaterStatusType.SENT.getRcRaterStatusTypeId() );
            }

            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rcFacade.saveRcRater(rater, false);
            
            if( !rc.getRcCheckStatusType().getIsStartedOrHigher() )
            {
                rc.setRcCheckStatusTypeId( RcCheckStatusType.STARTED.getRcCheckStatusTypeId() );
                rcFacade.saveRcCheck(rc, false);
            }
        }        
        return sent;
    }


    protected int sendRcCheckEmailToRater( RcCheck rc, RcOrgPrefs rcOrgPrefs, String[] params, User user, boolean reminder, Locale l, int rcRaterSourceTypeId) throws Exception
    {
        try
        {
            if( user==null )
                return 0;
            
            if( !EmailUtils.validateEmailNoErrors( user.getEmail() ) )
            {
                LogService.logIt("RcMessageUtils.sendRcCheckEmailToRater() To address is not a valid email address: " + user.getEmail() );
                return 0;
            }

            //if( rc.getLocale()==null)
            //    rc.setLocale(getLocale() );
            String subject = null;
            String content = null;
            
            if( rc.getOrg()==null )
                rc.setOrg( UserFacade.getInstance().getOrg( rc.getOrgId() ));                
            
            if( rc.getSuborgId()>0 && rc.getRcSuborgPrefs()==null )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rc.setRcSuborgPrefs(rcFacade.getRcSuborgPrefsForSuborgId( rc.getSuborgId()));
            }                
            
            
            if( l==null )
                throw new Exception( "Locale is null" );

            if( rc.getRcSuborgPrefs()!=null && rc.getRcSuborgPrefs().getInvitationSubjRater()!=null && !rc.getRcSuborgPrefs().getInvitationSubjRater().isBlank() )
            {
                subject = performMessageSubstitutions( rc.getRcSuborgPrefs().getInvitationSubjRater(), rc, user, rc.getOrg(), l, user.getTimeZone(), params[21] );
                if( reminder )
                    subject = MessageFactory.getStringMessage( rc.getLocale(), "g.REMINDERC" ) + " " + subject;
            }
            
            if( subject==null && rcOrgPrefs!=null && rcOrgPrefs.getInvitationSubjRater()!=null && !rcOrgPrefs.getInvitationSubjRater().isBlank() )
            {
                subject = performMessageSubstitutions( rcOrgPrefs.getInvitationSubjRater(), rc, user, rc.getOrg(), l, user.getTimeZone(), params[21] );
                if( reminder )
                    subject = MessageFactory.getStringMessage( rc.getLocale(), "g.REMINDERC" ) + " " + subject;
            }
            

            if( rc.getRcSuborgPrefs()!=null && rc.getRcSuborgPrefs().getInvitationRater()!=null && !rc.getRcSuborgPrefs().getInvitationRater().isBlank() )
            {
                content = performMessageSubstitutions( rc.getRcSuborgPrefs().getInvitationRater(), rc, user, rc.getOrg(), l, user.getTimeZone(), params[21] );
                content = StringUtils.addLineBreaksXhtml(content);
            }                                
            
            if( content==null &&  rcOrgPrefs!=null && rcOrgPrefs.getInvitationRater()!=null && !rcOrgPrefs.getInvitationRater().isBlank() )
            {
                content = performMessageSubstitutions( rcOrgPrefs.getInvitationRater(), rc, user, rc.getOrg(), l, user.getTimeZone(), params[21] );
                content = StringUtils.addLineBreaksXhtml(content);
            }                                
            
            String key = null;            
            if( subject==null || subject.isBlank() )
            {
                key = rc.getRcCheckType().getIsPrehire() ?  "g.RCEmailHireRaterSubj" : "g.RCEmail360RaterSubj";
                subject = MessageFactory.getStringMessage(l, key, params );
            }

            if( emailBlockFacade==null )
                emailBlockFacade=EmailBlockFacade.getInstance();
            if( emailBlockFacade.hasEmailBlock(user.getEmail(), true, true))
            {
                    String identifier = "RC_" + rc.getRcCheckId() + "_RATER_INVITATION_" + (new Date()).getTime();
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();
                    userFacade.saveMessageAction(rc.getAdminUserId(), // rc.getAdminUserId(), 
                                                    user, 
                                                    subject, 
                                                    UserActionType.SENT_EMAIL_BLOCKED.getUserActionTypeId(), 
                                                    0, // intParam1
                                                    rc.getRcCheckId(), // longparam1
                                                    0, // longparam2 (sourceCode)
                                                    0, // longparam4 
                                                    identifier, 
                                                    null, 
                                                    user.getEmail() );                    
                    return 0;                
            }                
            
            
            if( content==null || content.isBlank() )
            {
                if( rcRaterSourceTypeId==RcRaterSourceType.CANDIDATE.getRcRaterSourceTypeId() )
                    key = rc.getRcCheckType().getIsPrehire() ?  "g.RCEmailHireRaterContent" : "g.RCEmail360RaterContent";
                else
                    key = rc.getRcCheckType().getIsPrehire() ?  "g.RCEmailHireRaterContentAcctSrc" : "g.RCEmail360RaterContentAcctSrc";

                content = MessageFactory.getStringMessage(l, key, params );
            }
            
            String fromAddr = rc.getOrg().getHasCustomSupportSendEmail() ? rc.getOrg().getSupportSendEmail() : RuntimeConstants.getStringValue("no-reply-email");
                        
            // wrap content
            content = wrapEmailContent( content, l );
            
            Map<String, Object> emailMap = new HashMap<>();
            emailMap.put( EmailConstants.SUBJECT, subject );
            
            if( EmailUtils.isNoReplyAddress(fromAddr ) )
                content = EmailUtils.addNoReplyMessage(content, true, l );
            
            emailMap.put( EmailConstants.CONTENT, content );
            emailMap.put( EmailConstants.MIME_TYPE, "text/html" );            
            emailMap.put( EmailConstants.OVERRIDE_BLOCK, "true" );                        
            emailMap.put( EmailConstants.TO, user.getEmail() );            
            emailMap.put( EmailConstants.FROM, fromAddr );            
            EmailUtils emailUtils = EmailUtils.getInstance();
            boolean sent = emailUtils.sendEmail( emailMap );
            
            return sent ? 1 : 0;                
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcMessageUtils.sendRcCheckEmailToRater()  rcCheckId=" + rc.getRcCheckId()  );
            return 0;
        }
    }

    
    protected int sendRcCheckSmsToRaterOrCandi( RcCheck rc, String[] params, User user, boolean candidate, boolean reminder, Locale l, int raterSourceTypeId) throws Exception
    {
        try
        {
            if( user==null || !user.getHasMobilePhone() )
                return 0;
            
            if( l==null )
                throw new Exception( "Locale is null" );
            
            if( rc.getOrg()==null )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                rc.setOrg( userFacade.getOrg( rc.getOrgId()));
            }
            
            if( rc.getOrg()!=null && !rc.getOrg().getIsSmsOk() )
                return 0;
            
            boolean smsOk = GooglePhoneUtils.getIsPhoneNumberAllowedForSms( user.getMobilePhone(), rc.getOrg(), user, rc.getAdminUser() );
            if( !smsOk )
            {
                LogService.logIt("RcMessageUtils.sendRcCheckSmsToRaterOrCandi() testing to international number for this org is not allowed. rcCheckId=" + rc.getRcCheckId() + ", userId=" + user.getUserId() + ", phone=" + user.getMobilePhone() );
                return 0;                        
            }
            
            String key = null;
            
            if( candidate )
                key = rc.getRcCheckType().getIsPrehire() ?  "g.RCSmsHireCandContent" : "g.RCSms360CandContent";
            else
            {
                if( RcRaterSourceType.getValue( raterSourceTypeId).getIsCandidateOrEmployee())
                    key = rc.getRcCheckType().getIsPrehire() ?  "g.RCSmsHireRaterContent" : "g.RCSms360RaterContent";
                else
                    key = rc.getRcCheckType().getIsPrehire() ?  "g.RCSmsHireRaterContentAcctSrc" : "g.RCSms360RaterContentAcctSrc";                    
            }
            
            String content = MessageFactory.getStringMessage(l, key, params );
            
            int sent = PhoneUtils.sendTextMessage(user.getMobilePhone(), user.getCountryCode(), null, null, content);            
            if( sent>0 )
                Tracker.addTextMessageSent(); 

            return sent;            
        }
        catch( com.twilio.exception.ApiException e )
        {
            LogService.logIt( "RcMessageUtils.sendRcCheckSmsToRaterOrCandi() NONFATAL " + e.toString() + ", rcCheckId=" + rc.getRcCheckId() + ", candidate=" + candidate );
            return 0;
        }
        catch( STException e )
        {
            LogService.logIt( "RcMessageUtils.sendRcCheckSmsToRaterOrCandi() NONFATAL " + e.toString() + ", rcCheckId=" + rc.getRcCheckId() + ", candidate=" + candidate );
            return 0;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcMessageUtils.sendRcCheckSmsToRaterOrCandi() rcCheckId=" + rc.getRcCheckId() + ", candidate=" + candidate );
            return 0;
        }
    }
    
    public int sendRcCheckRestartLinkEmail(RcCheck rc, RefUserType refUserType, User user, String[] params, String email, Locale l) throws Exception
    {
        try
        {
            if( email==null || email.isBlank() )
                return 0;
            
            if( !EmailUtils.validateEmailNoErrors(email))
                throw new STException( "g.EmailInvalid", new String[]{email} );
                                    
            if( l==null )
                l = Locale.US;
            
            String key = "g.RCRestartEmailSubj";
            String subj = MessageFactory.getStringMessage(l, key, params );
            
            if( emailBlockFacade==null )
                emailBlockFacade=EmailBlockFacade.getInstance();
            if( emailBlockFacade.hasEmailBlock(email, true, true))
                throw new STException( "g.EmailBlocked", new String[]{email, RuntimeConstants.getStringValue("support-email")} );                
            
            if( refUserType.getIsCandidate() )
                key = "g.RCRestartEmailContent";
            else
                key = "g.RCRestartEmailContent";            
            String content = MessageFactory.getStringMessage(l, key, params );

            
            if( rc.getOrg()==null )
                rc.setOrg( UserFacade.getInstance().getOrg( rc.getOrgId() ));                
            
            String fromAddr = rc.getOrg().getHasCustomSupportSendEmail() ? rc.getOrg().getSupportSendEmail() : RuntimeConstants.getStringValue("no-reply-email");
            
            // wrap content
            content = wrapEmailContent( content, l );
            
            Map<String, Object> emailMap = new HashMap<>();
            emailMap.put( EmailConstants.SUBJECT, subj );
            
            if( EmailUtils.isNoReplyAddress(fromAddr ) )
                content = EmailUtils.addNoReplyMessage(content, true, l );
            
            emailMap.put( EmailConstants.CONTENT, content );
            emailMap.put( EmailConstants.MIME_TYPE, "text/html" );            
            emailMap.put( EmailConstants.OVERRIDE_BLOCK, "true" );                        
            emailMap.put( EmailConstants.TO, user.getEmail() );            
            emailMap.put( EmailConstants.FROM, fromAddr );            
            EmailUtils emailUtils = EmailUtils.getInstance();
            boolean sent = emailUtils.sendEmail( emailMap );
            if( sent )
            {
                Tracker.addTextMessageSent();
                
                if( user!=null )
                {
                    String identifier = "RC_" + rc.getRcCheckId() + "_restartlink_" + (new Date()).getTime();
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();
                    userFacade.saveMessageAction(user.getUserId(), // rc.getAdminUserId(), 
                                                    user, 
                                                    "RcCheck Restart Email", 
                                                    UserActionType.SENT_EMAIL.getUserActionTypeId(), 
                                                    0, // intParam1
                                                    rc.getRcCheckId(), // longparam1
                                                    0, // longparam2 (sourceCode)
                                                    refUserType.getIsRater() && rc.getRcRater()!=null ? rc.getRcRater().getRcRaterId() : 0, // longparam4 
                                                    identifier, 
                                                    null, 
                                                    email );                    
                }
            }            
            return sent ? 1 : 0;            
        }
        catch( com.twilio.exception.ApiException e )
        {
            LogService.logIt( "RcMessageUtils.sendRcCheckRestartLinkEmail() NONFATAL " + e.toString() + ", rcCheckId=" + rc.getRcCheckId() + ", userId=" + (user==null ? "null" : user.getUserId()) + ", email=" + email );
            return 0;
        }
        catch( STException e )
        {
            LogService.logIt( "RcMessageUtils.sendRcCheckRestartLinkEmail() NONFATAL " + e.toString() + ", rcCheckId=" + rc.getRcCheckId() + ", userId=" + (user==null ? "null" : user.getUserId()) + ", email=" + email );
            return 0;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcMessageUtils.sendRcCheckRestartLinkEmail() rcCheckId=" + rc.getRcCheckId() + ", userId=" + (user==null ? "null" : user.getUserId()) + ", email=" + email );
            return 0;
        }
        
    }

    public int sendRcCheckRestartLinkSms( RcCheck rc, RefUserType refUserType, User user, String[] params, String mobilePhone, String countryCode, Locale l ) throws Exception
    {
        try
        {
            if( mobilePhone==null || mobilePhone.isBlank() )
                return 0;
            
            if( l==null )
                throw new Exception( "Locale is null" );
            
            if( rc.getOrg()==null )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                rc.setOrg( userFacade.getOrg( rc.getOrgId()));
            }
            
            if( rc.getOrg()!=null && !rc.getOrg().getIsSmsOk() )
                return 0;
            
            boolean smsOk = GooglePhoneUtils.getIsPhoneNumberAllowedForSms( mobilePhone, rc.getOrg(), user, rc.getAdminUser() );
            if( !smsOk )
            {
                LogService.logIt("RcMessageUtils.sendRcCheckRestartLinkSms() testing to international number for this org is not allowed. rcCheckId=" + rc.getRcCheckId() + ", userId=" + (user==null ? "null" : user.getUserId()) + ", phone=" + mobilePhone );
                return 0;                        
            }
            
            
            String key = null;
            
            if( refUserType.getIsCandidate() )
                key = rc.getRcCheckType().getIsPrehire() ?  "g.RCRestartSmsHireCandContent" : "g.RCRestartSms360CandContent";
            else
                key = rc.getRcCheckType().getIsPrehire() ?  "g.RCRestartSmsHireRaterContent" : "g.RCRestartSms360RaterContent";
            
            String content = MessageFactory.getStringMessage(l, key, params );
            if( user!=null && ( countryCode==null || countryCode.isBlank()) )
                countryCode = user.getCountryCode()!=null && !user.getCountryCode().isBlank() ? user.getCountryCode() : user.getIpCountry();                

            
            
            int sent = PhoneUtils.sendTextMessage(mobilePhone, countryCode, null, null, content);            
            if( sent>0 )
            {
                Tracker.addTextMessageSent();
                
                if( user!=null )
                {
                    String identifier = "RC_" + rc.getRcCheckId() + "_restartlink_" + (new Date()).getTime();
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();
                    UserAction ua = userFacade.saveMessageAction(user.getUserId(), // rc.getAdminUserId(), 
                                                                user, 
                                                                "RcCheck Restart SMS", 
                                                                UserActionType.SENT_TEXT.getUserActionTypeId(), 
                                                                0, // intParam1
                                                                rc.getRcCheckId(), // longparam1
                                                                0, // longparam2 (sourceCode)
                                                                refUserType.getIsRater() && rc.getRcRater()!=null ? rc.getRcRater().getRcRaterId() : 0, // longparam4 
                                                                identifier, 
                                                                null, 
                                                                mobilePhone );                    
                }
            }            
            return sent;            
        }
        catch( com.twilio.exception.ApiException e )
        {
            LogService.logIt( "RcMessageUtils.sendRcCheckRestartLinkSms() NONFATAL " + e.toString() + ", rcCheckId=" + rc.getRcCheckId() + ", userId=" + (user==null ? "null" : user.getUserId()) );
            return 0;
        }
        catch( STException e )
        {
            LogService.logIt( "RcMessageUtils.sendRcCheckRestartLinkSms() NONFATAL " + e.toString() + ", rcCheckId=" + rc.getRcCheckId() + ", userId=" + (user==null ? "null" : user.getUserId()) );
            return 0;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcMessageUtils.sendRcCheckRestartLinkSms() rcCheckId=" + rc.getRcCheckId() + ", userId=" + (user==null ? "null" : user.getUserId()) );
            return 0;
        }
    }

    
    
    /*
     returns
        int[0] = 
    */
    public int[] sendProgressUpdateForRaterOrCandidateComplete( RcCheck rc, RcRater rater, Locale locale, boolean forceSend) throws Exception
    {
        int[] sent = new int[2];
        if( rc==null )
            throw new Exception( "rcCheck is null" );
        
        if( rater!=null && rater.getIsCandidateOrEmployee() )
            rater=null;

        if( rater==null && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() && !rc.getRcCheckStatusType().getIsComplete() )
        {
            LogService.logIt( "RcMessageUtils.sendProgressUpdateForRaterOrCandidateComplete() rater is null but neither RcCheck or RcCandidate is complete. rcCheckId=" + rc.getRcCheckId() );
            return sent;
        } 
        
        if( rater!=null && !rater.getRcRaterStatusType().getIsCompleteOrHigher())
        {
            LogService.logIt( "RcMessageUtils.sendProgressUpdateForRaterOrCandidateComplete() rater is not null and not complete. rcCheckId=" + rc.getRcCheckId() );
            return sent;            
        }            
        
        if( (rc.getEmailResultsTo()==null || rc.getEmailResultsTo().isBlank()) && (rc.getTextResultsTo()==null || rc.getTextResultsTo().isBlank() ) )
            return sent;
        
        //if( rater !=null && !forceSend && rater.getLastProgressMsgDate()!=null )
        //    return sent;
        
        if( !forceSend && !rc.getRcDistributionType().sendForRaterCompletion( rc, rater ) )
        {
            LogService.logIt( "RcmesageUtils.sendProgressUpdateForRaterOrCandidateComplete() RcDistributionType denies sending. rcCheckId=" + rc.getRcCheckId() );
            return sent;
        }
        
        // We handle candidate completes elsewhere.
        //if( rater.getIsCandidateOrEmployee()  )
        //    return sent;
        
        if( rc.getAdminUser()== null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setAdminUser( userFacade.getUser( rc.getAdminUserId() ) );            
        }        
        if( rc.getUser()==null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setUser( userFacade.getUser( rc.getUserId() ));
        }
        if( rc.getOrg()==null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setOrg( userFacade.getOrg( rc.getOrgId() ));
        }
        
        String[] params = getMessageParams( rc.getAdminUser(), rc, rater, locale, false ); 
        
        if( rc.getTextResultsTo()!=null && !rc.getTextResultsTo().isBlank() && rc.getOrg().getIsSmsOk() )
        {
            sent[1] = sendProgressUpdateSmsForRaterOrCandidateComplete(rc, rater, locale, params, rc.getAdminUser().getCountryCode() );
        }
        
        if( rc.getEmailResultsTo()!=null && !rc.getEmailResultsTo().isBlank() )
            sent[0] = sendProgressUpdateEmailForRaterOrCandidateComplete(rc, rater, locale, params, null, false );

        if( sent[0]>0 || sent[1]>0 )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();

            // rater
            if( rater !=null )
            {
                rater.setLastProgressMsgDate( new Date() );            
                rcFacade.saveRcRater(rater, false);
            }
            
            boolean chg = false;
            if( rc.getRcCheckStatusType().getIsComplete() )
            {
                rc.setLastProgressMsgDate( new Date() );
                chg=true;
            }
            
            // candidate
            if( rater==null && rc.getRcCandidateStatusType().getIsComplete() ) 
            {
                rc.setLastCandidateProgressMsgDate( new Date() );
                chg=true;
            }

            if( chg )
                rcFacade.saveRcCheck(rc, false );                
        }
        return sent;
    }


    
    
    /*
     returns
        int[0] = number of emails sent.
    */
    public int[] sendProgressUpdateEmailForCurrentStatus( RcCheck rc, Locale locale, String destEmails) throws Exception
    {
        int[] sent = new int[2];
        if( rc==null )
            throw new Exception( "rcCheck is null" );
        
        
        if( (destEmails==null || destEmails.isBlank()) )
            return sent;
                
        // We handle candidate completes elsewhere.
        //if( rater.getIsCandidateOrEmployee()  )
        //    return sent;
        
        if( rc.getAdminUser()== null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setAdminUser( userFacade.getUser( rc.getAdminUserId() ) );            
        }        
        if( rc.getUser()==null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setUser( userFacade.getUser( rc.getUserId() ));
        }
        if( rc.getOrg()==null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setOrg( userFacade.getOrg( rc.getOrgId() ));
        }
        
        String[] params = getMessageParams( rc.getAdminUser(), rc, null, locale, false ); 
        
        sent[0] = sendProgressUpdateEmailForRaterOrCandidateComplete(rc, null, locale, params, destEmails, true );

        if( sent[0]>0 )
        {
            if( rc.getRcCheckStatusType().getIsComplete() && rc.getLastProgressMsgDate()==null )
            {
                rc.setLastProgressMsgDate(new Date() );

                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();                        
                rcFacade.saveRcCheck(rc, false);
            }
        }
        
        return sent;
    }
    
    
    
    private int sendProgressUpdateEmailForRaterOrCandidateComplete( RcCheck rc, RcRater rater, Locale l, String[] params, String forceDestEmails, boolean forceForAnyStatus) throws Exception
    {
        try
        {            
            
            // missing any data for sending.
            if( rc==null )
            {
                LogService.logIt("RcMessageUtils.sendProgressUpdateEmailForRaterOrCandidateComplete() rcCheck is null. Nothing to send." );
                return 0;
            }
            
            // missing any data for sending.
            if( (forceDestEmails==null || forceDestEmails.isBlank()) && (rc.getEmailResultsTo()==null || rc.getEmailResultsTo().isBlank()) )
            {
                LogService.logIt("RcMessageUtils.sendProgressUpdateEmailForRaterOrCandidateComplete() No emails to send to. rcCheckId=" + rc.getRcCheckId() + ", rc.getEmailResultsTo=" + rc.getEmailResultsTo() + ", forceDestEmails=" + forceDestEmails );
                return 0;
            }
            
            if( forceDestEmails==null || forceDestEmails.isBlank() )
                forceDestEmails = rc.getEmailResultsTo();

            if( rater!=null && rater.getIsCandidateOrEmployee() )
                rater=null;
                        
            // if rater is null, then this must be a candidate completion or a full completion.
            if( !forceForAnyStatus && rater==null && !rc.getRcCheckStatusType().getCompleteOrHigher() && !rc.getRcCandidateStatusType().getIsCompletedOrHigher())
            {
                LogService.logIt("RcMessageUtils.sendProgressUpdateEmailForRaterOrCandidateComplete() Not forced, rater is null and rcCheck is not completed or the candidate is not completed. rcCheckId=" + rc.getRcCheckId() );
                return 0;
            }
                        
            List<String> toList = new ArrayList<>();
            for( String em : forceDestEmails.split(",") )
            {
                if( em.isBlank() )
                    continue;                
                if( !EmailUtils.validateEmailNoErrors( em ) )
                    continue;                

                em = em.toLowerCase();                
                if( toList.contains(em) )
                    continue;
                
                toList.add( em );
            }
            
            if( toList.isEmpty() )
            {
                LogService.logIt("RcMessageUtils.sendProgressUpdateEmailForRaterOrCandidateComplete() No valid emails to send to. Returning. rcCheckId=" + rc.getRcCheckId() );
                return 0;
            }
            
            //if( rc.getLocale()==null)
            //    rc.setLocale(getLocale() );
            if( l==null && rc.getLangCode()!=null && !rc.getLangCode().isBlank() )
                l = I18nUtils.getLocaleFromCompositeStr(rc.getLangCode());
            
            if( l==null )
            {
                l=Locale.US;
                LogService.logIt("RcMessageUtils.sendProgressUpdateEmailForRaterOrCandidateComplete() locale is null, using US. rcCheckId=" + rc.getRcCheckId() );
            }
            
            String key = null;
            RcDistributionType rcDistType = rc.getRcDistributionType();

            if( rc.getRcCheckType().getIsPrehire() )
            {
                if( rc.getRcCheckStatusType().getIsComplete() )
                    key = "g.RCEmailHireProgAllComplete";  

                else if( forceForAnyStatus )
                    key = "g.RCEmailHireProgPartial";  

                else if( rcDistType.getIsEachRating() )
                {
                    if( rater==null )
                    {
                        if( rc.getRcCandidateStatusType().getIsRejection() )
                            key = "g.RCEmailHireProgCandidateRejection";
                        else
                            key = "g.RCEmailHireProgCandidateComplete";
                    }
                    else
                    {
                        if( rater.getRcRaterStatusType().getIsRejection() )
                            key = "g.RCEmailHireProgCandidateRejection";
                        else
                            key = "g.RCEmailHireProgRaterComplete";
                    }
                }
                
                else if( rcDistType.getIsPartialProgress())
                    key = "g.RCEmailHireProgPartial";
                
                else 
                    return 0;
            }
            
            else
            {
                if( rc.getRcCheckStatusType().getIsComplete() )
                    key = "g.RCEmailEmpFbkProgAllComplete";
                
                else if( forceForAnyStatus )
                    key = "g.RCEmailEmpFbkProgPartial";  

                else if( rcDistType.getIsEachRating() )
                {
                    key = rater==null ? "g.RCEmailEmpFbkProgCandidateComplete" : "g.RCEmailEmpFbkProgRaterComplete";
                }
                else if( rcDistType.getIsPartialProgress())
                    key = "g.RCEmailEmpFbkProgPartial";
                else 
                    return 0;
            }
                        
            // wrap content
            
            Map<String, Object> emailMap = new HashMap<>();
            emailMap.put( EmailConstants.MIME_TYPE, "text/html" );            
            emailMap.put( EmailConstants.OVERRIDE_BLOCK, "true" );                        
            emailMap.put( EmailConstants.FROM, RuntimeConstants.getStringValue("no-reply-email") );            
            EmailUtils emailUtils = EmailUtils.getInstance();            
            String subject;                        
            String content;
            boolean sent;
            int ct = 0;
            User ru;
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            RcResultEmailFormatter emf;
            
            for( String em : toList )
            {
                ru=userFacade.getUserByEmailAndOrgId(em, rc.getOrgId() );
                
                params[10]=ru==null ? em : ru.getFullname();
                subject = MessageFactory.getStringMessage(l, key + ".subj", params );  

                if( emailBlockFacade==null )
                    emailBlockFacade = EmailBlockFacade.getInstance();
                if( emailBlockFacade.hasEmailBlock(em, true, true) )
                {
                    if( ru!=null )
                    {
                        String identifier = "RC_" + rc.getRcCheckId() + "_STATUS_UPDATE_" + (new Date()).getTime();
                        if( userFacade == null )
                            userFacade = UserFacade.getInstance();
                        userFacade.saveMessageAction(rc.getAdminUserId(), // rc.getAdminUserId(), 
                                                        ru, 
                                                        subject, 
                                                        UserActionType.SENT_EMAIL_BLOCKED.getUserActionTypeId(), 
                                                        0, // intParam1
                                                        rc.getRcCheckId(), // longparam1
                                                        0, // longparam2 (sourceCode)
                                                        0, // longparam4 
                                                        identifier, 
                                                        null, 
                                                        em );                    
                    }
                    
                    continue;
                }
                
                content = null;
                if( rc.getRcCheckStatusType().getIsComplete() || forceForAnyStatus )
                {
                    emf = ResultFormatterFactory.getRcResultEmailFormatter(rc);
                    content = emf.getResultEmailContent(rc, l);
                }
                
                if( content == null )
                {
                    content = MessageFactory.getStringMessage(l, key + ".content", params );
                    content = wrapEmailContent( content, l );
                }
                
                content = EmailUtils.addNoReplyMessage(content, true, l );
                          
                emailMap.put( EmailConstants.SUBJECT, subject );
                emailMap.put( EmailConstants.CONTENT, content );
                emailMap.put( EmailConstants.TO, em );            
                sent = emailUtils.sendEmail( emailMap );
                if( sent )
                {
                    ct++;
                    if( ru!=null )
                    {
                        String identifier = "RC_" + rc.getRcCheckId() + "_STATUS_UPDATE_" + (new Date()).getTime();
                        if( userFacade == null )
                            userFacade = UserFacade.getInstance();
                        userFacade.saveMessageAction(rc.getAdminUserId(), // rc.getAdminUserId(), 
                                                        ru, 
                                                        subject, 
                                                        UserActionType.SENT_EMAIL.getUserActionTypeId(), 
                                                        0, // intParam1
                                                        rc.getRcCheckId(), // longparam1
                                                        0, // longparam2 (sourceCode)
                                                        0, // longparam4 
                                                        identifier, 
                                                        null, 
                                                        em );                    
                    }                    
                }
            }
           
            return ct;                
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseRefUtils.sendProgressUpdateEmailForRaterComplete()  rcCheckId=" + rc.getRcCheckId()  );
            return 0;
        }
    }

    
    private int sendProgressUpdateSmsForRaterOrCandidateComplete( RcCheck rc, RcRater rater, Locale l, String[] params, String adminUserCountryCode) throws Exception
    {
        try
        {
            if( rc==null || rc.getTextResultsTo()==null || rc.getTextResultsTo().isBlank() )
                return 0;
            
            if( rater!=null && rater.getIsCandidateOrEmployee() )
                rater=null;
            
            // rater is not null, must be a 
            //if( rater !=null && rater.getLastProgressMsgDate()!=null  )
            //    return 0;
            
            // if rater is null, then this must be a candidate completion.
            if( rater==null && !rc.getRcCheckStatusType().getCompleteOrHigher() && !rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
                return 0;
                        
            List<String> toList = new ArrayList<>();
            boolean smsOk;
            
            
            for( String ph : rc.getTextResultsTo().split(",") )
            {
                if( ph.isBlank() )
                    continue;
                                
                if( !GooglePhoneUtils.isNumberValid(ph, adminUserCountryCode) )
                    continue;
                
                smsOk = GooglePhoneUtils.getIsPhoneNumberAllowedForSms( ph, rc.getOrg(), null, rc.getAdminUser() );
                if( !smsOk )
                {
                    LogService.logIt("RcMessageUtils.sendProgressUpdateSmsForRaterOrCandidateComplete() testing to international number for this org is not allowed. rcCheckId=" + rc.getRcCheckId() + ", phone=" + ph );
                    continue;                        
                }
                
                
                ph = ph.toLowerCase();
                
                if( toList.contains(ph) )
                    continue;
                
                toList.add(ph );
            }
            
            if( toList.isEmpty() )
                return 0;
            
            //if( rc.getLocale()==null)
            //    rc.setLocale(getLocale() );
            
            String key;
            
            RcDistributionType rcDistType = rc.getRcDistributionType();            
            if( rc.getRcCheckType().getIsPrehire() )
            {
                if( rc.getRcCheckStatusType().getIsComplete() )
                    key = "g.RCSmsHireProgAllComplete";
                
                else if( rcDistType.getIsEachRating() )
                {
                    if( rater==null )
                    {
                        if( rc.getRcCandidateStatusType().getIsRejection() )
                            key = "g.RCSmsHireProgCandidateRejection";
                        else
                            key = "g.RCSmsHireProgCandidateComplete";
                    }
                    else
                    {
                        if( rater.getRcRaterStatusType().getIsRejection() )
                            key = "g.RCSmsHireProgRaterRejection";
                        else
                            key = "g.RCSmsHireProgRaterComplete";
                    }
                }
                else if( rcDistType.getIsPartialProgress())
                    key = "g.RCSmsHireProgPartial";
                else 
                    return 0;
            }
            
            else
            {
                if( rc.getRcCheckStatusType().getIsComplete() )
                    key = "g.RCSmsEmpFbkProgAllComplete";
                else if( rcDistType.getIsEachRating() )
                {
                    if( rater==null )
                    {
                        if( rc.getRcCandidateStatusType().getIsRejection() )
                            key = "g.RCSmsHireProgCandidateRejection";
                        else
                            key = "g.RCSmsEmpFbkProgCandidateComplete";
                    }
                    else
                    {
                        if( rater.getRcRaterStatusType().getIsRejection() )
                            key = "g.RCSmsHireProgRaterRejection";
                        else
                            key = "g.RCSmsEmpFbkProgRaterComplete";
                    }
                }
                else if( rcDistType.getIsPartialProgress())
                    key = "g.RCSmsEmpFbkProgPartial";
                else 
                    return 0;
            }
            
            String content = MessageFactory.getStringMessage( l, key, params );
            
            int sent = 0;
            int c;
            for( String ph : toList )
            {
                c = PhoneUtils.sendTextMessage(ph, adminUserCountryCode, null, null, content); 
                sent += c;
                if( c>0 )
                    Tracker.addTextMessageSent();                            
            }                  
            return sent;            
        }        
        catch( com.twilio.exception.ApiException e )
        {
            LogService.logIt( "RcMessageUtils.sendProgressUpdateSmsForRaterComplete() NONFATAL " + e.toString() + ", rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + (rater==null ? "null" : rater.getRcRaterId()) );
            return 0;
        }
        catch( STException e )
        {
            LogService.logIt( "RcMessageUtils.sendProgressUpdateSmsForRaterComplete() NONFATAL " + e.toString() + ", rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + (rater==null ? "null" : rater.getRcRaterId()) );
            return 0;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcMessageUtils.sendProgressUpdateSmsForRaterComplete() rcCheckId=" + rc.getRcCheckId() );
            return 0;
        }
    }
    
    
    
    
    
    public static String[] getMessageParams( User adminUser, RcCheck rc, RcRater rater, Locale locale, boolean reminder )
    {
        if( locale==null )
            locale = rc.getLocale();
        if( locale==null )
            locale = Locale.US;
        
        String[] out = new String[28];        
        out[0] = adminUser.getFullname();
        out[1] = rc.getOrg().getName();
        out[2] = rc.getUser().getFullname();
        out[3] = rc.getJobTitle();
        out[4] = rc.getCandidateStartUrl();
        out[5] = reminder ? MessageFactory.getStringMessage( locale, "g.REMINDERC" ) + " " : "";
        
        out[6] = getRcCheckTypeName( rc );
        out[7] = Float.toString( rc.getPercentComplete() );
        out[8] = Float.toString( rc.getOverallScore() );
        out[9] = rc.getResultsViewUrl();
        
        out[10] = adminUser==null ? null : adminUser.getFullname(); // SAVE this for use of the name/email of the recipient for results.
        out[20] = "<a href=\"" + rc.getCandidateStartUrl() + "\">" + rc.getCandidateStartUrl() + "</a>";
        
        if( rater!=null )
        {
           if( rater.getOrgId()<=0 )
               rater.setOrgId( rc.getOrgId() );
           out[15] = rater.getUser().getFullname();
           out[16] = rater.getRaterStartUrl();
           out[21] = "<a href=\"" + rater.getRaterStartUrl() + "\">" + rater.getRaterStartUrl() + "</a>";
           
           if( rater.getRcRaterStatusType().getCompleteOrHigher() )
               out[18] = Float.toString( rater.getOverallScore() );
        }
        
        // out[20] - out[25] are custom slots.
        out[26]=RuntimeConstants.getStringValue("baseadmindomain");
        out[27]=RuntimeConstants.getStringValue("default-site-name");
        
        
        return out;
    }
        
    protected static String wrapEmailContent( String html, Locale locale )
    {
        // String baseUrl = RuntimeConstants.getStringValue( "baseprotocol" ) + "://" + RuntimeConstants.getStringValue( "basedomain" ) + "/" + RuntimeConstants.getStringValue( "basewebapp" );
        String iconUrl = "https://" + RuntimeConstants.getStringValue("baseadmindomain") + "/ta/images/ref-check-icon-white-54.png"; 
        String logoUrl = RuntimeConstants.getStringValue("baselogourlwhite"); //  
        
        String[] params = new String[]{iconUrl, logoUrl};
        
        return MessageFactory.getStringMessage(locale, "g.LVEmWrapTop", params ) + html + MessageFactory.getStringMessage(locale, "g.LVEmWrapBottom");
    }

    protected static String getRcCheckTypeName( RcCheck rc )
    {
        if( rc.getRcCheckType().getIsPrehire() )
            return RcCheckType.PREHIRE.getName( rc.getLocale() );
        
        return RcCheckType.EMPLOYEE_FBK.getName( rc.getLocale() );        
    }
    
    
}
