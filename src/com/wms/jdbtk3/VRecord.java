/*
 * v_row.java
 *
 * Created on June 21, 2004, 2:24 PM
 */


package com.wms.jdbtk3;

/**
 **
 *
 *
 * @author mstemen
 *
 */


import java.util.*;
import com.wms.util.WMSLog;
import com.wms.util.WMSTools;
/**
 * Abstract datastore for the Virtual Database
 *
 */

public class VRecord implements VRecordListener {
           
    private String name; // what    
    private String keyName = null;
    private String keyValue = null;
    private int keyColumn = -1;
    private String[] fieldData = null;
    private boolean[] fieldEditable = null;
    private boolean[] fieldHasDelta = null;
    // private VDBTypes.DBDataTypes[] fieldType = null;    
    
    private int numFields;
    private boolean hasDelta = false;
    private boolean hasRealRecord = true;
    
    private String rowKey = ""; // the row id in the table's hashtable
    

    // private int primaryKeyColumn;        
    private VTable vTable; // reference to the owning table
    private WMSLog log = null;
    /**
     *
     *
     *
     * @uml.property name="fields"
     *
     * @uml.associationEnd @uml.property name="fields" multiplicity="(0 -1)"
     *
     */       
    
    // contains the 'footprint' of the record    
    private Hashtable bindings = new Hashtable();
    /**
     *
     *
     *
     * @uml.property name="vTable"
     *
     * @uml.associationEnd @uml.property name="vTable" multiplicity="(0 1)"
     *
     *                     inverse="records:dbtk.db_toolkit.VTable"
     *
     */

        
    private List<VRecordListener> listeners = new ArrayList<VRecordListener>();
    private VDBMessage.DBMessage vdbMessage;
       
    
    // nake the base constructor private
    
    public VRecord( VTable owningTable, String keyName) {
        
        this.keyName = keyName;   
        if( owningTable != null )
        {
            this.log = owningTable.getLog();
            this.vTable = owningTable;
        }
    }
    
    public VRecord( VTable owningTable )
    {
        // this is for an "emtpy" record
        VMetaRecord md = owningTable.getMetaData();
        vTable = owningTable;
        if( md != null )
        {
            int max = md.getNumber();
            fieldData = new String[ max ];
            // fieldType = new VDBTypes.DBDataTypes[ max ];
            fieldEditable = new boolean[ max ];
            fieldHasDelta = new boolean[ max ];
            
            // populate the types, based on the meta data
            
            for( int i = 1; i <= fieldData.length; i++ )
            {
                // fieldType[ i - 1 ] = md.getType( i );
                fieldData[ i - 1 ] = "";
            }
            
            keyName = owningTable.getPrimaryKey();
            numFields = max;
        }
    }

    /** Creates a new instance of v_row */    
            
    public VRecord( String name, Vector fieldData )
    {
        
    }
   public  void recordChanged( VRecordEvent evt )
    {
        vdbMessage = evt.getMessage();
        fireVDBEvent( evt );        
    }

    public synchronized void addListener( VRecordListener listener)
    {
        if( ! listeners.contains( listener ) );
        listeners.add( listener );
    }
    
    public synchronized void removeListener( VRecordListener listener )
    {
        listeners.remove( listener );
    }

    private synchronized void fireVRecordEvent( VRecordEvent event )
    {
        // this.processUDTEventForSelf( event );
        Iterator eventListeners = listeners.iterator();
        while( eventListeners.hasNext() )
            ( (VRecordListener) eventListeners.next() ).recordChanged( event );
    }
           
    private synchronized void fireVDBEvent()
    {
        VRecordEvent event = new VRecordEvent( this, vdbMessage );
        Iterator eventListeners = listeners.iterator();
        while( eventListeners.hasNext() )
            ( (VRecordListener) eventListeners.next() ).recordChanged( event );
        
    }
    
    private synchronized void fireVDBEvent( VRecordEvent event )
    {
        // this.processUDTEventForSelf( event );
        Iterator eventListeners = listeners.iterator();
        while( eventListeners.hasNext() )
            ( (VRecordListener) eventListeners.next() ).recordChanged( event );
    }   
    
