/*
 * VDBTypes.java
 *
 * Created on February 1, 2005, 10:13 PM
 */

package com.wms.jdbtk3;

/**
 *
 * @author  mstemen
 */
public class VDBTypes {
   
    public enum DBDataTypes
    {
        Unknown(-1),
        Unsigned(0),
        Int(1),
        TinyText(2),
        Text(3),
        VarChar(4),
        BigInt(5),
        Date(6);
        
        
        private DBDataTypes( int type  )
        {
            this.type = type;
        }
        
        public static DBDataTypes getTypeForString(String db_type) 
        {		

		if (db_type.indexOf("unsigned") >= 0 && db_type.indexOf("int") >= 0)
                    return Unsigned;
		else if (db_type.indexOf("int") >= 0)
                    return Int;
		else if (db_type.indexOf("text") >= 0)
                    return VarChar;
                else if (db_type.indexOf("varchar") >= 0)                    
                    return VarChar;
                else if (db_type.indexOf("char") >= 0)                    
                    return VarChar;                
                else if( db_type.indexOf( "bigint" ) >= 0 )
                    return BigInt;  
                else if( db_type.indexOf( "timestamp" ) >= 0 )
                    return Date;              
                return Unknown;
	}
        
        static public boolean isNumber( DBDataTypes type )
        {
            if( type == Unsigned 
            || type ==  Int
            || type == BigInt )
            {
                return true;
            }
            return false;
        }        

        static public boolean isString( DBDataTypes type )
        {
            if( type == TinyText 
            || type ==  Text
            || type == VarChar )
            {
                return true;
            }
            return false;
        }        
        
        static public String getJavaType( DBDataTypes type )
        {
            switch( type )
            {
                case Unsigned:
                    return "Integer";                                
                case Int:
                    return  "Integer";
                case TinyText:
                    return "String";
                case Text:
                    return "String";
                case VarChar:
                    return "String";
                case BigInt: 
                    return "Long";
                case Unknown:
                default:
                    return null;
            }            
        }
        static public String getJDBCShortType(DBDataTypes type)
        {
            switch( type )
            {
                case Unsigned:
                    return "%d";
                case Int:
                    return  "%d";
                case TinyText:
                    return "%s";
                case Text:
                    return "%s";
                case VarChar:
                    return "%s";
                case BigInt:
                    return "%d";
                case Unknown:
                default:
                    return "%d";
            }
        }
        private int type;
    }    
    
    /** Creates a new instance of VDBTypes */
    public VDBTypes() {
    }
    
}
