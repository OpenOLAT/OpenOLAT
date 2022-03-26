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

package org.olat.core.gui.components.tree;


import static org.olat.core.gui.components.velocity.VelocityContainer.COMMAND_ID;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.tree.InsertionPoint.Position;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.INodeFilter;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class MenuTree extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new MenuTreeRenderer();

	/**
	 * Comment for <code>NODE_IDENT</code>
	 */
	public static final String NODE_IDENT = "nidle";
	
	/**
	 * Comment for <code>NODE_IDENT</code>
	 */
	public static final String TARGET_NODE_IDENT = "tnidle";
	
	
	/**
	 * Comment for <code>NODE_IDENT</code>
	 */
	public static final String SIBLING_NODE = "sne";
	
	/**
	 * Comment for <code>NODE_IDENT</code>
	 */
	public static final String COMMAND_TREENODE = "ctntr";

	/**
	 * Comment for <code>NODE_IDENT</code>
	 */
	public static final String TREENODE_OPEN = "open";
	
	/**
	 * Comment for <code>NODE_IDENT</code>
	 */
	public static final String TREENODE_CLOSE = "close";
	
	/**
	 * event fired when a treenode was clicked (all leaf nodes)
	 */
	public static final String COMMAND_TREENODE_CLICKED = "ctncl";
	
	/**
	 * Command to insert new in the tree
	 */
	public static final String COMMAND_TREENODE_INSERT_UP = "iup";
	public static final String COMMAND_TREENODE_INSERT_DOWN = "idown";
	public static final String COMMAND_TREENODE_INSERT_UNDER = "iunder";
	public static final String COMMAND_TREENODE_INSERT_REMOVE = "irm";
	
	/**
	 * event fired when a treenode was expanded (all nodes except leafs)
	 */
	public static final String COMMAND_TREENODE_EXPANDED = "ctnex";
	
	/**
	 * event fired when a treenode is dropper
	 */
	public static final String COMMAND_TREENODE_DROP = "ctdrop";
	
	protected static final DefaultFilter DEF_FILTER = new DefaultFilter();

	private TreeModel treeModel;
	private InsertionPoint insertionPoint;
	private String selectedNodeId = null;
	private final Set<String> selectedNodeIds = new HashSet<>();
	private final Set<String> openNodeIds = new HashSet<>();
	private boolean expandServerOnly = true; // default is serverside menu
	private boolean dragEnabled = false;
	private boolean dropEnabled = false;
	private boolean dropSiblingEnabled = false;
	private boolean expandSelectedNode = true;
	private boolean rootVisible = true;
	private boolean unselectNodes;
	private boolean showInsertTool;
	private boolean multiSelect;
	private boolean scrollTopOnClick;
	private String dndAcceptJSMethod = "treeAcceptDrop_notWithChildren";

	private boolean dirtyForUser = false;
	
	private INodeFilter filter = DEF_FILTER;
	private MenuTreeItem menuTreeItem;
	
	/**
	 * @param name
	 */
	public MenuTree(String name) {
		super(null, name);
	}
	
	public MenuTree(String id, String name) {
		super(id, name);
	}
	
	/**
	 * @param id Fix unique identifier for state-less behavior
	 * @param name
	 * @param eventListener
	 */
	public MenuTree(String id, String name, ComponentEventListener eventListener) {
		super(id, name);
		addListener(eventListener);
	}
	
	MenuTree(String id, String name, ComponentEventListener eventListener, MenuTreeItem menuTreeItem) {
		this(id, name, eventListener);
		this.menuTreeItem = menuTreeItem;
	}
	
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		
		if(isDragEnabled() || isDropEnabled() || isDropSiblingEnabled()) {
			JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
			jsa.addRequiredStaticJsFile("js/jquery/ui/jquery-ui-1.11.4.custom.dnd.min.js");
		}
	}
	
	private void scrollTop(UserRequest ureq) {
		Window window = Windows.getWindows(ureq).getWindow(ureq);
		if(window != null) {
			window.getWindowBackOffice().sendCommandTo(CommandFactory.createScrollTop());
		}
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(COMMAND_ID);
		String nodeId = ureq.getParameter(NODE_IDENT);
		if(COMMAND_TREENODE_CLICKED.equals(cmd)) {
			String openClose = ureq.getParameter(COMMAND_TREENODE);
			if(!StringHelper.containsNonWhitespace(openClose)) {
				boolean unselect = isUnselectNodes() && isSelectedOrDescendant(nodeId);
				if(unselect) {
					handleDeselect(nodeId);
				} else {
					selectedNodeId = nodeId;
				}
			}
			handleClick(ureq, openClose, nodeId);
		} else if (COMMAND_TREENODE_DROP.equals(cmd)) {
			String targetNodeId = ureq.getParameter(TARGET_NODE_IDENT);
			String sneValue = ureq.getParameter(SIBLING_NODE);
			boolean sibling = StringHelper.containsNonWhitespace(sneValue);
			boolean atTheEnd = "end".equals(sneValue);
			handleDropped(ureq, targetNodeId, nodeId, sibling, atTheEnd);
		} else if(COMMAND_TREENODE_INSERT_UP.equals(cmd)) {
			insertionPoint = new InsertionPoint(nodeId, InsertionPoint.Position.up);
			setDirty(true);
			fireEvent(ureq, new InsertEvent(COMMAND_TREENODE_INSERT_UP));
		} else if(COMMAND_TREENODE_INSERT_DOWN.equals(cmd)) {
			insertionPoint = new InsertionPoint(nodeId, InsertionPoint.Position.down);
			setDirty(true);
			fireEvent(ureq, new InsertEvent(COMMAND_TREENODE_INSERT_DOWN));
		} else if(COMMAND_TREENODE_INSERT_UNDER.equals(cmd)) {
			insertionPoint = new InsertionPoint(nodeId, InsertionPoint.Position.under);
			setDirty(true);
			fireEvent(ureq, new InsertEvent(COMMAND_TREENODE_INSERT_UNDER));
		} else if(COMMAND_TREENODE_INSERT_REMOVE.equals(cmd)) {
			insertionPoint = null;
			setDirty(true);
			fireEvent(ureq, new InsertEvent(COMMAND_TREENODE_INSERT_REMOVE));
		}
	}
	
	/**
	 * this is true when the user expanded a treenode to view its children.
	 * it is false when the user clicked on a node with an action 
	 */
	@Override
	public boolean isDirtyForUser() {
		return dirtyForUser;
	}
	
	@Override
	public void setDirty(boolean dirty) {
		super.setDirty(dirty);
		if (!dirty) {
			// clear the userdirty flag also
			dirtyForUser = false;
		}
	}
	
	// -- recorder methods
	private void handleDropped(UserRequest ureq, String droppedNodeId, String targetNodeId, boolean sibling, boolean atTheEnd) {
		TreeDropEvent te = new TreeDropEvent(COMMAND_TREENODE_DROP, droppedNodeId, targetNodeId, !sibling, atTheEnd);
		fireEvent(ureq, te);
		super.setDirty(true);
	}
	
	private void handleDeselect(String nodeId) {
		TreeNode node = treeModel.getNodeById(nodeId);
		INode parentNode = node.getParent();
		if(parentNode != null) {
			setSelectedNodeId(parentNode.getIdent());
		} else {
			clearSelection();
		}
	}
	
	/**
	 * @param selTreeNode
	 */
	private void handleClick(UserRequest ureq, String cmd, String selNodeId) {
		TreeNode selTreeNode = treeModel.getNodeById(selNodeId);
		
		// could be if upon click, an error occured -> timestamp check does not apply, but the tree model was regenerated (as in course)
		if (selTreeNode == null) return;
				
		if (!selTreeNode.isAccessible()){
			TreeEvent te = new TreeEvent(COMMAND_TREENODE_EXPANDED, selNodeId);
			dirtyForUser = true;
			super.setDirty(true);
			
			fireEvent(ureq, te);
			return;
		}

		TreeNode deleg = selTreeNode.getDelegate();
		if (deleg != null) {
			updateOpenedNode(selTreeNode, selNodeId, cmd);
			selNodeId = deleg.getIdent();
			selTreeNode = deleg;
		}
		
		String subCmd = null;
		if(TREENODE_CLOSE.equals(cmd)) {
			subCmd = TreeEvent.COMMAND_TREENODE_CLOSE;
		} else if (TREENODE_OPEN.equals(cmd)) {
			subCmd = TreeEvent.COMMAND_TREENODE_OPEN;
		} else {
			scrollTop(ureq);
		}
		updateOpenedNode(selTreeNode, selNodeId, cmd);

		TreeEvent te = new TreeEvent(COMMAND_TREENODE_CLICKED, subCmd, selNodeId);
		if (selTreeNode.getChildCount() > 0) {
			dirtyForUser = true;
		} // else dirtyForUser is false, since we clicked a node (which only results in the node beeing marked in a visual style)
		super.setDirty(true);
		fireEvent(ureq, te);
	}
	
	private boolean isSelectedOrDescendant(String nodeId) {
		if(nodeId.equals(getSelectedNodeId())) {
			return true;
		}
		
		return false;
	}
	
	private boolean updateOpenedNode(TreeNode treeNode, String nodeId, String cmd) {
		if(TREENODE_CLOSE.equals(cmd)) {
			removeTreeNodeFromOpenList(treeNode, nodeId);
			if(selectedNodeId != null && isChildOf(treeNode, selectedNodeId)) {
				clearSelection();
				setSelectedNodeId(nodeId);
				return true;
			}
		} else if (TREENODE_OPEN.equals(cmd)) {
			openNodeIds.add(nodeId);
			if(treeNode.getUserObject() instanceof String) {
				openNodeIds.add((String)treeNode.getUserObject());
			}
		} else if (cmd == null) {
			openNodeIds.add(nodeId);
			if(treeNode.getUserObject() instanceof String) {
				openNodeIds.add((String)treeNode.getUserObject());
			}
		}
		return false;
	}
	
	private void removeTreeNodeFromOpenList(TreeNode treeNode, String nodeId) {
		openNodeIds.remove(nodeId);
		openNodeIds.remove(treeNode.getUserObject());
		
		for(int i=treeNode.getChildCount(); i-->0; ) {
			TreeNode child = (TreeNode)treeNode.getChildAt(i);
			String childId = child.getIdent();
			TreeNode deleg = child.getDelegate();
			if (deleg != null) {
				childId = deleg.getIdent();
				child = deleg;
			}
			removeTreeNodeFromOpenList(child, childId);
		}
	}
	
	private boolean isChildOf(INode treeNode, String childId) {
		int childCount = treeNode.getChildCount();
		for(int i=0; i<childCount; i++) {
			INode childNode = treeNode.getChildAt(i);
			if(childNode.getIdent().equals(childId) ||
					(childNode instanceof TreeNode && childId.equals(((TreeNode)childNode).getUserObject())) ||
					(isChildOf(childNode, childId))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the selected node
	 */
	public TreeNode getSelectedNode() {
		return (selectedNodeId == null ? null : treeModel.getNodeById(selectedNodeId));
	}

	/**
	 * @return the selected node's id
	 */
	public String getSelectedNodeId() {
		return selectedNodeId;
	}

	/**
	 * @param nodeId
	 */
	public void setSelectedNodeId(String nodeId) {
		selectedNodeId = nodeId;
		setDirty(true);
	}

	public Set<String> getSelectedNodeIds() {
		return selectedNodeIds;
	}

	public void setSelectedNodeIds(Collection<String> newSelectedNodeIds) {
		selectedNodeIds.clear();
		selectedNodeIds.addAll(newSelectedNodeIds);
	}
	
	public boolean isSelected(TreeNode node) {
		return node != null && selectedNodeIds.contains(node.getIdent());
	}
	
	public void select(String id, boolean select) {
		if(select) {
			selectedNodeIds.add(id);
		} else {
			selectedNodeIds.remove(id);
		}
	}
	
	public boolean isOpen(TreeNode node) {
		return openNodeIds.contains(node.getIdent());
	}
	
	public void open(TreeNode node) {
		for(INode iteratorNode=node;
				node.getParent() != null && iteratorNode != null && !openNodeIds.contains(iteratorNode.getIdent());
				iteratorNode=iteratorNode.getParent()) {
			openNodeIds.add(iteratorNode.getIdent());
		}
	}

	public Collection<String> getOpenNodeIds() {
		return openNodeIds;
	}

	public void setOpenNodeIds(Collection<String> nodeIds) {
		openNodeIds.clear();
		if(nodeIds != null) {
			openNodeIds.addAll(nodeIds);
		}
		setDirty(true);
	}

	public void clearSelection() {
		selectedNodeId = null;
	}

	public MenuTreeItem getMenuTreeItem() {
		return menuTreeItem;
	}

	public InsertionPoint getInsertionPoint() {
		return insertionPoint;
	}
	
	public TreePosition getInsertionPosition() {
		if(insertionPoint == null) return null;

		TreeNode node = treeModel.getNodeById(insertionPoint.getNodeId());
		if(insertionPoint.getPosition() == Position.under) {
			return new TreePosition(node, 0);
		} else if(insertionPoint.getPosition() == Position.up) {
			TreeNode parent = (TreeNode)node.getParent();
			return new TreePosition(parent, node, Position.up, node.getPosition());
		} else if(insertionPoint.getPosition() == Position.down) {
			TreeNode parent = (TreeNode)node.getParent();
			int position = node.getPosition() + 1;
			return new TreePosition(parent, node, Position.down, position);
		}
		return null;
	}

	/**
	 * @return MutableTreeModel
	 */
	public TreeModel getTreeModel() {
		return treeModel;
	}

	/**
	 * Sets the treeModel.
	 * 
	 * @param treeModel The treeModel to set
	 */
	public void setTreeModel(TreeModel treeModel) {
		this.treeModel = treeModel;
		selectedNodeId = null; // clear selection if a new model is set
		dirtyForUser = true;
		super.setDirty(true);
	}

	/**
	 * @return Returns the expandServerOnly.
	 */
	public boolean isExpandServerOnly() {
		return expandServerOnly;
	}

	/**
	 * @param expandServerOnly The expandServerOnly to set.
	 */
	public void setExpandServerOnly(boolean expandServerOnly) {
		this.expandServerOnly = expandServerOnly;
	}
	
	public boolean isInsertToolEnabled() {
		return showInsertTool;
	}

	/**
	 * Use the insert tool
	 * @param enableInsertTool
	 */
	public void enableInsertTool(boolean enableInsertTool) {
		showInsertTool = enableInsertTool;
	}

	protected boolean isMultiSelect() {
		return multiSelect;
	}

	protected void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	public INodeFilter getFilter() {
		return filter;
	}

	public void setFilter(INodeFilter filter) {
		if(filter == null) {
			this.filter = DEF_FILTER;
		} else {
			this.filter = filter;
		}
	}

	public boolean isScrollTopOnClick() {
		return scrollTopOnClick;
	}

	public void setScrollTopOnClick(boolean scrollTopOnClick) {
		this.scrollTopOnClick = scrollTopOnClick;
	}

	public boolean isDragEnabled() {
		return dragEnabled;
	}

	/**
	 * @param enableDragAndDrop Enable or not drag and drop
	 */
	public void setDragEnabled(boolean enabled) {
		dragEnabled = enabled;
	}

	/**
	 * @return Is Drag & Drop enable for the tree
	 */
	public boolean isDropEnabled() {
		return dropEnabled;
	}
	
	public void setDropEnabled(boolean enabled) {
		dropEnabled = enabled;
	}
	
	public boolean isDropSiblingEnabled() {
		return dropSiblingEnabled;
	}
	
	public void setDropSiblingEnabled(boolean enabled) {
		dropSiblingEnabled = enabled;
	}
	
	public String getDndAcceptJSMethod() {
		return dndAcceptJSMethod;
	}

	public void setDndAcceptJSMethod(String dndAcceptJSMethod) {
		this.dndAcceptJSMethod = dndAcceptJSMethod;
	}

	/**
	 * Expand the selected node to view its children
	 * @return
	 */
	public boolean isExpandSelectedNode() {
		return expandSelectedNode;
	}

	public void setExpandSelectedNode(boolean expandSelectedNode) {
		this.expandSelectedNode = expandSelectedNode;
	}
	
	public boolean isUnselectNodes() {
		return unselectNodes;
	}
	
	public void setUnselectNodes(boolean unselectNodes) {
		this.unselectNodes = unselectNodes;
	}

	/**
	 * The root node is visible per default
	 * @return
	 */
	public boolean isRootVisible() {
		return rootVisible;
	}

	public void setRootVisible(boolean rootVisible) {
		this.rootVisible = rootVisible;
	}

	/**
	 * @param nodeForum
	 */
	public void setSelectedNode(TreeNode node) {
		if(node == null) {
			setSelectedNodeId(null);
		} else {
			String nId = node.getIdent();
			setSelectedNodeId(nId);
		}
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	private static class DefaultFilter implements INodeFilter {

		@Override
		public boolean isVisible(INode node) {
			return true;
		}
	}
}