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
package org.olat.modules.portfolio.ui.shared;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.portfolio.model.MySharedItemRow;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MySharedItemsDataModel extends DefaultFlexiTableDataModel<MySharedItemRow>
	implements SortableFlexiTableDataModel<MySharedItemRow> {
	
	private final Locale locale;
	
	public MySharedItemsDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<MySharedItemRow> views = new MySharedItemsSorterDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		MySharedItemRow itemRow = getObject(row);
		return getValueAt(itemRow, col);
	}
	
	@Override
	public Object getValueAt(MySharedItemRow itemRow, int col) {
		switch(MySharedItemCols.values()[col]) {
			case binderKey: return itemRow.getBinderKey();
			case binderName: return itemRow.getBinderTitle();
			case courseName: return itemRow.getCourseDisplayName();
			case lastModified: return itemRow.getLastModified();
		}
		return null;
	}

	public enum MySharedItemCols implements FlexiSortableColumnDef {
		binderKey("table.header.key"),
		binderName("table.header.title"),
		courseName("table.header.course"),
		lastModified("table.header.open");
		
		private final String i18nKey;
		
		private MySharedItemCols(String i18nKey) {
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
	
	public static class MySharedItemsSorterDelegate extends SortableFlexiTableModelDelegate<MySharedItemRow> {
		
		public MySharedItemsSorterDelegate(SortKey orderBy, MySharedItemsDataModel model, Locale locale) {
			super(orderBy, model, locale);
		}
		
	}
}
