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
package org.olat.repository.ui.settings;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.repository.CatalogEntry;

/**
 * Initial date: 27.05.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CatalogListModel extends DefaultFlexiTableDataModel<CatalogEntry> {

	public CatalogListModel(List<CatalogEntry> catalogEntries, FlexiTableColumnModel columnModel) {
		super(catalogEntries, columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CatalogEntry catEntry = getObject(row);
		if(col == 0) {
			// calculate cat entry path: travel up to the root node
			String path = "";
			CatalogEntry tempEntry = catEntry;
			while (tempEntry != null) {
				path = "/" + tempEntry.getName() + path;
				tempEntry = tempEntry.getParent();
			}
			return path;
		}
		return "ERROR";
	}
}