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

package org.olat.user;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationEmailDomainSearchParams;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.DeleteFileElementEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import de.bps.olat.user.ChangeEMailController;

/**
 * Provides a controller which lets the user edit their user profile and choose
 * the fields which are made publicly visible.
 * 
 * @author twuersch
 * 
 */
public class ProfileFormController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(ProfileFormController.class);

	private static final String USAGE_USER_IDENTIFIER = ProfileFormController.class.getCanonicalName();
	private static final String USAGE_INVITEE_IDENTIFIER = ProfileFormController.class.getCanonicalName() + "_invitee";

	private static final String EMAIL_CHANGE_KEY_PROP = "emchangeKey";

	private final Map<String, FormItem> formItems = new HashMap<>();
	private final Map<String, String> formContext = new HashMap<>();
	private RichTextElement textAboutMe;

	private Identity identityToModify;
	private Roles roles;

	private TextElement emailEl;
	private FileElement logoUpload;
	private FileElement portraitUpload;

	private FormLink changeEmailBtn;

	private final boolean canModify;
	private final boolean logoEnabled;
	private final boolean inviteeOnly;
	private final boolean isAdministrativeUser;
	private final String usageIdentifier;
	private List<OrganisationEmailDomain> matchingMailDomains;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private boolean portraitDeleted = false;
	private boolean logoDeleted = false;
	private final boolean canManageCritical;
	private String changedEmail;
	private String currentEmail;

	private ChangeMailUMDialogController changeMailUMDialogCtrl;
	private ChangeMailSupportController changeMailSupportCtrl;
	private ChangeOrganisationController changeOrgCtrl;
	private ChangeMailController changeMailCtrl;
	private CloseableModalController cmc;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private HomePageConfigManager hpcm;
	@Autowired
	private DisplayPortraitManager dps;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private OrganisationModule organisationModule;
	
	/**
	 * Create this controller with the request's identity as none administrative
	 * user. This constructor can be use in after login interceptor.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public ProfileFormController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, ureq.getIdentity(), false, true);
	}

	/**
	 * Creates this controller.
	 * 
	 * @param ureq The user request.
	 * @param wControl The window control.
	 * @param conf The homepage configuration (decides which user profile fields
	 *          are visible for everyone).
	 * @param identity The identity of the user.
	 * @param isAdministrativeUser true: user is editing another users profile as
	 *          user manager; false: use is editing his own profile
	 */
	public ProfileFormController(UserRequest ureq, WindowControl wControl,
			Identity identityToModify, boolean isAdministrativeUser, boolean canModify) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setFormStyle("o_user_profile_form");
		this.canModify = canModify;
		this.identityToModify = identityToModify;
		logoEnabled = userModule.isLogoByProfileEnabled();
		
		final Roles editedRoles = securityManager.getRoles(identityToModify);
		final Roles actingRoles = ureq.getUserSession().getRoles();
		canManageCritical = securityModule.isUserAllowedCriticalUserChanges(actingRoles, editedRoles)
				|| identityToModify.equals(getIdentity());
		
		inviteeOnly = editedRoles.isInviteeOnly();
		usageIdentifier = inviteeOnly ? USAGE_INVITEE_IDENTIFIER : USAGE_USER_IDENTIFIER;
		this.isAdministrativeUser = isAdministrativeUser;
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifier, isAdministrativeUser);
		
		initForm(ureq);
	}
	
	public Identity getIdentityToModify() {
		return identityToModify;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		User user = identityToModify.getUser();

		// show a form element for each property handler 
		boolean first = true;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) {
				continue;
			}
			
			// add spacer if necessary (i.e. when group name changes)
			String group = userPropertyHandler.getGroup();

			// special case, to put smstelmobile and email always into personal section, regardless of configuration
			// OO-8430
			String formId;
			if (UserConstants.SMSTELMOBILE.equals(userPropertyHandler.getName())
					|| UserConstants.EMAIL.equals(userPropertyHandler.getName())) {
				formId = "group.personal";
			} else {
				formId = "group." + group;
			}

			FormLayoutContainer groupContainer = (FormLayoutContainer)formLayout.getFormComponent(formId);
			if(groupContainer == null) {
				groupContainer = FormLayoutContainer.createDefaultFormLayout(formId, getTranslator());
				groupContainer.setFormTitle(translate("form.group." + group));
				if(first) {
					groupContainer.setFormContextHelp("manual_user/personal_menu/Profile/");
					first = false;
				}
				formItems.put(formId, groupContainer);
				formLayout.add(groupContainer);
			}
			
			// add input field to container
			FormItem formItem = userPropertyHandler.addFormItem(getLocale(), user, usageIdentifier, isAllowedToModifyWithoutVerification(), groupContainer);
			if(formItem.isEnabled() && (!canModify || UserConstants.NICKNAME.equals(userPropertyHandler.getName()))) {
				formItem.setEnabled(false);
			}
			
			String propertyName = userPropertyHandler.getName();
			formItems.put(propertyName, formItem);
			
			if (formItem instanceof TextElement textElement) {
				// it's a text field, so get the value of this property into the text field
				textElement.setValue(user.getProperty(propertyName, getLocale()));
				textElement.setEnabled(textElement.isEnabled()
						&& (!UserConstants.SECURITY_CRITICAL_PROPERTIES.contains(propertyName) || canManageCritical));
			} else if (formItem instanceof MultipleSelectionElement checkbox) {
				// it's a checkbox, so set the box to checked if the corresponding property is set to "true"
				String value = user.getProperty(propertyName, getLocale());
				if (value != null) {
					checkbox.select(propertyName, value.equals("true"));
				} else {
					// assume "false" if the property is not present
					checkbox.select(propertyName, false);
				}
			}
			
			if (UserConstants.EMAIL.equals(userPropertyHandler.getName())) {
				initEmailForm(formItem, groupContainer);
			}
		}
		
		// add the "about me" text field.
		FormLayoutContainer aboutMeContainer = FormLayoutContainer.createDefaultFormLayout("group.about", getTranslator());
		aboutMeContainer.setFormTitle(translate("form.group.about"));
		aboutMeContainer.setElementCssClass("o_user_aboutme");
		aboutMeContainer.setVisible(userModule.isUserAboutMeEnabled() && !inviteeOnly);
		formLayout.add(aboutMeContainer);
		
		HomePageConfig conf = hpcm.loadConfigFor(identityToModify);
		textAboutMe = uifactory.addRichTextElementForStringData("form.text", "form.text",
				conf.getTextAboutMe(), 10, -1, false, null, null, aboutMeContainer,
				ureq.getUserSession(), getWindowControl());
		textAboutMe.setVisible(userModule.isUserAboutMeEnabled() && !inviteeOnly);
		textAboutMe.setEnabled(canModify);
		textAboutMe.setMaxLength(10000);
		
		//upload image
		FormLayoutContainer groupContainer = FormLayoutContainer.createDefaultFormLayout("portraitupload", getTranslator());
		groupContainer.setFormTitle(translate("ul.header"));
		groupContainer.setVisible(!inviteeOnly);
		formLayout.add(groupContainer);

		File portraitFile = dps.getLargestPortrait(identityToModify);
		// Init upload controller
		Set<String> mimeTypes = new HashSet<>();
		mimeTypes.add("image/gif");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/png");

		boolean portraitEnable = isAdministrativeUser 
				|| (canModify && !userModule.isPortraitManaged());
		portraitUpload = uifactory.addFileElement(getWindowControl(), getIdentity(), "ul.select", "ul.select", groupContainer);
		portraitUpload.setMaxUploadSizeKB(10000, null, null);
		portraitUpload.setPreview(ureq.getUserSession(), true);
		portraitUpload.addActionListener(FormEvent.ONCHANGE);
		portraitUpload.setHelpTextKey("ul.select.fhelp", null);
		portraitUpload.setDeleteEnabled(true);
		portraitUpload.setEnabled(portraitEnable && canModify);
		portraitUpload.setVisible(!inviteeOnly);
		if(portraitFile != null) {
			portraitUpload.setInitialFile(portraitFile);
		}
		portraitUpload.limitToMimeType(mimeTypes, null, null);
		
		if(logoEnabled) {
			//upload image
			groupContainer = FormLayoutContainer.createDefaultFormLayout("logoupload", getTranslator());
			groupContainer.setFormTitle(translate("logo.header"));
			groupContainer.setVisible(!inviteeOnly);
			formLayout.add(groupContainer);

			File logoFile = dps.getLargestLogo(identityToModify);
			logoUpload = uifactory.addFileElement(getWindowControl(), getIdentity(), "logo.select", "logo.select", groupContainer);
			logoUpload.setMaxUploadSizeKB(10000, null, null);
			logoUpload.setPreview(ureq.getUserSession(), true);
			logoUpload.addActionListener(FormEvent.ONCHANGE);
			logoUpload.setHelpTextKey("ul.select.fhelp", null);
			logoUpload.setDeleteEnabled(true);
			logoUpload.setEnabled(canModify);
			logoUpload.setVisible(!inviteeOnly);
			if(logoFile != null) {
				logoUpload.setInitialFile(logoFile);
			}
			logoUpload.limitToMimeType(mimeTypes, null, null);
		}
		
		// Create submit and cancel buttons
		FormLayoutContainer buttonLayoutWrappper = FormLayoutContainer.createDefaultFormLayout("buttonLayoutWrappper", getTranslator());
		formLayout.add(buttonLayoutWrappper);
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayoutInner", getTranslator());
		buttonLayoutWrappper.add(buttonLayout);
		if(canModify) {
			uifactory.addFormSubmitButton("save", buttonLayout);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}
	}
	
	private void initEmailForm(FormItem formItem, FormLayoutContainer groupContainer) {
		// special case for email field
		emailEl = (TextElement)formItem;
		emailEl.setElementCssClass("o_sel_user_mail");
		emailEl.setEnabled(false);
		currentEmail = emailEl.getValue();

		FormLayoutContainer emailLayoutContainer = FormLayoutContainer.createButtonLayout("emails.buttons", getTranslator());
		groupContainer.add(emailLayoutContainer);
		emailLayoutContainer.setRootForm(mainForm);

		if (identityToModify.getUser().getProperty("emailDisabled") == null || identityToModify.getUser().getProperty("emailDisabled").equals("false")) {
			changeEmailBtn = uifactory.addFormLink("change.mail.in.process", emailLayoutContainer, Link.BUTTON_SMALL);
			changeEmailBtn.setElementCssClass("o_sel_user_change_mail");
			changeEmailBtn.setIconLeftCSS("o_icon o_icon_edit");
			changeEmailBtn.setEnabled(canModify);
		}

		if (!userModule.isEmailMandatory()) {
			emailEl.setMandatory(false);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		formContext.put("username", identityToModify.getName());
		
		User user = identityToModify.getUser();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem formItem = formItems.get(userPropertyHandler.getName());
			if(formItem.isEnabled()) {
				formItem.clearError();
				allOk &= userPropertyHandler.isValid(user, formItem, formContext);
			}
		}
		
		try {
			String aboutMe = textAboutMe.getValue();
			if(aboutMe.length() > 10000) {
				textAboutMe.setErrorKey("input.toolong", "10000");
				allOk = false;
			} else {
				textAboutMe.clearError();
			}
		} catch (Exception e) {
			textAboutMe.setErrorKey("input.toolong", "10000");
			allOk = false;
		}
		return allOk;
	}

	@Override
	protected void formNOK(UserRequest ureq) {
		fireEvent(ureq, Event.FAILED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(changeMailUMDialogCtrl);
		removeAsListenerAndDispose(changeMailSupportCtrl);
		removeAsListenerAndDispose(changeMailCtrl);
		removeAsListenerAndDispose(changeOrgCtrl);
		removeAsListenerAndDispose(cmc);
		changeMailUMDialogCtrl = null;
		changeMailSupportCtrl = null;
		changeMailCtrl = null;
		changeOrgCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (isAllowedToModifyWithoutVerification()
				&& source == changeMailCtrl
				&& event instanceof ChangeMailEvent cme) {
			changedEmail = cme.getChangedEmail();
			cmc.deactivate();
			cmc = null;
			doStartChangeMailDialogAdmin(ureq);
		} else if (source == changeMailCtrl
				|| source == changeOrgCtrl
				|| source == changeMailSupportCtrl
				|| source == changeMailUMDialogCtrl) {
			handleChangeEvent(ureq, event);
		} else if (source == cmc) {
			handleCmcEvent(ureq);
		}
		super.event(ureq, source, event);
	}

	private void handleChangeEvent(UserRequest ureq, Event event) {
		if (event instanceof ChangeMailEvent cme) {
			handleChangeMailEvent(ureq, cme, event);
		} else if (event == Event.CANCELLED_EVENT) {
			handleEmailChangeCancellation(ureq);
			cleanUp();
		} else if (event == Event.DONE_EVENT) {
			startChangeEmailWorkflow(ureq);
		}
	}

	private void handleChangeMailEvent(UserRequest ureq, ChangeMailEvent cme, Event event) {
		changedEmail = cme.getChangedEmail();
		cmc.deactivate();
		cmc = null;

		// default: when org module & emailDomain is disabled
		switch (event.getCommand()) {
			case ChangeMailEvent.CHANGED_EMAIL_EVENT:
				handleChangedEmailEvent(ureq, cme);
				break;
			case ChangeMailEvent.CHANGED_ORG_EVENT:
				handleChangedOrgEvent(ureq);
				break;
			default:
				startChangeEmailWorkflow(ureq);
				break;
		}
	}

	private void handleChangedEmailEvent(UserRequest ureq, ChangeMailEvent cme) {
		if (organisationModule.isEnabled()
				&& organisationModule.isEmailDomainEnabled()
				&& !isAllowedToModifyWithoutVerification()) {
			String newDomain = MailHelper.getMailDomain(cme.getChangedEmail());
			String currentDomain = MailHelper.getMailDomain(emailEl.getValue());

			if (newDomain.equals(currentDomain)) {
				startChangeEmailWorkflow(ureq);
			} else {
				processMailDomainChange(ureq, newDomain);
			}
		} else {
			// mail change for administrative users is straight forward without any consideration of orgs
			startChangeEmailWorkflow(ureq);
		}
	}

	private void processMailDomainChange(UserRequest ureq, String newDomain) {
		matchingMailDomains = getMatchingMailDomains();

		// Only go further to change org dialog if mail domain got changed
		// null is a special case: If a user already is in an org without having the mailDomain previously
		// then just change mail without any organisational change
		if (matchingMailDomains == null) {
			startChangeEmailWorkflow(ureq);
		} else if (matchingMailDomains.isEmpty()) {
			doCancelChangeMailProcess(ureq, MailHelper.getMailDomain(newDomain));
		} else {
			doStartChangeOrgProcess(ureq, matchingMailDomains);
		}
	}

	private void handleChangedOrgEvent(UserRequest ureq) {
		if (startChangeEmailWorkflow(ureq)) {
			if (!updateOrganisationMembership()) {
				showError("change.org.failed");
				handleEmailChangeCancellation(ureq);
			} else {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else {
			handleEmailChangeCancellation(ureq);
		}
	}

	private void handleCmcEvent(UserRequest ureq) {
		String emChangeKey = identityToModify.getUser().getProperty(EMAIL_CHANGE_KEY_PROP, null);
		if (emChangeKey != null) {
			handleEmailChangeCancellation(ureq);
		}
		cleanUp();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == portraitUpload) {
			if(event instanceof DeleteFileElementEvent) {
				if(DeleteFileElementEvent.DELETE.equals(event.getCommand())) {
					portraitDeleted = true;
					portraitUpload.setInitialFile(null);
					if(portraitUpload.getUploadFile() != null) {
						portraitUpload.reset();
					}
					setDirtyMarking(true);
				}
			} else if (portraitUpload.isUploadSuccess()) {
				setDirtyMarking(true);
			}
		} else if (source == logoUpload) {
			if(event instanceof DeleteFileElementEvent) {
				if(DeleteFileElementEvent.DELETE.equals(event.getCommand())) {
					logoDeleted = true;
					logoUpload.setInitialFile(null);
					if(logoUpload.getUploadFile() != null) {
						logoUpload.reset();
					}
					setDirtyMarking(true);
				}
			} else if (logoUpload.isUploadSuccess()) {
				setDirtyMarking(true);
			}
		} else if (source == changeEmailBtn) {
			doStartChangeMailProcess(ureq);
		}

		super.formInnerEvent(ureq, source, event);
	}

	private List<OrganisationEmailDomain> getMatchingMailDomains() {
		OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
		String mailDomain = MailHelper.getMailDomain(changedEmail);
		List<OrganisationEmailDomain> emailDomains = organisationService.getEmailDomains(searchParams);

		roles = securityManager.getRoles(identityToModify);
		List<OrganisationRef> currentOrgs = roles.getOrganisations();

		// Filter emailDomains
		List<OrganisationEmailDomain> matchingDomains = organisationService.getMatchingEmailDomains(emailDomains, mailDomain);

		// Check if a match with current orgs exists
		boolean matchFound = isMatchFound(matchingDomains, currentOrgs);

		// If a match is found, return null, because there is no need for an organisational change
		if (matchFound) {
			return null;
		} else if (matchingDomains.isEmpty()) {
			return Collections.emptyList();
		} else {
			return matchingDomains;
		}
	}

	/**
	 * special case: If a user already is in an org without having the mailDomain previously
	 * then just change mail without any organisational change
	 * @param emailDomains new possible organisational mailDomains
	 * @param currentOrgs List of current orgs
	 * @return true, so user already is in the "new" org, false if the user is not in the "new" org yet
	 */
	private boolean isMatchFound(List<OrganisationEmailDomain> emailDomains, List<OrganisationRef> currentOrgs) {
		return emailDomains.stream()
				.anyMatch(emailDomain -> currentOrgs.stream()
						.anyMatch(org -> org.getKey().equals(emailDomain.getOrganisation().getKey())));
	}

	private void doStartChangeMailDialogAdmin(UserRequest ureq) {
		changeMailUMDialogCtrl = new ChangeMailUMDialogController(ureq, getWindowControl(), changedEmail);
		listenTo(changeMailUMDialogCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				changeMailUMDialogCtrl.getInitialComponent(), true, translate("change.mail"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doStartChangeMailProcess(UserRequest ureq) {
		changeMailCtrl = new ChangeMailController(ureq, getWindowControl(),
				emailEl.getValue(), identityToModify, isAllowedToModifyWithoutVerification());
		listenTo(changeMailCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				changeMailCtrl.getInitialComponent(), true, translate("change.mail"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doStartChangeOrgProcess(UserRequest ureq, List<OrganisationEmailDomain> matchingMailDomains) {
		changeOrgCtrl = new ChangeOrganisationController(ureq, getWindowControl(),
				matchingMailDomains, changedEmail, identityToModify);
		listenTo(changeOrgCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				changeOrgCtrl.getInitialComponent(), true, translate("change.org"));
		listenTo(cmc);
		cmc.activate();
	}

	/**
	 * This cancel situation happens through the system
	 * because there are no matching mail domains for the changed mail
	 * @param ureq
	 */
	private void doCancelChangeMailProcess(UserRequest ureq, String changedEmailDomain) {
		changeMailSupportCtrl = new ChangeMailSupportController(ureq, getWindowControl(), changedEmailDomain);
		listenTo(changeMailSupportCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				changeMailSupportCtrl.getInitialComponent(), true, translate("change.org.cancel.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void handleEmailChangeCancellation(UserRequest ureq) {
		String key = identityToModify.getUser().getProperty(EMAIL_CHANGE_KEY_PROP, null);
		if (!StringHelper.containsNonWhitespace(key)) {
			fireEvent(ureq, Event.DONE_EVENT);
			return; // Exit if no key is present
		}

		deleteTemporaryKeyIfExists(key);

		// Clear the email change key
		identityToModify.getUser().setProperty(EMAIL_CHANGE_KEY_PROP, null);

		// Reload identity
		identityToModify = securityManager.loadIdentityByKey(identityToModify.getKey());

		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void deleteTemporaryKeyIfExists(String key) {
		TemporaryKey tempKey = registrationManager.loadTemporaryKeyByRegistrationKey(key);
		if (tempKey != null) {
			registrationManager.deleteTemporaryKey(tempKey);
		}
	}
	
	private void notifyPortraitChanged() {
		ProfileEvent newPortraitEvent = new ProfileEvent("changed-portrait", identityToModify.getKey());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("portrait", getIdentity().getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(newPortraitEvent, ores);
	}

	@Override
	protected void formOK(final UserRequest ureq) {
		// update each user field
		updateUserProperties();
		handlePortraitUpdates();
		handleLogoUpdates();
		// Store the "about me" text.
		updateAboutMeText();

		// update the user profile data
		identityToModify = updateUserProfile();

		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void updateUserProperties() {
		User user = identityToModify.getUser();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem formItem = formItems.get(userPropertyHandler.getName());
			if (formItem.isEnabled()) {
				userPropertyHandler.updateUserFromFormItem(user, formItem);
			}
		}
	}

	private void handlePortraitUpdates() {
		if (portraitDeleted) {
			File img = dps.getLargestPortrait(identityToModify);
			if (img != null) {
				dps.deletePortrait(identityToModify);
				notifyPortraitChanged();
			}
		}

		File uploadedImage = portraitUpload.getUploadFile();
		String uploadedFilename = portraitUpload.getUploadFileName();
		if (uploadedImage != null) {
			dps.setPortrait(uploadedImage, uploadedFilename, identityToModify);
			notifyPortraitChanged();
		}
	}

	private void handleLogoUpdates() {
		if (logoDeleted) {
			File img = dps.getLargestLogo(identityToModify);
			if (img != null) {
				dps.deleteLogo(identityToModify);
				notifyPortraitChanged();
			}
		}

		if (logoUpload != null) {
			File uploadedLogo = logoUpload.getUploadFile();
			String uploadedLogoname = logoUpload.getUploadFileName();
			if (uploadedLogo != null) {
				dps.setLogo(uploadedLogo, uploadedLogoname, identityToModify);
				notifyPortraitChanged();
			}
		}
	}

	private void updateAboutMeText() {
		HomePageConfig conf = hpcm.loadConfigFor(identityToModify);
		conf.setTextAboutMe(textAboutMe.getValue());
		hpcm.saveConfigTo(identityToModify, conf);
	}

	private Identity updateUserProfile() {
		// Final update of the user profile after save button was hit
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(
				OresHelper.createOLATResourceableInstance(Identity.class, identityToModify.getKey()), () -> {
					if (!userManager.updateUserFromIdentity(identityToModify)) {
						getWindowControl().setInfo(translate("profile.unsuccessful"));
						identityToModify = securityManager.loadIdentityByKey(identityToModify.getKey());
					}
					notifyProfileChanges();
					return identityToModify;
				});
	}

	private void notifyProfileChanges() {
		OLATResourceable modRes = OresHelper.createOLATResourceableInstance(Identity.class, identityToModify.getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new MultiUserEvent("changed"), modRes);
	}

	private boolean startChangeEmailWorkflow(UserRequest ureq) {
		if (changedEmail == null || changedEmail.trim().isEmpty()) {
			log.warn("Changed email is empty or null.");
			handleEmailChangeCancellation(ureq);
			return false;
		}
		changedEmail = changedEmail.trim();

		// Mailer configuration
		String serverPath = Settings.getServerContextPathURI();
		String serverName = ureq.getHttpReq().getServerName();
		logDebug("this servername is " + serverName + " and serverpath is " + serverPath);

		boolean areMailsSent;
		if (isAllowedToModifyWithoutVerification()) {
			// An usermanager does not need to verify the new mail and can change it directly
			areMailsSent = handleDirectEmailChange(ureq);
		} else {
			areMailsSent = handleVerifiedEmailChange(ureq);
		}

		if (areMailsSent) {
			updateUserEmail(ureq);
			return true;
		} else {
			log.error("Failed to send one or more emails for user: {}", identityToModify.getKey());
			handleSendingError(ureq);
			return false;
		}
	}

	private boolean handleDirectEmailChange(UserRequest ureq) {
		if (changeMailUMDialogCtrl != null && changeMailUMDialogCtrl.getIsNotifyUser()) {
			// Notify user via email
			String subject = translate("email.change.subject");
			String bodyUserManager = translate("email.change.body.usermanager");
			return sendEmail(ureq, subject, bodyUserManager, currentEmail);
		} else if (changeMailUMDialogCtrl != null && !changeMailUMDialogCtrl.getIsNotifyUser()) {
			// Directly update user email without notification
			updateUserEmail(ureq);
			return true;
		}
		return false; // Default case if conditions are not met
	}

	private boolean handleVerifiedEmailChange(UserRequest ureq) {
		// Load temporary key
		String key = identityToModify.getUser().getProperty(EMAIL_CHANGE_KEY_PROP, null);
		TemporaryKey tempKey = registrationManager.loadTemporaryKeyByRegistrationKey(key);
		if (tempKey == null) {
			// Temporary key not found, log error and cancel workflow
			log.error("Temporary key not found for user: {}!", identityToModify.getKey());
			handleEmailChangeCancellation(ureq);
			return false;
		}

		// Prepare email content
		String subject = translate("email.change.subject");
		String bodyOld = buildEmailBody("email.change.body.old");
		String bodyNew = buildEmailBody("email.change.body.new");

		boolean emailToCurrentSent = true;
		// Send emails to current and new email addresses
		if (StringHelper.containsNonWhitespace(currentEmail)) {
			emailToCurrentSent = sendEmail(ureq, subject, bodyOld, currentEmail);
		}
		boolean emailToNewSent = sendEmail(ureq, subject, bodyNew, changedEmail);
		boolean areMailsSent = emailToCurrentSent && emailToNewSent;

		// Update
		tempKey.setMailSent(areMailsSent);
		User user = identityToModify.getUser();
		user.setProperty(EMAIL_CHANGE_KEY_PROP, tempKey.getRegistrationKey());

		return areMailsSent;
	}

	private String buildEmailBody(String bodyKey) {
		return translate(bodyKey, userManager.getUserDisplayName(identityToModify.getUser()), WebappHelper.getMailConfig("mailSupport"));
	}

	private boolean sendEmail(UserRequest ureq, String subject,
							  String body, String recipient) {
		try {
			MailBundle bundle = new MailBundle();
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setTo(recipient);
			bundle.setContent(subject, body);

			MailerResult result = mailManager.sendMessage(bundle);
			return result.isSuccessful();
		} catch (Exception e) {
			log.error("Unexpected exception occurred while sending email", e);
			handleSendingError(ureq);
		}
		return false;
	}

	private void updateUserEmail(UserRequest ureq) {
		if (changeEMail(ureq)) {
			userManager.updateUserFromIdentity(identityToModify);
			identityToModify = securityManager.loadIdentityByKey(identityToModify.getKey());
			fireEvent(ureq, Event.DONE_EVENT);
		}
		setDirtyMarking(true);
	}

	private boolean changeEMail(UserRequest ureq) {
		if (identityToModify == null) {
			return false;
		}

		User user = identityToModify.getUser();

		String newMail = getNewEmail(user);
		if (newMail == null || newMail.isEmpty()) {
			// Failed to retrieve a valid new email; cannot proceed
			return false;
		}

		// Update the email property and clear the change key
		user.setProperty("email", newMail);
		user.setProperty(EMAIL_CHANGE_KEY_PROP, null);

		// success
		String currentEmailDisplay = userManager.getUserDisplayEmail(currentEmail, getLocale());
		String changedEmailDisplay = userManager.getUserDisplayEmail(newMail, getLocale());
		getWindowControl().setInfo(translate("success.change.email", currentEmailDisplay, changedEmailDisplay));

		// Remove session entries and cleanup
		ureq.getUserSession().removeEntryFromNonClearedStore(ChangeEMailController.CHANGE_EMAIL_ENTRY);
		securityManager.deleteInvalidAuthenticationsByEmail(currentEmail);
		emailEl.setValue(newMail);

		return true;
	}

	private String getNewEmail(User user) {
		if (isAllowedToModifyWithoutVerification()) {
			return changedEmail;
		}

		String key = user.getProperty(EMAIL_CHANGE_KEY_PROP, null);
		if (key == null) {
			// Missing change key; cannot proceed with email change
			return null;
		}

		// Using tempKey to retrieve changed E-Mail to also verify that the regKey is still valid
		TemporaryKey tempKey = registrationManager.loadTemporaryKeyByRegistrationKey(key);
		if (tempKey == null) {
			// Temporary key could not be found
			return null;
		}

		String newMail = tempKey.getEmailAddress();
		if (newMail == null || newMail.isEmpty()) {
			// Invalid email retrieved
			return null;
		}

		// Delete the used temporary registration key, because process is done after changing mail
		registrationManager.deleteTemporaryKeyWithId(tempKey.getRegistrationKey());

		return newMail;
	}

	private void handleSendingError(UserRequest ureq) {
		handleEmailChangeCancellation(ureq);
		getWindowControl().setError(translate("email.notsent"));
	}

	private boolean updateOrganisationMembership() {
		if (matchingMailDomains == null || matchingMailDomains.isEmpty()) {
			log.warn("No matching mail domains found.");
			return false;
		}

		Optional<OrganisationEmailDomain> selectedDomain;
		if (matchingMailDomains.size() == 1) {
			selectedDomain = Optional.of(matchingMailDomains.get(0));
		} else {
			// Identify the selected organisation based on the matching mail domains
			String selectedKey = changeOrgCtrl.getOrgSelection().getSelectedKey();
			selectedDomain = matchingMailDomains.stream()
					.filter(domain -> domain.getOrganisation().getKey().toString().equals(selectedKey))
					.findFirst();
		}

		if (selectedDomain.isEmpty()) {
			log.warn("No organisation found based on email domains and/or org selection.");
			return false;
		}

		OrganisationEmailDomain domain = selectedDomain.get();
		Organisation organisationEntity = domain.getOrganisation();
		Organisation newOrg = organisationService.getOrganisation(organisationEntity);
		if (newOrg == null) {
			log.error("Organisation not found for key: {}", organisationEntity.getKey());
			return false;
		}

		identityToModify = securityManager.loadIdentityByKey(identityToModify.getKey());
		// User changed his mail domain, thats why he is getting moved to a new matching org
		organisationService.addMember(newOrg, identityToModify, OrganisationRoles.user, getIdentity());

		// Remove the user from all old organisations
		roles.getOrganisations().forEach(orgRef -> {
			Organisation oldOrg = organisationService.getOrganisation(orgRef);
			organisationService.removeMember(oldOrg, identityToModify, getIdentity());
		});

		return true;
	}

	/**
	 * Sets the dirty mark for this form.
	 *
	 * @param isDirtyMarking <code>true</code> sets this form dirty.
	 */
	public void setDirtyMarking(boolean isDirtyMarking) {
		mainForm.setDirtyMarking(isDirtyMarking);
	}

	private boolean isAllowedToModifyWithoutVerification() {
		return isAdministrativeUser && !Objects.equals(identityToModify.getKey(), getIdentity().getKey());
	}
}
