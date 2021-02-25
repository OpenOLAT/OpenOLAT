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

package org.olat.user;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.dispatcher.DispatcherModule;
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
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.mail.MailBundle;
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

	private static final String usageIdentifier= ProfileFormController.class.getCanonicalName();
	private static final String SEPARATOR = "\n____________________________________________________________________\n";

	private final Map<String, FormItem> formItems = new HashMap<>();
	private final Map<String, String> formContext = new HashMap<>();
	private RichTextElement textAboutMe;

	private Identity identityToModify;
	private DialogBoxController dialogCtr;

	private TextElement emailEl;
	private FileElement logoUpload;
	private FileElement portraitUpload;
	
	private FormLink removeEmailInProcessButton;
	private FormLink confirmEmailInProcessButton;
	private FormLayoutContainer emailLayoutContainer;
	
	private final boolean canModify;
	private final boolean logoEnabled;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private boolean portraitDeleted = false;
	private boolean logoDeleted = false;
	private boolean emailChanged = false;
	private String changedEmail;
	private String currentEmail;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private HomePageConfigManager hpcm;
	@Autowired
	private DisplayPortraitManager dps;
	
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
		
		this.isAdministrativeUser = isAdministrativeUser;
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifier, isAdministrativeUser);
		
		initForm(ureq);
	}
	
	public Identity getIdentityToModify() {
		return identityToModify;
	}

	@Override
	protected void doDispose() {
		// nothing to dispose.
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
			String formId = "group." + group;
			FormLayoutContainer groupContainer = (FormLayoutContainer)formLayout.getFormComponent(formId);
			if(groupContainer == null) {
				groupContainer = FormLayoutContainer.createDefaultFormLayout(formId, getTranslator());
				groupContainer.setFormTitle(translate("form.group." + group));
				if(first) {
					groupContainer.setFormContextHelp("Configuration");
					first = false;
				}
				formItems.put(formId, groupContainer);
				formLayout.add(groupContainer);
			}
			
			// add input field to container
			FormItem formItem = userPropertyHandler.addFormItem(getLocale(), user, usageIdentifier, isAdministrativeUser, groupContainer);
			if(formItem.isEnabled() && (!canModify || UserConstants.NICKNAME.equals(userPropertyHandler.getName()))) {
				formItem.setEnabled(false);
			}
			
			String propertyName = userPropertyHandler.getName();
			formItems.put(propertyName, formItem);
			
			if (formItem instanceof TextElement) {
				// it's a text field, so get the value of this property into the text field
				TextElement textElement = (TextElement)formItem;
				textElement.setValue(user.getProperty(propertyName, getLocale()));
			} else if (formItem instanceof MultipleSelectionElement) {
				// it's a checkbox, so set the box to checked if the corresponding property is set to "true"
				MultipleSelectionElement checkbox = (MultipleSelectionElement)formItem;
				String value = user.getProperty(propertyName, getLocale());
				if (value != null) {
					checkbox.select(propertyName, value.equals("true"));
				} else {
					// assume "false" if the property is not present
					checkbox.select(propertyName, false);
				}
			}
			
			if (UserConstants.EMAIL.equals(userPropertyHandler.getName())) {
				initEmailForm(user, formItem, groupContainer);
			}
		}
		
		// add the "about me" text field.
		FormLayoutContainer aboutMeContainer = FormLayoutContainer.createDefaultFormLayout("group.about", getTranslator());
		aboutMeContainer.setFormTitle(translate("form.group.about"));
		aboutMeContainer.setElementCssClass("o_user_aboutme");
		aboutMeContainer.setVisible(userModule.isUserAboutMeEnabled());
		formLayout.add(aboutMeContainer);
		
		HomePageConfig conf = hpcm.loadConfigFor(identityToModify);
		textAboutMe = uifactory.addRichTextElementForStringData("form.text", "form.text",
				conf.getTextAboutMe(), 10, -1, false, null, null, aboutMeContainer,
				ureq.getUserSession(), getWindowControl());
		textAboutMe.setVisible(userModule.isUserAboutMeEnabled());
		textAboutMe.setEnabled(canModify);
		textAboutMe.setMaxLength(10000);
		
		//upload image
		FormLayoutContainer groupContainer = FormLayoutContainer.createDefaultFormLayout("portraitupload", getTranslator());
		groupContainer.setFormTitle(translate("ul.header"));
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
		portraitUpload.setEnabled(portraitEnable);
		if(portraitFile != null) {
			portraitUpload.setInitialFile(portraitFile);
		}
		portraitUpload.limitToMimeType(mimeTypes, null, null);
		
		if(logoEnabled) {
			//upload image
			groupContainer = FormLayoutContainer.createDefaultFormLayout("logoupload", getTranslator());
			groupContainer.setFormTitle(translate("logo.header"));
			formLayout.add(groupContainer);

			File logoFile = dps.getLargestLogo(identityToModify);
			logoUpload = uifactory.addFileElement(getWindowControl(), getIdentity(), "logo.select", "logo.select", groupContainer);
			logoUpload.setMaxUploadSizeKB(10000, null, null);
			logoUpload.setPreview(ureq.getUserSession(), true);
			logoUpload.addActionListener(FormEvent.ONCHANGE);
			logoUpload.setHelpTextKey("ul.select.fhelp", null);
			logoUpload.setDeleteEnabled(true);
			logoUpload.setEnabled(canModify);
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
		}
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private void initEmailForm(User user, FormItem formItem, FormLayoutContainer groupContainer) {
		// special case for email field
		emailEl = (TextElement)formItem;
		
		emailLayoutContainer = FormLayoutContainer.createButtonLayout("emails.buttons", getTranslator());
		groupContainer.add(emailLayoutContainer);
		emailLayoutContainer.setRootForm(mainForm);
		
		confirmEmailInProcessButton = uifactory.addFormLink("confirm.email.in.process", emailLayoutContainer, Link.BUTTON_SMALL);
		confirmEmailInProcessButton.setIconLeftCSS("o_icon o_icon_ok");
		removeEmailInProcessButton = uifactory.addFormLink("remove.emails.in.process", emailLayoutContainer, Link.BUTTON_SMALL);
		removeEmailInProcessButton.setIconLeftCSS("o_icon o_icon_delete");
		
		if (!userModule.isEmailMandatory()) {
			emailEl.setMandatory(false);
		}
		
		updateEmailForm(user);
	}
	
	private void updateEmailForm(User user) {
		String key = user.getProperty("emchangeKey", null);
		TemporaryKey tempKey = registrationManager.loadTemporaryKeyByRegistrationKey(key);
		boolean buttonsVisible = false;
		if (tempKey != null) {
			Map<String, String> mails = registrationManager.readTemporaryValue(tempKey.getEmailAddress());
			String mail = mails.get("changedEMail");
			emailEl.setExampleKey("email.change.form.info", new String[] { mail });
			emailEl.setElementCssClass("o_omit_margin");
			buttonsVisible = isAdministrativeUser;
		} else {
			emailEl.setExampleKey(null, null);
			emailEl.setElementCssClass(null);
		}
		removeEmailInProcessButton.setVisible(buttonsVisible);
		confirmEmailInProcessButton.setVisible(buttonsVisible);
		emailLayoutContainer.setVisible(buttonsVisible);
	}

	/**
	 * Stores the data from the form into a) the user's home page configuration
	 * and b) the user's properties.
	 * 
	 * @param config The user's home page configuration (i.e. flags for publicly
	 *          visible fields).
	 * @param identity The user's identity
	 */
	public void updateFromFormData() {
		User user = identityToModify.getUser();
		// For each user property...
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			// ...get the value from the form field and store it into the user
			// property...
			FormItem formItem = formItems.get(userPropertyHandler.getName());
			if(formItem.isEnabled()) {
				userPropertyHandler.updateUserFromFormItem(user, formItem);
			}
		}
		// Store the "about me" text.
		HomePageConfig conf = hpcm.loadConfigFor(identityToModify);
		conf.setTextAboutMe(textAboutMe.getValue());
		hpcm.saveConfigTo(identityToModify, conf);
	}
	
	public Identity updateIdentityFromFormData(Identity identity) {
		identityToModify = identity;
		User user = identityToModify.getUser();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem formItem = formItems.get(userPropertyHandler.getName());
			if(formItem.isEnabled()) {
				userPropertyHandler.updateUserFromFormItem(user, formItem);
			}
		}
		return identityToModify;
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
				textAboutMe.setErrorKey("input.toolong", new String[] {"10000"});
				allOk = false;
			} else {
				textAboutMe.clearError();
			}
		} catch (Exception e) {
			textAboutMe.setErrorKey("input.toolong", new String[] {"10000"});
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

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogCtr) {
			dialogCtr.dispose();
			dialogCtr = null;
			if (DialogBoxUIFactory.isYesEvent(event)) {
				if (changedEmail != null) {
					createChangeEmailWorkflow(ureq);
				}
			}
			fireEvent(ureq, Event.FAILED_EVENT);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(removeEmailInProcessButton == source) {
			doRemoveEmailsFromProcess(ureq);
		} else if(confirmEmailInProcessButton == source) {
			doConfirmEmailInProcess(ureq);
		} else if (source == portraitUpload) {
			if(event instanceof FileElementEvent) {
				if(FileElementEvent.DELETE.equals(event.getCommand())) {
					portraitDeleted = true;
					portraitUpload.setInitialFile(null);
					if(portraitUpload.getUploadFile() != null) {
						portraitUpload.reset();
					}
					flc.setDirty(true);
				}
			} else if (portraitUpload.isUploadSuccess()) {
				flc.setDirty(true);
			}
		} else if (source == logoUpload) {
			if(event instanceof FileElementEvent) {
				if(FileElementEvent.DELETE.equals(event.getCommand())) {
					logoDeleted = true;
					logoUpload.setInitialFile(null);
					if(logoUpload.getUploadFile() != null) {
						logoUpload.reset();
					}
					flc.setDirty(true);
				}
			} else if (logoUpload.isUploadSuccess()) {
				flc.setDirty(true);
			}
		}

		super.formInnerEvent(ureq, source, event);
	}
	
	private void doRemoveEmailsFromProcess(UserRequest ureq) {
		String key = identityToModify.getUser().getProperty("emchangeKey", null);
		if(StringHelper.containsNonWhitespace(key)) {
			TemporaryKey tempKey = registrationManager.loadTemporaryKeyByRegistrationKey(key);
			if(tempKey != null) {
				registrationManager.deleteTemporaryKey(tempKey);
			}
			
			identityToModify.getUser().setProperty("emchangeKey", null);
			userManager.updateUserFromIdentity(identityToModify);
			identityToModify = securityManager.loadIdentityByKey(identityToModify.getKey());
		}
		
		updateEmailForm(identityToModify.getUser());
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doConfirmEmailInProcess(UserRequest ureq) {
		String key = identityToModify.getUser().getProperty("emchangeKey", null);
		if(StringHelper.containsNonWhitespace(key)) {
			TemporaryKey tempKey = registrationManager.loadTemporaryKeyByRegistrationKey(key);
			if(tempKey != null) {
				Map<String, String> mails = registrationManager.readTemporaryValue(tempKey.getEmailAddress());
				String mail = mails.get("changedEMail");
				String oldEmail = identityToModify.getUser().getEmail();
				
				identityToModify.getUser().setProperty("emchangeKey", null);
				identityToModify.getUser().setProperty("email", mail);
				String value = identityToModify.getUser().getProperty("emailDisabled", null);
				if (value != null && value.equals("true")) {
					identityToModify.getUser().setProperty("emailDisabled", "false");
				}
				
				userManager.updateUserFromIdentity(identityToModify);
				identityToModify = securityManager.loadIdentityByKey(identityToModify.getKey());
				registrationManager.deleteTemporaryKey(tempKey);
				securityManager.deleteInvalidAuthenticationsByEmail(oldEmail);
				
				emailEl.setValue(identityToModify.getUser().getEmail());
			}
		}

		updateEmailForm(identityToModify.getUser());
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void notifyPortraitChanged() {
		ProfileEvent newPortraitEvent = new ProfileEvent("changed-portrait", identityToModify.getKey());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("portrait", getIdentity().getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(newPortraitEvent, ores);
	}

	@Override
	protected void formOK(final UserRequest ureq) {
		User user = identityToModify.getUser();
		// update each user field
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem formItem = formItems.get(userPropertyHandler.getName());
			if(formItem.isEnabled()) {
				userPropertyHandler.updateUserFromFormItem(user, formItem);
			}
		}
		
		if (portraitDeleted) {
			File img = dps.getLargestPortrait(identityToModify);
			if(img != null) {
				dps.deletePortrait(identityToModify);
				notifyPortraitChanged();
			}
		}
		
		File uploadedImage = portraitUpload.getUploadFile();
		String uploadedFilename = portraitUpload.getUploadFileName();
		if(uploadedImage != null) {
			dps.setPortrait(uploadedImage, uploadedFilename, identityToModify);
			notifyPortraitChanged();
		}
		
		if (logoDeleted) {
			File img = dps.getLargestLogo(identityToModify);
			if(img != null) {
				dps.deleteLogo(identityToModify);
				notifyPortraitChanged();
			}
		}
		
		if(logoUpload != null) {
			File uploadedLogo = logoUpload.getUploadFile();
			String uploadedLogoname = logoUpload.getUploadFileName();
			if(uploadedLogo != null) {
				dps.setLogo(uploadedLogo, uploadedLogoname, identityToModify);
				notifyPortraitChanged();
			}
		}
		
		// Store the "about me" text.
		HomePageConfig conf = hpcm.loadConfigFor(identityToModify);
		conf.setTextAboutMe(textAboutMe.getValue());
		hpcm.saveConfigTo(identityToModify, conf);

		// fire the appropriate event
		fireEvent(ureq, Event.DONE_EVENT);

		// update the user profile data
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(
			OresHelper.createOLATResourceableInstance(Identity.class, identityToModify.getKey()), new SyncerExecutor() {
			@Override
			public void execute() {
				identityToModify = securityManager.loadIdentityByKey(identityToModify.getKey());
				currentEmail = identityToModify.getUser().getProperty(UserConstants.EMAIL, null);

				identityToModify = updateIdentityFromFormData(identityToModify);
				changedEmail = identityToModify.getUser().getProperty("email", null);
				emailChanged = false;
				if ((currentEmail == null && StringHelper.containsNonWhitespace(changedEmail))
						|| (currentEmail != null && !currentEmail.equals(changedEmail))) {
					if (isAllowedToChangeEmailWithoutVerification(ureq) || !StringHelper.containsNonWhitespace(changedEmail)) {
						String key = identityToModify.getUser().getProperty("emchangeKey", null);
						TemporaryKey tempKey = registrationManager.loadTemporaryKeyByRegistrationKey(key);
						if (tempKey != null) {
							registrationManager.deleteTemporaryKey(tempKey);
						}
						securityManager.deleteInvalidAuthenticationsByEmail(currentEmail);
					} else {
						emailChanged = true;
						// change email address to old address until it is verified
						identityToModify.getUser().setProperty(UserConstants.EMAIL, currentEmail);
					}
				}
				if (!userManager.updateUserFromIdentity(identityToModify)) {
					getWindowControl().setInfo(translate("profile.unsuccessful"));
					// reload user data from db
					identityToModify = securityManager.loadIdentityByKey(identityToModify.getKey());
				}
				
				OLATResourceable modRes = OresHelper.createOLATResourceableInstance(Identity.class, identityToModify.getKey());
				CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(new MultiUserEvent("changed"), modRes);
				
				if (!emailChanged) {
					fireEvent(ureq, Event.FAILED_EVENT);
				}
			}
		});
		
		if (emailChanged) {
			removeAsListenerAndDispose(dialogCtr);

			String dialogText = "";
			if(identityToModify.equals(ureq.getIdentity())) {
				dialogText = translate("email.change.dialog.text");
			} else {
				dialogText = translate("email.change.dialog.text.usermanager");
			}
			dialogCtr = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), translate("email.change.dialog.title"), dialogText);
			listenTo(dialogCtr);
			dialogCtr.activate();
		}
	}
	
	private void createChangeEmailWorkflow(UserRequest ureq) {
		// send email
		changedEmail = changedEmail.trim();
		String body = null;
		String subject = null;
		// get remote address
		String ip = ureq.getHttpReq().getRemoteAddr();
		String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
		// mailer configuration
		String serverpath = Settings.getServerContextPathURI();
		String servername = ureq.getHttpReq().getServerName();

		logDebug("this servername is " + servername + " and serverpath is " + serverpath);
		// load or create temporary key
		Map<String, String> mailMap = new HashMap<>();
		mailMap.put("currentEMail", currentEmail);
		mailMap.put("changedEMail", changedEmail);
		String serMailMap = registrationManager.temporaryValueToString(mailMap);
		TemporaryKey tk = registrationManager.createAndDeleteOldTemporaryKey(identityToModify.getKey(), serMailMap, ip, RegistrationManager.EMAIL_CHANGE, null);
		
		// create date, time string
		Calendar cal = Calendar.getInstance();
		cal.setTime(tk.getCreationDate());
		cal.add(Calendar.DAY_OF_WEEK, ChangeEMailController.TIME_OUT);
		String time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(cal.getTime());
		// create body and subject for email
		String link = serverpath + DispatcherModule.getPathDefault() + "emchange/index.html?key=" + tk.getRegistrationKey() + "&language=" + ureq.getLocale().getLanguage();
		if(Settings.isDebuging()) {
			logInfo(link);
		}
		String currentEmailDisplay = userManager.getUserDisplayEmail(currentEmail, getLocale());
		String changedEmaildisplay = userManager.getUserDisplayEmail(changedEmail, getLocale());
		body = translate("email.change.body", new String[] { link, time, currentEmailDisplay, changedEmaildisplay })
				+ SEPARATOR + translate("email.change.wherefrom", new String[] { serverpath, today });
		subject = translate("email.change.subject");
		// send email
		try {
			
			MailBundle bundle = new MailBundle();
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setTo(changedEmail);
			bundle.setContent(subject, body);

			MailerResult result = mailManager.sendMessage(bundle);
			boolean isMailSent = result.isSuccessful();
			if (isMailSent) {
				tk.setMailSent(true);
				// set key
				User user = this.identityToModify.getUser();
				user.setProperty("emchangeKey", tk.getRegistrationKey());
				UserManager.getInstance().updateUser(user);
				getWindowControl().setInfo(translate("email.sent"));
			} else {
				tk.setMailSent(false);
				registrationManager.deleteTemporaryKeyWithId(tk.getRegistrationKey());
				getWindowControl().setError(translate("email.notsent"));
			}
		} catch (Exception e) {
			registrationManager.deleteTemporaryKeyWithId(tk.getRegistrationKey());
			getWindowControl().setError(translate("email.notsent"));
		}
	}

	/**
	 * Sets the dirty mark for this form.
	 * 
	 * @param isDirtyMarking <code>true</code> sets this form dirty.
	 */
	public void setDirtyMarking(boolean isDirtyMarking) {
		mainForm.setDirtyMarking(isDirtyMarking);
	}

	private boolean isAllowedToChangeEmailWithoutVerification(UserRequest ureq) {
		Roles managerRoles = ureq.getUserSession().getRoles();
		Roles identityToModifyRoles  = securityManager.getRoles(identityToModify);
		return managerRoles.isManagerOf(OrganisationRoles.administrator, identityToModifyRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.usermanager, identityToModifyRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, identityToModifyRoles);
	}
}
