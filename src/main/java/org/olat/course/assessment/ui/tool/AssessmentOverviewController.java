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
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMainController;
import org.olat.course.assessment.IAssessmentCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentOverviewController extends MainLayoutBasicController implements Activateable2 {
	
	private MenuTree menuTree;
	private final Panel mainPanel;
	private TooledStackedPanel stackPanel;
	
	private boolean hasAssessableNodes;
	private RepositoryEntry courseEntry;
	private IAssessmentCallback assessmentCallback;
	
	public AssessmentOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, IAssessmentCallback assessmentCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentMainController.class, getLocale(), getTranslator()));
		this.courseEntry = courseEntry;
		this.stackPanel = stackPanel;
		this.assessmentCallback = assessmentCallback;
		
		ICourse course = CourseFactory.loadCourse(courseEntry.getOlatResource());
		
		hasAssessableNodes = course.hasAssessableNodes();
		boolean hasCertificates = course.getCourseConfig().isAutomaticCertificationEnabled()
				|| course.getCourseConfig().isManualCertificationEnabled();
		
		mainPanel = new Panel("assessmentToolv2");
		
		// Navigation menu
		menuTree = new MenuTree("menuTree");
		TreeModel tm = buildTreeModel(hasAssessableNodes, hasCertificates);
		menuTree.setTreeModel(tm);
		menuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		menuTree.addListener(this);

		LayoutMain3ColsController columLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainPanel, "course" + course.getResourceableId());
		listenTo(columLayoutCtr); // cleanup on dispose
		putInitialPanel(columLayoutCtr.getInitialComponent());
	}
	
	private TreeModel buildTreeModel(boolean assessableNodes, boolean certificate) {
		GenericTreeNode root, gtn;

		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode();
		root.setTitle(translate("menu.index"));
		root.setUserObject("index");
		root.setAltText(translate("menu.index.alt"));
		gtm.setRootNode(root);

		// show real menu only when there are some assessable nodes
		/*
		if (assessableNodes) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.groupfocus"));
			gtn.setUserObject("groups");
			gtn.setAltText(translate("menu.groupfocus.alt"));
			gtn.setCssClass("o_sel_assessment_tool_groups");
			root.addChild(gtn);
	
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.nodefocus"));
			gtn.setUserObject("courseNodes");
			gtn.setAltText(translate("menu.nodefocus.alt"));
			gtn.setCssClass("o_sel_assessment_tool_nodes");
			root.addChild(gtn);
		}
		*/
		
		if (assessableNodes || certificate) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.userfocus"));
			gtn.setUserObject("users");
			gtn.setAltText(translate("menu.userfocus.alt"));
			gtn.setCssClass("o_sel_assessment_tool_users");
			root.addChild(gtn);
		}

		return gtm;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				Object uo = menuTree.getSelectedNode().getUserObject();
				if("groups".equals(uo)) {
					doSelectGroupView();
				} else if("courseNodes".equals(uo)) {
					doSelectCourseNodesView();
				} else if("users".equals(uo)) {
					doSelectUsersView(ureq);
				}
			}
		}
	}
	
	private void doSelectGroupView() {
		
	}

	private void doSelectCourseNodesView() {
		
	}
	
	private void doSelectUsersView(UserRequest ureq) {
		AssessedIdentityListController listController = new AssessedIdentityListController(ureq, getWindowControl(), stackPanel,
				courseEntry, assessmentCallback);
		listenTo(listController);
		mainPanel.setContent(listController.getInitialComponent());
	}
	

}
