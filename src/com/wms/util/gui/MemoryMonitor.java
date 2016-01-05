/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * memoryMonitor.java
 *
 * Created on Jun 19, 2009, 8:10:38 PM
 */
package com.wms.util.gui;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author mstemen
 */
public class MemoryMonitor extends javax.swing.JFrame implements Runnable {

    private static final long serialVersionUID = 637425185162568485L;
    private DecimalFormat memFormatMb = new DecimalFormat(" ####.## Mb");
    private DecimalFormat memFormatKb = new DecimalFormat(" #### Kb");
    private boolean running = false;

    /** Creates new form memoryMonitor */
    public MemoryMonitor() {
        initComponents();
    }

    public JPanel getMemoryMonitorPanel() {
        return this.memPanel;
    }

    public String getMaxMem() {
        return this.maxMem.getText();
    }

    public String getFreeMem() {
        return this.freeMem.getText();
    }

    public String getMemDelta() {
        return this.deltaMem.getText();
    }

    public void showMemProgress(int percent) {
        if (percent >= 75) {
            memProgress.setForeground(Color.GREEN);
        } else if (percent < 75 && percent >= 60) {
            memProgress.setForeground(Color.GREEN.darker());
        } else if (percent < 60 && percent >= 45) {
            memProgress.setForeground(Color.YELLOW);
        } else if (percent < 45 && percent >= 25) {
            memProgress.setForeground(Color.ORANGE.darker());
        } else if (percent < 25 && percent > 15) {
            memProgress.setForeground(Color.ORANGE);
        } else {
            memProgress.setForeground(Color.RED);
        }
        memProgress.setValue(percent);
        memProgress.setString(String.valueOf(percent) + "%");
    }

    public void showFreeMem(String value) {
        freeMem.setText(value);
    }

    public void showMaxMem(String value) {
        maxMem.setText(value);
    }

    public void showDeltaMem(String value, boolean more) {
        deltaMem.setText(value);
        if (more) {
            deltaMem.setForeground(Color.GREEN);
            deltaMem.setBackground(Color.BLACK);
        } else {
            deltaMem.setForeground(Color.RED);
            deltaMem.setBackground(Color.WHITE);
        }
    }

    public void statMemory() {

        double currentFree = 0.00D;
        double diffFree = 0.00D;
        double maxMemd = Runtime.getRuntime().maxMemory() / 1048576.00D;

        /// System.gc();
        double usedMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        currentFree = (Runtime.getRuntime().maxMemory() - usedMem) / 1048576.00D;
        diffFree = Math.abs(lastFreeMem - currentFree);
        String freeMemStr = null;
        int percent = (int) ((currentFree / maxMemd) * 100);
        if (currentFree < 1) {
            freeMemStr = memFormatKb.format(currentFree * 1024);
        } else {
            freeMemStr = memFormatMb.format(currentFree);
        }
        String deltaMemStr = null;

        if (diffFree < 1) {
            deltaMemStr = memFormatKb.format(diffFree * 1024);
        } else {
            deltaMemStr = memFormatMb.format(diffFree);
        }

        /*
        StringBuilder sb = new StringBuilder("GC #");
        sb.append( ++gcCount ).append( " free memory: ").append( Runtime.getRuntime().freeMemory()/1048576.00 );
        sb.append( "Mb total available to ParallaxMgr: ").append((double)Runtime.getRuntime().maxMemory()/1048576.00 );
        if( currentFree > lastFreeMem )
        sb.append( "Mb change is: +" ).append( (double)diffFree/1048576.00 ).append( "Mb");
        else
        sb.append( "Mb change is: -" ).append( (double)diffFree/1048576.00 ).append( "Mb");
         */
        showFreeMem(freeMemStr);
        showMaxMem(String.valueOf(maxMemd));
        if (currentFree > lastFreeMem) {
            showDeltaMem(String.valueOf(deltaMemStr), true);
        } else {
            showDeltaMem(String.valueOf(deltaMemStr), false);
        }
        showMemProgress(percent);
        lastFreeMem = currentFree;
    // sendStatusMessage( sb.toString() );
    }

