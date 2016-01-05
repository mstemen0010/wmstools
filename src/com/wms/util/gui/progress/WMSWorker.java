/*
 * SCPWorker.java
 *
 * Created on October 26, 2005, 9:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util.gui.progress;
/**
 *
 * @author mstemen
 */

public class WMSWorker implements Runnable
{
    private WMSJob taskToRun;
    private Object value;  // see getValue(), setValue()    
    private boolean running = true;
    
    /** Creates a new instance of SCPWorker */
    public WMSWorker( WMSJob taskToRun ) {
        this.taskToRun = taskToRun;

        Thread t = new Thread(this);
        t.setName(WMSWorker.class.getName() + "::" + "WMSJob::" + "Id = " + this.hashCode());
        t.start();
        // threadVar = new ThreadVar(t);
        // threadVar.get().setName("WorkerThread" + hashCode() );        
    }
            
    public void run() {                    
        while( running )                
        {                    
            try 
            {

                setValue(runTask());
                Thread.sleep( 100 );
                WMSWorker.this.taskToRun.checkDone();
                if( WMSWorker.this.taskToRun.isDone() )
                    running = false;                   
            }            
            catch( java.lang.InterruptedException e )             
            {
           
            }
           
        }        
        // threadVar.clear();                             
    }    
        
    public WMSJobInterface runTask()
    {
        // call the SCPJob
         return taskToRun.process();
    }
    

    /** 
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;
        ThreadVar(Thread t) { thread = t; }
        synchronized Thread get() { return thread; }
        synchronized void clear() { thread = null; }
    }

    private ThreadVar threadVar;

    /** 
     * Get the value produced by the worker thread, or null if it 
     * hasn't been constructed yet.
     */
    protected synchronized Object getValue() { 
        return value; 
    }

    /** 
     * Set the value produced by worker thread 
     */
    private synchronized void setValue(Object x) { 
        value = x; 
    }

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to stop what it's doing.
     */
    public void interrupt() {
        Thread t = threadVar.get();
        if (t != null) {
            t.interrupt();
        }
        threadVar.clear();
    }

    /**
     * Return the value created by the <code>construct</code> method.  
     * Returns null if either the constructing thread or the current
     * thread was interrupted before a value was produced.
     * 
     * @return the value created by the <code>construct</code> method
     */
    public Object get() {
        while (true) {  
            Thread t = threadVar.get();
            if (t == null) {
                return getValue();
            }
            try {
                t.join();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }


    /**
     * Start the worker thread.
     */
    public void start() {        
//        Thread workerThread = threadVar.get();
//        workerThread.setName( "WMSWorkerThread::" + this.hashCode());
//        if (workerThread != null) {
//            workerThread.start();
//        }
    }         
}
