/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.group.ui.context;

import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.management.BGManagementController;

/**
 * Description:<BR>
 * Management controller for the group context. A group context is a container
 * for groups and learning areas. Group contexts are associated with
 * OLATResources, currently only with courses. Default contexts belong only to
 * one resource, regular contexts can be associated with many courses, they then
 * share the same groupmanagement. With this controller this group contexts can
 * be created and managed.
 * <P>
 * 
 * Initial Date: Jan 24, 2005
 * @author gnaegi
 */
public class BGContextManagementController extends MainLayoutBasicController implements Activateable, Activateable2 {

	// Menu commands
	private static final String CMD_INDEX = "cmd.index";
	private static final String CMD_CONTEXTLIST = "cmd.contextlist";
	// Toolbox commands
	private static final String CMD_LG_CONTEXT_CREATE = "cmd.learninggroup.context.create";
	private static final String CMD_RG_CONTEXT_CREATE = "cmd.rightgroup.context.create";
	// List commands
	private static final String CMD_CONTEXT_RUN = "cmd.context.run";
	private static final String CMD_CONTEXT_EDIT = "cmd.context.edit";
	private static final String CMD_CONTEXT_DELETE = "cmd.context.delete";

	private VelocityContainer indexVC, newContextVC, contextListVC;
	private TableController contextListCtr;
	private BGContextTableModel contextTableModel;
	private DialogBoxController confirmDeleteContext;

	// Layout components and controllers

	private Panel content;
	private LayoutMain3ColsController columnLayoutCtr;
	private MenuTree olatMenuTree;
	private ToolController toolC;

	// Managers
	private BGContextManagerImpl contextManager;

	// components
	private BGContextFormController newContextController;

	// Workflow variables
	private BGManagementController groupManagementController;
	private BGContextEditController contextEditCtr;
	private BGContext currentGroupContext;

	/**
	 * Constructor for a business group management controller.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 */
	public BGContextManagementController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		// Initialize managers
		this.contextManager = (BGContextManagerImpl)BGContextManagerImpl.getInstance();
		// Initialize all velocity containers
		initVC();

