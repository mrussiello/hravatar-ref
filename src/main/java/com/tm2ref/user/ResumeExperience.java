/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.user;

import com.tm2ref.service.LogService;
import com.tm2ref.util.JsonUtils;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miker
 */
public class ResumeExperience implements Serializable 
{
    String title;
    String company;
    String period;
    List<Accomp> accomplishments;


    public ResumeExperience()
    {
    }

    public ResumeExperience( JsonObject jo )
    {
        parseJo(jo);
    }

    public boolean getHasFormData()
    {
        return (title!=null && !title.isBlank()) || (company!=null && !company.isBlank());
    }

    private void parseJo( JsonObject jo )
    {
        LogService.logIt( "ResumeExperience.parseJo() AAA.1 " );
        try
        {
            title = JsonUtils.getStringFmJson(jo, "title");
            company = JsonUtils.getStringFmJson(jo, "company");
            period = JsonUtils.getStringFmJson(jo, "period");
            JsonArray ja = jo.getJsonArray("accomplishments" );

            accomplishments = new ArrayList<>();
            if( ja!=null && !ja.isEmpty() )
            {
                for( String ss : ja.getValuesAs(JsonString::getString) )
                {
                    accomplishments.add( new Accomp(ss));
                }
                // accomplishments.addAll( ja.getValuesAs(JsonString::getString));
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ResumeExperience.parseJo() " + toString() );
        }
    }

    public JsonObjectBuilder getJsonObjectBuilder()
    {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("title", title==null ? "" : title );
        job.add("company", company==null ? "" : company );
        job.add("period", period==null ? "" : period );
        JsonArrayBuilder jab = Json.createArrayBuilder();


        if( this.accomplishments!=null )
        {
            for( Accomp a : accomplishments )
            {
                if( a.getAcc()!=null && !a.getAcc().isBlank() )
                    jab.add(a.getAcc());
            }
        }
        job.add("accomplishments",jab);
        return job;
    }


    public String toAiString()
    {
        StringBuilder sb = new StringBuilder();
        if( title!=null && !title.isBlank() )
            sb.append( title );

        if( company!=null && !company.isBlank() )
        {
            if( sb.length()>0 )
                sb.append( ", ");
            sb.append(company);
        }
        if( period!=null && !period.isBlank() )
        {
            if( sb.length()>0 )
                sb.append( ", ");
            sb.append(period);
        }
        if( accomplishments!=null && !accomplishments.isEmpty() )
        {
            if( sb.length()>0 )
                sb.append( "\n");
            for( Accomp s : accomplishments )
                sb.append( "    - " + s.getAcc() + "\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ResumeExperience{title=" + title + ", company=" + company + ", period=" + period + '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<Accomp> getAccomplishments() {
        //LogService.logIt( "ResumeExperience.getAccomplishments() accomplishments.size=" + (accomplishments==null ? "null" : accomplishments.size()));
        return accomplishments;
    }

    public void setAccomplishments(List<Accomp> acc) {

        //LogService.logIt( "ResumeExperience.setAccomplishments() accomplishments.size=" + (acc==null ? "null" : acc.size()));
        this.accomplishments = acc;
    }



    public class Accomp implements Serializable
    {
        String acc;

        public Accomp(String s)
        {
            acc=s;
        }
        public String getAcc()
        {
            return acc;
        }
        public void setAcc(String s)
        {
            this.acc=s;
        }
    }
}
