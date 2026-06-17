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
package org.olat.user.ui.importexternal;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial Date:  5 mar. 2020 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportExternalUserSearchDataModel extends DefaultFlexiTableDataModel<ImportExternalRow>
implements SortableFlexiTableDataModel<ImportExternalRow> {
	
	private static final ImportCols[] FIELDS = ImportCols.values();

	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public ImportExternalUserSearchDataModel(FlexiTableColumnModel columnsModel,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		ImportExternalRow identity = getObject(row);
		return getValueAt(identity, col);
	}
	
	@Override
	public Object getValueAt(ImportExternalRow row, int col) {
		if(col >= 0 && col < FIELDS.length) {
			return switch(FIELDS[col]) {
				case username -> row.getUsername();
				case authenticationProvider -> row.getAuthenticationProvider();
				default -> "ERROR";
			};
		}
		
		if(col >= ImportExternalUserSearchController.OFFSET_INDEX && col < ImportExternalUserSearchController.OFFSET_INDEX + userPropertyHandlers.size()) {
			int userCol = col - ImportExternalUserSearchController.OFFSET_INDEX;
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(userCol);
			return userPropertyHandler.getUserProperty(row.getUser(), locale);
		}
		return "ERROR";
	}
	
	public enum ImportCols implements FlexiSortableColumnDef {
		username("table.header.username"),
		authenticationProvider("table.header.authentication.provider");
		
		private final String i18nKey;
		
		private ImportCols(String i18nKey) {
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
