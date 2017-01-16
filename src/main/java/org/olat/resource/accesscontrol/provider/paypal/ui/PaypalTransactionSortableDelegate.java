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
package org.olat.resource.accesscontrol.provider.paypal.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.ui.mode.AssessmentModeListModel.Cols;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;

/**
 * 
 * Initial date: 5 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalTransactionSortableDelegate extends SortableFlexiTableModelDelegate<PaypalTransaction> {
	
	public PaypalTransactionSortableDelegate(SortKey orderBy, PaypalTransactionDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<PaypalTransaction> rows) {
		int columnIndex = getColumnIndex();
		Cols column = Cols.values()[columnIndex];
		switch(column) {
			case status: Collections.sort(rows, new StatusComparator()); break;
			default: {
				super.sort(rows);
			}
		}
	}
	
	private class StatusComparator implements Comparator<PaypalTransaction> {
		@Override
		public int compare(PaypalTransaction o1, PaypalTransaction o2) {
			PaypalMergedState s1 = PaypalMergedState.value(o1);
			PaypalMergedState s2 = PaypalMergedState.value(o2);
			int c = s1.compareTo(s2);
			if(c == 0) {
				c = compareString(o1.getRefNo(), o2.getRefNo());
			}
			return c;
		}
	}
}
