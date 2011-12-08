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
* <p>
*/ 

package org.olat.course.editor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperRegistry;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.HtmlHeaderComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.run.preview.PreviewConfigController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.group.ui.context.BGContextEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.testutils.codepoints.server.Codepoint;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * The editor controller generates a view which is used to manipulate a course.
 * The changes are all applied on the course editor model and not on the course
 * runtime structure. Changes must be published explicitely. This mechanism is
 * also part of the editor.<br>
 * The editor uses the full window (menu - content - tool) and has a close link
 * in the toolbox.
 * <P>
 * 
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class EditorMainController extends MainLayoutBasicController implements GenericEventListener {
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(EditorMainController.class);
	
	private static final String TB_ACTION = "o_tb_do_";

	private static final String CMD_COPYNODE = "copyn";
	private static final String CMD_MOVENODE = "moven";
	private static final String CMD_DELNODE = "deln";
	private static final String CMD_CLOSEEDITOR = "cmd.close";
	private static final String CMD_PUBLISH = "pbl";
	private static final String CMD_COURSEFOLDER = "cfd";
	private static final String CMD_COURSEPREVIEW = "cprev";
	private static final String CMD_KEEPCLOSED_ERROR = "keep.closed.error";
	private static final String CMD_KEEPOPEN_ERROR = "keep.open.error";
	private static final String CMD_KEEPCLOSED_WARNING = "keep.closed.warning";
	private static final String CMD_KEEPOPEN_WARNING = "keep.open.warning";

	// NLS support
	
	private static final String NLS_PUBLISHED_NEVER_YET = "published.never.yet";
	private static final String NLS_PUBLISHED_LATEST = "published.latest";
	private static final String NLS_HEADER_TOOLS = "header.tools";
	private static final String NLS_COMMAND_COURSEFOLDER = "command.coursefolder";
	private static final String NLS_COMMAND_COURSEPREVIEW = "command.coursepreview";
	private static final String NLS_COMMAND_PUBLISH = "command.publish";
	private static final String NLS_COMMAND_CLOSEEDITOR = "command.closeeditor";
	private static final String NLS_HEADER_INSERTNODES = "header.insertnodes";
	private static final String NLS_COMMAND_DELETENODE_HEADER = "command.deletenode.header";
	private static final String NLS_COMMAND_DELETENODE = "command.deletenode";
	private static final String NLS_COMMAND_MOVENODE = "command.movenode";
	private static final String NLS_COMMAND_COPYNODE = "command.copynode";
	private static final String NLS_DELETENODE_SUCCESS = "deletenode.success";
	private static final String NLS_START_HELP_WIZARD = "start.help.wizard";
	private static final String NLS_INSERTNODE_TITLE = "insertnode.title";
	private static final String NLS_DELETENODE_ERROR_SELECTFIRST = "deletenode.error.selectfirst";
	private static final String NLS_DELETENODE_ERROR_ROOTNODE = "deletenode.error.rootnode";
	private static final String NLS_MOVECOPYNODE_ERROR_SELECTFIRST = "movecopynode.error.selectfirst";
	private static final String NLS_MOVECOPYNODE_ERROR_ROOTNODE = "movecopynode.error.rootnode";
	private static final String NLS_COURSEFOLDER_NAME = "coursefolder.name";
	private static final String NLS_COURSEFOLDER_CLOSE = "coursefolder.close";
	
	private Boolean errorIsOpen = Boolean.TRUE;
	private Boolean warningIsOpen = Boolean.FALSE;

	private MenuTree menuTree;
	private VelocityContainer main;

	private TabbedPane tabbedNodeConfig;
	private SelectionTree selTree;

	CourseEditorTreeModel cetm;
	private TabbableController nodeEditCntrllr;
	private StepsMainRunController publishStepsController;
	private PreviewConfigController previewController;
	private ToolController toolC;
	private MoveCopySubtreeController moveCopyController;
	private InsertNodeController insertNodeController;
	private DialogBoxController deleteDialogController;		
	private LayoutMain3ColsController columnLayoutCtr;
	
	private Identity identity;
	private LockResult lockEntry;
	private String cssFileRef;
	private Mapper cssUriMapper;
	private MapperRegistry mapreg;
	
	private HtmlHeaderComponent hc;
	EditorUserCourseEnvironmentImpl euce;
	
	private Link undelButton;
	private Link keepClosedErrorButton;
	private Link keepOpenErrorButton;
	private Link keepClosedWarningButton;
	private Link keepOpenWarningButton;
	private CloseableModalController cmc;

	private OLATResourceable ores;
	
	private OLog log = Tracing.createLoggerFor(this.getClass());
	private final static String RELEASE_LOCK_AT_CATCH_EXCEPTION = "Must release course lock since an exception occured in " + EditorMainController.class;

	
	/**
	 * Constructor for the course editor controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window controller
	 * @param course The course
	 */
	public EditorMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores) {
		super(ureq,wControl);
		this.ores = ores;
		this.identity = ureq.getIdentity();

		// OLAT-4955: setting the stickyActionType here passes it on to any controller defined in the scope of the editor,
		//            basically forcing any logging action called within the course editor to be of type 'admin'
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		addLoggingResourceable(LoggingResourceable.wrap(CourseFactory.loadCourse(ores)));
		
		// try to acquire edit lock for this course.			
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, ureq.getIdentity(), CourseFactory.COURSE_EDITOR_LOCK);

		try {			
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_OPEN, getClass());

		if (lockEntry.isSuccess()) {			
			ICourse course = CourseFactory.openCourseEditSession(ores.getResourceableId());
			main = createVelocityContainer("index");
			
			undelButton = LinkFactory.createButton("undeletenode.button", main, this);
			keepClosedErrorButton = LinkFactory.createCustomLink("keepClosedErrorButton", CMD_KEEPCLOSED_ERROR, "keep.closed", Link.BUTTON_SMALL, main, this);
			keepOpenErrorButton = LinkFactory.createCustomLink("keepOpenErrorButton", CMD_KEEPOPEN_ERROR, "keep.open", Link.BUTTON_SMALL, main, this);
			keepClosedWarningButton = LinkFactory.createCustomLink("keepClosedWarningButton", CMD_KEEPCLOSED_WARNING, "keep.closed", Link.BUTTON_SMALL, main, this);
			keepOpenWarningButton = LinkFactory.createCustomLink("keepOpenWarningButton", CMD_KEEPOPEN_WARNING, "keep.open", Link.BUTTON_SMALL, main, this);
			
			// set the custom course css
			enableCustomCss(ureq);

			menuTree = new MenuTree("luTree");
						

			/*
			 * create editor user course environment for enhanced syntax/semantic
			 * checks. Initialize it with the current course node id, which is not set
			 * yet. Furthermore the course is refreshed, e.g. as it get's loaded by
			 * XSTREAM constructors are not called, but transient data must be
			 * caculated and initialized
			 */
			cetm = CourseFactory.getCourseEditSession(ores.getResourceableId()).getEditorTreeModel();
			CourseEditorEnv cev = new CourseEditorEnvImpl(cetm, course.getCourseEnvironment().getCourseGroupManager(), ureq.getLocale());
			euce = new EditorUserCourseEnvironmentImpl(cev);
			euce.getCourseEditorEnv().setCurrentCourseNodeId(null);
			/*
			 * validate course and update course status
			 */
			euce.getCourseEditorEnv().validateCourse();
			StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
			updateCourseStatusMessages(ureq.getLocale(), courseStatus);

			long lpTimeStamp = cetm.getLatestPublishTimestamp();
			if (lpTimeStamp == -1) {				
				showInfo(NLS_PUBLISHED_NEVER_YET);
			} else { // course has been published before
				Date d = new Date(lpTimeStamp);
				getWindowControl().setInfo(translate(NLS_PUBLISHED_LATEST, Formatter.getInstance(ureq.getLocale()).formatDateAndTime(d)));
			}
			menuTree.setTreeModel(cetm);
			menuTree.addListener(this);

			selTree = new SelectionTree("selection", getTranslator());
			selTree.setTreeModel(cetm);
			selTree.setActionCommand("processpublish");
			selTree.setFormButtonKey("publizieren");
			selTree.addListener(this);

			tabbedNodeConfig = new TabbedPane("tabbedNodeConfig", ureq.getLocale());
			main.put(tabbedNodeConfig.getComponentName(), tabbedNodeConfig);

			toolC = ToolFactory.createToolController(getWindowControl());
			listenTo(toolC);
			toolC.addHeader(translate(NLS_HEADER_TOOLS));
			toolC.addLink(CMD_COURSEFOLDER, translate(NLS_COMMAND_COURSEFOLDER), CMD_COURSEFOLDER, "o_toolbox_coursefolder");
			toolC.addLink(CMD_COURSEPREVIEW, translate(NLS_COMMAND_COURSEPREVIEW), CMD_COURSEPREVIEW, "b_toolbox_preview" );
			toolC.addLink(CMD_PUBLISH, translate(NLS_COMMAND_PUBLISH), CMD_PUBLISH,"b_toolbox_publish" );
			toolC.addLink(CMD_CLOSEEDITOR, translate(NLS_COMMAND_CLOSEEDITOR), null, "b_toolbox_close");

			toolC.addHeader(translate(NLS_HEADER_INSERTNODES));
			CourseNodeFactory cnf = CourseNodeFactory.getInstance();
			for (Iterator<String> iter = cnf.getRegisteredCourseNodeAliases().iterator(); iter.hasNext();) {
				String courseNodeAlias = iter.next();
				CourseNodeConfiguration cnConfig = cnf.getCourseNodeConfiguration(courseNodeAlias);
				try {
					toolC.addLink(TB_ACTION + courseNodeAlias, cnConfig.getLinkText(ureq.getLocale()), courseNodeAlias, cnConfig.getIconCSSClass());
				} catch (Exception e) {
					log.error("Error while trying to add a course buildingblock of type \""+courseNodeAlias +"\" to the editor", e);
				}
			}

			toolC.addHeader(translate(NLS_COMMAND_DELETENODE_HEADER));
			toolC.addLink(CMD_DELNODE, translate(NLS_COMMAND_DELETENODE), CMD_DELNODE, "b_toolbox_delete");
			toolC.addLink(CMD_MOVENODE, translate(NLS_COMMAND_MOVENODE), CMD_MOVENODE, "b_toolbox_move");
			toolC.addLink(CMD_COPYNODE, translate(NLS_COMMAND_COPYNODE), CMD_COPYNODE, "b_toolbox_copy");
			
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, toolC.getInitialComponent(), main, "course" + course.getResourceableId());			
			columnLayoutCtr.addCssClassToMain("o_editor");
			listenTo(columnLayoutCtr);
			putInitialPanel(columnLayoutCtr.getInitialComponent());

			// add as listener to course so we are being notified about course events:
			// - deleted events
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), course);
			// activate course root node
			String rootNodeIdent = cetm.getRootNode().getIdent();
			menuTree.setSelectedNodeId(rootNodeIdent);
			updateViewForSelectedNodeId(ureq, rootNodeIdent);
		}
		} catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION+" [in <init>]", e);		
			this.dispose();
			throw e;
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	  try {
		ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				// goto node in edit mode
				TreeEvent te = (TreeEvent) event;
				String nodeId = te.getNodeId();
				updateViewForSelectedNodeId(ureq, nodeId);				
			}
		} else if (source == main) {
			if (event.getCommand().startsWith(NLS_START_HELP_WIZARD)) {
				String findThis = event.getCommand().substring(NLS_START_HELP_WIZARD.length());
				StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
				for (int i = 0; i < courseStatus.length; i++) {
					String key = courseStatus[i].getDescriptionForUnit() + "." + courseStatus[i].getShortDescriptionKey();
					if (key.equals(findThis)) {
						menuTree.setSelectedNodeId(courseStatus[i].getDescriptionForUnit());
						euce.getCourseEditorEnv().setCurrentCourseNodeId(courseStatus[i].getDescriptionForUnit());
						jumpToNodeEditor(courseStatus[i].getActivateableViewIdentifier(), ureq, cetm.getCourseNode(courseStatus[i]
								.getDescriptionForUnit()), course.getCourseEnvironment().getCourseGroupManager());
						break;
					}
				}
				euce.getCourseEditorEnv().validateCourse();
				courseStatus = euce.getCourseEditorEnv().getCourseStatus();
				updateCourseStatusMessages(ureq.getLocale(), courseStatus);
			}
		} else if (source == keepClosedErrorButton){
			errorIsOpen = Boolean.FALSE;
			main.contextPut("errorIsOpen", errorIsOpen);
		} else if (source == keepOpenErrorButton){
			errorIsOpen = Boolean.TRUE;
			main.contextPut("errorIsOpen", errorIsOpen);
		} else if (source == keepClosedWarningButton){
			warningIsOpen = Boolean.FALSE;
			main.contextPut("warningIsOpen", warningIsOpen);
		} else if (source == keepOpenWarningButton){
			warningIsOpen = Boolean.TRUE;
			main.contextPut("warningIsOpen", warningIsOpen);
		} else if (source == undelButton){
			String ident = menuTree.getSelectedNode().getIdent();
			CourseEditorTreeNode activeNode = (CourseEditorTreeNode) cetm.getNodeById(ident);
			euce.getCourseEditorEnv().setCurrentCourseNodeId(activeNode.getIdent());
			
			CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
			cetm.markUnDeleted(activeNode);
			menuTree.setDirty(true);
			// show edit panels again
			initNodeEditor(ureq, activeNode.getCourseNode());
			tabbedNodeConfig.setVisible(true);
			toolC.setEnabled(CMD_DELNODE, true);
			toolC.setEnabled(CMD_MOVENODE, true);
			toolC.setEnabled(CMD_COPYNODE, true);
			main.setPage(VELOCITY_ROOT + "/index.html");
			/*
			 * validate course and update course status
			 */
			euce.getCourseEditorEnv().validateCourse();
			StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
			updateCourseStatusMessages(ureq.getLocale(), courseStatus);
			// do logging
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_RESTORED, getClass(),
					LoggingResourceable.wrap(activeNode.getCourseNode()));
		}
		} catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION+" [in event(UserRequest,Component,Event)]", e);			
			this.dispose();
			throw e;
		}
	}

	/**
	 * helper to update menu tree, content area, tools to a selected tree node
	 * @param ureq
	 * @param nodeId
	 */
	private void updateViewForSelectedNodeId(UserRequest ureq, String nodeId) {
		
		CourseEditorTreeNode cetn = (CourseEditorTreeNode) cetm.getNodeById(nodeId);
		// udpate the current node in the course editor environment
		euce.getCourseEditorEnv().setCurrentCourseNodeId(nodeId);
		// Start necessary controller for selected node

		if (cetn.isDeleted()) {
			tabbedNodeConfig.setVisible(false);
			toolC.setEnabled(CMD_DELNODE, false);
			toolC.setEnabled(CMD_MOVENODE, false);
			toolC.setEnabled(CMD_COPYNODE, false);
			if (((CourseEditorTreeNode) cetn.getParent()).isDeleted()) main.setPage(VELOCITY_ROOT + "/deletednode.html");
			else main.setPage(VELOCITY_ROOT + "/undeletenode.html");
		} else {
			tabbedNodeConfig.setVisible(true);			
			toolC.setEnabled(CMD_DELNODE, true);
			toolC.setEnabled(CMD_MOVENODE, true);
			toolC.setEnabled(CMD_COPYNODE, true);
			initNodeEditor(ureq, cetn.getCourseNode());
			main.setPage(VELOCITY_ROOT + "/index.html");					
		}
	}
	
	/**
	 * Initializes the node edit tabbed pane and its controller for this
	 * particular node
	 * 
	 * @param ureq
	 * @param chosenNode
	 * @param groupMgr
	 */
	private void initNodeEditor(UserRequest ureq, CourseNode chosenNode) {
		ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		tabbedNodeConfig.removeAll();
		// dispose old one, if there was one
		removeAsListenerAndDispose(nodeEditCntrllr);
		String type = chosenNode.getType();
		CourseNodeConfiguration cnConfig = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(type);
		if (cnConfig.isEnabled()) {
			nodeEditCntrllr = chosenNode.createEditController(ureq, getWindowControl(), course, euce);
			listenTo(nodeEditCntrllr);
			nodeEditCntrllr.addTabs(tabbedNodeConfig);
		} 
		main.contextPut("courseNodeDisabled", !cnConfig.isEnabled());
		main.contextPut("courseNodeCss", cnConfig.getIconCSSClass());
		main.contextPut("courseNode", chosenNode);
	}

	/**
	 * Initializes the node edit tabbed pane and its controller for this
	 * particular node
	 * 
	 * @param ureq
	 * @param chosenNode
	 * @param groupMgr
	 */
	private void jumpToNodeEditor(String activatorIdent, UserRequest ureq, CourseNode chosenNode, CourseGroupManager groupMgr) {
		initNodeEditor(ureq, chosenNode);
		if (nodeEditCntrllr instanceof ActivateableTabbableDefaultController) {
			((ActivateableTabbableDefaultController) nodeEditCntrllr).activate(ureq, activatorIdent);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event.getCommand().equals(BGContextEvent.RESOURCE_ADDED)) {
			System.out.println("emc:have to inform tabs");
			Iterator it = tabbedNodeConfig.getComponents().keySet().iterator();
			while (it.hasNext()) {
				Component c = tabbedNodeConfig.getComponents().get(it.next());
				System.out.println(c.getComponentName());
			}
			return;
		}

		try {
		ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		
		if (source == toolC) {
			if (event.getCommand().startsWith(TB_ACTION)) {
				String cnAlias = event.getCommand().substring(TB_ACTION.length());
				if (cnAlias == null) throw new AssertException("Received event from ButtonController which is not registered with the toolbox.");
				
				Codepoint.codepoint(EditorMainController.class, "startInsertNode");
				insertNodeController = new InsertNodeController(ureq, getWindowControl(), course, cnAlias);				
				listenTo(insertNodeController);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), insertNodeController.getInitialComponent(), true, translate(NLS_INSERTNODE_TITLE));
				cmc.activate();
			} else if (event.getCommand().equals(CMD_DELNODE)) {
				TreeNode tn = menuTree.getSelectedNode();
				if (tn == null) {
					showError(NLS_DELETENODE_ERROR_SELECTFIRST);
					return;
				}
				if (tn.getParent() == null) {
					showError(NLS_DELETENODE_ERROR_ROOTNODE);
					return;
				}
				// deletion is possible, start asking if really to delete.
				tabbedNodeConfig.setVisible(false);
				deleteDialogController = activateYesNoDialog(ureq, translate("deletenode.header", tn.getTitle()), translate("deletenode.confirm"), deleteDialogController);
								
			} else if (event.getCommand().equals(CMD_MOVENODE) || event.getCommand().equals(CMD_COPYNODE)) {
				TreeNode tn = menuTree.getSelectedNode();
				if (tn == null) {
					showError(NLS_MOVECOPYNODE_ERROR_SELECTFIRST);
					return;
				}
				if (tn.getParent() == null) {
					showError(NLS_MOVECOPYNODE_ERROR_ROOTNODE);
					return;
				}
				
				CourseEditorTreeNode cetn = cetm.getCourseEditorNodeById(tn.getIdent());
				moveCopyController = new MoveCopySubtreeController(ureq, getWindowControl(), course, cetn, event.getCommand().equals(CMD_COPYNODE));				
				this.listenTo(moveCopyController);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), moveCopyController.getInitialComponent(), true, translate(NLS_INSERTNODE_TITLE));
				cmc.activate();
			}
			
			else if (event.getCommand().equals(CMD_CLOSEEDITOR)) {
				doReleaseEditLock();
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event.getCommand().equals(CMD_PUBLISH)) {
				/*
				 * start follwoing steps -> cancel wizardf does not touch data
				 * (M) Mandatory (O) Optional
				 * - (M)Step 00  -> show selection tree to choose changed nodes to be published
				 * ...........-> calculate errors & warnings
				 * ...........(next|finish) available if no errors or nothing to publish
				 * - (O)Step 00A -> review errors & warnings
				 * ...........(previous|next|finish) available
				 * - (O)Step 00B -> review publish changes that will happen
				 * ...........(previous|next|finish) available
				 * - (O)Step 01  -> change general access to course
				 * ...........(previous|finish) available
				 * - FinishCallback -> apply course nodes change set
				 * .................-> apply general access changes.
				 */
				
				Step start  = new PublishStep00(ureq, cetm, course);
				
				/*
				 * callback executed in case wizard is finished.
				 */
				StepRunnerCallback finish = new StepRunnerCallback(){
					@SuppressWarnings("unchecked")
					public Step execute(UserRequest ureq1, WindowControl wControl1, StepsRunContext runContext) {
						/*
						 * all information to do now is within the runContext saved
						 */
						boolean hasChanges = false;

						if (runContext.containsKey("validPublish") && ((Boolean) runContext.get("validPublish")).booleanValue()) {

							Set<String> selectedNodeIds = (Set<String>) runContext.get("publishSetCreatedFor");
							hasChanges = (selectedNodeIds != null) && (selectedNodeIds.size() > 0);
							if (hasChanges) {
								PublishProcess publishManager = (PublishProcess) runContext.get("publishProcess");
								publishManager.applyPublishSet(ureq1.getIdentity(), ureq1.getLocale());
							}
						}
						if (runContext.containsKey("changedaccess")) {
							// there were changes made to the general course access
							String newAccessStr = (String) runContext.get("changedaccess");
							int newAccess;
							//fxdiff VCRP-1,2: access control of resources
							boolean membersOnly = RepositoryEntry.MEMBERS_ONLY.equals(newAccessStr);
							if(membersOnly) {
								newAccess = RepositoryEntry.ACC_OWNERS;
							} else {
								newAccess = Integer.valueOf(newAccessStr);
							}
							PublishProcess publishManager = (PublishProcess) runContext.get("publishProcess");
							// fires an EntryChangedEvent for repository entry notifying
							// about modification.
							publishManager.changeGeneralAccess(ureq1, newAccess, membersOnly);
							hasChanges = true;
						}

						// signal correct completion and tell if changes were made or not.
						return hasChanges ? StepsMainRunController.DONE_MODIFIED : StepsMainRunController.DONE_UNCHANGED;
					}
				};

				publishStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("publish.wizard.title") );
				listenTo(publishStepsController);
				getWindowControl().pushAsModalDialog(publishStepsController.getInitialComponent());
					
			} else if (event.getCommand().equals(CMD_COURSEPREVIEW)) {
				previewController = new PreviewConfigController(ureq, getWindowControl(), course);
				listenTo(previewController);		
				previewController.activate();
				
			} else if (event.getCommand().equals(CMD_COURSEFOLDER)) {
				// Folder for course with custom link model to jump to course nodes
				VFSContainer namedCourseFolder = new NamedContainerImpl(translate(NLS_COURSEFOLDER_NAME), course.getCourseFolderContainer());
				CustomLinkTreeModel customLinkTreeModel = new CourseInternalLinkTreeModel(course.getEditorTreeModel());
				FolderRunController bcrun = new FolderRunController(namedCourseFolder, true, true, ureq, getWindowControl(), null, customLinkTreeModel);
				bcrun.addLoggingResourceable(LoggingResourceable.wrap(course));
				Component folderComponent = bcrun.getInitialComponent();
				CloseableModalController clc = new CloseableModalController(getWindowControl(), translate(NLS_COURSEFOLDER_CLOSE),
						folderComponent);
				clc.activate();
				
			}
		} else if (source == nodeEditCntrllr) {
			// event from the tabbed pane (any tab)
			if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				// if the user changed the name of the node, we need to update the tree also.
				// the event is too generic to find out what happened -> update tree in all cases (applies to ajax mode only)
				menuTree.setDirty(true);
				
				cetm.nodeConfigChanged(menuTree.getSelectedNode());				
				CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
				euce.getCourseEditorEnv().validateCourse();
				StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
				updateCourseStatusMessages(ureq.getLocale(), courseStatus);
			}
		} else if (source == publishStepsController) {
			//if (event == Event.DONE_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(publishStepsController);
				publishStepsController = null;
				// reset to root node... may have published a deleted node -> this
				// resets the view
				cetm = course.getEditorTreeModel();
				menuTree.setTreeModel(cetm);
				String rootNodeIdent = menuTree.getTreeModel().getRootNode().getIdent();
				menuTree.setSelectedNodeId(rootNodeIdent);
				updateViewForSelectedNodeId(ureq, rootNodeIdent);
				if(event == Event.CHANGED_EVENT){					
					showInfo("pbl.success", null);
					// do logging
					ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_PUBLISHED, getClass());
				}//else Event.DONE -> nothing changed / else Event.CANCELLED -> cancelled wizard
				
		} else if (source == previewController) {
			if (event == Event.DONE_EVENT) {
				// no need to deactivate preview controller, already done internally
				removeAsListenerAndDispose(previewController);
			}
		} else if (source == moveCopyController) {	
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {					
				menuTree.setDirty(true); // setDirty when moving
				// Repositioning to move/copy course node
				String nodeId = moveCopyController.getCopyNodeId();				
				if (nodeId != null) {
					menuTree.setSelectedNodeId(nodeId);
					euce.getCourseEditorEnv().setCurrentCourseNodeId(nodeId);					
					CourseNode copyNode = cetm.getCourseNode(nodeId);
					initNodeEditor(ureq, copyNode);
				}												
				euce.getCourseEditorEnv().validateCourse();
				StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
				updateCourseStatusMessages(ureq.getLocale(), courseStatus);
				
			} else if (event == Event.FAILED_EVENT) {				
				getWindowControl().setError("Error in copy of subtree.");				
			} else if (event == Event.CANCELLED_EVENT) {
				// user canceled						
			}
			
		} else if (source == insertNodeController) {     			
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				// Activate new node in menu and create necessary edit controllers
				// necessary if previous action was a delete node action				
				tabbedNodeConfig.setVisible(true);
				main.setPage(VELOCITY_ROOT + "/index.html");				
				CourseNode newNode = insertNodeController.getInsertedNode();				
				menuTree.setSelectedNodeId(newNode.getIdent());
				// update the current node in the editor course environment
				euce.getCourseEditorEnv().setCurrentCourseNodeId(newNode.getIdent());
				euce.getCourseEditorEnv().validateCourse();
				StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
				updateCourseStatusMessages(ureq.getLocale(), courseStatus);					
				initNodeEditor(ureq, newNode);
				// do logging
				ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_CREATED, getClass(),
						LoggingResourceable.wrap(newNode));
				// Resize layout columns to make all nodes viewable in the menu column
				JSCommand resizeCommand = new JSCommand("b_AddOnDomReplacementFinishedCallback( B_ResizableColumns.adjustHeight.bind(B_ResizableColumns));");
				getWindowControl().getWindowBackOffice().sendCommandTo(resizeCommand);
			}
			// in all cases:
			removeAsListenerAndDispose(insertNodeController);

		} else if (source == deleteDialogController){
			removeAsListenerAndDispose(deleteDialogController);
			deleteDialogController = null;
			if (DialogBoxUIFactory.isYesEvent(event)){
				// delete confirmed
				String ident = menuTree.getSelectedNode().getIdent();
				// udpate the current node in the course editor environment
				euce.getCourseEditorEnv().setCurrentCourseNodeId(ident);
				CourseNode activeNode = cetm.getCourseNode(ident);

				cetm.markDeleted(activeNode);
				menuTree.setDirty(true);
			
				CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
				tabbedNodeConfig.removeAll();
				tabbedNodeConfig.setVisible(false);
				toolC.setEnabled(CMD_DELNODE, false);
				toolC.setEnabled(CMD_MOVENODE, false);
				toolC.setEnabled(CMD_COPYNODE, false);
				main.setPage(VELOCITY_ROOT + "/undeletenode.html"); // offer undelete
				showInfo(NLS_DELETENODE_SUCCESS);
				/*
				 * validate course and update course status
				 */
				euce.getCourseEditorEnv().validateCourse();
				StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
				updateCourseStatusMessages(ureq.getLocale(), courseStatus);

				ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_DELETED, getClass(),
						LoggingResourceable.wrap(activeNode));
			
			} else {
				tabbedNodeConfig.setVisible(true);
			}
		}
    } catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION+" [in event(UserRequest,Controller,Event)]", e);			
			this.dispose();
			throw e;
		}
	}

	/*
	 * FIXME:pb:b never used...
	 */
	private Map<String, List<String>> checkReferencesFor(TreeNode tn) {
		final CourseEditorTreeModel cetm = CourseFactory.getCourseEditSession(ores.getResourceableId()).getEditorTreeModel();
		 //create a list of all nodes in the selected subtree
		final Set<String> allSubTreeids = new HashSet<String>();
		TreeVisitor tv = new TreeVisitor(new Visitor() {
			public void visit(INode node) {
				allSubTreeids.add(node.getIdent());
			}
		}, tn, true);
		tv.visitAll();
		 //find all references pointing from outside the subtree into the subtree or
		 //on the subtree root node.
		final Map<String, List<String>> allRefs = new HashMap<String, List<String>>();
		tv = new TreeVisitor(new Visitor() {
			public void visit(INode node) {
				List referencingNodes = euce.getCourseEditorEnv().getReferencingNodeIdsFor(node.getIdent());
				// subtract the inner nodes. This allows to delete a whole subtree if
				// only references residing completly inside the subtree are active.
				referencingNodes.removeAll(allSubTreeids);
				if (referencingNodes.size() > 0) {
					List<String> nodeNames = new ArrayList<String>();
					for (Iterator iter = referencingNodes.iterator(); iter.hasNext();) {
						String nodeId = (String) iter.next();
						CourseNode cn = cetm.getCourseNode(nodeId);
						nodeNames.add(cn.getShortTitle());
					}
					allRefs.put(node.getIdent(), nodeNames);
				}
			}
		}, tn, true);
		// traverse all nodes from the deletion startpoint
		tv.visitAll();
		// allRefs contains now all references, or zero if ready for delete.
		return allRefs;
	}

	/**
	 * @param ureq
	 * @param courseStatus
	 */
	private void updateCourseStatusMessages(Locale locale, StatusDescription[] courseStatus) {

		/*
		 * clean up velocity context
		 */
		main.contextRemove("hasWarnings");
		main.contextRemove("warningIsForNode");
		main.contextRemove("warningMessage");
		main.contextRemove("warningHelpWizardLink");
		main.contextRemove("warningsCount");
		main.contextRemove("warningIsOpen");
		main.contextRemove("hasErrors");
		main.contextRemove("errorIsForNode");
		main.contextRemove("errorMessage");
		main.contextRemove("errorHelpWizardLink");
		main.contextRemove("errorsCount");
		main.contextRemove("errorIsOpen");
		if (courseStatus == null || courseStatus.length == 0) {
			main.contextPut("hasCourseStatus", Boolean.FALSE);
			main.contextPut("errorIsOpen", Boolean.FALSE);
			return;
		}
		//
		List<String> errorIsForNode = new ArrayList<String>();
		List<String> errorMessage = new ArrayList<String>();
		List<String> errorHelpWizardLink = new ArrayList<String>();
		List<String> warningIsForNode = new ArrayList<String>();
		List<String> warningMessage = new ArrayList<String>();
		List<String> warningHelpWizardLink = new ArrayList<String>();
		//
		int errCnt = 0;
		int warCnt = 0;
		String helpWizardCmd;
		for (int i = 0; i < courseStatus.length; i++) {
			StatusDescription description = courseStatus[i];
			String nodeId = courseStatus[i].getDescriptionForUnit();
			String nodeName = cetm.getCourseNode(nodeId).getShortName();
			// prepare wizard link
			helpWizardCmd = courseStatus[i].getActivateableViewIdentifier();
			if (helpWizardCmd != null) {
				helpWizardCmd = "start.help.wizard" + courseStatus[i].getDescriptionForUnit() + "." + courseStatus[i].getShortDescriptionKey();
			} else {
				helpWizardCmd = "NONE";
			}
			if (description.isError()) {
				errCnt++;
				errorIsForNode.add(nodeName);
				errorMessage.add(description.getShortDescription(locale));
				errorHelpWizardLink.add(helpWizardCmd);
			} else if (description.isWarning()) {
				warCnt++;
				warningIsForNode.add(nodeName);
				warningMessage.add(description.getShortDescription(locale));
				warningHelpWizardLink.add(helpWizardCmd);
			}
		}
		/*
		 * 
		 */
		if (errCnt > 0 || warCnt > 0) {
			if (warCnt > 0) {
				main.contextPut("hasWarnings", Boolean.TRUE);
				main.contextPut("warningIsForNode", warningIsForNode);
				main.contextPut("warningMessage", warningMessage);
				main.contextPut("warningHelpWizardLink", warningHelpWizardLink);
				main.contextPut("warningsCount", new String[] { Integer.toString(warCnt) });
				main.contextPut("warningIsOpen", warningIsOpen);
			}
			if (errCnt > 0) {
				main.contextPut("hasErrors", Boolean.TRUE);
				main.contextPut("errorIsForNode", errorIsForNode);
				main.contextPut("errorMessage", errorMessage);
				main.contextPut("errorHelpWizardLink", errorHelpWizardLink);
				main.contextPut("errorsCount", new String[] { Integer.toString(errCnt) });
				main.contextPut("errorIsOpen", errorIsOpen);
			}
		} else {
			main.contextPut("hasWarnings", Boolean.FALSE);
			main.contextPut("hasErrors", Boolean.FALSE);
		}
	}

	/**
	 * @return true if lock on this course has been acquired, flase otherwhise
	 */
	public LockResult getLockEntry() {
		return lockEntry;
	}

	protected void doDispose() {
		ICourse course = CourseFactory.loadCourse(ores.getResourceableId());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, course);
		// those controllers are disposed by BasicController:
		nodeEditCntrllr = null;
		publishStepsController = null;
		deleteDialogController = null;
		cmc = null;
		moveCopyController = null;
		insertNodeController = null;
		previewController = null;
		toolC = null;
		columnLayoutCtr = null;
		insertNodeController = null;
		moveCopyController = null;
		
		doReleaseEditLock();
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_CLOSE, getClass());
		if (mapreg !=null && cssUriMapper != null) {
			mapreg.deregister(cssUriMapper);
		}
	}
	
	private void doReleaseEditLock() {
		if (lockEntry!=null && lockEntry.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			CourseFactory.fireModifyCourseEvent(ores.getResourceableId());
			lockEntry = null;
		}
	}

	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
	  try {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent ojde = (OLATResourceableJustBeforeDeletedEvent) event;
			// make sure it is our course (actually not needed till now, since we
			// registered only to one event, but good style.
			if (ojde.targetEquals(ores, true)) {
				// true = throw an exception if the target does not match ores
				dispose();
			}
		}
		} catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION+" [in event(Event)]", e);			
			this.dispose();
			throw e;
		}
	}

	/**
	 * @param ureq
	 * @param course
	 */
	private void enableCustomCss(UserRequest ureq) {
		/*
		 * add also the choosen courselayout css if any
		 */
		final ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
		if (cc.hasCustomCourseCSS()) {
			cssFileRef = cc.getCssLayoutRef();
			mapreg = MapperRegistry.getInstanceFor(ureq.getUserSession());
			if (cssUriMapper != null) {
				// deregister old mapper
				mapreg.deregister(cssUriMapper);
			}
			cssUriMapper = new Mapper() {
				final VFSContainer courseFolder = course.getCourseFolderContainer();

				@SuppressWarnings("unused")
				public MediaResource handle(String relPath, HttpServletRequest request) {
					VFSItem vfsItem = courseFolder.resolve(relPath);
					MediaResource mr;
					if (vfsItem == null || !(vfsItem instanceof VFSLeaf)) mr = new NotFoundMediaResource(relPath);
					else mr = new VFSMediaResource((VFSLeaf) vfsItem);
					return mr;
				}
			};
			
			String uri;
			// Register mapper as cacheable
			String mapperID = VFSManager.getRealPath(course.getCourseFolderContainer());
			if (mapperID == null) {
				// Can't cache mapper, no cacheable context available
				uri  = mapreg.register(cssUriMapper);
			} else {
				// Add classname to the file path to remove conflicts with other
				// usages of the same file path
				mapperID = this.getClass().getSimpleName() + ":" + mapperID;
				uri = mapreg.registerCacheable(mapperID, cssUriMapper);				
			}
			final String fulluri = uri + cssFileRef; // the stylesheet's relative
			// path
			hc = new HtmlHeaderComponent("custom-css", null, "<link rel=\"StyleSheet\" href=\"" + fulluri
					+ "\" type=\"text/css\" media=\"screen\"/>");
			main.put("css-inset2", hc);
		}
	}

}
