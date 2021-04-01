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
package org.olat.modules.forms.model.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Rubric extends AbstractElement {

	private static final long serialVersionUID = -8486210445435845568L;
	
	public static final String TYPE = "formrubric";

	private boolean mandatory;
	private SliderType sliderType;
	private ScaleType scaleType;
	private List<Slider> sliders = new ArrayList<>();
	private List<StepLabel> stepLabels = new ArrayList<>();
	
	private String name;
	private List<NameDisplay> nameDisplays;
	private int start;
	private int end;
	private int steps;
	private boolean noResponseEnabled;
	private Double lowerBoundInsufficient;
	private Double upperBoundInsufficient;
	private Double lowerBoundNeutral;
	private Double upperBoundNeutral;
	private Double lowerBoundSufficient;
	private Double upperBoundSufficient;
	private boolean startGoodRating;
	
	@Override
	public String getType() {
		return TYPE;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public SliderType getSliderType() {
		return sliderType;
	}

	public void setSliderType(SliderType sliderType) {
		this.sliderType = sliderType;
	}

	public ScaleType getScaleType() {
		if (scaleType == null) {
			scaleType = ScaleType.oneToMax;
		}
		return scaleType;
	}

	public void setScaleType(ScaleType scaleType) {
		this.scaleType = scaleType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<NameDisplay> getNameDisplays() {
		return nameDisplays != null? nameDisplays: Arrays.asList(NameDisplay.report);
	}

	public void setNameDisplays(List<NameDisplay> nameDisplays) {
		this.nameDisplays = nameDisplays != null? nameDisplays: new ArrayList<>(0);
	}

	public enum NameDisplay {
		execution,
		report;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public boolean isNoResponseEnabled() {
		return noResponseEnabled;
	}

	public void setNoResponseEnabled(boolean noResponseEnabled) {
		this.noResponseEnabled = noResponseEnabled;
	}

	public List<StepLabel> getStepLabels() {
		return stepLabels;
	}

	public void setStepLabels(List<StepLabel> stepLabels) {
		this.stepLabels = stepLabels;
	}

	public List<Slider> getSliders() {
		return sliders;
	}

	public void setSliders(List<Slider> sliders) {
		this.sliders = sliders;
	}

	public enum SliderType {
		discrete,
		discrete_slider,
		continuous
	}
	
	public Double getLowerBoundInsufficient() {
		return lowerBoundInsufficient;
	}

	public void setLowerBoundInsufficient(Double lowerBoundInsufficient) {
		this.lowerBoundInsufficient = lowerBoundInsufficient;
	}

	public Double getUpperBoundInsufficient() {
		return upperBoundInsufficient;
	}

	public void setUpperBoundInsufficient(Double upperBoundInsufficient) {
		this.upperBoundInsufficient = upperBoundInsufficient;
	}

	public Double getLowerBoundNeutral() {
		return lowerBoundNeutral;
	}

	public void setLowerBoundNeutral(Double lowerBoundNeutral) {
		this.lowerBoundNeutral = lowerBoundNeutral;
	}

	public Double getUpperBoundNeutral() {
		return upperBoundNeutral;
	}

	public void setUpperBoundNeutral(Double upperBoundNeutral) {
		this.upperBoundNeutral = upperBoundNeutral;
	}

	public Double getLowerBoundSufficient() {
		return lowerBoundSufficient;
	}

	public void setLowerBoundSufficient(Double lowerBoundSufficient) {
		this.lowerBoundSufficient = lowerBoundSufficient;
	}

	public Double getUpperBoundSufficient() {
		return upperBoundSufficient;
	}

	public void setUpperBoundSufficient(Double upperBoundSufficient) {
		this.upperBoundSufficient = upperBoundSufficient;
	}

	public boolean isStartGoodRating() {
		return startGoodRating;
	}

	public void setStartGoodRating(boolean startGoodRating) {
		this.startGoodRating = startGoodRating;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Rubric) {
			Rubric rubric = (Rubric)obj;
			return getId() != null && getId().equals(rubric.getId());
		}
		return super.equals(obj);
	}

}
