/*
 * Created on June 21, 2004, 2:10 PM
 */

package com.wms.jdbtk3;

import java.util.*;


/**
 * 
 * @author mstemen
 */

public class VField {
        // for data transport when the vfield is not associated with a real
        // record
         
    private String fieldData = "";    
    private VDBTypes.DBDataTypes type = VDBTypes.DBDataTypes.Unknown;    
    private boolean isEditableFlag = false;           
    private VRecord parentRecord;        
	private int columnPos = 0;        
	private boolean editable = false;            
	private VRecord foreignRecord;		    
	private boolean hasDelta;          
    private Hashtable <String, ObjectMap>bindings = null;    
    private String name = "";
    
    public VField(VRecord parentRecord, int columnPos ) {
            
            this.columnPos = columnPos;
            if( parentRecord != null )
            {
                this.name = parentRecord.getFieldName(columnPos);
                this.parentRecord = parentRecord;
            }
        }
       
    
    public VField( String name, String data, VDBTypes.DBDataTypes type, boolean editable )      
    {
        this.name = name;           
        parentRecord = null;
        this.fieldData = data;
        this.type = type;
        this.isEditableFlag = editable;       
    }
        
	public void bindObjectToField(Object objectToBind, String objectName, String methodSignature) {
		ObjectMap objectMap = new ObjectMap();
		objectMap.addMappedObject(objectName, objectToBind);
		objectMap.bindFieldRelationship(getName(), methodSignature);
                bindings = new Hashtable<String, ObjectMap>();
		bindings.put(objectName, objectMap);
	}

	public void syncObjects() {
		Hashtable <String, String>dataMap = new Hashtable<String, String>();
		dataMap.put(this.getName(), (String)this.getFieldData());
		Enumeration bindingKeys = bindings.keys();
		while (bindingKeys.hasMoreElements()) {
			String keyName = (String) bindingKeys.nextElement();
			ObjectMap oMap = (ObjectMap) bindings.get(keyName);
			if (!oMap.updateWithHashtable(dataMap))
                        {
				System.out.println("Sync on Object:" + keyName + " failed");

			}
		}
	}

        public String getName()
        {
            if( parentRecord == null )
            {
                return name;
            }
            return parentRecord.getFieldName( columnPos );
        }

	/**
     * @return Returns the field_data.
     * 
     * @uml.property name="fieldData"
     */

	public String getFieldData() 
        {		
            if( parentRecord == null )
                return fieldData;
            
            return parentRecord.getData( this.columnPos );
	}         

	public void setFieldData( String data ) 
        {
            if( parentRecord == null )
            {
                fieldData = data;
                return;
            }
            parentRecord.setData( this.columnPos, data );
	}  

        public boolean getFieldHasDelta( )
        {
            if( parentRecord != null )
            {
                return parentRecord.getFieldDelta( this.columnPos );
            }
            return this.hasDelta;
        }
        
        public void setFieldHasDelta( boolean delta)
        {
            if( parentRecord != null )
            {
                parentRecord.setFieldDelta( this.columnPos, delta );
            }             
            else
            {
                this.hasDelta = delta;
            }
        }

        public void changeFieldData( String data )
        {
            if( parentRecord != null )
            {
                parentRecord.setData( columnPos, data );           
                parentRecord.setFieldDelta(columnPos, true );
             
            }
            else
            {
                this.fieldData = data;
                this.hasDelta = true;
            }
            
        }
      /*
       * public void reset() { String data; VDBTypes.DBDataTypes type =
       * parentRecord.getType(columnPos); if( type == VDBTypes.DBDataTypes.Int ||
       * type == VDBTypes.DBDataTypes.BigInt || type ==
       * VDBTypes.DBDataTypes.Unsigned ) { data = new String("0"); } else // all
       * others for now { data = new String(); } parentRecord.setData(columnPos,
       * data ); }
       *  
       */

	public VDBTypes.DBDataTypes getFieldType() {
            if( parentRecord == null )
                return type;
            return parentRecord.getType(columnPos );
	}


	public void setFieldType( VDBTypes.DBDataTypes type ) {
            if( parentRecord == null )
                this.type = type;
            
            // this.parentRecord.setType( columnPos, type );
	}

	public String getField_type_java() {               
		return VDBTypes.DBDataTypes.getJavaType( parentRecord.getType(columnPos) );
	}

	public int getColumnPos() {
		return columnPos;
	}

	void setColumnPos(int column_pos) {
		this.columnPos = column_pos;
	}

	public void setEditable(boolean editable) {
                if( this.parentRecord == null )
                    this.isEditableFlag = true;
		this.editable = editable;
	}

	public boolean isEditable() {
                if( parentRecord == null )
                    return isEditableFlag;
                
		return editable;
	}

        public void setForeignRecord(VRecord vRec) {
		foreignRecord = vRec;
	}

	public VRecord getForeignRecord() {
		return foreignRecord;
	}

	public String getTableName() {
		return parentRecord.getVTable().getName();
	}

        public void setDelta( boolean delta )
        {
            hasDelta = delta;           
        }

        public boolean hasDelta( )
        {
            return hasDelta;
        }
                                
        public VRecord getRecord( )
        {
            return parentRecord;
        }
        
        public String toString()
        {
            return getFieldData() ;
        }

        public char getQuote()
        {
            char c = '\"';
            switch(type)
            {
                case Int:
                case BigInt:
                case Unsigned:
                    c = '\'';
                    break;
                case Text:
                case VarChar:
                case TinyText:
                    c = '\"';
                    break;
            }

            return c;
        }
}