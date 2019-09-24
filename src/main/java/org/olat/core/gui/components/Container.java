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

package org.olat.core.gui.components;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.gui.translator.Translator;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public abstract class Container extends AbstractComponent implements ComponentCollection {
	private Map<String, Component> components = new HashMap<>(5);

	/**
	 * @param name
	 */
	public Container(String name) {
		super(name);
	}

	/**
	 * @param name
	 * @param translator
	 */
	public Container(String name, Translator translator) {
		super(name, translator);
	}
	
	/**
	 * @param id
	 * @param name
	 * @param translator
	 */
	public Container(String id, String name, Translator translator) {
		super(id, name, translator);
	}

	 /* puts the component into this container.
	 * @param name how the component is called, e.g. for rendering in a html fragment: $r.render("name")
	 * @param component the component to add as a child into this container
	 */
	public void put(String name, Component component) {
		if (name == null) throw new RuntimeException("name of component may not be null: childtype=" + component.getClass().getName()
				+ ", parent=" + getComponentName());
		components.put(name, component);
		component.setParent(this);
		setDirty(true);
		// inherit translator from container if component does not provide
		// translator (e.g. from velocitycontainer to Link component. it assumes that all containers have a translator on create time.
		if (component.getTranslator() == null) component.setTranslator(getTranslator());
		
	}

	/**
	 * removes the component from the container.
	 * Hint: it can often be more appropriate to use a panel and then use panel.setContent(null) to empty that panel
	 * @param component
	 */
	public void remove(Component component) {
		if(component != null) component.setParent(null);
		boolean removed = components.values().remove(component);
		if (removed) {
			setDirty(true);
		}
	}
	
	/**
	 * Remove a component from the container.
	 * 
	 * @param componentName the name of the component to remove
	 * @return The removed component or null if nothing was removed.
	 */
	public Component remove(String componentName) {
		Component removedComponent = components.remove(componentName);
		if(removedComponent != null) {
			setDirty(true);
		}
		return removedComponent;
	}

	/**
	 * use only rarely!
	 * @param name
	 * @return
	 */
	@Override
	public Component getComponent(String name) {
		return components.get(name);
	}

	/**
	 * Use only rarely!!
	 * @return
	 */
	@Override
	public Iterable<Component> getComponents() {
		return components.values();
	}
	
	public void clear() {
		components.clear();
	}
	
	public boolean contains(Component cmp) {
		return components.containsValue(cmp);
	}

	/**
	 * @see org.olat.core.gui.components.Component#getExtendedDebugInfo()
	 */
	@Override
	public String getExtendedDebugInfo() {
		return "";
	}
}