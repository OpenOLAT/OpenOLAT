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
package org.olat.modules.forms.ui.model;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.modules.forms.model.xml.Slider;

/**
 * 
 * Initial date: 13 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SliderWrapper {

	private final Slider slider;
	private final SliderElement sliderEl;
	private final SingleSelection radioEl;
	
	public SliderWrapper(Slider slider, SingleSelection radioEl) {
		this(slider, radioEl, null);
	}
	
	public SliderWrapper(Slider slider, SliderElement sliderEl) {
		this(slider, null, sliderEl);
	}
	
	private SliderWrapper(Slider slider, SingleSelection radioEl, SliderElement sliderEl) {
		this.slider = slider;
		this.radioEl = radioEl;
		this.sliderEl = sliderEl;
	}
	
	public String getId() {
		return slider.getId();
	}
	
	public String getStartLabel() {
		String start = slider.getStartLabel();
		return start == null ? "" : start;
	}
	
	public String getEndLabel() {
		String end = slider.getEndLabel();
		return end == null ? "" : end;
	}
	
	public Slider getSlider() {
		return slider;
	}
	
	public FormItem getFormItem() {
		return radioEl == null ? sliderEl : radioEl;
	}
	
	public SingleSelection getRadioEl() {
		return radioEl;
	}
	
	public SliderElement getSliderEl() {
		return sliderEl;
	}
}
