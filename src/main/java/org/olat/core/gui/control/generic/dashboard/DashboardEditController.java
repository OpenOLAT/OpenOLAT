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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dashboard.DashboardController.Widget;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;

/**
 * Edit controller for dashboard editing. Shows proxy widgets with
 * drag & drop reordering (Dragula), remove/add actions, and save/cancel/reset.
 * Fires {@link Event#CHANGED_EVENT} after saving or resetting,
 * {@link Event#CANCELLED_EVENT} on cancel.
 *
 * Initial date: Mar 07, 2026<br>
 * @author gnaegi, gn@frentix.com, https://www.frentix.com
 */
public class DashboardEditController extends BasicController {

	private static final String CMD_DROP_WIDGET = "drop-widget";
	private static final String CMD_ADD_WIDGET = "add-widget";
	private static final String CMD_REMOVE_WIDGET = "remove-widget";

	private final VelocityContainer mainVC;
	private final Link saveLink;
	private final Link cancelLink;
	private final Link resetLink;

	private final String dashboardId;

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
		} else if (CMD_DROP_WIDGET.equals(event.getCommand())) {
			doDropWidget(ureq);
		} else if (CMD_ADD_WIDGET.equals(event.getCommand())) {
			doAddWidget(ureq);
		} else if (CMD_REMOVE_WIDGET.equals(event.getCommand())) {
			doRemoveWidget(ureq);
		}
	}

	private void doSave(UserRequest ureq) {
		DashboardPrefs prefs = buildCurrentPrefs();
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		guiPrefs.putAndSave(DashboardController.class, dashboardId, prefs);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doReset(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		guiPrefs.putAndSave(DashboardController.class, dashboardId, null);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

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
		mainVC.setDirty(true);
	}

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
		mainVC.setDirty(true);
	}

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
		mainVC.setDirty(true);
	}
}
