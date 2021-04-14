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
package org.olat.registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Organisation;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositoryEntryFilter;
import org.olat.repository.controllers.RepositorySearchController.Can;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin panel to configure the registration settings: should link appear on the login page...
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RegistrationAdminController extends FormBasicController {

	private SingleSelection organisationsEl;
	private MultipleSelectionElement registrationElement;
	private MultipleSelectionElement registrationLinkElement;
	private MultipleSelectionElement registrationLoginElement;
	private MultipleSelectionElement staticPropElement;
	private MultipleSelectionElement emailValidationEl;
	private SingleSelection propertyElement;
	private SingleSelection pendingRegistrationStatusEl;
	private TextElement pendingRegistrationNotificationEl;
	private PropertyNameValueElements pendingProperty1Els;
	private PropertyNameValueElements pendingProperty2Els;
	private PropertyNameValueElements pendingProperty3Els;
	private PropertyNameValueElements pendingProperty4Els;
	private PropertyNameValueElements pendingProperty5Els;
	private TextElement propertyValueElement;
	private TextElement exampleElement;
	private TextElement domainListElement;
	private TextElement validUntilGuiEl;
	private TextElement validUntilRestEl;
	private TextElement expirationDateDaysEl;
	private FormLayoutContainer domainsContainer;
	private FormLayoutContainer pendingPropContainer;
	private FormLayoutContainer staticPropContainer;
	private FormLayoutContainer autoEnrolmentCoursesContainer;
	private FormLink openCourseBrowserLink;
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController selectCoursesController;
	
	private static final String[] pendingRegistrationKeys = new String[] { 
			RegistrationPendingStatus.active.name(),
			RegistrationPendingStatus.pending.name(),
			RegistrationPendingStatus.pendingMatchingProperties.name()
		};
	private static final String[] enableRegistrationKeys = new String[]{ "on" };
	private final String[] propertyKeys;
	private final String[] propertyValues;
	private final String[] pendingPropertyKeys;
	private final String[] pendingPropertyValues;
	
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private UserPropertiesConfig userPropertiesConfig;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AccessControlModule acModule;
	
	private final Translator userPropTranslator;
	
	public RegistrationAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin");
		
		//decorate the translator
		userPropTranslator = userPropertiesConfig.getTranslator(getTranslator());

		List<UserPropertyHandler> allPropertyHandlers = userPropertiesConfig.getAllUserPropertyHandlers();
		List<UserPropertyHandler> propertyHandlers = new ArrayList<>(allPropertyHandlers.size());
		for(UserPropertyHandler handler:allPropertyHandlers) {
			if(handler instanceof Generic127CharTextPropertyHandler
					&& !UserConstants.USERNAME.equals(handler.getName())) {
				propertyHandlers.add(handler);
			}
		}

		propertyKeys = new String[propertyHandlers.size() + 1];
		propertyValues = new String[propertyHandlers.size() + 1];
		int count = 0;
		propertyKeys[0] = "-";
		propertyValues[0] = "";
		for(UserPropertyHandler propertyHandler:propertyHandlers) {
			propertyKeys[1 + count] = propertyHandler.getName();
			propertyValues[1 + count++] = userPropTranslator.translate(propertyHandler.i18nFormElementLabelKey());
		}
		
		pendingPropertyKeys = new String[allPropertyHandlers.size() + 1];
		pendingPropertyValues = new String[allPropertyHandlers.size() + 1];
		count = 0;
		pendingPropertyKeys[0] = "-";
		pendingPropertyValues[0] = "";
		for(UserPropertyHandler propertyHandler:allPropertyHandlers) {
			pendingPropertyKeys[1 + count] = propertyHandler.getName();
			pendingPropertyValues[1 + count++] = userPropTranslator.translate(propertyHandler.i18nFormElementLabelKey());
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] enableRegistrationValues = new String[] { translate("admin.enableRegistration.on") };

		//settings
		FormLayoutContainer settingsContainer = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		settingsContainer.setRootForm(mainForm);
		settingsContainer.setFormTitle(translate("admin.registration.title"));
		formLayout.add(settingsContainer);
		
		registrationElement = uifactory.addCheckboxesHorizontal("enable.self.registration", "admin.enableRegistration", settingsContainer, enableRegistrationKeys, enableRegistrationValues);
		registrationElement.addActionListener(FormEvent.ONCHANGE);
		registrationElement.select("on", registrationModule.isSelfRegistrationEnabled());
		
		registrationLoginElement = uifactory.addCheckboxesHorizontal("enable.registration.login", "admin.enableRegistrationLogin", settingsContainer, enableRegistrationKeys, enableRegistrationValues);
		registrationLoginElement.addActionListener(FormEvent.ONCHANGE);
		registrationLoginElement.select("on", registrationModule.isSelfRegistrationLoginEnabled());

		registrationLinkElement = uifactory.addCheckboxesHorizontal("enable.registration.link", "admin.enableRegistrationLink", settingsContainer, enableRegistrationKeys, enableRegistrationValues);
		registrationLinkElement.addActionListener(FormEvent.ONCHANGE);
		registrationLinkElement.select("on", registrationModule.isSelfRegistrationLinkEnabled());
		
		emailValidationEl = uifactory.addCheckboxesHorizontal("email.validation", "admin.enable.email.validation", settingsContainer, enableRegistrationKeys, enableRegistrationValues);
		emailValidationEl.addActionListener(FormEvent.ONCHANGE);
		emailValidationEl.select("on", registrationModule.isEmailValidationEnabled());
		
		initOrganisationsEl(settingsContainer);
		
		initAutoEnrolment(settingsContainer);
		
		validUntilGuiEl = uifactory.addTextElement("admin.registration.valid.until.gui", 20, registrationModule.getValidUntilHoursGui().toString(), settingsContainer);
		validUntilGuiEl.setMandatory(true);
		validUntilRestEl = uifactory.addTextElement("admin.registration.valid.until.rest", 20, registrationModule.getValidUntilHoursRest().toString(), settingsContainer);
		validUntilRestEl.setMandatory(true);
		
		Integer expirationInDays = registrationModule.getAccountExpirationInDays();
		expirationDateDaysEl = uifactory.addTextElement("admin.registration.account.expiration.days", 20,
				expirationInDays == null ? "" : expirationInDays.toString(), settingsContainer);

		String example = generateExampleCode();
		exampleElement = uifactory.addTextAreaElement("registration.link.example", "admin.registrationLinkExample", 64000, 4, 65, true, false, example, settingsContainer);
		
		// pedning status
		initPendingPropForm(formLayout);
		//domain configuration
		initDomainForm(formLayout);
		//static property
		initStaticPropForm(formLayout, enableRegistrationValues);
		//static property
		initRemoteLogin(formLayout);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonGroupLayout);
		formLayout.add(buttonGroupLayout);
		
		updateUI();	
	}
	
	private void initAutoEnrolment(FormItemContainer formLayout) {
		String page = Util.getPackageVelocityRoot(getClass()) + "/auto_enrolment_courses.html";
		autoEnrolmentCoursesContainer = FormLayoutContainer.createCustomFormLayout("auto_enrolment", getTranslator(), page);
		autoEnrolmentCoursesContainer.setRootForm(mainForm);
		autoEnrolmentCoursesContainer.setLabel("auto.enrolment.courses.label", null);
		openCourseBrowserLink = uifactory.addFormLink("auto.enrolment.select.courses", autoEnrolmentCoursesContainer, Link.BUTTON_XSMALL);
		
		Map<Long, String> courseNames = new HashMap<>();
		
		for (Long courseKey : registrationModule.getAutoEnrolmentCourseKeys()) {
			String courseName = repositoryService.loadByKey(courseKey).getDisplayname();
			FormLink removeLink = uifactory.addFormLink("remove_course_" + courseKey.toString(), "remove_course", "remove", null, autoEnrolmentCoursesContainer, Link.LINK);
			removeLink.setElementCssClass("o_button_textstyle");
			removeLink.setUserObject(courseKey);
			
			courseNames.put(courseKey, courseName);
		}		
		
		autoEnrolmentCoursesContainer.contextPut("autoEnrolmentCourseKeys", registrationModule.getAutoEnrolmentCourseKeys());
		autoEnrolmentCoursesContainer.contextPut("autoEnrolmentCourseNames", courseNames);
		autoEnrolmentCoursesContainer.contextPut("autoBooking", acModule.isAutoEnabled());
		
		formLayout.add(autoEnrolmentCoursesContainer);
	}
	
	private void initDomainForm(FormItemContainer formLayout) {
		domainsContainer = FormLayoutContainer.createDefaultFormLayout("domains", getTranslator());
		domainsContainer.setRootForm(mainForm);
		domainsContainer.setFormTitle(translate("admin.registration.domains.title"));
		formLayout.add(domainsContainer);
		
		uifactory.addStaticTextElement("admin.registration.domains.error", null, translate("admin.registration.domains.desc"), domainsContainer);
		String domainsList = registrationModule.getDomainListRaw();
		domainListElement = uifactory.addTextAreaElement("registration.domain.list", "admin.registration.domains", 64000, 10, 65, true, false, domainsList, domainsContainer);
	}
	
	private void initPendingPropForm(FormItemContainer formLayout) {
		pendingPropContainer = FormLayoutContainer.createDefaultFormLayout("propertiespending", getTranslator());
		pendingPropContainer.setRootForm(mainForm);
		pendingPropContainer.setFormTitle(translate("admin.registration.pending.status"));
		formLayout.add(pendingPropContainer);
		
		String[] pendingRegistrationValues = new String[] {
				translate("registration.pending.status.active"),
				translate("registration.pending.status.pending"),
				translate("registration.pending.status.pending.props")
			};
		pendingRegistrationStatusEl = uifactory.addDropdownSingleselect("registration.pending.status", pendingPropContainer, pendingRegistrationKeys, pendingRegistrationValues);
		pendingRegistrationStatusEl.select(registrationModule.getRegistrationPendingStatus().name(), true);
		pendingRegistrationStatusEl.addActionListener(FormEvent.ONCHANGE);
	
		pendingProperty1Els = initPendingProperty(1, registrationModule.getRegistrationPendingPropertyName1(), registrationModule.getRegistrationPendingPropertyValue1(), pendingPropContainer);
		pendingProperty2Els = initPendingProperty(2, registrationModule.getRegistrationPendingPropertyName2(), registrationModule.getRegistrationPendingPropertyValue2(), pendingPropContainer);
		pendingProperty3Els = initPendingProperty(3, registrationModule.getRegistrationPendingPropertyName3(), registrationModule.getRegistrationPendingPropertyValue3(), pendingPropContainer);
		pendingProperty4Els = initPendingProperty(4, registrationModule.getRegistrationPendingPropertyName4(), registrationModule.getRegistrationPendingPropertyValue4(), pendingPropContainer);
		pendingProperty5Els = initPendingProperty(5, registrationModule.getRegistrationPendingPropertyName5(), registrationModule.getRegistrationPendingPropertyValue5(), pendingPropContainer);

		String email = "";
		if(registrationModule.isRegistrationNotificationEmailEnabled()) {
			email = registrationModule.getRegistrationNotificationEmail();
		}
		pendingRegistrationNotificationEl = uifactory.addTextElement("registration.pending.notification.mail", 2048, email, pendingPropContainer);
	}
	
	private PropertyNameValueElements initPendingProperty(int pos, String propName, String propValue, FormLayoutContainer formLayout) {
		SingleSelection pendingPropertyNameEl = uifactory.addDropdownSingleselect("registration.pending.prop.name" + pos, formLayout, pendingPropertyKeys, pendingPropertyValues);
		boolean found = false;
		for(int i=pendingPropertyKeys.length; i-->0; ) {
			if(pendingPropertyKeys[i].equals(propName)) {
				pendingPropertyNameEl.select(pendingPropertyKeys[i], true);
				found = true;
				break;
			}
		}
		if(!found) {
			pendingPropertyNameEl.select(pendingPropertyKeys[0], true);
		}

		TextElement pendingPropertyValueEl = uifactory.addTextElement("registration.pending.prop.value" + pos, 2048, propValue, formLayout);
		return new PropertyNameValueElements(pendingPropertyNameEl, pendingPropertyValueEl);
	}
	
	private void initStaticPropForm(FormItemContainer formLayout, String[] enableRegistrationValues) {
		staticPropContainer = FormLayoutContainer.createDefaultFormLayout("propertiesmapping", getTranslator());
		staticPropContainer.setRootForm(mainForm);
		staticPropContainer.setFormTitle(translate("admin.registration.staticprop.title"));
		formLayout.add(staticPropContainer);
		
		uifactory.addStaticTextElement("admin.registration.staticprop.error", null, translate("admin.registration.staticprop.desc"), staticPropContainer);
		
		staticPropElement = uifactory.addCheckboxesHorizontal("enable.staticprop", "admin.enableStaticProp", staticPropContainer, enableRegistrationKeys, enableRegistrationValues);
		staticPropElement.addActionListener(FormEvent.ONCHANGE);
		staticPropElement.select("on", registrationModule.isStaticPropertyMappingEnabled());

		propertyElement = uifactory.addDropdownSingleselect("property", "admin.registration.property", staticPropContainer, propertyKeys, propertyValues, null);
		String propertyName = registrationModule.getStaticPropertyMappingName();
		UserPropertyHandler handler = userPropertiesConfig.getPropertyHandler(propertyName);
		if(handler != null) {
			propertyElement.select(handler.getName(), true);
		}
		propertyElement.addActionListener(FormEvent.ONCHANGE);
		
		String propertyValue = registrationModule.getStaticPropertyMappingValue();
		propertyValueElement = uifactory.addTextElement("admin.registration.prop.value", "admin.registration.propertyValue", 255, propertyValue, staticPropContainer);
	}
	
	private void initRemoteLogin(FormItemContainer formLayout) {
		FormLayoutContainer remoteLoginContainerContainer = FormLayoutContainer.createDefaultFormLayout("remotelogin", getTranslator());
		remoteLoginContainerContainer.setRootForm(mainForm);
		remoteLoginContainerContainer.setFormTitle(translate("remote.login.title"));
		formLayout.add(remoteLoginContainerContainer);
		
		String remoteExample = generateRemoteLoginExampleCode();
		uifactory.addTextAreaElement("remotelogin.example", "admin.registrationLinkExample", 64000, 4, 65, true, false, remoteExample, remoteLoginContainerContainer);
	}
	
	private void initOrganisationsEl(FormLayoutContainer formLayout) {
		List<Organisation> organisations = organisationService.getOrganisations(OrganisationStatus.notDelete());
		Organisation registrationOrg = registrationManager.getOrganisationForRegistration();
		String registrationOrgKey = registrationOrg.getKey().toString();
		
		String[] theKeys = new String[organisations.size()];
		String[] theValues = new String[organisations.size()];
		for(int i=organisations.size(); i-->0; ) {
			Organisation organisation = organisations.get(i);
			theKeys[i] = organisation.getKey().toString();
			theValues[i] = organisation.getDisplayName();
		}
		organisationsEl = uifactory.addDropdownSingleselect("organisations", "admin.registrationOrganisation", formLayout, theKeys, theValues);
		for(int i=theKeys.length; i-->0; ) {
			if(theKeys[i].equals(registrationOrgKey)) {
				organisationsEl.select(theKeys[i], true);
				break;
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == registrationElement) {
			boolean  enable = registrationElement.isSelected(0);
			registrationModule.setSelfRegistrationEnabled(enable);
			updateUI();
		} else if(source == registrationLinkElement) {
			registrationModule.setSelfRegistrationLinkEnabled(registrationLinkElement.isSelected(0));
			updateUI();
		} else if(source == registrationLoginElement) {
			registrationModule.setSelfRegistrationLoginEnabled(registrationLoginElement.isSelected(0));
			updateUI();
		} else if(source == emailValidationEl) {
			registrationModule.setEmailValidationEnabled(emailValidationEl.isSelected(0));
		} else if (source == staticPropElement) {
			registrationModule.setStaticPropertyMappingEnabled(staticPropElement.isSelected(0));
			updateUI();
		} else if(source == pendingRegistrationStatusEl) {
			updateUI();
		} else if(source == openCourseBrowserLink) {
			openCourseBrowser(ureq);
		} else if (source instanceof FormLink) {
			if (((FormLink) source).getCmd().equals("remove_course")) {
				if (source.getUserObject() instanceof Long) {
					registrationModule.removeCourseFromAutoEnrolment((Long) source.getUserObject());
					initForm(ureq);
				}
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == selectCoursesController) {
			if (event.equals(ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) ||
				event.equals(ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED)) {
				
				for (RepositoryEntry entry : selectCoursesController.getSelectedEntries()) {
					registrationModule.addCourseToAutoEnrolment(entry.getKey());
				}
				
				initForm(ureq);
			} 
			
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
	}
	
	private void openCourseBrowser(UserRequest ureq) {
		cleanUp();
		
		selectCoursesController = new ReferencableEntriesSearchController(getWindowControl(), ureq, new String[]{ CourseModule.getCourseTypeName() }, new AutoEnrolmentCourseFilter(), null, translate("auto.enrolment.add"), false, false, true, false, true, false, Can.all);
		listenTo(selectCoursesController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectCoursesController.getInitialComponent(), true);
		listenTo(cmc);
		
		cmc.activate();
	}
	
	private void cleanUp() {
		if (cmc != null && cmc.isCloseable()) {
			cmc.deactivate();
		}
		
		removeAsListenerAndDispose(selectCoursesController);
		removeAsListenerAndDispose(cmc);
		
		selectCoursesController = null;
		cmc = null;
	}
	
	private void updateUI() {
		boolean  enableMain = registrationElement.isSelected(0);
		registrationLinkElement.setEnabled(enableMain);
		registrationLoginElement.setEnabled(enableMain);
		organisationsEl.setEnabled(enableMain);
		
		boolean example = enableMain && registrationLinkElement.isSelected(0);
		exampleElement.setVisible(example);
		
		boolean enableDomains = enableMain && (registrationLinkElement.isSelected(0) || registrationLoginElement.isSelected(0));
		domainsContainer.setVisible(enableDomains);
		
		pendingPropContainer.setVisible(enableMain);
		if(enableMain) {
			String selectedStatus = pendingRegistrationStatusEl.getSelectedKey();
			boolean useProps = RegistrationPendingStatus.pendingMatchingProperties.name().equals(selectedStatus);
			pendingProperty1Els.setVisible(useProps);
			pendingProperty2Els.setVisible(useProps);
			pendingProperty3Els.setVisible(useProps);
			pendingProperty4Els.setVisible(useProps);
			pendingProperty5Els.setVisible(useProps);

			boolean mail = RegistrationPendingStatus.pendingMatchingProperties.name().equals(selectedStatus)
					|| RegistrationPendingStatus.pending.name().equals(selectedStatus);
			pendingRegistrationNotificationEl.setVisible(mail);
		}
		
		//static prop
		boolean enableProps = enableMain && (registrationLinkElement.isSelected(0) || registrationLoginElement.isSelected(0));
		staticPropContainer.setVisible(enableProps);
		boolean enabledProp = staticPropElement.isSelected(0);
		propertyElement.setVisible(enableProps && enabledProp);
		propertyValueElement.setVisible(enableProps && enabledProp);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateInteger(validUntilGuiEl, 1);
		allOk &= validateInteger(validUntilRestEl, 1);
		allOk &= validateInteger(this.expirationDateDaysEl, 1);
		
		allOk &= validatePropertyNameValuePair(pendingProperty1Els);
		allOk &= validatePropertyNameValuePair(pendingProperty2Els);
		allOk &= validatePropertyNameValuePair(pendingProperty3Els);
		allOk &= validatePropertyNameValuePair(pendingProperty4Els);
		allOk &= validatePropertyNameValuePair(pendingProperty5Els);
		
		allOk &= validateEmail(pendingRegistrationNotificationEl);
		
		String whiteList = domainListElement.getValue();
		domainListElement.clearError();
		if(StringHelper.containsNonWhitespace(whiteList)) {
			List<String> normalizedList = registrationModule.getDomainList(whiteList);
			List<String> errors = registrationManager.validateWhiteList(normalizedList);
			if(!errors.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for(String error:errors) {
					if(sb.length() > 0) sb.append(" ,");
					sb.append(error);
				}
				domainListElement.setErrorKey("admin.registration.domains.error", new String[]{sb.toString()});
				allOk &= false;
			}
		}
		
		if(staticPropElement.isSelected(0)) {
			if(propertyElement.isOneSelected()) {
				String propertyName = propertyElement.getSelectedKey();
				String value = propertyValueElement.getValue();
				UserPropertyHandler handler = userPropertiesConfig.getPropertyHandler(propertyName);
				if(handler != null) {
					ValidationError validationError = new ValidationError();
					boolean valid = handler.isValidValue(null, value, validationError, getLocale());
					if(!valid) {
						propertyValueElement.setErrorKey("admin.registration.propertyValue.error", null);
						allOk &= false;
					}
				}
			}
		}
		
		organisationsEl.clearError();
		if(organisationsEl.isEnabled() && !organisationsEl.isOneSelected()) {
			organisationsEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean validateEmail(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())) {
			String[] emails = el.getValue().split("[,]");
			for(String email:emails) {
				if(!MailHelper.isValidEmailAddress(email)) {
					el.setErrorKey("email.address.notregular", null);
					allOk &= false;
					break;
				}
			}
		}

		return allOk;
	}
	
	private boolean validatePropertyNameValuePair(PropertyNameValueElements nameValueEls) {
		boolean allOk = true;
		
		SingleSelection nameEl = nameValueEls.getPropertyNameEl();
		TextElement valueEl = nameValueEls.getPropertyValueEl();
		
		nameEl.clearError();
		valueEl.clearError();
		if(!nameEl.isOneSelected()) {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!nameEl.getSelectedKey().equals(propertyKeys[0])
				&& !StringHelper.containsNonWhitespace(valueEl.getValue())) {
			valueEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean validateInteger(TextElement el, int min) {
		boolean allOk = true;
		el.clearError();
		String val = el.getValue();
		if(StringHelper.containsNonWhitespace(val)) {	
			try {
				int value = Integer.parseInt(val);
				if(min > value) {
					el.setErrorKey("error.wrong.int", null);
					allOk = false;
				}
			} catch (NumberFormatException e) {
				el.setErrorKey("error.wrong.int", null);
				allOk = false;
			}
		} else if(el.isMandatory()) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		}
		return allOk;	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		registrationModule.setSelfRegistrationEnabled(registrationElement.isSelected(0));
		registrationModule.setSelfRegistrationLinkEnabled(registrationLinkElement.isSelected(0));
		registrationModule.setSelfRegistrationLoginEnabled(registrationLoginElement.isSelected(0));
		
		if(organisationsEl.isOneSelected()) {
			registrationModule.setselfRegistrationOrganisationKey(organisationsEl.getSelectedKey());
		}
		if(pendingRegistrationStatusEl.isOneSelected()) {
			registrationModule.setRegistrationPendingStatus(RegistrationPendingStatus.valueOf(pendingRegistrationStatusEl.getSelectedKey()));
		}
		
		registrationModule.setRegistrationPendingProperty1(pendingProperty1Els.getName(), pendingProperty1Els.getValue());
		registrationModule.setRegistrationPendingProperty2(pendingProperty2Els.getName(), pendingProperty2Els.getValue());
		registrationModule.setRegistrationPendingProperty3(pendingProperty3Els.getName(), pendingProperty3Els.getValue());
		registrationModule.setRegistrationPendingProperty4(pendingProperty4Els.getName(), pendingProperty4Els.getValue());
		registrationModule.setRegistrationPendingProperty5(pendingProperty5Els.getName(), pendingProperty5Els.getValue());
		
		String notificationEmail = pendingRegistrationNotificationEl.getValue();
		if(StringHelper.containsNonWhitespace(notificationEmail)) {
			registrationModule.setRegistrationNotificationEmailEnabled(true);
			registrationModule.setRegistrationNotificationEmail(notificationEmail);
		} else {
			registrationModule.setRegistrationNotificationEmailEnabled(false);
		}
		
		Integer validUntilHoursGui = Integer.parseInt(validUntilGuiEl.getValue());
		registrationModule.setValidUntilHoursGui(validUntilHoursGui);
		Integer validUntilHoursRest = Integer.parseInt(validUntilRestEl.getValue());
		registrationModule.setValidUntilHoursRest(validUntilHoursRest);
		if(StringHelper.isLong(expirationDateDaysEl.getValue())) {
			registrationModule.setAccountExpirationInDays(Integer.valueOf(expirationDateDaysEl.getValue()));
		} else {
			registrationModule.setAccountExpirationInDays(null);
		}
		
		String domains = domainListElement.getValue();
		registrationModule.setDomainListRaw(domains);
		
		registrationModule.setStaticPropertyMappingEnabled(staticPropElement.isSelected(0));
		if(propertyElement.isOneSelected()) {
			registrationModule.setStaticPropertyMappingName(propertyElement.getSelectedKey());
		} else {
			registrationModule.setStaticPropertyMappingName("-");
		}
		registrationModule.setStaticPropertyMappingValue(propertyValueElement.getValue());
	}
	
	private String generateExampleCode() {
		StringBuilder code = new StringBuilder();
		code.append("<form name=\"openolatregistration\" action=\"")
		    .append(Settings.getServerContextPathURI()).append("/url/registration/0")
		    .append("\" method=\"post\" target=\"OpenOLAT\" onsubmit=\"var openolat=window.open('','OpenOLAT',''); openolat.focus();\">\n")
		    .append("  <input type=\"submit\" value=\"Go to registration\">\n")
		    .append("</form>");
		return code.toString();
	}
	
	private String generateRemoteLoginExampleCode() {
		StringBuilder code = new StringBuilder();
		code.append("<form name=\"olatremotelogin\" action=\"")
		    .append(Settings.getServerContextPathURI()).append("/remotelogin/")
		    .append("\" method=\"post\" target=\"OpenOLAT\" onsubmit=\"var openolat=window.open('','OpenOLAT', 'location=no,menubar=no,resizable=yes,toolbar=no,statusbar=no,scrollbars=yes'); openolat.focus();\">\n")
		    .append("  Benutzername <input type=\"text\" name=\"username\">")
		    .append("  Passwort <input type=\"password\" name=\"pwd\">")
		    .append("  <input type=\"submit\" value=\"Login\">\n")
		    .append("</form>");
		return code.toString();
	}
	
	public class PropertyNameValueElements {
		
		private final SingleSelection propertyNameEl;
		private final TextElement propertyValueEl;
		
		public PropertyNameValueElements(SingleSelection propertyNameEl, TextElement propertyValueEl) {
			this.propertyNameEl = propertyNameEl;
			this.propertyValueEl = propertyValueEl;
		}

		public SingleSelection getPropertyNameEl() {
			return propertyNameEl;
		}

		public TextElement getPropertyValueEl() {
			return propertyValueEl;
		}
		
		public void setVisible(boolean visible) {
			propertyNameEl.setVisible(visible);
			propertyValueEl.setVisible(visible);
		}
		
		public String getName() {
			String key = propertyNameEl.getSelectedKey();
			if(propertyKeys[0].equals(key)) {
				key = "";
			}
			return key;
		}
		
		public String getValue() {
			return propertyValueEl.getValue();
		}
	}
	
	private static class AutoEnrolmentCourseFilter implements RepositoryEntryFilter {

		@Override
		public boolean accept(RepositoryEntry re) {
			return re.getEntryStatus().equals(RepositoryEntryStatusEnum.published);
		}
		
	}
}
