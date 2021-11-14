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

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeDropEvent;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItem2Pool;
import org.olat.modules.qpool.QuestionItem2Resource;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.ui.events.QItemMarkedEvent;
import org.olat.modules.qpool.ui.events.QPoolEvent;
import org.olat.modules.qpool.ui.tree.CollectionTreeNode;
import org.olat.modules.qpool.ui.tree.ControllerTreeNode;
import org.olat.modules.qpool.ui.tree.MarkedQuestionsTreeNode;
import org.olat.modules.qpool.ui.tree.MyQuestionsTreeNode;
import org.olat.modules.qpool.ui.tree.QuestionPoolMenuTreeModel;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolMainEditorController extends BasicController implements Activateable2 {

	public static final OLATResourceable QITEM_MARKED = OresHelper.createOLATResourceableType("QItemMark");
	
	private final QuestionPoolMenuTreeModel treeModel;
	private final MenuTree menuTree;
	private final Panel content;
	private final TooledStackedPanel stackPanel;

	private Controller currentCtrl;
	
	private CloseableModalController cmc;
	private ShareItemOptionController shareItemsCtrl;
	private LayoutMain3ColsController columnLayoutCtr;
	private DialogBoxController copyToMyCtrl;

	private final MarkManager markManager;
	private final QPoolService qpoolService;
	
	public QuestionPoolMainEditorController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;

		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		
		treeModel = new QuestionPoolMenuTreeModel(stackPanel, getIdentity(), ureq.getUserSession().getRoles(), ureq.getLocale());
		menuTree = new MenuTree("qpoolTree");
		menuTree.setTreeModel(treeModel);
		menuTree.setSelectedNode(menuTree.getTreeModel().getRootNode());
		menuTree.setDragEnabled(false);
		menuTree.setDropEnabled(true);
		menuTree.setDropSiblingEnabled(false);
		menuTree.setDndAcceptJSMethod("treeAcceptDrop");
		menuTree.setExpandSelectedNode(false);
		menuTree.addListener(this);
		menuTree.setRootVisible(false);
		menuTree.setOpenNodeIds(treeModel.getDefaultOpenNodeIds());
		
		content = new Panel("list");
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, content, "qpool");
		columnLayoutCtr.addCssClassToMain("o_qpool");
		
		doSelectControllerTreeNode(ureq, treeModel.getMyNode(), null, null);
		putInitialPanel(columnLayoutCtr.getInitialComponent());
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
			} else if (event instanceof TreeEvent){
				TreeEvent te = (TreeEvent) event;
				if	(MenuTree.COMMAND_TREENODE_CLICKED.equals(te.getCommand())) {
					TreeNode node = menuTree.getTreeModel().getNodeById(te.getNodeId());
					doSelectControllerTreeNode(ureq, node, null, null);
				}
			}
		}
	}

	private void doSelectControllerTreeNode(UserRequest ureq, TreeNode node, List<ContextEntry> entries, StateEntry state) {
		TreeNode nodeToShow = getDelegate(node);
		menuTree.setSelectedNode(nodeToShow);
		if (nodeToShow instanceof ControllerTreeNode) {
			ControllerTreeNode cNode = (ControllerTreeNode) nodeToShow;
			doSelectQuestionsNode(ureq, cNode, entries, state);
		}
	}
	
	private TreeNode getDelegate(TreeNode node) {
		if (node != null && node.getDelegate() != null) {
			getDelegate(node.getDelegate());
		}
		return node;
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
				treeModel.buildShareSubTreeModel();
				menuTree.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(event instanceof QPoolEvent) {
			if(QPoolEvent.ITEM_SHARED.equals(event.getCommand())) {
				treeModel.buildShareSubTreeModel();
				menuTree.setDirty(true);
			}	else if(QPoolEvent.COLL_CREATED.equals(event.getCommand())
					|| QPoolEvent.COLL_CHANGED.equals(event.getCommand())) {
				treeModel.buildMySubTreeModel();
				Long collKey = ((QPoolEvent)event).getObjectKey();
				CollectionTreeNode qNode = (CollectionTreeNode) findNodeByPersistableUserObject(treeModel.getMyNode(), collKey);
				if(qNode != null) {
					menuTree.setSelectedNode(qNode);
					doSelectQuestionsNode(ureq, qNode, null, null);
				}
				menuTree.setDirty(true);
			}	else if(QPoolEvent.COLL_DELETED.equals(event.getCommand())) {
				treeModel.buildMySubTreeModel();
				menuTree.setSelectedNode(treeModel.getMyNode());
				doSelectControllerTreeNode(ureq, treeModel.getMyNode(), null, null);
			} else if(QPoolEvent.POOL_CREATED.equals(event.getCommand())
					|| QPoolEvent.POOL_DELETED.equals(event.getCommand())) {
				treeModel.buildShareSubTreeModel();
				menuTree.setDirty(true);
			} else if (QPoolEvent.ITEMS_RELOADED.equals(event.getCommand())) {
				treeModel.reloadReviewCounts();
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

		OLATResourceable resource = entries.get(0).getOLATResourceable();
		if("QuestionItem".equalsIgnoreCase(resource.getResourceableTypeName())) {
			activateQuestionItem(ureq, resource.getResourceableId(), entries);
		} else {
			TreeNode rootNode = menuTree.getTreeModel().getRootNode();
			TreeNode node = TreeHelper.findNodeByResourceableUserObject(resource, rootNode);
			if(node == null) {
				node = TreeHelper.findNodeByUserObject(resource.getResourceableTypeName(),  rootNode);
			}
			if(node != null) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				stackPanel.popUpToRootController(ureq);
				doSelectControllerTreeNode(ureq, node, subEntries, entries.get(0).getTransientState());
				menuTree.setSelectedNode(node);
			}
		}
	}
	
	private void activateQuestionItem(UserRequest ureq, Long itemKey, List<ContextEntry> entries) {
		QuestionItem item = qpoolService.loadItemById(itemKey);
		if(item == null) return;

		List<Identity> authors = qpoolService.getAuthors(item);
		if(authors.contains(getIdentity())) {	
			activateNode(ureq, treeModel.getMyQuestionsNode(), entries);
			return;
		}
		
		if(item.getTaxonomyLevel() != null && item.getQuestionStatus() != null
				&& QuestionStatus.finalVersion.equals(item.getQuestionStatus()) && treeModel.getFinalNode() != null) {
			TreeNode levelNode = treeModel.getFinalTanonomyLevelNode(item.getTaxonomyLevel());
			if(levelNode != null) {
				activateNode(ureq, levelNode, entries);
				return;
			}
		}
		
		if(treeModel.getSharesNode() != null) {
			List<QuestionItem2Resource> shares = qpoolService.getSharedResourceInfosByItem(item);
			for(QuestionItem2Resource share:shares) {
				TreeNode levelNode = treeModel.getShareNode(share);
				if(levelNode != null) {
					activateNode(ureq, levelNode, entries);
					return;
				}
			}
			
			List<QuestionItem2Pool> pools = qpoolService.getPoolInfosByItem(item);
			for(QuestionItem2Pool pool:pools) {
				TreeNode levelNode = treeModel.getShareNode(pool);
				if(levelNode != null) {
					activateNode(ureq, levelNode, entries);
					return;
				}
			}
		}
	}
	
	/**
	 * 
	 * @param ureq The user request
	 * @param nodeToActivate The tree node to activate
	 * @param entries The entries which map to the question
	 */
	private void activateNode(UserRequest ureq, TreeNode nodeToActivate, List<ContextEntry> entries) {
		if(nodeToActivate == null || entries == null || entries.size() < 1) return;
		
		stackPanel.popUpToRootController(ureq);
		if(entries == null || entries.isEmpty()) {
			doSelectControllerTreeNode(ureq, nodeToActivate, null, null);
		} else {
			doSelectControllerTreeNode(ureq, nodeToActivate, entries, entries.get(0).getTransientState());
		}
		menuTree.setSelectedNode(nodeToActivate);
	}
	
	private void doDrop(UserRequest ureq, String targetId, String dropId) {
		try {
			int lastIndex = dropId.lastIndexOf('-');
			String rowStr = dropId.substring(lastIndex+1, dropId.length());
			int row = Integer.parseInt(rowStr);
			QuestionItemShort item = ((QuestionsController)currentCtrl).getQuestionAt(row);
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
				} else if(node instanceof MyQuestionsTreeNode) {
					doCopyToMyConfirmation(ureq, item);
				} else if(node instanceof MarkedQuestionsTreeNode) {
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
		for (QuestionItem copy: copiedItems) {
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.CREATE_QUESTION_ITEM_BY_COPY);
			builder.withAfter(copy);
			qpoolService.persist(builder.create());
		}
		showInfo("item.copied", Integer.toString(copiedItems.size()));
		if (currentCtrl instanceof QuestionsController) {
			((QuestionsController)currentCtrl).updateSource();
		}
	}
	
	private void setContent(UserRequest ureq, Controller controller, List<ContextEntry> entries, StateEntry state) {
		addToHistory(ureq, controller);
		if(controller instanceof Activateable2) {
			((Activateable2)controller).activate(ureq, entries, state);
		}
		content.setContent(controller.getInitialComponent());
	}
	
	private void doSelectQuestionsNode(UserRequest ureq, ControllerTreeNode qNode, List<ContextEntry> entries,
			StateEntry state) {
		currentCtrl = qNode.getController(ureq, getWindowControl());
		if(currentCtrl instanceof BreadcrumbPanelAware) {
			((BreadcrumbPanelAware)currentCtrl).setBreadcrumbPanel(stackPanel);
		}
		listenTo(currentCtrl);
		setContent(ureq, currentCtrl, entries, state);
	}
	
	private TreeNode findNodeByPersistableUserObject(TreeNode parentNode, Long id) {
		if(parentNode == null || id == null) {
			return null;
		}
		
		for(int i=parentNode.getChildCount(); i-->0; ) {
			INode node = parentNode.getChildAt(i);
			if(node instanceof TreeNode) {
				TreeNode treeNode = (TreeNode)node;
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
}
