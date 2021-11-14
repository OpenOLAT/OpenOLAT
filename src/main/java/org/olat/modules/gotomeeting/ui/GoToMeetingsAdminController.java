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

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.ui.GoToMeetingTableModel.MeetingsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * The administration of all meetings / trainings in OpenOLAT.
 * 
 * Initial date: 24.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToMeetingsAdminController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private GoToMeetingTableModel tableModel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDelete;

	
	@Autowired
	private GoToMeetingManager meetingMgr;
	
	public GoToMeetingsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "meetings_admin");
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.name.i18nHeaderKey(), MeetingsCols.name.ordinal(), true, MeetingsCols.name.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.start.i18nHeaderKey(), MeetingsCols.start.ordinal(), true, MeetingsCols.start.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.end.i18nHeaderKey(), MeetingsCols.end.ordinal(), true, MeetingsCols.end.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.organizer.i18nHeaderKey(), MeetingsCols.organizer.ordinal(), true, MeetingsCols.organizer.name()));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("resource", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.resource.i18nHeaderKey(), MeetingsCols.resource.ordinal(), "resource",
				true, MeetingsCols.resource.name(), renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		
		tableModel = new GoToMeetingTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, getTranslator(), formLayout);
		updateModel();
	}
	
	private void updateModel() {
		List<GoToMeeting> meetings = meetingMgr.getAllMeetings();
		tableModel.setObjects(meetings);
		tableEl.reloadData();
		tableEl.reset();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				GoToMeeting meeting = (GoToMeeting)confirmDelete.getUserObject();
				doDelete(meeting);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDelete);
		removeAsListenerAndDispose(cmc);
		confirmDelete = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					GoToMeeting meeting = tableModel.getObject(se.getIndex());
					doConfirmDelete(ureq, meeting);
				} else if("resource".equals(se.getCommand())) {
					GoToMeeting meeting = tableModel.getObject(se.getIndex());
					doOpenResource(ureq, meeting);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenResource(UserRequest ureq, GoToMeeting meeting) {
		if(meeting.getEntry() != null) {
			String businessPath = "[RepositoryEntry:" + meeting.getEntry().getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else if(meeting.getBusinessGroup() != null) {
			String businessPath = "[BusinessGroup:" + meeting.getBusinessGroup().getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, GoToMeeting meeting) {
		String confirmDeleteText = translate("confirm.delete.meeting", new String[]{ meeting.getName() });
		confirmDelete = activateYesNoDialog(ureq, translate("delete"), confirmDeleteText, confirmDelete);
		confirmDelete.setUserObject(meeting);
	}
	
	private void doDelete(GoToMeeting meeting) {
		meetingMgr.delete(meeting);
		updateModel();
	}
}
