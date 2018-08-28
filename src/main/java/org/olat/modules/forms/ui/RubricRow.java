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
package org.olat.modules.forms.ui;

import java.util.Collections;
import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;

/**
 * 
 * Initial date: 21.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricRow {
	
	private final Rubric rubric;
	private final String startLabel;
	private final String endLabel;
	private final SliderStatistic sliderStatistic;

	public RubricRow(Rubric rubric, Slider slider, SliderStatistic sliderStatistic) {
		super();
		this.rubric = rubric;
		this.startLabel = slider != null? slider.getStartLabel(): null;
		this.endLabel = slider != null? slider.getEndLabel(): null;
		this.sliderStatistic = sliderStatistic;
	}

	public Object getStartLabel() {
		return startLabel;
	}

	public Object getEndLabel() {
		return endLabel;
	}

	public boolean hasEndLabel() {
		for (Slider slider: rubric.getSliders()) {
			String endLabel = slider.getEndLabel();
			if (StringHelper.containsNonWhitespace(endLabel)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isNoResponseEnabled() {
		return rubric.isNoResponseEnabled();
	}

	public Long getNumberOfNoResponses() {
		return sliderStatistic.getNumberOfNoResponses();
	}

	public Long getNumberOfResponses() {
		return sliderStatistic.getNumberOfResponses();
	}

	public Double getMedian() {
		return sliderStatistic.getMedian();
	}

	public Double getAvg() {
		return sliderStatistic.getAvg();
	}
	
	public Double getVariance() {
		return sliderStatistic.getVariance();
	}
	
	public Double getSdtDev() {
		return sliderStatistic.getStdDev();
	}

	public List<Long> getStepCounts() {
		if (rubric.getSliderType().equals(SliderType.continuous)) {
			return Collections.emptyList();
		}
		return sliderStatistic.getStepCounts();
	}

}
