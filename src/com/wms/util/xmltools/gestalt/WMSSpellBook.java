/*
 * GESSpellBook.java
 *
 * Created on May 18, 2006, 2:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util.xmltools.gestalt;

import javax.swing.JPanel;
import java.util.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.helpers.ParserAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 *
 * @author mstemen
 * Spell Books are used by wizards ( i.e. the script that drives the wizard )
 * At some point the books and there pages will be xml driven 
 * (i.e. <SPELLBOOK name=Employee Record Entry>
 *          <SPELLBOOKPAGE>
 *              <LABEL>Employee Name</LABEL>
 *              <TEXTFIELD>EmployeeNameValue</TEXTFIELD>
 *          </SPELLBOOKPAGE>
 *      <SPELLBOOK>
 *      
 *
 */
public class WMSSpellBook implements WMSSpellBookInterface, WMSSpellCauldronInterface  {

    // enum for preset spell book types
    public enum GESSpellBookType
    {
        CONFIGURATION,
        LICENSE,
        NONE
    };
            
    private WMSSpellBookPage currentSpellBookPage = null;
    private int currentSpellBookSize = 1;
    private WMSSpellBookPage allPages[] = new WMSSpellBookPage[ currentSpellBookSize ];
    private ArrayList<WMSSpellBookPage> spellBook = new ArrayList<WMSSpellBookPage>();
    private WMSSpellBookPage script[] = null;
    private int currentIndex = 0;        
    private int previousIndex = -1;
    private GESSpellBookType type = GESSpellBookType.NONE;
    private Document xmlDocument = null;
    private ParserAdapter pa = null;
    private WMSSpellCauldronInterface cauldron;
    private DocumentBuilder parser = null;
    private String key = null;
    /**
     * Creates a new instance of GESSpellBook
     */
        // private SCPTypes.ScriptType scriptType = SCPTypes.ScriptType.None;
                
        public WMSSpellBook( String key, WMSSpellCauldronInterface wizardCauldron )
        {
            this.key = key;
            this.cauldron = wizardCauldron;
            script = new WMSSpellBookPage[1];
            // learnSpellBookTypes();
        }
        
        public WMSSpellBook( String key, WMSSpellCauldronInterface wizardCauldron, String spellBookPath, String spellBookFileName )
        {
            this.key = key;
            this.cauldron = wizardCauldron;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try
            {            
                parser = factory.newDocumentBuilder();
            }
            catch( ParserConfigurationException e )
            {    
                System.out.println( "ParserConfigException: " + e.getMessage() );
            }    
            
            readSpellBook( new File( spellBookPath, spellBookFileName ) );
            
        }
        
        public HashSet<WMSSpellInterface> getSpells()
        {
            HashSet<WMSSpellInterface> spells = new HashSet<WMSSpellInterface>();
            Iterator<WMSSpellBookPage> allSpellPages = spellBook.iterator();
            while( allSpellPages.hasNext() )
            {
                WMSSpellBookPage page = allSpellPages.next();
                if( page != null )
                {
                    Iterator<WMSSpellInterface>allSpells = page.getSpells();
                    while( allSpells.hasNext() )
                    {
                        spells.add( allSpells.next() );
                    }
                }
            }
            
            return spells;
        }
        
        public String  getKey()
        {
            return key;
        }
        public void addPage( WMSSpellBookPage page )
        {
            spellBook.add( page );
        }
               
        public void add( String key, String value )
        {
            if( cauldron != null )
            {
                cauldron.add( key, value );
            }
        }
        public WMSSpellBook( String spellBookPathName )
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try
            {            
                parser = factory.newDocumentBuilder();
            }
            catch( ParserConfigurationException e )
            {    
                System.out.println( "ParserConfigException: " + e.getMessage() );
            }    
            
