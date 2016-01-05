/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wms.util.xmltools;

import org.w3c.dom.Node;

/**
 *
 * @author mstemen
 */
public interface NodeMapInterface {

    public void addKey(String keyName);

    public void addNode(String keyName, Node nodeToAdd );

    public void mapObject( String keyName, Object obj );


}
