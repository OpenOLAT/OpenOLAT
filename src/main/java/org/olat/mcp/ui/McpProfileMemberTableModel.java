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
package org.olat.mcp.ui;

import java.util.List;

import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.id.Identity;
import org.olat.mcp.McpProfileRef;
import org.olat.mcp.McpService;
import org.olat.user.UserManager;

/**
 * The paginated list of the identities with an explicit grant on a user
 * profile. The service orders them by key, the table is not sortable.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpProfileMemberTableModel extends DefaultFlexiTableDataSourceModel<Identity> {

	private static final MemberCols[] COLS = MemberCols.values();

	private final UserManager userManager;

	public McpProfileMemberTableModel(FlexiTableDataSourceDelegate<Identity> dataSource,
			FlexiTableColumnModel columnModel, UserManager userManager) {
		super(dataSource, columnModel);
		this.userManager = userManager;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity identity = getObject(row);
		if(identity == null) {
			return null;
		}
		return switch(COLS[col]) {
			case name -> userManager.getUserDisplayName(identity.getUser());
			case username -> identity.getUser().getNickName();
			case email -> identity.getUser().getEmail();
		};
	}

	@Override
	public McpProfileMemberTableModel createCopyWithEmptyList() {
		return new McpProfileMemberTableModel(getSourceDelegate(), getTableColumnModel(), userManager);
	}

	public enum MemberCols implements FlexiColumnDef {
		name("member.name"),
		username("member.username"),
		email("member.email");

		private final String i18nKey;

		private MemberCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

	/**
	 * Loads one page of members with {@link McpService#getGrantedIdentities(McpProfileRef, int, int)}.
	 */
	public static class MemberDataSource implements FlexiTableDataSourceDelegate<Identity> {

		private static final int MAX_PAGE_SIZE = 500;

		private final McpProfileRef profile;
		private final McpService mcpService;
		private Integer count;

		public MemberDataSource(McpProfileRef profile, McpService mcpService) {
			this.profile = profile;
			this.mcpService = mcpService;
		}

		public void reset() {
			count = null;
		}

		@Override
		public int getRowCount() {
			if(count == null) {
				count = Integer.valueOf(mcpService.countGrantedIdentities(profile));
			}
			return count.intValue();
		}

		@Override
		public List<Identity> reload(List<Identity> rows) {
			return rows;
		}

		@Override
		public ResultInfos<Identity> getRows(String query, List<FlexiTableFilter> filters,
				int firstResult, int maxResults, SortKey... orderBy) {
			// bounded even if the table asks for all rows
			int pageSize = maxResults > 0 ? Math.min(maxResults, MAX_PAGE_SIZE) : MAX_PAGE_SIZE;
			List<Identity> rows = mcpService.getGrantedIdentities(profile, firstResult, pageSize);
			return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
		}
	}
}
