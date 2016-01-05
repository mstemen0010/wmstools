/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wms.jdbtk3;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.derby.jdbc.ClientDriver;


/**
 *
 * @author mstemen
 */
public class VDriver
{

    /**
     * @return the derbyDriver
     */
    public ClientDriver getDerbyDriver()
    {
        return derbyDriver;
    }

    public enum DriverClass
    {
        MySql,
        Derby;
    }

    private String driverName;
    private Class driver;
    private Driver classDriver;
    private ClientDriver derbyDriver = null;
    private DriverClass currentDriverClass = DriverClass.Derby;
    private boolean driverLoaded = false;
    private boolean driverMapped = false;


    /**
     * @return the driverName
     */
    
    public VDriver( String driverName ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException
    {
        this.driverName = driverName;        
        this.loadDriver();
    }

    public String getDriverName()
    {
        return driverName;
    }

    /**
     * @param driverName the driverName to set
     */
    public void setDriverName(String driverName)
    {
        this.driverName = driverName;
    }

    /**
     * @return the driver
     */
    public Class getDriver()
    {
        return driver;
    }

    /**
     * @param driver the driver to set
     */
    public void setDriver(Class driver)
    {
        this.driver = driver;
    }

    public void loadDriver() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException
    {
        // Load the JDBC driver
        // Class.forName( db.getDriverName() );

        driver = (Class) ClassLoader.getSystemClassLoader().loadClass(driverName);
        driverLoaded = true;

        Object ob = driver.newInstance();
        if(Driver.class.isInstance(ob))
        {
            classDriver = (Driver) ob;
        }
        else if (ClientDriver.class.isInstance(ob))
        {
            derbyDriver = (ClientDriver) ob;
        }
//        classDriver = (Driver) driver.;

//      Class driverClass = ClassLoader.getSystemClassLoader().loadClass(driverName);
//      Driver driver;
//      if( driverClass != null  )
//      {
//          if( driverClass.isInstance(Driver.class) )
//          {
//              driver = driver.cast( driverClass.);
//          }
//      }

        if( classDriver != null )
            DriverManager.registerDriver(classDriver);
    }
    public boolean isDriverMapped()
    {
        return driverMapped;
    }

    // package level only
    void  driverIsMapped()
    {
        driverMapped = ! driverMapped; // toggle
    }
}
