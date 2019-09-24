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
package de.bps.course.nodes.den;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;

public class DENEditTableDataModel extends DefaultTableDataModel<KalendarEvent> {
	
	public static final String CHANGE_ACTION = "denDateChange";
	public static final String DELETE_ACTION = "denDateDelete";
	
	// begin, end, location, comment, num participants
	private static final int COLUMN_COUNT = 5;
	
	private Translator translator;
	private DENManager denManager;
	
	public DENEditTableDataModel(List<KalendarEvent> objects, Translator translator) {
		super(objects);
		
		this.translator = translator;
		denManager = DENManager.getInstance();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		KalendarEvent event = getObject(row);
		
		switch (col) {
			case 0: return event.getSubject();
			case 1:
				//begin
				return event.getBegin();
			case 2:
				//duration
				Date begin = event.getBegin();
				Date end = event.getEnd();
				long milliSeconds = denManager.getDuration(begin, end);
				
				return denManager.formatDuration(milliSeconds, translator);
			case 3:
				//location
				return event.getLocation();
			case 4: return event.getComment();
			case 5: return event.getNumParticipants();
		default:	return "error";
		}
	}
	
	public void removeEntries(BitSet choosenEntries) {
		Collection<Object> delList = new ArrayList<>();
		for (int i = 0; i < objects.size(); i++) {
			if(choosenEntries.get(i))
				delList.add(objects.get(i));
		}
		if(delList.size() > 0)
			objects.removeAll(delList);
	}
}
