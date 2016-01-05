/*
 * GESSpellBookPage.java
 *
 * Created on May 18, 2006, 2:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util.xmltools.gestalt;

import com.wms.util.xmltools.gestalt.WMSSpell;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.*;
import java.awt.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;


enum SpellBookPageTypes
{
    
};

/**
 *
 * @author mstemen
 */
public class WMSSpellBookPage implements WMSSpellCauldronInterface, WMSSpellBookPageInterface
{

            
    private int type = 0;
    private int size = 11;
    private String title = null;
    private JPanel pagePanel = null;
    private JPanel linePanel = null;
    private boolean hasSpells = false;
    private boolean useLinePanel = false;
    String layout = "flow";
    String position = null;
    int height =  0;
    int width = 0;
    WMSSpellCauldronInterface cauldron;
    HashSet<WMSSpellInterface> spells = new HashSet<WMSSpellInterface>();
    
    
    public WMSSpellBookPage( WMSSpellBookInterface spellBook, WMSSpellCauldronInterface cauldron, int type ) {
        this.cauldron = cauldron;
        this.type = type;
        pagePanel = new JPanel();        
    }

    // Interface Methods
    public void add( String key, String value )
    {
        if( cauldron != null )
        {
            cauldron.add( key, value );
        }
    }
    
    public Iterator<WMSSpellInterface> getSpells()
    {
        return spells.iterator();
    }
    
