package com.tm2ref.score;



public enum CaveatScoreType
{
    NONE(0,"None", "emst.none", 1, 1, 0 ),
    CLARITY(1,"Clarity and Coherence", "emst.clarity", 1, 2, 1 ),
    ARGUMENT(2,"Argument Strength", "emst.argument", 1, 2, 1 ),
    MECHANICS(3,"Mechanics", "emst.mechanics", 1, 2, 1 ),
    IDEAL(4, "Ideal Score Match", "emst.ideal", 1, 2, 1 ),
    ACCURACY(40, "Accuracy","emst.accuracy", 1, 1, 1 ),
    ERRORS_GROSS(41, "Gross Errors","emst.grosserrors", 1, 1, 0 ),
    ERRORS_GROSS_XY(42, "Gross Errors X of Y","emst.grosserrorsxy", 1, 1, 0 ),
    WPM_TYPING(43, "Words Per Minute Typing","emst.wpmtyping", 1, 1, 1 ),
    WPM(50, "Words Per Minute Essay","emst.wpm", 1, 1, 1 ),
    WPM_HI(51, "High WPM Essay","emst.wpmhi", 1, 1, 1 ),
    WPM_ACCURACY_ADJ(52, "Accuracy Adjusted WPM","emst.wpmaccadjst", 1, 1, 1 ),
    WORDS(53, "Total Words","emst.words", 1, 1, 0 ),
    PLAGIARIZED(54, "Plagiarized","emst.plag", 2, 1, 0 ),
    PLAGIARIZED_XY(55, "Plagiarized XY","emst.plagXY", 2, 1, 0 ),
    SPELLING_ERRORS(56, "Spelling Errors","emst.spellerrs", 1, 2, 1 ),
    GRAMMAR_ERRORS(57, "Grammar Errors","emst.gramerrs", 1, 2, 1 ),
    OTHER_ERRORS(58, "Other Errors","emst.othrerrs", 1, 2, 1 ),
    TRANSLATE_COMPARE(59, "Translate Comparison","emst.transcompare", 1, 1, 1 ),
    CHAT_RESP_TIME(70, "Chat Response Time","emst.chatresptime", 1, 1, 1 ),
    CHAT_RAPPORT(71, "Chat Rapport","emst.chatrapport", 1, 1, 1 ),
    CHAT_NEG_EXP(72, "Chat Negative Expressions","emst.chatnegexp", 1, 1, 1 ),
    CHAT_SPELL_GRAM_ERRORS(673, "Chat Spell Grammar Errors","emst.chatspellgramerrs", 1, 1, 0 ),
    KSPH(80, "KSPH","emst.ksph", 1, 1, 0 ),
    KSPH_ACCURACY_ADJ(81, "Accuracy Adjusted KSPH","emst.ksphaccadjst", 1, 1, 0 ),
    SECS_PER_PAGE(82, "Seconds Per Page","emst.secsperpage", 1, 1, 1 ),
    OVERALL_AI(100, "Overall AI Score","emst.overallai", 1, 1, 1 ),
    OVERALL_CONF(101, "AI Confidence Level","emst.overallconf", 1, 2, 0 ),
    SCORE_TEXT(150, "Score Text", "emst.scoretext", 2, 1, 0 ),
    TOPIC_CORRECT(151, "Topic Score", "emst.topiccorrect", 1, 1, 0 ),
    LEGACY_STRING(200, "Legacy String", "emst.legacy", 2, 1, 0 );


    private final int caveatScoreTypeId;

    private final String name;
    private final String key;
    private final int colspan;
    private final int headingLevel;
    // private final boolean useIntValues;
    private final int scrDigits;


    private CaveatScoreType( int s , String n, String k, int cols, int headingLevel, int scrDigits )
    {
        this.caveatScoreTypeId = s;
        this.name = n;
        this.key=k;
        this.colspan=cols;
        this.headingLevel=headingLevel;
        this.scrDigits=scrDigits;
    }


    public static CaveatScoreType getValue( int id )
    {
        CaveatScoreType[] vals = CaveatScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCaveatScoreTypeId() == id )
                return vals[i];
        }

        return NONE;
    }
    
    public int getHeadingLevel()
    {
        return headingLevel;
    }
    
    public int getColspan()
    {
        return colspan;
    }

    public int getScoreDigits()
    {
        return scrDigits;
    }
    
    public String getKey()
    {
        return key;
    }

    public String getKeyX()
    {
        return key + "X";
    }

    public int getCaveatScoreTypeId()
    {
        return caveatScoreTypeId;
    }

    public String getName()
    {
        return name;
    }
    
}
