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
package org.olat.modules.teams.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.ui.TeamsMeetingTableModel.MeetingsCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsEditMeetingsController extends FormBasicController {

	private FormLink deleteButton;
	private FormLink addMeetingButton;
	private FlexiTableElement tableEl;
	private TeamsMeetingTableModel tableModel;
	
	private final boolean readOnly;
	private final String subIdent;
	private final BusinessGroup group;
	private final RepositoryEntry entry;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDelete;
	private DialogBoxController confirmBatchDelete;
	private EditTeamsMeetingController editMeetingCtlr;
	
	@Autowired
	private TeamsService teamsService;
	
	public TeamsEditMeetingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdentifier,
			BusinessGroup group, boolean readOnly) {
		super(ureq, wControl, "meetings_admin");
		this.entry = entry;
		this.group = group;
		this.subIdent = subIdentifier;
		this.readOnly = readOnly;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!readOnly) {
			addMeetingButton = uifactory.addFormLink("add.single.meeting", formLayout, Link.BUTTON);
			addMeetingButton.setElementCssClass("o_sel_teams_single_meeting_add");

			deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		}
		
		// upcoming meetings table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.subject));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.end));
		if(!readOnly) {
			DefaultFlexiColumnModel editViewCol = new DefaultFlexiColumnModel(MeetingsCols.edit, "edit",
					new BooleanCellRenderer(
							new StaticFlexiCellRenderer(translate("edit"), "edit"),
							new StaticFlexiCellRenderer(translate("view"), "edit")));
			editViewCol.setExportable(false);
			editViewCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(editViewCol);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		}
		
		tableModel = new TeamsMeetingTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("no.meetings");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), true));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "teams-edit-meetings-list-v2");
		tableEl.setMultiSelect(!readOnly);
		tableEl.setSelectAllEnable(true);
		if(deleteButton != null) {
			tableEl.addBatchButton(deleteButton);
		}
	}
	
	private void loadModel() {
		List<TeamsMeeting> meetings = teamsService.getMeetings(entry, subIdent, group);
		tableModel.setObjects(meetings);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editMeetingCtlr == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				TeamsMeeting meeting = (TeamsMeeting)confirmDelete.getUserObject();
				doDelete(meeting);
			}
			cleanUp();
		} else if(confirmBatchDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<TeamsMeeting> meetings = (List<TeamsMeeting>)confirmBatchDelete.getUserObject();
				doDelete(meetings);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeControllerListener(confirmBatchDelete);
		removeControllerListener(editMeetingCtlr);
		removeControllerListener(confirmDelete);
		removeControllerListener(cmc);
		confirmBatchDelete = null;
		editMeetingCtlr = null;
		confirmDelete = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addMeetingButton == source) {
			doAddSingleMeeting(ureq);
		} else if(deleteButton == source) {
			List<TeamsMeeting> selectedMeetings = getSelectedMeetings();
			doConfirmDelete(ureq, selectedMeetings);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand())) {
					doEditMeeting(ureq, tableModel.getObject(se.getIndex()));
				} else if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private List<TeamsMeeting> getSelectedMeetings() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		return selectedIndex.stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.collect(Collectors.toList());
	}

	private void doAddSingleMeeting(UserRequest ureq) {
		editMeetingCtlr = new EditTeamsMeetingController(ureq, getWindowControl(), entry, subIdent, group);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editMeetingCtlr.getInitialComponent(),
				true, translate("add.single.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditMeeting(UserRequest ureq, TeamsMeeting meeting) {
		if(guardModalController(editMeetingCtlr)) return;
		
		meeting = teamsService.getMeeting(meeting);
		editMeetingCtlr = new EditTeamsMeetingController(ureq, getWindowControl(), meeting);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editMeetingCtlr.getInitialComponent(),
				true, translate("edit.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq, TeamsMeeting meeting) {
		String confirmDeleteTitle = translate("confirm.delete.meeting.title", new String[]{ meeting.getSubject() });
		String confirmDeleteText = translate("confirm.delete.meeting", new String[]{ meeting.getSubject() });
		confirmDelete = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmDelete);
		confirmDelete.setUserObject(meeting);
	}
	
	private void doDelete(TeamsMeeting meeting) {
		teamsService.deleteMeeting(meeting);
		loadModel();
	}
	
	private void doConfirmDelete(UserRequest ureq, List<TeamsMeeting> meetings) {
		if(meetings.isEmpty()) {
			showWarning("warning.at.least.one.meeting");
		} else if(meetings.size() == 1) {
			doConfirmDelete(ureq, meetings.get(0));
		} else {
			Set<String> names = new HashSet<>();
			StringBuilder namesBuilder = new StringBuilder(128);
			for(TeamsMeeting meeting:meetings) {
				if(names.contains(meeting.getSubject())) {
					continue;
				}
				
				if(namesBuilder.length() > 0) namesBuilder.append(", ");
				namesBuilder.append(StringHelper.escapeHtml(meeting.getSubject()));
				names.add(meeting.getSubject());
			}

			String confirmDeleteTitle = translate("confirm.delete.meetings.title", new String[]{ Integer.toString(meetings.size()) });
			String confirmDeleteText = translate("confirm.delete.meetings", new String[]{ Integer.toString(meetings.size()), namesBuilder.toString() });
			confirmBatchDelete = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmBatchDelete);
			confirmBatchDelete.setUserObject(meetings);
		}
	}
	
	private void doDelete(List<TeamsMeeting> meetings) {
		for(TeamsMeeting meeting:meetings) {
			teamsService.deleteMeeting(meeting);
		}
		loadModel();
	}

}
