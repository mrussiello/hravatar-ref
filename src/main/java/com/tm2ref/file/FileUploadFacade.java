/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.file;

import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
import java.util.Date;
import java.util.List;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

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
        //  utx.begin();

        try
        {
        	  // LogService.logIt( "FileUploadFacade.saveUploadedUserFile() saving UploadedUserFile "  );

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

        catch( Exception e )
        {
        	// utx.rollback();

            LogService.logIt( e, "FileUploadFacade.saveRcUploadedUserFile() " + uuf.toString() );

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
    
    
    public RcUploadedUserFile getSingleRcUploadedUserFileForRcCheckRcRaterRcItemAndType( long rcCheckId, long rcRaterId, int rcItemId, int uploadedUserFileTypeId) throws Exception
    {
        
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
