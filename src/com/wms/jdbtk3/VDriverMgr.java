/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wms.jdbtk3;

import com.wms.jdbtk3.VDriver.DriverClass;
import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 * @author mstemen
 */




public class VDriverMgr {

  
    static String derbyDriverName = "org.apache.derby.jdbc.ClientDriver";
    static String mysqlDriverName = "com.mysql.jdbc.Driver";

    private static HashMap<DriverClass, VDriver> vDriverMap = new HashMap<DriverClass, VDriver>();

    public static void addDriver(DriverClass driverKey) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException
    {
        VDriver newVDriver = null;

                switch( driverKey)
                {
                case Derby:
                    newVDriver = new VDriver( derbyDriverName );
                    break;
                case MySql:
                    newVDriver = new VDriver( mysqlDriverName );
                    break;
                }

                if( newVDriver != null )
                {
                    addDriver( driverKey, newVDriver);
                }
    }


    public static VDriver getVDriver(DriverClass driverKey)
    {
        return vDriverMap.get(driverKey);
    }

    private static void addDriver( DriverClass driverKey, VDriver vDriver )
    {
        vDriverMap.put(driverKey, vDriver);
    }

}
