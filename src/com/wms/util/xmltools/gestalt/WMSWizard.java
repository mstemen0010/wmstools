package com.wms.util.xmltools.gestalt;

/*
 * GESWizard.java
 *
 * Created on May 18, 2006, 12:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import com.wms.util.xmltools.gestalt.WMSSpellBook;
import java.util.*;
import javax.swing.JPanel;


/**
 *
 * @author mstemen
 */
public class WMSWizard implements WMSSpellCauldronInterface
{
    
/*
 * ParallaxWizardObject.java
 *
 * Created on May 10, 2005, 12:49 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author mstemen
 */
    

    private WMSSpellBook spellBook;
    private WMSSpellBook.GESSpellBookType spellBookType = WMSSpellBook.GESSpellBookType.NONE;
    private WMSSpellCauldron cauldron;
    
    /**
     * Creates a new instance of ParallaxWizardObject 
     */
    public WMSWizard( WMSSpellBook.GESSpellBookType spellBookType )
    {
        cauldron = new WMSSpellCauldron();
        spellBook = new WMSSpellBook( "test", this );
        this.spellBookType = spellBookType;
        build();
    }        
    
    public WMSWizard()
    {
        spellBook = new WMSSpellBook( "test", this );
        cauldron = new WMSSpellCauldron();
    }
    
    public void add( String key, String value )
    {
        cauldron.put( key, value );
    }
    
    public void setScriptType( WMSSpellBook.GESSpellBookType newType )
    {
        this.spellBookType = newType;
        build();
    }
    
    public void jumpToPage( WMSSpellBookPage newPage )
    {
        spellBook.jump(newPage);
    }
    
    public void setPanel( JPanel newPanel, WMSSpellBookPage scriptPage )
    {   
        spellBook.setScriptPagePanel( newPanel, scriptPage );
    }
    
    public JPanel getPanel( WMSSpellBookPage scriptPage )
    {
        return spellBook.getScriptPagePanel( scriptPage );
    }
    
    public void setTitle( String title, WMSSpellBookPage page )
    {
        spellBook.setPageTitle( title, page );
    }
    
    public String getTitle( WMSSpellBookPage page )
    {
        return spellBook.getPageTitle(page);
    }
    
    public String getTitle()
    {
        return spellBook.getPageTitle( spellBook.getCurrentSpellBookPage() );
    }
   
    public JPanel getPanel()
    {
        return spellBook.getScriptPagePanel( spellBook.getCurrentSpellBookPage() );
    }
    
    public int getIndex()
    {
        return spellBook.getCurrentIndex();
    }
    
    public WMSSpellBookPage getCurrentPage()
    {
        return spellBook.getCurrentSpellBookPage();
    }
    
    public int getIndex( WMSSpellBookPage page )
    {
        return spellBook.getScriptIndex( page );
    }    
    
    public void next()
    {
        spellBook.next();
    }
    
    public void last()
    {
        spellBook.last();
    }
    
    private void build()
    {
        // use the scriptType to build a wizard script
        /*
        ArrayList <GESSpellBookPage> newScript = new ArrayList<GESSpellBookPage>();
        switch( scriptType )
        {
            case FullConfigConsolidation:
                // newScript.add( ParallaxTypes.ScriptPage.Intro );
                newScript.add( GESSpellBookPage.PickScenario );
                newScript.add( GESSpellBookPage.AddServer );
                newScript.add( GESSpellBookPage.AddMediaMgr );
                newScript.add( GESSpellBookPage.AddRealLibs );
                newScript.add( GESSpellBookPage.AddVirtLib );
                newScript.add( GESSpellBookPage.AddVirtDrives );                
                newScript.add( GESSpellBookPage.AssocRealTapes );                
                break;
                
            case FullConfigPartition:
                // newScript.add( ParallaxTypes.ScriptPage.Intro );
                newScript.add( GESSpellBookPage.PickScenario );
                newScript.add( GESSpellBookPage.AddServer );
                newScript.add( GESSpellBookPage.AddMediaMgr );
                newScript.add( GESSpellBookPage.AddRealLib );
                newScript.add( GESSpellBookPage.AddVirtLibs );
                newScript.add( GESSpellBookPage.AddVirtDrives );                
                newScript.add( GESSpellBookPage.AssocRealTapes );                                
                break;
                
            default:
                break;
        }
        script.setScript( newScript);        
         */
    }                
}
