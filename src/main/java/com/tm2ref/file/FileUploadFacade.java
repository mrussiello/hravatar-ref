/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.file;

import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
import java.util.Date;
import java.util.List;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import java.sql.SQLIntegrityConstraintViolationException;
import org.eclipse.persistence.exceptions.DatabaseException;

/**
 *
 * @author Mike
 */
@Stateless
public class FileUploadFacade
{
    @PersistenceContext
    EntityManager em;
    
    public static FileUploadFacade getInstance()
    {
        try
        {
            return (FileUploadFacade) InitialContext.doLookup( "java:module/FileUploadFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileUploadFacade.getInstance() " );

            return null;
        }
    }


    public RcUploadedUserFile saveRcUploadedUserFile( RcUploadedUserFile uuf ) throws Exception
    {
        return saveRcUploadedUserFile( uuf, 0 );
    }


    public RcUploadedUserFile saveRcUploadedUserFile( RcUploadedUserFile uuf, int count ) throws Exception
    {
        //  utx.begin();

        try
        {
            if( uuf.getRcCheckId() <= 0  )
                throw new Exception( "rcCheckId=0" );

            if( uuf.getFilename() == null || uuf.getFilename().isEmpty() )
                throw new Exception( "filename is missing" );

            if( uuf.getCreateDate() == null )
                uuf.setCreateDate( new Date() );

            if( uuf.getLastUpload() == null )
                uuf.setLastUpload( new Date() );
            
            if( uuf.getNote()!=null && uuf.getNote().length()>999 )
            {
                uuf.setNote( StringUtils.truncateStringFromFront(uuf.getNote(), 999 ) );
            }

            if( uuf.getRcUploadedUserFileId() > 0 )
            {
                em.merge( uuf );
            }

            else
            {
                em.persist( uuf );
            }


            em.flush();

        }
        catch( DatabaseException | PersistenceException | SQLIntegrityConstraintViolationException e )
        {
            if( count<3 )
            {
                LogService.logIt( "FileUploadFacade.saveRcUploadedUserFile()XXX.1 Database error so waiting and trying again. count=" + count +", " + e.toString() + ", " + uuf.toString() );

                if( uuf.getRcUploadedUserFileId()<=0 )
                {
                    RcUploadedUserFile cUuf = this.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType(uuf.getRcCheckId(), uuf.getRcRaterId(), uuf.getRcItemId(), count);
                    if( cUuf!=null )
                    {
                        LogService.logIt( "FileUploadFacade.saveRcUploadedUserFile() XXX.2 Found existing UploadedUserFile so using it.UploadedUserFileId=" + cUuf.getRcUploadedUserFileId()+ ", rcCheckId=" + uuf.getRcCheckId() + ", rcRaterId=" + uuf.getRcRaterId() + ", rcItemId=" + uuf.getRcItemId() );
                        uuf.setRcUploadedUserFileId(cUuf.getRcUploadedUserFileId());                        
                        return cUuf;
                    }
                }                

                Thread.sleep((long) ((Math.random()) * 2000l));
                count++;
                return saveRcUploadedUserFile( uuf, count);
            }
            
            LogService.logIt( e, "FileUploadFacade.saveRcUploadedUserFile() XXX.2 count=" + count + ", " + uuf.toString() );
            throw new STException( e );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileUploadFacade.saveRcUploadedUserFile() ZZZ.1 " + uuf.toString() );
            throw new STException( e );
        }

        return uuf;
    }

    


      
    public List<RcUploadedUserFile> getRcUploadedUserFilesForRcCheckAndRater( long rcCheckId, long rcRaterId ) throws Exception
    {
        try
        {
            Query q = em.createNamedQuery("RcUploadedUserFile.findRcCheckIdAndRcRaterId",  RcUploadedUserFile.class );
            q.setParameter( "rcCheckId", rcCheckId );
            q.setParameter( "rcRaterId", rcRaterId );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            return q.getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "FileUploadFacade.getRcUploadedUserFilesForRcCheckAndRater( rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + " ) " );
            throw new STException( e );
        }        
    }
    
    public RcUploadedUserFile getRcUploadedUserFile( long rcUploadedUserFileId) throws Exception
    {        
        try
        {
            Query q = em.createNamedQuery( "RcUploadedUserFile.findById",  RcUploadedUserFile.class );
            q.setParameter( "rcUploadedUserFileId", rcUploadedUserFileId );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            return (RcUploadedUserFile) q.getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "FileUploadFacade.getRcUploadedUserFile( rcUploadedUserFileId=" + rcUploadedUserFileId + " ) " );
            throw new STException( e );
        }        
        
    }
    
    
    
    public RcUploadedUserFile getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType( long rcCheckId, long rcRaterId, int rcItemId, int uploadedUserFileTypeId) throws Exception
    {
        // LogService.logIt("FileUploadFacade.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType() START rcCheckId=" + rcCheckId + ", rcItemId=" + rcItemId + ", rcRaterId=" + rcRaterId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + " ) " );
        
        try
        {
            Query q = em.createNamedQuery( "RcUploadedUserFile.findRcCheckIdAndRcRaterIdRcItemIdAndTypeId",  RcUploadedUserFile.class );
            q.setParameter( "rcCheckId", rcCheckId );
            q.setParameter( "rcRaterId", rcRaterId );
            q.setParameter( "rcItemId", rcItemId );
            q.setParameter( "uploadedUserFileTypeId", uploadedUserFileTypeId );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            List<RcUploadedUserFile> l = q.getResultList();
            if( l==null|| l.isEmpty() )
                return null;
            if( l.size()>1 )
                LogService.logIt("FileUploadFacade.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType() ERROR More than one record returned. record count=" + l.size() + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + " ) " );
            return l.get(0);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "FileUploadFacade.getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType( rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", uploadedUserFileTypeId=" + uploadedUserFileTypeId + " ) " );
            throw new STException( e );
        }                
    }

}
