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

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
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
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCourseTreeController extends BasicController implements Activateable2 {
	
	private final Panel mainPanel;
	private final MenuTree menuTree;
	private final TooledStackedPanel stackPanel;

	private AssessmentCourseNodeController identityListCtrl; 
	
	private final RepositoryEntry courseEntry;
	private final UserCourseEnvironment coachCourseEnv;
	private final AssessmentToolContainer toolContainer;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
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
	
	public TreeNode getSelectedCourseNode() {
		return menuTree.getSelectedNode();
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		boolean emptyEntries = entries == null || entries.isEmpty();
		if(emptyEntries) {
			TreeNode rootNode = menuTree.getTreeModel().getRootNode();
			if(rootNode.getUserObject() instanceof CourseNode) {
				doSelectCourseNode(ureq, rootNode, (CourseNode)rootNode.getUserObject());
				menuTree.setSelectedNode(rootNode);
			}
		} else {
			ContextEntry entry = entries.get(0);
			String resourceTypeName = entry.getOLATResourceable().getResourceableTypeName();
			if("Identity".equalsIgnoreCase(resourceTypeName)) {
				TreeNode treeNode =  menuTree.getTreeModel().getRootNode();
				CourseNode courseNode = (CourseNode)treeNode.getUserObject();
				if(courseNode != null) {
					AssessmentCourseNodeController ctrl = doSelectCourseNode(ureq, treeNode, courseNode);
					if(ctrl != null) {
						ctrl.activate(ureq, entries, null);
					}
					menuTree.setSelectedNode(treeNode);
				}
			} else if("Node".equalsIgnoreCase(resourceTypeName) || "CourseNode".equalsIgnoreCase(resourceTypeName)) {
				Long nodeIdent = entries.get(0).getOLATResourceable().getResourceableId();
				CourseNode courseNode = CourseFactory.loadCourse(courseEntry).getRunStructure().getNode(nodeIdent.toString());
				TreeNode treeNode = TreeHelper.findNodeByUserObject(courseNode, menuTree.getTreeModel().getRootNode());
				if(courseNode != null) {
					AssessmentCourseNodeController ctrl = doSelectCourseNode(ureq, treeNode, courseNode);
					if(ctrl != null) {
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						ctrl.activate(ureq, subEntries, entry.getTransientState());
					}
					menuTree.setSelectedNode(treeNode);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selectedTreeNode = menuTree.getSelectedNode();
				Object uo = selectedTreeNode.getUserObject();
				if(uo instanceof CourseNode) {
					processSelectCourseNodeWithMemory(ureq, selectedTreeNode, (CourseNode)uo);
				}
			}
		}
	}
	
	private void processSelectCourseNodeWithMemory(UserRequest ureq, TreeNode tn, CourseNode cn) {
		StateEntry listState = identityListCtrl.getListState();
		AssessmentCourseNodeController ctrl = doSelectCourseNode(ureq, tn, cn);
		if(ctrl != null) {
			ctrl.activate(ureq, null, listState);
		}
	}
	
	/**
	 * Switch to the user list
	 * 
	 * @param ureq
	 * @param stateOfUserList Optional
	 */
	protected void switchToUsersView(UserRequest ureq, StateEntry stateOfUserList) {
		TreeNode treeNode = menuTree.getSelectedNode();
		CourseNode courseNode = (CourseNode)treeNode.getUserObject();
		AssessmentCourseNodeController ctrl = doSelectCourseNode(ureq, treeNode, courseNode);
		if(ctrl != null) {
			ctrl.activate(ureq, null, stateOfUserList);
		}
	}

	private AssessmentCourseNodeController doSelectCourseNode(UserRequest ureq, TreeNode treeNode, CourseNode courseNode) {
		stackPanel.changeDisplayname(treeNode.getTitle(), "o_icon " + treeNode.getIconCssClass(), this);
		stackPanel.popUpToController(this);

		removeAsListenerAndDispose(identityListCtrl);
		
		OLATResourceable oresNode = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(courseNode.getIdent()));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(oresNode, null, getWindowControl());
		identityListCtrl = courseAssessmentService.getIdentityListController(ureq, bwControl, stackPanel, courseNode, courseEntry,
				null, coachCourseEnv, toolContainer, assessmentCallback, true);
		if(identityListCtrl == null) {
			mainPanel.setContent(new Panel("empty"));
		} else {
			listenTo(identityListCtrl);
			mainPanel.setContent(identityListCtrl.getInitialComponent());
			addToHistory(ureq, identityListCtrl);
		}
		return identityListCtrl;
	}
}