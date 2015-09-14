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

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeDropEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.ui.admin.PoolsAdminController;
import org.olat.modules.qpool.ui.admin.QEducationalContextsAdminController;
import org.olat.modules.qpool.ui.admin.QItemTypesAdminController;
import org.olat.modules.qpool.ui.admin.QLicensesAdminController;
import org.olat.modules.qpool.ui.admin.TaxonomyAdminController;
import org.olat.modules.qpool.ui.datasource.CollectionOfItemsSource;
import org.olat.modules.qpool.ui.datasource.DefaultItemsSource;
import org.olat.modules.qpool.ui.datasource.MarkedItemsSource;
import org.olat.modules.qpool.ui.datasource.MyItemsSource;
import org.olat.modules.qpool.ui.datasource.PoolItemsSource;
import org.olat.modules.qpool.ui.datasource.SharedItemsSource;
import org.olat.modules.qpool.ui.events.QItemMarkedEvent;
import org.olat.modules.qpool.ui.events.QPoolEvent;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolMainEditorController extends BasicController implements Activateable2, BreadcrumbPanelAware {

	public static final OLATResourceable QITEM_MARKED = OresHelper.createOLATResourceableType("QItemMark");
	
	private final MenuTree menuTree;
	private GenericTreeNode sharesNode, myNode, myOwnNode;
	private final Panel content;
	private BreadcrumbPanel stackPanel;

	private QuestionsController currentCtrl;
	private QuestionsController myQuestionsCtrl;
	private QuestionsController markedQuestionsCtrl;
	
	private Controller presentationCtrl, sharePresentationCtrl;
	private CloseableModalController cmc;
	private PoolsAdminController poolAdminCtrl;
	private QItemTypesAdminController typesCtrl;
	private QEducationalContextsAdminController levelsCtrl;
	private QLicensesAdminController licensesCtrl;
	private TaxonomyAdminController taxonomyCtrl;
	private ShareItemOptionController shareItemsCtrl;
	private LayoutMain3ColsController columnLayoutCtr;
	private QuestionPoolAdminStatisticsController adminStatisticsCtrl;
	private DialogBoxController copyToMyCtrl;

	private final Roles roles;
	private final MarkManager markManager;
	private final QPoolService qpoolService;
	private final BusinessGroupService businessGroupService;
	
	public QuestionPoolMainEditorController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		roles = ureq.getUserSession().getRoles();
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		menuTree = new MenuTree("qpoolTree");
		menuTree.setTreeModel(buildTreeModel());
		menuTree.setSelectedNode(menuTree.getTreeModel().getRootNode());
		menuTree.setDragEnabled(false);
		menuTree.setDropEnabled(true);
		menuTree.setDropSiblingEnabled(false);
		menuTree.setDndAcceptJSMethod("treeAcceptDrop");
		menuTree.setExpandSelectedNode(false);
		menuTree.addListener(this);
		menuTree.setRootVisible(false);
		//open the nodes shared and my at start
		List<String> openNodeIds = new ArrayList<String>(2);
		openNodeIds.add(myNode.getIdent());
		openNodeIds.add(sharesNode.getIdent());
		menuTree.setOpenNodeIds(openNodeIds);
		
		content = new Panel("list");
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, content, "qpool");
		
		doSelectPresentation(ureq);
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
		if(myQuestionsCtrl != null) myQuestionsCtrl.setBreadcrumbPanel(stackPanel);
		if(markedQuestionsCtrl != null) markedQuestionsCtrl.setBreadcrumbPanel(stackPanel);
		if(currentCtrl != null) currentCtrl.setBreadcrumbPanel(stackPanel);
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
				doDrop(ureq, targetId, dropId);
			} else if(menuTree.getSelectedNode() != null){
				TreeNode node = menuTree.getSelectedNode();
				doSelectControllerTreeNode(ureq, node, null, null);
			}
		}
	}
	
	private void doSelectControllerTreeNode(UserRequest ureq, TreeNode node, List<ContextEntry> entries, StateEntry state) {
		Object uNode = node.getUserObject();
		if("Presentation".equals(uNode)) {
			doSelectPresentation(ureq);
		} else if("SharePresentation".equals(uNode)) {
			doSelectSharePresentation(ureq);
		} else if("Statistics".equals(uNode)) {
			doSelectAdmin(ureq, entries, state);
		} else if("Taxonomy".equals(uNode)) {
			doSelectAdminStudyFields(ureq, entries, state);
		} else if("Pools".equals(uNode)) {
			doSelectAdminPools(ureq, entries, state);
		} else if("Types".equals(uNode)) {
			doSelectAdminTypes(ureq, entries, state);
		} else if("EduContexts".equals(uNode)) {
			doSelectAdminLevels(ureq, entries, state);
		} else if("Licenses".equals(uNode)) {
			doSelectAdminLicenses(ureq, entries, state);
		} else if("My".equals(uNode)) {
			doSelectMyQuestions(ureq, entries, state);
		} else if("Marked".equals(uNode)) {
			doSelectMarkedQuestions(ureq, entries, state);
		} else if(uNode instanceof Pool) {
			Pool pool = (Pool)uNode;
			doSelectPool(ureq, pool, node, entries, state);
		} else if(uNode instanceof BusinessGroup) {
			BusinessGroup group = (BusinessGroup)uNode;
			doSelectGroup(ureq, group, node, entries, state);
		} else if(uNode instanceof QuestionItemCollection) {
			QuestionItemCollection coll = (QuestionItemCollection)uNode;
			doSelectCollection(ureq, coll, node, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(shareItemsCtrl == source) {
			if(QPoolEvent.ITEM_SHARED.equals(event.getCommand())) {
				List<QuestionItemShort> items = shareItemsCtrl.getItems();
				if(items.size() > 0) {//can only drop one item
					QuestionItemShort item = items.get(0);
					if(shareItemsCtrl.getGroups() != null) {
						showInfo("item.shared", item.getTitle());
					} else if(shareItemsCtrl.getPools() != null) {
						showInfo("item.pooled", item.getTitle());
					}
				}
				buildShareSubTreeModel(sharesNode);
				menuTree.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(event instanceof QPoolEvent) {
			if(QPoolEvent.ITEM_SHARED.equals(event.getCommand())) {
				buildShareSubTreeModel(sharesNode);
				menuTree.setDirty(true);
			}	else if(QPoolEvent.COLL_CREATED.equals(event.getCommand())
					|| QPoolEvent.COLL_CHANGED.equals(event.getCommand())) {
				buildMySubTreeModel(myNode);
				Long collKey = ((QPoolEvent)event).getObjectKey();
				GenericTreeNode nodeToSelect = findNodeByPersistableUserObject(myNode, collKey);
				if(nodeToSelect != null) {
					menuTree.setSelectedNode(nodeToSelect);
					QuestionItemCollection coll = (QuestionItemCollection)nodeToSelect.getUserObject();
					doSelectCollection(ureq, coll, nodeToSelect, null, null);
				}
				menuTree.setDirty(true);
			}	else if(QPoolEvent.COLL_DELETED.equals(event.getCommand())) {
				buildMySubTreeModel(myNode);
				menuTree.setSelectedNode(myOwnNode);
				doSelectMyQuestions(ureq, null, null);
			} else if(QPoolEvent.POOL_CREATED.equals(event.getCommand())
					|| QPoolEvent.POOL_DELETED.equals(event.getCommand())) {
				buildShareSubTreeModel(sharesNode);
				menuTree.setDirty(true);
			}
		} else if(copyToMyCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				QuestionItemShort item = (QuestionItemShort)copyToMyCtrl.getUserObject();
				doCopyToMy(item);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(shareItemsCtrl);
		removeAsListenerAndDispose(cmc);
		shareItemsCtrl = null;
		cmc = null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		OLATResourceable resource = entry.getOLATResourceable();
		TreeNode rootNode = menuTree.getTreeModel().getRootNode();
		TreeNode node = TreeHelper.findNodeByResourceableUserObject(resource, rootNode);
		if(node == null) {
			node = TreeHelper.findNodeByUserObject(resource.getResourceableTypeName(),  rootNode);
		}
		if(node != null) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			stackPanel.popUpToRootController(ureq);
			doSelectControllerTreeNode(ureq, node, subEntries, entry.getTransientState());
			menuTree.setSelectedNode(node);
		}
	}
	
	private void doDrop(UserRequest ureq, String targetId, String dropId) {
		try {
			int lastIndex = dropId.lastIndexOf('-');
			String rowStr = dropId.substring(lastIndex+1, dropId.length());
			int row = Integer.parseInt(rowStr);
			QuestionItemShort item = currentCtrl.getQuestionAt(row);
			TreeNode node = menuTree.getTreeModel().getNodeById(targetId);
			if(node != null) {
				Object userObj = node.getUserObject();
				if(userObj instanceof BusinessGroup) {
					doShareItemsOptions(ureq, singletonList(item), singletonList((BusinessGroup)userObj), null);
				} else if(userObj instanceof Pool) {
					doShareItemsOptions(ureq, singletonList(item), null, singletonList((Pool)userObj));
				} else if(userObj instanceof QuestionItemCollection) {
					qpoolService.addItemToCollection(singletonList(item), singletonList((QuestionItemCollection)userObj));
					showInfo("item.collectioned", item.getTitle());
				} else if("My".equals(userObj)) {
					doCopyToMyConfirmation(ureq, item);
				} else if("Marked".equals(userObj)) {
					String businessPath = "[QuestionItem:" + item.getKey() + "]";
					markManager.setMark(item, getIdentity(), null, businessPath);
					QItemMarkedEvent event = new QItemMarkedEvent("marked", item.getKey(), true);
					ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(event, QITEM_MARKED);
				}
			}
		} catch (Exception e) {
			logError("Cannot drop with id: " + dropId, e);
		}
	}
	
	private void doShareItemsOptions(UserRequest ureq, List<QuestionItemShort> items, List<BusinessGroup> groups, List<Pool> pools) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(shareItemsCtrl);
		shareItemsCtrl = new ShareItemOptionController(ureq, getWindowControl(), items, groups, pools);
		listenTo(shareItemsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				shareItemsCtrl.getInitialComponent(), true, translate("share.item"));
		cmc.activate();
		listenTo(cmc);	
	}
	
	private void doCopyToMyConfirmation(UserRequest ureq, QuestionItemShort item) {
		String title = translate("copy");
		String text = translate("copy.confirmation");
		copyToMyCtrl = activateYesNoDialog(ureq, title, text, copyToMyCtrl);
		copyToMyCtrl.setUserObject(item);
	}
	
	private void doCopyToMy(QuestionItemShort item) {
		List<QuestionItem> copiedItems = qpoolService.copyItems(getIdentity(), singletonList(item));
		showInfo("item.copied", Integer.toString(copiedItems.size()));
		if(myQuestionsCtrl != null) {
			myQuestionsCtrl.updateSource();
		}
	}
	
	private void setContent(UserRequest ureq, Controller controller, List<ContextEntry> entries, StateEntry state) {
		addToHistory(ureq, controller);
		if(controller instanceof Activateable2) {
			((Activateable2)controller).activate(ureq, entries, state);
		}
		content.setContent(controller.getInitialComponent());
	}
	
	private void doSelectPresentation(UserRequest ureq) {
		if(presentationCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Presentation"), null);
			presentationCtrl = new PresentationController(ureq, swControl);
			listenTo(presentationCtrl);
		} 
		setContent(ureq, presentationCtrl, null, null);
	}
	
	private void doSelectSharePresentation(UserRequest ureq) {
		if(sharePresentationCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("SharePresentation"), null);
			sharePresentationCtrl = new SharePresentationController(ureq, swControl);
			listenTo(sharePresentationCtrl);
		} 
		setContent(ureq, sharePresentationCtrl, null, null);
	}
	
	private void doSelectAdmin(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(adminStatisticsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Statistics"), null);
			adminStatisticsCtrl = new QuestionPoolAdminStatisticsController(ureq, swControl);
			listenTo(adminStatisticsCtrl);
		} 
		setContent(ureq, adminStatisticsCtrl, entries, state);
	}
	
	private void doSelectAdminStudyFields(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(taxonomyCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Taxonomy"), null);
			taxonomyCtrl = new TaxonomyAdminController(ureq, swControl);
			listenTo(taxonomyCtrl);
		}
		setContent(ureq, taxonomyCtrl, entries, state);
	}
	
	private void doSelectAdminPools(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(poolAdminCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Pools"), null);
			poolAdminCtrl = new PoolsAdminController(ureq, swControl);
			listenTo(poolAdminCtrl);
		}
		setContent(ureq, poolAdminCtrl, entries, state);
	}
	
	private void doSelectAdminTypes(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(typesCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Types"), null);
			typesCtrl = new QItemTypesAdminController(ureq, swControl);
			listenTo(typesCtrl);
		}
		setContent(ureq, typesCtrl, entries, state);
	}
	
	private void doSelectAdminLevels(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(levelsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("EduContexts"), null);
			levelsCtrl = new QEducationalContextsAdminController(ureq, swControl);
			listenTo(levelsCtrl);
		}
		setContent(ureq, levelsCtrl, entries, state);
	}
	
	private void doSelectAdminLicenses(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(licensesCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Licenses"), null);
			licensesCtrl = new QLicensesAdminController(ureq, swControl);
			listenTo(licensesCtrl);
		}
		setContent(ureq, licensesCtrl, entries, state);
	}
	
	private void doSelectMyQuestions(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		DefaultItemsSource source = new MyItemsSource(getIdentity(), ureq.getUserSession().getRoles(), "My"); 
		if(myQuestionsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("My"), null);
			myQuestionsCtrl = new QuestionsController(ureq, swControl, source, "my");
			myQuestionsCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(myQuestionsCtrl);
		} else {
			myQuestionsCtrl.updateSource(source);
		}
		currentCtrl = myQuestionsCtrl;
		setContent(ureq, myQuestionsCtrl, entries, state);
	}
	
	private void doSelectMarkedQuestions(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		DefaultItemsSource source = new MarkedItemsSource(getIdentity(), ureq.getUserSession().getRoles(), "Fav"); 
		if(markedQuestionsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Marked"), null);
			markedQuestionsCtrl = new QuestionsController(ureq, swControl, source, "favorit");
			markedQuestionsCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(markedQuestionsCtrl);
		} else {
			markedQuestionsCtrl.updateSource(source);
		}
		currentCtrl = markedQuestionsCtrl;
		setContent(ureq, markedQuestionsCtrl, entries, state);
	}
	
	private void doSelectPool(UserRequest ureq, Pool pool, TreeNode node, List<ContextEntry> entries, StateEntry state) {
		ControlledTreeNode cNode = (ControlledTreeNode)node;
		QuestionsController selectedPoolCtrl = cNode.getController();

		DefaultItemsSource source = new PoolItemsSource(getIdentity(), roles, pool);
		source.setRemoveEnabled(isShareAdmin(pool));
		if(selectedPoolCtrl == null) {
			WindowControl swControl = addToHistory(ureq, pool, null);
			selectedPoolCtrl = new QuestionsController(ureq, swControl, source, "poll-" + pool.getKey());
			selectedPoolCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(selectedPoolCtrl);
			cNode.setController(selectedPoolCtrl);
		} else {
			selectedPoolCtrl.updateSource(source);
		}
		currentCtrl = selectedPoolCtrl;
		setContent(ureq, selectedPoolCtrl, entries, state);
	}
	
	/**
	 * Can administrate if has role OLAT admin or Pool admin, if the pool is public,
	 * if owner of the pool
	 * @param pool
	 * @return
	 */
	private boolean isShareAdmin(Pool pool) {
		return roles != null && (roles.isOLATAdmin() || roles.isPoolAdmin() || pool.isPublicPool()
				|| qpoolService.isOwner(getIdentity(), pool));
	}
	
	private void doSelectGroup(UserRequest ureq, BusinessGroup group, TreeNode node, List<ContextEntry> entries, StateEntry state) {
		ControlledTreeNode cNode = (ControlledTreeNode)node;
		QuestionsController sharedItemsCtrl = cNode.getController();
		boolean shareAdmin = isShareAdmin(group);
		SharedItemsSource source = new SharedItemsSource(group, getIdentity(), roles, shareAdmin);

		if(sharedItemsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, group, null);
			sharedItemsCtrl = new QuestionsController(ureq, swControl, source, "share-" + group.getKey());
			sharedItemsCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(sharedItemsCtrl);
			cNode.setController(sharedItemsCtrl);
		} else {
			sharedItemsCtrl.updateSource(source);
		}
		currentCtrl = sharedItemsCtrl;
		setContent(ureq, sharedItemsCtrl, entries, state);
	}
	
	private boolean isShareAdmin(BusinessGroup group) {
		return roles != null && (roles.isOLATAdmin() || roles.isPoolAdmin()
				|| businessGroupService.isIdentityInBusinessGroup(getIdentity(), group.getKey(), true, false, null));
	}
	
	private void doSelectCollection(UserRequest ureq, QuestionItemCollection coll, TreeNode node, List<ContextEntry> entries, StateEntry state) {
		ControlledTreeNode cNode = (ControlledTreeNode)node;
		QuestionsController collItemsCtrl = cNode.getController();
		
		CollectionOfItemsSource source = new CollectionOfItemsSource(coll, getIdentity(), ureq.getUserSession().getRoles());
		if(collItemsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, coll, null);
			collItemsCtrl = new QuestionsController(ureq, swControl, source, "coll-" + coll.getKey());
			collItemsCtrl.setQuestionItemCollection(coll);
			collItemsCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(collItemsCtrl);
			cNode.setController(collItemsCtrl);
		} else {
			collItemsCtrl.updateSource(source);
		}
		collItemsCtrl.activate(ureq, entries, state);
		currentCtrl = collItemsCtrl;
		setContent(ureq, collItemsCtrl, entries, state);
	}
	
	private TreeModel buildTreeModel() {
		QuestionPoolMenuTreeModel gtm = new QuestionPoolMenuTreeModel();
		GenericTreeNode rootNode = new GenericTreeNode(translate("topnav.qpool"), "topnav.qpool.alt");
		rootNode.setCssClass("o_sel_qpool_home");
		gtm.setRootNode(rootNode);
		
		//question database
		myNode = new GenericTreeNode(translate("menu.database"), "Presentation");
		myNode.setCssClass("o_sel_qpool_database");
		rootNode.addChild(myNode);
		buildMySubTreeModel(myNode);

		//pools + shares
		sharesNode = new GenericTreeNode(translate("menu.share"), "SharePresentation");
		sharesNode.setCssClass("o_sel_qpool_shares");
		rootNode.addChild(sharesNode);	
		buildShareSubTreeModel(sharesNode);
		
		//administration
		if(roles.isOLATAdmin() || roles.isPoolAdmin()) {
			GenericTreeNode adminNode = new GenericTreeNode(translate("menu.admin"), "Statistics");
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
			GenericTreeNode node = new ControlledTreeNode(pool.getName(), pool);
			node.setIconCssClass("o_icon_pool_pool o_sel_qpool_pool");
			parentNode.addChild(node);
		}

		List<BusinessGroup> groups = qpoolService.getResourcesWithSharedItems(getIdentity());
		for(BusinessGroup group:groups) {
			GenericTreeNode node = new ControlledTreeNode(group.getName(), group);
			node.setIconCssClass("o_icon_pool_share o_sel_qpool_share");
			parentNode.addChild(node);
		}
	}
	
	private void buildAdminSubTreeModel(GenericTreeNode parentNode) {
		if(!roles.isOLATAdmin() && !roles.isPoolAdmin()) return;
		parentNode.removeAllChildren();
		
		GenericTreeNode node = new GenericTreeNode(translate("menu.admin.studyfields"), "Taxonomy");
		node.setIconCssClass("o_sel_qpool_study_fields");
		parentNode.addChild(node);
		parentNode.setDelegate(node);
		
		node = new GenericTreeNode(translate("menu.admin.pools"), "Pools");
		node.setIconCssClass("o_sel_qpool_admin_pools");
		parentNode.addChild(node);
		
		node = new GenericTreeNode(translate("menu.admin.types"), "Types");
		node.setIconCssClass("o_sel_qpool_admin_types");
		parentNode.addChild(node);
		
		node = new GenericTreeNode(translate("menu.admin.levels"), "EduContexts");
		node.setIconCssClass("o_sel_qpool_admin_levels");
		parentNode.addChild(node);

		node = new GenericTreeNode(translate("menu.admin.licenses"), "Licenses");
		node.setIconCssClass("o_sel_qpool_admin_licenses");
		parentNode.addChild(node);
	}
	
	private void buildMySubTreeModel(GenericTreeNode parentNode) {
		parentNode.removeAllChildren();
		
		myOwnNode = new GenericTreeNode(translate("menu.database.my"), "My");
		myOwnNode.setIconCssClass("o_icon_pool_my_items o_sel_qpool_my_items");
		parentNode.addChild(myOwnNode);
		
		GenericTreeNode node = new GenericTreeNode(translate("menu.database.favorit"), "Marked");
		node.setIconCssClass("o_icon_pool_favorits o_sel_qpool_favorits");
		parentNode.addChild(node);
		
		List<QuestionItemCollection> collections = qpoolService.getCollections(getIdentity());
		for(QuestionItemCollection coll: collections) {
			node = new ControlledTreeNode(coll.getName(), coll);
			node.setIconCssClass("o_icon_pool_collection o_sel_qpool_collection");
			parentNode.addChild(node);
		}
	}
	
	private GenericTreeNode findNodeByPersistableUserObject(GenericTreeNode parentNode, Long id) {
		if(parentNode == null || id == null) {
			return null;
		}
		
		for(int i=parentNode.getChildCount(); i-->0; ) {
			INode node = parentNode.getChildAt(i);
			if(node instanceof GenericTreeNode) {
				GenericTreeNode treeNode = (GenericTreeNode)node;
				Object userObj = treeNode.getUserObject();
				if(userObj instanceof Persistable) {
					Persistable obj = (Persistable)userObj;
					if(id.equals(obj.getKey())) {
						return treeNode;
					}
				}
			}
		}
		return null;
	}
	
	
	private static class ControlledTreeNode extends GenericTreeNode {
		private static final long serialVersionUID = 768640290449143804L;
		private QuestionsController controller;
		
		public ControlledTreeNode(String title, Object userObject) {
			super(title, userObject);
		}

		public QuestionsController getController() {
			return controller;
		}

		public void setController(QuestionsController controller) {
			this.controller = controller;
		}
	}
}
