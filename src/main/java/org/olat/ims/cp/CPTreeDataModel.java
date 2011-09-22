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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.ims.cp;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.tree.DefaultElement;
import org.json.JSONException;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeModel;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeNode;
import org.olat.core.util.Encoder;
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
public class CPTreeDataModel extends AjaxTreeModel {

	private ContentPackage cp;
	private Logger log;
	private Hashtable<String, String> map, reverseMap;

	private int nodeCounter;

	/**
	 * Constructor
	 * 
	 * @param orgaIdentifyer The identifier of the organization element of the
	 *          imsmanifest file.
	 * @param rootNodeId The root node id of the ext-js tree. (We use an md5 hash
	 *          to make sure it doesn't contain any weird characters.)
	 */
	public CPTreeDataModel(String orgaIdentifyer, String rootNodeId) {
		super(Encoder.encrypt(orgaIdentifyer));
		// initialize counter
		nodeCounter = 1;
		log = Logger.getLogger(this.getClass());
		map = new Hashtable<String, String>();
		reverseMap = new Hashtable<String, String>();
		map.put(rootNodeId, orgaIdentifyer);
		reverseMap.put(orgaIdentifyer, rootNodeId);
		// Set the cp icon to the root node
		setCustomRootIconCssClass("o_cp_org");
	}

	/**
	 * Sets the contentPackage, this model belongs to
	 * 
	 * @param cp
	 */
	public void setContentPackage(ContentPackage cp) {
		this.cp = cp;
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
		String identifier = map.get(nodeID);
		return identifier;
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

	@Override
	public List<AjaxTreeNode> getChildrenFor(String nodeId) {

		CPManagerImpl cpMgm = (CPManagerImpl) CPManager.getInstance();
		Vector<AjaxTreeNode> nodeList = new Vector<AjaxTreeNode>();

		nodeId = getIdentifierForNodeID(nodeId);
		DefaultElement el = cpMgm.getElementByIdentifier(cp, nodeId);
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
		} catch (JSONException e) {
			log.error("error while building treemodel");
		}

		return nodeList;
	}

	/**
	 * Adds the item to the node list. Also sets the necessary properties.
	 * 
	 * @param nodeList
	 * @param item
	 * @throws JSONException
	 */
	private void addItem(Vector<AjaxTreeNode> nodeList, CPItem item) throws JSONException {
		String nId;
		nId = putIdentifierForNodeID(item.getIdentifier());
		AjaxTreeNode child = new AjaxTreeNode(nId, item.getTitle());
		if (item.getItems().size() == 0) {
			// Expand the leaf in order to get rid of the plus sign in front of it.
			child.put(AjaxTreeNode.CONF_EXPANDED, true);
		} 
		child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, "o_cp_item");
		nodeList.add(child);
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
		StringBuffer path = new StringBuffer();
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
	private void addElementToPath(DefaultElement elem, StringBuffer path) {
		final String slash = "/";
		if (elem instanceof CPItem) {
			CPItem item = (CPItem) elem;
			path.insert(0, slash).insert(1, getNodeIDForIdentifier(item.getIdentifier()));
			DefaultElement parent = item.getParentElement();
			if (parent != null) addElementToPath(parent, path);
		} else if (elem instanceof CPOrganization) {
			CPOrganization item = (CPOrganization) elem;
			path.insert(0, slash).insert(1, getNodeIDForIdentifier(item.getIdentifier()));
		}
	}
}
