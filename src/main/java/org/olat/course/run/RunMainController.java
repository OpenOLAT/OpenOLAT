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

package org.olat.course.run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ConfigurationChangedListener;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.textmarker.GlossaryMarkupItemController;
import org.olat.core.gui.control.generic.title.TitledWrapperController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.DisposedCourseRestartController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentChangedEvent;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.config.CourseConfig;
import org.olat.course.editor.PublishEvent;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.glossary.CourseGlossaryFactory;
import org.olat.course.run.glossary.CourseGlossaryToolLinkController;
import org.olat.course.run.navigation.NavigationHandler;
import org.olat.course.run.navigation.NodeClickedRef;
import org.olat.course.run.userview.AssessmentModeTreeFilter;
import org.olat.course.run.userview.InvisibleTreeFilter;
import org.olat.course.run.userview.TreeFilter;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.run.userview.VisibleTreeFilter;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.modules.cp.TreeNodeEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class RunMainController extends MainLayoutBasicController implements GenericEventListener, Activateable2 {

	public static final String REBUILD = "rebuild";
	public static final String ORES_TYPE_COURSE_RUN = OresHelper.calculateTypeName(RunMainController.class, CourseModule.ORES_TYPE_COURSE);
	private final OLATResourceable courseRunOres; //course run ores for course run channel 

	private ICourse course;//o_clusterOK: this controller listen to course change events
	private RepositoryEntry courseRepositoryEntry;

	private Panel contentP;
	private MenuTree luTree;
	private VelocityContainer coursemain;
	private NavigationHandler navHandler;
	private UserCourseEnvironmentImpl uce;
	private TooledStackedPanel toolbarPanel;
	private LayoutMain3ColsController columnLayoutCtr;

	private Controller currentNodeController; // the currently open node config

	private boolean isInEditor = false;

	private CourseNode currentCourseNode;
	private TreeModel treeModel;
	private TreeFilter treeFilter;
	private boolean needsRebuildAfter = false;
	private boolean needsRebuildAfterPublish = false;
	private boolean needsRebuildAfterRunDone = false;
	private boolean assessmentChangedEventReceived = false;
	
	private String courseTitle;
	private Link nextLink, previousLink;
	private GlossaryMarkupItemController glossaryMarkerCtr;

	@Autowired
	private RepositoryService repositoryService;
	
	/**
	 * Constructor for the run main controller
	 * 
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @param course The current course
	 * @param initialViewIdentifier if null the default view will be started,
	 *          otherwise a controllerfactory type dependant view will be
	 *          activated (subscription subtype) number: node with nodenumber will
	 *          be activated "assessmentTool": assessment tool will be activated
	 * @param offerBookmark - whether to offer bookmarks or not
	 * @param showCourseConfigLink  Flag to enable/disable link to detail-page in tool menu. 
	 */
	public RunMainController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			ICourse course, RepositoryEntry re, RepositoryEntrySecurity reSecurity, AssessmentMode assessmentMode) {

		// Use repository package as fallback translator
		super(ureq, wControl, Util.createPackageTranslator(RepositoryEntry.class, ureq.getLocale()));

		this.course = course;
		this.toolbarPanel = toolbarPanel;
		addLoggingResourceable(LoggingResourceable.wrap(course));
		this.courseTitle = course.getCourseTitle();
		this.courseRepositoryEntry = re;
		this.courseRunOres = OresHelper.createOLATResourceableInstance(ORES_TYPE_COURSE_RUN, course.getResourceableId());
		
		Identity identity = ureq.getIdentity();
		// course and system logging
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_ENTERING, getClass());
		
		// log shows who entered which course, this can then be further used to jump
		// to the courselog
		getLogger().info(Tracing.M_AUDIT, "Entering course: [[[{}]]] {}", courseTitle, course.getResourceableId().toString());
		
		luTree = new MenuTree(null, "luTreeRun", this);
		luTree.setScrollTopOnClick(true);
		luTree.setExpandSelectedNode(false);
		luTree.setElementCssClass("o_course_menu");
		contentP = new Panel("building_block_content");

		// build up the running structure for this user
		// get all group memberships for this course
		uce = loadUserCourseEnvironment(ureq, reSecurity);

		// build score now
		uce.getScoreAccounting().evaluateAll();
		
		if(assessmentMode != null && assessmentMode.isRestrictAccessElements()) {
			Status assessmentStatus = assessmentMode.getStatus();
			if(assessmentStatus == Status.assessment) {
				treeFilter = new AssessmentModeTreeFilter(assessmentMode, uce.getCourseEnvironment().getRunStructure());
			} else if(assessmentStatus == Status.leadtime || assessmentStatus == Status.followup) {
				treeFilter = new InvisibleTreeFilter();
			} else {
				treeFilter = new VisibleTreeFilter();
			}
		} else {
			treeFilter = new VisibleTreeFilter();
		}
		navHandler = new NavigationHandler(uce, treeFilter, false);

		currentCourseNode = updateTreeAndContent(ureq, currentCourseNode, null);

		if (courseRepositoryEntry != null && courseRepositoryEntry.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
			wControl.setWarning(translate("course.closed"));
		}

		// add text marker wrapper controller to implement course glossary
		// textMarkerCtr must be created before the toolC!
		CourseConfig cc = uce.getCourseEnvironment().getCourseConfig();
		glossaryMarkerCtr = CourseGlossaryFactory.createGlossaryMarkupWrapper(ureq, wControl, contentP, cc);
		
		MenuTree layoutTree = luTree;
		if(!cc.isMenuEnabled() && !uce.isAdmin()) {
			layoutTree = null;
		}
		
		if (glossaryMarkerCtr != null) {
			listenTo(glossaryMarkerCtr);
			// enable / disable glossary highlighting according to user prefs
			Preferences prefs = ureq.getUserSession().getGuiPreferences();
			Boolean state = (Boolean) prefs.get(CourseGlossaryToolLinkController.class, CourseGlossaryFactory.createGuiPrefsKey(course));

			//Glossary always on for guests. OLAT-4241
			if(ureq.getUserSession().getRoles().isGuestOnly()){
				state = Boolean.TRUE;
			}
			
			if (state == null) {
				glossaryMarkerCtr.setTextMarkingEnabled(false);
			} else {
				glossaryMarkerCtr.setTextMarkingEnabled(state.booleanValue());
			}
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), layoutTree, glossaryMarkerCtr.getInitialComponent(), "course" + course.getResourceableId());				
		} else {
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), layoutTree, contentP, "courseRun" + course.getResourceableId());							
		}
		listenTo(columnLayoutCtr);

		// activate the custom course css if any
		setCustomCSS(CourseFactory.getCustomCourseCss(ureq.getUserSession(), uce.getCourseEnvironment()));

		coursemain = createVelocityContainer("index");
		coursemain.setDomReplaceable(false);
		// see function gotonode in functions.js to see why we need the repositoryentry-key here:
		// it is to correctly apply external links using course-internal links via javascript
		coursemain.contextPut("courserepokey", courseRepositoryEntry.getKey());
		coursemain.put("coursemain", columnLayoutCtr.getInitialComponent());
		// on initial call we have to set the data-nodeid manually. later it
		// will be updated by updateCourseDataAttributes() automatically, but
		// only when course visible to users (menu tree not null)
		if (treeModel != null) {
			String initNodeId = currentCourseNode != null ? currentCourseNode.getIdent() : null;
			if (initNodeId == null) {
				initNodeId = treeModel.getRootNode().getIdent();
			}
			coursemain.contextPut("initNodeId", initNodeId);
		}
		putInitialPanel(coursemain);

		// disposed message controller must be created beforehand
		Controller courseCloser = new DisposedCourseRestartController(ureq, getWindowControl(), courseRepositoryEntry);
		Controller disposedRestartController = new LayoutMain3ColsController(ureq, wControl, courseCloser);
		setDisposedMsgController(disposedRestartController);

		// add as listener to course so we are being notified about course events:
		// - publish changes
		// - assessment events
		// - listen for CourseConfig events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, course);
		// - group modification events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, courseRepositoryEntry);
	}
	
	protected void setTextMarkingEnabled(boolean enabled) {
		if (glossaryMarkerCtr != null) {
			glossaryMarkerCtr.setTextMarkingEnabled(enabled);
		}
	}
	
	protected void initToolbar() {
		if(toolbarPanel != null) {
			// new toolbox 'general'
			previousLink = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
			previousLink.setTitle(translate("command.previous"));
			toolbarPanel.addTool(previousLink, Align.rightEdge, false, "o_tool_previous");
			nextLink = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
			nextLink.setTitle(translate("command.next"));
			toolbarPanel.addTool(nextLink, Align.rightEdge, false, "o_tool_next");
			updateNextPrevious();
		}
	}
	
	private UserCourseEnvironmentImpl loadUserCourseEnvironment(UserRequest ureq, RepositoryEntrySecurity reSecurity) {
		return UserCourseEnvironmentImpl.load(ureq, course, reSecurity, getWindowControl());
	}
	
	/**
	 * Initialize the users group memberships for groups used within this course
	 * 
	 * @param identity
	 */
	protected void reloadGroupMemberships(RepositoryEntrySecurity reSecurity) {
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		List<BusinessGroup> coachedGroups;
		if(reSecurity.isGroupCoach()) {
			coachedGroups = cgm.getOwnedBusinessGroups(getIdentity());
		} else {
			coachedGroups = Collections.emptyList();
		}
		List<BusinessGroup> participatedGroups;
		
		if(reSecurity.isGroupParticipant()) {
			participatedGroups = cgm.getParticipatingBusinessGroups(getIdentity());
		} else {
			participatedGroups = Collections.emptyList();
		}
		List<BusinessGroup> waitingLists;
		if(reSecurity.isGroupWaiting()) {
			waitingLists = cgm.getWaitingListGroups(getIdentity());
		} else {
			waitingLists = Collections.emptyList();
		}
		uce.setCourseReadOnly(reSecurity.isReadOnly());
		uce.setGroupMemberships(coachedGroups, participatedGroups, waitingLists);
		needsRebuildAfterRunDone = true;
	}
	
	private CourseNode updateAfterChanges(CourseNode courseNode) {
		if(currentCourseNode == null) return null;
		
		CourseNode newCurrentCourseNode;
		NodeClickedRef nclr = navHandler.reloadTreeAfterChanges(courseNode);
		if(nclr == null) {
			doDisposeAfterEvent();
			newCurrentCourseNode = null;
		} else {
			treeModel = nclr.getTreeModel();
			luTree.setTreeModel(treeModel);
			String selNodeId = nclr.getSelectedNodeId();
			luTree.setSelectedNodeId(selNodeId);
			luTree.setOpenNodeIds(nclr.getOpenNodeIds());
			newCurrentCourseNode = nclr.getCalledCourseNode();
		}
		return newCurrentCourseNode;
	}
	
	protected void updateNextPrevious() {
		if(nextLink == null || previousLink == null || luTree == null) {
			return;
		}
		
		boolean hasPrevious;
		boolean hasNext;
		if(luTree.getSelectedNode() == null) {
			hasPrevious = true;
			hasNext = true;
		} else {
			List<TreeNode> flatTree = new ArrayList<>();
			TreeHelper.makeTreeFlat(luTree.getTreeModel().getRootNode(), flatTree);
			int index = flatTree.indexOf(luTree.getSelectedNode());
			hasPrevious = index > 0;
			hasNext = index  >= 0 && index+1 < flatTree.size();
		}
		previousLink.setEnabled(hasPrevious);
		nextLink.setEnabled(hasNext);
	}
	
	protected CourseNode updateCurrentCourseNode(UserRequest ureq) {
		return updateTreeAndContent(ureq, getCurrentCourseNode(), "", null, null);
	}

	/**
	 * side-effecty to content and luTree
	 * 
	 * @param ureq
	 * @param calledCourseNode the node to jump to, if null = jump to root node
	 * @param nodecmd An optional command used to activate the run view or NULL if not available
	 * @return true if the node jumped to is visible
	 */
	private CourseNode updateTreeAndContent(UserRequest ureq, CourseNode calledCourseNode, String nodecmd) {
		return updateTreeAndContent(ureq, calledCourseNode, nodecmd, null, null);
	}
	
	private CourseNode updateTreeAndContent(UserRequest ureq, CourseNode calledCourseNode, String nodecmd, List<ContextEntry> entries, StateEntry state) {
		// build menu (treemodel)
		// dispose old node controller before creating the NodeClickedRef which creates 
		// the new node controller. It is important that the old node controller is 
		// disposed before the new one to not get conflicts with cacheable mappers that
		// might be used in both controllers with the same ID (e.g. the course folder)
		if (currentNodeController != null && !currentNodeController.isDisposed() && !navHandler.isListening(currentNodeController)) {
			currentNodeController.dispose();
		}
		NodeClickedRef nclr = navHandler.evaluateJumpToCourseNode(ureq, getWindowControl(), calledCourseNode, this, nodecmd);
		if (!nclr.isVisible()) {
			// if not root -> fallback to root. e.g. when a direct node jump fails
			if (calledCourseNode != null) {
				nclr = navHandler.evaluateJumpToCourseNode(ureq, getWindowControl(), null, this, null);
			}
			if (!nclr.isVisible()) {
				MessageController msgController = MessageUIFactory.createInfoMessage(ureq, getWindowControl(),	translate("course.noaccess.title"), translate("course.noaccess.text"));
				contentP.setContent(msgController.getInitialComponent());					
				luTree.setTreeModel(new GenericTreeModel());
				return null;
			}
		}

		treeModel = nclr.getTreeModel();
		luTree.setTreeModel(treeModel);
		String selNodeId = nclr.getSelectedNodeId();
		luTree.setSelectedNodeId(selNodeId);
		luTree.setOpenNodeIds(nclr.getOpenNodeIds());

		// get new run controller.
		currentNodeController = nclr.getRunController();
		addToHistory(ureq, currentNodeController);
		
		if (currentNodeController instanceof TitledWrapperController) {
			Controller contentcontroller = ((TitledWrapperController)currentNodeController).getContentController();
			addToHistory(ureq, contentcontroller);
			if(contentcontroller instanceof Activateable2) {
				((Activateable2)contentcontroller).activate(ureq, entries, state);
			}
		} else if(currentNodeController instanceof Activateable2) {
			((Activateable2)currentNodeController).activate(ureq, entries, state);
		}
		if(currentNodeController != null) {
			contentP.setContent(currentNodeController.getInitialComponent());
		} else {
			MessageController msgCtrl = MessageUIFactory
					.createWarnMessage(ureq, getWindowControl(), null, translate("msg.nodenotavailableanymore"));
			listenTo(msgCtrl);
			contentP.setContent(msgCtrl.getInitialComponent());
		}
		
		updateNextPrevious();
		updateCourseDataAttributes(nclr.getCalledCourseNode());
		updateLastUsage(nclr.getCalledCourseNode());
		return nclr.getCalledCourseNode();
	}
	
	private void updateLastUsage(CourseNode calledCourseNode) {
		if(calledCourseNode != null && calledCourseNode.needsReferenceToARepositoryEntry()) {
			RepositoryEntry referencedRe = calledCourseNode.getReferencedRepositoryEntry();
			if(referencedRe != null) {
				repositoryService.setLastUsageNowFor(referencedRe);
			}
		}
	}
	
	private void updateCourseDataAttributes(CourseNode calledCourseNode) {
		StringBuilder sb = new StringBuilder();
		sb.append("try {var oocourse = jQuery('.o_course_run');");
		if (calledCourseNode == null) {
			sb.append("oocourse.removeAttr('data-nodeid');");						
		} else {
			sb.append("oocourse.attr('data-nodeid','");
			sb.append(Formatter.escapeDoubleQuotes(calledCourseNode.getIdent()));
			sb.append("');");			
		}
		sb.append("oocourse=null;}catch(e){}");
		JSCommand jsc = new JSCommand(sb.toString());
		WindowControl wControl = getWindowControl();
		if (wControl != null && wControl.getWindowBackOffice() != null) {
			wControl.getWindowBackOffice().sendCommandTo(jsc);			
		}
		// update window title, but only if a tree node is activated. Initial
		// course title already set by BaseFullWebappController on tab activate
		if (calledCourseNode != null) {
			String newTitle = courseTitle + " - " + calledCourseNode.getShortTitle();
			getWindowControl().getWindowBackOffice().getWindow().setTitle(getTranslator(), newTitle);						
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(needsRebuildAfter) {
			currentCourseNode = updateAfterChanges(currentCourseNode);
			needsRebuildAfter = false;
		}
		
		// Links in editTools dropdown
		if(nextLink == source) {
			doNext(ureq);
		} else if(previousLink == source) {
			doPrevious(ureq);
		} else if (source == luTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED) || event.getCommand().equals(MenuTree.COMMAND_TREENODE_EXPANDED)) {
				TreeEvent tev = (TreeEvent) event;
				doNodeClick(ureq, tev);
			}
		} else if (source == coursemain) {
			if ("activateCourseNode".equals(event.getCommand())) {
				// Events from the JS function o_activateCourseNode() - activate the given node id
				String nodeid = ureq.getParameter("nodeid");
				if (nodeid != null) {
					CourseNode identNode = course.getRunStructure().getNode(nodeid);
					CourseNode node = updateTreeAndContent(ureq, identNode, null);
					if (node != null) {
						currentCourseNode = node;
					} else {
						getWindowControl().setWarning(translate("msg.nodenotavailableanymore"));
					}
				}
			}
		}
	}
	
	protected void toolCtrDone(UserRequest ureq, RepositoryEntrySecurity reSecurity) {
		if (isInEditor) {
			isInEditor = false; // for clarity
			if (needsRebuildAfterPublish) {
				needsRebuildAfterPublish = false;
				
			  // rebuild up the running structure for this user, after publish
				course = CourseFactory.loadCourse(course.getResourceableId());
				uce = loadUserCourseEnvironment(ureq, reSecurity);
				// build score now
				uce.getScoreAccounting().evaluateAll();
				navHandler = new NavigationHandler(uce, treeFilter, false);
				
				// rebuild and jump to root node
				updateTreeAndContent(ureq, null, null);
			}
		}
	}

	protected boolean isInEditor() {
		return isInEditor;
	}

	protected void setInEditor(boolean isInEditor) {
		this.isInEditor = isInEditor;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(needsRebuildAfter) {
			currentCourseNode = updateAfterChanges(currentCourseNode);
			needsRebuildAfter = false;
		}
		
		// event from the current tool (editor, groupmanagement, archiver)	
		
		if (source == currentNodeController) {
			if (event instanceof OlatCmdEvent) {
				OlatCmdEvent oe = (OlatCmdEvent) event;
				String cmd = oe.getCommand();
				if (OlatCmdEvent.GOTONODE_CMD.equals(cmd)) {
					String subcmd = oe.getSubcommand(); // "69680861018558/node-specific-data";
					CourseNode identNode;
					String nodecmd = null;
					int slPos = subcmd.indexOf('/');
					if (slPos != -1) {
						nodecmd = subcmd.substring(slPos + 1);
						identNode = course.getRunStructure().getNode(subcmd.substring(0, slPos));
					} else {
						identNode = course.getRunStructure().getNode(subcmd);
					}
					if(identNode == null) {
						showWarning("msg.nodenotavailableanymore");
					} else {
						addLoggingResourceable(LoggingResourceable.wrap(identNode));
						currentCourseNode = identNode;
						updateTreeAndContent(ureq, identNode, nodecmd);
						oe.accept();
					}
				}
			} else if (event == Event.DONE_EVENT) {
				// the controller is done.
				// we have a chance here to test if we need to refresh the evalution.
				// this is the case when a test was submitted and scoring has changed ->
				// preconditions may have changed
				if (needsRebuildAfterRunDone) {
					needsRebuildAfterRunDone = false;
					updateTreeAndContent(ureq, currentCourseNode, null);
				}
			} else if (REBUILD.equals(event.getCommand())) {
				needsRebuildAfterRunDone = false;
				updateTreeAndContent(ureq, currentCourseNode, null);
			} else if (event instanceof TreeNodeEvent) {
				TreeNodeEvent tne = (TreeNodeEvent) event;
				TreeNode newCpTreeNode = tne.getChosenTreeNode();
				luTree.setSelectedNodeId(newCpTreeNode.getIdent());
			} else if (event == Event.CHANGED_EVENT) {
				updateTreeAndContent(ureq, currentCourseNode, null);
			} else if (event instanceof BusinessGroupModifiedEvent) {
				fireEvent(ureq, event);
				updateTreeAndContent(ureq, currentCourseNode, null);
			}
		}
	}
	
	private void doNext(UserRequest ureq) {
		List<TreeNode> flatList = new ArrayList<>();
		TreeNode currentNode = luTree.getSelectedNode();
		TreeHelper.makeTreeFlat(luTree.getTreeModel().getRootNode(), flatList);
		int index = flatList.indexOf(currentNode);
		if(index >= 0 && index+1 <flatList.size()) {
			TreeNode nextNode = flatList.get(index + 1);
			TreeEvent tev = new TreeEvent(MenuTree.COMMAND_TREENODE_CLICKED, nextNode.getIdent());
			doNodeClick(ureq, tev);
		}
	}
	
	private void doPrevious(UserRequest ureq) {
		List<TreeNode> flatList = new ArrayList<>();
		TreeNode currentNode = luTree.getSelectedNode();
		TreeHelper.makeTreeFlat(luTree.getTreeModel().getRootNode(), flatList);
		int index = flatList.indexOf(currentNode);
		if(index-1 >= 0 && index-1 < flatList.size()) {
			TreeNode previousNode = flatList.get(index - 1);
			TreeEvent tev = new TreeEvent(MenuTree.COMMAND_TREENODE_CLICKED, previousNode.getIdent());
			doNodeClick(ureq, tev);
		}
	}
	
	private void doNodeClick(UserRequest ureq, TreeEvent tev) {
		if(assessmentChangedEventReceived) {
			uce.getScoreAccounting().evaluateAll();
			assessmentChangedEventReceived = false;
		}
		
		// goto node:
		// after a click in the tree, evaluate the model anew, and set the
		// selection of the tree again
		NodeClickedRef nclr = navHandler.evaluateJumpToTreeNode(ureq, getWindowControl(), treeModel, tev, this, null, currentNodeController);
		if (!nclr.isVisible()) {
			getWindowControl().setWarning(translate("msg.nodenotavailableanymore"));
			// go to root since the current node is no more visible 
			updateTreeAndContent(ureq, null, null);
			updateNextPrevious();
			updateCourseDataAttributes(nclr.getCalledCourseNode());
			return;
		}
		// a click to a subtree's node
		if (nclr.isHandledBySubTreeModelListener() || nclr.getSelectedNodeId() == null) {
			if(nclr.getRunController() != null) {
				//there is an update to the currentNodeController, apply it
				if (currentNodeController != null && !currentNodeController.isDisposed() && !navHandler.isListening(currentNodeController)) {
					currentNodeController.dispose();
				}
				currentNodeController = nclr.getRunController();
				Component nodeComp = currentNodeController.getInitialComponent();
				contentP.setContent(nodeComp);
				updateLastUsage(nclr.getCalledCourseNode());
			}
			
			if(nclr.getSelectedNodeId() != null && nclr.getOpenNodeIds() != null) {
				luTree.setSelectedNodeId(nclr.getSelectedNodeId());
				luTree.setOpenNodeIds(nclr.getOpenNodeIds());
			}
			updateNextPrevious();
			updateCourseDataAttributes(nclr.getCalledCourseNode());
			return;
		}

		// set the new treemodel
		treeModel = nclr.getTreeModel();
		luTree.setTreeModel(treeModel);

		// set the new tree selection
		String nodeId = nclr.getSelectedNodeId();
		luTree.setSelectedNodeId(nodeId);
		luTree.setOpenNodeIds(nclr.getOpenNodeIds());
		currentCourseNode = nclr.getCalledCourseNode();

		// get new run controller. Dispose only if not already disposed in navHandler.evaluateJumpToTreeNode()
		if (currentNodeController != null && !currentNodeController.isDisposed() && !navHandler.isListening(currentNodeController)) {
			currentNodeController.dispose();
		}
		currentNodeController = nclr.getRunController();
		updateLastUsage(nclr.getCalledCourseNode());
		Component nodeComp = currentNodeController.getInitialComponent();
		contentP.setContent(nodeComp);
		addToHistory(ureq, currentNodeController);
		
		// set glossary wrapper dirty after menu click to make it reload the glossary
		// stuff properly when in AJAX mode
		if (glossaryMarkerCtr != null && glossaryMarkerCtr.isTextMarkingEnabled()) {
			glossaryMarkerCtr.getInitialComponent().setDirty(true);
		}
		
		updateNextPrevious();
		updateCourseDataAttributes(nclr.getCalledCourseNode());
	}


	/**
	 * implementation of listener which listens to publish events
	 * 
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(Event event) {	
		if (event instanceof PublishEvent) {
			PublishEvent pe = (PublishEvent)event;
			if(course.getResourceableId().equals(pe.getPublishedCourseResId())) {
				processPublishEvent(pe);
			}
		} else if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent ojde = (OLATResourceableJustBeforeDeletedEvent) event;
			// make sure it is our course (actually not needed till now, since we
			// registered only to one event, but good style.
			if (ojde.targetEquals(course, true)) {
				doDisposeAfterEvent();
			}
		} else if (event instanceof AssessmentChangedEvent) {
			AssessmentChangedEvent ace = (AssessmentChangedEvent) event;
			Identity identity = uce.getIdentityEnvironment().getIdentity();
			// reevaluate the changed node if the event changed the current user
			if (ace.getIdentityKey().equals(identity.getKey())) {
				String assessmentChangeType = ace.getCommand();
				// do not re-evaluate things if only comment has been changed
				if (assessmentChangeType.equals(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED)
						|| assessmentChangeType.equals(AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED)) {
					//LD: do not recalculate the score now, but at the next click, since the event comes before DB commit
					//uce.getScoreAccounting().evaluateAll(); 
					assessmentChangedEventReceived = true;										
				}
				// raise a flag to indicate refresh
				needsRebuildAfterRunDone = true;
			}
		}
	}
	
	private void processPublishEvent(PublishEvent pe) {
		if (pe.getState() == PublishEvent.PRE_PUBLISH) {
			// so far not interested in PRE PUBLISH event, but one could add user
			// and the currently active BB information. This in turn could be used 
			// by the publish event issuer to decide whether or not to publish...
		} else if (pe.getState() == PublishEvent.PUBLISH) {
			// disable this controller and issue a information
			if (isInEditor) {
				needsRebuildAfterPublish = true;
				// author went in editor and published the course -> raise a flag to
				// later prepare the new
				// course to present him/her a nice view when
				// he/she closes the editor to return to the run main (this controller)
			} else if(getIdentity().getKey().equals(pe.getAuthorKey())) {
				//do nothing
			} else {
				if(currentCourseNode == null) {
					needsRebuildAfter = true;
				} else {
					try {
						String currentNodeIdent = currentCourseNode.getIdent();
						Set<String> deletedNodeIds = pe.getDeletedCourseNodeIds();
						Set<String> modifiedNodeIds = pe.getModifiedCourseNodeIds();
						
						if(deletedNodeIds != null && deletedNodeIds.contains(currentNodeIdent)) {
							doDisposeAfterEvent();
						} else if(modifiedNodeIds != null && modifiedNodeIds.contains(currentNodeIdent)) {
							doDisposeAfterEvent();
							//needsRebuildAfter = true;
						} else {
							needsRebuildAfter = true;
						}
					} catch (Exception e) {
						logError("", e);
						//beyond update, be paranoid
						doDisposeAfterEvent();
					}
				}
			}
		}
	}
	
	protected void doDisposeAfterEvent() {
		if(currentNodeController instanceof ConfigurationChangedListener) {
			//give to opportunity to close popups ...
			((ConfigurationChangedListener)currentNodeController).configurationChanged();
		}
		dispose();
	}
	
	UserCourseEnvironmentImpl getUce() {
		return uce;
	}
	
	CourseNode getCurrentCourseNode() {
		return currentCourseNode;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// remove as listener from this course events:
		// - group modification events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, courseRepositoryEntry);
		// - publish changes
		// - assessment events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, course);
		//remove as listener from course run eventAgency
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, courseRunOres);
				
		// currentNodeController must be disposed manually does not work with
		// general BasicController methods
		if (currentNodeController != null && !currentNodeController.isDisposed()) {
			currentNodeController.dispose();
		}
		currentNodeController = null;
		navHandler.dispose();

		// log to Statistical and User log
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_LEAVING, getClass());
		
		// log the fact that the user is leaving the course in the log file
		logAudit("Leaving course: [[["+courseTitle+"]]]", course.getResourceableId().toString());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(currentNodeController != null) {
				addToHistory(ureq, currentNodeController);
			} else {
				addToHistory(ureq, this);
			}
			return;
		}
		
		ContextEntry firstEntry = entries.get(0);
		String type = firstEntry.getOLATResourceable().getResourceableTypeName();
		if("CourseNode".equalsIgnoreCase(type) || "Part".equalsIgnoreCase(type)) {
			CourseNode cn = course.getRunStructure().getNode(firstEntry.getOLATResourceable().getResourceableId().toString());
			if(currentCourseNode == null || !currentCourseNode.equals(cn)) {
				getWindowControl().makeFlat();
				// add logging information for case course gets started via jump-in
				// link/search
				addLoggingResourceable(LoggingResourceable.wrap(course));
				if (cn != null) {
					addLoggingResourceable(LoggingResourceable.wrap(cn));
				}
				// consume our entry
				if(entries.size() > 1) {
					entries = entries.subList(1, entries.size());
				}
				currentCourseNode = updateTreeAndContent(ureq, cn, null, entries, firstEntry.getTransientState());
			} else {
				// consume our entry
				if(entries.size() > 1) {
					entries = entries.subList(1, entries.size());
				}
				// the node to be activated is the one that is already on the screen
				if (currentNodeController instanceof Activateable2) {
					Activateable2 activateable = (Activateable2) currentNodeController;
					activateable.activate(ureq, entries, state);
				}
			}
		}
	}

	public void disableToolController(boolean disable) {
		toolbarPanel.setToolbarEnabled(!disable);
	}
	public void disableCourseClose(boolean disable) {
		toolbarPanel.setShowCloseLink(true, !disable);
	}

	@Override
	public void setDisposedMsgController(Controller disposeMsgController) {
		super.setDisposedMsgController(disposeMsgController);
	}
}