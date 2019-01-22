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

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricEditorController extends FormBasicController implements PageElementEditorController {
	
	private static final String GOOD_RATING_END_KEY = "rubric.good.rating.end";
	private static final String GOOD_RATING_START_KEY = "rubric.good.rating.start";
	private final String[] GOOD_RATING_KEYS = new String[] {
			GOOD_RATING_END_KEY, GOOD_RATING_START_KEY
	};

	private static AtomicInteger count = new AtomicInteger();
	private final Rubric rubric;
	private boolean editMode = false;
	private final boolean restrictedEdit;
	private RubricController rubricCtrl;
	
	private final String[] sliderTypeKeys = new String[] { SliderType.discrete.name(), SliderType.discrete_slider.name(), SliderType.continuous.name() };
	private final String[] sliderStepKeys = new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10" };
	private final String[] showResponseKey = new String[] { "show.no.response" };
	
	private List<StepLabelColumn> stepLabels = new ArrayList<>();
	private List<SliderRow> sliders = new ArrayList<>();
	
	private FormLink saveButton;
	private SingleSelection sliderTypeEl;
	private SingleSelection scaleTypeEl;
	private TextElement nameEl;
	private SingleSelection stepsEl;
	private MultipleSelectionElement noAnswerEl;
	private TextElement lowerBoundInsufficientEl;
	private TextElement upperBoundInsufficientEl;
	private TextElement lowerBoundNeutralEl;
	private TextElement upperBoundNeutralEl;
	private TextElement lowerBoundSufficientEl;
	private TextElement upperBoundSufficientEl;
	private SingleSelection goodRatingEl;
	private FormLink addSliderButton;
	private FormLayoutContainer settingsLayout;
	
	public RubricEditorController(UserRequest ureq, WindowControl wControl, Rubric rubric, boolean restrictedEdit) {
		super(ureq, wControl, "rubric_editor");
		this.rubric = rubric;
		this.restrictedEdit = restrictedEdit;

		initForm(ureq);
		setEditMode(editMode);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		rubricCtrl = new RubricController(ureq, getWindowControl(), rubric, mainForm);
		listenTo(rubricCtrl);
		formLayout.add("rubric", rubricCtrl.getInitialFormItem());
		
		settingsLayout = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		settingsLayout.setRootForm(mainForm);
		formLayout.add("settings", settingsLayout);

		nameEl = uifactory.addTextElement("rubric.name", 128, rubric.getName(), settingsLayout);
		
		String[] sliderTypeValues = new String[] { translate("slider.discrete"), translate("slider.discrete.slider"), translate("slider.continuous") };
		sliderTypeEl = uifactory.addDropdownSingleselect("slider.type." + count.incrementAndGet(), "slider.type", settingsLayout, sliderTypeKeys, sliderTypeValues, null);
		sliderTypeEl.addActionListener(FormEvent.ONCHANGE);
		sliderTypeEl.setEnabled(!restrictedEdit);
		boolean typeSelected = false;
		if(rubric != null && rubric.getSliderType() != null) {
			for(String sliderTypeKey:sliderTypeKeys) {
				if(sliderTypeKey.equals(rubric.getSliderType().name())) {
					sliderTypeEl.select(sliderTypeKey, true);
					typeSelected = true;
				}
			}
		}
		if(!typeSelected) {
			sliderTypeEl.select(sliderTypeKeys[0], true);
		}
		
		stepsEl = uifactory.addDropdownSingleselect("slider.steps." + count.incrementAndGet(), "slider.steps", settingsLayout, sliderStepKeys, sliderStepKeys, null);
		stepsEl.addActionListener(FormEvent.ONCHANGE);
		stepsEl.setEnabled(!restrictedEdit);
		boolean stepSelected = false;
		if(rubric != null && rubric.getSteps() > 0) {
			String steps = Integer.toString(rubric.getSteps());
			for(String sliderStepKey:sliderStepKeys) {
				if(sliderStepKey.equals(steps)) {
					stepsEl.select(sliderStepKey, true);
					stepSelected = true;
				}
			}
		}
		if(!stepSelected) {
			stepsEl.select(sliderStepKeys[4], true);
		}
		
		scaleTypeEl = uifactory.addDropdownSingleselect("scale.type." + count.incrementAndGet(), "rubric.scale.type",
				settingsLayout, ScaleType.getKeys(), ScaleType.getValues(getTranslator()), null);
		scaleTypeEl.addActionListener(FormEvent.ONCHANGE);
		scaleTypeEl.setEnabled(!restrictedEdit);
		boolean scaleSelected = false;
		if(rubric != null && rubric.getScaleType() != null) {
			for(String scaleTypeKey: ScaleType.getKeys()) {
				if(scaleTypeKey.equals(rubric.getScaleType().getKey())) {
					scaleTypeEl.select(scaleTypeKey, true);
					scaleSelected = true;
				}
			}
		}
		if(!scaleSelected) {
			scaleTypeEl.select(ScaleType.getKeys()[0], true);
		}
		
		noAnswerEl = uifactory.addCheckboxesVertical("no.response." + count.incrementAndGet(),
				"rubric.no.response.enabled", settingsLayout, showResponseKey,
				new String[] { translate("rubric.no.response.enabled.show") }, 1);
		noAnswerEl.select(showResponseKey[0], rubric.isNoResponseEnabled());
		noAnswerEl.setEnabled(!restrictedEdit);
		
		String insufficientPage = velocity_root + "/rubric_range_insufficient.html";
		FormLayoutContainer insufficientCont = FormLayoutContainer.createCustomFormLayout("insufficient",
				getTranslator(), insufficientPage);
		insufficientCont.setRootForm(mainForm);
		settingsLayout.add("insufficient", insufficientCont);
		insufficientCont.setLabel("rubric.insufficient", null);
		String insufficientLowerBound = rubric.getLowerBoundInsufficient() != null
				? String.valueOf(rubric.getLowerBoundInsufficient())
				: null;
		lowerBoundInsufficientEl = uifactory.addTextElement("rubric.lower.bound.insufficient", 4,
				insufficientLowerBound, insufficientCont);
		lowerBoundInsufficientEl.setDomReplacementWrapperRequired(false);
		lowerBoundInsufficientEl.setDisplaySize(4);
		String insufficientUpperBound = rubric.getUpperBoundInsufficient() != null
				? String.valueOf(rubric.getUpperBoundInsufficient())
				: null;
		upperBoundInsufficientEl = uifactory.addTextElement("rubric.upper.bound.insufficient", 4,
				insufficientUpperBound, insufficientCont);
		upperBoundInsufficientEl.setDomReplacementWrapperRequired(false);
		upperBoundInsufficientEl.setDisplaySize(4);

		String neutralPage = velocity_root + "/rubric_range_neutral.html";
		FormLayoutContainer neutralCont = FormLayoutContainer.createCustomFormLayout("neutral", getTranslator(),
				neutralPage);
		neutralCont.setRootForm(mainForm);
		settingsLayout.add("neutral", neutralCont);
		neutralCont.setLabel("rubric.neutral", null);
		String neutralLowerBound = rubric.getLowerBoundNeutral() != null ? String.valueOf(rubric.getLowerBoundNeutral())
				: null;
		lowerBoundNeutralEl = uifactory.addTextElement("rubric.lower.bound.neutral", 4, neutralLowerBound, neutralCont);
		lowerBoundNeutralEl.setDomReplacementWrapperRequired(false);
		lowerBoundNeutralEl.setDisplaySize(4);
		String neutralUpperBound = rubric.getUpperBoundNeutral() != null ? String.valueOf(rubric.getUpperBoundNeutral())
				: null;
		upperBoundNeutralEl = uifactory.addTextElement("rubric.upper.bound.neutral", 4, neutralUpperBound, neutralCont);
		upperBoundNeutralEl.setDomReplacementWrapperRequired(false);
		upperBoundNeutralEl.setDisplaySize(4);

		String sufficientPage = velocity_root + "/rubric_range_sufficient.html";
		FormLayoutContainer sufficientCont = FormLayoutContainer.createCustomFormLayout("sufficient", getTranslator(),
				sufficientPage);
		sufficientCont.setRootForm(mainForm);
		settingsLayout.add("sufficient", sufficientCont);
		sufficientCont.setLabel("rubric.sufficient", null);
		String sufficientLowerBound = rubric.getLowerBoundSufficient() != null
				? String.valueOf(rubric.getLowerBoundSufficient())
				: null;
		lowerBoundSufficientEl = uifactory.addTextElement("rubric.lower.bound.sufficient", 4, sufficientLowerBound,
				sufficientCont);
		lowerBoundSufficientEl.setDomReplacementWrapperRequired(false);
		lowerBoundSufficientEl.setDisplaySize(4);
		String sufficientUpperBound = rubric.getUpperBoundSufficient() != null
				? String.valueOf(rubric.getUpperBoundSufficient())
				: null;
		upperBoundSufficientEl = uifactory.addTextElement("rubric.upper.bound.sufficient", 4, sufficientUpperBound,
				sufficientCont);
		upperBoundSufficientEl.setDomReplacementWrapperRequired(false);
		upperBoundSufficientEl.setDisplaySize(4);
		
		goodRatingEl = uifactory.addDropdownSingleselect("rubric.good.rating" + count.incrementAndGet(), "rubric.good.rating",
				settingsLayout, GOOD_RATING_KEYS, translateAll(getTranslator(), GOOD_RATING_KEYS), null);
		if (rubric != null) {
			String goodRatingKey = rubric.isStartGoodRating()? GOOD_RATING_START_KEY: GOOD_RATING_END_KEY;
			goodRatingEl.select(goodRatingKey, true);
		}

		updateTypeSettings();
		updateSteps();
		
		for(Slider slider:rubric.getSliders()) {
			SliderRow row = forgeSliderRow(slider);
			sliders.add(row);
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("sliders", sliders);
		}
		
		long postfix = CodeHelper.getRAMUniqueID();
		saveButton = uifactory.addFormLink("save_" + postfix, "save", null, formLayout, Link.BUTTON);
		if(!restrictedEdit) {
			addSliderButton = uifactory.addFormLink("add.slider." + postfix, "add.slider", null, formLayout, Link.BUTTON);
			addSliderButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		}
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("postfix", Long.toString(postfix));
		}
	}
	
	private void updateSteps() {
		List<StepLabelColumn> stepLabelColumns = new ArrayList<>();
		if (stepsEl.isVisible() && stepsEl.isOneSelected()
				&& (sliderTypeEl.isSelected(0) || sliderTypeEl.isSelected(1))) {
			int steps = Integer.parseInt(stepsEl.getSelectedKey());
			for(int i=0; i<steps; i++) {
				Integer step = new Integer(i);
				StepLabelColumn col = stepLabels.size() > step? stepLabels.get(step): null;
				if(col == null) {
					String label = "";
					if(rubric.getStepLabels() != null && i<rubric.getStepLabels().size()) {
						label = rubric.getStepLabels().get(i).getLabel();
					}
					
					TextElement textEl = uifactory.addTextElement("steplabel_" + count.incrementAndGet(), "steplabel_" + count.incrementAndGet(), null, 256, label, flc);
					textEl.setDomReplacementWrapperRequired(false);
					textEl.setDisplaySize(4);
					col = new StepLabelColumn(i, textEl);
				}
				if (scaleTypeEl.isVisible() && scaleTypeEl.isOneSelected()) {
					String selectedScaleTypeKey = scaleTypeEl.getSelectedKey();
					ScaleType scaleType = ScaleType.getEnum(selectedScaleTypeKey);
					double stepValue = scaleType.getStepValue(steps, i + 1);
					col.setExampleText(String.valueOf(stepValue));
				} else {
					col.removeExampleText();
				}
				stepLabelColumns.add(col);
			}
			
			int stepInPercent = Math.round(90.0f / steps);//90 is empirically choose to not make a second line
			flc.contextPut("stepInPercent", stepInPercent);
		}
		stepLabels = stepLabelColumns;
		flc.contextPut("stepLabels", stepLabelColumns);
	}
	
	private void updateTypeSettings() {
		if(!sliderTypeEl.isOneSelected()) return;
		
		SliderType selectedType = SliderType.valueOf(sliderTypeEl.getSelectedKey());
		if(selectedType == SliderType.discrete || selectedType == SliderType.discrete_slider) {
			stepsEl.setVisible(true);
		} else if(selectedType == SliderType.continuous) {
			stepsEl.setVisible(false);
		}
	}
	
	private void updateSliders() {
		for (SliderRow row: sliders) {
			row.setSliderEl(createSliderEl());
		}
	}
	
	private SliderRow forgeSliderRow(Slider slider) {
		String startLabel = slider.getStartLabel();
		TextElement startLabelEl = uifactory.addTextElement("start.label." + count.incrementAndGet(), "start.label", 256, startLabel, flc);
		startLabelEl.setDomReplacementWrapperRequired(false);
		String endLabel = slider.getEndLabel();
		TextElement endLabelEl = uifactory.addTextElement("end.label." + count.incrementAndGet(), "end.label", 256, endLabel, flc);
		endLabelEl.setDomReplacementWrapperRequired(false);

		SliderRow row = new SliderRow(slider, startLabelEl, endLabelEl, createSliderEl());
		if(!restrictedEdit) {
			FormLink deleteButton = uifactory.addFormLink("del." + count.incrementAndGet(), "delete_slider", "", null, flc, Link.BUTTON | Link.NONTRANSLATED);
			deleteButton.setDomReplacementWrapperRequired(false);
			deleteButton.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
			deleteButton.setUserObject(row);
			row.setDeleteButton(deleteButton);
			flc.contextPut("deleteButtons", Boolean.TRUE);
		}
		return row;
	}

	private FormItem createSliderEl() {
		SliderType selectedType = SliderType.valueOf(sliderTypeEl.getSelectedKey());
		if (selectedType == SliderType.discrete) {
			return createRadioEl();
		} else if (selectedType == SliderType.discrete_slider) {
			return createDescreteSliderEl();
		}
		return createContinousSliderEl();
	}

	private FormItem createRadioEl() {
		int start = 1;
		int steps = Integer.parseInt(stepsEl.getSelectedKey());
		int end = 1 + steps;
		
		double[] theSteps = new double[steps];
		String[] theKeys = new String[steps];
		String[] theValues = new String[steps];
		
		double step = (end - start + 1) / (double)steps;
		for(int i=0; i<steps; i++) {
			theSteps[i] = start + (i * step);
			theKeys[i] = Double.toString(theSteps[i]);
			theValues[i] = "";
		}

		SingleSelection radioEl = uifactory.addRadiosVertical("slider_" + CodeHelper.getRAMUniqueID(), null, flc, theKeys, theValues);
		radioEl.setAllowNoSelection(true);
		radioEl.setDomReplacementWrapperRequired(false);
		int widthInPercent = Math.round(100.0f / steps) - 1;
		radioEl.setWidthInPercent(widthInPercent, true);
		return radioEl;
	}

	private FormItem createDescreteSliderEl() {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + CodeHelper.getRAMUniqueID(), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(1);
		sliderEl.setMaxValue( Integer.parseInt(stepsEl.getSelectedKey()));
		sliderEl.setStep(1);
		return sliderEl;
	}

	private FormItem createContinousSliderEl() {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + CodeHelper.getRAMUniqueID(), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(1);
		sliderEl.setMaxValue(100);
		return sliderEl;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		flc.getFormItemComponent().contextPut("editMode", Boolean.valueOf(editMode));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addSliderButton == source) {
			doAddSlider();
		} else if(sliderTypeEl == source) {
			updateTypeSettings();
			updateSteps();
			updateSliders();
		} else if(stepsEl == source) {
			updateSteps();
			updateSliders();
		} else if (scaleTypeEl == source) {
			updateSteps();
			updateSliders();
		} else if(saveButton == source) {
			if(validateFormLogic(ureq)) {
				formOK(ureq);
			}
		} else if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			if("delete_slider".equals(button.getCmd())) {
				doRemoveSlider((SliderRow)button.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doRemoveSlider(SliderRow row) {
		updateSteps();
		sliders.remove(row);
		rubric.getSliders().remove(row.getSlider());
		flc.setDirty(true);
	}
	
	private void doAddSlider() {
		Slider slider = new Slider();
		slider.setId(UUID.randomUUID().toString());
		rubric.getSliders().add(slider);
		SliderRow row = forgeSliderRow(slider);
		sliders.add(row);
		flc.setDirty(true);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		sliderTypeEl.clearError();
		if(!sliderTypeEl.isOneSelected()) {
			sliderTypeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		double min = -101;
		double max = 101;
		int steps = 100;
		String selectedSliderType = sliderTypeEl.getSelectedKey();
		SliderType sliderType =  SliderType.valueOf(selectedSliderType);
		String selectedScaleTypeKey = scaleTypeEl.getSelectedKey();
		ScaleType scaleType = ScaleType.getEnum(selectedScaleTypeKey);
		if (sliderType != SliderType.continuous) {
			steps = Integer.parseInt(stepsEl.getSelectedKey());
		}
		min = scaleType.getStepValue(steps, 1);
		max = scaleType.getStepValue(steps, steps);
		if (min > max) {
			double temp = min;
			min = max;
			max = temp;
		}
		lowerBoundInsufficientEl.clearError();
		if (isInvalidDouble(lowerBoundInsufficientEl.getValue(), min, max)
				|| isInvalidDouble(upperBoundInsufficientEl.getValue(), min, max)
				|| isOnlyOnePresent(lowerBoundInsufficientEl, upperBoundInsufficientEl)) {
			lowerBoundInsufficientEl.setErrorKey("error.outside.range",
					new String[] { String.valueOf(min), String.valueOf(max) });
			allOk = false;
		}
		lowerBoundNeutralEl.clearError();
		if (isInvalidDouble(lowerBoundNeutralEl.getValue(), min, max)
				|| isInvalidDouble(upperBoundNeutralEl.getValue(), min, max)
				|| isOnlyOnePresent(lowerBoundNeutralEl, upperBoundNeutralEl)) {
			lowerBoundNeutralEl.setErrorKey("error.outside.range",
					new String[] { String.valueOf(min), String.valueOf(max) });
			allOk = false;
		}
		lowerBoundSufficientEl.clearError();
		if (isInvalidDouble(lowerBoundSufficientEl.getValue(), min, max)
				|| isInvalidDouble(upperBoundSufficientEl.getValue(), min, max)
				|| isOnlyOnePresent(lowerBoundSufficientEl, upperBoundSufficientEl)) {
			lowerBoundSufficientEl.setErrorKey("error.outside.range",
					new String[] { String.valueOf(min), String.valueOf(max) });
			allOk = false;
		}

		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean isOnlyOnePresent(TextElement element1, TextElement element2) {
		boolean present1 = StringHelper.containsNonWhitespace(element1.getValue());
		boolean present2 = StringHelper.containsNonWhitespace(element2.getValue());
		if (present1 && !present2) return true;
		if (!present1 && present2) return true;
		return false;
	}

	private boolean isInvalidDouble(String val, double min, double max) {
		boolean inside = true;
		if (StringHelper.containsNonWhitespace(val)) {
			try {
				double value = Double.parseDouble(val);
				if(min > value) {
					inside =  false;
				} else if(max < value) {
					inside =  false;
				}
			} catch (NumberFormatException e) {
				inside =  false;
			}
		}
		return !inside;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		commitFields();
		commitStepLabels();
		
		rubric.setName(nameEl.getValue());

		String selectedSliderType = sliderTypeEl.getSelectedKey();
		SliderType sliderType =  SliderType.valueOf(selectedSliderType);
		rubric.setSliderType(sliderType);
		if(sliderType == SliderType.continuous) {
			rubric.setStart(1);
			rubric.setEnd(100);
			rubric.setSteps(100);
		} else {
			int steps = Integer.parseInt(stepsEl.getSelectedKey());
			rubric.setStart(1);
			rubric.setEnd(steps);
			rubric.setSteps(steps);
		}
		
		String selectedScaleTypeKey = scaleTypeEl.getSelectedKey();
		ScaleType scaleType = ScaleType.getEnum(selectedScaleTypeKey);
		rubric.setScaleType(scaleType);
		
		for(Iterator<Slider> sliderIt=rubric.getSliders().iterator(); sliderIt.hasNext(); ) {
			Slider slider = sliderIt.next();
			if(!StringHelper.containsNonWhitespace(slider.getStartLabel()) && !StringHelper.containsNonWhitespace(slider.getEndLabel())) {
				sliderIt.remove();
				sliders.removeIf(row -> row.getSlider().equals(slider));
				flc.setDirty(true);
			}
		}
		
		boolean noResonse = noAnswerEl.isAtLeastSelected(1);
		rubric.setNoResponseEnabled(noResonse);
		
		String lowerBoundInsufficientValue = lowerBoundInsufficientEl.getValue();
		if (StringHelper.containsNonWhitespace(lowerBoundInsufficientValue)) {
			double lowerBoundInsufficient = Double.parseDouble(lowerBoundInsufficientValue);
			rubric.setLowerBoundInsufficient(lowerBoundInsufficient);
		} else {
			rubric.setLowerBoundInsufficient(null);
		}
		String upperBoundInsufficientValue = upperBoundInsufficientEl.getValue();
		if (StringHelper.containsNonWhitespace(upperBoundInsufficientValue)) {
			double upperBoundInsufficient = Double.parseDouble(upperBoundInsufficientValue);
			rubric.setUpperBoundInsufficient(upperBoundInsufficient);
		} else {
			rubric.setUpperBoundInsufficient(null);
		}
		String lowerBoundNeutralValue = lowerBoundNeutralEl.getValue();
		if (StringHelper.containsNonWhitespace(lowerBoundNeutralValue)) {
			double lowerBoundNeutral = Double.parseDouble(lowerBoundNeutralValue);
			rubric.setLowerBoundNeutral(lowerBoundNeutral);
		} else {
			rubric.setLowerBoundNeutral(null);
		}
		String upperBoundNeutralValue = upperBoundNeutralEl.getValue();
		if (StringHelper.containsNonWhitespace(upperBoundNeutralValue)) {
			double upperBoundNeutral = Double.parseDouble(upperBoundNeutralValue);
			rubric.setUpperBoundNeutral(upperBoundNeutral);
		} else {
			rubric.setUpperBoundNeutral(null);
		}
		String lowerBoundSufficientValue = lowerBoundSufficientEl.getValue();
		if (StringHelper.containsNonWhitespace(lowerBoundSufficientValue)) {
			double lowerBoundSufficient = Double.parseDouble(lowerBoundSufficientValue);
			rubric.setLowerBoundSufficient(lowerBoundSufficient);
		} else {
			rubric.setLowerBoundSufficient(null);
		}
		String upperBoundSufficientValue = upperBoundSufficientEl.getValue();
		if (StringHelper.containsNonWhitespace(upperBoundSufficientValue)) {
			double upperBoundSufficient = Double.parseDouble(upperBoundSufficientValue);
			rubric.setUpperBoundSufficient(upperBoundSufficient);
		} else {
			rubric.setUpperBoundSufficient(null);
		}
		
		boolean startGoodRating = goodRatingEl.isOneSelected() && GOOD_RATING_START_KEY.equals(goodRatingEl.getSelectedKey())
				? true
				: false;
		rubric.setStartGoodRating(startGoodRating);
		
		rubricCtrl.updateForm();
		
		fireEvent(ureq, new ChangePartEvent(rubric));
		fireEvent(ureq, new ClosePartEvent(rubric));
	}
	
	private void commitStepLabels() {
		if(!sliderTypeEl.isOneSelected()) return;
		
		SliderType selectedType = SliderType.valueOf(sliderTypeEl.getSelectedKey());
		if(selectedType == SliderType.discrete || selectedType == SliderType.discrete_slider) {
			if(rubric.getStepLabels() == null) {
				rubric.setStepLabels(new ArrayList<>());
			}

			int steps = Integer.parseInt(stepsEl.getSelectedKey());
			for(int i=0; i<stepLabels.size() && i<steps; i++) {
				StepLabelColumn stepLabel = stepLabels.get(i);
				if(i < rubric.getStepLabels().size()) {
					rubric.getStepLabels().get(i).setLabel(stepLabel.getStepLabelEl().getValue());
				} else {
					StepLabel label = new StepLabel();
					label.setId(UUID.randomUUID().toString());
					label.setLabel(stepLabel.getStepLabelEl().getValue());
					rubric.getStepLabels().add(label);
				}
			}
			
			if(rubric.getStepLabels().size() > steps) {
				List<StepLabel> labels = new ArrayList<>(rubric.getStepLabels().subList(0, steps));
				rubric.setStepLabels(labels);
			}
		} else {
			rubric.getStepLabels().clear();
		}
	}
	
	private void commitFields() {
		for(SliderRow row:sliders) {
			String start = row.getStartLabelEl().getValue();
			String end = row.getEndLabelEl().getValue();
			
			if(StringHelper.containsNonWhitespace(start)) {
				row.getSlider().setStartLabel(start);
			} else {
				row.getSlider().setStartLabel(null);
			}
			if(StringHelper.containsNonWhitespace(end)) {
				row.getSlider().setEndLabel(end);
			} else {
				row.getSlider().setEndLabel(null);
			}
		}
	}
	
	public class StepLabelColumn {
		
		private final int step;
		private final TextElement stepLabelEl;
		
		public StepLabelColumn(int step, TextElement stepLabelEl) {
			this.step = step;
			this.stepLabelEl = stepLabelEl;
		}

		public int getStep() {
			return step;
		}

		public TextElement getStepLabelEl() {
			return stepLabelEl;
		}
		
		public void setExampleText(String example) {
			stepLabelEl.setExampleKey("rubric.scale.example.value", new String[] {example});
		}

		public void removeExampleText() {
			stepLabelEl.setExampleKey(null, null);
		}
	}
	
	public class SliderRow {
		
		private final TextElement startLabelEl;
		private final TextElement endLabelEl;
		private FormLink deleteButton;
		private FormItem sliderEl;
		
		private final Slider slider;
		
		public SliderRow(Slider slider, TextElement startLabelEl, TextElement endLabelEl, FormItem sliderEl) {
			this.slider = slider;
			this.startLabelEl = startLabelEl;
			this.endLabelEl = endLabelEl;
			this.sliderEl = sliderEl;
		}
		
		public Slider getSlider() {
			return slider;
		}
		
		public TextElement getStartLabelEl() {
			return startLabelEl;
		}

		public TextElement getEndLabelEl() {
			return endLabelEl;
		}
		
		public FormLink getDeleteButton() {
			return deleteButton;
		}
		
		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
		}
		
		public FormItem getSliderEl() {
			return sliderEl;
		}

		public void setSliderEl(FormItem sliderEl) {
			this.sliderEl = sliderEl;
		}
		
		public String getSliderCss() {
			if (sliderEl instanceof SingleSelection) {
				return "o_slider_descrete_radio";
			}
			if (sliderEl instanceof SliderElement) {
				SliderElement sliderElement = (SliderElement) sliderEl;
				if (sliderElement.getStep() == 0) {
					return "o_slider_continous";
				}
				return "o_slider_descrete";
			}
			return "";
		}
		
	}
}
