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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
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
public class StudentsTableDataModel extends DefaultFlexiTableDataModel<StudentStatEntry>
	implements SortableFlexiTableDataModel<StudentStatEntry> {
	
	private static final Logger log = Tracing.createLoggerFor(StudentsTableDataModel.class);
	private static final Columns[] COLS = Columns.values();

	private List<StudentStatEntry> backupList;
	
	public StudentsTableDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	public void search(final String searchString) {
		if(StringHelper.containsNonWhitespace(searchString)) {
			try {
				List<StudentStatEntry> filteredList;
				if(StringHelper.isLong(searchString)) {
					Long identityKey = Long.valueOf(searchString);
					filteredList = backupList.stream()
						.filter(entry ->  entry.getIdentityKey().equals(identityKey))
						.collect(Collectors.toList());
				} else {
					final String loweredSearchString = searchString.toLowerCase();
					filteredList = backupList.stream()
						.filter(entry -> StudentListProvider.contains(loweredSearchString, entry))
						.collect(Collectors.toList());
				}
				super.setObjects(filteredList);
			} catch (Exception e) {
				log.error("", e);
				super.setObjects(backupList);
			}
		} else {
			super.setObjects(backupList);
		}
	}

	@Override
	public void sort(SortKey orderBy) {
		super.setObjects(new SortableFlexiTableModelDelegate<>(orderBy, this, null).sort());
	}

	@Override
	public Object getValueAt(int row, int col) {
		StudentStatEntry student = getObject(row);
		return getValueAt(student, col);
	}
	
	@Override
	public Object getValueAt(StudentStatEntry student, int col) {
		if(col >= 0 && col < COLS.length) {
			int countRepo = student.getCountRepo();
			switch(COLS[col]) {
				case countCourse: return Integer.valueOf(countRepo);
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
				case completion: return student.getAverageCompletion();
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
				default: return "ERROR";
			}
		}

		int propPos = col - UserListController.USER_PROPS_OFFSET;
		return student.getIdentityProp(propPos);
	}
	
	@Override
	public void setObjects(List<StudentStatEntry> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}
	
	public enum Columns implements FlexiSortableColumnDef {
		countCourse("table.header.countCourses"),
		initialLaunch("table.header.login"),
		completion("table.header.completion"),
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
			return null;
		}
	}
}
