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
package org.olat.modules.grading.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.grading.GraderStatus;
import org.olat.modules.grading.GradingSecurityCallback;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.model.GraderWithStatistics;
import org.olat.modules.grading.model.GradersSearchParameters;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters.SearchStatus;
import org.olat.modules.grading.ui.GradersListTableModel.GradersCol;
import org.olat.modules.grading.ui.component.GraderAbsenceLeaveCellRenderer;
import org.olat.modules.grading.ui.component.GraderMailTemplate;
import org.olat.modules.grading.ui.component.GraderStatusCellRenderer;
import org.olat.modules.grading.ui.confirmation.ConfirmDeactivationGraderController;
import org.olat.modules.grading.ui.event.OpenAssignmentsEvent;
import org.olat.modules.grading.ui.wizard.ImportGrader1ChooseMemberStep;
import org.olat.modules.grading.ui.wizard.ImportGradersContext;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.absenceleave.CreateAbsenceLeaveController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradersListController extends FormBasicController {
	
	public static final String USER_PROPS_ID = GradersListController.class.getCanonicalName();

	public static final int USER_PROPS_OFFSET = 500;
	
	private FormLink addGraderButton;
	private FlexiTableElement tableEl;
	private GradersListTableModel tableModel;
	
	private int counter = 0;
	private final RepositoryEntry referenceEntry;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	private final GradingSecurityCallback secCallback;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private GradersSearchController searchCtrl;
	private ReportCalloutController reportCtrl;
	private ContactFormController contactGraderCtrl;
	private StepsMainRunController importGradersWizard;
	private CreateAbsenceLeaveController addAbsenceLeaveCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CloseableCalloutWindowController reportCalloutCtrl;
	private ConfirmDeactivationGraderController confirmRemoveCtrl;
	private ConfirmDeactivationGraderController confirmDeactivationCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public GradersListController(UserRequest ureq, WindowControl wControl, GradingSecurityCallback secCallback) {
		this(ureq, wControl, null, secCallback);

		searchCtrl = new GradersSearchController(ureq, getWindowControl(), mainForm);
		listenTo(searchCtrl);
		flc.add("search", searchCtrl.getInitialFormItem());
	}
	
	public GradersListController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry referenceEntry, GradingSecurityCallback secCallback) {
		super(ureq, wControl, "graders_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.secCallback = secCallback;
		this.referenceEntry = referenceEntry;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addGraderButton = uifactory.addFormLink("add.grader", formLayout, Link.BUTTON);
		addGraderButton.setIconLeftCSS("o_icon o_icon_add_item");
		addGraderButton.setElementCssClass("o_sel_repo_grading_add_graders");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, "select_grader", true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradersCol.status, new GraderStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradersCol.total, "total"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradersCol.done, "done"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradersCol.open, "open"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradersCol.overdue, "overdue"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradersCol.oldestOpenAssignment));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradersCol.recordedMetadataTime));
		if(secCallback.canViewRecordedRealMinutes()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradersCol.recordedTime));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradersCol.absence, new GraderAbsenceLeaveCellRenderer(getTranslator())));
		
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(GradersCol.tools);
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsCol.setHeaderLabel(translate("table.header.tools"));
		toolsCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new GradersListTableModel(columnsModel, userPropertyHandlers, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "graders", tableModel, 24, false, getTranslator(), formLayout);
	}
	
	protected void updateModel() {
		loadModel(false);
	}

	private void loadModel(boolean reload) {
		GradersSearchParameters searchParams = getSearchParameters(); 
		List<GraderWithStatistics> rawGraders = gradingService.getGradersWithStatistics(searchParams);
		List<GraderRow> rows = new ArrayList<>(rawGraders.size());
		for(GraderWithStatistics rawGrader:rawGraders) {
			rows.add(forgeRow(rawGrader));
		}
		tableModel.setObjects(rows);
		tableEl.reset(reload, reload, true);
	}
	
	private GraderRow forgeRow(GraderWithStatistics rawGrader) {
		GraderRow row = new GraderRow(rawGrader.getGrader(), rawGrader.getStatistics(),
				rawGrader.getRecordedTimeInSeconds(), rawGrader.getRecordedMetadataTimeInSeconds(),
				rawGrader.getAbsenceLeaves(), rawGrader.getGraderStatus());
		// tools
		String linkName = "tools-" + counter++;
		FormLink toolsLink = uifactory.addFormLink(linkName, "tools", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setUserObject(row);
		flc.add(linkName, toolsLink);
		row.setToolsLink(toolsLink);
		return row;
	}
	
	private GradersSearchParameters getSearchParameters() {
		GradersSearchParameters searchParams = new GradersSearchParameters();
		if (searchCtrl != null) {
			searchParams.setGradingFrom(searchCtrl.getGradingFrom());
			searchParams.setGradingTo(searchCtrl.getGradingTo());
			searchParams.setStatus(searchCtrl.getGraderStatus());
			searchParams.setReferenceEntry(searchCtrl.getReferenceEntry());
			searchParams.setGrader(searchCtrl.getGrader());
		}

		if(referenceEntry != null) {
			searchParams.setReferenceEntry(referenceEntry);
			searchParams.setManager(null);
		} else {
			// limit by role
			searchParams.setManager(getIdentity());
		}
		return searchParams;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(importGradersWizard == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				cleanUp();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel(true);
				}
			}
		} else if(cmc == source || toolsCalloutCtrl == source || contactGraderCtrl == source) {
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if(confirmDeactivationCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT) {
				if(event == Event.DONE_EVENT) {
					loadModel(false);
				}
				cmc.deactivate();
				cleanUp();
			}
		} else if(confirmRemoveCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT) {
				if(event == Event.DONE_EVENT) {
					loadModel(true);
				}
				cmc.deactivate();
				cleanUp();
			}
		} else if(searchCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT) {
				loadModel(true);
			}
		} else if(addAbsenceLeaveCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel(true);
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeactivationCtrl);
		removeAsListenerAndDispose(importGradersWizard);
		removeAsListenerAndDispose(contactGraderCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeactivationCtrl = null;
		importGradersWizard = null;
		contactGraderCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addGraderButton == source) {
			doAddGrader(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd())) {
				doOpenTools(ureq, (GraderRow)link.getUserObject(), link);
			}
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("total".equals(se.getCommand())) {
					doShowAssignments(ureq, tableModel.getObject(se.getIndex()), null);
				} else if("done".equals(se.getCommand())) {
					doShowAssignments(ureq, tableModel.getObject(se.getIndex()), SearchStatus.closed);
				} else if("open".equals(se.getCommand())) {
					doShowAssignments(ureq, tableModel.getObject(se.getIndex()), SearchStatus.open);
				} else if("overdue".equals(se.getCommand())) {
					doShowAssignments(ureq, tableModel.getObject(se.getIndex()), SearchStatus.deadlineMissed);
				} else if("select_grader".equals(se.getCommand())) {
					doShowAssignments(ureq, tableModel.getObject(se.getIndex()), null);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddGrader(UserRequest ureq) {
		removeAsListenerAndDispose(importGradersWizard);

		final ImportGradersContext graders = new ImportGradersContext(referenceEntry);
		GraderMailTemplate mailTemplate = GraderMailTemplate.graderTo(getTranslator(), null, null, referenceEntry);
		
		Step start = new ImportGrader1ChooseMemberStep(ureq, graders, mailTemplate, referenceEntry == null);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			List<Identity> futureGraders = graders.getGraders();
			if(!futureGraders.isEmpty()) {
				MailerResult result = new MailerResult();
				GraderMailTemplate sendTemplate = graders.isSendEmail() ? mailTemplate : null;
				gradingService.addGraders(graders.getEntry(), futureGraders, sendTemplate, result);
				if(mailTemplate.getAttachmentsTmpDir() != null) {
					FileUtils.deleteDirsAndFiles(mailTemplate.getAttachmentsTmpDir(), true, true);
				}
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importGradersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.grader"), "o_sel_repo_add_grader_1_wizard");
		listenTo(importGradersWizard);
		getWindowControl().pushAsModalDialog(importGradersWizard.getInitialComponent());
	}
	
	private void doConfirmDeactivate(UserRequest ureq, GraderRow row) {
		confirmDeactivationCtrl = new ConfirmDeactivationGraderController(ureq, getWindowControl(),
				referenceEntry, row.getGrader(), false);
		listenTo(confirmDeactivationCtrl);

		String graderName = userManager.getUserDisplayName(row.getGrader());
		String title = translate("deactivate.grader.title", new String[] { graderName });
		cmc = new CloseableModalController(getWindowControl(), "close", confirmDeactivationCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doActivate(Identity grader) {
		if(referenceEntry == null) {
			gradingService.activateGrader(grader);
		} else {
			gradingService.activateGrader(referenceEntry, grader);
		}
		loadModel(false);
		String graderName = userManager.getUserDisplayName(grader);
		showInfo("info.grader.activated", graderName);
	}
	
	private void doConfirmRemove(UserRequest ureq, GraderRow row) {
		confirmRemoveCtrl = new ConfirmDeactivationGraderController(ureq, getWindowControl(), referenceEntry, row.getGrader(), true);
		listenTo(confirmRemoveCtrl);
		
		String graderName = userManager.getUserDisplayName(row.getGrader());
		String title = translate("remove.grader.title", new String[] { graderName });
		cmc = new CloseableModalController(getWindowControl(), "close", confirmRemoveCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doContact(UserRequest ureq, GraderRow row) {
		ContactMessage msg = new ContactMessage(getIdentity());
		ContactList contact = new ContactList(translate("contact.grader.mail"));
		contact.add(row.getGrader());
		msg.addEmailTo(contact);
		
		List<MailTemplate> templates = getTemplates(referenceEntry);
		contactGraderCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, msg, templates);
		contactGraderCtrl.getAndRemoveTitle();
		listenTo(contactGraderCtrl);
		
		String graderName = userManager.getUserDisplayName(row.getGrader());
		String title = translate("contact.grader.title", new String[] { graderName });
		cmc = new CloseableModalController(getWindowControl(), "close", contactGraderCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private List<MailTemplate> getTemplates(RepositoryEntry refEntry) {
		RepositoryEntryGradingConfiguration configuration = null;
		if(refEntry != null) {
			configuration = gradingService.getOrCreateConfiguration(refEntry);
		}
		
		List<MailTemplate> templates = new ArrayList<>();
		templates.add(GraderMailTemplate.empty(getTranslator(), null, null, referenceEntry));
		templates.add(GraderMailTemplate.graderTo(getTranslator(), null, null, refEntry));
		templates.add(GraderMailTemplate.notification(getTranslator(), null, null, refEntry, configuration));
		templates.add(GraderMailTemplate.firstReminder(getTranslator(), null, null, refEntry, configuration));
		templates.add(GraderMailTemplate.secondReminder(getTranslator(), null, null, refEntry, configuration));
		return templates;
	}
	
	private void doAddAbsenceLeave(UserRequest ureq, GraderRow row) {
		Identity grader = row.getGrader();
		OLATResourceable resource = null;
		if(referenceEntry != null) {
			resource = referenceEntry.getOlatResource();
		}
		addAbsenceLeaveCtrl = new CreateAbsenceLeaveController(ureq, getWindowControl(), grader, resource, null);
		listenTo(addAbsenceLeaveCtrl);
		
		String graderName = userManager.getUserDisplayName(row.getGrader());
		String title = translate("absence.grader.title", new String[] { graderName });
		cmc = new CloseableModalController(getWindowControl(), "close", addAbsenceLeaveCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doShowAssignments(UserRequest ureq, GraderRow row, SearchStatus searchStatus) {
		OpenAssignmentsEvent event = new OpenAssignmentsEvent(row.getGrader(), searchStatus);
		fireEvent(ureq, event);
	}
	
	private void doOpenTools(UserRequest ureq, GraderRow row, FormLink link) {
		if(toolsCtrl != null) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private Link removeLink;
		private Link absenceLink;
		private Link activateLink;
		private Link deactivateLink;
		private final Link reportLink;
		private final Link sendMailLink;
		private final Link assignmentsLink;
		
		private final GraderRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, GraderRow row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("tools_graders");
			assignmentsLink = addLink("tool.show.assignments", "show_assignments", "o_icon o_icon_assessment_mode", mainVC);
			sendMailLink = addLink("tool.send.mail", "send_mail", "o_icon o_icon_mail", mainVC);
			reportLink = addLink("tool.download.report", "report", "o_icon o_icon_download", mainVC);
			
			if(row.hasGraderStatus(GraderStatus.activated)) {
				deactivateLink = addLink("tool.deactivate", "deactivate", "o_icon o_icon_deactivate", mainVC);
				absenceLink = addLink("tool.absence", "absence", "o_icon o_icon_absence_leave", mainVC);
			} else {
				activateLink = addLink("tool.activate", "activate", "o_icon o_icon_activate", mainVC);
			}
			if(!row.hasOnlyGraderStatus(GraderStatus.removed)) {
				removeLink = addLink("tool.remove", "remove", "o_icon o_icon_remove", mainVC);
			}
			
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String cmd, String iconCSS, VelocityContainer mainVC) {
			Link link = LinkFactory.createLink(name, name, cmd, mainVC, this);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			return link;
		}
		
		public GraderRow getGraderRow() {
			return row;
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(reportCalloutCtrl == source || reportCtrl == source) {
				reportCalloutCtrl.deactivate();
				close();
			}
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(assignmentsLink == source) {
				close();
				doShowAssignments(ureq, getGraderRow(), null);
			} else if(sendMailLink == source) {
				close();
				doContact(ureq, getGraderRow());
			} else if(reportLink == source) {
				doOpenReportConfiguration(ureq, reportLink);
			} else if(removeLink == source) {
				close();
				doConfirmRemove(ureq, getGraderRow());
			} else if(absenceLink == source) {
				close();
				doAddAbsenceLeave(ureq, getGraderRow());
			} else if(activateLink == source) {
				close();
				doActivate(getGraderRow().getGrader());
			} else if(deactivateLink == source) {
				close();
				doConfirmDeactivate(ureq, getGraderRow());
			}
		}
		
		private void doOpenReportConfiguration(UserRequest ureq, Link link) {
			if(guardModalController(reportCtrl)) return;
			
			reportCtrl = new ReportCalloutController(ureq, getWindowControl(), referenceEntry, row.getGrader());
			listenTo(reportCtrl);
			
			reportCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					reportCtrl.getInitialComponent(), link.getDispatchID(), "", true, "");
			listenTo(reportCalloutCtrl);
			reportCalloutCtrl.activate();
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
