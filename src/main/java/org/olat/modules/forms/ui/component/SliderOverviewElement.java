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
package org.olat.modules.forms.ui.component;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 19 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SliderOverviewElement extends FormItemImpl {
	
	private final SliderOverviewComponent component;
	
	public SliderOverviewElement(String name) {
		super(name);
		component = new SliderOverviewComponent(name, this);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}
	
	public double getMinValue() {
		return component.getMinValue();
	}

	public void setMinValue(double min) {
		component.setMinValue(min);
	}

	public double getMaxValue() {
		return component.getMaxValue();
	}

	public void setMaxValue(double max) {
		component.setMaxValue(max);
	}
	
	public List<SliderPoint> getValues() {
		return component.getValues();
	}
	
	public void setValues(List<SliderPoint> values) {
		component.setValues(values);
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
