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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileChangedEvent;
import org.olat.mcp.McpProfileType;
import org.olat.mcp.McpService;
import org.olat.mcp.McpToken;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.ui.McpUserProfileTableModel.UserProfileCols;
import org.olat.mcp.ui.McpUserProfileTableModel.UserProfileRow;
import org.olat.mcp.ui.McpUserTokenTableModel.UserTokenCols;
import org.olat.restapi.audit.ApiAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The tab MCP of the user administration: one table with the
 * matching system profiles (role profiles) and the user profiles the manager
 * may assign, each with a read-only preview, the merged tools of the user as
 * a tree and the tokens of the user with their usage in the audit log.
 * The caller shows the tab only if the manager may manage the MCP access
 * of the user ({@link McpService#canManageMembers}); every change checks
 * this right again. The data reloads on every activation of the tab and on
 * {@link McpProfileChangedEvent}.
 *
 * Initial date: 12 Oct 2026<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public class McpUserAdminController extends FormBasicController implements GenericEventListener {

	private static final Logger log = Tracing.createLoggerFor(McpUserAdminController.class);

	private static final String CMD_REVOKE = "revoke";
	private static final String CMD_PREVIEW = "preview";

	private FlexiTableElement profilesTableEl;
	private McpUserProfileTableModel profilesModel;
	private McpToolTreeController toolsCtrl;
	private FormLink generateButton;
	private FlexiTableElement tokensTableEl;
	private McpUserTokenTableModel tokensModel;

	private CloseableModalController cmc;
	private McpTokenGenerateController generateCtrl;
	private McpTokenCreatedController createdCtrl;
	private ConfirmationController confirmRevokeCtrl;
	private McpProfilePreviewController previewCtrl;

	private int counter = 0;
	private final Identity editedIdentity;
	private final Roles managerRoles;

	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpService mcpService;
	@Autowired
	private McpTokenService tokenService;
	@Autowired
	private ApiAuditLogService auditLogService;
	@Autowired
	private CoordinatorManager coordinatorManager;

	public McpUserAdminController(UserRequest ureq, WindowControl wControl, Identity editedIdentity) {
		super(ureq, wControl, "user_admin");
		this.editedIdentity = editedIdentity;
		managerRoles = ureq.getUserSession().getRoles();
		initForm(ureq);
		loadModel();
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, getIdentity(), McpProfileChangedEvent.ORES);
	}

	@Override
	protected void doDispose() {
		coordinatorManager.getCoordinator().getEventBus().deregisterFor(this, McpProfileChangedEvent.ORES);
		super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initProfilesForm(formLayout);

		toolsCtrl = new McpToolTreeController(ureq, getWindowControl(), mainForm, Set.of());
		listenTo(toolsCtrl);
		formLayout.add("tools", toolsCtrl.getInitialFormItem());

		initTokensForm(formLayout, ureq);
	}

	private void initProfilesForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserProfileCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserProfileCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserProfileCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserProfileCols.organisations));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserProfileCols.enabled, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserProfileCols.tools));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserProfileCols.member));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("user.admin.preview", translate("user.admin.preview"), CMD_PREVIEW));

		profilesModel = new McpUserProfileTableModel(columnsModel, getLocale());
		profilesTableEl = uifactory.addTableElement(getWindowControl(), "profiles", profilesModel, 0, false, getTranslator(), formLayout);
		profilesTableEl.setElementCssClass("o_sel_mcp_user_profiles");
		profilesTableEl.setCustomizeColumns(false);
		profilesTableEl.setNumOfRowsEnabled(false);
		profilesTableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withIconCss("o_icon_tools")
				.withMessageI18nKey("user.admin.profiles.empty")
				.build());
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(UserProfileCols.type.sortKey(), true));
		profilesTableEl.setSortSettings(sortOptions);
	}

	private void initTokensForm(FormItemContainer formLayout, UserRequest ureq) {
		generateButton = uifactory.addFormLink("user.admin.generate", formLayout, Link.BUTTON);
		generateButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		generateButton.setElementCssClass("o_sel_mcp_user_admin_token_generate");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserTokenCols.label));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserTokenCols.tokenId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserTokenCols.creationDate, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserTokenCols.expirationDate,
				new McpTokenExpirationCellRenderer(getLocale(), getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserTokenCols.auditedRequests));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserTokenCols.lastAuditedUse, new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("token.revoke", translate("token.revoke"), CMD_REVOKE));

		tokensModel = new McpUserTokenTableModel(columnsModel);
		tokensTableEl = uifactory.addTableElement(getWindowControl(), "tokens", tokensModel, 25, false, getTranslator(), formLayout);
		tokensTableEl.setElementCssClass("o_sel_mcp_user_admin_tokens");
		tokensTableEl.setCustomizeColumns(false);
		tokensTableEl.setNumOfRowsEnabled(false);
		tokensTableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withIconCss("o_icon_password")
				.withMessageI18nKey("user.admin.tokens.empty")
				.build());
	}

	/**
	 * Loads the profiles, the merged tools and the tokens of the user.
	 */
	public void loadModel() {
		loadProfiles();
		loadTools();
		loadTokens();
	}

	private void loadProfiles() {
		Map<Long,List<Organisation>> organisationsByProfileKey = mcpService.getOrganisationsByProfileKey();

		// drop the switches of the previous load
		for(UserProfileRow oldRow:profilesModel.getObjects()) {
			if(oldRow.getMemberToggle() != null) {
				flc.remove(oldRow.getMemberToggle());
			}
		}

		List<UserProfileRow> rows = new ArrayList<>();
		String systemType = translate("profile.type.system");
		for(McpProfile profile:mcpService.getProfilesOf(editedIdentity)) {
			if(profile.getType() == McpProfileType.system) {
				rows.add(new UserProfileRow(profile, systemType,
						McpUIHelper.getOrganisationNames(organisationsByProfileKey.get(profile.getKey()))));
			}
		}

		Set<Long> memberOf = new HashSet<>();
		for(McpProfile profile:mcpService.getUserProfilesOf(editedIdentity)) {
			memberOf.add(profile.getKey());
		}
		String userType = translate("profile.type.user");
		for(McpProfile profile:mcpService.getManageableUserProfiles(managerRoles, editedIdentity)) {
			UserProfileRow row = new UserProfileRow(profile, userType,
					McpUIHelper.getOrganisationNames(organisationsByProfileKey.get(profile.getKey())));
			FormToggle toggle = uifactory.addToggleButton("member_" + (++counter), null, translate("on"), translate("off"), flc);
			toggle.toggle(memberOf.contains(profile.getKey()));
			toggle.setAriaLabel(translate("user.admin.member.aria", profile.getName()));
			toggle.setElementCssClass("o_sel_mcp_member_" + profile.getIdentifier());
			toggle.addActionListener(FormEvent.ONCHANGE);
			toggle.setUserObject(row);
			row.setMemberToggle(toggle);
			rows.add(row);
		}

		profilesModel.setObjects(rows);
		profilesModel.sort(profilesTableEl.getOrderBy() == null || profilesTableEl.getOrderBy().length == 0
				? new SortKey(UserProfileCols.type.sortKey(), true) : profilesTableEl.getOrderBy()[0]);
		profilesTableEl.reset(true, true, true);
	}

	private void loadTools() {
		McpGrants grants = mcpService.resolveGrants(editedIdentity);
		toolsCtrl.setTools(grants.tools());
		toolsCtrl.getInitialFormItem().setVisible(!grants.isEmpty());

		flc.contextPut("noAccess", Boolean.valueOf(grants.isEmpty()));
		String rateLimit = grants.requestsPerMinute() == null
				? translate("access.check.rpm.default", Integer.toString(mcpModule.getRateLimitRequestsPerMinute()))
				: translate("access.check.rpm.override", grants.requestsPerMinute().toString());
		flc.contextPut("rateLimit", rateLimit);
		flc.contextPut("profileIdentifiers", String.join(", ", grants.profileIdentifiers()));
	}

	private void loadTokens() {
		List<McpToken> tokens = tokenService.getTokens(editedIdentity);
		tokensModel.setUsages(McpTokenUsage.load(auditLogService, tokens));
		tokensModel.setObjects(tokens);
		tokensTableEl.reset(true, true, true);
	}

	@Override
	public void event(Event event) {
		if(event instanceof McpProfileChangedEvent) {
			loadModel();
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == generateButton) {
			doOpenGenerate(ureq);
		} else if(source == profilesTableEl) {
			if(event instanceof SelectionEvent se && CMD_PREVIEW.equals(se.getCommand())) {
				UserProfileRow row = profilesModel.getObject(se.getIndex());
				if(row != null) {
					doOpenPreview(ureq, row);
				}
			}
		} else if(source == tokensTableEl) {
			if(event instanceof SelectionEvent se && CMD_REVOKE.equals(se.getCommand())) {
				McpToken token = tokensModel.getObject(se.getIndex());
				if(token != null) {
					doConfirmRevoke(ureq, token);
				}
			}
		} else if(source instanceof FormToggle toggle && toggle.getUserObject() instanceof UserProfileRow row) {
			doToggleMember(ureq, row, toggle.isOn());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == generateCtrl) {
			McpTokenCreation creation = generateCtrl.getCreation();
			cmc.deactivate();
			cleanUp();
			loadTokens();
			if(event == Event.DONE_EVENT && creation != null) {
				doShowCreated(ureq, creation);
			}
		} else if(source == createdCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmRevokeCtrl) {
			if(event == Event.DONE_EVENT && confirmRevokeCtrl.getUserObject() instanceof McpToken token) {
				doRevoke(ureq, token);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == previewCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(confirmRevokeCtrl);
		removeAsListenerAndDispose(previewCtrl);
		removeAsListenerAndDispose(generateCtrl);
		removeAsListenerAndDispose(createdCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRevokeCtrl = null;
		previewCtrl = null;
		generateCtrl = null;
		createdCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private boolean canManage(UserRequest ureq) {
		return mcpModule.isEnabled()
				&& mcpService.canManageMembers(ureq.getUserSession().getRoles(), editedIdentity);
	}

	private void doToggleMember(UserRequest ureq, UserProfileRow row, boolean member) {
		McpProfile profile = mcpService.getProfile(row);
		boolean manageable = profile != null && canManage(ureq)
				&& mcpService.getManageableUserProfiles(ureq.getUserSession().getRoles(), editedIdentity).stream()
					.anyMatch(manageableProfile -> manageableProfile.getKey().equals(profile.getKey()));
		if(!manageable) {
			showWarning("user.admin.member.not.allowed");
		} else if(member) {
			mcpService.addGrant(profile, editedIdentity);
			log.info(Tracing.M_AUDIT, "MCP profile {} granted to identity {} by {}", profile.getIdentifier(), editedIdentity.getKey(), getIdentity().getKey());
		} else {
			mcpService.removeGrant(profile, editedIdentity);
			log.info(Tracing.M_AUDIT, "MCP profile {} removed from identity {} by {}", profile.getIdentifier(), editedIdentity.getKey(), getIdentity().getKey());
		}
		loadModel();
	}

	private void doOpenPreview(UserRequest ureq, UserProfileRow row) {
		if(guardModalController(previewCtrl)) return;

		McpProfile profile = mcpService.getProfile(row);
		if(profile == null) {
			showWarning("profile.deleted");
			loadModel();
			return;
		}

		removeAsListenerAndDispose(previewCtrl);
		removeAsListenerAndDispose(cmc);
		previewCtrl = new McpProfilePreviewController(ureq, getWindowControl(), profile, row.getOrganisations());
		listenTo(previewCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), previewCtrl.getInitialComponent(),
				true, translate("user.admin.preview.title", StringHelper.escapeHtml(profile.getName())));
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenGenerate(UserRequest ureq) {
		if(guardModalController(generateCtrl)) return;

		removeAsListenerAndDispose(generateCtrl);
		removeAsListenerAndDispose(cmc);
		generateCtrl = new McpTokenGenerateController(ureq, getWindowControl(), editedIdentity, tokensModel);
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

		// no close icon: the manager leaves the dialog with the button below the token
		cmc = new CloseableModalController(getWindowControl(), translate("close"), createdCtrl.getInitialComponent(),
				true, translate("user.created.title"), false);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmRevoke(UserRequest ureq, McpToken token) {
		if(guardModalController(confirmRevokeCtrl)) return;

		String message = translate("user.admin.revoke.confirm", StringHelper.escapeHtml(token.label()),
				StringHelper.escapeHtml(token.tokenId()));
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

	private void doRevoke(UserRequest ureq, McpToken token) {
		if(!canManage(ureq)) {
			showWarning("user.admin.member.not.allowed");
		} else if(tokenService.revokeToken(token.tokenId(), editedIdentity)) {
			log.info(Tracing.M_AUDIT, "MCP token {} of identity {} revoked by the manager {}", token.tokenId(), editedIdentity.getKey(), getIdentity().getKey());
			showInfo("token.revoked", StringHelper.escapeHtml(token.label()));
		} else {
			showWarning("token.revoke.notfound");
		}
		loadTokens();
	}
}
