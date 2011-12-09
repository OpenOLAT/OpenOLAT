/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.catalog.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeModel;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeNode;
import org.olat.core.logging.OLATRuntimeException;

/**
 * Description:<br>
 * This class represents the catalog data model that can be used with the ajax
 * tree controller. It supports dynamic loading of catalog elements and can be
 * configured to display only the categories or the categories and the children.
 * <p>
 * The datamodel can also support drag&drop. In this mode, the categories act as
 * drop zones for categories and leafs.
 * 
 * 
 * <P>
 * Initial Date: 30.05.2008 <br>
 * 
 * @author gnaegi
 */
public class CatalogAjaxTreeModel extends AjaxTreeModel {
	private boolean showLeafs;
	private boolean allowDragNdrop;
	private CatalogEntry toBeMovedEntry;
	private List<CatalogEntry> ownedEntries;
	
	/**
	 * Constructor for a ajax tree catalog data model
	 * 
	 * @param rootEntry The catalog root entry
	 * @param toBeMovedEntry The entry what should be moved or NULL if no such
	 *          entry. If not null this entry will not be displayed in the tree
	 * @param ownedEntries List of entries (categories) that are owned by the
	 *          current user. In category move operations the target category will
	 *          be checked against this list. Can be NULL
	 * @param showLeafs true: show also leafs in the tree; false: show only
	 *          categories
	 * @param allowDragNdrop true: enable drag&dop of categories and leafs; false:
	 *          don't use drag&drop
	 */
	public CatalogAjaxTreeModel(CatalogEntry rootEntry, CatalogEntry toBeMovedEntry, List<CatalogEntry> ownedEntries, boolean showLeafs, boolean allowDragNdrop) {
		super(rootEntry.getKey().toString());
		this.toBeMovedEntry = toBeMovedEntry;
		this.ownedEntries = ownedEntries;
		this.showLeafs = showLeafs;
		this.allowDragNdrop = allowDragNdrop;
	}

	/**
	 * @see org.olat.core.gui.control.generic.ajax.tree.TreeDataModel#getChildrenFor(java.lang.String)
	 */
	@Override
	public List<AjaxTreeNode> getChildrenFor(String nodeId) {
		List<AjaxTreeNode> childNodes = new ArrayList<AjaxTreeNode>();
		CatalogManager cm = CatalogManager.getInstance();
		// load node with given id
		Long entryKey = Long.parseLong(nodeId);
		CatalogEntry entry = cm.loadCatalogEntry(entryKey);
		// load children of node and add them to the list 
		List<CatalogEntry> childEntries = cm.getChildrenOf(entry);
		for (CatalogEntry childEntry : childEntries) {
			// don't add the to be moved child itself!
			if (toBeMovedEntry != null && toBeMovedEntry.getKey().equals(childEntry.getKey())) {
				continue;
			}			
			// Add children of type node and children of type leaf only when leafs
			// should be shown
			if (childEntry.getType() == CatalogEntry.TYPE_NODE || showLeafs) {
				AjaxTreeNode childNode = buildNode(childEntry);
				childNodes.add(childNode);
			}
		}
		return childNodes;
	}

	/**
	 * Internal helper to build a tree node from a catalog entry
	 * @param catalogEntry
	 * @return 
	 */
	private AjaxTreeNode buildNode(CatalogEntry catalogEntry) {		
		AjaxTreeNode node;
		try {
			node = new AjaxTreeNode(catalogEntry.getKey().toString(), catalogEntry.getName());
			// Setting some node attributes - see the Treenode or the extjs
			// documentation on what else you could use
			node.put(AjaxTreeNode.CONF_LEAF, (catalogEntry.getType() == CatalogEntry.TYPE_LEAF));
			if (catalogEntry.getType() == CatalogEntry.TYPE_NODE) {
				// set different css class depending on access rights
				CatalogManager cm = CatalogManager.getInstance();			
				if (ownedEntries != null && !cm.isEntryWithinCategory(catalogEntry, ownedEntries)) {
					node.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, "o_catalog_cat_noaccess_icon");
				} else {
					node.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, "o_catalog_cat_icon");					
				}
				// Check if node is expandable. ignore this check in d&d mode,
				// otherwhise those categories could not act as drop zones
				if (!allowDragNdrop && !cm.hasChildEntries(catalogEntry, CatalogEntry.TYPE_NODE)) {
					node.put(AjaxTreeNode.CONF_LEAF, true);
				}
				
			} else {
				node.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, "o_catalog_entry_icon");
			}
			// use description as hoover text
			node.put(AjaxTreeNode.CONF_QTIP, catalogEntry.getDescription());
			// dragging for leafs and nodes, but only when configured
			node.put(AjaxTreeNode.CONF_ALLOWDRAG, allowDragNdrop);
			// top zones only for nodes
			node.put(AjaxTreeNode.CONF_ALLOWDROP, (allowDragNdrop && catalogEntry.getType() == CatalogEntry.TYPE_NODE));
		} catch (JSONException e) {
			throw new OLATRuntimeException("Error while creating AjaxTreeNode for catalog entry::" + catalogEntry, e);
		}
		
		return node;
	}

}
