package com.tm2ref.entity.ref;


import com.tm2ref.ref.RcCompetencyWrapper;
import com.tm2ref.ref.RcImportanceType;
import com.tm2ref.ref.RcItemWrapper;
import com.tm2ref.ref.RcRatingScaleType;
import com.tm2ref.service.LogService;
import com.tm2ref.util.JsonUtils;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
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
@Table( name = "rcscript" )
@NamedQueries({
    @NamedQuery( name = "RcScript.findByRcScriptId", query = "SELECT o FROM RcScript AS o WHERE o.rcScriptId=:rcScriptId" ),
})
public class RcScript implements Serializable, Cloneable, Comparable<RcScript>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="rcscriptid")
    private int rcScriptId;

    @Column(name="orgid")
    private int orgId;
    
    @Column(name="suborgid")
    private int suborgId;
    
    @Column(name="authoruserid")
    private long authorUserId;
                
    @Column(name="orgaccesstypeid")
    private int orgAccessTypeId;

    @Column(name="rcscriptstatustypeid")
    private int rcScriptStatusTypeId;

    /**
     * 0=pre-hire
     * 1=employee
     */
    @Column(name="rcchecktypeid")
    private int rcCheckTypeId;
    
    @Column(name="langcode")
    private String langCode;

    /**
     * 0=no
     * 1=audio
     * 2=video
     */
    @Column(name="audiovideook")
    private int audioVideoOk;

    @Column(name="idealscoresok")
    private int idealScoresOk;
    
    @Column(name="ratingscaletypeid")
    private int ratingScaleTypeId;
    
    
    @Column(name="nocommentsratingitems")
    private int noCommentsRatingItems;
    
    
    
    @Column(name="usediscreteratings")
    private int useDiscreteRatings;
    
    @Column(name="allcommentsrequired")
    private int allCommentsRequired;

    
    /**
     * This is the soc that was used to calculate the weights, if any. 
     */
    @Column(name="onetsoccode")
    private String onetSocCode;

    
    /**
     * {
     *     rcscriptid:rcScriptId,
     *     competencies: [
     *                      {   
     *                          rccompetencyid: rcCompetencyId,
     *                          onetelementid: onet elementid,  (since a competency can have multiple, store the actual one here)
     *                          displayorder: display order,
                                onetimportance: onet importance value,
                                userimportancetypeid: user importance typeid,
                                idealscore: float ideal score for this competency for this script.
     *                          items: [
     *                                     { 
     *                                         rcitemid: rcItemId,
     *                                         weight: weight for item
                                               displayorder: display order
     *                                     }
     *                                 ]
     *                      }
     *                   ],
     *      specialinstructionscandidate:special instructions
     *      specialinstructionsraters:special instructions
     *     
     * }     */
    @Column(name="scriptjson")
    private String scriptJson;


    
    @Column(name="name")
    private String name;
    
    @Column(name="note")
    private String note;
    
    @Column(name="candidatestr1title")
    private String candidateStr1Title;
    
    @Column(name="candidatestr2title")
    private String candidateStr2Title;
    
    @Column(name="candidatestr3title")
    private String candidateStr3Title;
    
    @Column(name="candidatestr4title")
    private String candidateStr4Title;
    
    @Column(name="candidatestr5title")
    private String candidateStr5Title;

    @Column(name="candidatestr1question")
    private String candidateStr1Question;
    
    @Column(name="candidatestr2question")
    private String candidateStr2Question;
    
    @Column(name="candidatestr3question")
    private String candidateStr3Question;
    
    @Column(name="candidatestr4question")
    private String candidateStr4Question;
    
    @Column(name="candidatestr5question")
    private String candidateStr5Question;
    
    @Column(name="forceallanonymous")
    private int forceAllAnonymous;
        
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;
    
    
    @Transient
    private List<RcCompetencyWrapper> rcCompetencyWrapperList;
    
    @Transient
    private List<RcItemWrapper> rcItemWrapperList;
    
    @Transient
    private Locale locale;
    
    @Transient
    private String specialInstructionsCandidate;
    
    @Transient
    private String specialInstructionsRaters;
    
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString() {
        return "RcScript{" + "rcScriptId=" + rcScriptId + ", orgId=" + orgId + ", name=" + name + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.rcScriptId;
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
        final RcScript other = (RcScript) obj;
        if (this.rcScriptId != other.rcScriptId) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(RcScript o) 
    {
        
        if( name!=null && !name.isBlank() && o.getName()!=null )
            return name.compareTo(o.getName() );
        
        return ((Integer)this.rcScriptId).compareTo( o.getRcScriptId() );
    }

    public int getItemCount(boolean isCandidateOrEmployee)
    {
        if( rcCompetencyWrapperList==null )
            return 0;
        int c = 0;
        for( RcCompetencyWrapper rcw : rcCompetencyWrapperList )
        {
            if( rcw.getRcItemWrapperList()==null )
                continue;
            for( RcItemWrapper rciw : rcw.getRcItemWrapperList() )
            {
                if( isCandidateOrEmployee && rciw.getRcItem().getSkipforCandidate()>0 )
                    continue;
                c++;
            }
        }
        
        return c;
    }
    
    public int getItemsAnswered(boolean isCandidateOrEmployee)
    {
        if( rcCompetencyWrapperList==null )
            return 0;
        int c = 0;
        for( RcCompetencyWrapper rcw : rcCompetencyWrapperList )
        {
            if( rcw.getRcItemWrapperList()==null )
                continue;
            for( RcItemWrapper rciw : rcw.getRcItemWrapperList() )
            {
                if( isCandidateOrEmployee && rciw.getRcItem().getSkipforCandidate()>0 )
                    continue;
                if( rciw.getIsCompleteOrHigher() )
                    c++;
            }
        }        
        return c;
    }
    
    public RcRatingScaleType getRcRatingScaleType()
    {
        return RcRatingScaleType.getValue( ratingScaleTypeId );
    }
    
    
    public void clearRatings()
    {
        for( RcItemWrapper rciw : getAllItemWrapperList() )
        {
            rciw.clearRatings();
        }
    }
    
    
    public synchronized List<RcItemWrapper> getAllItemWrapperList()
    {
        
        if( rcItemWrapperList!=null )
            return rcItemWrapperList;
        
        List<RcItemWrapper> out = new ArrayList<>();        
        if( rcCompetencyWrapperList==null )
            return out;
        
        for( RcCompetencyWrapper rcw : rcCompetencyWrapperList )
        {
            if( rcw.getRcItemWrapperList()==null )
                continue;
            out.addAll( rcw.getRcItemWrapperList() );
        }
         
        int count = 0;
        int candCount = 0;
        for( RcItemWrapper w : out )
        {
            w.setRaterDisplayOrder(++count);
            if( w.getRcItem()==null || w.getRcItem().getSkipforCandidate()<=0 )
                w.setCandidateDisplayOrder( ++candCount );
        }
        
        rcItemWrapperList = out;        
        return out;
    }
    
    public List<RcCompetency> getRcCompetencyList()
    {
        List<RcCompetency> o = new ArrayList<>();        
        if( rcCompetencyWrapperList==null )
            return o;
        
        for( RcCompetencyWrapper i : rcCompetencyWrapperList )
        {
            if( i.getRcCompetency()==null )
                continue;
            if( o.contains( i.getRcCompetency() ) )
                continue;
            o.add( i.getRcCompetency() );
        }
        Collections.sort( o );
        return o;
    }
    
    public int getActiveCompetencyCount()
    {
        return computeCounts( true )[0];
    }
    
    public int getActiveItemCount()
    {
        return computeCounts( true )[1];
    }

    /*
     returns int[]
       data[0]=competencies
       data[1]=items
    */
    private int[] computeCounts( boolean activeOnly )
    {
        int[] out = new int[2];
        if( rcCompetencyWrapperList==null )
            return out;
        //int c = 0;
        for( RcCompetencyWrapper rcw : rcCompetencyWrapperList )
        {
            if( activeOnly && !rcw.getRcCompetency().getRcCompetencyStatusType().getIsActive() )
                continue;
            
            // count competency
            out[0]++;
            
            if( rcw.getRcItemWrapperList()==null )
                continue;
            out[1] += rcw.getRcItemWrapperList().size();
        }
        
        return out;
    }
    
    
    
    public synchronized void parseScriptJson()
    {
       rcCompetencyWrapperList = new ArrayList<>();
       
       if( scriptJson==null || scriptJson.isBlank() )
           return;
       
       try
       {
            JsonReader r = Json.createReader( new StringReader(scriptJson) );
            JsonObject top = r.readObject();
            RcCompetencyWrapper compWrap;
            RcItemWrapper itemWrap;
            JsonArray itemArray;
            
            if( top.containsKey("competencies") && !top.isNull("competencies") )
            {
                JsonArray ja = top.getJsonArray("competencies" );
                
                for( JsonObject comp : ja.getValuesAs(JsonObject.class) )
                {
                    compWrap = new RcCompetencyWrapper();
                    rcCompetencyWrapperList.add(compWrap);

                    if( comp.containsKey( "rccompetencyid" ) )
                        compWrap.setRcCompetencyId( comp.getInt("rccompetencyid"));
                    else
                        throw new Exception("No competency id in json." );
                    
                    if( comp.containsKey( "displayorder" ) )
                        compWrap.setDisplayOrder( comp.getInt("displayorder"));
                    if( comp.containsKey("onetelementid") && !comp.isNull( "onetelementid" ) )
                        compWrap.setOnetElementId( comp.getString("onetelementid"));
                    if( comp.containsKey("onetimportance") )
                        compWrap.setOnetImportance(  (float) comp.getJsonNumber("onetimportance").doubleValue() );
                    if( comp.containsKey( "userimportancetypeid" ) )
                        compWrap.setUserImportanceTypeId(comp.getInt("userimportancetypeid"));
                    if( comp.containsKey( "idealscore" ) )
                        compWrap.setIdealScore( (float) comp.getJsonNumber("idealscore").doubleValue());
                    
                    if( comp.containsKey( "items" ) )
                    {
                        itemArray = comp.getJsonArray( "items" );
                        for( JsonObject itemObj : itemArray.getValuesAs( JsonObject.class ) )
                        {
                            itemWrap = new RcItemWrapper();
                            // itemWrap.setDisplayOrder( itemObj.containsKey("displayorder") ? itemObj.getInt("displayorder") : 0 );
                            
                            if( itemObj.containsKey("rcitemid") )
                                itemWrap.setRcItemId( itemObj.getInt("rcitemid") );
                            else
                                throw new Exception( "Item doesn't have any rcItemId." );
                            if( itemObj.containsKey("weight") )
                                itemWrap.setWeight( (float) itemObj.getJsonNumber("weight").doubleValue()  );
                            
                            compWrap.addItemWrapper(itemWrap);
                        }
                    }
                }
            } 
            if( top.containsKey( "specialinstructionscandidate") && !top.isNull( "specialinstructionscandidate" ) )
                specialInstructionsCandidate = top.getString( "specialinstructionscandidate" );
            
            if( top.containsKey( "specialinstructionsraters") && !top.isNull( "specialinstructionsraters" ) )
                specialInstructionsRaters = top.getString( "specialinstructionsraters" );
            
       }
       catch( Exception e )
       {
           LogService.logIt( e, "RcScript.parseScriptJson() scriptJson=" + scriptJson );
       }
    }
    
    
    public synchronized void writeScriptJson()
    {
        try
        {
            scriptJson=null;
            
            JsonObjectBuilder top = Json.createObjectBuilder();
            top.add( "rcscriptid",  rcScriptId );
            
            JsonArrayBuilder compJab = Json.createArrayBuilder();

            JsonObjectBuilder comp;
            JsonObjectBuilder item;
            JsonArrayBuilder itemJab;
            
            if( rcCompetencyWrapperList!=null )
            {
                setItemWeights();
                
                for( RcCompetencyWrapper rcw : rcCompetencyWrapperList )
                {
                    if( rcw.getRcCompetencyId()<=0 )
                        continue;
                    
                    comp = Json.createObjectBuilder();
                    comp.add( "displayorder", rcw.getDisplayOrder() );
                    if( rcw.getOnetElementId()!=null && !rcw.getOnetElementId().isBlank() )
                        comp.add( "onetelementid", rcw.getOnetElementId() );
                    comp.add( "onetimportance", rcw.getOnetImportance() );
                    comp.add( "rccompetencyid", rcw.getRcCompetencyId() );
                    comp.add( "userimportancetypeid", rcw.getUserImportanceTypeId() );                    
                    //LogService.logIt( "RcScript.writeScriptJson() CompWrapper Adding rcCompetencyId=" + rcw.getRcCompetencyId() );
                    
                    itemJab = Json.createArrayBuilder();
                    if( rcw.getRcItemWrapperList()!=null )
                    {
                        for( RcItemWrapper rciw : rcw.getRcItemWrapperList() )
                        {
                            if( rciw.getRcItemId()<=0 )
                                continue;
                            item = Json.createObjectBuilder();
                            // item.add( "displayorder", rciw.getDisplayOrder() );
                            item.add( "rcitemid", rciw.getRcItemId() );
                            item.add( "weight", rciw.getWeight() );
                            //LogService.logIt( "RcScript.writeScriptJson() ItemWrapper adding rcItemId=" + rciw.getRcItemId() );
                            itemJab.add(item.build());
                        }                        
                    }                    
                    comp.add("items", itemJab );
                    compJab.add( comp );
                }
            }

            top.add( "competencies", compJab );
            
            if( specialInstructionsCandidate!=null && !specialInstructionsCandidate.isBlank() )
                top.add("specialinstructionscandidate", specialInstructionsCandidate );

            if( specialInstructionsRaters!=null && !specialInstructionsRaters.isBlank() )
                top.add("specialinstructionsraters", specialInstructionsRaters );
            
            scriptJson = JsonUtils.getJsonObjectAsString(top.build()); 
            
            //LogService.logIt( "RcScript.writeScriptJson()  scriptJson=" + scriptJson );
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RcScript.writeScriptJson() scriptJson=" + scriptJson );
        }
    }
    
    
    public void setItemWeights()
    {
        if( rcCompetencyWrapperList!=null )
        {
            float wt;
            float wtPerItem;
            RcImportanceType rcit;
            for( RcCompetencyWrapper rcw : rcCompetencyWrapperList )
            {
                rcit = rcw.getUserRcImportanceType();
                
                if( rcit.equals( RcImportanceType.USE_ONET ) )
                    wt = rcw.getOnetImportance();
                else
                    wt = rcit.getImportance();
                if( rcw.getRcItemWrapperList()!=null )
                {
                    wtPerItem = rcw.getRcItemWrapperList().isEmpty() ? 0 : wt/((float)rcw.getRcItemWrapperList().size());
                    
                    for( RcItemWrapper rciw : rcw.getRcItemWrapperList() )
                    {
                        rciw.setWeight( wtPerItem);
                    }                        
                }                    
            }
        }        
    }
    
    
    
    
    public String getCandidateStrQuestion( int idx )
    {
        switch (idx )
        {
            case 1:
                return candidateStr1Question;
            case 2:
                return candidateStr2Question;
            case 3:
                return candidateStr3Question;
            case 4:
                return candidateStr4Question;
            case 5:
                return candidateStr5Question;
            default:
                return null;
        }
    }
    
    public boolean getHasCandidateInput( int idx )
    {
        switch (idx )
        {
            case 1:
                return getHasCandidateInput1();
            case 2:
                return getHasCandidateInput2();
            case 3:
                return getHasCandidateInput3();
            case 4:
                return getHasCandidateInput4();
            case 5:
                return getHasCandidateInput5();
            default:
                return false;
        }        
    }
    
    public int getCandidateQuestionCount()
    {
        int n = 0;
        for( int i=1;i<=5;i++ )
        {
            if( getHasCandidateInput( i ) )
                n++;
        }
        return n;
    }
    
    public boolean getHasAnyCandidateRatings()
    {
        if( this.getRcCompetencyWrapperList()==null )
            return false;
        
        return getItemCount( true )>0;        
    }
    
    public boolean getHasAnyCandidateInput()
    {
        return getHasCandidateInput1() || getHasCandidateInput2() || getHasCandidateInput3() || getHasCandidateInput4() || getHasCandidateInput5();
    }
    
    public boolean getHasCandidateInput1()
    {
        return candidateStr1Question!=null && !candidateStr1Question.isBlank();        
    }
    public boolean getHasCandidateInput2()
    {
        return candidateStr2Question!=null && !candidateStr2Question.isBlank();        
    }
    public boolean getHasCandidateInput3()
    {
        return candidateStr3Question!=null && !candidateStr3Question.isBlank();        
    }
    public boolean getHasCandidateInput4()
    {
        return candidateStr4Question!=null && !candidateStr4Question.isBlank();        
    }
    public boolean getHasCandidateInput5()
    {
        return candidateStr5Question!=null && !candidateStr5Question.isBlank();        
    }

    

    public int getRcScriptId() {
        return rcScriptId;
    }

    public void setRcScriptId(int rcScriptId) {
        this.rcScriptId = rcScriptId;
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

    public int getRcScriptStatusTypeId() {
        return rcScriptStatusTypeId;
    }

    public void setRcScriptStatusTypeId(int rcScriptStatusTypeId) {
        this.rcScriptStatusTypeId = rcScriptStatusTypeId;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCandidateStr1Title() {
        return candidateStr1Title;
    }

    public void setCandidateStr1Title(String candidateStr1Title) {
        this.candidateStr1Title = candidateStr1Title;
    }

    public String getCandidateStr2Title() {
        return candidateStr2Title;
    }

    public void setCandidateStr2Title(String candidateStr2Title) {
        this.candidateStr2Title = candidateStr2Title;
    }

    public String getCandidateStr3Title() {
        return candidateStr3Title;
    }

    public void setCandidateStr3Title(String candidateStr3Title) {
        this.candidateStr3Title = candidateStr3Title;
    }

    public String getCandidateStr4Title() {
        return candidateStr4Title;
    }

    public void setCandidateStr4Title(String candidateStr4Title) {
        this.candidateStr4Title = candidateStr4Title;
    }

    public String getCandidateStr5Title() {
        return candidateStr5Title;
    }

    public void setCandidateStr5Title(String candidateStr5Title) {
        this.candidateStr5Title = candidateStr5Title;
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


    public String getOnetSocCode() {
        return onetSocCode;
    }

    public void setOnetSocCode(String onetSocCode) {
        this.onetSocCode = onetSocCode;
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

    public List<RcCompetencyWrapper> getRcCompetencyWrapperList() {
        return rcCompetencyWrapperList;
    }

    public void setRcCompetencyWrapperList(List<RcCompetencyWrapper> crl) {
        this.rcCompetencyWrapperList = crl;
    }

    public String getScriptJson() {
        return scriptJson;
    }

    public void setScriptJson(String scriptJson) {
        this.scriptJson = scriptJson;
    }

    public int getAudioVideoOk() {
        return audioVideoOk;
    }

    public void setAudioVideoOk(int audioVideoOk) {
        this.audioVideoOk = audioVideoOk;
    }

    public String getSpecialInstructionsCandidate() {
        return specialInstructionsCandidate;
    }

    public void setSpecialInstructionsCandidate(String specialInstructionsCandidate) {
        this.specialInstructionsCandidate = specialInstructionsCandidate;
    }

    public String getSpecialInstructionsRaters() {
        return specialInstructionsRaters;
    }

    public void setSpecialInstructionsRaters(String specialInstructionsRaters) {
        this.specialInstructionsRaters = specialInstructionsRaters;
    }

    public String getCandidateStr1Question() {
        return candidateStr1Question;
    }

    public void setCandidateStr1Question(String candidateStr1Question) {
        this.candidateStr1Question = candidateStr1Question;
    }

    public String getCandidateStr2Question() {
        return candidateStr2Question;
    }

    public void setCandidateStr2Question(String candidateStr2Question) {
        this.candidateStr2Question = candidateStr2Question;
    }

    public String getCandidateStr3Question() {
        return candidateStr3Question;
    }

    public void setCandidateStr3Question(String candidateStr3Question) {
        this.candidateStr3Question = candidateStr3Question;
    }

    public String getCandidateStr4Question() {
        return candidateStr4Question;
    }

    public void setCandidateStr4Question(String candidateStr4Question) {
        this.candidateStr4Question = candidateStr4Question;
    }

    public String getCandidateStr5Question() {
        return candidateStr5Question;
    }

    public void setCandidateStr5Question(String candidateStr5Question) {
        this.candidateStr5Question = candidateStr5Question;
    }

    public int getAllCommentsRequired() {
        return allCommentsRequired;
    }

    public void setAllCommentsRequired(int allCommentsRequired) {
        this.allCommentsRequired = allCommentsRequired;
    }

    public boolean getAllCommentsRequiredB() {
        return allCommentsRequired==1;
    }

    public int getUseDiscreteRatings() {
        return useDiscreteRatings;
    }

    public void setUseDiscreteRatings(int useDiscreteRatings) {
        this.useDiscreteRatings = useDiscreteRatings;
    }

    public boolean getUseDiscreteRatingsB() {
        return useDiscreteRatings == 1;
    }

    public int getForceAllAnonymous() {
        return forceAllAnonymous;
    }

    public void setForceAllAnonymous(int forceAllAnonymous) {
        this.forceAllAnonymous = forceAllAnonymous;
    }

    public int getIdealScoresOk() {
        return idealScoresOk;
    }

    public void setIdealScoresOk(int idealScoresOk) {
        this.idealScoresOk = idealScoresOk;
    }

    public int getRatingScaleTypeId() {
        return ratingScaleTypeId;
    }

    public void setRatingScaleTypeId(int ratingScaleTypeId) {
        this.ratingScaleTypeId = ratingScaleTypeId;
    }

    public int getNoCommentsRatingItems() {
        return noCommentsRatingItems;
    }

    public void setNoCommentsRatingItems(int noCommentsRatingItems) {
        this.noCommentsRatingItems = noCommentsRatingItems;
    }
    
    public boolean getNoCommentsRatingItemsB() {
        return noCommentsRatingItems==1;
    }

    public int getRcCheckTypeId() {
        return rcCheckTypeId;
    }

    public void setRcCheckTypeId(int rcCheckTypeId) {
        this.rcCheckTypeId = rcCheckTypeId;
    }

    
}
