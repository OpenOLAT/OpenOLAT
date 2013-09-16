/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.user;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.olat.registration.TemporaryKeyImpl;

import com.thoughtworks.xstream.XStream;

import de.bps.olat.user.ChangeEMailController;

/**
 * Initial Date: Jul 14, 2005
 * 
 * @author Alexander Schneider
 * 
 *         Comment:
 */
public class ProfileAndHomePageEditController extends BasicController implements SupportsAfterLoginInterceptor {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ProfileAndHomePageEditController.class);
	private static final long UPLOAD_LIMIT_KB = 500;

	private VelocityContainer myContent;
	private Link previewButton;

	private Translator translator;

	protected ProfileFormController profileFormController;
	private Component profileFormControllerComponent;
	private HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
	private PortraitUploadController portraitUploadController;
	private Controller hpDispC;
	private CloseableModalController clc;
	protected Identity identityToModify;
	protected HomePageConfig homePageConfig;
	private DialogBoxController dialogCtr;
	private final MailManager mailManager;
	private RegistrationManager rm = RegistrationManager.getInstance();
	private static String SEPARATOR = "\n____________________________________________________________________\n";

	private boolean emailChanged = false;
	protected String changedEmail;
	protected String currentEmail;
	private boolean isAdministrativeUser;

	public ProfileAndHomePageEditController(UserRequest ureq, WindowControl wControl) {		
		this(ureq,wControl,ureq.getIdentity(),ureq.getUserSession().getRoles().isOLATAdmin());
	}
	
	/**
	 * @param ureq
	 * @param wControl
	 * @param identity the identity to be changed. Can be different than current
	 *          user (usermanager that edits another users profile)
	 * @param isAdministrativeUser
	 */
	public ProfileAndHomePageEditController(UserRequest ureq, WindowControl wControl, Identity identityToModify, boolean isAdministrativeUser) {
		super(ureq, wControl);
		this.identityToModify = identityToModify;
		this.isAdministrativeUser = isAdministrativeUser;
		this.translator = Util.createPackageTranslator(ProfileAndHomePageEditController.class, ureq.getLocale());
		this.translator = UserManager.getInstance().getPropertyHandlerTranslator(this.translator);
		mailManager = CoreSpringFactory.getImpl(MailManager.class);

		this.myContent = new VelocityContainer("homepage", VELOCITY_ROOT + "/homepage.html", this.translator, this);
		this.previewButton = LinkFactory.createButtonSmall("command.preview", this.myContent, this);
		this.homePageConfig = this.hpcm.loadConfigFor(this.identityToModify.getName());

		this.profileFormController = new ProfileFormController(ureq, wControl, this.homePageConfig, this.identityToModify, isAdministrativeUser);
		listenTo(this.profileFormController);
		this.profileFormControllerComponent = this.profileFormController.getInitialComponent();
		this.myContent.put("homepageform", this.profileFormControllerComponent);

		this.portraitUploadController = new PortraitUploadController(ureq, getWindowControl(), this.identityToModify, UPLOAD_LIMIT_KB);
		listenTo(this.portraitUploadController);

		Component c = this.portraitUploadController.getInitialComponent();
		this.myContent.put("c", c);
		putInitialPanel(this.myContent);
	}
	
	@Override
	public boolean isInterceptionRequired(UserRequest ureq) {
		if (ureq.getUserSession().getRoles() == null || ureq.getUserSession().getRoles().isInvitee()
				|| ureq.getUserSession().getRoles().isGuestOnly()) return false;
		else return true; 
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == this.previewButton) {
			if (this.hpDispC != null) removeAsListenerAndDispose(this.hpDispC);
			hpDispC = new HomePageDisplayController(ureq, getWindowControl(), identityToModify, homePageConfig);
			listenTo(hpDispC);
			if (clc != null) removeAsListenerAndDispose(clc);
			clc = new CloseableModalController(getWindowControl(), this.translator.translate("command.closehp"), this.hpDispC
					.getInitialComponent());
			listenTo(clc);
			clc.activate();
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(final UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == this.portraitUploadController) {
			if (event.equals(Event.DONE_EVENT) || event.getCommand().equals(PortraitUploadController.PORTRAIT_DELETED_EVENT.getCommand())) {
				// should not fire event, as only needed to update identity if useradmin changed it. portrait doesnt change identity and is not shown in table. 
				// see UserAdminController
//				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if (source == this.profileFormController) {
			if (event == Event.DONE_EVENT) {
				// get the new values from the form
				this.profileFormController.updateFromFormData(this.homePageConfig, this.identityToModify);

				// update the home page configuration
				this.hpcm.saveConfigTo(this.identityToModify.getName(), this.homePageConfig);
				
				// update the portrait upload (gender specific image)
				if (portraitUploadController != null) removeAsListenerAndDispose(portraitUploadController);
				portraitUploadController = new PortraitUploadController(ureq, getWindowControl(), this.identityToModify, UPLOAD_LIMIT_KB);
				listenTo(this.portraitUploadController);
				this.myContent.put("c", this.portraitUploadController.getInitialComponent());
				
				// fire the appropriate event
				fireEvent(ureq, Event.DONE_EVENT);

				// update the user profile data
				CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(
					OresHelper.createOLATResourceableInstance(Identity.class, ProfileAndHomePageEditController.this.identityToModify.getKey()), new SyncerExecutor() {
						@SuppressWarnings("synthetic-access")
						public void execute() {
							UserManager um = UserManager.getInstance();
							ProfileAndHomePageEditController.this.identityToModify = (Identity) DBFactory.getInstance().loadObject(ProfileAndHomePageEditController.this.identityToModify);
							currentEmail = ProfileAndHomePageEditController.this.identityToModify.getUser().getProperty("email", null);

							ProfileAndHomePageEditController.this.identityToModify = profileFormController
									.updateIdentityFromFormData(ProfileAndHomePageEditController.this.identityToModify);
							changedEmail = ProfileAndHomePageEditController.this.identityToModify.getUser().getProperty("email", null);
							if (!currentEmail.equals(changedEmail)) {
								// allow an admin to change email without verification workflow. usermanager is only permitted to do so, if set by config.
								if ( !(ureq.getUserSession().getRoles().isOLATAdmin() || ( BaseSecurityModule.USERMANAGER_CAN_BYPASS_EMAILVERIFICATION && ureq.getUserSession().getRoles().isUserManager() ))) {
									emailChanged = true;
									// change email address to old address until it is verified
									ProfileAndHomePageEditController.this.identityToModify.getUser().setProperty("email", currentEmail);
								} else {
									// fxdiff: FXOLAT-44 delete previous change-workflows
									String key = ProfileAndHomePageEditController.this.identityToModify.getUser().getProperty("emchangeKey", null);
									TemporaryKeyImpl tempKey = rm.loadTemporaryKeyByRegistrationKey(key);
									if (tempKey != null) {
										rm.deleteTemporaryKey(tempKey);
									}		
								}
							}
							if (!um.updateUserFromIdentity(ProfileAndHomePageEditController.this.identityToModify)) {
								getWindowControl().setInfo(translate("profile.unsuccessful"));
								// reload user data from db
								ProfileAndHomePageEditController.this.identityToModify = BaseSecurityManager.getInstance().loadIdentityByKey(
										ProfileAndHomePageEditController.this.identityToModify.getKey());
							}
							CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new MultiUserEvent("changed"),
									OresHelper.createOLATResourceableInstance(Identity.class, ProfileAndHomePageEditController.this.identityToModify.getKey()));
							if (!emailChanged) resetForm(ureq, getWindowControl());
						}
					});
				if (emailChanged) {
					if (dialogCtr != null) {
						dialogCtr.dispose();
					}
					String changerEMail = ureq.getIdentity().getUser().getProperty("email", ureq.getLocale());
					String dialogText = "";
					if(changerEMail != null && changerEMail.length() > 0 && changerEMail.equals(currentEmail)) {
						dialogText = translate("email.change.dialog.text");
					} else {
						dialogText = translate("email.change.dialog.text.usermanager");
					}
					dialogCtr = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), translate("email.change.dialog.title"), dialogText);
					dialogCtr.addControllerListener(this);
					dialogCtr.activate();
				}
			}
		} else if (source == dialogCtr) {
			dialogCtr.dispose();
			dialogCtr = null;
			if (DialogBoxUIFactory.isYesEvent(event)) {
				if (changedEmail != null) {
					createChangeEmailWorkflow(ureq);
				}
			}
			resetForm(ureq, getWindowControl());
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

		logDebug("this servername is " + servername + " and serverpath is " + serverpath, null);
		// load or create temporary key
		Map<String, String> mailMap = new HashMap<String, String>();
		mailMap.put("currentEMail", currentEmail);
		mailMap.put("changedEMail", changedEmail);
		
		XStream xml = new XStream();
		String serMailMap = xml.toXML(mailMap);
		
		TemporaryKey tk = loadCleanTemporaryKey(serMailMap);				
		if (tk == null) {
			tk = rm.createTemporaryKeyByEmail(serMailMap, ip, RegistrationManager.EMAIL_CHANGE);
		} else {
			rm.deleteTemporaryKeyWithId(tk.getRegistrationKey());
			tk = rm.createTemporaryKeyByEmail(serMailMap, ip, RegistrationManager.EMAIL_CHANGE);
		}
		
		// create date, time string
		Calendar cal = Calendar.getInstance();
		cal.setTime(tk.getCreationDate());
		cal.add(Calendar.DAY_OF_WEEK, ChangeEMailController.TIME_OUT);
		String time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(cal.getTime());
		// create body and subject for email
		body = this.translator.translate("email.change.body", new String[] { serverpath + "/dmz/emchange/index.html?key=" + tk.getRegistrationKey() + "&lang=" + ureq.getLocale().getLanguage(), time, currentEmail, changedEmail })
				+ SEPARATOR + this.translator.translate("email.change.wherefrom", new String[] { serverpath, today, ip });
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
				getWindowControl().setInfo(this.translator.translate("email.sent"));
			} else {
				tk.setMailSent(false);
				rm.deleteTemporaryKeyWithId(tk.getRegistrationKey());
				getWindowControl().setError(this.translator.translate("email.notsent"));
			}
		} catch (Exception e) {
			rm.deleteTemporaryKeyWithId(tk.getRegistrationKey());
			getWindowControl().setError(translator.translate("email.notsent"));
		}
	}

	/**
	 * Load and clean temporary keys with action "EMAIL_CHANGE".
	 * @param serMailMap
	 * @return
	 */
	private TemporaryKey loadCleanTemporaryKey(String serMailMap) {
		TemporaryKey tk = rm.loadTemporaryKeyByEmail(serMailMap);
		if (tk == null) {
			XStream xml = new XStream();
			@SuppressWarnings("unchecked")
			Map<String, String> mails = (Map<String, String>) xml.fromXML(serMailMap);
			String currentEMail = mails.get("currentEMail");
			List<TemporaryKey> tks = rm.loadTemporaryKeyByAction(RegistrationManager.EMAIL_CHANGE);
			if (tks != null) {
				synchronized (tks) {
					tks = rm.loadTemporaryKeyByAction(RegistrationManager.EMAIL_CHANGE);
					int countCurrentEMail = 0;
					for (TemporaryKey temporaryKey : tks) {
						@SuppressWarnings("unchecked")
						Map<String, String> tkMails = (Map<String, String>) xml.fromXML(temporaryKey.getEmailAddress());
						if (tkMails.get("currentEMail").equals(currentEMail)) {
							if (countCurrentEMail > 0) {
								// clean
								rm.deleteTemporaryKeyWithId(temporaryKey.getRegistrationKey());
							} else {
								// load
								tk = temporaryKey;
							}
							countCurrentEMail++;
						}
					}
				}
			}
		}
		return tk;
	}


	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
	// controllers disposed by basic controller
	}

	public void resetForm(UserRequest ureq, WindowControl wControl) {
		if (this.profileFormController != null) {
			this.myContent.remove(this.profileFormControllerComponent);
			removeAsListenerAndDispose(this.profileFormController);
		}
		this.profileFormController = new ProfileFormController(ureq, wControl, this.homePageConfig, this.identityToModify, isAdministrativeUser);
		listenTo(this.profileFormController);
		this.profileFormControllerComponent = this.profileFormController.getInitialComponent();
		this.myContent.put("homepageform", this.profileFormControllerComponent);
	}
}
