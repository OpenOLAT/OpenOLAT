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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 *
 * Initial date: 2026-05-27<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ObjectSelectionComponent extends FormBaseComponentImpl implements ComponentCollection {

	static final String CMD_BROWSE = "browse";

	private static final ComponentRenderer RENDERER = new ObjectSelectionComponentRenderer();

	private final ObjectSelectionElementImpl element;

	private boolean browserButtonVisible;
	private String browserButtonIconCss;
	private String browserButtonTitle;
	private String browserButtonAriaLabel;

	public ObjectSelectionComponent(ObjectSelectionElementImpl element) {
		super(element.getFormItemId(), element.getName());
		this.element = element;
		setSpanAsDomReplaceable(true);
		setDomReplacementWrapperRequired(false);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	public FormItem getFormItem() {
		return element;
	}

	@Override
	public Component getComponent(String name) {
		FormItem item = element.getFormComponent(name);
		return item == null ? null : item.getComponent();
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmpList = new ArrayList<>(1);
		for (FormItem item : element.getFormItems()) {
			cmpList.add(item.getComponent());
		}
		return cmpList;
	}

	public boolean isBrowserButtonVisible() {
		return browserButtonVisible;
	}

	public void setBrowserButtonVisible(boolean browserButtonVisible) {
		this.browserButtonVisible = browserButtonVisible;
		setDirty(true);
	}

	public String getBrowserButtonIconCss() {
		return browserButtonIconCss;
	}

	public void setBrowserButtonIconCss(String browserButtonIconCss) {
		this.browserButtonIconCss = browserButtonIconCss;
	}

	public String getBrowserButtonTitle() {
		return browserButtonTitle;
	}

	public void setBrowserButtonTitle(String browserButtonTitle) {
		this.browserButtonTitle = browserButtonTitle;
	}

	public String getBrowserButtonAriaLabel() {
		return browserButtonAriaLabel;
	}

	public void setBrowserButtonAriaLabel(String browserButtonAriaLabel) {
		this.browserButtonAriaLabel = browserButtonAriaLabel;
	}
}