		// Layout is controlled with generic controller: menu - content - tools
		// Navigation menu
		this.olatMenuTree = new MenuTree("olatMenuTree");
		TreeModel tm = buildTreeModel();
		this.olatMenuTree.setTreeModel(tm);
		this.olatMenuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		this.olatMenuTree.addListener(this);
		// Content
		this.content = new Panel("content");
		// Tools
		// 1 create empty Tools and init menuAndToolController
		// 2 set correct tools using setTools method (override step 1)
		this.toolC = ToolFactory.createToolController(getWindowControl());
		this.columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), this.olatMenuTree, this.toolC.getInitialComponent(), this.content, "groupcontextmngt");
		columnLayoutCtr.addCssClassToMain("o_groupsmanagement");
		
		listenTo(this.columnLayoutCtr);

		doIndex(ureq);

		putInitialPanel(this.columnLayoutCtr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		String cmd = event.getCommand();
		if (source == this.olatMenuTree) {
			if (cmd.equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				handleMenuCommands(ureq);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		String cmd = event.getCommand();
		if (source == this.toolC) {
			handleToolCommands(ureq, cmd);
		} else if (source == groupManagementController) {
			if (event == Event.DONE_EVENT) {
				getWindowControl().pop();
				//fxdiff BAKS-7 Resume function
				if(contextListCtr != null) {//de facto -> contextlist
					addToHistory(ureq, contextListCtr);
				}
			}
		} else if (source == this.confirmDeleteContext) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				// yes case
				doContextDelete();
				this.content.popContent();
				doContextList(ureq, true);
				MultiUserEvent mue = new BGContextEvent(BGContextEvent.CONTEXT_DELETED, this.currentGroupContext);
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(mue, this.currentGroupContext);
			}
		} else if (source == this.contextListCtr) {
			if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				this.currentGroupContext = this.contextTableModel.getGroupContextAt(rowid);
				if (actionid.equals(CMD_CONTEXT_EDIT)) {
					doContextEdit(ureq);
				} else if (actionid.equals(CMD_CONTEXT_RUN)) {
					doContextRun(ureq);
				} else if (actionid.equals(CMD_CONTEXT_DELETE)) {
					doContextDeleteConfirm(ureq);
				}
			}
		} else if (source == this.newContextController) {
			if (event == Event.DONE_EVENT) {
				BGContext newContext = doContextCreate(ureq);
				if (newContext == null) {
					throw new AssertException("Could not create new BGContext - unknown reason");
				} else {
					this.currentGroupContext = newContext;
					doContextEdit(ureq);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				doIndex(ureq);
			}
		}
	}

	private void handleToolCommands(UserRequest ureq, String cmd) {
		if (cmd.equals(CMD_LG_CONTEXT_CREATE)) {
			doContextCreateForm(ureq, BusinessGroup.TYPE_LEARNINGROUP);
		} else if (cmd.equals(CMD_RG_CONTEXT_CREATE)) {
			doContextCreateForm(ureq, BusinessGroup.TYPE_RIGHTGROUP);
		} else if (cmd.equals(CMD_CONTEXT_RUN)) {
			doContextRun(ureq);
		} else if (cmd.equals(CMD_CONTEXT_DELETE)) {
			doContextDeleteConfirm(ureq);
		}

	}

	private void handleMenuCommands(UserRequest ureq) {
		
		// remove lock from current context
		removeAsListenerAndDispose(contextEditCtr);

		TreeNode selTreeNode = this.olatMenuTree.getSelectedNode();
		String cmd = (String) selTreeNode.getUserObject();

		if (cmd.equals(CMD_INDEX)) {
			doIndex(ureq);
		} else if (cmd.equals(CMD_CONTEXTLIST)) {
			doContextList(ureq, true);
		}
	}

	private TreeModel buildTreeModel() {
		GenericTreeNode root, gtn;

		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode();
		root.setTitle(translate("menu.index"));
		root.setUserObject(CMD_INDEX);
		root.setAltText(translate("menu.index.alt"));
		gtm.setRootNode(root);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.allcontexts"));
		gtn.setUserObject(CMD_CONTEXTLIST);
		gtn.setAltText(translate("menu.allcontexts.alt"));
		root.addChild(gtn);

		return gtm;
	}

	private void setTools(boolean contextSelected) {
		removeAsListenerAndDispose(toolC);
		toolC = ToolFactory.createToolController(getWindowControl());
		listenTo(toolC);
		
		this.columnLayoutCtr.setCol2(this.toolC.getInitialComponent());
		this.toolC.addHeader(translate("tools.title.contextmanagement"));
		// Generic actions
		this.toolC.addLink(CMD_LG_CONTEXT_CREATE, translate(CMD_LG_CONTEXT_CREATE));
		this.toolC.addLink(CMD_RG_CONTEXT_CREATE, translate(CMD_RG_CONTEXT_CREATE));
		// context specific actions
		if (contextSelected) {
			this.toolC.addHeader(translate("tools.title.context"));
			this.toolC.addLink(CMD_CONTEXT_RUN, translate(CMD_CONTEXT_RUN));
			this.toolC.addLink(CMD_CONTEXT_DELETE, translate(CMD_CONTEXT_DELETE));
		}
	}

	private void initVC() {
		this.indexVC = createVelocityContainer("contextmanagement");
		// Create new context form
		this.newContextVC = createVelocityContainer("newcontext");
		// Context list
		this.contextListVC = createVelocityContainer("contextlist");
	}

	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, String viewIdentifier) {
		if(viewIdentifier != null && viewIdentifier.endsWith(":0")) {
			viewIdentifier = viewIdentifier.substring(0, viewIdentifier.length() - 2);
		}
		
		if(CMD_CONTEXTLIST.equals(viewIdentifier)) {
			TreeNode node = ((GenericTreeModel)olatMenuTree.getTreeModel()).findNodeByUserObject(CMD_CONTEXTLIST);
			olatMenuTree.setSelectedNode(node);
			doContextList(ureq, true);
		}
	}

	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.remove(0);
		String type = ce.getOLATResourceable().getResourceableTypeName();
		TreeNode node = ((GenericTreeModel)olatMenuTree.getTreeModel()).findNodeByUserObject(type);
		if(node != null) {
			olatMenuTree.setSelectedNode(node);
			
			handleMenuCommands(ureq);
			if(CMD_CONTEXTLIST.equals(ce.getOLATResourceable().getResourceableTypeName())) {
				//try to select a context if there is one
				if(!entries.isEmpty()) {
					ContextEntry groupCe = entries.remove(0);
					List<BGContext> contexts = contextTableModel.getObjects();
					for(BGContext context:contexts) {
						if(context.getKey().equals(groupCe.getOLATResourceable().getResourceableId())) {
							currentGroupContext = context;
							break;
						}
					}
					
					if(currentGroupContext != null) {
						doContextRun(ureq);
					}
				}
			}
		}
	}

	private void doIndex(UserRequest ureq) {
		this.content.setContent(this.indexVC);
		setTools(false);
		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CMD_INDEX, 0l);
		addToHistory(ureq, ores, null);
	}

	private void doContextCreateForm(UserRequest ureq, String type) {
		
		removeAsListenerAndDispose(this.newContextController);
		this.newContextController = new BGContextFormController(ureq, getWindowControl(), type, ureq.getUserSession().getRoles().isOLATAdmin());
		listenTo(this.newContextController);
		
		this.newContextVC.put("newContextForm", this.newContextController.getInitialComponent());
		this.content.setContent(this.newContextVC);
	}

	private BGContext doContextCreate(UserRequest ureq) {
		String name = this.newContextController.getName();
		String desc = this.newContextController.getDescription();
		String type = this.newContextController.getType();
		return this.contextManager.createAndPersistBGContext(name, desc, type, ureq.getIdentity(), false);
	}

	private void doContextEdit(UserRequest ureq) {
		
		// create new edit controller
		removeAsListenerAndDispose(contextEditCtr);
		contextEditCtr = new BGContextEditController(ureq, getWindowControl(), this.currentGroupContext);
		listenTo(contextEditCtr);
		
		if (this.contextEditCtr.isLockAcquired()) {
			this.content.setContent(this.contextEditCtr.getInitialComponent());
			setTools(true);
		}
	}

	private void doContextRun(UserRequest ureq) {
		removeAsListenerAndDispose(groupManagementController);
		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(BGContext.class, currentGroupContext.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null, contextListCtr.getWindowControlForDebug(), true);
		groupManagementController = BGControllerFactory.getInstance().createManagementController(ureq, bwControl, this.currentGroupContext, false);
		listenTo (groupManagementController);
		
		//FIXME fg: no layout ctr in a modal panel!
		getWindowControl().pushToMainArea(this.groupManagementController.getInitialComponent());
	}

	private void doContextDeleteConfirm(UserRequest ureq) {
		List resource = this.contextManager.findOLATResourcesForBGContext(this.currentGroupContext);
		if (resource.size() == 0) {
			this.confirmDeleteContext = activateYesNoDialog(ureq, null, translate(
					"context.delete.used.zero", this.currentGroupContext.getName() ), this.confirmDeleteContext);
		} else if (resource.size() == 1) {
			this.confirmDeleteContext = activateYesNoDialog(ureq, null, translate(
					"context.delete.used.one", this.currentGroupContext.getName() ), this.confirmDeleteContext);
		} else {
			this.confirmDeleteContext = activateYesNoDialog(ureq, null, getTranslator().translate(
					"context.delete.used.multi", new String[] { this.currentGroupContext.getName(), Integer.toString(resource.size()) }), this.confirmDeleteContext);
		}
	}

	private void doContextDelete() {
		this.contextManager.deleteBGContext(this.currentGroupContext);
	}

	private void doContextList(UserRequest ureq, boolean initializeModel) {
		// Init table only once
		if (this.contextListCtr == null) {
			TableGuiConfiguration tableConfig = new TableGuiConfiguration();
			tableConfig.setTableEmptyMessage(translate("contextlist.no.contexts"));
			// init group list filter controller
			removeAsListenerAndDispose(contextListCtr);
			//fxdiff BAKS-7 Resume function
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CMD_CONTEXTLIST, 0l);
			WindowControl bwControl = addToHistory(ureq, ores, null, getWindowControl(), false);
			contextListCtr = new TableController(tableConfig, ureq, bwControl, getTranslator());
			listenTo(contextListCtr);

			this.contextListCtr.addColumnDescriptor(new DefaultColumnDescriptor("contextlist.table.name", 0, CMD_CONTEXT_RUN, ureq.getLocale()));
			this.contextListCtr.addColumnDescriptor(new DefaultColumnDescriptor("contextlist.table.desc", 1, null, ureq.getLocale()));
			this.contextListCtr.addColumnDescriptor(new DefaultColumnDescriptor("contextlist.table.type", 2, null, ureq.getLocale()));
			this.contextListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_CONTEXT_EDIT, "contextlist.table.edit", 
					translate(CMD_CONTEXT_EDIT)));
			this.contextListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_CONTEXT_DELETE, "contextlist.table.delete", 
					translate(CMD_CONTEXT_DELETE)));
			this.contextListVC.put("contextlist", this.contextListCtr.getInitialComponent());
		}

		if (this.contextTableModel == null || initializeModel) {
			List contexts = this.contextManager.findBGContextsForIdentity(ureq.getIdentity(), false, true);
			this.contextTableModel = new BGContextTableModel(contexts, getTranslator(), true, false);
			this.contextListCtr.setTableDataModel(this.contextTableModel);
		}
		//fxdiff BAKS-7 Resume function
		addToHistory(ureq, contextListCtr);
		this.content.setContent(this.contextListVC);
		setTools(false);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// Controllers disposed by BasicController:
	}

	private void cleanupContextLock() {
		
	}
}