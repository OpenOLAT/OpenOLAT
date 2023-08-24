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
package org.olat.modules.project.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.project.ui.ProjFileDataModel.FileCols;

/**
 * 
 * Initial date: 1 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileRowSortDelegate extends SortableFlexiTableModelDelegate<ProjFileRow> {
	
	public ProjFileRowSortDelegate(SortKey orderBy, ProjFileDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<ProjFileRow> rows) {
		int columnIndex = getColumnIndex();
		FileCols column = FileCols.values()[columnIndex];
		switch(column) {
			case displayName: Collections.sort(rows, new DisplayNameComparator()); break;
			default: super.sort(rows);
		}
	}
	
	private class DisplayNameComparator implements Comparator<ProjFileRow> {
		@Override
		public int compare(ProjFileRow t1, ProjFileRow t2) {
			return compareString(t1.getDisplayName(), t2.getDisplayName());
		}
	}

}
