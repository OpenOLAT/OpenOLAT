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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QPoolEvent;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionMetadataController extends FormBasicController  {
	
	private FormLink editLink;
	private StaticTextElement typeEl, difficultyEl, stdevDifficultyEl, differentiationEl, numAnswerAltEl, usageEl, assessmentTypeEl;

	private final boolean edit;
	
	public QuestionMetadataController(UserRequest ureq, WindowControl wControl, QuestionItem item, boolean edit) {
		super(ureq, wControl, "view");
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
	
		this.edit = edit;
		initForm(ureq);
		setItem(item);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("question");
		if(edit) {
			editLink = uifactory.addFormLink("edit", "edit", null, formLayout, Link.BUTTON_XSMALL);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		}
		
		typeEl = uifactory.addStaticTextElement("question.type", "", formLayout);
		difficultyEl = uifactory.addStaticTextElement("question.difficulty", "", formLayout);
		stdevDifficultyEl = uifactory.addStaticTextElement("question.stdevDifficulty", "", formLayout);
		differentiationEl = uifactory.addStaticTextElement("question.differentiation", "", formLayout);
		numAnswerAltEl = uifactory.addStaticTextElement("question.numOfAnswerAlternatives", "", formLayout);
		usageEl = uifactory.addStaticTextElement("question.usage", "", formLayout);
		assessmentTypeEl = uifactory.addStaticTextElement("question.assessmentType", "", formLayout);
	}
	
	public void setItem(QuestionItem item) {
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
		
		difficultyEl.setValue(MetaUIFactory.bigDToString(item.getDifficulty()));
		stdevDifficultyEl.setValue(MetaUIFactory.bigDToString(item.getStdevDifficulty()));
		differentiationEl.setValue(MetaUIFactory.bigDToString(item.getDifferentiation()));
		numAnswerAltEl.setValue(Integer.toString(item.getNumOfAnswerAlternatives()));
		usageEl.setValue(Integer.toString(item.getUsage()));
		String assessmentType = item.getAssessmentType() == null ?
				"" : translate("question.assessmentType." + item.getAssessmentType());
		assessmentTypeEl.setValue(assessmentType);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editLink == source) {
			fireEvent(ureq, new QPoolEvent(QPoolEvent.EDIT));
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
}
