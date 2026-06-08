/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmationStepController extends StepFormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement acknowledgeEl;
	
	private final FeedbackMembersContext feedbacksContext;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public ConfirmationStepController(UserRequest ureq, WindowControl wControl,
			FeedbackMembersContext feedbacksContext, StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "confirmation");
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.feedbacksContext = feedbacksContext;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String[] args = new String[] {
				Integer.toString(feedbacksContext.getSelectedApps().size()),	// 0
				Integer.toString(feedbacksContext.getMembers().size()),			// 1
				documentList()													// 2
			};
			String msg = translate("apps.feedback.acknowledge.text", args);
			layoutCont.contextPut("msg", msg);
		}
		
		String[] onValues = new String[] { translate("apps.feedback.acknowledge") };

		acknowledgeEl = uifactory.addCheckboxesHorizontal("acknowledge", null, formLayout, onKeys, onValues);
	}
	
	private String documentList() {
		ApplicationsFeedbackConfiguration configuration = feedbacksContext.getConfiguration();
		Set<String> documents = configuration.getDocuments();
		boolean expertsDocs = configuration.isExpertsDocs();
		boolean refereesdocs = configuration.isRefereesDocs();
		boolean expertsComparativeAssessmentDocs = configuration.isExpertsComparativeAssessmentDocs();
		if(documents.isEmpty() && !expertsDocs && !refereesdocs && !expertsComparativeAssessmentDocs) {
			return "-";
		}
		
		Position position = feedbacksContext.getPosition();
		Set<String> positionDocuments = position.getAvailableDocuments();
		
		StringBuilder sb = new StringBuilder(128);
		for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
			DocumentEnum doc = docOption.getDoc();
			if(documents.contains(doc.name()) && positionDocuments.contains(doc.name())) {
				if(sb.length() > 0) sb.append(", ");
				
				String docName = position.getDocumentName(doc, getLocale());
				if(!StringHelper.containsNonWhitespace(docName)) {
					docName = translate(doc.i18nKey());
				}
				sb.append(docName);
			}
		}
		
		if(expertsDocs) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(translate("edit.application.document.experts.docs.short"));
		}
		if(expertsComparativeAssessmentDocs) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(translate("edit.application.document.comparative.assessment.docs.short"));
		}
		if(refereesdocs) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(translate("edit.application.document.referees.docs.short"));
		}
		
		return sb.toString();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		acknowledgeEl.clearError();
		if(!acknowledgeEl.isAtLeastSelected(1)) {
			acknowledgeEl.setErrorKey("apps.feedback.acknowledge.error");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
