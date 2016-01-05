/*
 * 
 * 
 * Created on Jun 21, 2004
 * 
 * 
 * 
 * 
 * 
 * To change the template for this generated file go to
 * 
 * 
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * 
 *  
 */
package com.wms.jdbtk3;

/**
 * 
 * 
 * @author mstemen
 * 
 * 
 * 
 * 
 * 
 * To change the template for this generated type comment go to
 * 
 * 
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * 
 *  
 */
import com.wms.jdbtk3.VDriver.DriverClass;
import java.util.*;
import java.sql.*;
import com.wms.util.WMSLog;
import com.wms.util.gui.GUILogInterface;
import com.wms.util.gui.GUILogInterface.GLogLevel;




public class VDatabase extends Hashtable<Object, Object> implements VDBListener
{
    private static final long serialVersionUID = -836081305879958233L;

    /**
     * 
     * 
     * 
     * 
     * 
     * @uml.property name="dbUser"
     * 
     * 
     * @uml.associationEnd
     * @uml.property name="dbUser" multiplicity="(1 1)"
     * 
     *  
     */
    private String name;
    private DBUser dbUser = null;
    /**
     * 
     * @uml.property name="dbUsers"
     * @uml.associationEnd
     * @uml.property name="dbUsers" multiplicity="(0 1)"
     *               qualifier="getName:java.lang.String
     *               user:dbtk.db_toolkit.DBUser"
     */
    // derby driver info: "org.apache.derby.jdbc.ClientDriver"
    private Hashtable<String, DBUser> dbUsers = new Hashtable<String, DBUser>();
    private Hashtable<String, String> primaryKeys = new Hashtable<String, String>();
    private String dbUrl;
    private String driverName;
    private int primaryKeyIndex = -1;
    private int numTables = 0;
    private boolean setPolling = false;
    private long pollingWait = 5000;
    private Thread databaseThread = null;
    private DatabaseWorker worker = new DatabaseWorker();
    private WMSLog log = null;
    private GUILogInterface gLog = null;
    private boolean suspended = false;
    private List<VDBListener> listeners = new ArrayList<VDBListener>();
    private VDBMessage vdbMessage;
    
    private DriverClass driverClass = DriverClass.MySql;
    private VDriver vDriver = null;
    //derby driver: org.apache.derby.jdbc.ClientDriver

    public VDatabase(GUILogInterface log, DriverClass driverClass)
    {
        this.gLog = log;
        this.driverClass = driverClass;
        int currentTimeout = DriverManager.getLoginTimeout();
        try {
            VDriverMgr.addDriver(driverClass);
        // System.out.println( "Current driver login timeout set to: " + currentTimeout  + ". Setting timeout to: 30");
        // DriverManager.setLoginTimeout( 30 );
        // System.out.println( "Current driver login timeout set to: " + currentTimeout  + ". Setting timeout to: 30");
        // DriverManager.setLoginTimeout( 30 );
        }
        catch (ClassNotFoundException ex) {
            gLog.writeToGui(ex.toString(), GLogLevel.Debug);
        }
        catch (InstantiationException ex) {
            gLog.writeToGui(ex.toString(), GLogLevel.Debug);
        }
        catch (IllegalAccessException ex) {
            gLog.writeToGui(ex.toString(), GLogLevel.Debug);
        }
        catch (SQLException ex) {
            gLog.writeToGui(ex.toString(), GLogLevel.Debug);
        }
    // System.out.println( "Current driver login timeout set to: " + currentTimeout  + ". Setting timeout to: 30");
    // DriverManager.setLoginTimeout( 30 );
    }

    public void construct(String tableName)
    {

        if (tableName != null) {
            build(tableName);
            populate(tableName);
        }

        StringBuilder sb = new StringBuilder("Loaded  ");
        sb.append(getTable(tableName).getRowCount());
        sb.append(" rows for Table: ").append(tableName);
            
        log( sb.toString(), GLogLevel.Trace );

    }

    public void startPolling(long pollTime)
    {
        worker.setPollTime(pollTime);
        worker.start();
        databaseThread = new Thread(worker);
        databaseThread.setName("VDatabase-Worker: " + databaseThread.hashCode());
        databaseThread.start();
    }

    public void addObserver(Observer observer)
    {
        worker.addObserver(observer);
    }

    public void build()
    {
        // go to the real database and build table objects based on what is in


        // the named database
        String sql_string = "Show tables";
        DBToolkit2 dbt = DBToolkit2.getInstance();
        Connection conn;
        try {
            conn = dbt.getDbm().getConnection(dbUser, dbUrl);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql_string);
            while (rs.next()) {


                String table_name = rs.getString(1);


                if (table_name != null && table_name.length() > 0) {
                    VTable v_table = new VTable(dbUser, table_name);
                    v_table.build();
                    // learn about the primary key field;
                    String primaryKey = v_table.getPrimaryKey();


                    if (primaryKey != null) {
                        primaryKeys.put(primaryKey, primaryKey);
                    }


                    this.put(table_name, v_table);


                }


            }


        // now that the tables are built, learn about the relationships
        }
        catch (SQLException e) {
            System.out.println("VDatabase::build error: " + e.getMessage());
            System.out.println(e.getMessage());
            return;
        }


