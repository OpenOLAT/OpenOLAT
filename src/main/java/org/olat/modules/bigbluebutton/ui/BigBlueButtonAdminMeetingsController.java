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
package org.olat.modules.bigbluebutton.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingAdminInfos;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingDataModel.SoMeetingsCols;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingTableModel.BMeetingsCols;
import org.olat.modules.gotomeeting.ui.GoToMeetingTableModel.MeetingsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonAdminMeetingsController extends FormBasicController {

	private FormLink deleteButton;
	private FlexiTableElement tableEl;
	private BigBlueButtonMeetingDataModel tableModel;
	private BigBlueButtonMeetingDataSource dataSource;
	
	private DialogBoxController confirmDelete;
	private DialogBoxController confirmBatchDelete;

	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;

	public BigBlueButtonAdminMeetingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "meetings_admin");
		dataSource = new BigBlueButtonMeetingDataSource();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(bigBlueButtonModule.isPermanentMeetingEnabled(), BMeetingsCols.permanent));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.startDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.endDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.template));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.server, new ServerCellRenderer()));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("resource", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.resource.i18nHeaderKey(), SoMeetingsCols.resource.ordinal(), "resource",
				true, SoMeetingsCols.resource.name(), renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SoMeetingsCols.recordings));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));

		tableModel = new BigBlueButtonMeetingDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, 20, true, getTranslator(), formLayout);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "bigbluebutton-admin-meetings-list");
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		tableEl.addBatchButton(deleteButton);
		tableEl.setFilters(null, getFilters(), false);
	}
	
	private List<FlexiTableFilter> getFilters() {
		List<FlexiTableFilter> filters = new ArrayList<>(5);
		filters.add(new FlexiTableFilter(translate("with.recordings"), "with-recordings"));
		filters.add(new FlexiTableFilter(translate("without.recording"), "no-recordings"));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("show.all"), "showall", true));
		return filters;
	}
	
	private void reloadRows() {
		tableEl.deselectAll();
		tableEl.reloadData();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				BigBlueButtonMeeting meeting = (BigBlueButtonMeeting)confirmDelete.getUserObject();
				doDelete(meeting);
			}
			cleanUp();
		} else if(confirmBatchDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<BigBlueButtonMeetingAdminInfos> meetings = (List<BigBlueButtonMeetingAdminInfos>)confirmBatchDelete.getUserObject();
				doDelete(meetings);
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			List<BigBlueButtonMeetingAdminInfos> selectedMeetings = getSelectedMeetings();
			doConfirmDelete(ureq, selectedMeetings);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("resource".equals(se.getCommand())) {
					doOpenResource(ureq, tableModel.getObject(se.getIndex()));
				} else if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private List<BigBlueButtonMeetingAdminInfos> getSelectedMeetings() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		return selectedIndex.stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDelete);
		confirmDelete = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenResource(UserRequest ureq, BigBlueButtonMeetingAdminInfos meetingInfos) {
		BigBlueButtonMeeting meeting = meetingInfos.getMeeting();
		if(meeting.getEntry() != null) {
			String businessPath = "[RepositoryEntry:" + meeting.getEntry().getKey() + "]";
			if(StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
				businessPath += "[CourseNode:" + meeting.getSubIdent() + "]";
			}
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else if(meeting.getBusinessGroup() != null) {
			String businessPath = "[BusinessGroup:" + meeting.getBusinessGroup().getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, BigBlueButtonMeetingAdminInfos meetingInfos) {
		BigBlueButtonMeeting meeting = meetingInfos.getMeeting();
		doConfirmDelete(ureq, meeting);
	}
	
	private void doConfirmDelete(UserRequest ureq, BigBlueButtonMeeting meeting) {
		String confirmDeleteTitle = translate("confirm.delete.meeting.title", new String[]{ meeting.getName() });
		String confirmDeleteText = translate("confirm.delete.meeting", new String[]{ meeting.getName() });
		confirmDelete = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmDelete);
		confirmDelete.setUserObject(meeting);
	}
	
	private void doDelete(BigBlueButtonMeeting meeting) {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		meeting = bigBlueButtonManager.getMeeting(meeting);
		if(meeting != null) {
			bigBlueButtonManager.deleteMeeting(meeting, errors);
		}
		reloadRows();
		if(errors.hasErrors()) {
			getWindowControl().setError(BigBlueButtonErrorHelper.formatErrors(getTranslator(), errors));
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, List<BigBlueButtonMeetingAdminInfos> meetings) {
		if(meetings.isEmpty()) {
			showWarning("warning.at.least.one.meeting");
		} else if(meetings.size() == 1) {
			doConfirmDelete(ureq, meetings.get(0));
		} else {
			Set<String> names = new HashSet<>();
			long numOfRecordings = 0;
			StringBuilder namesBuilder = new StringBuilder(128);
			for(BigBlueButtonMeetingAdminInfos meeting:meetings) {
				numOfRecordings += meeting.getNumOfRecordings();
				String name = meeting.getMeeting().getName();
				if(names.contains(name)) {
					continue;
				}
				
				if(namesBuilder.length() > 0) namesBuilder.append(", ");
				namesBuilder.append(StringHelper.escapeHtml(name));
				names.add(name);
			}

			String confirmDeleteTitle = translate("confirm.delete.meetings.title", new String[]{ Integer.toString( meetings.size()) });
			String i18nDelete = "confirm.delete.meetings";
			if(numOfRecordings == 1) {
				i18nDelete = "confirm.delete.meetings.recording";
			} else if(numOfRecordings > 1) {
				i18nDelete = "confirm.delete.meetings.recordings";
			}
			String confirmDeleteText = translate(i18nDelete, new String[]{
					Integer.toString(meetings.size()), namesBuilder.toString(), Long.toString(numOfRecordings)
				});
			confirmBatchDelete = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmBatchDelete);
			confirmBatchDelete.setUserObject(meetings);
		}
	}
	
	private void doDelete(List<BigBlueButtonMeetingAdminInfos> meetingsInfos) {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		for(BigBlueButtonMeetingAdminInfos meetingInfos:meetingsInfos) {
			BigBlueButtonMeeting meeting = meetingInfos.getMeeting();
			meeting = bigBlueButtonManager.getMeeting(meeting);
			if(meeting != null) {
				bigBlueButtonManager.deleteMeeting(meeting, errors);
			}
		}
		if(errors.hasErrors()) {
			getWindowControl().setError(BigBlueButtonErrorHelper.formatErrors(getTranslator(), errors));
		}
		reloadRows();
	}
}
