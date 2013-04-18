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
package org.olat.modules.qpool.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeDropEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.ui.admin.PoolsAdminController;
import org.olat.modules.qpool.ui.admin.QEducationalContextsAdminController;
import org.olat.modules.qpool.ui.admin.QItemTypesAdminController;
import org.olat.modules.qpool.ui.admin.QLicensesAdminController;
import org.olat.modules.qpool.ui.admin.TaxonomyAdminController;
import org.olat.modules.qpool.ui.datasource.CollectionOfItemsSource;
import org.olat.modules.qpool.ui.datasource.DefaultItemsSource;
import org.olat.modules.qpool.ui.datasource.SharedItemsSource;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolMainEditorController extends BasicController implements Activateable2, StackedControllerAware {

	private final MenuTree menuTree;
	private GenericTreeNode sharesNode, myNode;
	
	private final Panel content;
	private StackedController stackPanel;

	private QuestionsController currentCtrl;
	private QuestionsController myQuestionsCtrl;
	private QuestionsController sharedItemsCtrl;
	private CollectionQuestionsController collItemsCtrl;
	private QuestionsController selectedPoolCtrl;
	private QuestionsController markedQuestionsCtrl;
	
	private PoolsAdminController poolAdminCtrl;
	private QItemTypesAdminController typesCtrl;
	private QEducationalContextsAdminController levelsCtrl;
	private QLicensesAdminController licensesCtrl;
	private TaxonomyAdminController studyFieldCtrl;
	private LayoutMain3ColsController columnLayoutCtr;
	private QuestionPoolAdminStatisticsController adminStatisticsCtrl;

	private final Roles roles;
	private final MarkManager markManager;
	private final QPoolService qpoolService;
	
	public QuestionPoolMainEditorController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		roles = ureq.getUserSession().getRoles();
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		
		menuTree = new MenuTree("qpoolTree");
		menuTree.setTreeModel(buildTreeModel());
		menuTree.setSelectedNode(menuTree.getTreeModel().getRootNode());
		menuTree.setDragEnabled(false);
		menuTree.setDropEnabled(true);
		menuTree.setDropSiblingEnabled(false);
		menuTree.addListener(this);
		menuTree.setRootVisible(false);
		//open the nodes shared and my at start
		List<String> openNodeIds = new ArrayList<String>(2);
		openNodeIds.add(myNode.getIdent());
		openNodeIds.add(sharesNode.getIdent());
		menuTree.setOpenNodeIds(openNodeIds);
		
		content = new Panel("list");
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, null, content, "qpool");				
		
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		this.stackPanel = stackPanel;
		if(myQuestionsCtrl != null) myQuestionsCtrl.setStackedController(stackPanel);
		if(markedQuestionsCtrl != null) markedQuestionsCtrl.setStackedController(stackPanel);
		if(selectedPoolCtrl != null) selectedPoolCtrl.setStackedController(stackPanel);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(menuTree == source) {
			if(event instanceof TreeDropEvent) {
				TreeDropEvent e = (TreeDropEvent)event;
				String targetId = e.getTargetNodeId();
				String dropId = e.getDroppedNodeId();
				//drop id w_o_fi1000002357-4
				doDrop(targetId, dropId);
			} else if(menuTree.getSelectedNode() != null){
				TreeNode node = menuTree.getSelectedNode();
				Object uNode = node.getUserObject();
				if("menu.admin".equals(uNode)) {
					doSelectAdmin(ureq);
				} else if("menu.admin.studyfields".equals(uNode)) {
					doSelectAdminStudyFields(ureq);
				} else if("menu.admin.pools".equals(uNode)) {
					doSelectAdminPools(ureq);
				} else if("menu.admin.types".equals(uNode)) {
					doSelectAdminTypes(ureq);
				} else if("menu.admin.levels".equals(uNode)) {
					doSelectAdminLevels(ureq);
				} else if("menu.admin.licenses".equals(uNode)) {
					doSelectAdminLicenses(ureq);
				} else if("menu.database.my".equals(uNode)) {
					doSelectMyQuestions(ureq);
				} else if("menu.database.favorit".equals(uNode)) {
					doSelectMarkedQuestions(ureq);
				} else if(uNode instanceof Pool) {
					Pool pool = (Pool)uNode;
					doSelect(ureq, pool);
				} else if(uNode instanceof BusinessGroup) {
					BusinessGroup group = (BusinessGroup)uNode;
					doSelect(ureq, group);
				} else if(uNode instanceof QuestionItemCollection) {
					QuestionItemCollection coll = (QuestionItemCollection)uNode;
					doSelect(ureq, coll);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof QPoolEvent) {
			if(QPoolEvent.ITEM_SHARED.equals(event.getCommand())) {
				buildShareSubTreeModel(sharesNode);
				menuTree.setDirty(true);
			}	else if(QPoolEvent.COLL_CREATED.equals(event.getCommand())
					|| QPoolEvent.COLL_CHANGED.equals(event.getCommand())) {
				buildMySubTreeModel(myNode);
				menuTree.setDirty(true);
			}	else if(QPoolEvent.COLL_DELETED.equals(event.getCommand())) {
				buildMySubTreeModel(myNode);
				menuTree.setDirty(true);
				menuTree.setSelectedNode(myNode);
				currentCtrl = null;
				content.popContent();
			} else if(QPoolEvent.POOL_CREATED.equals(event.getCommand())
					|| QPoolEvent.POOL_DELETED.equals(event.getCommand())) {
				buildShareSubTreeModel(sharesNode);
				menuTree.setDirty(true);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	private void doDrop(String targetId, String dropId) {
		try {
			int lastIndex = dropId.lastIndexOf('-');
			String rowStr = dropId.substring(lastIndex+1, dropId.length());
			int row = Integer.parseInt(rowStr);
			QuestionItemShort item = currentCtrl.getQuestionAt(row);
			TreeNode node = menuTree.getTreeModel().getNodeById(targetId);
			if(node != null) {
				Object userObj = node.getUserObject();
				if(userObj instanceof BusinessGroup) {
					qpoolService.shareItemsWithGroups(Collections.singletonList(item), Collections.singletonList((BusinessGroup)userObj), false);
					showInfo("item.shared", item.getTitle());
				} else if(userObj instanceof Pool) {
					qpoolService.addItemsInPools(Collections.singletonList(item), Collections.singletonList((Pool)userObj), false);
					showInfo("item.pooled", item.getTitle());
				} else if(userObj instanceof QuestionItemCollection) {
					qpoolService.addItemToCollection(item, (QuestionItemCollection)userObj);
					showInfo("item.collectioned", item.getTitle());
				} else if("menu.database.favorit".equals(userObj)) {
					String businessPath = "[QuestionItem:" + item.getKey() + "]";
					markManager.setMark(item, getIdentity(), null, businessPath);
				}
			}
			
		} catch (Exception e) {
			logError("Cannot drop with id: " + dropId, e);
		}
	}
	
	private void doSelectAdmin(UserRequest ureq) {
		if(adminStatisticsCtrl == null) {
			adminStatisticsCtrl = new QuestionPoolAdminStatisticsController(ureq, getWindowControl());
			listenTo(adminStatisticsCtrl);
		}
		content.setContent(adminStatisticsCtrl.getInitialComponent());
	}
	
	private void doSelectAdminStudyFields(UserRequest ureq) {
		if(studyFieldCtrl == null) {
			studyFieldCtrl = new TaxonomyAdminController(ureq, getWindowControl());
			listenTo(studyFieldCtrl);
		}
		content.setContent(studyFieldCtrl.getInitialComponent());
	}
	
	private void doSelectAdminPools(UserRequest ureq) {
		if(poolAdminCtrl == null) {
			poolAdminCtrl = new PoolsAdminController(ureq, getWindowControl());
			listenTo(poolAdminCtrl);
		}
		content.setContent(poolAdminCtrl.getInitialComponent());
	}
	
	private void doSelectAdminTypes(UserRequest ureq) {
		if(typesCtrl == null) {
			typesCtrl = new QItemTypesAdminController(ureq, getWindowControl());
			listenTo(typesCtrl);
		}
		content.setContent(typesCtrl.getInitialComponent());
	}
	
	private void doSelectAdminLevels(UserRequest ureq) {
		if(levelsCtrl == null) {
			levelsCtrl = new QEducationalContextsAdminController(ureq, getWindowControl());
			listenTo(levelsCtrl);
		}
		content.setContent(levelsCtrl.getInitialComponent());
	}
	
	private void doSelectAdminLicenses(UserRequest ureq) {
		if(licensesCtrl == null) {
			licensesCtrl = new QLicensesAdminController(ureq, getWindowControl());
			listenTo(licensesCtrl);
		}
		content.setContent(licensesCtrl.getInitialComponent());
	}
	
	private void doSelectMyQuestions(UserRequest ureq) {
		DefaultItemsSource source = new DefaultItemsSource(getIdentity(), ureq.getUserSession().getRoles(), "My"); 
		source.getDefaultParams().setAuthor(getIdentity());
		if(myQuestionsCtrl == null) {
			myQuestionsCtrl = new QuestionsController(ureq, getWindowControl(), source);
			myQuestionsCtrl.setStackedController(stackPanel);
			listenTo(myQuestionsCtrl);
		} else {
			myQuestionsCtrl.updateSource(ureq, source);
		}
		currentCtrl = myQuestionsCtrl;
		content.setContent(myQuestionsCtrl.getInitialComponent());
	}
	
	private void doSelectMarkedQuestions(UserRequest ureq) {
		DefaultItemsSource source = new DefaultItemsSource(getIdentity(), ureq.getUserSession().getRoles(), "Fav"); 
		source.getDefaultParams().setFavoritOnly(true);
		if(markedQuestionsCtrl == null) {
			markedQuestionsCtrl = new QuestionsController(ureq, getWindowControl(), source);
			markedQuestionsCtrl.setStackedController(stackPanel);
			listenTo(markedQuestionsCtrl);
		} else {
			markedQuestionsCtrl.updateSource(ureq, source);
		}
		currentCtrl = markedQuestionsCtrl;
		content.setContent(markedQuestionsCtrl.getInitialComponent());
	}
	
	private void doSelect(UserRequest ureq, Pool pool) {
		DefaultItemsSource source = new DefaultItemsSource(getIdentity(), ureq.getUserSession().getRoles(), pool.getName());
		source.getDefaultParams().setPoolKey(pool.getKey());
		if(selectedPoolCtrl == null) {
			selectedPoolCtrl = new QuestionsController(ureq, getWindowControl(), source);
			selectedPoolCtrl.setStackedController(stackPanel);
			listenTo(selectedPoolCtrl);
		} else {
			selectedPoolCtrl.updateSource(ureq, source);
		}
		currentCtrl = selectedPoolCtrl;
		content.setContent(selectedPoolCtrl.getInitialComponent());
	}
	
	private void doSelect(UserRequest ureq, BusinessGroup group) {
		if(sharedItemsCtrl == null) {
			sharedItemsCtrl = new QuestionsController(ureq, getWindowControl(), new SharedItemsSource(group, getIdentity(), ureq.getUserSession().getRoles()));
			sharedItemsCtrl.setStackedController(stackPanel);
			listenTo(sharedItemsCtrl);
		} else {
			sharedItemsCtrl.updateSource(ureq, new SharedItemsSource(group, getIdentity(), ureq.getUserSession().getRoles()));
		}
		currentCtrl = sharedItemsCtrl;
		content.setContent(sharedItemsCtrl.getInitialComponent());
	}
	
	private void doSelect(UserRequest ureq, QuestionItemCollection coll) {
		CollectionOfItemsSource source = new CollectionOfItemsSource(coll, getIdentity(), ureq.getUserSession().getRoles());
		if(collItemsCtrl == null) {
			collItemsCtrl = new CollectionQuestionsController(ureq, getWindowControl(), source);
			collItemsCtrl.setStackedController(stackPanel);
			listenTo(collItemsCtrl);
		} else {
			collItemsCtrl.updateSource(ureq, source);
		}
		currentCtrl = collItemsCtrl;
		content.setContent(collItemsCtrl.getInitialComponent());
	}
	
	private TreeModel buildTreeModel() {
		QuestionPoolMenuTreeModel gtm = new QuestionPoolMenuTreeModel();
		GenericTreeNode rootNode = new GenericTreeNode(translate("topnav.qpool"), "topnav.qpool.alt");
		rootNode.setCssClass("o_sel_qpool_home");
		gtm.setRootNode(rootNode);
		
		//question database
		myNode = new GenericTreeNode(translate("menu.database"), "menu.database");
		myNode.setCssClass("o_sel_qpool_database");
		rootNode.addChild(myNode);
		buildMySubTreeModel(myNode);

		//pools + shares
		sharesNode = new GenericTreeNode(translate("menu.share"), "menu.share");
		sharesNode.setCssClass("o_sel_qpool_shares");
		rootNode.addChild(sharesNode);	
		buildShareSubTreeModel(sharesNode);
		
		//administration
		if(roles.isOLATAdmin() || roles.isPoolAdmin()) {
			GenericTreeNode adminNode = new GenericTreeNode(translate("menu.admin"), "menu.admin");
			adminNode.setCssClass("o_sel_qpool_admin");
			rootNode.addChild(adminNode);
			buildAdminSubTreeModel(adminNode);
		}
		return gtm;
	}
	
	private void buildShareSubTreeModel(GenericTreeNode parentNode) {
		parentNode.removeAllChildren();
		
		List<Pool> pools = qpoolService.getPools(getIdentity(), roles);
		for(Pool pool:pools) {
			GenericTreeNode node = new GenericTreeNode(pool.getName(), pool);
			node.setIconCssClass("o_sel_qpool_pool");
			parentNode.addChild(node);
		}

		List<BusinessGroup> groups = qpoolService.getResourcesWithSharedItems(getIdentity());
		for(BusinessGroup group:groups) {
			GenericTreeNode node = new GenericTreeNode(group.getName(), group);
			node.setIconCssClass("o_sel_qpool_share");
			parentNode.addChild(node);
		}
	}
	
	private void buildAdminSubTreeModel(GenericTreeNode parentNode) {
		if(!roles.isOLATAdmin() && !roles.isPoolAdmin()) return;
		parentNode.removeAllChildren();
		
		GenericTreeNode node = new GenericTreeNode(translate("menu.admin.studyfields"), "menu.admin.studyfields");
		node.setIconCssClass("o_sel_qpool_study_fields");
		parentNode.addChild(node);
		
		node = new GenericTreeNode(translate("menu.admin.pools"), "menu.admin.pools");
		node.setIconCssClass("o_sel_qpool_admin_pools");
		parentNode.addChild(node);
		
		node = new GenericTreeNode(translate("menu.admin.types"), "menu.admin.types");
		node.setIconCssClass("o_sel_qpool_admin_types");
		parentNode.addChild(node);
		
		node = new GenericTreeNode(translate("menu.admin.levels"), "menu.admin.levels");
		node.setIconCssClass("o_sel_qpool_admin_levels");
		parentNode.addChild(node);

		node = new GenericTreeNode(translate("menu.admin.licenses"), "menu.admin.licenses");
		node.setIconCssClass("o_sel_qpool_admin_licenses");
		parentNode.addChild(node);
	}
	
	private void buildMySubTreeModel(GenericTreeNode parentNode) {
		parentNode.removeAllChildren();
		
		GenericTreeNode node = new GenericTreeNode(translate("menu.database.my"), "menu.database.my");
		node.setIconCssClass("o_sel_qpool_my_items");
		parentNode.addChild(node);
		
		node = new GenericTreeNode(translate("menu.database.favorit"), "menu.database.favorit");
		node.setIconCssClass("o_sel_qpool_favorits");
		parentNode.addChild(node);
		
		List<QuestionItemCollection> collections = qpoolService.getCollections(getIdentity());
		for(QuestionItemCollection coll: collections) {
			node = new GenericTreeNode(coll.getName(), coll);
			node.setIconCssClass("o_sel_qpool_collection");
			parentNode.addChild(node);
		}
	}
}
