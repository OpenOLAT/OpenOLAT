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

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;

/**
 * Initial date: 2023-10-02<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeClassTableModel extends DefaultFlexiTableDataModel<BadgeClassRow> 
		implements SortableFlexiTableDataModel<BadgeClassRow> {

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

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
			List<BadgeClassRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, translator.getLocale()).sort(); 
			super.setObjects(rows);
		}
	}

	public Object getValueAt(BadgeClassRow row, int col) {
		BadgeClass badgeClass = row.badgeClassWithSizeAndCount().badgeClass();
		return switch (BadgeClassCols.values()[col]) {
			case image -> badgeClass.getImage();
			case name -> badgeClass.getName();
			case version -> OpenBadgesUIFactory.versionString(translator, badgeClass, false, false);
			case verification -> badgeClass.getVerificationMethod();
			case creationDate -> Formatter.getInstance(translator.getLocale()).formatDateAndTime(badgeClass.getCreationDate());
			case status -> badgeClass.getStatus();
			case type -> translator.translate(BadgeCriteriaXStream.fromXml(badgeClass.getCriteria()).isAwardAutomatically() ?
					"form.award.procedure.automatic.short" : "form.award.procedure.manual.short");
			case awardedCount -> getAwardedCount(row.badgeClassWithSizeAndCount());
			case tools -> row.toolLink();
		};
	}

	private String getAwardedCount(OpenBadgesManager.BadgeClassWithSizeAndCount badgeClassWithSizeAndCount) {
		long currentCount = badgeClassWithSizeAndCount.count() - badgeClassWithSizeAndCount.revokedCount() 
				- badgeClassWithSizeAndCount.resetCount();
		long totalUseCount = badgeClassWithSizeAndCount.totalUseCount();
		if (OpenBadgesUIFactory.showTotalBadgeAssertionCount && currentCount != totalUseCount) {
			return translator.translate("class.awarded.to.all.version", Long.toString(currentCount), Long.toString(totalUseCount));
		} else {
			return Long.toString(totalUseCount);
		}
	}

	public enum BadgeClassCols implements FlexiSortableColumnDef {
		image("form.image", false),
		name("form.name", true),
		version("form.version", true),
		verification("verification", true),
		creationDate("form.createdOn", true),
		status("form.status", true),
		type("form.type", true),
		awardedCount("form.awarded.to", true),
		tools("action.more", false);

		private final String i18nKey;
		private final boolean sortable;
		
		BadgeClassCols(String i18nKey, boolean sortable) {
			this.i18nKey = i18nKey;
			this.sortable = sortable;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return sortable;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
