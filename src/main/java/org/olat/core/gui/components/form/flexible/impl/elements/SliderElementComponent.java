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
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;

/**
 * 
 * Initial date: 9 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SliderElementComponent extends FormBaseComponentImpl {

	private static final ComponentRenderer RENDERER = new SliderElementRenderer();
	
	private double value;
	private boolean hasValue = false;
	private double minValue;
	private double maxValue;
	private int step;
	
	private final SliderElement sliderElement;
	
	public SliderElementComponent(String name, SliderElement sliderElement) {
		super(name);
		this.sliderElement = sliderElement;
	}
	
	@Override
	public SliderElement getFormItem() {
		return sliderElement;
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.hasValue = true;
		this.value = value;
	}
	
	public void deleteValue() {
		this.hasValue = false;
		this.value = 0;
	}
	
	public boolean hasValue() {
		return hasValue;
	}
	
	public double getMinValue() {
		return minValue;
	}
	
	public void setMinValue(double min) {
		minValue = min;
	}
	
	public double getMaxValue() {
		return maxValue;
	}
	
	public void setMaxValue(double max) {
		maxValue = max;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredStaticJsFile("js/jquery/ui/jquery-ui-1.11.4.custom.qti.min.js");
		jsa.addRequiredStaticJsFile("js/jquery/sliderpips/jquery-ui-slider-pips.js");
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
