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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.rating.RatingFormItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.BlockLayoutClassFactory;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.handler.RubricHandler;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.Progress;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricController extends FormBasicController implements EvaluationFormResponseController {
	
	private static final String NO_RESPONSE_EL_PREFIX = "no_resp_";
	private static final String NO_RESPONSE_KEY = "enabled";
	private static final String[] NO_RESPONSE_KEYS = new String[] { NO_RESPONSE_KEY };
	
	private final Rubric rubric;
	private List<SliderWrapper> sliderWrappers;
	private Map<String, EvaluationFormResponse> rubricResponses = new HashMap<>();
	private Map<String, EvaluationFormResponse> commentResponses = new HashMap<>();
	private boolean validationEnabled = true;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public RubricController(UserRequest ureq, WindowControl wControl, Rubric rubric) {
		super(ureq, wControl, "rubric");
		this.rubric = rubric;
		initForm(ureq);
		setBlockLayoutClass(rubric.getLayoutSettings());
	}
	
	public RubricController(UserRequest ureq, WindowControl wControl, Rubric rubric, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "rubric", rootForm);
		this.rubric = rubric;
		initForm(ureq);
		setBlockLayoutClass(rubric.getLayoutSettings());
	}

	private void setBlockLayoutClass(BlockLayoutSettings layoutSettings) {
		flc.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(layoutSettings, true));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateForm();
	}
	
	public void updateForm() {
		RubricWrapper wrapper = new RubricWrapper(rubric);
		List<Slider> sliders = rubric.getSliders();
		sliderWrappers = new ArrayList<>(sliders.size());
		for(Slider slider:sliders) {
			SliderType type = rubric.getSliderType();
			SliderWrapper sliderWrapper = null;
			if(type == SliderType.discrete) {
				sliderWrapper = forgeDiscreteRadioButtons(slider, rubric);
			} else if(type == SliderType.discrete_star) {
				sliderWrapper = forgeDiscreteStar(slider, rubric, wrapper);
			} else if(type == SliderType.discrete_slider) {
				sliderWrapper = forgeDiscreteSlider(slider, rubric);
			} else if(type == SliderType.continuous) {
				sliderWrapper = forgeContinuousSlider(slider, rubric);
			}
			
			if(sliderWrapper != null) {
				sliderWrappers.add(sliderWrapper);
			}
		}
		wrapper.setSliders(sliderWrappers);
		flc.contextPut("element", wrapper);
	}

	private SliderWrapper forgeContinuousSlider(Slider slider, Rubric element) {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + CodeHelper.getRAMUniqueID(), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(element.getStart());
		sliderEl.setMaxValue(element.getEnd());
		sliderEl.addActionListener(FormEvent.ONCHANGE);
		SingleSelection noResponseEl = createNoResponseEl(element);
		SliderWrapper sliderWrapper = new SliderWrapper(slider, sliderEl, noResponseEl, createSliderCommentEl(element));
		sliderEl.setUserObject(sliderWrapper);
		if (noResponseEl != null) {
			noResponseEl.setUserObject(sliderWrapper);
		}
		return sliderWrapper;
	}
	
	private SliderWrapper forgeDiscreteStar(Slider slider, Rubric element, RubricWrapper wrapper) {
		RatingFormItem sliderEl = uifactory.addRatingItem("slider_" + CodeHelper.getRAMUniqueID(), null, 0, rubric.getSteps(), true, flc);
		String cssClass = "o_slider_star";
		if ((wrapper.isStepLabels() && element.isNoResponseEnabled()) || wrapper.isRightLabels()) {
			cssClass += " o_slider_star_distributed";
		}
		sliderEl.setCssClass(cssClass);
		sliderEl.setExplanation(null);
		setStarLavelLabels(sliderEl, wrapper);
		sliderEl.addActionListener(FormEvent.ONCHANGE);
		SingleSelection noResponseEl = createNoResponseEl(element);
		SliderWrapper sliderWrapper = new SliderWrapper(slider, sliderEl, noResponseEl, createSliderCommentEl(element));
		sliderEl.setUserObject(sliderWrapper);
		if (noResponseEl != null) {
			noResponseEl.setUserObject(sliderWrapper);
		}
		return sliderWrapper;
	}

	private void setStarLavelLabels(RatingFormItem sliderEl, RubricWrapper wrapper) {
		sliderEl.setTranslateRatingLabels(false);
		List<String> stepLabels = wrapper.getStepLabels();
		for (int i=0; i < sliderEl.getMaxRating(); i++) {
			if (stepLabels.size() > i) {
				String label = stepLabels.get(i);
				if (!StringHelper.containsNonWhitespace(label)) {
					label = null;
				}
				sliderEl.setLevelLabel(i, label);
			}
		}
	}
	
	private SliderWrapper forgeDiscreteSlider(Slider slider, Rubric element) {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + CodeHelper.getRAMUniqueID(), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(element.getStart());
		sliderEl.setMaxValue(element.getEnd());
		sliderEl.setStep(1);
		sliderEl.addActionListener(FormEvent.ONCHANGE);
		SingleSelection noResponseEl = createNoResponseEl(element);
		SliderWrapper sliderWrapper = new SliderWrapper(slider, sliderEl, noResponseEl, createSliderCommentEl(element));
		sliderEl.setUserObject(sliderWrapper);
		if (noResponseEl != null) {
			noResponseEl.setUserObject(sliderWrapper);
		}
		return sliderWrapper;
	}

	private SliderWrapper forgeDiscreteRadioButtons(Slider slider, Rubric element) {
		int start = element.getStart();
		int end = element.getEnd();
		int steps = element.getSteps();
		
		SelectionValues stepsSV = new SelectionValues();
		
		double[] theSteps = new double[steps];
		double step = (end - start + 1) / (double)steps;
		for(int i=0; i<steps; i++) {
			theSteps[i] = start + (i * step);
			String value = null;
			String description = null;
			String cssClass = null;
			
			if (rubric.isSliderStepLabelsEnabled()) {
				if (rubric.getStepLabels() != null && rubric.getStepLabels().size() > i) {
					String label = rubric.getStepLabels().get(i).getLabel();
					if (StringHelper.containsNonWhitespace(label)) {
						value = label;
					}
				}
				if (slider.getStepLabels() != null && slider.getStepLabels().size() > i) {
					String label = slider.getStepLabels().get(i).getLabel();
					if (StringHelper.containsNonWhitespace(label)) {
						description = label;
					}
				}
				RubricRating rating = evaluationFormManager.getRubricRating(element, Double.valueOf(i + 1));
				cssClass = RubricAvgRenderer.getRatingCssClass(rating);
			} else {
				value = "";
			}
			
			stepsSV.add(SelectionValues.entry(Double.toString(theSteps[i]), value, description, null, cssClass, true));
		}
		
		SingleSelection radioEl = null;
		if (rubric.isSliderStepLabelsEnabled()) {
			radioEl = uifactory.addCardSingleSelectHorizontal("slider_" + CodeHelper.getRAMUniqueID(), null, flc, stepsSV);
		} else {
			radioEl = uifactory.addRadiosVertical("slider_" + CodeHelper.getRAMUniqueID(), null, flc, stepsSV.keys(), stepsSV.values());
			radioEl.setDomReplacementWrapperRequired(false);
		}
		radioEl.setAllowNoSelection(true);
		radioEl.addActionListener(FormEvent.ONCHANGE);
		
		SingleSelection noResponseEl = createNoResponseEl(element);
		
		SliderWrapper sliderWrapper = new SliderWrapper(slider, radioEl, noResponseEl, createSliderCommentEl(element));
		radioEl.setUserObject(sliderWrapper);
		if (noResponseEl != null) {
			noResponseEl.setUserObject(sliderWrapper);
		}
		return sliderWrapper;
	}

	private SingleSelection createNoResponseEl(Rubric element) {
		SingleSelection noResponseEl = null;
		if (element.isNoResponseEnabled()) {
			noResponseEl = uifactory.addRadiosVertical(NO_RESPONSE_EL_PREFIX + CodeHelper.getRAMUniqueID(), null, flc,
					NO_RESPONSE_KEYS, getNoResponseValue());
			noResponseEl.setAllowNoSelection(true);
			noResponseEl.setEscapeHtml(false);
			noResponseEl.setDomReplacementWrapperRequired(false);
			noResponseEl.addActionListener(FormEvent.ONCHANGE);
		}
		return noResponseEl;
	}
	
	private String[] getNoResponseValue() {
		return new String[] { "&nbsp;<span class='o_evaluation_no_resp_value'>" + getTranslator().translate("no.response") + "</span>"};
	}
	
	private TextAreaElement createSliderCommentEl(Rubric element) {
		TextAreaElement sliderCommentEl = null;
		if (element.isSliderCommentsEnabled()) {
			sliderCommentEl = uifactory.addTextAreaElement("o_slider_comment_" + CodeHelper.getRAMUniqueID(),
					"slider.comment", 500, 2, 72, false, false, true, null, flc);
			sliderCommentEl.setDomReplacementWrapperRequired(false);
		}
		return sliderCommentEl;
	}

	private void updateCommentUI(SliderWrapper sliderWrapper) {
		if (sliderWrapper.getSliderCommentEl() == null) {
			return;
		}
		
		boolean placeholder = false;
		BigDecimal value = getValue(sliderWrapper);
		if (value != null) {
			RubricRating rating = evaluationFormManager.getRubricRating(rubric, value.doubleValue());
			if (RubricRating.INSUFFICIENT == rating) {
				placeholder = true;
			}
		}
		if (placeholder) {
			sliderWrapper.getSliderCommentEl().setPlaceholderKey("slider.comment.placeholder", null);
		} else {
			sliderWrapper.getSliderCommentEl().setPlaceholderText(null);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	public void setValidationEnabled(boolean enabled) {
		this.validationEnabled = enabled;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		flc.contextRemove("errorMandatory");
		if (!validationEnabled) return true;
		
		boolean allOk = super.validateFormLogic(ureq);
		
		if (rubric.isMandatory() && !areAllSlidersFilledIn()) {
			flc.contextPut("errorMandatory", Boolean.TRUE);
			allOk = false;
		}
		
		return allOk;
	}

	private boolean areAllSlidersFilledIn() {
		for (SliderWrapper sliderWrapper: sliderWrappers) {
			if (!isFilledOut(sliderWrapper)) {
				return false;
			}
		}
		return true;
	}

	private boolean isFilledOut(SliderWrapper sliderWrapper) {
		return isRadioFilledIn(sliderWrapper) || isStarFilledIn(sliderWrapper) || isSliderFilledIn(sliderWrapper) || isNoResponseFilledId(sliderWrapper);
	}

	private boolean isRadioFilledIn(SliderWrapper sliderWrapper) {
		SingleSelection radioEl = sliderWrapper.getRadioEl();
		return radioEl != null && radioEl.isOneSelected();
	}

	private boolean isStarFilledIn(SliderWrapper sliderWrapper) {
		RatingFormItem starEl = sliderWrapper.getStarEl();
		return starEl != null && starEl.getCurrentRating() > 0;
	}

	private boolean isSliderFilledIn(SliderWrapper sliderWrapper) {
		SliderElement sliderEl = sliderWrapper.getSliderEl();
		return sliderEl != null && sliderEl.hasValue();
	}

	private boolean isNoResponseFilledId(SliderWrapper sliderWrapper) {
		SingleSelection noResponseEl = sliderWrapper.getNoResponseEl();
		return noResponseEl != null && noResponseEl.isOneSelected();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof ChangePartEvent) {
			updateForm();
			setBlockLayoutClass(rubric.getLayoutSettings());
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof SingleSelection || source instanceof RatingFormItem || source instanceof SliderElement) {
			Object uobject = source.getUserObject();
			if (uobject instanceof SliderWrapper sliderWrapper && sliderWrappers.contains(sliderWrapper)) {
				if (source.getName().startsWith(NO_RESPONSE_EL_PREFIX)) {
					// No response was clicked.
					if (sliderWrapper.getRadioEl() != null && sliderWrapper.getRadioEl().isOneSelected()) {
						String selectedKey = sliderWrapper.getRadioEl().getSelectedKey();
						sliderWrapper.getRadioEl().select(selectedKey, false);
					}
					if (sliderWrapper.getStarEl() != null && sliderWrapper.getStarEl().getCurrentRating() > 0) {
						sliderWrapper.getStarEl().setCurrentRating(0);
					}
					if (sliderWrapper.getSliderEl() != null) {
						sliderWrapper.getSliderEl().deleteValue();
					}
				} else {
					// Slider was clicked.
					SingleSelection noResponseEl = sliderWrapper.getNoResponseEl();
					if (noResponseEl != null) {
						noResponseEl.select(NO_RESPONSE_KEY, false);
					}
				}
				updateCommentUI(sliderWrapper);
				flc.setDirty(true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		for (SliderWrapper sliderWrapper: sliderWrappers) {
			sliderWrapper.getFormItem().setEnabled(!readOnly);
			if (sliderWrapper.getNoResponseEl() != null) {
				sliderWrapper.getNoResponseEl().setEnabled(!readOnly);
			}
			if (sliderWrapper.getSliderCommentEl()!= null) {
				sliderWrapper.getSliderCommentEl().setEnabled(!readOnly);
			}
		}
	}

	@Override
	public boolean hasResponse() {
		// All sliders have to be filled out
		for (SliderWrapper sliderWrapper: sliderWrappers) {
			if (!rubricResponses.containsKey(sliderWrapper.getId())) {
				return false;
			}
			EvaluationFormResponse response = rubricResponses.get(sliderWrapper.getId());
			if (response == null || (!response.isNoResponse()) && response.getNumericalResponse() == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void initResponse(UserRequest ureq, EvaluationFormSession session, EvaluationFormResponses responses) {
		for (SliderWrapper sliderWrapper: sliderWrappers) {
			EvaluationFormResponse response = responses.getResponse(session, sliderWrapper.getId());
			if (response != null) {
				rubricResponses.put(sliderWrapper.getId(), response);
				if (response.getNumericalResponse() != null) {
					BigDecimal numericalResponse = response.getNumericalResponse();
					setValue(sliderWrapper, numericalResponse);
				} else if (response.isNoResponse()) {
					SingleSelection noResponseEl = sliderWrapper.getNoResponseEl();
					if (noResponseEl != null) {
						noResponseEl.select(NO_RESPONSE_KEY, true);
					}
				}
			}
			if (sliderWrapper.isSliderCommentEnabled()) {
				EvaluationFormResponse commentResponse = responses.getResponse(session, sliderWrapper.getSliderCommentId());
				if (commentResponse != null) {
					commentResponses.put(sliderWrapper.getSliderCommentId(), commentResponse);
					sliderWrapper.getSliderCommentEl().setValue(commentResponse.getStringuifiedResponse());
				}
			}
		}
	}

	private void setValue(SliderWrapper sliderWrapper, BigDecimal numericalResponse) {
		if (sliderWrapper.getRadioEl() != null) {
			setValue(sliderWrapper.getRadioEl(), numericalResponse);
		} else if (sliderWrapper.getStarEl() != null) {
			sliderWrapper.getStarEl().setCurrentRating(numericalResponse.floatValue());
		} else if (sliderWrapper.getSliderEl() != null) {
			setValue(sliderWrapper.getSliderEl(), numericalResponse);
		}
		updateCommentUI(sliderWrapper);
	}

	private void setValue(SingleSelection radioEl, BigDecimal numericalResponse) {
		int start = rubric.getStart();
		int end = rubric.getEnd();
		int steps = rubric.getSteps();
		
		double[] theSteps = new double[steps];
		String[] theKeys = new String[steps];
		
		double step = (end - start + 1) / (double) steps;
		for(int i=0; i<steps; i++) {
			theSteps[i] = start + (i * step);
			theKeys[i] = Double.toString(theSteps[i]);
		}
		
		double val = numericalResponse.doubleValue();
		double error = step / 10.0d;
		for (int i = 0; i < theSteps.length; i++) {
			double theStep = theSteps[i];
			double margin = Math.abs(theStep - val);
			if (margin < error) {
				radioEl.select(theKeys[i], true);
			}
		}
	}

	private void setValue(SliderElement sliderEl, BigDecimal numericalResponse) {
		double val = numericalResponse.doubleValue();
		sliderEl.setValue(val);
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		for (SliderWrapper sliderWrapper: sliderWrappers) {
			boolean noResponseSelected = sliderWrapper.getNoResponseEl() != null && sliderWrapper.getNoResponseEl().isOneSelected();
			if (noResponseSelected) {
				saveNoResponse(session, sliderWrapper);
			} else {
				saveSliderResponse(session, sliderWrapper);
			}
		}
	}
	
	private void saveNoResponse(EvaluationFormSession session, SliderWrapper sliderWrapper) {
		EvaluationFormResponse response = rubricResponses.get(sliderWrapper.getId());
		if (response == null) {
			response = evaluationFormManager.createNoResponse(sliderWrapper.getId(), session);
		} else {
			response = evaluationFormManager.updateNoResponse(response);
		}
		rubricResponses.put(sliderWrapper.getId(), response);
	}
	
	private void saveSliderResponse(EvaluationFormSession session, SliderWrapper sliderWrapper) {
		BigDecimal value = getValue(sliderWrapper);
		if (value != null) {
			EvaluationFormResponse response = rubricResponses.get(sliderWrapper.getId());
			if (response == null) {
				response = evaluationFormManager.createNumericalResponse(sliderWrapper.getId(), session, value);
			} else {
				response = evaluationFormManager.updateNumericalResponse(response, value);
			}
			rubricResponses.put(sliderWrapper.getId(), response);
		} else {
			EvaluationFormResponse response = rubricResponses.get(sliderWrapper.getId());
			if (response != null) {
				evaluationFormManager.deleteResponse(response);
				rubricResponses.remove(sliderWrapper.getId());
			}
		}
		
		
		EvaluationFormResponse commentResponse = commentResponses.get(sliderWrapper.getSliderCommentId());
		if (sliderWrapper.isSliderCommentEnabled()) {
			String comment = sliderWrapper.getSliderCommentEl().getValue();
			if (StringHelper.containsNonWhitespace(comment)) {
				if (commentResponse == null) {
					commentResponse = evaluationFormManager.createStringResponse(sliderWrapper.getSliderCommentId(), session, comment);
				} else {
					commentResponse = evaluationFormManager.updateStringResponse(commentResponse, comment);
				}
			} else if (commentResponse != null) {
				evaluationFormManager.deleteResponse(commentResponse);
				commentResponses.remove(sliderWrapper.getSliderCommentId());
			}
		} else if (commentResponse != null) {
			evaluationFormManager.deleteResponse(commentResponse);
			commentResponses.remove(sliderWrapper.getSliderCommentId());
		}
	}

	private BigDecimal getValue(SliderWrapper sliderWrapper) {
		BigDecimal value = null;
		SliderElement slider = sliderWrapper.getSliderEl();
		if (slider != null && slider.hasValue()) {
			value = BigDecimal.valueOf(slider.getValue());
		} else if (sliderWrapper.getStarEl() != null && sliderWrapper.getStarEl().getCurrentRating() > 0) {
			value = BigDecimal.valueOf(sliderWrapper.getStarEl().getCurrentRating());
		} else {
			SingleSelection radioEl = sliderWrapper.getRadioEl();
			if (radioEl != null && radioEl.isOneSelected()) {
				String selectedKey = radioEl.getSelectedKey();
				if (StringHelper.containsNonWhitespace(selectedKey)) {
					value = new BigDecimal(selectedKey);
				}
			}
		}
		return value;
	}
	
	@Override
	public void deleteResponse(EvaluationFormSession session) {
		if (rubricResponses != null) {
			rubricResponses.values().forEach(response -> evaluationFormManager.deleteResponse(response));
			rubricResponses = new HashMap<>();
		}
		if (commentResponses != null) {
			commentResponses.values().forEach(response -> evaluationFormManager.deleteResponse(response));
			commentResponses = new HashMap<>();
		}
	}

	@Override
	public Progress getProgress() {
		int max = sliderWrappers.size();
		int current = rubricResponses.size();
		return Progress.of(current, max);
	}
	
	public static final class RubricWrapper {

		private final Rubric rubric;
		private List<SliderWrapper> sliders;

		public RubricWrapper(Rubric rubric) {
			this.rubric = rubric;
		}
		
		public boolean showName() {
			return rubric.getNameDisplays().contains(NameDisplay.execution);
		}
		
		public String getName() {
			return rubric.getName();
		}
		
		public boolean isDiscreteRubric() {
			return rubric.getSliderType() == SliderType.discrete && !rubric.isSliderStepLabelsEnabled();
		}
		
		public boolean isDiscreteCardRubric() {
			return rubric.getSliderType() == SliderType.discrete && rubric.isSliderStepLabelsEnabled();
		}
		
		public boolean isDiscreteStarRubric() {
			return rubric.getSliderType() == SliderType.discrete_star;
		}
		
		public boolean isUndistributedStar() {
			if (isDiscreteStarRubric()) {
				return !isTopLabelRow();
			}
			return true;
		}
		
		public boolean isDiscreteSliderRubric() {
			return rubric.getSliderType() == SliderType.discrete_slider;
		}
		
		public boolean isNoResponseEnabled() {
			return rubric.isNoResponseEnabled();
		}
		

		public static int getWidthInPercent(Rubric theRubric) {
			if(theRubric.getSliderType() == SliderType.discrete) {
				int steps = theRubric.getSteps();
				int stepInPercent = Math.round(100.0f / steps) - 1;
				return stepInPercent;
			}
			return 0;
		}
		
		public int getStepInPercent() {
			return getWidthInPercent(rubric);
		}
		
		public boolean isTopLabelRow() {
			if (isDiscreteRubric() || isDiscreteSliderRubric()) {
				return isStepLabels() || isNoResponseEnabled();
			} else if (isDiscreteStarRubric()) {
				return isStepLabels() && isNoResponseEnabled();
			}
			return false;
		}
		
		public boolean isStepLabels() {
			if(rubric.getStepLabels() == null || rubric.getStepLabels().isEmpty()) {
				return false;
			}
			
			List<StepLabel> stepLabels = rubric.getStepLabels();
			for(StepLabel stepLabel:stepLabels) {
				if(stepLabel != null && StringHelper.containsNonWhitespace(stepLabel.getLabel())) {
					return true;
				}
			}
			return false;
		}
		
		public boolean isLeftLabels() {
			List<Slider> rubricSliders = rubric.getSliders();
			if(rubricSliders != null && rubricSliders.size() > 0) {
				for(Slider slider:rubricSliders) {
					if(slider != null && StringHelper.containsNonWhitespace(slider.getStartLabel())) {
						return true;
					}
				}
			}
			return false;
		}
		
		public boolean isRightLabels() {
			List<Slider> rubricSliders = rubric.getSliders();
			if(rubricSliders != null && rubricSliders.size() > 0) {
				for(Slider slider:rubricSliders) {
					if(slider != null && StringHelper.containsNonWhitespace(slider.getEndLabel())) {
						return true;
					}
				}
			}	
			return false;
		}
		
		public List<String> getStepLabels() {
			if(rubric.getStepLabels() != null && rubric.getStepLabels().size() > 0) {
				List<String> stepLabels = new ArrayList<>(rubric.getStepLabels().size());
				for(StepLabel stepLabel:rubric.getStepLabels()) {
					stepLabels.add(stepLabel.getLabel());
				}
				return stepLabels;
			}
			return new ArrayList<>(1);
		}

		public List<SliderWrapper> getSliders() {
			return sliders;
		}

		public void setSliders(List<SliderWrapper> sliders) {
			this.sliders = sliders;
		}
	}
	
	public final static class SliderWrapper {

		private final Slider slider;
		private final SliderElement sliderEl;
		private final RatingFormItem starEl;
		private final SingleSelection radioEl;
		private final SingleSelection noResponseEl;
		private final String sliderCommentId;
		private final TextAreaElement sliderCommentEl;
		
		public SliderWrapper(Slider slider, SingleSelection radioEl, SingleSelection noResponseEl, TextAreaElement sliderCommentEl) {
			this(slider, radioEl, null, null, noResponseEl, sliderCommentEl);
		}
		
		public SliderWrapper(Slider slider, RatingFormItem starEl, SingleSelection noResponseEl, TextAreaElement sliderCommentEl) {
			this(slider, null, starEl, null, noResponseEl, sliderCommentEl);
		}
		
		public SliderWrapper(Slider slider, SliderElement sliderEl, SingleSelection noResponseEl, TextAreaElement sliderCommentEl) {
			this(slider, null, null, sliderEl, noResponseEl, sliderCommentEl);
		}
		
		private SliderWrapper(Slider slider, SingleSelection radioEl, RatingFormItem starEl, SliderElement sliderEl,
				SingleSelection noResponseEl, TextAreaElement sliderCommentEl) {
			this.slider = slider;
			this.radioEl = radioEl;
			this.starEl = starEl;
			this.sliderEl = sliderEl;
			this.noResponseEl = noResponseEl;
			this.sliderCommentId = RubricHandler.getSliderCommentId(slider);
			this.sliderCommentEl = sliderCommentEl;
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
			if (radioEl != null) {
				return radioEl;
			} else if (starEl != null) {
				return starEl;
			}
			return sliderEl;
		}
		
		public SingleSelection getRadioEl() {
			return radioEl;
		}
		
		public RatingFormItem getStarEl() {
			return starEl;
		}

		public SliderElement getSliderEl() {
			return sliderEl;
		}
		
		public SingleSelection getNoResponseEl() {
			return noResponseEl;
		}
		
		public boolean isSliderCommentEnabled() {
			return sliderCommentEl != null;
		}

		public String getSliderCommentId() {
			return sliderCommentId;
		}

		public TextAreaElement getSliderCommentEl() {
			return sliderCommentEl;
		}
		
		public String getSliderCommentElName() {
			return sliderCommentEl == null? null: sliderCommentEl.getComponent().getComponentName();
		}
		
	}

}
