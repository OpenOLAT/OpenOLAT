/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.list;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.CurriculumElementStatus;

/**
 * 
 * Initial date: 23 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationsListDataModel extends DefaultFlexiTableDataModel<ImplementationRow>
implements SortableFlexiTableDataModel<ImplementationRow> {
	
	private static final ImplementationsCols[] COLS = ImplementationsCols.values();
	private static final List<CurriculumElementStatus> ACTIVE_STATUS = List.of(CurriculumElementStatus.preparation,
			CurriculumElementStatus.provisional, CurriculumElementStatus.confirmed, CurriculumElementStatus.active);
	private static final List<CurriculumElementStatus> FINISHED_STATUS = List.of(CurriculumElementStatus.finished);
	
	private final Locale locale;
	private List<ImplementationRow> backups;
	
	public ImplementationsListDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	public boolean hasMarked() {
		if(backups == null || backups.isEmpty()) return false;
		return backups.stream().anyMatch(ImplementationRow::isMarked);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ImplementationRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	public void filter(String tabId) {
		if(ImplementationsListController.FAVORITE_TAB.equals(tabId)) {
			List<ImplementationRow> filtered = backups.stream()
					.filter(ImplementationRow::isMarked)
					.toList();
			super.setObjects(filtered);
		} else if(ImplementationsListController.ACTIVE_TAB.equals(tabId)) {
			List<ImplementationRow> filtered = backups.stream()
					.filter(r -> ACTIVE_STATUS.contains(r.getCurriculumElement().getElementStatus()))
					.toList();
			super.setObjects(filtered);
		} else if(ImplementationsListController.FINISHED_TAB.equals(tabId)) {
			List<ImplementationRow> filtered = backups.stream()
					.filter(r -> FINISHED_STATUS.contains(r.getCurriculumElement().getElementStatus()))
					.toList();
			super.setObjects(filtered);
		} else {
			super.setObjects(backups);
		}
	}
	
	public ImplementationRow getObjectByKey(Long key) {
		return backups.stream()
				.filter(r -> key.equals(r.getKey()))
				.findFirst().orElse(null);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ImplementationRow implementationRow = getObject(row);
		return getValueAt(implementationRow, col);
	}

	@Override
	public Object getValueAt(ImplementationRow row, int col) {
		return switch(COLS[col]) {
			case key -> row.getKey();
			case displayName -> row.getDisplayName();
			case externalRef -> row.getIdentifier();
			case lifecycleStart -> row.getBeginDate();
			case lifecycleEnd -> row.getEndDate();
			case mark -> row.getMarkLink();
			default -> "ERROR";
		};
	}	
	
	@Override
	public void setObjects(List<ImplementationRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}
	
	public enum ImplementationsCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("cif.title"),
		externalRef("table.header.externalref"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		mark("table.header.mark");
		
		private final String i18nKey;
		
		private ImplementationsCols(String i18nKey) {
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
