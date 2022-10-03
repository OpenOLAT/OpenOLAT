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
package org.olat.course.assessment.ui.tool;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Container;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.event.ParticipantTypeFilterEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCourseTreeController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	private final MenuTree overviewMenuTree;
	private final GenericTreeNode overviewNode;
	private final MenuTree menuTree;
	private final SegmentViewComponent segmentView;
	private final TooledStackedPanel stackPanel;
	private final Link courseNodeOverviewLink;
	private final Link participantsLink;

	private AssessmentCourseOverviewController overviewCtrl;
	private AssessmentCourseNodeOverviewController courseNodeOverviewCtrl;
	private AssessmentEventToState assessmentEventToState;
	private AssessmentCourseNodeController identityListCtrl; 
	
	private final RepositoryEntry courseEntry;
	private final UserCourseEnvironment coachCourseEnv;
	private final AssessmentToolContainer toolContainer;
	private final AssessmentToolSecurityCallback assessmentCallback;
	private final String rootCourseNodeIdent;
	private List<ParticipantType> participantTypeFilter = List.of(ParticipantType.member);
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public AssessmentCourseTreeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.courseEntry = courseEntry;
		this.toolContainer = toolContainer;
		this.coachCourseEnv = coachCourseEnv;
		this.assessmentCallback = assessmentCallback;
		
		stackPanel.addListener(this);
		
		ICourse course = CourseFactory.loadCourse(courseEntry);
		rootCourseNodeIdent = course.getRunStructure().getRootNode().getIdent();
		
		// Overview navigation
		overviewMenuTree = new MenuTree("menuTree");
		GenericTreeModel overviewTreeModel = new GenericTreeModel();
		overviewNode = new GenericTreeNode();
		overviewNode.setTitle(translate("assessment.tool.overview"));
		overviewNode.setIconCssClass("o_icon_assessment_tool");
		overviewTreeModel.setRootNode(overviewNode);
		overviewMenuTree.setTreeModel(overviewTreeModel);
		overviewMenuTree.setSelectedNodeId(overviewNode.getIdent());
		overviewMenuTree.addListener(this);

		// Navigation menu
		menuTree = new MenuTree("menuTree");
		TreeModel tm = AssessmentHelper.assessmentTreeModel(course, getLocale());
		menuTree.setTreeModel(tm);
		menuTree.setSelectedNode(tm.getRootNode());
		menuTree.setHighlightSelection(false);
		menuTree.addListener(this);
		
		Container menuCont = createVelocityContainer("menu");
		menuCont.put("overview", overviewMenuTree);
		menuCont.put("nodes", menuTree);
		
		mainVC = createVelocityContainer("tree_main");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		courseNodeOverviewLink = LinkFactory.createLink("segment.overview", mainVC, this);
		courseNodeOverviewLink.setElementCssClass("o_sel_assessment_tool_node_overview");
		segmentView.addSegment(courseNodeOverviewLink, true);
		participantsLink = LinkFactory.createLink("segment.participants", mainVC, this);
		participantsLink.setElementCssClass("o_sel_assessment_tool_node_participants");
		segmentView.addSegment(participantsLink, false);
		
		LayoutMain3ColsController columLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuCont, mainVC, "course" + course.getResourceableId());
		listenTo(columLayoutCtr); // cleanup on dispose
		putInitialPanel(columLayoutCtr.getInitialComponent());
	}
	
	public String getRootNodeId() {
		return rootCourseNodeIdent;
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
        super.doDispose();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String resourceTypeName = entry.getOLATResourceable().getResourceableTypeName();
			if("Identity".equalsIgnoreCase(resourceTypeName)) {
				TreeNode treeNode =  menuTree.getTreeModel().getRootNode();
				CourseNode courseNode = (CourseNode)treeNode.getUserObject();
				if(courseNode != null) {
					AssessmentCourseNodeController ctrl = doOpenParticipants(ureq, treeNode, courseNode);
					if(ctrl != null) {
						ctrl.activate(ureq, entries, syncParticipantTypes(state));
					}
					menuTree.setSelectedNode(treeNode);
				}
			} else if("Node".equalsIgnoreCase(resourceTypeName) || "CourseNode".equalsIgnoreCase(resourceTypeName)) {
				Long nodeIdent = entries.get(0).getOLATResourceable().getResourceableId();
				CourseNode courseNode = CourseFactory.loadCourse(courseEntry).getRunStructure().getNode(nodeIdent.toString());
				TreeNode treeNode = TreeHelper.findNodeByUserObject(courseNode, menuTree.getTreeModel().getRootNode());
				if(courseNode != null) {
					AssessmentCourseNodeController ctrl = doOpenParticipants(ureq, treeNode, courseNode);
					if(ctrl != null) {
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						ctrl.activate(ureq, subEntries, syncParticipantTypes(state));
					}
					menuTree.setSelectedNode(treeNode);
				}
			} else if ("NodeOverview".equalsIgnoreCase(resourceTypeName)) {
				Long nodeIdent = entries.get(0).getOLATResourceable().getResourceableId();
				CourseNode courseNode = CourseFactory.loadCourse(courseEntry).getRunStructure().getNode(nodeIdent.toString());
				TreeNode treeNode = TreeHelper.findNodeByUserObject(courseNode, menuTree.getTreeModel().getRootNode());
				if (courseNode != null) {
					doOpenCourseNodeOverview(ureq, treeNode, courseNode);
					menuTree.setSelectedNode(treeNode);
				}
			} else if ("Overview".equalsIgnoreCase(resourceTypeName)) {
				doOpenOverview(ureq);
			}
		} else {
			doOpenOverview(ureq);
		}
	}

	private AssessedIdentityListState syncParticipantTypes(StateEntry state) {
		AssessedIdentityListState listState = null;
		if (state instanceof AssessedIdentityListState) {
			listState = (AssessedIdentityListState)state;
			List<String> members = listState.getMembers();
			if (members == null) {
				members = participantTypeFilter.stream().map(ParticipantType::name).collect(Collectors.toList());
				listState.setMembers(members);
			} else {
				participantTypeFilter = members.stream().map(ParticipantType::valueOf).collect(Collectors.toList());
			}
		} else {
			List<String> members = participantTypeFilter.stream().map(ParticipantType::name).collect(Collectors.toList());
			listState = new AssessedIdentityListState(null, null, null, members, null, null, null, true);
		}
		return listState;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == overviewMenuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selectedTreeNode = overviewMenuTree.getSelectedNode();
				if (selectedTreeNode == overviewNode) {
					doOpenOverview(ureq);
				}
			}
		} else if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selectedTreeNode = menuTree.getSelectedNode();
				Object uo = selectedTreeNode.getUserObject();
				if(uo instanceof CourseNode) {
					if (segmentView.isSelected(courseNodeOverviewLink)) {
						doOpenCourseNodeOverview(ureq, selectedTreeNode, (CourseNode)uo);
					} else {
						processSelectCourseNodeWithMemory(ureq, selectedTreeNode, (CourseNode)uo);
					}
				}
			}
		} else if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				TreeNode selectedTreeNode = menuTree.getSelectedNode();
				Object uo = selectedTreeNode.getUserObject();
				if (uo instanceof CourseNode) {
					if (clickedLink == courseNodeOverviewLink) {
						doOpenCourseNodeOverview(ureq, selectedTreeNode, (CourseNode)uo);
					} else if (clickedLink == participantsLink) {
						doOpenParticipants(ureq, selectedTreeNode, (CourseNode)uo).activate(ureq, null, syncParticipantTypes(null));
					}
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (assessmentEventToState != null && assessmentEventToState.handlesEvent(source, event)) {
			TreeNode selectedTreeNode = menuTree.getSelectedNode();
			Object uo = selectedTreeNode.getUserObject();
			if (uo instanceof CourseNode) {
				AssessedIdentityListState state = assessmentEventToState.getState(event);
				doOpenParticipants(ureq, selectedTreeNode, (CourseNode)uo).activate(ureq, null, syncParticipantTypes(state));
			}
		} else if (source == overviewCtrl) {
			if (event instanceof ParticipantTypeFilterEvent) {
				participantTypeFilter = ((ParticipantTypeFilterEvent)event).getParticipantTypes();
			} else {
				fireEvent(ureq, event);
			}
		} else if (source == courseNodeOverviewCtrl) {
			if (event instanceof ParticipantTypeFilterEvent) {
				participantTypeFilter = ((ParticipantTypeFilterEvent)event).getParticipantTypes();
			}
		} else if (source == identityListCtrl) {
			if (event instanceof ParticipantTypeFilterEvent) {
				participantTypeFilter = ((ParticipantTypeFilterEvent)event).getParticipantTypes();
			}
		}
		super.event(ureq, source, event);
	}

	private void processSelectCourseNodeWithMemory(UserRequest ureq, TreeNode tn, CourseNode cn) {
		StateEntry listState = null;
		if (identityListCtrl != null) {
			listState = identityListCtrl.getListState();
		}
		
		AssessmentCourseNodeController ctrl = doOpenParticipants(ureq, tn, cn);
		if(ctrl != null) {
			ctrl.activate(ureq, null, syncParticipantTypes(listState));
		}
	}

	private AssessmentCourseNodeController doOpenParticipants(UserRequest ureq, TreeNode treeNode, CourseNode courseNode) {
		doInitOpenCourseNode(treeNode, courseNode);
		removeAsListenerAndDispose(identityListCtrl);
		
		OLATResourceable oresNode = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(courseNode.getIdent()));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(oresNode, null, getWindowControl());
		identityListCtrl = courseAssessmentService.getIdentityListController(ureq, bwControl, stackPanel, courseNode, courseEntry,
				coachCourseEnv, toolContainer, assessmentCallback, false);
		if(identityListCtrl == null) {
			mainVC.remove("segments");
		} else {
			listenTo(identityListCtrl);
			addToHistory(ureq, identityListCtrl);
			mainVC.put("segments", segmentView);
			mainVC.put("segmentCmp", identityListCtrl.getInitialComponent());
		}
		
		segmentView.select(participantsLink);
		return identityListCtrl;
	}

	private void doInitOpenCourseNode(TreeNode treeNode, CourseNode courseNode) {
		overviewMenuTree.setHighlightSelection(false);
		menuTree.setHighlightSelection(true);
		
		stackPanel.popUpToController(this);
		stackPanel.changeDisplayname(treeNode.getTitle(), "o_icon " + treeNode.getIconCssClass(), this);
		mainVC.contextPut("courseNodeIcon", treeNode.getIconCssClass());
		mainVC.contextPut("courseNodeTitle", courseNode.getLongTitle());		
		CourseNodeConfiguration nodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(courseNode.getType());
		String translatedType = nodeConfig.getLinkText(getLocale());
		mainVC.contextPut("translatedType", translatedType);
		mainVC.remove("overview");
	}
	
	private void doOpenCourseNodeOverview(UserRequest ureq, TreeNode treeNode, CourseNode courseNode) {
		doInitOpenCourseNode(treeNode, courseNode);
		removeAsListenerAndDispose(courseNodeOverviewCtrl);
		
		OLATResourceable oresNode = OresHelper.createOLATResourceableInstance("NodeOverview", Long.valueOf(courseNode.getIdent()));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(oresNode, null, getWindowControl());
		boolean courseInfoLaunch = rootCourseNodeIdent.equals(courseNode.getIdent());
		courseNodeOverviewCtrl = courseAssessmentService.getCourseNodeOverviewController(ureq, bwControl, courseNode,
				coachCourseEnv, courseInfoLaunch, false, false);
		courseNodeOverviewCtrl.reload(participantTypeFilter);
		listenTo(courseNodeOverviewCtrl);
		assessmentEventToState = new AssessmentEventToState(courseNodeOverviewCtrl);
		addToHistory(ureq, courseNodeOverviewCtrl);
		mainVC.put("segments", segmentView);
		mainVC.put("segmentCmp", courseNodeOverviewCtrl.getInitialComponent());
		segmentView.select(courseNodeOverviewLink);
	}
	
	private void doOpenOverview(UserRequest ureq) {
		overviewMenuTree.setHighlightSelection(true);
		menuTree.setHighlightSelection(false);
		
		stackPanel.popUpToController(this);
		stackPanel.changeDisplayname(translate("assessment.tool.overview"), "o_icon o_icon_assessment_tool", this);
		
		removeAsListenerAndDispose(overviewCtrl);
	
		OLATResourceable oresNode = OresHelper.createOLATResourceableInstance("Overview", Long.valueOf(0));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(oresNode, null, getWindowControl());
		overviewCtrl = new AssessmentCourseOverviewController(ureq, bwControl, courseEntry, coachCourseEnv, assessmentCallback);
		overviewCtrl.reload(participantTypeFilter);
		listenTo(overviewCtrl);
		addToHistory(ureq, overviewCtrl);
		mainVC.put("overview", overviewCtrl.getInitialComponent());
		mainVC.remove("segments");
	}

	public void reload() {
		if (overviewCtrl != null) {
			overviewCtrl.reload();
		}
		if (courseNodeOverviewCtrl != null) {
			courseNodeOverviewCtrl.reload();
		}
	}
	
	public void reloadAssessmentModes() {
		if (overviewCtrl != null) {
			overviewCtrl.reloadAssessmentModes();
		}
	}

}