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

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;

/**
 * Initial date: Mai 21, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedItemTableModel extends DefaultFlexiTableDataModel<FeedItemRow>
implements FlexiTableCssDelegate, FilterableFlexiTableModel {

	private static final ItemsCols[] COLS = ItemsCols.values();

	public FeedItemTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {

	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return "";
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return "";
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		return "";
	}

	@Override
	public Object getValueAt(int row, int col) {
		FeedItemRow item = getObject(row);
		if(col >= 0 && col < COLS.length) {
			return switch (COLS[col]) {
				case title -> item.getFeedEntryLink();
				case status -> item.getStatus();
				case publishDate -> item.getPublishDate();
				case author -> item.getAuthor();
				case tags -> item.getTags();
				case changedFrom -> item.getChangedFrom();
				case rating -> item.getRatingFormItem();
				case comments -> item.getCommentLink();
				default -> "ERROR";
			};
		}
		return null;
	}


	protected enum ItemsCols implements FlexiSortableColumnDef {
		title("table.feed.header.title"),
		status("table.feed.header.status"),
		publishDate("table.feed.header.publish.date"),
		author("table.feed.header.author"),
		tags("table.feed.header.tags"),
		changedFrom("table.feed.header.modifier"),
		rating("table.feed.header.rating"),
		comments("table.feed.header.comments"),
		toolsLink("table.feed.header.actions");

		private final String i18nKey;

		ItemsCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
