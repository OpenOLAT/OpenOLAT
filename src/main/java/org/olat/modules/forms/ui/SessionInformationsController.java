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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;
import org.olat.modules.forms.model.xml.SessionInformations.Obligation;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.ExecutionIdentity;
import org.olat.modules.forms.ui.model.Progress;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SessionInformationsController extends FormBasicController implements EvaluationFormResponseController {

	private List<SessionInformationWrapper> sessionInformationWrappers = new ArrayList<>();
	private FormLink fillInButton;
	
	private final SessionInformations sessionInformations;
	private final ExecutionIdentity executionIdentity;
	private boolean validationEnabled = true;

	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public SessionInformationsController(UserRequest ureq, WindowControl wControl,
			SessionInformations sessionInformations) {
		super(ureq, wControl, LAYOUT_HORIZONTAL);
		this.sessionInformations = sessionInformations;
		this.executionIdentity = new ExecutionIdentity(getIdentity());
		initForm(ureq);
	}

	public SessionInformationsController(UserRequest ureq, WindowControl wControl,
			SessionInformations sessionInformations, Form rootForm, ExecutionIdentity executionIdentity) {
		super(ureq, wControl, LAYOUT_HORIZONTAL, null, rootForm);
		this.sessionInformations = sessionInformations;
		this.executionIdentity = executionIdentity;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		update();
	}

	void update() {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			flc.remove(wrapper.getName());
		}
		if (fillInButton != null) {
			flc.remove(fillInButton);
		}
		
		sessionInformationWrappers = new ArrayList<>();
		for (InformationType informationType: sessionInformations.getInformationTypes()) {
			SessionInformationWrapper wrapper = createWrapper(informationType);
			sessionInformationWrappers.add(wrapper);
		}
		
		fillInButton = uifactory.addFormLink("gi_" + CodeHelper.getRAMUniqueID(), "session.informations.fill.in",
				"session.informations.fill.in.label", flc, Link.BUTTON);
		fillInButton.addActionListener(FormEvent.ONCLICK);
		boolean hasFields = !sessionInformationWrappers.isEmpty();
		fillInButton.setVisible(hasFields && isNotAutoFill());
		
		if (isAutoFill()) {
			doFillIn();
		}
	}

	private SessionInformationWrapper createWrapper(InformationType informationType) {
		String name = "si_" + CodeHelper.getRAMUniqueID();
		TextElement informationEl = uifactory.addTextElement(name, name, "session.informations.label", 400, null, flc);
		String label = SessionInformationsUIFactory.getTranslatedType(informationType, getLocale());
		informationEl.setLabel("session.informations.label", new String[] { label });
		informationEl.setMandatory(isMandatory());
		informationEl.setEnabled(isNotAutoFill());
		return new SessionInformationWrapper(informationType, name, informationEl);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fillInButton) {
			doFillIn();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public void setValidationEnabled(boolean enabled) {
		this.validationEnabled = enabled;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		sessionInformationWrappers.forEach(wrapper -> wrapper.getInformationEl().clearError());
		if (!validationEnabled) return true;
		
		boolean allOk = super.validateFormLogic(ureq);
		
		if (isMandatory()) {
			for (SessionInformationWrapper sessionInformationWrapper : sessionInformationWrappers) {
				TextElement informationEl = sessionInformationWrapper.getInformationEl();
				if (!StringHelper.containsNonWhitespace(informationEl.getValue())) {
					informationEl.setErrorKey("form.legende.mandatory", null);
					allOk = false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

	private void doFillIn() {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			InformationType informationType = wrapper.getInformationType();
			String initialValue = getInitialValue(informationType);
			TextElement informationEl = wrapper.getInformationEl();
			informationEl.setValue(initialValue);
		}
	}

	private String getInitialValue(InformationType informationType) {
		if (informationType.name().startsWith("USER_")) {
			return getUserProperty(informationType);
		}
		return null;
	}

	private String getUserProperty(InformationType informationType) {
		String propertyName = SessionInformationsUIFactory.getUserProperty(informationType);
		return executionIdentity.getUser().getProperty(propertyName, getLocale());
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			wrapper.getInformationEl().setEnabled(!readOnly && isNotAutoFill());
		}
		boolean fillInVisible = !readOnly && !sessionInformationWrappers.isEmpty() && isNotAutoFill();
		fillInButton.setVisible(fillInVisible);
	}

	@Override
	public boolean hasResponse() {
		return true;
	}

	@Override
	public void initResponse(UserRequest ureq, EvaluationFormSession session, EvaluationFormResponses responses) {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			String value = SessionInformationsUIFactory.getValue(wrapper.getInformationType(), session);
			if (StringHelper.containsNonWhitespace(value)) {
				wrapper.getInformationEl().setValue(value);
			}
		}
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		EvaluationFormSession reloadedSession = evaluationFormManager.loadSessionByKey(session);
		if (reloadedSession != null) {
			SessionInformationWrapper emailWrapper = getWrapper(InformationType.USER_EMAIL); 
			String email = emailWrapper != null && StringHelper.containsNonWhitespace(emailWrapper.getValue())
					? emailWrapper.getValue()
					: reloadedSession.getEmail();
			
			SessionInformationWrapper firstnameWrapper = getWrapper(InformationType.USER_FIRSTNAME); 
			String firstname = firstnameWrapper != null && StringHelper.containsNonWhitespace(firstnameWrapper.getValue())
					? firstnameWrapper.getValue()
					: reloadedSession.getFirstname();

			SessionInformationWrapper lastnameWrapper = getWrapper(InformationType.USER_LASTNAME); 
			String lastname = lastnameWrapper != null && StringHelper.containsNonWhitespace(lastnameWrapper.getValue())
					? lastnameWrapper.getValue()
					: reloadedSession.getLastname();

			SessionInformationWrapper ageWrapper = getWrapper(InformationType.AGE); 
			String age = ageWrapper != null && StringHelper.containsNonWhitespace(ageWrapper.getValue())
					? ageWrapper.getValue()
					: reloadedSession.getAge();

			SessionInformationWrapper genderWrapper = getWrapper(InformationType.USER_GENDER); 
			String gender = genderWrapper != null && StringHelper.containsNonWhitespace(genderWrapper.getValue())
					? genderWrapper.getValue()
					: reloadedSession.getGender();

			SessionInformationWrapper orgUnitWrapper = getWrapper(InformationType.USER_ORGUNIT); 
			String orgUnit = orgUnitWrapper != null && StringHelper.containsNonWhitespace(orgUnitWrapper.getValue())
					? orgUnitWrapper.getValue()
					: reloadedSession.getOrgUnit();
			
			SessionInformationWrapper studySubjectWrapper = getWrapper(InformationType.USER_STUDYSUBJECT); 
			String studySubject = studySubjectWrapper != null && StringHelper.containsNonWhitespace(studySubjectWrapper.getValue())
					? studySubjectWrapper.getValue()
					: reloadedSession.getStudySubject();
			
			evaluationFormManager.updateSession(reloadedSession, email, firstname, lastname, age, gender, orgUnit, studySubject);
		}
	}
	

	
	@Override
	public void deleteResponse(EvaluationFormSession session) {
		EvaluationFormSession reloadedSession = evaluationFormManager.loadSessionByKey(session);
		evaluationFormManager.updateSession(reloadedSession, null, null, null, null, null, null, null);
	}
	
	@Override
	public Progress getProgress() {
		int current = isAsLeastOneFilledIn()? 1: 0;
		return Progress.of(current, 1);
	}

	private boolean isAsLeastOneFilledIn() {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			if (StringHelper.containsNonWhitespace(wrapper.getValue())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isMandatory() {
		return Obligation.mandatory.equals(sessionInformations.getObligation());
	}
	
	private boolean isNotAutoFill() {
		return !isAutoFill();
	}
	
	private boolean isAutoFill() {
		return Obligation.autofill.equals(sessionInformations.getObligation());
	}
	
	private SessionInformationWrapper getWrapper(InformationType informationType) {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			if (informationType.equals(wrapper.getInformationType())) {
				return wrapper;
			}
		}
		return null;
	}
	
	public static final class SessionInformationWrapper {
		
		private final InformationType informationType;
		private final String name;
		private final TextElement informationEl;
		
		public SessionInformationWrapper(InformationType informationType, String name, TextElement informationEl) {
			super();
			this.informationType = informationType;
			this.name = name;
			this.informationEl = informationEl;
		}

		public InformationType getInformationType() {
			return informationType;
		}

		public String getName() {
			return name;
		}

		public TextElement getInformationEl() {
			return informationEl;
		}
		
		public String getValue() {
			return informationEl.getValue();
		}
		
	}

}
