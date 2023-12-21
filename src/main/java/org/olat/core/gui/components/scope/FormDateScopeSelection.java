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
package org.olat.core.gui.components.scope;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateRange;

/**
 * 
 * Initial date: 1 Dev 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormDateScopeSelection extends FormItemImpl implements ComponentEventListener {
	
	private final DateScopeSelection component;
	
	public FormDateScopeSelection(WindowControl wControl, String name, Locale locale) {
		super(name);
		component = new DateScopeSelection(wControl, name, locale);
		component.addListener(this);
	}

	public boolean isSelected() {
		return component.isSelected();
	}
	
	public String getSelectedKey() {
		return component.getSelectedKey();
	}
	
	public void setSelectedKey(String selectedKey) {
		component.setSelectedKey(selectedKey);
	}
	
	public DateRange getSelectedDateRange() {
		return component.getSelectedDateRange();
	}
	
	public void setHintsEnabled(boolean hintsEnabled) {
		component.setHintsEnabled(hintsEnabled);
	}
	
	public void setAllowNoSelection(boolean allowNoSelection) {
		component.setAllowNoSelection(allowNoSelection);
	}
	
	public void setDateScopes(List<DateScope> scopes) {
		component.setDateScopes(scopes);
	}
	
	public void setAdditionalDateScopes(List<DateScope> additionalDateScopes) {
		component.setAdditionalDateScopes(additionalDateScopes);
	}
	
	public void setCustomScopeLimit(DateRange customScopeLimit) {
		component.setCustomScopeLimit(customScopeLimit);
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
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (source == component) {
			if(event instanceof ScopeEvent scopeEvent) {
				getRootForm().fireFormEvent(ureq, new FormEvent(scopeEvent, this, FormEvent.ONCLICK));
			}
		}
	}

}
