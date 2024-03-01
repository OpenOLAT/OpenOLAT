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
package org.olat.modules.quality.generator.ui;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.quality.generator.ui.PreviewDataModel.PreviewCols;

/**
 * 
 * Initial date: 25 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PreviewRowSortDelegate extends SortableFlexiTableModelDelegate<PreviewRow> {

	public PreviewRowSortDelegate(SortKey orderBy, PreviewDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<PreviewRow> rows) {
		int columnIndex = getColumnIndex();
		PreviewCols column = PreviewCols.values()[columnIndex];
		switch(column) {
			case status: Collections.sort(rows, (r1, r2) -> compareString(r1.getTranslatedStatus(), r2.getTranslatedStatus())); break;
			case topic: Collections.sort(rows, (r1, r2) -> compareString(r1.getTopic(), r2.getTopic())); break;
			default: {
				super.sort(rows);
			}
		}
	}

}
