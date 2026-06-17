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
package org.olat.modules.selectus.ui.reference;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.InvitationVariables;
import org.olat.modules.selectus.model.references.TransientReference;

/**
 * 
 * Initial date: 30 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferencePreviewEmailsController extends StepFormBasicController {
	
	private FormLink nextButton;
	private FormLink previousButton;
	private SingleSelection mailListEl;
	private StaticTextElement bodyEl;
	private StaticTextElement subjectEl;

	private final InvitationVariables emailVar;
	
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService recruitingService;
	
	public ReferencePreviewEmailsController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form form, InvitationVariables emailVar) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "mail_preview");
		this.emailVar = emailVar;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousButton = uifactory.addFormLink("previous.mail", formLayout, Link.BUTTON);
		previousButton.setIconLeftCSS("o_icon o_icon_previous");
		nextButton = uifactory.addFormLink("next.mail", formLayout, Link.BUTTON);
		nextButton.setIconRightCSS("o_icon o_icon_next");
		
		SelectionValues appValues = new SelectionValues();
		List<Reference> references = emailVar.getSelectedReferences();
		for(Reference reference:references) {
			String fullName = salutationGenerator.getFullname(reference, getLocale());
			appValues.add(SelectionValues.entry(reference.getKey().toString(), fullName));
		}
		mailListEl = uifactory.addDropdownSingleselect("select.mail", null, formLayout, appValues.keys(), appValues.values(), null);
		mailListEl.setDomReplacementWrapperRequired(false);
		mailListEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer verticalCont = FormLayoutContainer.createVerticalFormLayout("mail", getTranslator());
		formLayout.add(verticalCont);
		subjectEl = uifactory.addStaticTextElement("preview.subject", "", verticalCont);
		bodyEl = uifactory.addStaticTextElement("preview.body", "", verticalCont);
		
		boolean navigation = references.size() > 1;
		previousButton.setVisible(navigation);
		nextButton.setVisible(navigation);
		mailListEl.setVisible(navigation);
		boolean hasContent = !references.isEmpty();
		subjectEl.setVisible(hasContent);
		bodyEl.setVisible(hasContent);
		if(hasContent) {
			select(0);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(previousButton == source) {
			select(mailListEl.getSelected() - 1);
		} else if(nextButton == source) {
			select(mailListEl.getSelected() + 1);
		} else if(mailListEl == source) {
			select(mailListEl.getSelected());
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}

	private void select(int index) {
		if(index < 0) {
			index = 0;
		} else if(index >= mailListEl.getSize()) {
			index = mailListEl.getSize() - 1;
		}
		String nextKey = mailListEl.getKey(index);
		mailListEl.select(nextKey, true);
		selectReference();
		
		if(index <= 0) {
			previousButton.setEnabled(false);
			nextButton.setEnabled(true);
		} else if(index >= mailListEl.getSize() - 1) {
			previousButton.setEnabled(true);
			nextButton.setEnabled(false);
		} else {
			previousButton.setEnabled(true);
			nextButton.setEnabled(true);
		}
	}
	
	private void selectReference() {
		String refKey = mailListEl.getSelectedKey();
		Position position = emailVar.getPosition();
		Date deadline = emailVar.getSubmissionDeadline();
		Reference dbReference = getReference(refKey);
		Reference mailReference = dbReference;
		if(mailReference != null) {
			mailReference = new TransientReference(mailReference, deadline);
			
			ApplicationMailTemplate template;
			if(mailReference.getReferenceType() == ReferenceType.expert) {
				template = emailVar.getExpertTemplate();
			} else if(mailReference.getReferenceType() == ReferenceType.recommendation) {
				template = emailVar.getRecommendationTemplate();
			} else if(mailReference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				template = emailVar.getComparativeExpertTemplate();
			} else {
				return;
			}
			
			Application app = null;
			List<Application> appList = null;
			if(mailReference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				appList = recruitingService.getReferenceToApplicationsList(dbReference);
			} else {
				app = mailReference.getApplication();
			}
			
			MailerSender sender = recruitingService.createMailSender();
			MailerResult mailerResult = new MailerResult();
			SubjectAndBody subjectBody = sender.createWithContext(app, appList, mailReference, null, null, null, position, template, mailerResult);
			subjectEl.setValue(subjectBody.getSubject());
			String content = subjectBody.getBody();
			if(StringHelper.containsNonWhitespace(content) && !StringHelper.isHtml(content)) {
				content = Formatter.escWithBR(content).toString();
			}
			bodyEl.setValue(content);
		}
	}

	private Reference getReference(String referenceKey) {
		return emailVar.getSelectedReferences().stream()
			.filter(app -> app.getKey().toString().equals(referenceKey))
			.findFirst().orElse(null);
	}
}
