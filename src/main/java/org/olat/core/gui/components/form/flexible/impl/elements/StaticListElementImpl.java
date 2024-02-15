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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.StaticListElement;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 14 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class StaticListElementImpl extends FormItemImpl implements StaticListElement {
	
	private final StaticListComponent component;

	public StaticListElementImpl(String name) {
		super(name);
		this.component = new StaticListComponent(name + "_comp", this);
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
	public void setValues(List<String> values) {
		component.setValues(values);
	}
	
	@Override
	public void setInitialNumValues(int initialNumValues) {
		component.setInitialNumValues(initialNumValues);
	}
	
	@Override
	public void setShowAllI18nKey(String showAllI18nKey) {
		component.setShowAllI18nKey(showAllI18nKey);
	}
	
}
