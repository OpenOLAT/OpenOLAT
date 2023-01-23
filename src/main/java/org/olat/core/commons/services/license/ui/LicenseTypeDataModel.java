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
package org.olat.core.commons.services.license.ui;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 19.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class LicenseTypeDataModel extends DefaultFlexiTableDataModel<LicenseTypeRow> {
	
	private Locale locale;

	public LicenseTypeDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		LicenseTypeRow licenseTypeRow = getObject(row);
			if (col <= LicenseTypeCols.values().length - 1) {
			switch (LicenseTypeCols.values()[col]) {
				case up:
					return row == 0 ? Boolean.FALSE : Boolean.TRUE;
				case down:
					return row >= (getRowCount() - 1) ? Boolean.FALSE : Boolean.TRUE;
				case name:
					return licenseTypeRow.getLicenseType().getName();
				case nameTranslation:
					return LicenseUIFactory.translate(licenseTypeRow.getLicenseType(), locale);
				case text:
					return Formatter.truncate(licenseTypeRow.getLicenseType().getText(), 100);
				case cssClass:
					return licenseTypeRow.getLicenseType().getCssClass();
				case edit:
					return licenseTypeRow.getLicenseType().isPredefined();
				case oer:
					return licenseTypeRow.getLicenseType().isOerLicense();
				default:
			}
		}
		return getFormItem(licenseTypeRow, col);
	}

	private MultipleSelectionElement getFormItem(LicenseTypeRow row, int col) {
		int offset = LicenseTypeCols.values().length;
		int formItemIndex = col - offset;
		return row.getFormItems().get(formItemIndex);
	}
	
	enum LicenseTypeCols implements FlexiSortableColumnDef  {
		up("license.type.up"),
		down("license.type.down"),
		name("license.type.name"),
		nameTranslation("license.type.name.translation"),
		text("license.type.text"),
		cssClass("license.type.css.class"),
		edit("table.header.edit"),
		oer("table.header.oer");
		
		private final String i18nKey;
	
		private LicenseTypeCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
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
