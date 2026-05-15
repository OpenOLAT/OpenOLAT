/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import static org.olat.modules.selectus.ui.events.SelectPositionLightEvent.SELECT_POSITION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableNumericalRangeFilter.NumericalRange;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionLightWithStatistics;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.ui.PositionListController;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate.FieldFilter;

/**
 * 
 * Initial date: 26 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionsDataModel extends DefaultFlexiTableDataModel<PositionLightWithStatistics>
implements SortableFlexiTableDataModel<PositionLightWithStatistics>, FilterableFlexiTableModel, FlexiBusinessPathModel {
	
	private static final Logger log = Tracing.createLoggerFor(PositionsDataModel.class);

	private static final Fields[] COLS = Fields.values();
	
	private final Locale locale;
	private List<PositionLightWithStatistics> backupList;
	
	public PositionsDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			try {
				List<PositionLightWithStatistics> views = new PositionsSortDelegate(orderBy, this, locale).sort();
				super.setObjects(views);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();

			final List<String> status = getFilteredList(filters, PositionListController.FILTER_STATUS_KEY);
			final List<String> organisations = getFilteredList(filters, PositionListController.FILTER_ORGANISATION_KEY);
			List<PositionLightWithStatistics> filteredRows = new ArrayList<>(backupList.size());
			final List<FieldFilter> fieldsFilters = getFilteredField(filters);
			
			for(PositionLightWithStatistics row:backupList) {
				boolean accept = acceptStatus(status, row)
						&& accept(loweredSearchString, row)
						&& acceptOrganisations(organisations, row)
						&& acceptFieldFilters(fieldsFilters, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private List<FieldFilter> getFilteredField(List<FlexiTableFilter> filters) {
		List<FieldFilter> fieldFilter = new ArrayList<>();
		for(FlexiTableFilter filter:filters) {
			if(filter.getFilter().startsWith("filter.")) {
				int column = Integer.parseInt(filter.getFilter().substring(7));
				FieldFilter values = ApplicationAttributesDelegate.getFilterValue(filter, column);
				if(values != null) {
					fieldFilter.add(values);
				}
			}
		}
		return fieldFilter;
	}
	
	private List<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? filterValues : null;
		}
		return null;
	}
	
	private boolean accept(String searchValue, PositionLightWithStatistics row) {
		if(searchValue == null) return true;
		return accept(searchValue, row.getPositionTitle())
				|| accept(searchValue, row.getPositionTitleDe())
				|| accept(searchValue, row.getPositionTitleFr());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	private boolean acceptStatus(List<String> status, PositionLightWithStatistics row) {
		if(status == null || status.isEmpty()) return true;
		return status.contains(row.getStatus());
	}
	
	private boolean acceptOrganisations(List<String> organisations, PositionLightWithStatistics row) {
		if(organisations == null || organisations.isEmpty()) return true;
		
		Organisation organisation = row.getOrganisation();
		return organisation != null && organisations.contains(organisation.getKey().toString());
	}
	
	private boolean acceptFieldFilters(List<FieldFilter> fieldsFilters, PositionLightWithStatistics row) {
		if(fieldsFilters == null || fieldsFilters.isEmpty()) return true;
		
		boolean allOk = true;
		for(FieldFilter fieldFilter:fieldsFilters) {
			if(fieldFilter.set() != null) {
				allOk &= acceptField(fieldFilter.set(), row, fieldFilter.column()); 
			} else if(fieldFilter.range() != null) {
				allOk &= acceptDateRange(fieldFilter.range(), row, fieldFilter.column()); 
			} else if(fieldFilter.numericalRange() != null) {
				allOk &= acceptNumericalRange(fieldFilter.numericalRange(), row, fieldFilter.column()); 
			} else if(fieldFilter.text() != null) {
				allOk &= acceptText(fieldFilter.text(), row, fieldFilter.column()); 
			}
		}
		
		return allOk;
	}
	
	private boolean acceptDateRange(DateRange range, PositionLightWithStatistics row, int column) {
		if(range == null || (range.getStart() == null && range.getEnd() == null)) return true;
		
		Object val = getValueAt(row, column);
		if(val instanceof Date date) {
			if((range.getStart() != null && range.getStart().compareTo(date) > 0)
					|| (range.getEnd() != null && range.getEnd().compareTo(date) < 0)) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean acceptText(String text, PositionLightWithStatistics row, int column) {
		if(!StringHelper.containsNonWhitespace(text)) return true;
		
		Object val = getValueAt(row, column);
		if(val instanceof String str) {
			return accept(text, str);
		}
		return false;
	}
	
	private boolean acceptNumericalRange(NumericalRange range, PositionLightWithStatistics row, int column) {
		if(range == null || (range.getStart() == null && range.getEnd() == null)) return true;
		
		Object val = getValueAt(row, column);
		if(val instanceof String str && StringHelper.containsNonWhitespace(str)) {
			try {
				val = Double.valueOf(str);
			} catch (NumberFormatException e) {
				//
			}
		}
		if(val instanceof Number num) {
			if((range.getStart() != null && range.getStart().compareTo(num.doubleValue()) > 0)
					|| (range.getEnd() != null && range.getEnd().compareTo(num.doubleValue()) < 0)) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean acceptField(Set<String> searchValues, PositionLightWithStatistics row, int column) {
		if(searchValues == null || searchValues.isEmpty()) return true;
		
		Object val = getValueAt(row, column);
		if(val == null) {
			return searchValues.contains(PositionListController.FILTER_NULL_KEY);
		}
		if(val instanceof String str) {
			if(StringHelper.containsNonWhitespace(str)) {
				return searchValues.contains(str);
			}
			return searchValues.contains(PositionListController.FILTER_NULL_KEY);
		} else if (val instanceof String[] strArr) {
			for(String str:strArr) {
				if(StringHelper.containsNonWhitespace(str)) {
					return searchValues.contains(str);
				}
			}
		}
		return false;
	}

	@Override
	public String getUrl(Component source, Object object, String action) {
		if(SELECT_POSITION.equals(action) && object instanceof PositionLightWithStatistics) {
			return ((PositionLightWithStatistics)object).getUrl();
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		PositionLightWithStatistics member = getObject(row);
		return getValueAt(member, col);
	}

	@Override
	public Object getValueAt(PositionLightWithStatistics position, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case positionTitle: return getPositionTitle(position);
				case status: return position.getStatus();
				case planingsNumber: return position.getPlaningsNumber();
				case department: return position.getMLDepartment(locale);
				case deadline: return position.getApplicationDeadline();
				case numOfApplications: return position.getNumOfApplications() == null ? 0 : position.getNumOfApplications();
				case numOfMaleApplications: return position.getNumOfMaleApplications() == null ? 0 : position.getNumOfMaleApplications();
				case numOfFemaleApplications: return position.getNumOfFemaleApplications() == null ? 0 : position.getNumOfFemaleApplications();
				case organisation: return position.getOrganisation() == null ? null : position.getOrganisation().getDisplayName();
				default: return position;
			}
		}
		
		if(col >= PositionListController.CUSTOM_ATTRIBUTES_COLS_OFFSET) {
			int index = col - PositionListController.CUSTOM_ATTRIBUTES_COLS_OFFSET;
			return position.getAdditionalValue(index);
		}
		return "ERROR";
	}
	
	private String getPositionTitle(PositionLightWithStatistics position) {
		String title = position.getMLTitle(locale);
		if(!StringHelper.containsNonWhitespace(title)) {
			title = "Untitled";
		}
		return title;
	}

	@Override
	public void setObjects(List<PositionLightWithStatistics> objects) {
		this.backupList = new ArrayList<>(objects);
		super.setObjects(objects);
	}

	private class PositionsSortDelegate extends SortableFlexiTableModelDelegate<PositionLightWithStatistics> {
		
		public PositionsSortDelegate(SortKey orderBy, PositionsDataModel tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}

		@Override
		protected void sort(List<PositionLightWithStatistics> rows) {
			int columnIndex = getColumnIndex();
			if(columnIndex < COLS.length) {
				switch(COLS[columnIndex]) {
					case status: Collections.sort(rows, new StatusComparator()); break;
					default: super.sort(rows);
				}
			} else {
				super.sort(rows);
			}
		}
	}
	
	private static class StatusComparator implements Comparator<PositionLight> {
		@Override
		public int compare(PositionLight p1, PositionLight p2) {
			String s1 = p1.getStatus();
			String s2 = p2.getStatus();
			
			PositionStatus ps1 = PositionStatus.valueOf(s1);
			PositionStatus ps2 = PositionStatus.valueOf(s2);
			return ps1.ordinal() - ps2.ordinal();
		}
	}
	
	public enum Fields implements FlexiSortableColumnDef {
		positionTitle("edit.position_title"),
		status("edit.status"),
		deadline("edit.deadline"),
		planingsNumber("edit.position_id"),
		department("edit.department"),
		numOfApplications("edit.num_of_applications"),
		numOfMaleApplications("edit.num_of_male_applications"),
		numOfFemaleApplications("edit.num_of_female_applications"),
		organisation("table.header.organisation.unit");

		private final String key;
		
		private Fields(String key) {
			this.key = key;
		}

		@Override
		public String i18nHeaderKey() {
			return key;
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
