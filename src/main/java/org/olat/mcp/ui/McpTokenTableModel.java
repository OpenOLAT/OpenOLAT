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
import java.util.Map;

import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.util.StringHelper;
import org.olat.mcp.McpToken;
import org.olat.mcp.McpTokenService;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.user.UserManager;

/**
 * The paginated list of every MCP token for the administration. The search
 * field of the table filters on label, token id, username and email. The
 * service orders the tokens by creation date, newest first.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpTokenTableModel extends DefaultFlexiTableDataSourceModel<McpToken> {

	private static final TokenCols[] COLS = TokenCols.values();

	private final UserManager userManager;

	public McpTokenTableModel(FlexiTableDataSourceDelegate<McpToken> dataSource, FlexiTableColumnModel columnModel,
			UserManager userManager) {
		super(dataSource, columnModel);
		this.userManager = userManager;
	}

	@Override
	public Object getValueAt(int row, int col) {
		McpToken token = getObject(row);
		if(token == null) {
			return null;
		}
		return switch(COLS[col]) {
			case owner -> token.identity() == null ? null : userManager.getUserDisplayName(token.identity().getUser());
			case username -> token.identity() == null ? null : token.identity().getUser().getNickName();
			case label -> token.label();
			case tokenId -> token.tokenId();
			case creationDate -> token.creationDate();
			case expirationDate -> token;
			case auditedRequests -> Integer.valueOf(getUsage(token).count());
			case lastAuditedUse -> getUsage(token).lastUse();
		};
	}

	private McpTokenUsage getUsage(McpToken token) {
		if(getSourceDelegate() instanceof TokenDataSource tokenDataSource) {
			return tokenDataSource.getUsage(token.tokenId());
		}
		return McpTokenUsage.NONE;
	}

	@Override
	public McpTokenTableModel createCopyWithEmptyList() {
		return new McpTokenTableModel(getSourceDelegate(), getTableColumnModel(), userManager);
	}

	public enum TokenCols implements FlexiColumnDef {
		owner("token.owner"),
		username("token.username"),
		label("token.label"),
		tokenId("token.id"),
		creationDate("token.created"),
		expirationDate("token.expires"),
		auditedRequests("token.audited.requests"),
		lastAuditedUse("token.audited.last");

		private final String i18nKey;

		private TokenCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

	/**
	 * Loads one page of tokens with {@link McpTokenService#searchTokens(String, int, int)}.
	 * The count follows the last search string.
	 */
	public static class TokenDataSource implements FlexiTableDataSourceDelegate<McpToken> {

		private static final int MAX_PAGE_SIZE = 500;

		private final McpTokenService tokenService;
		private final ApiAuditLogService auditLogService;
		private String searchString;
		private Integer count;
		private Map<String,McpTokenUsage> usages = Map.of();

		public TokenDataSource(McpTokenService tokenService, ApiAuditLogService auditLogService) {
			this.tokenService = tokenService;
			this.auditLogService = auditLogService;
		}

		/**
		 * @param tokenId The token id
		 * @return The usage of a token of the current page
		 */
		public McpTokenUsage getUsage(String tokenId) {
			return usages.getOrDefault(tokenId, McpTokenUsage.NONE);
		}

		public void reset() {
			count = null;
		}

		@Override
		public int getRowCount() {
			if(count == null) {
				count = Integer.valueOf(tokenService.countSearchTokens(searchString));
			}
			return count.intValue();
		}

		@Override
		public List<McpToken> reload(List<McpToken> rows) {
			return rows;
		}

		@Override
		public ResultInfos<McpToken> getRows(String query, List<FlexiTableFilter> filters,
				int firstResult, int maxResults, SortKey... orderBy) {
			String search = StringHelper.containsNonWhitespace(query) ? query.trim() : null;
			if(search == null ? searchString != null : !search.equals(searchString)) {
				searchString = search;
				count = null;
			}
			// bounded even if the table asks for all rows
			int pageSize = maxResults > 0 ? Math.min(maxResults, MAX_PAGE_SIZE) : MAX_PAGE_SIZE;
			List<McpToken> rows = tokenService.searchTokens(searchString, firstResult, pageSize);
			// one audit query for the owners of the page
			usages = McpTokenUsage.load(auditLogService, rows);
			return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
		}
	}
}
