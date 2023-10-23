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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricEditorController extends FormBasicController implements PageElementEditorController {
	
	private RubricSliderStepLabelsEditCtrl sliderStepLabelsEditCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	private int count = 0;
	private Rubric rubric;
	private final boolean restrictedEdit;
	private final boolean restrictedEditWeight;
	
	private List<StepLabelColumn> stepLabels = new ArrayList<>();
	private List<SliderRow> sliders = new ArrayList<>();
	
	private FormLink saveButton;

	private FormLink addSliderButton;
	private Boolean showEnd;
	private FormLink showEndButton;
	private FormLink hideEndButton;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public RubricEditorController(UserRequest ureq, WindowControl wControl, Rubric rubric, boolean restrictedEdit,
			boolean restrictedEditWeight) {
		super(ureq, wControl, "rubric_editor");
		this.rubric = rubric;
		this.restrictedEdit = restrictedEdit;
		this.restrictedEditWeight = restrictedEditWeight;
		this.showEnd = initShowEnd();

		initForm(ureq);
	}

	private Boolean initShowEnd() {
		for (Slider slider : rubric.getSliders()) {
			if (StringHelper.containsNonWhitespace(slider.getEndLabel())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer settingsLayout = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		settingsLayout.setRootForm(mainForm);
		formLayout.add("settings", settingsLayout);

		updateSteps();
		
		sliders.clear();
		for(Slider slider:rubric.getSliders()) {
			SliderRow row = forgeSliderRow(slider);
			sliders.add(row);
		}
		setUpDownVisibility();
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("sliders", sliders);
		}
		
		long postfix = CodeHelper.getRAMUniqueID();
		saveButton = uifactory.addFormLink("save_" + postfix, "save", null, formLayout, Link.BUTTON);
		if(!restrictedEdit) {
			addSliderButton = uifactory.addFormLink("add.slider." + postfix, "add.slider", null, formLayout, Link.BUTTON);
			addSliderButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");

			showEndButton = uifactory.addFormLink("show.end", "show.end", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			showEndButton.setIconLeftCSS("o_icon o_icon_eva_end_show");

			hideEndButton = uifactory.addFormLink("hide.end", "hide.end", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			hideEndButton.setIconLeftCSS("o_icon o_icon_eva_end_hide");
		}
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("postfix", Long.toString(postfix));
		}
		
		doShowHideEnd();
	}
	
	private void doShowEnd() {
		showEnd = Boolean.TRUE;
		doShowHideEnd();
	}
	
	private void doHideEnd() {
		showEnd = Boolean.FALSE;
		doShowHideEnd();
	}

	private void doShowHideEnd() {
		flc.contextPut("showEnd", showEnd);
		flc.setDirty(true);
	}
	
	private void updateSliders() {
		for (SliderRow row: sliders) {
			row.setSliderEl(createSliderEl(row));
			if (row.getSliderStepLabelsEditEl() != null) {
				row.getSliderStepLabelsEditEl().setVisible(rubric.isSliderStepLabelsEnabled());
			}
		}
	}

	private void updateSteps() {
		List<StepLabelColumn> stepLabelColumns = new ArrayList<>();
		
		SliderType sType = rubric.getSliderType();
		if (sType == SliderType.discrete || sType == SliderType.discrete_slider) {
			int steps = rubric.getSteps();
			for(int i=0; i<steps; i++) {
				Integer step = Integer.valueOf(i);
				StepLabelColumn col = stepLabels.size() > step? stepLabels.get(step): null;
				if(col == null) {
					String label = "";
					if(rubric.getStepLabels() != null && i<rubric.getStepLabels().size()) {
						label = rubric.getStepLabels().get(i).getLabel();
					}
					
					String id = "steplabel_" + (++count);
					TextElement textEl = uifactory.addTextElement(id, id, null, 256, label, flc);
					textEl.setDomReplacementWrapperRequired(false);
					textEl.addActionListener(FormEvent.ONCHANGE);
					textEl.setDisplaySize(4);
					col = new StepLabelColumn(i, textEl);
				}
				if (rubric.getScaleType() != null) {
					ScaleType scaleType = rubric.getScaleType();
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

	private SliderRow forgeSliderRow(Slider slider) {
		String startLabel = slider.getStartLabel();
		String id = Integer.toString(++count);
		RichTextElement startLabelEl = uifactory.addRichTextElementForStringDataMinimalistic(
				"start.label.".concat(id), null, startLabel, 4, -1, flc, getWindowControl());
		startLabelEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
		
		String endLabel = slider.getEndLabel();
		RichTextElement endLabelEl = uifactory.addRichTextElementForStringDataMinimalistic(
				"end.label.".concat(id), null, endLabel, 4, -1, flc, getWindowControl());
		endLabelEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
		
		if(!restrictedEdit) {
			startLabelEl.addActionListener(FormEvent.ONCHANGE);
			endLabelEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		// weight
		String weight = slider.getWeight() != null? slider.getWeight().toString(): "";
		TextElement weightEl = uifactory.addTextElement("weight".concat(id), null, 4, weight, flc);
		weightEl.setElementCssClass("o_slider_weight");
		weightEl.setExampleKey("slider.weight", null);
		weightEl.setEnabled(!restrictedEditWeight);
		if(!restrictedEditWeight) {
			weightEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		SliderRow row = new SliderRow(slider, startLabelEl, endLabelEl, weightEl);
		row.setSliderEl(createSliderEl(row));
		
		FormLink upButton = uifactory.addFormLink("up.".concat(id), "up", "", null, flc, Link.BUTTON | Link.NONTRANSLATED);
		upButton.setDomReplacementWrapperRequired(false);
		upButton.setIconLeftCSS("o_icon o_icon-lg o_icon_move_up");
		upButton.setUserObject(row);
		row.setUpButton(upButton);
		
		FormLink downButton = uifactory.addFormLink("down.".concat(id), "down", "", null, flc, Link.BUTTON | Link.NONTRANSLATED);
		downButton.setDomReplacementWrapperRequired(false);
		downButton.setIconLeftCSS("o_icon o_icon-lg o_icon_move_down");
		downButton.setUserObject(row);
		row.setDownButton(downButton);
		
		if(!restrictedEdit) {
			FormLink deleteButton = uifactory.addFormLink("del.".concat(id), "delete_slider", "", null, flc, Link.BUTTON | Link.NONTRANSLATED);
			deleteButton.setDomReplacementWrapperRequired(false);
			deleteButton.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
			deleteButton.setUserObject(row);
			row.setDeleteButton(deleteButton);
			flc.contextPut("deleteButtons", Boolean.TRUE);
		}
		
		return row;
	}

	private FormItem createSliderEl(SliderRow row) {
		SliderType selectedType = rubric.getSliderType();
		if (selectedType == SliderType.discrete) {
			if (rubric.isSliderStepLabelsEnabled()) {
				return createSliderStepLabelsEl(row);
			}
			return createRadioEl();
		} else if (selectedType == SliderType.discrete_slider) {
			return createDescreteSliderEl();
		}
		return createContinousSliderEl();
	}
	
	private FormItem createSliderStepLabelsEl(SliderRow row) {
		FormLayoutContainer cont = FormLayoutContainer.createCustomFormLayout("slider_" + CodeHelper.getRAMUniqueID(),
				getTranslator(), velocity_root + "/rubric_slider_step_labels.html");
		cont.setRootForm(mainForm);
		flc.add(cont);
		
		List<StepLabel> sliderStepLabels = row.getSlider().getStepLabels();
		List<String> elementNames = new ArrayList<>(rubric.getSteps());
		for(int i=0; i<rubric.getSteps(); i++) {
			String sliderStepLabel = sliderStepLabels.size() > i? sliderStepLabels.get(i).getLabel(): null;
			String name = "step_label_" + CodeHelper.getRAMUniqueID();
			StaticTextElement labelEl = uifactory.addStaticTextElement(name, sliderStepLabel, cont);
			RubricRating rating = evaluationFormManager.getRubricRating(rubric, Double.valueOf(i + 1));
			String cssClass = RubricAvgRenderer.getRatingCssClass(rating);
			labelEl.setElementCssClass(cssClass);
			elementNames.add(name);
		}
		cont.contextPut("elementNames", elementNames);
		
		String editName = "step.label.".concat(row.getSlider().getId());
		cont.contextPut("editName", editName);
		FormLink sliderStepLabelsEditEl = uifactory.addFormLink(editName, "editStepLabel", "", null, cont, Link.BUTTON | Link.NONTRANSLATED);
		sliderStepLabelsEditEl.setDomReplacementWrapperRequired(false);
		sliderStepLabelsEditEl.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
		sliderStepLabelsEditEl.setTitle(translate("rubric.slider.step.labels.edit"));
		sliderStepLabelsEditEl.setUserObject(row);
		row.setSliderStepLabelsEditEl(sliderStepLabelsEditEl);
		
		return cont;
	}

	private FormItem createRadioEl() {
		int start = 1;
		int steps = rubric.getSteps();
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
		sliderEl.setMaxValue(rubric.getSteps());
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof RubricInspectorController && event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			if(cpe.isElement(rubric)) {
				rubric = (Rubric)cpe.getElement();
				updateSteps();
				updateSliders();
			}
		} else if (calloutCtrl == source) {
			cleanUp();
		} else if (sliderStepLabelsEditCtrl == source) {
			if (event == Event.CHANGED_EVENT) {
				updateSliders();
			} else if (event == Event.DONE_EVENT) {
				updateSliders();
				if (calloutCtrl != null) {
					calloutCtrl.deactivate();
				}
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(sliderStepLabelsEditCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		sliderStepLabelsEditCtrl = null;
		calloutCtrl = null;
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if(!(fiSrc instanceof TextElement)) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addSliderButton == source) {
			doAddSlider();
		} else if (showEndButton == source) {
			doShowEnd();
		} else if (hideEndButton == source) {
			doHideEnd();
		} else if(saveButton == source) {
			if(validateFormLogic(ureq)) {
				formOK(ureq);
			}
		} else if(source instanceof FormLink button) {
			if ("editStepLabel".equals(button.getCmd())) {
				doEditSliderStepLabels(ureq, (SliderRow)button.getUserObject(), button);
			} else if ("up".equals(button.getCmd())) {
				doMoveUp((SliderRow)button.getUserObject());
			} else if ("down".equals(button.getCmd())) {
				doMoveDown((SliderRow)button.getUserObject());
			} else if ("delete_slider".equals(button.getCmd())) {
				doRemoveSlider((SliderRow)button.getUserObject());
			}
		} else if(source instanceof TextElement) {
			String name = source.getName();
			if(name != null && name.startsWith("steplabel_")) {
				commitStepLabels();
			} else {
				commitFields();
			}
			fireEvent(ureq, new ChangePartEvent(rubric));
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doMoveUp(SliderRow slideRow) {
		int index = sliders.indexOf(slideRow);
		if (index > -1) {
			swapSliders(index - 1, index);
			setUpDownVisibility();
			flc.setDirty(true);
		}
	}

	private void doMoveDown(SliderRow slideRow) {
		int index = sliders.indexOf(slideRow);
		if (index > -1) {
			swapSliders(index, index + 1);
			setUpDownVisibility();
			flc.setDirty(true);
		}
	}

	private void swapSliders(int i, int j) {
		if(i >= 0 && j >= 0 && i < sliders.size() && j < sliders.size()) {
			SliderRow tempSlider = sliders.get(i);
			sliders.set(i, sliders.get(j));
			sliders.set(j, tempSlider);
		}
	}

	private void doRemoveSlider(SliderRow row) {
		updateSteps();
		sliders.remove(row);
		setUpDownVisibility();
		flc.setDirty(true);
	}
	
	private void doAddSlider() {
		Slider slider = new Slider();
		slider.setId(UUID.randomUUID().toString());
		SliderRow row = forgeSliderRow(slider);
		sliders.add(row);
		setUpDownVisibility();
		flc.setDirty(true);
	}
	
	private void setUpDownVisibility() {
		for (int i = 0; i < sliders.size(); i++) {
			SliderRow sliderRow = sliders.get(i);
			sliderRow.getUpButton().setEnabled(true);
			sliderRow.getDownButton().setEnabled(true);
			if (i == 0) {
				sliderRow.getUpButton().setEnabled(false);
			}
			if (i == sliders.size() -1) {
				sliderRow.getDownButton().setEnabled(false);
			}
		}
	}

	private void doEditSliderStepLabels(UserRequest ureq, SliderRow row, FormLink link) {
		removeAsListenerAndDispose(sliderStepLabelsEditCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		
		sliderStepLabelsEditCtrl = new RubricSliderStepLabelsEditCtrl(ureq, getWindowControl(), rubric, row);
		listenTo(sliderStepLabelsEditCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				sliderStepLabelsEditCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		calloutCtrl.setCloseBysubmittingForm(true);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(SliderRow row:sliders) {
			TextElement weightEl = row.getWeightEl();
			weightEl.clearError();
			if (isInvalidInteger(weightEl.getValue(), 0, 1000)) {
				weightEl.setErrorKey("error.wrong.int");
				allOk = false;
			}
			if (restrictedEdit) {
				row.getStartLabelEl().clearError();
				row.getEndLabelEl().clearError();
				if (row.getEndLabelEl().isVisible()) {
					if (!StringHelper.containsNonWhitespace(row.getStartLabelEl().getValue()) && !StringHelper.containsNonWhitespace(row.getEndLabelEl().getValue()) ) {
						row.getStartLabelEl().setErrorKey("form.legende.mandatory");
						allOk = false;
					}
				} else if (!StringHelper.containsNonWhitespace(row.getStartLabelEl().getValue())) {
					row.getStartLabelEl().setErrorKey("form.legende.mandatory");
					allOk = false;
				}
			}
		}

		return allOk;
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
	
	private boolean isValidInteger(String val, int min, int max) {
		return !isInvalidDouble(val, min, max);
	}
	
	private boolean isInvalidInteger(String val, int min, int max) {
		boolean inside = true;
		if (StringHelper.containsNonWhitespace(val)) {
			try {
				double value = Integer.parseInt(val);
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
		
		fireEvent(ureq, new ChangePartEvent(rubric));
		fireEvent(ureq, new ClosePartEvent(rubric));
	}
	
	private void commitStepLabels() {

		SliderType selectedType = rubric.getSliderType();
		if(selectedType == SliderType.discrete || selectedType == SliderType.discrete_slider) {
			if(rubric.getStepLabels() == null) {
				rubric.setStepLabels(new ArrayList<>());
			}

			int steps = rubric.getSteps();
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
		List<Slider> editedSliders = new ArrayList<>(sliders.size());
		for(SliderRow row:sliders) {
			String start = row.getStartLabelEl().getValue();
			if(StringHelper.containsNonWhitespace(start)) {
				row.getSlider().setStartLabel(start);
			} else {
				row.getSlider().setStartLabel(null);
			}

			String end = row.getEndLabelEl().getValue();
			if(StringHelper.containsNonWhitespace(end) && showEnd) {
				row.getSlider().setEndLabel(end);
			} else {
				row.getSlider().setEndLabel(null);
				row.endLabelEl.setValue(null);
			}
			
			Integer weight = getIntOrNull(row.getWeightEl());
			row.getSlider().setWeight(weight);
			if (row.getSlider().getStartLabel() != null || row.getSlider().getEndLabel() != null) {
				editedSliders.add(row.getSlider());
			}
			
			if (!rubric.isSliderStepLabelsEnabled()) {
				row.getSlider().setStepLabels(null);
			} else {
				List<StepLabel> sliderStepLabels = row.getSlider().getStepLabels();
				if (sliderStepLabels.size() < rubric.getSteps()) {
					for (int i = sliderStepLabels.size(); i < rubric.getSteps(); i++) {
						StepLabel label = new StepLabel();
						label.setId(UUID.randomUUID().toString());
						label.setLabel(null);
						sliderStepLabels.add(label);
					}
				} else if (sliderStepLabels.size() > rubric.getSteps()) {
					sliderStepLabels.subList(0, rubric.getSteps() - 1);
				}
			}
		}
		rubric.setSliders(editedSliders);
	}
	
	private Integer getIntOrNull(TextElement weightEl) {
		return StringHelper.containsNonWhitespace(weightEl.getValue())
				&& isValidInteger(weightEl.getValue(), 0, 10000000)
				? Integer.parseInt(weightEl.getValue())
				: null;
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
		private final TextElement weightEl;
		private FormLink sliderStepLabelsEditEl;
		private FormLink upButton;
		private FormLink downButton;
		private FormLink deleteButton;
		private FormItem sliderEl;
		
		private final Slider slider;
		
		public SliderRow(Slider slider, TextElement startLabelEl, TextElement endLabelEl, TextElement weightEl) {
			this.slider = slider;
			this.startLabelEl = startLabelEl;
			this.endLabelEl = endLabelEl;
			this.weightEl  = weightEl;
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

		public FormLink getSliderStepLabelsEditEl() {
			return sliderStepLabelsEditEl;
		}

		public void setSliderStepLabelsEditEl(FormLink sliderStepLabelsEditEl) {
			this.sliderStepLabelsEditEl = sliderStepLabelsEditEl;
		}

		public TextElement getWeightEl() {
			return weightEl;
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}
		
		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
		}
		
		public FormLink getUpButton() {
			return upButton;
		}

		public void setUpButton(FormLink upButton) {
			this.upButton = upButton;
		}

		public FormLink getDownButton() {
			return downButton;
		}

		public void setDownButton(FormLink downButton) {
			this.downButton = downButton;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((slider == null) ? 0 : slider.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SliderRow other = (SliderRow) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (slider == null) {
				if (other.slider != null)
					return false;
			} else if (!slider.equals(other.slider))
				return false;
			return true;
		}

		private RubricEditorController getOuterType() {
			return RubricEditorController.this;
		}
		
	}
}
