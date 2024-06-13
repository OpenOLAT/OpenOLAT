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
package org.olat.modules.openbadges.ui;

import java.util.List;

import org.olat.admin.user.tools.UserToolsModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.modules.openbadges.BadgeOrganization;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.OpenBadgesModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesAdminConfigurationController extends FormBasicController {
	private static final String CMD_TOOLS = "tools";

	private final SelectionValues enabledKV;
	private MultipleSelectionElement enabledEl;
	private FormLink addLinkedInOrganizationButton;
	private LinkedInOrganizationTableModel tableModel;
	private FlexiTableElement tableEl;
	private CloseableModalController modalCtrl;
	private LinkedInOrganizationEditController editController;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	@Autowired
	private OpenBadgesModule openBadgesModule;

	@Autowired
	private UserToolsModule userToolsModule;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	protected OpenBadgesAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		enabledKV = new SelectionValues();
		enabledKV.add(SelectionValues.entry("on", translate("on")));
		initForm(ureq);
		updateUI();
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_admin/administration/e-Assessment_openBadges/");
		setFormTitle("openBadges.configuration");
		setFormInfo("admin.info");
		enabledEl = uifactory.addCheckboxesHorizontal("enabled", "admin.menu.openbadges.title",
				formLayout, enabledKV.keys(), enabledKV.values());
		enabledEl.select(enabledKV.keys()[0], openBadgesModule.isEnabled());
		enabledEl.addActionListener(FormEvent.ONCHANGE);

		initTable(formLayout);

		addLinkedInOrganizationButton = uifactory.addFormLink("add.linkedin.organization", formLayout,
				Link.BUTTON);
	}

	private void initTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LinkedInOrganizationTableModel.Columns.organizationId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LinkedInOrganizationTableModel.Columns.organizationName));

		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(
				LinkedInOrganizationTableModel.Columns.tools.i18nHeaderKey(),
				LinkedInOrganizationTableModel.Columns.tools.ordinal()
		);
		columnsModel.addFlexiColumnModel(toolsColumn);

		tableModel = new LinkedInOrganizationTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "linkedInOrganizations", tableModel,
				10, false, getTranslator(), formLayout);
		tableEl.setLabel("linkedin.organizations", null);
	}

	private void loadModel() {
		List<LinkedInOrganizationRow> rows = openBadgesManager.loadLinkedInOrganizations().stream().map(this::mapToRow)
				.toList();
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private LinkedInOrganizationRow mapToRow(BadgeOrganization badgeOrganization) {
		LinkedInOrganizationRow row = new LinkedInOrganizationRow();
		row.setBadgeOrganization(badgeOrganization);
		row.setOrganizationId(badgeOrganization.getOrganizationKey());
		row.setOrganizationName(badgeOrganization.getOrganizationValue());
		addToolLink(row, badgeOrganization);
		return row;
	}

	private void addToolLink(LinkedInOrganizationRow row, BadgeOrganization badgeOrganization) {
		String toolId = "tool_" + badgeOrganization.getKey();
		FormLink toolLink = (FormLink) flc.getFormComponent(toolId);
		if (toolLink == null) {
			toolLink = uifactory.addFormLink(toolId, CMD_TOOLS, "", tableEl,
					Link.LINK | Link.NONTRANSLATED);
			toolLink.setTranslator(getTranslator());
			toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolLink.setTitle(translate("table.header.actions"));
		}
		toolLink.setUserObject(row);
		row.setToolLink(toolLink);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (enabledEl == source) {
			doSetEnabled();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (addLinkedInOrganizationButton == source) {
			doAdd(ureq);
		} else if (source instanceof FormLink link) {
			if (CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof LinkedInOrganizationRow row) {
				doOpenTools(ureq, link, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (modalCtrl == source) {
			cleanUp();
		} else if (editController == source) {
			if (event == Event.DONE_EVENT) {
				loadModel();
			}
			modalCtrl.deactivate();
			cleanUp();
		} else if (toolsCtrl == source) {
			if (calloutCtrl != null) {
				calloutCtrl.deactivate();
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void doAdd(UserRequest ureq) {
		if (guardModalController(editController)) {
			return;
		}

		editController = new LinkedInOrganizationEditController(ureq, getWindowControl(), null);
		listenTo(editController);

		modalCtrl = new CloseableModalController(getWindowControl(), translate("close"),
				editController.getInitialComponent(), true, translate("add.linkedin.organization"));
		modalCtrl.activate();
		listenTo(modalCtrl);
	}

	private void doOpenTools(UserRequest ureq, FormLink link, LinkedInOrganizationRow row) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	private void doSetEnabled() {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		openBadgesModule.setEnabled(enabled);
		if (enabled) {
			String availableTools = userToolsModule.getAvailableUserTools();
			if (!"none".equals(availableTools) && StringHelper.containsNonWhitespace(availableTools)
					&& !availableTools.contains(BadgesUserToolExtension.BADGES_USER_TOOL_ID)) {
				availableTools += "," + BadgesUserToolExtension.BADGES_USER_TOOL_ID;
				userToolsModule.setAvailableUserTools(availableTools);
			}
		}

		tableEl.setVisible(openBadgesManager.isEnabled());
		addLinkedInOrganizationButton.setVisible(openBadgesManager.isEnabled());
	}

	private void doEdit(UserRequest ureq, LinkedInOrganizationRow row) {
		if (guardModalController(editController)) {
			return;
		}

		BadgeOrganization badgeOrganization = row.getBadgeOrganization();
		editController = new LinkedInOrganizationEditController(ureq, getWindowControl(), badgeOrganization);
		listenTo(editController);

		String title = translate("edit.linkedin.organization");
		modalCtrl = new CloseableModalController(getWindowControl(), title, editController.getInitialComponent(),
				true, title);
		modalCtrl.activate();
		listenTo(modalCtrl);
	}

	private void doDelete(UserRequest ureq, LinkedInOrganizationRow row) {
		if (guardModalController(editController)) {
			return;
		}

		BadgeOrganization badgeOrganization = row.getBadgeOrganization();
		openBadgesManager.deleteBadgeOrganization(badgeOrganization);
	}

	private void updateUI() {
		enabledEl.select(enabledKV.keys()[0], openBadgesModule.isEnabled());

		tableEl.setVisible(openBadgesManager.isEnabled());
		addLinkedInOrganizationButton.setVisible(openBadgesManager.isEnabled());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void cleanUp() {
		removeAsListenerAndDispose(editController);
		removeAsListenerAndDispose(modalCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		editController = null;
		modalCtrl = null;
		toolsCtrl = null;
		calloutCtrl = null;
	}

	private class ToolsController extends BasicController {
		private final Link editLink, deleteLink;
		private final LinkedInOrganizationRow row;

		public ToolsController(UserRequest ureq, WindowControl wControl, LinkedInOrganizationRow row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("tools");

			editLink = LinkFactory.createLink("edit", "edit", getTranslator(), mainVC, this, Link.LINK);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			mainVC.put("tool.edit", editLink);

			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			mainVC.put("tool.delete", deleteLink);

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if (editLink == source) {
				doEdit(ureq, row);
			} else if (deleteLink == source) {
				doDelete(ureq, row);
			}
		}
	}
}
