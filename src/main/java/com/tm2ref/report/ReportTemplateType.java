package com.tm2ref.report;



public enum ReportTemplateType
{
    STD_SELECTION(0,"Standard Selection", "com.tm2ref.custom.ct2.CT2RcReport"),
    STD_INTERVIEW(1,"Standard Interview", "com.tm2ref.custom.ct2.CT2RcReport"),
    STD_DEVELOPMENT(2,"Standard Development", "com.tm2ref.custom.ct2.CT2RcReport"),
    STD_REFERENCE(100,"Standard Reference", "com.tm2ref.custom.ct2.CT2RcReport"),
    CUSTOM(200,"Custom", null );

    private final int reportTemplateTypeId;

    private final String name;

    private final String implementationClass;


    private ReportTemplateType( int s , String n, String c )
    {
        this.reportTemplateTypeId = s;

        this.name = n;

        this.implementationClass = c;
    }


    public boolean getIsStandard()
    {
        return equals( STD_REFERENCE );
    }


    public boolean getIsCustom()
    {
        return equals( CUSTOM );
    }

    public String getImplementationClass()
    {
        return implementationClass;
    }



    public static ReportTemplateType getValue( int id )
    {
        ReportTemplateType[] vals = ReportTemplateType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getReportTemplateTypeId() == id )
                return vals[i];
        }

        return STD_SELECTION;
    }


    public int getReportTemplateTypeId()
    {
        return reportTemplateTypeId;
    }

    public String getName()
    {
        return name;
    }

}
