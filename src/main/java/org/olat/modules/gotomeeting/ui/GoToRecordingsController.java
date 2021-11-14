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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.model.GoToError;
import org.olat.modules.gotomeeting.model.GoToRecordingsG2T;
import org.olat.modules.gotomeeting.ui.GoToRecordingsTableModel.RecordingsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToRecordingsController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private GoToRecordingsTableModel tableModel;
	
	private CloseableModalController cmc;
	private GoToRecordingController recordingController;
	
	private final GoToMeeting meeting;
	
	@Autowired
	private GoToMeetingManager meetingMgr;
	
	public GoToRecordingsController(UserRequest ureq, WindowControl wControl, GoToMeeting meeting) {
		super(ureq, wControl, "recordings");
		this.meeting = meeting;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecordingsCols.name.i18nHeaderKey(), RecordingsCols.name.ordinal(), true, RecordingsCols.name.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecordingsCols.start.i18nHeaderKey(), RecordingsCols.start.ordinal(), true, RecordingsCols.start.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RecordingsCols.end.i18nHeaderKey(), RecordingsCols.end.ordinal(), true, RecordingsCols.end.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));

		tableModel = new GoToRecordingsTableModel(columnsModel);
		GoToError error = new GoToError();
		List<GoToRecordingsG2T> recordings = meetingMgr.getRecordings(meeting, error);
		if(recordings == null) {
			recordings = new ArrayList<>(1);
		}
		tableModel.setObjects(recordings);
		tableEl = uifactory.addTableElement(getWindowControl(), "recordings", tableModel, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("recordings.empty");
		tableEl.setCustomizeColumns(false);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(recordingController == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(recordingController);
		removeAsListenerAndDispose(cmc);
		recordingController = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					GoToRecordingsG2T recording = tableModel.getObject(se.getIndex());
					doView(ureq, recording);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doView(UserRequest ureq, GoToRecordingsG2T recording) {
		if(guardModalController(recordingController)) return;
		
		recordingController = new GoToRecordingController(ureq, getWindowControl(), recording);
		listenTo(recordingController);

		String name = recording.getName();
		cmc = new CloseableModalController(getWindowControl(), translate("close"), recordingController.getInitialComponent(), true, name);
		listenTo(cmc);
		cmc.activate();
	}
}