    public void addSpell( WMSSpellInterface spell )
    {
        spells.add( spell );
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

    /**
     * Creates a new instance of GESSpellBookPage
     */
    public WMSSpellBookPage( WMSSpellCauldronInterface cauldron ) {
        this.cauldron = cauldron;
        pagePanel = new JPanel();
    }

    public String getLayout() {
        return layout;
    }
    
    private void setLayout( )
    {
        if( layout == null )
            return;
        if( layout.equalsIgnoreCase("box"))
        {
            pagePanel.setLayout( new BoxLayout( pagePanel, BoxLayout.Y_AXIS ) );
        }
        else if( layout.equalsIgnoreCase("flow"))
        {
            pagePanel.setLayout( new FlowLayout());
        }
        else if ( layout.equalsIgnoreCase( "border"))
        {
            pagePanel.setLayout( new BorderLayout() );
        }
    }
    private void setOptions( Node node )
    {
        if( node.hasAttributes() )
        {
            NamedNodeMap nnm = node.getAttributes();
            String[] optionsList = {"layout", "name" };
            for( int i = 0; i < optionsList.length; i++ )
            {
                Node optNode = nnm.getNamedItem( optionsList[i] );
                if( optNode != null )
                {
                    if( optionsList[i].equalsIgnoreCase("layout"))
                    {
                        this.layout = optNode.getNodeValue();
                        setLayout();
                    }
                    else if( optionsList[i].equalsIgnoreCase( "name"))
                    {
                        this.title = optNode.getNodeValue();
                    }
                }
            }
        }
    }
    public void buildPage( Node spellBookPageNode ) throws WMSWizardException
    {
        if( spellBookPageNode != null )
        {
            String spellBookPageNodeName = spellBookPageNode.getNodeName();
            if( spellBookPageNodeName.equals( "SPELLBOOKPAGE"))
            {
                setOptions( spellBookPageNode );
                NodeList spellBookPageNodeList = spellBookPageNode.getChildNodes();
                if( spellBookPageNodeList == null )
                {
                    throw new WMSWizardException( "MalfolmedSpellBook. page list was null" );
                }
                // walk the node list ( spell book page ) 
                for( int d = 0; d < spellBookPageNodeList.getLength(); d++ )
                {
                    Node spellBookNode = spellBookPageNodeList.item( d );
                    String spellNodeName = spellBookNode.getNodeName();
                    linePanel = null;
                    useLinePanel = false;
                    
                    if( spellNodeName.equals("SPELL"))
                    {
                        // see if there are attributes for this item
                        if( spellBookNode.hasAttributes() )
                        {       
                            
                            NamedNodeMap nnm = spellBookNode.getAttributes();
                            if( nnm != null )
                            {
                                String[] args = {"type", "bgcolor", "position", "height", "width" };
                                for( int i = 0; i < args.length; i++ )
                                {
                                    Node attribNode = nnm.getNamedItem(args[i]);
                                    if( attribNode != null )
                                    {
                                        String value = attribNode.getNodeValue();
                                        if( value != null )
                                        {
                                            if( args[i].equals("type"))
                                            {
                                               if( value.equals("panel"))
                                               {
                                                  if( linePanel == null )
                                                  {
                                                    linePanel = new JPanel();
                                                    linePanel.setBackground( new java.awt.Color( 255,255,255 ));
                                                    useLinePanel = true;                                                    
                                                  }                                                
                                                   
                                               }
                                            }
                                            else if( args[i].equals( "bgcolor"))
                                            {
                                                // Color bgColor = LogosTools.convertColorFromString( value );
                                                if( linePanel != null )
                                                {
                                                    
                                                    //linePanel.setBackground( bgColor );
                                                }
                                            }
                                            else if( args[i].equalsIgnoreCase("position"))
                                            {
                                                position = value;
                                            }
                                            else if( args[i].equalsIgnoreCase("width"))
                                            {
                                                width = Integer.valueOf( value );
                                            }
                                            else if( args[i].equalsIgnoreCase("height"))
                                            {
                                                height = Integer.valueOf( value );
                                            }
                                        }
                                    }
                                }                                        
                            }                                                        
                        }
                        NodeList spellNodeList = spellBookNode.getChildNodes();
                        for( int e = 0; e < spellNodeList.getLength(); e++ )
                        {
                            Node spellNode = spellNodeList.item( e );
                            String spellName = spellNode.getNodeName();
                            WMSSpell spell = new WMSSpell( this );
                            spell.buildSpell( spellNode );
                            addSpell( spell );
                            
                            if( spell.doesHaveValues())
                            {
                                JComponent component = spell.getComponent();
                                setPanelSize();
                                if( component !=  null )
                                {
                                    if( useLinePanel )
                                    {
                                        
                                        linePanel.add( component );
                                        if( layout != null && layout.equalsIgnoreCase("border"))
                                        {
                                            
                                            if( position != null && position.equalsIgnoreCase("center"))
                                            {
                                                setPanelSize();
                                                pagePanel.add( linePanel, BorderLayout.CENTER );
                                            }
                                            else if( position != null && position.equalsIgnoreCase("south"))
                                            {
                                                pagePanel.add( linePanel, BorderLayout.SOUTH );
                                            }
                                            else if( position != null && position.equalsIgnoreCase("north"))
                                            {
                                                pagePanel.add( linePanel, BorderLayout.NORTH );
                                            }
                                            
                                        }   
                                        else
                                        {
                                            pagePanel.add( linePanel );    
                                        }                                        
                                    }
                                    else
                                    {
                                        if( layout != null && layout.equalsIgnoreCase("border"))
                                        {
                                            if( position != null && position.equalsIgnoreCase("center"))
                                            {
                                                pagePanel.add( component, BorderLayout.CENTER );
                                            }
                                            else if( position != null && position.equalsIgnoreCase("south"))
                                            {
                                                pagePanel.add( component, BorderLayout.SOUTH );
                                            }
                                            else if( position != null && position.equalsIgnoreCase("north"))
                                            {
                                                pagePanel.add( component, BorderLayout.NORTH );
                                            }
                                            
                                        }   
                                        else
                                        {
                                            pagePanel.add( component );    
                                        }
                                    }
                                    
                                    hasSpells = true;
                                }
                            }
                        }

                    }
                }
            }        
        }
        else
        {
            throw new WMSWizardException( "Value passed was not a spell book page" );
        }
    }
    private void setPanelSize()
    { 
        if( width > 0 && height > 0 )
        {
            if( linePanel != null )
            {
                linePanel.setPreferredSize( new Dimension( width, height ) );
            }
            else 
            {
                pagePanel.setPreferredSize( new Dimension( width, height ) );
            }
        }
    }

    public boolean doesHaveSpells() {
        return hasSpells;
    }
    
}
