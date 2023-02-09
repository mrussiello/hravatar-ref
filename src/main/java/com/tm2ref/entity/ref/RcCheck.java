package com.tm2ref.entity.ref;


import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.User;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.global.Constants;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.ref.RcAvType;
import com.tm2ref.ref.RcCandidatePhotoCaptureType;
import com.tm2ref.ref.RcCandidateStatusType;
import com.tm2ref.ref.RcCheckStatusType;
import com.tm2ref.ref.RcCheckType;
import com.tm2ref.ref.RcDistributionType;
import com.tm2ref.ref.RcItemWrapper;
import com.tm2ref.ref.RcRaterPhotoCaptureType;
import com.tm2ref.ref.RcRaterRoleType;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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


@Entity
@Table( name = "rccheck" )
@NamedQueries({
        @NamedQuery( name = "RcCheck.findByTestKeyId", query = "SELECT o FROM RcCheck AS o WHERE o.testKeyId=:testKeyId" ),
        @NamedQuery( name = "RcCheck.findByRcCheckId", query = "SELECT o FROM RcCheck AS o WHERE o.rcCheckId=:rcCheckId" ),
        @NamedQuery( name = "RcCheck.findByCandidateAccessCode", query = "SELECT o FROM RcCheck AS o WHERE o.candidateAccessCode=:candidateAccessCode" )    
})
public class RcCheck implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="rccheckid")
    private long rcCheckId;

    @Column(name="orgid")
    private int orgId;

    @Column(name="suborgid")
    private int suborgId;

    @Column(name="remindertypeid")
    private int reminderTypeId;

    @Column(name="testkeyid")
    private long testKeyId;
    
    @Column(name="reportid")
    private int reportId;

    @Column(name="reportid2")
    private int reportId2;

    /**
     * 0=pre-hire
     * 1=employee
     */
    @Column(name="rcchecktypeid")
    private int rcCheckTypeId;

    @Column(name="rccheckstatustypeid")
    private int rcCheckStatusTypeId;

    @Column(name="rccheckscoringstatustypeid")
    private int rcCheckScoringStatusTypeId;
    
    @Column(name="forceallanonymous")
    private int forceAllAnonymous;
    
    @Column(name="candidatephotocapturetypeid")
    private int candidatePhotoCaptureTypeId = 0;
            
    @Column(name="raterphotocapturetypeid")
    private int raterPhotoCaptureTypeId = 0;
    
    
    @Column(name="distributiontypeid")
    private int distributionTypeId;
    
    @Column(name="emailresultsto")
    private String emailResultsTo;

    @Column(name="textresultsto")
    private String textResultsTo;
        
    @Column(name="returnurl")
    private String returnUrl;
    
    @Column(name="adminuserid")
    private long adminUserId;

    @Column(name="userid")
    private long userId;

    @Column(name="candidatecannotaddraters")
    private int candidateCannotAddRaters;

    @Column(name="askforreferrals")
    private int askForReferrals;
    
    
    //@Column(name="disablecandidatedirectinput")
    //private int disableCandidateDirectInput;
    
    @Column(name="collectcandidateratings")
    private int collectCandidateRatings;

    
    @Column(name="candidateaccesscode")
    private String candidateAccessCode;

    @Column(name="candidatestarts")
    private int candidateStarts;

    @Column(name="candidateseconds")
    private int candidateSeconds;
    
    @Column(name="creditid")
    private long creditId;

    @Column(name="creditindex")
    private int creditIndex = 0;
    
    @Column(name="jobtitle")
    private String jobTitle;

    @Column(name="langcode")
    private String langCode;

    @Column(name="overallscore")
    private float overallScore;

    @Column(name="percentcomplete")
    private float percentComplete;
    
    @Column(name="minsupervisors")
    private int minSupervisors;

    @Column(name="minraters")
    private int minRaters;

    @Column(name="maxraters")
    private int maxRaters;

    @Column(name="enforceraterlimits")
    private int enforceRaterLimits;

    @Column(name="avcommentstypeid")
    private int avCommentsTypeId;
    
    @Column(name="corpid")
    private int corpId;

    @Column(name="rccandidatestatustypeid")
    private int rcCandidateStatusTypeId = 0;

    @Column(name="extref")
    private String extRef;

    @Column(name="rcscriptid")
    private int rcScriptId;

    @Column(name="ipaddress")
    private String ipAddress;

    @Column(name="ipcountry")
    private String ipCountry;

    @Column(name="ipstate")
    private String ipState;

    @Column(name="ipcity")
    private String ipCity;
    
    @Column(name="useragent")
    private String userAgent;

    @Column(name="textstr1")
    private String textStr1;
    
    @Column(name="customparameters")
    private String customParameters;

    
    /**
     * This is the number of competencies included in the top/bottom competencies lists in reports.
     */
    @Column(name="topbottomcount")
    private int topBottomCount=3;
   
    /*
     This is the category of ratings used to select the top/bottom.
     0 = others (all except the candidate)
     1 = the candidate.
     10=all ratings
    */
    @Column(name="topbottomsrctypeid")
    private int topBottomSrcTypeId=3;
   
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="senddate")
    private Date sendDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="firstcandidatesenddate")
    private Date firstCandidateSendDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastcandidatesenddate")
    private Date lastCandidateSendDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="firstcandidatereferencedate")
    private Date firstCandidateReferenceDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastcandidatereferencedate")
    private Date lastCandidateReferenceDate;
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="candidatestartdate")
    private Date candidateStartDate;

    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="candidatecompletedate")
    private Date candidateCompleteDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="candidatelastupdate")
    private Date candidateLastUpdate;

    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="completedate")
    private Date completeDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="scoredate")
    private Date scoreDate;

    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="expiredate")
    private Date expireDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="candidatereleasedate")
    private Date candidateReleaseDate;

    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastcandidatereminderdate")
    private Date lastCandidateReminderDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastcandidateprogressmsgdate")
    private Date lastCandidateProgressMsgDate;
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastprogressmsgdate")
    private Date lastProgressMsgDate;
    
    
    
    @Transient
    private List<RcRater> rcRaterList;
    
    @Transient
    private List<RcSuspiciousActivity> rcSuspiciousActivityList;
    
    @Transient
    private User adminUser;
    
    @Transient
    private User user;
    
    @Transient
    private Org org;
    
    @Transient
    private RcScript rcScript;

    @Transient
    private Locale locale;

    @Transient
    private RcRater rcRater;
    
    @Transient
    private RcOrgPrefs rcOrgPrefs;
    
    @Transient
    private RcSuborgPrefs rcSuborgPrefs;
    
    @Transient
    private Date lastSecondsDate;
    
    @Transient
    private List<RcUploadedUserFile> rcUploadedUserFileList;

    @Transient
    private List<RcUploadedUserFile> fauxRcUploadedUserFileList;
    
    //@Transient
    //private RefUserType refUserType;

    
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString() {
        return "RcCheck{" + "rcCheckId=" + rcCheckId + ", orgId=" + orgId + ", userId=" + userId + '}';
    }

    public String toStringShort() {
        return "RcCheck{" + "rcCheckId=" + rcCheckId + ", rcRaterId=" + (rcRater==null ? "0" : rcRater.getRcRaterId() ) + '}';
    }
    
    public boolean getHasRaterPhotos()
    {
        if( rcRaterList==null || rcRaterList.isEmpty() )
            return false;
        for( RcRater r : rcRaterList )
        {
            if( r.getHasPhotos() )
                return true;
        }
        return false;
    }
    public boolean getHasRaterIdPhotos()
    {
        if( rcRaterList==null || rcRaterList.isEmpty() )
            return false;
        for( RcRater r : rcRaterList )
        {
            if( r.getHasIdPhotos() )
                return true;
        }
        return false;
    }

    
    public boolean getHasPhotos()
    {
        return fauxRcUploadedUserFileList!=null && !fauxRcUploadedUserFileList.isEmpty();
    }
    public boolean getHasIdPhotos()
    {
        if( !getHasPhotos() )
            return false;
        for( RcUploadedUserFile u : this.fauxRcUploadedUserFileList )
        {
            if( u.getUploadedUserFileType().getIsRcCandidateId() )
                return true;
        }
        return false;
    }

    public List<RcUploadedUserFile> getFauxPhotoRcUploadedUserFilesPhoto()
    {
        return getFauxPhotoRcUploadedUserFiles( UploadedUserFileType.REF_CHECK_IMAGES.getUploadedUserFileTypeId() );
    }
    public List<RcUploadedUserFile> getFauxPhotoRcUploadedUserFilesId()
    {
        return getFauxPhotoRcUploadedUserFiles( UploadedUserFileType.REF_CHECK_ID.getUploadedUserFileTypeId() );
    }

    public List<RcUploadedUserFile> getFauxPhotoRcUploadedUserFiles(int uploadedUserFileTypeId)
    {
        List<RcUploadedUserFile> out = new ArrayList<>();
        if( fauxRcUploadedUserFileList!=null  )
        {
            for( RcUploadedUserFile u : fauxRcUploadedUserFileList )
            {
                if( u.getUploadedUserFileTypeId()==uploadedUserFileTypeId )
                    out.add(u);
            }
        }
        return out;
    }
    
    
    public RcUploadedUserFile getSingleFauxPhotoRcUploadedUserFile()
    {
        return getSingleFauxPhotoRcUploadedUserFile( UploadedUserFileType.REF_CHECK_IMAGES.getUploadedUserFileTypeId() );
    }
    
    public RcUploadedUserFile getSingleFauxPhotoIdRcUploadedUserFile()
    {
        return getSingleFauxPhotoRcUploadedUserFile( UploadedUserFileType.REF_CHECK_ID.getUploadedUserFileTypeId() );
    }
    
    private RcUploadedUserFile getSingleFauxPhotoRcUploadedUserFile( int uploadedUserFileTypeId )
    {
        if( fauxRcUploadedUserFileList!=null  )
        {
            RcUploadedUserFile u;
            for( int i=fauxRcUploadedUserFileList.size()-1;i>=0;i-- )
            {
                u = fauxRcUploadedUserFileList.get(i);
                if( u.getUploadedUserFileTypeId()==uploadedUserFileTypeId )
                    return u;
            }
        }
            
        return null;        
    }
    
    
    public boolean getHasCandidateIpLocationData()
    {
        return ipCountry!=null && !ipCountry.isBlank();
    }

    public String getCandidateIpLocationData()
    {
        if( !getHasCandidateIpLocationData() )
            return "";
        
        String out = ipCountry;
        if( ipState!=null && !ipState.isBlank() )
            out += ", " + ipState;
        if( ipCity!=null && !ipCity.isBlank() )
            out += ", " + ipCity;
        return out;
    }
    
    
    
    public boolean getRequiresAnyCandidateInputOrSelfRating()
    {
        return candidateCannotAddRaters!=1 || (rcScript==null || rcScript.getHasAnyCandidateInput()) || collectCandidateRatings>0;        
    }
        
    
    public boolean getCollectRatingsFmCandidate()
    {
        return this.collectCandidateRatings>0;
    }
    
    public long getCandidateRcRaterId()
    {
        if( this.getRcRaterList()==null )
            return 0;
        for( RcRater r : this.rcRaterList )
        {
            if( r.getUserId()==this.userId )
                return r.getRcRaterId();
        }
        return 0;
    }
    
    public int getCandidateQuestionsAndSelfRatingCount()
    {
        if( rcScript==null )
            return 0;
        int ct = rcScript.getCandidateQuestionCount();
        if( rcScript.getHasAnyCandidateInput() )
            ct += rcScript.getItemCount(true);
        return ct;
    }
    
    public boolean getIsSelfOnly()
    {
        return getCollectRatingsFmCandidate() && rcRaterList!=null && rcRaterList.size()==1 && rcRaterList.get(0).getUserId()==userId;        
    }
    
    /*
    public boolean getRequiresAnyCandidateInput()
    {
        if( getCollectRatingsFmCandidate() )
            return true;
        if( minRaters>0 && candidateCannotAddRaters<=0 )
            return true;
        if( rcScript!=null && rcScript.getHasAnyCandidateInput() )
            return true;
        return false;
    }
    */
    
    
    
    public boolean getHasSuspiciousActivity()
    {
        return rcSuspiciousActivityList!=null && !rcSuspiciousActivityList.isEmpty();
    }

    public boolean getNeedsSupervisors()
    {
        if( rcRaterList==null || rcRaterList.isEmpty() )
            return false;

        //int[] rcs = getRaterRoleTypeCounts();
        
        //if( rcs[0]<=0 || rcs[1]>=minSupervisors )
        //    return false;

        int[] rcs = getRaterRoleTypeCountsCandidate();        
        return rcs[1]<getMinSupervisorsCandidate();
    }
    
    public int getMinSupervisorsNeeded()
    {
        if( rcRaterList==null || rcRaterList.isEmpty() )
            return getMinSupervisorsCandidate();

        //int[] rcs = getRaterRoleTypeCounts();        
        //if( rcs[0]<=0 || rcs[1]>=minSupervisors )
        //    return 0;

        int[] rcs = getRaterRoleTypeCountsCandidate();        
        if( rcs[0]<=0 || rcs[1]>=getMinSupervisorsCandidate() )
            return 0;
        
        return getMinSupervisorsCandidate() - rcs[1];        
    }
    
    public int[] getRaterRoleTypeCountsCandidate()
    {
        return getRaterRoleTypeCounts( true );
    }
    public int[] getRaterRoleTypeCounts()
    {
        return getRaterRoleTypeCounts( false );
    }
    
    /**
     * data[0]=total
     * data[1]=supervisor/Manager
     * data[2]=peer
     * data[3]=subordinate
     * data[4]=other or unknown
     * 
     * 
     * @param candidateOnly
     * @return 
     */
    public int[] getRaterRoleTypeCounts( boolean candidateOnly )
    {
        int[] out = new int[5];
        if( rcRaterList==null )
            return out;
        for( RcRater r : rcRaterList )
        {
            //if( candidateOnly && (r.getRcRaterSourceType()==null || r.getRcRaterSourceType().getIsAccountUser()) )
            //    continue;
            
            if( r.getIsCandidateOrEmployee() )
                continue;
            
            out[0] ++;
            
            if( r.getRcRaterRoleType().getIsSupervisorOrManager() )
                out[1]++;
            if( r.getRcRaterRoleType().getIsPeer() )
                out[2]++;
            if( r.getRcRaterRoleType().getIsSubordinate() )
                out[3]++;
            if( r.getRcRaterRoleType().getIsOther() || r.getRcRaterRoleType().equals( RcRaterRoleType.UNKNOWN ) )
                out[4]++;
        }
        return out;
    }

    public List<RcRater> getRcRaterListCandidateSupers()
    {
        return getRcRaterListCandidate( true );
    }
    public List<RcRater> getRcRaterListCandidate()
    {
        return getRcRaterListCandidate( false );
    }
        
    private List<RcRater> getRcRaterListCandidate( boolean supervisors )
    {
        List<RcRater> out = new ArrayList<>();
        if( rcRaterList==null )
            return out;
        
        //if( 1==1 )
        //    return rcRaterList;
        
        for( RcRater r : rcRaterList )
        {
            //if( r.getRcRaterSourceType()==null || r.getRcRaterSourceType().getIsAccountUser() )
            //    continue;
            if( r.getIsCandidateOrEmployee() )
                continue;
            
            if( supervisors && !r.getRcRaterRoleType().getIsSupervisorOrManager() )
                continue;            
            out.add(r);
        }
        return out;
    }

    public String getRcCheckIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( rcCheckId );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcCheck.getRcCheckIdEncrypted() " + toString()  );
            return "";
        }
    }
    public String getUserIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( userId );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcCheck.getUserIdEncrypted() " + toString()  );
            return "";
        }
    }
    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (this.rcCheckId ^ (this.rcCheckId >>> 32));
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
        final RcCheck other = (RcCheck) obj;
        if (this.rcCheckId != other.rcCheckId) {
            return false;
        }
        return true;
    }
    
    public String getRcCheckName()
    {
        return this.getRcCheckType().getName( locale==null ? Locale.US : locale );
    }
    
    
    public void setRcUploadedUserFilesInCandidateRatings( List<RcRating> rcRatingList, long rcRaterId)
    {
        if( rcUploadedUserFileList==null || rcUploadedUserFileList.isEmpty() || rcRatingList==null || rcRatingList.isEmpty() )
            return;
        
        for( RcRating rtg : rcRatingList )
        {
            rtg.setRcUploadedUserFile( getRcUploadedUserFileForItemComment( rtg.getRcItemId(), rcRaterId ) );
        }
    }
    
    public RcUploadedUserFile getRcUploadedUserFileForItemComment( int rcItemId, long rcRaterId  )
    {
        if( rcUploadedUserFileList==null || rcItemId<=0 || rcRaterId<=0 )
            return null;
        
        for( RcUploadedUserFile u : this.rcUploadedUserFileList )
        {
            if( u.getUploadedUserFileType().getIsRcComment() && u.getRcRaterId()==rcRaterId && u.getRcItemId()==rcItemId )
                return u;
        }
        
        return null;
    }
    
    
    
    
    public void setRcRatingsInScript( List<RcRating> rcrl, boolean forScoringOrReporting) throws Exception
    {
        if( rcrl==null  )
            throw new Exception( "RcRatingList is null" );

        if( rcScript==null )
            throw new Exception( "RcScript is null" );

        if( rcScript.getAllItemWrapperList()==null  )
            throw new Exception( "RcScript.itemWrapperList is null" );

        if( rcrl.isEmpty() )
            return;
        
        for( RcItemWrapper rciw : rcScript.getAllItemWrapperList() )
        {
            for( RcRating rcr : rcrl )
            {
                if( rcr.getRcItemId()==rciw.getRcItemId() )
                {
                    rcr.setRcItem( rciw.getRcItem() );
                    
                    if( forScoringOrReporting )
                    {
                        rciw.setRcRating(null);
                        rciw.addRating(rcr);
                    }
                    else
                        rciw.setRcRating(rcr);
                    // rciw.addRating(rcr);
                }
            }
        }        
    }
    
    
    
    public String getCandidateInputStr1()
    {
        return getCandidateInputStr( 1 );
    }
    public String getCandidateInputStr2()
    {
        return getCandidateInputStr( 2 );
    }
    public String getCandidateInputStr3()
    {
        return getCandidateInputStr( 3 );
    }
    public String getCandidateInputStr4()
    {
        return getCandidateInputStr( 4 );
    }
    public String getCandidateInputStr5()
    {
        return getCandidateInputStr( 6 );
    }
    public void setCandidateInputStr1( String v )
    {
        setCandidateInputStr( 1, v );
    }
    public void setCandidateInputStr2( String v )
    {
        setCandidateInputStr( 2, v );
    }
    public void setCandidateInputStr3( String v )
    {
        setCandidateInputStr( 3, v );
    }
    public void setCandidateInputStr4( String v )
    {
        setCandidateInputStr( 4, v );
    }
    public void setCandidateInputStr5( String v )
    {
        setCandidateInputStr( 5, v );
    }

    public String getResultsViewUrl()
    {
        return RuntimeConstants.getStringValue( "RefCheckResultsBaseUrl" ) + "?rci=" + this.getRcCheckIdEncrypted() + "&am=SR";
    }
    

    /**
     * idx must be between 1 and 5
     * @param idx
     * @param s 
     */
    public void setCandidateInputStr( int idx, String s )
    {
        // idx must be between 1 and 5
        if( idx<=0 || idx>5 )
            return;
        
        textStr1 = StringUtils.removeBracketedArtifactFromString( textStr1, "CANDIDATEINPUTSTR" + idx );
        
        if( s==null || s.isBlank() )
            return;
        
        if( textStr1==null )
            textStr1 = "";        
        textStr1 += "[CANDIDATEINPUTSTR" + idx + "]" + s;
    }
    
    public String getCandidateInputStr( int idx )
    {
        // idx must be between 1 and 5
        if( idx<=0 || idx>5 )
            return null;
        String s = textStr1==null || textStr1.isBlank() ? null : StringUtils.getBracketedArtifactFromString( textStr1, "CANDIDATEINPUTSTR" + idx );
        return s==null ? "" : s;
    }
    
    public RcRater getRcRaterForRcRaterId( long rcRaterId )
    {
        if( rcRaterList==null )
            return null;
        for( RcRater r : rcRaterList )
        {
            if( r.getRcRaterId()==rcRaterId )
                return r;
        }
        return null;
    }
    
    public RcRater getRcRaterForUserId( long userId )
    {
        if( rcRaterList==null )
            return null;
        for( RcRater r : rcRaterList )
        {
            if( r.getUserId()==userId )
                return r;
        }
        return null;
    }
    
    public String getCandidateStartUrl()
    {
        return RuntimeConstants.getStringValue( "RefCheckCandBaseUrl" ) + candidateAccessCode;        
    }
    
    
    public void setItemsAndRatings() throws Exception
    {
        if( rcScript==null )
        {
            LogService.logIt( "RcCheck.setItemsAndRatings() rcScript is null. Cannot set items and ratings. rcCheckId=" + rcCheckId );
            return;
        }
        
        RcRating rtg;
        for( RcItemWrapper rciw : rcScript.getAllItemWrapperList() )
        {
            for( RcRater rcr : rcRaterList )
            {
                rtg = rcr.getRcRating( rciw.getRcItemId() );
                if( rtg!=null && rtg.getIsComplete() )
                {
                    rtg.setUser( rcr.getUser() );
                    rtg.setRcRater( rcr );
                    // rciw.addRating( rtg );
                }
            }            
        }
    }
    
        
    public long getRcCheckId() {
        return rcCheckId;
    }

    public void setRcCheckId(long rcCheckId) {
        this.rcCheckId = rcCheckId;
    }

    public RcCandidatePhotoCaptureType getRcCandidatePhotoCaptureType()
    {
        return RcCandidatePhotoCaptureType.getValue( this.candidatePhotoCaptureTypeId);
    }
    
    public RcRaterPhotoCaptureType getRcRaterPhotoCaptureType()
    {
        return RcRaterPhotoCaptureType.getValue( this.raterPhotoCaptureTypeId);
    }
    
    public RcCandidateStatusType getRcCandidateStatusType()
    {
        return RcCandidateStatusType.getValue(this.rcCandidateStatusTypeId );
    }
    

    
    public RcCheckStatusType getRcCheckStatusType()
    {
        return RcCheckStatusType.getValue( rcCheckStatusTypeId );
    }

    public RcCheckType getRcCheckType()
    {
        return RcCheckType.getValue( rcCheckTypeId );
    }
    
    public RcDistributionType getRcDistributionType()
    {
        return RcDistributionType.getValue( distributionTypeId );
    }
    
    public RcAvType getRcAvType()
    {
        return RcAvType.getValue( avCommentsTypeId );
    }
    
    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getReminderTypeId() {
        return reminderTypeId;
    }

    public void setReminderTypeId(int reminderTypeId) {
        this.reminderTypeId = reminderTypeId;
    }

    public int getRcCheckStatusTypeId() {
        return rcCheckStatusTypeId;
    }

    public void setRcCheckStatusTypeId(int rcCheckStatusTypeId) {
        this.rcCheckStatusTypeId = rcCheckStatusTypeId;
    }

    public long getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCreditId() {
        return creditId;
    }

    public void setCreditId(long creditId) {
        this.creditId = creditId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public float getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(float overallScore) {
        this.overallScore = overallScore;
    }

    public int getCorpId() {
        return corpId;
    }

    public void setCorpId(int corpId) {
        this.corpId = corpId;
    }


    public String getExtRef() {
        return extRef;
    }

    public void setExtRef(String extRef) {
        this.extRef = extRef;
    }

    public int getRcScriptId() {
        return rcScriptId;
    }

    public void setRcScriptId(int rcScriptId) {
        this.rcScriptId = rcScriptId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getTextStr1() {
        return textStr1;
    }

    public void setTextStr1(String textStr1) {
        this.textStr1 = textStr1;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getRcCheckTypeId() {
        return rcCheckTypeId;
    }

    public void setRcCheckTypeId(int rcCheckTypeId) {
        this.rcCheckTypeId = rcCheckTypeId;
    }

    public int getDistributionTypeId() {
        return distributionTypeId;
    }

    public void setDistributionTypeId(int distributionTypeId) {
        this.distributionTypeId = distributionTypeId;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public List<RcRater> getRcRaterList() {
        return rcRaterList;
    }

    public void setRcRaterList(List<RcRater> rcRaterList) {
        this.rcRaterList = rcRaterList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public Date getCandidateCompleteDate() {
        return candidateCompleteDate;
    }

    public void setCandidateCompleteDate(Date candidateCompleteDate) {
        this.candidateCompleteDate = candidateCompleteDate;
    }

    public Date getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public RcScript getRcScript() {
        return rcScript;
    }

    public void setRcScript(RcScript rcScript) {
        this.rcScript = rcScript;
    }

    public User getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(User adminUser) {
        this.adminUser = adminUser;
    }

    public List<RcSuspiciousActivity> getRcSuspiciousActivityList() {
        return rcSuspiciousActivityList;
    }

    public void setRcSuspiciousActivityList(List<RcSuspiciousActivity> rcSuspiciousActivityList) {
        this.rcSuspiciousActivityList = rcSuspiciousActivityList;
    }

    public int getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(int suborgId) {
        this.suborgId = suborgId;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public float getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(float percentComplete) {
        this.percentComplete = percentComplete;
    }

    public String getCandidateAccessCode() {
        return candidateAccessCode;
    }

    public void setCandidateAccessCode(String candidateAccessCode) {
        this.candidateAccessCode = candidateAccessCode;
    }

    public RcRater getRcRater() {
        return rcRater;
    }

    public void setRcRater(RcRater rcRater) {
        this.rcRater = rcRater;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }

    public String getEmailResultsTo() {
        return emailResultsTo;
    }

    public void setEmailResultsTo(String emailResultsTo) {
        this.emailResultsTo = emailResultsTo;
    }

    public String getTextResultsTo() {
        return textResultsTo;
    }

    public void setTextResultsTo(String textResultsTo) {
        this.textResultsTo = textResultsTo;
    }

    public Date getCandidateStartDate() {
        return candidateStartDate;
    }

    public void setCandidateStartDate(Date candidateStartDate) {
        this.candidateStartDate = candidateStartDate;
    }

    public int getCandidateStarts() {
        return candidateStarts;
    }

    public void setCandidateStarts(int candidateStarts) {
        this.candidateStarts = candidateStarts;
    }

    public int getCandidateSeconds() {
        return candidateSeconds;
    }

    public void setCandidateSeconds(int candidateSeconds) {
        this.candidateSeconds = candidateSeconds;
    }

    public RcOrgPrefs getRcOrgPrefs() {
        return rcOrgPrefs;
    }

    public void setRcOrgPrefs(RcOrgPrefs rcOrgPrefs) {
        this.rcOrgPrefs = rcOrgPrefs;
    }

    /*
       This is the number of supervisors to be added by the candidate
    */
    public int getMinSupervisorsCandidate()
    {
        // return minSupervisors;
        
        // Count non-candidates values.
        int c = 0;
        if( rcRaterList!=null )
        {
            for( RcRater r : rcRaterList )
            {
                if( !r.getRcRaterRoleType().getIsSupervisorOrManager() )
                    continue;
                if( !r.getRcRaterSourceType().getIsAccountUser()  )
                    continue;
                c++;
            }
        }
        int mr = minSupervisors - c;
        return mr>=0 ? mr : 0;
        
    }

    
    /**
     * This is the number or raters to be entered by the candidate.
     * @return 
     */
    public int getMinRatersCandidate()
    {
        int c = 0;
        if( rcRaterList!=null )
        {
            for( RcRater r : rcRaterList )
            {
                // don't count self
                if( r.getIsCandidateOrEmployee() )
                    continue;
                
                // don't count non-candidate
                if( !r.getRcRaterSourceType().getIsAccountUser() )
                    continue;
                c++;
            }
        }
        int mr = minRaters - c;
        return mr>=0 ? mr : 0;
    }

    public int getMinRaters() {
        return minRaters;
    }

    public void setMinRaters(int minRaters) {
        this.minRaters = minRaters;
    }

    public int getMaxRaters() {
        return maxRaters;
    }

    public int getMaxRatersDefault() {
        return maxRaters>0 ? maxRaters : Constants.DEFAULT_MAX_RATERS;
    }
    
    public int getMinRatersOptimum()
    {
        int gap = getMaxRatersDefault() - getMinRaters();
        return getMinRaters() + Math.round( ((float)gap)/2f );
    }

    public void setMaxRaters(int maxRaters) {
        this.maxRaters = maxRaters;
    }

    public int getEnforceRaterLimits() {
        return enforceRaterLimits;
    }

    public void setEnforceRaterLimits(int enforceRaterLimits) {
        this.enforceRaterLimits = enforceRaterLimits;
    }

    public Date getCandidateReleaseDate() {
        return candidateReleaseDate;
    }

    public void setCandidateReleaseDate(Date candidateReleaseDate) {
        this.candidateReleaseDate = candidateReleaseDate;
    }

    public int getRcCandidateStatusTypeId() {
        return rcCandidateStatusTypeId;
    }

    public void setRcCandidateStatusTypeId(int candidateStatusTypeId) {
        this.rcCandidateStatusTypeId = candidateStatusTypeId;
    }

    public Date getCandidateLastUpdate() {
        return candidateLastUpdate;
    }

    public void setCandidateLastUpdate(Date candidateLastUpdate) {
        this.candidateLastUpdate = candidateLastUpdate;
    }

    //public int getDisableCandidateDirectInput() {
    //    return disableCandidateDirectInput;
    //}

    //public void setDisableCandidateDirectInput(int disableCandidateDirectInput) {
    //    this.disableCandidateDirectInput = disableCandidateDirectInput;
    //}

    public int getRcCheckScoringStatusTypeId() {
        return rcCheckScoringStatusTypeId;
    }

    public void setRcCheckScoringStatusTypeId(int rcCheckScoringStatusTypeId) {
        this.rcCheckScoringStatusTypeId = rcCheckScoringStatusTypeId;
    }

    public Date getScoreDate() {
        return scoreDate;
    }

    public void setScoreDate(Date scoreDate) {
        this.scoreDate = scoreDate;
    }

    public Date getLastCandidateReminderDate() {
        return lastCandidateReminderDate;
    }

    public void setLastCandidateReminderDate(Date lastCandidateReminderDate) {
        this.lastCandidateReminderDate = lastCandidateReminderDate;
    }

    public int getMinSupervisors() {
        return minSupervisors;
    }

    public void setMinSupervisors(int minSupervisors) {
        this.minSupervisors = minSupervisors;
    }

    public Date getLastSecondsDate() {
        return lastSecondsDate;
    }

    public void setLastSecondsDate(Date lastSecondsDate) {
        this.lastSecondsDate = lastSecondsDate;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public int getCollectCandidateRatings() {
        return collectCandidateRatings;
    }

    public void setCollectCandidateRatings(int collectCandidateRatings) {
        this.collectCandidateRatings = collectCandidateRatings;
    }

    public Date getLastCandidateProgressMsgDate() {
        return lastCandidateProgressMsgDate;
    }

    public void setLastCandidateProgressMsgDate(Date lastCandidateProgressMsgDate) {
        this.lastCandidateProgressMsgDate = lastCandidateProgressMsgDate;
    }

    public Date getLastProgressMsgDate() {
        return lastProgressMsgDate;
    }

    public void setLastProgressMsgDate(Date lastProgressMsgDate) {
        this.lastProgressMsgDate = lastProgressMsgDate;
    }

    public boolean getCandidateCanAddRaters() {
        return candidateCannotAddRaters==0;
    }

    
    
    public int getCandidateCannotAddRaters() {
        return candidateCannotAddRaters;
    }

    public void setCandidateCannotAddRaters(int candidateCannotAddRaters) {
        this.candidateCannotAddRaters = candidateCannotAddRaters;
    }

    public String getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(String customParameters) {
        this.customParameters = customParameters;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getReportId2() {
        return reportId2;
    }

    public void setReportId2(int reportId2) {
        this.reportId2 = reportId2;
    }

    public Date getFirstCandidateSendDate() {
        return firstCandidateSendDate;
    }

    public void setFirstCandidateSendDate(Date firstCandidateSendDate) {
        this.firstCandidateSendDate = firstCandidateSendDate;
    }

    public Date getFirstCandidateReferenceDate() {
        return firstCandidateReferenceDate;
    }

    public void setFirstCandidateReferenceDate(Date firstCandidateReferenceDate) {
        this.firstCandidateReferenceDate = firstCandidateReferenceDate;
    }

    public Date getLastCandidateReferenceDate() {
        return lastCandidateReferenceDate;
    }

    public void setLastCandidateReferenceDate(Date lastCandidateReferenceDate) {
        this.lastCandidateReferenceDate = lastCandidateReferenceDate;
    }

    public Date getLastCandidateSendDate() {
        return lastCandidateSendDate;
    }

    public void setLastCandidateSendDate(Date lastCandidateSendDate) {
        this.lastCandidateSendDate = lastCandidateSendDate;
    }

    public int getCreditIndex() {
        return creditIndex;
    }

    public void setCreditIndex(int creditIndex) {
        this.creditIndex = creditIndex;
    }

    public int getForceAllAnonymous() {
        return forceAllAnonymous;
    }

    public void setForceAllAnonymous(int forceAllAnonymous) {
        this.forceAllAnonymous = forceAllAnonymous;
    }

    public RcSuborgPrefs getRcSuborgPrefs() {
        return rcSuborgPrefs;
    }

    public void setRcSuborgPrefs(RcSuborgPrefs rcSuborgPrefs) {
        this.rcSuborgPrefs = rcSuborgPrefs;
    }

    public String getIpCountry() {
        return ipCountry;
    }

    public void setIpCountry(String ipCountry) {
        this.ipCountry = ipCountry;
    }

    public String getIpState() {
        return ipState;
    }

    public void setIpState(String ipState) {
        this.ipState = ipState;
    }

    public String getIpCity() {
        return ipCity;
    }

    public void setIpCity(String ipCity) {
        this.ipCity = ipCity;
    }

    public int getCandidatePhotoCaptureTypeId() {
        return candidatePhotoCaptureTypeId;
    }

    public void setCandidatePhotoCaptureTypeId(int candidatePhotoCaptureTypeId) {
        this.candidatePhotoCaptureTypeId = candidatePhotoCaptureTypeId;
    }

    public int getRaterPhotoCaptureTypeId() {
        return raterPhotoCaptureTypeId;
    }

    public void setRaterPhotoCaptureTypeId(int raterPhotoCaptureTypeId) {
        this.raterPhotoCaptureTypeId = raterPhotoCaptureTypeId;
    }

    public List<RcUploadedUserFile> getRcUploadedUserFileList() {
        return rcUploadedUserFileList;
    }

    public void setRcUploadedUserFileList(List<RcUploadedUserFile> rcUploadedUserFileList) {
        this.rcUploadedUserFileList = rcUploadedUserFileList;
    }

    public RcUploadedUserFile getCandidatePhotoUploadedUserFile()
    {
        return getUploadedUserFile( UploadedUserFileType.REF_CHECK_IMAGES );
    }
    
    public RcUploadedUserFile getCandidateIdUploadedUserFile()
    {
        return getUploadedUserFile( UploadedUserFileType.REF_CHECK_ID );
    }
    
    private RcUploadedUserFile getUploadedUserFile( UploadedUserFileType uft )
    {
        if( this.rcUploadedUserFileList==null || this.rcUploadedUserFileList.isEmpty() )
            return null;
        for( RcUploadedUserFile f : this.rcUploadedUserFileList )
        {
            if( f.getUploadedUserFileType().equals(uft) )
                return f;
        }
        return null;
    }

    public List<RcUploadedUserFile> getFauxRcUploadedUserFileList() {
        return fauxRcUploadedUserFileList;
    }

    public void setFauxRcUploadedUserFileList(List<RcUploadedUserFile> fauxRcUploadedUserFileList) {
        this.fauxRcUploadedUserFileList = fauxRcUploadedUserFileList;
    }

    public int getAvCommentsTypeId() {
        return avCommentsTypeId;
    }

    public void setAvCommentsTypeId(int avCommentsTypeId) {
        this.avCommentsTypeId = avCommentsTypeId;
    }

    public int getAskForReferrals() {
        return askForReferrals;
    }

    public void setAskForReferrals(int askForReferrals) {
        this.askForReferrals = askForReferrals;
    }

    public int getTopBottomCount() {
        return topBottomCount;
    }

    public void setTopBottomCount(int topBottomCount) {
        this.topBottomCount = topBottomCount;
    }

    public int getTopBottomSrcTypeId() {
        return topBottomSrcTypeId;
    }

    public void setTopBottomSrcTypeId(int topBottomSrcTypeId) {
        this.topBottomSrcTypeId = topBottomSrcTypeId;
    }

    
    
}
