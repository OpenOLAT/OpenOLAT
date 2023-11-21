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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.control.Event;

/**
 * Description:<br>
 * Extending the formlink to be used as a toggle-switch supporting on/off.
 * 
 * <P>
 * Initial Date: 21.07.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class FormToggleImpl extends FormItemImpl implements FormToggle {

	private final FormToggleComponent component;

	public FormToggleImpl(String name, String toggleOnText, String toggleOffText) {
		super(name);
		component = new FormToggleComponent(name ,toggleOnText, toggleOffText, this);
	}
	
	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		super.setElementCssClass(elementCssClass);
		component.setElementCssClass(elementCssClass);
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void dispatchFormRequest(UserRequest ureq) {
		toggle();
		getRootForm().fireFormEvent(ureq,
				new FormEvent(Event.DONE_EVENT, this, FormEvent.ONCLICK));
	}
	

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void toggle() {
		if (isOn()) {
			toggleOff();
		} else {
			toggleOn();
		}
	}
	
	@Override
	public void toggle(boolean on) {
		component.setOn(on);
	}

	@Override
	public boolean isOn() {
		return component.isOn();
	}

	@Override
	public void toggleOn() {
		component.setOn(true);
	}

	@Override
	public void toggleOff() {
		component.setOn(false);
	}

	@Override
	public void setIconsCss(String iconOnCss, String iconOffCss) {
		component.setIconCss(iconOnCss, iconOffCss);
	}
	
	@Override
	public void setTitle(String title) {
		component.setTitle(title);
	}
	
	@Override
	public void setAriaLabel(String ariaLabel) {
		component.setAriaLabel(ariaLabel);
	}
	
	@Override
	public void setPresentation(Presentation presentation) {
		component.setPresentation(presentation);
	}
	
	@Override
	public void reset() {
		//
	}

}
