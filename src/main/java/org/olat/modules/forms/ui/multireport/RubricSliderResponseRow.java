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
package org.olat.modules.forms.ui.multireport;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.boxplot.BoxPlot;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.model.xml.Slider;

/**
 * 
 * Initial date: 31 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricSliderResponseRow {

	private Slider slider;
	private final String label;
	
	private Long numOfNoResponses;
	private Long numOfResponses;
	private Long numOfComments;
	
	private BoxPlot assessmentsPlot;
	
	private FormBasicController detailsCtrl;
	
	public RubricSliderResponseRow(String label) {
		this.label = label;
	}
	
	public RubricSliderResponseRow(Slider slider) {
		this.slider = slider;
		if(StringHelper.containsNonWhitespace(slider.getStartLabel())) {
			label = slider.getStartLabel();
		} else {
			label = slider.getEndLabel();
		}
	}
	
	public String getLabel() {
		return label;
	}
	
	public Slider getSlider() {
		return slider;
	}
	
	public Long getNumOfNoResponses() {
		return numOfNoResponses;
	}

	public void setNumOfNoResponses(Long numOfNoResponses) {
		this.numOfNoResponses = numOfNoResponses;
	}

	public Long getNumOfResponses() {
		return numOfResponses;
	}

	public void setNumOfResponses(Long numOfResponses) {
		this.numOfResponses = numOfResponses;
	}

	public Long getNumOfComments() {
		return numOfComments;
	}

	public void setNumOfComments(Long numOfComments) {
		this.numOfComments = numOfComments;
	}

	public BoxPlot getAssessmentsPlot() {
		return assessmentsPlot;
	}

	public void setAssessmentsPlot(BoxPlot assessmentsPlot) {
		this.assessmentsPlot = assessmentsPlot;
	}

	public FormBasicController getDetailsCtrl() {
		return detailsCtrl;
	}
	
	public void setDetailsCtrl(FormBasicController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}
	
	public Component getDetailsControllerComponent() {
		return detailsCtrl == null ? null : detailsCtrl.getInitialFormItem().getComponent();
	}
	
	public String getDetailsControllerName() {
		return detailsCtrl == null ? null : detailsCtrl.getInitialFormItem().getComponent().getComponentName();
	}
}
