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
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * 
 * Initial date: 13.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class MultipleSelectionComponent extends FormBaseComponentImpl {
	
	private final MultipleSelectionElementImpl element;
	private static final ComponentRenderer RENDERER =  new MultipleSelectionRenderer();
	
	private CheckboxElement[] checkComponents;
	
	public MultipleSelectionComponent(String id, MultipleSelectionElementImpl element) {
		super(id, element.getName(), null);
		this.element = element;
	}
	
	@Override
	public MultipleSelectionElementImpl getFormItem() {
		return element;
	}

	public CheckboxElement[] getCheckComponents() {
		return checkComponents;
	}

	public void setCheckComponents(CheckboxElement[] checkComponents) {
		this.checkComponents = checkComponents;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if(checkComponents != null) {
			for(CheckboxElement checkComponent:checkComponents) {
				checkComponent.setEnabled(enabled);
			}
		}
		super.setEnabled(enabled);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
