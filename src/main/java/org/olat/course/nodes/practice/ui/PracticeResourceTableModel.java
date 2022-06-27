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
package org.olat.course.nodes.practice.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.model.PracticeResourceInfos;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeResourceTableModel extends DefaultFlexiTableDataModel<PracticeResourceInfos>
implements SortableFlexiTableDataModel<PracticeResourceInfos> {
	
	private static final PracticeResourceCols[] COLS = PracticeResourceCols.values();
	
	private final Locale locale;
	
	public PracticeResourceTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<PracticeResourceInfos> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	public int getTotalNumOfItems() {
		int total = 0;
		List<PracticeResourceInfos> rows = getObjects();
		for(PracticeResourceInfos row:rows) {
			total += row.getNumOfItems();
		}
		return total;
	}

	@Override
	public Object getValueAt(int row, int col) {
		PracticeResourceInfos resourceRow = getObject(row);
		return getValueAt(resourceRow, col);
	}

	@Override
	public Object getValueAt(PracticeResourceInfos row, int col) {
		switch(COLS[col]) {
			case id: return row.getResource().getKey();
			case icon: return getIcon(row.getResource());
			case title: return row.getName();
			case numOfQuestions: return row.getNumOfItems();
			default: return "ERROR";
		}
	}
	
	public String getIcon(PracticeResource resource) {
		String iconCssClass = null;
		if(resource.getTestEntry() != null) {
			iconCssClass = "o_icon-lg o_FileResource-IMSQTI21_icon";
		} else if(resource.getPool() != null) {
			iconCssClass = "o_icon-lg o_icon_pool_pool";
		} else if(resource.getItemCollection() != null) {
			iconCssClass = "o_icon-lg o_icon_pool_collection";
		} else if(resource.getResourceShare() != null) {
			iconCssClass = "o_icon-lg o_icon_pool_share";
		}
		return iconCssClass;
	}
	
	public boolean onlyTests() {
		boolean onlyTest = true;
		List<PracticeResourceInfos> rows = getObjects();
		for(PracticeResourceInfos row:rows) {
			onlyTest &= row.getResource().getTestEntry() != null;
		}
		return onlyTest;
	}
	
	public enum PracticeResourceCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		icon("table.header.icon"),
		title("table.header.title"),
		numOfQuestions("table.header.questions");
		
		private final String i18nKey;
		
		private PracticeResourceCols(String i18nKey) {
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
