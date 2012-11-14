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

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;

/**
 * 
 * Initial date: 08.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsRoomsDataModel implements TableDataModel<OpenMeetingsRoom> {
	
	private List<OpenMeetingsRoom> rooms;
	
	
	public OpenMeetingsRoomsDataModel() {
		//
	}
	
	public OpenMeetingsRoomsDataModel(List<OpenMeetingsRoom> rooms) {
		this.rooms = rooms;
	}
	

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public int getRowCount() {
		return rooms == null ? 0 : rooms.size();
	}
	
	@Override
	public OpenMeetingsRoom getObject(int row) {
		return rooms.get(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		OpenMeetingsRoom room = getObject(row);
		switch(Column.values()[col]) {
			case name: {
				String name = room.getName();
				return name;
			}

			case roomSize: {
				long roomSize = room.getSize();
				if(roomSize > 0) {
					return Long.toString(roomSize);
				}
				return "-";
			}
			case numOfUsers: {
				int numOfUsers = room.getNumOfUsers();
				if(numOfUsers > 0) {
					return Integer.toString(numOfUsers);
				}
				return "-";
			}
			case resource: {
				String resourceName = room.getResourceName();
				if(StringHelper.containsNonWhitespace(resourceName)) {
					return resourceName;
				}
				return "???";
			}
			default: return "";
		}
	}

	@Override
	public void setObjects(List<OpenMeetingsRoom> objects) {
		this.rooms = objects;
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new OpenMeetingsRoomsDataModel();
	}

	public enum Column {
		name,
		begin,
		end,
		group,
		roomSize,
		numOfUsers,
		resource,
		open,
		sign,
	}
}