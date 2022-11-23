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
package org.olat.core.gui.components.date;

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 28 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TimeElement extends FormItemImpl {
	
	private final TimeComponent component;
	
	public TimeElement(String name, Locale locale) {
		super(name);
		component = new TimeComponent(name, locale);
	}
	
	@Override
	protected Component getFormItemComponent() {
		return component;
	}
	
	public Date getDate() {
		return component.getDate();
	}
	
	public void setDate(Date date) {
		component.setDate(date);
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
	public void reset() {
		//
	}
}
