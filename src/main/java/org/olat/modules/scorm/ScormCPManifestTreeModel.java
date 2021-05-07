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

package org.olat.modules.scorm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.resources.IMSEntityResolver;

/**
* @author Felix Jost
*/
public class ScormCPManifestTreeModel extends GenericTreeModel {

	private static final long serialVersionUID = 1L;
	
	private Element rootElement;
	private Map<String,String>	nsuris = new HashMap<>(2);
	private Map<String,GenericTreeNode> hrefToTreeNode = new HashMap<>();
	private Map<String,String> resources; // keys: resource att 'identifier'; values: resource att 'href'
	private int nodeId = 0;
	//number tree ascending in depth first traversal. Keys: GenericTreeNode | values: int
	private Map<GenericTreeNode,Integer> nodeToId = new HashMap<>();
	private Map<String,GenericTreeNode> scormIdToNode = new HashMap<>();
	private Map<String,String> itemStatus;

	/**
	 * Constructor of the content packaging tree model
	 * @param manifest the imsmanifest.xml file
	 * @param itemStatus a Map containing the status of each item like "completed, not attempted, ..."
	 */
	public ScormCPManifestTreeModel(File manifest, Map<String,String> itemStatus) {
		this.itemStatus = itemStatus;
		Document doc = loadDocument(manifest);
		// get all organization elements. need to set namespace
		rootElement = doc.getRootElement();
		String nsuri = rootElement.getNamespace().getURI();
		nsuris.put( "ns", nsuri);

		XPath meta = rootElement.createXPath("//ns:organization");
		meta.setNamespaceURIs(nsuris);

		XPath metares = rootElement.createXPath("//ns:resources");
		metares.setNamespaceURIs(nsuris);
		Element elResources = (Element) metares.selectSingleNode(rootElement);
		if (elResources == null) throw new AssertException("could not find element resources");
		
		List<Element> resourcesList = elResources.elements("resource");
		resources = new HashMap<>(resourcesList.size());
		for (Iterator<Element> iter = resourcesList.iterator(); iter.hasNext();) {
			Element elRes = iter.next();
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
		/*
		 * Get all organizations
		 */
		List<Node> organizations = meta.selectNodes(rootElement);
		if (organizations.isEmpty()) {
			throw new AssertException("could not find element organization");
		}
		GenericTreeNode gtn = buildTreeNodes(organizations);
		setRootNode(gtn);
		rootElement = null; // help gc
		resources = null;
	}
	
	public int size() {
		return nodeToId.size();
	}
	
	/**
	 * @param href
	 * @return TreeNode representing this href
	 */
	public TreeNode lookupTreeNodeByHref(String href) {
		return hrefToTreeNode.get(href);
	}

	private GenericTreeNode buildTreeNodes(List<Node> organizations) {
		GenericTreeNode gtn = new GenericTreeNode();
		// 0 is a valid index since List is testet be be not empty above
		String rootNode = organizations.get(0).getParent().elementText("default");
		// if only one organization avoid too much hierarchy levels...
		if (organizations.size() == 1) {
			return buildNode((Element)organizations.get(0));
		}
		// FIXME: localize "Content:"
		gtn.setTitle((rootNode == null) ? "Content:" : rootNode);
		gtn.setIconCssClass("o_scorm_org");
		gtn.setAccessible(false);
		
		for (int i = 0; i < organizations.size(); ++i) {
			GenericTreeNode gtnchild = buildNode((Element)organizations.get(i));
			gtn.addChild(gtnchild);
		}
		return gtn;
	}

	private GenericTreeNode buildNode(Element item) {
		GenericTreeNode treeNode = new GenericTreeNode();
		
		// extract title
		String title = item.elementText("title");
		if (title == null) title = item.attributeValue("identifier");
		treeNode.setAltText(title);
		treeNode.setTitle(title);
		
		if (item.getName().equals("organization")) {
			treeNode.setIconCssClass("o_scorm_org");
			treeNode.setAccessible(false);
		} else if (item.getName().equals("item")) {
			scormIdToNode.put(Integer.toString(nodeId), treeNode);
			nodeToId.put(treeNode, Integer.valueOf(nodeId));
			
			//set node images according to scorm sco status
			String itemStatusDesc = itemStatus.get(Integer.toString(nodeId));
			treeNode.setIconCssClass("o_scorm_item");
			if(itemStatusDesc != null && !"not_attempted".equals(itemStatusDesc)) {
				// add icon decorator for current status
				String decorator = "o_scorm_" + itemStatusDesc;
				treeNode.setIconDecorator1CssClass(decorator);
			}
			
			nodeId++;
			//set resolved file path directly
			String identifierref = item.attributeValue("identifierref");
			XPath meta = rootElement.createXPath("//ns:resource[@identifier='" + identifierref + "']");
			meta.setNamespaceURIs(nsuris);
			String href = resources.get(identifierref);
			if (href != null) {
				treeNode.setUserObject(href);
				// allow lookup of a treenode given a href so we can quickly adjust the menu if the user clicks on hyperlinks within the text
				hrefToTreeNode.put(href, treeNode);
			} 
			else treeNode.setAccessible(false);
		}
		
		List<Element> chds = item.elements("item");
		int childcnt = chds.size();
		for (int i = 0; i < childcnt; i++) {
			Element childitem = chds.get(i);
			GenericTreeNode gtnchild = buildNode(childitem);
			treeNode.addChild(gtnchild);
		}
		return treeNode;
	}
	
	private Document loadDocument(File documentF) {
		Document doc = null;
		try(FileInputStream in = new FileInputStream(documentF);
				BufferedInputStream bis = new BufferedInputStream(in)) {
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			doc = xmlParser.parse(bis, false);
		} catch (IOException e) {
			throw new OLATRuntimeException(ScormCPManifestTreeModel.class, "could not read and parse from file "+documentF.getAbsolutePath(),e);
		}
		return doc;
	}

	/**
	 * @param treeNode
	 * @return int
	 */
	public int lookupScormNodeId(TreeNode treeNode){
		Integer nodeInteger = nodeToId.get(treeNode);
		return nodeInteger.intValue();
	}

	/**
	 * @param itemId
	 * @return an uri that points to the ressource identified by a flat id
	 */
	public TreeNode getNodeByScormItemId(String itemId) {
		return scormIdToNode.get(itemId);
	}
	
	/**
	 * @return a map with key->ascending flat string number from traversing tree. value->TreeNode 
	 */
	public Map<String,GenericTreeNode> getScormIdToNodeRelation(){
		return scormIdToNode;
	}
}
