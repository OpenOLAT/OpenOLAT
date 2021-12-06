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
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.portfolio.ui.model.AssignmentTemplateRow;

/**
 * 
 * Initial date: 13 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentTemplatesDataModel extends DefaultFlexiTableDataModel<AssignmentTemplateRow>
implements SortableFlexiTableDataModel<AssignmentTemplateRow> {
	
	private final Locale locale;
	
	public AssignmentTemplatesDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AssignmentTemplateRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssignmentTemplateRow assignment = getObject(row);
		return getValueAt(assignment, col);
	}

	@Override
	public Object getValueAt(AssignmentTemplateRow row, int col) {
		switch(TemplateCols.values()[col]) {
			case type: return row.getAssignment().getAssignmentType();
			case name: return row.getAssignment().getTitle();
			case creationDate: return row.getAssignment().getCreationDate();
			case action: return row.getActionLink();
			default: return "ERROR";
		}
	}
	
	public enum TemplateCols implements FlexiSortableColumnDef {
		type("table.header.template.type"),
		name("table.header.template.name"),
		creationDate("table.header.template.creation.date"),
		action("table.header.action");
		
		private final String i18nKey;
		
		private TemplateCols(String i18nKey) {
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
