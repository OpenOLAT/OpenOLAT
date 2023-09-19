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
import java.util.List;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Slider {
	
	private String id;
	
	private String startLabel;
	private String endLabel;
	private List<StepLabel> stepLabels;
	private Integer weight;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getStartLabel() {
		return startLabel;
	}
	
	public void setStartLabel(String startLabel) {
		this.startLabel = startLabel;
	}
	
	public String getEndLabel() {
		return endLabel;
	}
	
	public void setEndLabel(String endLabel) {
		this.endLabel = endLabel;
	}

	public List<StepLabel> getStepLabels() {
		if (stepLabels == null) {
			stepLabels = new ArrayList<>();
		}
		return stepLabels;
	}

	public void setStepLabels(List<StepLabel> stepLabels) {
		this.stepLabels = stepLabels;
	}

	public Integer getWeight() {
		if (weight == null) {
			weight = 1;
		}
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Slider) {
			Slider slider = (Slider)obj;
			return getId() != null && getId().equals(slider.getId());
		}
		return super.equals(obj);
	}
}
