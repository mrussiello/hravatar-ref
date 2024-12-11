package com.tm2ref.service;

import com.tm2ref.global.RuntimeConstants;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class Tracker
{
    public static Date startDate = null;

    private static int errors = 0;
    private static int facesErrors = 0;
    
    private static int logonCount = 0;

    private static int logoutCount = 0;
    
    private static int simpleEntries = 0;
    private static int servletEntries = 0;
    private static int testKeyEntries = 0;
    private static int candidateEntries = 0;
    private static int raterEntries = 0;
    private static int raterCreations = 0;
    private static int itemResponses = 0;
    private static int itemSkips = 0;
    private static int itemAnswers = 0;
    private static int candidateCompletes = 0;
    private static int raterCompletes = 0;
    private static int rcCheckCompletes = 0;
    private static int rcSelfReferrals = 0;
    private static int rcExtraReferrals = 0;
    
    private static int emailsSent = 0;
    private static int textMessagesSent = 0;
    
    private static int reminderBatches = 0;
    private static int reminderCandidates = 0;
    private static int reminderRaters = 0;
    private static int reminderEmailsSent = 0;
    private static int reminderTextMessages = 0;
    
    private static int delayedRaterInvitations=0;
    private static int delayedRaterInvitationEmails=0;
    private static int delayedRaterInvitationTextMessages=0;
    
    private static int rcLogErrorMessages = 0;
    private static int rcLogWarningMessages = 0;
    private static int rcLogInfoMessages = 0;
    private static int imageFileUploads = 0;
    private static int imageFileUploadErrors = 0;
    private static int mediaFileUploads = 0;
    private static int mediaFileUploadErrors = 0;
    private static int candidateFileUploadErrors = 0;
    private static int candidateFileUploads = 0;

    
    public static void addSelfReferral()
    {
        rcSelfReferrals++;
    }

    public static void addExtraReferral()
    {
        rcExtraReferrals++;
    }

    public static void addCandidateFileUpload()
    {
        candidateFileUploads++;
    }
    
    public static void addMediaFileUpload()
    {
        mediaFileUploads++;
    }
    public static void addImageFileUpload()
    {
        imageFileUploads++;
    }
    public static void addCandidateFileUploadError()
    {
        candidateFileUploadErrors++;
    }
    public static void addImageFileUploadError()
    {
        imageFileUploadErrors++;
    }
    public static void addMediaFileUploadError()
    {
        mediaFileUploadErrors++;
    }
    
    public static void addRcLogErrorMessage()
    {
        rcLogErrorMessages++;
    }
    public static void addRcLogWarningMessage()
    {
        rcLogWarningMessages++;
    }
    public static void addRcLogInfoMessage()
    {
        rcLogInfoMessages++;
    }


    public static void addReminderBatch()
    {
        reminderBatches++;
    }
    public static void addReminderCandidate()
    {
        reminderCandidates++;
    }
    public static void addReminderRater()
    {
        reminderRaters++;
    }
    public static void addReminderEmail()
    {
        reminderEmailsSent++;
    }
    public static void addReminderText()
    {
        reminderTextMessages++;
    }
    
    public static void addDelayedInvitationRater()
    {
        delayedRaterInvitations++;
    }
    public static void addDelayedInvitationEmailRater()
    {
        delayedRaterInvitationEmails++;
    }
    public static void addDelayedInvitationTextRater()
    {
        delayedRaterInvitationTextMessages++;
    }


    
    public static void addTestKeyEntry()
    {
        testKeyEntries++;
    }
    


    
    

    public static void addEmailSent()
    {
        emailsSent++;
    }
    public static void addTextMessageSent()
    {
        textMessagesSent++;
    }

    public static void addSimpleEntry()
    {
        simpleEntries++;
    }
    public static void addServletEntry()
    {
        servletEntries++;
    }
    public static void addCandidateEntry()
    {
        candidateEntries++;
    }
    public static void addRaterEntry()
    {
        raterEntries++;
    }
    public static void addRaterCreation()
    {
        raterCreations++;
    }
    public static void addItemResponse()
    {
        itemResponses++;
    }
    public static void addItemSkip()
    {
        itemSkips++;
    }
    public static void addItemAnswer()
    {
        itemAnswers++;
    }
    public static void addCandidateComplete()
    {
        candidateCompletes++;
    }
    public static void addRaterComplete()
    {
        raterCompletes++;
    }
    public static void addRcCheckComplete()
    {
        rcCheckCompletes++;
    }
    
    


    public static void addError()
    {
        errors++;
    }


    public static void addFacesError()
    {
        facesErrors++;
    }

    public static void addLogon()
    {
        logonCount++;
    }

    public static void addLogout()
    {
        logoutCount++;
    }

    
    

    public static Map<String, Object> getStatusMap()
    {
        Map<String, Object> outMap = new TreeMap<>();

        outMap.put( "AA NEW STARTS: ", RuntimeConstants.getBooleanValue("newRefStartsOK") ? "ON" : "OFF" );
        
        outMap.put( "ERRORS: ", ((int) errors ) );
        outMap.put( "ERRORS: Faces", ((int) facesErrors ) );
        outMap.put( "ERRORS: Candidate File Uploads", ((int) candidateFileUploadErrors ) );
        outMap.put( "ERRORS: Image File Uploads", ((int) imageFileUploadErrors ) );
        outMap.put( "ERRORS: Media File Uploads", ((int) mediaFileUploadErrors ) );

        outMap.put( "REF: Simple Entries", ((int) simpleEntries ) );
        outMap.put( "REF: Servlet Entries", ((int) servletEntries ) );
        outMap.put( "REF: TestKey Entries", ((int) testKeyEntries ) );
        
        outMap.put( "REF: Candidate Starts", ((int) candidateEntries ) );
        outMap.put( "REF: Rater Starts", ((int) raterEntries ) );
        outMap.put( "REF: New Raters Created", ((int) raterCreations ) );
        outMap.put( "REF: Rating Responses", ((int) itemResponses ) );
        outMap.put( "REF: Ratings Answered", ((int) itemAnswers ) );
        outMap.put( "REF: Ratings Skipped", ((int) itemSkips ) );
        outMap.put( "REF: Ratings Self-Referrals", ((int) rcSelfReferrals ) );
        outMap.put( "REF: Ratings Extra-Referrals", ((int) rcExtraReferrals ) );
        outMap.put( "REF: Candidate Completes", ((int) candidateCompletes ) );
        outMap.put( "REF: Rater Completes", ((int) raterCompletes ) );
        outMap.put( "REF: RC Check Completes", ((int) rcCheckCompletes ) );

        outMap.put( "REF: Remote Log Error Messages", ((int) rcLogErrorMessages ) );
        outMap.put( "REF: Remote Log Warning Messages", ((int) rcLogWarningMessages ) );
        outMap.put( "REF: Remote Log Info Messages", ((int) rcLogInfoMessages ) );

        outMap.put( "FILE: Candidate File Uploads", ((int) candidateFileUploads ) );
        outMap.put( "FILE: Image File Uploads", ((int) imageFileUploads ) );
        outMap.put( "FILE: Media File Uploads", ((int) mediaFileUploads ) );
                
        outMap.put( "MSG: Emails Sent", ((int) emailsSent ) );
        outMap.put( "MSG: Text Messages Sent", ((int) textMessagesSent ) );
        
        outMap.put( "REMINDER: Batches", ((int) reminderBatches ) );
        outMap.put( "REMINDER: Candidate Sends", ((int) reminderCandidates ) );
        outMap.put( "REMINDER: Rater Sends", ((int) reminderRaters ) );
        outMap.put( "REMINDER: Emails Sent", ((int) reminderEmailsSent ) );
        outMap.put( "REMINDER: Text Messages Sent", ((int) reminderTextMessages ) );
        
        outMap.put( "INVITATION: Delayed Rater Invitations", ((int)delayedRaterInvitations));
        outMap.put( "INVITATION: Delayed Rater Invitation Emails", ((int)delayedRaterInvitationEmails));
        outMap.put( "INVITATION: Delayed Rater Invitations Texts", ((int)delayedRaterInvitationTextMessages));
        
        outMap.put( "USER: Admin Logons", ((int) logonCount ) );
        outMap.put( "USER: Admin Logouts", ((int) logoutCount ) );

        return outMap;
    }

}
