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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileType;
import org.olat.mcp.McpService;
import org.olat.mcp.ui.McpProfileTableModel.ProfileCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The profiles tab of the MCP administration: the list of the profiles with
 * create, edit, delete and the members of the user profiles.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpProfileListController extends FormBasicController {

	private static final String CMD_EDIT = "edit";
	private static final String CMD_MEMBERS = "members";
	private static final String CMD_DELETE = "delete";

	private FormLink createButton;
	private FormLink accessCheckButton;
	private FlexiTableElement tableEl;
	private McpProfileTableModel tableModel;

	private CloseableModalController cmc;
	private McpProfileEditController editCtrl;
	private McpProfileMembersController membersCtrl;
	private McpAccessCheckController accessCheckCtrl;
	private ConfirmationController confirmDeleteCtrl;

	@Autowired
	private McpService mcpService;

	public McpProfileListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "profiles");
		initForm(ureq);
		reloadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		createButton = uifactory.addFormLink("profile.create", formLayout, Link.BUTTON);
		createButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		createButton.setElementCssClass("o_sel_mcp_profile_create");
		accessCheckButton = uifactory.addFormLink("access.check", formLayout, Link.BUTTON);
		accessCheckButton.setIconLeftCSS("o_icon o_icon-fw o_icon_search");
		accessCheckButton.setElementCssClass("o_sel_mcp_access_check");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProfileCols.name, CMD_EDIT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProfileCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProfileCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProfileCols.organisations));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProfileCols.enabled, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProfileCols.tools));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProfileCols.members));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProfileCols.rateLimit));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProfileCols.manageMembers,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("profile.members.manage"), CMD_MEMBERS), null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), CMD_EDIT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), CMD_DELETE));

		tableModel = new McpProfileTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_mcp_profiles_table");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withIconCss("o_icon_tools")
				.withMessageI18nKey("profile.empty")
				.build());

		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(ProfileCols.name.sortKey(), true));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "mcp-admin-profiles-v2");
	}

	/**
	 * Loads the profiles and the number of members of the user profiles.
	 */
	public void reloadModel() {
		List<McpProfile> profiles = mcpService.getProfiles();
		Map<Long,List<Organisation>> organisationsByProfileKey = mcpService.getOrganisationsByProfileKey();
		List<McpProfileRow> rows = new ArrayList<>(profiles.size());
		for(McpProfile profile:profiles) {
			Integer numOfMembers = null;
			if(profile.getType() == McpProfileType.user) {
				numOfMembers = Integer.valueOf(mcpService.countGrantedIdentities(profile));
			}
			String type = profile.getType() == null ? null : translate("profile.type." + profile.getType().name());
			String organisations = McpUIHelper.getOrganisationNames(organisationsByProfileKey.get(profile.getKey()));
			rows.add(new McpProfileRow(profile, type, numOfMembers, organisations));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == createButton) {
			doEdit(ureq, null);
		} else if(source == accessCheckButton) {
			doOpenAccessCheck(ureq);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent se) {
				McpProfileRow row = tableModel.getObject(se.getIndex());
				if(row != null) {
					if(CMD_EDIT.equals(se.getCommand())) {
						doEdit(ureq, row);
					} else if(CMD_MEMBERS.equals(se.getCommand())) {
						doOpenMembers(ureq, row);
					} else if(CMD_DELETE.equals(se.getCommand())) {
						doConfirmDelete(ureq, row);
					}
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reloadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == accessCheckCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if(source == membersCtrl) {
			reloadModel();
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmDeleteCtrl) {
			if(event == Event.DONE_EVENT && confirmDeleteCtrl.getUserObject() instanceof McpProfileRow row) {
				doDelete(row);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			if(membersCtrl != null) {
				reloadModel();
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(accessCheckCtrl);
		removeAsListenerAndDispose(membersCtrl);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		accessCheckCtrl = null;
		membersCtrl = null;
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doEdit(UserRequest ureq, McpProfileRow row) {
		if(guardModalController(editCtrl)) return;

		McpProfile profile = null;
		if(row != null) {
			profile = mcpService.getProfile(row);
			if(profile == null) {
				showWarning("profile.deleted");
				reloadModel();
				return;
			}
		}

		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = new McpProfileEditController(ureq, getWindowControl(), profile);
		listenTo(editCtrl);

		String title = profile == null
				? translate("profile.create")
				: translate("profile.edit.title", StringHelper.escapeHtml(profile.getName()));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenAccessCheck(UserRequest ureq) {
		if(guardModalController(accessCheckCtrl)) return;

		removeAsListenerAndDispose(accessCheckCtrl);
		removeAsListenerAndDispose(cmc);
		accessCheckCtrl = new McpAccessCheckController(ureq, getWindowControl());
		listenTo(accessCheckCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), accessCheckCtrl.getInitialComponent(),
				true, translate("access.check.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenMembers(UserRequest ureq, McpProfileRow row) {
		if(guardModalController(membersCtrl)) return;

		McpProfile profile = mcpService.getProfile(row);
		if(profile == null) {
			showWarning("profile.deleted");
			reloadModel();
			return;
		}

		removeAsListenerAndDispose(membersCtrl);
		removeAsListenerAndDispose(cmc);
		membersCtrl = new McpProfileMembersController(ureq, getWindowControl(), profile);
		listenTo(membersCtrl);

		String title = translate("profile.members.title", StringHelper.escapeHtml(profile.getName()));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), membersCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmDelete(UserRequest ureq, McpProfileRow row) {
		if(guardModalController(confirmDeleteCtrl)) return;

		String name = StringHelper.escapeHtml(row.getName());
		String message;
		if(row.isUserType() && row.getNumOfMembers() != null && row.getNumOfMembers().intValue() > 0) {
			message = translate("profile.delete.confirm.members", name, row.getNumOfMembers().toString());
		} else {
			message = translate("profile.delete.confirm", name);
		}
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = new ConfirmationController(ureq, getWindowControl(), message,
				translate("profile.delete.confirm.check"), translate("profile.delete.button"), ButtonType.danger);
		confirmDeleteCtrl.setUserObject(row);
		listenTo(confirmDeleteCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(),
				true, translate("profile.delete.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doDelete(McpProfileRow row) {
		mcpService.deleteProfile(row);
		reloadModel();
		showInfo("profile.deleted.info", StringHelper.escapeHtml(row.getName()));
	}
}
