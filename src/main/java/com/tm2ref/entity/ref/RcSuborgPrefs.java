package com.tm2ref.entity.ref;


import java.io.Serializable;
import java.util.Date;
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
@Table( name = "rcsuborgprefs" )
@NamedQueries({
        @NamedQuery( name = "RcSuborgPrefs.findBySuborgId", query = "SELECT o FROM RcSuborgPrefs AS o WHERE o.suborgId=:suborgId" )    
})
public class RcSuborgPrefs implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="rcsuborgprefsid")
    private int rcSuborgPrefsId;

    @Column(name="orgid")
    private int orgId;

    @Column(name="suborgid")
    private int suborgId;
    
    @Column(name="corpid")
    private int corpId = -1;

    @Column(name="remindertypeid")
    private int reminderTypeId=-1;

    
    @Column(name="distributiontypeid")
    private int distributionTypeId=-1;
    
    @Column(name="defaultrcchecktypeid")
    private int defaultRcCheckTypeId=-1;

    @Column(name="minsupervisors")
    private int minSupervisors = -1;

    @Column(name="candidatecannotaddraters")
    private int candidateCannotAddRaters=-1;

    
    @Column(name="minraters")
    private int minRaters = -1;

    @Column(name="maxraters")
    private int maxRaters = -1;
    
    @Column(name="collectcandidateratings")
    private int collectCandidateRatings = -1;

    @Column(name="askforreferrals")
    private int askForReferrals=-1;
    
    
    @Column(name="invitationsubj")
    private String invitationSubj;

    @Column(name="invitationsubjrater")
    private String invitationSubjRater;

    @Column(name="invitation")
    private String invitation;

    @Column(name="invitationrater")
    private String invitationRater;

    @Column(name="otherrolename")
    private String otherRoleName;

    @Column(name="otherrolename2")
    private String otherRoleName2;

    @Column(name="otherrolename3")
    private String otherRoleName3;
    
   
    @Column(name="defaultrcscriptid")
    private int defaultRcScriptId=-1;
   
    @Column(name="candidatephotocapturetypeid")
    private int candidatePhotoCaptureTypeId;
            
    @Column(name="raterphotocapturetypeid")
    private int raterPhotoCaptureTypeId;
    
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;
   
    
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
        
    public boolean hasRoleNameData()
    {
        return (otherRoleName!=null && !otherRoleName.isBlank()) || 
               (otherRoleName2!=null && !otherRoleName2.isBlank()) || 
                (otherRoleName3!=null && !otherRoleName3.isBlank());
    }
    
    public String[] getOtherRoleTypeNames()
    {
        return new String[] { this.otherRoleName==null || otherRoleName.isBlank() ? null : otherRoleName, 
                              this.otherRoleName2==null || otherRoleName2.isBlank() ? null : otherRoleName2,
                              this.otherRoleName3==null || otherRoleName3.isBlank() ? null : otherRoleName3 };
    }
    
    @Override
    public String toString() {
        return "RcSuborgPrefs{" + "rcSuborgPrefsId=" + rcSuborgPrefsId + ", orgId=" + orgId + ", suborgId=" + suborgId + '}';
    }

    public int getRcSuborgPrefsId() {
        return rcSuborgPrefsId;
    }

    public void setRcSuborgPrefsId(int rcSuborgPrefsId) {
        this.rcSuborgPrefsId = rcSuborgPrefsId;
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


    public int getCorpId() {
        return corpId;
    }

    public void setCorpId(int corpId) {
        this.corpId = corpId;
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



    public int getDistributionTypeId() {
        return distributionTypeId;
    }

    public void setDistributionTypeId(int distributionTypeId) {
        this.distributionTypeId = distributionTypeId;
    }

    public int getDefaultRcScriptId() {
        return defaultRcScriptId;
    }

    public void setDefaultRcScriptId(int defaultRcScriptId) {
        this.defaultRcScriptId = defaultRcScriptId;
    }

    public int getDefaultRcCheckTypeId() {
        return defaultRcCheckTypeId;
    }

    public void setDefaultRcCheckTypeId(int defaultRcCheckTypeId) {
        this.defaultRcCheckTypeId = defaultRcCheckTypeId;
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

    public void setMaxRaters(int maxRaters) {
        this.maxRaters = maxRaters;
    }


    public int getMinSupervisors() {
        return minSupervisors;
    }

    public void setMinSupervisors(int minSupervisors) {
        this.minSupervisors = minSupervisors;
    }


    public int getCollectCandidateRatings() {
        return collectCandidateRatings;
    }

    public void setCollectCandidateRatings(int collectCandidateRatings) {
        this.collectCandidateRatings = collectCandidateRatings;
    }
    
    public boolean getCollectCandidateRatingsB() {
        return collectCandidateRatings==1;
    }

    public void setCollectCandidateRatingsB( boolean b) {
        this.collectCandidateRatings = b ? 1 : 0;
    }

    public int getCandidateCannotAddRaters() {
        return candidateCannotAddRaters;
    }

    public void setCandidateCannotAddRaters(int candidateCannotAddRaters) {
        this.candidateCannotAddRaters = candidateCannotAddRaters;
    }
    
    public boolean getCandidateCanAddRatersB() {
        return candidateCannotAddRaters<=0;
    }

    public void setCandidateCanAddRatersB(boolean b) {
        this.candidateCannotAddRaters = b ? 0 : 1;
    }



    public String getInvitationSubj() {
        return invitationSubj;
    }

    public void setInvitationSubj(String invitationSubj) {
        this.invitationSubj = invitationSubj;
    }

    public String getInvitationSubjRater() {
        return invitationSubjRater;
    }

    public void setInvitationSubjRater(String invitationSubjRater) {
        this.invitationSubjRater = invitationSubjRater;
    }

    public String getInvitation() {
        return invitation;
    }

    public void setInvitation(String invitation) {
        this.invitation = invitation;
    }

    public String getInvitationRater() {
        return invitationRater;
    }

    public void setInvitationRater(String invitationRater) {
        this.invitationRater = invitationRater;
    }

    public String getOtherRoleName() {
        return otherRoleName;
    }

    public void setOtherRoleName(String otherRoleName) {
        this.otherRoleName = otherRoleName;
    }

    public String getOtherRoleName2() {
        return otherRoleName2;
    }

    public void setOtherRoleName2(String otherRoleName2) {
        this.otherRoleName2 = otherRoleName2;
    }

    public String getOtherRoleName3() {
        return otherRoleName3;
    }

    public void setOtherRoleName3(String otherRoleName3) {
        this.otherRoleName3 = otherRoleName3;
    }

    public int getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(int suborgId) {
        this.suborgId = suborgId;
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

    public int getAskForReferrals() {
        return askForReferrals;
    }

    public void setAskForReferrals(int askForReferrals) {
        this.askForReferrals = askForReferrals;
    }


    
}
