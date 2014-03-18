//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc;

import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;


/**
 * 
 * Description:<br>
 * Table model for run view of vc course node. Summarizes all planned dates for meetings.
 * 
 * <P>
 * Initial Date:  19.01.2011 <br>
 * @author skoeber
 */
public class VCDatesTableDataModel extends DefaultTableDataModel<MeetingDate> {
	
	//title, description, begin, end
	private static final int COLUMN_COUNT = 4;

	public VCDatesTableDataModel(List<MeetingDate> objects) {
		super(objects);
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		final MeetingDate model = objects.get(row);

		switch (col) {
			case 0:
				return model.getTitle();
			case 1:
				return model.getDescription();
			case 2:
				return model.getBegin();
			case 3:
				return model.getEnd();
			default:
				return "error";
		}
	}

	public MeetingDate getEntryAt(int row) {
		return objects.get(row);
	}

	public void setEntries(List<MeetingDate> newEntries) {
		this.objects = newEntries;
	}
}
//</OLATCE-103>