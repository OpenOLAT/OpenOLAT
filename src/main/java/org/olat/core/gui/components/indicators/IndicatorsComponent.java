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
package org.olat.core.gui.components.indicators;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * 
 * Initial date: Oct 28, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class IndicatorsComponent extends FormBaseComponentImpl implements ComponentCollection {
	
	private static final ComponentRenderer RENDERER = new IndicatorsRenderer();
	
	private final IndicatorsItem element;
	private Component keyIndicator;
	private List<Component> focusIndicators;

	public IndicatorsComponent(String name) {
		super(name);
		this.element = null;
	}
	
	public IndicatorsComponent(IndicatorsItem element) {
		super(element.getFormItemId(), element.getName());
		this.element = element;
	}

	@Override
	public FormItem getFormItem() {
		return element;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	public Component getComponent(String name) {
		for (Component component : getComponents()) {
			if(component.getComponentName().equals(name)) {
				return component;
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		if (keyIndicator == null) {
			return focusIndicators;
		}
		
		List<Component> componenets = new ArrayList<>();
		componenets.add(keyIndicator);
		
		if (focusIndicators != null && !focusIndicators.isEmpty()) {
			componenets.addAll(focusIndicators);
		}
		
		return componenets;
	}

	public Component getKeyIndicator() {
		return keyIndicator;
	}

	public void setKeyIndicator(Component keyIndicator) {
		this.keyIndicator = keyIndicator;
		setDirty(true);
	}

	public List<Component> getFocusIndicators() {
		return focusIndicators;
	}

	public void setFocusIndicators(List<Component> focusIndicators) {
		this.focusIndicators = focusIndicators;
		setDirty(true);
	}

}