    private void memProgressMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_memProgressMouseClicked
        System.gc();
        statMemory();
    }//GEN-LAST:event_memProgressMouseClicked

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        memPanel = new javax.swing.JPanel();
        jLabel66 = new javax.swing.JLabel();
        freeMem = new javax.swing.JFormattedTextField();
        jLabel67 = new javax.swing.JLabel();
        maxMem = new javax.swing.JFormattedTextField();
        jLabel68 = new javax.swing.JLabel();
        deltaMem = new javax.swing.JFormattedTextField();
        memProgress = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        memPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        memPanel.setMaximumSize(new java.awt.Dimension(400, 24));
        memPanel.setMinimumSize(new java.awt.Dimension(400, 24));
        memPanel.setPreferredSize(new java.awt.Dimension(400, 24));

        jLabel66.setFont(new java.awt.Font("Trebuchet MS", 1, 11)); // NOI18N
        jLabel66.setLabelFor(freeMem);
        jLabel66.setText("A:");
        jLabel66.setToolTipText("Available Mem");
        jLabel66.setMaximumSize(new java.awt.Dimension(11, 18));
        jLabel66.setMinimumSize(new java.awt.Dimension(11, 18));
        jLabel66.setPreferredSize(new java.awt.Dimension(11, 18));

        freeMem.setText("0.0");
        freeMem.setToolTipText("Available Mem");
        freeMem.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        freeMem.setMaximumSize(new java.awt.Dimension(40, 18));
        freeMem.setMinimumSize(new java.awt.Dimension(40, 18));
        freeMem.setPreferredSize(new java.awt.Dimension(40, 18));

        jLabel67.setFont(new java.awt.Font("Trebuchet MS", 1, 11)); // NOI18N
        jLabel67.setLabelFor(maxMem);
        jLabel67.setText("M:");
        jLabel67.setToolTipText("Max Mem");
        jLabel67.setMaximumSize(new java.awt.Dimension(12, 18));
        jLabel67.setMinimumSize(new java.awt.Dimension(12, 18));
        jLabel67.setPreferredSize(new java.awt.Dimension(12, 18));

        maxMem.setText("0.0");
        maxMem.setToolTipText("Max Mem");
        maxMem.setFont(new java.awt.Font("Trebuchet MS", 0, 11));
        maxMem.setMinimumSize(new java.awt.Dimension(40, 20));
        maxMem.setPreferredSize(new java.awt.Dimension(40, 20));

        jLabel68.setFont(new java.awt.Font("Trebuchet MS", 1, 11)); // NOI18N
        jLabel68.setLabelFor(deltaMem);
        jLabel68.setText("Δ :");
        jLabel68.setToolTipText("Mem change");

        deltaMem.setBackground(new java.awt.Color(0, 0, 0));
        deltaMem.setText("0.0");
        deltaMem.setToolTipText("Mem change");
        deltaMem.setFont(new java.awt.Font("Trebuchet MS", 1, 11));
        deltaMem.setMaximumSize(new java.awt.Dimension(50, 18));
        deltaMem.setMinimumSize(new java.awt.Dimension(50, 18));
        deltaMem.setPreferredSize(new java.awt.Dimension(50, 18));
        deltaMem.setRequestFocusEnabled(false);

        memProgress.setBackground(new java.awt.Color(0, 0, 0));
        memProgress.setForeground(new java.awt.Color(0, 255, 102));
        memProgress.setMaximumSize(new java.awt.Dimension(150, 18));
        memProgress.setMinimumSize(new java.awt.Dimension(150, 18));
        memProgress.setPreferredSize(new java.awt.Dimension(150, 18));
        memProgress.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                memProgressMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout memPanelLayout = new org.jdesktop.layout.GroupLayout(memPanel);
        memPanel.setLayout(memPanelLayout);
        memPanelLayout.setHorizontalGroup(
            memPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(memPanelLayout.createSequentialGroup()
                .add(jLabel66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 33, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(freeMem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel67, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(maxMem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(deltaMem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(memProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        memPanelLayout.setVerticalGroup(
            memPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(memPanelLayout.createSequentialGroup()
                .add(memPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(memPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(freeMem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(maxMem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(deltaMem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(memProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel66.getAccessibleContext().setAccessibleName("free:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            MemoryMonitor memMon = null;
            boolean running = true;

            public void run() {
                memMon = new MemoryMonitor();
                memMon.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField deltaMem;
    private javax.swing.JFormattedTextField freeMem;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JFormattedTextField maxMem;
    private javax.swing.JPanel memPanel;
    private javax.swing.JProgressBar memProgress;
    // End of variables declaration//GEN-END:variables
    java.awt.GridBagConstraints gridBagConstraints;
    private double lastFreeMem = 0;

    public void run() {

        running = true;
        while (running) {
            try {
                Thread.sleep(1000);
                statMemory();
                // System.out.println("Update");
            } catch (InterruptedException ex) {
                Logger.getLogger(MemoryMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}