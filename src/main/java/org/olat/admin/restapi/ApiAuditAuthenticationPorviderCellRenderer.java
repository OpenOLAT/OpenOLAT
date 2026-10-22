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
package org.olat.admin.restapi;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.mcp.McpTokenService;
import org.olat.restapi.audit.ApiAuditLogService;

/**
 * 
 * Initial date: 22 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApiAuditAuthenticationPorviderCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public ApiAuditAuthenticationPorviderCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator transl) {
		if(cellValue instanceof String type) {
			switch(type) {
				case ApiAuditLogService.AUTH_API_KEY: target.append(translator.translate("auditlog.auth.apikey")); break;
				case ApiAuditLogService.AUTH_PASSWORD: target.append(translator.translate("auditlog.auth.password")); break;
				case ApiAuditLogService.AUTH_TOKEN: target.append(translator.translate("auditlog.auth.token")); break;
				case ApiAuditLogService.AUTH_SESSION: target.append(translator.translate("auditlog.auth.session")); break;
				case ApiAuditLogService.AUTH_IP: target.append(translator.translate("auditlog.auth.ip")); break;
				case McpTokenService.MCP_AUTH_PROVIDER: target.append(translator.translate("auditlog.auth.mcp")); break;
				case ApiAuditLogService.AUTH_NONE: target.append(translator.translate("auditlog.auth.none")); break;
				default: target.append(type);
			}
		}
	}
}
