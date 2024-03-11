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
package org.olat.core.commons.services.folder.ui;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.folder.ui.FolderDataModel.FolderCols;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 28 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderModelSort extends SortableFlexiTableModelDelegate<FolderRow> {
	
	public FolderModelSort(SortKey orderBy, SortableFlexiTableDataModel<FolderRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<FolderRow> rows) {
		int columnIndex = getColumnIndex();
		FolderCols column = FolderDataModel.COLS[columnIndex];
		switch(column) {
			case title: Collections.sort(rows, (r1, r2) -> compareString(r1.getTitle(), r2.getTitle())); break;
			case status: Collections.sort(rows, (r1, r2) -> compareString(r1.getTranslatedStatus(), r2.getTranslatedStatus())); break;
			default: {
				super.sort(rows);
			}
		}
	}
}
