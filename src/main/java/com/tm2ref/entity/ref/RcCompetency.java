package com.tm2ref.entity.ref;


import com.tm2ref.ref.RcCompetencyStatusType;
import com.tm2ref.ref.RcCompetencyType;
import com.tm2ref.ref.RcImportanceType;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;


@Entity
@Table( name = "rccompetency" )
@NamedQueries({    
})
public class RcCompetency implements Serializable, Comparable<RcCompetency>, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="rccompetencyid")
    private int rcCompetencyId;

    @Column(name="orgid")
    private int orgId;
    
    @Column(name="identifier")
    private String identifier;

    @Column(name="authoruserid")
    private long authorUserId;
                
    @Column(name="orgaccesstypeid")
    private int orgAccessTypeId;

    @Column(name="rccompetencytypeid")
    private int rcCompetencyTypeId;

    @Column(name="rccompetencysubtypeid")
    private int rcCompetencySubTypeId;
    
    @Column(name="rccompetencystatustypeid")
    private int rcCompetencyStatusTypeId;

    @Column(name="displayorder")
    private int displayOrder;

    
    /**
     * Comma-delimited list of ONET ElementIds to be used in calculating weights.
     */
    @Column(name="onetelementids")
    private String onetElementIds;
    
    @Column(name="importancetypeid")
    private int importanceTypeId;

    
    @Column(name="langcode")
    private String langCode;
    
    @Column(name="name")
    private String name;

    @Column(name="nameenglish")
    private String nameEnglish;
    
    @Column(name="description")
    private String description;

    @Column(name="interviewquestion")
    private String interviewQuestion;
    
    @Column(name="anchorlow")
    private String anchorLow;
    
    @Column(name="anchorhi")
    private String anchorHi;
    
    @Column(name="developmentsuggestions")
    private String developmentSuggestions;
    

    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;
    
    //@Transient
    //private List<RcItem> rcItemList;
    
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString() {
        return "RcCompetency{" + "rcCompetencyId=" + rcCompetencyId + ", orgId=" + orgId + ", name=" + name + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.rcCompetencyId;
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
        final RcCompetency other = (RcCompetency) obj;
        if (this.rcCompetencyId != other.rcCompetencyId) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(RcCompetency o) {
        
        if( this.rcCompetencyTypeId!=o.getRcCompetencyTypeId() )
            return ((Integer)this.rcCompetencyTypeId).compareTo(o.getRcCompetencyTypeId());
        if( name!=null && o.getName()!=null )
            return name.compareTo(o.getName() );
        return 0;
    }
    
    
    public boolean getHasInterviewQuestion()
    {
        return interviewQuestion!=null && !interviewQuestion.isBlank();
    }
    
    public RcCompetencyType getRcCompetencyType()
    {
        return RcCompetencyType.getValue( rcCompetencyTypeId );
    }
    
    public RcCompetencyStatusType getRcCompetencyStatusType()
    {
        return RcCompetencyStatusType.getValue( rcCompetencyStatusTypeId );
    }
    
    public RcImportanceType getRcCompetencyImportanceType()
    {
        return RcImportanceType.getValue( importanceTypeId );
    }
    
    public boolean getUsesOnet()
    {
        return onetElementIds!=null && !onetElementIds.isBlank();
    }
    
    public int getRcCompetencyId() {
        return rcCompetencyId;
    }

    public void setRcCompetencyId(int rcCompetencyId) {
        this.rcCompetencyId = rcCompetencyId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public long getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(long authorUserId) {
        this.authorUserId = authorUserId;
    }

    public int getOrgAccessTypeId() {
        return orgAccessTypeId;
    }

    public void setOrgAccessTypeId(int orgAccessTypeId) {
        this.orgAccessTypeId = orgAccessTypeId;
    }

    public int getRcCompetencyTypeId() {
        return rcCompetencyTypeId;
    }

    public void setRcCompetencyTypeId(int rcCompetencyTypeId) {
        this.rcCompetencyTypeId = rcCompetencyTypeId;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getOnetElementIds() {
        return onetElementIds;
    }

    public void setOnetElementIds(String onetElementIds) {
        this.onetElementIds = onetElementIds;
    }


    public int getImportanceTypeId() {
        return importanceTypeId;
    }

    public void setImportanceTypeId(int importanceTypeId) {
        this.importanceTypeId = importanceTypeId;
    }

    public String getInterviewQuestion() {
        return interviewQuestion;
    }

    public void setInterviewQuestion(String interviewQuestion) {
        this.interviewQuestion = interviewQuestion;
    }

    public String getAnchorLow() {
        return anchorLow;
    }

    public void setAnchorLow(String anchorLow) {
        this.anchorLow = anchorLow;
    }

    public String getAnchorHi() {
        return anchorHi;
    }

    public void setAnchorHi(String anchorHi) {
        this.anchorHi = anchorHi;
    }



    public String getDevelopmentSuggestions() {
        return developmentSuggestions;
    }

    public void setDevelopmentSuggestions(String developmentSuggestions) {
        this.developmentSuggestions = developmentSuggestions;
    }

    public int getRcCompetencyStatusTypeId() {
        return rcCompetencyStatusTypeId;
    }

    public void setRcCompetencyStatusTypeId(int rcCompetencyStatusTypeId) {
        this.rcCompetencyStatusTypeId = rcCompetencyStatusTypeId;
    }

    public int getRcCompetencySubTypeId() {
        return rcCompetencySubTypeId;
    }

    public void setRcCompetencySubTypeId(int rcCompetencySubTypeId) {
        this.rcCompetencySubTypeId = rcCompetencySubTypeId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    
}
