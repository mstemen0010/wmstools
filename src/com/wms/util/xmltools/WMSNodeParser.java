/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wms.util.xmltools;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author mstemen
 */
public class WMSNodeParser
{

    private Document xmlDocument = null;
    private ParserAdapter pa = null;
    private DocumentBuilder parser = null;
    private String key = null;
    private String fileNameToParse = null;
    private NodeList parsedNodes = null;
    private InputSource inSrc = null;
    private Schema xsdSchema;
    private boolean targetNodeFound = false;

    public WMSNodeParser(File xsdSchemaFile, File xmlFileToParse, String targetNodeName)
    {
        // try and construct the Schema

        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            if (xsdSchemaFile != null) {
                xsdSchema = schemaFactory.newSchema(xsdSchemaFile);
                factory.setSchema(xsdSchema);
            }

            parser = factory.newDocumentBuilder();
        }
        catch (SAXException ex) {
            Logger.getLogger(WMSNodeParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ParserConfigurationException e) {
            System.out.println("ParserConfigException: " + e.getMessage());
        }

        parse(xmlFileToParse, targetNodeName);

    }

    public Document getDoc()
    {
        return xmlDocument;
    }

    private void parse(File fileName, String targetNodeName)
    {
        targetNodeFound = false;
        try {

            xmlDocument = parser.parse(fileName);
            while (!targetNodeFound) {
                NodeList nl = xmlDocument.getChildNodes();
                Node node = xmlDocument.getFirstChild();
                String name = node.getNodeName();
                if (nl != null) {
                    for (int i = 0; i < nl.getLength(); i++) {
                        Node nextNode = nl.item(i);
                        String nodeName = nextNode.getNodeName();
                        if (nodeName.equals(targetNodeName)) {
                            System.out.println("Node name:" + nodeName);
                            targetNodeFound = true;
                            this.parsedNodes = nl;
                            break;
                        }
                    }
                }
            }
        }
        catch (SAXException ex) {
            Logger.getLogger(WMSNodeParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(WMSNodeParser.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
    // get a sub set of nodes from the current node list





    public boolean targetNodeFound()
    {
        return this.targetNodeFound;
    }

    public NodeList getList( String tagName )
    {
        NodeList foundNodes = null;

        foundNodes = this.xmlDocument.getElementsByTagName(tagName);

        return foundNodes;
    }
//    private void readSpellBook( File fileName )
//    {
//            try
//            {
//                xmlDocument = parser.parse( fileName );
//                NodeList nl = xmlDocument.getChildNodes();
//                Node node = xmlDocument.getFirstChild();
//                if( nl != null )
//                {
//                    // contine to walk the nodes looking for spell nodes
//                    String spellBookNodeName = node.getNodeName();
//                    if( spellBookNodeName.equals("SPELLBOOK"))
//                    {
//                        // we got the book, now see if there are any pages.
//                        if( node.hasChildNodes() )
//                        {
//                            NodeList spellBookNodeList = node.getChildNodes();
//                            for( int c = 0; c < spellBookNodeList.getLength(); c++ )
//                            {
//                                Node spellBookPageNode = spellBookNodeList.item( c );
//                                WMSSpellBookPage spellBookPage = new WMSSpellBookPage( this );
//                                try
//                                {
//                                    spellBookPage.buildPage( spellBookPageNode );
//                                    if( spellBookPage.doesHaveSpells() )
//                                    {
//                                        addPage( spellBookPage );
//                                        if( currentSpellBookPage == null )
//                                            currentSpellBookPage = spellBookPage;
//                                    }
//                                }
//                                catch( GESWizardException e )
//                                {
//
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }
//            catch ( org.xml.sax.SAXException ex )
//            {
//                System.out.println( "SAXException: " + ex.getMessage() );
//
//            }
//            catch( java.io.IOException exp )
//            {
//                System.out.println( "IOException: " + exp.getMessage() );
//
//            }
//    }
}
