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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.util.StringHelper;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpService;
import org.olat.mcp.McpToken;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.ui.McpUserTokenTableModel.UserTokenCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The MCP segment of the personal settings: the tokens of the user, the
 * generation of a token with a label and the revocation of a token. The
 * plaintext of a new token is shown once in a modal dialog. A user without
 * a matching enabled profile sees one sentence only.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpUserTokenController extends FormBasicController {

	private static final String CMD_REVOKE = "revoke";

	private FormLink generateButton;
	private McpToolTreeController toolsCtrl;
	private FlexiTableElement tableEl;
	private McpUserTokenTableModel tableModel;

	private CloseableModalController cmc;
	private McpTokenGenerateController generateCtrl;
	private McpTokenCreatedController createdCtrl;
	private ConfirmationController confirmRevokeCtrl;

	private final boolean hasAccess;
	private final McpGrants grants;

	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpService mcpService;
	@Autowired
	private McpTokenService tokenService;

	public McpUserTokenController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "user_tokens");
		grants = mcpModule.isEnabled()
				? mcpService.resolveGrants(getIdentity())
				: McpGrants.EMPTY;
		hasAccess = !grants.isEmpty();
		initForm(ureq);
		if(hasAccess) {
			loadModel();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("hasAccess", Boolean.valueOf(hasAccess));
		}
		if(!hasAccess) {
			return;
		}

		toolsCtrl = new McpToolTreeController(ureq, getWindowControl(), mainForm, grants.tools());
		listenTo(toolsCtrl);
		formLayout.add("tools", toolsCtrl.getInitialFormItem());
		String rateLimit = grants.requestsPerMinute() == null
				? Integer.toString(mcpModule.getRateLimitRequestsPerMinute())
				: grants.requestsPerMinute().toString();
		flc.contextPut("rateLimit", rateLimit);

		generateButton = uifactory.addFormLink("user.generate", formLayout, Link.BUTTON);
		generateButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		generateButton.setElementCssClass("o_sel_mcp_token_generate");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserTokenCols.label));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserTokenCols.tokenId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserTokenCols.creationDate, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserTokenCols.expirationDate,
				new McpTokenExpirationCellRenderer(getLocale(), getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("token.revoke", translate("token.revoke"), CMD_REVOKE));

		tableModel = new McpUserTokenTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "tokens", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_mcp_user_tokens");
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withIconCss("o_icon_password")
				.withMessageI18nKey("user.tokens.empty")
				.build());
	}

	private void loadModel() {
		List<McpToken> tokens = tokenService.getTokens(getIdentity());
		tableModel.setObjects(tokens);
		tableEl.reset(true, true, true);

		boolean generationEnabled = mcpModule.isUserTokenGenerationEnabled();
		int max = mcpModule.getUserTokenMax();
		boolean limitReached = max > 0 && tokens.size() >= max;
		generateButton.setVisible(generationEnabled && !limitReached);
		flc.contextPut("generationDisabled", Boolean.valueOf(!generationEnabled));
		flc.contextPut("limitReached", Boolean.valueOf(generationEnabled && limitReached));
		flc.contextPut("maxTokens", Integer.toString(max));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == generateButton) {
			doOpenGenerate(ureq);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent se && CMD_REVOKE.equals(se.getCommand())) {
				McpToken token = tableModel.getObject(se.getIndex());
				if(token != null) {
					doConfirmRevoke(ureq, token);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == generateCtrl) {
			McpTokenCreation creation = generateCtrl.getCreation();
			cmc.deactivate();
			cleanUp();
			loadModel();
			if(event == Event.DONE_EVENT && creation != null) {
				doShowCreated(ureq, creation);
			}
		} else if(source == createdCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmRevokeCtrl) {
			if(event == Event.DONE_EVENT && confirmRevokeCtrl.getUserObject() instanceof McpToken token) {
				doRevoke(token);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	/**
	 * Disposes the modal controllers. The controller with the plaintext of a
	 * new token is disposed as soon as its dialog closes.
	 */
	private void cleanUp() {
		removeAsListenerAndDispose(confirmRevokeCtrl);
		removeAsListenerAndDispose(generateCtrl);
		removeAsListenerAndDispose(createdCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRevokeCtrl = null;
		generateCtrl = null;
		createdCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doOpenGenerate(UserRequest ureq) {
		if(guardModalController(generateCtrl)) return;

		removeAsListenerAndDispose(generateCtrl);
		removeAsListenerAndDispose(cmc);
		generateCtrl = new McpTokenGenerateController(ureq, getWindowControl(), tableModel);
		listenTo(generateCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), generateCtrl.getInitialComponent(),
				true, translate("user.generate.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doShowCreated(UserRequest ureq, McpTokenCreation creation) {
		removeAsListenerAndDispose(createdCtrl);
		removeAsListenerAndDispose(cmc);
		createdCtrl = new McpTokenCreatedController(ureq, getWindowControl(), creation);
		listenTo(createdCtrl);

		// no close icon: the user leaves the dialog with the button below the token
		cmc = new CloseableModalController(getWindowControl(), translate("close"), createdCtrl.getInitialComponent(),
				true, translate("user.created.title"), false);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmRevoke(UserRequest ureq, McpToken token) {
		if(guardModalController(confirmRevokeCtrl)) return;

		String message = translate("user.revoke.confirm", StringHelper.escapeHtml(token.label()));
		removeAsListenerAndDispose(confirmRevokeCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRevokeCtrl = new ConfirmationController(ureq, getWindowControl(), message, null,
				translate("token.revoke"), ButtonType.danger);
		confirmRevokeCtrl.setUserObject(token);
		listenTo(confirmRevokeCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRevokeCtrl.getInitialComponent(),
				true, translate("token.revoke.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doRevoke(McpToken token) {
		if(tokenService.revokeToken(token.tokenId(), getIdentity())) {
			showInfo("token.revoked", StringHelper.escapeHtml(token.label()));
		} else {
			showWarning("token.revoke.notfound");
		}
		loadModel();
	}
}
