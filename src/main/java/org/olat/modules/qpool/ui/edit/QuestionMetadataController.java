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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.ui.QPoolEvent;
import org.olat.modules.qpool.ui.MetadatasController;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionMetadataController extends FormBasicController  {
	
	private FormLink editLink;
	private StaticTextElement typeEl, difficultyEl, stdevDifficultyEl, differentiationEl, numAnswerAltEl, usageEl, assessmentTypeEl;

	public QuestionMetadataController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl, "view");
		setTranslator(Util.createPackageTranslator(MetadatasController.class, ureq.getLocale(), getTranslator()));
	
		initForm(ureq);
		setItem(item);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("question");
		editLink = uifactory.addFormLink("edit", "edit", null, formLayout, Link.BUTTON_XSMALL);
		editLink.setCustomEnabledLinkCSS("b_link_left_icon b_link_edit");
		
		FormLayoutContainer metaCont = FormLayoutContainer.createDefaultFormLayout("metadatas", getTranslator());
		formLayout.add("metadatas", metaCont);
		
		typeEl = uifactory.addStaticTextElement("question.type", "", metaCont);
		difficultyEl = uifactory.addStaticTextElement("question.difficulty", "", metaCont);
		stdevDifficultyEl = uifactory.addStaticTextElement("question.stdevDifficulty", "", metaCont);
		differentiationEl = uifactory.addStaticTextElement("question.differentiation", "", metaCont);
		numAnswerAltEl = uifactory.addStaticTextElement("question.numOfAnswerAlternatives", "", metaCont);
		usageEl = uifactory.addStaticTextElement("question.usage", "", metaCont);
		assessmentTypeEl = uifactory.addStaticTextElement("question.assessmentType", "", metaCont);
	}
	
	public void setItem(QuestionItem item) {
		String type = item.getType() == null ? "" : item.getType();
		typeEl.setValue(type);
		difficultyEl.setValue(toString(item.getDifficulty()));
		stdevDifficultyEl.setValue(toString(item.getStdevDifficulty()));
		differentiationEl.setValue(toString(item.getDifferentiation()));
		numAnswerAltEl.setValue(Integer.toString(item.getNumOfAnswerAlternatives()));
		usageEl.setValue(Integer.toString(item.getUsage()));
		String assessmentType = item.getAssessmentType() == null ?
				"" : translate("question.assessmentType." + item.getAssessmentType());
		assessmentTypeEl.setValue(assessmentType);
	}
	
	private String toString(BigDecimal val) {
		if(val == null) {
			return "";
		} else {
			return val.toPlainString();
		}
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
