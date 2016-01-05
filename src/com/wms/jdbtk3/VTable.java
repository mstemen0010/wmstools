package com.wms.jdbtk3;
/*
 * v_table.java
 *
 * Created on June 21, 2004, 2:36 PM
 */

/**
 *
 *
 *
 * @author mstemen
 *
 *
 */
/**
 *
 * VTables consists of VRows which are made of VFields
 *
 */
import com.wms.jdbtk3.VDBTypes.DBDataTypes;
import com.wms.jdbtk3.VTableIndex.RecordGroup;
import java.util.*;
import java.sql.*;
import java.text.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import javax.swing.table.TableModel;
import com.wms.util.*;

public class VTable extends Vector<VRecord> implements VDBListener, VTableListener {

    private String header_top_bottom = new String();
    private String name;
    private String db_name;
    private String maxKeyValue = new String();
    private VTable parentTable = null;
    private String header = new String("| ");
    private String totalHeader = new String();
    private WMSLog log = null;
    private boolean isLocked = false;
    private boolean alwaysPoll = false;
    private boolean searchIgnoreCase = false;
    private Object lock = new Object();
    private Object lockHolder = null;
    private int rowCount = 0;
    private int columnCount = 0;
    // private Hashtable<String, VRecord> records = new Hashtable<String,
    // VRecord>();
    private Hashtable<String, VField> objFields = new Hashtable<String, VField>();
    private String primaryKeyName;
    private int primaryKeyColumn = -1;
    private Timestamp CurrentTimeStamp = null;
    private VMetaRecord metaData;
    private String orderByFieldName = "";
    private boolean singleConnection = false;
    private Hashtable<String, VRecord> records = new Hashtable<String, VRecord>();
    private boolean enableFastLookup = true;
    // the following provides arbitrary indexing on a particular field in a table to allow fast ( hashmap based ) lookups
    private boolean isIndexed = false;
    private VTableIndex index = null;
    // private VRecord metaData;
    /**
     *
     * @uml.property name="tableSignature"
     * @uml.associationEnd
     * @uml.property name="tableSignature" multiplicity="(1 1)"
     *
     *
     */
    private Connection singleConn = null;
    private boolean isView = false;
    private Timestamp updateTime = null;

    /**
     * @uml.property name="user"
     * @uml.associationEnd
     * @uml.property name="user" multiplicity="(1 1)"
     *
     */
    public enum TableKeyType {

        UNKNOWN,
        PRIMARY,
        AUTOINC,
        COMPOUND;
    }
    private DBUser user = null;
    private boolean wasPopulated = false;
    private boolean wasBuilt = false;
    private boolean polling = false;
    private boolean synced = false;
    private Hashtable<String, ObjectMap> objectBindings = new Hashtable<String, ObjectMap>();
    /** Creates a new instance of v_table */
    private List<VDBListener> listeners = new ArrayList<VDBListener>();
    private List<VTableListener> tableListeners = new ArrayList<VTableListener>();
    private VDBMessage vdbMessage;
    private VDBMessage.DBMessage vTableMessage;
    private TableKeyType keyType = TableKeyType.UNKNOWN;
    private String sqlSelectString = null;
    private VDatabase vdb = null;

    public VTable(DBUser user, String name) {

        // super<String, Object>();

        this.name = name;
        this.user = user;
        this.db_name = user.getDb_name();

        DBToolkit2 dbtk = DBToolkit2.getInstance();
        log = dbtk.getLog();
        vdb = user.getVDB();
        this.translateSelectPattern("SELECT * FROM %T");
    }

    public VTable(ResultSet rs, VTable templateTable) {
        // construct from a resultset that is a describe or result of a query
        if (rs == null) {
            return;
        }
        // "build" the table from the templateTable
        this.metaData = templateTable.getMetaData();
        this.setPrimaryKey(templateTable.getPrimaryKey());
        this.user = templateTable.getDbUser();
        this.db_name = user.getDb_name();
        DBToolkit2 dbtk = DBToolkit2.getInstance();
        log = dbtk.getLog();
        try {
            build(rs.getMetaData());
        } catch (SQLException e) {
            log.write(e.getMessage());
        }
        if (templateTable.getName() != null) {
            this.name = templateTable.getName();
        }


        this.populate(rs);
        this.isView = templateTable.isView();
    }

    public void setIgnoreCase(boolean flag )
    {
        this.searchIgnoreCase = flag;
    }

    public VTable(ResultSet rs, String tableName) {


        // construct from a resultset that is a describe or result of a query


        VRecord signature_record;


        if (rs == null) {
            return;
        }


        try {





            if (tableName != null) {
                this.name = tableName;
            }





            this.build(rs.getMetaData());


            this.populate(rs);


            // this is technically a view, because there is no real table for it
            // in the database


            isView = true;


        } catch (SQLException e) {


            log.write("VTable( ResultSet ): " + e.getMessage());


        }


        DBToolkit2 dbtk = DBToolkit2.getInstance();


        log = dbtk.getLog();
    }

    public void finalize() {
        if (singleConn != null) {
            try {
                singleConn.close();
            } catch (SQLException e) {
                log.write(WMSLog.ERROR, "Unable to close connection. Reason was: " + e.getMessage());
            }
        }
    }

    public synchronized void addListener(VDBListener listener) {
        if (!listeners.contains(listener));
        listeners.add(listener);
    }

    public synchronized void addTableListener(VTableListener tableListener) {
        if (!tableListeners.contains(tableListener));
        tableListeners.add(tableListener);
    }

    public synchronized void removeListener(VDBListener listener) {
        listeners.remove(listener);
    }

    private synchronized void fireVRecordEvent(VDBEvent event) {
        // this.processUDTEventForSelf( event );
        Iterator eventListeners = listeners.iterator();
        while (eventListeners.hasNext()) {
            ((VDBListener) eventListeners.next()).databaseChanged(event);
        }
    }

    private synchronized void fireVDBEvent() {
        VDBEvent event = new VDBEvent(this, vdbMessage);
        Iterator eventListeners = listeners.iterator();
        while (eventListeners.hasNext()) {
            ((VDBListener) eventListeners.next()).databaseChanged(event);
        }

    }

    private synchronized void fireVDBEvent(VDBEvent event) {
        // this.processUDTEventForSelf( event );
        Iterator eventListeners = listeners.iterator();
        while (eventListeners.hasNext()) {
            ((VDBListener) eventListeners.next()).databaseChanged(event);
        }
    }

    private synchronized void fireVDBEventError(VDBEvent event) {
        // this.processUDTEventForSelf( event );
        Iterator eventListeners = listeners.iterator();
        while (eventListeners.hasNext()) {
            ((VDBListener) eventListeners.next()).databaseError(event);
        }
    }

    public void databaseChanged(VDBEvent event) {
        vdbMessage = event.getMessage();
        fireVDBEvent(event);
    }

    public void databaseError(VDBEvent event) {
        vdbMessage = event.getMessage();
        fireVDBEventError(event);
    }

    public synchronized void tableChanged(VTableEvent evt) {
        vTableMessage = evt.getMessage();
        fireTableEvent(evt);
    }

    private synchronized void fireTableEvent(VTableEvent event) {
        // this.processUDTEventForSelf( event );
        Iterator<VTableListener> eventListeners = tableListeners.iterator();
        while (eventListeners.hasNext()) {
            VTableListener listener = eventListeners.next();
            if (listener != this) {
                listener.tableChanged(event);
            }
        }
    }

