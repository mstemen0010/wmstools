/*
 * VTableProxy.java
 *
 * Created on August 8, 2005, 11:11 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.wms.jdbtk3;

import java.util.*;
import java.sql.*;

import com.wms.util.*;


/**
 *
 * @author mstemen
 */
public class VTableProxy 
{
    /**
     * A proxy table is designed such that its contends are determined on the fly
     * there are no synchronization elements for this type of table
     */
    
    /** Creates a new instance of VTableProxy */
    
    private VTable vTable;
    private WMSLog log;
     private DBUser user = null;
     
    public VTableProxy( DBUser user, String name )
    {
        vTable = new VTable( user, name );
        this.user = user;
        vTable.build();
    }
      
    
    public void runQuery( String newQuery )
    {               
        ResultSet rs = null;
        Statement st = null;
        
        Connection conn = connect();
        if (null == conn) {
            log.write("Unable to get connection, aborting populate");
            return;
        }
        try 
        {
            st = conn.createStatement();
            rs = st.executeQuery(newQuery);
            
            if( rs != null )
            {
                vTable.populate(rs);
            }
        }         
        catch (SQLException e) 
        {
            // Could not connect to the database
            if (e.toString().indexOf("Illegal operation on emtpy") != -1)
                log.write("Database Error:" + e.toString() + " for table: " + vTable.getName());
        }
//        if( SCPMgr.getMgr() != null )
//        {
//            log = SCPMgr.getMgrLog();
//        }
    }
    
    public VRecord getRecord(String columnName, String columnValue)
    {
        return vTable.getRecord( columnName, columnValue );
    }
    
    public Vector<VRecord> getRecords( String columnName, String columnValue ) 
    {
        return vTable.getRecords( columnName, columnValue );
    }
    
        
    public Vector<VRecord> getRows() {
        return vTable.getRows();
    }
    
    public int getRowCount()
    {
        return vTable.getRowCount();
    }
    
    private Connection connect() {
        Connection conn = null;
        
        
        // get the DBTK
        Connection connection  = null;
        int reconnects = 5;
        int reconnectWaitTime = 1000;
        while( reconnects > 0 ) {
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                connection  = DriverManager.getConnection( user.getDbUrl(), user.getName(), user.getPasswd());
            } catch (SQLException e) {
                // Could not connect to the database
                log.write( "Database Error:" + e.toString());
            } catch ( java.lang.Exception e ) {
                log.write( "Instance creation error:" + e.toString());
            }
            if( connection != null ) {
                break;
            } else {
                reconnects--;
            }
            try {
                Thread.sleep( reconnectWaitTime );
            } catch( InterruptedException e ) {
                
            }
        }
        if( connection == null ) {            
//            SCPMgr.getMgr().sendPopup( new ParallaxEvent( this, ParallaxMessage.POPWARN, vTable.getName() + ": Unable to get connection.\nConnection was NULL for operation"));
        }
        
        return connection;
    }    
}
