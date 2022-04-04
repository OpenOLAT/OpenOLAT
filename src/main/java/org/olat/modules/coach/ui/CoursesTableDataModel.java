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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.coach.model.CourseStatEntry;
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
public class CoursesTableDataModel extends DefaultFlexiTableDataModel<CourseStatEntry>
	implements SortableFlexiTableDataModel<CourseStatEntry> {
	
	private static final Columns[] COLS = Columns.values();

	public CoursesTableDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	public int getIndexOfObject(CourseStatEntry entry) {
		return getObjects().indexOf(entry);
	}

	@Override
	public void sort(SortKey orderBy) {
		super.setObjects(new SortableFlexiTableModelDelegate<>(orderBy, this, null).sort());
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseStatEntry c = getObject(row);
		return getValueAt(c, col);
	}

	@Override
	public Object getValueAt(CourseStatEntry row, int col) {
		switch(COLS[col]) {
			case key: return row.getRepoKey();
			case name: return row.getRepoDisplayName();
			case externalId: return row.getRepoExternalId();
			case externalRef: return row.getRepoExternalRef();
			case access: return row.getRepoStatus();
			case countStudents: return Integer.valueOf(row.getCountStudents());
			case initialLaunch: {
				int count = row.getCountStudents();
				if(count == 0) {
					return new LightedValue(null, Light.grey);
				}

				int launch = row.getInitialLaunch();
				Light light = Light.yellow;
				if(launch == count) {
					light = Light.green;
				} else if (launch == 0) {
					light = Light.red;
				}
				return new LightedValue(launch, light);
			}
			case completion: return row.getAverageCompletion();
			case countPassed: {
				int numOfStudents = row.getCountStudents();
				if(numOfStudents == 0) {
					return numOfStudents;
				}
				
				ProgressValue val = new ProgressValue();
				val.setTotal(numOfStudents);
				val.setGreen(row.getCountPassed());
				return val;
			}
			case countPassedLight: {
				int count = row.getCountStudents();
				if(count == 0) {
					return new LightedValue(null, Light.grey);
				}
				
				int passed = row.getCountPassed();
				Light light = Light.yellow;
				if(passed == count) {
					light = Light.green;
				} else if (passed == 0) {
					light = Light.red;
				}
				return new LightedValue(row.getCountPassed(), light);
			}
			case averageScore: return row.getAverageScore();
			default: return "ERROR";
		}
	}
	
	public enum Columns implements FlexiSortableColumnDef {
		key("table.header.course.key"),
		name("table.header.course.name"),
		externalId("table.header.course.externalId"),
		externalRef("table.header.course.externalRef"),
		access("table.header.course.access"),
		countStudents("table.header.countStudents"),
		initialLaunch("table.header.login"),
		completion("table.header.completion"),
		countPassed("table.header.passed"),
		countPassedLight("table.header.passed"),
		averageScore("table.header.averageScore");
		
		private final String i18nKey;
		
		private Columns(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		public static Columns getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return name;
		}
	}
}
