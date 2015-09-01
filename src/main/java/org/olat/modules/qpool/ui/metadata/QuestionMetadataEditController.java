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
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.getQItemTypeKeyValues;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.toBigDecimal;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.toInt;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateBigDecimal;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateSelection;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateInteger;

import java.math.BigDecimal;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory.KeyValues;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionMetadataEditController extends FormBasicController {

	private SingleSelection typeEl, assessmentTypeEl;
	private TextElement difficultyEl, stdevDifficultyEl, differentiationEl, numAnswerAltEl;
	
	private QuestionItem item;
	private final QPoolService qpoolService;

	public QuestionMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("question");

		KeyValues typeKeys = getQItemTypeKeyValues(getTranslator(), qpoolService);
		typeEl = uifactory.addDropdownSingleselect("question.type", "question.type", formLayout, typeKeys.getKeys(), typeKeys.getValues(), null);
		if(item.getType() != null) {
			typeEl.select(item.getType().getType(), true);
		}
		
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
		
		uifactory.addStaticTextElement("question.usage", Integer.toString(item.getUsage()), formLayout);
		String[] assessmentTypeKeys = new String[]{ "summative", "formative", "both"};
		String[] assessmentTypeValues = new String[]{
			translate("question.assessmentType.summative"), translate("question.assessmentType.formative"),
			translate("question.assessmentType.both"),	
		};
		assessmentTypeEl = uifactory.addDropdownSingleselect("question.assessmentType", "question.assessmentType", formLayout,
				assessmentTypeKeys, assessmentTypeValues, null);
		if(StringHelper.containsNonWhitespace(item.getAssessmentType())) {
			assessmentTypeEl.select(item.getAssessmentType(), true);
		}

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateSelection(typeEl, true);
		allOk &= validateBigDecimal(difficultyEl, 0.0d, 1.0d, true);
		allOk &= validateBigDecimal(stdevDifficultyEl, 0.0d, 1.0d, true);
		allOk &= validateBigDecimal(differentiationEl, -1.0d, 1.0d, true);
		allOk &= validateInteger(numAnswerAltEl, 0, Integer.MAX_VALUE, true);
		allOk &= validateSelection(assessmentTypeEl, true);
		return allOk && super.validateFormLogic(ureq);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			if(typeEl.isOneSelected()) {
				String typeKey = typeEl.getSelectedKey();
				itemImpl.setType(qpoolService.getItemType(typeKey));
			}
			
			BigDecimal difficulty = toBigDecimal(difficultyEl.getValue());
			itemImpl.setDifficulty(difficulty);
			BigDecimal stdevDifficulty = toBigDecimal(stdevDifficultyEl.getValue());
			itemImpl.setStdevDifficulty(stdevDifficulty);
			BigDecimal differentiation = toBigDecimal(differentiationEl.getValue());
			itemImpl.setDifferentiation(differentiation);
			int numOfAnswerAlternatives = toInt(numAnswerAltEl.getValue());
			itemImpl.setNumOfAnswerAlternatives(numOfAnswerAlternatives);
			
			String assessmentType = assessmentTypeEl.isOneSelected() ? assessmentTypeEl.getSelectedKey() : null;
			itemImpl.setAssessmentType(assessmentType);
		}
		item = qpoolService.updateItem(item);
		fireEvent(ureq, new QItemEdited(item));
	}
}