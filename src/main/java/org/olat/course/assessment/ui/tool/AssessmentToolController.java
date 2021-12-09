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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.bulk.BulkAssessmentOverviewController;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeIdentityEvent;
import org.olat.course.config.ui.AssessmentResetController;
import org.olat.course.config.ui.AssessmentResetController.AssessmentResetEvent;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToolController extends MainLayoutBasicController implements Activateable2 {

	private final RepositoryEntry courseEntry;
	private final UserCourseEnvironment coachUserEnv;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	private Link usersLink;
	private Link overviewLink;
	private Link bulkAssessmentLink;
	private Link recalculateLink;
	private final TooledStackedPanel stackPanel;
	private final AssessmentToolContainer toolContainer;
	private final ButtonGroupComponent segmentButtonsCmp;

	private CloseableModalController cmc;
	private AssessmentCourseTreeController courseTreeCtrl;
	private AssessmentCourseOverviewController overviewCtrl;
	private BulkAssessmentOverviewController bulkAssessmentOverviewCtrl;
	private AssessmentResetController assessmentResetCtrl;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private AssessmentService assessmentService;
	
	public AssessmentToolController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, UserCourseEnvironment coachUserEnv, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		this.courseEntry = courseEntry;
		this.stackPanel = stackPanel;
		this.coachUserEnv = coachUserEnv;
		this.assessmentCallback = assessmentCallback;
		
		toolContainer = new AssessmentToolContainer();

		stackPanel.addListener(this);
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		
		overviewCtrl = new AssessmentCourseOverviewController(ureq, getWindowControl(), courseEntry, coachUserEnv, assessmentCallback);
		listenTo(overviewCtrl);
		putInitialPanel(overviewCtrl.getInitialComponent());
	}
	
	public void initToolbar() {
		overviewLink = LinkFactory.createToolLink("overview", translate("overview"), this/*, "o_icon_user"*/);
		overviewLink.setElementCssClass("o_sel_assessment_tool_overview");
		segmentButtonsCmp.addButton(overviewLink, true);
		
		usersLink = LinkFactory.createToolLink("users", translate("details"), this/*, "o_icon_user"*/);
		usersLink.setElementCssClass("o_sel_assessment_tool_users");
		segmentButtonsCmp.addButton(usersLink, false);
		
		stackPanel.addTool(segmentButtonsCmp, Align.segment, true);
		
		recalculateLink = LinkFactory.createToolLink("recalculate", translate("menu.recalculate"), this, "o_icon_recalculate");
		stackPanel.addTool(recalculateLink, Align.right);
		
		bulkAssessmentLink = LinkFactory.createToolLink("bulkAssessment", translate("menu.bulkfocus"), this, "o_icon_group");
		bulkAssessmentLink.setElementCssClass("o_sel_assessment_tool_bulk");
		stackPanel.addTool(bulkAssessmentLink, Align.right);
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
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Node".equalsIgnoreCase(resName)) {
			doSelectUsersView(ureq, null).activate(ureq, entries, null);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (overviewLink == source) {
			cleanUp();
			stackPanel.popUpToController(this);
			addToHistory(ureq, getWindowControl());
		} else if (source == usersLink) {
			cleanUp();
			doSelectUsersView(ureq, null);
		} else if(recalculateLink == source) {
			cleanUp();
			doOpenRecalculate(ureq);
		} else if(bulkAssessmentLink == source) {
			cleanUp();
			doBulkAssessmentView(ureq);
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.isClose()) {
					stackPanel.popUpToRootController(ureq);
				} else if(pe.getController() == courseTreeCtrl) {
					removeAsListenerAndDispose(courseTreeCtrl);
					courseTreeCtrl = null;
					segmentButtonsCmp.setSelectedButton(overviewLink);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(overviewCtrl == source) {
			if(event == AssessmentCourseOverviewController.SELECT_USERS_EVENT) {
				doSelectUsersView(ureq, null);
			} else if(event == AssessmentCourseOverviewController.SELECT_NODES_EVENT) {
				doSelectUsersView(ureq, null);
			} else if(event == AssessmentCourseOverviewController.SELECT_PASSED_EVENT) {
				doSelectUsersView(ureq, new AssessedIdentityListState(null, Collections.singletonList("passed"), null, null, null, "Passed", false));
			} else if(event == AssessmentCourseOverviewController.SELECT_FAILED_EVENT) {
				doSelectUsersView(ureq, new AssessedIdentityListState(null, Collections.singletonList("failed"), null, null, null, "Failed", false));
			} else if (event instanceof CourseNodeIdentityEvent) {
				CourseNodeIdentityEvent cnie = (CourseNodeIdentityEvent)event;
				if (StringHelper.isLong(cnie.getCourseNodeIdent())) {
					if(cnie.getAssessedIdentity() == null) {
						OLATResourceable resource = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(cnie.getCourseNodeIdent()));
						List<ContextEntry> entries = BusinessControlFactory.getInstance()
								.createCEListFromResourceable(resource, cnie.getFilter().get());
						doSelectNodeView(ureq, cnie.getCourseNodeIdent()).activate(ureq, entries, null);
					} else {
						OLATResourceable nodeRes = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(cnie.getCourseNodeIdent()));
						OLATResourceable idRes = OresHelper.createOLATResourceableInstance("Identity", cnie.getAssessedIdentity().getKey());
						List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(nodeRes, idRes);
						doSelectNodeView(ureq, cnie.getCourseNodeIdent()).activate(ureq, entries, null);
					}
				}
			} else if(event instanceof CourseNodeEvent) {
				CourseNodeEvent cne = (CourseNodeEvent)event;
				if(cne.getIdent() != null) {
					doSelectNodeView(ureq, cne.getIdent());
				}
			} else if(event instanceof AssessmentModeStatusEvent) {
				fireEvent(ureq, event);
			}
		} else if(source == assessmentResetCtrl) {
			if (event instanceof AssessmentResetEvent) {
				AssessmentResetEvent are = (AssessmentResetEvent)event;
				doRecalculate(are);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(bulkAssessmentOverviewCtrl);
		removeAsListenerAndDispose(assessmentResetCtrl);
		removeAsListenerAndDispose(cmc);
		bulkAssessmentOverviewCtrl = null;
		assessmentResetCtrl = null;
		cmc = null;
	}
	
	public void reloadAssessmentModes() {
		overviewCtrl.reloadAssessmentModes();
	}
	
	private void doBulkAssessmentView(UserRequest ureq) {
		boolean canChangeUserVisibility = coachUserEnv.isAdmin()
				|| coachUserEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		bulkAssessmentOverviewCtrl = new BulkAssessmentOverviewController(ureq, getWindowControl(), courseEntry, canChangeUserVisibility);
		listenTo(bulkAssessmentOverviewCtrl);
		stackPanel.pushController(translate("menu.bulkfocus"), bulkAssessmentOverviewCtrl);
	}
	
	private void doOpenRecalculate(UserRequest ureq) {
		boolean showResetOverriden = !nodeAccessService
				.isScoreCalculatorSupported(coachUserEnv.getCourseEnvironment().getCourseConfig().getNodeAccessType());
		assessmentResetCtrl = new AssessmentResetController(ureq, getWindowControl(), showResetOverriden, false);
		listenTo(assessmentResetCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				assessmentResetCtrl.getInitialComponent(), true, translate("assessment.reset.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRecalculate(AssessmentResetEvent are) {
		if (are.isResetOverriden()) {
			assessmentService.resetAllOverridenRootPassed(courseEntry);
		}
		if (are.isResetPassed()) {
			assessmentService.resetAllRootPassed(courseEntry);
		}
		if (are.isRecalculateAll()) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			courseAssessmentService.evaluateAll(course);
		}
	}

	private AssessmentCourseTreeController doSelectUsersView(UserRequest ureq, StateEntry stateUserList) {
		if(courseTreeCtrl == null || courseTreeCtrl.isDisposed()) {
			stackPanel.popUpToController(this);
			
			courseTreeCtrl = new AssessmentCourseTreeController(ureq, getWindowControl(), stackPanel, courseEntry, coachUserEnv, toolContainer, assessmentCallback);
			listenTo(courseTreeCtrl);
			TreeNode node = courseTreeCtrl.getSelectedCourseNode();
			stackPanel.pushController(node.getTitle(), "o_icon " + node.getIconCssClass(), courseTreeCtrl);
		}
		courseTreeCtrl.switchToUsersView(ureq, stateUserList);
		segmentButtonsCmp.setSelectedButton(usersLink);
		return courseTreeCtrl;
	}
	
	private AssessmentCourseTreeController doSelectNodeView(UserRequest ureq, String nodeIdent) {
		if(courseTreeCtrl == null || courseTreeCtrl.isDisposed()) {
			stackPanel.popUpToController(this);
			
			courseTreeCtrl = new AssessmentCourseTreeController(ureq, getWindowControl(), stackPanel, courseEntry, coachUserEnv, toolContainer, assessmentCallback);
			listenTo(courseTreeCtrl);
			OLATResourceable nodeRes = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(nodeIdent));
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(nodeRes);
			courseTreeCtrl.activate(ureq, entries, null);
			
			TreeNode node = courseTreeCtrl.getSelectedCourseNode();
			stackPanel.pushController(node.getTitle(), "o_icon " + node.getIconCssClass(), courseTreeCtrl);
		}
		courseTreeCtrl.switchToUsersView(ureq, null);
		segmentButtonsCmp.setSelectedButton(usersLink);
		return courseTreeCtrl;
	}
}
