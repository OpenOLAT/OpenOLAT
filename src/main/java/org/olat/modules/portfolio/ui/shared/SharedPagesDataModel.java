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

import java.util.ArrayList;
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
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.PageStatus;

/**
 * 
 * Initial date: 11 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedPagesDataModel extends DefaultFlexiTableDataModel<SharedPageRow>
	implements SortableFlexiTableDataModel<SharedPageRow>, FilterableFlexiTableModel {
	
	private static final Logger log = Tracing.createLoggerFor(SharedPagesDataModel.class);
	
	private final Locale locale;
	private List<SharedPageRow> backups;
	
	public SharedPagesDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		try {
			List<SharedPageRow> views = new SharedPagesDataModelSortDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if("all".equals(key)) {
			super.setObjects(backups);
		} else if(StringHelper.containsNonWhitespace(key)) {
			List<String> allowedStatus = filters.stream()
					.map(f -> f.getFilter()).collect(Collectors.toList());
			List<SharedPageRow> filteredRows = backups.stream()
						.filter(r -> accept(r, allowedStatus))
						.collect(Collectors.toList());

			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}
	
	private boolean accept(SharedPageRow row, List<String> allowedStatus) {
		PageStatus status = row.getStatus();
		if(status == null) {
			status = PageStatus.draft;
		}
		return allowedStatus.contains(status.name());
	}
	
	public List<SharedPageRow> getBackups() {
		if(backups == null) return new ArrayList<>(1);
		return new ArrayList<>(backups);
	}

	@Override
	public Object getValueAt(int row, int col) {
		SharedPageRow page = getObject(row);
		return getValueAt(page, col);
	}

	@Override
	public Object getValueAt(SharedPageRow itemRow, int col) {
		if(col >= 0 && col < SharePageCols.values().length) {
			switch(SharePageCols.values()[col]) {
				case pageKey: return itemRow.getPageKey();
				case pageName: {
					boolean draft = itemRow.getStatus() == null || PageStatus.draft.equals(itemRow.getStatus());
					return !draft;
				}
				case lastChanges: return itemRow.getLastChanges();
				case pageStatus: return itemRow.getStatus();
				case bookmark: return itemRow.getBookmarkLink();
				case userInfosStatus: return itemRow;
			}
		}
		
		int propPos = col - SharedPagesController.USER_PROPS_OFFSET;
		return itemRow.getIdentityProp(propPos);
	}
	
	@Override
	public void setObjects(List<SharedPageRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}
	
	public enum SharePageCols implements FlexiSortableColumnDef {
		pageKey("table.header.key"),
		pageName("table.header.title"),
		lastChanges("table.header.lastUpdate"),
		pageStatus("table.header.status.user"),
		bookmark("table.header.mark"),
		userInfosStatus("table.header.status.viewer");
		
		private final String i18nKey;
		
		private SharePageCols(String i18nKey) {
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