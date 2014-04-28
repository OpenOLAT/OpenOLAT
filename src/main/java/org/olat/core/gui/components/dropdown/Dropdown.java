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
package org.olat.core.gui.components.dropdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 25.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Dropdown extends AbstractComponent implements ComponentCollection {
	
	private static final ComponentRenderer RENDERER = new DropdownRenderer();
	
	private String i18nKey;
	private boolean button;
	private String iconCSS;
	private List<Component> components = new ArrayList<>();
	
	public Dropdown(String name, Translator translator) {
		super(name, translator);
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public void setI18nKey(String i18nKey) {
		this.i18nKey = i18nKey;
	}

	public boolean isButton() {
		return button;
	}

	public void setButton(boolean button) {
		this.button = button;
	}

	public String getIconCSS() {
		return iconCSS;
	}

	public void setIconCSS(String iconCSS) {
		this.iconCSS = iconCSS;
	}

	public void addComponent(Component component) {
		components.add(component);
	}
	
	public int size() {
		return components.size();
	}

	@Override
	public Component getComponent(String name) {
		for(Component component:components) {
			if(component.getComponentName().equals(name)) {
				return component;
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		return components;
	}

	@Override
	public Map<String, Component> getComponentMap() {
		return Collections.emptyMap();
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
