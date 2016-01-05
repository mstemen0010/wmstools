/*
 * DBMessage.java
 *
 * Created on April 19, 2005, 12:13 AM
 */

package com.wms.jdbtk3;

/**
 *
 * @author Matt Stemen
 */
public class VDBMessage
{
    private DBMessage messageType;
    
    public enum DBMessage
    {
        TableChanged,
        TableAltered,
        RecordChanged,
        RecordDeleted,
        FieldChanged,
        TableError,
         RecordError,
         FieldError
    }
    /** Creates a new instance of DBMessage */
    public VDBMessage(DBMessage messageType)
    {
        this.messageType = messageType;
    }
    
}
