/*
 * GESSpell.java
 *
 * Created on May 18, 2006, 3:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util.xmltools.gestalt;

import com.wms.util.xmltools.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import javax.swing.*;

/**
 *
 * @author mstemen
 */
public class WMSSpell implements WMSSpellInterface {

    private boolean hasValues = false;
    private JComponent component = null;
    private int width = 0;
    private String value = "";
    private String name = "";
    private WMSSpellBookPage spellBookPage;
    public enum GESSpellType
    {
        EMPTY,
        BR,
        LABEL,
        TEXTFIELD,
        CHECKBOX;
                
    };
    
    // GESSpellInterface metthods
    
    public String getName()
    {
        return name;
    }
    
    public String getValue()
    {
        return value;
    }
    
    /** Creates a new instance of GESSpell */
    public WMSSpell( WMSSpellBookPage spellBookPage ) {
        this.spellBookPage = spellBookPage;
        
    }
    
    public void buildSpell( Node spellNode )
    {
        String spellName = spellNode.getNodeName();
        if( spellName.equalsIgnoreCase("EMPTY") || spellName.equalsIgnoreCase("BR") )
        {
            invokeSpell( spellNode, GESSpellType.valueOf(spellName), "");
        }
        if( spellNode != null )
        {            
            NodeList nodeList= spellNode.getChildNodes();
            for( int f = 0; f < nodeList.getLength(); f++ )
            {
                Node labelNode = nodeList.item( f );
                if( labelNode != null )
                {              
                    String name = labelNode.getNodeName();
                    String value = labelNode.getNodeValue();                      
                    invokeSpell( spellNode, GESSpellType.valueOf(spellName), value);                    
                    System.out.println( "value: " + value );
                }
            }
        }        
    }
    
    public void invokeSpell( Node spellNode, WMSSpell.GESSpellType type, String value )
    {        
        switch( type )
        {
            case LABEL:
                if( value != null )
                {
                    component = new JLabel( value );                    
                    hasValues = true;
                }                
                break;
            case TEXTFIELD:                 
                hasValues = true;
                component = new JTextField();
                setArgs( spellNode );
                createCallBack();                
                ((JTextField)component).setColumns( width );
                break;                                 
            case EMPTY:
            case BR:
                hasValues = true;
                component = new JLabel("");
               
                break;
        }        
    }
    
    private void add( )
    {
        if( value.length() > 0 && name.length() > 0 )
        {
            spellBookPage.add( name, value );
        }
    }
    private void createCallBack()
    {
       if( JTextField.class.isInstance( component ))
       {
            ((JTextField)component).addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    value = ((JTextField)component).getText();
                    add();
                }
            });
       }
    }
    
    private void setArgs( Node spellNode )
    {
        if( component != null )
        {
            if( spellNode.hasAttributes() )
            {
                NamedNodeMap nnm = spellNode.getAttributes();
                if( nnm != null )
                {
                    Node n = nnm.getNamedItem("width");
                    if( n != null )
                    {
                        String name = n.getNodeName();
                        String value = n.getNodeValue();
                        if( value != null )
                        {
                            width = Integer.parseInt( value );
                        }
                    }
                    n = nnm.getNamedItem( "name");
                    if( n != null )
                    {
                        this.name = n.getNodeValue();
                    }
                }
            }
        }
    }

    public boolean doesHaveValues() {
        return hasValues;
    }

    public JComponent getComponent() {
        return component;
    }
}
