/*
 * VTableIndex.java
 *
 * Created on November 30, 2005, 10:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.jdbtk3;

import java.util.*;
/***
 *
 * @author mstemen
 */
public class VTableIndex {
   
    // String is the value of the field that is indexed
    // Since this is an inner class, all the methods that would normally be public due to extension have to be 
    // explicitly be declared public.
    class RecordGroup extends HashSet<VRecord>
    {
        public VRecord getOnly()
        {
            if( super.size() == 1 )
            {
                Iterator<VRecord> it = this.iterator();
                return it.next();
            }
            return null;
        }        
        
        public boolean containsValue( VRecord rec )
        {            
            return super.contains( rec );
        }
        
        public Vector<VRecord> getVector()
        {            
            return new Vector( this );
        }
    }
    
    class IndexGroup
    {
        
        HashMap<String, RecordGroup> map = new HashMap<String, RecordGroup>();
        public void addElement( String key, VRecord record )
        {
            
            if( map.containsKey( key ))
            {
               RecordGroup rg = map.get( key );
               if( rg != null )
               {
                   if( ! rg.containsValue( record ))
                       rg.add( record );
               }
               // map.get(key).add( record );
            }
            else
            {
                RecordGroup recordSet = new RecordGroup();
                recordSet.add( record );
                map.put( key, recordSet );
            }
        }
        
        public RecordGroup get( String key )
        {
            return map.get( key );
        }
                
        
    }
    private HashMap<String, IndexGroup> index = new HashMap<String, IndexGroup>();
    /** Creates a new instance of VTableIndex */
    public VTableIndex() {
        
        
    }
    public boolean isIndexed( String fieldName )
    {
        return( index.containsKey( fieldName ));
    }
    
    public void addIndexedField( String fieldName )
    {
        index.put( fieldName, new IndexGroup() );
    }
    public boolean addIndexedValue( String fieldName, String value, VRecord record )
    {
        IndexGroup ig = null;
        boolean added = false;
        if( index.containsKey( fieldName ))
        {
            ig = index.get( fieldName );
            if( ig != null )
            {
                ig.addElement( value, record );
                added = true;
            }
        }
        return added;
    }   
    public RecordGroup getRecords( String fieldName, String fieldValue )
    {
        if( isIndexed( fieldName ))
        {
            return index.get( fieldName).get( fieldValue );
        }
        return null;
    }
    
    public VRecord getRecord( String fieldName, String fieldValue )
    {
        if( isIndexed( fieldName ))
        {
            IndexGroup ig  = index.get( fieldName );            
            RecordGroup rg = ig.get( fieldValue );           
            if( rg != null )
                return rg.getOnly();
        }
        return null;
    }
    
}
