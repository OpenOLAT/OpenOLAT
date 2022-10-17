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
package org.olat.modules.ceditor.ui.component;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.render.ValidationResult;
import org.olat.modules.ceditor.ui.event.CloseElementsEvent;
import org.olat.modules.ceditor.ui.event.DropToEditorEvent;
import org.olat.modules.ceditor.ui.event.PositionEnum;

/**
 * 
 * Initial date: 6 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentEditorComponent extends FormBaseComponentImpl implements ComponentCollection, ComponentEventListener, ControllerEventListener {
	
	private static final ContentEditorComponentRenderer RENDERER = new ContentEditorComponentRenderer();

	private List<Component> rootComponents = new ArrayList<>();
	
	public ContentEditorComponent(String name) {
		super(name);
		setDomReplacementWrapperRequired(false);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		if("close_edit_fragment".equals(cmd)) {
			fireEvent(ureq, new CloseElementsEvent());
		} else if("drop_fragment".equals(cmd)) {
			doDropFragment(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		//
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void doDropFragment(UserRequest ureq) {
		String sourceId = ureq.getParameter("source");
		String position = ureq.getParameter("position");
		fireEvent(ureq, new DropToEditorEvent(sourceId, this,
				PositionEnum.valueOf(position, PositionEnum.bottom)));
	}
	
	public void setRootComponents(List<ContentEditorFragment> components) {
		rootComponents = new ArrayList<>(components);
		for(ContentEditorFragment component:components) {
			if(getTranslator() != null) {
				component.setTranslator(getTranslator());
			}
		}
		setDirty(true);
	}
	
	public void addRootComponent(ContentEditorFragment component) {
		if(rootComponents.contains(component) || !checkAdd(component)) return;
		
		rootComponents.add(component);
		setDirty(true);
		if(getTranslator() != null) {
			component.setTranslator(getTranslator());
		}
	}
	
	public void addRootComponent(int index, ContentEditorFragment component) {
		if(!checkAdd(component)) return;
		
		if(index >= 0 && index < rootComponents.size()) {
			rootComponents.add(index, component);
		} else {
			rootComponents.add(component);
		}
		setDirty(true);
		if(getTranslator() != null) {
			component.setTranslator(getTranslator());
		}
	}
	
	private boolean checkAdd(ContentEditorFragment componentToAdd) {
		if(componentToAdd == this) {
			setDirty(true);// add to itself forbidden
			return false;
		}
		return true;
	}
	
	public boolean removeRootComponent(ContentEditorFragment component) {
		boolean removed = rootComponents.remove(component);
		if(removed) {
			setDirty(true);
		}
		return removed;
	}
	
	public int indexOfRootComponent(ContentEditorFragment component) {
		return rootComponents.indexOf(component);
	}
	
	public int numberOfRootComponents() {
		return rootComponents.size();
	}
	
	public boolean moveUpRootComponent(ContentEditorFragment component) {
		int index = rootComponents.indexOf(component);
		if(index > 0 && rootComponents.remove(component)) {
			rootComponents.add(index - 1, component);
			setDirty(true);
			return true;
		}
		return false;
	}
	
	public boolean moveDownRootComponent(ContentEditorFragment component) {
		int index = rootComponents.indexOf(component) + 1;
		if(index < rootComponents.size()) {
			rootComponents.remove(component);
			rootComponents.add(index, component);
			setDirty(true);
			return true;
		}
		return false;
	}
	
	public boolean moveComponentUnderSibling(ContentEditorFragment component, ContentEditorFragment sibling) {
		int index = rootComponents.indexOf(sibling) + 1;
		if(index < rootComponents.size()) {
			rootComponents.remove(component);
			rootComponents.add(index, component);
			setDirty(true);
			return true;
		}
		return false;
	}
	

	public ContentEditorContainerComponent previousRootContainerComponent(ContentEditorFragment component) {
		int index = rootComponents.indexOf(component);
		if(index < 1) {
			return null;
		}
		
		for(int i=index; i-->0; ) {
			ContentEditorFragment fragment = (ContentEditorFragment)rootComponents.get(i);
			if(fragment instanceof ContentEditorContainerComponent) {
				return (ContentEditorContainerComponent)fragment;
			}
		}
		return null;
	}
	
	public ContentEditorContainerComponent nextRootContainerComponent(ContentEditorFragment component) {
		int index = rootComponents.indexOf(component);
		if(index < 1) {
			return null;
		}
		
		for(int i=index + 1; i<rootComponents.size(); i++) {
			ContentEditorFragment fragment = (ContentEditorFragment)rootComponents.get(i);
			if(fragment instanceof ContentEditorContainerComponent) {
				return (ContentEditorContainerComponent)fragment;
			}
		}
		return null;
	}

	@Override
	public Component getComponent(String name) {
		for(Component cmp:getComponents()) {
			if(name.equals(cmp.getComponentName())) {
				return cmp;
			}	
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		return rootComponents;
	}
	
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		/*if(Settings.isDebuging()) {
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/interactjs/interact.js");
		} else {
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/interactjs/interact.min.js");
		}*/
		vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/jquery/openolat/jquery.contenteditor.v3.js");
		vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/dragula/dragula.js");

		super.validate(ureq, vr);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
