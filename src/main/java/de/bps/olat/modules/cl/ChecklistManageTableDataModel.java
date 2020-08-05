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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.id.Identity;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import de.bps.olat.modules.cl.ChecklistManageTableDataModel.Row;

public class ChecklistManageTableDataModel extends DefaultTableDataModel<Row> {
	
	private int colCount;
	private int rowCount;

	public ChecklistManageTableDataModel(List<Checkpoint> checkpointList, List<Identity> participants,
			List<UserPropertyHandler> userPropertyHandlers, int cols) {
		super(Collections.<Row>emptyList());
		
		colCount = cols;
		rowCount = participants.size();
		
		List<Row> entries = new ArrayList<>(rowCount);
		for( Identity identity : participants ) {
			entries.add(new Row(identity, userPropertyHandlers, checkpointList, Locale.ENGLISH));
		}
		setObjects(entries);
	}

	@Override
	public int getColumnCount() {
		// name, 1-n checkpoints, action
		return colCount;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		Row rowObj = getObject(row);
		if(col < 500) {
			String[] props = rowObj.getIdentityProps();
			if(col >= 0 && col < props.length) {
				return props[col];
			}
		} else {
			Boolean[] props = rowObj.getCheckpoints();
			int index = col - 500;
			if(index >= 0 && index < props.length) {
				return props[index];
			}
		}
		return "";
	}
	
	public Long getParticipantKeyAt(int row) {
		Row rowObj = getObject(row);
		return rowObj.getIdentityKey();
	}
	
	protected static class Row {
		private final Long identityKey;
		private final String identityName;
		private final String[] identityProps;
		private final Boolean[] checkpoints;
		
		public Row(Identity identity, List<UserPropertyHandler> userPropertyHandlers, List<Checkpoint> checkpointList, Locale locale) {
			this.identityKey = identity.getKey();
			this.identityName = identity.getName();
			
			identityProps = new String[userPropertyHandlers.size()];
			for(int i=userPropertyHandlers.size(); i-->0; ) {
				identityProps[i] = userPropertyHandlers.get(i).getUserProperty(identity.getUser(), locale);
			}
			
			checkpoints = new Boolean[checkpointList.size()];
			for( int i=checkpointList.size(); i-->0; ) {
				checkpoints[i] =  checkpointList.get(i).getSelectionFor(identity);
			}
		}

		public Long getIdentityKey() {
			return identityKey;
		}

		public String getIdentityName() {
			return identityName;
		}

		public String[] getIdentityProps() {
			return identityProps;
		}

		public Boolean[] getCheckpoints() {
			return checkpoints;
		}
	}
}