    String getFieldName( int columnPos )
    {
        if( vTable != null )
            return vTable.getColumnName( columnPos );
        return "";
    }
    
    String getData( String columnName )
    {
        int pos = vTable.getColumnNumberForName ( columnName );
        return getData( pos );        
    }
    
    public boolean getEditable( int columnPos )
    {
        if( fieldEditable == null )
        {
            return false;
        }
        else if( columnPos > fieldEditable.length )
        {
            return false;            
        }
        else
        {
            return fieldEditable[ columnPos - 1];
        }
    }

    public void setEditable( int columnPos, boolean editable )
    {
        if( fieldEditable == null )
        {
            log.write( "The editable array is not set for this record");
            return;
        }
        else if( columnPos > fieldEditable.length )
        {
            log.write( "The requested column is greater than the width of the table" );
            return;            
        }
        else
        {            
            fieldEditable[ columnPos - 1] = editable;
        }
    }

    
    String getData( int columnPos )
    {
        if( fieldData != null && columnPos <= this.fieldData.length )
        {
            return fieldData[columnPos - 1 ];
        }
        return null;
    }
    
    void setData( int columnPos, String data )
    {
        fieldData[ columnPos - 1 ] = data;
    }
    
    VDBTypes.DBDataTypes getType( int columnPos )
    {
        return this.vTable.getMetaData().getType(columnPos );
        // return fieldType[columnPos - 1];
    }
    
    /*
    void setType( int columnPos, VDBTypes.DBDataTypes type )
    {
        this.fieldType[ columnPos - 1 ] = type;
    }
     */

    public boolean getFieldDelta( int columnPos )
    {
        return this.fieldHasDelta[ columnPos - 1 ];
    }
    
    public void setFieldDelta( int columnPos, boolean hasDelta )
    {
        this.fieldHasDelta[ columnPos - 1 ] = hasDelta;
    }
    
    public void reset()
    {
        fieldData = new String[ numFields];
        // fieldType  = new VDBTypes.DBDataTypes[ numFields ];
    }
             
