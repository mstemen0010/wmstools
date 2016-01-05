/*
 * v_row.java
 *
 * Created on June 21, 2004, 2:24 PM
 */


package com.wms.jdbtk3;

/**
 *
 *
 *
 * @author mstemen
 *
 */


import java.util.*;

/**
 * Abstract datastore for the Virtual Database
 *
 */

public class VMetaRecord {
           
    private String name; // what    
    private String key = null;
    private String[] fieldNames = null;
    
    private VDBTypes.DBDataTypes[] fieldType = null;    
    private String[] foreignKeyTableNameForColumn = null;
    
    private int numFields;
    private boolean hasDelta = false;

    // private int primaryKeyColumn;
    
    private VField primaryKeyField = null;
    private VTable vTable; // reference to the owning table
    /**
     *
     *
     *
     * @uml.property name="fields"
     *
     * @uml.associationEnd @uml.property name="fields" multiplicity="(0 -1)"
     *
     */       
    
    
       
    
    // nake the base constructor private
    
    public VMetaRecord( VTable owningTable, String key) {
        this.vTable = owningTable;
        this.key = key;                
    }
           

    /** Creates a new instance of v_row */    
            
    public VMetaRecord( String name, Vector fieldData )
    {
        
    }
    
    public VField getField(String name) {       
        int columnPos = vTable.getColumnNumberForName(name );
        VField field = new VField( null, columnPos );
        field.setFieldData( fieldNames[ columnPos - 1 ]);
        field.setFieldType( fieldType[ columnPos - 1 ] );
        return field;               
    }  

    public VField getField( int columnPos ) {               
        VField field = new VField( null, columnPos );
        field.setFieldData( fieldNames[ columnPos - 1 ]);
        field.setFieldType( fieldType[ columnPos - 1 ] );
        return field;               
    }  
   
    String getFieldName( int columnPos )
    {
        return getData( columnPos );
    }
    
    String getData( String columnName )
    {
        int pos = vTable.getColumnNumberForName ( columnName );
        return getData( pos );        
    }
    String getData( int columnPos )
    {
        if( columnPos <= this.fieldNames.length )
        {
            return fieldNames[columnPos - 1];
        }
        return null;
    }
    
    void setData( int columnPos, String data )
    {
        fieldNames[ columnPos - 1 ] = data;
    }
    
    VDBTypes.DBDataTypes getType( int columnPos )
    {
        return fieldType[columnPos - 1];
    }
    
    void setType( int columnPos, VDBTypes.DBDataTypes type )
    {
        this.fieldType[ columnPos - 1 ] = type;
    }
    
    public void addVRecordListener( VDBListener listener )    
    {
                      
    }

    
    public void reset()
    {
        fieldNames = new String[ numFields];
        fieldType  = new VDBTypes.DBDataTypes[ numFields ];
    }
             
    public Iterator keys() {        
        LinkedList<String> list = new LinkedList<String>();        
        boolean test = false;                       
        for (int i = 0; i < numFields; i++)            
        {                        if( fieldNames[i] != null )               
                list.add(fieldNames[i]);            
        }                       
        return list.iterator();        
    }
           
    public void clear()    
    {
        reset();
    }
           
    /**
     *
     * Method to add a new field to an exisiting record
     *
     */
           
    public void put( int columnPos, String data, VDBTypes.DBDataTypes type ) 
    {       
        if( fieldNames == null )
        {
            fieldNames = new String[1];
            fieldType = new VDBTypes.DBDataTypes[ 1 ];
            fieldNames[  0 ]  = data;
            fieldType[ 0 ] = type;
            numFields++;
        }
        else if( columnPos > numFields )
        {
            // crerate new space...
            String[] tempFields  = new String[ numFields + 1 ];
            VDBTypes.DBDataTypes[] tempTypes = new VDBTypes.DBDataTypes[ numFields + 1 ];
            
            // copy the new data into the last index
            tempFields[ tempFields.length - 1 ] = data;
            tempTypes[ tempTypes.length - 1 ] = type;
            
            // copy the rest of the old Data
            for( int  i = 0; i < fieldNames.length ; i++ )
            {
                tempFields[ i ]  = fieldNames[ i  ];
                tempTypes[ i ] = fieldType[ i ];
            }
            fieldNames = tempFields;
            fieldType = tempTypes;
            numFields++;
        }
        else
        {
        }
        // new_field.setMetaRecord( this );                             
    }
            
