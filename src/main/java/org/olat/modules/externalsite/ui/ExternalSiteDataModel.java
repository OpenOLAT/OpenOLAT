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
package org.olat.modules.externalsite.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.modules.externalsite.ExternalSitesConfigRow;

/**
 * Initial date: Nov 17, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ExternalSiteDataModel extends DefaultFlexiTableDataModel<ExternalSitesConfigRow> {

	public ExternalSiteDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ExternalSitesConfigRow id = getObject(row);
		return switch (ESCols.values()[col]) {
			case language -> id.getLanguage();
			case title -> id.titleEl();
			case url -> id.urlEl();
		};
	}

	public enum ESCols {
		language("external.site.language"),
		title("external.site.title"),
		url("external.site.url");

		private final String i18n;

		ESCols(String i18n) {
			this.i18n = i18n;
		}

		public String i18nKey() {
			return i18n;
		}
	}
}
