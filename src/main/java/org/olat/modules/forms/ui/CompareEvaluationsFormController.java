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
import org.olat.core.gui.components.chart.RadarChartComponent.Format;
import org.olat.core.gui.components.chart.RadarChartElement;
import org.olat.core.gui.components.chart.RadarSeries;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
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
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.ui.component.SliderOverviewElement;
import org.olat.modules.forms.ui.component.SliderPoint;
import org.olat.modules.forms.ui.model.EvaluationFormElementWrapper;
import org.olat.modules.forms.ui.model.SliderWrapper;
import org.olat.modules.forms.ui.model.TextInputWrapper;
import org.olat.modules.portfolio.PageBody;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CompareEvaluationsFormController extends FormBasicController {
	
	private static final String[] colors = new String[]{
			"#EDC951", "#CC333F", "#00A0B0", "#4E4E6C", "#8DC1A1",
			"#F7BC00", "#BB6511", "#B28092", "#003D40", "#FF69D1"
		};

	private int count = 0;
	private final Form form;
	private PageBody anchor;
	private final List<Identity> evaluators;
	
	private EvaluationFormSession session;
	private final Map<String, List<EvaluationFormResponse>> identifierToResponses = new HashMap<>();
	private final Map<Identity,String> evaluatorToColors = new HashMap<>();
	private final Map<Identity,String> evaluatorToNumbers = new HashMap<>();
	
	@Autowired
	private UserManager userManager;
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
		
		int colorCount = 0;
		int evaluatorCount = 0;
		for(Identity evaluator:evaluators) {
			int i = (colorCount++) % colors.length;
			evaluatorToColors.put(evaluator, colors[i]);
			if(evaluator.equals(getIdentity())) {
				evaluatorToNumbers.put(evaluator, "0");
			} else {
				evaluatorToNumbers.put(evaluator, Integer.toString(++evaluatorCount));
			}
		}
		
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
				Rubric rubric = (Rubric)element;
				EvaluationFormElementWrapper sliderWrapper;
				if(rubric.getSliders().size() > 2) {
					sliderWrapper = forgeRadarRubric((Rubric)element);
				} else {
					sliderWrapper = forgeRubric((Rubric)element);
				}
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
		List<EvaluationFormResponse> responses = identifierToResponses.get(element.getId());
		List<EvaluationFormElementWrapper> inputWrappers = new ArrayList<>(responses.size());
		for(EvaluationFormResponse response:responses) {
			if(StringHelper.containsNonWhitespace(response.getStringuifiedResponse())) {
				String initialValue = response.getStringuifiedResponse();
				if(initialValue != null) {
					initialValue = Formatter.stripTabsAndReturns(initialValue).toString();
				}
				Identity evaluator = response.getSession().getIdentity();
				String legend = getLegend(evaluator);
				String color = evaluatorToColors.get(evaluator);
		
				TextInputWrapper textInputWrapper = new TextInputWrapper(legend, color, initialValue, null);
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
	
	private EvaluationFormElementWrapper forgeRadarRubric(Rubric element) {
		EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(element);
		wrapper.setRadarOverview(true);
		List<Slider> sliders = element.getSliders();
		Map<EvaluationFormSession,RadarSeries> series = new HashMap<>(sliders.size());
		for(Slider slider:sliders) {
			String axis;
			if(StringHelper.containsNonWhitespace(slider.getStartLabel())) {
				axis = slider.getStartLabel();
			} else if(StringHelper.containsNonWhitespace(slider.getEndLabel())) {
				axis = slider.getEndLabel();
			} else {
				axis = "";
			}
			String responseIdentifier = slider.getId();
			List<EvaluationFormResponse> responses = identifierToResponses.get(responseIdentifier);
			
			for(EvaluationFormResponse response:responses) {
				EvaluationFormSession responseSession = response.getSession();
				if(!series.containsKey(responseSession)) {
					Identity identity = responseSession.getIdentity();
					String legend = getLegend(identity);
					String color = evaluatorToColors.get(identity);
					series.put(responseSession, new RadarSeries(legend, color));
				}
				if(response.getNumericalResponse() != null ) {
					double value = response.getNumericalResponse().doubleValue();
					series.get(responseSession).addPoint(axis, value);
				}
			}
		}
		
		String id = "radar_" + (count++);
		RadarChartElement radarEl = new RadarChartElement(id);
		radarEl.setSeries(new ArrayList<>(series.values()));
		radarEl.setShowLegend(true);
		if(element.getSliderType() == SliderType.discrete || element.getSliderType() == SliderType.discrete_slider) {
			radarEl.setLevels(element.getSteps());
			radarEl.setMaxValue(element.getSteps());
			radarEl.setFormat(Format.integer);
		} else if(element.getSliderType() == SliderType.continuous) {
			radarEl.setLevels(10);
			radarEl.setMaxValue(100);
			radarEl.setFormat(Format.integer);
		}
		wrapper.setRadarEl(radarEl);
		flc.add(id, radarEl);
		return wrapper;
	}
	
	private SliderWrapper forgeSliderStats(Slider slider, Rubric element, List<EvaluationFormResponse> responses) {
		String id = "overview_" + (count++);
		SliderOverviewElement overviewEl = new SliderOverviewElement(id);
		overviewEl.setMinValue(element.getStart());
		overviewEl.setMaxValue(element.getEnd());
		flc.add(id, overviewEl);
		
		List<SliderPoint> values = new ArrayList<>();
		if(responses != null && responses.size() > 0) {
			for(EvaluationFormResponse response:responses) {
				if(response.getNumericalResponse() != null) {
					Identity evaluator = response.getSession().getIdentity();
					String color = evaluatorToColors.get(evaluator);
					double value = response.getNumericalResponse().doubleValue();
					values.add(new SliderPoint(color, value));
				}
			}
		}
		overviewEl.setValues(values);
		
		return new SliderWrapper(slider, overviewEl);
	}
	
	private String getLegend(Identity identity) {
		String legend;
		if(identity.equals(getIdentity())) {
			legend = userManager.getUserDisplayName(identity);
		} else {
			String nr = evaluatorToNumbers.get(identity);
			legend = translate("evaluator", new String[]{ nr });
		}
		return legend;
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
