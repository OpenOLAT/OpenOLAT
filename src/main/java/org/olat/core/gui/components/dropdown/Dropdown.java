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
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 25.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Dropdown extends AbstractComponent implements ComponentCollection {
	
	private static final ComponentRenderer RENDERER = new DropdownRenderer();
	
	private String i18nKey;
	private boolean button = false;
	private boolean embbeded = false;
	private boolean translated = false;
	private DropdownOrientation orientation = DropdownOrientation.normal;
	private String iconCSS;
	private String innerText;
	private String innerCSS;
	private List<Component> components = new ArrayList<>();
	
	public Dropdown(String name, String i18nKey, boolean domReplacementWrapperRequired, Translator translator) {
		super(name, translator);
		this.i18nKey = i18nKey;
		setDomReplacementWrapperRequired(domReplacementWrapperRequired);
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public void setI18nKey(String i18nKey) {
		this.i18nKey = i18nKey;
	}
	
	public void setTranslatedLabel(String label) {
		this.i18nKey = label;
		this.translated = true;
	}
	
	public boolean isTranslated() {
		return translated;
	}

	public boolean isButton() {
		return button;
	}

	public void setButton(boolean button) {
		this.button = button;
	}

	public boolean isEmbbeded() {
		return embbeded;
	}

	public void setEmbbeded(boolean embbeded) {
		this.embbeded = embbeded;
	}

	public DropdownOrientation getOrientation() {
		return orientation;
	}

	public void setOrientation(DropdownOrientation orientation) {
		this.orientation = orientation;
	}

	public String getIconCSS() {
		return iconCSS;
	}

	public void setIconCSS(String iconCSS) {
		this.iconCSS = iconCSS;
	}
	
	public void addComponent(Component component) {
		if(component != null) {
			components.add(component);
		}
	}

	public void addComponent(Link component) {
		if(component != null) {
			components.add(component);
		}
	}
	
	public void addComponent(int index, Link component) {
		if(component != null) {
			if(index >= 0 && index < components.size()) {
				components.add(index, component);
			} else {
				components.add(component);
			}
		}
	}
	
	public void addComponent(Spacer spacer) {
		if(spacer != null) {
			components.add(spacer);
		}
	}
	
	public void removeComponent(String name) {
		for(Iterator<Component> it=components.iterator(); it.hasNext(); ) {
			if(it.next().getComponentName().equals(name)) {
				it.remove();
			}
		}
	}
	
	public void removeAllComponents() {
		components.clear();
	}
	
	public int size() {
		return components.size();
	}

	/**
	 * Mark the active link from this dropdown as active. This is only a GUI issue, see active method on link
	 * @param activeLink
	 */
	public void setActiveLink(Link activeLink) {
		for (Component component : components) {
			if (component instanceof Link) {
				// only handle link
				Link link = (Link) component;
				if (activeLink == null) {
					// deactivate all links
					link.setActive(false);
				} else if (link == activeLink && !link.isActive()) { 
					// activate active link if not already activated
					link.setActive(true);
				} else if (link != activeLink && link.isActive()) {
					// deactivate all other links that are activated
					link.setActive(false);
				}
			}
		}
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
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {		
		return RENDERER;
	}
	
	/**
	 * 
	 * @return The text displayed as part of the button or NULL if not used
	 *         (default)
	 */
	public String getInnerText() {
		return innerText;
	}

	/**
	 * Set an optional text displayed together with the dropdown icon as an
	 * icon-text combination.
	 * 
	 * @param innerText The text displayed as part of the button or NULL if not used
	 *                  (default)
	 */
	public void setInnerText(String innerText) {
		if (StringHelper.containsNonWhitespace(innerText)) {
			this.innerText = innerText;
		} else {			
			this.innerText = null;
		}
	}

	/**
	 * @return CSS classes that should surround the inner dropdown wrapper or NULL
	 *         (default)
	 */
	public String getInnerCSS() {
		return innerCSS;
	}

	/**
	 * Set an option CSS class that is added to the wrapper element surrounding the
	 * icon or icon-text combination.
	 * 
	 * @param innerCSS
	 */
	public void setInnerCSS(String innerCSS) {
		if (StringHelper.containsNonWhitespace(innerCSS)) {
			this.innerCSS = innerCSS;
		} else {			
			this.innerCSS = null;
		}
	}

	public static class Spacer extends AbstractComponent {
		
		public Spacer(String name) {
			super(name);
		}

		@Override
		protected void doDispatchRequest(UserRequest ureq) {
			//
		}

		@Override
		public ComponentRenderer getHTMLRendererSingleton() {
			return null;
		}
	}
}