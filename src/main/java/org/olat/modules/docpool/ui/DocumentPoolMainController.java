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
package org.olat.modules.docpool.ui;

import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.FullAccessCallback;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.docpool.manager.DocumentPoolNotificationsHandler;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyTreeBuilder;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.modules.taxonomy.model.TaxonomyTreeNode;
import org.olat.modules.taxonomy.model.TaxonomyTreeNodeType;
import org.olat.modules.taxonomy.ui.component.TaxonomyVFSSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentPoolMainController extends MainLayoutBasicController implements Activateable2 {

	private final MenuTree taxonomyTree;
	private final TooledStackedPanel content;
	
	private final LayoutMain3ColsController columnLayoutCtr;
	
	private Taxonomy taxonomy;
	private final boolean isTaxonomyAdmin;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private DocumentPoolModule docPoolModule;
	@Autowired
	private DocumentPoolNotificationsHandler notificationsHandler;
	
	public DocumentPoolMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		Roles roles = ureq.getUserSession().getRoles();
		isTaxonomyAdmin = roles.isAdministrator() || roles.isSystemAdmin();
		
		String taxonomyKey = docPoolModule.getTaxonomyTreeKey();
		if(StringHelper.isLong(taxonomyKey)) {
			TaxonomyRef taxonomyRef = new TaxonomyRefImpl(Long.valueOf(taxonomyKey));
			taxonomy = taxonomyService.getTaxonomy(taxonomyRef);
		}

		String rootTitle = translate("admin.menu.title");//same as site title
		TaxonomyTreeBuilder builder = new TaxonomyTreeBuilder(taxonomy, getIdentity(), rootTitle,
				isTaxonomyAdmin, docPoolModule.isTemplatesDirectoryEnabled(), translate("document.pool.templates"), getLocale());
		
		taxonomyTree = new MenuTree(null, "taxonomy-menu", this);
		taxonomyTree.setExpandSelectedNode(false);
		taxonomyTree.setRootVisible(true);
		taxonomyTree.setTreeModel(builder.buildTreeModel());
		
		content = new TooledStackedPanel("taxonomy-stack", getTranslator(), this);
		content.setNeverDisposeRootController(true);
		content.setToolbarAutoEnabled(true);
		
		
		TreeNode root = taxonomyTree.getTreeModel().getRootNode();
		if(root.getChildCount() > 0) {
			taxonomyTree.open((TreeNode)root.getChildAt(0));

			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), taxonomyTree, content, "docpool");
			columnLayoutCtr.addCssClassToMain("o_taxonomy");
			listenTo(columnLayoutCtr); // auto dispose later
			putInitialPanel(columnLayoutCtr.getInitialComponent());
			
			DocumentPoolTaxonomyController rootCtrl = new DocumentPoolTaxonomyController(ureq, getWindowControl());
			listenTo(rootCtrl);
			String displayName = taxonomy == null ? "ROOT" : taxonomy.getDisplayName();
			content.rootController(displayName, rootCtrl);
		} else {
			VelocityContainer errorVC = createVelocityContainer("error");
			errorVC.contextPut("message", translate("not.configured"));
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, errorVC, "docpool");
			columnLayoutCtr.addCssClassToMain("o_taxonomy");
			listenTo(columnLayoutCtr); // auto dispose later
			putInitialPanel(columnLayoutCtr.getInitialComponent());
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(taxonomyTree.getTreeModel().getRootNode().getChildCount() > 0) {
				doSelectTaxonomy(ureq);
			}
		} else {
			String resourceName = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Templates".equals(resourceName) || resourceName.startsWith("path=")) {
				TreeNode rootNode = taxonomyTree.getTreeModel().getRootNode();
				if(rootNode.getChildCount() > 0) {
					TaxonomyTreeNode node = (TaxonomyTreeNode)rootNode.getChildAt(0);
					if(node.getType() == TaxonomyTreeNodeType.templates) {
						DocumentDirectoryController directoryCtrl = doSelectTemplatesDirectory(ureq, node);
						if(directoryCtrl != null) {
							taxonomyTree.setSelectedNode(node);
							List<ContextEntry> subEntries = entries.subList(1, entries.size());
							directoryCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
						}
					}
				}
			} else if("TaxonomyLevel".equalsIgnoreCase(resourceName)) {
				String levelKey = entries.get(0).getOLATResourceable().getResourceableId().toString();
				TaxonomyTreeNode node = (TaxonomyTreeNode)taxonomyTree.getTreeModel().getNodeById(levelKey);
				if(node != null) {
					DocumentPoolLevelController levelCtrl = doSelectTaxonomyLevel(ureq, node);
					if(levelCtrl != null) {
						taxonomyTree.setSelectedNode(node);
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						levelCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
					}
				}
			} 
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(taxonomyTree == source) {
			if(event instanceof TreeEvent) {
				TreeEvent te = (TreeEvent)event;
				if(MenuTree.COMMAND_TREENODE_CLICKED.equals(te.getCommand())) {
					TaxonomyTreeNode node = (TaxonomyTreeNode)taxonomyTree.getTreeModel().getNodeById(te.getNodeId());
					doSelect(ureq, node);
				}
			}
		} else if(content == source) {
			if(event instanceof PopEvent) {
				PopEvent popEvent = (PopEvent)event;
				if(popEvent.getController() instanceof DocumentPoolLevelController) {
					DocumentPoolLevelController taxonomyLevelCtrl = (DocumentPoolLevelController)popEvent.getController();
					TaxonomyLevel level = taxonomyLevelCtrl.getTaxonomyLevel();
					TaxonomyTreeNode node = (TaxonomyTreeNode)TreeHelper
						.findNodeByUserObject(level, taxonomyTree.getTreeModel().getRootNode());
					TaxonomyTreeNode parentNode = (TaxonomyTreeNode)node.getParent();
					if(parentNode == null) {
						doSelectTaxonomy(ureq);
						taxonomyTree.setSelectedNode(taxonomyTree.getTreeModel().getRootNode());
					} else {
						doSelect(ureq, parentNode);
						taxonomyTree.setSelectedNode(parentNode);
					}
				} else if(popEvent.getUserObject() instanceof TaxonomyTreeNode) {
					TaxonomyTreeNode node = (TaxonomyTreeNode)popEvent.getUserObject();
					doSelect(ureq, node);
					taxonomyTree.setSelectedNode(node);
				} else if(popEvent.getController() instanceof DocumentDirectoryController) {
					//pop the templates
					taxonomyTree.setSelectedNode(taxonomyTree.getTreeModel().getRootNode());
				}
			}
		}
	}
	
	private void doSelect(UserRequest ureq, TaxonomyTreeNode node) {
		switch(node.getType()) {
			case taxonomy:
				doSelectTaxonomy(ureq);
				break;
			case templates:
				doSelectTemplatesDirectory(ureq, node);
				break;
			case taxonomyLevel:
				doSelectTaxonomyLevel(ureq, node);
				break;
			case lostAndFound:
				break;
		}
	}
	
	private void doSelectTaxonomy(UserRequest ureq) {
		content.popUpToRootController(ureq);
	}
	
	private DocumentDirectoryController doSelectTemplatesDirectory(UserRequest ureq, TaxonomyTreeNode node) {
		content.popUpToRootController(ureq);
		
		VFSContainer directory = node.getDirectory();
		VFSSecurityCallback secCallback = isTaxonomyAdmin ? new FullAccessCallback() : new ReadOnlyCallback();
		directory.setLocalSecurityCallback(secCallback);
		
		String name = translate("document.pool.templates");
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Templates", 0l);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		DocumentDirectoryController directoryCtrl = new DocumentDirectoryController(ureq, bwControl, directory, name);
		directoryCtrl.setAdditionalResourceURL("[Templates:0]");
		listenTo(directoryCtrl);

		content.pushController(name, directoryCtrl);
		return directoryCtrl;
	}
	
	private DocumentPoolLevelController doSelectTaxonomyLevel(UserRequest ureq, TaxonomyTreeNode node) {
		if(isTaxonomyAdmin || node.isCanRead() || node.isCanWrite()) {
			TaxonomyLevel level = node.getTaxonomyLevel();

			SubscriptionContext subscriptionCtx = notificationsHandler.getTaxonomyDocumentsLibrarySubscriptionContext();
			TaxonomyVFSSecurityCallback secCallback = new TaxonomyVFSSecurityCallback(node, subscriptionCtx);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("TaxonomyLevel", node.getTaxonomyLevel().getKey());
			WindowControl bwControl = addToHistory(ureq, ores, null);
			DocumentPoolLevelController levelCtrl = new DocumentPoolLevelController(ureq, bwControl, level, node, secCallback);
			listenTo(levelCtrl);
			String displayName = level.getDisplayName();
			
			content.popUpToRootController(ureq);
			List<TreeNode> parentLines = TreeHelper.getTreePath(node);
			for(int i=1; i<parentLines.size() - 1; i++) {
				TreeNode parent = parentLines.get(i);
				content.pushController(parent.getTitle(), null, parent);
			}
			content.pushController(displayName, levelCtrl);
			return levelCtrl;
		}
		return null;
	}
}