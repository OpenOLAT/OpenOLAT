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
package org.olat.modules.qpool.ui.edit;

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
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.MetadatasController;

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
		setTranslator(Util.createPackageTranslator(MetadatasController.class, ureq.getLocale(), getTranslator()));
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("question");
		
		String[] typeKeys = new String[]{ "" };
		typeEl = uifactory.addDropdownSingleselect("question.type", "question.type", formLayout, typeKeys, typeKeys, null);
		
		difficultyEl = uifactory.addTextElement("question.difficulty", "question.difficulty", 10, "", formLayout);
		difficultyEl.setExampleKey("question.difficulty.example", null);
		difficultyEl.setDisplaySize(4);
		stdevDifficultyEl = uifactory.addTextElement("question.stdevDifficulty", "question.stdevDifficulty", 10, "", formLayout);
		stdevDifficultyEl.setExampleKey("question.stdevDifficulty.example", null);
		stdevDifficultyEl.setDisplaySize(4);
		differentiationEl = uifactory.addTextElement("question.differentiation", "question.differentiation", 10, "", formLayout);
		differentiationEl.setExampleKey("question.differentiation.example", null);
		differentiationEl.setDisplaySize(4);
		
		numAnswerAltEl = uifactory.addTextElement("question.numOfAnswerAlternatives", "question.numOfAnswerAlternatives", 10, "", formLayout);
		numAnswerAltEl.setDisplaySize(4);
		
		uifactory.addStaticTextElement("question.usage", Integer.toString(item.getUsage()), formLayout);
		String[] assessmentTypeKeys = new String[]{ "summative", "formative", "both"};
		String[] assessmentTypeValues = new String[]{
			translate("question.assessmentType.summative"), translate("question.assessmentType.formative"),
			translate("question.assessmentType.both"),	
		};
		assessmentTypeEl = uifactory.addDropdownSingleselect("question.assessmentType", "question.assessmentType", formLayout,
				assessmentTypeKeys, assessmentTypeValues, null);

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
		typeEl.clearError();
		if(!typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		allOk &= validateBigDecimal(difficultyEl, 0.0d, 1.0d);
		allOk &= validateBigDecimal(stdevDifficultyEl, 0.0d, 1.0d);
		allOk &= validateBigDecimal(differentiationEl, -1.0d, 1.0d);
		
		assessmentTypeEl.clearError();
		if(!assessmentTypeEl.isOneSelected()) {
			assessmentTypeEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		return allOk && super.validateFormLogic(ureq);
	}
	
	/**
	 * The value is here not mandatory!
	 * @param el
	 * @param min
	 * @param max
	 * @return
	 */
	protected boolean validateBigDecimal(TextElement el, double min, double max) {
		boolean allOk = true;

		el.clearError();
		String val = el.getValue();
		if(StringHelper.containsNonWhitespace(val)) {
			
			try {
				double value = Double.parseDouble(val);
				if(min > value) {
					el.setErrorKey("error.wrongFloat", null);
					allOk = false;
				} else if(max < value) {
					el.setErrorKey("error.wrongFloat", null);
					allOk = false;
				}
			} catch (NumberFormatException e) {
				el.setErrorKey("error.wrongFloat", null);
				allOk = false;
			}
		}
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
			String type = typeEl.isOneSelected() ? typeEl.getSelectedKey() : null;
			itemImpl.setType(type);
			
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
	
	private BigDecimal toBigDecimal(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			return new BigDecimal(val);
		}
		return null;
	}
	
	private int toInt(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			return Integer.parseInt(val);
		}
		return 1;
	}
}