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
package org.olat.core.gui.components.tree;

import static org.olat.core.gui.components.tree.MenuTreeEvent.DESELECT;
import static org.olat.core.gui.components.tree.MenuTreeEvent.SELECT;

import java.util.*;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.util.tree.INodeFilter;
import org.olat.core.util.tree.TreeHelper;

/**
 * 
 * Initial date: 13.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MenuTreeItem extends FormItemImpl {
	
	private final MenuTree treeCmp;
	private boolean noDirtyCheckOnClick = false;
	private Set<String> visibleNodeIds = new HashSet<>();
	
	public MenuTreeItem(String name, ComponentEventListener listener) {
		super(name);
		treeCmp = new MenuTree(null, name + "_CMP", listener, this);
	}

	public boolean isNoDirtyCheckOnClick() {
		return noDirtyCheckOnClick;
	}

	public void setNoDirtyCheckOnClick(boolean noDirtyCheckOnClick) {
		this.noDirtyCheckOnClick = noDirtyCheckOnClick;
	}

	@Override
	protected MenuTree getFormItemComponent() {
		return treeCmp;
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		String[] selectedIndexArr = getRootForm().getRequestParameterValues("tcb_ms");
		if(selectedIndexArr == null) {
			//be suspicious
			if(getFormDispatchId().equals(getRootForm().getRequestParameter("dispatchuri"))) {
				selectedIndexArr = new String[0];
			}
		}
		
		if(selectedIndexArr != null) {
			List<MenuTreeEvent> events = new ArrayList<>();
			Set<String> referenceIds = new HashSet<>(visibleNodeIds);
			for(String index:selectedIndexArr) {
				referenceIds.remove(index);
				treeCmp.select(index, true);
				events.add(new MenuTreeEvent(SELECT, this, index));
			}
			for(String deselectedId:referenceIds) {
				if(treeCmp.getSelectedNodeIds().contains(deselectedId)) {
					treeCmp.select(deselectedId, false);
					events.add(new MenuTreeEvent(DESELECT, this, deselectedId));
				}
			}
			for(MenuTreeEvent e:events) {
				getRootForm().fireFormEvent(ureq, e);
			}
		}
	}
	
	protected void clearVisibleNodes() {
		visibleNodeIds.clear();
	}
	
	protected void trackVisibleNode(TreeNode node) {
		visibleNodeIds.add(node.getIdent());
	}
	
	public boolean isIndeterminate(TreeNode node) {
		if(treeCmp.isSelected(node) /* OO-1883 || treeCmp.isOpen(node) */) {
			return false;
		}
		for(int i=node.getChildCount(); i-->0; ) {
			TreeNode child = (TreeNode)node.getChildAt(i);
			/* OO-1883 if(treeCmp.isOpen(child)) {
				return false;
			}*/
			if(isSelectRec(child)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isSelectRec(TreeNode node) {
		if(treeCmp.isSelected(node)) {
			return true;
		}
		
		for(int i=node.getChildCount(); i-->0; ) {
			TreeNode child = (TreeNode)node.getChildAt(i);
			if(isSelectRec(child)) {
				return true;
			}
		}
		return false;
	}

	public void selectAll() {
		TreeModel model = getTreeModel();
		List<String> nodeIdentifiers = new ArrayList<>();
 		TreeHelper.collectNodeIdentifiersRecursive(model.getRootNode(), nodeIdentifiers);
		treeCmp.setSelectedNodeIds(nodeIdentifiers);
		treeCmp.setDirty(true);
	}

	public void deselectAll() {
		treeCmp.setSelectedNodeIds(Collections.<String>emptyList());
		treeCmp.setDirty(true);
	}
	
	public void setFilter(INodeFilter filter) {
		treeCmp.setFilter(filter);
	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		super.setElementCssClass(elementCssClass);
		treeCmp.setElementCssClass(elementCssClass);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		treeCmp.setEnabled(enabled);
	}
	
	public Set<String> getSelectedKeys() {
		return treeCmp.getSelectedNodeIds();
	}
	
	public void setSelectedKeys(Collection<String> keys) {
		treeCmp.setSelectedNodeIds(keys);
	}
	
	public void select(String key, boolean select) {
		treeCmp.select(key, select);
	}

	public TreeNode getSelectedNode() {
		return treeCmp.getSelectedNode();
	}

	public String getSelectedNodeId() {
		return treeCmp.getSelectedNodeId();
	}

	public void setSelectedNodeId(String nodeId) {
		treeCmp.setSelectedNodeId(nodeId);
	}
	
	public void open(TreeNode node) {
		treeCmp.open(node);
	}

	public Collection<String> getOpenNodeIds() {
		return treeCmp.getOpenNodeIds();
	}

	public void setOpenNodeIds(Collection<String> nodeIds) {
		treeCmp.setOpenNodeIds(nodeIds);
	}

	public InsertionPoint getInsertionPoint() {
		return treeCmp.getInsertionPoint();
	}

	public TreePosition getInsertionPosition() {
		return treeCmp.getInsertionPosition();
	}

	public TreeModel getTreeModel() {
		return treeCmp.getTreeModel();
	}

	public void setTreeModel(TreeModel treeModel) {
		treeCmp.setTreeModel(treeModel);
	}

	public void setExpandServerOnly(boolean expandServerOnly) {
		treeCmp.setExpandServerOnly(expandServerOnly);
	}
	
	

	public boolean isMultiSelect() {
		return treeCmp.isMultiSelect();
	}

	public void setMultiSelect(boolean multiSelect) {
		treeCmp.setMultiSelect(multiSelect);
	}

	public void setDragEnabled(boolean enabled) {
		treeCmp.setDragEnabled(enabled);
	}

	public void setDropEnabled(boolean enabled) {
		treeCmp.setDropEnabled(enabled);
	}

	public void setDropSiblingEnabled(boolean enabled) {
		treeCmp.setDropSiblingEnabled(enabled);
	}

	public void setExpandSelectedNode(boolean expandSelectedNode) {
		treeCmp.setExpandSelectedNode(expandSelectedNode);
	}

	public void setUnselectNodes(boolean unselectNodes) {
		treeCmp.setUnselectNodes(unselectNodes);
	}

	public void setRootVisible(boolean rootVisible) {
		treeCmp.setRootVisible(rootVisible);
	}
	
	public void setInsertTool(boolean enabled) {
		treeCmp.enableInsertTool(enabled);
	}

	public void setSelectedNode(TreeNode node) {
		treeCmp.setSelectedNode(node);
	}

	@Override
	protected void rootFormAvailable() {
		//
	}



	@Override
	public void reset() {
		//
	}
}
