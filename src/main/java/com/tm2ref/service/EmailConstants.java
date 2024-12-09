/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2ref.service;

/**
 *
 * @author Mike
 */
public class EmailConstants {
    public static final String HTML_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
                                             "<html><head><meta content=\"text/html;charset=UTF-8\" http-equiv=\"Content-Type\"><title></title></head><body bgcolor=\"#ffffff\" text=\"#000000\">\n";

    public static final String HTML_FOOTER = "</body></html>";


    public static String CONTENT = "content";

    public static String TO = "to";

    public static String FROM = "from";

    public static String CC = "cc";

    public static String BCC = "bcc";

    public static String SUBJECT = "subject";

    public static String MIME_TYPE = "mime";
    
    public static String OVERRIDE_BLOCK = "overrideblock";

    public static String ATTACH_BYTES = "attach_bytes_";
    public static String ATTACH_MIME = "attach_mime_";
    public static String ATTACH_FN = "attach_name_";
    
    
    public static String[] VALID_SUPPORT_ADDRESSES = { "support" , "help" , "payments" , "info" , "sales" };

}
