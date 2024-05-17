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
package org.olat.core.gui.components.widget;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * 
 * Initial date: 15 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class WidgetGroup extends AbstractComponent implements ComponentCollection {
	
	private static final ComponentRenderer RENDERER = new WidgetGroupRenderer();
	
	private final List<Widget> widgets = new ArrayList<>();

	protected WidgetGroup(String name) {
		super(name);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public Component getComponent(String name) {
		for (Widget widget : widgets) {
			if (name.equals(widget.getComponentName())) {
				return widget;
			} else if (widget instanceof ComponentWidget componenetWidget) {
				if (name.equals(componenetWidget.getContent().getComponentName())) {
					return componenetWidget.getContent();
				}
			}
		}
		
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> copy = new ArrayList<>(widgets.size());
		for (Widget widget : widgets) {
			copy.add(widget);
			if (widget instanceof ComponentWidget componenetWidget) {
				copy.add(componenetWidget.getContent());
			}
		}
		return copy;
	}
	
	public void add(Widget widget) {
		if (widget == null) {
			return;
		}
		
		widget.setDomReplacementWrapperRequired(false);
		widgets.add(widget);
		setDirty(true);
	}

	List<Widget> getWidgets() {
		return widgets;
	}

}
