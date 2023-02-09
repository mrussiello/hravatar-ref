package com.tm2ref.user;


public enum UserCompanyStatusType
{
    UNKNOWN(0,"usercompanytype.unknown"),
    APPLICANT(5,"usercompanytype.candidateapplicant"),
    POST_SCREENING(10,"usercompanytype.candidatepostscreening"),
    PREINTERVIEW(15,"usercompanytype.candidatepreinterview"),
    POSTINTERVIEW(20,"usercompanytype.candidatepostinterview"),
    OFFER(25,"usercompanytype.candidateoffer"),
    ACCEPTED(30,"usercompanytype.candidateaccept"),
    REJECTED(50,"usercompanytype.candidaterejected"),
    APP_TERMINATED(51,"usercompanytype.candiateterminated"),
    EMPLOYEE(100,"usercompanytype.employee"),
    TERMINATED(200,"usercompanytype.employeeterminated");

    private int userCompanyStatusTypeId;

    private String key;

    private UserCompanyStatusType( int typeId , String key )
    {
        this.userCompanyStatusTypeId = typeId;

        this.key = key;
    }

    public int getUserCompanyStatusTypeId()
    {
        return userCompanyStatusTypeId;
    }




    public UserCompanyStatusType getValue( int id )
    {
        UserCompanyStatusType[] vals = UserCompanyStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUserCompanyStatusTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }

    public String getKey()
    {
        return key;
    }
}
