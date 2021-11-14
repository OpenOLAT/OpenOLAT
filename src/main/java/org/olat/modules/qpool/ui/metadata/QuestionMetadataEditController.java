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
package org.olat.modules.qpool.ui.metadata;

import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.bigDToString;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.toBigDecimal;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.toInt;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateBigDecimal;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateInteger;

import java.math.BigDecimal;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.MetadataSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionItemEditable;
import org.olat.modules.qpool.manager.MetadataConverterHelper;
import org.olat.modules.qpool.model.LOMDuration;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionMetadataEditController extends FormBasicController {

	private IntegerElement learningTimeDayElement;
	private IntegerElement learningTimeHourElement;
	private IntegerElement learningTimeMinuteElement;
	private IntegerElement learningTimeSecondElement;
	private FormLayoutContainer learningTimeContainer;
	private TextElement difficultyEl;
	private TextElement stdevDifficultyEl;
	private TextElement differentiationEl;
	private TextElement numAnswerAltEl;
	private TextElement usageEl;
	private TextElement correctionTimeMinuteElement;
	private FormLayoutContainer buttonsCont;
	
	private QuestionItem item;
	
	@Autowired
	private QPoolService qpoolService;

	public QuestionMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item,
			MetadataSecurityCallback securityCallback, boolean wideLayout) {
		super(ureq, wControl, wideLayout ? LAYOUT_DEFAULT : LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		
		initForm(ureq);
		setItem(item, securityCallback);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		StaticTextElement typeEl = uifactory.addStaticTextElement("question.type", "", formLayout);
		QItemType type = item.getType();
		if(type == null || type.getType() == null) {
			typeEl.setValue("");
		} else {
			String translation = translate("item.type." + type.getType().toLowerCase());
			if(translation.length() > 128) {
				translation = type.getType();
			}
			typeEl.setValue(translation);
		}
		
		String page = velocity_root + "/learning_time.html";
		learningTimeContainer = FormLayoutContainer.createCustomFormLayout("learningTime", getTranslator(), page);
		((AbstractComponent)learningTimeContainer.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeContainer.setRootForm(mainForm);
		learningTimeContainer.setLabel("educational.learningTime", null);
		formLayout.add(learningTimeContainer);
		
		LOMDuration duration = MetadataConverterHelper.convertDuration(item.getEducationalLearningTime());
		learningTimeDayElement = uifactory.addIntegerElement("learningTime.day", "", duration.getDay(), learningTimeContainer);
		learningTimeDayElement.setElementCssClass("o_sel_learning_time_d");
		((AbstractComponent)learningTimeDayElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeDayElement.setDisplaySize(3);
		learningTimeDayElement.setMandatory(true);
		
		learningTimeHourElement = uifactory.addIntegerElement("learningTime.hour", "", duration.getHour(), learningTimeContainer);
		((AbstractComponent)learningTimeHourElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeHourElement.setElementCssClass("o_sel_learning_time_H");
		learningTimeHourElement.setDisplaySize(3);
		learningTimeHourElement.setMandatory(true);
		
		learningTimeMinuteElement = uifactory.addIntegerElement("learningTime.minute", "", duration.getMinute(), learningTimeContainer);
		((AbstractComponent)learningTimeMinuteElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeMinuteElement.setElementCssClass("o_sel_learning_time_m");
		learningTimeMinuteElement.setDisplaySize(3);
		learningTimeMinuteElement.setMandatory(true);
		
		learningTimeSecondElement = uifactory.addIntegerElement("learningTime.second", "", duration.getSeconds(), learningTimeContainer);
		((AbstractComponent)learningTimeSecondElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeSecondElement.setElementCssClass("o_sel_learning_time_s");
		learningTimeSecondElement.setDisplaySize(3);
		learningTimeSecondElement.setMandatory(true);
		
		String difficulty = bigDToString(item.getDifficulty());
		difficultyEl = uifactory.addTextElement("question.difficulty", "question.difficulty", 24, difficulty, formLayout);
		difficultyEl.setElementCssClass("o_sel_difficulty");
		difficultyEl.setExampleKey("question.difficulty.example", null);
		difficultyEl.setDisplaySize(4);

		String stdevDifficulty = bigDToString(item.getStdevDifficulty());
		stdevDifficultyEl = uifactory.addTextElement("question.stdevDifficulty", "question.stdevDifficulty", 24, stdevDifficulty, formLayout);
		stdevDifficultyEl.setElementCssClass("o_sel_std_dev_difficulty");
		stdevDifficultyEl.setExampleKey("question.stdevDifficulty.example", null);
		stdevDifficultyEl.setDisplaySize(4);
		
		String differentiation = bigDToString(item.getDifferentiation());
		differentiationEl = uifactory.addTextElement("question.differentiation", "question.differentiation", 24, differentiation, formLayout);
		differentiationEl.setExampleKey("question.differentiation.example", null);
		differentiationEl.setElementCssClass("o_sel_std_differentation");
		differentiationEl.setDisplaySize(4);
		
		String numAnswerAlt = item.getNumOfAnswerAlternatives() < 0 ? "" : Integer.toString(item.getNumOfAnswerAlternatives());
		numAnswerAltEl = uifactory.addTextElement("question.numOfAnswerAlternatives", "question.numOfAnswerAlternatives", 24, numAnswerAlt, formLayout);
		numAnswerAltEl.setElementCssClass("o_sel_distractors");
		numAnswerAltEl.setDisplaySize(4);
		
		String numUsage = item.getUsage() < 0 ? "" : Integer.toString(item.getUsage());
		usageEl = uifactory.addTextElement("question.usage", "question.usage", 24, numUsage, formLayout);
		usageEl.setElementCssClass("o_sel_usage");
		usageEl.setDisplaySize(4);
		
		String correctionTime = item.getCorrectionTime() == null ? "" : item.getCorrectionTime().toString();
		correctionTimeMinuteElement = uifactory.addTextElement("question.correctionTime", 8, correctionTime, formLayout);
		correctionTimeMinuteElement.setElementCssClass("o_sel_correction_time");
		correctionTimeMinuteElement.setDisplaySize(4);
		
		buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setElementCssClass("o_sel_qpool_metadata_buttons");
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
	}
	
	private void setReadOnly(MetadataSecurityCallback securityCallback) {
		boolean canEditMetadata = securityCallback.canEditMetadata();
		learningTimeDayElement.setEnabled(canEditMetadata);
		learningTimeHourElement.setEnabled(canEditMetadata);
		learningTimeMinuteElement.setEnabled(canEditMetadata);
		learningTimeSecondElement.setEnabled(canEditMetadata);
		learningTimeContainer.setEnabled(canEditMetadata);
		difficultyEl.setEnabled(canEditMetadata);
		stdevDifficultyEl.setEnabled(canEditMetadata);
		differentiationEl.setEnabled(canEditMetadata);
		numAnswerAltEl.setEnabled(canEditMetadata);
		usageEl.setEnabled(canEditMetadata);
		correctionTimeMinuteElement.setEnabled(canEditMetadata);
		buttonsCont.setVisible(canEditMetadata);
	}

	public void setItem(QuestionItem item, MetadataSecurityCallback securityCallback) {
		this.item = item;
		if (securityCallback != null) {
			setReadOnly(securityCallback);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateBigDecimal(difficultyEl, 0.0d, 1.0d, true);
		allOk &= validateBigDecimal(stdevDifficultyEl, 0.0d, 1.0d, true);
		allOk &= validateBigDecimal(differentiationEl, -1.0d, 1.0d, true);
		allOk &= validateInteger(numAnswerAltEl, 0, Integer.MAX_VALUE, true);
		allOk &= validateInteger(usageEl, 0, Integer.MAX_VALUE, true);
		allOk &= validateInteger(correctionTimeMinuteElement, 0, Integer.MAX_VALUE, true);
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemEditable) {
			QuestionItemEditable itemImpl = (QuestionItemEditable)item;
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.UPDATE_QUESTION_ITEM_METADATA);
			if(itemImpl instanceof QuestionItemImpl) {
				builder.withBefore(item);
			}

			int day = learningTimeDayElement.getIntValue();
			int hour = learningTimeHourElement.getIntValue();
			int minute = learningTimeMinuteElement.getIntValue();
			int seconds = learningTimeSecondElement.getIntValue();
			String timeStr = MetadataConverterHelper.convertDuration(day, hour, minute, seconds);
			itemImpl.setEducationalLearningTime(timeStr);
			
			BigDecimal difficulty = toBigDecimal(difficultyEl.getValue());
			itemImpl.setDifficulty(difficulty);
			
			BigDecimal stdevDifficulty = toBigDecimal(stdevDifficultyEl.getValue());
			itemImpl.setStdevDifficulty(stdevDifficulty);
			
			BigDecimal differentiation = toBigDecimal(differentiationEl.getValue());
			itemImpl.setDifferentiation(differentiation);
			
			int numOfAnswerAlternatives = toInt(numAnswerAltEl.getValue());
			itemImpl.setNumOfAnswerAlternatives(numOfAnswerAlternatives);
			
			int numUsage = toInt(usageEl.getValue());
			itemImpl.setUsage(numUsage);
			
			if(StringHelper.containsNonWhitespace(correctionTimeMinuteElement.getValue())
					&& StringHelper.isLong(correctionTimeMinuteElement.getValue())) {
				itemImpl.setCorrectionTime(Integer.valueOf(correctionTimeMinuteElement.getValue()));
			}

			if(item instanceof QuestionItemImpl) {
				item = qpoolService.updateItem(item);
				builder.withAfter(item);
				qpoolService.persist(builder.create());
			}
			fireEvent(ureq, new QItemEdited(item));
		}
	}

}