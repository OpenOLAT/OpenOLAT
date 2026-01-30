/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.util.ArrayList;
import java.util.Collection;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 6 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramLogTableModel extends DefaultFlexiTableDataModel<CertificationProgramLogRow> 
implements SortableFlexiTableDataModel<CertificationProgramLogRow>, FilterableFlexiTableModel {
	
	private static final ActivityLogCols[] COLS = ActivityLogCols.values();

	private final Locale locale;
	private List<CertificationProgramLogRow> backupList;
	
	public CertificationProgramLogTableModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		locale = translator.getLocale();
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CertificationProgramLogRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();
			final Set<String> contexts = getFilteredList(filters, CertificationProgramLogController.FILTER_CONTEXT);
			final Set<String> activities = getFilteredList(filters, CertificationProgramLogController.FILTER_ACTIVITY);
			final Set<Long> members = getFilteredListOfKeys(filters, CertificationProgramLogController.FILTER_MEMBER);
			final Set<Long> users = getFilteredListOfKeys(filters, CertificationProgramLogController.FILTER_USER);
			
			List<CertificationProgramLogRow> filteredRows = new ArrayList<>(backupList.size());
			for(CertificationProgramLogRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptContext(contexts, row)
						&& acceptActivity(activities, row)
						&& acceptMember(members, row)
						&& acceptUser(users, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private Set<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? Set.copyOf(filterValues) : null;
		}
		return null;
	}
	
	private Set<Long> getFilteredListOfKeys(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.filter(val -> StringHelper.isLong(val))
						.map(val -> Long.valueOf(val))
						.collect(Collectors.toSet());
			}
		}
		return null;
	}
	
	private boolean acceptMember(Collection<Long> keys, CertificationProgramLogRow entry) {
		if(keys == null || keys.isEmpty()) return true;
		return entry.getMemberKey() != null && keys.contains(entry.getMemberKey());
	}
	
	private boolean acceptUser(Collection<Long> keys, CertificationProgramLogRow entry) {
		if(keys == null || keys.isEmpty()) return true;
		return entry.getActorKey() != null && keys.contains(entry.getActorKey());
	}
	
	private boolean acceptActivity(Collection<String> activities, CertificationProgramLogRow entry) {
		if(activities == null || activities.isEmpty()) return true;
		return entry.getAction() != null && activities.contains(entry.getAction().name());
	}
	
	private boolean acceptContext(Collection<String> contexts, CertificationProgramLogRow entry) {
		if(contexts == null || contexts.isEmpty()) return true;
		return entry.getContext() != null && contexts.contains(entry.getContext().name());
	}
	
	private boolean accept(String searchValue, CertificationProgramLogRow entry) {
		if(searchValue == null) return true;
		return accept(searchValue, entry.getOriginalValue())
				|| accept(searchValue, entry.getNewValue())
				|| accept(searchValue, entry.getObject())
				|| accept(searchValue, entry.getActor())
				|| accept(searchValue, entry.getObject());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CertificationProgramLogRow logRow = getObject(row);
		return getValueAt(logRow, col);
	}

	@Override
	public Object getValueAt(CertificationProgramLogRow row, int col) {
		return switch(COLS[col]) {
			case date -> row.getCreationDate();
			case context -> row.getContext();
			case object -> row.getObject();
			case message -> row.getMessage();// activity
			case originalValue -> row.getOriginalValue();
			case newValue -> row.getNewValue();
			case user -> row.getActor();
			default -> "ERROR";
		};
	}

	@Override
	public void setObjects(List<CertificationProgramLogRow> objects) {
		this.backupList = new ArrayList<>(objects);
		super.setObjects(objects);
	}

	public enum ActivityLogCols implements FlexiSortableColumnDef {
		date("activity.log.date"),
		message("activity.log.message"),
		context("activity.log.context"),
		object("activity.log.object"),
		originalValue("activity.log.original.value"),
		newValue("activity.log.new.value"),
		user("activity.log.user");
		
		private final String i18nKey;

		private ActivityLogCols(String i18nKey) {
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
