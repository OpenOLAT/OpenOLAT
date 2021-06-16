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
import org.olat.core.gui.components.stack.PopEvent;
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
 * Initial date: 13.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCourseTreeController extends BasicController implements Activateable2 {
	
	private final Panel mainPanel;
	private final MenuTree menuTree;
	private final TooledStackedPanel stackPanel;

	private Controller currentCtrl;
	private Controller businessGroupListCtrl;
	private AssessmentCourseNodeController identityListCtrl; 
	
	private View view = View.users;
	private TreeNode selectedNodeChanged;
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
				if(view == null) {
					view = View.users;
				}
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
					view = View.users;
					Controller ctrl = doSelectCourseNode(ureq, treeNode, courseNode);
					if(ctrl instanceof Activateable2) {
						((Activateable2)ctrl).activate(ureq, entries, null);
					}
					menuTree.setSelectedNode(treeNode);
				}
			} else if("Node".equalsIgnoreCase(resourceTypeName) || "CourseNode".equalsIgnoreCase(resourceTypeName)) {
				Long nodeIdent = entries.get(0).getOLATResourceable().getResourceableId();
				CourseNode courseNode = CourseFactory.loadCourse(courseEntry).getRunStructure().getNode(nodeIdent.toString());
				TreeNode treeNode = TreeHelper.findNodeByUserObject(courseNode, menuTree.getTreeModel().getRootNode());
				if(courseNode != null) {
					if(view == null) {
						view = View.users;
					}
					Controller ctrl = doSelectCourseNode(ureq, treeNode, courseNode);
					if(ctrl instanceof Activateable2) {
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						((Activateable2)ctrl).activate(ureq, subEntries, entry.getTransientState());
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
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if("users".equals(pe.getUserObject())) {
					fixHistory(ureq, "Users", "users");
				} else if("groups".equals(pe.getUserObject())) {
					if(selectedNodeChanged != null) {
						CourseNode cn = (CourseNode)selectedNodeChanged.getUserObject();
						menuTree.setSelectedNode(selectedNodeChanged);
						processSelectCourseNodeWithMemory(ureq, selectedNodeChanged, cn);
						selectedNodeChanged = null;
					} else {
						fixHistory(ureq, "BusinessGroups", "groups");
					}
				}
			}
		}
	}
	
	private void processSelectCourseNodeWithMemory(UserRequest ureq, TreeNode tn, CourseNode cn) {
		StateEntry listState = null;
		if(currentCtrl != null && currentCtrl == identityListCtrl) {
			listState = identityListCtrl.getListState();
		}
		Controller ctrl = doSelectCourseNode(ureq, tn, cn);
		if(ctrl instanceof Activateable2) {
			((Activateable2)ctrl).activate(ureq, null, listState);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == businessGroupListCtrl) {
			if(event instanceof CourseNodeEvent) {
				CourseNodeEvent cne = (CourseNodeEvent)event;
				CourseNode courseNode = CourseFactory.loadCourse(courseEntry).getRunStructure().getNode(cne.getIdent());
				TreeNode treeNode = TreeHelper.findNodeByUserObject(courseNode, menuTree.getTreeModel().getRootNode());
				if(treeNode == null) {
					treeNode = menuTree.getTreeModel().getRootNode();
					courseNode = CourseFactory.loadCourse(courseEntry).getRunStructure().getRootNode();
					doSelectCourseNode(ureq, treeNode, courseNode);
					menuTree.setSelectedNode(treeNode);
					showWarning("warning.course.node.deleted");
				} else {
					stackPanel.changeDisplayname(treeNode.getTitle(), "o_icon " + treeNode.getIconCssClass(), this);
					selectedNodeChanged = treeNode;
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void fixHistory(UserRequest ureq, String oresName, String i18nName) {
		CourseNode courseNode;
		if(menuTree.getSelectedNode() != null) {
			courseNode = (CourseNode)menuTree.getSelectedNode().getUserObject();
		} else {
			courseNode = (CourseNode)menuTree.getTreeModel().getRootNode().getUserObject();
		}
		OLATResourceable oresUsers = OresHelper.createOLATResourceableInstance(oresName, 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(oresUsers, null, getWindowControl());
		OLATResourceable oresNode = OresHelper.createOLATResourceableInstance("Node", new Long(courseNode.getIdent()));
		WindowControl bbwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(oresNode, null, bwControl);
		addToHistory(ureq, bbwControl);
		
		stackPanel.pushController(translate(i18nName), null, i18nName);
	}
	
	protected void switchToBusinessGroupsView(UserRequest ureq) {
		view = View.groups;

		TreeNode treeNode = menuTree.getSelectedNode();
		CourseNode courseNode = (CourseNode)treeNode.getUserObject();
		Controller ctrl = doSelectCourseNode(ureq, treeNode, courseNode);
		if(ctrl instanceof Activateable2) {
			((Activateable2)ctrl).activate(ureq, null, null);
		}
	}
	
	/**
	 * Switch to the user list
	 * 
	 * @param ureq
	 * @param stateOfUserList Optional
	 */
	protected void switchToUsersView(UserRequest ureq, StateEntry stateOfUserList) {
		view = View.users;
		
		TreeNode treeNode = menuTree.getSelectedNode();
		CourseNode courseNode = (CourseNode)treeNode.getUserObject();
		Controller ctrl = doSelectCourseNode(ureq, treeNode, courseNode);
		if(ctrl instanceof Activateable2) {
			((Activateable2)ctrl).activate(ureq, null, stateOfUserList);
		}
	}


	private Controller doSelectCourseNode(UserRequest ureq, TreeNode treeNode, CourseNode courseNode) {
		stackPanel.changeDisplayname(treeNode.getTitle(), "o_icon " + treeNode.getIconCssClass(), this);
		stackPanel.popUpToController(this);
		
		if(view == View.users) {
			currentCtrl = doSelectCourseNodeUsersView(ureq, courseNode);
			stackPanel.pushController(translate("users"), null, "users");
		} else if(view == View.groups) {
			currentCtrl = doSelectCourseNodeBusinessGroupsView(ureq, courseNode);
			stackPanel.pushController(translate("groups"), null, "groups");
		}

		listenTo(currentCtrl);
		mainPanel.setContent(currentCtrl.getInitialComponent());
		addToHistory(ureq, currentCtrl);
		return currentCtrl;
	}

	private Controller doSelectCourseNodeUsersView(UserRequest ureq, CourseNode courseNode) {
		removeAsListenerAndDispose(identityListCtrl);
		
		OLATResourceable oresUsers = OresHelper.createOLATResourceableInstance("Users", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(oresUsers, null, getWindowControl());
		OLATResourceable oresNode = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(courseNode.getIdent()));
		WindowControl bbwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(oresNode, null, bwControl);
		return courseAssessmentService.getIdentityListController(ureq, bbwControl, stackPanel, courseNode, courseEntry,
				null, coachCourseEnv, toolContainer, assessmentCallback, true);
	}
	
	private Controller doSelectCourseNodeBusinessGroupsView(UserRequest ureq, CourseNode courseNode) {
		removeAsListenerAndDispose(businessGroupListCtrl);
		
		OLATResourceable oresGroups = OresHelper.createOLATResourceableInstance("BusinessGroups", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(oresGroups, null, getWindowControl());
		OLATResourceable oresNode = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(courseNode.getIdent()));
		WindowControl bbwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(oresNode, null, bwControl);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		if(assessmentConfig.isAssessedBusinessGroups()) {
			if(courseNode instanceof GTACourseNode) {
				CourseEnvironment courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
				
				List<BusinessGroup> coachedGroups;
				if(assessmentCallback.isAdmin()) {
					coachedGroups = courseEnv.getCourseGroupManager().getAllBusinessGroups();
				} else {
					coachedGroups = assessmentCallback.getCoachedGroups();
				}
				businessGroupListCtrl = ((GTACourseNode)courseNode).getCoachedGroupListController(ureq, bbwControl, stackPanel,
						coachCourseEnv, assessmentCallback.isAdmin(), coachedGroups);
			}
		} else {
			businessGroupListCtrl = new AssessedBusinessGroupCourseNodeListController(ureq, bbwControl, stackPanel,
					courseEntry, courseNode, coachCourseEnv, toolContainer, assessmentCallback);
		}
		return businessGroupListCtrl;
	}
	
	private enum View {
		groups,
		users
	}
}