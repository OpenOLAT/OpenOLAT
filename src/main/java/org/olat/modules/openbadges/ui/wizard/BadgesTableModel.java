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
package org.olat.modules.openbadges.ui.wizard;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.openbadges.BadgeClass;

/**
 * Initial date: 2024-08-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgesTableModel extends DefaultFlexiTableDataModel<BadgesRow> implements SortableFlexiTableDataModel<BadgesRow> {

	private final Locale locale;

	public BadgesTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		BadgesRow badgesRow = getObject(row);
		return getValueAt(badgesRow, col);
	}

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
			List<BadgesRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
			super.setObjects(rows);
		}
	}

	public Object getValueAt(BadgesRow row, int col) {
		BadgeClass badgeClass = row.badgeClassWithSizeAndCount().badgeClass();
		return switch (BadgesCols.values()[col]) {
			case image -> badgeClass.getImage();
			case title -> badgeClass.getName();
			case assertions -> row.badgeClassWithSizeAndCount().count();
			case course -> badgeClass.getEntry().getDisplayname();
			case courseReference -> badgeClass.getEntry().getExternalRef();
		};
	}

	public enum BadgesCols implements FlexiSortableColumnDef {
		image("form.image"),
		title("var.title"),
		assertions("openBadges.assertions"),
		course("form.course"),
		courseReference("form.course.reference");

		private final String i18nKey;

		BadgesCols(String i18nKey) {
			this.i18nKey = i18nKey;
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
			return i18nKey;
		}
	}
}
