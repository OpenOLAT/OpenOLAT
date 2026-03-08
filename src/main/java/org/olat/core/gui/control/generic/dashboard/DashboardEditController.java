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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownUIFactory;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dashboard.DashboardController.Widget;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Edit controller for dashboard widget configuration. Created by
 * {@link DashboardController#doEdit(UserRequest)} when the user clicks
 * "Edit dashboard". Replaces the dashboard view in the stacked panel
 * and fires an event when the user is done.
 * <p>
 * The edit view is split into two sections:
 * <ul>
 *   <li><b>Enabled widgets</b> — shown in a Dragula-enabled container
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
public class DashboardEditController extends BasicController {

	private static final Logger log = Tracing.createLoggerFor(DashboardEditController.class);

	/** JavaScript command sent by Dragula when a widget is dropped at a new position. */
	private static final String CMD_DROP_WIDGET = "drop-widget";
	/** JavaScript command sent when the "Add" button on a disabled widget is clicked. */
	private static final String CMD_ADD_WIDGET = "add-widget";
	/** JavaScript command sent when the remove button on an enabled widget is clicked. */
	private static final String CMD_REMOVE_WIDGET = "remove-widget";

	private final VelocityContainer mainVC;
	private final Link saveLink;
	private final Link cancelLink;
	private final Link resetLink;
	private Link saveSystemDefaultLink;
	private Link resetSystemDefaultLink;

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
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(DashboardController.class, getLocale()));
		this.dashboardId = dashboardId;

		mainVC = createVelocityContainer("dashboard_edit");

		JSAndCSSComponent dragulaJs = new JSAndCSSComponent("dragula",
				new String[] { "js/dragula/dragula.js" }, null);
		mainVC.put("dragula", dragulaJs);

		mainVC.contextPut("enabledWidgets", new ArrayList<>(enabledWidgets));
		mainVC.contextPut("disabledWidgets", new ArrayList<>(disabledWidgets));

		saveLink = LinkFactory.createButton("dashboard.save", mainVC, this);
		saveLink.setPrimary(true);

		cancelLink = LinkFactory.createButton("dashboard.cancel", mainVC, this);

		resetLink = LinkFactory.createButton("dashboard.reset", mainVC, this);
		resetLink.setGhost(true);

		if (ureq.getUserSession().getRoles().isSystemAdmin()) {
			Dropdown adminDropdown = DropdownUIFactory.createMoreDropdown("adminMenu", getTranslator());
			adminDropdown.setButton(true);
			adminDropdown.setEmbbeded(true);
			mainVC.put("adminMenu", adminDropdown);

			saveSystemDefaultLink = LinkFactory.createToolLink("dashboard.system.default.save",
					translate("dashboard.system.default.save"), this, "o_icon o_icon_save");
			adminDropdown.addComponent(saveSystemDefaultLink);

			resetSystemDefaultLink = LinkFactory.createToolLink("dashboard.system.default.reset",
					translate("dashboard.system.default.reset"), this, "o_icon o_icon_delete_item");
			adminDropdown.addComponent(resetSystemDefaultLink);
		}

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == saveLink) {
			doSave(ureq);
		} else if (source == cancelLink) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == resetLink) {
			doReset(ureq);
		} else if (source == saveSystemDefaultLink) {
			doSaveSystemDefault(ureq);
		} else if (source == resetSystemDefaultLink) {
			doResetSystemDefault(ureq);
		} else if (CMD_DROP_WIDGET.equals(event.getCommand())) {
			doDropWidget(ureq);
		} else if (CMD_ADD_WIDGET.equals(event.getCommand())) {
			doAddWidget(ureq);
		} else if (CMD_REMOVE_WIDGET.equals(event.getCommand())) {
			doRemoveWidget(ureq);
		}
	}

	/**
	 * Save the current widget configuration as personal preferences.
	 */
	private void doSave(UserRequest ureq) {
		DashboardPrefs prefs = buildCurrentPrefs();
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		guiPrefs.putAndSave(DashboardController.class, dashboardId, prefs);
		log.debug("Dashboard '{}' personal preferences saved: {}", dashboardId, prefs.getEnabledWidgets());
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * Delete the personal preferences, reverting to system default or all widgets.
	 */
	private void doReset(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		guiPrefs.putAndSave(DashboardController.class, dashboardId, null);
		log.debug("Dashboard '{}' personal preferences reset", dashboardId);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * Save the current widget configuration as the system-wide default.
	 * Audit-logged at INFO level.
	 */
	private void doSaveSystemDefault(UserRequest ureq) {
		DashboardPrefs prefs = buildCurrentPrefs();
		dashboardSystemDefaultsManager.saveSystemDefault(dashboardId, prefs);
		log.info("Dashboard '{}' system default saved by identity::{} with widgets: {}",
				dashboardId, getIdentity().getKey(), prefs.getEnabledWidgets());
		showInfo("dashboard.system.default.saved");
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * Delete the system-wide default configuration. Audit-logged at INFO level.
	 */
	private void doResetSystemDefault(UserRequest ureq) {
		dashboardSystemDefaultsManager.deleteSystemDefault(dashboardId);
		log.info("Dashboard '{}' system default reset by identity::{}",
				dashboardId, getIdentity().getKey());
		showInfo("dashboard.system.default.deleted");
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * Build a {@link DashboardPrefs} from the current enabled widgets list
	 * in the Velocity context.
	 */
	private DashboardPrefs buildCurrentPrefs() {
		@SuppressWarnings("unchecked")
		List<Widget> enabled = (List<Widget>) mainVC.getContext().get("enabledWidgets");
		List<String> names = new ArrayList<>();
		if (enabled != null) {
			for (Widget w : enabled) {
				names.add(w.getName());
			}
		}
		return new DashboardPrefs(names);
	}

	/**
	 * Handle Dragula drop event: reorder the dragged widget before the sibling.
	 * Parameters {@code "dragged"} and {@code "sibling"} are sent from JavaScript.
	 */
	private void doDropWidget(UserRequest ureq) {
		String draggedName = ureq.getParameter("dragged");
		String siblingName = ureq.getParameter("sibling");
		if (!StringHelper.containsNonWhitespace(draggedName)) return;

		@SuppressWarnings("unchecked")
		List<Widget> enabled = (List<Widget>) mainVC.getContext().get("enabledWidgets");
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
		log.debug("Dashboard '{}' widget '{}' reordered (before: '{}')", dashboardId, draggedName, siblingName);
		mainVC.setDirty(true);
	}

	/**
	 * Move a widget from the disabled list to the enabled list.
	 * Parameter {@code "widget"} is sent from JavaScript.
	 */
	private void doAddWidget(UserRequest ureq) {
		String widgetName = ureq.getParameter("widget");
		if (!StringHelper.containsNonWhitespace(widgetName)) return;

		@SuppressWarnings("unchecked")
		List<Widget> enabled = (List<Widget>) mainVC.getContext().get("enabledWidgets");
		@SuppressWarnings("unchecked")
		List<Widget> disabled = (List<Widget>) mainVC.getContext().get("disabledWidgets");
		if (enabled == null || disabled == null) return;

		Widget toAdd = null;
		for (Widget w : disabled) {
			if (w.getName().equals(widgetName)) {
				toAdd = w;
				break;
			}
		}
		if (toAdd == null) return;

		disabled.remove(toAdd);
		enabled.add(toAdd);
		log.debug("Dashboard '{}' widget '{}' added", dashboardId, widgetName);
		mainVC.setDirty(true);
	}

	/**
	 * Move a widget from the enabled list to the disabled list.
	 * Parameter {@code "widget"} is sent from JavaScript.
	 */
	private void doRemoveWidget(UserRequest ureq) {
		String widgetName = ureq.getParameter("widget");
		if (!StringHelper.containsNonWhitespace(widgetName)) return;

		@SuppressWarnings("unchecked")
		List<Widget> enabled = (List<Widget>) mainVC.getContext().get("enabledWidgets");
		@SuppressWarnings("unchecked")
		List<Widget> disabled = (List<Widget>) mainVC.getContext().get("disabledWidgets");
		if (enabled == null || disabled == null) return;

		Widget toRemove = null;
		for (Widget w : enabled) {
			if (w.getName().equals(widgetName)) {
				toRemove = w;
				break;
			}
		}
		if (toRemove == null) return;

		enabled.remove(toRemove);
		disabled.add(toRemove);
		log.debug("Dashboard '{}' widget '{}' removed", dashboardId, widgetName);
		mainVC.setDirty(true);
	}
}
