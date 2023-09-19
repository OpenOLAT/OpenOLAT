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
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.FormToggle.Presentation;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 9 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FormToggleComponent extends FormBaseComponentImpl {

	private static final ComponentRenderer RENDERER = new FormToggleRenderer();
	
	private final FormToggleImpl element;
	private boolean isOn = false;
	private String toggleOnText;
	private String toggleOffText;
	private String ariaLabel;
	private String ariaLabelledBy;
	private Presentation presentation = Presentation.SWITCH;
	
	public FormToggleComponent(String name, String toggleOnText, String toggleOffText) {
		this(name, toggleOnText, toggleOffText, null);
	}
	
	public FormToggleComponent(String name, String toggleOnText, String toggleOffText, FormToggleImpl element) {
		super(name);
		this.toggleOnText = toggleOnText;
		this.toggleOffText = toggleOffText;
		this.element = element;

		setSpanAsDomReplaceable(true);
		setDomReplacementWrapperRequired(false);
	}

	@Override
	public FormToggle getFormItem() {
		return element;
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		if("toggle".equals(cmd)) {
			setOn(!isOn());
			fireEvent(ureq, new Event(cmd));
		}
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	public void toggleOn() {
		setOn(true);
	}
	
	public void toggleOff() {
		setOn(false);
	}

	public boolean isOn() {
		return isOn;
	}

	public void setOn(boolean isOn) {
		this.isOn = isOn;
		setDirty(true);
	}

	public String getToggleOnText() {
		return toggleOnText;
	}

	public void setToggleOnText(String toggleOnText) {
		this.toggleOnText = toggleOnText;
	}

	public String getToggleOffText() {
		return toggleOffText;
	}

	public void setToggleOffText(String toggleOffText) {
		this.toggleOffText = toggleOffText;
	}

	public String getAriaLabel() {
		return ariaLabel;
	}

	public void setAriaLabel(String ariaLabel) {
		this.ariaLabel = ariaLabel;
	}

	public String getAriaLabelledBy() {
		return ariaLabelledBy;
	}

	public void setAriaLabelledBy(String ariaLabelBy) {
		this.ariaLabelledBy = ariaLabelBy;
	}

	public Presentation getPresentation() {
		return presentation;
	}

	public void setPresentation(Presentation presentation) {
		this.presentation = presentation;
	}
	
}
