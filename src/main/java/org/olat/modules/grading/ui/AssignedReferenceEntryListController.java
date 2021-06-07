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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters.SearchStatus;
import org.olat.modules.grading.model.ReferenceEntryWithStatistics;
import org.olat.modules.grading.ui.AssignedReferenceEntryListTableModel.GEntryCol;
import org.olat.modules.grading.ui.component.GraderAbsenceLeaveCellRenderer;
import org.olat.modules.grading.ui.component.GraderMailTemplate;
import org.olat.modules.grading.ui.event.OpenEntryAssignmentsEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.ui.absenceleave.CreateAbsenceLeaveController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignedReferenceEntryListController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private AssignedReferenceEntryListTableModel tableModel;
	
	private int counter = 0;
	private final Identity grader;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private ContactFormController contactGraderCtrl;
	private CreateAbsenceLeaveController addAbsenceLeaveCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GradingService gradingService;
	
	public AssignedReferenceEntryListController(UserRequest ureq, WindowControl wControl, Identity grader) {
		super(ureq, wControl, "assigned_entries");
		this.grader = grader;
		initForm(ureq);
		loadModel(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GEntryCol.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GEntryCol.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GEntryCol.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GEntryCol.total, "total"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GEntryCol.done, "done"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GEntryCol.open, "open"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GEntryCol.overdue, "overdue"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GEntryCol.oldestOpenAssignment));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GEntryCol.absence,
				new GraderAbsenceLeaveCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GEntryCol.recordedMetadataTime));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GEntryCol.recordedTime));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(GEntryCol.tools);
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsCol.setHeaderLabel(translate("table.header.tools"));
		toolsCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new AssignedReferenceEntryListTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "entries", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("table.assignments.empty", null, FlexiTableElement.TABLE_EMPTY_ICON);
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "grading-entries-list");
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel(boolean reaload) {
		List<ReferenceEntryWithStatistics> statistics = gradingService.getGradedEntriesWithStatistics(grader);
		List<AssignedReferenceEntryRow> rows = new ArrayList<>(statistics.size());
		for(ReferenceEntryWithStatistics stats:statistics) {
			rows.add(forgeRow(stats));
		}
		tableModel.setObjects(rows);
		tableEl.reset(reaload, reaload, true);
	}
	
	private AssignedReferenceEntryRow forgeRow(ReferenceEntryWithStatistics statistics) {
		AssignedReferenceEntryRow row = new AssignedReferenceEntryRow(statistics);
		
		// tools
		String linkName = "tools-" + counter++;
		FormLink toolsLink = uifactory.addFormLink(linkName, "tools", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setAriaLabel(translate("table.action"));
		toolsLink.setUserObject(row);
		flc.add(linkName, toolsLink);
		row.setToolsLink(toolsLink);
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCalloutCtrl == source || cmc == source) {
			cleanUp();
		} else if(contactGraderCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(addAbsenceLeaveCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel(false);
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(contactGraderCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		contactGraderCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
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
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd())) {
				doOpenTools(ureq, (AssignedReferenceEntryRow)link.getUserObject(), link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doShowAssignments(UserRequest ureq, AssignedReferenceEntryRow row, SearchStatus searchStatus) {
		fireEvent(ureq, new OpenEntryAssignmentsEvent(row.getReferenceEntry(), searchStatus));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenTools(UserRequest ureq, AssignedReferenceEntryRow row, FormLink link) {
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
	
	private void doContact(UserRequest ureq, AssignedReferenceEntryRow row) {
		ContactMessage msg = new ContactMessage(getIdentity());
		ContactList contact = new ContactList(translate("contact.grader.mail"));
		contact.add(grader);
		msg.addEmailTo(contact);
		
		RepositoryEntry referenceEntry = row.getReferenceEntry();
		List<MailTemplate> templates = getTemplates(referenceEntry);
		contactGraderCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, msg, templates);
		listenTo(contactGraderCtrl);
		
		String graderName = userManager.getUserDisplayName(grader);
		String title = translate("contact.grader.title", new String[] { graderName });
		cmc = new CloseableModalController(getWindowControl(), "close", contactGraderCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private List<MailTemplate> getTemplates(RepositoryEntry refEntry) {
		RepositoryEntryGradingConfiguration configuration = null;
		if(refEntry != null)  {
			configuration = gradingService.getOrCreateConfiguration(refEntry);
		}
		
		List<MailTemplate> templates = new ArrayList<>();
		templates.add(GraderMailTemplate.empty(getTranslator(), null, null, refEntry));
		templates.add(GraderMailTemplate.graderTo(getTranslator(), null, null, refEntry));
		templates.add(GraderMailTemplate.notification(getTranslator(), null, null, refEntry, configuration));
		templates.add(GraderMailTemplate.firstReminder(getTranslator(), null, null, refEntry, configuration));
		templates.add(GraderMailTemplate.secondReminder(getTranslator(), null, null, refEntry, configuration));
		return templates;
	}
	
	private void doAddAbsenceLeave(UserRequest ureq, AssignedReferenceEntryRow row) {
		OLATResourceable resource = row.getReferenceEntry().getOlatResource();
		addAbsenceLeaveCtrl = new CreateAbsenceLeaveController(ureq, getWindowControl(), grader, resource, null);
		listenTo(addAbsenceLeaveCtrl);
		
		String title = translate("absence.grader.title", new String[] { row.getDisplayname() });
		cmc = new CloseableModalController(getWindowControl(), "close", addAbsenceLeaveCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}

	private class ToolsController extends BasicController {
		
		private final Link sendMailLink;
		private final Link absenceLink;
		
		private final AssignedReferenceEntryRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, AssignedReferenceEntryRow row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("tools_assigned_entries");
			
			sendMailLink = addLink("tool.send.mail", "send_mail", "o_icon o_icon_mail", mainVC);
			absenceLink = addLink("tool.absence", "absence", "o_icon o_icon_absence_leave", mainVC);
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

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(sendMailLink == source) {
				close();
				doContact(ureq, row);
			} else if(absenceLink == source) {
				close();
				doAddAbsenceLeave(ureq, row);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
