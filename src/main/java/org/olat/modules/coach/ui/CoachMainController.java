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
package org.olat.modules.coach.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.RelationRole;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.grading.GradingModule;
import org.olat.modules.grading.GradingSecurityCallback;
import org.olat.modules.grading.GradingSecurityCallbackFactory;
import org.olat.modules.grading.model.GradingSecurity;
import org.olat.modules.grading.ui.GradingCoachingOverviewController;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.LecturesSecurityCallbackFactory;
import org.olat.modules.lecture.ui.coach.LecturesCoachingController;
import org.olat.user.ui.role.RelationRolesAndRightsUIFactory;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Description:<br>
 *
 * <P>
 * Initial Date:  7 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoachMainController extends MainLayoutBasicController implements Activateable2 {

	private final MenuTree menu;
	private final TooledStackedPanel content;

	private final boolean userSearchAllowed;
	private final GradingSecurity gradingSec;
	private final CoachingSecurity coachingSec;

	private GroupListController groupListCtrl;
	private UserSearchController userSearchCtrl;
	private CourseListController courseListCtrl;
	private StudentListController studentListCtrl;
	private LayoutMain3ColsController columnLayoutCtr;
	private GradingCoachingOverviewController gradingCtrl;
	private LecturesCoachingController lecturesTeacherCtrl;
	private LecturesCoachingController lecturesMasterCoachCtrl;

	private Map<String, RelationRole> userRelationRolesMap;

	@Autowired
	private GradingModule gradingModule;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private IdentityRelationshipService identityRelationsService;

	public CoachMainController(UserRequest ureq, WindowControl control, CoachingSecurity coachingSec, GradingSecurity gradingSec) {
		super(ureq, control);
		this.gradingSec = gradingSec;
		this.coachingSec = coachingSec;
		this.userRelationRolesMap = listAvailableRoles(identityRelationsService.getRelationsAsSource(ureq.getIdentity()));

		Roles roles = ureq.getUserSession().getRoles();
		userSearchAllowed = roles.isAdministrator() || roles.isLearnResourceManager() || roles.isPrincipal();

		menu = new MenuTree(null, "coachMenu", this);
		menu.setExpandSelectedNode(false);
		menu.setRootVisible(false);
		menu.setTreeModel(buildTreeModel());

		// Hide menu if only one entry is visible
		boolean hideCol1 = menu.getTreeModel().getRootNode().getChildCount() == 1;
		if (hideCol1) {
			// Check if the menu item has subitems
			hideCol1 = menu.getTreeModel().getRootNode().getChildAt(0).getChildCount() == 1;
		}

		content = new TooledStackedPanel("coaching-stack", getTranslator(), this);
		content.setNeverDisposeRootController(true);
		content.setToolbarAutoEnabled(true);
		content.setInvisibleCrumb(1);

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), hideCol1 ? null : menu, content, "coaching");
		columnLayoutCtr.addCssClassToMain("o_coaching");
		listenTo(columnLayoutCtr); // auto dispose later
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menu) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = menu.getSelectedNode();

				if (selTreeNode.getDelegate() != null) {
					String cmd = (String) selTreeNode.getDelegate().getUserObject();
					selectMenuItem(ureq,cmd);
				} else if (selTreeNode.getUserObject() instanceof String) {
					String cmd = (String)selTreeNode.getUserObject();
					selectMenuItem(ureq, cmd);
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			selectMenuItem(ureq, getDefaultMenuItem());
		} else {
			ContextEntry currentEntry = entries.get(0);
			String cmd = currentEntry.getOLATResourceable().getResourceableTypeName();
			Activateable2 selectedCtrl = selectMenuItem(ureq, cmd);

			if(selectedCtrl == null) {
				selectMenuItem(ureq, getDefaultMenuItem());
			} else {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				selectedCtrl.activate(ureq, subEntries, currentEntry.getTransientState());
			}
		}
	}

	private String getDefaultMenuItem() {
		if(lectureModule.isEnabled()) {
			if(coachingSec.isTeacher()) {
				return "Lectures";
			}
			if(coachingSec.isMasterCoachForLectures()) {
				return "Classes";
			}
		}
		if(coachingSec.isCoach()) {
			return "Members";
		}
		if(userSearchAllowed) {
			return "Search";
		}
		if(gradingModule.isEnabled() && (gradingSec.isGrader() || gradingSec.isGradedResourcesManager())) {
			return "Grading";
		}
		if (coachingSec.isUserRelationSource()) {
			return userRelationRolesMap.keySet().stream().findFirst().get();
		}
		return "Members";
	}

	private Activateable2 selectMenuItem(UserRequest ureq, String cmd) {
		Controller selectedCtrl = null;
		if("members".equalsIgnoreCase(cmd) || "students".equalsIgnoreCase(cmd)) {
			if(studentListCtrl == null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("Members", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
				studentListCtrl = new StudentListController(ureq, bwControl, content);
				listenTo(studentListCtrl);
			}
			selectedCtrl = studentListCtrl;
		} else if("groups".equalsIgnoreCase(cmd)) {
			if(groupListCtrl == null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("Groups", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
				groupListCtrl = new GroupListController(ureq, bwControl, content);
				listenTo(groupListCtrl);
			}
			selectedCtrl = groupListCtrl;
		} else if("courses".equalsIgnoreCase(cmd)) {
			if(courseListCtrl == null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("Courses", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
				courseListCtrl = new CourseListController(ureq, bwControl, content);
				listenTo(courseListCtrl);
			}
			selectedCtrl = courseListCtrl;
		} else if("lectures".equalsIgnoreCase(cmd) && lectureModule.isEnabled() && coachingSec.isTeacher()) {
			if(lecturesTeacherCtrl == null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("Lectures", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
				LecturesSecurityCallback secCallback = LecturesSecurityCallbackFactory.getSecurityCallback(false, false, true, LectureRoles.teacher, false);
				lecturesTeacherCtrl = new LecturesCoachingController(ureq, bwControl, content, secCallback);
				listenTo(lecturesTeacherCtrl);
			}
			selectedCtrl = lecturesTeacherCtrl;
		} else if("classes".equalsIgnoreCase(cmd) && lectureModule.isEnabled() && coachingSec.isMasterCoachForLectures()) {
			if(lecturesMasterCoachCtrl == null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("Classes", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
				LecturesSecurityCallback secCallback = LecturesSecurityCallbackFactory.getSecurityCallback(false, true, false, LectureRoles.mastercoach, false);
				lecturesMasterCoachCtrl = new LecturesCoachingController(ureq, bwControl, content, secCallback);
				listenTo(lecturesMasterCoachCtrl);
			}
			selectedCtrl = lecturesMasterCoachCtrl;
		} else if("search".equalsIgnoreCase(cmd) && userSearchAllowed) {
			if(userSearchCtrl == null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
				userSearchCtrl = new UserSearchController(ureq, bwControl, content);
				listenTo(userSearchCtrl);
			}
			selectedCtrl = userSearchCtrl;
		} else if("grading".equalsIgnoreCase(cmd) && gradingModule.isEnabled() && (gradingSec.isGrader() || gradingSec.isGradedResourcesManager())) {
			if(gradingCtrl == null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("Grading", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
				GradingSecurityCallback secCallback = GradingSecurityCallbackFactory.getSecurityCalllback(getIdentity(), gradingSec);
				gradingCtrl = new GradingCoachingOverviewController(ureq, bwControl, content, secCallback);
				listenTo(gradingCtrl);
			}
			selectedCtrl = gradingCtrl;
		} else if(userRelationRolesMap.containsKey(cmd) && securityModule.isRelationRoleEnabled()) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(cmd, 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			UserRelationListController userRelationsListController = new UserRelationListController(ureq, bwControl, content, userRelationRolesMap.get(cmd));
			listenTo(userRelationsListController);

			selectedCtrl = userRelationsListController;
		}

		if(selectedCtrl != null) {
			String title = "Root";
			TreeNode selTreeNode = TreeHelper.findNodeByUserObject(cmd, menu.getTreeModel().getRootNode());
			if(selTreeNode != null) {
				title = selTreeNode.getTitle();
				if(!selTreeNode.getIdent().equals(menu.getSelectedNodeId())) {
					menu.setSelectedNodeId(selTreeNode.getIdent());
				}
			}
			content.rootController(title, selectedCtrl);
			addToHistory(ureq, selectedCtrl);
		}
		return (Activateable2)selectedCtrl;
	}

	private TreeModel buildTreeModel() {
		GenericTreeModel gtm = new GenericTreeModel();
		GenericTreeNode root = new GenericTreeNode();
		gtm.setRootNode(root);

		if(lectureModule.isEnabled()) {
			if(coachingSec.isTeacher()) {
				GenericTreeNode lecturesAsTeacher = new GenericTreeNode();
				lecturesAsTeacher.setUserObject("Lectures");
				lecturesAsTeacher.setTitle(translate("lectures.teacher.menu.title"));
				lecturesAsTeacher.setAltText(translate("lectures.teacher.menu.title.alt"));
				root.addChild(lecturesAsTeacher);
			}
			if(coachingSec.isMasterCoachForLectures()) {
				GenericTreeNode lecturesAsMastercoach = new GenericTreeNode();
				lecturesAsMastercoach.setUserObject("Classes");
				lecturesAsMastercoach.setTitle(translate("lectures.mastercoach.menu.title"));
				lecturesAsMastercoach.setAltText(translate("lectures.mastercoach.menu.title.alt"));
				root.addChild(lecturesAsMastercoach);
			}
		}

		if(coachingSec.isCoach()) {
			GenericTreeNode students = new GenericTreeNode();
			students.setUserObject("Members");
			students.setTitle(translate("students.menu.title"));
			students.setAltText(translate("students.menu.title.alt"));
			root.addChild(students);

			GenericTreeNode groups = new GenericTreeNode();
			groups.setUserObject("Groups");
			groups.setTitle(translate("groups.menu.title"));
			groups.setAltText(translate("groups.menu.title.alt"));
			root.addChild(groups);

			GenericTreeNode courses = new GenericTreeNode();
			courses.setUserObject("Courses");
			courses.setTitle(translate("courses.menu.title"));
			courses.setAltText(translate("courses.menu.title.alt"));
			root.addChild(courses);
		}

		// Add menu entry with sub entries
		if (isUserRelationAvailable() == 2) {
			GenericTreeNode relations = new GenericTreeNode();
			relations.setUserObject("UserRelationsNode");
			relations.setTitle(translate("relations.menu.title"));
			relations.setAltText("relations.menu.title");

			for (RelationRole relationRole : userRelationRolesMap.values()) {
				GenericTreeNode relationRoleNode = new GenericTreeNode();
				relationRoleNode.setUserObject(relationRole.getRole());
				relationRoleNode.setTitle(RelationRolesAndRightsUIFactory.getTranslatedContraRole(relationRole, getLocale()));
				relationRoleNode.setAltText(RelationRolesAndRightsUIFactory.getTranslatedContraDescription(relationRole, getLocale()));
				relations.addChild(relationRoleNode);
			}

			setFirstChildAsDelegate(relations);
			root.addChild(relations);
		}
		// Add one menu entry
		else if (isUserRelationAvailable() == 1) {
			for (RelationRole relationRole : userRelationRolesMap.values()) {
				GenericTreeNode relationRoleNode = new GenericTreeNode();
				relationRoleNode.setUserObject(relationRole.getRole());
				relationRoleNode.setTitle(RelationRolesAndRightsUIFactory.getTranslatedContraRole(relationRole, getLocale()));
				relationRoleNode.setAltText(RelationRolesAndRightsUIFactory.getTranslatedContraDescription(relationRole, getLocale()));
				root.addChild(relationRoleNode);
			}
		}

		if(gradingModule.isEnabled() && (gradingSec.isGrader() || gradingSec.isGradedResourcesManager())) {
			GenericTreeNode courses = new GenericTreeNode();
			courses.setUserObject("Grading");
			courses.setTitle(translate("grading.menu.title"));
			courses.setAltText(translate("grading.menu.title.alt"));
			root.addChild(courses);
		}

		if(userSearchAllowed) {
			GenericTreeNode search = new GenericTreeNode();
			search.setUserObject("Search");
			search.setTitle(translate("search.menu.title"));
			search.setAltText(translate("search.menu.title.alt"));
			root.addChild(search);
		}
		return gtm;
	}

	/**
	 * Returns 0 if nothing is available
	 * Returns 1 if exactly one role is available
	 * Returns 2 if more than one role is available
	 *
	 * @return
	 */
	private int isUserRelationAvailable() {
		if (securityModule.isRelationRoleEnabled()) {
			if (userRelationRolesMap != null && userRelationRolesMap.size() > 1) {
				return 2;
			} else if (userRelationRolesMap != null && userRelationRolesMap.size() == 1) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Returns different roles for a given list of relations
	 *
	 * @param relations
	 * @return
	 */
	private Map<String, RelationRole> listAvailableRoles(List<IdentityToIdentityRelation> relations) {
		Map<String, RelationRole> relationRoles = new HashMap<>();

		for (IdentityToIdentityRelation relation : relations) {
			// Prevent double entries
			if (relationRoles.get(relation.getRole().getRole()) == null) {
				// Add entry to list
				relationRoles.put(relation.getRole().getRole(), relation.getRole());
			}
		}

		return relationRoles;
	}

	/**
	 * Selects the first menu entry when clicking on the root
	 *
	 * @param node
	 */
	private void setFirstChildAsDelegate(INode node) {
		if (node.getChildCount() > 0) {
			INode childNode = node.getChildAt(0);
			if (node instanceof GenericTreeNode && childNode instanceof TreeNode) {
				GenericTreeNode parent = (GenericTreeNode) node;
				TreeNode child = (TreeNode) childNode;
				parent.setDelegate(child);
			}
		}
	}
}
