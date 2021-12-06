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
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.portfolio.model.SharedItemRow;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedBindersDataModel extends DefaultFlexiTableDataModel<SharedItemRow>
	implements SortableFlexiTableDataModel<SharedItemRow>, FilterableFlexiTableModel {
	
	protected static final String EMPTY_SECTIONS = "sections-empty";
	
	private final Locale locale;
	private List<SharedItemRow> backups;
	
	public SharedBindersDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if(SharedBindersDataModel.EMPTY_SECTIONS.equals(key)) {
			List<SharedItemRow> filteredRows = backups.stream()
						.filter(r -> r.getNumOfOpenSections() > 0)
						.collect(Collectors.toList());
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<SharedItemRow> views = new SharedItemsSorterDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		SharedItemRow itemRow = getObject(row);
		return getValueAt(itemRow, col);
	}	
	
	@Override
	public Object getValueAt(SharedItemRow itemRow, int col) {
		if(col >= 0 && col < ShareItemCols.values().length) {
			switch(ShareItemCols.values()[col]) {
				case binderKey: return itemRow.getBinderKey();
				case binderName: return itemRow.getBinderTitle();
				case courseName: return itemRow.getEntryDisplayName();
				case openSections: return itemRow.getNumOfOpenSections();
				case selectSections: return itemRow;
				case grading: return itemRow.getAssessmentEntry();
				case lastModified: return itemRow.getLastModified();
				case recentLaunch: return itemRow.getRecentLaunch();
				case draftPage: return itemRow.getNumOfDraftPages();
				case inRevisionPage: return itemRow.getNumOfInRevisionPages();
				case closedPage: return itemRow.getNumOfClosedPages();
				case newlyPublishedPage: return itemRow.getNumOfNewlyPublishedPages();
			}
		}
		
		int propPos = col - SharedBindersController.USER_PROPS_OFFSET;
		return itemRow.getIdentityProp(propPos);
	}
	
	@Override
	public void setObjects(List<SharedItemRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}

	public enum ShareItemCols implements FlexiSortableColumnDef {
		binderKey("table.header.key"),
		binderName("table.header.title"),
		courseName("table.header.course"),
		openSections("table.header.open.sections"),
		selectSections("table.header.select.sections"),
		grading("table.header.grading"),
		lastModified("table.header.lastUpdate"),
		recentLaunch("table.header.recentLaunch"),
		draftPage("table.header.draft"),
		inRevisionPage("table.header.inRevision"),
		closedPage("table.header.closed"),
		newlyPublishedPage("table.header.new");
		
		private final String i18nKey;
		
		private ShareItemCols(String i18nKey) {
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
	
	
	public static class SharedItemsSorterDelegate extends SortableFlexiTableModelDelegate<SharedItemRow> {
		
		public SharedItemsSorterDelegate(SortKey orderBy, SharedBindersDataModel model, Locale locale) {
			super(orderBy, model, locale);
		}
	}
}
