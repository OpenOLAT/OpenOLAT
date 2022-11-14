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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.util.ValidationStatus;

/**
 * Implements an HTML horizontal bar (&lt;HR&gt;) element.
 * 
 * @author twuersch
 * 
 */
public class SpacerElementImpl extends FormItemImpl implements SpacerElement {
	
	private final SpacerElementComponent component;
	
	public SpacerElementImpl(String name) {
		super(name);
		component = new SpacerElementComponent(name, this);
	}


	@Override
	public void evalFormRequest(UserRequest ureq) {
		// No need to do that for this element.

	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	public void reset() {
		// No need to do that for this element.
	}

	@Override
	protected void rootFormAvailable() {
		// Not available for this element.
	}

	@Override
	public void validate(List<ValidationStatus> validationResults) {
		// No need to do that for this element.
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SpacerElement#setSpacerCssClass(java.lang.String)
	 */
	@Override
	public void setSpacerCssClass(String spacerCssClass) {
		component.setSpacerCssClass(spacerCssClass);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SpacerElement#getSpacerCssClass()
	 */
	@Override
	public String getSpacerCssClass() {
		return component.getSpacerCssClass();
	}
}
