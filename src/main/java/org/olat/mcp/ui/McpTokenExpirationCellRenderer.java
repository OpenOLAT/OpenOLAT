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

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.mcp.McpToken;

/**
 * Renders the end of the validity of a token: the date, "Never" for a token
 * without expiry and a label for an expired token.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpTokenExpirationCellRenderer implements FlexiCellRenderer {

	private final Formatter formatter;
	private final Translator translator;

	public McpTokenExpirationCellRenderer(Locale locale, Translator translator) {
		this.formatter = Formatter.getInstance(locale);
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(cellValue instanceof McpToken token) {
			Date expirationDate = token.expirationDate();
			if(expirationDate == null) {
				target.append(translator.translate("token.expires.never"));
			} else {
				target.append(formatter.formatDate(expirationDate));
				if(token.isExpired(new Date())) {
					target.append(" <span class=\"o_labeled_light o_mcp_token_expired\">")
						.append(translator.translate("token.expired"))
						.append("</span>");
				}
			}
		}
	}
}
