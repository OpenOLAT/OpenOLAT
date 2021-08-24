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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.gui.components.dropdown;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown.ButtonSize;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 15.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DropdownItem extends FormItemImpl implements FormItemCollection {
	
	private final Dropdown dropdown;
	private final List<FormItem> items = new ArrayList<>();
	
	public DropdownItem(String name, String label, Translator translator) {
		super(name);
		dropdown = new Dropdown(name, label, false, translator);
	}
	
	public void setButton(boolean button) {
		dropdown.setButton(button);
	}
	
	public void setEmbbeded(boolean embbeded) {
		dropdown.setEmbbeded(embbeded);
	}
	
	public DropdownOrientation getOrientation() {
		return dropdown.getOrientation();
	}

	public void setOrientation(DropdownOrientation orientation) {
		dropdown.setOrientation(orientation);
	}
	
	public String getIconCSS() {
		return dropdown.getIconCSS();
	}
	
	public void setIconCSS(String iconCSS) {
		dropdown.setIconCSS(iconCSS);
	}
	
	public String getCarretIconCSS() {
		return dropdown.getCarretIconCSS();
	}

	public void setCarretIconCSS(String carretIconCSS) {
		dropdown.setCarretIconCSS(carretIconCSS);
	}
	
	public void setDomReplacementWrapperRequired(boolean required) {
		dropdown.setDomReplacementWrapperRequired(required);
	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		dropdown.setElementCssClass(elementCssClass);
		super.setElementCssClass(elementCssClass);
	}

	public void addElement(FormItem item) {
		items.add(item);
		if(item instanceof FormLink) {
			Link linkCmp =((FormLink)item).getComponent();
			linkCmp.setDomReplacementWrapperRequired(false);
			dropdown.addComponent(linkCmp);
		} else {
			dropdown.addComponent(item.getComponent());
		}
	}
	
	public void removeAllFormItems() {
		items.clear();
		dropdown.removeAllComponents();
	}
	
	public int size() {
		return items.size();
	}

	@Override
	protected Component getFormItemComponent() {
		return dropdown;
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		return items;
	}

	@Override
	public FormItem getFormComponent(String name) {
		for(FormItem item:items) {
			if(item != null && item.getName().equals(name)) {
				return item;
			}
		}
		return null;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}

	/**
	 * Trigger content height check to see if drop down has enough space. If not,
	 * enlarge content. Only set this to true if you have cut drop downs in the 
	 * GUI. 
	 * 
	 * @param expandContentHeight
	 */
	public void setExpandContentHeight(boolean expandContentHeight) {
		dropdown.setExpandContentHeight(expandContentHeight);
	}
	
	/**
	 * @return true: check if drop down fits into content area and expand if
	 *         necessary; false: don't check.
	 */
	public boolean isExpandContentHeight() {
		return dropdown.isExpandContentHeight();
	}

	public void setButtonSize(ButtonSize buttonSize) {
		dropdown.setButtonSize(buttonSize);
	}
}
