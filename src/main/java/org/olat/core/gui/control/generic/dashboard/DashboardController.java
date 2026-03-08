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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Dashboard controller that renders widgets in a Bento grid layout.
 * Supports user-configurable widget order and visibility, stored in
 * GuiPreferences. When editing is enabled (dashboardId is set),
 * an "Edit dashboard" button opens the {@link DashboardEditController}.
 *
 * Initial date: Jan 19, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class DashboardController extends BasicController {

	private final StackedPanel mainPanel;
	private final VelocityContainer mainVC;
	private Link editLink;

	private final String dashboardId;
	private final List<Widget> allWidgets = new ArrayList<>();
	private List<String> enabledWidgetNames = null;
	private List<String> systemDefaultEnabledWidgetNames = null;
	private final List<Widget> enabledWidgets = new ArrayList<>();
	private final Map<String, Widget> widgetsByName = new HashMap<>();

	private DashboardEditController editCtrl;

	@Autowired
	private DashboardSystemDefaultsManager dashboardSystemDefaultsManager;

	/**
	 * Creates a dashboard without edit support (backward compatible).
	 */
	public DashboardController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null);
	}

	/**
	 * Creates a dashboard with edit support.
	 *
	 * @param dashboardId unique identifier for storing user preferences,
	 *                    or null to disable editing
	 */
	public DashboardController(UserRequest ureq, WindowControl wControl, String dashboardId) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(DashboardController.class, getLocale()));
		this.dashboardId = dashboardId;
		mainVC = createVelocityContainer("dashboard");

		if (dashboardId != null && !ureq.getUserSession().getRoles().isGuestOnly()) {
			editLink = LinkFactory.createButton("dashboard.edit", mainVC, this);
			editLink.setGhost(true);
			editLink.setIconLeftCSS("o_icon o_icon_edit");
			// load from the users preferences and system defaults
			enabledWidgetNames = loadEnabledNames(ureq);
			systemDefaultEnabledWidgetNames = loadSystemDefaultEnabledNames();
		}

		mainVC.contextPut("hasWidgets", Boolean.FALSE);
		mainVC.contextPut("enabledWidgets", enabledWidgets);
		mainPanel = putInitialPanel(mainVC);
	}

	public void setDashboardCss(String dashboardCss) {
		mainVC.contextPut("dashboardCss", dashboardCss);
	}
	
	/**
	 * Add a widget. The title is auto-detected from the controller
	 * if it implements {@link DashboardWidget}, or can be passed explicitly.
	 *
	 * @param name  unique widget name within this dashboard
	 * @param title human-readable title shown in edit mode, or null for auto-detection
	 * @param ctrl  the widget controller
	 * @param size  the bento box size
	 */
	public void addWidget(String name, String title, Controller ctrl, BentoBoxSize size) {
		if (title == null && ctrl instanceof DashboardWidget dashboardWidget) {
			title = dashboardWidget.getWidgetTitle();
		}
		Widget widget = new Widget(name, title, size.getCss());
		allWidgets.add(widget);
		widgetsByName.put(name, widget);
		mainVC.put(name, ctrl.getInitialComponent());
		applyConfiguration();
		mainVC.contextPut("hasWidgets", Boolean.TRUE);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == editLink) {
			doEdit(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editCtrl) {
			cleanUpEdit();
			if (event == Event.CHANGED_EVENT) {
				reloadAndApplyConfiguration(ureq);				
			}
		}
	}

	private List<String> loadEnabledNames(UserRequest ureq) {
		if (dashboardId == null) return null;
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Object stored = guiPrefs.get(DashboardController.class, dashboardId);
		if (stored instanceof DashboardPrefs prefs) {
			return prefs.getEnabledWidgets();
		}
		return null;
	}

	private List<String> loadSystemDefaultEnabledNames() {
		if (dashboardId == null) return null;
		DashboardPrefs prefs = dashboardSystemDefaultsManager.loadSystemDefault(dashboardId);
		if (prefs != null) {
			return prefs.getEnabledWidgets();
		}
		return null;
	}

	private void reloadAndApplyConfiguration(UserRequest ureq) {
		enabledWidgetNames = loadEnabledNames(ureq);
		systemDefaultEnabledWidgetNames = loadSystemDefaultEnabledNames();
		applyConfiguration();
	}

	private void applyConfiguration() {
		enabledWidgets.clear();
		// Cascade: personal > system default > all widgets
		List<String> effectiveNames = enabledWidgetNames;
		if (effectiveNames == null) {
			effectiveNames = systemDefaultEnabledWidgetNames;
		}
		if (effectiveNames != null) {
			for (String name : effectiveNames) {
				Widget w = widgetsByName.get(name);
				if (w != null) {
					enabledWidgets.add(w);
				}
			}
		} else {
			enabledWidgets.addAll(allWidgets);
		}
	}

	private void doEdit(UserRequest ureq) {
		cleanUpEdit();

		// Cascade: personal > system default > all widgets
		List<String> effectiveNames = loadEnabledNames(ureq);
		if (effectiveNames == null) {
			effectiveNames = loadSystemDefaultEnabledNames();
		}
		List<Widget> enabled = new ArrayList<>();
		List<Widget> disabled = new ArrayList<>();
		if (effectiveNames != null) {
			for (String name : effectiveNames) {
				Widget w = widgetsByName.get(name);
				if (w != null) enabled.add(w);
			}
			for (Widget w : allWidgets) {
				if (!effectiveNames.contains(w.getName())) {
					disabled.add(w);
				}
			}
		} else {
			enabled.addAll(allWidgets);
		}

		editCtrl = new DashboardEditController(ureq, getWindowControl(), dashboardId, enabled, disabled);
		listenTo(editCtrl);
		mainPanel.setContent(editCtrl.getInitialComponent());
	}

	private void cleanUpEdit() {
		mainPanel.setContent(mainVC);
		removeAsListenerAndDispose(editCtrl);
		editCtrl = null;
	}

	/**
	 * View model for a widget in the Velocity template.
	 */
	public static final class Widget {

		private final String name;
		private final String title;
		private final String css;

		public Widget(String name, String title, String css) {
			this.name = name;
			this.title = title;
			this.css = css;
		}

		public String getName() {
			return name;
		}

		public String getTitle() {
			return title;
		}

		public String getCss() {
			return css;
		}
	}
}
