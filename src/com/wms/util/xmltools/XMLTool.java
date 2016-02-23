package com.wms.util.xmltools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Matthew_G_Stemen
 *
 * This was written originally to be used server side parsing through WBEM/CIM elements (around 2010)
 * this was very useful in a large project that was WBEM based and there was assumptions being made about node position and containment that was not actually true or constant
 * this code was used to find target nodes despite these inconsistencies 
 */
public class XMLTool {

	private enum WalkType {

		ElementName,
		NameSpaceURI,
		ElementValue;
	}
	private List<Node> nodeList = new ArrayList<Node>();
	LinkedHashMap<String, String> namespaces = new LinkedHashMap<String, String>();


	public static String findValueItTree(Document docToWalk) {
		String valueFound = "";


		return valueFound;
	}

	private Node walkTreeFindNode(String nameName) {
		Node nodeFound = null;


		return nodeFound;
	}

	public void put(String prefix, String uri) {
		this.namespaces.put(prefix, uri);
	}

	private void walk(Node node, WalkType walkType) //walk the DOM tree and set name-spaces to prefixes for all nodes in Doc
	{
		int type = node.getNodeType();
		nodeList.add(node);
		String nameSpaceName = node.getNamespaceURI();
		String prefixName = node.getPrefix();


		switch (type) {
			case Node.DOCUMENT_NODE: {
				break;
			}//end of document
			case Node.ELEMENT_NODE: {
				NamedNodeMap nnm = node.getAttributes();
				if (nnm != null) {
					int len = nnm.getLength();
					Attr attr;
					for (int i = 0; i < len; i++) {
						attr = (Attr) nnm.item(i);
					}
				}
				break;

			}//end of element
			case Node.ENTITY_REFERENCE_NODE: {
				break;

			}//end of entity
			case Node.CDATA_SECTION_NODE: {
				break;

			}
			case Node.TEXT_NODE: {
				break;
			}
			case Node.PROCESSING_INSTRUCTION_NODE: {
				String data = node.getNodeValue();
				break;

			}
		}//end of switch

		// based on walk type, do something
		switch (walkType) {
			case NameSpaceURI:
				put(nameSpaceName, prefixName);
		}


		//recurse
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			walk(child,walkType );
		}

		//without this the ending tags will miss
		if (type == Node.ELEMENT_NODE) {
		}



	}//end of walk
}
