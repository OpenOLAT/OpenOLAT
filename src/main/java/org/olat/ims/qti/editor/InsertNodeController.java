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
package org.olat.ims.qti.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.InsertEvent;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.tree.TreePosition;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.qti.editor.tree.InsertItemTreeModel;

/**
 * 
 * Initial date: 13.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InsertNodeController extends BasicController {
	
	private final MenuTree insertTree;
	private final Link selectButton, cancelButton;
	
	private Object userObject;
	
	public InsertNodeController(UserRequest ureq, WindowControl wControl, InsertItemTreeModel treeModel) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("selection_tree");
		insertTree = new MenuTree(null, "insertTree", this);
		insertTree.enableInsertTool(true);
		insertTree.setTreeModel(treeModel);
		mainVC.put("tree", insertTree);
		
		selectButton = LinkFactory.createButton("submit", mainVC, this);
		selectButton.setCustomEnabledLinkCSS("btn btn-primary");
		selectButton.setCustomDisabledLinkCSS("btn btn-default");
		selectButton.setEnabled(false);
		cancelButton = LinkFactory.createButton("cancel", mainVC, this);
		
		putInitialPanel(mainVC);
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	public TreePosition getInsertPosition() {
		TreePosition pos = insertTree.getInsertionPosition();
		if(pos == null) return null;
		TreeNode node = (TreeNode)pos.getParentTreeNode().getUserObject();
		return new TreePosition(node, pos.getChildpos());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(selectButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(cancelButton == source) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if(event instanceof InsertEvent) {
			boolean canSelect = insertTree.getInsertionPoint() != null;
			selectButton.setEnabled(canSelect);
		}
	}
}
