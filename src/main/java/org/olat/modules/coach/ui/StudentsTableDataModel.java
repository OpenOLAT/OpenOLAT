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

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.coach.model.StudentStatEntry;
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
public class StudentsTableDataModel extends DefaultFlexiTableDataModel<StudentStatEntry> implements SortableFlexiTableDataModel<StudentStatEntry> {

	
	public StudentsTableDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		StudentStatEntry student = getObject(row);
		return getValueAt(student, col);
	}
	
	@Override
	public Object getValueAt(StudentStatEntry student, int col) {
		if(col >= 0 && col < Columns.values().length) {
			int countRepo = student.getCountRepo();
			switch(Columns.getValueAt(col)) {
				case name: return student.getIdentityName();
				case countCourse: return new Integer(countRepo);
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
		}

		int propPos = col - UserListController.USER_PROPS_OFFSET;
		return student.getIdentityProp(propPos);
	}

	@Override
	public StudentsTableDataModel createCopyWithEmptyList() {
		return new StudentsTableDataModel(getTableColumnModel());
	}
	
	public static enum Columns implements FlexiColumnDef {
		name("student.name"),
		countCourse("table.header.countCourses"),
		initialLaunch("table.header.login"),
		countPassed("table.header.passed"),
		countPassedLight("table.header.passed");
		
		private final String i18nKey;
		
		private Columns(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
		
		public static Columns getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return name;
		}
	}
}
