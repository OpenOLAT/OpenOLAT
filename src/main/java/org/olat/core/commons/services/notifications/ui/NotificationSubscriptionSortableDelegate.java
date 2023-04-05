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
package org.olat.core.commons.services.notifications.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;

/**
 * Initial date: Apr 05, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class NotificationSubscriptionSortableDelegate extends SortableFlexiTableModelDelegate<NotificationSubscriptionRow> {

	public NotificationSubscriptionSortableDelegate(SortKey orderBy, SortableFlexiTableDataModel<NotificationSubscriptionRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<NotificationSubscriptionRow> rows) {
		Collections.sort(rows, new NotificationSubscriptionComparator());
	}

	private class NotificationSubscriptionComparator implements Comparator<NotificationSubscriptionRow> {

		@Override
		public int compare(NotificationSubscriptionRow t1, NotificationSubscriptionRow t2) {
			Object val1 = getTableModel().getValueAt(t1, getColumnIndex());
			Object val2 = getTableModel().getValueAt(t2, getColumnIndex());

			if (val1 == null || val2 == null) {
				return compareNullsLast(val1, val2);
			}
			if (val1 instanceof String s1 && val2 instanceof String s2) {

				s1 = StringHelper.containsNonWhitespace(s1) ? s1 : null;
				s2 = StringHelper.containsNonWhitespace(s2) ? s2 : null;

				if (s1 == null || s2 == null) {
					return compareNullsLast(s1, s2);
				}

				return getCollator().compare(s1, s2);
			}
			if (val1 instanceof Date d1 && val2 instanceof Date d2) {
				return compareDateAndTimestamps(d1, d2);
			}
			if (val1 instanceof FormLink c1 && val2 instanceof FormLink c2) {
				return c1.getI18nKey().compareTo(c2.getI18nKey());
			}
			return val1.toString().compareTo(val2.toString());
		}

		private int compareNullsLast(Object o1, Object o2) {
			return (isAsc() ? -1 : 1) * compareNullObjects(o1, o2);
		}
	}
}
