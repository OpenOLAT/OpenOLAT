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
package org.olat.user.ui.organisation;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 10 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationTreeDataModel extends DefaultFlexiTreeTableDataModel<OrganisationRow> {
	
	public OrganisationTreeDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		OrganisationRow level = getObject(row);
		return level.hasChildren();
	}

	@Override
	public Object getValueAt(int row, int col) {
		OrganisationRow organisation = getObject(row);
		switch(OrganisationCols.values()[col]) {
			case key: return organisation.getKey();
			case displayName: return organisation.getDisplayName();
			case identifier: return organisation.getIdentifier();
			case externalId: return organisation.getExternalId();
			case typeIdentifier: {
				String typeIdentifier = organisation.getTypeIdentifier();
				if(StringHelper.containsNonWhitespace(typeIdentifier)) {
					typeIdentifier = organisation.getTypeDisplayName();
				}
				return typeIdentifier;
			}
			case tools: return organisation.getTools();
			default: return "ERROR";
		}
	}
	
	public enum OrganisationCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.displayName"),
		identifier("table.header.identifier"),
		externalId("table.header.external.id"),
		typeIdentifier("table.header.type.identifier"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private OrganisationCols(String i18nKey) {
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
