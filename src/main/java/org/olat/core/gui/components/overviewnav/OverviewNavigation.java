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
package org.olat.core.gui.components.overviewnav;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;

/**
 *
 * Initial date: 16 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OverviewNavigation extends AbstractComponent implements ComponentCollection, ComponentEventListener {
	
	private static final ComponentRenderer RENDERER = new OverviewNavigationRenderer();
	
	private final String guiPrefsKey;
	private final Translator navTranslator;
	private final Link toggleLink;
	private boolean expanded;
	private String ariaLabel;
	private List<OverviewNavigationGroup> groups = List.of();
	
	public OverviewNavigation(String name, String guiPrefsKey, UserRequest ureq) {
		super(name);
		this.guiPrefsKey = guiPrefsKey;
		navTranslator = Util.createPackageTranslator(OverviewNavigation.class, ureq.getLocale());
		
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		expanded = ((Boolean)prefs.get(OverviewNavigation.class, guiPrefsKey, Boolean.TRUE)).booleanValue();
		
		toggleLink = LinkFactory.createCustomLink(name + "_toggle", "toggle", null, Link.BUTTON_XSMALL + Link.NONTRANSLATED, null, this);
		toggleLink.setElementCssClass("o_button_details");
		updateToggle();
	}
	
	String getPanelId() {
		return "o_onav_" + getDispatchID() + "_panel";
	}
	
	String getDescriptionId(OverviewNavigationItem item) {
		return "o_onav_" + item.getLink().getDispatchID() + "_desc";
	}
	
	Translator getNavTranslator() {
		return navTranslator;
	}
	
	Link getToggleLink() {
		return toggleLink;
	}
	
	public boolean isExpanded() {
		return expanded;
	}
	
	public String getAriaLabel() {
		return ariaLabel;
	}
	
	public void setAriaLabel(String ariaLabel) {
		this.ariaLabel = ariaLabel;
		setDirty(true);
	}
	
	public List<OverviewNavigationGroup> getGroups() {
		return groups;
	}
	
	public void setGroups(List<OverviewNavigationGroup> groups) {
		this.groups = groups == null ? List.of() : groups;
		for (OverviewNavigationGroup group : this.groups) {
			group.setNavigation(this);
		}
		itemsChanged();
	}
	
	void itemsChanged() {
		updateDescriptions();
		setDirty(true);
	}
	
	@Override
	public Component getComponent(String name) {
		for (Component component : getComponents()) {
			if (component.getComponentName().equals(name)) {
				return component;
			}
		}
		return null;
	}
	
	@Override
	public Iterable<Component> getComponents() {
		List<Component> components = new ArrayList<>();
		components.add(toggleLink);
		for (OverviewNavigationGroup group : groups) {
			for (OverviewNavigationItem item : group.getItems()) {
				components.add(item.getLink());
			}
		}
		return components;
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (source == toggleLink) {
			doToggle(ureq);
		}
	}
	
	private void doToggle(UserRequest ureq) {
		expanded = !expanded;
		ureq.getUserSession().getGuiPreferences().putAndSave(OverviewNavigation.class, guiPrefsKey, Boolean.valueOf(expanded));
		updateToggle();
		itemsChanged();
	}
	
	private void updateToggle() {
		toggleLink.setCustomDisplayText(navTranslator.translate(expanded ? "overview.nav.collapse" : "overview.nav.expand"));
		toggleLink.setIconLeftCSS("o_icon " + (expanded ? "o_icon_details_collaps" : "o_icon_details_expand"));
		toggleLink.setAriaExpanded(Boolean.valueOf(expanded));
	}
	
	private void updateDescriptions() {
		for (OverviewNavigationGroup group : groups) {
			for (OverviewNavigationItem item : group.getItems()) {
				item.getLink().setAriaDescribedBy(expanded && StringHelper.containsNonWhitespace(item.getDescription()) ? getDescriptionId(item) : null);
			}
		}
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
}
