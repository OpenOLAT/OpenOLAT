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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package org.olat.repository.ui.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.InsertionPoint.Position;
import org.olat.core.gui.components.tree.InsertionTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.repository.CatalogEntry;

/**
 * Description:<br>
 * The catalog tree model implements an old-school selection tree model to move
 * or insert a catalog entry into a catalog. Dependin on your catalog size and
 * your browser it might be slow. In the ajax mode the new ajax version shold be
 * used.
 * <p>
 * Initial Date: 28.05.2008 <br>
 * 
 * @author BPS
 */
public class CatalogTreeModel extends GenericTreeModel implements InsertionTreeModel {

	private static final long serialVersionUID = 2893877155919419400L;
	
	private final Map<Long, GenericTreeNode> entryMap = new HashMap<>();
	private final CatalogEntry entryToMove;
	private final List<CatalogEntry> ownedEntries;

	/**
	 * Constructor
	 * 
	 * @param catEntryList The list of all entries, preloaded with one fetch
	 * @param entryToMove The entry to be moved or NULL to just select a catalog category
	 * @param ownedEntries A list of catalog categories that are manageable by the
	 *          current user or NULL if no such check should be performed
	 */
	public CatalogTreeModel(List<CatalogEntry> catEntryList, CatalogEntry entryToMove, List<CatalogEntry> ownedEntries) {
		this.entryToMove = entryToMove;
		this.ownedEntries = ownedEntries;
		setRootNode(buildTree(catEntryList));
	}

	@Override
	public boolean isSource(TreeNode node) {
		return entryToMove == node.getUserObject();
	}

	@Override
	public Position[] getInsertionPosition(TreeNode node) {
		if(getRootNode() == node || node.isAccessible()) {
			return new Position[] { Position.under };
		}
		return new Position[0];
	}

	/**
	 * Build a tree node from the given category entry
	 * @param catEntry
	 * @return
	 */
	private GenericTreeNode buildNode(CatalogEntry catEntry) {
		GenericTreeNode node = new GenericTreeNode();
		node.setAccessible(false);
		node.setIdent(catEntry.getKey().toString());
		node.setTitle(catEntry.getName());
		node.setUserObject(catEntry);
		entryMap.put(catEntry.getKey(), node);
		return node;
	}

	/**
	 * Build the selection tree from the given list of entries
	 * @param entryList
	 * @return The root node of the tree
	 */
	private GenericTreeNode buildTree(List<CatalogEntry> entryList) {
		GenericTreeNode rootNode = null;
		// build the tree - note that the entry list is not ordered in any way
		if(entryList != null) {	
			for (CatalogEntry elem :entryList) {
				// create a tree node and add it to the entry map
				GenericTreeNode n = addNode(elem);
				if (n != null) {
					// a node was created, keep it as a potential root node candidate
					rootNode = addNode(elem);
				}
			}
		}

		// walk to the final root node of the tree
		while (rootNode != null && rootNode.getParent() != null) {
			rootNode = (GenericTreeNode) rootNode.getParent();
		}
		
		//we always need a root 
		if(rootNode == null) {
			rootNode = new GenericTreeNode("keine Node :(", null);
		}

		calculateAccessibility(rootNode);
		return rootNode;
	}

	/**
	 * Create and add a node to the tree
	 * @param entry
	 * @return 
	 */
	private GenericTreeNode addNode(CatalogEntry entry) {
		GenericTreeNode node = entryMap.get(entry.getKey());

		if (node == null && entry.getType() == CatalogEntry.TYPE_NODE) {
			if (entry.getParent() != null) {
				node = entryMap.get(entry.getParent().getKey());
				if (node != null) {
					GenericTreeNode build = buildNode(entry);
					node.addChild(build);
					build.setParent(node);
				} else {
					addMissingNodes(entry);
				}
			} else {//it's the root
				node = buildNode(entry);
			}
		}
		return node;
	}

	/**
	 * Create the parent and child nodes that are not yet in the entry map
	 * 
	 * @param fault
	 */
	private void addMissingNodes(CatalogEntry fault) {
		GenericTreeNode branch = null;
		GenericTreeNode helper = null;

		while (fault != null) {
			if (entryMap.get(fault.getKey()) == null) {
				GenericTreeNode build = buildNode(fault);
				if (fault.getParent() != null) // in case of catalog root
				branch = entryMap.get(fault.getParent().getKey());
				if (helper != null) {
					helper.setParent(build);
					build.addChild(helper);
				}
				if (branch != null) {
					build.setParent(branch);
					branch.addChild(build);
					return;
				}
				helper = build;
			}
			fault = fault.getParent();
		}
	}

	/**
	 * Limit accessability to node to the nodes that are owned by the current user
	 */
	private void calculateAccessibility(GenericTreeNode rootNode) {
		GenericTreeNode node = null;
		// first : allow access to all children of the owned entries
		if (ownedEntries != null) {
			for (CatalogEntry entry : ownedEntries) {
				node = entryMap.get(entry.getKey());
				changeAccessibility(node, true);
			}
		}
		// second : reduce access for all children of the element you want to move (
		// to avoid circles and not dissolve elements from the catalog root )
		if (entryToMove != null && entryToMove.getType() == CatalogEntry.TYPE_NODE) {
			node = entryMap.get(entryToMove.getKey());
			changeAccessibility(node, false);
		}
		// when no node has to be moved (just a selection of any category) and no
		// entries are owned, we assume that all entries can be selected by user
		if (entryToMove == null && ownedEntries == null) {
			changeAccessibility(rootNode, true);
		}
	}

	/**	 
	 * Limit accessibility to the given node
	 * 
	 * @param node
	 * @param accessible
	 */
	private void changeAccessibility(GenericTreeNode node, boolean accessible) {
		if(node == null) return;
		
		if (accessible && ownedEntries != null) {
			node.setAccessible(accessible);
		}
		for (int cnt = 0; cnt < node.getChildCount(); cnt++) {
			changeAccessibility((GenericTreeNode) node.getChildAt(cnt), accessible);
		}
	}
}
