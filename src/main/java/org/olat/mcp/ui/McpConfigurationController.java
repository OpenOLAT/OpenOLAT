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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.mcp.McpModule;
import org.olat.restapi.RestModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The configuration tab of the MCP administration: the module switch, the
 * endpoint with the client command, the token rules, the request limits and
 * the audit log. Every setter of {@link McpModule} for an administrator has
 * a form element here.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpConfigurationController extends FormBasicController {

	private static final String ON_KEY = "on";
	private static final String[] ON_KEYS = { ON_KEY };

	static final int MAX_TOKENS_LIMIT = 1000;
	static final int VALIDITY_DAYS_LIMIT = 3650;
	static final int SESSION_TIMEOUT_MIN = 30;
	static final int SESSION_TIMEOUT_MAX = 86400;
	static final int REQUESTS_LIMIT = 100000;
	static final int PARALLEL_LIMIT = 100;
	static final int PARALLEL_NODE_LIMIT = 1000;
	static final int BODY_SIZE_MIN = 1024;
	static final int BODY_SIZE_MAX = 1048576;

	private FormToggle enabledEl;
	private FormLayoutContainer endpointCont;
	private FormLayoutContainer commandCont;

	private FormLayoutContainer tokenCont;
	private MultipleSelectionElement userTokenGenerationEl;
	private IntegerElement userTokenMaxEl;
	private IntegerElement tokenValidityDaysEl;
	private IntegerElement sessionTimeoutEl;

	private FormLayoutContainer rateLimitCont;
	private FormToggle rateLimitEnabledEl;
	private IntegerElement requestsPerMinuteEl;
	private IntegerElement maxParallelEl;
	private IntegerElement maxParallelNodeEl;
	private IntegerElement anonymousRequestsPerMinuteEl;

	private FormLayoutContainer auditCont;
	private FormToggle auditEnabledEl;
	private MultipleSelectionElement auditReadsEl;
	private MultipleSelectionElement auditBodyEl;
	private IntegerElement auditBodyMaxSizeEl;

	@Autowired
	private McpModule mcpModule;
	@Autowired
	private RestModule restModule;

	public McpConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "configuration");
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initServerForm(formLayout);
		initTokenForm(formLayout);
		initRateLimitForm(formLayout);
		initAuditForm(formLayout);

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	private void initServerForm(FormItemContainer formLayout) {
		FormLayoutContainer serverCont = uifactory.addDefaultFormLayout("server", null, formLayout);
		serverCont.setFormTitle(translate("config.server.title"));
		serverCont.setFormInfo(translate("config.server.intro"));

		enabledEl = uifactory.addToggleButton("config.enabled", "config.enabled", translate("on"), translate("off"), serverCont);
		enabledEl.setElementCssClass("o_sel_mcp_enabled");
		enabledEl.toggle(mcpModule.isEnabled());
		enabledEl.addActionListener(FormEvent.ONCHANGE);

		String endpointPage = velocity_root + "/endpoint.html";
		endpointCont = uifactory.addCustomFormLayout("endpoint", "config.endpoint", endpointPage, serverCont);
		endpointCont.contextPut("endpointUrl", McpUIHelper.getEndpointUrl());

		String commandPage = velocity_root + "/command.html";
		commandCont = uifactory.addCustomFormLayout("command", "config.command", commandPage, serverCont);
		commandCont.contextPut("command", McpUIHelper.getClaudeCodeCommand(McpUIHelper.TOKEN_PLACEHOLDER));
		commandCont.contextPut("commandHint", translate("config.command.hint"));
	}

	private void initTokenForm(FormItemContainer formLayout) {
		tokenCont = uifactory.addDefaultFormLayout("tokens", null, formLayout);
		tokenCont.setFormTitle(translate("config.tokens.title"));
		tokenCont.setFormInfo(translate("config.tokens.intro"));

		userTokenGenerationEl = uifactory.addCheckboxesHorizontal("config.user.token.generation", tokenCont,
				ON_KEYS, new String[] { translate("config.user.token.generation.on") });
		userTokenGenerationEl.select(ON_KEY, mcpModule.isUserTokenGenerationEnabled());

		userTokenMaxEl = uifactory.addIntegerElement("config.user.token.max", "config.user.token.max",
				mcpModule.getUserTokenMax(), tokenCont);
		initIntegerElement(userTokenMaxEl, "config.user.token.max.hint");

		tokenValidityDaysEl = uifactory.addIntegerElement("config.token.validity.days", "config.token.validity.days",
				mcpModule.getTokenValidityDays(), tokenCont);
		initIntegerElement(tokenValidityDaysEl, "config.token.validity.days.hint");

		sessionTimeoutEl = uifactory.addIntegerElement("config.session.timeout", "config.session.timeout",
				mcpModule.getSessionTimeout(), tokenCont);
		initIntegerElement(sessionTimeoutEl, "config.session.timeout.hint");
	}

	private void initRateLimitForm(FormItemContainer formLayout) {
		rateLimitCont = uifactory.addDefaultFormLayout("rateLimit", null, formLayout);
		rateLimitCont.setFormTitle(translate("config.ratelimit.title"));
		rateLimitCont.setFormInfo(translate("config.ratelimit.intro"));

		rateLimitEnabledEl = uifactory.addToggleButton("config.ratelimit.enabled", "config.ratelimit.enabled",
				translate("on"), translate("off"), rateLimitCont);
		rateLimitEnabledEl.toggle(mcpModule.isRateLimitEnabled());
		rateLimitEnabledEl.addActionListener(FormEvent.ONCHANGE);

		requestsPerMinuteEl = uifactory.addIntegerElement("config.ratelimit.requests.per.minute", "config.ratelimit.requests.per.minute",
				mcpModule.getRateLimitRequestsPerMinute(), rateLimitCont);
		initIntegerElement(requestsPerMinuteEl, "config.ratelimit.requests.per.minute.hint");

		maxParallelEl = uifactory.addIntegerElement("config.ratelimit.max.parallel", "config.ratelimit.max.parallel",
				mcpModule.getRateLimitMaxParallel(), rateLimitCont);
		initIntegerElement(maxParallelEl, "config.ratelimit.per.node.hint");

		maxParallelNodeEl = uifactory.addIntegerElement("config.ratelimit.max.parallel.node", "config.ratelimit.max.parallel.node",
				mcpModule.getRateLimitMaxParallelNode(), rateLimitCont);
		initIntegerElement(maxParallelNodeEl, "config.ratelimit.max.parallel.node.hint");

		anonymousRequestsPerMinuteEl = uifactory.addIntegerElement("config.ratelimit.anonymous.requests.per.minute",
				"config.ratelimit.anonymous.requests.per.minute", mcpModule.getRateLimitAnonymousRequestsPerMinute(), rateLimitCont);
		initIntegerElement(anonymousRequestsPerMinuteEl, "config.ratelimit.per.node.hint");
	}

	private void initAuditForm(FormItemContainer formLayout) {
		auditCont = uifactory.addDefaultFormLayout("audit", null, formLayout);
		auditCont.setFormTitle(translate("config.auditlog.title"));
		auditCont.setFormInfo(translate("config.auditlog.intro", Integer.toString(restModule.getAuditLogRetentionDays())));

		auditEnabledEl = uifactory.addToggleButton("config.auditlog.enabled", "config.auditlog.enabled",
				translate("on"), translate("off"), auditCont);
		auditEnabledEl.toggle(mcpModule.isAuditLogEnabled());
		auditEnabledEl.addActionListener(FormEvent.ONCHANGE);

		auditReadsEl = uifactory.addCheckboxesHorizontal("config.auditlog.reads", auditCont,
				ON_KEYS, new String[] { translate("config.auditlog.reads.on") });
		auditReadsEl.select(ON_KEY, mcpModule.isAuditLogReads());
		auditReadsEl.setExampleKey("config.auditlog.reads.hint", null);

		auditBodyEl = uifactory.addCheckboxesHorizontal("config.auditlog.body", auditCont,
				ON_KEYS, new String[] { translate("config.auditlog.body.on") });
		auditBodyEl.select(ON_KEY, mcpModule.isAuditLogBody());
		auditBodyEl.setExampleKey("config.auditlog.body.hint", null);
		auditBodyEl.addActionListener(FormEvent.ONCHANGE);

		auditBodyMaxSizeEl = uifactory.addIntegerElement("config.auditlog.body.maxsize", "config.auditlog.body.maxsize",
				mcpModule.getAuditLogBodyMaxSize(), auditCont);
		initIntegerElement(auditBodyMaxSizeEl, null);
	}

	private void initIntegerElement(IntegerElement el, String hintKey) {
		el.setMandatory(true);
		el.setDisplaySize(8);
		if(hintKey != null) {
			el.setExampleKey(hintKey, null);
		}
	}

	private void updateUI() {
		boolean enabled = enabledEl.isOn();
		endpointCont.setVisible(enabled);
		commandCont.setVisible(enabled);
		tokenCont.setVisible(enabled);
		rateLimitCont.setVisible(enabled);
		auditCont.setVisible(enabled);

		boolean rateLimit = rateLimitEnabledEl.isOn();
		requestsPerMinuteEl.setVisible(rateLimit);
		maxParallelEl.setVisible(rateLimit);
		maxParallelNodeEl.setVisible(rateLimit);
		anonymousRequestsPerMinuteEl.setVisible(rateLimit);

		boolean audit = auditEnabledEl.isOn();
		auditReadsEl.setVisible(audit);
		auditBodyEl.setVisible(audit);
		auditBodyMaxSizeEl.setVisible(audit && auditBodyEl.isAtLeastSelected(1));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enabledEl || source == rateLimitEnabledEl
				|| source == auditEnabledEl || source == auditBodyEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		// the sections are hidden and not saved while the module is switched off
		if(enabledEl.isOn()) {
			allOk &= validateInteger(userTokenMaxEl, 0, MAX_TOKENS_LIMIT);
			allOk &= validateInteger(tokenValidityDaysEl, 0, VALIDITY_DAYS_LIMIT);
			allOk &= validateInteger(sessionTimeoutEl, SESSION_TIMEOUT_MIN, SESSION_TIMEOUT_MAX);
			allOk &= validateInteger(requestsPerMinuteEl, 1, REQUESTS_LIMIT);
			allOk &= validateInteger(maxParallelEl, 1, PARALLEL_LIMIT);
			allOk &= validateInteger(maxParallelNodeEl, 0, PARALLEL_NODE_LIMIT);
			allOk &= validateInteger(anonymousRequestsPerMinuteEl, 1, REQUESTS_LIMIT);
			allOk &= validateInteger(auditBodyMaxSizeEl, BODY_SIZE_MIN, BODY_SIZE_MAX);
		}

		return allOk;
	}

	private int intValue(IntegerElement el) {
		return McpUIHelper.getInteger(el).intValue();
	}

	private boolean validateInteger(IntegerElement el, int min, int max) {
		return McpUIHelper.validateInteger(el, min, max, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isOn();
		mcpModule.setEnabled(enabled);
		if(enabled) {
			mcpModule.setUserTokenGenerationEnabled(userTokenGenerationEl.isAtLeastSelected(1));
			mcpModule.setUserTokenMax(intValue(userTokenMaxEl));
			mcpModule.setTokenValidityDays(intValue(tokenValidityDaysEl));
			mcpModule.setSessionTimeout(intValue(sessionTimeoutEl));

			boolean rateLimit = rateLimitEnabledEl.isOn();
			mcpModule.setRateLimitEnabled(rateLimit);
			if(rateLimit) {
				mcpModule.setRateLimitRequestsPerMinute(intValue(requestsPerMinuteEl));
				mcpModule.setRateLimitMaxParallel(intValue(maxParallelEl));
				mcpModule.setRateLimitMaxParallelNode(intValue(maxParallelNodeEl));
				mcpModule.setRateLimitAnonymousRequestsPerMinute(intValue(anonymousRequestsPerMinuteEl));
			}

			boolean audit = auditEnabledEl.isOn();
			mcpModule.setAuditLogEnabled(audit);
			if(audit) {
				boolean body = auditBodyEl.isAtLeastSelected(1);
				mcpModule.setAuditLogReads(auditReadsEl.isAtLeastSelected(1));
				mcpModule.setAuditLogBody(body);
				if(body) {
					mcpModule.setAuditLogBodyMaxSize(intValue(auditBodyMaxSizeEl));
				}
			}
		}
		showInfo("info.saved");
	}
}
