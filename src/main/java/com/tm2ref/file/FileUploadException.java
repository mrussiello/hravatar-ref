/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.file;

/**
 *
 * @author miker_000
 */
public class FileUploadException extends Exception {
    
    int fileUploadErrorTypeId;
    
    public FileUploadException( int fileUploadErrorTypeId, String msg )
    {
        super( msg );
        this.fileUploadErrorTypeId=fileUploadErrorTypeId;
    }

    public FileUploadException( FileUploadErrorType fileUploadErrorType, String msg )
    {
        super( msg );
        this.fileUploadErrorTypeId=fileUploadErrorType.getFileUploadErrorTypeId();
    }
    
    
    public int getFileUploadErrorTypeId() {
        return fileUploadErrorTypeId;
    }

    @Override
    public String toString() {
        return "FileUploadException{" + "fileUploadErrorTypeId=" + fileUploadErrorTypeId + ", msg=" + this.getMessage() + '}';
    }
    
    
    
}
