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
package org.olat.resource.accesscontrol.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 31 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressDataModel extends DefaultFlexiTableDataModel<BillingAddressRow>
implements SortableFlexiTableDataModel<BillingAddressRow> {
	
	private static final BillingAddressCols[] COLS = BillingAddressCols.values();
	private final Locale locale;
	
	public BillingAddressDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<BillingAddressRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		BillingAddressRow emailDomainRow = getObject(row);
		return getValueAt(emailDomainRow, col);
	}

	@Override
	public Object getValueAt(BillingAddressRow row, int col) {
		switch(COLS[col]) {
			case id: return row.getBillingAddress().getKey();
			case identifier: return row.getBillingAddress().getIdentifier();
			case nameLine1: return row.getBillingAddress().getNameLine1();
			case nameLine2: return row.getBillingAddress().getNameLine2();
			case addressLine1: return row.getBillingAddress().getAddressLine1();
			case addressLine2: return row.getBillingAddress().getAddressLine2();
			case addressLine3: return row.getBillingAddress().getAddressLine3();
			case addressLine4: return row.getBillingAddress().getAddressLine4();
			case poBox: return row.getBillingAddress().getPoBox();
			case region: return row.getBillingAddress().getRegion();
			case zip: return row.getBillingAddress().getZip();
			case city: return row.getBillingAddress().getCity();
			case country: return row.getBillingAddress().getCountry();
			case enabled: return Boolean.valueOf(row.getBillingAddress().isEnabled());
			case numOrders: return row.getNumOrders();
			case tools: return row.getToolsLink();
			default: return null;
		}
	}
	
	public enum BillingAddressCols implements FlexiSortableColumnDef {
		id("billing.address.id"),
		identifier("billing.address.identifier"),
		nameLine1("billing.address.name.line1"),
		nameLine2("billing.address.name.line2"),
		addressLine1("billing.address.address.line1"),
		addressLine2("billing.address.address.line2"),
		addressLine3("billing.address.address.line3"),
		addressLine4("billing.address.address.line4"),
		poBox("billing.address.pobox"),
		region("billing.address.region"),
		zip("billing.address.zip"),
		city("billing.address.city"),
		country("billing.address.country"),
		enabled("billing.address.enabled"),
		numOrders("billing.address.num.orders"),
		tools("tools");
		
		private final String i18nKey;
		
		private BillingAddressCols(String i18nKey) {
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
