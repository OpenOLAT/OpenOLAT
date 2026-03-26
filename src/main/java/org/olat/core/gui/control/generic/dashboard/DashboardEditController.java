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
package org.olat.core.gui.control.generic.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownUIFactory;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.DashboardController.Widget;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Edit controller for dashboard widget configuration. Created by
 * {@link DashboardController#doEdit(UserRequest)} when the user clicks
 * "Edit overview". Replaces the dashboard view in the stacked panel
 * and fires an event when the user is done.
 * <p>
 * The edit view is split into two sections:
 * <ul>
 *   <li><b>Enabled widgets</b> — shown in a SortableJS-enabled container
 *       that supports drag &amp; drop reordering. Each widget has a
 *       remove button.</li>
 *   <li><b>Disabled widgets</b> — shown below with "Add" buttons to
 *       move them back into the enabled section.</li>
 * </ul>
 * <p>
 * Actions:
 * <ul>
 *   <li><b>Save</b> — persists the current widget order as personal
 *       preferences in {@code GuiPreferences}.</li>
 *   <li><b>Cancel</b> — discards all changes.</li>
 *   <li><b>Reset</b> — deletes the personal preferences, reverting to
 *       the system default or all-widgets fallback.</li>
 * </ul>
 * <p>
 * System administrators see an additional ellipsis menu with:
 * <ul>
 *   <li><b>Save as system default</b> — stores the current widget
 *       configuration as the system-wide default via
 *       {@link DashboardSystemDefaultsManager}.</li>
 *   <li><b>Reset system default</b> — deletes the system-wide default.</li>
 * </ul>
 * Changes to the system default are audit-logged at INFO level with
 * the identity key of the administrator.
 * <p>
 * Fires {@link Event#CHANGED_EVENT} after save, reset, or system default
 * changes. Fires {@link Event#CANCELLED_EVENT} on cancel.
 *
 * Initial date: Mar 07, 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class DashboardEditController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(DashboardEditController.class);

	private static final String CMD_ADD_WIDGET = "add-widget";
	private static final String CMD_REMOVE_WIDGET = "remove-widget";
	private static final String CMD_MOVE_UP = "move-up";
	private static final String CMD_MOVE_DOWN = "move-down";

	private static final String CMD_REMOVE_PREFIX = "remove_";
	private static final String CMD_ADD_PREFIX = "add_";
	private static final String CMD_MOVE_UP_PREFIX = "up_";
	private static final String CMD_MOVE_DOWN_PREFIX = "down_";

	private FormLayoutContainer editCont;
	private FormLink saveSystemDefaultLink;
	private FormLink resetSystemDefaultLink;

	private final String dashboardId;

	@Autowired
	private DashboardSystemDefaultsManager dashboardSystemDefaultsManager;

	/**
	 * @param ureq            the user request
	 * @param wControl        the window control
	 * @param dashboardId     unique dashboard identifier used as storage key
	 * @param enabledWidgets  currently enabled widgets in display order
	 * @param disabledWidgets currently disabled (hidden) widgets
	 */
	DashboardEditController(UserRequest ureq, WindowControl wControl,
			String dashboardId, List<Widget> enabledWidgets, List<Widget> disabledWidgets) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(DashboardController.class, getLocale()));
		this.dashboardId = dashboardId;
		initForm(ureq);

		editCont.contextPut("formId", mainForm.getDispatchFieldId());
		editCont.contextPut("formName", mainForm.getFormName());
		editCont.contextPut("editContId", editCont.getFormDispatchId());
		editCont.contextPut("eventFieldId", mainForm.getEventFieldId());
		editCont.contextPut("enabledWidgets", new ArrayList<>(enabledWidgets));
		editCont.contextPut("disabledWidgets", new ArrayList<>(disabledWidgets));

		for (int i = 0; i < enabledWidgets.size(); i++) {
			addRemoveLink(enabledWidgets.get(i));
			addMoveLinks(enabledWidgets.get(i), i, enabledWidgets.size());
		}
		for (Widget widget : disabledWidgets) {
			addAddLink(widget);
		}
	}

	private void addRemoveLink(Widget widget) {
		String name = CMD_REMOVE_PREFIX + widget.getName();
		FormLink link = uifactory.addFormLink(name, CMD_REMOVE_WIDGET, "", null, editCont, Link.BUTTON + Link.NONTRANSLATED);
		link.setIconLeftCSS("o_icon o_icon_trash");
		link.setTitle(translate("dashboard.remove"));
		link.setElementCssClass("o_dashboard_edit_remove");
		link.setUserObject(widget);
	}

	private void addAddLink(Widget widget) {
		String name = CMD_ADD_PREFIX + widget.getName();
		FormLink link = uifactory.addFormLink(name, CMD_ADD_WIDGET, "dashboard.add", null, editCont, Link.BUTTON);
		link.setIconLeftCSS("o_icon o_icon_add");
		link.setGhost(true);
		link.setUserObject(widget);
	}

	private void addMoveLinks(Widget widget, int index, int total) {
		String upName = CMD_MOVE_UP_PREFIX + widget.getName();
		FormLink upLink = uifactory.addFormLink(upName, CMD_MOVE_UP, "", null, editCont, Link.BUTTON_XSMALL + Link.NONTRANSLATED);
		upLink.setIconLeftCSS("o_icon o_icon_move_up");
		upLink.setTitle(translate("dashboard.move.up"));
		upLink.setEnabled(index > 0);
		upLink.setUserObject(widget);

		String downName = CMD_MOVE_DOWN_PREFIX + widget.getName();
		FormLink downLink = uifactory.addFormLink(downName, CMD_MOVE_DOWN, "", null, editCont, Link.BUTTON_XSMALL + Link.NONTRANSLATED);
		downLink.setIconLeftCSS("o_icon o_icon_move_down");
		downLink.setTitle(translate("dashboard.move.down"));
		downLink.setEnabled(index < total - 1);
		downLink.setUserObject(widget);
	}

	private void recreateMoveLinks(List<Widget> enabled) {
		for (Widget w : enabled) {
			editCont.remove(CMD_MOVE_UP_PREFIX + w.getName());
			editCont.remove(CMD_MOVE_DOWN_PREFIX + w.getName());
		}
		for (int i = 0; i < enabled.size(); i++) {
			addMoveLinks(enabled.get(i), i, enabled.size());
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String page = Util.getPackageVelocityRoot(DashboardEditController.class) + "/dashboard_edit.html";
		editCont = FormLayoutContainer.createCustomFormLayout("editCont", getTranslator(), page);
		editCont.setRootForm(mainForm);
		formLayout.add(editCont);

		String sortableJsFile = Settings.isDebuging() ? "js/sortable/Sortable.js" : "js/sortable/Sortable.min.js";
		JSAndCSSComponent sortableJs = new JSAndCSSComponent("sortable",
				new String[] { sortableJsFile }, null);
		editCont.put("sortable", sortableJs);

		uifactory.addFormSubmitButton("dashboard.save", editCont);

		uifactory.addFormCancelButton("dashboard.cancel", editCont, ureq, getWindowControl());

		uifactory.addFormResetButton("dashboard.reset", "dashboard.reset", editCont);

		if (ureq.getUserSession().getRoles().isSystemAdmin()) {
			Dropdown adminDropdown = DropdownUIFactory.createMoreDropdown("adminMenu", getTranslator());
			adminDropdown.setButton(true);
			adminDropdown.setEmbbeded(true);
			editCont.put("adminMenu", adminDropdown);

			saveSystemDefaultLink = uifactory.addFormLink("dashboard.system.default.save",
					"dashboard.system.default.save", "dashboard.system.default.save", null, editCont, Link.LINK);
			saveSystemDefaultLink.setIconLeftCSS("o_icon o_icon_save");
			adminDropdown.addComponent(saveSystemDefaultLink.getComponent());

			resetSystemDefaultLink = uifactory.addFormLink("dashboard.system.default.reset",
					"dashboard.system.default.reset", "dashboard.system.default.reset", null, editCont, Link.LINK);
			resetSystemDefaultLink.setIconLeftCSS("o_icon o_icon_delete_item");
			adminDropdown.addComponent(resetSystemDefaultLink.getComponent());
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == saveSystemDefaultLink) {
			doSaveSystemDefault(ureq);
		} else if (source == resetSystemDefaultLink) {
			doResetSystemDefault(ureq);
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if (CMD_REMOVE_WIDGET.equals(cmd)) {
				doRemoveWidget((Widget) link.getUserObject());
			} else if (CMD_ADD_WIDGET.equals(cmd)) {
				doAddWidget((Widget) link.getUserObject());
			} else if (CMD_MOVE_UP.equals(cmd)) {
				doMoveWidget((Widget) link.getUserObject(), -1);
			} else if (CMD_MOVE_DOWN.equals(cmd)) {
				doMoveWidget((Widget) link.getUserObject(), 1);
			}
		} else if (source == editCont) {
			String cmd = event.getCommand();
			if ("ONCLICK".equals(cmd)) {
				doDropWidget(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formResetted(UserRequest ureq) {
		doReset(ureq);
	}

	private void doSave(UserRequest ureq) {
		DashboardPrefs prefs = buildCurrentPrefs();
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		guiPrefs.putAndSave(DashboardController.class, dashboardId, prefs);
		log.debug("Dashboard '{}' personal preferences saved: enabled={}, disabled={}", dashboardId, prefs.getEnabledWidgets(), prefs.getDisabledWidgets());
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doReset(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		guiPrefs.putAndSave(DashboardController.class, dashboardId, null);
		log.debug("Dashboard '{}' personal preferences reset", dashboardId);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doSaveSystemDefault(UserRequest ureq) {
		DashboardPrefs prefs = buildCurrentPrefs();
		dashboardSystemDefaultsManager.saveSystemDefault(dashboardId, prefs);
		log.info("Dashboard '{}' system default saved by identity::{}: enabled={}, disabled={}",
				dashboardId, getIdentity().getKey(), prefs.getEnabledWidgets(), prefs.getDisabledWidgets());
		showInfo("dashboard.system.default.saved");
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doResetSystemDefault(UserRequest ureq) {
		dashboardSystemDefaultsManager.deleteSystemDefault(dashboardId);
		log.info("Dashboard '{}' system default reset by identity::{}",
				dashboardId, getIdentity().getKey());
		showInfo("dashboard.system.default.deleted");
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private DashboardPrefs buildCurrentPrefs() {
		@SuppressWarnings("unchecked")
		List<Widget> enabled = (List<Widget>) editCont.contextGet("enabledWidgets");
		@SuppressWarnings("unchecked")
		List<Widget> disabled = (List<Widget>) editCont.contextGet("disabledWidgets");
		return new DashboardPrefs(toNameList(enabled), toNameList(disabled));
	}

	private List<String> toNameList(List<Widget> widgets) {
		if (widgets == null) return List.of();
		List<String> names = new ArrayList<>(widgets.size());
		for (Widget w : widgets) {
			names.add(w.getName());
		}
		return names;
	}

	/**
	 * Handle SortableJS drop event: reorder the dragged widget before the sibling.
	 * Parameter {@code "drop-data"} contains {@code "draggedName:siblingName"}.
	 */
	private void doDropWidget(UserRequest ureq) {
		String draggedName = ureq.getParameter("draggedName");
		if (!StringHelper.containsNonWhitespace(draggedName)) return;
		String siblingName = ureq.getParameter("siblingName");
		// siblingName is empty when dragged at last position

		@SuppressWarnings("unchecked")
		List<Widget> enabled = (List<Widget>) editCont.contextGet("enabledWidgets");
		if (enabled == null) return;

		Widget dragged = null;
		for (Widget w : enabled) {
			if (w.getName().equals(draggedName)) {
				dragged = w;
				break;
			}
		}
		if (dragged == null) return;

		enabled.remove(dragged);
		if (StringHelper.containsNonWhitespace(siblingName)) {
			for (int i = 0; i < enabled.size(); i++) {
				if (enabled.get(i).getName().equals(siblingName)) {
					enabled.add(i, dragged);
					break;
				}
			}
		} else {
			enabled.add(dragged);
		}
		recreateMoveLinks(enabled);
		log.debug("Dashboard '{}' widget '{}' reordered (before: '{}')", dashboardId, draggedName, siblingName);
		editCont.setDirty(true);
	}

	private void doMoveWidget(Widget widget, int direction) {
		@SuppressWarnings("unchecked")
		List<Widget> enabled = (List<Widget>) editCont.contextGet("enabledWidgets");
		if (enabled == null) return;

		int index = enabled.indexOf(widget);
		int newIndex = index + direction;
		if (index < 0 || newIndex < 0 || newIndex >= enabled.size()) return;

		enabled.remove(index);
		enabled.add(newIndex, widget);
		recreateMoveLinks(enabled);

		String announcement = translate("dashboard.moved", widget.getTitle(),
				String.valueOf(newIndex + 1), String.valueOf(enabled.size()));
		editCont.contextPut("announcement", announcement);

		log.debug("Dashboard '{}' widget '{}' moved to position {}", dashboardId, widget.getName(), newIndex);
		editCont.setDirty(true);
	}

	private void doAddWidget(Widget widget) {
		@SuppressWarnings("unchecked")
		List<Widget> enabled = (List<Widget>) editCont.contextGet("enabledWidgets");
		@SuppressWarnings("unchecked")
		List<Widget> disabled = (List<Widget>) editCont.contextGet("disabledWidgets");
		if (enabled == null || disabled == null) return;

		disabled.remove(widget);
		enabled.add(widget);

		editCont.remove(CMD_ADD_PREFIX + widget.getName());
		addRemoveLink(widget);
		recreateMoveLinks(enabled);

		log.debug("Dashboard '{}' widget '{}' added", dashboardId, widget.getName());
		editCont.setDirty(true);
	}

	private void doRemoveWidget(Widget widget) {
		@SuppressWarnings("unchecked")
		List<Widget> enabled = (List<Widget>) editCont.contextGet("enabledWidgets");
		@SuppressWarnings("unchecked")
		List<Widget> disabled = (List<Widget>) editCont.contextGet("disabledWidgets");
		if (enabled == null || disabled == null) return;

		enabled.remove(widget);
		disabled.add(widget);

		editCont.remove(CMD_REMOVE_PREFIX + widget.getName());
		editCont.remove(CMD_MOVE_UP_PREFIX + widget.getName());
		editCont.remove(CMD_MOVE_DOWN_PREFIX + widget.getName());
		addAddLink(widget);
		recreateMoveLinks(enabled);

		log.debug("Dashboard '{}' widget '{}' removed", dashboardId, widget.getName());
		editCont.setDirty(true);
	}
}
