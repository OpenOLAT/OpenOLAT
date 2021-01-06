/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.translator.Translator;

/**
 * @author Christian Guretzki
 */
public class FlexiTableComponent extends FormBaseComponentImpl implements ComponentCollection {

	private static final ComponentRenderer CLASSIC_RENDERER = new FlexiTableClassicRenderer();
	private static final ComponentRenderer CUSTOM_RENDERER = new FlexiTableCustomRenderer();
	
	private FlexiTableElementImpl element;
	private final Map<String,Component> components = new HashMap<>();
	
	public FlexiTableComponent(FlexiTableElementImpl element) {
		super(element.getName());
		this.element = element;
	}
	
	public FlexiTableComponent(FlexiTableElementImpl element, Translator translator) {
		super(element.getName(), translator);
		this.element = element;
	}
	
	public FlexiTableElementImpl getFlexiTableElement() {
		return element;
	}

	@Override
	public Component getComponent(String name) {
		FormItem item = element.getFormComponent(name);
		if(item != null) {
			return item.getComponent();
		}
		return components.get(name);
	}
	
	public void put(String name, Component cmp) {
		components.put(name, cmp);
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmps = new ArrayList<>();
		for(FormItem item:element.getFormItems()) {
			Component cmp = item.getComponent();
			if(cmp != null) {// it's possible that not used form links as a null component
				cmps.add(cmp);
			}
		}
		cmps.addAll(components.values());
		return cmps;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		switch(element.getRendererType()) {
			case classic: return CLASSIC_RENDERER;
			case custom: return CUSTOM_RENDERER;
			default: return CLASSIC_RENDERER;
		}
	}
}