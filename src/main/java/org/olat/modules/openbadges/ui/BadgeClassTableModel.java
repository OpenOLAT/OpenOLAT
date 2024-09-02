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
package org.olat.modules.openbadges.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;

/**
 * Initial date: 2023-10-02<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeClassTableModel extends DefaultFlexiTableDataModel<BadgeClassRow> {

	private final Translator translator;

	public BadgeClassTableModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public Object getValueAt(int row, int col) {
		BadgeClassRow badgeClassRow = getObject(row);
		return getValueAt(badgeClassRow, col);
	}

	public Object getValueAt(BadgeClassRow row, int col) {
		BadgeClass badgeClass = row.badgeClassWithSizeAndCount().badgeClass();
		return switch (BadgeClassCols.values()[col]) {
			case image -> badgeClass.getImage();
			case name -> badgeClass.getName();
			case version -> badgeClass.getVersion();
			case creationDate -> Formatter.getInstance(translator.getLocale()).formatDateAndTime(badgeClass.getCreationDate());
			case status -> badgeClass.getStatus();
			case type -> translator.translate(BadgeCriteriaXStream.fromXml(badgeClass.getCriteria()).isAwardAutomatically() ?
					"form.award.procedure.automatic.short" : "form.award.procedure.manual.short");
			case awardedCount -> row.badgeClassWithSizeAndCount().count()
					- row.badgeClassWithSizeAndCount().revokedCount() - row.badgeClassWithSizeAndCount().resetCount();
			case tools -> row.toolLink();
		};
	}

	public enum BadgeClassCols implements FlexiSortableColumnDef {
		image("form.image"),
		name("form.name"),
		version("form.version"),
		creationDate("form.createdOn"),
		status("form.status"),
		type("form.type"),
		awardedCount("form.awarded.to"),
		tools("table.header.actions");

		private final String i18nKey;

		BadgeClassCols(String i18nKey) {
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
