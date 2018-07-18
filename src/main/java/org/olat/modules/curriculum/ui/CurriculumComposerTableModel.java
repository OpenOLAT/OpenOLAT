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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumComposerTableModel extends DefaultFlexiTreeTableDataModel<CurriculumElementRow> {
	
	public CurriculumComposerTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void filter(List<FlexiTableFilter> filters) {
		//
	}

	@Override
	public boolean hasChildren(int row) {
		CurriculumElementRow element = getObject(row);
		return element.hasChildren();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementRow element = getObject(row);
		switch(ElementCols.values()[col]) {
			case key: return element.getKey();
			case displayName: return element.getDisplayName();
			case identifier: return element.getIdentifier();
			case externalId: return element.getExternalId();
			case beginDate: return element.getBeginDate();
			case endDate: return element.getEndDate();
			case resources: return element.getResources();
			case status: return element.getStatus();
			case tools: return element.getTools();
			default: return "ERROR";
		}
	}
	
	@Override
	public CurriculumComposerTableModel createCopyWithEmptyList() {
		return new CurriculumComposerTableModel(getTableColumnModel());
	}
	
	public enum ElementCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.displayName"),
		identifier("table.header.identifier"),
		externalId("table.header.external.id"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		resources("table.header.resources"),
		status("table.header.status"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private ElementCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
