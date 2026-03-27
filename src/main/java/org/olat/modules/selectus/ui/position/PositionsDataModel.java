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
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionLightWithStatistics;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.ui.PositionListController;

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
	private final List<PositionAttributeDefinition> globalDefinitions;

	private List<Position> excludedPositions;
	private List<PositionLightWithStatistics> backupList;
	
	public PositionsDataModel(FlexiTableColumnModel columnsModel, IdentityEnvironment identityEnv,
			List<PositionAttributeDefinition> globalDefinitions, Locale locale) {
		super(columnsModel);
		CoreSpringFactory.autowireObject(this);
		this.locale = locale;
		this.globalDefinitions = globalDefinitions;
	}
	
	public List<Position> getExcludedPositions() {
		return excludedPositions;
	}

	public void setExcludedPositions(List<Position> excludedPositions) {
		this.excludedPositions = excludedPositions;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			try {
				List<PositionLightWithStatistics> views = new PositionsSortDelegate(orderBy, this, null).sort();
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
			List<PositionLightWithStatistics> filteredRows = new ArrayList<>(backupList.size());
			for(PositionLightWithStatistics row:backupList) {
				boolean accept = acceptStatus(status, row)
						&& accept(loweredSearchString, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private DateRange getFilterDate(List<FlexiTableFilter> filters, String filterName) {
		FlexiTableFilter dFilter = FlexiTableFilter.getFilter(filters, filterName);
		if(dFilter instanceof FlexiTableDateRangeFilter dateFilter && dateFilter.getDateRange() != null) {
			return dateFilter.getDateRange();
		}
		return null;
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