    public Iterator keys() {        
        LinkedList<String> list = new LinkedList<String>();        
        boolean test = false;                       
        for (int i = 0; i < numFields; i++)            
        {                        if( fieldData[i] != null )               
                list.add(fieldData[i]);            
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
        if( fieldData == null )
        {
            fieldData = new String[1];
            // fieldType = new VDBTypes.DBDataTypes[1];
            fieldEditable = new boolean[1];
            fieldHasDelta = new boolean[1];
                    
            fieldData[0] = data;
            // fieldType[0] = type;
            fieldEditable[0] = false;
            numFields++;
        }
        else if( columnPos > numFields )
        {
            // crerate new space...
            String[] tempFields  = new String[ numFields + 1 ];
            VDBTypes.DBDataTypes[] tempTypes = new VDBTypes.DBDataTypes[ numFields + 1 ];
            boolean[] tempEditable = new boolean[ numFields + 1 ];
            boolean[] tempHasDelta = new boolean[ numFields + 1 ];
            
            // copy the new data into the last index
            tempFields[ tempFields.length - 1 ] = data;
            tempTypes[ tempTypes.length - 1 ] = type;
            tempEditable[ tempEditable.length - 1 ] = false;
            tempHasDelta[ tempHasDelta.length - 1 ] = false;
            
            // copy the rest of the old Data
            for( int  i = 0; i < fieldData.length ; i++ )
            {
                tempFields[ i ]  = fieldData[ i  ];
                // tempTypes[ i ] = fieldType[ i ];
                tempEditable[ i ] = fieldEditable[ i ];
                tempHasDelta[ i ] = fieldHasDelta[ i ];
            }
            fieldData = tempFields;
            // fieldType = tempTypes;
            fieldEditable = tempEditable;
            fieldHasDelta = tempHasDelta;
            numFields++;
        }
        else
        {
            fieldData[  columnPos -1   ]  = data;
            // fieldType[ columnPos - 1 ] = type;
            fieldEditable[ columnPos - 1 ] = false;
            fieldHasDelta[ columnPos - 1 ] = false;
        }
        // new_field.setMetaRecord( this );      
                

        // try and index the value, if this column is indexed on the parent table
        vTable.addIndexValue( columnPos, data, this );

    }

           

    public void put( int columnPos, String data, VDBTypes.DBDataTypes type, boolean editable ) 
    {       
        if( fieldData == null )
        {
            fieldData = new String[1];
             // fieldType = new VDBTypes.DBDataTypes[1];
            fieldEditable = new boolean[1];
            fieldHasDelta = new boolean[1];
                    
            fieldData[0] = data;
            // fieldType[0] = type;
            fieldEditable[0] = editable;
            numFields++;
        }
        else if( columnPos > numFields )
        {
            // crerate new space...
            String[] tempFields  = new String[ numFields + 1 ];
            VDBTypes.DBDataTypes[] tempTypes = new VDBTypes.DBDataTypes[ numFields + 1 ];
            boolean[] tempEditable = new boolean[ numFields + 1 ];
            boolean[] tempHasDelta = new boolean[ numFields + 1 ];
            
            // copy the new data into the last index
            tempFields[ tempFields.length - 1 ] = data;
            tempTypes[ tempTypes.length - 1 ] = type;
            tempEditable[ tempEditable.length - 1 ] = editable;
            tempHasDelta[ tempHasDelta.length - 1 ] = false;
            
            // copy the rest of the old Data
            for( int  i = 0; i < fieldData.length ; i++ )
            {
                tempFields[ i ]  = fieldData[ i  ];
                // tempTypes[ i ] = fieldType[ i ];
                tempEditable[ i ] = fieldEditable[ i ];
                tempHasDelta[ i ] = fieldHasDelta[ i ];
            }
            fieldData = tempFields;
            // fieldType = tempTypes;
            fieldEditable = tempEditable;
            fieldHasDelta = tempHasDelta;
            numFields++;
        }
        else
        {
            fieldData[  columnPos  ]  = data;
            // fieldType[ columnPos ] = type;
            fieldEditable[ columnPos ] = editable;
            fieldHasDelta[ columnPos ] = false;
        }
        // try and index the value, if this column is indexed on the parent table
        vTable.addIndexValue( columnPos, data, this );
        // new_field.setMetaRecord( this );                             
    }

    public void setKeyColumn( int col )
    {
        keyColumn = col;
        if( fieldData.length >= col )
        {
            // try and look up the key value
            if( fieldData[ col - 1 ] != null )
            {
                this.keyValue = fieldData[ col - 1 ];
                vTable.updateFastStorage( fieldData[ col - 1 ], this );
            }
        }
    }
    
    public void setKeyValue( int columnPos, String data )    
    {
        this.keyColumn = columnPos;
        this.keyValue = data;
        vTable.updateFastStorage( data, this );
        if( columnPos < numFields )
        {
            if( data != null )
            {
                fieldData[ columnPos - 1 ] = data;
                setFieldDelta(columnPos, true );
                setIndex( columnPos );
            }
        
        }
    }

    public void setValue( String columnName, String value )
    {
        int columnPos = vTable.getColumnNumberForName(columnName);     
        if( columnName.equals( this.keyName ))
        {
            keyValue = value;
            vTable.updateFastStorage( keyValue, this );
        }
        
        try
        {
            if( columnPos < numFields )
            {
                fieldData[ columnPos - 1 ] = value;           
                setFieldDelta(columnPos, true );
            }

            else
            {
                put( numFields, value, vTable.getMetaData().getType( columnPos) );
                setFieldDelta(columnPos, true );
            }            
        }
        catch( ArrayIndexOutOfBoundsException e )
        {
           vTable.getDbUser().getVDB().databaseError(  new VDBEvent( this,  new VDBMessage( VDBMessage.DBMessage.FieldError ), "Missing Field: " + columnName + " in Table: " + vTable.getName() ));
        }
        // try and add the value to the parent tables indexes, if any
        this.setDelta(true);
        vTable.addIndexValue( columnName, value, this );
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
                System.out.print( fieldData[i]  + " ");                
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

    public VField getField(String name) {       
        int columnPos = vTable.getColumnNumberForName(name );
        return new VField( this, columnPos );               
    }
    
    public String getFieldData( String name )
    {
        int columnPos = vTable.getColumnNumberForName(name );
        return getFieldData( columnPos );
    }
    
    public String getFieldData( int columnPos )    
    {            
        try
        {
            return fieldData[ columnPos - 1 ];
        }
        catch( Exception e )
        {
            log.write("Caught an exception while looking up data: " + e.getMessage() );
            return "";
        }
    }
    
    public Date getFieldDataAsDate( String name )
    {
        Date date = null;
        try
        {
            int columnPos = vTable.getColumnNumberForName(name );
            String dateAsString = fieldData[ columnPos - 1 ];
            
            date =  WMSTools.convertDateFromMySQLString( dateAsString, log );
            if( date != null )
            {
                log.write( "Got Date: " + date.toString() );
            }
        }
        catch( Exception ex )
        {
            log.write("Caught an exception while looking up data: " + ex.getMessage() );
            return null;
            
        }
        return date;
    }
    
    public VField getField( int columnPos ) {               
        return new VField( this, columnPos );               
    }  
    
    
    public void setPrimaryKeyName(  String primaryKey ) {        
        this.keyName = primaryKey;   
    }
    
    

    public String getPrimaryKeyName() {
        return keyName;
    }
    
    public String getPrimaryKeyValue()
    {
        return keyValue;
    }
    
    public Iterator<String> getColumnNames() {        
        return ( vTable.getColumnNames() );
    }
    
    public int getColumnNumberByName(String columnName) {
        int columnIndex = 1;
        for( int i = 0; i < numFields; i++ )
        {
            if( fieldData[i] != null
            && fieldData[i].equals( columnName ) )
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
        return this.keyName;
    }

    public void setName(String name) {        
        this.keyName = name;        
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
            rowData.addElement( fieldData[i] );            
        }                        
        return rowData;        
    }
    
    
    public Vector<VField> getFields()   
    {        
        Vector <VField>retFields = new Vector<VField>();        
        for( int i = 0; i < numFields; i++ )            
        {            
            VField field = new VField( this, i  + 1  );
            field.setDelta( fieldHasDelta[ i ] );
            retFields.add( field ); 
        }        
        return retFields;        
    }
          
    public String getDataAsString() {
        StringBuffer buf = new StringBuffer();        
        for (int i = 0; i < numFields; i++) {
            buf.append( fieldData[i] );
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
        // log.write(GesLog.TRACE, "Delta count is now: " + vTable.deltaCount() );
    }
    
    public void resetDeltas( )
    {
                    
        for( int i = 1; i <= fieldHasDelta.length; i++ )            
        {
                fieldHasDelta[ i - 1 ] = false;                            
        }
    }
    
    public boolean allDeltas()
    {
        boolean allDeltasTrue = true;
         for( int i = 1; i <= fieldHasDelta.length; i++ )            
         {
            if( ! fieldHasDelta[ i - 1 ] )
            {
                allDeltasTrue = false;
            }
         }
         return allDeltasTrue;
    }
    
    public boolean hasRealRecord()
    {
        return hasRealRecord;
    }
    
    public void hasRealRecord( boolean flag )
    {
        hasRealRecord = flag;
    }
    
    public boolean hasPrimaryField( )
    {
        return ( keyName != null );
    }
    
    public String getRowKey()
    {
        return this.rowKey;
    }
    
    public void setRowKey( String newRowKey )
    {
        this.rowKey = newRowKey;
    }
    
    public boolean updateField( int columnNumber, String newValue )
    {
        if( getFieldData( columnNumber ) != null && ! getFieldData( columnNumber ).equals( newValue ))
        {
            setData( columnNumber, newValue );
            setDelta( true );
            vTable.addIndexValue( columnNumber, newValue, this );
            return true;
        }      
        
        return false;
    }
    
    public void setIndex( int pos )
    {
        String columnName = vTable.getColumnName( pos );
        if( columnName != null )
        {
            vTable.addIndexValue( columnName, getFieldData( columnName ), this );
        }
    }
    
    public void setIndex( String ColumnName )
    {
        int pos = vTable.getColumnNumberForName(ColumnName); 
        vTable.addIndexValue( ColumnName, getFieldData( ColumnName ), this );
    }
}