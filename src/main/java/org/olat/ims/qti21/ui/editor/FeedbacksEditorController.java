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
package org.olat.ims.qti21.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ModalFeedbackBuilder;
import org.olat.ims.qti21.model.xml.ModalFeedbackBuilder.ModalFeedbackType;
import org.olat.ims.qti21.model.xml.ModalFeedbackCondition;
import org.olat.ims.qti21.model.xml.ResponseIdentifierForFeedback;
import org.olat.ims.qti21.model.xml.ResponseIdentifierForFeedback.Answer;
import org.olat.ims.qti21.model.xml.TestFeedbackBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;

/**
 * 
 * 
 * Initial date: 31 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedbacksEditorController extends FormBasicController implements SyncAssessmentItem {
	
	private FormLink addHintButton, addCorrectSolutionButton, addCorrectButton,
		addIncorrectButton, addAdditionalButton, addAnsweredButton, addEmptyButton;
	
	private SimpleFeedbackForm hintForm, correctSolutionForm;
	private SimpleFeedbackForm correctForm, incorrectForm;
	private SimpleFeedbackForm answeredForm, emptyForm;
	private List<RuledFeedbackForm> additionalForms = new ArrayList<>();
	
	private final File itemFile;
	private final VFSContainer itemContainer;
	private final String mapperUri;
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final FeedbacksEnabler enable;
	private final AssessmentItemBuilder itemBuilder;
	private final AtomicInteger counter = new AtomicInteger();

	public FeedbacksEditorController(UserRequest ureq, WindowControl wControl, AssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, FeedbacksEnabler enable,
			boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, "feedbacks");
		this.enable = enable;
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;
		
		mapperUri = registerCacheableMapper(null, "DrawingEditorController::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(itemFile.toURI(), rootDirectory));
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		DropdownItem dropdownEl = uifactory.addDropdownMenu("add.feedback.menu", null, formLayout, getTranslator());
		dropdownEl.setOrientation(DropdownOrientation.right);
		dropdownEl.setElementCssClass("o_sel_add_feedbacks");
		dropdownEl.setVisible(!restrictedEdit && !readOnly);
		dropdownEl.setEmbbeded(true);
		
		addHintButton = uifactory.addFormLink("add.hint.feedback", formLayout, Link.LINK);
		addHintButton.setElementCssClass("o_sel_add_hint");
		addHintButton.setVisible(enable.isEnabled(ModalFeedbackType.hint));
		dropdownEl.addElement(addHintButton);
		addCorrectSolutionButton = uifactory.addFormLink("add.correctSolution.feedback", formLayout, Link.LINK);
		addCorrectSolutionButton.setElementCssClass("o_sel_add_correct_solution");
		addCorrectSolutionButton.setVisible(enable.isEnabled(ModalFeedbackType.correctSolution));
		dropdownEl.addElement(addCorrectSolutionButton);
		
		boolean sc = (itemBuilder instanceof SingleChoiceAssessmentItemBuilder);
		String addCorrectLabel = "add.correct.feedback" + (sc ? ".single" : "");
		addCorrectButton = uifactory.addFormLink("add.correct.feedback", addCorrectLabel, null, formLayout, Link.LINK);
		addCorrectButton.setElementCssClass("o_sel_add_correct");
		addCorrectButton.setVisible(enable.isEnabled(ModalFeedbackType.correct));
		dropdownEl.addElement(addCorrectButton);
		addIncorrectButton = uifactory.addFormLink("add.incorrect.feedback", formLayout, Link.LINK);
		addIncorrectButton.setElementCssClass("o_sel_add_incorrect");
		addIncorrectButton.setVisible(enable.isEnabled(ModalFeedbackType.incorrect));
		dropdownEl.addElement(addIncorrectButton);
		
		addAnsweredButton = uifactory.addFormLink("add.answered.feedback", formLayout, Link.LINK);
		addAnsweredButton.setElementCssClass("o_sel_add_answered");
		addAnsweredButton.setVisible(enable.isEnabled(ModalFeedbackType.answered));
		dropdownEl.addElement(addAnsweredButton);
		addEmptyButton = uifactory.addFormLink("add.empty.feedback", formLayout, Link.LINK);
		addEmptyButton.setElementCssClass("o_sel_add_empty");
		addEmptyButton.setVisible(enable.isEnabled(ModalFeedbackType.empty));
		dropdownEl.addElement(addEmptyButton);
		
		addAdditionalButton = uifactory.addFormLink("add.additional.feedback", formLayout, Link.LINK);
		addAdditionalButton.setElementCssClass("o_sel_add_conditional");
		dropdownEl.addElement(addAdditionalButton);

		ModalFeedbackBuilder hint = itemBuilder.getHint();
		hintForm = new SimpleFeedbackForm(hint, ModalFeedbackType.hint);
		hintForm.initForm(ureq, formLayout);
		hintForm.setVisible(!hintForm.isEmpty());

		ModalFeedbackBuilder correctSolution = itemBuilder.getCorrectSolutionFeedback();
		correctSolutionForm = new SimpleFeedbackForm(correctSolution, ModalFeedbackType.correctSolution);
		correctSolutionForm.initForm(ureq, formLayout);
		correctSolutionForm.setVisible(!correctSolutionForm.isEmpty());
		
		ModalFeedbackBuilder correct = itemBuilder.getCorrectFeedback();
		correctForm = new SimpleFeedbackForm(correct, ModalFeedbackType.correct);
		correctForm.initForm(ureq, formLayout);
		correctForm.setVisible(!correctForm.isEmpty());
		
		ModalFeedbackBuilder incorrect = itemBuilder.getIncorrectFeedback();
		incorrectForm = new SimpleFeedbackForm(incorrect, ModalFeedbackType.incorrect);
		incorrectForm.initForm(ureq, formLayout);
		incorrectForm.setVisible(!incorrectForm.isEmpty());
		
		ModalFeedbackBuilder answered = itemBuilder.getAnsweredFeedback();
		answeredForm = new SimpleFeedbackForm(answered, ModalFeedbackType.answered);
		answeredForm.initForm(ureq, formLayout);
		answeredForm.setVisible(!answeredForm.isEmpty());

		ModalFeedbackBuilder empty = itemBuilder.getEmptyFeedback();
		emptyForm = new SimpleFeedbackForm(empty, ModalFeedbackType.empty);
		emptyForm.initForm(ureq, formLayout);
		emptyForm.setVisible(!emptyForm.isEmpty());
		
		List<ModalFeedbackBuilder> additionals = itemBuilder.getAdditionalFeedbackBuilders();
		if(additionals != null && additionals.size() > 0) {
			int count = 0;
			for(ModalFeedbackBuilder additional:additionals) {
				RuledFeedbackForm conditionForm = new RuledFeedbackForm(additional, ++count);
				conditionForm.initForm(ureq, formLayout);
				additionalForms.add(conditionForm);
			}
		}
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("additionals", additionalForms);
		}

		// Submit Button
		if(!readOnly) {
			uifactory.addFormSubmitButton("submit", formLayout);
		}
		updateAddButtons();
	}

	@Override
	public void sync(UserRequest ureq, AssessmentItemBuilder builder) {
		if(itemBuilder == builder && builder instanceof ResponseIdentifierForFeedback) {
			for(RuledFeedbackForm additionalForm:additionalForms) {
				additionalForm.sync();
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addHintButton == source) {
			hintForm.setVisible(true);
			updateAddButtons();
		} else if(addCorrectSolutionButton == source) {
			correctSolutionForm.setVisible(true);
			updateAddButtons();
		} else if(addCorrectButton == source) {
			correctForm.setVisible(true);
			updateAddButtons();
		} else if(addIncorrectButton == source) {
			incorrectForm.setVisible(true);
			updateAddButtons();
		} else if(addAnsweredButton == source) {
			answeredForm.setVisible(true);
			updateAddButtons();
		} else if(addEmptyButton == source) {
			emptyForm.setVisible(true);
			updateAddButtons();
		} else if(addAdditionalButton == source) {
			doAddAdditionalFeedback(ureq);
		} else {
			if(additionalForms != null && additionalForms.size() > 0) {
				for(RuledFeedbackForm conditionForm:additionalForms) {
					conditionForm.formInnerEvent(source);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateAddButtons() {
		addHintButton.setEnabled(!hintForm.isVisible());
		addCorrectSolutionButton.setEnabled(!correctSolutionForm.isVisible());
		addCorrectButton.setEnabled(!correctForm.isVisible());
		addIncorrectButton.setEnabled(!incorrectForm.isVisible());
		addAnsweredButton.setEnabled(!answeredForm.isVisible());
		addEmptyButton.setEnabled(!emptyForm.isVisible());
	}
	
	private void doAddAdditionalFeedback(UserRequest ureq) {
		ModalFeedbackBuilder feedbackBuilder = new ModalFeedbackBuilder(itemBuilder.getAssessmentItem(), ModalFeedbackType.additional);
		RuledFeedbackForm conditionForm = new RuledFeedbackForm(feedbackBuilder, additionalForms.size() + 1);
		conditionForm.initForm(ureq, flc);
		additionalForms.add(conditionForm);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		
		hintForm.commit();
		hintForm.setVisible(!hintForm.isEmpty());
		correctSolutionForm.commit();
		correctSolutionForm.setVisible(!correctSolutionForm.isEmpty());
		correctForm.commit();
		correctForm.setVisible(!correctForm.isEmpty());
		incorrectForm.commit();
		incorrectForm.setVisible(!incorrectForm.isEmpty());
		answeredForm.commit();
		answeredForm.setVisible(!answeredForm.isEmpty());
		emptyForm.commit();
		emptyForm.setVisible(!emptyForm.isEmpty());

		List<RuledFeedbackForm> validAdditionalForms = new ArrayList<>();
		List<ModalFeedbackBuilder> additionalBuilders = new ArrayList<>();
		for(RuledFeedbackForm additionalForm:additionalForms) {
			ModalFeedbackBuilder additionalBuilder = additionalForm.commit();
			if(additionalBuilder != null) {
				additionalBuilders.add(additionalBuilder);
				validAdditionalForms.add(additionalForm);
			}
		}
		itemBuilder.setAdditionalFeedbackBuilders(additionalBuilders);
		additionalForms.clear();
		additionalForms.addAll(validAdditionalForms);
		updateAddButtons();

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem()));
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(restrictedEdit) {
			allOk &= hintForm.validateFormLogic();
			allOk &= correctSolutionForm.validateFormLogic();
			allOk &= correctForm.validateFormLogic();
			allOk &= incorrectForm.validateFormLogic();
			allOk &= answeredForm.validateFormLogic();
			allOk &= emptyForm.validateFormLogic();
		}
		
		for(RuledFeedbackForm additionalForm:additionalForms) {
			allOk &= additionalForm.validateFormLogic();
		}
		
		return allOk;
	}
	
	public class SimpleFeedbackForm {
		
		private TextElement titleEl;
		private RichTextElement textEl;
		private FormLayoutContainer formLayout;

		private final ModalFeedbackType feedbackType;
		private ModalFeedbackBuilder feedbackBuilder;
		
		public SimpleFeedbackForm(ModalFeedbackBuilder feedbackBuilder, ModalFeedbackType feedbackType) {
			this.feedbackType = feedbackType;
			this.feedbackBuilder = feedbackBuilder;
		}

		public void initForm(UserRequest ureq, FormItemContainer parentFormLayout) {
			String id = Integer.toString(counter.incrementAndGet());

			formLayout = FormLayoutContainer.createDefaultFormLayout_2_10(feedbackType.name(), getTranslator());
			parentFormLayout.add(formLayout);
			formLayout.setRootForm(mainForm);
			
			String formTitle = "form.imd." + feedbackType.name();
			if(feedbackType == ModalFeedbackType.correct && itemBuilder instanceof SingleChoiceAssessmentItemBuilder) {
				formTitle += ".single";
			}
			formLayout.setFormTitle(translate(formTitle + ".text"));
			
			String title = feedbackBuilder == null ? "" : feedbackBuilder.getTitle();
			titleEl = uifactory.addTextElement("title_".concat(id), "form.imd.feedback.title", -1, title, formLayout);
			titleEl.setUserObject(feedbackBuilder);
			titleEl.setEnabled(!readOnly);
			titleEl.setElementCssClass("o_sel_assessment_item_" + feedbackType.name() + "_title");
			
			String text = feedbackBuilder == null ? "" : feedbackBuilder.getText();
			textEl = uifactory.addRichTextElementForQTI21("text_".concat(id), "form.imd.feedback.text", text, 8, -1,
					itemContainer, formLayout, ureq.getUserSession(), getWindowControl());
			textEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
			textEl.setEnabled(!readOnly);
			textEl.setVisible(!readOnly);
			
			String helpText = "feedback." + feedbackType.name();
			if(feedbackType == ModalFeedbackType.correct && itemBuilder instanceof SingleChoiceAssessmentItemBuilder) {
				helpText += ".single";
			}
			textEl.setHelpTextKey(helpText + ".help", null);
			textEl.setHelpUrlForManualPage("manual_user/tests/Configure_test_questions/#feedback");
			textEl.setElementCssClass("o_sel_assessment_item_" + feedbackType.name() + "_feedback");
			RichTextConfiguration richTextConfig2 = textEl.getEditorConfiguration();
			richTextConfig2.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
			
			if(readOnly) {
				List<FlowStatic> textFlow = feedbackBuilder == null ? Collections.emptyList() : feedbackBuilder.getTextFlowStatic();
				FlowFormItem textReadOnlyEl = new FlowFormItem("textro_".concat(id), itemFile);
				textReadOnlyEl.setLabel("form.imd.feedback.text", null);
				textReadOnlyEl.setFlowStatics(textFlow);
				textReadOnlyEl.setMapperUri(mapperUri);
				formLayout.add(textReadOnlyEl);
			}
		}

		public boolean isEmpty() {
			return TestFeedbackBuilder.isEmpty(textEl.getValue());
		}
		
		public boolean isVisible() {
			return formLayout.isVisible();
		}
		
		public void setVisible(boolean visible) {
			formLayout.setVisible(visible);
		}
		
		public boolean validateFormLogic() {
			boolean allOk = true;
			
			if(restrictedEdit && isVisible()) {
				textEl.clearError();
				String text = textEl.getRawValue();
				if(restrictedEdit && TestFeedbackBuilder.isEmpty(text)) {
					textEl.setErrorKey("error.cannot.remove.feedback", null);
					allOk = false;
				}
			}
			
			return allOk;
		}
		
		public void commit() {
			String title = titleEl.getValue();
			String text = textEl.getRawValue();
			if(restrictedEdit && TestFeedbackBuilder.isEmpty(text)) {
				//not allowed to remove a feedback
			} else if(!TestFeedbackBuilder.isEmpty(text)) {
				feedbackBuilder = itemBuilder.getFeedbackBuilder(feedbackType);
				if(feedbackBuilder == null) {
					feedbackBuilder = itemBuilder.createFeedbackBuilder(feedbackType);
				}
				feedbackBuilder.setTitle(title);
				feedbackBuilder.setText(text);
			} else {
				itemBuilder.removeFeedbackBuilder(feedbackType);
			}
		}
	}
	
	public class RuledFeedbackForm {
		
		private TextElement titleEl;
		private RichTextElement textEl;
		private FormLayoutContainer formLayout;
		private FormLayoutContainer conditionListContainer;
		private List<ConditionForm> conditions = new ArrayList<>();

		private int position;
		private ModalFeedbackBuilder feedbackBuilder;
		private final ModalFeedbackType feedbackType = ModalFeedbackType.additional;
		
		public RuledFeedbackForm(ModalFeedbackBuilder feedbackBuilder, int position) {
			this.position = position;
			this.feedbackBuilder = feedbackBuilder;
		}

		public void initForm(UserRequest ureq, FormItemContainer parentFormLayout) {
			String id = Integer.toString(counter.incrementAndGet());
			
			formLayout = FormLayoutContainer.createDefaultFormLayout_2_10("feedback".concat(id), getTranslator());
			formLayout.setElementCssClass("o_sel_assessment_item_" + feedbackType.name() + "_" + position);
			parentFormLayout.add(formLayout);
			formLayout.setRootForm(mainForm);
			formLayout.setFormTitle(translate("form.imd.additional.text", new String[] { Integer.toString(position) }));

			String title = feedbackBuilder == null ? "" : feedbackBuilder.getTitle();
			titleEl = uifactory.addTextElement("title_".concat(id), "form.imd.feedback.title", -1, title, formLayout);
			titleEl.setUserObject(feedbackBuilder);
			titleEl.setEnabled(!readOnly);
			titleEl.setElementCssClass("o_sel_assessment_item_" + feedbackType.name() + "_feedback_title");
			
			String conditionListPage = velocity_root + "/feedback_condition_list.html";
			conditionListContainer = FormLayoutContainer.createCustomFormLayout("cond_list_".concat(id),
					getTranslator(), conditionListPage);
			formLayout.add(conditionListContainer);
			conditionListContainer.setRootForm(mainForm);
			conditionListContainer.contextPut("conditions", conditions);
			conditionListContainer.setLabel("form.imd.condition", null);
			
			// rules
			if(feedbackBuilder.getFeedbackConditons() != null && !feedbackBuilder.getFeedbackConditons().isEmpty()) {
				for(ModalFeedbackCondition condition:feedbackBuilder.getFeedbackConditons()) {
					ConditionForm conditionForm = new ConditionForm(condition);
					conditionForm.initForm(conditionListContainer);
					conditions.add(conditionForm);
				}
			} else {
				ConditionForm conditionForm = new ConditionForm();
				conditionForm.initForm(conditionListContainer);
				conditions.add(conditionForm);
			}
			
			String text = feedbackBuilder == null ? "" : feedbackBuilder.getText();
			textEl = uifactory.addRichTextElementForQTI21("text_".concat(id), "form.imd.feedback.text", text, 8, -1,
					itemContainer, formLayout, ureq.getUserSession(), getWindowControl());
			textEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
			textEl.setEnabled(!readOnly);
			textEl.setVisible(!readOnly);
			textEl.setHelpTextKey("feedback." + feedbackType.name() + ".help", null);
			textEl.setHelpUrlForManualPage("manual_user/tests/Configure_test_questions/#feedback");
			textEl.setElementCssClass("o_sel_assessment_item_" + feedbackType.name() + "_feedback");
			RichTextConfiguration richTextConfig2 = textEl.getEditorConfiguration();
			richTextConfig2.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
			
			if(readOnly) {
				List<FlowStatic> textFlow = feedbackBuilder == null ? Collections.emptyList() : feedbackBuilder.getTextFlowStatic();
				FlowFormItem textReadOnlyEl = new FlowFormItem("textro_".concat(id), itemFile);
				textReadOnlyEl.setLabel("form.imd.feedback.text", null);
				textReadOnlyEl.setFlowStatics(textFlow);
				textReadOnlyEl.setMapperUri(mapperUri);
				formLayout.add(textReadOnlyEl);
			}
			
			updateDeleteButtons();
		}
		
		public FormLayoutContainer getFormLayoutContainer() {
			return formLayout;
		}
		
		protected void sync() {
			for(ConditionForm conditionForm:conditions) {
				conditionForm.sync();
			}
		}
		
		protected boolean validateFormLogic() {
			boolean allOk = true;
			
			for(ConditionForm condition:conditions) {
				allOk &= condition.validateFormLogic();
			}

			textEl.clearError();
			String text = textEl.getRawValue();
			if(restrictedEdit && TestFeedbackBuilder.isEmpty(text)) {
				textEl.setErrorKey("error.cannot.remove.feedback", null);
				allOk = false;
			}
			
			return allOk;
		}
		
		protected void formInnerEvent(FormItem source) {
			if(conditions != null && !conditions.isEmpty()) {
				ConditionForm[] conditionArray = conditions.toArray(new ConditionForm[conditions.size()]);
				for(ConditionForm condition:conditionArray) {
					condition.formInnerEvent(source);
				}
			}
		}

		public ModalFeedbackBuilder commit() {
			String title = titleEl.getValue();
			String text = textEl.getRawValue();
			
			List<ModalFeedbackCondition> feedbackConditions = new ArrayList<>();
			for(ConditionForm condition:conditions) {
				feedbackConditions.add(condition.commit());
			}
			if(restrictedEdit && TestFeedbackBuilder.isEmpty(text)) {
				//not allowed to remove a feedback already in use
			} else if(!TestFeedbackBuilder.isEmpty(text) && !feedbackConditions.isEmpty()) {
				if(feedbackBuilder == null) {
					feedbackBuilder = itemBuilder.createFeedbackBuilder(feedbackType);
				}
				feedbackBuilder.setTitle(title);
				feedbackBuilder.setText(text);
				feedbackBuilder.setFeedbackConditions(feedbackConditions);
				return feedbackBuilder;
			}
			return null;
		}

		public void doAddCondition(ConditionForm current) {
			ConditionForm conditionForm = new ConditionForm();
			conditionForm.initForm(conditionListContainer);
			
			int pos = -1;
			if(current != null) {
				pos = conditions.indexOf(current) + 1;
			}
			if(pos >= 0 && pos < conditions.size()) {
				conditions.add(pos, conditionForm);
			} else {
				conditions.add(conditionForm);
			}
			conditionListContainer.setDirty(true);
			updateDeleteButtons();
		}
		
		public void doRemoveCondition(ConditionForm toRemove) {
			conditions.remove(toRemove);
			conditionListContainer.remove(toRemove.getFormLayoutContainer());
			updateDeleteButtons();
		}
		
		private void updateDeleteButtons() {
			boolean enableDelete = conditions.size() > 1;
			for(ConditionForm condition:conditions) {
				condition.setDeleteEnable(enableDelete);
			}
		}
		
		public class ConditionForm {
			
			private SingleSelection variableEl;
			private SingleSelection operatorEl;
			private SingleSelection dropDownValueEl;
			private TextElement textValueEl;

			private FormLink addButton;
			private FormLink deleteButton;
			private FormLayoutContainer ruleContainer;
			
			private String[] variableKeys = new String[] {
					ModalFeedbackCondition.Variable.score.name(),
					ModalFeedbackCondition.Variable.attempts.name(),
			};
			
			private String[] variableWithResponseKeys = new String[] {
					ModalFeedbackCondition.Variable.score.name(),
					ModalFeedbackCondition.Variable.attempts.name(),
					ModalFeedbackCondition.Variable.response.name(),	
			};
			
			private String[] matchOperatorKeys = new String[] {
					ModalFeedbackCondition.Operator.equals.name(),
					ModalFeedbackCondition.Operator.notEquals.name(),
			};
			
			private String[] mathOperatorKeys = new String[] {
					ModalFeedbackCondition.Operator.bigger.name(),
					ModalFeedbackCondition.Operator.biggerEquals.name(),
					ModalFeedbackCondition.Operator.equals.name(),
					ModalFeedbackCondition.Operator.notEquals.name(),
					ModalFeedbackCondition.Operator.smaller.name(),
					ModalFeedbackCondition.Operator.smallerEquals.name()
			};
			
			private final ModalFeedbackCondition condition;
			
			public ConditionForm() {
				condition = new ModalFeedbackCondition();
			}
			
			public ConditionForm(ModalFeedbackCondition condition) {
				this.condition = condition;
			}
			
			public FormLayoutContainer getRuleContainer() {
				return ruleContainer;
			}
			
			public void initForm(FormItemContainer feedbackFormLayout) {
				String id = Integer.toString(counter.incrementAndGet());

				String page = velocity_root + "/feedback_condition.html";
				ruleContainer = FormLayoutContainer.createCustomFormLayout("rule_".concat(id), getTranslator(), page);
				feedbackFormLayout.add(ruleContainer);
				ruleContainer.setRootForm(mainForm);
				ruleContainer.contextPut("id", id);
				ruleContainer.contextPut("rule", this);
				
				String[] varKeys = (itemBuilder instanceof ResponseIdentifierForFeedback) ? variableWithResponseKeys : variableKeys;
				String[] variableValues = new String[varKeys.length];
				for(int i=varKeys.length; i-->0; ) {
					variableValues[i] = translate("variable.".concat(varKeys[i]));
				}
				variableEl = uifactory.addDropdownSingleselect("var_".concat(id), null, ruleContainer, varKeys, variableValues, null);
				variableEl.setDomReplacementWrapperRequired(false);
				variableEl.setEnabled(!restrictedEdit && !readOnly);
				variableEl.addActionListener(FormEvent.ONCHANGE);
				boolean found = false;
				if(condition.getVariable() != null) {
					for(String variableKey:varKeys) {
						if(variableKey.equals(condition.getVariable().name())) {
							variableEl.select(variableKey, true);
							found = true;
						}
					}
				}
				if(!found) {
					variableEl.select(varKeys[0], true);
				}

				operatorEl = uifactory.addDropdownSingleselect("ope_".concat(id), null, ruleContainer, new String[0], new String[0], null);
				String[] operatorKeys = updateOperators();
				operatorEl.setDomReplacementWrapperRequired(false);
				operatorEl.setEnabled(!restrictedEdit && !readOnly);
				boolean foundOp = false;
				if(condition.getVariable() != null) {
					for(String operatorKey:operatorKeys) {
						if(operatorKey.equals(condition.getOperator().name())) {
							operatorEl.select(operatorKey, true);
							foundOp = true;
						}
					}
				}
				if(!foundOp) {
					operatorEl.select(operatorKeys[0], true);
				}
				
				String val = condition.getValue();
				textValueEl = uifactory.addTextElement("txt_val_".concat(id), null, 8, val, ruleContainer);
				textValueEl.setDomReplacementWrapperRequired(false);
				textValueEl.setEnabled(!restrictedEdit && !readOnly);
				
				String[] answerKeys = new String[0];		
				dropDownValueEl = uifactory.addDropdownSingleselect("ans_".concat(id), null, ruleContainer, answerKeys, answerKeys, null);
				dropDownValueEl.setDomReplacementWrapperRequired(false);
				dropDownValueEl.setEnabled(!restrictedEdit && !readOnly);
				
				updateValues(val);
				
				if(!restrictedEdit && !readOnly) {
					addButton = uifactory.addFormLink("add_".concat(id), "add", null, ruleContainer, Link.BUTTON);
					addButton.setIconLeftCSS("o_icon o_icon_add");
					deleteButton = uifactory.addFormLink("del_".concat(id), "delete", null, ruleContainer, Link.BUTTON);	
					deleteButton.setIconLeftCSS("o_icon o_icon_remove");
				}
			}
			
			public void setDeleteEnable(boolean enable) {
				if(deleteButton != null) {
					deleteButton.setEnabled(enable);
				}
			}
			
			protected void sync() {
				updateValues(getValue());
			}
			
			protected boolean validateFormLogic() {
				boolean allOk = true;
				
				variableEl.clearError();
				textValueEl.clearError();
				dropDownValueEl.clearError();
				if(variableEl.isOneSelected()) {
					String selectedKey = variableEl.getSelectedKey();
					if(ModalFeedbackCondition.Variable.score.name().equals(selectedKey)) {
						try {
							Double.parseDouble(textValueEl.getValue());
						} catch (NumberFormatException e) {
							textValueEl.setErrorKey("error.double", null);
							allOk &= false;
						}
					} else if(ModalFeedbackCondition.Variable.attempts.name().equals(selectedKey)) {
						try {
							Integer.parseInt(textValueEl.getValue());
						} catch (Exception e) {
							textValueEl.setErrorKey("error.integer", null);
							allOk &= false;
						}
					} else if(ModalFeedbackCondition.Variable.response.name().equals(selectedKey)) {
						if(!dropDownValueEl.isOneSelected()) {
							dropDownValueEl.setErrorKey("form.legende.mandatory", null);
							allOk &= false;
						}
					}
				} else {
					variableEl.setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				}
				
				return allOk;
			}
			
			protected void formInnerEvent(FormItem source) {
				if(addButton == source) {
					doAddCondition(this);
				} else if(deleteButton == source) {
					doRemoveCondition(this);
				} else if(variableEl == source) {
					updateOperators();
					updateValues(getValue());
				}	
			}
			
			private String[] updateOperators() {
				String[] operatorKeys;
				if(variableEl.getSelectedKey().equals(ModalFeedbackCondition.Variable.response.name())) {
					operatorKeys = matchOperatorKeys;
				} else {
					operatorKeys = mathOperatorKeys;
				}

				String[] operatorValues = new String[operatorKeys.length];
				for(int i=operatorKeys.length; i-->0; ) {
					operatorValues[i] = translate("math.operator.".concat(operatorKeys[i]));
				}
				operatorEl.setKeysAndValues(operatorKeys, operatorValues, null);
				return operatorKeys;
			}
			
			private void updateValues(String val) {
				boolean responseVar = variableEl.getSelectedKey().equals(ModalFeedbackCondition.Variable.response.name());
				textValueEl.setVisible(!responseVar);
				dropDownValueEl.setVisible(responseVar);
				
				if(itemBuilder instanceof ResponseIdentifierForFeedback) {
					ResponseIdentifierForFeedback responseFeedback = (ResponseIdentifierForFeedback)itemBuilder;
					List<Answer> answers = responseFeedback.getAnswers();
					String[] answerKeys = new String[answers.size()];
					String[] answerValues = new String[answers.size()];
					
					for(int i=0; i<answers.size(); i++) {
						Answer answer = answers.get(i);
						answerKeys[i] = answer.getIdentifier().toString();
						answerValues[i] = answer.getLabel();
					}
					dropDownValueEl.setKeysAndValues(answerKeys, answerValues, null);
					
					boolean foundAnswer = false;
					if(val != null) {
						for(String answerKey:answerKeys) {
							if(answerKey.equals(val)) {
								dropDownValueEl.select(answerKey, true);
								foundAnswer = true;
							}
						}
					}
					if(!foundAnswer && answerKeys.length > 0) {
						dropDownValueEl.select(answerKeys[0], true);
					}
				}
			}
			
			public ModalFeedbackCondition commit() {
				ModalFeedbackCondition.Variable var = ModalFeedbackCondition.Variable.valueOf(variableEl.getSelectedKey());
				ModalFeedbackCondition.Operator operator = ModalFeedbackCondition.Operator.valueOf(operatorEl.getSelectedKey());
				String val = getValue();
				return new ModalFeedbackCondition(var, operator, val);
			}
			
			public String getValue() {
				ModalFeedbackCondition.Variable var = ModalFeedbackCondition.Variable.valueOf(variableEl.getSelectedKey());
				String val = null;
				switch(var) {
					case score:
					case attempts:
						val = textValueEl.getValue();
						break;
					case response:
						val = dropDownValueEl.getSelectedKey();
						break;
					
				}
				return val;
			}
			
			public FormLink getAddButton() {
				return addButton;
			}
			
			public FormLink getDeleteButton() {
				return deleteButton;
			}
			
			
			public SingleSelection getVariableEl() {
				return variableEl;
			}
			
			public SingleSelection getOperatorEl() {
				return operatorEl;
			}
			
			public TextElement getTextValueEl() {
				return textValueEl;
			}
			
			public FormLayoutContainer getFormLayoutContainer() {
				return ruleContainer;
			}
		}
	}
}
