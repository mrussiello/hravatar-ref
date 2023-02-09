/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.proctor;

/**
 *
 * @author miker_000
 */
public enum ProctorImageErrorType {

    NONE(0,"piet.none"),
    NO_FACE(1,"piet.noface"),
    MULTIPLE_FACES(2,"piet.multface"),
    FACIAL_MISMATCH(3,"piet.facemismatch"),
    MOBILE_DEVICE_PRESENT(3,"piet.mobileinphoto"),
    NO_ID_CARD(20,"piet.noidcard"),
    ID_FACIAL_MISSING(21,"piet.idfacemissing"),
    ID_FACIAL_MISMATCH(22,"piet.idfacemismatch"),
    ID_MULTIFACE_MISMATCH(23,"piet.idmultifaceemismatch"),
    ID_TOO_MANY_FACES(24,"piet.idtoomanyfaces"),
    HIGH_PITCH(25,"piet.highpitch"),
    HIGH_YAW(26,"piet.highyaw"),
    SUCCESSFUL_FACE_AND_COMPARISON(101,"piet.successfulfaceandanal");

    private final int proctorImageErrorTypeId;

    private final String key;

    private ProctorImageErrorType( int typeId , String key )
    {
        this.proctorImageErrorTypeId = typeId;

        this.key = key;
    }

    

    public static ProctorImageErrorType getValue( int id )
    {
        ProctorImageErrorType[] vals = ProctorImageErrorType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getProctorImageErrorTypeId() == id )
                return vals[i];
        }

        return NONE;
    }
    
    
    public int getProctorImageErrorTypeId()
    {
        return proctorImageErrorTypeId;
    }

    public String getKey()
    {
        return key;
    }    
}
