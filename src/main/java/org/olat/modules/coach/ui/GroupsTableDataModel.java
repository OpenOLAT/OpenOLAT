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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.ui.LightedValue.Light;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupsTableDataModel implements TableDataModel<GroupStatEntry> {
	
	private List<GroupStatEntry> groups;
	
	public GroupsTableDataModel(List<GroupStatEntry> groups) {
		this.groups = groups;
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public int getRowCount() {
		return groups == null ? 0 : groups.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		GroupStatEntry g = groups.get(row);
		int numOfStudents = g.getCountStudents();
		switch(Columns.getValueAt(col)) {
			case name: return g.getGroupName();
			case countCourses: return Integer.valueOf(g.getCountCourses());
			case countStudents: return Integer.valueOf(numOfStudents);
			case initialLaunch: {
				int count = g.getCountCourses() * g.getCountStudents();
				if(count == 0) {
					return new LightedValue(null, Light.grey);
				}

				int launch = g.getInitialLaunch();
				Light light = Light.yellow;
				if(launch == count) {
					light = Light.green;
				} else if (launch == 0) {
					light = Light.red;
				}
				return new LightedValue(launch, light);
			}
			case countPassed: {
				if(numOfStudents == 0) {
					return numOfStudents;
				}
				
				ProgressValue val = new ProgressValue();
				val.setTotal(numOfStudents);
				val.setGreen(g.getCountPassed());
				return val;
			}
			case countPassedLight: {
				if(numOfStudents == 0) {
					return new LightedValue(null, Light.grey);
				}
				
				int passed = g.getCountPassed();
				Light light = Light.yellow;
				if(passed == numOfStudents) {
					light = Light.green;
				} else if (passed == 0) {
					light = Light.red;
				}
				return new LightedValue(passed, light);
			}
			case averageScore: return g.getAverageScore();
			case statistics: {
				return g;
			}
		}
		return null;
	}

	@Override
	public GroupStatEntry getObject(int row) {
		return groups.get(row);
	}

	@Override
	public void setObjects(List<GroupStatEntry> objects) {
		groups = objects;
	}

	@Override
	public GroupsTableDataModel createCopyWithEmptyList() {
		return new GroupsTableDataModel(new ArrayList<GroupStatEntry>());
	}
	
	public enum Columns {
		name,
		countCourses,
		countStudents,
		initialLaunch,
		countPassed,
		countPassedLight,
		averageScore,
		statistics;

		public static Columns getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return name;
		}
	}
}
