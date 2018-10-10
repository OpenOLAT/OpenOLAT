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
package org.olat.course.assessment.ui.tool;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.assessment.model.AssessedBusinessGroup;
import org.olat.modules.coach.ui.ProgressValue;

/**
 * 
 * Initial date: 09.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedBusinessGroupTableModel extends DefaultFlexiTableDataModel<AssessedBusinessGroup>
implements SortableFlexiTableDataModel<AssessedBusinessGroup> {
	
	private final Locale locale;
	
	public AssessedBusinessGroupTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<AssessedBusinessGroup> views = new AssessedBusinessGroupSorter(orderBy, this, locale).sort();
		super.setObjects(views);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessedBusinessGroup groupRow = getObject(row);
		return getValueAt(groupRow, col);
	}

	@Override
	public Object getValueAt(AssessedBusinessGroup row, int col) {
		switch(ABGCols.values()[col]) {
			case key: return row.getKey();
			case name: return row.getName();
			case countPassed: {
				int numOfParticipants = row.getNumOfParticipants();
				if(numOfParticipants == 0) {
					return numOfParticipants;
				}
				ProgressValue val = new ProgressValue();
				val.setTotal(numOfParticipants);
				val.setGreen(row.getNumOfPassed());
				return val;
			}
			case averageScore: {
				if(row.getNumOfParticipants() == row.getNumOfNotAttempted()) {
					return null;
				}
				if(row.getAverageScore() == 0.0) {
					return null;
				}
				return row.getAverageScore();
			}
		}
		return null;
	}

	@Override
	public DefaultFlexiTableDataModel<AssessedBusinessGroup> createCopyWithEmptyList() {
		return new AssessedBusinessGroupTableModel(getTableColumnModel(), locale);
	}

	public enum ABGCols implements FlexiSortableColumnDef {
		key("table.header.id"),
		name("table.header.group.name"),
		countPassed("table.header.passed"),
		averageScore("table.header.scoreAverage");
		
		private final String i18nKey;
		
		private ABGCols(String i18nKey) {
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
	}
}
