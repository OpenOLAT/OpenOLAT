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
package de.bps.olat.modules.cl;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;

public class ChecklistManageTableDataModel extends DefaultTableDataModel {
	
	private static int COLUMN_COUNT;
	private static int ROW_COUNT;
	
	private Checklist checklist;
	private List<Identity> participants;
	private List entries;

	@SuppressWarnings("unchecked")
	public ChecklistManageTableDataModel(Checklist checklist, List<Identity> participants) {
		super(participants);
		this.checklist = checklist;
		this.participants = participants;
		
		COLUMN_COUNT = checklist.getCheckpoints().size() + 2;
		ROW_COUNT = participants.size();
		
		this.entries = new ArrayList(ROW_COUNT);
		for( Identity identity : participants ) {
			List row = new ArrayList(COLUMN_COUNT);
			// name
			row.add(
					identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()) + " " +
					identity.getUser().getProperty(UserConstants.LASTNAME, getLocale()));
			// checkpoints value
			for( Checkpoint checkpoint : this.checklist.getCheckpointsSorted(ChecklistUIFactory.comparatorTitleAsc) ) {
				row.add(checkpoint.getSelectionFor(identity));
			}
			// action
			row.add(true);
			// add to columns
			entries.add(row);
		}
	}

	public int getColumnCount() {
		// name, 1-n checkpoints, action
		return COLUMN_COUNT;
	}
	
	public int getRowCount() {
		return ROW_COUNT;
	}
	
	public Object getValueAt(int row, int col) {
		List entry = (List)entries.get(row);
		return entry.get(col);
	}
	
	public Identity getParticipantAt(int row) {
		return participants.get(row);
	}
}
