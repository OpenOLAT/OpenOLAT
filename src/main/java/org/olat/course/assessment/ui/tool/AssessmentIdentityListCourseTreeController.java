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

import java.util.Collections;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
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
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentIdentityListCourseTreeController extends BasicController implements Activateable2 {
	
	private final MenuTree menuTree;
	private final Panel mainPanel;
	private final TooledStackedPanel stackPanel;
	private final AssessmentToolContainer toolContainer;
	private Controller currentCtrl;
	
	private final RepositoryEntry courseEntry;
	private final BusinessGroup businessGroup;
	private final UserCourseEnvironment coachCourseEnv;
	private AssessmentToolSecurityCallback assessmentCallback;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public AssessmentIdentityListCourseTreeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup businessGroup, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		this.courseEntry = courseEntry;
		this.stackPanel = stackPanel;
		this.toolContainer = toolContainer;
		this.assessmentCallback = assessmentCallback;
		this.businessGroup = businessGroup;
		this.coachCourseEnv = coachCourseEnv;

		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		// Navigation menu
		menuTree = new MenuTree("menuTree");
		TreeModel tm = AssessmentHelper.assessmentTreeModel(course);
		menuTree.setTreeModel(tm);
		menuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		menuTree.addListener(this);
		
		mainPanel = new Panel("empty");
		LayoutMain3ColsController columLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainPanel, "course" + course.getResourceableId());
		listenTo(columLayoutCtr); // cleanup on dispose
		putInitialPanel(columLayoutCtr.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		boolean emptyEntries = entries == null || entries.isEmpty();
		if(emptyEntries) {
			TreeNode rootNode = menuTree.getTreeModel().getRootNode();
			if(rootNode.getUserObject() instanceof CourseNode) {
				doSelectCourseNode(ureq, (CourseNode)rootNode.getUserObject());
			}
		} else {
			String resourceTypeName = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Node".equalsIgnoreCase(resourceTypeName)) {
				Long nodeIdent = entries.get(0).getOLATResourceable().getResourceableId();
				CourseNode courseNode = CourseFactory.loadCourse(courseEntry).getRunStructure()
						.getNode(nodeIdent.toString());
				TreeNode courseTreeNode = TreeHelper
						.findNodeByUserObject(courseNode, menuTree.getTreeModel().getRootNode());	
				if(courseNode != null && courseTreeNode != null) {
					doSelectCourseNode(ureq, courseNode);
					menuTree.setSelectedNode(courseTreeNode);
				}
			}
		}
		
		if(currentCtrl instanceof Activateable2) {
			List<ContextEntry> subEntries = emptyEntries
					? entries : entries.subList(1, entries.size());
			StateEntry subState = emptyEntries ? state : entries.get(0).getTransientState();
			((Activateable2)currentCtrl).activate(ureq, subEntries, subState);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				Object uo = menuTree.getSelectedNode().getUserObject();
				if(uo instanceof CourseNode) {
					CourseNode cn = (CourseNode)uo;
					Controller ctrl = doSelectCourseNode(ureq, cn);
					if(ctrl instanceof Activateable2) {
						((Activateable2)ctrl).activate(ureq, null, null);
					}
					fireEvent(ureq, new CourseNodeEvent(CourseNodeEvent.SELECT_COURSE_NODE, cn.getIdent()));
				}
			}
		}
	}

	private Controller doSelectCourseNode(UserRequest ureq, CourseNode courseNode) {
		removeAsListenerAndDispose(currentCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Node", new Long(courseNode.getIdent()));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		if(assessmentConfig.isAssessedBusinessGroups() && courseNode instanceof GTACourseNode) {
			CourseEnvironment courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
			List<BusinessGroup> coachedGroups;
			if(businessGroup != null) {
				coachedGroups = Collections.singletonList(businessGroup);
			} else if(assessmentCallback.isAdmin()) {
				coachedGroups = courseEnv.getCourseGroupManager().getAllBusinessGroups();
			} else {
				coachedGroups = assessmentCallback.getCoachedGroups();
			}
			currentCtrl = ((GTACourseNode)courseNode).getCoachedGroupListController(ureq, bwControl, stackPanel,
					coachCourseEnv, assessmentCallback.isAdmin(), coachedGroups);
		} else if (courseAssessmentService.hasCustomIdentityList(courseNode)) {
			currentCtrl = courseAssessmentService.getIdentityListController(ureq, bwControl, stackPanel, courseNode,
					courseEntry, businessGroup, coachCourseEnv, toolContainer, assessmentCallback);
		} else {
			currentCtrl = new IdentityListCourseNodeController(ureq, bwControl, stackPanel, courseEntry, businessGroup,
					courseNode, coachCourseEnv, toolContainer, assessmentCallback, true);
		}
		listenTo(currentCtrl);
		mainPanel.setContent(currentCtrl.getInitialComponent());
		addToHistory(ureq, currentCtrl);
		return currentCtrl;
	}
}
