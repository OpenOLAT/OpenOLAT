/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.chart;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 30 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SpeedometerElement extends FormItemImpl {
	
	private final SpeedometerComponent component;
	
	public SpeedometerElement(String name) {
		super(name);
		component = new SpeedometerComponent(name);
	}
	
	public double getValue() {
		return component.getValue();
	}

	public void setValue(double value) {
		component.setValue(value);
	}
	
	public double getMaxValue() {
		return component.getValue();
	}

	public void setMaxValue(double value) {
		component.setMaxValue(value);
	}
	
	public String getValueCssClass() {
		return component.getValueCssClass();
	}

	public void setValueCssClass(String valueCssClass) {
		component.setValueCssClass(valueCssClass);
	}
	
	@Override
	protected Component getFormItemComponent() {
		return component;
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
	protected void rootFormAvailable() {
		//
	}
}
