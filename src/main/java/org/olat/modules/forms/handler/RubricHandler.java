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
package org.olat.modules.forms.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.ui.PageRunControllerElement;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.ui.RubricController;
import org.olat.modules.forms.ui.RubricEditorController;
import org.olat.modules.forms.ui.RubricInspectorController;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.EvaluationFormResponseControllerElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricHandler implements EvaluationFormElementHandler, SimpleAddPageElementHandler, CloneElementHandler {
	
	private final boolean restrictedEdit;
	private final boolean restrictedEditWheight;
	
	public RubricHandler(boolean restrictedEdit, boolean restrictedEditWheight) {
		this.restrictedEdit = restrictedEdit;
		this.restrictedEditWheight = restrictedEditWheight;
	}

	@Override
	public String getType() {
		return "formrubric";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_rubric";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.questionType;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints hints) {
		if(element instanceof Rubric) {
			Controller ctrl = new RubricController(ureq, wControl, (Rubric)element);
			return new PageRunControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof Rubric) {
			return new RubricEditorController(ureq, wControl, (Rubric)element, restrictedEdit, restrictedEditWheight);
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof Rubric) {
			return new RubricInspectorController(ureq, wControl, (Rubric)element, restrictedEdit);
		}
		return null;
	}
	
	@Override
	public PageElement createPageElement(Locale locale) {
		Rubric rubric = new Rubric();
		rubric.setId(UUID.randomUUID().toString());
		rubric.setMandatory(false);
		rubric.setStart(1);
		rubric.setEnd(5);
		rubric.setSteps(5);
		rubric.setSliderType(SliderType.discrete);
		rubric.setScaleType(ScaleType.oneToMax);
		
		Slider slider = new Slider();
		slider.setId(UUID.randomUUID().toString());
		slider.setStartLabel("Start");
		slider.setWeight(1);
		rubric.getSliders().add(slider);
		return rubric;
	}
	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof Rubric) {
			Rubric rubric = (Rubric)element;
			Rubric clone = new Rubric();
			clone.setId(UUID.randomUUID().toString());
			clone.setMandatory(rubric.isMandatory());
			clone.setEnd(rubric.getEnd());
			clone.setLowerBoundInsufficient(rubric.getLowerBoundInsufficient());
			clone.setLowerBoundNeutral(rubric.getLowerBoundNeutral());
			clone.setLowerBoundSufficient(rubric.getLowerBoundSufficient());
			clone.setName(rubric.getName());
			clone.setNameDisplays(new ArrayList<>(rubric.getNameDisplays()));
			clone.setNoResponseEnabled(rubric.isNoResponseEnabled());
			clone.setSliderStepLabelsEnabled(rubric.isSliderStepLabelsEnabled());
			clone.setScaleType(rubric.getScaleType());
			clone.setSliderType(rubric.getSliderType());
			clone.setStart(rubric.getStart());
			clone.setStartGoodRating(rubric.isStartGoodRating());
			clone.setSteps(rubric.getSteps());
			clone.setUpperBoundInsufficient(rubric.getUpperBoundInsufficient());
			clone.setUpperBoundNeutral(rubric.getUpperBoundNeutral());
			clone.setUpperBoundSufficient(rubric.getUpperBoundSufficient());
			if (rubric.getStepLabels() != null) {
				List<StepLabel> clonedLabels = new ArrayList<>(rubric.getStepLabels().size());
				clone.setStepLabels(clonedLabels);
				for (StepLabel stepLabel : rubric.getStepLabels()) {
					StepLabel clonedLabel = new StepLabel();
					clonedLabel.setId(UUID.randomUUID().toString());
					clonedLabel.setLabel(stepLabel.getLabel());
					clonedLabels.add(clonedLabel);
				}
			}
			if (rubric.getSliders() != null) {
				List<Slider> clonedSliders = new ArrayList<>(rubric.getSliders().size());
				clone.setSliders(clonedSliders);
				for (Slider slider : rubric.getSliders()) {
					Slider clonedSlider = new Slider();
					clonedSlider.setId(UUID.randomUUID().toString());
					clonedSlider.setEndLabel(slider.getEndLabel());
					clonedSlider.setStartLabel(slider.getStartLabel());
					clonedSlider.setWeight(slider.getWeight());
					clonedSliders.add(clonedSlider);
					
					if (slider.getStepLabels() != null) {
						List<StepLabel> clonedStepLabels = new ArrayList<>(slider.getStepLabels().size());
						for (StepLabel stepLabel : slider.getStepLabels()) {
							StepLabel clonedStepLabel = new StepLabel();
							clonedStepLabel.setId(UUID.randomUUID().toString());
							clonedStepLabel.setLabel(stepLabel.getLabel());
							clonedStepLabels.add(clonedStepLabel);
						}
						clonedSlider.setStepLabels(clonedStepLabels);
					}
				}
			}
			return clone;
		}
		return null;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element, ExecutionIdentity executionIdentity) {
		if (element instanceof Rubric) {
			Rubric rubric = (Rubric) element;
			EvaluationFormResponseController ctrl = new RubricController(ureq, wControl, rubric, rootForm);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

}
