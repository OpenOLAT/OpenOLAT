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

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.ui.TeamsMeetingDataModel.SoMeetingsCols;
import org.olat.modules.teams.ui.TeamsMeetingTableModel.MeetingsCols;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 2 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsAdminMeetingsController extends FormBasicController {

	private FormLink deleteButton;
	private FlexiTableElement tableEl;
	private TeamsMeetingDataModel tableModel;
	private TeamsMeetingDataSource dataSource;
	
	private final boolean readOnly;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDelete;
	private DialogBoxController confirmBatchDelete;
	
	@Autowired
	private TeamsService teamsService;
	
	public TeamsAdminMeetingsController(UserRequest ureq, WindowControl wControl, boolean readOnly) {
		super(ureq, wControl, "meetings_admin");
		this.readOnly = readOnly;
		dataSource = new TeamsMeetingDataSource();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!readOnly) {
			deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.subject));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.permanent));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.startDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.endDate));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("context", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.context.i18nHeaderKey(), SoMeetingsCols.context.ordinal(), "context",
				true, SoMeetingsCols.context.name(), renderer));
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		}
		
		tableModel = new TeamsMeetingDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, 20, true, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("no.meetings", null, "o_icon_calendar");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), true));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "teams-edit-meetings-list-v2");
		tableEl.setMultiSelect(!readOnly);
		tableEl.setSelectAllEnable(!readOnly);
		tableEl.setSearchEnabled(true);
		tableEl.addBatchButton(deleteButton);
	}
	
	protected void reloadRows() {
		tableEl.deselectAll();
		tableEl.reloadData();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDelete == source) {
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
		removeControllerListener(confirmDelete);
		removeControllerListener(cmc);
		confirmBatchDelete = null;
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
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, tableModel.getObject(se.getIndex()));
				} else if("context".equals(se.getCommand())) {
					doOpenContext(ureq, tableModel.getObject(se.getIndex()));
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
	
	private void doConfirmDelete(UserRequest ureq, TeamsMeeting meeting) {
		String confirmDeleteTitle = translate("confirm.delete.meeting.title", new String[]{ meeting.getSubject() });
		String confirmDeleteText = translate("confirm.delete.meeting", new String[]{ meeting.getSubject() });
		confirmDelete = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmDelete);
		confirmDelete.setUserObject(meeting);
	}
	
	private void doDelete(TeamsMeeting meeting) {
		teamsService.deleteMeeting(meeting);
		reloadRows();
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
		reloadRows();
	}
	
	private void doOpenContext(UserRequest ureq, TeamsMeeting meeting) {
		String businessPath;
		if(meeting.getEntry() != null) {
			businessPath = "[RepositoryEntry:" + meeting.getEntry().getKey() + "]";
			if(StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
				businessPath += "[CourseNode:" + meeting.getSubIdent() + "]";
			}
		} else if(meeting.getBusinessGroup() != null) {
			businessPath = "[BusinessGroup:" + meeting.getBusinessGroup().getKey() + "]";
		} else {
			return;
		}
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

}
