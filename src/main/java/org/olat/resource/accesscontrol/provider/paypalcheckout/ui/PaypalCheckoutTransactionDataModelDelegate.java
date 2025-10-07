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
package org.olat.resource.accesscontrol.provider.paypalcheckout.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.resource.accesscontrol.provider.paypalcheckout.ui.PaypalCheckoutTransactionDataModel.CheckoutCols;

/**
 * 
 * Initial date: 7 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class PaypalCheckoutTransactionDataModelDelegate extends SortableFlexiTableModelDelegate<PaypalCheckoutTransactionRow> {

	private static final CheckoutCols[] COLS = CheckoutCols.values();
	
	public PaypalCheckoutTransactionDataModelDelegate(SortKey orderBy, PaypalCheckoutTransactionDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<PaypalCheckoutTransactionRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex < PaypalCheckoutTransactionsController.USER_PROPS_OFFSET) {
			switch(COLS[columnIndex]) {
				case status: Collections.sort(rows, new StatusComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}

	private class StatusComparator implements Comparator<PaypalCheckoutTransactionRow> {
		@Override
		public int compare(PaypalCheckoutTransactionRow o1, PaypalCheckoutTransactionRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			String r1 = o1.getPaypalOrderStatus();
			String r2 = o2.getPaypalOrderStatus();
			int c = compareString(r1, r2);
			
			if(c == 0) {
				Long k1 = o1.getKey();
				Long k2 = o2.getKey();
				c = k1.compareTo(k2);
			}
			return c;
		}
	}
}
