/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.editor;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;

/**
 * 
 * Initial date: 14 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditorTreeController extends BasicController {
	
	private final MenuTree menuTree;
	private final VelocityContainer mainVC;
	private final MenuTree configurationMenuTree;
	private final GenericTreeNode configurationNode;
	
	public EditorTreeController(UserRequest ureq, WindowControl wControl, NodeAccessType nodeAccessType) {
		super(ureq, wControl);
		
		menuTree = new MenuTree("luTree");
		menuTree.setExpandSelectedNode(false);
		menuTree.setDragEnabled(true);
		menuTree.setDropEnabled(true);
		menuTree.setDropSiblingEnabled(true);	
		menuTree.setDndAcceptJSMethod("treeAcceptDrop_notWithChildren");
		menuTree.setDomReplacementWrapperRequired(false);
		String menuTreeCss = LearningPathNodeAccessProvider.TYPE.equals(nodeAccessType.getType())
				? "o_editor_menu o_lp_edit"
				: "o_editor_menu";
		menuTree.setElementCssClass(menuTreeCss);
		menuTree.addListener(this);
		
		configurationMenuTree = new MenuTree("configurationTree");
		configurationMenuTree.setElementCssClass("o_editor_configuration_menu");
		configurationMenuTree.addListener(this);
		configurationMenuTree.setHighlightSelection(false);
		configurationMenuTree.setDomReplacementWrapperRequired(false);
		
		GenericTreeModel overviewTreeModel = new GenericTreeModel();
		configurationNode = new GenericTreeNode();
		configurationNode.setTitle(translate("command.overview"));
		configurationNode.setIconCssClass("o_icon_description");
		overviewTreeModel.setRootNode(configurationNode);
		configurationMenuTree.setTreeModel(overviewTreeModel);

		mainVC = createVelocityContainer("editor_tree");
		mainVC.setDomReplacementWrapperRequired(false);
		mainVC.put("courseTree", menuTree);
		mainVC.put("configurationTree", configurationMenuTree);
		
		putInitialPanel(mainVC);
	}
	
	public TreeModel getTreeModel() {
		return menuTree.getTreeModel();
	}
	
	public void setTreeModel(TreeModel model) {
		menuTree.setTreeModel(model);
	}
	
	public TreeNode getSelectedNode() {
		return menuTree.getSelectedNode();
	}
	
	public void setSelectedNodeId(String nodeIdent) {
		if(!menuTree.isHighlightSelection()) {
			configurationMenuTree.setHighlightSelection(false);
			menuTree.setHighlightSelection(true);
		}
		menuTree.setSelectedNodeId(nodeIdent);
	}
	
	public void setOpenNodeId(String nodeIdent) {
		menuTree.setOpenNodeIds(List.of(nodeIdent));
	}
	
	public void setDirty(boolean dirty) {
		mainVC.setDirty(dirty);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(menuTree == source) {
			if (MenuTree.COMMAND_TREENODE_CLICKED.equals(event.getCommand()) && event instanceof TreeEvent te) {
				configurationMenuTree.setHighlightSelection(false);
				menuTree.setHighlightSelection(true);
			}
			fireEvent(ureq, event);
		} else if(configurationMenuTree == source) {
			if (MenuTree.COMMAND_TREENODE_CLICKED.equals(event.getCommand()) && event instanceof TreeEvent te) {
				configurationMenuTree.setHighlightSelection(true);
				menuTree.setHighlightSelection(false);
				fireEvent(ureq, new EditorTreeEvent(EditorTreeEvent.CONFIGURATION_VIEW));
			}
		}
	}
}
