/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.cp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.resources.IMSEntityResolver;

/**
*  Description:<br>
* @author Felix Jost
*/
public class CPManifestTreeModel extends GenericTreeModel {

	private Element rootElement;
	private final Map<String,String>	nsuris = new HashMap<String,String>(2);
	private final Map<String,TreeNode> hrefToTreeNode = new HashMap<String,TreeNode>();
	private Map<String,String> resources; // keys: resource att 'identifier'; values: resource att 'href'
	private final List<TreeNode> treeNodes = new ArrayList<TreeNode>();

	/**
	 * Constructor of the content packaging tree model
	 * @param manifest the imsmanifest.xml file
	 */
	CPManifestTreeModel(VFSLeaf manifest) {
		Document doc = loadDocument(manifest);
		// get all organization elements. need to set namespace
		rootElement = doc.getRootElement();
		String nsuri = rootElement.getNamespace().getURI();
		nsuris.put( "ns", nsuri);

		XPath meta = rootElement.createXPath("//ns:organization");
		meta.setNamespaceURIs(nsuris);
		Element orgaEl = (Element) meta.selectSingleNode(rootElement); // TODO: accept several organizations?
		if (orgaEl == null) throw new AssertException("could not find element organization");

		XPath metares = rootElement.createXPath("//ns:resources");
		metares.setNamespaceURIs(nsuris);
		Element elResources = (Element) metares.selectSingleNode(rootElement);
		if (elResources == null) throw new AssertException("could not find element resources");
		
		List resourcesList = elResources.elements("resource");
		resources = new HashMap<String,String>(resourcesList.size());
		for (Iterator iter = resourcesList.iterator(); iter.hasNext();) {
			Element elRes = (Element) iter.next();
			String identVal = elRes.attributeValue("identifier");
			String hrefVal = elRes.attributeValue("href");
			if (hrefVal != null) { // href is optional element for resource element
				try {
					hrefVal = URLDecoder.decode(hrefVal, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// each JVM must implement UTF-8
				} 
			}
			resources.put(identVal, hrefVal);
		}
		GenericTreeNode gtn = buildNode(orgaEl);
		setRootNode(gtn);
		rootElement = null; // help gc
		resources = null;
	}
	
	/**
	 * @param href
	 * @return TreeNode representing this href
	 */
	public TreeNode lookupTreeNodeByHref(String href) {
		return hrefToTreeNode.get(href);
	}
	
	//fxdiff VCRP-13: cp navigation
	public List<TreeNode> getFlattedTree() {
		return new ArrayList<TreeNode>(treeNodes);
	}
	
	//fxdiff VCRP-13: cp navigation
	public TreeNode getNextNodeWithContent(TreeNode node) {
		if(node == null) return null;
		int index = treeNodes.indexOf(node);
		
		for(int i=index+1; i<treeNodes.size(); i++) {
			TreeNode nextNode = treeNodes.get(i);
			if(nextNode.getUserObject() != null) {
				return nextNode;
			}
		}
		
		return null;
	}
	
	public TreeNode getPreviousNodeWithContent(TreeNode node) {
		if(node == null) return null;
		int index = treeNodes.indexOf(node);
		
		for(int i=index; i-->0; ) {
			TreeNode nextNode = treeNodes.get(i);
			if(nextNode.getUserObject() != null) {
				return nextNode;
			}
		}

		return null;
	}

	private GenericTreeNode buildNode(Element item) {
		GenericTreeNode gtn = new GenericTreeNode();
		//fxdiff VCRP-13: cp navigation
		treeNodes.add(gtn);

		// extract title
		String title = item.elementText("title");
		if (title == null) title = item.attributeValue("identifier");
		gtn.setAltText(title);
		gtn.setTitle(title);
		
		if (item.getName().equals("organization")) {
			gtn.setIconCssClass("o_cp_org");
			gtn.setAccessible(false);
		} else if (item.getName().equals("item")) {
			gtn.setIconCssClass("o_cp_item");
			//set resolved file path directly
			String identifierref = item.attributeValue("identifierref");
			XPath meta = rootElement.createXPath("//ns:resource[@identifier='" + identifierref + "']");
			meta.setNamespaceURIs(nsuris);
			String href = resources.get(identifierref);
			if (href != null) {
				gtn.setUserObject(href);
				// allow lookup of a treenode given a href so we can quickly adjust the menu if the user clicks on hyperlinks within the text
				hrefToTreeNode.put(href, gtn);
			} 
			else gtn.setAccessible(false);
		}
		
		List chds = item.elements("item");
		int childcnt = chds.size();
		for (int i = 0; i < childcnt; i++) {
			Element childitem = (Element) chds.get(i);
			GenericTreeNode gtnchild = buildNode(childitem);
			gtn.addChild(gtnchild);
		}
		return gtn;
	}
	
	private Document loadDocument(VFSLeaf documentF) {
		InputStream in = null;
		Document doc = null;
		try {
			in = documentF.getInputStream();
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			doc = xmlParser.parse(in, false);
			in.close();
		}
		catch (IOException e) {
			throw new OLATRuntimeException(CPManifestTreeModel.class, "could not read and parse from file " + documentF, e);
		}
		finally {
			try {
				if (in != null)
					in.close();
			}
			catch (Exception e) {
				// we did our best to close the inputStream
			}
		}
		return doc;
	}

}
