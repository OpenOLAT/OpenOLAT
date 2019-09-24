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

package org.olat.course.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.badge.Badge.Level;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeDropEvent;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockEntry;
import org.olat.core.util.coordinate.LockRemovedEvent;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.DisposedCourseRestartController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.cl.ui.wizard.CheckListStepRunnerCallback;
import org.olat.course.nodes.cl.ui.wizard.CheckList_1_CheckboxStep;
import org.olat.course.run.preview.PreviewConfigController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.course.tree.PublishTreeModel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntryRuntimeController.ToolbarAware;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

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
public class EditorMainController extends MainLayoutBasicController implements GenericEventListener, VetoableCloseController, ToolbarAware {
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(EditorMainController.class);
	
	protected static final String TB_ACTION = "o_tb_do_";

	private static final String CMD_COPYNODE = "copyn";
	private static final String CMD_MOVENODE = "moven";
	private static final String CMD_DELNODE = "deln";
	private static final String CMD_PUBLISH = "pbl";
	private static final String CMD_COURSEPREVIEW = "cprev";
	protected static final String CMD_MULTI_SP = "cmp.multi.sp";
	protected static final String CMD_MULTI_CHECKLIST = "cmp.multi.checklist";

	// NLS support
	private static final String NLS_COMMAND_COURSEPREVIEW = "command.coursepreview";
	private static final String NLS_COMMAND_PUBLISH = "command.publish";
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
	
	protected static final Event MANUAL_PUBLISH = new Event("manual-publish");

	private MenuTree menuTree;
	private VelocityContainer main;

	private TabbedPane tabbedNodeConfig;

	private CourseEditorTreeModel cetm;
	private TabbableController nodeEditCntrllr;
	private StepsMainRunController publishStepsController;
	private StepsMainRunController checklistWizard;
	private PreviewConfigController previewController;
	private MoveCopySubtreeController moveCopyController;
	private DialogBoxController deleteDialogController;		
	private LayoutMain3ColsController columnLayoutCtr;
	private AlternativeCourseNodeController alternateCtr;
	private EditorStatusController statusCtr;
	private ChooseNodeController chooseNodeTypeCtr;
	private QuickPublishController quickPublishCtr;
	
	private LockResult lockEntry;
	
	private EditorUserCourseEnvironmentImpl euce;
	
	private Dropdown nodeTools;
	private Link undelButton, alternativeLink, statusLink;
	private Link previewLink, publishLink, closeLink;
	private Link createNodeLink, deleteNodeLink, moveNodeLink, copyNodeLink;
	
	private CloseableModalController cmc;
	private CloseableCalloutWindowController calloutCtrl;
	
	private TooledStackedPanel stackPanel;
	private MultiSPController multiSPChooserCtr;

	private final OLATResourceable ores;
	private RepositoryEntry repoEntry;
	
