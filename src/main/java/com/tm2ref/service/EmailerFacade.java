/*
 * Created on Dec 30, 2006
 *
 */
package com.tm2ref.service;

import com.tm2ref.entity.email.EmailBlock;
import com.tm2ref.global.STException;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.MapMessage;
import jakarta.jms.Queue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.Date;

import javax.naming.InitialContext;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class EmailerFacade
{
    @Resource(mappedName = "jms/ConnectionFactory")
    protected ConnectionFactory connectionFactory;

    @Resource(mappedName = "jms/seenthatemailqueue")
    protected Queue queue;

    static JMSContext context = null;
    JMSProducer  messageProducer = null;
    
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" ) 
    EntityManager emt;

    public static EmailerFacade getInstance()
    {
        try
        {
            return (EmailerFacade) InitialContext.doLookup( "java:module/EmailerFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EmailerFacade.getInstance() " );

            return null;
        }
    }

    public boolean sendEmail(Map<String, Object> messageInfoMap) throws Exception
    {
        // MapMessage message = null;

        try
        {
            correctAllEmailsForBlocks(messageInfoMap); 
            
            return sendEmailMsg(messageInfoMap);

        } catch (JMSException e)
        {
            LogService.logIt(e, "EmailerFacade.sendEmail() XXX.1 ");
            return false;
        } catch (Exception e)
        {
            LogService.logIt(e, "EmailerFacade.sendEmail() XXX.2 ");
            return false;
        }
    }
    

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private boolean sendEmailMsg(Map<String, Object> messageInfoMap) throws Exception
    {
        MapMessage message = null;

        try
        {
            String to1 = (String) messageInfoMap.get(EmailConstants.TO);

            String to = (String) messageInfoMap.get(EmailConstants.TO);
            if (to == null || to.isEmpty())
            {
                LogService.logIt("EmailerFacade.sendEmailMsg() After block processing, there are no to emails. Subj=" + (String) messageInfoMap.get(EmailConstants.SUBJECT) + ", Original To: " + to1);
                return false;
            }

            String from = (String) messageInfoMap.get(EmailConstants.FROM);
            if (from == null || from.isEmpty())
                return false;

            if (context == null)
                context = connectionFactory.createContext();

            if (messageProducer == null)
                messageProducer = context.createProducer();

            message = context.createMapMessage();

            // now copy all keys to MapMessage
            Set<String> keys = messageInfoMap.keySet();

            Object obj;

            for (String key : keys)
            {
                // LogService.logIt( "Setting object: " + key + ", " + messageInfoMap.get( key ) );

                obj = messageInfoMap.get(key);

                if (obj instanceof String)
                    message.setString(key, (String) obj);

                if (obj instanceof byte[])
                    message.setBytes(key, (byte[]) obj);
            }

            // LogService.logIt( "EmailerBean.sendEmail() SENDING. " );
            messageProducer.send(queue, message);

        } catch (JMSException e)
        {
            LogService.logIt(e, "EmailerFacade.sendEmailMsg() XXX.1 ");
            return false;
        } catch (Exception e)
        {
            LogService.logIt(e, "EmailerFacade.sendEmailMsg() XXX.2 ");
            return false;
        }

        return true;
    }
    
    
    /*
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean sendEmail( Map<String, Object> messageInfoMap ) throws Exception
    {
        MapMessage message = null;

        try
        {
            String to1 = (String) messageInfoMap.get( EmailConstants.TO );     
            
            // First, Make sure there is no Full Email Block on the emails
            correctAllEmailsForBlocks( messageInfoMap );

            String to = (String) messageInfoMap.get( EmailConstants.TO );        
            if( to==null || to.isEmpty() )
            {
                LogService.logIt( "EmailerFacade.sendEmail() After block processing, there are no to emails. Subj=" + (String) messageInfoMap.get( EmailConstants.SUBJECT ) + ", Original To: " + to1 );
                return false;
            }
            
            String from = (String) messageInfoMap.get( EmailConstants.FROM );        
            if( from==null || from.isEmpty() )
                return false;

            if( context==null )
                context = connectionFactory.createContext();

            if( messageProducer==null )
                messageProducer = context.createProducer();

            message = context.createMapMessage();

            // now copy all keys to MapMessage

            Set<String> keys = messageInfoMap.keySet();

            Object obj;

            for( String key : keys )
            {
               // LogService.logIt( "Setting object: " + key + ", " + messageInfoMap.get( key ) );

                obj = messageInfoMap.get( key );

                if( obj instanceof String )
                    message.setString( key, (String) obj );

                if( obj instanceof byte[] )
                    message.setBytes( key, (byte[]) obj );
            }

            messageProducer.send(queue,message);
            Tracker.addEmailSent();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "EmailerFacade.sendEmail()" );
            return false;
        }

        return true;
    }
    */
    
    
    private void correctAllEmailsForBlocks( Map<String, Object> messageInfoMap ) throws Exception
    {
        String emails = (String) messageInfoMap.get(EmailConstants.TO );        
        String o = correctForBlocks( emails );        
        if( o==null || o.trim().isEmpty() )
            messageInfoMap.remove(EmailConstants.TO );
        else
            messageInfoMap.put(EmailConstants.TO, o );

        emails = (String) messageInfoMap.get(EmailConstants.CC );        
        o = correctForBlocks( emails );        
        if( o==null || o.trim().isEmpty() )
            messageInfoMap.remove(EmailConstants.CC );
        else
            messageInfoMap.put(EmailConstants.CC, o );

        emails = (String) messageInfoMap.get(EmailConstants.BCC );        
        o = correctForBlocks( emails );        
        if( o==null || o.trim().isEmpty() )
            messageInfoMap.remove(EmailConstants.BCC );
        else
            messageInfoMap.put(EmailConstants.BCC, o );

        emails = (String) messageInfoMap.get(EmailConstants.FROM );        
        o = correctForBlocks( emails );        
        if( o==null || o.trim().isEmpty() )
            messageInfoMap.remove(EmailConstants.FROM );
        else
            messageInfoMap.put(EmailConstants.FROM, o );
    }
    
    private String correctForBlocks( String emailStr ) throws Exception
    {
        // First, Make sure there is no Full Email Block on the emails
        
        if( emailStr==null )
            return null;
        
        String[] emails;
        
        if( emailStr.indexOf(";")>0 )
            emails = emailStr.split( ";" );
        
        else
            emails = emailStr.split( "," );
        
        StringBuilder emails2 = new StringBuilder();
        
        // EmailBlockFacade elf = EmailBlockFacade.getInstance();
        
        String eml;
        
        for( String em : emails )
        {
            eml=em.trim();
            
            if( eml.contains("|") )
            {
                eml = eml.substring(0, eml.indexOf("|"));
                eml = eml.trim();
            }
            
            if( eml.isEmpty() )
            {
                LogService.logIt( "EmailerFacade.correctForBlocks() skipping because edited email is empty. " + em );
                continue;                
            }
            
            if( hasEmailBlock(eml, true, true) )
            {
                LogService.logIt( "EmailerFacade.correctForBlocks() skipping due to full email block. " + em );
                continue;
            }
            
            if( emails2.length()>0 )
                emails2.append(",");
            
            emails2.append(em);
        }
               
        if( emails2.length()<=0 )
            return null;
        
        return emails2.toString();
        
    }
    
    public boolean hasEmailBlock(String email, boolean fullBlock, boolean treatBouncesComplaintsAsFullBlock) throws Exception
    {
        EmailBlock emailBlock = getEmailBlock(email, fullBlock, treatBouncesComplaintsAsFullBlock);
        return emailBlock != null;
    }
    
    public EmailBlock getEmailBlock(String email, boolean fullBlock, boolean treatBouncesComplaintsAsFullBlock) throws Exception
    {
        try
        {
            if (email == null || email.trim().isEmpty())
                return null;

            email = email.trim();

            
            EntityManager em = emt; // (EntityManager) envCtx.lookup("persistence/tm2");

            if (treatBouncesComplaintsAsFullBlock && !fullBlock)
                treatBouncesComplaintsAsFullBlock = false;

            Query q = em.createNamedQuery(treatBouncesComplaintsAsFullBlock ? "EmailBlock.findFullBlockOrBounceOrComplainForEmail" : (fullBlock ? "EmailBlock.findFullBlockForEmail" : "EmailBlock.findForEmail"));
            // Query q = em.createNamedQuery( fullBlock ? "EmailBlock.findFullBlockForEmail" : "EmailBlock.findForEmail" );

            q.setParameter("email", email);

            q.setHint("jakarta.persistence.cache.retrieveMode", "BYPASS");

            EmailBlock eb = (EmailBlock) q.getSingleResult();
            if (eb != null && eb.getExpireDate() != null && eb.getExpireDate().before(new Date()))
            {
                em.remove(eb);
                em.flush();
                eb = null;
            }
            return eb;
        } catch (NoResultException e)
        {
            return null;
        } catch (Exception e)
        {
            LogService.logIt(e, "EmailerFacade.getEmailBlock() email=" + email);
            throw new STException(e);
        }
    }
    
    
}
