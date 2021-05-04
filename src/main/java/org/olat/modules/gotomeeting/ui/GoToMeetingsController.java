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
package org.olat.modules.gotomeeting.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.model.GoToType;
import org.olat.modules.gotomeeting.ui.GoToMeetingTableModel.MeetingsCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToMeetingsController extends FormBasicController {

	private final String subIdent;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	
	private FlexiTableElement upcomingTableEl, pastTableEl;
	private GoToMeetingTableModel upcomingTableModel, pastTableModel;
	
	@Autowired
	private GoToMeetingManager meetingMgr;
	
	public GoToMeetingsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		super(ureq, wControl, "meetings");
		
		this.businessGroup = businessGroup;
		this.entry = entry;
		this.subIdent = subIdent;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.name.i18nHeaderKey(), MeetingsCols.name.ordinal(), true, MeetingsCols.name.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.start.i18nHeaderKey(), MeetingsCols.start.ordinal(), true, MeetingsCols.start.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.end.i18nHeaderKey(), MeetingsCols.end.ordinal(), true, MeetingsCols.end.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select-upcoming"));

		upcomingTableModel = new GoToMeetingTableModel(columnsModel);
		upcomingTableEl = uifactory.addTableElement(getWindowControl(), "upcomingmeetings", upcomingTableModel, getTranslator(), formLayout);
		upcomingTableEl.setEmptyTableMessageKey("table.empty");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), true));
		upcomingTableEl.setSortSettings(sortOptions);
		
		
		FlexiTableColumnModel pastColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.name.i18nHeaderKey(), MeetingsCols.name.ordinal(), true, MeetingsCols.name.name()));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.start.i18nHeaderKey(), MeetingsCols.start.ordinal(), true, MeetingsCols.start.name()));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.end.i18nHeaderKey(), MeetingsCols.end.ordinal(), true, MeetingsCols.end.name()));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select-past"));

		pastTableModel = new GoToMeetingTableModel(pastColumnsModel);
		pastTableEl = uifactory.addTableElement(getWindowControl(), "pastmeetings", pastTableModel, getTranslator(), formLayout);

		FlexiTableSortOptions pastSortOptions = new FlexiTableSortOptions();
		pastSortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), false));
		pastTableEl.setSortSettings(sortOptions);

		updateModel();
	}
	
	protected void updateModel() {
		List<GoToMeeting> meetings = meetingMgr.getMeetings(GoToType.training, entry, subIdent, businessGroup);
		
		List<GoToMeeting> upcomingMeetings = new ArrayList<>();
		List<GoToMeeting> pastMeetings = new ArrayList<>();
		
		Date now = new Date();
		for(GoToMeeting meeting:meetings) {
			Date endDate = meeting.getEndDate();
			if(now.after(endDate)) {
				pastMeetings.add(meeting);
			} else {
				upcomingMeetings.add(meeting);
			}
		}
		
		upcomingTableModel.setObjects(upcomingMeetings);
		upcomingTableEl.reloadData();
		upcomingTableEl.reset();
		
		pastTableModel.setObjects(pastMeetings);
		pastTableEl.reloadData();
		pastTableEl.reset();
		pastTableEl.setVisible(!pastMeetings.isEmpty());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(upcomingTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select-upcoming".equals(se.getCommand())) {
					GoToMeeting meeting = upcomingTableModel.getObject(se.getIndex());
					fireEvent(ureq, new SelectGoToMeetingEvent(meeting));
				}
			}
		} else if(pastTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select-past".equals(se.getCommand())) {
					GoToMeeting meeting = pastTableModel.getObject(se.getIndex());
					fireEvent(ureq, new SelectGoToMeetingEvent(meeting));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
