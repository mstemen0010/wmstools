/*

 * ProcessWorker.java

 *

 * Created on October 21, 2004, 9:50 AM

 */



package com.wms.util;



import java.util.*;



/**

 *

 * @author  mstemen

 */

public class ProcessWorker extends Thread 

{

    

    /** Creates a new instance of ProcessWorker */

    public ProcessWorker() 

    {

    }

   

    

    public void run()

    {

        

    }

    

    public void start()

    {

        

    }

    

    public void setProcessCmd( String processCmd )

    {

        this.processCmd = processCmd;

    }

    

    private Vector outputInfo = new Vector();    

    private WMSLog log = new WMSLog( "/tmp", "processWorker_" + this.hashCode() + ".log" );

    private String processCmd = new String();

}

