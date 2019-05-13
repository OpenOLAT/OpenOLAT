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
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 9 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SliderElementImpl extends FormItemImpl implements SliderElement {
	
	private static final Logger log = Tracing.createLoggerFor(SliderElementImpl.class);
	
	private final SliderElementComponent component;
	
	private double value;
	
	public SliderElementImpl(String name) {
		super(name);
		component = new SliderElementComponent(name, this);
	}

	@Override
	public double getMinValue() {
		return component.getMinValue();
	}

	@Override
	public void setMinValue(double min) {
		component.setMinValue(min);
	}

	@Override
	public double getMaxValue() {
		return component.getMaxValue();
	}

	@Override
	public void setMaxValue(double max) {
		component.setMaxValue(max);
	}

	@Override
	public int getStep() {
		return component.getStep();
	}

	@Override
	public void setStep(int step) {
		component.setStep(step);
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	public void setValue(double value) {
		this.value = value;
		component.setValue(value);
	}

	@Override
	public boolean hasValue() {
		return component.hasValue();
	}

	@Override
	public void deleteValue() {
		component.deleteValue();
	}

	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		component.setDomReplacementWrapperRequired(required);
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
		String paramId = component.getFormDispatchId();
		String paramValue = getRootForm().getRequestParameter(paramId);
		if (StringHelper.containsNonWhitespace(paramValue)) {
			try {
				setValue(Double.parseDouble(paramValue));
			} catch (NumberFormatException e) {
				log.error("Cannot parse: " + paramValue, e);
			}
			component.setDirty(true);
		}
	}

	@Override
	public void reset() {
		//
	}
}
