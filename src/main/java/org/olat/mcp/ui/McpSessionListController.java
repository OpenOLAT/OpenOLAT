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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpToken;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.manager.McpIdentityCache;
import org.olat.mcp.manager.McpIdentityEntry;
import org.olat.mcp.manager.McpSessionActivity;
import org.olat.mcp.ui.McpSessionTableModel.SessionCols;
import org.olat.user.PortraitSize;
import org.olat.user.PortraitUser;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitComponent;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The segment "MCP sessions" of the session administration: the entries of
 * the MCP identity cache of this node, one per user and token in use. The
 * administrator can revoke the token of a session; the revocation drops the
 * entry from the cache.
 *
 * Initial date: 14 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpSessionListController extends FormBasicController {

	private static final String CMD_REVOKE = "revoke";
	private static final String CMD_USER = "user";
	private static final int DEFAULT_TIMEOUT = 300;

	private FormLink refreshButton;
	private FlexiTableElement tableEl;
	private McpSessionTableModel tableModel;

	private CloseableModalController cmc;
	private ConfirmationController confirmRevokeCtrl;

	@Autowired
	private McpModule mcpModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private McpTokenService tokenService;
	@Autowired
	private McpIdentityCache identityCache;
	@Autowired
	private UserPortraitService userPortraitService;

	public McpSessionListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "sessions");
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int timeout = mcpModule.getSessionTimeout() > 0 ? mcpModule.getSessionTimeout() : DEFAULT_TIMEOUT;
		flc.contextPut("intro", translate("sessions.intro", Integer.toString(timeout)));

		refreshButton = uifactory.addFormLink("sessions.refresh", formLayout, Link.BUTTON);
		refreshButton.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
		refreshButton.setElementCssClass("o_sel_mcp_sessions_refresh");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel portraitCol = new DefaultFlexiColumnModel(SessionCols.portrait);
		portraitCol.setExportable(false);
		portraitCol.setIconHeader("o_icon o_icon_profile");
		portraitCol.setHeaderTooltip(translate("user.portrait"));
		columnsModel.addFlexiColumnModel(portraitCol);
		// person and username link to the user in the user management
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SessionCols.user, CMD_USER,
				new StaticFlexiCellRenderer(CMD_USER, new TextFlexiCellRenderer())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SessionCols.username, CMD_USER,
				new StaticFlexiCellRenderer(CMD_USER, new TextFlexiCellRenderer())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SessionCols.tokenLabel));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SessionCols.client));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SessionCols.ip));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SessionCols.firstRequest, new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SessionCols.lastRequest, new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("session.revoke", translate("session.revoke"), CMD_REVOKE));

		tableModel = new McpSessionTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_mcp_sessions_table");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withIconCss("o_icon_password")
				.withMessageI18nKey("sessions.empty")
				.build());
		tableEl.setAndLoadPersistedPreferences(ureq, "mcp-admin-sessions-v1");
	}

	/**
	 * Reads the entries of the identity cache of this node and loads the
	 * identities in one query for the names and the portraits.
	 */
	private void loadModel() {
		List<McpIdentityEntry> entries = identityCache.entries();
		List<Long> identityKeys = entries.stream()
				.map(McpIdentityEntry::getIdentityKey)
				.filter(key -> key != null)
				.distinct()
				.toList();
		Map<Long,Identity> identities = identityKeys.isEmpty() ? Map.of()
				: securityManager.loadIdentityByKeys(identityKeys).stream()
					.collect(Collectors.toMap(Identity::getKey, Function.identity(), (u, v) -> u));

		List<McpSessionRow> rows = new ArrayList<>(entries.size());
		for(McpIdentityEntry entry:entries) {
			Identity identity = identities.get(entry.getIdentityKey());
			if(identity == null) {
				continue;
			}
			McpSessionActivity activity = entry.activity();
			McpSessionRow row = new McpSessionRow(identity.getKey(), userManager.getUserDisplayName(identity),
					identity.getUser().getNickName(), entry.token(), activity.getUserAgent(), activity.getClientIp(),
					toDate(activity.getFirstRequest()), toDate(activity.getLastRequest()));
			forgePortrait(row, identity, rows.size());
			rows.add(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void forgePortrait(McpSessionRow row, Identity identity, int index) {
		// one component per row, the same identity can have several tokens
		String portraitId = "portrait_" + index;
		Component comp = tableEl.getComponent().getComponent(portraitId);
		UserPortraitComponent portraitComp;
		if(comp instanceof UserPortraitComponent existing) {
			portraitComp = existing;
		} else {
			portraitComp = UserPortraitFactory.createUserPortrait(portraitId, tableEl, getLocale());
			portraitComp.setSize(PortraitSize.small);
		}
		PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), identity);
		portraitComp.setPortraitUser(portraitUser);
		row.setPortraitComponent(portraitComp);
	}

	private static Date toDate(long time) {
		return time > 0 ? new Date(time) : null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == refreshButton) {
			loadModel();
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent se) {
				McpSessionRow row = tableModel.getObject(se.getIndex());
				if(row != null && CMD_REVOKE.equals(se.getCommand()) && row.getToken() != null) {
					doConfirmRevoke(ureq, row);
				} else if(row != null && CMD_USER.equals(se.getCommand())) {
					doOpenUser(ureq, row);
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

	/**
	 * Opens the user in the user management, the business path of the
	 * user search table.
	 */
	private void doOpenUser(UserRequest ureq, McpSessionRow row) {
		String businessPath = "[UserAdminSite:0][usearch:0][table:0][Identity:" + row.getIdentityKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doConfirmRevoke(UserRequest ureq, McpSessionRow row) {
		if(guardModalController(confirmRevokeCtrl)) return;

		McpToken token = row.getToken();
		String message = translate("session.revoke.confirm", StringHelper.escapeHtml(token.label()),
				StringHelper.escapeHtml(token.tokenId()), StringHelper.escapeHtml(row.getDisplayName()));
		removeAsListenerAndDispose(confirmRevokeCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRevokeCtrl = new ConfirmationController(ureq, getWindowControl(), message, null,
				translate("session.revoke"), ButtonType.danger);
		confirmRevokeCtrl.setUserObject(token);
		listenTo(confirmRevokeCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRevokeCtrl.getInitialComponent(),
				true, translate("token.revoke.title"));
		listenTo(cmc);
		cmc.activate();
	}

	/**
	 * Deletes the token row. The token service drops the entry of the
	 * identity cache of this node; other nodes refuse the token at the next
	 * re-validation, at the latest after one minute.
	 */
	private void doRevoke(McpToken token) {
		if(tokenService.revokeToken(token.tokenId(), null)) {
			showInfo("token.revoked", StringHelper.escapeHtml(token.label()));
		} else {
			showWarning("token.revoke.notfound");
		}
		loadModel();
	}
}
