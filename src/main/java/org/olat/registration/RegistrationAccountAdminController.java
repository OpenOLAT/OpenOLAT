/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
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
import org.olat.repository.controllers.RepositorySearchController;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Feb 25, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationAccountAdminController extends FormBasicController {

	private static final String[] pendingRegistrationKeys = new String[]{
			RegistrationPendingStatus.active.name(),
			RegistrationPendingStatus.pending.name(),
			RegistrationPendingStatus.pendingMatchingProperties.name()
	};

	private final String[] propertyKeys;
	private final String[] propertyValues;
	private final String[] pendingPropertyKeys;
	private final String[] pendingPropertyValues;
	private Map<Long, String> courseNames = new HashMap<>();
	private List<Long> courseKeys;
	private final boolean orgEmailDomainEnabled;

	private SingleSelection organisationsEl;
	private MultipleSelectionElement addDefaultOrgEl;
	private FormToggle staticPropElement;
	private FormToggle autoEnrolmentCoursesEl;
	private SingleSelection propertyElement;
	private SingleSelection pendingRegistrationStatusEl;
	private FormLayoutContainer autoEnrolmentCoursesContainer;
	private TextElement propertyValueElement;
	private TextElement expirationDateDaysEl;
	private TextElement pendingRegistrationNotificationEl;
	private PropertyNameValueElements pendingProperty1Els;
	private PropertyNameValueElements pendingProperty2Els;
	private PropertyNameValueElements pendingProperty3Els;
	private PropertyNameValueElements pendingProperty4Els;
	private PropertyNameValueElements pendingProperty5Els;
	private FormLink openCourseBrowserLink;

	private CloseableModalController cmc;
	private ReferencableEntriesSearchController selectCoursesController;

	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private UserPropertiesConfig userPropertiesConfig;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AccessControlModule acModule;

	public RegistrationAccountAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_account");

		//decorate the translator
		Translator userPropTranslator = userPropertiesConfig.getTranslator(getTranslator());

		List<UserPropertyHandler> allPropertyHandlers = userPropertiesConfig.getAllUserPropertyHandlers();
		List<UserPropertyHandler> propertyHandlers = new ArrayList<>(allPropertyHandlers.size());
		for (UserPropertyHandler handler : allPropertyHandlers) {
			if (handler instanceof Generic127CharTextPropertyHandler
					&& !UserConstants.USERNAME.equals(handler.getName())) {
				propertyHandlers.add(handler);
			}
		}

		propertyKeys = new String[propertyHandlers.size() + 1];
		propertyValues = new String[propertyHandlers.size() + 1];
		int count = 0;
		propertyKeys[0] = "-";
		propertyValues[0] = "";
		for (UserPropertyHandler propertyHandler : propertyHandlers) {
			propertyKeys[1 + count] = propertyHandler.getName();
			propertyValues[1 + count++] = userPropTranslator.translate(propertyHandler.i18nFormElementLabelKey());
		}

		pendingPropertyKeys = new String[allPropertyHandlers.size() + 1];
		pendingPropertyValues = new String[allPropertyHandlers.size() + 1];
		count = 0;
		pendingPropertyKeys[0] = "-";
		pendingPropertyValues[0] = "";
		for (UserPropertyHandler propertyHandler : allPropertyHandlers) {
			pendingPropertyKeys[1 + count] = propertyHandler.getName();
			pendingPropertyValues[1 + count++] = userPropTranslator.translate(propertyHandler.i18nFormElementLabelKey());
		}

		orgEmailDomainEnabled = organisationModule.isEnabled() && organisationModule.isEmailDomainEnabled();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer accountConfigCont = FormLayoutContainer.createDefaultFormLayout("accountConfigCont", getTranslator());
		accountConfigCont.setRootForm(mainForm);
		accountConfigCont.setFormTitle(translate("admin.registration.account.config"));
		formLayout.add(accountConfigCont);
		initOrganisationsEl(accountConfigCont);
		// pending status
		initPendingRegistrationStatus(accountConfigCont);
		initPendingPropForm(accountConfigCont);
		initAutoEnrolment(accountConfigCont);

		Integer expirationInDays = registrationModule.getAccountExpirationInDays();
		expirationDateDaysEl = uifactory.addTextElement("admin.registration.account.expiration.days", 20,
				expirationInDays == null ? "" : expirationInDays.toString(), accountConfigCont);

		FormLayoutContainer accountAttrCont = FormLayoutContainer.createDefaultFormLayout("accountAttrCont", getTranslator());
		accountAttrCont.setRootForm(mainForm);
		accountAttrCont.setFormTitle(translate("admin.registration.account.attr"));
		formLayout.add(accountAttrCont);

		//static property
		initStaticPropForm(accountAttrCont);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		formLayout.add(buttonsCont);
	}

	private void initOrganisationsEl(FormLayoutContainer formLayoutCont) {
		String[] theKeys;
		String[] theValues;
		String registrationOrgKey = null;

		if (orgEmailDomainEnabled) {
			theKeys = new String[]{"orgModuleEnabled"};
			theValues = new String[]{translate("admin.enable.email.validation.disabled")};
		} else {
			List<Organisation> organisations = organisationService.getOrganisations(OrganisationStatus.notDelete());
			Organisation registrationOrg = registrationManager.getOrganisationForRegistration(null);
			registrationOrgKey = registrationOrg.getKey().toString();

			theKeys = new String[organisations.size()];
			theValues = new String[organisations.size()];
			for (int i = 0; i < organisations.size(); i++) {
				Organisation organisation = organisations.get(i);
				theKeys[i] = organisation.getKey().toString();
				theValues[i] = organisation.getDisplayName();
			}
		}

		organisationsEl = uifactory.addDropdownSingleselect("organisations", "admin.registrationOrganisation", formLayoutCont, theKeys, theValues);
		if (!orgEmailDomainEnabled) {
			for (String key : theKeys) {
				if (key.equals(registrationOrgKey)) {
					organisationsEl.select(key, true);
					break;
				}
			}
		}

		organisationsEl.addActionListener(FormEvent.ONCHANGE);
		organisationsEl.setEnabled(!orgEmailDomainEnabled);

		addDefaultOrgEl = uifactory.addCheckboxesHorizontal("enable.add.default.org", "admin.enable.add.default.org", formLayoutCont,
				new String[]{"on"}, new String[]{translate("admin.enable.add.default.org.label")});
		// only show this option if the non-default org is selected
		Long defaultOrgKey = organisationService.getDefaultOrganisation().getKey();
		boolean showAddDefaultOrg = !orgEmailDomainEnabled && !organisationsEl.getSelectedKey().equals(String.valueOf(defaultOrgKey));
		addDefaultOrgEl.setVisible(showAddDefaultOrg);
		addDefaultOrgEl.select("on", registrationModule.isAddDefaultOrgEnabled());
	}

	private void initPendingRegistrationStatus(FormLayoutContainer formLayoutContainer) {
		String[] pendingRegistrationValues = new String[]{
				translate("registration.pending.status.active"),
				translate("registration.pending.status.pending"),
				translate("registration.pending.status.pending.props")
		};

		pendingRegistrationStatusEl = uifactory.addRadiosVertical(
				"registration.pending.status", formLayoutContainer, pendingRegistrationKeys, pendingRegistrationValues
		);

		// Preselect the current registration status
		pendingRegistrationStatusEl.select(registrationModule.getRegistrationPendingStatus().name(), true);
		pendingRegistrationStatusEl.addActionListener(FormEvent.ONCHANGE);
	}

	private void initAutoEnrolment(FormItemContainer formLayout) {
		autoEnrolmentCoursesEl = uifactory.addToggleButton("auto.enrolment.courses", "auto.enrolment.courses.toggle", translate("on"), translate("off"), formLayout);
		autoEnrolmentCoursesEl.addActionListener(FormEvent.ONCHANGE);
		autoEnrolmentCoursesEl.toggle(registrationModule.isAutoEnrolmentCoursesEnabled());

		String page = Util.getPackageVelocityRoot(getClass()) + "/auto_enrolment_courses.html";
		autoEnrolmentCoursesContainer = FormLayoutContainer.createCustomFormLayout("auto_enrolment", getTranslator(), page);
		autoEnrolmentCoursesContainer.setRootForm(formLayout.getRootForm());
		autoEnrolmentCoursesContainer.setLabel("auto.enrolment.courses.label", null);
		autoEnrolmentCoursesContainer.setVisible(registrationModule.isAutoEnrolmentCoursesEnabled());
		openCourseBrowserLink = uifactory.addFormLink("auto.enrolment.select.courses", autoEnrolmentCoursesContainer, Link.BUTTON_XSMALL);

		courseKeys = new ArrayList<>(registrationModule.getAutoEnrolmentCourseKeys());
		for (Iterator<Long> courseIt = courseKeys.iterator(); courseIt.hasNext(); ) {
			Long courseKey = courseIt.next();
			RepositoryEntry entry = repositoryService.loadByKey(courseKey);
			if (entry != null) {
				String courseName = entry.getDisplayname();
				FormLink removeLink = uifactory.addFormLink("remove_course_" + courseKey, "remove_course", "remove", null, autoEnrolmentCoursesContainer, Link.LINK);
				removeLink.setElementCssClass("o_button_textstyle");
				removeLink.setUserObject(courseKey);
				courseNames.put(courseKey, courseName);
			} else {
				courseIt.remove();
			}
		}

		autoEnrolmentCoursesContainer.contextPut("autoEnrolmentCourseKeys", courseKeys);
		autoEnrolmentCoursesContainer.contextPut("autoEnrolmentCourseNames", courseNames);
		autoEnrolmentCoursesContainer.contextPut("autoBooking", acModule.isAutoEnabled());

		formLayout.add(autoEnrolmentCoursesContainer);
	}


	private void updatePendingFormVisibility() {
		String selectedStatus = pendingRegistrationStatusEl.getSelectedKey();
		boolean useProps = RegistrationPendingStatus.pendingMatchingProperties.name().equals(selectedStatus);

		// Toggle visibility of property fields
		pendingProperty1Els.setVisible(useProps);
		pendingProperty2Els.setVisible(useProps);
		pendingProperty3Els.setVisible(useProps);
		pendingProperty4Els.setVisible(useProps);
		pendingProperty5Els.setVisible(useProps);

		// Toggle email notification visibility
		boolean showMail = useProps || RegistrationPendingStatus.pending.name().equals(selectedStatus);
		pendingRegistrationNotificationEl.setVisible(showMail);
	}

	private void initPendingPropForm(FormLayoutContainer formLayout) {
		pendingProperty1Els = initPendingProperty(1, registrationModule.getRegistrationPendingPropertyName1(), registrationModule.getRegistrationPendingPropertyValue1(), formLayout);
		pendingProperty2Els = initPendingProperty(2, registrationModule.getRegistrationPendingPropertyName2(), registrationModule.getRegistrationPendingPropertyValue2(), formLayout);
		pendingProperty3Els = initPendingProperty(3, registrationModule.getRegistrationPendingPropertyName3(), registrationModule.getRegistrationPendingPropertyValue3(), formLayout);
		pendingProperty4Els = initPendingProperty(4, registrationModule.getRegistrationPendingPropertyName4(), registrationModule.getRegistrationPendingPropertyValue4(), formLayout);
		pendingProperty5Els = initPendingProperty(5, registrationModule.getRegistrationPendingPropertyName5(), registrationModule.getRegistrationPendingPropertyValue5(), formLayout);

		String email = "";
		if (registrationModule.isRegistrationNotificationEmailEnabled()) {
			email = registrationModule.getRegistrationNotificationEmail();
		}
		pendingRegistrationNotificationEl = uifactory.addTextElement("registration.pending.notification.mail", 2048, email, formLayout);

		updatePendingFormVisibility();
	}

	private PropertyNameValueElements initPendingProperty(int pos, String propName, String propValue, FormLayoutContainer formLayout) {
		SingleSelection pendingPropertyNameEl = uifactory.addDropdownSingleselect("registration.pending.prop.name" + pos, formLayout, pendingPropertyKeys, pendingPropertyValues);
		boolean found = false;
		for (int i = pendingPropertyKeys.length; i-- > 0; ) {
			if (pendingPropertyKeys[i].equals(propName)) {
				pendingPropertyNameEl.select(pendingPropertyKeys[i], true);
				found = true;
				break;
			}
		}
		if (!found) {
			pendingPropertyNameEl.select(pendingPropertyKeys[0], true);
		}

		TextElement pendingPropertyValueEl = uifactory.addTextElement("registration.pending.prop.value" + pos, 2048, propValue, formLayout);
		return new PropertyNameValueElements(pendingPropertyNameEl, pendingPropertyValueEl);
	}

	private void initStaticPropForm(FormItemContainer formLayout) {

		uifactory.addStaticTextElement("admin.registration.staticprop.error", null, translate("admin.registration.staticprop.desc"), formLayout);

		staticPropElement = uifactory.addToggleButton("enable.staticprop", "admin.enableStaticProp", translate("on"), translate("off"), formLayout);
		staticPropElement.addActionListener(FormEvent.ONCHANGE);
		staticPropElement.toggle(registrationModule.isStaticPropertyMappingEnabled());

		propertyElement = uifactory.addDropdownSingleselect("property", "admin.registration.property", formLayout, propertyKeys, propertyValues, null);
		String propertyName = registrationModule.getStaticPropertyMappingName();
		UserPropertyHandler handler = userPropertiesConfig.getPropertyHandler(propertyName);
		if (handler != null) {
			propertyElement.select(handler.getName(), true);
		}
		propertyElement.addActionListener(FormEvent.ONCHANGE);
		propertyElement.setVisible(registrationModule.isStaticPropertyMappingEnabled());

		String propertyValue = registrationModule.getStaticPropertyMappingValue();
		propertyValueElement = uifactory.addTextElement("admin.registration.prop.value", "admin.registration.propertyValue", 255, propertyValue, formLayout);
		propertyValueElement.setVisible(registrationModule.isStaticPropertyMappingEnabled());
	}

	private void openCourseBrowser(UserRequest ureq) {
		cleanUp();

		selectCoursesController = new ReferencableEntriesSearchController(getWindowControl(), ureq, new String[]{CourseModule.getCourseTypeName()}, new AutoEnrolmentCourseFilter(), null, translate("auto.enrolment.add"), false, false, true, false, true, false, RepositorySearchController.Can.all);
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

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateInteger(expirationDateDaysEl, 1);

		allOk &= validatePropertyNameValuePair(pendingProperty1Els);
		allOk &= validatePropertyNameValuePair(pendingProperty2Els);
		allOk &= validatePropertyNameValuePair(pendingProperty3Els);
		allOk &= validatePropertyNameValuePair(pendingProperty4Els);
		allOk &= validatePropertyNameValuePair(pendingProperty5Els);

		allOk &= validateEmail(pendingRegistrationNotificationEl);

		if (staticPropElement.isOn()
				&& propertyElement.isOneSelected()) {
			String propertyName = propertyElement.getSelectedKey();
			String value = propertyValueElement.getValue();
			UserPropertyHandler handler = userPropertiesConfig.getPropertyHandler(propertyName);
			if (handler != null) {
				ValidationError validationError = new ValidationError();
				boolean valid = handler.isValidValue(null, value, validationError, getLocale());
				if (!valid) {
					propertyValueElement.setErrorKey("admin.registration.propertyValue.error");
					allOk = false;
				}
			}
		}

		organisationsEl.clearError();
		if (organisationsEl.isEnabled() && !organisationsEl.isOneSelected()) {
			organisationsEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		if (staticPropElement.isOn()
				&& propertyElement.isOneSelected()) {
			String propertyName = propertyElement.getSelectedKey();
			String value = propertyValueElement.getValue();
			UserPropertyHandler handler = userPropertiesConfig.getPropertyHandler(propertyName);
			if (handler != null) {
				ValidationError validationError = new ValidationError();
				boolean valid = handler.isValidValue(null, value, validationError, getLocale());
				if (!valid) {
					propertyValueElement.setErrorKey("admin.registration.propertyValue.error");
					allOk = false;
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
		if (!nameEl.isOneSelected()) {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		} else if (!nameEl.getSelectedKey().equals(propertyKeys[0])
				&& !StringHelper.containsNonWhitespace(valueEl.getValue())) {
			valueEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		return allOk;
	}

	private boolean validateInteger(TextElement el, int min) {
		boolean allOk = true;
		el.clearError();
		String val = el.getValue();
		if (StringHelper.containsNonWhitespace(val)) {
			try {
				int value = Integer.parseInt(val);
				if (min > value) {
					el.setErrorKey("error.wrong.int");
					allOk = false;
				}
			} catch (NumberFormatException e) {
				el.setErrorKey("error.wrong.int");
				allOk = false;
			}
		} else if (el.isMandatory()) {
			el.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		return allOk;
	}

	private boolean validateEmail(TextElement el) {
		boolean allOk = true;

		el.clearError();
		if (StringHelper.containsNonWhitespace(el.getValue())) {
			String[] emails = el.getValue().split("[,]");
			for (String email : emails) {
				if (!MailHelper.isValidEmailAddress(email)) {
					el.setErrorKey("email.address.notregular");
					allOk = false;
					break;
				}
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == organisationsEl) {
			Long defaultOrgKey = organisationService.getDefaultOrganisation().getKey();
			addDefaultOrgEl.setVisible(!organisationsEl.getSelectedKey().equals(String.valueOf(defaultOrgKey)));
		} else if (source == pendingRegistrationStatusEl) {
			updatePendingFormVisibility();
		} else if (source == staticPropElement) {
			boolean enabledProp = staticPropElement.isOn();
			propertyElement.setVisible(enabledProp);
			propertyValueElement.setVisible(enabledProp);
		} else if (source == autoEnrolmentCoursesEl) {
			boolean enabled = autoEnrolmentCoursesEl.isOn();
			autoEnrolmentCoursesContainer.setVisible(enabled);
		} else if (source == openCourseBrowserLink) {
			openCourseBrowser(ureq);
		} else if (source instanceof FormLink link) {
			if (link.getCmd().equals("remove_course")) {
				if (source.getUserObject() instanceof Long courseKey) {
					registrationModule.removeCourseFromAutoEnrolment(courseKey);
					initForm(ureq);
				}
			}
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == selectCoursesController) {
			if (event.equals(ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) ||
					event.equals(ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED)) {
				for (RepositoryEntry entry : selectCoursesController.getSelectedEntries()) {
					registrationModule.addCourseToAutoEnrolment(entry.getKey());
				}
				initForm(ureq);
			}
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (organisationsEl.isOneSelected()) {
			registrationModule.setSelfRegistrationOrganisationKey(organisationsEl.getSelectedKey());
			// adding to default org value can be false, if default org is already selected
			registrationModule.setAddDefaultOrgEnabled(addDefaultOrgEl.isVisible() && addDefaultOrgEl.isAtLeastSelected(1));
		}
		if (pendingRegistrationStatusEl.isOneSelected()) {
			registrationModule.setRegistrationPendingStatus(RegistrationPendingStatus.valueOf(pendingRegistrationStatusEl.getSelectedKey()));
		}

		registrationModule.setRegistrationPendingProperty1(pendingProperty1Els.getName(), pendingProperty1Els.getValue());
		registrationModule.setRegistrationPendingProperty2(pendingProperty2Els.getName(), pendingProperty2Els.getValue());
		registrationModule.setRegistrationPendingProperty3(pendingProperty3Els.getName(), pendingProperty3Els.getValue());
		registrationModule.setRegistrationPendingProperty4(pendingProperty4Els.getName(), pendingProperty4Els.getValue());
		registrationModule.setRegistrationPendingProperty5(pendingProperty5Els.getName(), pendingProperty5Els.getValue());

		String notificationEmail = pendingRegistrationNotificationEl.getValue();
		if (StringHelper.containsNonWhitespace(notificationEmail)) {
			registrationModule.setRegistrationNotificationEmailEnabled(true);
			registrationModule.setRegistrationNotificationEmail(notificationEmail);
		} else {
			registrationModule.setRegistrationNotificationEmailEnabled(false);
		}

		if (StringHelper.isLong(expirationDateDaysEl.getValue())) {
			registrationModule.setAccountExpirationInDays(Integer.valueOf(expirationDateDaysEl.getValue()));
		} else {
			registrationModule.setAccountExpirationInDays(null);
		}

		registrationModule.setStaticPropertyMappingEnabled(staticPropElement.isOn());
		if (propertyElement.isOneSelected()) {
			registrationModule.setStaticPropertyMappingName(propertyElement.getSelectedKey());
		} else {
			registrationModule.setStaticPropertyMappingName("-");
		}
		registrationModule.setStaticPropertyMappingValue(propertyValueElement.getValue());

		if (!autoEnrolmentCoursesEl.isOn()) {
			registrationModule.saveCourseKeys(Collections.emptyList());
			courseKeys.clear();
			courseNames.clear();
		}
		registrationModule.setAutoEnrolmentCoursesEnabled(autoEnrolmentCoursesEl.isOn());
	}

	private static class AutoEnrolmentCourseFilter implements RepositoryEntryFilter {

		@Override
		public boolean accept(RepositoryEntry re) {
			return re.getEntryStatus().equals(RepositoryEntryStatusEnum.published);
		}

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
			if (propertyKeys[0].equals(key)) {
				key = "";
			}
			return key;
		}

		public String getValue() {
			return propertyValueEl.getValue();
		}
	}
}
