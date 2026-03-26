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
package org.olat.modules.curriculum.ui.component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.NullOrder;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.CurriculumElementStatus;

/**
 * Initial date: 24 Mar 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class RelevanceSortDelegate<T extends RelevanceSortable> extends SortableFlexiTableModelDelegate<T> {

	public static final String SORT_KEY = "relevant";

	public RelevanceSortDelegate(SortKey orderBy, SortableFlexiTableDataModel<T> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<T> rows) {
		if(SORT_KEY.equals(getOrderBy().getKey())) {
			Collections.sort(rows, new RelevanceComparator());
		} else {
			super.sort(rows);
		}
	}

	private class RelevanceComparator implements Comparator<T> {
		@Override
		public int compare(T o1, T o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2, NullOrder.NULLS_ALWAYS_LAST);
			}

			int c = Integer.compare(statusOrder(o1.getElementStatus()), statusOrder(o2.getElementStatus()));
			if(c != 0) return c;

			c = compareDateAndTimestamps(o1.getBeginDate(), o2.getBeginDate());
			if(c != 0) return c;

			c = compareDateAndTimestamps(o1.getEndDate(), o2.getEndDate());
			if(c != 0) return c;

			return compareString(o1.getDisplayName(), o2.getDisplayName());
		}

		private int statusOrder(CurriculumElementStatus status) {
			if(status == null) return 99;
			return switch(status) {
				case preparation -> 0;
				case provisional -> 1;
				case confirmed -> 2;
				case active -> 3;
				case cancelled -> 4;
				case finished -> 5;
				default -> 99;
			};
		}
	}
}
