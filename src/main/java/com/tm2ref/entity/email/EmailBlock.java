package com.tm2ref.entity.email;

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
@Table( name="emailblock" )
@NamedQueries({
    @NamedQuery ( name="EmailBlock.findFullBlockOrBounceOrComplainForEmail", query="SELECT o FROM EmailBlock AS o WHERE o.email = :email AND (o.fullBlock=1 OR o.emailBlockReasonId IN (98,99) )" ),
    @NamedQuery ( name="EmailBlock.findForEmail", query="SELECT o FROM EmailBlock AS o WHERE o.email = :email" ),
    @NamedQuery ( name="EmailBlock.findFullBlockForEmail", query="SELECT o FROM EmailBlock AS o WHERE o.email = :email AND o.fullBlock=1" )
})
public class EmailBlock implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="emailblockid")
    private long emailBlockId;

    @Column(name="userid")
    private long userId;

    @Column(name="emailblockreasonid")
    private int emailBlockReasonId;

    @Column(name="email")
    private String email;

    @Column(name="fullblock")
    private int fullBlock;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="expiredate")
    private Date expireDate;

    
    

    @Override
    public String toString()
    {
        return "EmailBlock=" + emailBlockId + ", emailBlockReasonId=" + emailBlockReasonId + ", userId=" + userId +", " + ( createDate == null ? "null date" : createDate.toString() ) + ", " + email;
    }


    public Date getCreateDate()
    {
        return createDate;
    }


    public void setCreateDate( Date createDate )
    {
        this.createDate = createDate;
    }


    public String getEmail()
    {
        return email;
    }


    public void setEmail( String email )
    {
        this.email = email;
    }


    public long getEmailBlockId()
    {
        return emailBlockId;
    }


    public void setEmailBlockId( long emailBlockId )
    {
        this.emailBlockId = emailBlockId;
    }


    public int getEmailBlockReasonId()
    {
        return emailBlockReasonId;
    }


    public void setEmailBlockReasonId( int emailBlockReasonId )
    {
        this.emailBlockReasonId = emailBlockReasonId;
    }


    public long getUserId()
    {
        return userId;
    }


    public void setUserId( long userId )
    {
        this.userId = userId;
    }

    public int getFullBlock() {
        return fullBlock;
    }

    public void setFullBlock(int fullBlock) {
        this.fullBlock = fullBlock;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }




}
