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

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * 
 * Initial date: 19 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SliderOverviewComponent extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new SliderOverviewRenderer();
	
	private double minValue;
	private double maxValue;
	private List<SliderPoint> values;
	
	private final SliderOverviewElement sliderElement;
	
	public SliderOverviewComponent(String name, SliderOverviewElement sliderElement) {
		super(name);
		this.sliderElement = sliderElement;
	}
	
	@Override
	public SliderOverviewElement getFormItem() {
		return sliderElement;
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

	public List<SliderPoint> getValues() {
		return values;
	}

	public void setValues(List<SliderPoint> values) {
		this.values = values;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	

}
