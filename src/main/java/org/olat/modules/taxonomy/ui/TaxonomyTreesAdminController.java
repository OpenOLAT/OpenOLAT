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
package org.olat.modules.taxonomy.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.InsertEvent;
import org.olat.core.gui.components.tree.InsertionPoint.Position;
import org.olat.core.gui.components.tree.InsertionTreeModel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.tree.TreePosition;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyAllTreesBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreesAdminController extends BasicController {
	
	private MenuTree taxonomiesTree; 
	private Link createTaxonomyButton, addLevelButton;
	private VelocityContainer mainVC;
	private TaxonomyTreesModel taxonomyTreesModel;

	private CloseableModalController cmc;
	private EditTaxonomyController editTaxonomyCtrl;
	private EditTaxonomyLevelController createTaxonomyLevelCtrl;
	
	private DetailsTaxonomyController detailsCtrl;
	private DetailsTaxonomyLevelController detailsLevelCtrl;
	
	@Autowired
	private TaxonomyService taxonomyService;

	public TaxonomyTreesAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin_trees");
		
		createTaxonomyButton = LinkFactory.createButton("create.taxonomy", mainVC, this);
		addLevelButton = LinkFactory.createButton("add.taxonomy.level", mainVC, this);
		mainVC.put("createTaxonomyButton", createTaxonomyButton);
		mainVC.put("addTaxonomyLevelButton", addLevelButton);
		
		detailsLevelCtrl = new DetailsTaxonomyLevelController(ureq, getWindowControl());
		listenTo(detailsLevelCtrl);
		detailsCtrl = new DetailsTaxonomyController(ureq, getWindowControl());
		listenTo(detailsCtrl);

		taxonomiesTree = new MenuTree("taxonomyTrees");
		taxonomyTreesModel = new TaxonomyTreesModel();
		taxonomiesTree.setTreeModel(taxonomyTreesModel);
		taxonomiesTree.setSelectedNode(taxonomiesTree.getTreeModel().getRootNode());
		taxonomiesTree.setDropEnabled(false);
		taxonomiesTree.addListener(this);
		loadTreeModel();

		mainVC.put("trees", taxonomiesTree);
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editTaxonomyCtrl == source) {
			if(event == Event.DONE_EVENT) {
				reloadTreeModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(createTaxonomyLevelCtrl == source) {
			if(event == Event.DONE_EVENT) {
				reloadTreeModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(createTaxonomyButton == source) {
			doCreateTaxonomy(ureq);
		} else if(addLevelButton == source) {
			doEnableAddLevel(ureq);
		} else if(taxonomiesTree == source) {
			if(event instanceof TreeEvent) {
				TreeEvent te = (TreeEvent)event;
				if(MenuTree.COMMAND_TREENODE_CLICKED.equals(te.getCommand())) {
					TreeNode node = taxonomyTreesModel.getNodeById(te.getNodeId());
					doSelect(node);
				}
			} else if(event instanceof InsertEvent) {
				boolean canAdd = taxonomiesTree.getInsertionPoint() != null;
				if(canAdd) {
					addLevelButton.setElementCssClass("btn-primary");
				} else {
					addLevelButton.setElementCssClass(null);
				}
			}
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(createTaxonomyLevelCtrl);
		removeAsListenerAndDispose(editTaxonomyCtrl);
		removeAsListenerAndDispose(cmc);
		createTaxonomyLevelCtrl = null;
		editTaxonomyCtrl = null;
		cmc = null;
	}
	
	private void reloadTreeModel() {
		List<String> openedNodeIds = new ArrayList<>(taxonomiesTree.getOpenNodeIds());
		String selectedId = taxonomiesTree.getSelectedNodeId();
		
		loadTreeModel();
		taxonomiesTree.setTreeModel(taxonomyTreesModel);
		taxonomiesTree.select(selectedId, true);
		taxonomiesTree.setOpenNodeIds(openedNodeIds);
	}
	
	private void loadTreeModel() {
		new TaxonomyAllTreesBuilder().loadTreeModel(taxonomyTreesModel);
	}
	
	private void doSelect(TreeNode node) {
		if(node != null) {
			if(node.getUserObject() instanceof TaxonomyLevel) {
				TaxonomyLevel taxonomyLevel = (TaxonomyLevel)node.getUserObject();
				taxonomyLevel = taxonomyService.getTaxonomyLevel(taxonomyLevel);
				detailsLevelCtrl.setTaxonomyLevel(taxonomyLevel);
				mainVC.put("details", detailsLevelCtrl.getInitialComponent());
			} else if(node.getUserObject() instanceof Taxonomy) {
				Taxonomy taxonomy = (Taxonomy)node.getUserObject();
				taxonomy = taxonomyService.getTaxonomy(taxonomy);
				detailsCtrl.setTaxonomy(taxonomy);
				mainVC.put("details", detailsCtrl.getInitialComponent());
			} else {
				mainVC.remove("details");
			}
		} else {
			mainVC.remove("details");
		}
	}
	
	private void doEnableAddLevel(UserRequest ureq) {
		if(taxonomiesTree.getInsertionPosition() != null) {
			TreePosition position = taxonomiesTree.getInsertionPosition();	

			TreeNode parent = position.getParentTreeNode();
			Object uobject = parent.getUserObject();
			if(uobject instanceof Taxonomy) {
				doCreateTaxonomyLevel(ureq, null, (Taxonomy)uobject);
			} else if(uobject instanceof TaxonomyLevel) {
				TaxonomyLevel parentLevel = (TaxonomyLevel)uobject;
				doCreateTaxonomyLevel(ureq, parentLevel, parentLevel.getTaxonomy());
			}	
		} else {
			taxonomiesTree.enableInsertTool(true);
			taxonomiesTree.setDirty(true);
		}
	}

	private void doCreateTaxonomy(UserRequest ureq) {
		if(editTaxonomyCtrl != null) return;
		
		editTaxonomyCtrl = new EditTaxonomyController(ureq, getWindowControl(), null);
		listenTo(editTaxonomyCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editTaxonomyCtrl.getInitialComponent(), true, translate("create.taxonomy"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateTaxonomyLevel(UserRequest ureq, TaxonomyLevel parentLevel, Taxonomy taxonomy) {
		if(createTaxonomyLevelCtrl != null) return;

		createTaxonomyLevelCtrl = new EditTaxonomyLevelController(ureq, getWindowControl(), parentLevel, taxonomy);
		listenTo(createTaxonomyLevelCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", createTaxonomyLevelCtrl.getInitialComponent(), true, translate("add.taxonomy.level"));
		listenTo(cmc);
		cmc.activate();
	}
	
	public class TaxonomyTreesModel extends GenericTreeModel implements InsertionTreeModel {

		private static final long serialVersionUID = -3141702425361197931L;
		
		@Override
		public boolean isSource(TreeNode node) {
			return false;
		}

		@Override
		public Position[] getInsertionPosition(TreeNode node) {
			Position[] positions;
			if(node.getUserObject() instanceof TaxonomyLevel) {
				positions = new Position[] { Position.down, Position.under };
			} else if(node.getUserObject() instanceof Taxonomy) {
				positions = new Position[] { Position.under };
			} else {
				positions = new Position[0];
			}
			return positions;
		}
	}
}