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
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.util.StringHelper;
import org.olat.mcp.McpToken;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.ui.McpTokenTableModel.TokenCols;
import org.olat.mcp.ui.McpTokenTableModel.TokenDataSource;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The tokens tab of the MCP administration: every MCP token of every user,
 * with a search field and the revocation of one token after a confirmation.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpTokenAdminController extends FormBasicController {

	private static final String CMD_REVOKE = "revoke";

	private FlexiTableElement tableEl;
	private TokenDataSource dataSource;
	private McpTokenTableModel tableModel;

	private CloseableModalController cmc;
	private ConfirmationController confirmRevokeCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private McpTokenService tokenService;
	@Autowired
	private ApiAuditLogService auditLogService;

	public McpTokenAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "tokens");
		initForm(ureq);
		reloadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TokenCols.owner));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TokenCols.username));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TokenCols.label));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TokenCols.tokenId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TokenCols.creationDate, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TokenCols.expirationDate,
				new McpTokenExpirationCellRenderer(getLocale(), getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TokenCols.auditedRequests));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TokenCols.lastAuditedUse, new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("token.revoke", translate("token.revoke"), CMD_REVOKE));

		dataSource = new TokenDataSource(tokenService, auditLogService);
		tableModel = new McpTokenTableModel(dataSource, columnsModel, userManager);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_mcp_tokens_table");
		tableEl.setSearchEnabled(true);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withIconCss("o_icon_password")
				.withMessageI18nKey("token.admin.empty")
				.build());
		tableEl.setAndLoadPersistedPreferences(ureq, "mcp-admin-tokens-v2");
	}

	/**
	 * Reloads the current page, for example after a revocation in another tab.
	 */
	public void reloadModel() {
		dataSource.reset();
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
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
		if(source == confirmRevokeCtrl) {
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

	private void cleanUp() {
		removeAsListenerAndDispose(confirmRevokeCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRevokeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doConfirmRevoke(UserRequest ureq, McpToken token) {
		if(guardModalController(confirmRevokeCtrl)) return;

		String owner = token.identity() == null ? "" : userManager.getUserDisplayName(token.identity().getUser());
		String message = translate("token.admin.revoke.confirm", StringHelper.escapeHtml(token.label()),
				StringHelper.escapeHtml(token.tokenId()), StringHelper.escapeHtml(owner));
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
		if(tokenService.revokeToken(token.tokenId(), null)) {
			showInfo("token.revoked", StringHelper.escapeHtml(token.label()));
		} else {
			showWarning("token.revoke.notfound");
		}
		reloadModel();
	}
}
