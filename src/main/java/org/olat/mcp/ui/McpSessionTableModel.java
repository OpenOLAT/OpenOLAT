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
package org.olat.mcp.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * The MCP sessions of this node, sorted in memory.
 *
 * Initial date: 14 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpSessionTableModel extends DefaultFlexiTableDataModel<McpSessionRow>
implements SortableFlexiTableDataModel<McpSessionRow> {

	private static final SessionCols[] COLS = SessionCols.values();

	private final Locale locale;

	public McpSessionTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<McpSessionRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		McpSessionRow sessionRow = getObject(row);
		return getValueAt(sessionRow, col);
	}

	@Override
	public Object getValueAt(McpSessionRow row, int col) {
		return switch(COLS[col]) {
			case portrait -> row.getPortraitComponent();
			case user -> row.getDisplayName();
			case username -> row.getUsername();
			case tokenLabel -> row.getTokenLabel();
			case client -> row.getClient();
			case ip -> row.getClientIp();
			case firstRequest -> row.getFirstRequest();
			case lastRequest -> row.getLastRequest();
		};
	}

	public enum SessionCols implements FlexiSortableColumnDef {
		portrait("user.portrait"),
		user("token.owner"),
		username("token.username"),
		tokenLabel("token.label"),
		client("session.client"),
		ip("session.ip"),
		firstRequest("session.first.request"),
		lastRequest("session.last.request");

		private final String i18nKey;

		private SessionCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != portrait;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
