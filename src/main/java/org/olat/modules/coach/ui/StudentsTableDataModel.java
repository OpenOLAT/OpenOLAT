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
import java.util.Map;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.modules.coach.model.StudentStatEntry;
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
public class StudentsTableDataModel implements TableDataModel<StudentStatEntry> {

	private List<StudentStatEntry> students;

	private Map<Long,String> identityFullNameMap;
	
	public StudentsTableDataModel(List<StudentStatEntry> identities, Map<Long,String> identityFullNameMap) {
		this.students = identities;
		this.identityFullNameMap = identityFullNameMap;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return getEntries() == null ? 0 : getEntries().size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		StudentStatEntry student = getEntries().get(row);
		int countRepo = student.getCountRepo();
		
		switch(Columns.getValueAt(col)) {
			case name: {
				Long name = student.getStudentKey();
				return identityFullNameMap.get(name);
			}
			case countCourse: {
				return new Integer(countRepo);
			}
			case initialLaunch: {
				if(countRepo == 0) {
					return null;
				}
				
				int launched = student.getInitialLaunch();
				Light light = Light.yellow;
				if(launched == countRepo) {
					light = Light.green;
				} else if (launched == 0) {
					light = Light.red;
				}
				return new LightedValue(launched, light);
			}
			case countPassed: {
				if(countRepo == 0) {
					return null;
				}

				ProgressValue val = new ProgressValue();
				val.setTotal(countRepo);
				val.setGreen(student.getCountPassed());
				return val;
			}
			case countPassedLight: {
				if(countRepo == 0) {
					return null;
				}
				int passed = student.getCountPassed();
				Light light = Light.yellow;
				if(passed == countRepo) {
					light = Light.green;
				} else if (passed == 0) {
					light = Light.red;
				}
				return new LightedValue(passed, light);
			}
		}
		return null;
	}
	
	public List<StudentStatEntry> getEntries() {
		return students;
	}

	@Override
	public StudentStatEntry getObject(int row) {
		return getEntries().get(row);
	}

	@Override
	public void setObjects(List<StudentStatEntry> objects) {
		students = objects;
	}

	@Override
	public StudentsTableDataModel createCopyWithEmptyList() {
		return new StudentsTableDataModel(new ArrayList<StudentStatEntry>(), identityFullNameMap);
	}
	
	public static enum Columns {
		name,
		countCourse,
		initialLaunch,
		countPassed,
		countPassedLight;
		

		public static Columns getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return name;
		}
	}
}
