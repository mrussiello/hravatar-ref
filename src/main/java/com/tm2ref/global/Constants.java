package com.tm2ref.global;

import com.itextpdf.text.BaseColor;

public class Constants
{
    // Countries where texting is allowed. To get name of country use cntry.PY.
    public static String[] SMS_OK_COUNTRIES = new String[] {"AU","CA","GB","MX","PE", "SG","US","UK","ZA"};
    public static int[] SMS_OK_COUNTRY_CODES = new int[] {61,1,44,52,51,65,1,44,27};

    public final static int YES = 1;

    public final static String DEFAULT_RESOURCE_BUNDLE = "com.tm2ref.resources.TM2Messages";

    public static int MAX_SESSIONLISTENER_DURATION = 600;

    public static int IDLE_SESSION_TIMEOUT_MINS = 20;

    public static final String SERVER_START_LOG_MARKER = "*************************************** SERVER START ******************************************";

    public final static String SYSTEM_SESSION_COUNTER = "tm2refSystemSessionCounter";

    public static String DUMMY_PASSWORD = "******";

    public static int MIN_PASSWORD_LENGTH = 8;
    public static int MAX_PASSWORD_AGE_MONTHS = 12;

    // public static int MAX_COOKIE_AGE = 15552000;

    // public static int TESTKEY_LENGTH = 12;
    public static int MAX_FAILED_LOGON_ATTEMPTS  = 5;

    public static int LOGON_LOCKOUT_MINUTES = 30;

    public static final int MAX_DAYS_PREV_TESTEVENT = 90;

    public static final int REFERENCE_CHECK_LEGACY_CREDITS = 40;

    public static final int DEFAULT_SCORE_PRECISION_DIGITS = 1;

    public static final int DEFAULT_INTERVIEW_QUESTIONS = 5;

    public static final int DEFAULT_HILOWCOMPETENCIES = 3;

    public static final int DEFAULT_MAX_RATERS = 10;

    public static final int MAX_PHOTO_UPLOAD_ATTEMPTS = 4;

    public static final int PROCTOR_MAX_FACEMATCH_TRIES = 4;
    public static final float PROCTOR_MIN_FACE_CONFIDENCE = 0.5f;
    public static final float PROCTOR_MIN_ID_FACE_CONFIDENCE = 0.5f;
    public static final float PROCTOR_MIN_FACE_MATCH_CONFIDENCE = 0.5f;
    public static final float PROCTOR_MIN_FACE_MATCH_PERCENT = 35f;
    public static final float PROCTOR_MIN_IDCARD_CONFIDENCE = 40f;

    public static BaseColor LOW_COMP_COLOR_OTHERS = new BaseColor(220,20,60);
    public static BaseColor LOW_COMP_COLOR_SELF = new BaseColor(240,128,128);

    public static BaseColor MED_COMP_COLOR_OTHERS = new BaseColor(0,128,255);
    public static BaseColor MED_COMP_COLOR_SELF = new BaseColor(153,204,255);

    public static BaseColor HIGH_COMP_COLOR_OTHERS = new BaseColor(60,179,113);
    public static BaseColor HIGH_COMP_COLOR_SELF = new BaseColor(143,188,143);


    public static String LOW_COMP_RGBCOLOR_OTHERS = "#DC143C";
    public static String LOW_COMP_RGBCOLOR_SELF = "#F08080";

    public static String MED_COMP_RGBCOLOR_OTHERS = "#0080ff"; // "#FFFF00";
    public static String MED_COMP_RGBCOLOR_SELF = "#99ccff";// "#FFFFBB";

    public static String HIGH_COMP_RGBCOLOR_OTHERS = "#3CB371";
    public static String HIGH_COMP_RGBCOLOR_SELF = "#8FBC8F";
    
    public static long MAX_FILE_UPLOAD_SIZE = 100000000l;
    
    public static final String CSVPOSTTESTCONTACTSTR="CSVPOSTTESTCONTACTSTR";
    

}

