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

import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.ui.PageRunControllerElement;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.ui.RubricController;
import org.olat.modules.forms.ui.RubricEditorController;
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
public class RubricHandler implements EvaluationFormElementHandler, SimpleAddPageElementHandler {
	
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
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints hints) {
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
