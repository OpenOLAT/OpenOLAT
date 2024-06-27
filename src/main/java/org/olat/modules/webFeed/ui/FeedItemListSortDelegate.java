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
package org.olat.modules.webFeed.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.rating.RatingFormItem;

/**
 * Initial date: Jun 26, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedItemListSortDelegate extends SortableFlexiTableModelDelegate<FeedItemRow> {

	private static final FeedItemTableModel.ItemsCols[] COLS = FeedItemTableModel.ItemsCols.values();

	public FeedItemListSortDelegate(SortKey orderBy, SortableFlexiTableDataModel<FeedItemRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	/**
	 * specific implementation for special cases e.g. FormLinks/Ratings
	 * @param rows
	 */
	@Override
	protected void sort(List<FeedItemRow> rows) {
		int columnIndex = getColumnIndex();
		if (columnIndex == FeedItemTableModel.ItemsCols.comments.ordinal()
				|| columnIndex == FeedItemTableModel.ItemsCols.rating.ordinal()
				|| columnIndex == FeedItemTableModel.ItemsCols.title.ordinal()) {
			switch (COLS[columnIndex]) {
				case title:
					rows.sort(Comparator.comparing(row -> row.getItem().getTitle()));
					break;
				case comments:
					rows.sort(Comparator.comparing(row -> row.getCommentLink().getComponent().getI18n()));
					break;
				case rating:
					rows.sort(Comparator.comparing(row -> ((RatingFormItem) row.getRatingFormItem()).getCurrentRating()));
					break;
				default:
					super.sort(rows);
					break;
			}
		} else {
			super.sort(rows);
		}

	}
}
