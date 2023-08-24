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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * 
 * Initial date: 31 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SelectionDisplayComponent extends FormBaseComponentImpl {

	private final FormItem element;
	private static final ComponentRenderer RENDERER = new SelectionDisplayRenderer();
	
	private String value;
	private boolean ariaExpanded = false;

	public SelectionDisplayComponent(FormItem element) {
		super(element.getFormItemId(), element.getName());
		this.element = element;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	public FormItem getFormItem() {
		return element;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (this.value != value) {
			setDirty(true);
		}
		this.value = value;
	}

	public boolean isAriaExpanded() {
		return ariaExpanded;
	}

	public void setAriaExpanded(boolean ariaExpanded) {
		if (this.ariaExpanded != ariaExpanded) {
			setDirty(true);
		}
		this.ariaExpanded = ariaExpanded;
	}

}
