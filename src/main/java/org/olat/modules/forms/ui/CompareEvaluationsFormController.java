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

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.FormXStream;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.ui.component.SliderOverviewElement;
import org.olat.modules.forms.ui.model.EvaluationFormElementWrapper;
import org.olat.modules.forms.ui.model.SliderWrapper;
import org.olat.modules.forms.ui.model.TextInputWrapper;
import org.olat.modules.portfolio.PageBody;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CompareEvaluationsFormController extends FormBasicController {

	private int count = 0;
	private final Form form;
	private PageBody anchor;
	private final List<Identity> evaluators;
	
	private EvaluationFormSession session;
	private final Map<String, List<EvaluationFormResponse>> identifierToResponses = new HashMap<>();
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	/**
	 * The responses are saved and linked to the anchor.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param form
	 * @param anchor The database object which hold the evaluation.
	 */
	public CompareEvaluationsFormController(UserRequest ureq, WindowControl wControl,
			List<Identity> evaluators, PageBody anchor, RepositoryEntry formEntry) {
		super(ureq, wControl, "run");
		this.anchor = anchor;
		this.evaluators = evaluators;
		
		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()), FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		form = (Form)XStreamHelper.readObject(FormXStream.getXStream(), formFile);
		
		loadResponses();
		initForm(ureq);
	}
	
	public EvaluationFormSession getSession() {
		return session;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateElements();
	}
	
	private void updateElements() {
		List<EvaluationFormElementWrapper> elementWrappers = new ArrayList<>();
		for(AbstractElement element:form.getElements()) {
			appendsElement(element, elementWrappers);
		}

		flc.contextPut("elements", elementWrappers);
	}
	
	private void loadResponses() {
		flc.contextPut("messageNotDone", Boolean.FALSE);

		List<EvaluationFormResponse> responses = evaluationFormManager.getResponsesFromPortfolioEvaluation(evaluators, anchor, EvaluationFormSessionStatus.done);
		for(EvaluationFormResponse response:responses) {
			List<EvaluationFormResponse> responseList = identifierToResponses.get(response.getResponseIdentifier());
			if(responseList == null) {
				responseList = new ArrayList<>();
				identifierToResponses.put(response.getResponseIdentifier(), responseList);
			}
			responseList.add(response);
		}
	}
	
	private void appendsElement(AbstractElement element, List<EvaluationFormElementWrapper> wrappers) {
		String type = element.getType();
		switch(type) {
			case "formhtitle":
			case "formhr":
			case "formhtmlraw":
				wrappers.add(new EvaluationFormElementWrapper(element));
				break;
			case "formrubric":
				EvaluationFormElementWrapper sliderWrapper = forgeRubric((Rubric)element);
				if(sliderWrapper != null) {
					wrappers.add(sliderWrapper);
				}
				break;
			case "formtextinput":
				List<EvaluationFormElementWrapper> inputWrappers = forgeTextInput((TextInput)element);
				if(inputWrappers != null && inputWrappers.size() > 0) {
					wrappers.addAll(inputWrappers);
				}
				break;
		}
	}

	private List<EvaluationFormElementWrapper> forgeTextInput(TextInput element) {
		int rows = 12;
		if(element.getRows() > 0) {
			rows = element.getRows();
		}
		
		List<EvaluationFormResponse> responses = identifierToResponses.get(element.getId());
		List<EvaluationFormElementWrapper> inputWrappers = new ArrayList<>(responses.size());
		for(EvaluationFormResponse response:responses) {
			if(StringHelper.containsNonWhitespace(response.getStringuifiedResponse())) {
				String initialValue = response.getStringuifiedResponse();
				TextElement textEl = uifactory.addTextAreaElement("textinput_" + (count++), rows, 72, initialValue, flc);
				textEl.setEnabled(false);
		
				TextInputWrapper textInputWrapper = new TextInputWrapper(element, textEl, null);
				textEl.setUserObject(textInputWrapper);
				EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(element);
				wrapper.setTextInputWrapper(textInputWrapper);
				inputWrappers.add(wrapper);
			}
		}
		return inputWrappers;
	}
	
	private EvaluationFormElementWrapper forgeRubric(Rubric element) {
		EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(element);
		wrapper.setSliderOverview(true);
		List<Slider> sliders = element.getSliders();
		List<SliderWrapper> sliderWrappers = new ArrayList<>(sliders.size());
		for(Slider slider:sliders) {
			String responseIdentifier = slider.getId();
			List<EvaluationFormResponse> responses = identifierToResponses.get(responseIdentifier);
			SliderWrapper sliderWrapper = forgeSliderStats(slider, element, responses);
			sliderWrappers.add(sliderWrapper);
		}
		wrapper.setSliders(sliderWrappers);
		return wrapper;
	}
	
	private SliderWrapper forgeSliderStats(Slider slider, Rubric element, List<EvaluationFormResponse> responses) {
		String id = "overview_" + (count++);
		SliderOverviewElement overviewEl = new SliderOverviewElement(id);
		overviewEl.setMinValue(element.getStart());
		overviewEl.setMaxValue(element.getEnd());
		flc.add(id, overviewEl);
		
		List<Double> values = new ArrayList<>();
		if(responses != null && responses.size() > 0) {
			for(EvaluationFormResponse response:responses) {
				if(response.getNumericalResponse() != null) {
					values.add(response.getNumericalResponse().doubleValue());
				}
			}
		}
		overviewEl.setValues(values);
		
		return new SliderWrapper(slider, overviewEl);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//super.propagateDirtinessToContainer(fiSrc, fe);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}
