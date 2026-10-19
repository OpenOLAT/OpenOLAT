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

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpService;
import org.olat.mcp.ui.McpProfileMemberTableModel.MemberCols;
import org.olat.mcp.ui.McpProfileMemberTableModel.MemberDataSource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The members of a user profile: the paginated list of the explicit grants,
 * add members with the user search, remove one member after a confirmation.
 * Fires {@link Event#DONE_EVENT} when the user closes the list.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpProfileMembersController extends FormBasicController {

	private static final String CMD_REMOVE = "remove";

	private FormLink addButton;
	private FormLink closeButton;
	private FlexiTableElement tableEl;
	private MemberDataSource dataSource;
	private McpProfileMemberTableModel tableModel;

	private CloseableModalController cmc;
	private UserSearchFlexiController userSearchCtrl;
	private ConfirmationController confirmRemoveCtrl;

	private final McpProfile profile;

	@Autowired
	private UserManager userManager;
	@Autowired
	private McpService mcpService;

	public McpProfileMembersController(UserRequest ureq, WindowControl wControl, McpProfile profile) {
		super(ureq, wControl, "members");
		this.profile = profile;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("profileDisabled", Boolean.valueOf(!profile.isEnabled()));
		}

		addButton = uifactory.addFormLink("members.add", formLayout, Link.BUTTON);
		addButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
		addButton.setElementCssClass("o_sel_mcp_members_add");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.username));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.email));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("remove", translate("remove"), CMD_REMOVE));

		dataSource = new MemberDataSource(profile, mcpService);
		tableModel = new McpProfileMemberTableModel(dataSource, columnsModel, userManager);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_mcp_members_table");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withIconCss("o_icon_user")
				.withMessageI18nKey("members.empty")
				.build());

		closeButton = uifactory.addFormLink("close", formLayout, Link.BUTTON);
	}

	private void loadModel() {
		dataSource.reset();
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addButton) {
			doOpenUserSearch(ureq);
		} else if(source == closeButton) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent se && CMD_REMOVE.equals(se.getCommand())) {
				Identity member = tableModel.getObject(se.getIndex());
				if(member != null) {
					doConfirmRemove(ureq, member);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == userSearchCtrl) {
			if(event instanceof SingleIdentityChosenEvent sice) {
				doAddMembers(List.of(sice.getChosenIdentity()));
			} else if(event instanceof MultiIdentityChosenEvent mice) {
				doAddMembers(mice.getChosenIdentities());
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmRemoveCtrl) {
			if(event == Event.DONE_EVENT && confirmRemoveCtrl.getUserObject() instanceof Identity member) {
				doRemove(member);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(confirmRemoveCtrl);
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRemoveCtrl = null;
		userSearchCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doOpenUserSearch(UserRequest ureq) {
		if(guardModalController(userSearchCtrl)) return;

		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(cmc);
		userSearchCtrl = new UserSearchFlexiController(ureq, getWindowControl(), (GroupRoles)null, true);
		listenTo(userSearchCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(),
				true, translate("members.add.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddMembers(List<Identity> identities) {
		if(identities == null || identities.isEmpty()) {
			return;
		}
		for(Identity identity:identities) {
			mcpService.addGrant(profile, identity);
		}
		loadModel();
		showInfo("members.added", Integer.toString(identities.size()));
	}

	private void doConfirmRemove(UserRequest ureq, Identity member) {
		if(guardModalController(confirmRemoveCtrl)) return;

		String name = StringHelper.escapeHtml(userManager.getUserDisplayName(member.getUser()));
		String message = translate("members.remove.confirm", name, StringHelper.escapeHtml(profile.getName()));
		removeAsListenerAndDispose(confirmRemoveCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRemoveCtrl = new ConfirmationController(ureq, getWindowControl(), message, null,
				translate("members.remove.button"), ButtonType.danger);
		confirmRemoveCtrl.setUserObject(member);
		listenTo(confirmRemoveCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRemoveCtrl.getInitialComponent(),
				true, translate("members.remove.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doRemove(Identity member) {
		mcpService.removeGrant(profile, member);
		loadModel();
		showInfo("members.removed", StringHelper.escapeHtml(userManager.getUserDisplayName(member.getUser())));
	}
}