    /**
     *
     *
     * Allows an indiviual field to be bound to a respective field in an object
     *
     *
     * if there is an Object with two member fields called "x,y" and that object
     *
     *
     * has accessor methods called setX( int ) and setY( int ), and the table
     *
     *
     * has two fields that would contain the vaules of x and y, then the field x
     *
     *
     * can be bound to setX( x ) by calling the method setX on the bound object.
     *
     *
     *
     *
     *
     * Method signature is of the form: "objectName.methodName( arg0 );"
     *
     *
     *
     *
     *
     *
     *
     *
     * @param objectToBind -
     *
     *
     * an instance of the actual bound object
     *
     *
     * @param objectName -
     *
     *
     * the actual name of the object ( i.e. Drive );
     *
     *
     * @param recordNumber -
     *
     *
     * the row that we are bound to.
     *
     *
     * @param fieldName -
     *
     *
     * the field to be bound to ( i.e. full );
     *
     *
     * @param methodSig -
     *
     *
     * the signature of the method on the object to be used to update
     *
     *
     * the object
     *
     *
     */
    public void reset() {
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null) {
                rec.reset();
            }
        }
        clear();
    }

    /*
     * synchronized public void bindObjectToTable (Object objectToBind, String
     * objectName, int recordNumber, String fieldName, String methodSignature) {
     *  // look up the record VRecord rec = this.getRecord(
     * metaData.recordNumber); if (rec == null) { log.write ("No such row number
     * in table "); return; } StringBuilder strBld = new StringBuilder ();
     * strBld.append ( String.valueOf ( recordNumber )).append (":").append (
     * fieldName ); String key = strBld.toString (); // first look to see if it
     * is already in the list VField field = (VField) objFields.get ( key ); if(
     * field == null ) { // it didn't exist, create it field = new VField ( rec,
     * this.getColumnNumberForName ( fieldName ) ); objFields.put ( key, field ); }
     * field.bindObjectToField (objectToBind, objectName, methodSignature );
     *  }
     */
    synchronized public void describe() {
    }

    synchronized public void dumpTable() {
        printHeader();
        // this.tableSignature.printUsingTemplate( 0, "" );
        for (int row = 0; row <= getRowCount(); row++) {
            //  ((VRecord) ( get(String.valueOf(row)))).printData();
        }
    }

    public boolean checkTable() {
        Connection conn = this.connect();
        StringBuffer sqlBuf = new StringBuffer("CHECK  TABLE ");
        sqlBuf.append(getName()).append(" CHANGED");
        try {
            Statement stmt = conn.createStatement();
            // log.write( "Executing: " + sqlBuf.toString() );
            ResultSet rs = stmt.executeQuery(sqlBuf.toString());
            if (rs != null) {
                // result looks like this:
                // | Table | Op | Msg_type | Msg_text |
                // | - | check | status | Table is already up to date |
                rs.next();
                String upToDate = rs.getString(4); // Msg_txt column
                if (upToDate.equalsIgnoreCase("table is already up to date")) {
                    return true;
                }
            }
            stmt.close();
            connClose(conn);
        } catch (SQLException e) {
            log.write(e.getMessage());
        }
        return false;
    }

    public boolean checkUpdated() {
        boolean updated = false;
        Timestamp os = CurrentTimeStamp;

        if (os == null) {
            return false;
        }
        getStatus();
        Timestamp cs = CurrentTimeStamp;
        long ctl2 = CurrentTimeStamp.getNanos();
        // log.write( "Table: " + name + " current timestamp:( " + cs.toString() + ") old time stamp: " + os.toString() );
        return (cs.after(os));
    }

    private Timestamp getDbTime(String stringTime) {
        java.util.Date td = null;

        SimpleDateFormat sdf = new SimpleDateFormat();
        String mySqlDatePattern = "yyyy-MM-dd HH:mm:ss.S"; // mysql date format
        sdf.applyPattern(mySqlDatePattern);
        try {
            td = sdf.parse(stringTime);
        } catch (ParseException pe) {
            log.write(pe.getMessage());
        } catch (Exception e) {
            log.write(e.getMessage());
        }
        return new Timestamp(td.getTime());
    }

    private void getStatus() {
        // connect();
        Connection conn = this.connect();
        StringBuilder query = null;
        query = new StringBuilder();
        StringBuilder message = null;
        query.append("SHOW TABLE STATUS FROM ").append(this.user.getDb_name()).append(" LIKE '").append(name).append("'");


        try {
            Statement stmt = null;

            stmt = conn.createStatement();
            // log.write( "Executing: " + sqlBuf.toString() );
            ResultSet rs = null;
            rs = stmt.executeQuery(query.toString());
            //System.out.println("query=" + query.toString() );
            rs.next();
            if (rs != null && rs.getString("Update_time") != null) {

                updateTime = getDbTime(rs.getString("Update_time"));
                if (CurrentTimeStamp != null) {
                    if (updateTime != CurrentTimeStamp) {
                        message = new StringBuilder("Table time stamp for table: ");
                        message.append(name).append(" has been updated");
                        log.write(WMSLog.TRACE, message.toString());
                        CurrentTimeStamp = updateTime;

                    }


                } else {
                    message = new StringBuilder("Created Table time stamp for table: ");
                    message.append(name);
                    log.write(message.toString());
                    CurrentTimeStamp = getDbTime(rs.getString("Update_time"));
                }
            } else {
                message = new StringBuilder("Received a null resultset for SQL call: ");
                message.append(query.toString());

                log.write(message.toString());
                // at this point, we cannot depend on the update time to determine changes in the table
                // set a flag to let the database worker in VDatabase know this to force the update
                alwaysPoll = true;
            }
            stmt.close();
            connClose(conn);
        } catch (SQLException e) {
        }
    }

    private Connection connect() {
        if (isSingleConnection()) {
            if (singleConn == null) {
                try {
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                    singleConn = DriverManager.getConnection(user.getDbUrl(), user.getName(), user.getPasswd());
                } catch (SQLException e) {
                    // Could not connect to the database
                    log.write("Database Error:" + e.toString());
                    // ParallaxMgr.getMgr().sendPopup( new ParallaxEvent( this, ParallaxMessage.POPERR, "Unable to get Connection\nSQLException:\n" + e.getMessage() ) );
                } catch (java.lang.Exception e) {
                    log.write("Instance creation error:" + e.toString());
                    // ParallaxMgr.getMgr().sendPopup( new ParallaxEvent( this, ParallaxMessage.POPERR, "Unable to get Connection\n General Exception:\n" + e.getMessage() ) );
                }
            }
            return singleConn;
        }

        // get the DBTK
        Connection connection = null;
        int reconnects = 5;
        int reconnectWaitTime = 1000;
        while (reconnects > 0) {
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                connection = DriverManager.getConnection(user.getDbUrl(), user.getName(), user.getPasswd());
            } catch (SQLException e) {
                // Could not connect to the database
                log.write("Database Error:" + e.toString());
                // ParallaxMgr.getMgr().sendPopup( new ParallaxEvent( this, ParallaxMessage.POPERR, "Unable to get Connection\nSQLException:\n" + e.getMessage() ) );
            } catch (java.lang.Exception e) {
                log.write("Instance creation error:" + e.toString());
                // ParallaxMgr.getMgr().sendPopup( new ParallaxEvent( this, ParallaxMessage.POPERR, "Unable to get Connection\n General Exception:\n" + e.getMessage() ) );
            }
            if (connection != null) {
                break;
            } else {
                reconnects--;
            }
            try {
                Thread.sleep(reconnectWaitTime);
            } catch (InterruptedException e) {
            }
        }
        if (connection == null) {
//      if( SCPMgr.getMgr() != null )
            {
                // ParallaxMgr.getMgr().sendPopup( new ParallaxEvent( this, ParallaxMessage.POPWARN, this.name + ": Unable to get connection.\nConnection was NULL for operation"));
            }
        }
        return connection;
    }

    synchronized public void markDeltas(boolean newDelta) {


        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null) {
                rec.setDelta(newDelta);

            }
            // .write( GesLog.TRACE, "Delta count is now: " + deltaCount() );
        }
    }

    public void invalidate() {
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null) {
                rec.hasRealRecord(false);
            }
        }
    }

    public void sync() {
        if (vdb.isSuspended() == true) {
            log.write("VTable::upates are suspened, returning");
            return;
        }

        HashSet<VRecord> removedRecs = new HashSet<VRecord>();
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null) {
                if (!rec.hasRealRecord()) {
                    removedRecs.add(rec);
                    // need to notify record listeners

                    log.write("Removed record: " + rec.getName() + " from table: " + name + " as it had no respective record in the database");
                    rec = null;
                }
            }
        }
        // clean up the ones to be deleted
        it = removedRecs.iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null) {
                rec.recordChanged(new VRecordEvent(rec, VDBMessage.DBMessage.RecordDeleted, rec.getName()));
                remove(rec);
            }
        }
    }

    synchronized public void purge() {


        // go through all the records in this table and remove any that do not
        // have a delta on them


        int deleteCount = 0;
        int recCount = 0;
        ArrayList<VRecord> recordsToPurge = new ArrayList<VRecord>();
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null) {
                log.write(WMSLog.TRACE, "Record with primary key of: " + primaryKeyName + "=" + rec.getField(primaryKeyName).getFieldData() + " has a delta flag of: " + rec.hasDelta());
            }
            if (rec != null && rec.hasDelta()) {
                recordsToPurge.add(rec);
                deleteCount++;
            }
            recCount++;
        }
        // clean up
        it = recordsToPurge.iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            remove(rec);
            deleteRecord(rec);
        }
        markDeltas(false);
        refresh();
        log.write("Deleted : " + deleteCount + " of (" + recCount + ") records");
    }

    public int deltaCount() {
        int deltaCount = 0;
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null && rec.hasDelta()) {
                deltaCount++;
            }
        }
        return deltaCount;
    }

    synchronized public void update() {
        log.write("Update called on table: " + name + " , be sure this was the right call");
        /**
         * update will completely rebuild the virtual table
         */
        clear();
        metaData = new VMetaRecord(this, null);
        wasBuilt = false;
        wasPopulated = false;

        build();
        populate();

        if (primaryKeyName != null) {
            setMaxKeyValue();
        }
        getStatus();
        // tableChanged( new VTableEvent( this, VDBMessage.DBMessage.TableAltered ));
    }

    /**
     * refresh will not cause the table geometry to be re-created
     *
     */
    synchronized public void vFlush() {
        /**
         * update a table via connect and select * query
         */
        clear();
        wasPopulated = false;
        populate();
        if (primaryKeyName != null) {
            setMaxKeyValue();
        }
        // notify anyone who cares that this table has changed...
        getStatus();
        // tableChanged( new VTableEvent( this, VDBMessage.DBMessage.TableAltered ));
    }

    public boolean remove(Object rec) {
        boolean success = super.remove(rec);
        rowCount = this.size();
        return success;
    }

    /*
     * synchronized public void syncObjects () { Enumeration keys = records.keys
     * (); while (keys.hasMoreElements ()) { String key = (String)
     * keys.nextElement (); ((VRecord) records.get (key)).syncObjects (); } }
     */
    synchronized public void truncate() {
        log.write("Truncating table:  " + getName());
        StringBuilder sqlStr = new StringBuilder("TRUNCATE TABLE ");
        sqlStr.append(getName());
        try {
            Connection conn = this.connect();
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.execute(sqlStr.toString());
                stmt.close();
                connClose(conn);
            }
        } catch (SQLException e) {
            log.write(": " + e);
        }
        // TODO: need to inform all the table listeners and record listeners
        // that the table has changed
        vFlush();
    }

    synchronized public void commit() {

        Iterator<VRecord> rows = this.iterator();

        Connection conn = null;
        Statement stmt = null;
        StringBuilder sqlStmt = new StringBuilder();
        while (rows.hasNext()) {
            VRecord rec = rows.next();
            if (rec.hasDelta()) {
                if (conn == null) {
                    conn = this.connect();
                }
                if (conn != null) {
                    Iterator<VField> fields =  rec.getFields().iterator();
                    while (fields.hasNext()) {
                        VField field = fields.next();
                        if (field.hasDelta()) {
                            try {
                              stmt = conn.createStatement();
                            } catch (SQLException ex) {
                                Logger.getLogger(VTable.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            sqlStmt.append("UPDATE ").append(this.getName()).append(" SET ");
                            sqlStmt.append(field.getName()).append("=").append(field.getQuote());
                            sqlStmt.append(field.getFieldData());
                            sqlStmt.append(field.getQuote());
                            sqlStmt.append(";\n");

                        }

                    }

                }
            }
        }
        try {
            int updateCount = 0;
            if( stmt != null && sqlStmt != null )

                updateCount = stmt.executeUpdate(sqlStmt.toString());
            /**
             *
             *
             * Commit changes on records that have pending deltas
             *
             *
             */
            System.out.println("Updated " + updateCount + " rows in table: " + getName() );

        } catch (SQLException ex) {
            Logger.getLogger(VTable.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         *
         *
         * Commit changes on records that have pending deltas
         *
         *
         */
    }

    public void updateFastStorage(String key, VRecord rec) {
        records.put(key, rec);
    }

    synchronized public void build() {
        /**
         *
         * Build a table via a connect and 'describe' query
         *
         */
        metaData = new VMetaRecord(this, null);
        boolean keyFound = false;
        String sql_string = "describe " + this.getName();
        DBToolkit2 dbtk = DBToolkit2.getInstance();
        Connection conn = this.connect();
        ResultSetMetaData rsMetaData = null;
        keyType = TableKeyType.COMPOUND;

        if (null == conn) {
            log.write("Unable to get connection, aborting build");
            return;
        }
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql_string);
            String record_name = "";
            if (rs != null) {
                rsMetaData = rs.getMetaData();
            }
            int colIndex = 1;
            int metaDataIndex = 0;
            // rs.beforeFirst();
            String field_name;
            String field_type;
            String field_key;
            int numColummns = rsMetaData.getColumnCount();

            while (rs.next()) {
                field_name = rs.getString(1);
                field_type = rs.getString(2);
                if (!keyFound) {
                    field_key = rs.getString(4);
                    if (field_key.equalsIgnoreCase("PRI")) {
                        primaryKeyName = field_name;
                        primaryKeyColumn = colIndex;
                        metaData.setPrimaryKey(primaryKeyName);

                        //FIXME: This should be fixed to work
                        /**
                        // look and see if the field is Auto Increment
                        String auto = rs.getString( 6 );
                        if( auto != null && auto.length() > 0  && auto.contains( "auto" ))
                        {
                        keyType = TableKeyType.AUTOINC;
                        }
                        else
                        {
                        keyType = TableKeyType.PRIMARY;
                        }
                         */
                        keyType = TableKeyType.PRIMARY;
                        keyFound = true;
                    }
                }
                VDBTypes.DBDataTypes fieldType = VDBTypes.DBDataTypes.getTypeForString(field_type);
                if (fieldType == VDBTypes.DBDataTypes.Unknown) {
                    log.write(WMSLog.CRITICAL, "Unknown database type found in table: " + name + " for native type: " + field_type);
                }
                metaData.put(colIndex++, field_name, VDBTypes.DBDataTypes.getTypeForString(field_type));
                columnCount++;
            }

            if (keyType == TableKeyType.COMPOUND) {
                // primaryKeyName = "Compond";
            }
            st.close();
            connClose(conn);
        } catch (SQLException e) {
            // Could not connect to the database
            log.write("Database Error:" + e.toString());
        }


        wasBuilt = true;
    }

    synchronized public void build(ResultSetMetaData metaData) {
        /**
         * build a VTable using the meta data from a result set
         */
        if (metaData == null) {
            log.write("ResultSetMetaData was null");
            return;
        }
        try {
            DBToolkit2 dbtk = DBToolkit2.getInstance();
            columnCount = metaData.getColumnCount();
            this.metaData = new VMetaRecord(this, null);

            for (int colIndex = 1; colIndex <= metaData.getColumnCount(); colIndex++) {
                String field_name = metaData.getColumnName(colIndex);
                String field_type = metaData.getColumnTypeName(colIndex);
                String field_key = ""; // no way of telling if it is a primary
                // key short of a table describe
                this.metaData.put(colIndex, field_name, VDBTypes.DBDataTypes.getTypeForString(field_type));
            }
            wasBuilt = true;
        } catch (SQLException e) {
            log.write("build: " + e.getMessage());
        }
    }

    synchronized public void populate() {
        /**
         *
         * populate a table via connect and select * query
         */
        ResultSet rs = null;
        Statement st = null;
        if (!wasBuilt) {
            log.write("Table must be built first...");
            return;
        }
        if (wasPopulated) {
            log.write("Table: (" + this.name + ") was already populated, run update");
            return;
        }
        StringBuffer sqlBuffer = new StringBuffer("SELECT * FROM ");
        sqlBuffer.append(getName());
        if (getOrderByFieldName().length() > 0) {
            sqlBuffer.append(" ORDER BY ").append(getOrderByFieldName());
        }
        Connection conn = this.connect();
        if (null == conn) {
            log.write("Unable to get connection, aborting populate");
            return;
        }
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sqlBuffer.toString());
            VRecord rec = new VRecord(this, primaryKeyName);
            String record_name = "";
            int arrayIndex = 0;
            int columnIndex = 1;
            String field_data;
            boolean has_more_records = true;
            // prime the results
            if (rs.next());

            long inTime = System.currentTimeMillis();
            int intCount = 0;
            while (has_more_records) {
                DBDataTypes dbType = metaData.getType(columnIndex);
                if (dbType == VDBTypes.DBDataTypes.Date) {
                    // convert the timestamp to a string, this is redundant now, but breaking this out might be useful later, at least we now know the corrent field type
                    java.sql.Timestamp ts = rs.getTimestamp(columnIndex);
                    field_data = ts.toString();
                } else {
                    try {
                        field_data = rs.getString(columnIndex);

                    } catch (SQLException ex) {
                        log.write("Got bad field on record read. Vendor error was:  " + ex.getSQLState());
                        if (!rs.next()) {
                            has_more_records = false;
                        }
                        continue;
                    }
                }
                if (field_data != null) {

                    rec.put(columnIndex, field_data, dbType);
                    intCount++;

                }
                //TODO: make sure this is really what we want to do here...
                else if( rs.wasNull())
                {
                    rec.put(columnIndex, "", dbType );
                }
                // reset the column count if we have gone through all the fields
                // on this row

                if (columnIndex == metaData.getNumber()) {
                    // row is completed, insert it
                    add(rec);
                    rec.setRowKey(record_name);
                    rec.setKeyColumn(this.primaryKeyColumn);
                    // populate the primary key field for this record

                    arrayIndex = 0;
                    columnIndex = 1;
                    log.write(WMSLog.TRACE, "Inserted record(" + record_name + ") into Table: " + this.name + " Column count was: " + rec.getNumber());

                    rec = new VRecord(this, primaryKeyName);

                    if (!rs.next()) {
                        has_more_records = false;
                    }
                } else {
                    columnIndex++;
                    arrayIndex++;
                }

                wasPopulated = true;
            }
            long outTime = System.currentTimeMillis();
            long eTime = outTime - inTime;
            long outSecs = eTime / 1000;
            long outRem = eTime % 1000;
            float perField = (float) ((float) eTime / (float) intCount);
            // System.out.println( "Table-" + name  + ": elapsed time building fields: " +  String.valueOf(outSecs) + ":" + String.valueOf(outRem ) +  "(sec). "+ intCount + " fields. " + String.valueOf(perField) + "msec per field" ) ;

            st.close();
            connClose(conn);
        } catch (SQLException e) {
            // Could not connect to the database
            if (e.toString().indexOf("Illegal operation on emtpy") != -1) {
                log.write("Database Error:" + e.toString() + " for table: " + this.getName());
            }
        }
        getStatus();
        // wtf();
    }

    synchronized public void populate(ResultSet rs) {
        /**
         * populate a table via connect and select * query
         *
         */
        if (rs == null) {
            log.write("Got null result set. Cannot populate from it");
        }

        if (!wasBuilt) {
            log.write("Table must be built first...");
            return;
        }
        if (wasPopulated) {
            log.write("Table: (" + this.name + ") was already populated, run update");
            return;
        }
        try {
            VRecord rec = new VRecord(this, primaryKeyName);
            String record_name = "";
            int arrayIndex = 0;
            int columnIndex = 1;
            String field_data;
            boolean has_more_records = true;
            // prime the results
            if (rs.next());
            while (has_more_records) {

                DBDataTypes dbType = metaData.getType(columnIndex);
                field_data = rs.getString(columnIndex);
                if (dbType == VDBTypes.DBDataTypes.BigInt) {
                    // convert the long value to a string.
                    java.util.Date d = rs.getDate(columnIndex);

                }

                if (field_data != null) {

                    rec.put(columnIndex, field_data, dbType);

                }
                // reset the column count if we have gone through all the fields
                // on this row

                if (columnIndex == metaData.getNumber()) {
                    // row is completed, insert it
                    add(rec);
                    // populate the primary key field for this record

                    arrayIndex = 0;
                    columnIndex = 1;
                    log.write(WMSLog.TRACE, "Inserted record(" + record_name + ") into Table: " + this.name + " Column count was: " + rec.getNumber());

                    rec = new VRecord(this, primaryKeyName);

                    if (!rs.next()) {
                        has_more_records = false;
                    }
                } else {
                    columnIndex++;
                    arrayIndex++;
                }
            }
            wasPopulated = true;
        } catch (SQLException e) {
            // Could not connect to the database
            if (e.toString().indexOf("Illegal operation on emtpy") != -1) {
                log.write("Database Error:" + e.toString() + " for table: " + this.getName());
            }
        }
    }

    private void translateSelectPattern(String newPattern) {
        StringBuilder newSqlStmt = new StringBuilder();
        if (newPattern.contains("%")) {
            StringTokenizer stk = new StringTokenizer(newPattern, " ");
            while (stk.hasMoreTokens()) {
                String word = stk.nextToken();
                if (word.contains("%")) {
                    StringTokenizer stk2 = new StringTokenizer(word, "%");
                    if (stk2.hasMoreTokens()) {
                        String patternSymbol = stk2.nextToken();
                        if (patternSymbol.equalsIgnoreCase("t")) {
                            newSqlStmt.append(getName()).append(" "); // add the table name to the stmt string
                        }
                    }
                } else {
                    newSqlStmt.append(word).append(" ");
                }
            }
        }
        sqlSelectString = newSqlStmt.toString();
    }

    synchronized public void refresh() {
        if (this.primaryKeyName == null) {
            // abort
            log.write("Primary key for this table is null. Aborting update");
            return;
        }
        markDeltas(false);
        String recordKeyValue = null;
        ResultSet rs = null;
        Statement st = null;

        StringBuffer sqlBuffer = new StringBuffer("SELECT * FROM ");
        sqlBuffer.append(getName());
        if (getOrderByFieldName().length() > 0) {
            sqlBuffer.append(" ORDER BY ").append(getOrderByFieldName());
        }


        ///log.write( GesLog.TRACE, "Getting connection...");
        Connection conn = this.connect();
        ///log.write( GesLog.TRACE, "Done getting connection...");
        if (null == conn) {
            log.write("Unable to get connection, aborting populate");
            return;
        }

        try {
            // log.write(GesLog.TRACE, "Creating Statement...");
            st = conn.createStatement();
            ///log.write(GesLog.TRACE, "Done Creating Statement");

            ///log.write( GesLog.TRACE, "Executing SELECT query..");
            rs = st.executeQuery(sqlBuffer.toString());
            ;
            ///log.write( GesLog.TRACE, "Done executing SELECT query..");
            // prime the results
            String recordFieldData = null;
            String recordFieldName = null;
            int rowNumber = 1;
            boolean hasMoreRecords = true;
            if (rs == null) {
                log.write("Got null resultset, bailing...");
                return;
            }
            if (rs != null) {
                rs.beforeFirst();
            }
            invalidate();

            /// log.write( GesLog.TRACE, "Building new table from Resultset...");
            long inTime = System.currentTimeMillis();
            int intCount = 0;
            while (rs.next()) {
                if (this.keyType == TableKeyType.PRIMARY) {
                    intCount++;
                    // log.iso( "row number: " + rowNumber );
                    String keyData = rs.getString(primaryKeyName);
                    // get the record based on the keyvalue
                    // VRecord rec = getRecord( primaryKeyName, keyData) ;
                    VRecord rec = null;
                    if (enableFastLookup) {
                        if (records.containsKey(keyData)) {
                            rec = records.get(keyData);
                        } else if (primaryKeyColumn != -1) {
                            rec = getRecord(keyData);
                        } else {
                            rec = getRecord(primaryKeyName, keyData);
                        }
                    } else {
                        rec = getRecord(primaryKeyName, keyData);
                    }

                    int columnCount = 1;
                    if (rec != null) {
                        // rec.setDelta(true); // mark as current to later check for
                        // missing records
                        // go through each field in the real data base record and
                        // adjust the value of the virtual field if changed
                        int numFields = metaData.getNumber();
                        for (int i = 0; i < numFields; i++) {
                            intCount++;
                            recordFieldData = rs.getString(i + 1);

                            if (rec.updateField(i + 1, recordFieldData)) {
                                rec.recordChanged(new VRecordEvent(rec, VDBMessage.DBMessage.RecordChanged, ""));
                            }
                        }
                        rec.hasRealRecord(true);
                    } else // record does not exist, add it
                    {
                        VRecord newRecord = getEmptyRecord();
                        for (int i = 0; i < metaData.getNumber(); i++) {
                            String columnName = metaData.getFieldName(i + 1);
                            if (columnName != null) {
                                recordFieldData = rs.getString(columnName);
                                newRecord.setData(i + 1, recordFieldData);
                                newRecord.setDelta(true);
                                newRecord.hasRealRecord(true);
                                // try and set the primary key, if any...
                                if (columnName.equals(newRecord.getPrimaryKeyName())) {
                                    newRecord.setKeyValue(i + 1, recordFieldData);
                                }
                            }
                        }
                        add(newRecord);
                        rowNumber++;
                    }
                }
                //  if( ! rs.next() )
                //  hasMoreRecords = false;
            }

            long outTime = System.currentTimeMillis();
            long eTime = outTime - inTime;
            long outSecs = eTime / 1000;
            long outRem = eTime % 1000;
            float perField = (float) ((float) eTime / (float) intCount);
            log.write(WMSLog.TRACE, "Table-" + name + ": elapsed time updating fields: " + String.valueOf(outSecs) + ":" + String.valueOf(outRem) + "(sec). " + intCount + " fields. " + String.valueOf(perField) + "msec per field");

            // remove any VRecords that did not have a real record found in the database
            if (synced && !vdb.isSuspended()) {
                sync();
            }


            // log.write( GesLog.TRACE, "Done Building new table from Resultset...");
            // check to see if any records where deleted
            // purge();
            // sync();
            st.close();
            connClose(conn);
        } catch (SQLException e) {
            // Could not connect to the database
            if (e.toString().indexOf("Illegal operation on emtpy") != -1) {
                log.write("Database Error:" + e.toString() + " for table: " + this.getName());
            } else {
                log.write("SQL Error: " + e.getMessage());
            }
        }

        getStatus();
        // wtf();
        // tableChanged( new VTableEvent( this, VDBMessage.DBMessage.TableChanged ));
    }

    private void sortByOrderField() {
    }

    public boolean add(VRecord rec) {
        // log.write( "RowNumber is: " + rowNumber );
        boolean success = super.add(rec);
        rowCount = this.size();
        return success;
    }

    public Vector<String> getColumnByColumnNumber(int index) {
        Vector<String> column = new Vector<String>();
        int row = 1;
        Iterator<VRecord> it = this.iterator();
        while (it.hasNext()) {
            // test
            VRecord tRec = it.next();
            if (tRec != null) {
                String data = tRec.getData(index);

                if (data != null) {
                    column.add(data);
                }
            }
        }
        return column;
    }

    /**
     *
     *
     *
     *
     *
     * The following methods support the TableModel interface
     *
     *
     */
    synchronized public String getName() {
        return name;
    }

    public synchronized VRecord getRecord(int rowNumber) {
        int currentRow = 1;
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null && currentRow == rowNumber) {
                return rec;
            }
        }
        return null;
    }

    public synchronized void setValueAt(String aValue, int rowNumber, int colNumber) {
        log.write(WMSLog.TRACE, "called for row: " + rowNumber + " col: " + colNumber);

        VRecord rec = getRecord(rowNumber);

        if (!rec.getData(colNumber).equals((String) aValue)) {
            log.write("Value has chanaged. old value: " + rec.getData(colNumber));
            rec.setData(colNumber, aValue);
            rec.setDelta(true);
            log.write("Table:" + getName() + ": Field value is now: " + rec.getData(colNumber) + " in Table: " + rec.getVTable().getName());
        }
    }

    public synchronized String getValueAt(int rowNumber, int colNumber) {
        // log.write( "called for row: " + rowNumber + " col: " + colNumber );
        if (this.getColumnCount() == 0 && this.getRowCount() == 0) {
            // log.write( ": " + rowNumber + " Col: " + colNumber + ": table is
            // empty" );
            return null;
        }
        if (rowNumber > getRowCount() || colNumber > getColumnCount()) {
            log.write(": Row: " + rowNumber + " Col: " + colNumber + ": row is greater than rows: " + getRowCount());
            return null;
        }
        VRecord rec = getRecord(rowNumber);
        if (rec == null) {
            log.write("Value at: Row: " + rowNumber + " Col: " + colNumber + ": null record");
            return null;
        }
        if (rec.getData(colNumber) == null) {
            log.write(": Row: " + rowNumber + " Col: " + colNumber + ": null in Table: " + getName());
            return null;
        }
        return rec.getData(colNumber);
    }

    public VRecord getRecord(String keyValue) {
        // method to retrieve a record by key value
        VRecord tRec = null;
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            tRec = it.next();
            try {
                if (tRec != null && tRec.getPrimaryKeyValue().equals(keyValue)) {
                    return tRec;
                }
            } catch (NullPointerException e) {
                log.write("Got Null pointer: " + e.getMessage());
            }
        }
        return null;
    }

    public synchronized VRecord getRecord(String columnName, String columnValue) {
        VRecord tRec = null;
        if (isIndexed && index.isIndexed(columnName)) {
            tRec = index.getRecord(columnName, columnValue);
        } else {
            Iterator<VRecord> it = iterator();
            while (it.hasNext()) {
                tRec = it.next();
                if (tRec == null || tRec.getFieldData(columnName) == null) {
                    continue;
                }
                if (tRec.getFieldData(columnName).equals(columnValue)) {
                    break;
                } else {
                    tRec = null;
                }
            }
        }
        return tRec;
    }

    /*
    synchronized public VRecord getRecord(String columnName, String columnValue)
    {
    VRecord tRec = null;
    // first figure out the column number
    int targetColumnIndex = metaData.getColumnNumberByName(columnName);

    Vector column = getColumnByColumnNumber(targetColumnIndex);
    for (int i = 0; i < column.size(); i++)
    {
    String value = (String) column.elementAt(i);
    if (value.equals(columnValue))
    {
    // found the value, now return the row we are on
    tRec = this.getRecord(i);
    break;
    }
    }
    return tRec;
    }
     */
    /*
     * public Hashtable <String, VRecord> findRecords ( String columnName,
     * String columnValue ) { Hashtable <String, VRecord> records = new
     * Hashtable < String, VRecord>(); int targetColumnIndex =
     * metaData.getColumnNumberByName (columnName); Vector column =
     * getColumnByColumnNumber (targetColumnIndex); for (int i = 0; i <
     * column.size (); i++) { String value = (String) column.elementAt(i); if
     * (value.equals (columnValue)) { String key = (String) getRecord
     * (i).getField ("idelements").getFieldData (); // found the value, now
     * return the row we are on records.put ( key, this.getRecord (i) ); } }
     * return records; }
     */
    public synchronized Vector<VRecord> getRecords(String columnName, String columnValue) {
        if (isIndexed && index.isIndexed(columnName)) {
            RecordGroup rg = index.getRecords(columnName, columnValue);
            Vector<VRecord> rv = null;
            if (rg != null) {
                rv = rg.getVector();
            }
            return rv;
        }
        Vector<VRecord> recordVect = new Vector<VRecord>();
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null) {
                String fieldValue = rec.getFieldData(columnName);
                if (fieldValue.equals(columnValue)) {
                    if (!recordVect.contains(rec)) {
                        recordVect.add(rec);
                    }
                }
            }
        }
        return recordVect;
    }

    public synchronized Vector<VRecord> getInverseRecords(String columnName, String columnValue) {
        Vector<VRecord> recordVect = new Vector<VRecord>();
        // first figure out the column number
        int targetColumnIndex = metaData.getColumnNumberByName(columnName);
        Vector column = getColumnByColumnNumber(targetColumnIndex);
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null && !rec.getFieldData(columnName).equals(columnValue)) {
                recordVect.add(rec);
            }
        }
        return recordVect;
    }

    public synchronized Vector<VRecord> getInverseRecordsWhere(String columnName, String columnValue, String targetColumnName, String targetColumnValue) {
        Vector<VRecord> recordVect = new Vector<VRecord>();
        // first figure out the column number
        int targetColumnIndex = metaData.getColumnNumberByName(columnName);
        Vector column = getColumnByColumnNumber(targetColumnIndex);
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null && !rec.getFieldData(columnName).equals(columnValue)) {
                if (rec.getFieldData(targetColumnName).equals(targetColumnValue)) {
                    recordVect.add(rec);
                }
            }
        }
        return recordVect;
    }

    synchronized public Vector<String> getValues(String columnName) {


        // first figure out the column number


        int targetColumnIndex = metaData.getColumnNumberByName(columnName);


        Vector<String> column = getColumnByColumnNumber(targetColumnIndex);


        return column;


    }
    /*
     * public Vector getColumnByColumnNumber (int index) { Vector <VField>column =
     * new Vector <VField>(); for (int row = 0; row < getRowCount (); row++) {
     * VRecord tRec = (VRecord) records.get (String.valueOf (row)); VField tField =
     * tRec.getField (index); if (tField != null) { column.add (tField); } } return
     * column; }
     */
    /*
     * public String getForeignKeyRecord (int row, int col) {
     *
     *
     * VField field = getRecord (row).getField (col);
     *
     *
     * VRecord rec = field.getForeignRecord ();
     *
     *
     * if (rec != null) {
     *
     *
     * if (field.isForeignKey ())
     *
     *
     * return rec.toString ();
     *
     *  }
     *
     *
     * return ("");
     *
     *  }
     *
     */

    public String getForeignKeyTableName(int col) {
        return this.metaData.getForeignTableNameForColumn(col);
    }

    public int getColumnNumberForName(String name) {
        return metaData.getColumnNumberByName(name);
    }

    public String getColumnName(int aColumn) {
        // log.write( "called for column: " + aColumn + " value is: " +
        // tableSignature.getSignatureField(aColumn).getName() );
        return metaData.getFieldName(aColumn);
    }

    public Iterator<String> getColumnNames() {
        return metaData.getColumnNames();
    }

    public Class getColumnClass(int aColumn) {
        return String.class;
    }

    public boolean isCellEditable(int row, int column) {
        VRecord rec = getRecord(row);
        if (rec == null) {
            return false;
        }
        // log.write( "row: " + row + " columns: " + column + " is editable: " +
        // rec.getField(column).isEditable( ));
        return rec.getEditable(column);
    }

    public void setColumnEditable(int columnIndex, boolean editable) {
        Vector<VRecord> rows = getRows();
        for (int i = 0; i < rows.size(); i++) {
            VRecord rec = (VRecord) rows.elementAt(i);
            if (rec != null) {
                rec.setEditable(columnIndex, editable);
            }
        }
    }

    synchronized public RecordGroup getRowsAsGroup(String fieldName, String fieldValue) {
        RecordGroup rg = null;
        if (this.isIndexed && index.isIndexed(fieldName)) {
            rg = index.getRecords(fieldName, fieldValue);
        }
        return rg;
    }

    synchronized public HashMap<String, VRecord> getRowsAsMap(String fieldToIndexBy) {
        Iterator<VRecord> it = this.iterator();
        HashMap<String, VRecord> map = new HashMap<String, VRecord>();
        while (it.hasNext()) {
            VRecord rec = it.next();
            map.put(rec.getFieldData(fieldToIndexBy), rec);

        }
        return map;
    }

    synchronized public Vector<VRecord> getRows() {
        return this;
        /*
        Vector <VRecord>rowData = new Vector<VRecord>();
        Iterator<VRecord>  it = iterator();
        while( it.hasNext() ) {
        try {
        rowData.add( it.next()  );
        } catch (Exception e) {
        log.write("Got exception: " + e.getMessage());
        }
        }
        return rowData;
         */
    }

    synchronized public Vector getRowData() {


        Vector<Vector> rowData = new Vector<Vector>();





        for (int i = 1; i <= this.getRowCount(); i++) {


            try {


                VRecord tempRec = this.getRecord(i);


                rowData.add(tempRec.getRowData());


            } catch (Exception e) {


                log.write("Got exception: " + e.getMessage());


            }


        }


        return rowData;


    }

    public int getRowCount() {
        return size();
    }

    public int getColumnCount() {
        return metaData.getNumber();
    }

    public VTableView getTableModel() {
        return new VTableView(this);
    }

    synchronized public void setPrimaryKey(String key_field) {
        primaryKeyName = key_field;

    }

    synchronized public String getPrimaryKey() {
        return primaryKeyName;
    }

    synchronized public boolean isView() {


        return isView;
    }

    // Database operations
    synchronized public boolean insertInto(VRecord rec, String pattern, Object data) {


        return false;


    }

    public synchronized String getMaxValue(String fieldName) {
        String value = null;
        if (fieldName == null) {
            log.write("FieldName given was null");
            return value;
        }
        StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("Select MAX(").append(fieldName).append(") FROM ");
        sqlBuf.append(getName());
        try {
            Connection conn = connect();
            Statement stmt = conn.createStatement();
            // log.write( "Executing: " + sqlBuf.toString() );
            ResultSet rs = stmt.executeQuery(sqlBuf.toString());
            if (rs != null) {
                rs.next();
                if (rs.getString(1) != null) {
                    value = rs.getString(1);
                }
            }
            stmt.close();
            connClose(conn);
        } catch (SQLException e) {
            log.write(e.getMessage());
        }
        return value;
    }

    synchronized public void setMaxKeyValue() {
        if (primaryKeyName == null) {
            log.write("Unable to get max key value from table this method. Table has no primary key");
            return;
        }
        Connection conn = this.connect();
        StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("Select MAX(").append(primaryKeyName).append(") FROM ");
        sqlBuf.append(getName());
        try {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                // log.write( "Executing: " + sqlBuf.toString() );
                ResultSet rs = stmt.executeQuery(sqlBuf.toString());
                if (rs != null) {
                    rs.next();
                    if (rs.getString(1) != null) {
                        maxKeyValue = rs.getString(1);
                    }
                }
                stmt.close();
                connClose(conn);

            }
        } catch (SQLException e) {
            log.write(e.getMessage());
        }
    }

    synchronized public String getMaxKeyValue() {
        if( maxKeyValue != null && maxKeyValue.isEmpty())
            setMaxKeyValue();
        return maxKeyValue;
    }

    private synchronized void lockTable() {
        // log.write( "Locking table" );
        try {
            Connection conn = this.connect();
            if (!isLocked) {
                Statement stmt = conn.createStatement();
                stmt.execute("LOCK TABLES " + getName() + " WRITE");
                isLocked = true;
                stmt.close();
            }
            if (conn != null) {
                connClose(conn);
            }
        } catch (SQLException e) {
            log.write("Unable to lock table: " + getName() + ": " + e.getMessage());
        }
    }

    private synchronized void unlockTable(String caller) {


        // log.write( "unlockTable: Unlocking table" );


        try {
            Connection conn = this.connect();
            if (this.isLocked) {
                Statement stmt = conn.createStatement();
                stmt.execute("UNLOCK TABLES");
                isLocked = false;
                stmt.close();
            }
            if (conn != null) {
                connClose(conn);
            }
        } catch (SQLException e) {
            log.write("Unable to unlock tables from: " + getName() + ": " + e.getMessage());
        }
        log.write("Caller: " + caller + " Unlocked table: " + getName());
    }

    public synchronized int deleteRecords(String columnName, String columnValue) {
        Vector<VRecord> matchingRecords = getRecords(columnName, columnValue);
        int deleteCount = 0;
        for (int i = 0; i < matchingRecords.size(); i++) {
            VRecord rec = matchingRecords.elementAt(i);
            if (rec != null) {
                String matchingField = rec.getFieldData(columnName);
                if (matchingField != null && matchingField.equals(columnValue)) {
                    deleteRecord(rec);
                    deleteCount++;
                }
            }
        }
        return deleteCount;
    }

    public synchronized int deleteRecordsWhere(String targetFieldName, String targetFieldValue) {

        int numDeleted = 0;
        Connection conn = this.connect();
        StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("DELETE FROM ");
        sqlBuf.append(getName());
        sqlBuf.append(" WHERE ");
        sqlBuf.append(targetFieldName);
        sqlBuf.append(" = ");
        sqlBuf.append("'");
        sqlBuf.append(targetFieldValue);
        sqlBuf.append("'");
        try {
            Statement stmt = conn.createStatement();
            log.write(WMSLog.DEBUG, "Executing: " + sqlBuf.toString());
            stmt.execute(sqlBuf.toString());
            stmt.close();
            connClose(conn);
        } catch (SQLException e) {
            log.write(e.getMessage());
            return numDeleted;
        }

        // now that the real records are deleted, we need to find and delete the matching virtual ones
        Iterator<VRecord> it = iterator();
        HashSet<VRecord> removedRecords = new HashSet<VRecord>();
        while (it.hasNext()) {
            // test
            VRecord tRec = it.next();
            if (tRec.getFieldData(targetFieldName).equals(targetFieldValue)) {
                removedRecords.add(tRec);
                numDeleted++;
            }
        }
        it = removedRecords.iterator();
        while (it.hasNext()) {
            VRecord tRec = it.next();
            remove(tRec);
        }
        removedRecords.clear();
        return numDeleted;
    }

    public synchronized int deleteRecord(VRecord recToDelete) {
        Connection conn = this.connect();
        if (primaryKeyName == null) {
            log.write("Unable to delete from table: " + name + " without primary key with this method");
            return -1;
        }
        if (conn == null) {
            log.write("Unable to open a connection");
            return -1;
        }
        // get the primrary key value
        String keyValue = "NONE";
        VField key = recToDelete.getField(primaryKeyName);
        if (key != null) {
            keyValue = key.getFieldData();
        }

        StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("DELETE FROM ");
        sqlBuf.append(getName());
        sqlBuf.append(" WHERE ");
        sqlBuf.append(primaryKeyName);
        sqlBuf.append(" = ");
        sqlBuf.append("'");
        sqlBuf.append(keyValue);
        sqlBuf.append("'");
        try {
            Statement stmt = conn.createStatement();
            log.write(WMSLog.DEBUG, "Executing: " + sqlBuf.toString());
            stmt.execute(sqlBuf.toString());
            stmt.close();
            connClose(conn);
        } catch (SQLException e) {
            log.write(e.getMessage());
        }

        // give the database time to catchup
        try {
            Thread.sleep(50);
        } catch (java.lang.InterruptedException e) {
            log.write(e.getMessage());
        }

        remove(recToDelete);
        return 1;
    }

    public synchronized int insertRecord(VRecord rec, String pattern) {
        // INSERT INTO Event (idservers, Event, State, Parm1, Parm2, Parm3,
        // Parm4) VALUES (%d,'%s',%d,'%s','%s','%s','%s')
        // first break off the list of column names

        // set the primary key
        if (primaryKeyName != null) {
            try {

                int newKey = -1;
                if (primaryKeyName != null) {
                    rec.setPrimaryKeyName(primaryKeyName);
                }
                if (getMaxKeyValue() != null && getMaxKeyValue().length() > 0) {
                    newKey = Integer.parseInt(getMaxKeyValue()) + 1;
                } else {
                    setMaxKeyValue();
                    newKey = Integer.parseInt(getMaxKeyValue()) + 1;
                }
                int primaryColumn = getColumnNumberForName(primaryKeyName);
                // if the key is truely Primary and Incremental, then we want the key value to increment.
                if (keyType != TableKeyType.COMPOUND && rec.hasPrimaryField() && newKey != -1) {
                    rec.setKeyValue(primaryColumn, String.valueOf(newKey));
                }
            } catch (NumberFormatException e) {
                log.write("NumberFormatException: " + e.getMessage());
            }
        }
        int rowNumberInserted = -1;
        Vector columnNames = this.getColumnListFromPattern(pattern);
        Vector columnTypes = this.getColumnTypesFromPattern(pattern);
        try {
            Connection conn = this.connect();
            // PreparedStatement pstmt = conn.prepareStatement( pattern );
            pattern = pattern.replaceAll("%d", "?");
            pattern = pattern.replaceAll("%s", "?");
            pattern = pattern.replaceAll("%u", "?");
            pattern = pattern.replaceAll("'", "");

            PreparedStatement pstmt = conn.prepareStatement(pattern);
            for (int i = 0; i < columnNames.size(); i++) {
                String cName = (String) columnNames.elementAt(i);
                int targetIndex = getColumnNumberForName(cName);
                // VField field = rec.getField( targetIndex );
                // field.setFieldType( metaData.getType( targetIndex ) );
                // rec.setType( targetIndex, metaData.getType( targetIndex ));
                String data = rec.getData(targetIndex);
                VDBTypes.DBDataTypes type = rec.getType(targetIndex);
                if (VDBTypes.DBDataTypes.isNumber(type)) {
                    int value = 0;
                    if (data.length() > 0) {
                        value = Integer.parseInt(data);
                    }
                    pstmt.setInt((i + 1), value);
                } else if (VDBTypes.DBDataTypes.isString(type)) {
                    if (data == null || data.length() == 0) {
                        pstmt.setString((i + 1), "");
                    } else {
                        pstmt.setString((i + 1), data);
                    }
                }
            }
            lockTable();
            int rows = pstmt.executeUpdate();
            setMaxKeyValue();

            log.write("Executed: " + pstmt.toString());
            if (rows == 1) {
                // log.write( "VTable: before row count is now: " + recordCount
                // );
                // this is not the right thing to do here, as this will not add the record object that was passed in
                // refresh();
                // THIS is what should happen
                add(rec);
                // update();
                rowNumberInserted = getRowCount();
                // log.write( "VTable: after row count is now: " + recordCount
                // );
                unlockTable("InsertRecord");
            } else {
                log.write("Row insert Failed");
                unlockTable("InsertRecord");
                return rowNumberInserted;
            }
            pstmt.close();
            connClose(conn);
        } catch (SQLException e) {
            log.write("SQLException: " + e.getMessage());
            unlockTable("InsertRecord");
            return rowNumberInserted;
        } catch (NumberFormatException e) {
            log.write("NumberFormatException: " + e.getMessage());
            unlockTable("InsertRecord");
            return rowNumberInserted;
        } catch (NullPointerException e) {
            e.printStackTrace();
            log.write("NullPointerException: " + e.getMessage());
            unlockTable("InsertRecord");
            return rowNumberInserted;
        }
        // this.update();


        getStatus();
        unlockTable("InsertRecord");
        return rowNumberInserted;
    }

    public int insertRecords(Vector<VRecord> recs, String pattern, boolean flag) {
        // INSERT INTO Event (idservers, Event, State, Parm1, Parm2, Parm3,
        // Parm4) VALUES (%d,'%s',%d,'%s','%s','%s','%s')
        // first break off the list of column names


        int rowNumberInserted = -1;
        Vector columnNames = this.getColumnListFromPattern(pattern);
        Vector columnTypes = this.getColumnTypesFromPattern(pattern);
        try {
            Connection conn = this.connect();
            // PreparedStatement pstmt = conn.prepareStatement( pattern );
            pattern = pattern.replaceAll("%d", "?");
            pattern = pattern.replaceAll("%s", "?");
            pattern = pattern.replaceAll("%u", "?");
            pattern = pattern.replaceAll("'", "");
            int rows = -1;
            // PreparedStatement pstmt = conn.prepareStatement( pattern );
            Statement stmt = conn.createStatement();
            // get each record
            for (int recNo = 0; recNo < recs.size(); recNo++) {
                VRecord rec = (VRecord) recs.elementAt(recNo);
                rec.setPrimaryKeyName(primaryKeyName);
                StringTokenizer stk = new StringTokenizer(pattern, "?");
                StringBuilder sqlStr = new StringBuilder();
                sqlStr.append(stk.nextToken());
                if (rec != null) {
                    for (int i = 0; i < columnNames.size(); i++) {
                        String cName = (String) columnNames.elementAt(i);
                        int targetColumn = this.getColumnNumberForName(cName);
                        String data = rec.getData(targetColumn);
                        VDBTypes.DBDataTypes type = rec.getType(targetColumn);
                        if (VDBTypes.DBDataTypes.isNumber(type)) {
                            int value = 0;
                            if (data.length() > 0) {
                                value = Integer.parseInt(data);
                            }

                            // pstmt.setInt( (i + 1) , value );
                            sqlStr.append(String.valueOf(value));
                        } else if (VDBTypes.DBDataTypes.isString(type)) {
                            if (data == null || data.length() == 0) {
                                // pstmt.setString( (i + 1) , "" );
                                sqlStr.append("\'\'");
                            } else {
                                // pstmt.setString( (i + 1) , value );
                                sqlStr.append(data);
                            }
                        }
                        sqlStr.append(stk.nextToken());
                    }

                    // rows = pstmt.executeUpdate();
                    // pstmt.clearParameters();
                    log.write(WMSLog.TRACE, "executing insert: " + sqlStr.toString());
                    lockTable();
                    rows = stmt.executeUpdate(sqlStr.toString());
                    add(rec);
                    setMaxKeyValue();
                    rec.setKeyValue(1, maxKeyValue);
                    log.write(WMSLog.TRACE, "executed insert: " + sqlStr.toString());
                }
            }


            if (rows == 1) {
                // log.write( "VTable: before row count is now: " + recordCount
                // );
                // refresh();
                unlockTable("InsertRecords");
                rowNumberInserted = getRowCount();
                // log.write( "VTable: after row count is now: " + recordCount
                // );
            } else {
                unlockTable("InsertRecords");
                log.write("Row insert Failed. rows was: " + rows);
                return rowNumberInserted;
            }
            stmt.close();
            connClose(conn);
        } catch (SQLException e) {
            log.write("SQLException" + e.getMessage());
            unlockTable("InsertRecords");
            return rowNumberInserted;
        } catch (NumberFormatException e) {
            log.write("NumberFormatException" + e.getMessage());
            unlockTable("InsertRecords");
            return rowNumberInserted;
        } catch (NullPointerException e) {
            e.printStackTrace();
            log.write("NullPointerException" + e.getMessage());
            unlockTable("InsertRecords");
            return rowNumberInserted;
        }
        // this.update();
        return rowNumberInserted;
    }

    synchronized public VRecord getEmptyRecord() {
        VRecord emptyRec = new VRecord(this);
        emptyRec.setPrimaryKeyName(primaryKeyName);
        return emptyRec;
    }

    synchronized public Vector<VField> getRecordTemplate() {
        Vector<VField> template = new Vector<VField>();
        for (int i = 1; i <= metaData.getNumber(); i++) {
            VField templateField = new VField(metaData.getFieldName(i), "", metaData.getType(i), false);

            if (VDBTypes.DBDataTypes.isNumber(metaData.getType(i))) {
                templateField.setFieldData(new String("0"));
            } else {
                templateField.setFieldData(new String(""));
            }
            template.add(templateField);
        }

        return template;
    }

    public Vector getColumnListFromPattern(String pattern) {
        //INSERT INTO Event (idservers, Event, State, Parm1, Parm2, Parm3,
        // Parm4) VALUES (%d,'%s',%d,'%s','%s','%s','%s')
        Vector<String> columns = new Vector<String>();
        int firstParaIndex = pattern.indexOf('(') + 1;
        int secondParaIndex = pattern.indexOf(')');
        String sub = pattern.substring(firstParaIndex, secondParaIndex);
        StringTokenizer stk = new StringTokenizer(sub, ",");
        while (stk.hasMoreTokens()) {
            columns.add(stk.nextToken().trim());
        }
        return columns;
    }

    public Vector getColumnTypesFromPattern(String pattern) {


        //INSERT INTO Event (idservers, Event, State, Parm1, Parm2, Parm3,
        // Parm4) VALUES (%d,'%s',%d,'%s','%s','%s','%s')


        Vector<String> columns = new Vector<String>();





        int firstLeftParaIndex = pattern.indexOf('(');


        int firstRightParaIndex = pattern.indexOf(')');


        int secondLeftParaIndex = pattern.indexOf('(', firstLeftParaIndex + 1);


        int secondRightParaIndex = pattern.lastIndexOf(')');





        String sub = pattern.substring(secondLeftParaIndex + 1, secondRightParaIndex);


        StringTokenizer stk = new StringTokenizer(sub, ",");


        while (stk.hasMoreTokens()) {


            columns.add(stk.nextToken());


        }





        return columns;


    }

    /*
    public void batchUpdateRecords( )
    {

    Enumeration records = keys();
    while( records.hasMoreElements() )
    {
    VRecord tableRecord = (VRecord) get( records.nextElement());
    if( tableRecord != null )
    {
    if( ! tableRecord.hasDelta() )
    continue;
    Vector <VField>fieldsPendingUpdate = new Vector<VField>();
    Vector <VField>fields = tableRecord.getFields();
    // collect all the fields to update
    for( int i = 0; i < fields.size(); i++ )
    {
    VField field = (VField) fields.elementAt( i );
    if(  field.hasDelta()  )
    {
    fieldsPendingUpdate.add(  field );
    }
    }
    // build an update statement beased on the fields needing change
    // UPDATE tableName SET A=value, B=value WHERE key=value
    StringBuffer sqlBuf = new StringBuffer();
    sqlBuf.append( "UPDATE ").append( getName() ).append( " SET " );
    for( int index =0; index < fieldsPendingUpdate.size(); index++ )
    {
    VField field = (VField) fieldsPendingUpdate.elementAt( index );
    if( field == null )
    {
    continue;
    }
    sqlBuf.append(  field.getName() ).append( "='").append( (String)field.getFieldData()).append( "'");
    int num = fieldsPendingUpdate.size();
    if( index < (fieldsPendingUpdate.size() - 1) )  // don't put
    // a comma
    // on the
    // last one
    sqlBuf.append("," );
    }
    // add the WHERE clause
    String primaryKeyData = tableRecord.getData( getColumnNumberForName( primaryKeyName ) );

    sqlBuf.append( " WHERE ").append( primaryKeyName ).append("='").append( primaryKeyData ).append("'");
    log.write( "Update sql is: " + sqlBuf.toString()  );

    try
    {
    Statement stmt = conn.createStatement();
    log.write( "Executing: " + sqlBuf.toString() );
    // stmt.execute( sqlBuf.toString() );
    }
    catch( SQLException e )
    {
    log.write( e.getMessage() );
    }

    // rebuild the table
    updateNoRebuild();
    }
    }
    }
     **/
    /**
     *
     *
     * This method uses the check table sql command to tell if the real table is
     * dirty
     *
     *
     *
     *
     *
     */
    /**
     *
     *
     * This method is used to update the real database when a virtual recond is
     * dirty
     *
     *
     */
    public synchronized void updateRecord(String key, boolean rebuildTable) {

        boolean updateNeeded = false;
        if (primaryKeyName == null) {
            log.write("Unable to delete from table: " + name + " without primary key with this method");
            return;
        }
        Iterator<VRecord> it = iterator();
        Vector<VField> fieldsPendingUpdate = new Vector<VField>();
        while (it.hasNext()) {
            VRecord rec = it.next();
            if (rec != null) {
                // go through all the fields to see if they are in need of an
                // update
                for (int i = 1; i <= rec.getNumber(); i++) {
                    if (rec.getFieldDelta(i)) {
                        VField field = rec.getField(i);
                        fieldsPendingUpdate.add(field);
                        updateNeeded = true;
                    }
                }
            }
        }
        // buidl an update statement beased on the fields needing change
        // UPDATE tableName SET A=value, B=value WHERE key=value
        if (updateNeeded) {
            StringBuffer sqlBuf = new StringBuffer();
            sqlBuf.append("UPDATE ").append(getName()).append(" SET ");
            for (int index = 0; index < fieldsPendingUpdate.size(); index++) {
                VField field = (VField) fieldsPendingUpdate.elementAt(index);
                if (field == null) {
                    continue;
                }
                sqlBuf.append(field.getName()).append("='").append((String) field.getFieldData()).append("'");
                int num = fieldsPendingUpdate.size();
                if (index < (fieldsPendingUpdate.size() - 1)) // don't put a
                // comma on the
                // last one
                {
                    sqlBuf.append(",");
                }
            }
            // add the WHERE clause
            sqlBuf.append(" WHERE ").append(primaryKeyName).append("='").append(key).append("'");
            log.write("Update sql is: " + sqlBuf.toString());
            try {
                Connection conn = this.connect();
                Statement stmt = conn.createStatement();
                log.write("Executing: " + sqlBuf.toString());
                stmt.execute(sqlBuf.toString());
                log.write("Done! : " + sqlBuf.toString());
                stmt.close();
                connClose(conn);
            } catch (SQLException e) {
                log.write(e.getMessage());
            }
            // rebuild the table
            if (rebuildTable) {
                log.write("Updating table...");
                update();
                log.write("Done, updating table...");
            } else {
                log.write("Refreshing table...");
                refresh();
                log.write("Done, refreshing table...");

            }
        }
        getStatus();
    }

    public synchronized VRecord findRecord(String columnName, String columnValue) {
        log.write(WMSLog.TRACE, name + " is searching for record where field: " + columnName + "=" + columnValue);
        VRecord retRec = null;

        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            retRec = it.next();
            if (retRec != null) {
                String foundValue = retRec.getFieldData(columnName);
                if( this.searchIgnoreCase )
                {
                    if (foundValue != null && foundValue.equalsIgnoreCase(columnValue)) {
                        break;
                    } else {
                        retRec = null;
                    }

                }
                else
                {
                    if (foundValue != null && foundValue.equals(columnValue)) {
                        break;
                    } else {
                        retRec = null;
                    }
                }
            }
        }
        return retRec;
    }

    public synchronized Hashtable<String, VRecord> findRecords(String columnName, String columnValue) {
        log.write(name + " is searching for records where field: " + columnName + "=" + columnValue);
        Hashtable<String, VRecord> records = new Hashtable<String, VRecord>();
        Iterator<VRecord> it = iterator();
        VRecord retRec = null;
        while (it.hasNext()) {
            retRec = it.next();
            if (retRec != null) {
                String foundValue = retRec.getFieldData(columnName);
                if (foundValue != null && foundValue.equals(columnValue)) {
                    String key = retRec.getFieldData(primaryKeyName);
                    records.put(key, retRec);
                }
            }
        }
        return records;
    }

    public int findRecordNumber(VRecord rec) {


        int retRow = -1;





        for (int index = 0; index < getRowCount(); index++) {


            VRecord targetRec = this.getRecord(index);


            if (rec.equals(targetRec)) {


                retRow = index;


                break;


            }


        }





        return retRow;


    }

    public boolean testRecordExists(HashMap<String, VField> candidateMap) {
        boolean exists = false;
        boolean[] recordMatch = new boolean[candidateMap.size()];
        Iterator<VRecord> it = iterator();
        while (it.hasNext()) {
            VRecord testRec = it.next();
            Vector<VField> testFields = testRec.getFields();
            // set all the deltas in the candidateMap fields to false;
            Iterator<VField> candidateIt = candidateMap.values().iterator();
            while (candidateIt.hasNext()) {
                candidateIt.next().setFieldHasDelta(false);
            }
            for (int i = 0; i < testFields.size(); i++) {
                VField testField = testFields.elementAt(i);
                String testFieldName = testField.getName();
                String testFieldValue = testField.getFieldData();
                if (candidateMap.containsKey(testFieldName)) {

                    if (candidateMap.get(testFieldName).getFieldData().equals(testFieldValue)) {
                        candidateMap.get(testFieldName).setFieldHasDelta(true);
                    }
                }
            }
            // if at this point all the fields for the candidate record have "true" in the delta, then the current record matches the  candidate record
            exists = true;
            candidateIt = candidateMap.values().iterator();
            while (candidateIt.hasNext()) {
                VField candidateField = candidateIt.next();

                if (!candidateField.getFieldHasDelta()) {
                    exists = false;
                }
            }
            if (exists) {
                break;
            }
        }
        return exists;
    }

    public void updateRecord(String keyFieldName, String keyFieldValue) {
        boolean updateNeeded = false;
        if (keyFieldName == null || keyFieldValue == null) {
            log.write("Unable to delete from table: " + name + " without  key with this method");
            return;
        }
        Vector<VField> fieldsPendingUpdate = new Vector<VField>();
        VRecord rec = getRecord(keyFieldName, keyFieldValue);
        if (rec != null) {
            // go through all the fields to see if they are in need of an
            // update
            Vector<VField> fields = rec.getFields();
            for (int i = 0; i < fields.size(); i++) {
                VField field = fields.elementAt(i);
                if (field.hasDelta()) {
                    fieldsPendingUpdate.add(field);
                    updateNeeded = true;
                }
            }
        }
        // buidl an update statement beased on the fields needing change
        // UPDATE tableName SET A=value, B=value WHERE key=value
        if (updateNeeded) {
            StringBuffer sqlBuf = new StringBuffer();
            sqlBuf.append("UPDATE ").append(getName()).append(" SET ");
            for (int index = 0; index < fieldsPendingUpdate.size(); index++) {
                VField field = (VField) fieldsPendingUpdate.elementAt(index);
                if (field == null) {
                    continue;
                }
                sqlBuf.append(field.getName()).append("='").append((String) field.getFieldData()).append("'");
                // if( index < (fieldsPendingUpdate.size() - 1) ); // don't put
                // a comma on the last one
                sqlBuf.append(",");
            }
            //strip any trailing commas
            String sqlStr = sqlBuf.toString();
            if (sqlStr.endsWith(",")) {
                sqlStr = sqlStr.substring(0, sqlStr.lastIndexOf(','));
            }
            sqlBuf = new StringBuffer(sqlStr);
            // add the WHERE clause
            sqlBuf.append(" WHERE ").append(keyFieldName).append("='").append(keyFieldValue).append("'");
            log.write("Update sql is: " + sqlBuf.toString());

            try {
                Connection conn = this.connect();
                if (conn != null) {
                    Statement stmt = conn.createStatement();
                    log.write("Executing: " + sqlBuf.toString());
                    lockTable();
                    stmt.execute(sqlBuf.toString());
                    unlockTable("updateRecord");
                    stmt.close();
                    connClose(conn);
                }
            } catch (SQLException e) {
                log.write(e.getMessage());
            }
            // rebuild the table
            refresh();
        }
    }

    public VTableView executeQuery(String sqlStatement) {
        // log.write( "Attempting to execeute: " + sqlStatement );
        ResultSet rs = null;
        String tableName = null;
        java.sql.ResultSetMetaData md = null;
        ;
        try {
            Connection conn = this.connect();
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlStatement);
            md = rs.getMetaData();
            tableName = md.getTableName(1);
            // log.write( "Table name is: " + name );
            stmt.close();
            connClose(conn);
        } catch (SQLException e) {
            log.write(": " + e.getMessage());
        }
        VTable tempTable = new VTable(rs, "tmpTable");
        VTableView view = new VTableView(this);
        // view.setColumnCount( getColumnCount() );
        view.setRowCount(tempTable.getRowCount());
        int realRow = 0;
        for (int i = 0; i < tempTable.getRowCount(); i++) {
            VRecord rec = tempTable.getRecord(i);
            if (rec == null) {
                log.write("null rec found ");
                return null;
            }
            if (primaryKeyName != null) {
                int primaryKeyIndex = getColumnNumberForName(primaryKeyName);
                VRecord targetRec = getRecord(primaryKeyName, rec.getData(primaryKeyIndex));
                if (targetRec != null) {
                    realRow = Integer.parseInt(targetRec.getName());
                    if (realRow != 0) {
                        realRow--; // rows start at zero
                    }
                    view.mapRowToRow(realRow, i);

                }
            } else {


                VRecord targetRec = this.getRecord(i);


                if (targetRec != null) {


                    realRow = Integer.parseInt(targetRec.getName());





                    if (realRow != 0) {
                        realRow--; // rows start at zero
                    }

                    view.mapRowToRow(realRow, i);


                }





            }


        }








        return view;


    }

    /**
     *
     *
     * Method to allow a new table to be created via a set of columns.
     *
     *
     * This allows tables to be broken up into more viewable "chunks"
     *
     *
     * The range must be contigious. If one column is desired, both indexes can
     * be the same.
     *
     *
     */
    public VTableView getSubTable(String tableName, int beginingIndex, int endingIndex) {

        // note: begining index should always start at 1
        if (endingIndex < beginingIndex) {
            return null;
        }
        int numCols = endingIndex - beginingIndex;
        if (numCols == 0) {
            numCols = 1;
        } else {
            // number of columns is inclusive, so we add one
            numCols++;
        }

        Vector[] columns = new Vector[numCols];
        VField[] header = new VField[numCols];
        VTableView view = null;

        int colIndex = 0;
        view = new VTableView(this);
        view.setColumnCount(numCols);

        for (int i = beginingIndex; i < endingIndex; i++) {
            // get the column for this "i", it will be a Vector of VFields
               /*
             * Vector columnFields = getColumnByColumnNumber( i - 1 ); if(
             * columnFields.size() > 0 ) { // get the fields, and hence
             * records by column columns[ colIndex ] = columnFields; header[
             * colIndex ] = this.tableSignature.getSignatureField( i - 1 ); }
             */


            view.mapColToCol(colIndex, i);


            colIndex++;


        }


        // now take the resulting headers and make a new TableSignature for them

        view.setRowCount(getRowCount());

        for (int i = 0; i < getRowCount(); i++) {
            // one to one on the row maping
            view.mapRowToRow(i, i);
        }


        // this subTable is really a view of this table
        return view;


    }

    DBUser getDbUser() {


        return this.user;


    }

    VTable getParent() {


        return parentTable;


    }

    void appendToHeader(String column_name) {


        /**
         *
         *
         * the header looks something like this:
         *
         *
         *
         *
         *
         * ---------------------------------------------- | column_1 | column_2 |
         *
         *
         * column_3 | column_4 | ----------------------------------------------
         *
         *
         *
         *
         *
         */
        StringBuffer top_bottom = new StringBuffer("");


        StringBuffer new_header = new StringBuffer("");


        StringBuffer new_total = new StringBuffer("");





        // add four to account for the padding in the cell


        for (int i = 0; i < (column_name.length() + 3); i++) {


            top_bottom.append("-");


        }


        top_bottom.append(header_top_bottom);


        header_top_bottom = top_bottom.toString();





        new_header.append(header);


        new_header.append(column_name).append(" | ");


        //new_header.append(column_name).append(", ");


        this.header = new_header.toString();





        new_total.append(header_top_bottom).append("\n").append(header).append(
                "\n").append(header_top_bottom);





        totalHeader = new_total.toString();


    }

    public VMetaRecord getMetaData() {
        return metaData;
    }

    public void printHeader() {


        log.write(totalHeader);


    }

    public String getHeader() {


        return totalHeader;


    }

    public synchronized boolean isLocked() {


        return isLocked;


    }

    synchronized public void releaseLock(Object requester) {


        if (requester == lockHolder) {


            // isLocked = false;


            // ock.notify();


            lockHolder = null;


        }


    }

    private void setLocked(boolean locked) {
        isLocked = locked;
        // lock.notifyAll( );
    }

    public WMSLog getLog() {
        return log;
    }

    synchronized public boolean getLock(Object holder) {

        if (isLocked) {
            return false;
        }

        /*
         *
         *
         * while( isLocked )
         *
         *  {
         *
         *
         * try
         *
         *  {
         *
         *
         * lock.wait( timeOut);
         *
         *  // try and get the lock within the timeout period
         *
         *
         *
         *
         *  }
         *
         *
         * catch( InterruptedException e )
         *
         *  {
         *
         *
         * return false;
         *
         *  }
         *
         *  }
         *
         *
         */


        lockHolder = holder;


        // isLocked = true;


        return true;


    }

    public void setPolling(boolean flag) {
        polling = flag;
    }

    public boolean isPolling() {
        return polling;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean flag) {
        synced = flag;
    }

    public boolean isSingleConnection() {
        return singleConnection;
    }

    public void setSingleConnection(boolean singleConnection) {
        this.singleConnection = singleConnection;
    }

    public Vector<Vector<String>> getData() {
        Vector<Vector<String>> data = new Vector<Vector<String>>();
        Vector<VRecord> rows = getRows();
        Iterator<VRecord> it = rows.iterator();
        Vector<String> dataRow = new Vector<String>();

        while (it.hasNext()) {
            VRecord rec = it.next();
            for (int column = 0; column < rec.getNumber(); column++) {
                String dataStr = rec.getFieldData(column);
                if (dataStr != null) {
                    dataRow.add(dataStr);
                } else {
                    dataRow.add("VNULL");
                }
            }
            data.add(dataRow);
            dataRow = new Vector<String>();
        }
        return data;
    }

    public Vector<Vector<String>> getData(String columnName, String columnValue, Vector<String> filter) {
        Vector<Vector<String>> data = new Vector<Vector<String>>();

        Vector<VRecord> rows = this.getRecords(columnName, columnValue);
        Iterator<VRecord> it = rows.iterator();
        Vector<String> dataRow = new Vector<String>();
        if (filter == null) {
            filter = new Vector<String>();
        }

        while (it.hasNext()) {
            VRecord rec = it.next();
            for (int column = 1; column < rec.getNumber(); column++) {
                String fieldName = metaData.getFieldName(column);
                if (!filter.contains(fieldName)) {
                    String dataStr = rec.getFieldData(column);
                    if (dataStr != null) {
                        dataRow.add(dataStr);
                    } else {
                        dataRow.add("VNULL");
                    }
                }
            }
            data.add(dataRow);
            dataRow = new Vector<String>();
        }
        return data;
    }

    public void makeCompoundKeyName() {
        // create a compound key name, by concatinating all the name fields together....
    }

    public TableKeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(TableKeyType keyType) {
        this.keyType = keyType;
    }

    public boolean isAlwaysPoll() {
        return alwaysPoll;
    }

    public void setAlwaysPoll(boolean alwaysPoll) {
        this.alwaysPoll = alwaysPoll;
    }

    public String getOrderByFieldName() {
        return orderByFieldName;
    }

    public void setOrderByFieldName(String orderByFieldName) {
        this.orderByFieldName = orderByFieldName;
        // refresh(); // apply the new order
    }

    public String getSqlSelectString() {
        return sqlSelectString;
    }

    public void setSqlSelectString(String sqlSelectStringPattern) {
        translateSelectPattern(sqlSelectStringPattern);
    }

    private void connClose(Connection conn) {
        if (conn == this.singleConn) {
            return;
        } else {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.write(WMSLog.ERROR, "Unable to close connection. Reason was: " + e.getMessage());
                }

            }
        }
    }

    public boolean isEnableFastLookup() {
        return enableFastLookup;
    }

    public void setEnableFastLookup(boolean enableFastLookup) {
        this.enableFastLookup = enableFastLookup;
    }

    public void addIndex(String fieldName) {
        isIndexed = true;
        if (index == null) {
            index = new VTableIndex();
        }
        index.addIndexedField(fieldName);
        // index the current records
        Iterator<VRecord> it = this.iterator();
        while (it.hasNext()) {
            it.next().setIndex(fieldName);
        }
    }
    // this should be only visible and usable by mainly VRecord.

    public void addIndexValue(String fieldName, String fieldValue, VRecord record) {
        if (!isIndexed) {
            return;
        }

        if (index.isIndexed(fieldName)) {
            index.addIndexedValue(fieldName, fieldValue, record);
        }
    }

    public void addIndexValue(int columnPosition, String fieldValue, VRecord record) {
        if (!isIndexed) {
            return;
        }

        String fieldName = this.metaData.getFieldName(columnPosition);
        if (fieldName != null && index.isIndexed(fieldName)) {
            index.addIndexedValue(fieldName, fieldValue, record);
        }
    }

    // This would be used to force a table to behave like the key is compound, even though the describe 
    // says the key is Primary. This is allowed behavior in DB's like MySql where a key can be primary, but not unique ( non-incremental )
    public void forceCompondKey() {
        this.keyType = TableKeyType.COMPOUND;
    }

    public String getInsertStatement()
    {
        //  "INSERT INTO ticket (ticketNum, carId, cusId, estId, comment, carcomment, stage, type) VALUES (%d,%d,%d,%d,%s,%s,%d,%d)";
        String retStr = null;
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(name).append( " (");
        int numFields = this.columnCount;
        StringBuilder names = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for( int pos = 1; pos <= numFields; pos++ )
        {
            String colName = this.metaData.getFieldName(pos);
            DBDataTypes type = this.metaData.getType(pos);
            names.append( colName.trim()).append(",");
            String value = DBDataTypes.getJDBCShortType(type);
            values.append(value).append(",");
            
        }
        String ns = names.substring(0, names.length() - 1);
        String vs = values.substring(0, values.length() - 1 );
        sb.append(ns).append( ") VALUES (").append(vs).append(")");
        retStr = sb.toString();
        return retStr;
    }
}
