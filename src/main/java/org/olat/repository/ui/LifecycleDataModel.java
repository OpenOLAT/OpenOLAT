/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.repository.ui;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * Initial date: 10.06.2013<br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class LifecycleDataModel
		extends DefaultFlexiTableDataModel<RepositoryEntryLifecycle>
		implements SortableFlexiTableDataModel<RepositoryEntryLifecycle> {

	private static final LCCols[] COLS = LCCols.values();

	private final Locale locale;
	private Map<Long, Long> usageCounts = Map.of();
	private Map<RepositoryEntryLifecycle, FormLink> toolsLinks;

	public LifecycleDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		RepositoryEntryLifecycle cycle = getObject(row);
		return getValueAt(cycle, col);
	}

	public void setUsageCounts(Map<Long, Long> usageCounts) {
		this.usageCounts = usageCounts != null ? usageCounts : Map.of();
	}

	public void setToolsLinks(Map<RepositoryEntryLifecycle, FormLink> toolsLinks) {
		this.toolsLinks = toolsLinks != null ? toolsLinks : Map.of();
	}

	@Override
	public void sort(SortKey sortKey) {
		List<RepositoryEntryLifecycle> sorted = new RepositoryEntryLifecycleSortDelegate(sortKey, this, locale).sort();
		super.setObjects(sorted);
	}

	@Override
	public Object getValueAt(RepositoryEntryLifecycle row, int col) {
		return switch (COLS[col]) {
			case softkey -> row.getSoftKey();
			case label -> row.getLabel();
			case validFrom -> row.getValidFrom();
			case validTo -> row.getValidTo();
			case edit -> Boolean.TRUE;
			case defaultCycle -> row.isDefaultPublicCycle();
			case usages -> usageCounts.getOrDefault(row.getKey(), 0L);
			case tools -> toolsLinks.get(row);
		};
	}

	public enum LCCols implements FlexiSortableColumnDef {
		softkey("lifecycle.softkey"),
		label("lifecycle.label"),
		validFrom("lifecycle.validFrom"),
		validTo("lifecycle.validTo"),
		edit("table.header.edit"),
		defaultCycle("lifecycle.default"),
		usages("lifecycle.usages"),
		tools("action.more");

		private final String i18nKey;

		private LCCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		public String i18nKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return switch (this) {
				case softkey, label, validFrom, validTo, defaultCycle, usages -> true;
				default -> false;
			};
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