        try {
            conn.close();
        }
        catch (SQLException e) {
            System.out.println("VDatabase::populate error while closing: " + e.getMessage());
            return;
        }
    }

    public void build(String tableName)
    {
        // go to the real database and build table objects based on what is in
        // the named database
        DBToolkit2 dbt = DBToolkit2.getInstance();
        if (tableName != null && tableName.length() > 0) {
            VTable v_table = new VTable(dbUser, tableName);
            v_table.build();
            // learn about the primary key field;
            String primaryKey = v_table.getPrimaryKey();
            if (primaryKey != null) {
                primaryKeys.put(primaryKey, primaryKey);
            }
            this.put(tableName, v_table);
        }
    // now that the tables are built, learn about the relationships

    }

    public DriverClass getVDriverClass()
    {
        return driverClass;
    }

    public void learnTopolgy()
    {
        //TODO: this needs to be altered for the new design...
        /*
         * // go through all the header fields in all the tables and find ones
         * that // are primary in other tables Enumeration table_keys =
         * this.keys(); while (table_keys.hasMoreElements()) { VTable v_table =
         * (VTable) get(table_keys.nextElement()); if (v_table != null) {
         * System.out.println("Analyzing table: " + v_table.getName());
         * VMetaRecord header = v_table.getMetaData(); if (header.getNumber() ==
         * 0) { continue; } for (int i = 1; i < header.getNumber(); i++) { //
         * System.out.println( "Index is: " + i ); // VField v_field =
         * header.getSignatureField(i);
         * 
         * if (v_field != null) { String targetKey = header.getFieldName( i );
         * String key_field = (String ) primaryKeys.get(targetKey); if
         * (key_field != null && !v_field.isKeyField() &&
         * !v_field.getTableName().equals( key_field.getTableName())) // if(
         * primaryKeys.contains( targetKey ) ) { String keyFieldTableName =
         * key_field.getTableName(); // v_field.setForeignKey( v_table.getName() );
         * v_field.setForeignKey(keyFieldTableName); VTable foreignTable =
         * (VTable) get( keyFieldTableName ); Vector fields =
         * v_table.getColumnByColumnNumber(i); if( fields == null ) break;
         * 
         * 
         * for (int j = 0; j < fields.size(); j++) {
         * 
         * 
         * VField tField = (VField) fields.get(j);
         * 
         * 
         * if (tField != null
         * 
         *  && foreignTable != null )
         * 
         *  {
         * 
         * 
         * v_field.setForeignRecord(foreignTable.getRecord(key_field
         * 
         * 
         * .getName(), tField
         * 
         * 
         * .getFieldData()
         * 
         * 
         * .toString()));
         * 
         * 
         * tField.setForeignKey(keyFieldTableName);
         * 
         * 
         * tField.setForeignRecord(foreignTable.getRecord(key_field
         * 
         * 
         * .getName(), tField
         * 
         * 
         * .getFieldData()
         * 
         * 
         * .toString()));
         * 
         *  }
         * 
         *  }
         * 
         * 
         * 
         * 
         *  }
         * 
         * 
         * 
         * 
         *  }
         * 
         *  }
         * 
         *  }
         * 
         *  }
         * 
         * 
         * 
         *  
         */
    }

    public void populate(String tableName)
    {
        // go to the real database and build table objects based on what is in
        // the named database
        if (tableName != null && tableName.length() > 0) {
            VTable v_table = (VTable) this.get(tableName);
            v_table.populate();
        }
    }

    public void populate()
    {
        // go to the real database and build table objects based on what is in
        // the named database
        String sql_string = "Show tables";
        DBToolkit2 dbt = DBToolkit2.getInstance();
        Connection conn;
        try {
            conn = dbt.getDbm().getConnection(dbUser, dbUrl);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql_string);
            Enumeration table_names = this.getTableNames().elements();
            while (table_names.hasMoreElements()) {
                String table_name = (String) table_names.nextElement();
                if (table_name != null && table_name.length() > 0) {
                    VTable v_table = (VTable) this.get(table_name);
                    v_table.populate();
                }
            }
        }
        catch (SQLException e) {
            System.out.println("VDatabase::populate error: " + e.getMessage());
            return;
        }
    }

    public void describeTables()
    {


        Enumeration enumer = keys();


        while (enumer.hasMoreElements()) {


            VTable v_table = (VTable) get(enumer.nextElement());
            System.out.println("\n\nTable: " + v_table.getName() + "\n----------------------------\n");
            v_table.describe();
        }





    }

    public Vector getTableNames()
    {


        Vector<String> ret_vec = new Vector<String>();
        Enumeration enumer = keys();
        System.out.println("Tables for database: " + getName() + "\n");
        while (enumer.hasMoreElements()) {
            String table_name = (String) enumer.nextElement();
            ret_vec.addElement(table_name);
        }

        return ret_vec;
    }

    public void showTables()
    {


        Enumeration enumer = keys();


        System.out.println("Tables for database: " + getName() + "\n");

        while (enumer.hasMoreElements()) {
            String table_name = (String) enumer.nextElement();
            System.out.println(table_name);
        }





    }

    private void resetTables()
    {
        Enumeration tables = this.elements();
        while (tables.hasMoreElements()) {
            VTable table = (VTable) tables.nextElement();
            if (table != null) {
                table.reset();
            }
        }
    // this.clear();

    }

    public Enumeration getTables()
    {


        return keys();


    }

    public Object put(Object key, Object table)
    {


        try {





            // String class_name = table.getClass().getName();


            //System.out.println( "Table class name is: " + class_name );


            Class table_class = table.getClass();


            Class base_class = Class.forName("com.wms.jdbtk3.VTable");


            if (table_class != base_class) {


                System.out.println("VDatabase::put: Unable to insert non-v_columns");


            }





            super.put(key, table);


            this.numTables++;


        }
        catch (ClassNotFoundException e) {


            System.out.println(e.getMessage());


        }





        return this;


    }

    public VTable getTable(String table_name)
    {


        VTable vt = null;





        if (this.containsKey(table_name)) {


            vt = (VTable) get(table_name);


        }


        return vt;


    }

    public void addUser(DBUser user)
    {


        this.dbUsers.put(user.getName(), user);


    }

    /**
     * 
     * 
     * @return
     * 
     * 
     * @uml.property name="dbUrl"
     * 
     *  
     */
    public String getDbUrl()
    {


        return dbUrl;


    }

    /**
     * 
     * 
     * @return
     * 
     * 
     * @uml.property name="dbUser"
     * 
     *  
     */
    public DBUser getDbUser()
    {


        return dbUser;


    }

    /**
     * 
     * 
     * @return
     * 
     * 
     * @uml.property name="driverName"
     * 
     *  
     */
    public String getDriverName()
    {


        return driverName;


    }

    /**
     * 
     * 
     * @return
     * 
     * 
     * @uml.property name="name"
     * 
     *  
     */
    public String getName()
    {


        return name;


    }

    /**
     * 
     * 
     * @param string
     * 
     * 
     * 
     * 
     * 
     * @uml.property name="dbUrl"
     * 
     *  
     */
    public void setDbUrl(String string)
    {


        dbUrl = string;


    }

    /**
     * 
     * 
     * @param user
     * 
     * 
     * 
     * 
     * 
     * @uml.property name="dbUser"
     * 
     *  
     */
    public void setDbUser(DBUser user)
    {


        dbUser = user;


    }

    /**
     * 
     * 
     * @param string
     * 
     * 
     * 
     * 
     * 
     * @uml.property name="driverName"
     * 
     *  
     */
    public void setDriverName(String string)
    {


        driverName = string;


    }

    /**
     * 
     * 
     * @param string
     * 
     * 
     * 
     * 
     * 
     * @uml.property name="name"
     * 
     *  
     */
    public void setName(String string)
    {


        name = string;


    }

    /**
     * 
     * 
     * This method with purge the virtual footprint of the database tables
     * 
     *  
     */
    public void clear()
    {


        reset();


    }

    public void reset()
    {
        resetTables();
        Enumeration db_keys = keys();
        while (db_keys.hasMoreElements()) {
            remove(db_keys.nextElement());
        }
        primaryKeys.clear();
        numTables = 0;
    }

    public void suspendUpdates(boolean flag)
    {
        suspended = flag;
    }

    public int getTableCount()
    {


        return numTables;


    }

    class DatabaseWorker extends Observable implements Runnable
    {

        private long waitTime = 5000;
        private boolean running = false;
        private String name = "Unitialized Thread name";
        private String currentTableName = "Unknown";

        public void run()
        {
            StringBuffer sb = null;
            while (running) {

                try {
                    // update all the tables in the database then let the observers
                    // know it is done                                
                    Enumeration dbKeys = keys();
                    // log.write( "updating tables");     
                    // VDatabase.this.suspended = true;
                    while (dbKeys.hasMoreElements()) {
                        
                        if (VDatabase.this.suspended == true) {
                            log.write("Updates are suspended, sleeping...");
                            break; // go to sleep, until the suspend is lifted
                        }

                        VTable table = (VTable) get(dbKeys.nextElement());
                        if (table != null && !table.isLocked() && table.isPolling()) {
                            currentTableName = table.getName();
                            // System.out.println( "VDataBase::DatabaseWorker::run: checking table:" + table.getName());            
                            if (!table.isAlwaysPoll()) // this path is prefered, as it only updates if the table has changes to it
                            {
                                if (table.checkUpdated()) // if( !  table.checkTable() )
                                {

                                    sb = new StringBuffer("Not up to date, updating table:");
                                    log.write(sb.append(table.getName()).toString());
                                    // System.out.println( sb.toString() );    
                                    if (table.getKeyType() == VTable.TableKeyType.PRIMARY) {
                                        table.refresh();
                                        table.databaseChanged(new VDBEvent(table, new VDBMessage(VDBMessage.DBMessage.TableChanged), ""));
                                    }
                                    /*
                                    else if( table.getKeyType() == VTable.TableKeyType.COMPOUND )
                                    {
                                    table.vFlush();
                                    table.databaseChanged( new VDBEvent( table, new VDBMessage( VDBMessage.DBMessage.TableChanged ), "" ));
                                    }
                                     **/
                                    // table.dumpTable();
                                    log.write(WMSLog.TRACE, "VDataBase::DatabaseWorker::run: updating observers");
                                    setChanged();
                                    notifyObservers(table);


                                }
                                else {
                                    sb = new StringBuffer("Up to date, on table:");
                                    log.write(WMSLog.TRACE, sb.append(table.getName()).toString());
                                }
                            }
                            else // force the update, this is not preferred, but is required if the database does not support update time stamps
                            {
                                sb = new StringBuffer("Not up to date, updating table:");
                                log.write(sb.append(table.getName()).toString());
                                // System.out.println( sb.toString() );
                                if (table.getKeyType() == VTable.TableKeyType.PRIMARY) {
                                    table.refresh();
                                    table.databaseChanged(new VDBEvent(table, new VDBMessage(VDBMessage.DBMessage.TableChanged), ""));
                                }
                                /*
                                else if( table.getKeyType() == VTable.TableKeyType.COMPOUND )
                                {
                                table.vFlush();
                                table.databaseChanged( new VDBEvent( table, new VDBMessage( VDBMessage.DBMessage.TableChanged ), "" ));
                                }
                                 **/
                                // table.dumpTable();
                                log.write(WMSLog.TRACE, "VDataBase::DatabaseWorker::run: updating observers");
                                setChanged();
                                notifyObservers(table);
                            }

                        }
                    }
                }
                catch (NullPointerException e) {
                    if (name != null) {
                        log.write(WMSLog.CRITICAL, name + " got a NullPointerException while updating Table: " + currentTableName);
                    }
                    e.printStackTrace();
                }
                catch (OutOfMemoryError e) {
                    if (name != null) {
                        log.write(WMSLog.CRITICAL, name + " got a OutOfMemoryError while updating Table: " + currentTableName);
                    }
                }

                try {
                    Thread.sleep(waitTime);
                }
                catch (InterruptedException e) {
                    System.out.print("Sleep interrupted:" + e);
                }
            }

        }

        private void setPollTime(long newPollTime)
        {
            this.waitTime = newPollTime;
        }

        public void stop()
        {
            running = false;
        }

        public void start()
        {


            running = true;


        // run();


        }
    }

    public synchronized void addListener(VDBListener listener)
    {
        if (!listeners.contains(listener));
        listeners.add(listener);
    }

    public synchronized void removeListener(VDBListener listener)
    {
        listeners.remove(listener);
    }

    private synchronized void fireVDBEvent(VDBEvent event)
    {
        // this.processUDTEventForSelf( event );
        Iterator eventListeners = listeners.iterator();
        while (eventListeners.hasNext()) {
            ((VDBListener) eventListeners.next()).databaseChanged(event);
        }
    }

    private synchronized void fireVDBEventError(VDBEvent event)
    {
        // this.processUDTEventForSelf( event );
        Iterator eventListeners = listeners.iterator();
        while (eventListeners.hasNext()) {
            ((VDBListener) eventListeners.next()).databaseError(event);
        }
    }

    public void databaseChanged(VDBEvent event)
    {
        vdbMessage = event.getMessage();
        fireVDBEvent(event);
    }

    public void databaseError(VDBEvent event)
    {
        vdbMessage = event.getMessage();
        fireVDBEventError(event);
    }

    public boolean isSuspended()
    {
        return suspended;
    }

    private void log( String msg, GUILogInterface.GLogLevel lvl)
    {
	if( gLog != null )
		gLog.writeToGui(msg, lvl);
    }

    private void log( String msg, WMSLog.LogLevel lvl )
    {
        log.write(lvl.ordinal(), msg);
    }

}
 
