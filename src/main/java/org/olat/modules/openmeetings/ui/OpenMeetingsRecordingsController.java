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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.openmeetings.manager.OpenMeetingsException;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.modules.openmeetings.model.OpenMeetingsRecording;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 12.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsRecordingsController extends BasicController {

	private TableController tableCtr;
	private CloseableModalController cmc;
	private OpenMeetingsRecordingController recordingController;

	private final long roomId;
	private final OpenMeetingsManager openMeetingsManager;
	
	public OpenMeetingsRecordingsController(UserRequest ureq, WindowControl wControl, long roomId) {
		super(ureq, wControl);

		this.roomId = roomId;
		openMeetingsManager = CoreSpringFactory.getImpl(OpenMeetingsManager.class);

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("users.empty"));
		
		Translator trans = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
		listenTo(tableCtr);
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("room.name", OpenMeetingsRecordingsDataModel.Col.name.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("download", "download", translate("download")));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("view", "view", translate("view")));
		tableCtr.setTableDataModel(new OpenMeetingsRecordingsDataModel());
		loadModel();
		
		putInitialPanel(tableCtr.getInitialComponent());
	}
	
	private void loadModel() {
		try {
			List<OpenMeetingsRecording> recordings = openMeetingsManager.getRecordings(roomId);
			((OpenMeetingsRecordingsDataModel)tableCtr.getTableDataModel()).setObjects(recordings);
			tableCtr.modelChanged();
		} catch (OpenMeetingsException e) {
			showError(e.getType().i18nKey());
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent)event;
				int row = e.getRowId();
				OpenMeetingsRecording recording = (OpenMeetingsRecording)tableCtr.getTableDataModel().getObject(row);
				if("download".equals(e.getActionId())) {
					doDownload(ureq, recording);
				}else if("view".equals(e.getActionId())) {
					doView(ureq, recording);
				}
			}
		}
	}
	
	private void doView(UserRequest ureq, OpenMeetingsRecording recording) {
		removeAsListenerAndDispose(recordingController);
		removeAsListenerAndDispose(cmc);
		
		recordingController = new OpenMeetingsRecordingController(ureq, getWindowControl(), recording);
		listenTo(recordingController);

		String name = recording.getFilename();
		cmc = new CloseableModalController(getWindowControl(), translate("close"), recordingController.getInitialComponent(), true, name);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDownload(UserRequest ureq, OpenMeetingsRecording recording) {
		String url = openMeetingsManager.getRecordingURL(recording, null);
		System.out.println(url);
	}
}
