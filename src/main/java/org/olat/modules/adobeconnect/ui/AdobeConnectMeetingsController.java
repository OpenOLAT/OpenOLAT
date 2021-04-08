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
package org.olat.modules.adobeconnect.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.ui.AdobeConnectMeetingTableModel.ACMeetingsCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectMeetingsController extends FormBasicController {
	
	private FlexiTableElement pastTableEl;
	private FlexiTableElement upcomingTableEl;
	private AdobeConnectMeetingTableModel pastTableModel;
	private AdobeConnectMeetingTableModel upcomingTableModel;
	
	private final RepositoryEntry entry;
	private final String subIdent;
	private final BusinessGroup businessGroup;
	private final boolean showPermanentCol;
	
	@Autowired
	private AdobeConnectModule adobeConnectModule;
	@Autowired
	private AdobeConnectManager adobeConnectManager;
	
	public AdobeConnectMeetingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdent,
			BusinessGroup businessGroup, boolean administrator, boolean moderator) {
		super(ureq, wControl, "meetings");
		this.entry = entry;
		this.subIdent = subIdent;
		this.businessGroup = businessGroup;
		showPermanentCol = (administrator || moderator) && !adobeConnectModule.isSingleMeetingMode();
		
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// upcoming meetings table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.name));
		if(showPermanentCol) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.permanent));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));

		upcomingTableModel = new AdobeConnectMeetingTableModel(columnsModel, getLocale());
		upcomingTableEl = uifactory.addTableElement(getWindowControl(), "upcomingMeetings", upcomingTableModel, getTranslator(), formLayout);
		upcomingTableEl.setEmptyTableSettings("no.upcoming.meetings", null, "o_icon_calendar");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(ACMeetingsCols.start.name(), false));
		upcomingTableEl.setSortSettings(sortOptions);
		upcomingTableEl.setAndLoadPersistedPreferences(ureq, "adobe-connect-upcoming-meetings-list");

		// past meetings
		FlexiTableColumnModel pastColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.name));
		if(showPermanentCol) {
			pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.permanent));
		}
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.start));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.end));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));

		pastTableModel = new AdobeConnectMeetingTableModel(pastColumnsModel, getLocale());
		pastTableEl = uifactory.addTableElement(getWindowControl(), "pastMeetings", pastTableModel, getTranslator(), formLayout);
		
		FlexiTableSortOptions pastSortOptions = new FlexiTableSortOptions();
		pastSortOptions.setDefaultOrderBy(new SortKey(ACMeetingsCols.start.name(), false));
		pastTableEl.setSortSettings(pastSortOptions);
		pastTableEl.setAndLoadPersistedPreferences(ureq, "adobe-connect-past-meetings-list");
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void updateModel() {
		List<AdobeConnectMeeting> meetings = adobeConnectManager.getMeetings(entry, subIdent, businessGroup);
		
		Date now = new Date();
		List<AdobeConnectMeeting> pastMeetings = new ArrayList<>();
		List<AdobeConnectMeeting> upcomingMeetings = new ArrayList<>();
		for(AdobeConnectMeeting meeting:meetings) {
			if(meeting.getStartDate() == null || meeting.getEndDate() == null
					|| now.compareTo(meeting.getEndDate()) <= 0) {
				upcomingMeetings.add(meeting);
			} else {
				pastMeetings.add(meeting);
			}
		}
		
		upcomingTableModel.setObjects(upcomingMeetings);
		upcomingTableEl.reset(true, true, true);
		pastTableModel.setObjects(pastMeetings);
		pastTableEl.reset(true, true, true);
		pastTableEl.setVisible(!pastMeetings.isEmpty());
	}
	
	public boolean hasMeetingByKey(Long meetingKey) {
		boolean has = upcomingTableModel.getObjects().stream()
			.anyMatch(m -> meetingKey.equals(m.getKey()));
		return has || pastTableModel.getObjects().stream()
				.anyMatch(m -> meetingKey.equals(m.getKey()));
	}
	
	public AdobeConnectMeeting getMeetingByKey(Long meetingKey) {
		Optional<AdobeConnectMeeting> has = upcomingTableModel.getObjects().stream()
				.filter(m -> meetingKey.equals(m.getKey())).findFirst();
		if(!has.isPresent()) {
			has = pastTableModel.getObjects().stream()
					.filter(m -> meetingKey.equals(m.getKey())).findFirst();
		}
		return has.isPresent() ? has.get() : null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(upcomingTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					doSelect(ureq, upcomingTableModel.getObject(se.getIndex()));
				}
			}
		} else if(pastTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					doSelect(ureq, pastTableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelect(UserRequest ureq, AdobeConnectMeeting meeting) {
		fireEvent(ureq, new SelectAdobeConnectMeetingEvent(meeting));
	}
}
