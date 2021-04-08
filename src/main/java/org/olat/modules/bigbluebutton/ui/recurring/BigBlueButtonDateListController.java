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
package org.olat.modules.bigbluebutton.ui.recurring;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingsCalendarController;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.olat.modules.bigbluebutton.ui.recurring.RecurringMeetingsTableModel.RecurringCols;
import org.olat.modules.gotomeeting.ui.GoToMeetingTableModel.MeetingsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonDateListController extends StepFormBasicController implements FlexiTableCssDelegate {
	
	private FormLink addButton;
	private FormLink openCalLink;
	private FlexiTableElement tableEl;
	private RecurringMeetingsTableModel tableModel;
	
	private final RecurringMeetingsContext meetingsContext;
	
	private CloseableModalController cmc;
	private AddMeetingController addMeetingController;
	private BigBlueButtonMeetingsCalendarController calendarCtr;
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public BigBlueButtonDateListController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			RecurringMeetingsContext meetingsContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "dates_step");
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale()));
		this.meetingsContext = meetingsContext;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addButton = uifactory.addFormLink("add.meeting", formLayout, Link.BUTTON);
		addButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		
		openCalLink = uifactory.addFormLink("calendar.open", formLayout, Link.BUTTON);
		openCalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecurringCols.available,
				new RecurringMeetingAvailabilityCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecurringCols.dayOfWeek));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecurringCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecurringCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecurringCols.delete.i18nHeaderKey(),
				RecurringCols.delete.ordinal(), "delete",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("undelete"), "undelete"),
						new StaticFlexiCellRenderer(translate("delete"), "delete"))));

		tableModel = new RecurringMeetingsTableModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("no.meeting.configured", null, "o_icon_calendar");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), true));
		tableEl.setSortSettings(sortOptions);
		tableEl.setCssDelegate(this);
	}
	
	private void loadModel() {
		List<RecurringMeeting> meetings = meetingsContext.getMeetings();
		for(RecurringMeeting meeting:meetings) {
			boolean available = bigBlueButtonManager.isSlotAvailable(null, meetingsContext.getTemplate(),
					meeting.getStartDate(), meetingsContext.getLeadTime(),
					meeting.getEndDate(), meetingsContext.getFollowupTime());
			meeting.setSlotAvailable(available);
		}
		
		tableModel.setObjects(meetingsContext.getMeetings());
		tableEl.reset(true, true, true);
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		RecurringMeeting meeting = tableModel.getObject(pos);
		return meeting.isDeleted() ? "o_bbb_deleted" : null;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addMeetingController == source) {
			if(event == Event.DONE_EVENT) {
				addMeeting(addMeetingController.getDate());
			}
			cmc.deactivate();
			cleanUp();
		} else if(calendarCtr == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addMeetingController);
		removeAsListenerAndDispose(calendarCtr);
		removeAsListenerAndDispose(cmc);
		addMeetingController = null;
		calendarCtr = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addButton == source) {
			doAddMeeting(ureq);
		} else if(openCalLink == source) {
			doOpenCalendar(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					tableModel.getObject(se.getIndex()).setDeleted(true);
					tableEl.reset(false, false, true);
				} else if("undelete".equals(se.getCommand())) {
					tableModel.getObject(se.getIndex()).setDeleted(false);
					tableEl.reset(false, false, true);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
	
	private void doAddMeeting(UserRequest ureq) {
		if(guardModalController(addMeetingController)) return;

		addMeetingController = new AddMeetingController(ureq, getWindowControl(), meetingsContext);
		listenTo(addMeetingController);
		
		cmc = new CloseableModalController(getWindowControl(), "close", addMeetingController.getInitialComponent(),
				true, translate("add.single.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void addMeeting(Date additionalDate) {
		meetingsContext.addMeetingAt(additionalDate);
		loadModel();
	}
	
	private void doOpenCalendar(UserRequest ureq) {
		if(guardModalController(calendarCtr)) return;

		calendarCtr = new BigBlueButtonMeetingsCalendarController(ureq, getWindowControl());
		listenTo(calendarCtr);
		cmc = new CloseableModalController(getWindowControl(), "close", calendarCtr.getInitialComponent(), true,
				translate("calendar.open"));
		cmc.activate();
		listenTo(cmc);
	}
}
