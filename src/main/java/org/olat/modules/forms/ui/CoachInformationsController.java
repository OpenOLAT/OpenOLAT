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
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.BlockLayoutClassFactory;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.CoachCandidates;
import org.olat.modules.forms.CoachCandidatesAware;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.CoachInformations;
import org.olat.modules.forms.model.xml.FormXStream;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;
import org.olat.modules.forms.model.xml.SessionInformations.Obligation;
import org.olat.modules.forms.model.xml.UserInfo;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.Progress;
import org.olat.user.PortraitSize;
import org.olat.user.PortraitUser;
import org.olat.user.UserPortraitComponent;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 19, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CoachInformationsController extends FormBasicController
		implements EvaluationFormResponseController, CoachCandidatesAware {

	private static final String COACH_SELECTION_PREFIX = "ci_sel_";
	
	private DropdownItem coachDropdown;
	private List<FormLink> coachLinks = List.of();
	private List<SessionInformationWrapper> sessionInformationWrappers = List.of();
	
	private CoachInformations coachInformations;
	private boolean readOnly = false;
	private boolean validationEnabled = true;
	private EvaluationFormResponse response;

	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private UserPortraitService portraitService;

	public CoachInformationsController(UserRequest ureq, WindowControl wControl, CoachInformations coachInformations) {
		super(ureq, wControl, "coach_information");
		this.coachInformations = coachInformations;
		initForm(ureq);
		setBlockLayoutClass(coachInformations.getLayoutSettings());
		
		CoachCandidates coachCandidates = roles -> Set.of(getIdentity());
		initCoachCandidates(coachCandidates);
	}

	public CoachInformationsController(UserRequest ureq, WindowControl wControl, CoachInformations coachInformations, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "coach_information", rootForm);
		this.coachInformations = coachInformations;
		initForm(ureq);
		setBlockLayoutClass(coachInformations.getLayoutSettings());
	}

	private void setBlockLayoutClass(BlockLayoutSettings layoutSettings) {
		this.setFormStyle("o_form_session_info o_form_two_cols o_form_element " + BlockLayoutClassFactory.buildClass(layoutSettings, true));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		update();
	}

	void update() {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			flc.remove(wrapper.getName());
		}
		if (coachDropdown != null) {
			flc.remove(coachDropdown);
		}
		
		coachDropdown = uifactory.addDropdownMenu("gi_" + CodeHelper.getRAMUniqueID(), "coach.information.apply", null, flc, getTranslator());
		coachDropdown.setIconCSS("o_icon o_icon_eva_coach_apply");
		coachDropdown.addActionListener(FormEvent.ONCLICK);
		flc.contextPut("coachDropDownName", coachDropdown.getName());
		
		sessionInformationWrappers = new ArrayList<>(coachInformations.getInformationTypes().size());
		for (InformationType informationType: coachInformations.getInformationTypes()) {
			SessionInformationWrapper wrapper = createWrapper(informationType);
			sessionInformationWrappers.add(wrapper);
		}
		flc.contextPut("wrappers", sessionInformationWrappers);
	}

	private SessionInformationWrapper createWrapper(InformationType informationType) {
		String name = "si_" + CodeHelper.getRAMUniqueID();
		TextElement informationEl = uifactory.addTextElement(name, name, "session.informations.label", 400, null, flc);
		String label = SessionInformationsUIFactory.getTranslatedType(informationType, getLocale());
		informationEl.setLabel("session.informations.label", new String[] { label });
		informationEl.setMandatory(isMandatory());
		informationEl.setEnabled(isObligationEditable());
		return new SessionInformationWrapper(informationType, name, informationEl);
	}

	@Override
	public void initCoachCandidates(CoachCandidates coachCandidates) {
		coachDropdown.removeAllFormItems();

		Set<Identity> coaches = coachCandidates.getCoaches(coachInformations.getRoles());
		coachLinks = new ArrayList<>(coaches.size());
		coaches.stream()
			.map(this::createCoachLink)
			.sorted((l1, l2) -> l1.getI18nKey().compareToIgnoreCase(l2.getI18nKey()))
			.forEach(coachLink -> coachDropdown.addElement(coachLink));
		
		updateUI();
	}

	private FormLink createCoachLink(Identity coach) {
		UserDropdownComponent userDropdownComp = new UserDropdownComponent("odc_" + CodeHelper.getRAMUniqueID(), coach);
		
		FormLink coachLink = uifactory.addFormLink(COACH_SELECTION_PREFIX + CodeHelper.getRAMUniqueID(), null, null, flc, Link.LINK + Link.NONTRANSLATED);
		coachLink.setI18nKey(userDropdownComp.getPortraitUser().getDisplayName());
		coachLink.setInnerComponent(userDropdownComp);
		coachLink.setUserObject(coach);
		coachLinks.add(coachLink);
		return coachLink;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof ChangePartEvent cpe && cpe.getElement() instanceof CoachInformations coachInformations) {
			this.coachInformations = coachInformations;
			update();
			setBlockLayoutClass(coachInformations.getLayoutSettings());
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link) { 
			if (link.getName().startsWith(COACH_SELECTION_PREFIX)) {
				if (coachLinks.contains(link)) {
					if (link.getUserObject() instanceof Identity coach) {
						doCoachSelected(coach);
					}
				}
			}
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
		flc.contextRemove("error");
		if (!validationEnabled) return true;
		
		boolean allOk = super.validateFormLogic(ureq);
		
		if (isMandatory()) {
			for (SessionInformationWrapper sessionInformationWrapper : sessionInformationWrappers) {
				TextElement informationEl = sessionInformationWrapper.getInformationEl();
				if (!StringHelper.containsNonWhitespace(informationEl.getValue())) {
					informationEl.setErrorKey("form.legende.mandatory");
					allOk = false;
				}
			}
		} else if (isMandatoryNotEditable()) {
			if (!isAnyValueAvailable()) {
				flc.contextPut("error", translate("coach.information.error.mandatory"));
				allOk = false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doCoachSelected(Identity coach) {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			InformationType informationType = wrapper.getInformationType();
			String initialValue = getInitialValue(coach, informationType);
			TextElement informationEl = wrapper.getInformationEl();
			informationEl.setValue(initialValue);
		}
	}

	private String getInitialValue(Identity coach, InformationType informationType) {
		if (informationType.name().startsWith("USER_")) {
			return getUserProperty(coach, informationType);
		}
		return null;
	}

	private String getUserProperty(Identity coach, InformationType informationType) {
		String propertyName = SessionInformationsUIFactory.getUserProperty(informationType);
		return coach.getUser().getProperty(propertyName, getLocale());
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		updateUI();
	}

	private void updateUI() {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			wrapper.getInformationEl().setEnabled(!readOnly && isObligationEditable());
		}
		
		boolean fillInVisible = !readOnly && coachDropdown.size() > 0;
		coachDropdown.setVisible(fillInVisible);
		if(!readOnly && coachDropdown.size() == 0 && isMandatoryNotEditable()) {
			flc.contextPut("warning", translate("coach.information.error.no.coaches.available"));
		} else {
			flc.contextRemove("warning");
		}
	}

	@Override
	public boolean hasResponse() {
		if (isMandatory()) {
			return isAnyValueAvailable();
		}
		
		return true;
	}
	
	private boolean isAnyValueAvailable() {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			String value = wrapper.getInformationEl().getValue();
			if (StringHelper.containsNonWhitespace(value)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void initResponse(UserRequest ureq, EvaluationFormSession session, EvaluationFormResponses responses) {
		response = responses.getResponse(session, coachInformations.getId());
		if (response != null) {
			if (StringHelper.containsNonWhitespace(response.getStringuifiedResponse())) {
				UserInfo userInfo = FormXStream.fromXml(response.getStringuifiedResponse(), UserInfo.class);
				fromUserInfo(userInfo);
			}
		}
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		if (isAnyValueAvailable()) {
			UserInfo userInfo = toUserInfo();
			String xmlValue = FormXStream.toXml(userInfo);
			if (response == null) {
				EvaluationFormSession reloadedSession = evaluationFormManager.loadSessionByKey(session);
				evaluationFormManager.createStringResponse(coachInformations.getId(), reloadedSession, xmlValue);
			} else {
				evaluationFormManager.updateStringResponse(response, xmlValue);
			}
		} else if (response != null) {
			evaluationFormManager.deleteResponse(response);
		}
	}
	
	private UserInfo toUserInfo() {
		UserInfo userInfo = new UserInfo();
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			String value = wrapper.getInformationEl().getValue();
			if (StringHelper.containsNonWhitespace(value)) {
				if (InformationType.USER_FIRSTNAME == wrapper.getInformationType()) {
					userInfo.setFirstName(value);
				} else if (InformationType.USER_LASTNAME == wrapper.getInformationType()) {
					userInfo.setLastName(value);
				} else if (InformationType.USER_EMAIL == wrapper.getInformationType()) {
					userInfo.setEmail(value);
				}
			}
		}
		return userInfo;
	}
	
	private void fromUserInfo(UserInfo userInfo) {
		for (SessionInformationWrapper wrapper: sessionInformationWrappers) {
			wrapper.getInformationEl().setValue(SessionInformationsUIFactory.getValue(wrapper.getInformationType(), userInfo));
		}
	}

	@Override
	public void deleteResponse(EvaluationFormSession session) {
		if (response != null) {
			evaluationFormManager.deleteResponse(response);
			response = null;
		}
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
		return Obligation.mandatory.equals(coachInformations.getObligation());
	}
	
	private boolean isMandatoryNotEditable() {
		return Obligation.autofill.equals(coachInformations.getObligation());
	}
	
	private boolean isObligationEditable() {
		return ! Obligation.autofill.equals(coachInformations.getObligation());
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
	
	/**
	 * Prototype for a user dropdown component.
	 * Feel free to convert it in a standalone, reusable component.
	 * 
	 * Initial date: Dec 23, 2025<br>
	 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
	 *
	 */
	public class UserDropdownComponent extends AbstractComponent {
		
		private static final ComponentRenderer RENDERER = new UserDropdownRenderer();
		
		private final PortraitUser portraitUser;
		private final UserPortraitComponent userPortraitComp;
		
		public UserDropdownComponent(String name, Identity identity) {
			super(name);
			portraitUser = portraitService.createPortraitUser(getLocale(), identity);
			userPortraitComp = UserPortraitFactory.createUserPortrait("user_" + CodeHelper.getRAMUniqueID(), flc.getFormItemComponent(), getLocale());
			userPortraitComp.setSize(PortraitSize.	small);
			userPortraitComp.setPortraitUser(portraitUser);
		}
		
		@Override
		protected void doDispatchRequest(UserRequest ureq) {
			//
		}
		
		@Override
		public ComponentRenderer getHTMLRendererSingleton() {
			return RENDERER;
		}
		
		public PortraitUser getPortraitUser() {
			return portraitUser;
		}
		
		public UserPortraitComponent getUserPortraitComp() {
			return userPortraitComp;
		}
		
		private static class UserDropdownRenderer extends DefaultComponentRenderer {
			
			@Override
			public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
					Translator translator, RenderResult renderResult, String[] args) {
				
				UserDropdownComponent udc = (UserDropdownComponent)source;
				
				sb.append("<div class=\"o_user_dropdown_user\">");
				renderer.render(udc.getUserPortraitComp(), sb, args);
				sb.append("<div class=\"o_user_dropdown_name\">");
				sb.append(StringHelper.escapeHtml(udc.getPortraitUser().getDisplayName()));
				sb.append("</div>");
				sb.append("</div>");
			}
			
		}
		
	}

}
