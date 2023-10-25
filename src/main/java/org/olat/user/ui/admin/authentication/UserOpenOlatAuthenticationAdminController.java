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
package org.olat.user.ui.admin.authentication;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.ChangeUserPasswordForm;
import org.olat.admin.user.SendTokenToUserForm;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateConfigBuilder;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.IconPanel;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.login.LoginModule;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.webauthn.PasskeyLevels;
import org.olat.login.webauthn.ui.PasskeyListController;
import org.olat.login.webauthn.ui.SendRecoveryKeyToUserForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UserOpenOlatAuthenticationAdminController extends BasicController {
	
	private Link sendPasswordLink;
	private Link resetPasswordLink;
	private final VelocityContainer mainVC;
	private final Link sendRecoveryKeysLink;
	private EmptyState noAuthenticationState;

	private final Roles roles;
	private final Formatter format;
	private final boolean withPasskey;
	private PasskeyLevels currentLevel;
	private PasskeyLevels minimalLevel;
	private final boolean canResetPassword;
	private final Identity identityToModify;
	private final boolean canSendPasswordLink;
	private List<Authentication> authentications;
	
	
	private CloseableModalController cmc;
	private SendTokenToUserForm sendPasswordLinkCtrl;
	private ChangeUserPasswordForm resetPasswordCtrl;
	private final PasskeyListController passkeyListCtrl;
	private SendRecoveryKeyToUserForm sendRecoveryKeyCtrl;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;
	
	public UserOpenOlatAuthenticationAdminController(UserRequest ureq, WindowControl wControl, Identity identityToModify,
			boolean canResetPassword, boolean canSendPasswordLink) {
		super(ureq, wControl);
		this.identityToModify = identityToModify;
		this.canResetPassword = canResetPassword;
		this.canSendPasswordLink = canSendPasswordLink;
		format = Formatter.getInstance(getLocale());
		roles = securityManager.getRoles(identityToModify);
		authentications = securityManager.getAuthentications(identityToModify);
		
		mainVC = createVelocityContainer("openolat_authentications");
		
		withPasskey = loginModule.isOlatProviderWithPasskey();
		mainVC.contextPut("withPasskey", Boolean.valueOf(withPasskey));
		
		minimalLevel = loginModule.getPasskeyLevel(roles);
		
		currentLevel = PasskeyLevels.currentLevel(authentications);
		
		// Levels
		initLevels();
		// OpenOlat authentication
		initAuthentications();
		
		// List of passkeys
		passkeyListCtrl = new PasskeyListController(ureq, getWindowControl(), identityToModify,
				getAuthenticationbyProvider(authentications, "OLAT") == null, true, true, canSendPasswordLink);
		listenTo(passkeyListCtrl);
		mainVC.put("passkeys", passkeyListCtrl.getInitialComponent());
		passkeyListCtrl.getInitialComponent().setVisible(withPasskey && currentLevel != PasskeyLevels.level1);
		
		// Recovery key
		sendRecoveryKeysLink = LinkFactory.createButton("send.recovery.key", mainVC, this);
		sendRecoveryKeysLink.setIconLeftCSS("o_icon o_icon-fw o_icon_owner");
		sendRecoveryKeysLink.setVisible(withPasskey && passkeyListCtrl.hasPasskeys());
		
		putInitialPanel(mainVC);
	}
	
	private void initLevels() {
		String levelString;
		if(withPasskey) {
			if(currentLevel == null) {
				mainVC.contextPut("level", "none");
				levelString = translate("security.level.none");
			} else {
				mainVC.contextPut("level", currentLevel.name());
				levelString = translate("security.level." + currentLevel.name());
			}
			mainVC.contextPut("minimalLevel", translate("security.level." + minimalLevel.name()));
		} else {
			mainVC.contextPut("level", PasskeyLevels.level1.name());
			levelString = translate("security.level.level1.only");
		}
		mainVC.contextPut("title", translate("security.level.title.admin", levelString));
	}

	private void initAuthentications() {
		Authentication olatAuthentication = getAuthenticationbyProvider(authentications, "OLAT");
		mainVC.contextPut("olatAuthenticationInUse", Boolean.valueOf(olatAuthentication != null));
		if(olatAuthentication != null) {
			IconPanel iconPanel = new IconPanel("olatauthentication");
			mainVC.put("olatauthentication", iconPanel);
			mainVC.remove("noolatauthentication");
			
			String username = olatAuthentication.getAuthusername();
			if(loginModule.isAllowLoginUsingEmail()
					&& StringHelper.containsNonWhitespace(identityToModify.getUser().getEmail())) {
				username += ", " + identityToModify.getUser().getEmail();
			}
			iconPanel.setTitle(username);
			iconPanel.setIconCssClass("o_icon_provider_olat");
			
			IconPanelLabelTextContent content = new IconPanelLabelTextContent("content_olatauthentication");
			iconPanel.setContent(content);

			List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("last.login"), format.formatDate(identityToModify.getLastLogin())));
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("creation.date"), format.formatDate(olatAuthentication.getCreationDate())));
			content.setLabelTexts(labelTexts);
			
			if(canResetPassword) {
				resetPasswordLink = LinkFactory.createButton("reset.password", mainVC, this);
				resetPasswordLink.setIconLeftCSS("o_icon o_icon-fw o_icon_owner");
				resetPasswordLink.setGhost(true);
				iconPanel.addLink(resetPasswordLink);
			}
			
			if(canSendPasswordLink) {
				sendPasswordLink = LinkFactory.createButton("send.password", mainVC, this);
				sendPasswordLink.setIconLeftCSS("o_icon o_icon-fw o_icon_external_link");
				sendPasswordLink.setGhost(true);
				iconPanel.addLink(sendPasswordLink);
			}
		} else {
			EmptyStateConfigBuilder config = EmptyStateConfig.builder()
					.withButtonI18nKey("new.password")
					.withIconCss("o_icon_provider_olat")
					.withIndicatorIconCss("o-empty")
					.withMessageI18nKey("olat.authentication.no.password");
			if(currentLevel != PasskeyLevels.level2 && minimalLevel != PasskeyLevels.level2) {
				config = config.withSecondaryButtonI18nKey("send.password.link");
			}
			noAuthenticationState = EmptyStateFactory.create("noolatauthentication", mainVC, this, config.build());
			mainVC.put("noolatauthentication", noAuthenticationState);
			mainVC.remove("olatauthentication");
		}
	}
	
	private Authentication getAuthenticationbyProvider(List<Authentication> authentications, String provider) {
		return authentications.stream()
				.filter(auth -> provider.equals(auth.getProvider()))
				.findFirst()
				.orElse(null);
	}
	
	private void updateUI() {
		authentications = securityManager.getAuthentications(identityToModify);
		currentLevel = PasskeyLevels.currentLevel(authentications);
		
		initLevels();
		initAuthentications();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(sendPasswordLink == source) {
			doSendPassword(ureq);
		} else if(resetPasswordLink == source) {
			doResetPassword(ureq);
		} else if(noAuthenticationState == source) {
			if(EmptyState.EVENT == event) {
				doResetPassword(ureq);
			} else if(EmptyState.SECONDARY_EVENT == event) {
				doSendPassword(ureq);
			}
		} else if(sendRecoveryKeysLink == source) {
			doSendRecoveryKey(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(passkeyListCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				updateUI();
			}
		} else if(resetPasswordCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doFinishResetPassword(resetPasswordCtrl.getNewPassword());
				updateUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if(sendRecoveryKeyCtrl == source || sendPasswordLinkCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(sendPasswordLinkCtrl);
		removeAsListenerAndDispose(sendRecoveryKeyCtrl);
		removeAsListenerAndDispose(resetPasswordCtrl);
		removeAsListenerAndDispose(cmc);
		sendPasswordLinkCtrl = null;
		sendRecoveryKeyCtrl = null;
		resetPasswordCtrl = null;
		cmc = null;
	}
	
	private void doSendPassword(UserRequest ureq) {
		sendPasswordLinkCtrl = new SendTokenToUserForm(ureq, getWindowControl(), identityToModify, false, false, true);
		listenTo(sendPasswordLinkCtrl);
		
		String title = translate("send.password.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), sendPasswordLinkCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doResetPassword(UserRequest ureq) {
		String authenticationUsername = olatAuthenticationSpi.getAuthenticationUsername(identityToModify);
		if (authenticationUsername == null) { // create new authentication for provider OLAT
			authenticationUsername = olatAuthenticationSpi.getOlatAuthusernameFromIdentity(identityToModify);
		}
		
		resetPasswordCtrl = new ChangeUserPasswordForm(ureq, getWindowControl(),
				identityToModify, authenticationUsername, false, true);
		resetPasswordCtrl.getAndRemoveFormTitle();
		listenTo(resetPasswordCtrl);
		
		String title = translate("reset.password.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), resetPasswordCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doFinishResetPassword(String newPwd) {
		if (olatAuthenticationSpi.changePassword(getIdentity(), identityToModify, newPwd)) {
			showInfo("changeuserpwd.successful");
			logAudit("user password changed successfully of " + identityToModify.getKey());
		} else {
			showError("changeuserpwd.failed");
		}
	}
	
	private void doSendRecoveryKey(UserRequest ureq) {
		sendRecoveryKeyCtrl = new SendRecoveryKeyToUserForm(ureq, getWindowControl(), identityToModify);
		listenTo(sendRecoveryKeyCtrl);
		
		String title = translate("send.recovery.key.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), sendRecoveryKeyCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
}
