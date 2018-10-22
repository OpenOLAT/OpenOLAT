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
* <p>
*/

package org.olat.ims.cp;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.olat.core.gui.components.tree.DnDTreeModel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.ims.cp.objects.CPItem;
import org.olat.ims.cp.objects.CPOrganization;

/**
 * 
 * Description:<br>
 * This is the TreeDataModel which is used for the GUI. (Visual Representation
 * of the manifest-data-model)
 * 
 * <P>
 * Initial Date: 23.07.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPTreeDataModel extends GenericTreeModel implements DnDTreeModel {

	private static final long serialVersionUID = -6843143820668185636L;
	private static final OLog log = Tracing.createLoggerFor(CPTreeDataModel.class);
	
	private final ContentPackage cp;
	private Map<String, String> map;
	private Map<String, String> reverseMap;

	private int nodeCounter;
	private final String rootNodeId;

	/**
	 * Constructor
	 * 
	 * @param orgaIdentifyer The identifier of the organization element of the
	 *          imsmanifest file.
	 * @param rootNodeId The root node id of the ext-js tree. (We use an md5 hash
	 *          to make sure it doesn't contain any weird characters.)
	 */
	public CPTreeDataModel(String orgaIdentifyer, String rootNodeId, ContentPackage cp) {
		this.cp = cp;
		this.rootNodeId = rootNodeId;
		nodeCounter = 1;
		map = new Hashtable<>();
		reverseMap = new Hashtable<>();
		map.put(rootNodeId, orgaIdentifyer);
		reverseMap.put(orgaIdentifyer, rootNodeId);
		update();
	}

	/**
	 * Node-ids in the datamodel must be integers This method returns the next
	 * nodeID (incremental)
	 * 
	 * @return
	 */
	private String getNextNodeID() {
		return Integer.toString(nodeCounter++);
	}

	/**
	 * manifest-node-identifiers are mapped to tree-model-node-id's Returns the
	 * corresponding identifier for the given nodeID
	 * 
	 * @param nodeID
	 * @return
	 */
	public String getIdentifierForNodeID(String nodeID) {
		return map.get(nodeID);
	}

	/**
	 * manifest-node-identifiers are mapped to tree-model-node-id's Returns the
	 * corresponding identifier for the given nodeID
	 * 
	 * @param nodeID
	 * @return
	 */
	public String getNodeIDForIdentifier(String identifier) {
		String nodeId = reverseMap.get(identifier);
		if (nodeId == null) {
			nodeId = putIdentifierForNodeID(identifier);
		}
		return nodeId;
	}

	/**
	 * gets a new nodeID for the identifier and puts the key/value pair into the
	 * map Returns the new nodeID
	 * 
	 * @param identifier
	 * @return
	 */
	private String putIdentifierForNodeID(String identifier) {
		String nodeId = reverseMap.get(identifier);
		if (nodeId == null) {
			nodeId = getNextNodeID();
			map.put(nodeId, identifier);
			reverseMap.put(identifier, nodeId);
		}
		return nodeId;
	}
	
	public void update() {
		String realNodeId = getIdentifierForNodeID(rootNodeId);
		DefaultElement el = cp.getElementByIdentifier(realNodeId);
		if(el instanceof CPOrganization) {
			CPOrganization item = (CPOrganization)el;
			String rootTitle = cp.getFirstOrganizationInManifest().getTitle();
			GenericTreeNode rootNode = new GenericTreeNode(rootNodeId, rootTitle, item);
			rootNode.setIconCssClass("o_cp_org");
			
			List<TreeNode> children = getChildrenFor(rootNodeId);
			for(TreeNode child:children) {
				rootNode.addChild(child);
				buildTreeModel(child);
			}
			
			setRootNode(rootNode);
		}
	}
	
	public void buildTreeModel(TreeNode node) {
		List<TreeNode> children = getChildrenFor(node.getIdent());
		for(TreeNode child:children) {
			node.addChild(child);
			buildTreeModel(child);
		}
	}
	
	public List<TreeNode> getChildrenFor(String nodeId) {
		List<TreeNode> nodeList = new ArrayList<>();
		nodeId = getIdentifierForNodeID(nodeId);
		DefaultElement el = cp.getElementByIdentifier(nodeId);
		if (el == null) {
			log.info("element not found (id " + nodeId + ")");
			return nodeList;
		}
		try {
			if (el.getName().equals(CPCore.ORGANIZATION)) {
				CPOrganization org = (CPOrganization) el;
				for (Iterator<CPItem> it = org.getItemIterator(); it.hasNext();) {
					CPItem item = it.next();
					addItem(nodeList, item);
				}
			} else if (el.getName().equals(CPCore.ITEM)) {
				CPItem pItem = (CPItem) el;
				for (Iterator<CPItem> it = pItem.getItemIterator(); it.hasNext();) {
					CPItem item = it.next();
					addItem(nodeList, item);
				}
			} else {
				// element is not item nor orga -> ergo wrong element
				log.info("unknown element while building treemodel for gui (id: " + nodeId + ")");
			}
		} catch (Exception e) {
			log.error("error while building treemodel");
		}

		return nodeList;
	}
	
	private void addItem(List<TreeNode> nodeList, CPItem item) {
		String nId = putIdentifierForNodeID(item.getIdentifier());
		GenericTreeNode child = new GenericTreeNode(nId, item.getTitle(), item);
		child.setIconCssClass("o_cp_item");
		nodeList.add(child);
	}
	
	@Override
	public boolean isNodeDroppable(TreeNode node) {
		return true;
	}

	@Override
	public boolean isNodeDraggable(TreeNode node) {
		return !rootNodeId.equals(node.getIdent());
	}

	/**
	 * Returns the path of the given item in the tree.
	 * <p>
	 * Example: /0/12/3
	 * <p>
	 * The empty string signifies the root node.
	 * 
	 * @param identifier The identifier of the page
	 * @return The path to the node in the tree
	 */
	public String getPath(String identifier) {
		StringBuilder path = new StringBuilder(128);
		DefaultElement elem = cp.getElementByIdentifier(identifier);
		if (elem instanceof CPOrganization) {
			// Special case. Somehow, the root path should be an empty string.
			path.append("");
		} else {
			addElementToPath(elem, path);
		}
		return path.toString();
	}

	/**
	 * @param identifier
	 * @param path
	 * @param slash
	 * @param elem
	 */
	private void addElementToPath(Element elem, StringBuilder path) {
		final String slash = "/";
		if (elem instanceof CPItem) {
			CPItem item = (CPItem) elem;
			path.insert(0, slash).insert(1, getNodeIDForIdentifier(item.getIdentifier()));
			Element parent = item.getParentElement();
			if (parent != null) addElementToPath(parent, path);
		} else if (elem instanceof CPOrganization) {
			CPOrganization item = (CPOrganization) elem;
			path.insert(0, slash).insert(1, getNodeIDForIdentifier(item.getIdentifier()));
		}
	}

	public void removePath() {
		update();
	}
}
