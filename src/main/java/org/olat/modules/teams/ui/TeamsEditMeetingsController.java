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
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
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
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.ui.EditTeamsMeetingController.Mode;
import org.olat.modules.teams.ui.TeamsMeetingTableModel.MeetingsCols;
import org.olat.modules.teams.ui.recurring.TeamsRecurringMeeting;
import org.olat.modules.teams.ui.recurring.TeamsRecurringMeeting1Step;
import org.olat.modules.teams.ui.recurring.TeamsRecurringMeetingsContext;
import org.olat.modules.teams.ui.recurring.TeamsRecurringMeetingsContext.RecurringMode;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsEditMeetingsController extends FormBasicController {

	private FormLink deleteButton;
	private FormLink addSingleMeetingButton;
	private FormLink addPermanentMeetingButton;
	private FormLink addDailyRecurringMeetingsLink;
	private FormLink addWeekyRecurringMeetingsLink;
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
	private StepsMainRunController addDailyMeetingCtrl;
	private StepsMainRunController addWeeklyMeetingCtrl;

	@Autowired
	private UserManager userManager;
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
			DropdownItem addMeetingDropdown = uifactory.addDropdownMenu("add.meeting", "add.meeting", formLayout, getTranslator());
			addMeetingDropdown.setOrientation(DropdownOrientation.right);
			addMeetingDropdown.setElementCssClass("o_sel_teams_meeting_add");

			addSingleMeetingButton = uifactory.addFormLink("add.single.meeting", formLayout, Link.LINK);
			addSingleMeetingButton.setElementCssClass("o_sel_teams_single_meeting_add");
			addMeetingDropdown.addElement(addSingleMeetingButton);
			
			addPermanentMeetingButton = uifactory.addFormLink("add.permanent.meeting", formLayout, Link.LINK);
			addPermanentMeetingButton.setElementCssClass("o_sel_teams_permanent_meeting_add");
			addMeetingDropdown.addElement(addPermanentMeetingButton);
			
			addDailyRecurringMeetingsLink = uifactory.addFormLink("add.daily.meeting", formLayout, Link.LINK);
			addDailyRecurringMeetingsLink.setElementCssClass("o_sel_teams_daily_meeting_add");
			addMeetingDropdown.addElement(addDailyRecurringMeetingsLink);
			addWeekyRecurringMeetingsLink = uifactory.addFormLink("add.weekly.meeting", formLayout, Link.LINK);
			addWeekyRecurringMeetingsLink.setElementCssClass("o_sel_teams_weekly_meeting_add");
			addMeetingDropdown.addElement(addWeekyRecurringMeetingsLink);

			deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		}
		
		// upcoming meetings table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.subject));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.permanent));
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
		tableEl.setEmptyTableSettings("no.meetings", null, "o_icon_calendar");

		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), true));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "teams-edit-meetings-list-v2");
		tableEl.setMultiSelect(!readOnly);
		tableEl.setSelectAllEnable(true);
		tableEl.addBatchButton(deleteButton);
	}
	
	private void loadModel() {
		List<TeamsMeeting> meetings = teamsService.getMeetings(entry, subIdent, group);
		tableModel.setObjects(meetings);
		tableEl.reset(true, true, true);
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
		} else if(addDailyMeetingCtrl == source || addWeeklyMeetingCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel();
				}
				cleanUp();
			}
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
		if(deleteButton == source) {
			List<TeamsMeeting> selectedMeetings = getSelectedMeetings();
			doConfirmDelete(ureq, selectedMeetings);
		} else if(addSingleMeetingButton == source) {
			doAddSingleMeeting(ureq);
		} else if(addPermanentMeetingButton == source) {
			doAddPermanentMeeting(ureq);
		} else if(addDailyRecurringMeetingsLink == source) {
			doAddDailyRecurringMeeting(ureq);
		} else if(addWeekyRecurringMeetingsLink == source) {
			doAddWeeklyRecurringMeeting(ureq);
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
		editMeetingCtlr = new EditTeamsMeetingController(ureq, getWindowControl(), entry, subIdent, group, Mode.dates);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editMeetingCtlr.getInitialComponent(),
				true, translate("add.single.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAddPermanentMeeting(UserRequest ureq) {
		editMeetingCtlr = new EditTeamsMeetingController(ureq, getWindowControl(), entry, subIdent, group, Mode.permanent);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editMeetingCtlr.getInitialComponent(),
				true, translate("add.single.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditMeeting(UserRequest ureq, TeamsMeeting meeting) {
		if(guardModalController(editMeetingCtlr)) return;
		
		meeting = teamsService.getMeeting(meeting);
		if(meeting == null) {
			showWarning("warning.no.meeting");
			loadModel();
		} else {
			editMeetingCtlr = new EditTeamsMeetingController(ureq, getWindowControl(), meeting);
			listenTo(editMeetingCtlr);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), editMeetingCtlr.getInitialComponent(),
					true, translate("edit.meeting"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doAddDailyRecurringMeeting(UserRequest ureq) {
		removeAsListenerAndDispose(addDailyMeetingCtrl);
		
		final TeamsRecurringMeetingsContext context = new TeamsRecurringMeetingsContext(entry, subIdent, group, RecurringMode.daily);
		context.setMainPresenter(userManager.getUserDisplayName(getIdentity()));
		
		TeamsRecurringMeeting1Step step = new TeamsRecurringMeeting1Step(ureq, context);
		StepRunnerCallback finishCallback = (uureq, swControl, runContext) -> {
			addRecurringMeetings(context);
			return StepsMainRunController.DONE_MODIFIED;
		};
		String title = translate("add.daily.meeting");
		addDailyMeetingCtrl = new StepsMainRunController(ureq, getWindowControl(), step, finishCallback, null, title, "");
		listenTo(addDailyMeetingCtrl);
		getWindowControl().pushAsModalDialog(addDailyMeetingCtrl.getInitialComponent());
	}

	
	private void doAddWeeklyRecurringMeeting(UserRequest ureq) {
		removeAsListenerAndDispose(addWeeklyMeetingCtrl);
		
		final TeamsRecurringMeetingsContext context = new TeamsRecurringMeetingsContext(entry, subIdent, group, RecurringMode.weekly);
		context.setMainPresenter(userManager.getUserDisplayName(getIdentity()));

		TeamsRecurringMeeting1Step step = new TeamsRecurringMeeting1Step(ureq, context);
		StepRunnerCallback finishCallback = (uureq, swControl, runContext) -> {
			addRecurringMeetings(context);
			return StepsMainRunController.DONE_MODIFIED;
		};
		String title = translate("add.weekly.meeting");
		addWeeklyMeetingCtrl = new StepsMainRunController(ureq, getWindowControl(), step, finishCallback, null, title, "");
		listenTo(addWeeklyMeetingCtrl);
		getWindowControl().pushAsModalDialog(addWeeklyMeetingCtrl.getInitialComponent());
	}
	
	private void addRecurringMeetings(TeamsRecurringMeetingsContext context) {
		for(TeamsRecurringMeeting meeting:context.getMeetings()) {
			if(meeting.isDeleted()) {
				continue;
			}
			
			TeamsMeeting tMeeting = teamsService.createMeeting(context.getName(), meeting.getStartDate(), meeting.getEndDate(),
					context.getEntry(), context.getSubIdent(), context.getBusinessGroup(), getIdentity());
			tMeeting.setDescription(context.getDescription());
			tMeeting.setMainPresenter(context.getMainPresenter());
			tMeeting.setPermanent(false);
			tMeeting.setLeadTime(context.getLeadTime());
			tMeeting.setFollowupTime(context.getFollowupTime());
			tMeeting.setAccessLevel(context.getAccessLevel());
			tMeeting.setAllowedPresenters(context.getAllowedPresenters());
			tMeeting.setEntryExitAnnouncement(context.isEntryExitAnnouncement());
			tMeeting.setLobbyBypassScope(context.getLobbyBypassScope());
			tMeeting.setParticipantsCanOpen(context.isParticipantsCanOpen());
			teamsService.updateMeeting(tMeeting);
		}
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
