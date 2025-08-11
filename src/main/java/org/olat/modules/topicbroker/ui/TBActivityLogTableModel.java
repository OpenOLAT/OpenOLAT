/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Aug 5, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBActivityLogTableModel extends DefaultFlexiTableDataModel<TBActivityLogRow>
	implements SortableFlexiTableDataModel<TBActivityLogRow>, FilterableFlexiTableModel {
		
		private static final TBActivityLogCols[] COLS = TBActivityLogCols.values();

		private final Locale locale;
		private List<TBActivityLogRow> backups;
		
		public TBActivityLogTableModel(FlexiTableColumnModel columnModel, Locale locale) {
			super(columnModel);
			this.locale = locale;
		}
		
		@Override
		public void sort(SortKey orderBy) {
			if(orderBy != null) {
				SortableFlexiTableModelDelegate<TBActivityLogRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
				super.setObjects(sort.sort());
			}
		}

		@Override
		public void filter(String searchString, List<FlexiTableFilter> filters) {
			if(StringHelper.containsNonWhitespace(searchString) || (filters != null && !filters.isEmpty() && filters.get(0) != null)) {
				searchString = searchString.toLowerCase();
				DateRange range = getRange(filters);
				Set<TBActivityLogContext> contexts = getContexts(filters);
				Set<String> activities = getActivities(filters);
				Set<Long> identityKeys = getIdentitiyKeys(filters);
				
				List<TBActivityLogRow> filteredRows = new ArrayList<>(backups.size());
				for (TBActivityLogRow row:backups) {
					boolean accept = accept(row, searchString)
							&& acceptDateRange(row, range)
							&& acceptContext(row, contexts)
							&& acceptActivities(row, activities)
							&& acceptIdentities(row, identityKeys) ;
					if (accept) {
						filteredRows.add(row);
					}
				}
				super.setObjects(filteredRows);
			} else {
				super.setObjects(backups);
			}
		}
		
		private boolean accept(TBActivityLogRow row, String searchString) {
			if(!StringHelper.containsNonWhitespace(searchString)) {
				return true;
			}
			return row.getDoerDisplayName() != null && row.getDoerDisplayName().toLowerCase().contains(searchString);
		}
		
		private boolean acceptDateRange(TBActivityLogRow row, DateRange range) {
			if(range == null) {
				return true;
			}
			Date date = row.getDate();
			return (range.getStart() == null || range.getStart().compareTo(date) <= 0)
					&& (range.getEnd() == null || range.getEnd().compareTo(date) >= 0);
		}
		
		private DateRange getRange(List<FlexiTableFilter> filters) {
			FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, TBActivityLogController.FILTER_DATE);
			if(filter instanceof FlexiTableDateRangeFilter extendedFilter) {
				DateRange range = extendedFilter.getDateRange();
				if(range != null && (range.getStart() != null || range.getEnd() != null)) {
					return range;
				}
			}
			return null;
		}

		private boolean acceptContext(TBActivityLogRow row, Set<TBActivityLogContext> contexts) {
			if (contexts == null || contexts.isEmpty()) {
				return true;
			}
			return contexts.contains(row.getContext());
		}
		
		private Set<TBActivityLogContext> getContexts(List<FlexiTableFilter> filters) {
			FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, TBActivityLogController.FILTER_CONTEXT);
			if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> values = extendedFilter.getValues();
				if(values != null && !values.isEmpty()) {
					return values.stream()
							.map(TBActivityLogContext::valueOf)
							.collect(Collectors.toSet());
				}
			}
			return null;
		}

		private boolean acceptActivities(TBActivityLogRow row, Set<String> activities) {
			if (activities == null || activities.isEmpty()) {
				return true;
			}
			return activities.contains(row.getTranslatedActivity());
		}

		private Set<String> getActivities(List<FlexiTableFilter> filters) {
			FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, TBActivityLogController.FILTER_ACTIVITY);
			if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> values = extendedFilter.getValues();
				if(values != null && !values.isEmpty()) {
					return new HashSet<>(values);
				}
			}
			return null;
		}
		
		private boolean acceptIdentities(TBActivityLogRow row, Set<Long> identityKeys) {
			if (identityKeys == null || identityKeys.isEmpty()) {
				return true;
			}
			if (row.getIdentityKey() == null) {
				return false;
			}
			return identityKeys.contains(row.getIdentityKey());
		}
		
		private Set<Long> getIdentitiyKeys(List<FlexiTableFilter> filters) {
			FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, TBActivityLogController.FILTER_IDENTITY);
			if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> values = extendedFilter.getValues();
				if(values != null && !values.isEmpty()) {
					return values.stream()
							.map(Long::valueOf)
							.collect(Collectors.toSet());
				}
			}
			return null;
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			TBActivityLogRow detailsRow = getObject(row);
			return getValueAt(detailsRow, col);
		}

		@Override
		public Object getValueAt(TBActivityLogRow activityRow, int col) {
			if(col >= 0 && col < COLS.length) {
				return switch(COLS[col]) {
					case date -> activityRow.getDate();
					case context -> activityRow.getContext();
					case object -> activityRow.getObject();
					case activity -> activityRow.getTranslatedActivity();
					case valueOriginal -> activityRow.getValueOriginal();
					case valueNew -> activityRow.getValueNew();
					case user -> activityRow.getDoerDisplayName();
					default -> "ERROR";
				};
			}
			
			return "ERROR";
		}
		
		@Override
		public void setObjects(List<TBActivityLogRow> objects) {
			backups = new ArrayList<>(objects);
			super.setObjects(objects);
		}

		public enum TBActivityLogCols implements FlexiSortableColumnDef {
			date("activity.log.date"),
			context("activity.log.context"),
			object("activity.log.object"),
			activity("activity.log.activity"),
			valueOriginal("activity.log.value.original"),
			valueNew("activity.log.value.new"),
			user("activity.log.user");
			
			private final String i18nKey;
			
			private TBActivityLogCols(String i18nKey) {
				this.i18nKey = i18nKey;
			}
			
			@Override
			public String i18nHeaderKey() {
				return i18nKey;
			}

			@Override
			public boolean sortable() {
				return this == date;
			}

			@Override
			public String sortKey() {
				return name();
			}
		}

}
