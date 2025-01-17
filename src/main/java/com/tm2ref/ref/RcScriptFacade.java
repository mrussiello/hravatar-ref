package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCompetency;
import com.tm2ref.entity.ref.RcItem;
import com.tm2ref.entity.ref.RcScript;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class RcScriptFacade
{
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )  // ( unitName = "tm2" )
    EntityManager em;

    // private static EntityManagerFactory tm2Factory;

    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;


    public static RcScriptFacade getInstance()
    {
        try
        {
            return (RcScriptFacade) InitialContext.doLookup( "java:module/RcScriptFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "RcScriptFacade.getInstance() " );
            return null;
        }
    }
     
    
    public RcScript getRcScript( int rcScriptId, boolean refresh) throws Exception
    {
        try
        {
            if( refresh )
                return (RcScript) em.createNamedQuery( "RcScript.findByRcScriptId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcScriptId", rcScriptId ).getSingleResult();
            
            else
                return em.find(RcScript.class, rcScriptId );
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcScriptFacade.getRcScript( " + rcScriptId + " ) " );
            throw new STException( e );
        }
    }       
    
    
    
    public RcItem getRcItem( int rcItemId, boolean load, boolean refresh) throws STException
    {
        try
        {
            RcItem r;
            
            if( refresh )
                r = (RcItem) em.createNamedQuery( "RcItem.findByRcItemId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("rcItemId", rcItemId ).getSingleResult();
            else
                r = (RcItem) em.find(RcItem.class, rcItemId );
            
            if( load && r!=null && r.getRcCompetencyId()>0 )
                r.setRcCompetency( getRcCompetency( r.getRcCompetencyId() ));
            
            return r;
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcScriptFacade.getRcItem( " + rcItemId + " ) " );
            throw new STException( e );
        }
    }       
    
    
    //public RcItem getRcItemForCompetency( int rcCompetencyId ) throws Exception
    //{
    //    List<RcItem> l = getRcItemList( rcCompetencyId );
    //    if( l.isEmpty() )
    //        return null;
    //    Collections.shuffle(l);
    //    return l.get(0);
    //}
    
    public List<RcItem> getRcItemList( int rcCompetencyId, int rcItemStatusTypeId) throws Exception
    {
        try
        {
            TypedQuery<RcItem> q = em.createNamedQuery( rcItemStatusTypeId>=0 ? "RcItem.findByCompetencyIdAndStatus" : "RcItem.findByCompetencyId", RcItem.class).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "rcCompetencyId", rcCompetencyId );
            
            if( rcItemStatusTypeId>=0 )
                q.setParameter("rcItemStatusTypeId", rcItemStatusTypeId );
            return q.getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcScriptFacade.getRcCompetencyList( rcCompetencyId=" + rcCompetencyId + " ) " );
            throw e;
        }        
    }
    
    
    
    public RcCompetency getRcCompetency( int rcCompetencyId ) throws Exception
    {
        try
        {
            return em.find(RcCompetency.class, rcCompetencyId );
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcScriptFacade.getRcCompetency( " + rcCompetencyId + " ) " );
            throw new STException( e );
        }
    }       


    
    public void loadScriptObjects( RcScript s, boolean loadIfNeeded) throws Exception
    {
        s.parseScriptJson();        
        List<RcItem> itms;
        RcItemWrapper w; 
        // List<RcItemWrapper> iwl;
        //Date lastItemUpdate = null;
        boolean save = false;
        for( RcCompetencyWrapper rcw : s.getRcCompetencyWrapperList() )
        {
            if( rcw.getRcCompetencyId()>0 && ( !loadIfNeeded || rcw.getRcCompetency()==null) )
                rcw.setRcCompetency( getRcCompetency( rcw.getRcCompetencyId() ) );

            itms = getRcItemList(rcw.getRcCompetencyId(), -1 );
            // iwl = new ArrayList<>();
            
            for( RcItem itm : itms )
            {
                itm.setRcCompetency( rcw.getRcCompetency() );
                
                //if( lastItemUpdate==null || lastItemUpdate.before( itm.getLastUpdate() ) )
                //    lastItemUpdate = itm.getLastUpdate();
                
                w = rcw.getRcItemWrapper( itm.getRcItemId() );

                // not all items may still be included in the competency. Some may be archived and not used. 
                // However, if the competency is custom and is available for other scripts competencies, it might have been updated in another script and we should fix this script..
                if( w==null )    
                {
                    // Item is archived, so ignore it.
                    if( itm.getRcItemStatusTypeId()==RcItemStatusType.ARCHIVED.getRcItemStatusTypeId() )                    
                        continue;
                    
                    // Item is not archived, so it should be here. Add it.
                    w = new RcItemWrapper();
                    w.setRcItemId( itm.getRcItemId() );  
                    rcw.addItemWrapper(w);
                    LogService.logIt( "RcScriptFacade.loadScriptObjects() ADDING Item rcItemId=" + itm.getRcItemId() + " " + itm.getQuestion() + ", to RcCompetency " + rcw.getRcCompetencyId() + " " + rcw.getRcCompetency().getName() + " because it is not archived and not in Script." );
                    save = true;                    
                }
                
                else if( itm.getRcItemStatusTypeId()!=RcItemStatusType.ACTIVE.getRcItemStatusTypeId() )
                {
                    LogService.logIt( "RcScriptFacade.loadScriptObjects() REMOVING Item rcItemId=" + itm.getRcItemId() + " " + itm.getQuestion() + ", from RcCompetency " + rcw.getRcCompetencyId() + " " + rcw.getRcCompetency().getName() + " because it is archived but still referencd in Script." );                    
                    rcw.removeRcItemWrapper( w );
                    save = true;
                    continue;
                }
                
                w.setRcItem(itm);                    
                // iwl.add(w);
            }
            //if( rcw.getRcItemWrapperList().size()!=iwl.size() )
            //    save = true;
            Collections.sort(rcw.getRcItemWrapperList());
            // rcw.setRcItemWrapperList(iwl);
        }

        if( save ) // || (lastItemUpdate!=null && lastItemUpdate.after( s.getLastUpdate() )) )
        {
            saveRcScript(s);
        }
    }
    
    public RcScript saveRcScript( RcScript ir) throws Exception
    {
        try
        {
            if( ir.getOrgId()<=0 )
                throw new Exception( "OrgId is 0" );
            
            if( ir.getAuthorUserId()<=0 )
                throw new Exception( "Author UserId is 0" );
            
            if( ir.getName()==null || ir.getName().isBlank() )
                throw new Exception( "Name is missing." );
            
            if( ir.getCreateDate()==null )
                ir.setCreateDate( new Date() );
            
           ir.setLastUpdate( new Date() );           
           ir.writeScriptJson();
            
            if( ir.getRcScriptId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                em.detach( ir );
                em.persist( ir );
            }

            em.flush();
            return ir;
        }
        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RcScriptFacade.saveRcScript() " + ir.toString() );
            throw new STException( e );
        }
    }   
    

}