            readSpellBook( new File( spellBookPathName ) );
            
        }
        
        public WMSSpellBook( GESSpellBookType type, ArrayList<WMSSpellBookPage>script )
        {
            this.type = type;
            //TODO: ceate the code that would "preload" know spell book types...
            // learnSpellBookTypes()             
            setScript( script );            
            
        }        
        /*
        private void learmSpellBookTypes()
        {
            GESSpellBookPage allTypes[] = GESSpellBookPage.values();
            for( int i = 0; i < allTypes.length; i++ )
            {
                allPages[i] = allTypes[i];
            }
        }
        */
        
        public void setScript( ArrayList<WMSSpellBookPage> newScript )
        {
            Iterator<WMSSpellBookPage> it = newScript.iterator();
            if( newScript.size() > script.length )
                script = new WMSSpellBookPage[ newScript.size() ];
            int page = 0;
            while( it.hasNext() )
            {
                WMSSpellBookPage currentPage = it.next();
                if( currentSpellBookPage == null )
                    currentSpellBookPage = currentPage;
                
                script[page++] = allPages[currentPage.toInt()];
            }
        }
        
        public void setPageTitle( String title, WMSSpellBookPage page )
        {                
            allPages[ page.toInt() ].setTitle( title );
        }

        public String getPageTitle( WMSSpellBookPage page )
        {
           return allPages[ page.toInt() ].getTitle();
        }
        
        public int getIndex()
        {
            return currentIndex;
        }
        
        
        public void setScriptPagePanel( JPanel panel, WMSSpellBookPage page )
        {                
            allPages[ page.toInt()].setPagePanel( panel );                
        }
        
        public JPanel getScriptPagePanel( WMSSpellBookPage page )
        {                        
            return allPages[ page.toInt() ].getPagePanel();                        
        }
        
        public int getScriptIndex( WMSSpellBookPage page )
        {
            return spellBook.indexOf( page );
        }
        
        public WMSSpellBookPage next()
        {
            previousIndex = currentIndex++;
            WMSSpellBookPage gsbp = null;
            if( currentIndex < spellBook.size() )
            {
                
                gsbp = spellBook.get( currentIndex );
            }
            
            return gsbp;
        }        
        
        public WMSSpellBookPage last()
        {
            int newIndex = currentIndex;
            WMSSpellBookPage lastPage = null;
            try
            {
                newIndex = currentIndex - 1;
                currentIndex = newIndex;
                currentSpellBookPage = script[ newIndex ];
                lastPage = script[ newIndex ];
            }
            catch( ArrayIndexOutOfBoundsException e )
            {
                currentSpellBookPage = lastPage;
            }
            return lastPage;
        }
        
        public void jump( WMSSpellBookPage newPage )
        {
            // figure out what index the newPage is at
            int foundIndex = -1;
            for( int i = 0; i < script.length; i++ )
            {
                foundIndex = i;
                if( script[i] == newPage )
                    break;
            }
            currentIndex = foundIndex;
        }    
        

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public WMSSpellBookPage getCurrentSpellBookPage() {
        return currentSpellBookPage;
    }

    public void setCurrentSpellBookPage(WMSSpellBookPage currentSpellBookPage) {
        this.currentSpellBookPage = currentSpellBookPage;
    }
    
    
    private void readSpellBook( File spellBookFile )
    {
            try
            {
                xmlDocument = parser.parse( spellBookFile );
                NodeList nl = xmlDocument.getChildNodes();
                Node node = xmlDocument.getFirstChild();
                if( nl != null )
                {
                    // contine to walk the nodes looking for spell nodes
                    String spellBookNodeName = node.getNodeName();
                    if( spellBookNodeName.equals("SPELLBOOK"))
                    {
                        // we got the book, now see if there are any pages.                        
                        if( node.hasChildNodes() )
                        {                                                
                            NodeList spellBookNodeList = node.getChildNodes();
                            for( int c = 0; c < spellBookNodeList.getLength(); c++ )
                            {
                                Node spellBookPageNode = spellBookNodeList.item( c );
                                WMSSpellBookPage spellBookPage = new WMSSpellBookPage( this );
                                try
                                {
                                    spellBookPage.buildPage( spellBookPageNode );                                    
                                    if( spellBookPage.doesHaveSpells() )
                                    {                                        
                                        addPage( spellBookPage );
                                        if( currentSpellBookPage == null )
                                            currentSpellBookPage = spellBookPage;
                                    }
                                }
                                catch( WMSWizardException e )
                                {
                                    
                                }                                
                            }                            
                        }
                    }                                        
                }
                                              
            }
            catch ( org.xml.sax.SAXException ex )
            {
                System.out.println( "SAXException: " + ex.getMessage() );
                
            }
            catch( java.io.IOException exp )
            {
                System.out.println( "IOException: " + exp.getMessage() );
                
            }
    }
}
