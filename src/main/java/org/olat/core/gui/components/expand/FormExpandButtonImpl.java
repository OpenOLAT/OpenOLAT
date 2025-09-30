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
package org.olat.core.gui.components.expand;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: Sep 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormExpandButtonImpl extends FormItemImpl implements FormExpandButton {
	
	private final ExpandButton component;

	public FormExpandButtonImpl(String name) {
		super(name, false);
		
		component = new ExpandButton(this);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		// No data to remember
	}
	
	@Override
	public void dispatchFormRequest(UserRequest ureq) {
		component.setExpanded(!component.isExpanded());
		getRootForm().fireFormEvent(ureq, new FormEvent(Event.DONE_EVENT, this, FormEvent.ONCLICK));
	}

	@Override
	public void reset() {
		//
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void setText(String text) {
		component.setText(text);
	}

	@Override
	public void setEscapeMode(EscapeMode escapeMode) {
		component.setEscapeMode(escapeMode);
	}

	@Override
	public void setTitle(String title) {
		component.setTitle(title);
	}

	@Override
	public void setCssClass(String cssClass) {
		component.setCssClass(cssClass);
	}

	@Override
	public void setIconLeftExpandedCss(String iconLeftExpandedCss) {
		component.setIconLeftExpandedCss(iconLeftExpandedCss);
	}

	@Override
	public void setIconLeftCollapsedCss(String iconLeftCollapsedCss) {
		component.setIconLeftCollapsedCss(iconLeftCollapsedCss);
	}

	@Override
	public void setIconRightExpandedCss(String iconRightExpandedCss) {
		component.setIconRightExpandedCss(iconRightExpandedCss);
	}

	@Override
	public void setIconRightCollapsedCss(String iconRightCollapsedCss) {
		component.setIconRightCollapsedCss(iconRightCollapsedCss);
	}

	@Override
	public void setAriaLabel(String ariaLabel) {
		component.setAriaLabel(ariaLabel);
	}
	
	@Override
	public void setAriaHasPopup(String ariaHasPopup) {
		component.setAriaHasPopup(ariaHasPopup);
	}
	
	@Override
	public void setAriaControls(String ariaControls) {
		component.setAriaControls(ariaControls);
	}
	
	@Override
	public boolean isExpanded() {
		return component.isExpanded();
	}

	@Override
	public void setExpanded(boolean expanded) {
		component.setExpanded(expanded);
	}

}
