/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramDetailsController extends BasicController implements Activateable2 {
	
	private int membersTab;
	private int overviewTab;
	private int settingsTab;
	private int ownersTab;
	private int messagesTab;
	private int implementationTab;

	private Dropdown statusDropdown;
	private final TabbedPane tabPane;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private boolean canChangeStatus;
	private CertificationProgram certificationProgram;
	private final CertificationProgramSecurityCallback secCallback;
	
	private CertificationProgramOwnersController ownersCtrl;
	private EditCertificationProgramController settingsCtrl;
	private CertificationProgramMessagesController messagesCtrl;
	private CertificationProgramMembersOverviewController membersCtrl;
	private CertificationProgramDashboardController overviewCtrl;
	private CertificationProgramCurriculumElementListController curriculumElementsListCtrl;
	
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificationProgramDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CertificationProgram certificationProgram, CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		this.certificationProgram = certificationProgram;
		canChangeStatus = certificationProgram != null; //TODO certification
		
		mainVC = createVelocityContainer("program_details");
		tabPane = new TabbedPane("tabs", getLocale());
		tabPane.addListener(this);
		initTabPane(ureq);
		initMetadata();
		
		mainVC.put("tabs", tabPane);
		putInitialPanel(mainVC);
	}
	
	private void initMetadata() {
		statusDropdown = new Dropdown("status", null, false, getTranslator());
		statusDropdown.setOrientation(DropdownOrientation.right);
		statusDropdown.setEmbbeded(true);
		statusDropdown.setLabeled(true, true);
		mainVC.put("status", statusDropdown);
		updateStatusDropdown();

		mainVC.contextPut("displayName", certificationProgram.getDisplayName());
	}
	
	private void updateStatusDropdown() {
		CertificationProgramStatusEnum currentStatus = certificationProgram.getStatus();
		statusDropdown.setIconCSS("o_icon o_icon_certification_status_" + currentStatus);
		statusDropdown.setInnerText(translate("certification.status." + currentStatus));
		statusDropdown.setToggleCSS("o_labeled o_certification_status_" + currentStatus);
		statusDropdown.removeAllComponents();
		
		if (!canChangeStatus) {
			return;
		}
		
		for (CertificationProgramStatusEnum programStatus : CertificationProgramStatusEnum.values()) {
			Link statusLink = LinkFactory.createCustomLink("certification.status." + programStatus, "status", "certification.status." + programStatus, Link.LINK, mainVC, this);
			statusLink.setIconLeftCSS("o_icon o_icon-fw o_icon_certification_status_" + programStatus);
			statusLink.setElementCssClass("o_labeled o_certification_status_" + programStatus);
			statusLink.setUserObject(programStatus);
			statusDropdown.addComponent(statusLink);
		}
	}
	
	private void initTabPane(UserRequest ureq) {
		overviewTab = tabPane.addTab(ureq, translate("certification.program.overview"), uureq -> createDashBoard(uureq).getInitialComponent());
		
		implementationTab = tabPane.addTab(ureq, translate("certification.program.implementations"), "o_sel_certification_program_implementations", uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CertificationProgramListController.CONTEXT_ELEMENTS), null);
			curriculumElementsListCtrl = new CertificationProgramCurriculumElementListController(uureq, subControl, certificationProgram);
			listenTo(curriculumElementsListCtrl);

			return curriculumElementsListCtrl.getInitialComponent();
		}, false);
		
		membersTab = tabPane.addTab(ureq, translate("certification.program.members"), "o_sel_certification_program_members", uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CertificationProgramListController.CONTEXT_MEMBERS), null);
			membersCtrl = new CertificationProgramMembersOverviewController(uureq, subControl, toolbarPanel, certificationProgram, secCallback);
			listenTo(membersCtrl);

			return membersCtrl.getInitialComponent();
		}, true);
		
		messagesTab = tabPane.addTab(ureq, translate("certification.program.messages"), "o_sel_certification_program_messages", uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CertificationProgramListController.CONTEXT_MESSAGES), null);
			messagesCtrl = new CertificationProgramMessagesController(uureq, subControl, certificationProgram);
			listenTo(messagesCtrl);

			return messagesCtrl.getInitialComponent();
		}, true);
		
		ownersTab = tabPane.addTab(ureq, translate("certification.program.owners"), "o_sel_certification_program_owners", uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CertificationProgramListController.CONTEXT_OWNERS), null);
			ownersCtrl = new CertificationProgramOwnersController(uureq, subControl, certificationProgram);
			listenTo(ownersCtrl);

			return ownersCtrl.getInitialComponent();
		}, true);
		
		settingsTab = tabPane.addTab(ureq, translate("certification.program.settings"), "o_sel_certification_program_settings", uureq -> {
			WindowControl subControl = addToHistory(uureq, OresHelper
					.createOLATResourceableType(CertificationProgramListController.CONTEXT_SETTINGS), null);
			settingsCtrl = new EditCertificationProgramController(uureq, subControl, certificationProgram);
			listenTo(settingsCtrl);

			return settingsCtrl.getInitialComponent();
		}, false);
		
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(CertificationProgramListController.CONTEXT_ELEMENTS.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			tabPane.setSelectedPane(ureq, implementationTab);
			if(curriculumElementsListCtrl != null) {
				curriculumElementsListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if(CertificationProgramListController.CONTEXT_MEMBERS.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			tabPane.setSelectedPane(ureq, membersTab);
			if(membersCtrl != null) {
				membersCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if(CertificationProgramListController.CONTEXT_MESSAGES.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			tabPane.setSelectedPane(ureq, messagesTab);
			if(messagesCtrl != null) {
				messagesCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if(CertificationProgramListController.CONTEXT_SETTINGS.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			tabPane.setSelectedPane(ureq, settingsTab);
			if(settingsCtrl != null) {
				settingsCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if(CertificationProgramListController.CONTEXT_OVERVIEW.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, overviewTab);
		} else if(CertificationProgramListController.CONTEXT_OWNERS.equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, ownersTab);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			if ("status".equals(link.getCommand())
					&& link.getUserObject() instanceof CertificationProgramStatusEnum status) {
				doChangeStatus(ureq, status);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(settingsCtrl == source) {
			certificationProgram = settingsCtrl.getCertificationProgram();
			toolbarPanel.changeDisplayname(certificationProgram.getDisplayName());
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.event(ureq, source, event);
	}
	

	private void doChangeStatus(UserRequest ureq, CertificationProgramStatusEnum newStatus) {
		certificationProgram = certificationProgramService.getCertificationProgram(certificationProgram);
		if (certificationProgram == null) {
			showWarning("warning.certification.program.deleted");
			return;
		} else if (certificationProgram.getStatus() == newStatus) {
			updateStatusDropdown();
			return;
		}
		
		certificationProgram.setStatus(newStatus);
		certificationProgram = certificationProgramService.updateCertificationProgram(certificationProgram);
		updateStatusDropdown();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private CertificationProgramDashboardController createDashBoard(UserRequest ureq) {
		WindowControl subControl = addToHistory(ureq, OresHelper
				.createOLATResourceableType(CertificationProgramListController.CONTEXT_OVERVIEW), null);
		overviewCtrl = new CertificationProgramDashboardController(ureq, subControl);
		listenTo(overviewCtrl);
		
		return overviewCtrl;
	}
}
