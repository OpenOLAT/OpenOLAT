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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.EfficiencyStatementAssessmentController;
import org.olat.course.assessment.bulk.BulkAssessmentOverviewController;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.UserSelectionEvent;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToolController extends MainLayoutBasicController implements Activateable2 {


	private RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	private Link usersLink, efficiencyStatementsLink, bulkAssessmentLink;
	private final TooledStackedPanel stackPanel;
	private final AssessmentToolContainer toolContainer;
	
	private AssessedBusinessGroupListController groupsCtrl;
	private AssessmentCourseOverviewController overviewCtrl;
	private AssessmentIdentityListCourseTreeController currentCtrl;
	private BulkAssessmentOverviewController bulkAssessmentOverviewCtrl;
	private EfficiencyStatementAssessmentController efficiencyStatementCtrl;
	
	public AssessmentToolController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		this.courseEntry = courseEntry;
		this.stackPanel = stackPanel;
		this.assessmentCallback = assessmentCallback;
		toolContainer = new AssessmentToolContainer();
		
		overviewCtrl = new AssessmentCourseOverviewController(ureq, getWindowControl(), courseEntry, assessmentCallback);
		listenTo(overviewCtrl);
		putInitialPanel(overviewCtrl.getInitialComponent());
	}
	
	public void initToolbar() {
		usersLink = LinkFactory.createToolLink("users", translate("users"), this, "o_icon_user");
		usersLink.setElementCssClass("o_sel_assessment_tool_users");
		stackPanel.addTool(usersLink);
		
		efficiencyStatementsLink = LinkFactory.createToolLink("efficiencyStatements", translate("menu.efficiency.statment"), this, "o_icon_certificate");
		efficiencyStatementsLink.setElementCssClass("o_sel_assessment_tool_efficiency_statements");
		stackPanel.addTool(efficiencyStatementsLink, Align.right);
		
		bulkAssessmentLink = LinkFactory.createToolLink("bulkAssessment", translate("menu.bulkfocus"), this, "o_icon_group");
		bulkAssessmentLink.setElementCssClass("o_sel_assessment_tool_bulk");
		stackPanel.addTool(bulkAssessmentLink, Align.right);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.size() == 0) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Users".equalsIgnoreCase(resName)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doSelectUsersView(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("BusinessGroups".equalsIgnoreCase(resName) || "Groups".equalsIgnoreCase(resName)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doSelectGroupsView(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == usersLink) {
			cleanUp();
			doSelectUsersView(ureq);
		} else if(efficiencyStatementsLink == source) {
			cleanUp();
			doEfficiencyStatementView(ureq);
		} else if(bulkAssessmentLink == source) {
			cleanUp();
			doBulkAssessmentView(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(overviewCtrl == source) {
			if(event == AssessmentCourseOverviewController.SELECT_USERS_EVENT) {
				doSelectUsersView(ureq);
			} else if(event == AssessmentCourseOverviewController.SELECT_GROUPS_EVENT) {
				doSelectGroupsView(ureq);
			} else if(event == AssessmentCourseOverviewController.SELECT_PASSED_EVENT) {
				doSelectPassedView(ureq);
			} else if(event == AssessmentCourseOverviewController.SELECT_FAILED_EVENT) {
				doSelectFailedView(ureq);
			} else if (event instanceof UserSelectionEvent) {
				UserSelectionEvent use = (UserSelectionEvent)event;
				if(use.getCourseNodeIdents() == null || use.getCourseNodeIdents().isEmpty() || use.getCourseNodeIdents().size() > 1) {
					OLATResourceable resource = OresHelper.createOLATResourceableInstance("Identity", use.getIdentityKey());
					List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(resource);
					doSelectUsersView(ureq).activate(ureq, entries, null);
				} else {
					OLATResourceable nodeRes = OresHelper.createOLATResourceableInstance("Node", new Long(use.getCourseNodeIdents().get(0)));
					OLATResourceable idRes = OresHelper.createOLATResourceableInstance("Identity", use.getIdentityKey());
					List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(nodeRes, idRes);
					doSelectUsersView(ureq).activate(ureq, entries, null);
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(bulkAssessmentOverviewCtrl);
		removeAsListenerAndDispose(currentCtrl);
		bulkAssessmentOverviewCtrl = null;
		currentCtrl = null;
	}
	
	private void doBulkAssessmentView(UserRequest ureq) {
		bulkAssessmentOverviewCtrl = new BulkAssessmentOverviewController(ureq, getWindowControl(), courseEntry);
		listenTo(bulkAssessmentOverviewCtrl);
		stackPanel.pushController(translate("menu.bulkfocus"), bulkAssessmentOverviewCtrl);
	}
	
	private void doEfficiencyStatementView(UserRequest ureq) {
		efficiencyStatementCtrl = new EfficiencyStatementAssessmentController(ureq, getWindowControl(), courseEntry);
		listenTo(efficiencyStatementCtrl);
		stackPanel.pushController(translate("menu.efficiency.statment"), efficiencyStatementCtrl);
	}
	

	private AssessedBusinessGroupListController doSelectGroupsView(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("BusinessGroups", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		groupsCtrl = new AssessedBusinessGroupListController(ureq, bwControl, stackPanel, courseEntry, toolContainer, assessmentCallback);
		listenTo(groupsCtrl);
		stackPanel.pushController(translate("groups"), groupsCtrl);
		groupsCtrl.activate(ureq, null, null);
		return groupsCtrl;
	}

	private AssessmentIdentityListCourseTreeController doSelectUsersView(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Users", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		AssessmentIdentityListCourseTreeController treeCtrl = new AssessmentIdentityListCourseTreeController(ureq, bwControl, stackPanel,
				courseEntry, null, toolContainer, assessmentCallback);
		listenTo(treeCtrl);
		stackPanel.pushController(translate("users"), treeCtrl);
		currentCtrl = treeCtrl;
		treeCtrl.activate(ureq, null, null);
		return treeCtrl;
	}
	
	private void doSelectPassedView(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Passed", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		AssessmentIdentityListCourseTreeController treeCtrl = new AssessmentIdentityListCourseTreeController(ureq, bwControl, stackPanel,
				courseEntry, null, toolContainer, assessmentCallback);
		listenTo(treeCtrl);
		stackPanel.pushController(translate("users"), treeCtrl);
		currentCtrl = treeCtrl;
		
		AssessedIdentityListState state = new AssessedIdentityListState();
		state.setFilter("passed");
		treeCtrl.activate(ureq, null, state);
	}
	
	private void doSelectFailedView(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("NotPassed", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		AssessmentIdentityListCourseTreeController treeCtrl = new AssessmentIdentityListCourseTreeController(ureq, bwControl, stackPanel,
				courseEntry, null, toolContainer, assessmentCallback);
		listenTo(treeCtrl);
		stackPanel.pushController(translate("users"), treeCtrl);
		currentCtrl = treeCtrl;
		
		AssessedIdentityListState state = new AssessedIdentityListState();
		state.setFilter("failed");
		treeCtrl.activate(ureq, null, state);
	}
}
