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
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.assessment.ui.AssessableResource;
import org.olat.modules.assessment.ui.AssessmentToolController;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.ui.model.AssessableBinderResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderRuntimeController extends RepositoryEntryRuntimeController {
	
	private Link assessmentLink, optionsLink;
	
	private AssessmentToolController assessmentToolCtrl;
	private BinderDeliveryOptionsController optionsCtrl;

	@Autowired
	private PortfolioService portfolioService;
	
	public BinderRuntimeController(UserRequest ureq, WindowControl wControl, RepositoryEntry re,
			RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}

	@Override
	protected void initRuntimeTools(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			membersLink = LinkFactory.createToolLink("members", translate("details.members"), this, "o_sel_repo_members");
			membersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_membersmanagement");
			toolsDropdown.addComponent(membersLink);
		}
		
		if (reSecurity.isEntryAdmin() || reSecurity.isCoach()) {
			assessmentLink = LinkFactory.createToolLink("assessment", translate("command.openassessment"), this, "o_icon_assessment_tool");
			assessmentLink.setElementCssClass("o_sel_course_assessment_tool");
			toolsDropdown.addComponent(assessmentLink);
		}
		
		if (reSecurity.isEntryAdmin()) {
			RepositoryEntry re = getRepositoryEntry();
			ordersLink = LinkFactory.createToolLink("bookings", translate("details.orders"), this, "o_sel_repo_booking");
			ordersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_booking");
			boolean booking = acService.isResourceAccessControled(re.getOlatResource(), null);
			ordersLink.setEnabled(booking);
			toolsDropdown.addComponent(ordersLink);	
		}
	}
	
	@Override
	protected void initSettingsTools(Dropdown settingsDropdown) {
		super.initSettingsTools(settingsDropdown);
		if (reSecurity.isEntryAdmin()) {
			settingsDropdown.addComponent(new Spacer(""));

			optionsLink = LinkFactory.createToolLink("options", translate("portfolio.template.options"), this, "o_sel_repo_options");
			optionsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_options");
			settingsDropdown.addComponent(optionsLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(assessmentLink == source) {
			doAssessmentTool(ureq);
		} else if(optionsLink == source) {
			doOptions(ureq);
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
		OLATResourceable ores = OresHelper.createOLATResourceableType("TestStatistics");
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
	
	private Activateable2 doOptions(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("Options");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		if (reSecurity.isEntryAdmin()) {
			Binder binder = portfolioService.getBinderByResource(getRepositoryEntry().getOlatResource());
			BinderDeliveryOptionsController ctrl = new BinderDeliveryOptionsController(ureq, swControl, binder);
			listenTo(ctrl);
			optionsCtrl = pushController(ureq, "Options", ctrl);
			currentToolCtr = optionsCtrl;
			setActiveTool(optionsLink);
			enableRuntimeNavBar(false);
			return optionsCtrl;
		}
		return null;
	}
	
	@Override
	protected void doAccess(UserRequest ureq) {
		super.doAccess(ureq);
		enableRuntimeNavBar(false);
	}

	@Override
	protected void doEdit(UserRequest ureq) {
		super.doEdit(ureq);
		enableRuntimeNavBar(false);
	}

	@Override
	protected void doDetails(UserRequest ureq) {
		super.doDetails(ureq);
		enableRuntimeNavBar(false);
	}

	@Override
	protected void doEditSettings(UserRequest ureq) {
		super.doEditSettings(ureq);
		enableRuntimeNavBar(false);
	}

	@Override
	protected void doCatalog(UserRequest ureq) {
		super.doCatalog(ureq);
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
	protected void launchContent(UserRequest ureq, RepositoryEntrySecurity security) {
		super.launchContent(ureq, security);
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
