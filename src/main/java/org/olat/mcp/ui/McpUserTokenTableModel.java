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
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.mcp.McpToken;

/**
 * The tokens of the current user in the personal settings.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpUserTokenTableModel extends DefaultFlexiTableDataModel<McpToken> {

	private static final UserTokenCols[] COLS = UserTokenCols.values();

	private Map<String,McpTokenUsage> usages = Map.of();

	public McpUserTokenTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		McpToken token = getObject(row);
		return switch(COLS[col]) {
			case label -> token.label();
			case tokenId -> token.tokenId();
			case creationDate -> token.creationDate();
			case expirationDate -> token;
			case auditedRequests -> Integer.valueOf(getUsage(token).count());
			case lastAuditedUse -> getUsage(token).lastUse();
		};
	}

	private McpTokenUsage getUsage(McpToken token) {
		return usages.getOrDefault(token.tokenId(), McpTokenUsage.NONE);
	}

	/**
	 * @param usages The usage per token id for the columns of the audit log
	 */
	public void setUsages(Map<String,McpTokenUsage> usages) {
		this.usages = usages == null ? Map.of() : usages;
	}

	/**
	 * @param label A label
	 * @param locale The locale of the user
	 * @return true if one of the loaded tokens has the label, compared ignoring case
	 */
	public boolean hasLabel(String label, Locale locale) {
		if(label == null) {
			return false;
		}
		String normalized = label.trim().toLowerCase(locale);
		List<McpToken> tokens = getObjects();
		for(McpToken token:tokens) {
			if(token.label() != null && token.label().trim().toLowerCase(locale).equals(normalized)) {
				return true;
			}
		}
		return false;
	}

	public enum UserTokenCols implements FlexiColumnDef {
		label("token.label"),
		tokenId("token.id"),
		creationDate("token.created"),
		expirationDate("token.expires"),
		auditedRequests("token.audited.requests"),
		lastAuditedUse("token.audited.last");

		private final String i18nKey;

		private UserTokenCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
