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
package org.olat.core.gui.components.util;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * Renders a list of components as one component, wrapped in a flex layout
 * with a gap between the components. Use it wherever a single slot (e.g. a
 * Fact value in org.olat.core.gui.components.factsheet) must show more than
 * one component.
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ComponentList extends AbstractComponent implements ComponentCollection {

	private static final ComponentRenderer RENDERER = new ComponentListRenderer();

	private final List<Component> components;

	public ComponentList(String name, List<Component> components) {
		super(name);
		this.components = components == null ? List.of() : components;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	public List<Component> getComponentList() {
		return components;
	}

	@Override
	public Component getComponent(String name) {
		for (Component component : components) {
			if (name.equals(component.getComponentName())) {
				return component;
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		return new ArrayList<>(components);
	}

}
