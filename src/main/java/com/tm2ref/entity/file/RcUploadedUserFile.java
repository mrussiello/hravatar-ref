/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.entity.file;

import com.tm2ref.av.AvItemSpeechTextStatusType;
import com.tm2ref.file.BucketType;
import com.tm2ref.file.ConversionStatusType;
import com.tm2ref.file.FileContentType;
import com.tm2ref.file.UploadedUserFileFauxSource;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.util.StringUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

/**
 *
 * @author Mike
 */
@Entity
@Table( name = "rcuploadeduserfile" )
@NamedQueries( {          
    @NamedQuery( name = "RcUploadedUserFile.findById", query = "SELECT o FROM RcUploadedUserFile AS o WHERE o.rcUploadedUserFileId=:rcUploadedUserFileId" ),
    @NamedQuery( name = "RcUploadedUserFile.findRcCheckIdAndRcRaterIdRcItemIdAndTypeId", query = "SELECT o FROM RcUploadedUserFile AS o WHERE o.rcCheckId=:rcCheckId AND o.rcRaterId=:rcRaterId AND o.rcItemId=:rcItemId AND o.uploadedUserFileTypeId=:uploadedUserFileTypeId" ),
    @NamedQuery( name = "RcUploadedUserFile.findPhotoRcCheckIdAndRcRaterId", query = "SELECT o FROM RcUploadedUserFile AS o WHERE o.rcCheckId=:rcCheckId AND o.rcRaterId=:rcRaterId AND o.uploadedUserFileTypeId IN (201,202,211)" ),
    @NamedQuery( name = "RcUploadedUserFile.findRcCheckIdAndRcRaterId", query = "SELECT o FROM RcUploadedUserFile AS o WHERE o.rcCheckId=:rcCheckId AND o.rcRaterId=:rcRaterId" )
} )
public class RcUploadedUserFile implements Serializable, UploadedUserFileFauxSource
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "rcuploadeduserfileid" )
    private long rcUploadedUserFileId;

    @Column( name = "uploadeduserfiletypeid" )
    private int uploadedUserFileTypeId;
    
    @Column( name = "uploadeduserfilestatustypeid" )
    private int uploadedUserFileStatusTypeId;

    @Column( name = "rccheckid" )
    private long rcCheckId;
    
    @Column( name = "rcraterid" )
    private long rcRaterId;
            
    @Column( name = "rcratingid" )
    private long rcRatingId;
    
    @Column( name = "rcitemid" )
    private int rcItemId;
    
    @Column( name = "userid" )
    private long userId;
    
    @Column( name = "thumbfilename" )
    private String thumbFilename;    
    
    @Column( name = "thumbwidth" )
    private int thumbWidth;    
    
    @Column( name = "thumbheight" )
    private int thumbHeight;    
    
    @Column( name = "orientation" )
    private int orientation;
    
    @Column( name = "filename" )
    private String filename;

    @Column( name = "filesize" )
    private long fileSize;

    @Column( name = "mime" )
    private String mime;

    @Column( name = "filecontenttypeid" )
    private int fileContentTypeId;

    @Column( name = "r1" )
    private int r1;

    @Column( name = "r2" )
    private long r2;


    @Column( name = "width" )
    private int width;

    @Column( name = "height" )
    private int height;

    @Column( name = "note" )
    private String note;

    @Column( name = "maxthumbindex" )
    private int maxThumbIndex;    
    
    @Column( name = "failedthumbindices" )
    private String failedThumbIndices;    

    @Column( name = "pretestthumbindices" )
    private String preTestThumbIndices;    

    @Column( name = "audiofilename" )
    private String audioFilename;

    @Column( name = "audiofilecontenttypeid" )
    private int audioFileContentTypeId;
           
    @Column( name = "audiosize" )
    private int audioSize;

    @Column( name = "rotation" )
    private int rotation;
    
    @Column( name = "duration" )
    private float duration;
    
    
    /**
     * Packed string 
     * tran1a,tran1b,tran1c;confidence1;tran2a,tran2b|confidence2; ... 
     */
    @Column( name = "speechtext" )
    private String speechText;

    @Column( name = "speechtextconfidence" )
    private float speechTextConfidence;

    @Column( name = "speechtextstatustypeid" )
    private int speechTextStatusTypeId;
    
    @Column( name = "speechtexterrorcount" )
    private int speechTextErrorCount;
    
    @Column( name = "fileprocessingtypeid" )
    private int fileProcessingTypeId;
    
    /**
     *  This is used only for General File Uploads to hold the text content in the uploaded file for AI Processing.
     */
    @Column( name = "uploadedtext" )
    private String uploadedText;
        
    
    
    @Column( name = "initialfilesize" )
    private int initialFileSize;

    @Column( name = "initialfilename" )
    private String initialFilename;

    @Column( name = "initialfilecontenttypeid" )
    private int initialFileContentTypeId;

    /**
     * 0=uploaded
     * 1=deleted
     */
    @Column( name = "initialfilestatustypeid" )
    private int initialFileStatusTypeId;
    
    @Column( name = "initialmime" )
    private String initialMime;

    @Column( name = "originalsavedfilename" )
    private String originalSavedFilename;

    @Column( name = "conversionstatustypeid" )
    private int conversionStatusTypeId;
    
    @Column( name = "errorcount" )
    private int errorCount;
    
    @Column( name = "needsfiledelete" )
    private int needsFileDelete;
    
    @Column( name = "filestodelete" )
    private String filesToDelete;
    
    
    
    
    /*
     name;value;name;value etc.
    */
    @Column( name = "processingparams" )
    private String processingParams;
        
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupload")
    private Date lastUpload;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="conversionstatusdate")
    private Date conversionStatusDate;

    @Transient
    private int tempInt1;
    
    @Transient
    private Set<Integer> preTestIndexSet;

    @Transient
    private Map<Integer,Integer> failedIndexMap;
    
    @Transient
    private String uploadedFileUrl;
    
    @Transient
    private String uploadedFileIconFilename;
    
    @Transient
    private String uploadedFileTypeName;
    
    
    
    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (int) (this.rcUploadedUserFileId ^ (this.rcUploadedUserFileId >>> 32));
        hash = 71 * hash + (int) (this.rcCheckId ^ (this.rcCheckId >>> 32));
        hash = 71 * hash + (int) (this.rcRaterId ^ (this.rcRaterId >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RcUploadedUserFile other = (RcUploadedUserFile) obj;
        if (this.rcUploadedUserFileId != other.rcUploadedUserFileId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RcUploadedUserFile{" + "rcUploadedUserFileId=" + rcUploadedUserFileId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", rcItemId=" + rcItemId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + '}';
    }
    
    
    public String getThumbUrl()
    {
        String fn = this.thumbFilename;
        if( fn!=null && fn.contains(  ".IDX." ) )
            fn = StringUtils.replaceStr( fn, ".IDX." , ".1." );
                        
        return getMediaBaseUrl() + fn;
    }
    
    public String getMediaBaseUrl()
    {
        BucketType bt = RuntimeConstants.getBooleanValue( "useAwsTestFoldersForProctoring" ) ? BucketType.REFRECORDING_TEST : BucketType.REFRECORDING;

        return RuntimeConstants.getStringValue( "awsS3BaseUrl" ) + bt.getBucket() + "/" + bt.getBaseKey() +  r1 + "/" + r2 + "/"; 
    }
    
    
    
    // /ta/rcavpb/rcCheckId/uufId/audio.mp4
    //             or /ta/rcavpb/rcCheckId/uufId/video.mp4
    public String getReportingMediaUrl()
    {
        return RuntimeConstants.getStringValue("baseadminurl") + "/rcavpb/" + rcCheckId + "/" + this.rcUploadedUserFileId + "/" + (this.getIsAudio() ? "audio.mp4" : "video.mp4" ); 
    }
    
    public String getMediaUrl()
    {
        return getMediaBaseUrl() + filename;
    }
    
    
    public boolean getHasRecordingReadyForPlayback()
    {
        return this.fileSize>0 && filename!=null && !filename.isBlank() && mime!=null && !mime.isBlank() && getConversionStatusType().getReadyForViewing();
    }

    public boolean getHasRecordingInConversion()
    {
        return getHasValidInitialFile() && getConversionStatusType().getIsActive();
    }
    
    public boolean getHasValidInitialFile()
    {
        return this.initialFileSize>0 && initialFilename!=null && !initialFilename.isBlank();
    }
    
    public boolean getHasValidThumbs()
    {
        if( thumbFilename==null || thumbFilename.isBlank() )
            return false;
        
        // RcComment files have a direct thumb in the thumbFilename field.
        if( getUploadedUserFileType().getIsRcComment() )
            return true;
        
        // Other types use the maxThumbIndex
        if( maxThumbIndex<=0 )
            return false;
        
        for( int i=1;i<=maxThumbIndex;i++ )
        {
            if( !hasFailedIndex( i ) )
                return true;
        }
        return false;
    }
    
    public boolean hasFailedIndex( int idx )
    {
        if( failedIndexMap==null )
            initFailedIndexMap();

        return failedIndexMap.containsKey( idx );
    }
    

    public void addFailedIndex( int idx, int proctorImageErrorTypeId )
    {
        if( failedIndexMap==null )
            initFailedIndexMap();
        
        if( idx>0 )
            failedIndexMap.put( idx, proctorImageErrorTypeId );
    }
    
    public synchronized void initFailedIndexMap()
    {
        if( failedIndexMap==null )
            failedIndexMap = new TreeMap<>();
        
        if( failedThumbIndices==null || failedThumbIndices.isBlank() )
            return;
        
        int errorTypeId;
        int idx;
        
        for( String s : failedThumbIndices.split(",") )
        {
            if( s.isBlank() )
                continue;
            
            errorTypeId=0;
            if( s.indexOf(":")>0 )
            {
                idx = Integer.parseInt( s.substring(0, s.indexOf(":")));
                errorTypeId = Integer.parseInt( s.substring(s.indexOf(":")+1,s.length()));
            }    
            else
                idx = Integer.parseInt( s );
            
            failedIndexMap.put( idx, errorTypeId );
        }
    }

    public void saveFailedIndexMap()
    {
        if( failedIndexMap==null || failedIndexMap.isEmpty() )
        {
            failedThumbIndices=null;
            return;
        }
        
        Integer val;
        StringBuilder sb = new StringBuilder();
        for( Integer i : failedIndexMap.keySet() )
        {
            if( sb.length()>=950 )
                break;
            
            val = failedIndexMap.get(i);
            if( val==null )
                val = 0;
            if( sb.length()>0 )
                sb.append(",");
            sb.append( i.toString() + ":" + val.toString() );
        }
        failedThumbIndices = sb.length()>0 ? sb.toString() : null;
    }
    
    
    
    public boolean hasPreTestIndex( int idx )
    {
        if( preTestIndexSet==null )
            initPreTestIndexSet();

        return preTestIndexSet.contains( idx );
    }
    
    public void addPreTestIndex( int idx )
    {
        if( preTestIndexSet==null )
            initPreTestIndexSet();
        
        if( idx>0 )
            preTestIndexSet.add( idx );
    }
    
    public synchronized void initPreTestIndexSet()
    {
        if( preTestIndexSet==null )
            preTestIndexSet = new TreeSet<>();
        
        if( preTestThumbIndices==null || preTestThumbIndices.isBlank() )
            return;
        
        for( String s : preTestThumbIndices.split(",") )
        {
            if( s.isBlank() )
                continue;
            preTestIndexSet.add( Integer.parseInt(s) );
        }
    }

    public void savePreTestIndexSet()
    {
        if( preTestIndexSet==null || preTestIndexSet.isEmpty() )
        {
            preTestThumbIndices=null;
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        for( Integer i : preTestIndexSet )
        {
            if( sb.length()>=240 )
                break;
            if( sb.length()>0 )
                sb.append(",");
            sb.append( i.toString() );
        }
        preTestThumbIndices = sb.length()>0 ? sb.toString() : null;
    }
    
    public boolean getIsAudio()
    {
        return getFileContentType()!=null && getFileContentType().isAudio();
    }

    public boolean getIsVideo()
    {
        return getFileContentType()!=null && getFileContentType().isVideo();
    }
    
    public FileContentType getFileContentType()
    {
        if( filename==null || filename.isBlank() )
            return null;
        
        return FileContentType.getFileContentTypeFromContentType(mime, filename);
    }
    
    public ConversionStatusType getConversionStatusType()
    {
        return ConversionStatusType.getValue( conversionStatusTypeId );
    }
   
    public UploadedUserFileType getUploadedUserFileType()
    {
        return UploadedUserFileType.getValue( uploadedUserFileTypeId );
    }
    
    public AvItemSpeechTextStatusType getSpeechTextStatusType()
    {
        return AvItemSpeechTextStatusType.getValue( this.speechTextStatusTypeId );
    }
    
    public String getDirectory()
    {
        return "/" + r1 + "/" + r2;
    }


    public void appendNote( String m )
    {
        if( m==null || m.isEmpty() )
            return;
        
        if( note==null || note.isEmpty() )
            note = m;
        
        note += ",\n" + (new Date()).toString() + ": " + m;
    }

    public long getRcUploadedUserFileId() {
        return rcUploadedUserFileId;
    }

    public void setRcUploadedUserFileId(long rcUploadedUserFileId) {
        this.rcUploadedUserFileId = rcUploadedUserFileId;
    }

    public int getUploadedUserFileTypeId() {
        return uploadedUserFileTypeId;
    }

    public void setUploadedUserFileTypeId(int uploadedUserFileTypeId) {
        this.uploadedUserFileTypeId = uploadedUserFileTypeId;
    }

    public int getUploadedUserFileStatusTypeId() {
        return uploadedUserFileStatusTypeId;
    }

    public void setUploadedUserFileStatusTypeId(int uploadedUserFileStatusTypeId) {
        this.uploadedUserFileStatusTypeId = uploadedUserFileStatusTypeId;
    }

    public long getRcCheckId() {
        return rcCheckId;
    }

    public void setRcCheckId(long rcCheckId) {
        this.rcCheckId = rcCheckId;
    }

    public long getRcRaterId() {
        return rcRaterId;
    }

    public void setRcRaterId(long rcRaterId) {
        this.rcRaterId = rcRaterId;
    }

    public String getThumbFilename() {
        return thumbFilename;
    }

    public void setThumbFilename(String thumbFilename) {
        this.thumbFilename = thumbFilename;
    }

    public int getThumbWidth() {
        return thumbWidth;
    }

    public void setThumbWidth(int thumbWidth) {
        this.thumbWidth = thumbWidth;
    }

    public int getThumbHeight() {
        return thumbHeight;
    }

    public void setThumbHeight(int thumbHeight) {
        this.thumbHeight = thumbHeight;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public int getFileContentTypeId() {
        return fileContentTypeId;
    }

    public void setFileContentTypeId(int fileContentTypeId) {
        this.fileContentTypeId = fileContentTypeId;
    }

    public int getR1() {
        return r1;
    }

    public void setR1(int r1) {
        this.r1 = r1;
    }

    public long getR2() {
        return r2;
    }

    public void setR2(long r2) {
        this.r2 = r2;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getMaxThumbIndex() {
        return maxThumbIndex;
    }

    public void setMaxThumbIndex(int maxThumbIndex) {
        this.maxThumbIndex = maxThumbIndex;
    }

    public String getFailedThumbIndices() {
        return failedThumbIndices;
    }

    public void setFailedThumbIndices(String failedThumbIndices) {
        this.failedThumbIndices = failedThumbIndices;
    }

    public String getPreTestThumbIndices() {
        return preTestThumbIndices;
    }

    public void setPreTestThumbIndices(String preTestThumbIndices) {
        this.preTestThumbIndices = preTestThumbIndices;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastUpload() {
        return lastUpload;
    }

    public void setLastUpload(Date lastUpload) {
        this.lastUpload = lastUpload;
    }

    public Set<Integer> getPreTestIndexSet() {
        return preTestIndexSet;
    }

    public void setPreTestIndexSet(Set<Integer> preTestIndexSet) {
        this.preTestIndexSet = preTestIndexSet;
    }

    public Map<Integer, Integer> getFailedIndexMap() {
        return failedIndexMap;
    }

    public void setFailedIndexMap(Map<Integer, Integer> failedIndexMap) {
        this.failedIndexMap = failedIndexMap;
    }

    public String getProcessingParams() {
        return processingParams;
    }

    public void setProcessingParams(String processingParams) {
        this.processingParams = processingParams;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }


    public long getRcRatingId() {
        return rcRatingId;
    }

    public void setRcRatingId(long rcRatingId) {
        this.rcRatingId = rcRatingId;
    }

    public int getRcItemId() {
        return rcItemId;
    }

    public void setRcItemId(int rcItemId) {
        this.rcItemId = rcItemId;
    }

    public int getInitialFileSize() {
        return initialFileSize;
    }

    public void setInitialFileSize(int initialFileSize) {
        this.initialFileSize = initialFileSize;
    }

    public String getInitialFilename() {
        return initialFilename;
    }

    public void setInitialFilename(String initialFilename) {
        this.initialFilename = initialFilename;
    }

    public int getInitialFileContentTypeId() {
        return initialFileContentTypeId;
    }

    public void setInitialFileContentTypeId(int initialFileContentTypeId) {
        this.initialFileContentTypeId = initialFileContentTypeId;
    }

    public int getInitialFileStatusTypeId() {
        return initialFileStatusTypeId;
    }

    public void setInitialFileStatusTypeId(int initialFileStatusTypeId) {
        this.initialFileStatusTypeId = initialFileStatusTypeId;
    }

    public String getInitialMime() {
        return initialMime;
    }

    public void setInitialMime(String initialMime) {
        this.initialMime = initialMime;
    }

    public String getOriginalSavedFilename() {
        return originalSavedFilename;
    }

    public void setOriginalSavedFilename(String originalSavedFilename) {
        this.originalSavedFilename = originalSavedFilename;
    }

    public int getConversionStatusTypeId() {
        return conversionStatusTypeId;
    }

    public void setConversionStatusTypeId(int conversionStatusTypeId) {
        this.conversionStatusDate=new Date();
        this.conversionStatusTypeId = conversionStatusTypeId;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getNeedsFileDelete() {
        return needsFileDelete;
    }

    public void setNeedsFileDelete(int needsFileDelete) {
        this.needsFileDelete = needsFileDelete;
    }

    public String getFilesToDelete() {
        return filesToDelete;
    }

    public void setFilesToDelete(String filesToDelete) {
        this.filesToDelete = filesToDelete;
    }

    public Date getConversionStatusDate() {
        return conversionStatusDate;
    }

    public void setConversionStatusDate(Date conversionStatusDate) {
        this.conversionStatusDate = conversionStatusDate;
    }

    public int getFileProcessingTypeId() {
        return fileProcessingTypeId;
    }

    public void setFileProcessingTypeId(int fileProcessingTypeId) {
        this.fileProcessingTypeId = fileProcessingTypeId;
    }

    public String getAudioFilename() {
        return audioFilename;
    }

    public void setAudioFilename(String audioFilename) {
        this.audioFilename = audioFilename;
    }

    public int getAudioFileContentTypeId() {
        return audioFileContentTypeId;
    }

    public void setAudioFileContentTypeId(int audioFileContentTypeId) {
        this.audioFileContentTypeId = audioFileContentTypeId;
    }

    public int getAudioSize() {
        return audioSize;
    }

    public void setAudioSize(int audioSize) {
        this.audioSize = audioSize;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getSpeechText() {
        return speechText;
    }

    public void setSpeechText(String speechText) {
        this.speechText = speechText;
    }

    public float getSpeechTextConfidence() {
        return speechTextConfidence;
    }

    public void setSpeechTextConfidence(float speechTextConfidence) {
        this.speechTextConfidence = speechTextConfidence;
    }

    public int getSpeechTextStatusTypeId() {
        return speechTextStatusTypeId;
    }

    public void setSpeechTextStatusTypeId(int speechTextStatusTypeId) {
        this.speechTextStatusTypeId = speechTextStatusTypeId;
    }

    public int getSpeechTextErrorCount() {
        return speechTextErrorCount;
    }

    public void setSpeechTextErrorCount(int speechTextErrorCount) {
        this.speechTextErrorCount = speechTextErrorCount;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getTempInt1() {
        return tempInt1;
    }

    public void setTempInt1(int tempInt1) {
        this.tempInt1 = tempInt1;
    }

    public String getUploadedFileUrl() {
        return uploadedFileUrl;
    }

    public void setUploadedFileUrl(String uploadedFileUrl) {
        this.uploadedFileUrl = uploadedFileUrl;
    }

    public String getUploadedFileIconFilename() {
        return uploadedFileIconFilename;
    }

    public void setUploadedFileIconFilename(String uploadedFileIconFilename) {
        this.uploadedFileIconFilename = uploadedFileIconFilename;
    }

    public String getUploadedFileTypeName() {
        return uploadedFileTypeName;
    }

    public void setUploadedFileTypeName(String uploadedFileTypeName) {
        this.uploadedFileTypeName = uploadedFileTypeName;
    }

    public String getUploadedText()
    {
        return uploadedText;
    }

    public void setUploadedText(String uploadedText)
    {
        if( uploadedText!=null && uploadedText.isBlank() )
            uploadedText=null;
        
        this.uploadedText = uploadedText;
    }

}
