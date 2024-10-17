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

import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_ACTIVE;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_CANCELLED;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_CONFIRMED;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_DELETED;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_DETAILS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_FINISHED;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_IMPLEMENTATIONS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_PREPARATION;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_PROVISIONAL;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.SUB_PATH_ACTIVE;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.SUB_PATH_CANCELLED;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.SUB_PATH_CONFIRMED;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.SUB_PATH_DELETED;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.SUB_PATH_DETAILS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.SUB_PATH_FINISHED;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.SUB_PATH_IMPLEMENTATIONS;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.SUB_PATH_PREPARATION;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.SUB_PATH_PROVISIONAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 13 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumManagerDataModel extends DefaultFlexiTableDataModel<CurriculumRow>
implements SortableFlexiTableDataModel<CurriculumRow>, FilterableFlexiTableModel, FlexiBusinessPathModel {
	
	private static final CurriculumCols[] COLS = CurriculumCols.values();
	
	private final Locale locale;
	private List<CurriculumRow> backups;
	
	public CurriculumManagerDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CurriculumRow> rows = new CurriculumManagerTableSort(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		FlexiTableFilter filter = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0);
		if(filter != null && !filter.isShowAll()) {
			List<CurriculumRow> filteredRows;
			if("active".equals(filter.getFilter())) {
				filteredRows = backups.stream()
						.filter(CurriculumRow::isActive)
						.toList();
			} else if("inactive".equals(filter.getFilter())) {
				filteredRows = backups.stream()
						.filter(node -> !node.isActive())
						.toList();
			} else {
				filteredRows = new ArrayList<>(backups);
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public String getUrl(Component source, Object object, String action) {
		if(object instanceof CurriculumRow row) {
			if(action == null || row.getBaseUrl() == null) {
				return row.getBaseUrl();
			}
			return switch(action) {
				case CONTEXT_DETAILS -> row.getBaseUrl().concat(SUB_PATH_DETAILS);
				case CONTEXT_IMPLEMENTATIONS -> row.getBaseUrl().concat(SUB_PATH_IMPLEMENTATIONS);
				case CONTEXT_PREPARATION -> row.getBaseUrl().concat(SUB_PATH_PREPARATION);
				case CONTEXT_PROVISIONAL -> row.getBaseUrl().concat(SUB_PATH_PROVISIONAL);
				case CONTEXT_CONFIRMED -> row.getBaseUrl().concat(SUB_PATH_CONFIRMED);
				case CONTEXT_ACTIVE -> row.getBaseUrl().concat(SUB_PATH_ACTIVE);
				case CONTEXT_CANCELLED -> row.getBaseUrl().concat(SUB_PATH_CANCELLED);
				case CONTEXT_FINISHED -> row.getBaseUrl().concat(SUB_PATH_FINISHED);
				case CONTEXT_DELETED -> row.getBaseUrl().concat(SUB_PATH_DELETED);
				default -> row.getBaseUrl();
			};
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumRow curriculum = getObject(row);
		return getValueAt(curriculum, col);
	}

	@Override
	public Object getValueAt(CurriculumRow row, int col) {
		return switch(COLS[col]) {
			case key -> row.getKey();
			case active -> row.isActive();
			case displayName -> row.getDisplayName();
			case externalRef -> row.getExternalRef();
			case externalId -> row.getExternalId();
			case organisation -> row.getOrganisation();
			case numOfElements -> row.getImplementationsStatistics().numOfRootElements();
			case numOfPreparationRootElements -> row.getImplementationsStatistics().numOfPreparationRootElements();
			case numOfProvisionalRootElements -> row.getImplementationsStatistics().numOfProvisionalRootElements();
			case numOfConfirmedRootElements -> row.getImplementationsStatistics().numOfConfirmedRootElements();
			case numOfActiveRootElements -> row.getImplementationsStatistics().numOfActiveRootElements();
			case numOfCancelledRootElements -> row.getImplementationsStatistics().numOfCancelledRootElements();
			case numOfFinishedRootElements -> row.getImplementationsStatistics().numOfFinishedRootElements();
			case numOfDeletedRootElements -> row.getImplementationsStatistics().numOfDeletedRootElements();
			case status -> row.getStatus();
			case lectures -> row.isLecturesEnabled();
			case tools -> row.getTools();
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CurriculumRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}
	
	public enum CurriculumCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		active("table.header.active"),
		displayName("table.header.displayName"),
		externalRef("table.header.external.ref"),
		externalId("table.header.external.id"),
		numOfElements("table.header.num.elements"),
		numOfPreparationRootElements("table.header.num.elements.preparation"),
		numOfProvisionalRootElements("table.header.num.elements.provisional"),
		numOfConfirmedRootElements("table.header.num.elements.confirmed"),
		numOfActiveRootElements("table.header.num.elements.active"),
		numOfCancelledRootElements("table.header.num.elements.cancelled"),
		numOfFinishedRootElements("table.header.num.elements.finished"),
		numOfDeletedRootElements("table.header.num.elements.deleted"),
		status("table.header.status"),
		lectures("table.header.lectures"),
		tools("table.header.tools"),
		organisation("table.header.organisation");
		
		private final String i18nHeaderKey;
		
		private CurriculumCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
