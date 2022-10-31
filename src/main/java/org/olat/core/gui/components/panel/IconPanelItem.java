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
package org.olat.core.gui.components.panel;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 28 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IconPanelItem extends FormItemImpl implements FormItemCollection {
	
	private final IconPanel iconPanel;
	private final List<FormItem> items = new ArrayList<>();
	
	public IconPanelItem(String name) {
		super(name);
		iconPanel = new IconPanel(name);
	}

	@Override
	protected Component getFormItemComponent() {
		return iconPanel;
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		return items;
	}

	@Override
	public FormItem getFormComponent(String name) {
		for (FormItem item:items) {
			if (item != null && item.getName().equals(name)) {
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

	@Override
	public void setElementCssClass(String elementCssClass) {
		iconPanel.setElementCssClass(elementCssClass);
		super.setElementCssClass(elementCssClass);
	}
	
	public void setDomReplacementWrapperRequired(boolean required) {
		iconPanel.setDomReplacementWrapperRequired(required);
	}

	public void setContent(Component content) {
		iconPanel.setContent(content);
	}

	public void setIconCssClass(String iconCssClass) {
		iconPanel.setIconCssClass(iconCssClass);
	}
	
	public void addLink(FormLink link) {
		if (link != null) {
			link.setDomReplacementWrapperRequired(false);
			items.add(link);
			iconPanel.addLink(link.getComponent());
		}
	}
	
	public void removeLink(String name) {
		if (StringHelper.containsNonWhitespace(name)) {
			items.removeIf(link -> link.getComponent().getComponentName().equals(name));
			iconPanel.removeLink(name);
		}
	}
	
	public void removeAllLinks() {
		items.clear();
		iconPanel.removeAllLinks();
	}
	
	public void setTitle(String title) {
		iconPanel.setTitle(title);
	}

}
