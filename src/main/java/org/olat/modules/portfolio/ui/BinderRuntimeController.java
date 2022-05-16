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
package org.olat.modules.portfolio.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.assessment.ui.AssessableResource;
import org.olat.modules.assessment.ui.AssessmentToolController;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.portfolio.ui.model.AssessableBinderResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.model.SingleRoleRepositoryEntrySecurity.Role;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.RepositoryEntrySettingsController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderRuntimeController extends RepositoryEntryRuntimeController {
	
	private Link assessmentLink;
	
	private AssessmentToolController assessmentToolCtrl;
	
	public BinderRuntimeController(UserRequest ureq, WindowControl wControl, RepositoryEntry re,
			RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}
	
	@Override
	protected void initToolsMenuEditor(Dropdown toolsDropdown) {
		if (reSecurity.getWrappedSecurity().isEntryAdmin() && reSecurity.getWrappedSecurity().isParticipant()) {
			super.initToolsMenuEditor(toolsDropdown);
		}
	}

	@Override
	protected void initToolsMenuRuntime(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin() || reSecurity.isCoach()) {
			toolsDropdown.addComponent(new Spacer(""));
			
			assessmentLink = LinkFactory.createToolLink("assessment", translate("command.openassessment"), this, "o_icon_assessment_tool");
			assessmentLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[AssessmentTool:0]]"));
			assessmentLink.setElementCssClass("o_sel_course_assessment_tool");
			toolsDropdown.addComponent(assessmentLink);
		}
		
		if (reSecurity.isEntryAdmin()) {
			RepositoryEntry re = getRepositoryEntry();
			ordersLink = LinkFactory.createToolLink("bookings", translate("details.orders"), this, "o_sel_repo_booking");
			ordersLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Booking:0]]"));
			ordersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_booking");
			boolean booking = re.isPublicVisible() && acService.isResourceAccessControled(re.getOlatResource(), null);
			ordersLink.setEnabled(booking);
			toolsDropdown.addComponent(ordersLink);	
		}
		
		Controller runner = getRuntimeController();
		if(runner instanceof BinderController) {
			((BinderController)runner).setSegmentButtonsVisible(true);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		entries = removeRepositoryEntry(entries);
		if(entries == null  || entries.isEmpty()) {
			if(reSecurity.isEntryAdmin()) {
				doEdit(ureq);
			}
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("AssessmentTool".equalsIgnoreCase(type) && assessmentLink != null && assessmentLink.isVisible()) {
				activateSubEntries(ureq, doAssessmentTool(ureq), entries);
			}
		}
		super.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(assessmentLink == source) {
			doAssessmentTool(ureq);
		} else if(source == toolbarPanel) {
			if(event instanceof PopEvent) {
				if(toolbarPanel.getRootController() == getRuntimeController()) {
					enableRuntimeNavBar(true);
				}
			}
		}
		super.event(ureq, source, event);
	}

	private Activateable2 doAssessmentTool(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("AssessmentTool");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		if (reSecurity.isEntryAdmin() || reSecurity.isCoach()) {
			AssessmentToolSecurityCallback secCallback
				= new AssessmentToolSecurityCallback(reSecurity.isEntryAdmin(), reSecurity.isEntryAdmin(),
						reSecurity.isCourseCoach(), reSecurity.isGroupCoach(), reSecurity.isCurriculumCoach(), null);

			AssessableResource el = getAssessableElement();
			AssessmentToolController ctrl = new AssessmentToolController(ureq, swControl, toolbarPanel,
					getRepositoryEntry(), el, secCallback);
			listenTo(ctrl);
			assessmentToolCtrl = pushController(ureq, "Statistics", ctrl);
			currentToolCtr = assessmentToolCtrl;
			setActiveTool(assessmentLink);
			enableRuntimeNavBar(false);
			return assessmentToolCtrl;
		}
		return null;
	}
	
	@Override
	protected void doSwitchRole(UserRequest ureq, Role role) {
		super.doSwitchRole(ureq, role);
		
		launchContent(ureq);
		initToolbar();
		if(getRuntimeController() instanceof BinderController) {
			((BinderController)getRuntimeController()).activate(ureq, null, null);
		}
	}
	
	@Override
	protected Activateable2 doSettings(UserRequest ureq, List<ContextEntry> entries) {
		Activateable2 ctrl = super.doSettings(ureq, entries);
		enableRuntimeNavBar(false);
		return ctrl;
	}

	@Override
	protected RepositoryEntrySettingsController createSettingsController(UserRequest ureq, WindowControl bwControl, RepositoryEntry refreshedEntry) {
		return new BinderSettingsController(ureq, addToHistory(ureq, bwControl), toolbarPanel, refreshedEntry);
	}

	@Override
	protected void doEdit(UserRequest ureq) {
		super.doEdit(ureq);
		if(editorCtrl instanceof BinderController) {
			BinderController binderCtrl = (BinderController)editorCtrl;
			binderCtrl.activate(ureq, null, null);
		}
		enableRuntimeNavBar(true);
	}

	@Override
	protected void doDetails(UserRequest ureq) {
		super.doDetails(ureq);
		enableRuntimeNavBar(false);
	}

	@Override
	protected Activateable2 doMembers(UserRequest ureq) {
		Activateable2 controller = super.doMembers(ureq);
		enableRuntimeNavBar(false);
		return controller;
	}

	@Override
	protected void doOrders(UserRequest ureq) {
		super.doOrders(ureq);
		enableRuntimeNavBar(false);
	}

	@Override
	protected void launchContent(UserRequest ureq) {
		super.launchContent(ureq);
		enableRuntimeNavBar(true); 
	}

	private AssessableResource getAssessableElement() {
		boolean hasScore = false;
		boolean hasPassed = true;
		return new AssessableBinderResource(hasScore, hasPassed, true, true, null, null, null);
	}
	
	private void enableRuntimeNavBar(boolean enabled) {
		Controller runner = getRuntimeController();
		if(runner instanceof BinderController) {
			((BinderController)runner).setSegmentButtonsVisible(enabled);
		}
	}
}
