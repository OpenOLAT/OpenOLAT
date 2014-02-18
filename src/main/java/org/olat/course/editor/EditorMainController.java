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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.HtmlHeaderComponent;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerImpl;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.tree.TreeDropEvent;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
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
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.area.CourseAreasController;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.ui.courselayout.CourseLayoutHelper;
import org.olat.course.editor.PublishStepCatalog.CategoryLabel;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.cl.ui.wizard.CheckListStepRunnerCallback;
import org.olat.course.nodes.cl.ui.wizard.CheckList_1_CheckboxStep;
import org.olat.course.run.preview.PreviewConfigController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
	private static final String CMD_COURSEAREAS = "careas";
	private static final String CMD_COURSEPREVIEW = "cprev";
	private static final String CMD_KEEPCLOSED_ERROR = "keep.closed.error";
	private static final String CMD_KEEPOPEN_ERROR = "keep.open.error";
	private static final String CMD_KEEPCLOSED_WARNING = "keep.closed.warning";
	private static final String CMD_KEEPOPEN_WARNING = "keep.open.warning";
	private static final String CMD_MULTI_SP = "cmp.multi.sp";
	private static final String CMD_MULTI_CHECKLIST = "cmp.multi.checklist";

	// NLS support
	
	private static final String NLS_PUBLISHED_NEVER_YET = "published.never.yet";
	private static final String NLS_PUBLISHED_LATEST = "published.latest";
	private static final String NLS_HEADER_TOOLS = "header.tools";
	private static final String NLS_COMMAND_COURSEFOLDER = "command.coursefolder";
	private static final String NLS_COMMAND_COURSEAREAS = "command.courseareas";
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
	private static final String NLS_ADMIN_HEADER = "command.admin.header";
	private static final String NLS_MULTI_SPS = "command.multi.sps";
	private static final String NLS_MULTI_CHECKLIST = "command.multi.checklist";
	
	private Boolean errorIsOpen = Boolean.TRUE;
	private Boolean warningIsOpen = Boolean.FALSE;

	private MenuTree menuTree;
	private VelocityContainer main;

	private TabbedPane tabbedNodeConfig;
	private SelectionTree selTree;

	CourseEditorTreeModel cetm;
	private TabbableController nodeEditCntrllr;
	private StepsMainRunController publishStepsController;
	private StepsMainRunController checklistWizard;
	private PreviewConfigController previewController;
	private ToolController toolC;
	private MoveCopySubtreeController moveCopyController;
	private InsertNodeController insertNodeController;
	private Controller folderController;
	private Controller areasController;
	private DialogBoxController deleteDialogController;		
	private LayoutMain3ColsController columnLayoutCtr;
	private AlternativeCourseNodeController alternateCtr;
	
	private LockResult lockEntry;
	
	private HtmlHeaderComponent hc;
	private EditorUserCourseEnvironmentImpl euce;
	
	private Link undelButton;
	private Link keepClosedErrorButton;
	private Link keepOpenErrorButton;
	private Link keepClosedWarningButton;
	private Link keepOpenWarningButton;
	private Link alternativeLink;
	private CloseableModalController cmc;
	
	private MultiSPController multiSPChooserCtr;
	private final StackedController stackPanel;

	private final OLATResourceable ores;
	
	private OLog log = Tracing.createLoggerFor(this.getClass());
	private final static String RELEASE_LOCK_AT_CATCH_EXCEPTION = "Must release course lock since an exception occured in " + EditorMainController.class;
	
	/**
	 * Constructor for the course editor controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window controller
	 * @param course The course
	 */
	public EditorMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores,
			StackedController externStack, CourseNode selectedNode) {
		super(ureq,wControl);
		this.ores = ores;
		stackPanel = externStack == null
				? new StackedControllerImpl(getWindowControl(), getTranslator(), "o_course_breadcrumbs") : externStack;

		// OLAT-4955: setting the stickyActionType here passes it on to any controller defined in the scope of the editor,
		//            basically forcing any logging action called within the course editor to be of type 'admin'
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		addLoggingResourceable(LoggingResourceable.wrap(CourseFactory.loadCourse(ores)));
		
		// try to acquire edit lock for this course.			
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, ureq.getIdentity(), CourseFactory.COURSE_EDITOR_LOCK);
		OLATResourceable lockEntryOres = OresHelper.createOLATResourceableInstance(LockEntry.class, 0l);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), lockEntryOres);
		
		try {			
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_OPEN, getClass());

		if (lockEntry.isSuccess()) {			
			ICourse course = CourseFactory.openCourseEditSession(ores.getResourceableId());
			main = createVelocityContainer("index");
			
			Panel empty = new Panel("empty");// empty panel set as "menu" and "tool"
			OLATResourceable courseOres = OresHelper.createOLATResourceableInstance("CourseModule", ores.getResourceableId());
			RepositoryEntry repo = RepositoryManager.getInstance().lookupRepositoryEntry(courseOres, false);
			Controller courseCloser = CourseFactory.createDisposedCourseRestartController(ureq, wControl, repo);
			Controller disposedRestartController = new LayoutMain3ColsController(ureq, wControl, empty, empty,
					courseCloser.getInitialComponent(), "disposed course" + course.getResourceableId());
			setDisposedMsgController(disposedRestartController);
			
			undelButton = LinkFactory.createButton("undeletenode.button", main, this);
			keepClosedErrorButton = LinkFactory.createCustomLink("keepClosedErrorButton", CMD_KEEPCLOSED_ERROR, "keep.closed", Link.BUTTON_SMALL, main, this);
			keepOpenErrorButton = LinkFactory.createCustomLink("keepOpenErrorButton", CMD_KEEPOPEN_ERROR, "keep.open", Link.BUTTON_SMALL, main, this);
			keepClosedWarningButton = LinkFactory.createCustomLink("keepClosedWarningButton", CMD_KEEPCLOSED_WARNING, "keep.closed", Link.BUTTON_SMALL, main, this);
			keepOpenWarningButton = LinkFactory.createCustomLink("keepOpenWarningButton", CMD_KEEPOPEN_WARNING, "keep.open", Link.BUTTON_SMALL, main, this);
			
			// set the custom course css
			enableCustomCss(ureq);

			menuTree = new MenuTree("luTree");
			menuTree.setExpandSelectedNode(false);
			menuTree.setDragEnabled(true);
			menuTree.setDropEnabled(true);
			menuTree.setDropSiblingEnabled(true);	
			menuTree.setDndAcceptJSMethod("treeAcceptDrop_notWithChildren");	

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
			menuTree.setOpenNodeIds(Collections.singleton(cetm.getRootNode().getIdent()));
			menuTree.addListener(this);

			selTree = new SelectionTree("selection", getTranslator());
			selTree.setTreeModel(cetm);
			selTree.setActionCommand("processpublish");
			selTree.setFormButtonKey("publizieren");
			selTree.addListener(this);

			tabbedNodeConfig = new TabbedPane("tabbedNodeConfig", ureq.getLocale());
			main.put(tabbedNodeConfig.getComponentName(), tabbedNodeConfig);
			
			alternativeLink = LinkFactory.createButton("alternative", main, this);
			main.put("alternative", alternativeLink);

			toolC = ToolFactory.createToolController(getWindowControl());
			listenTo(toolC);
			toolC.addHeader(translate(NLS_HEADER_TOOLS));
			toolC.addLink(CMD_COURSEFOLDER, translate(NLS_COMMAND_COURSEFOLDER), CMD_COURSEFOLDER, "o_toolbox_coursefolder");
			toolC.addLink(CMD_COURSEAREAS, translate(NLS_COMMAND_COURSEAREAS), CMD_COURSEAREAS, "o_toolbox_courseareas");
			toolC.addLink(CMD_COURSEPREVIEW, translate(NLS_COMMAND_COURSEPREVIEW), CMD_COURSEPREVIEW, "b_toolbox_preview" );
			toolC.addLink(CMD_PUBLISH, translate(NLS_COMMAND_PUBLISH), CMD_PUBLISH,"b_toolbox_publish" );
			if(externStack == null) {
				toolC.addLink(CMD_CLOSEEDITOR, translate(NLS_COMMAND_CLOSEEDITOR), null, "b_toolbox_close");
			}

			toolC.addHeader(translate(NLS_HEADER_INSERTNODES));
			CourseNodeFactory cnf = CourseNodeFactory.getInstance();
			for (String courseNodeAlias : cnf.getRegisteredCourseNodeAliases()) {
				CourseNodeConfiguration cnConfig = cnf.getCourseNodeConfiguration(courseNodeAlias);
				try {
					toolC.addLink(TB_ACTION + courseNodeAlias, cnConfig.getLinkText(ureq.getLocale()), courseNodeAlias, cnConfig.getIconCSSClass());
				} catch (Exception e) {
					log.error("Error while trying to add a course buildingblock of type \""+courseNodeAlias +"\" to the editor", e);
				}
			}
			
			toolC.addHeader(translate(NLS_ADMIN_HEADER));
			toolC.addLink(CMD_MULTI_SP, translate(NLS_MULTI_SPS), CMD_MULTI_SP, "b_toolbox_copy");
			toolC.addLink(CMD_MULTI_CHECKLIST, translate(NLS_MULTI_CHECKLIST), CMD_MULTI_CHECKLIST, "b_toolbox_copy");

			toolC.addHeader(translate(NLS_COMMAND_DELETENODE_HEADER));
			toolC.addLink(CMD_DELNODE, translate(NLS_COMMAND_DELETENODE), CMD_DELNODE, "b_toolbox_delete");
			toolC.addLink(CMD_MOVENODE, translate(NLS_COMMAND_MOVENODE), CMD_MOVENODE, "b_toolbox_move");
			toolC.addLink(CMD_COPYNODE, translate(NLS_COMMAND_COPYNODE), CMD_COPYNODE, "b_toolbox_copy");
			
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, toolC.getInitialComponent(), main, "course" + course.getResourceableId());			
			columnLayoutCtr.addCssClassToMain("o_editor");
			listenTo(columnLayoutCtr);
			
			if(externStack == null) {
				putInitialPanel(stackPanel.getInitialComponent());
				stackPanel.pushController(course.getCourseTitle(), columnLayoutCtr);
			} else {
				putInitialPanel(columnLayoutCtr.getInitialComponent());
			}

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
			//fxdiff VCRP-9: drag and drop in menu tree
			} else if(event.getCommand().equals(MenuTree.COMMAND_TREENODE_DROP)) {
				TreeDropEvent te = (TreeDropEvent) event;
				dropNodeAsChild(ureq, course, te.getDroppedNodeId(), te.getTargetNodeId(), te.isAsChild(), te.isAtTheEnd());
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
						jumpToNodeEditor(courseStatus[i].getActivateableViewIdentifier(), ureq,
								cetm.getCourseNode(courseStatus[i].getDescriptionForUnit()));
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
		} else if(source == alternativeLink) {
			CourseNode chosenNode = (CourseNode)alternativeLink.getUserObject();
			askForAlternative(ureq, chosenNode);
		}
		} catch (RuntimeException e) {
			log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION+" [in event(UserRequest,Component,Event)]", e);			
			this.dispose();
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
		chosenNode.copyConfigurationTo(newNode);
		//insert the node
		CourseEditorTreeNode cetn = (CourseEditorTreeNode)cetm.getNodeById(chosenNode.getIdent());
		CourseEditorTreeNode parentNode = (CourseEditorTreeNode)cetn.getParent();
		int position = cetn.getPosition() + 1;
		CourseEditorTreeNode newCetn =course.getEditorTreeModel().insertCourseNodeAt(newNode, parentNode.getCourseNode(), position);
		doInsert(ureq, newNode);
		
		//copy the children
		while(cetn.getChildCount() > 0) {
			CourseEditorTreeNode childNode = (CourseEditorTreeNode)cetn.getChildAt(0);
			newCetn.addChild(childNode);
		}
		
		//set all dirty
		TreeVisitor tv = new TreeVisitor( new Visitor() {
			public void visit(INode node) {
				CourseEditorTreeNode cetn = (CourseEditorTreeNode)node;
				cetn.setDirty(true);
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
			nodeEditCntrllr = chosenNode.createEditController(ureq, getWindowControl(), stackPanel, course, euce);
			listenTo(nodeEditCntrllr);
			nodeEditCntrllr.addTabs(tabbedNodeConfig);
		}
		boolean disabled = !cnConfig.isEnabled();
		main.contextPut("courseNodeDisabled", disabled);
		alternativeLink.setVisible(disabled && !cnConfig.getAlternativeCourseNodes().isEmpty());
		alternativeLink.setUserObject(chosenNode);
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
	private void jumpToNodeEditor(String activatorIdent, UserRequest ureq, CourseNode chosenNode) {
		initNodeEditor(ureq, chosenNode);
		if (nodeEditCntrllr instanceof ActivateableTabbableDefaultController) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck(activatorIdent, 0l);
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(ores);
			((ActivateableTabbableDefaultController) nodeEditCntrllr).activate(ureq, entries, null);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		try {
		ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		
		if (source == toolC) {
			if (event.getCommand().startsWith(TB_ACTION)) {
				String cnAlias = event.getCommand().substring(TB_ACTION.length());
				if (cnAlias == null) throw new AssertException("Received event from ButtonController which is not registered with the toolbox.");
				removeAsListenerAndDispose(insertNodeController);
				removeAsListenerAndDispose(cmc);
				
				Codepoint.codepoint(EditorMainController.class, "startInsertNode");
				insertNodeController = new InsertNodeController(ureq, getWindowControl(), course, cnAlias);				
				listenTo(insertNodeController);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), insertNodeController.getInitialComponent(), true, translate(NLS_INSERTNODE_TITLE));
				listenTo(cmc);
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
				removeAsListenerAndDispose(moveCopyController);
				removeAsListenerAndDispose(cmc);
				
				CourseEditorTreeNode cetn = cetm.getCourseEditorNodeById(tn.getIdent());
				moveCopyController = new MoveCopySubtreeController(ureq, getWindowControl(), course, cetn, event.getCommand().equals(CMD_COPYNODE));				
				listenTo(moveCopyController);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), moveCopyController.getInitialComponent(), true, translate(NLS_INSERTNODE_TITLE));
				listenTo(cmc);
				cmc.activate();
			}
			
			else if (event.getCommand().equals(CMD_CLOSEEDITOR)) {
				doReleaseEditLock();
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event.getCommand().equals(CMD_PUBLISH)) {
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
					
					public Step execute(UserRequest ureq1, WindowControl wControl1, StepsRunContext runContext) {
						//all information to do now is within the runContext saved
						boolean hasChanges = false;
						
						PublishProcess publishManager = (PublishProcess)runContext.get("publishProcess");
						PublishEvents publishEvents = publishManager.getPublishEvents();
						if (runContext.containsKey("validPublish") && ((Boolean)runContext.get("validPublish")).booleanValue()) {
							@SuppressWarnings("unchecked")
							Set<String> selectedNodeIds = (Set<String>) runContext.get("publishSetCreatedFor");
							hasChanges = (selectedNodeIds != null) && (selectedNodeIds.size() > 0);
							if (hasChanges) {
								publishManager.applyPublishSet(ureq1.getIdentity(), ureq1.getLocale());
								publishManager.applyUpdateSet(ureq1.getIdentity(), ureq1.getLocale());
							}
						}
						
						if (runContext.containsKey("changedaccess")) {
							// there were changes made to the general course access
							String newAccessStr = (String) runContext.get("changedaccess");
							int newAccess;
							boolean membersOnly = RepositoryEntry.MEMBERS_ONLY.equals(newAccessStr);
							if(membersOnly) {
								newAccess = RepositoryEntry.ACC_OWNERS;
							} else {
								newAccess = Integer.valueOf(newAccessStr);
							}
							
							// fires an EntryChangedEvent for repository entry notifying
							// about modification.
							publishManager.changeGeneralAccess(ureq1, newAccess, membersOnly);
							hasChanges = true;
						}
						
						if (runContext.containsKey("catalogChoice")) {
							String choice = (String) runContext.get("catalogChoice");
							@SuppressWarnings("unchecked")
							List<CategoryLabel> categories = (List<CategoryLabel>)runContext.get("categories");
							publishManager.publishToCatalog(choice, categories);
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

				publishStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("publish.wizard.title"), "o_sel_course_publish_wizard");
				listenTo(publishStepsController);
				getWindowControl().pushAsModalDialog(publishStepsController.getInitialComponent());
					
			} else if (event.getCommand().equals(CMD_COURSEPREVIEW)) {
				previewController = new PreviewConfigController(ureq, getWindowControl(), course);
				listenTo(previewController);
				stackPanel.pushController(translate("command.coursepreview"), previewController);
				
			} else if (event.getCommand().equals(CMD_COURSEFOLDER)) {
				// Folder for course with custom link model to jump to course nodes
				VFSContainer namedCourseFolder = new NamedContainerImpl(translate(NLS_COURSEFOLDER_NAME), course.getCourseFolderContainer());
				CustomLinkTreeModel customLinkTreeModel = new CourseInternalLinkTreeModel(course.getEditorTreeModel());
				removeAsListenerAndDispose(folderController);
				FolderRunController folderMainCtl = new FolderRunController(namedCourseFolder, true, true, true, ureq, getWindowControl(), null, customLinkTreeModel);
				folderMainCtl.addLoggingResourceable(LoggingResourceable.wrap(course));
				folderController = new LayoutMain3ColsController(ureq, getWindowControl(), folderMainCtl);
				stackPanel.pushController(translate("command.coursefolder"), folderController);
				
			} else if (event.getCommand().equals(CMD_COURSEAREAS)) {
				removeAsListenerAndDispose(areasController);
				CourseAreasController areasMainCtl = new CourseAreasController(ureq, getWindowControl(), course.getCourseEnvironment().getCourseGroupManager().getCourseResource());
				areasMainCtl.addLoggingResourceable(LoggingResourceable.wrap(course));
				areasController = new LayoutMain3ColsController(ureq, getWindowControl(), areasMainCtl);
				stackPanel.pushController(translate("command.courseareas"), areasController);
				
			} else if (event.getCommand().equals(CMD_MULTI_SP)) {
				removeAsListenerAndDispose(multiSPChooserCtr);
				VFSContainer rootContainer = course.getCourseEnvironment().getCourseFolderContainer();
				CourseEditorTreeNode selectedNode = (CourseEditorTreeNode)menuTree.getSelectedNode();
				multiSPChooserCtr = new MultiSPController(ureq, getWindowControl(), rootContainer, ores, selectedNode);
				listenTo(multiSPChooserCtr);
				
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), multiSPChooserCtr.getInitialComponent());
				listenTo(cmc);
				cmc.activate();
			} else if (event.getCommand().equals(CMD_MULTI_CHECKLIST)) {
				removeAsListenerAndDispose(checklistWizard);

				Step start = new CheckList_1_CheckboxStep(ureq);
				StepRunnerCallback finish = new CheckListStepRunnerCallback(ores);
				checklistWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
						translate("checklist.wizard"), "o_sel_checklist_wizard");
				listenTo(checklistWizard);
				getWindowControl().pushAsModalDialog(checklistWizard.getInitialComponent());
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
				previewController = null;
			}
			
		} else if (source == checklistWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(checklistWizard);
				checklistWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					menuTree.setDirty(true);
					euce.getCourseEditorEnv().validateCourse();
					StatusDescription[] courseStatus = euce.getCourseEditorEnv().getCourseStatus();
					updateCourseStatusMessages(ureq.getLocale(), courseStatus);
				}
			}
		} else if (source == cmc) {
			//aggressive clean-up
			removeAsListenerAndDispose(multiSPChooserCtr);
			removeAsListenerAndDispose(moveCopyController);
			removeAsListenerAndDispose(insertNodeController);
			removeAsListenerAndDispose(folderController);
			removeAsListenerAndDispose(alternateCtr);
			removeAsListenerAndDispose(cmc);
			moveCopyController = null;
			insertNodeController = null;
			multiSPChooserCtr = null;
			folderController = null;
			alternateCtr = null;
			cmc = null;
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
			removeAsListenerAndDispose(moveCopyController);
			removeAsListenerAndDispose(cmc);
			moveCopyController = null;
			cmc = null;
		} else if (source == insertNodeController) {     			
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				// Activate new node in menu and create necessary edit controllers
				// necessary if previous action was a delete node action				
				tabbedNodeConfig.setVisible(true);
				main.setPage(VELOCITY_ROOT + "/index.html");				
				CourseNode newNode = insertNodeController.getInsertedNode();		
				doInsert(ureq, newNode);
			}
			// in all cases:
			removeAsListenerAndDispose(insertNodeController);
			removeAsListenerAndDispose(cmc);
			insertNodeController = null;
			cmc = null;
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
	
	private void doDelete(ICourse course, String ident) {
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
		updateCourseStatusMessages(getLocale(), courseStatus);

		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_DELETED, getClass(),
				LoggingResourceable.wrap(activeNode));
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
		JSCommand resizeCommand = new JSCommand("try { OPOL.adjustHeight(); } catch(e) {if(console) console.log(e); }");
		getWindowControl().getWindowBackOffice().sendCommandTo(resizeCommand);
	}

	//fxdiff VCRP-9: drag and drop in menu tree
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
			if(selectedNode.getParent() == null) {
				//root node
				insertParent = selectedNode;
				position = 0;
			} else {
				insertParent = course.getEditorTreeModel().getCourseEditorNodeById(selectedNode.getParent().getIdent());
				position = 0;
				for(position=insertParent.getChildCount(); position-->0; ) {
					if(insertParent.getChildAt(position).getIdent().equals(selectedNode.getIdent())) {
						position++;
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
		OLATResourceable lockEntryOres = OresHelper.createOLATResourceableInstance(LockEntry.class, 0l);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, lockEntryOres);

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
			} else if (event instanceof LockRemovedEvent) {
				LockRemovedEvent lockEvent = (LockRemovedEvent)event;
				if(lockEntry != null && lockEntry.getLockEntry() != null && lockEntry.getLockEntry().equals(lockEvent.getLockEntry())) {
					this.dispose();
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
			CustomCSS localCustomCSS = CourseLayoutHelper.getCustomCSS(ureq.getUserSession(), course.getCourseEnvironment());
			if (localCustomCSS != null) {
				String fulluri = localCustomCSS.getCSSURL();			
				// path
				hc = new HtmlHeaderComponent("custom-css", null, "<link rel=\"StyleSheet\" href=\"" + fulluri
						+ "\" type=\"text/css\" media=\"screen\"/>");
				main.put("css-inset2", hc);
			}
		}
	}

}
