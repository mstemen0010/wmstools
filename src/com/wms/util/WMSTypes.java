/*
 * GESTypes.java
 *
 * Created on May 18, 2006, 12:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util;

import javax.swing.JPanel;

/**
 *
 * @author mstemen
 */
public class WMSTypes {
    
    /** Creates a new instance of GESTypes */
    public WMSTypes() {
    }
   public enum ScriptType {
        None,
        FullConfigPartition,
        FullConfigConsolidation,
        ReConfigPartition,
        ReConfigConsolidation
    }

    public enum ScriptPage {
        None(0),
        Intro(1),
        PickScenario(2),
        AddServer(3),
        AddMediaMgr(4),
        AddRealLib(5),
        AddRealLibs(6),
        AddVirtLib(7),
        AddVirtLibs(8),
        AddVirtDrives(9),
        AssocRealTapes(10);

        private ScriptPage( int type ) {
            this.type = type;
        }

        public void setPagePanel( JPanel newPanel ) {
            pagePanel = newPanel;
        }

        public JPanel getPagePanel() {
            return pagePanel;
        }

        public void setTitle( String title ) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public int toInt() {
            return type;
        }

        public int size() {
            return size;
        }
        private int type = 0;
        private int size = 11;
        private String title = null;
        private JPanel pagePanel;
    }

    public enum UDTOperationTypes {
        AddVirtualDrive,
        AddVirtualLibrary,
        AddRealLibraryToRelation,
        AddVirtualDriveToLibrary,
        CreateVirtualDriveFromStock,
        CreateVirtualLibraryFromStock,
        AssignVolumeToVirtualLibrary,
        AddUDTMediaManager,
        SetDriveElement,
    }
    
    
}
