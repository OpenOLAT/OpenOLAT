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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.modules.openmeetings.model.OpenMeetingsRecording;

/**
 * 
 * Initial date: 12.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsRecordingsDataModel implements TableDataModel<OpenMeetingsRecording> {

	private List<OpenMeetingsRecording> recordings;
	
	public OpenMeetingsRecordingsDataModel() {
		//
	}
	
	public OpenMeetingsRecordingsDataModel(List<OpenMeetingsRecording> recordings) {
		this.recordings = recordings;
	}
	
	@Override
	public int getColumnCount() {
		return 0;
	}

	@Override
	public int getRowCount() {
		return recordings == null ? 0 : recordings.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		OpenMeetingsRecording recording = getObject(row);
		switch(Col.values()[col]) {
			case name: return recording.getFilename();
			default: return "ERROR";
		}
	}

	@Override
	public OpenMeetingsRecording getObject(int row) {
		return recordings.get(row);
	}

	@Override
	public void setObjects(List<OpenMeetingsRecording> objects) {
		recordings = new ArrayList<>(objects);
	}

	@Override
	public OpenMeetingsRecordingsDataModel createCopyWithEmptyList() {
		return new OpenMeetingsRecordingsDataModel();
	}

	public enum Col {
		name("recording.name");
		
		private final String i18nKey;
		
		private Col(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String getI18nKey() {
			return i18nKey;
		}
	}
}
