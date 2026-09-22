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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shows one row of the API audit log. Every value comes from a client, the
 * request body and the path parameters are escaped before they are rendered.
 * 
 * Initial date: 17 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditLogDetailsController extends FormBasicController {
	
	private static final String EMPTY = "-";
	
	private final ApiAuditLog auditLog;
	
	@Autowired
	private UserManager userManager;
	
	public ApiAuditLogDetailsController(UserRequest ureq, WindowControl wControl, ApiAuditLog auditLog) {
		super(ureq, wControl);
		this.auditLog = auditLog;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Formatter formatter = Formatter.getInstance(getLocale());
		addText("creationDate", "auditlog.col.creationDate",
				auditLog.getCreationDate() == null ? null : formatter.formatDateAndTime(auditLog.getCreationDate()), formLayout);
		addText("channel", "auditlog.col.channel",
				auditLog.getChannel() == null ? null : translate(auditLog.getChannel().i18nKey()), formLayout);
		addText("identity", "auditlog.col.identity",
				auditLog.getIdentity() == null ? null : userManager.getUserDisplayName(auditLog.getIdentity()), formLayout);
		addText("loginAttempt", "auditlog.col.loginAttempt", auditLog.getLoginAttempt(), formLayout);
		addText("authProvider", "auditlog.col.authProvider", auditLog.getAuthProvider(), formLayout);
		addText("ip", "auditlog.col.ip", auditLog.getIp(), formLayout);
		addText("userAgent", "auditlog.col.userAgent", auditLog.getUserAgent(), formLayout);
		addText("nodeId", "auditlog.col.nodeId",
				auditLog.getNodeId() == null ? null : auditLog.getNodeId().toString(), formLayout);
		addText("method", "auditlog.col.method", auditLog.getMethod(), formLayout);
		addText("path", "auditlog.col.path", auditLog.getPath(), formLayout);
		addText("query", "auditlog.col.query", auditLog.getQuery(), formLayout);
		addText("resourceClass", "auditlog.col.resourceClass", auditLog.getResourceClass(), formLayout);
		addText("resourceMethod", "auditlog.col.resourceMethod", auditLog.getResourceMethod(), formLayout);
		addText("status", "auditlog.col.status", Integer.toString(auditLog.getStatus()), formLayout);
		addText("durationMs", "auditlog.col.durationMs",
				auditLog.getDurationMs() == null ? null : auditLog.getDurationMs().toString(), formLayout);
		addText("ref", "auditlog.col.ref", auditLog.getRef(), formLayout);
		addCode("pathParams", "auditlog.col.pathParams", auditLog.getPathParams(), formLayout);
		addCode("requestBody", "auditlog.col.requestBody", auditLog.getRequestBody(), formLayout);
	}
	
	private void addText(String name, String i18nLabel, String value, FormItemContainer formLayout) {
		String escaped = StringHelper.containsNonWhitespace(value) ? StringHelper.escapeHtml(value) : EMPTY;
		uifactory.addStaticTextElement(name, i18nLabel, escaped, formLayout);
	}
	
	private void addCode(String name, String i18nLabel, String value, FormItemContainer formLayout) {
		if(!StringHelper.containsNonWhitespace(value)) {
			uifactory.addStaticTextElement(name, i18nLabel, EMPTY, formLayout);
			return;
		}
		String escaped = StringHelper.escapeHtml(value);
		uifactory.addStaticTextElement(name, i18nLabel, "<pre class=\"o_code\">" + escaped + "</pre>", formLayout);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
