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
package org.olat.course.certificate.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.modules.certificationprogram.ui.CertificationStatus;

/**
 * 
 * Initial date: 23 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificatesListDataModel extends DefaultFlexiTableDataModel<CertificateRow>
implements SortableFlexiTableDataModel<CertificateRow>, FilterableFlexiTableModel {
	
	private CertificateCols[] COLS = CertificateCols.values();
	
	private final Locale locale;
	private List<CertificateRow> backupList;
	
	public CertificatesListDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CertificateRow> views= new SortableFlexiTableModelDelegate<>(orderBy, this, locale)
					.sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();

			final List<String> status = getFilteredList(filters, CertificatesListOverviewController.FILTER_STATUS);
			final List<String> origin = getFilteredList(filters, CertificatesListOverviewController.FILTER_ORIGIN);
			final boolean withRecertification = isFilterSelected(filters, CertificatesListOverviewController.FILTER_WITH_RECERTIFICATION);

			List<CertificateRow> filteredRows = new ArrayList<>(backupList.size());
			for(CertificateRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptStatus(status, row)
						&& acceptOrigin(origin, row)
						&& acceptWithRecertification(withRecertification, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private boolean accept(String searchValue, CertificateRow row) {
		if(searchValue == null) return true;
		return accept(searchValue, row.getAwardedBy())
				|| accept(searchValue, row.getCourseTitle());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	private boolean acceptWithRecertification(boolean withRecertification, CertificateRow row) {
		if(!withRecertification) return true;
		return row.isWithRecertification();
	}
	
	private boolean acceptOrigin(List<String> origin, CertificateRow row) {
		if(origin == null || origin.isEmpty()) return true;
		return (origin.contains(CertificatesListOverviewController.FILTER_ORIGIN_UPLOAD_KEY) && row.isUploaded())
				|| (origin.contains(CertificatesListOverviewController.FILTER_ORIGIN_COURSE_KEY) && row.isCourse())
				|| (origin.contains(CertificatesListOverviewController.FILTER_ORIGIN_PROGRAM_KEY) && row.isCertificationProgram());
	}
	
	private List<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? filterValues : null;
		}
		return null;
	}
	
	private boolean isFilterSelected(List<FlexiTableFilter> filters, String id) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, id);
		if (filter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)filter).getValues();
			return filterValues != null && filterValues.contains(id);
		}
		return false;
	}
	
	private boolean acceptStatus(List<String> refs, CertificateRow row) {
		if(refs == null || refs.isEmpty()) return true;
		
		// Filter doesn't nuance expired
		CertificationStatus status = row.getStatus();
		if(status == CertificationStatus.EXPIRED_RENEWABLE) {
			status = CertificationStatus.EXPIRED;
		}
		return refs.contains(status.name());
	}
	
	public CertificateRow getObjectByCertificateKey(Long certificateKey) {
		List<CertificateRow> rows = getObjects();
		return rows.stream()
				.filter(row -> row.getKey().equals(certificateKey))
				.findFirst().orElse(null);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CertificateRow certificateRow = getObject(row);
		return getValueAt(certificateRow, col);
	}

	@Override
	public Object getValueAt(CertificateRow row, int col) {
		return switch(COLS[col]) {
			case awardedBy -> row.getAwardedBy();
			case origin -> row.getOrigin();
			case issuedOn -> row.getCreationDate();
			case validUntil -> row.getValidUntil();
			case dateRecertification -> row.getRecertificationInDays();
			case recertificationCount -> row.getRecertificationCount();
			case status -> row.getStatus();
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CertificateRow> objects) {
		this.backupList = new ArrayList<>(objects);
		super.setObjects(objects);
	}
	
	public enum CertificateCols implements FlexiSortableColumnDef {

		awardedBy("table.header.awarded.by"),
		origin("table.header.origin"),
		issuedOn("table.header.issued.on"),
		validUntil("table.header.valid.until"),
		dateRecertification("table.header.recertification.date"),
		recertificationCount("table.header.recertification.count"),
		status("table.header.status")
		;

		private final String i18n;

		private CertificateCols(String i18n) {
			this.i18n = i18n;
		}

		@Override
		public String i18nHeaderKey() {
			return i18n;
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
