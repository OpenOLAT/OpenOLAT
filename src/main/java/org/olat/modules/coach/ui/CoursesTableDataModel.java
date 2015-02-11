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
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.ui.ProgressValue;

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
public class CoursesTableDataModel implements TableDataModel<CourseStatEntry> {
	
	private List<CourseStatEntry> statistics;
	
	public CoursesTableDataModel(List<CourseStatEntry> cours) {
		this.statistics = cours;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public int getRowCount() {
		return statistics == null ? 0 : statistics.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseStatEntry c = statistics.get(row);
		switch(Columns.getValueAt(col)) {
			case name: {
				return c.getRepoDisplayName();
			}
			case countStudents: {
				return new Integer(c.getCountStudents());
			}
			case initialLaunch: {
				int count = c.getCountStudents();
				if(count == 0) {
					return new LightedValue(null, Light.grey);
				}

				int launch = c.getInitialLaunch();
				Light light = Light.yellow;
				if(launch == count) {
					light = Light.green;
				} else if (launch == 0) {
					light = Light.red;
				}
				return new LightedValue(launch, light);
			}
			case countPassed: {
				int numOfStudents = c.getCountStudents();
				if(numOfStudents == 0) {
					return numOfStudents;
				}
				
				ProgressValue val = new ProgressValue();
				val.setTotal(numOfStudents);
				val.setGreen(c.getCountPassed());
				return val;
			}
			case countPassedLight: {
				int count = c.getCountStudents();
				if(count == 0) {
					return new LightedValue(null, Light.grey);
				}
				
				int passed = c.getCountPassed();
				Light light = Light.yellow;
				if(passed == count) {
					light = Light.green;
				} else if (passed == 0) {
					light = Light.red;
				}
				return new LightedValue(c.getCountPassed(), light);
			}
			case averageScore: return c.getAverageScore();
		}
		return null;
	}

	@Override
	public CourseStatEntry getObject(int row) {
		return statistics.get(row);
	}

	@Override
	public void setObjects(List<CourseStatEntry> objects) {
		statistics = objects;
	}

	@Override
	public CoursesTableDataModel createCopyWithEmptyList() {
		return new CoursesTableDataModel(new ArrayList<CourseStatEntry>());
	}
	
	public static enum Columns {
		name,
		countStudents,
		initialLaunch,
		countPassed,
		countPassedLight,
		averageScore;

		public static Columns getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return name;
		}
	}
}
