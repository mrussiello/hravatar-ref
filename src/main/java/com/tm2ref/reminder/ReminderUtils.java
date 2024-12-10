/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.reminder;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.ref.CandidateRefUtils;
import com.tm2ref.ref.RcCheckUtils;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.ref.RcMessageUtils;
import com.tm2ref.ref.RcReminderType;
import com.tm2ref.ref.RcScriptFacade;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.UserFacade;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class ReminderUtils 
{
    RcFacade rcFacade;
    RcScriptFacade rcScriptFacade;
    RcReminderFacade rcReminderFacade;
    UserFacade userFacade;
    
    RcMessageUtils rcMessageUtils = null;
    
    public void doReminderBatch() throws Exception
    {
        Tracker.addReminderBatch();
        int[] count = new int[2];
        int[] sent;
        try
        {
            sent = sendCandidateReminders();
            if( sent[0]>0 )
                count[0] += sent[0];
            if( sent[1]>0 )
                count[1] += sent[1];
            
            sent = sendRaterReminders();
            if( sent[0]>0 )
                count[0] += sent[0];
            if( sent[1]>0 )
                count[1] += sent[1];
            
            LogService.logIt( "ReminderUtils.doReminderBatch() COMPLETE sent " + count[0] + " emails and " + count[1] + " text messages." );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.doReminderBatch() " );                        
        }
    }

    public void doDelayedInvitationBatch() throws Exception
    {
        int[] count = new int[2];
        int[] sent;
        try
        {
            sent = sendRaterDelayedInvitations();
            if( sent[0]>0 )
                count[0] += sent[0];
            if( sent[1]>0 )
                count[1] += sent[1];
            
            LogService.logIt( "ReminderUtils.doDelayedInvitationBatch() COMPLETE sent " + count[0] + " emails and " + count[1] + " text messages." );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.doDelayedInvitationBatch() " );                        
        }
    }

    
    public int[] sendCandidateReminders() throws Exception
    {
        if( rcReminderFacade==null )
            rcReminderFacade = RcReminderFacade.getInstance();


        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.HOUR, -24 );
        Date oneDayBefore = cal.getTime();        
        cal.add( Calendar.HOUR, -24 );
        Date twoDaysBefore = cal.getTime();
        cal.add( Calendar.HOUR, -24 );
        Date threeDaysBefore = cal.getTime();
        cal.add( Calendar.HOUR, -24 );
        Date fourDaysBefore = cal.getTime();
        
        RcReminderType rcReminderType;
        
        
        List<Long> idList = rcReminderFacade.getRcCheckIdsNeedingCandidateReminder(); 
        if( !idList.isEmpty() )
            LogService.logIt( "ReminderUtils.sendCandidateReminders() START RcCheckIds found: " + idList.size() );
        RcCheck rc;
        int[] count = new int[2];
        int[] sent;
        for( Long id : idList )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rc = rcFacade.getRcCheck( id, true );
            
            rcReminderType = RcReminderType.getValue( rc.getReminderTypeId());

            if( rc.getRcCandidateStatusType().getIsCompletedOrHigher() )
            {
                LogService.logIt( "ReminderUtils.sendCandidateReminders() Skipping candidate has completed. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }            
            
            // Candidate has started within the last day.
            if( rc.getRcCandidateStatusType().getIsStartedOrHigher() && rc.getCandidateStartDate().after(oneDayBefore) )
            {
                LogService.logIt( "ReminderUtils.sendCandidateReminders() Skipping candidate has started. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }            
                
            
            if( rc.getSendDate()==null || rc.getLastCandidateSendDate()==null )
            {
                LogService.logIt( "ReminderUtils.sendCandidateReminders() Skipping candidate send because send date is null. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }            
            
            // no reminders until one day after candidate send date.
            if(  rc.getLastCandidateSendDate().after(oneDayBefore))
            {
                LogService.logIt( "ReminderUtils.sendCandidateReminders() Skipping candidate send because send date within 24 hours. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }

            // no more than one reminder a day
            if( rc.getLastCandidateReminderDate()!=null && rc.getLastCandidateReminderDate().after(oneDayBefore))
            {
                LogService.logIt( "ReminderUtils.sendCandidateReminders() Skipping candidate send because last candidate reminder date within 24 hours. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }
            // Only send once - in 24 hours. Send if the send date was between 1 and 2 days ago
            if( rcReminderType.getMaxDaysSinceSend()==1 && rc.getSendDate().before( twoDaysBefore ) )
            {
                LogService.logIt( "ReminderUtils.sendCandidateReminders() Skipping candidate send because send date is outside the window for reminder type (1-2 days). rcCheckId=" + rc.getRcCheckId() );
                continue;                
            }
            // Only send twice - 24 hours & 48 hours. Send if the send date was between 1 and 3 days ago
            else if( rcReminderType.getMaxDaysSinceSend()==2 && rc.getSendDate().before( threeDaysBefore ) )
            {
                LogService.logIt( "ReminderUtils.sendCandidateReminders() Skipping candidate send because send date is outside the window for reminder type (1-3 days). rcCheckId=" + rc.getRcCheckId() );
                continue;                
            }
            // Only send 3 times - 24 hours, 48, 72 hours hours. Send if the send date was between 1 and 4 days ago
            else if( rcReminderType.getMaxDaysSinceSend()==3 && rc.getSendDate().before( fourDaysBefore ) )
            {
                LogService.logIt( "ReminderUtils.sendCandidateReminders() Skipping candidate send because send date is outside the window for reminder type (1-4 days). rcCheckId=" + rc.getRcCheckId() );
                continue;                
            }
            
            sent = sendCandidateReminder( rc );
            
            if( sent[0]>0 || sent[1]>0 )
                Tracker.addReminderCandidate();

            if( sent[0]>0 )
            {
                count[0] += sent[0];
                Tracker.addReminderEmail();
            }
            if( sent[1]>0 )
            {
                count[1] += sent[1];
                Tracker.addReminderText();
            }
        }
        
        if( !idList.isEmpty() )
            LogService.logIt( "ReminderUtils.sendCandidateReminders() COMPLETE idList=" + idList.size() + ", sent " + count[0] + " emails and " + count[1] + " text messages." );
        return count;
    }

    public int[] sendRaterReminders() throws Exception
    {
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.HOUR, -24 );
        Date oneDayBefore = cal.getTime();        
        cal.add( Calendar.HOUR, -24 );
        Date twoDaysBefore = cal.getTime();
        cal.add( Calendar.HOUR, -24 );
        Date threeDaysBefore = cal.getTime();
        cal.add( Calendar.HOUR, -24 );
        Date fourDaysBefore = cal.getTime();
        
        RcReminderType rcReminderType;
        
        
        if( rcReminderFacade==null )
            rcReminderFacade = RcReminderFacade.getInstance();

        List<Long> idList = rcReminderFacade.getRcRaterIdsNeedingReminder();        
        LogService.logIt( "ReminderUtils.sendRaterReminders() START RcRaterIds found: " + idList.size() );
        int[] count = new int[2];
        int[] sent;
        RcRater rater;
        RcCheck rc = null;
        for( Long id : idList )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rater = rcFacade.getRcRater( id, true );
            if( rc==null || rc.getRcCheckId()!=rater.getRcCheckId() )
                rc = rcFacade.getRcCheck( rater.getRcCheckId(), true );
            
            rcReminderType = RcReminderType.getValue( rc.getReminderTypeId());

            if( rater.getRcRaterStatusType().getCompleteOrHigher())
            {
                LogService.logIt( "ReminderUtils.sendRaterReminders() Skipping rater has completed. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }   
            
            // Candidate has acted within the last day.
            if( rater.getRcRaterStatusType().getStartedOrHigher() && rater.getLastUpdate()!=null && rater.getLastUpdate().after(oneDayBefore) )
            {
                LogService.logIt( "ReminderUtils.sendRaterReminders() Skipping rater has started. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }            
            
            if( rc.getRaterSendDelayTypeId()>0 && rc.getCandidateRatingsCompleteDate()==null )
            {
                LogService.logIt( "ReminderUtils.sendRaterReminders() Skipping rater because RcCheck set to not send until Candidate Ratings Completed and they are not complete. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }
            
            if( rater.getSendDate()==null )
            {
                LogService.logIt( "ReminderUtils.sendRaterReminders() Skipping rater send because send date is null. Never sent. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }            
            
            // no reminders until one day after send date.
            if(  rater.getSendDate().after(oneDayBefore))
            {
                LogService.logIt( "ReminderUtils.sendRaterReminders() Skipping rater send because send date within 24 hours. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }

            // no more than one reminder a day
            if( rater.getLastReminderDate()!=null && rater.getLastReminderDate().after(oneDayBefore))
            {
                LogService.logIt( "ReminderUtils.sendRaterReminders() Skipping rater send because last candidate reminder date within 24 hours. rcCheckId=" + rc.getRcCheckId() );
                continue;
            }
            // Only send once - in 24 hours. Send if the send date was between 1 and 2 days ago
            if( rcReminderType.getMaxDaysSinceSend()==1 && rater.getSendDate().before( twoDaysBefore ) )
            {
                LogService.logIt( "ReminderUtils.sendRaterReminders() Skipping rater send because send date is outside the window for reminder type (1-2 days). rcCheckId=" + rc.getRcCheckId() );
                continue;                
            }
            // Only send twice - 24 hours & 48 hours. Send if the send date was between 1 and 3 days ago
            else if( rcReminderType.getMaxDaysSinceSend()==2 && rater.getSendDate().before( threeDaysBefore ) )
            {
                LogService.logIt( "ReminderUtils.sendRaterReminders() Skipping rater send because send date is outside the window for reminder type (1-3 days). rcCheckId=" + rc.getRcCheckId() );
                continue;                
            }
            // Only send 3 times - 24 hours, 48, 72 hours hours. Send if the send date was between 1 and 4 days ago
            else if( rcReminderType.getMaxDaysSinceSend()==3 && rater.getSendDate().before( fourDaysBefore ) )
            {
                LogService.logIt( "ReminderUtils.sendRaterReminders() Skipping rater send because send date is outside the window for reminder type (1-4 days). rcCheckId=" + rc.getRcCheckId() );
                continue;                
            }
            
            sent = sendRaterReminder( rc, rater );

            if( sent[0]>0 || sent[1]>0 )
                Tracker.addReminderRater();

            if( sent[0]>0 )
            {
                count[0] += sent[0];
                Tracker.addReminderEmail();
            }
            if( sent[1]>0 )
            {
                count[1] += sent[1];
                Tracker.addReminderText();
            }
        }
        LogService.logIt( "ReminderUtils.sendRaterReminders() COMPLETE idList=" + idList.size() + ", sent " + count[0] + " emails and " + count[1] + " text messages." );
        return count;
    }
    
    public int[] sendRaterDelayedInvitations()throws Exception
    {
        if( rcReminderFacade==null )
            rcReminderFacade = RcReminderFacade.getInstance();

        List<Long> idList = rcReminderFacade.getRcRaterIdsNeedingDelayedInvitationSends();        
        LogService.logIt( "ReminderUtils.sendRaterDelayedInvitations() START RcRaterIds found: " + idList.size() );
        int[] count = new int[2];
        int[] sent;
        RcRater rater;
        RcCheck rc = null;
        
        int errorCount = 0;
        for( Long id : idList )
        {
            try
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rater = rcFacade.getRcRater( id, true );
                
                if( rater.getIsCandidateOrEmployee() )
                {
                    LogService.logIt( "ReminderUtils.sendRaterDelayedInvitations() Skipping rater this rater is the Candidate or Employee. rcRaterId=" + rater.getRcRaterId() );
                    continue;
                }
                
                if( rc==null || rc.getRcCheckId()!=rater.getRcCheckId() )
                    rc = rcFacade.getRcCheck( rater.getRcCheckId(), true );

                // set for delayed send after media
                if( rc.getRaterSendDelayTypeId()!=10 )
                    continue;

                if( rc.getRaterSendDelayTypeId()>0 && rc.getCandidateRatingsCompleteDate()==null )
                {
                    LogService.logIt( "ReminderUtils.sendRaterDelayedInvitations() Skipping rater because RcCheck set to not send until Candidate Ratings Completed and they are not complete. rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rater.getRcRaterId() );
                    continue;
                }

                if( rater.getRcRaterStatusType().getCompleteOrHigher())
                {
                    LogService.logIt( "ReminderUtils.sendRaterDelayedInvitations() Skipping rater has completed. rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rater.getRcRaterId() );
                    continue;
                }   

                if( RcCheckUtils.hasUnconvertedCandidateMediaForRaterReview(rc, rcFacade) )
                {
                    LogService.logIt( "ReminderUtils.sendRaterDelayedInvitations() Skipping. Candidate Rater still has media in conversion to be reviewed. rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rater.getRcRaterId() );
                    continue;
                }

                if( rater.getSendDate()!=null )
                {
                    LogService.logIt( "ReminderUtils.sendRaterDelayedInvitations() Skipping rater send because send date is NOT null. Invitation already sent. rcCheckId=" + rc.getRcCheckId() + ", rcRaterId=" + rater.getRcRaterId() );
                    continue;
                }            

                sent = sendRcCheckInvitationToRater( rc, rater );

                if( sent[0]>0 || sent[1]>0 )
                    Tracker.addDelayedInvitationRater();

                if( sent[0]>0 )
                {
                    count[0] += sent[0];
                    Tracker.addDelayedInvitationEmailRater();
                }
                if( sent[1]>0 )
                {
                    count[1] += sent[1];
                    Tracker.addDelayedInvitationTextRater();
                }
            }
            catch( Exception e )
            {
                errorCount++;
                LogService.logIt( e, "ReminderUtils.sendRaterDelayedInvitations() Processing rcRaterId=" + id + ", errorCount=" + errorCount );
                if( errorCount>20)
                    break;
            }
        }
        LogService.logIt( "ReminderUtils.sendRaterDelayedInvitations() COMPLETE idList=" + idList.size() + ", sent " + count[0] + " emails and " + count[1] + " text messages." );
        return count;        
    }

    
    /*
     data[0] = email sent count
     data[1] = text sent count

    */
    protected int[] sendRcCheckInvitationToRater( RcCheck rc, RcRater rater ) throws Exception
    {
        if( rc==null )
            throw new Exception( "rcCheck is null" );
        if( rater==null )
            throw new Exception( "rater is null");
        if( userFacade == null )
            userFacade = UserFacade.getInstance();
        User user = rater.getUser();

        if( user==null )
        {
            user = userFacade.getUser( rater.getUserId());
            rater.setUser(user);
        }

        if( user==null )
            throw new Exception( "CandidateRefUtils.sendRcCheckToRater() user is null. rcCheckId=" + rc.getRcCheckId() );

        if( rc.getAdminUser()==null )
            rc.setAdminUser( userFacade.getUser( rc.getAdminUserId() ) );
        if( rc.getLocale()==null && rc.getLangCode()!=null )
            rc.setLocale( I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() ));

        if( rc.getRcOrgPrefs()==null )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rc.setRcOrgPrefs( rcFacade.getRcOrgPrefsForOrgId( rc.getOrgId() ));
        }

        if( rc.getSuborgId()>0 && rc.getRcSuborgPrefs()==null )
        {
            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            rc.setRcSuborgPrefs( rcFacade.getRcSuborgPrefsForSuborgId( rc.getSuborgId() ));
        }

        RcMessageUtils rcmu = new RcMessageUtils();
        int[] sent = rcmu.sendRcCheckToRater(rc, rater, rc.getRcOrgPrefs(), 1, rc.getUserId(), true, false );

        return sent;
    }
    
    
    
    /**
     * returns 
     *     out[0]=emails
     *     out[1]=texts
     * 
     * 
     */
    public int[] sendCandidateReminder( RcCheck rc )
    {
        if( rc==null || rc.getRcCandidateStatusType().getIsStartedOrHigher() || rc.getRcCheckStatusType().getCompleteOrHigher() )
            return new int[2];
        try
        {
            if( rc.getUser()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                rc.setUser( userFacade.getUser( rc.getUserId() ));
            }
            if( rc.getOrg()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                rc.setOrg( userFacade.getOrg( (int) rc.getOrgId() ));
            }
            if( rc.getAdminUser()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                rc.setAdminUser( userFacade.getUser( rc.getAdminUserId()));
            }
            if( rc.getLocale()==null && rc.getLangCode()!=null && !rc.getLangCode().isBlank() )
                rc.setLocale( I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() ));
            
            if( rcMessageUtils == null )
                rcMessageUtils = new RcMessageUtils();
            
            if( rc.getRcOrgPrefs()==null )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rc.setRcOrgPrefs( rcFacade.getRcOrgPrefsForOrgId( rc.getOrgId() ));
            }
            if( rc.getSuborgId()>0 && rc.getRcSuborgPrefs()==null )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rc.setRcSuborgPrefs( rcFacade.getRcSuborgPrefsForSuborgId( rc.getSuborgId() ));            
            }
                        
            return rcMessageUtils.sendRcCheckReminderToCandidate(rc, rc.getRcOrgPrefs(), 3 );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.sendCandidateReminder() sending rcCheckId=" + rc.getRcCheckId() );            
        }
        return new int[2];
    }

    /**
     * returns 
     *     out[0]=emails
     *     out[1]=texts
     * 
     */
    public int[] sendRaterReminder( RcCheck rc, RcRater rater )
    {
        int[] count = new int[2];
        if( rc==null  || rc.getRcCheckStatusType().getCompleteOrHigher() || rater==null || rater.getRcRaterStatusType().getStartedOrHigher() )
            return count;
        try
        {
            if( rc.getOrg()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                rc.setOrg( userFacade.getOrg( rc.getOrgId()));
            }
            if( rc.getUser()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                rc.setUser( userFacade.getUser( rc.getUserId() ));
            }
            if( rc.getAdminUser()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                rc.setAdminUser( userFacade.getUser( rc.getAdminUserId()));
            }
            if( rater.getUser()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                rater.setUser( userFacade.getUser( rater.getUserId()));                
            }

            if( rater.getIsCandidateOrEmployee() )
                return count;
            
            if( rc.getLocale()==null && rc.getLangCode()!=null && !rc.getLangCode().isBlank() )
                rc.setLocale( I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() ));
            
            if( rcMessageUtils == null )
                rcMessageUtils = new RcMessageUtils();
            
            if( rc.getRcOrgPrefs()==null )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rc.setRcOrgPrefs( rcFacade.getRcOrgPrefsForOrgId( rc.getOrgId() ));
            }
            if( rc.getSuborgId()>0 && rc.getRcSuborgPrefs()==null )
            {
                if( rcFacade==null )
                    rcFacade=RcFacade.getInstance();
                rc.setRcSuborgPrefs( rcFacade.getRcSuborgPrefsForSuborgId( rc.getSuborgId() ));            
            }
            
            if( rc.getRcScript()==null )
            {
                if( rcScriptFacade==null )
                    rcScriptFacade=RcScriptFacade.getInstance();
                
                rc.setRcScript(rcScriptFacade.getRcScript(rc.getRcScriptId(), true));
            }
            
            
            return rcMessageUtils.sendRcCheckToRater( rc, rater, rc.getRcOrgPrefs(), 3, 0, false, true );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.sendRaterReminder() sending rcRaterId=" + rater.getRcRaterId() );            
        }
        return count;
    }

}
