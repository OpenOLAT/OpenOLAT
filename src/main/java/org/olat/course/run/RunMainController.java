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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.ListPanel;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
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
import org.olat.core.util.StringHelper;
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
import org.olat.course.assessment.AssessmentEvents;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.config.CourseConfig;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.course.disclaimer.ui.CourseDisclaimerConsentController;
import org.olat.course.editor.PublishEvent;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.glossary.CourseGlossaryFactory;
import org.olat.course.run.glossary.CourseGlossaryToolLinkController;
import org.olat.course.run.navigation.NavigationHandler;
import org.olat.course.run.navigation.NodeClickedRef;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.tools.CourseTool;
import org.olat.course.run.tools.OpenCourseToolEvent;
import org.olat.course.run.userview.AssessmentModeTreeFilter;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.InvisibleTreeFilter;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.course.style.ui.HeaderContentController;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.modules.cp.TreeNodeEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class RunMainController extends MainLayoutBasicController implements GenericEventListener, Activateable2 {

	private static final Logger log = Tracing.createLoggerFor(RunMainController.class);

	public static final Event RELOAD_COURSE_NODE = new Event("reload-course-node");
	public static final Event COURSE_DISCLAIMER_ACCEPTED = new Event("course-disclaimer-accepted");
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
	private CourseDisclaimerConsentController disclaimerController;
	
	private CoursePaginationController topPaginationCtrl, bottomPaginationCtrl;
	private ProgressBar courseProgress;
	private Controller currentNodeController;

	private boolean isInEditor = false;
	private final boolean leaningPath;

	private CourseNode currentCourseNode;
	private TreeModel treeModel;
	private VisibilityFilter visibilityFilter;
	private boolean needsRebuildAfter = false;
	private boolean needsRebuildAfterPublish = false;
	private boolean needsRebuildAfterRunDone = false;
	private boolean disclaimerAccepted = true;
	
	private String courseTitle;
	private Link nextLink, previousLink;
	private GlossaryMarkupItemController glossaryMarkerCtr;
	
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private CourseModule courseModule;
	@Autowired 
	private CourseDisclaimerManager disclaimerManager;

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
		String treeCssClass = nodeAccessService.getCourseTreeCssClass(course.getCourseConfig());
		luTree.setElementCssClass("o_course_menu " + treeCssClass);
		contentP = new Panel("building_block_content");
		
		// build up the running structure for this user
		// get all group memberships for this course
		uce = loadUserCourseEnvironment(ureq, reSecurity);
		
		// build score now
		uce.getScoreAccounting().evaluateAll(true);
		
		// Legacy pagination in header
		topPaginationCtrl = nodeAccessService.getCoursePaginationController(ureq, getWindowControl(), NodeAccessType.of(course));
		if (topPaginationCtrl != null) {
			listenTo(topPaginationCtrl);
			// Regular pagination at bottom of course content
			bottomPaginationCtrl = nodeAccessService.getCoursePaginationController(ureq, getWindowControl(), NodeAccessType.of(course));
			if (bottomPaginationCtrl != null) {
				bottomPaginationCtrl.enableLargeStyleRendering();
				listenTo(bottomPaginationCtrl);
			}
		}
		
		if(assessmentMode != null && assessmentMode.isRestrictAccessElements()) {
			Status assessmentStatus = assessmentMode.getStatus();
			if(assessmentStatus == Status.assessment) {
				visibilityFilter = new AssessmentModeTreeFilter(assessmentMode, uce.getCourseEnvironment().getRunStructure());
			} else if(assessmentStatus == Status.leadtime || assessmentStatus == Status.followup) {
				visibilityFilter = new InvisibleTreeFilter();
			}
		}
		navHandler = new NavigationHandler(uce, visibilityFilter, false);

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
		
		Component contentComp = contentP;
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
			contentComp = glossaryMarkerCtr.getInitialComponent();
		}
		// Add paging navigation at bottom
		if (bottomPaginationCtrl != null) {
			ListPanel pagedContentPanel = new ListPanel("pagedCourseContent", "o_main_center_with_paging");
			pagedContentPanel.addContent(contentComp);
			pagedContentPanel.addContent(bottomPaginationCtrl.getInitialComponent());		
			contentComp = pagedContentPanel;			
		}
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), layoutTree,
				contentComp, "courseRun" + course.getResourceableId());
		listenTo(columnLayoutCtr);

		// activate the custom course css if any
		setCustomCSS(CourseFactory.getCustomCourseCss(ureq.getUserSession(), uce.getCourseEnvironment()));

		coursemain = createVelocityContainer("index");
		coursemain.setDomReplaceable(false);
		// see function gotonode in functions.js to see why we need the repositoryentry-key here:
		// it is to correctly apply external links using course-internal links via javascript
		coursemain.contextPut("courserepokey", courseRepositoryEntry.getKey());
		
		if (courseRepositoryEntry.getEducationalType() != null) {
			toolbarPanel.addCssClass(courseRepositoryEntry.getEducationalType().getCssClass());
		}
		
		if (courseModule.isInfoDetailsEnabled()) {
			String oInfoCourse = null;
			try {
				InfoCourse infoCourse = InfoCourse.of(courseRepositoryEntry);
				if (infoCourse != null) {
					oInfoCourse = objectMapper.writeValueAsString(infoCourse);
				}
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
			coursemain.contextPut("oInfoCourse", oInfoCourse);
		}
		
		leaningPath = NodeAccessType.of(course).getType().equals(LearningPathNodeAccessProvider.TYPE);
		coursemain.contextPut("learningPath", Boolean.valueOf(leaningPath));
		
		// if a disclaimer is enabled, show it first
		disclaimerAccepted = !courseModule.isDisclaimerEnabled()
				|| !course.getCourseEnvironment().getCourseConfig().isDisclaimerEnabled() 
				|| disclaimerManager.isAccessGranted(courseRepositoryEntry, getIdentity(), ureq.getUserSession().getRoles());
		if (disclaimerAccepted) {
			coursemain.put("coursemain", columnLayoutCtr.getInitialComponent());
		} else {
			disclaimerController = new CourseDisclaimerConsentController(ureq, getWindowControl(), courseRepositoryEntry);
			listenTo(disclaimerController);
			coursemain.put("coursemain", disclaimerController.getInitialComponent());
		}
		
		// on initial call we have to set the data-nodeid manually. later it
		// will be updated by updateCourseDataAttributes() automatically, but
		// only when course visible to users (menu tree not null)
		if (treeModel != null) {
			String initNodeId = currentCourseNode != null ? currentCourseNode.getIdent() : null;
			if (initNodeId == null) {
				initNodeId = treeModel.getRootNode().getIdent();
			}
			coursemain.contextPut("initNodeId", initNodeId);
			
			String oInfoCourseNode = null;
			try {
				CourseNode courseNode = currentCourseNode != null ? currentCourseNode : (CourseNode)treeModel.getRootNode();
				InfoCourseNode infoCourseNode = InfoCourseNode.of(courseNode);
				if (infoCourseNode != null) {
					oInfoCourseNode = objectMapper.writeValueAsString(infoCourseNode);
				}
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
			coursemain.contextPut("oInfoCourseNode", oInfoCourseNode);
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
	
	public boolean isDisclaimerAccepted() {
		return disclaimerAccepted;
	}

	protected void setTextMarkingEnabled(boolean enabled) {
		if (glossaryMarkerCtr != null) {
			glossaryMarkerCtr.setTextMarkingEnabled(enabled);
		}
	}
	
	protected void initToolbarAndProgressbar() {
		// Progress bar only for learning path courses
		if (topPaginationCtrl != null) {
			toolbarPanel.setNavigationComponent(topPaginationCtrl.getInitialComponent(), this);			
			courseProgress = new ProgressBar("courseProgress");	
			courseProgress.setWidth(100);
			courseProgress.setMax(100);
			courseProgress.setWidthInPercent(true);
			courseProgress.setLabelAlignment(LabelAlignment.none);
			if (course.getCourseConfig().isToolbarEnabled() && toolbarPanel != null) {
				// Normally progress is showed in the toolbar. 
				courseProgress.setRenderStyle(RenderStyle.radial);
				courseProgress.setPercentagesEnabled(true);		
				toolbarPanel.addTool(courseProgress, Align.right);
			} else {
				// If toolbar is not visible, show the progress below the breadcrumb. If
				// breadcrub is also disabled, showprogress at top of page.
				courseProgress.setRenderStyle(RenderStyle.horizontal);
				courseProgress.setRenderSize(RenderSize.small);
				courseProgress.setPercentagesEnabled(false);		
				coursemain.put("courseProgress", courseProgress);			
			}
			updateProgressUI();
		}
		// Next-next navigation for old courses
		if(toolbarPanel != null && ConditionNodeAccessProvider.TYPE.equals(course.getCourseConfig().getNodeAccessType().getType())) {
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
		uce.setGroupMemberships(coachedGroups, participatedGroups, waitingLists);
		needsRebuildAfterRunDone = true;
	}
	
	/**
	 * 
	 * @param courseNode
	 * @param selectedNodeId my be the nodeId of a node of the subtree
	 * @return
	 */
	private CourseNode updateAfterChanges(CourseNode courseNode, String selectedNodeId) {
		if(currentCourseNode == null) return null;
		
		if(!uce.getCourseEnvironment().isPreview()) {
			course = uce.getCourseEnvironment().updateCourse();
		}
		
		CourseNode newCurrentCourseNode;
		NodeClickedRef nclr = navHandler.reloadTreeAfterChanges(courseNode, selectedNodeId);
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
		if (luTree == null) {
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
			hasPrevious = getPreviousNonDelegatingNode(flatTree, luTree.getSelectedNode()) != null;
			int index = flatTree.indexOf(luTree.getSelectedNode());
			hasNext = index  >= 0 && index+1 < flatTree.size();
		}
		
		if (previousLink != null) {
			previousLink.setEnabled(hasPrevious);
		}
		if (nextLink != null) {
			nextLink.setEnabled(hasNext);
		}
		if (topPaginationCtrl != null) {
			topPaginationCtrl.updateNextPreviousUI(hasPrevious, hasNext);
		}
		if (bottomPaginationCtrl != null) {
			bottomPaginationCtrl.updateNextPreviousUI(hasPrevious, hasNext);
		}
	}
	
	protected CourseNode updateCurrentCourseNode(UserRequest ureq) {
		String treeCssClass = nodeAccessService.getCourseTreeCssClass(course.getCourseConfig());
		luTree.setElementCssClass("o_course_menu " + treeCssClass);
		return updateTreeAndContent(ureq, getCurrentCourseNode(), "", null, null);
	}
	
	/**
	 * 
	 * @param ureq The user request
	 * @param nodeId The identifier of the course element
	 * @return The current course node
	 */
	protected CourseNode updateTreeAndContent(UserRequest ureq, String nodeId) {
		CourseNode identNode = course.getRunStructure().getNode(nodeId);
		CourseNode node = updateTreeAndContent(ureq, identNode, null);
		if (node != null) {
			currentCourseNode = node;
		} else {
			getWindowControl().setWarning(translate("msg.nodenotavailableanymore"));
		}
		return currentCourseNode;
	}

	/**
	 * side-effect to content and luTree
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
		} else if (currentNodeController instanceof HeaderContentController) {
			Controller contentcontroller = ((HeaderContentController)currentNodeController).getContentController();
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
		updateAssessmentConfirmUI(nclr.getCalledCourseNode());
		updateProgressUI();
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
		
		if (courseModule.isInfoDetailsEnabled()) {
			String oInfoCourseNode = null;
			try {
				InfoCourseNode infoCourseNode = InfoCourseNode.of(calledCourseNode);
				if (infoCourseNode != null) {
					oInfoCourseNode = objectMapper.writeValueAsString(infoCourseNode);
				}
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
			sb.append("try {");
			if (StringHelper.containsNonWhitespace(oInfoCourseNode)) {
				sb.append("o_info.course_node=").append(oInfoCourseNode).append(";");
			} else {
				sb.append("delete o_info.course_node;");
			}
			sb.append("}catch(e){}");
		}
		
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
	
	private void updateAssessmentConfirmUI(CourseNode calledCourseNode) {
		if (topPaginationCtrl != null) {
			boolean confirmVisible = false;
			boolean showDone = false;
			if (calledCourseNode != null) {
				TreeNode treeNode = treeModel.getNodeById(calledCourseNode.getIdent());
				if (treeNode != null) {
					boolean confirmationEnabled = nodeAccessService.isAssessmentConfirmationEnabled(calledCourseNode, getUce());
					AssessmentEvaluation assessmentEvaluation = getUce().getScoreAccounting().evalCourseNode(calledCourseNode);
					confirmVisible = confirmationEnabled && !uce.isCourseReadOnly() && treeNode.isAccessible();
					showDone = !Boolean.TRUE.equals(assessmentEvaluation.getFullyAssessed());
				}
			}
			topPaginationCtrl.updateAssessmentConfirmUI(confirmVisible, showDone);
			if (bottomPaginationCtrl != null) {
				bottomPaginationCtrl.updateAssessmentConfirmUI(confirmVisible, showDone);
			}
			updateProgressUI();
		}
	}
	
	private void updateProgressUI() {
		if (courseProgress != null) {
			// update visibility on role change
			if (courseProgress.isVisible() && !uce.isParticipant()) {
				courseProgress.setVisible(false);				
			} else if (!courseProgress.isVisible() && uce.isParticipant()) {
				courseProgress.setVisible(true);				
			} 
			// Update progress only if visible
			if (courseProgress.isVisible()) {				
				// 1) Progress
				CourseNode rootNode = getUce().getCourseEnvironment().getRunStructure().getRootNode();
				AssessmentEvaluation assessmentEvaluation = getUce().getScoreAccounting().evalCourseNode(rootNode);
				Double completion = assessmentEvaluation.getCompletion();
				float actual = completion != null? completion.floatValue(): 0;
				if (actual * 100 != courseProgress.getActual()) {
					courseProgress.setActual(actual * 100);
					courseProgress.setBarColor(BarColor.success);					
				}
				// 2) SCORE
				Float score = assessmentEvaluation.getScore();
				if (score != null && score > 0) {
					courseProgress.setInfo(Math.round(score) + "pt");
				}
				// 3) Status
				Boolean passed = assessmentEvaluation.getPassed();
				if (passed != null) {
					if (passed.booleanValue()) {
						courseProgress.setBarColor(BarColor.success);					
						courseProgress.setCssClass("o_progress_passed");
					} else {
						courseProgress.setBarColor(BarColor.danger);											
						courseProgress.setCssClass("o_progress_failed");
					}
				}
				
				
			}
		}		
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(needsRebuildAfter) {
			currentCourseNode = updateAfterChanges(currentCourseNode, currentCourseNode.getIdent());
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
				doNodeClick(ureq, tev, false);
			}
		} else if (source == coursemain) {
			if ("activateCourseNode".equals(event.getCommand())) {
				// Events from the JS function o_activateCourseNode() - activate the given node id
				String nodeid = ureq.getParameter("nodeid");
				if (nodeid != null) {
					updateTreeAndContent(ureq, nodeid);
				}
			} else if ("activateCourseTool".equals(event.getCommand())) {
				String toolname = ureq.getParameter("toolname");
				if (toolname != null) {
					try {
						toolname = toolname.toLowerCase();
						CourseTool tool = CourseTool.valueOf(toolname);
						fireEvent(ureq, new OpenCourseToolEvent(tool));
					} catch (Exception e) {
						getWindowControl().setWarning(translate("msg.tool.not.available", new String[] { toolname } ));
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
				navHandler = new NavigationHandler(uce, visibilityFilter, false);
				
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

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(needsRebuildAfter) {
			currentCourseNode = updateAfterChanges(currentCourseNode, currentCourseNode.getIdent());
			needsRebuildAfter = false;
		}
		
		// event from the current tool (editor, group management, archiver)	
		
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
				// we have a chance here to test if we need to refresh the evaluation.
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
			} else if (event == AssessmentEvents.CHANGED_EVENT) {
				doAssessmentChanged();
			} else if (event instanceof BusinessGroupModifiedEvent) {
				fireEvent(ureq, event);
				updateTreeAndContent(ureq, currentCourseNode, null);
			} else if(event instanceof GoToEvent) {
				fireEvent(ureq, event);
			}
		} else if (source == topPaginationCtrl || source == bottomPaginationCtrl) {
			if (event == CoursePaginationController.NEXT_EVENT) {
				doNext(ureq);
			} else if (event == CoursePaginationController.PREVIOUS_EVENT) {
				doPrevious(ureq);
			} else if (event == CoursePaginationController.CONFIRMED_EVENT) {
				doAssessmentConfirmation(true);
			} else if (event == CoursePaginationController.UNCONFIRMED_EVENT) {
				doAssessmentConfirmation(false);
			}
		} else if (source == disclaimerController) {
			if (event == Event.DONE_EVENT) {
				disclaimerAccepted = true;
				coursemain.put("coursemain", columnLayoutCtr.getInitialComponent());
				coursemain.setDirty(true);
				fireEvent(ureq, COURSE_DISCLAIMER_ACCEPTED);
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
			doNodeClick(ureq, tev, leaningPath);
		}
		doScrollTop();
	}

	private void doPrevious(UserRequest ureq) {
		List<TreeNode> flatList = new ArrayList<>();
		TreeNode currentNode = luTree.getSelectedNode();
		TreeHelper.makeTreeFlat(luTree.getTreeModel().getRootNode(), flatList);
		TreeNode previousNonDelegatingNode = getPreviousNonDelegatingNode(flatList, currentNode);
		if (previousNonDelegatingNode != null) {
			TreeEvent tev = new TreeEvent(MenuTree.COMMAND_TREENODE_CLICKED, previousNonDelegatingNode.getIdent());
			doNodeClick(ureq, tev, leaningPath);
		}
		doScrollTop();
	}
	
	private TreeNode getPreviousNonDelegatingNode(List<TreeNode> flatList, TreeNode treeNode) {
		int index = flatList.indexOf(treeNode);
		if (index-1 >= 0 && index-1 < flatList.size()) {
			TreeNode previousNode = flatList.get(index - 1);
			if (previousNode != null) {
				if (isPreviuosDelegatingNode(previousNode, treeNode)) {
					return getPreviousNonDelegatingNode(flatList, previousNode);
				}
				return previousNode;
			}
		}
		return null;
	}
	
	private boolean isPreviuosDelegatingNode(TreeNode previousNode, TreeNode currentNode) {
		// Delegating wiki or content package
		boolean previousCourseTreeNode = previousNode instanceof CourseTreeNode;
		boolean currentNoCourseTreeNode = !(currentNode instanceof CourseTreeNode);
		if (currentNoCourseTreeNode && previousCourseTreeNode) return true;
		
		// If it is delegating but not accessible it's ok, because the no access message is shown.
		CourseNode previousCourseNode = previousCourseTreeNode? ((CourseTreeNode)previousNode).getCourseNode(): null;
		return STCourseNode.isDelegatingSTCourseNode(previousCourseNode) && previousNode.isAccessible();
	}
	
	private void doAssessmentConfirmation(boolean confirmed) {
		nodeAccessService.onAssessmentConfirmed(getCurrentCourseNode(), getUce(), confirmed);
		updateAfterChanges(getCurrentCourseNode(), luTree.getSelectedNodeId());
		updateAssessmentConfirmUI(getCurrentCourseNode());
	}
	
	private void doScrollTop() {
		String srollToTopJS = "try {o_scrollToElement('#o_top');}catch(e){}";
		JSCommand jsc = new JSCommand(srollToTopJS);
		WindowControl wControl = getWindowControl();
		if (wControl != null && wControl.getWindowBackOffice() != null) {
			wControl.getWindowBackOffice().sendCommandTo(jsc);
		}
	}
	
	private void doAssessmentChanged() {
		updateAfterChanges(getCurrentCourseNode(), luTree.getSelectedNodeId());
		updateProgressUI();
	}

	private void doNodeClick(UserRequest ureq, TreeEvent tev, boolean singleBranch) {
		// goto node:
		// after a click in the tree, evaluate the model anew, and set the
		// selection of the tree again
		NodeClickedRef nclr = navHandler.evaluateJumpToTreeNode(ureq, getWindowControl(), treeModel, tev, this, null, currentNodeController, singleBranch);
		if (!nclr.isVisible()) {
			getWindowControl().setWarning(translate("msg.nodenotavailableanymore"));
			// go to root since the current node is no more visible 
			updateTreeAndContent(ureq, null, null);
			updateNextPrevious();
			updateCourseDataAttributes(nclr.getCalledCourseNode());
			updateAssessmentConfirmUI(nclr.getCalledCourseNode());
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
			updateAssessmentConfirmUI(nclr.getCalledCourseNode());
			updateProgressUI();
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
		updateLastUsage(nclr.getCalledCourseNode());
		try {
			currentNodeController = nclr.getRunController();
			Component nodeComp = currentNodeController.getInitialComponent();
			contentP.setContent(nodeComp);
			addToHistory(ureq, currentNodeController);
		} catch (Exception e) {
			log.error("Error on course node clicked! repositoryEntry={}, node={}, selectedNode={}, subTreeListener={}, identity={}"
					, course.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey()
					, nclr.getCalledCourseNode().getIdent()
					, nclr.getSelectedNodeId()
					, nclr.isHandledBySubTreeModelListener()
					, getIdentity());
			log.error("", e);
		}
		
		// set glossary wrapper dirty after menu click to make it reload the glossary
		// stuff properly when in AJAX mode
		if (glossaryMarkerCtr != null && glossaryMarkerCtr.isTextMarkingEnabled()) {
			glossaryMarkerCtr.getInitialComponent().setDirty(true);
		}
		
		updateNextPrevious();
		updateCourseDataAttributes(nclr.getCalledCourseNode());
		updateAssessmentConfirmUI(nclr.getCalledCourseNode());
		updateProgressUI();
	}

	/**
	 * Implementation of listener which listens to publish events
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
					uce.getScoreAccounting().evaluateAll();
					updateProgressUI();
					updateAfterChanges(getCurrentCourseNode(), luTree.getSelectedNodeId());
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

	@Override
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
        super.doDispose();
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
		
		if(!navHandler.checkPublicationTimestamp()) {
			reloadOnActivate();
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
	
	private void reloadOnActivate() {
		try {
			CourseNode courseNode = getCurrentCourseNode();
			String selectedNodeId = luTree.getSelectedNodeId();
			if(courseNode != null && selectedNodeId != null) {
				currentCourseNode = updateAfterChanges(courseNode, selectedNodeId);
				needsRebuildAfter = false;
			}
		} catch (Exception e) {
			logError("", e);
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