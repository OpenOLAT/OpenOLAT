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
package org.olat.modules.qpool.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.core.util.tree.TreeHelper;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyAdminController extends BasicController {

	private Link addButton;
	private final MenuTree taxonomyTree;
	private TaxonomyTreeModel taxonomyTreeModel;

	private CloseableModalController cmc;
	private TaxonomyLevelEditController editCtrl;
	private TaxonomyLevelController detailsCtrl;
	
	private final VelocityContainer mainVC;
	
	public TaxonomyAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));
		
		mainVC = createVelocityContainer("admin_study_fields");
		
		addButton = LinkFactory.createButton("add.taxonomyLevel", mainVC, this);
		mainVC.put("add", addButton);
		
		detailsCtrl = new TaxonomyLevelController(ureq, getWindowControl());
		listenTo(detailsCtrl);
		mainVC.put("details", detailsCtrl.getInitialComponent());

		taxonomyTree = new MenuTree("qpoolTree");
		taxonomyTreeModel = new TaxonomyTreeModel(translate("root.taxonomyLevel"));
		taxonomyTree.setTreeModel(taxonomyTreeModel);
		taxonomyTree.setSelectedNode(taxonomyTree.getTreeModel().getRootNode());
		taxonomyTree.setDropEnabled(false);
		taxonomyTree.addListener(this);

		mainVC.put("tree", taxonomyTree);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(addButton == source) {
			doAddLevel(ureq);
		} else if(taxonomyTree == source) {
			if(event instanceof TreeEvent) {
				TreeEvent te = (TreeEvent)event;
				if(MenuTree.COMMAND_TREENODE_CLICKED.equals(te.getCommand())) {
					TreeNode node = taxonomyTreeModel.getNodeById(te.getNodeId());
					if(node != null && node.getUserObject() instanceof TaxonomyLevel) {
						TaxonomyLevel taxonomyLevel = (TaxonomyLevel)node.getUserObject();
						detailsCtrl.setTaxonomyLevel(taxonomyLevel);
					} else {
						detailsCtrl.setTaxonomyLevel(null);
					}
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				taxonomyTreeModel = new TaxonomyTreeModel(translate("root.taxonomyLevel"));
				taxonomyTree.setTreeModel(taxonomyTreeModel);
				if(editCtrl.getTaxonomyLevel() != null) {
					TreeNode newNode
						= TreeHelper.findNodeByUserObject(editCtrl.getTaxonomyLevel(), taxonomyTreeModel.getRootNode());
					if(newNode != null) {
						taxonomyTree.setSelectedNode(newNode);
					}
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(detailsCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				taxonomyTreeModel = new TaxonomyTreeModel(translate("root.taxonomyLevel"));
				taxonomyTree.setTreeModel(taxonomyTreeModel);
				if(detailsCtrl.getTaxonomyLevel() != null) {
					TreeNode newNode
						= TreeHelper.findNodeByUserObject(detailsCtrl.getTaxonomyLevel(), taxonomyTreeModel.getRootNode());
					if(newNode != null) {
						taxonomyTree.setSelectedNode(newNode);
					}
				}
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}

	private void doAddLevel(UserRequest ureq) {
		TreeNode node = taxonomyTree.getSelectedNode();
		TaxonomyLevel parentLevel = null;
		if(node != null && node.getUserObject() instanceof TaxonomyLevel) {
			parentLevel = (TaxonomyLevel)node.getUserObject();
		}

		removeAsListenerAndDispose(editCtrl);
		editCtrl = new TaxonomyLevelEditController(ureq, getWindowControl(), parentLevel, null);
		listenTo(editCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editCtrl.getInitialComponent(), true, translate("add.taxonomyLevel"));
		cmc.activate();
		listenTo(cmc);
	}
}