    public void setValue( int columnPos, String data )    
    {
        if( columnPos < numFields )
            fieldNames[ columnPos ] = data;
    }

            
    /**
     *
     *  Method to append a new column to the record (tableSignature)
     *
     *
     *
     */
                             
    public void printData() {        
        for (int i = 0; i < numFields; i++) {            
                System.out.print( fieldNames[i]  + " ");                
        }       
        System.out.println("");        
    }
            
    public void printSignature() {       
        this.vTable.printHeader();
    }                          
           
    public String getRecordAsString() {        
        StringBuffer buf = new StringBuffer();        
        buf.append(vTable.getHeader());        
        buf.append("\n");        
        buf.append(getDataAsString());                        
        return buf.toString();        
    }            

    public String getForeignTableNameForColumn( int column )
    {
        return foreignKeyTableNameForColumn[ column - 1];
    }
    
    public void setForeignTableNameForColumn( int column, String foreignTableName )
    {
        int columnIndex = column - 1;
        
        if( foreignKeyTableNameForColumn == null )
        {
            foreignKeyTableNameForColumn = new String[1];
            this.foreignKeyTableNameForColumn[ 0 ] = foreignTableName;
        }                            
        else if ( columnIndex > foreignKeyTableNameForColumn.length )
        {
            // crerate new space...
            String[] tempFields  = new String[ foreignKeyTableNameForColumn.length + 1 ];
            
            // copy the new data into the last index
            tempFields[ tempFields.length - 1 ] = foreignTableName;
            
            
            // copy the rest of the old Data
            for( int  i = 0; i < foreignKeyTableNameForColumn.length ; i++ )
            {
                tempFields[ i ]  = foreignKeyTableNameForColumn[ i  ];                
            }
            fieldNames = tempFields;
        }      
        else
        {
            this.foreignKeyTableNameForColumn[ column ] = foreignTableName;
        }
    }
        
    public void setPrimaryKey(  String primaryKey ) {        
        this.key = primaryKey;   
    }

    public String getPrimaryKey() {
        return key;
    }
    
    public Iterator<String> getColumnNames() {        
        Vector<String> list= new Vector<String>( fieldNames.length );
        for( int i = 0; i < fieldNames.length; i++ )
        {
        list.add( fieldNames[i] );
        }
        return list.iterator();
    }
    
    public int getColumnNumberByName(String columnName) {

        int columnIndex = 1;
        for( int i = 0; i < numFields; i++ )
        {
            if( fieldNames[i] != null
            && fieldNames[i].equals( columnName ) )
            {
                return columnIndex;
            }
            else
            {
                columnIndex++;
            }
        }
        return  -1;        
    }

    public String getName() {
        return this.key;
    }

    public void setName(String name) {        
        this.key = name;        
    }
            
    public int getNumber() {        
        return numFields;        
    }
                   
    /**
     *
     *
     *
     * @uml.property name="vTable"
     *
     */
    
    public VTable getVTable() {        
        return vTable;        
    }

    /**
     *
     *
     *
     * @uml.property name="vTable"
     *
     */

    public Vector getRowData() {       
        Vector <String> rowData = new Vector<String>();        
                
        for (int i = 0; i < this.numFields; i++) {            
            rowData.addElement( fieldNames[i] );            
        }                        
        return rowData;        
    }
    
          
    public String getDataAsString() {
        StringBuffer buf = new StringBuffer();        
        for (int i = 0; i < numFields; i++) {
            buf.append( fieldNames[i] );
            buf.append(" | ");                                                           
        }        
        return buf.toString();        
    }            
    
    public String toString() {
        return getDataAsString();
    }
    
    public boolean hasDelta( )    
    {
        return hasDelta;
    }
    
    public void setDelta( boolean delta )    
    {        
        hasDelta = delta;        
    }
}