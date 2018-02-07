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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.chart.RadarChartElement;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.AbstractHTMLElement;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.model.xml.TextInput;

/**
 * 
 * Initial date: 13 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormElementWrapper {

	private boolean raw;
	private boolean sliderOverview;
	private boolean radarOverview;
	protected final AbstractElement element;
	
	private RadarChartElement radarEl;
	private TextInputWrapper textInput;
	private FileUploadWrapper fileUploadWrapper;
	private FileUploadCompareWrapper fileUploadCompareWrapper;
	private List<SliderWrapper> sliders;
	
	public EvaluationFormElementWrapper(AbstractElement element) {
		this.element = element;
		raw = (element instanceof AbstractHTMLElement);
	}
	
	public String getContent() {
		return ((AbstractHTMLElement)element).getContent();
	}
	
	public boolean isRaw() {
		return raw;
	}
	
	public boolean isRubric() {
		return element instanceof Rubric;
	}
	
	public boolean isSliderOverview() {
		return sliderOverview;
	}
	
	public void setSliderOverview(boolean sliderOverview) {
		this.sliderOverview = sliderOverview;
	}
	
	public boolean isRadarOverview() {
		return radarOverview;
	}

	public void setRadarOverview(boolean radarOverview) {
		this.radarOverview = radarOverview;
	}

	public boolean isDiscreteRubric() {
		if(element instanceof Rubric) {
			Rubric rubric = (Rubric)element;
			return rubric.getSliderType() == SliderType.discrete;
		}
		return false;
	}
	
	public boolean isDiscreteSliderRubric() {
		if(element instanceof Rubric) {
			Rubric rubric = (Rubric)element;
			return rubric.getSliderType() == SliderType.discrete_slider;
		}
		return false;
	}
	
	public boolean isTextInput() {
		return element instanceof TextInput;
	}

	public TextInputWrapper getTextInputWrapper() {
		return textInput;
	}

	public void setTextInputWrapper(TextInputWrapper textInput) {
		this.textInput = textInput;
	}
	
	public boolean isFileUpload() {
		return fileUploadWrapper != null;
	}
	
	public FileUploadWrapper getFileUploadWrapper() {
		return fileUploadWrapper;
	}

	public void setFileUploadWrapper(FileUploadWrapper fileUploadWrapper) {
		this.fileUploadWrapper = fileUploadWrapper;
	}
	
	public boolean isFileUploadCompare() {
		return fileUploadCompareWrapper != null;
	}

	public FileUploadCompareWrapper getFileUploadCompareWrapper() {
		return fileUploadCompareWrapper;
	}

	public void setFileUploadCompareWrapper(FileUploadCompareWrapper fileUploadCompareWrapper) {
		this.fileUploadCompareWrapper = fileUploadCompareWrapper;
	}

	public static int getWidthInPercent(Rubric rubric) {
		if(rubric.getSliderType() == SliderType.discrete) {
			int steps = rubric.getSteps();
			int stepInPercent = Math.round(100.0f / steps) - 1;
			return stepInPercent;
		}
		return 0;
	}
	
	public int getStepInPercent() {
		if(element instanceof Rubric) {
			return getWidthInPercent((Rubric)element);
		}
		return 0;
	}
	
	public boolean isStepLabels() {
		if(element instanceof Rubric) {
			Rubric rubric = (Rubric)element;
			if(rubric.getStepLabels() == null || rubric.getStepLabels().isEmpty()) {
				return false;
			}
			
			List<StepLabel> stepLabels = rubric.getStepLabels();
			for(StepLabel stepLabel:stepLabels) {
				if(stepLabel != null && StringHelper.containsNonWhitespace(stepLabel.getLabel())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isLeftLabels() {
		if(element instanceof Rubric) {
			Rubric rubric = (Rubric)element;
			List<Slider> rubricSliders = rubric.getSliders();
			if(rubricSliders != null && rubricSliders.size() > 0) {
				for(Slider slider:rubricSliders) {
					if(slider != null && StringHelper.containsNonWhitespace(slider.getStartLabel())) {
						return true;
					}
				}
			}	
		}
		return false;
	}
	
	public boolean isRightLabels() {
		if(element instanceof Rubric) {
			Rubric rubric = (Rubric)element;
			List<Slider> rubricSliders = rubric.getSliders();
			if(rubricSliders != null && rubricSliders.size() > 0) {
				for(Slider slider:rubricSliders) {
					if(slider != null && StringHelper.containsNonWhitespace(slider.getEndLabel())) {
						return true;
					}
				}
			}	
		}
		return false;
	}
	
	public List<String> getStepLabels() {
		if(element instanceof Rubric) {
			Rubric rubric = (Rubric)element;
			if(rubric.getStepLabels() != null && rubric.getStepLabels().size() > 0) {
				List<String> stepLabels = new ArrayList<>(rubric.getStepLabels().size());
				for(StepLabel stepLabel:rubric.getStepLabels()) {
					stepLabels.add(stepLabel.getLabel());
				}
				return stepLabels;
			}
		}
		return new ArrayList<>(1);
	}

	public List<SliderWrapper> getSliders() {
		return sliders;
	}

	public void setSliders(List<SliderWrapper> sliders) {
		this.sliders = sliders;
	}

	public RadarChartElement getRadarEl() {
		return radarEl;
	}

	public void setRadarEl(RadarChartElement radarEl) {
		this.radarEl = radarEl;
	}
}
