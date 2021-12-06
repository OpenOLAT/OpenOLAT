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
package org.olat.modules.portfolio.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.portfolio.ui.model.CourseTemplateRow;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseTemplateSearchDataModel extends DefaultFlexiTableDataModel<CourseTemplateRow>
	implements SortableFlexiTableDataModel<CourseTemplateRow> {
	
	public CourseTemplateSearchDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		SortableFlexiTableModelDelegate<CourseTemplateRow> sorter = new SortableFlexiTableModelDelegate<>(orderBy, this, null);
		List<CourseTemplateRow> rows = sorter.sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseTemplateRow page = getObject(row);
		return getValueAt(page, col);
	}
	
	@Override
	public Object getValueAt(CourseTemplateRow page, int col) {
		switch(CTCols.values()[col]) {
			case course: return page.getCourseTitle();
			case courseNode: return page.getCourseNodeTitle();
		}
		return null;
	}

	public enum CTCols implements FlexiSortableColumnDef {
		course("table.header.course"),
		courseNode("table.header.course.node");
		
		private final String i18nKey;
		
		private CTCols(String i18nKey) {
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
