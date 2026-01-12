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
package org.olat.core.gui.components.daynav;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: Jan 6, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DayNavElementImpl extends FormItemImpl implements DayNavElement {
	
	private final DayNavComponent component;

	protected DayNavElementImpl(String name) {
		super(name, false);
		
		component = new DayNavComponent(this);
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
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void dispatchFormRequest(UserRequest ureq) {
		component.doDispatchRequest(ureq);
		getRootForm().fireFormEvent(ureq, new FormEvent("change", this, FormEvent.ONCLICK));
	}

	@Override
	public void reset() {
		//
	}

	@Override
	public Date getStartDate() {
		return component.getStartDate();
	}

	@Override
	public void setStartDate(Date startDate) {
		component.setStartDate(startDate);
	}

	@Override
	public Date getEndDate() {
		return component.getEndDate();
	}

	@Override
	public Date getSelectedDate() {
		return component.getSelectedDate();
	}

	@Override
	public void setSelectedDate(Date selectedDate) {
		component.setSelectedDate(selectedDate);
	}

}
