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
package org.olat.core.gui.components.tabbedpane;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.tabbedpane.TabbedPane.TabPane;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 1 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TabbedPaneItem extends FormItemImpl implements FormItemCollection {
	
	public enum TabIndentation { none, defaultFormLayout }
	
	private final TabbedPane component;
	private TabIndentation tabIndentation = TabIndentation.none;
	
	public TabbedPaneItem(String name, Locale locale) {
		super(name);
		component = new TabbedPane(this, name, locale);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		String taid = ureq.getParameter(TabbedPane.PARAM_PANE_ID);
		if(StringHelper.containsNonWhitespace(taid) && isVisible()) {
			component.dispatchRequest(ureq);
		}
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		List<TabPane> tabPanes = component.getTabPanes();
		List<FormItem> items = new ArrayList<>(tabPanes.size());
		for(TabPane tabPane:tabPanes) {
			FormItem item = tabPane.getComponentItem();
			if(item != null) {
				items.add(item);
			}
		}
		return items;
	}

	@Override
	public FormItem getFormComponent(String name) {
		List<TabPane> tabPanes = component.getTabPanes();
		for(TabPane tabPane:tabPanes) {
			FormItem item = tabPane.getComponentItem();
			if(item != null && item.getName().equals(name)) {
				return item;
			}
		}
		return null;
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}
	
	public int addTab(String displayName, FormItem item) {
		return component.addTab(displayName, item);
	}
	
	public int addTab(String displayName, String elementCssClass, FormItem item) {
		return component.addTab(displayName, elementCssClass, item);
	}
	
	public void setSelectedPane(UserRequest ureq, int newSelectedPane) {
		component.setSelectedPane(ureq, newSelectedPane);
	}

	@Override
	protected void rootFormAvailable() {
		List<TabPane> tabPanes = component.getTabPanes();
		for(TabPane tabPane:tabPanes) {
			rootFormAvailable(tabPane.getComponentItem());
		}
	}
	
	private final void rootFormAvailable(FormItem item) {
		if(item != null && item.getRootForm() != getRootForm()) {
			item.setRootForm(getRootForm());
		}
	}
	
	@Override
	public void reset() {
		//
	}

	public TabIndentation getTabIndentation() {
		return tabIndentation;
	}

	public void setTabIndentation(TabIndentation tabIndentation) {
		this.tabIndentation = tabIndentation;
	}
}
