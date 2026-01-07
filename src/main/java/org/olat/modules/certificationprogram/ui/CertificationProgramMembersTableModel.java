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

import static org.olat.modules.certificationprogram.ui.AbstractCertificationProgramMembersController.FILTER_EXPIRATION;
import static org.olat.modules.certificationprogram.ui.AbstractCertificationProgramMembersController.FILTER_EXPIRE_SOON;
import static org.olat.modules.certificationprogram.ui.AbstractCertificationProgramMembersController.FILTER_NOT_ENOUGH_CREDIT_POINTS;
import static org.olat.modules.certificationprogram.ui.AbstractCertificationProgramMembersController.FILTER_RECERTIFIED;
import static org.olat.modules.certificationprogram.ui.AbstractCertificationProgramMembersController.FILTER_STATUS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import org.olat.modules.certificationprogram.ui.component.NextRecertificationInDays;

/**
 * 
 * Initial date: 3 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramMembersTableModel extends DefaultFlexiTableDataModel<CertificationProgramMemberRow>
implements SortableFlexiTableDataModel<CertificationProgramMemberRow>, FilterableFlexiTableModel {
	
	private static final CertificationProgramMembersCols[] COLS = CertificationProgramMembersCols.values();
	
	private final Locale locale;
	private final BigDecimal creditPointsLimit;
	private List<CertificationProgramMemberRow> backupList;
	
	public CertificationProgramMembersTableModel(FlexiTableColumnModel columnModel, BigDecimal creditPointsLimit, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.creditPointsLimit = creditPointsLimit;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<CertificationProgramMemberRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();

			final List<String> status = getFilteredList(filters, FILTER_STATUS);
			final Set<String> statusSet = status == null ? Set.of() : new HashSet<>(status);
			final boolean expireSoon = isFilterSelected(filters, FILTER_EXPIRE_SOON);
			final boolean recertified = isFilterSelected(filters, FILTER_RECERTIFIED);
			final boolean notEnoughCreditPoints = isFilterSelected(filters, FILTER_NOT_ENOUGH_CREDIT_POINTS);
			final DateRange nextRecertificationDateRange = getFilterDateRange(filters, FILTER_EXPIRATION);
			
			List<CertificationProgramMemberRow> filteredRows = new ArrayList<>(backupList.size());
			for(CertificationProgramMemberRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptStatus(statusSet, row)
						&& acceptRecertified(recertified, row)
						&& acceptExpireSoon(expireSoon, row)
						&& acceptNotEnoughCreditPoints(notEnoughCreditPoints, row)
						&& acceptNextRecertification(nextRecertificationDateRange, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private boolean accept(String searchValue, CertificationProgramMemberRow memberRow) {
		if(searchValue == null) return true;
		return accept(searchValue, memberRow.getIdentityExternalId())
				|| accept(searchValue, memberRow.getIdentityProps());
	}
	
	private boolean accept(String searchValue, String[] values) {
		for(String val:values) {
			if(accept(searchValue, val)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	private boolean acceptStatus(Set<String> status, CertificationProgramMemberRow memberRow) {
		if(status == null || status.isEmpty()) return true;
		
		// We group both EXPIRED status under one
		CertificationStatus rStatus = memberRow.getCertificateStatus();
		if(status.contains(rStatus.name())) {
			return true;
		}
		if(rStatus == CertificationStatus.VALID
				&& (status.contains(CertificationStatus.VALID.name())
						|| status.contains(CertificationIdentityStatus.CERTIFIED.name())) ) {
			return true;
		}
		CertificationIdentityStatus iStatus = memberRow.getIdentityStatus();
		return status.contains(iStatus.name());
	}
	
	private boolean acceptRecertified(boolean recertified, CertificationProgramMemberRow memberRow) {
		if(!recertified) return true;
		return memberRow.getRecertificationCount() > 0;
	}
	
	private boolean acceptExpireSoon(boolean expireSoon, CertificationProgramMemberRow memberRow) {
		if(!expireSoon) return true;
		NextRecertificationInDays nextRecertification = memberRow.getNextRecertification();
		
		return nextRecertification != null && nextRecertification.days() != null
				&& nextRecertification.days().longValue() >= 0l && nextRecertification.days().longValue() <= 7l;
	}
	
	private boolean acceptNotEnoughCreditPoints(boolean notEnoughCreditPoints, CertificationProgramMemberRow memberRow) {
		if(!notEnoughCreditPoints) return true;
		BigDecimal balance = memberRow.getWalletBalance();
		
		return balance != null && creditPointsLimit != null
				&& balance.compareTo(creditPointsLimit) < 0;
	}
	
	private boolean acceptNextRecertification(DateRange range, CertificationProgramMemberRow memberRow) {
		if(range == null || (range.getStart() == null && range.getEnd() == null)) return true;
		
		Date date = memberRow.getNextRecertificationDate();
		if(date == null) {
			return false;
		}
		return (range.getStart() == null || range.getStart().compareTo(date) <= 0)
				&& (range.getEnd() == null || range.getEnd().compareTo(date) >= 0);
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
	
	private DateRange getFilterDateRange(List<FlexiTableFilter> filters, String filterName) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableDateRangeFilter extendedFilter) {
			return extendedFilter.getDateRange();
		}
		return null; 
	}
	
	public List<Long> getIdentitiesKeys() {
		List<CertificationProgramMemberRow> memberRows = this.getObjects();
		return memberRows.stream()
				.map(CertificationProgramMemberRow::getIdentityKey)
				.toList();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CertificationProgramMemberRow memberRow = getObject(row);
		return getValueAt(memberRow, col);
	}

	@Override
	public Object getValueAt(CertificationProgramMemberRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case id -> row.getIdentityKey();
				case identityStatus -> row.getIdentityStatus();
				case certificateStatus -> row.getCertificateStatus();
				case recertificationCount -> row.getRecertificationCount() > 0
					? Long.valueOf(row.getRecertificationCount())
					: null;
				case validUntil -> row.getNextRecertificationDate();
				case revocationDate -> row.getRevocationDate();
				case nextRecertificationDays -> row.getNextRecertification();
				case walletBalance -> row.getWalletBalance();
				case tools -> Boolean.TRUE;
				default -> "ERROR";
			};
		}
		
		int propPos = col - AbstractCertificationProgramMembersController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	@Override
	public void setObjects(List<CertificationProgramMemberRow> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}
	
	public enum CertificationProgramMembersCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		identityStatus("table.header.status"),
		certificateStatus("table.header.certificate"),
		recertificationCount("table.header.recertification.count"),
		validUntil("table.header.valid.until"),
		revocationDate("table.header.revocation.date"),
		nextRecertificationDays("table.header.next.recertification.days"),
		walletBalance("table.header.wallet.balance"),
		tools("action.more");
		
		private final String i18nKey;
		
		private CertificationProgramMembersCols(String i18nKey) {
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
