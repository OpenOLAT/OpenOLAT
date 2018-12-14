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
import org.olat.core.util.Util;
import org.olat.modules.qpool.MetadataSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
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

	private StaticTextElement typeEl;
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
	private FormLayoutContainer buttonsCont;
	
	private QuestionItem item;
	
	@Autowired
	private QPoolService qpoolService;

	public QuestionMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item,
			MetadataSecurityCallback securityCallback) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		
		initForm(ureq);
		setItem(item, securityCallback);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		typeEl = uifactory.addStaticTextElement("question.type", "", formLayout);
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
		((AbstractComponent)learningTimeDayElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeDayElement.setDisplaySize(3);
		learningTimeDayElement.setMandatory(true);
		
		learningTimeHourElement = uifactory.addIntegerElement("learningTime.hour", "", duration.getHour(), learningTimeContainer);
		((AbstractComponent)learningTimeHourElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeHourElement.setDisplaySize(3);
		learningTimeHourElement.setMandatory(true);
		
		learningTimeMinuteElement = uifactory.addIntegerElement("learningTime.minute", "", duration.getMinute(), learningTimeContainer);
		((AbstractComponent)learningTimeMinuteElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeMinuteElement.setDisplaySize(3);
		learningTimeMinuteElement.setMandatory(true);
		
		learningTimeSecondElement = uifactory.addIntegerElement("learningTime.second", "", duration.getSeconds(), learningTimeContainer);
		((AbstractComponent)learningTimeSecondElement.getComponent()).setDomReplacementWrapperRequired(false);
		learningTimeSecondElement.setDisplaySize(3);
		learningTimeSecondElement.setMandatory(true);
		
		String difficulty = bigDToString(item.getDifficulty());
		difficultyEl = uifactory.addTextElement("question.difficulty", "question.difficulty", 24, difficulty, formLayout);
		difficultyEl.setExampleKey("question.difficulty.example", null);
		difficultyEl.setDisplaySize(4);

		String stdevDifficulty = bigDToString(item.getStdevDifficulty());
		stdevDifficultyEl = uifactory.addTextElement("question.stdevDifficulty", "question.stdevDifficulty", 24, stdevDifficulty, formLayout);
		stdevDifficultyEl.setExampleKey("question.stdevDifficulty.example", null);
		stdevDifficultyEl.setDisplaySize(4);
		
		String differentiation = bigDToString(item.getDifferentiation());
		differentiationEl = uifactory.addTextElement("question.differentiation", "question.differentiation", 24, differentiation, formLayout);
		differentiationEl.setExampleKey("question.differentiation.example", null);
		differentiationEl.setDisplaySize(4);
		
		String numAnswerAlt = item.getNumOfAnswerAlternatives() < 0 ? "" : Integer.toString(item.getNumOfAnswerAlternatives());
		numAnswerAltEl = uifactory.addTextElement("question.numOfAnswerAlternatives", "question.numOfAnswerAlternatives", 24, numAnswerAlt, formLayout);
		numAnswerAltEl.setDisplaySize(4);
		
		String numUsage = item.getUsage() < 0 ? "" : Integer.toString(item.getUsage());
		usageEl = uifactory.addTextElement("question.usage", "question.usage", 24, numUsage, formLayout);
		usageEl.setDisplaySize(4);

		buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
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
		buttonsCont.setVisible(canEditMetadata);
	}

	public void setItem(QuestionItem item, MetadataSecurityCallback securityCallback) {
		this.item = item;
		if (securityCallback != null) {
			setReadOnly(securityCallback);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateBigDecimal(difficultyEl, 0.0d, 1.0d, true);
		allOk &= validateBigDecimal(stdevDifficultyEl, 0.0d, 1.0d, true);
		allOk &= validateBigDecimal(differentiationEl, -1.0d, 1.0d, true);
		allOk &= validateInteger(numAnswerAltEl, 0, Integer.MAX_VALUE, true);
		allOk &= validateInteger(usageEl, 0, Integer.MAX_VALUE, true);
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.UPDATE_QUESTION_ITEM_METADATA);
			builder.withBefore(itemImpl);

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

			item = qpoolService.updateItem(itemImpl);
			builder.withAfter(item);
			qpoolService.persist(builder.create());
			fireEvent(ureq, new QItemEdited(item));
		}
	}

}