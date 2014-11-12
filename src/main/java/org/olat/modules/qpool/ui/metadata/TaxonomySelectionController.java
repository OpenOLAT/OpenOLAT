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
package org.olat.modules.qpool.ui.metadata;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.tree.TreeHelper;
import org.olat.modules.qpool.TaxonomyLevel;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.admin.TaxonomyTreeModel;

/**
 * 
 * Initial date: 12.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomySelectionController extends BasicController {
	
	private final Link selectButton, cancelButton;
	private final MenuTree selectionTree;
	
	public TaxonomySelectionController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null, null);
	}
	
	public TaxonomySelectionController(UserRequest ureq, WindowControl wControl,
			QuestionItemImpl itemImpl, TaxonomyLevel selectedTaxonomicPath) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("taxanomy_selection");
		
		selectionTree = new MenuTree(null, "TaxanomyTree", this);
		selectionTree.setRootVisible(false);
		TaxonomyTreeModel treeModel = new TaxonomyTreeModel("");
		selectionTree.setTreeModel(treeModel);
		
		if((itemImpl != null && itemImpl.getTaxonomyLevel() != null) || selectedTaxonomicPath != null) {
			TaxonomyLevel txPath = selectedTaxonomicPath == null ? itemImpl.getTaxonomyLevel() : selectedTaxonomicPath;
			TreeNode selectedNode = TreeHelper.findNodeByUserObject(txPath, treeModel.getRootNode());
			selectionTree.setSelectedNodeId(selectedNode.getIdent());
		}

		mainVC.put("tree", selectionTree);
		selectButton = LinkFactory.createButton("select", mainVC, this);
		cancelButton = LinkFactory.createButton("cancel", mainVC, this);
		putInitialPanel(mainVC);
	}
	
	public TaxonomyLevel getSelectedLevel() {
		TreeNode selectedNode = selectionTree.getSelectedNode();
		if(selectedNode != null && selectedNode.getUserObject() instanceof TaxonomyLevel) {
			return (TaxonomyLevel)selectedNode.getUserObject();
		}
		return null;
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
		}
	}
}
