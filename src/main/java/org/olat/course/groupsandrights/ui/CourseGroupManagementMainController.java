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

package org.olat.course.groupsandrights.ui;

import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroup;
import org.olat.group.GroupLoggingAction;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.context.BGContextEditController;
import org.olat.group.ui.context.BGContextTableModel;
import org.olat.group.ui.management.BGManagementController;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<BR/> This controller searches for available group contexts for
 * this course. Currently only one context per grouptype per course is
 * supported. <P/>
 * 
 * Initial Date: Aug 25, 2004
 * @author gnaegi
 */
public class CourseGroupManagementMainController extends MainLayoutBasicController {
	private static final String CMD_CLOSE = "cmd.close";
	private static final String CMD_CONTEXT_RUN = "cmd.context.run";

	private Panel content;

	private LayoutMain3ColsController columnLayoutCtr;
	private MenuTree olatMenuTree;
	private ToolController toolC;

	private VelocityContainer contextListVC;
	private TableController contextListCtr;
	private BGContextTableModel contextTableModel;

	private BGManagementController groupManageCtr;
	private String groupType;
	private OLATResourceable ores;
	
	/**
	 * Constructor for the course group management main controller
	 * 
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param groupType
	 */
	public CourseGroupManagementMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, String groupType) {
		super(ureq, wControl);
		
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		
		this.ores = ores;
		this.groupType = groupType;
		// set user activity logger for this controller
		ICourse course = CourseFactory.loadCourse(ores);
		addLoggingResourceable(LoggingResourceable.wrap(course));

		CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();

		List groupContexts;
		String defaultContextName;
		
		// init content panel. current panel content will be set later in init process, use null for now
		content = putInitialPanel(null);
/*
		if (BusinessGroup.TYPE_LEARNINGROUP.equals(groupType)) {
			groupContexts = groupManager.getLearningGroupContexts();
			defaultContextName = CourseGroupManager.DEFAULT_NAME_LC_PREFIX + course.getCourseTitle();
		} else if (BusinessGroup.TYPE_RIGHTGROUP.equals(groupType)) {
			groupContexts = groupManager.getRightGroupContexts();
			defaultContextName = CourseGroupManager.DEFAULT_NAME_RC_PREFIX + course.getCourseTitle();
		} else {
			throw new AssertException("Invalid group type ::" + groupType);
		}
		
		if (groupContexts.size() == 0) {
			// create new default context if none exists
			BGContextManager contextManager = BGContextManagerImpl.getInstance();
			OLATResource courseResource = OLATResourceManager.getInstance().findOrPersistResourceable(course);
			BGContext context = contextManager.createAndAddBGContextToResource(defaultContextName, courseResource, groupType, null, true);
			groupContexts.add(context);
			doInitGroupmanagement(ureq, context, false);
		} else if (groupContexts.size() == 1) {
			BGContext context = (BGContext) groupContexts.get(0);
			doInitGroupmanagement(ureq, context, false);
		} else {
			// multiple, show list first
			Translator fallback = new PackageTranslator(Util.getPackageName(BGContextEditController.class), ureq.getLocale());
			setTranslator(Util.createPackageTranslator(this.getClass(), ureq.getLocale(), fallback));
			doInitContextListLayout(ureq);
			doInitContextList(ureq, groupContexts);
		}*/
		
	}

	private void doInitContextListLayout(UserRequest ureq) {
		// Layout is controlled with generic controller: menu - content - tools to
		// look the same as in the groupmanagement
		// 1) menu
		olatMenuTree = new MenuTree("olatMenuTree");
		GenericTreeModel gtm = new GenericTreeModel();
		GenericTreeNode root = new GenericTreeNode();
		if (groupType.equals(BusinessGroup.TYPE_RIGHTGROUP)) {
			root.setTitle(translate("rightmanagement.index"));
			root.setAltText(translate("rightmanagement.index.alt"));
		} else {
			root.setTitle(translate("groupmanagement.index"));
			root.setAltText(translate("groupmanagement.index.alt"));
		}
		gtm.setRootNode(root);
		olatMenuTree.setTreeModel(gtm);
		olatMenuTree.setSelectedNodeId(gtm.getRootNode().getIdent());
		// 2) context list
		contextListVC = createVelocityContainer("contextlist");
		// 3) tools
		toolC = ToolFactory.createToolController(getWindowControl());
		listenTo(toolC);
		if (groupType.equals(BusinessGroup.TYPE_RIGHTGROUP)) toolC.addHeader(translate("tools.title.rightmanagement"));
		else toolC.addHeader(translate("tools.title.groupmanagement"));
		toolC.addLink(CMD_CLOSE, translate(CMD_CLOSE), null, "b_toolbox_close");
		// now build layout controller
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, toolC.getInitialComponent(), contextListVC, null);
		listenTo(columnLayoutCtr); // cleanup on dispose
	}

	private void doInitContextList(UserRequest ureq, List groupContexts) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("contextlist.no.contexts"));
		// init group list filter controller
		removeAsListenerAndDispose(contextListCtr);
		contextListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(contextListCtr);
		contextListCtr.addColumnDescriptor(new DefaultColumnDescriptor("contextlist.table.name", 0, CMD_CONTEXT_RUN, ureq.getLocale()));
		contextListCtr.addColumnDescriptor(new DefaultColumnDescriptor("contextlist.table.desc", 1, null, ureq.getLocale()));
		contextListCtr.addColumnDescriptor(new BooleanColumnDescriptor("contextlist.table.default", 2, null, 
				translate("contextlist.table.default.true"), translate("contextlist.table.default.false")));
		contextListVC.put("contextlist", contextListCtr.getInitialComponent());

		contextTableModel = new BGContextTableModel(groupContexts, getTranslator(), false, true);
		contextListCtr.setTableDataModel(contextTableModel);
		content.setContent(columnLayoutCtr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	// empty
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == groupManageCtr) {
			if (event == Event.DONE_EVENT) {
				// Send done event to parent controller
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event == Event.BACK_EVENT) {
				// show context list again
				// reinitialize context list since it could be dirty
				List groupContexts;
				ICourse course = CourseFactory.loadCourse(ores);
				CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();
				//TODO gm
				/*
				if (BusinessGroup.TYPE_LEARNINGROUP.equals(groupType)) {
					groupContexts = groupManager.getLearningGroupContexts();
				} else {
					groupContexts = groupManager.getRightGroupContexts();
				}
				*/
				//doInitContextList(ureq, groupContexts);
				content.setContent(columnLayoutCtr.getInitialComponent());
			}
		} else if (source == contextListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				BGContext context = contextTableModel.getGroupContextAt(rowid);
				if (actionid.equals(CMD_CONTEXT_RUN)) {
					doInitGroupmanagement(ureq, context, true);
				}
			}
		} else if (source == toolC) {
			if (event.getCommand().equals(CMD_CLOSE)) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}

	}

	private void doInitGroupmanagement(UserRequest ureq, BGContext groupContext, boolean contextSelectSwitch) {
		// launch generic group management controller
		removeAsListenerAndDispose(groupManageCtr);
		groupManageCtr = BGControllerFactory.getInstance().createManagementController(ureq, getWindowControl(), groupContext, contextSelectSwitch);
		listenTo(groupManageCtr);
		content.setContent(groupManageCtr.getInitialComponent());
		
		// logging
		addLoggingResourceable(LoggingResourceable.wrap(groupContext));
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUPMANAGEMENT_START, getClass());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUPMANAGEMENT_CLOSE, getClass());
	}

}