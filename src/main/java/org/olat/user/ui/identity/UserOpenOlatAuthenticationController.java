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
package org.olat.user.ui.identity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
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
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.ldap.LDAPError;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.login.webauthn.PasskeyLevels;
import org.olat.login.webauthn.ui.NewPasskeyController;
import org.olat.login.webauthn.ui.PasskeyListController;
import org.olat.login.webauthn.ui.RecoveryKeysController;
import org.olat.restapi.RestModule;
import org.olat.restapi.ui.RestApiKeyListController;
import org.olat.user.ChangePasswordForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserOpenOlatAuthenticationController extends BasicController {
	
	public static final String HELP_URL = "manual_user/login_registration/";
	
	private Link changeSettingsButton;
	private final VelocityContainer mainVC;
	
	private final Roles roles;
	private final boolean canUpgrade;
	private final boolean withPasskey;
	private final boolean withApiKey;
	private PasskeyLevels currentLevel;
	private PasskeyLevels minimalLevel;
	private List<Authentication> authentications;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RestModule restModule;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LDAPLoginManager ldapLoginManager;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;

	private CloseableModalController cmc;
	private ChangePasswordForm newPasswordCtrl;
	private NewPasskeyController newPasskeyCtrl;
	private final ChangePasswordForm changePwdForm;
	private RecoveryKeysController recoveryKeysCtrl;
	private final PasskeyListController passkeyListCtrl;
	private final UserRecoveryKeysController recoveryKeyCtrl;
	private final RestApiKeyListController restApiKeyListCtrl;
	private UserAuthenticationChangeSettingsController changeSettingsCtrl;
	
	public UserOpenOlatAuthenticationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		roles = ureq.getUserSession().getRoles();
		authentications = securityManager.getAuthentications(getIdentity());

		mainVC = createVelocityContainer("openolat_authentication");
		
		minimalLevel = loginModule.getPasskeyLevel(roles);
		
		currentLevel = PasskeyLevels.currentLevel(authentications);
		
		withApiKey = restModule.isEnabled() && restModule.isUserAllowedGenerateApiKey();
		withPasskey = loginModule.isOlatProviderWithPasskey();
		canUpgrade = loginModule.isPasskeyUpgradeAllowed();
		
		mainVC.contextPut("withPasskey", Boolean.valueOf(withPasskey));
		mainVC.contextPut("helpUrl", HELP_URL);
		
		initOverview();
		
		// Change password
		changePwdForm = new ChangePasswordForm(ureq, wControl, getIdentity(), true);
		listenTo(changePwdForm);
		mainVC.put("chpwdform", changePwdForm.getInitialComponent());
		Authentication olatAuthentication = getAuthenticationbyProvider(authentications, "OLAT");
		changePwdForm.getInitialComponent().setVisible(olatAuthentication != null);
		
		// List of passkeys
		passkeyListCtrl = new PasskeyListController(ureq, getWindowControl(),
				getIdentity(), olatAuthentication == null, false, false);
		listenTo(passkeyListCtrl);
		mainVC.put("passkeys", passkeyListCtrl.getInitialComponent());
		boolean passKeyAvailable = withPasskey
				&& getAuthenticationbyProvider(authentications, OLATWebAuthnManager.PASSKEY) != null;
		passkeyListCtrl.getInitialComponent().setVisible(passKeyAvailable);
		
		recoveryKeyCtrl = new UserRecoveryKeysController(ureq, getWindowControl());
		listenTo(recoveryKeyCtrl);
		mainVC.put("recoverykeys", recoveryKeyCtrl.getInitialComponent());
		recoveryKeyCtrl.getInitialComponent().setVisible(passKeyAvailable);
		
		restApiKeyListCtrl = new RestApiKeyListController(ureq, getWindowControl());
		listenTo(restApiKeyListCtrl);
		mainVC.put("restApiKeys", restApiKeyListCtrl.getInitialComponent());
		restApiKeyListCtrl.getInitialComponent().setVisible(withApiKey);
		
		putInitialPanel(mainVC);
	}
	
	public boolean hasAuthentications() {
		return (getAuthenticationbyProvider(authentications, "OLAT") != null) ||
				(withPasskey && getAuthenticationbyProvider(authentications, OLATWebAuthnManager.PASSKEY) != null);
	}
	
	private void initOverview() {
		List<PasskeyLevels> possibleLevels = getPossibleLevels();
		if(withPasskey) {
			String currentLevelName = currentLevel == null ? "none" : currentLevel.name();
			mainVC.contextPut("level", currentLevelName);
		
			String levelString;
			if(canUpgrade) {
				levelString = translate("security.level." + currentLevelName);
			} else {
				levelString = translate("security.level." + currentLevelName + ".fix");
			}
			mainVC.contextPut("title", translate("security.level.title", levelString));
			mainVC.contextPut("explain", translate("security.level." + currentLevelName + ".explain"));

			if(canUpgrade && !possibleLevels.isEmpty()) {
				initUpgradeMessages();
				changeSettingsButton = LinkFactory.createButton("change.authentication.settings", mainVC, this);
				changeSettingsButton.setElementCssClass("btn-primary");
			}
		} else {
			mainVC.contextPut("level", PasskeyLevels.level1.name());
			String levelString = translate("security.level.level1.only");
			mainVC.contextPut("title", translate("security.level.title", levelString));
			mainVC.contextPut("explain", translate("security.level.level1.only.explain"));
		}
	}
	
	private void initUpgradeMessages() {
		String message;
		if(currentLevel == PasskeyLevels.level3) {
			if(minimalLevel == PasskeyLevels.level2) {
				message = translate("security.level.change.level3.to.level2");
			} else {
				message = translate("security.level.change.level3");
			}
		} else if(currentLevel == PasskeyLevels.level2) {
			if(minimalLevel == PasskeyLevels.level2) {
				message = translate("security.level.change.level2.to.level3");
			} else {
				message = translate("security.level.change.level2");
			}
		} else {
			message = translate("security.level.change.level1");
		}
		mainVC.contextPut("upgrade", message);
	}
	
	private Authentication getAuthenticationbyProvider(List<Authentication> authentications, String provider) {
		return authentications.stream()
				.filter(auth -> provider.equals(auth.getProvider()))
				.findFirst()
				.orElse(null);
	}
	
	private void updateUI() {
		boolean passKeyAvailable = withPasskey && getAuthenticationbyProvider(authentications, OLATWebAuthnManager.PASSKEY) != null;
		passkeyListCtrl.loadModel();
		passkeyListCtrl.getInitialComponent().setVisible(passKeyAvailable);
		recoveryKeyCtrl.getInitialComponent().setVisible(passKeyAvailable);
		
		Authentication olatAuthentication = getAuthenticationbyProvider(authentications, "OLAT");
		changePwdForm.getInitialComponent().setVisible(olatAuthentication != null);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(changePwdForm == source) {
			String oldPwd = changePwdForm.getOldPasswordValue();
			String newPwd = changePwdForm.getNewPasswordValue();
			doChangePassword(ureq, oldPwd, newPwd);
		} else if(changeSettingsCtrl == source) {
			final PasskeyLevels nextLevel = changeSettingsCtrl.getSelectedLevel();
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT && nextLevel != null) {
				doLevel(ureq, nextLevel);
			}
		} else if(newPasskeyCtrl == source) {
			cmc.deactivate();
			cleanUp();
			// All is done by the passkey controller
			doFinishLevelChange(ureq);
		} else if(recoveryKeysCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(newPasswordCtrl == source
				&& newPasswordCtrl.getUserObject() instanceof PasskeyLevels level) {
			// Change password controller does nothing
			if(event == Event.DONE_EVENT) {
				doFinishChangePassword(ureq, newPasswordCtrl.getNewPasswordValue(), level);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(changeSettingsCtrl);
		removeAsListenerAndDispose(recoveryKeysCtrl);
		removeAsListenerAndDispose(newPasswordCtrl);
		removeAsListenerAndDispose(newPasskeyCtrl);
		removeAsListenerAndDispose(cmc);
		changeSettingsCtrl = null;
		recoveryKeysCtrl = null;
		newPasswordCtrl = null;
		newPasskeyCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(changeSettingsButton == source) {
			doChangeSettings(ureq);
		}
	}
	
	private void doChangeSettings(UserRequest ureq) {
		List<PasskeyLevels> possibleLevels = getPossibleLevels();
		if(possibleLevels.size() == 1) {
			PasskeyLevels nextLevel = possibleLevels.get(0);
			doLevel(ureq, nextLevel);
		} else {
			changeSettingsCtrl = new UserAuthenticationChangeSettingsController(ureq, getWindowControl(), currentLevel, possibleLevels);
			listenTo(changeSettingsCtrl);
			
			String title = translate("change.authentication.settings");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), changeSettingsCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doLevel(UserRequest ureq, PasskeyLevels level) {
		if(level == PasskeyLevels.level1) {
			if(currentLevel == PasskeyLevels.level3) {
				doRemovePasskey(ureq);
			} else {
				doChangePassword(ureq, level);
			}
		} else if(level == PasskeyLevels.level2) {
			if(currentLevel == PasskeyLevels.level3) {
				doRemoveOlatAuthentication(ureq);
			} else {
				doGeneratePasskey(ureq, level);
			}
		} else if(level == PasskeyLevels.level3) {
			Authentication olatAuthentication = getAuthenticationbyProvider(authentications, "OLAT");
			Authentication somePasskey = getAuthenticationbyProvider(authentications, OLATWebAuthnManager.PASSKEY);
			if((olatAuthentication != null && somePasskey != null)
					|| olatAuthentication != null) {
				doGeneratePasskey(ureq, level);
			} else if(somePasskey != null) {
				doChangePassword(ureq, level);
			}
		}
	}
	
	private void doChangePassword(UserRequest ureq, String oldPwd, String newPwd) {
		Identity provenIdent = null;
		Authentication ldapAuthentication = securityManager.findAuthentication(getIdentity(), LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		if (ldapAuthentication != null) {
			LDAPError ldapError = new LDAPError();
			//fallback to OLAT if enabled happen automatically in LDAPAuthenticationController
			String userName = ldapAuthentication.getAuthusername();
			provenIdent = ldapLoginManager.authenticate(userName, oldPwd, ldapError);
		} else if(securityManager.findAuthentication(ureq.getIdentity(), BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER) != null) {
			provenIdent = olatAuthenticationSpi.authenticate(ureq.getIdentity(), ureq.getIdentity().getName(), oldPwd, new AuthenticationStatus());
		}

		if (provenIdent == null || !provenIdent.equals(getIdentity())) {
			showError("error.password.noauth");	
		} else {
			if(olatAuthenticationSpi.changePassword(getIdentity(), getIdentity(), newPwd)) {			
				fireEvent(ureq, Event.DONE_EVENT);
				getLogger().info(Tracing.M_AUDIT, "Changed password for identity: {}", getIdentity().getKey());
				showInfo("password.successful");
			} else {
				showError("password.failed");
			}
		}
	}
	
	private void doChangePassword(UserRequest ureq, PasskeyLevels level) {
		newPasswordCtrl = new ChangePasswordForm(ureq, getWindowControl(), getIdentity(), false);
		newPasswordCtrl.setUserObject(level);
		listenTo(newPasswordCtrl);
		
		String title = translate("new.password.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), newPasswordCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doFinishChangePassword(UserRequest ureq, String newPwd, PasskeyLevels level) {
		if(olatAuthenticationSpi.changePassword(getIdentity(), getIdentity(), newPwd)) {		
			getLogger().info(Tracing.M_AUDIT, "Changed password for identity: {}", getIdentity().getKey());
			if(level == PasskeyLevels.level1) {
				deletePasskeys();
			}
			dbInstance.commit();
		}
		doFinishLevelChange(ureq);
	}
	
	private void doFinishLevelChange(UserRequest ureq) {
		authentications = securityManager.getAuthentications(getIdentity());
		currentLevel = PasskeyLevels.currentLevel(authentications);
		// Update the UI
		initOverview();
		initUpgradeMessages();
		updateUI();
		
		if(currentLevel == PasskeyLevels.level2 || currentLevel == PasskeyLevels.level3) {
			doShowRecoveryKey(ureq);
		}
		
	}
	
	private void doShowRecoveryKey(UserRequest ureq) {
		recoveryKeysCtrl = new RecoveryKeysController(ureq, getWindowControl(), getIdentity());
		listenTo(recoveryKeysCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), recoveryKeysCtrl.getInitialComponent(), true, translate("new.passkey.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doRemovePasskey(UserRequest ureq) {
		deletePasskeys();
		doFinishLevelChange(ureq);
	}
	
	private void deletePasskeys() {
		List<Authentication> passkeys = securityManager.findAuthentications(getIdentity(), List.of(OLATWebAuthnManager.PASSKEY));
		for(Authentication passkey:passkeys) {
			securityManager.deleteAuthentication(passkey);
		}
		dbInstance.commit();
	}
	
	private void doRemoveOlatAuthentication(UserRequest ureq) {
		List<Authentication> olatAuthentications = securityManager.findAuthentications(getIdentity(), List.of("OLAT"));
		for(Authentication olatAuthentication:olatAuthentications) {
			securityManager.deleteAuthentication(olatAuthentication);
		}
		dbInstance.commit();
		doFinishLevelChange(ureq);
	}
	
	private void doGeneratePasskey(UserRequest ureq, PasskeyLevels level) {
		boolean deleteOlatAuthentication = (level == PasskeyLevels.level2);
		newPasskeyCtrl = new NewPasskeyController(ureq, getWindowControl(), getIdentity(), deleteOlatAuthentication, false, true);
		if(level == PasskeyLevels.level3 && currentLevel == PasskeyLevels.level1) {
			newPasskeyCtrl.setFormInfo(translate("new.passkey.level3.from.1.hint"), HELP_URL);
		} else if(level == PasskeyLevels.level3 &&currentLevel == PasskeyLevels.level2) {
			newPasskeyCtrl.setFormInfo(translate("new.passkey.level3.from.2.hint"), HELP_URL);
		} else {
			newPasskeyCtrl.setFormInfo(translate("new.passkey.level2.hint"), HELP_URL);
		}
		listenTo(newPasskeyCtrl);
		
		String title = translate("new.passkey.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), newPasskeyCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private List<PasskeyLevels> getPossibleLevels() {
		List<PasskeyLevels> levels = new ArrayList<>();
		levels.add(PasskeyLevels.level1);
		levels.add(PasskeyLevels.level2);
		levels.add(PasskeyLevels.level3);
		levels.remove(currentLevel);
		for(Iterator<PasskeyLevels> levelIt=levels.iterator(); levelIt.hasNext(); ) {
			if(levelIt.next().ordinal() < minimalLevel.ordinal()) {
				levelIt.remove();
			}
		}
		return List.copyOf(levels);
	}
}
