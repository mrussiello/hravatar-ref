/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.report;

import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.report.Report;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.Suborg;
import com.tm2ref.file.BucketType;
import com.tm2ref.file.FileXferUtils;
import com.tm2ref.file.MediaTempUrlSourceType;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.util.NVPair;
import com.tm2ref.util.StringUtils;

/**
 *
 * @author Mike
 */
public class ReportUtils
{
    
    
    public static String getMediaTempUrlSourceLink( int orgId, RcUploadedUserFile uuf, int thumbIndex, String fn, MediaTempUrlSourceType mediaTempUrlSourceType )
    {
        if( orgId<=0 || uuf==null || mediaTempUrlSourceType==null )
            return "";
        
        if( !RuntimeConstants.getBooleanValue("useAwsTempUrlsForMedia") )
            return getUploadedUserFileThumbUrl( uuf, fn );
        
        try
        {
            return RuntimeConstants.getStringValue("baseadminurl") + RuntimeConstants.getStringValue("mediaTempUrlSourcePath") + "/" + mediaTempUrlSourceType.getMediaTempUrlSourceTypeId() + "/" + thumbIndex + "/" + EncryptUtils.urlSafeEncrypt(orgId) + "/" + EncryptUtils.urlSafeEncrypt(uuf.getRcUploadedUserFileId() ) + "/" + fn;
        }   
        catch( Exception e )
        {
            LogService.logIt( e, "ReportUtils.getMediaTempUrlSourceLink() orgId=" + orgId + ", RcUploadedUserFileId=" + uuf.getRcUploadedUserFileId() + ", filename=" + fn );
            return getUploadedUserFileThumbUrl( uuf, fn );
        }
    }
    
    public static String getUploadedUserFileThumbUrl( RcUploadedUserFile uuf, String fn )
    {
        // String thumbUrl = null;
        UploadedUserFileType uft = uuf.getUploadedUserFileType();
        BucketType bt = RuntimeConstants.getBooleanValue("useAwsTestFoldersForProctoring") ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING;;
        boolean aws = RuntimeConstants.getBooleanValue("useAwsForUploadedRefMedia");
        
        if( aws )
        {
            String dir = uuf.getDirectory();
            if( dir.startsWith("/") )
                dir = dir.substring(1, dir.length() );
            if( RuntimeConstants.getBooleanValue("useAwsTempUrlsForMedia") )
            {
                try
                {
                    return FileXferUtils.getPresignedUrlAws( dir, fn, bt.getBucketTypeId(), null, RuntimeConstants.getIntValue( "awsTempUrlMinutes") );
                }
                catch( Exception e )
                {
                    LogService.logIt( e, "ReportUtils.getUploadedUserFileThumbUrl() dir=" + dir + ", filename=" + fn  );
                    return "";
                }
            }

            // Normal Method.
            return RuntimeConstants.getStringValue( "awsS3BaseUrl") + bt.getBucket() + "/" + bt.getBaseKey() + dir + "/" + fn;
        }
        
        // Not AWS
        return RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps") + uuf.getDirectory() + "/" + fn;           
    }
    
    
    
        
    public static String getReportFlagStringValue( String name, Suborg s, Org o, Report r)
    {
        String v = null;
                
        NVPair pr = null;
        
        if( s != null )
        {
            pr = StringUtils.getNVPairFromList(name, s.getReportFlags(), "|");
            
            if( pr!=null )
            {
                v = (String) pr.getValue();
                
                if( v!=null && !v.equals( "0") )
                    return v;
            }
        }
        
        if( o != null )
        {
            pr = StringUtils.getNVPairFromList(name, o.getReportFlags(), "|");
            
            if( pr!=null )
            {
                v = (String) pr.getValue();
                
                if( v!=null )
                    return v;
            }
        }
        
        if( r != null )
        {
            // LogService.logIt( "ReportUtils.getReportFlagStringValue() AAA" );
            
            pr = StringUtils.getNVPairFromList( name, r.getReportFlags(), "|");
            
            // LogService.logIt( "ReportUtils.getReportFlagStringValue() XXX pr: " + (pr!=null) + ", ");
            if( pr!=null )
            {
                v = (String) pr.getValue();
                
                if(v!=null )
                    return v;
            }
        }
        
        // LogService.logIt( "ReportUtils.getReportFlagStringValue() XXX returning null " );
        return null;        
    }

    
    
    /**
     * Returns null
     * @param name
     * @param tk
     * @param s
     * @param o
     * @param r
     * @return 
     */
    public static Integer getReportFlagIntValue( String name, Suborg s, Org o, Report r)
    {
        String v = getReportFlagStringValue(name, s, o, r );
        
        if( v==null || v.isEmpty() )
            return 0;
        
        return getInteger( v );
    }

    public static boolean getReportFlagBooleanValue( String name, Suborg s, Org o, Report r)
    {
        Integer v = getReportFlagIntValue(name, s, o, r );
        
        // LogService.logIt( "ReportUtils.getReportFlagBooleanValue() name=" + name + ", v=" + v );
        
        return v!=null && v.intValue()==1;
    }

    

    private static Integer getInteger( String s )
    {
        if( s==null || s.isEmpty() )
            return  null;
        
        try
        {
            int i = Integer.parseInt( s );
            
            return i; //  new Integer(i);
        }
        catch( Exception e )
        {
            LogService.logIt( "ReportUtils.getInteger() Unable to parse string to integer: " + s + ", " + e.toString() );            
        }
        return null;
    }
    
    

}
