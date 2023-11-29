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
package org.olat.core.gui.components.scope;

import static org.olat.core.util.StringHelper.blankIfNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.elements.FormToggleComponent;
import org.olat.core.gui.control.Event;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 24 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ScopeSelection extends AbstractComponent implements ComponentCollection, ComponentEventListener {
	
	private static final ComponentRenderer RENDERER = new ScopeRenderer();
	
	private boolean hintsEnabled = true;
	private List<ScopeItem> scopeItems;
	protected String selectedKey;

	ScopeSelection(String name) {
		super(name);
	}
	
	public void setHintsEnabled(boolean hintsEnabled) {
		this.hintsEnabled = hintsEnabled;
		setDirty(true);
	}

	boolean isHintsEnabled() {
		return hintsEnabled;
	}

	void setScopes(List<? extends Scope> scopes) {
		if (scopes == null) {
			this.scopeItems = List.of();
		} else {
			this.scopeItems = scopes.stream()
					.map(this::createScopeItem)
					.toList();
		}
		
		if (StringHelper.containsNonWhitespace(selectedKey)) {
			scopeItems.stream()
					.filter(item -> selectedKey.equals(item.getKey()))
					.findFirst()
					.ifPresentOrElse(
							scopeItem -> scopeItem.getToggle().toggleOn(),
							() -> selectedKey = null);
		}
		
		setDirty(true);
	}

	List<ScopeItem> getScopeItems() {
		return scopeItems;
	}

	public void setScopeItems(List<ScopeItem> scopeItems) {
		this.scopeItems = scopeItems;
	}

	List<FormToggleComponent> getScopeToggles() {
		return scopeItems.stream().map(ScopeItem::getToggle).toList();
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (source instanceof FormToggleComponent toggle) {
			Optional<ScopeItem> foundItem = scopeItems.stream()
					.filter(item -> item.getToggle() == toggle)
					.findFirst();
			if (foundItem.isPresent()) {
				doToggleItem(ureq, foundItem.get());
			}
		}
	}

	@Override
	public Component getComponent(String name) {
		return scopeItems.stream()
				.map(ScopeItem::getToggle)
				.filter(link -> link.getComponentName().equals(name)).
				findFirst().orElse(null);
	}

	@Override
	public Iterable<Component> getComponents() {
		return new ArrayList<>(getScopeToggles());
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}
	
	ScopeItem createScopeItem(Scope scope) {
		String toggleText = createToggleText(scope.getDisplayName(), scope.getHint());
		
		FormToggleComponent toggle = new FormToggleComponent("o_scope_" + CodeHelper.getRAMUniqueID(), toggleText, toggleText);
		toggle.setPresentation(FormToggle.Presentation.BUTTON);
		toggle.setElementCssClass("o_scope_toggle");
		toggle.setIconCss(null, null);
		toggle.addListener(this);
		
		return new ScopeItem(scope, toggle);
	}

	String createToggleText(String displayName, String hint) {
		StringBuilder toggleTextSb = new StringBuilder();
		toggleTextSb.append("<div class=\"o_scope\">");
		toggleTextSb.append("<div class=\"o_scope_title\">").append(displayName).append("</div>");
		toggleTextSb.append("<div class=\"o_scope_hint\">").append(blankIfNull(hint)).append("</div>");
		toggleTextSb.append("</div>");
		return toggleTextSb.toString();
	}

	protected void doToggleItem(UserRequest ureq, ScopeItem scopeItem) {
		String deselectedKey = doSetSelectedKey(scopeItem);
		toggleOff(deselectedKey);
		fireEvent(ureq, new ScopeEvent(deselectedKey, selectedKey));
	}

	String doSetSelectedKey(ScopeItem scopeItem) {
		String deselectedKey = selectedKey;
		if (scopeItem.getToggle().isOn()) {
			selectedKey = scopeItem.getKey();
		} else {
			selectedKey = null;
		}
		return deselectedKey;
	}
	
	void toggleOff(String key) {
		if (key != null) {
			scopeItems.stream()
					.filter(item -> key.equals(item.getKey()))
					.findFirst()
					.ifPresent(item -> item.getToggle().toggleOff());
		}
	}

}