	private static final Logger log = Tracing.createLoggerFor(EditorMainController.class);
	private final static String RELEASE_LOCK_AT_CATCH_EXCEPTION = "Must release course lock since an exception occured in " + EditorMainController.class;
	
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	
	/**
	 * Constructor for the course editor controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window controller
	 * @param course The course
	 */
	public EditorMainController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, ICourse course, CourseNode selectedNode) {
		super(ureq,wControl);
		this.ores = OresHelper.clone(course);	
		this.stackPanel = toolbar;

		// OLAT-4955: setting the stickyActionType here passes it on to any controller defined in the scope of the editor,
		//            basically forcing any logging action called within the course editor to be of type 'admin'
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		addLoggingResourceable(LoggingResourceable.wrap(course));
		
		// try to acquire edit lock for this course.			
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, ureq.getIdentity(), CourseFactory.COURSE_EDITOR_LOCK);

		if(CourseFactory.isCourseEditSessionOpen(ores.getResourceableId())) {
			MainPanel empty = new MainPanel("empty");
			putInitialPanel(empty);
			return;
		}
		
		OLATResourceable lockEntryOres = OresHelper.createOLATResourceableInstance(LockEntry.class, 0l);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), lockEntryOres);
		
		try {			
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_OPEN, getClass());
	
			if (!lockEntry.isSuccess()) {
				MainPanel empty = new MainPanel("empty");
				putInitialPanel(empty);
			} else {
				course = CourseFactory.openCourseEditSession(ores.getResourceableId());
				CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
				repoEntry = cgm.getCourseEntry();
				
				main = createVelocityContainer("index");
				//must be true for deleted course node
				main.setDomReplacementWrapperRequired(true);
				
				Controller courseCloser = new DisposedCourseRestartController(ureq, wControl, repoEntry);
				Controller disposedRestartController = new LayoutMain3ColsController(ureq, wControl, courseCloser);
				setDisposedMsgController(disposedRestartController);
				
				undelButton = LinkFactory.createButton("undeletenode.button", main, this);
	
				menuTree = new MenuTree("luTree");
				menuTree.setExpandSelectedNode(false);
				menuTree.setDragEnabled(true);
				menuTree.setDropEnabled(true);
				menuTree.setDropSiblingEnabled(true);	
				menuTree.setDndAcceptJSMethod("treeAcceptDrop_notWithChildren");	
				menuTree.setElementCssClass("o_editor_menu");
	
				/*
				 * create editor user course environment for enhanced syntax/semantic
				 * checks. Initialize it with the current course node id, which is not set
				 * yet. Furthermore the course is refreshed, e.g. as it get's loaded by
				 * XSTREAM constructors are not called, but transient data must be
				 * caculated and initialized
				 */
				cetm = course.getEditorTreeModel();
	
				CourseEditorEnv cev = new CourseEditorEnvImpl(cetm, cgm, getLocale());
				euce = new EditorUserCourseEnvironmentImpl(cev, getWindowControl());
				euce.getCourseEditorEnv().setCurrentCourseNodeId(null);
				
				menuTree.setTreeModel(cetm);
				menuTree.setOpenNodeIds(Collections.singleton(cetm.getRootNode().getIdent()));
				menuTree.addListener(this);
	
				tabbedNodeConfig = new TabbedPane("tabbedNodeConfig", getLocale());
				tabbedNodeConfig.setElementCssClass("o_node_config");
				main.put(tabbedNodeConfig.getComponentName(), tabbedNodeConfig);
				
				alternativeLink = LinkFactory.createButton("alternative", main, this);
				main.put("alternative", alternativeLink);
	
				columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, main, "course" + course.getResourceableId());			
				columnLayoutCtr.addCssClassToMain("o_editor");
				listenTo(columnLayoutCtr);
				StackedPanel initialPanel = putInitialPanel(new SimpleStackedPanel("coursePanel", "o_edit_mode"));
				initialPanel.setContent(columnLayoutCtr.getInitialComponent());
				
				//tools
				statusLink = LinkFactory.createToolLink("status", translate("status"), this, null);
				statusLink.setUserObject(new EditedCourseStatus());
				
				createNodeLink = LinkFactory.createToolLink(NLS_HEADER_INSERTNODES, translate(NLS_HEADER_INSERTNODES), this, "o_icon_add");
				createNodeLink.setElementCssClass("o_sel_course_editor_create_node");
				createNodeLink.setDomReplacementWrapperRequired(false);

				nodeTools = new Dropdown("insertNodes", NLS_COMMAND_DELETENODE_HEADER, false, getTranslator());
				nodeTools.setIconCSS("o_icon o_icon_customize");
				nodeTools.setElementCssClass("o_sel_course_editor_change_node");

				deleteNodeLink = LinkFactory.createToolLink(CMD_DELNODE, translate(NLS_COMMAND_DELETENODE), this, "o_icon_delete_item");
				nodeTools.addComponent(deleteNodeLink);
				moveNodeLink = LinkFactory.createToolLink(CMD_MOVENODE, translate(NLS_COMMAND_MOVENODE), this, "o_icon_move");
				moveNodeLink.setElementCssClass("o_sel_course_editor_move_node");
				nodeTools.addComponent(moveNodeLink);
				copyNodeLink = LinkFactory.createToolLink(CMD_COPYNODE, translate(NLS_COMMAND_COPYNODE), this, "o_icon_copy");
				nodeTools.addComponent(copyNodeLink);

				previewLink = LinkFactory.createToolLink(CMD_COURSEPREVIEW, translate(NLS_COMMAND_COURSEPREVIEW), this, "o_icon_preview");
				publishLink = LinkFactory.createToolLink(CMD_PUBLISH, translate(NLS_COMMAND_PUBLISH), this, "o_icon_publish");
				publishLink.setElementCssClass("o_sel_course_editor_publish");

				// validate course and update course status
				euce.getCourseEditorEnv().validateCourse();
				StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
				updateCourseStatusMessages(getLocale(), courseStatus);
	
				// add as listener to course so we are being notified about course events:
				// - deleted events
				CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), course);
				// activate course root node
				String nodeIdent = cetm.getRootNode().getIdent();
				if(selectedNode != null) {
					CourseEditorTreeNode editorNode = cetm.getCourseEditorNodeContaining(selectedNode);
					if(editorNode != null) {
						nodeIdent = editorNode.getIdent();
					}
				}
				menuTree.setSelectedNodeId(nodeIdent);
				updateViewForSelectedNodeId(ureq, nodeIdent);
			}
		} catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION+" [in <init>]", e);		
			dispose();
			throw e;
		}
	}

	@Override
	public void initToolbar() {
		stackPanel.addTool(createNodeLink, Align.left);
		stackPanel.addTool(nodeTools, Align.left);
		stackPanel.addTool(statusLink, Align.right);
		stackPanel.addTool(previewLink, Align.right);
		stackPanel.addTool(publishLink, Align.right);
	}

	@Override
	public boolean requestForClose(UserRequest ureq) {
		boolean immediateClose = true;
		try {
			ICourse course = CourseFactory.loadCourse(ores.getResourceableId());
			if(hasPublishableChanges(course)) {
				doQuickPublish(ureq, course);
				immediateClose = false;
			}
		} catch (CorruptedCourseException | NullPointerException e) {
			logError("Error request on close: " + ores, e);
		}
		return immediateClose;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		try {
			ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
			
			if (source == menuTree) {
				if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
					// goto node in edit mode
					TreeEvent te = (TreeEvent) event;
					String nodeId = te.getNodeId();
					updateViewForSelectedNodeId(ureq, nodeId);				
				} else if(event.getCommand().equals(MenuTree.COMMAND_TREENODE_DROP)) {
					TreeDropEvent te = (TreeDropEvent) event;
					dropNodeAsChild(ureq, course, te.getDroppedNodeId(), te.getTargetNodeId(), te.isAsChild(), te.isAtTheEnd());
				}
			} else if (source == main) {
				if (event.getCommand().startsWith(NLS_START_HELP_WIZARD)) {
					doStartHelpWizard(ureq, event);
				}
			} else if (source == undelButton){
				doUndelete(ureq, course);
			} else if(source == alternativeLink) {
				CourseNode chosenNode = (CourseNode)alternativeLink.getUserObject();
				askForAlternative(ureq, chosenNode);
			} else if(previewLink == source) {
				launchPreview(ureq, course);
			} else if(publishLink == source) {
				launchPublishingWizard(ureq, course, false);
			} else if(closeLink == source) {
				doReleaseEditLock();
				fireEvent(ureq, Event.DONE_EVENT);
			} else if(createNodeLink == source) { 
				doOpenNodeTypeChooser(ureq);
			} else if(deleteNodeLink == source) {
				doDeleteNode(ureq);
			} else if(moveNodeLink == source) {
				doMove(ureq, course, false);
			} else if(copyNodeLink == source) {
				doMove(ureq, course, true);
			} else if(statusLink == source) {
				doOpenStatusOverview(ureq);
			}
		} catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION+" [in event(UserRequest,Component,Event)]", e);			
			dispose();
			throw e;
		}
	}
	
	private void askForAlternative(UserRequest ureq, CourseNode chosenNode) {
		removeAsListenerAndDispose(alternateCtr);
		removeAsListenerAndDispose(cmc);

		alternateCtr = new AlternativeCourseNodeController(ureq, getWindowControl(), chosenNode);				
		listenTo(alternateCtr);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), alternateCtr.getInitialComponent(), true, translate("alternative.choose"));
		listenTo(cmc);
		cmc.activate();
	}
	
	/**
	 * The following operation are done:
	 * <ul>
	 * 	<li>create a new instance of the replacement type
	 * 	<li>add the new element below the original element
	 * 	<li>copy the element title, description and the generic configuration options
	 * 	<li>copy the access, visibility and scoring rules (easy and expert)
	 * 	<li>optionally copy some other configuration if this is possible at all
	 * 	<li>move all child elements from the original to the replacement element
	 * 	<li>mark the original element as deleted
	 * </ul>
	 * 
	 * @param chosenNode
	 * @param selectAlternative
	 */
	private void doCreateAlternateBuildingBlock(UserRequest ureq, ICourse course, CourseNode chosenNode, String selectAlternative) {
		if(!StringHelper.containsNonWhitespace(selectAlternative)) return;

		//create the alternative node
		CourseNodeConfiguration newConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(selectAlternative);
		CourseNode newNode = newConfig.getInstance();
		//copy configurations
		chosenNode.copyConfigurationTo(newNode, course);
		//insert the node
		CourseEditorTreeNode cetn = (CourseEditorTreeNode)cetm.getNodeById(chosenNode.getIdent());
		CourseEditorTreeNode parentNode = (CourseEditorTreeNode)cetn.getParent();
		int position = cetn.getPosition() + 1;
		CourseEditorTreeNode newCetn = course.getEditorTreeModel().insertCourseNodeAt(newNode, parentNode.getCourseNode(), position);
		doInsert(ureq, newNode);
		
		//copy the children
		while(cetn.getChildCount() > 0) {
			CourseEditorTreeNode childNode = (CourseEditorTreeNode)cetn.getChildAt(0);
			newCetn.addChild(childNode);
		}
		
		//set all dirty
		TreeVisitor tv = new TreeVisitor( new Visitor() {
			@Override
			public void visit(INode node) {
				((CourseEditorTreeNode)node).setDirty(true);
			}
		}, newCetn, true);
		tv.visitAll();
		
		//mark as deleted
		doDelete(course, chosenNode.getIdent());

		//save
		CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
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
			deleteNodeLink.setEnabled(false);
			moveNodeLink.setEnabled(false);
			copyNodeLink.setEnabled(false);

			if (((CourseEditorTreeNode) cetn.getParent()).isDeleted()) {
				main.setPage(VELOCITY_ROOT + "/deletednode.html");
			} else {
				main.setPage(VELOCITY_ROOT + "/undeletenode.html");
			}
		} else {
			tabbedNodeConfig.setVisible(true);
			deleteNodeLink.setEnabled(true);
			moveNodeLink.setEnabled(true);
			copyNodeLink.setEnabled(true);

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
			nodeEditCntrllr = chosenNode.createEditController(ureq, getWindowControl(), stackPanel, course, euce);
			listenTo(nodeEditCntrllr);
			nodeEditCntrllr.addTabs(tabbedNodeConfig);
		}
		boolean disabled = !cnConfig.isEnabled();
		boolean deprecated = cnConfig.isDeprecated();
		main.contextPut("courseNodeDisabled", disabled);
		main.contextPut("courseNodeDeprecated", deprecated);
		main.contextPut("courseNodeDeprecatedHasAlternative", !cnConfig.getAlternativeCourseNodes().isEmpty());
		alternativeLink.setVisible((disabled || deprecated) && !cnConfig.getAlternativeCourseNodes().isEmpty());
		alternativeLink.setUserObject(chosenNode);
		String nodeCssClass = null;
		if (this.cetm.getRootNode().getIdent().equals(chosenNode.getIdent())) {
			// Special case for root node
			nodeCssClass = "o_CourseModule_icon";
		} else {
			nodeCssClass = cnConfig.getIconCSSClass();					
		}
		main.contextPut("courseNodeCss", nodeCssClass);
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
	private void jumpToNodeEditor(String activatorIdent, UserRequest ureq, CourseNode chosenNode) {
		initNodeEditor(ureq, chosenNode);
		if (nodeEditCntrllr instanceof ActivateableTabbableDefaultController) {
			OLATResourceable activeOres = OresHelper.createOLATResourceableInstanceWithoutCheck(activatorIdent, 0l);
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(activeOres);
			((ActivateableTabbableDefaultController) nodeEditCntrllr).activate(ureq, entries, null);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		try {
		ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		
		if (source == nodeEditCntrllr) {
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
				TreeNode node = menuTree.getSelectedNode();
				if(node instanceof CourseEditorTreeNode) {
					CourseEditorTreeNode cet = (CourseEditorTreeNode)node;
					main.contextPut("courseNode", cet.getCourseNode());
				}
			}
		} else if (source == statusCtr) {
			if (event.getCommand().startsWith(NLS_START_HELP_WIZARD)) {
				doStartHelpWizard(ureq, event);
			}
			calloutCtrl.deactivate();
			cleanUp();
		} else if (source == chooseNodeTypeCtr) {
			cmc.deactivate();

			String cmd = event.getCommand();
			if(cmd.startsWith(TB_ACTION)) {
				CourseNode newNode = chooseNodeTypeCtr.getCreatedNode();
				cleanUp();		
				doInsert(ureq, newNode);
			} else if(CMD_MULTI_SP.equals(cmd)) {
				cleanUp();
				launchSinglePagesWizard(ureq, course);
			} else if(CMD_MULTI_CHECKLIST.equals(cmd)) {
				cleanUp();
				launchChecklistsWizard(ureq);
			}
		} else if (source == publishStepsController) {
			getWindowControl().pop();
			
			Object requestOnClose = publishStepsController.getRunContext().get("requestOnClose");
			removeAsListenerAndDispose(publishStepsController);
			publishStepsController = null;
			// reset to root node... may have published a deleted node -> this
			// resets the view
			//else Event.DONE -> nothing changed / else Event.CANCELLED -> cancelled wizard	
			updateAfterPublishing(ureq, course, event == Event.CHANGED_EVENT);
			if(Boolean.TRUE.equals(requestOnClose)) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(source == quickPublishCtr) {
			if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				fireEvent(ureq, Event.DONE_EVENT);
			} else if(event == MANUAL_PUBLISH) {
				cmc.deactivate();
				launchPublishingWizard(ureq, course, true);
			} else if(event == Event.CHANGED_EVENT) {
				updateAfterPublishing(ureq, course, event == Event.CHANGED_EVENT);
				cleanUpNodeController();
				cmc.deactivate();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if (source == previewController) {
			if (event == Event.DONE_EVENT) {
				// no need to deactivate preview controller, already done internally
				removeAsListenerAndDispose(previewController);
				previewController = null;
			}
			
		} else if (source == checklistWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(checklistWizard);
				checklistWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					menuTree.setDirty(true);
					CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
					euce.getCourseEditorEnv().validateCourse();
					StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
					updateCourseStatusMessages(ureq.getLocale(), courseStatus);
				}
			}
		} else if (source == cmc) {
			cleanUp();
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
			
			//aggressive clean-up
			cleanUp();
		} else if (source == deleteDialogController){
			removeAsListenerAndDispose(deleteDialogController);
			deleteDialogController = null;
			if (DialogBoxUIFactory.isYesEvent(event)){
				// delete confirmed
				String ident = menuTree.getSelectedNode().getIdent();
				// udpate the current node in the course editor environment
				doDelete(course, ident);
			} else {
				tabbedNodeConfig.setVisible(true);
			}
		} else if (source == multiSPChooserCtr) {
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(multiSPChooserCtr);
			cmc = null;

			if(event == Event.CHANGED_EVENT) {
				menuTree.setDirty(true);
				euce.getCourseEditorEnv().validateCourse();
				StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
				updateCourseStatusMessages(ureq.getLocale(), courseStatus);
			}
		} else if (source == alternateCtr) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT) {
				CourseNode chosenNode = alternateCtr.getCourseNode();
				String selectAlternative = alternateCtr.getSelectedAlternative();
				doCreateAlternateBuildingBlock(ureq, course, chosenNode, selectAlternative);
			}
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(alternateCtr);
			cmc = null;
			alternateCtr = null;
		}
    } catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION+" [in event(UserRequest,Controller,Event)]", e);			
			this.dispose();
			throw e;
		}
	}
	
	/**
	 * Aggressive clean-up of popup controllers
	 */
	private void cleanUp() {
		removeAsListenerAndDispose(moveCopyController);
		removeAsListenerAndDispose(multiSPChooserCtr);
		removeAsListenerAndDispose(chooseNodeTypeCtr);
		removeAsListenerAndDispose(alternateCtr);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(statusCtr);
		removeAsListenerAndDispose(cmc);
		moveCopyController = null;
		chooseNodeTypeCtr = null;
		multiSPChooserCtr = null;
		alternateCtr = null;
		calloutCtrl = null;
		statusCtr = null;
		cmc = null;
	}
	
	private void cleanUpNodeController() {
		tabbedNodeConfig.removeAll();
		// dispose old one, if there was one
		removeAsListenerAndDispose(nodeEditCntrllr);
	}
	
	private void updateAfterPublishing(UserRequest ureq, ICourse course, boolean changed) {
		cetm = course.getEditorTreeModel();
		menuTree.setTreeModel(cetm);
		String rootNodeIdent = menuTree.getTreeModel().getRootNode().getIdent();
		menuTree.setSelectedNodeId(rootNodeIdent);
		updateViewForSelectedNodeId(ureq, rootNodeIdent);
		if(changed) {					
			showInfo("pbl.success");
			// do logging
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_PUBLISHED, getClass());
		}
	}
	
	private void doMove(UserRequest ureq, ICourse course, boolean copy) {
		if(moveCopyController != null) return;
		
		TreeNode tn = menuTree.getSelectedNode();
		if (tn == null) {
			showError(NLS_MOVECOPYNODE_ERROR_SELECTFIRST);
			return;
		}
		if (tn.getParent() == null) {
			showError(NLS_MOVECOPYNODE_ERROR_ROOTNODE);
			return;
		}
		removeAsListenerAndDispose(moveCopyController);
		removeAsListenerAndDispose(cmc);
		
		CourseEditorTreeNode cetn = cetm.getCourseEditorNodeById(tn.getIdent());
		moveCopyController = new MoveCopySubtreeController(ureq, getWindowControl(), course, cetn, copy);				
		listenTo(moveCopyController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), moveCopyController.getInitialComponent(), true, translate(NLS_INSERTNODE_TITLE));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doQuickPublish(UserRequest ureq, ICourse course) {
		removeAsListenerAndDispose(quickPublishCtr);
		removeAsListenerAndDispose(cmc);
		
		quickPublishCtr = new QuickPublishController(ureq, getWindowControl(), course);
		listenTo(quickPublishCtr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", quickPublishCtr.getInitialComponent(),
				true, translate("pbl.quick.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(ICourse course, String ident) {
		CourseNode activeNode = cetm.getCourseNode(ident);

		cetm.markDeleted(activeNode);
		menuTree.setDirty(true);
	
		CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
		tabbedNodeConfig.removeAll();
		tabbedNodeConfig.setVisible(false);
		deleteNodeLink.setEnabled(false);
		moveNodeLink.setEnabled(false);
		copyNodeLink.setEnabled(false);

		main.setPage(VELOCITY_ROOT + "/undeletenode.html"); // offer undelete
		showInfo(NLS_DELETENODE_SUCCESS);
		// validate course and update course status
		euce.getCourseEditorEnv().validateCourse();
		StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
		updateCourseStatusMessages(getLocale(), courseStatus);

		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_DELETED, getClass(),
				LoggingResourceable.wrap(activeNode));
	}
	
	private void doUndelete(UserRequest ureq, ICourse course) {
		String ident = menuTree.getSelectedNode().getIdent();
		CourseEditorTreeNode activeNode = (CourseEditorTreeNode) cetm.getNodeById(ident);
		euce.getCourseEditorEnv().setCurrentCourseNodeId(activeNode.getIdent());
		
		CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
		cetm.markUnDeleted(activeNode);
		menuTree.setDirty(true);
		// show edit panels again
		initNodeEditor(ureq, activeNode.getCourseNode());
		tabbedNodeConfig.setVisible(true);
		deleteNodeLink.setEnabled(true);
		moveNodeLink.setEnabled(true);
		copyNodeLink.setEnabled(true);

		main.setPage(VELOCITY_ROOT + "/index.html");
		// validate course and update course status
		euce.getCourseEditorEnv().validateCourse();
		StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
		updateCourseStatusMessages(ureq.getLocale(), courseStatus);
		// do logging
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_RESTORED, getClass(),
				LoggingResourceable.wrap(activeNode.getCourseNode()));
	}
	
	private void doDeleteNode(UserRequest ureq) {
		TreeNode tn = menuTree.getSelectedNode();
		if (tn == null) {
			showError(NLS_DELETENODE_ERROR_SELECTFIRST);
		} else if (tn.getParent() == null) {
			showError(NLS_DELETENODE_ERROR_ROOTNODE);
		} else {
			// deletion is possible, start asking if really to delete.
			tabbedNodeConfig.setVisible(false);

			String title = translate("deletenode.header", tn.getTitle());
			String message = translate("deletenode.confirm");
			if(tn instanceof CourseEditorTreeNode
					&& assessmentModeMgr.isNodeInUse(repoEntry, ((CourseEditorTreeNode)tn).getCourseNode())) {
				message = translate("deletenode.confirm.inuse.assessment.mode");
			} else {
				message = translate("deletenode.confirm");
			}
			
			deleteDialogController = activateYesNoDialog(ureq, title, message, deleteDialogController);
		}
	}
	
	private void doInsert(UserRequest ureq, CourseNode newNode) {
		menuTree.setSelectedNodeId(newNode.getIdent());
		// update the current node in the editor course environment
		euce.getCourseEditorEnv().setCurrentCourseNodeId(newNode.getIdent());
		euce.getCourseEditorEnv().validateCourse();
		StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
		updateCourseStatusMessages(getLocale(), courseStatus);					
		initNodeEditor(ureq, newNode);
		// do logging
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_CREATED, getClass(),
				LoggingResourceable.wrap(newNode));
		// Resize layout columns to make all nodes viewable in the menu column
		JSCommand resizeCommand = new JSCommand("try { OPOL.adjustHeight(); } catch(e) {if(window.console) console.log(e); }");
		getWindowControl().getWindowBackOffice().sendCommandTo(resizeCommand);
	}
	
	private void doOpenNodeTypeChooser(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(chooseNodeTypeCtr);
		
		menuTree.getSelectedNode();
		
		TreeNode tn = menuTree.getSelectedNode();
		CourseEditorTreeNode cetn = tn == null ? null : cetm.getCourseEditorNodeById(tn.getIdent());
		chooseNodeTypeCtr = new ChooseNodeController(ureq, getWindowControl(), ores, cetn);
		listenTo(chooseNodeTypeCtr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), chooseNodeTypeCtr.getInitialComponent(),
				true, translate("header.insertnodes"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenStatusOverview(UserRequest ureq) {
		removeAsListenerAndDispose(statusCtr);
		
		statusCtr = new EditorStatusController(ureq, getWindowControl());
		listenTo(statusCtr);
		
		euce.getCourseEditorEnv().validateCourse();
		StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
		statusCtr.updateStatus(cetm, courseStatus);	

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				statusCtr.getInitialComponent(), statusLink, "", true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
	}
	
	private void doStartHelpWizard(UserRequest ureq, Event event) {
		String findThis = event.getCommand().substring(NLS_START_HELP_WIZARD.length());
		StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
		for (int i = 0; i < courseStatus.length; i++) {
			String key = courseStatus[i].getDescriptionForUnit() + "." + courseStatus[i].getShortDescriptionKey();
			if (key.equals(findThis)) {
				menuTree.setSelectedNodeId(courseStatus[i].getDescriptionForUnit());
				euce.getCourseEditorEnv().setCurrentCourseNodeId(courseStatus[i].getDescriptionForUnit());
				jumpToNodeEditor(courseStatus[i].getActivateableViewIdentifier(), ureq,
						cetm.getCourseNode(courseStatus[i].getDescriptionForUnit()));
				break;
			}
		}
		euce.getCourseEditorEnv().validateCourse();
		courseStatus = euce.getCourseEditorEnv().getCourseStatus();
		updateCourseStatusMessages(ureq.getLocale(), courseStatus);
	}

	private void dropNodeAsChild(UserRequest ureq, ICourse course, String droppedNodeId, String targetNodeId, boolean asChild, boolean atTheEnd) {
		menuTree.setDirty(true); // setDirty when moving
		CourseNode droppedNode = cetm.getCourseNode(droppedNodeId);

		int position;
		CourseEditorTreeNode insertParent;
		if(asChild) {
			insertParent = cetm.getCourseEditorNodeById(targetNodeId);
			position = atTheEnd ? -1 : 0;
		} else {
			CourseEditorTreeNode selectedNode = cetm.getCourseEditorNodeById(targetNodeId);
			CourseEditorTreeNode droppedEditorNode = cetm.getCourseEditorNodeById(droppedNodeId);
			if(selectedNode.getParent() == null) {
				//root node
				insertParent = selectedNode;
				position = 0;
			} else {
				boolean currentlySiblings = droppedEditorNode != null && droppedEditorNode.getParent() != null
						&& droppedEditorNode.getParent().getIdent().equals(selectedNode.getParent().getIdent());

				insertParent = course.getEditorTreeModel().getCourseEditorNodeById(selectedNode.getParent().getIdent());
				int currentDroppedNodePosition;
				for(currentDroppedNodePosition=insertParent.getChildCount(); currentDroppedNodePosition-->0; ) {
					if(insertParent.getChildAt(currentDroppedNodePosition).getIdent().equals(droppedNode.getIdent())) {
						break;
					}
				}

				for(position=insertParent.getChildCount(); position-->0; ) {
					if(insertParent.getChildAt(position).getIdent().equals(selectedNode.getIdent())) {
						if(!currentlySiblings || currentDroppedNodePosition > position) {
							position++;
						}
						break;
					}
				}
			}
		}
		
		CourseEditorTreeNode moveFrom = course.getEditorTreeModel().getCourseEditorNodeById(droppedNode.getIdent());
		//check if an ancestor is not dropped on a child
		if (course.getEditorTreeModel().checkIfIsChild(insertParent, moveFrom)) {					
			showError("movecopynode.error.overlap");
			fireEvent(ureq, Event.CANCELLED_EVENT);
			return;
		}
		
		//don't generate red screen for that. If the position is too high -> add the node at the end
		if(position >= insertParent.getChildCount()) {
			position = -1;
		}

		try {
			if(position >= 0) {
				insertParent.insert(moveFrom, position);
			} else {
				insertParent.addChild(moveFrom);
			}
		} catch (IndexOutOfBoundsException e) {
			logError("", e);
			//reattach the node as security, if not, the node is lost
			insertParent.addChild(moveFrom);
		}

		moveFrom.setDirty(true);
		//mark subtree as dirty
		TreeVisitor tv = new TreeVisitor( new Visitor() {
			@Override
			public void visit(INode node) {
				CourseEditorTreeNode cetn = (CourseEditorTreeNode)node;
				cetn.setDirty(true);
			}
		}, moveFrom, true);
		tv.visitAll();					
		
		CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
		showInfo("movecopynode.info.condmoved");
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_MOVED, getClass());

		euce.getCourseEditorEnv().validateCourse();
		StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
		updateCourseStatusMessages(ureq.getLocale(), courseStatus);
	}

	/**
	 * @param ureq
	 * @param courseStatus
	 */
	private void updateCourseStatusMessages(Locale locale, StatusDescription[] courseStatus) {
		List<String> errorIsForNode = new ArrayList<>();
		List<String> errorMessage = new ArrayList<>();
		List<String> errorHelpWizardLink = new ArrayList<>();
		List<String> warningIsForNode = new ArrayList<>();
		List<String> warningMessage = new ArrayList<>();
		List<String> warningHelpWizardLink = new ArrayList<>();
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
		
		if (errCnt > 0) {
			statusLink.setBadge(Integer.toString(errCnt), Level.error);
		} else if (warCnt > 0) {
			statusLink.setBadge(Integer.toString(warCnt), Level.warning);
		} else {
			statusLink.setBadge("\u2713", Level.success);
		}
	}

	private void launchPublishingWizard(UserRequest ureq, ICourse course, boolean requestOnClose) {
		if(publishStepsController != null) return;//ignore enter
		
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
			@Override
			public Step execute(UserRequest ureq1, WindowControl wControl1, StepsRunContext runContext) {
				//all information to do now is within the runContext saved
				boolean hasChanges = false;
				
				PublishProcess publishManager = (PublishProcess)runContext.get("publishProcess");
				PublishEvents publishEvents = publishManager.getPublishEvents();
				if (runContext.containsKey("validPublish") && ((Boolean)runContext.get("validPublish")).booleanValue()) {
					@SuppressWarnings("unchecked")
					Collection<String> selectedNodeIds = (Collection<String>) runContext.get("publishSetCreatedFor");
					hasChanges = (selectedNodeIds != null) && (selectedNodeIds.size() > 0);
					if (hasChanges) {
						publishManager.applyPublishSet(ureq1.getIdentity(), ureq1.getLocale(), false);
					}
				}
				
				if (runContext.containsKey("accessAndProperties")) {
					CourseAccessAndProperties accessAndProperties = (CourseAccessAndProperties) runContext.get("accessAndProperties");
					// fires an EntryChangedEvent for repository entry notifying
					// about modification.
					publishManager.changeAccessAndProperties(getIdentity(), accessAndProperties);
					hasChanges = true;					
				}
				
				CourseCatalog courseCatalog = (CourseCatalog)runContext.get("categories");
				if(courseCatalog != null) {
					publishManager.publishToCatalog(courseCatalog.getChoiceValue(), courseCatalog.getCategoryLabels());
				}
				
				if(publishEvents.getPostPublishingEvents().size() > 0) {
					for(MultiUserEvent event:publishEvents.getPostPublishingEvents()) {
						CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, ores);
					}
				}

				// signal correct completion and tell if changes were made or not.
				return hasChanges ? StepsMainRunController.DONE_MODIFIED : StepsMainRunController.DONE_UNCHANGED;
			}
		};

		publishStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("publish.wizard.title"), "o_sel_course_publish_wizard");
		listenTo(publishStepsController);
		publishStepsController.getRunContext().put("requestOnClose", requestOnClose);
		getWindowControl().pushAsModalDialog(publishStepsController.getInitialComponent());
	}
	
	private void launchPreview(UserRequest ureq, ICourse course) {
		previewController = new PreviewConfigController(ureq, getWindowControl(), course);
		listenTo(previewController);
		stackPanel.pushController(translate("command.coursepreview"), previewController);
	}
	
	private void launchSinglePagesWizard(UserRequest ureq, ICourse course) {
		removeAsListenerAndDispose(multiSPChooserCtr);
		removeAsListenerAndDispose(cmc);
		
		VFSContainer rootContainer = course.getCourseEnvironment().getCourseFolderContainer(CourseContainerOptions.withoutElements());
		CourseEditorTreeNode selectedNode = (CourseEditorTreeNode)menuTree.getSelectedNode();
		multiSPChooserCtr = new MultiSPController(ureq, getWindowControl(), rootContainer, ores, selectedNode);
		listenTo(multiSPChooserCtr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				multiSPChooserCtr.getInitialComponent(), true, translate("multi.sps.title"));
		listenTo(cmc);
		cmc.activate();
	} 
	
	private void launchChecklistsWizard(UserRequest ureq) {
		removeAsListenerAndDispose(checklistWizard);

		Step start = new CheckList_1_CheckboxStep(ureq, ores);
		StepRunnerCallback finish = new CheckListStepRunnerCallback(ores);
		checklistWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("checklist.wizard"), "o_sel_checklist_wizard", "Assessment#_checklist_multiple");
		listenTo(checklistWizard);
		getWindowControl().pushAsModalDialog(checklistWizard.getInitialComponent());
	}

	/**
	 * @return true if lock on this course has been acquired, flase otherwhise
	 */
	public LockResult getLockEntry() {
		return lockEntry;
	}
	
	public boolean hasPublishableChanges(ICourse course) {
		if(cetm == null || course == null) {
			return false;
		}
		PublishProcess publishProcess = PublishProcess.getInstance(course, cetm, getLocale());
		PublishTreeModel publishTreeModel = publishProcess.getPublishTreeModel();
		return publishTreeModel.hasPublishableChanges();
	}

	@Override
	protected void doDispose() {
		ICourse course = CourseFactory.loadCourse(ores.getResourceableId());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, course);
		OLATResourceable lockEntryOres = OresHelper.createOLATResourceableInstance(LockEntry.class, 0l);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, lockEntryOres);

		// those controllers are disposed by BasicController:
		nodeEditCntrllr = null;
		publishStepsController = null;
		deleteDialogController = null;
		cmc = null;
		moveCopyController = null;
		previewController = null;
		//toolC = null;
		columnLayoutCtr = null;
		moveCopyController = null;
		
		doReleaseEditLock();
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_CLOSE, getClass());
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
	@Override
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
			} else if (event instanceof LockRemovedEvent) {
				LockRemovedEvent lockEvent = (LockRemovedEvent)event;
				if(lockEntry != null && lockEntry.getLockEntry() != null && lockEntry.getLockEntry().equals(lockEvent.getLockEntry())) {
					this.dispose();
				}
			}
		} catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION+" [in event(Event)]", e);			
			dispose();
			throw e;
		}
	}
}