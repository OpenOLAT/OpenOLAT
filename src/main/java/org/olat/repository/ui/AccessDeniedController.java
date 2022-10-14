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
package org.olat.repository.ui;

import java.util.Locale;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Aug 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AccessDeniedController extends BasicController {
	
	private VelocityContainer mainVC;
	private Link membershipRequestLink;
	private Link goToLoginLink;
	
	private RepositoryEntry entry;
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MailManager mailManager;

	protected AccessDeniedController(UserRequest ureq, WindowControl wControl, String messageI18nKey, String hintI18nKey, String[] hintArgs) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(getTranslator(), RepositoryService.class, getLocale()));
		
		mainVC = createVelocityContainer("access_denied");
		
		String message = translate(messageI18nKey);
		mainVC.contextPut("message", message);
		if (StringHelper.containsNonWhitespace(hintI18nKey)) {
			String hint = translate(hintI18nKey, hintArgs);
			mainVC.contextPut("hint", hint);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == membershipRequestLink) {
			doRequestMembership();
		} else if (source == goToLoginLink) {
			doGoToLogin(ureq);
		}
	}

	public void enableMembershipRequest(RepositoryEntry entry) {
		this.entry = entry;
		membershipRequestLink = LinkFactory.createButton("membership.request", mainVC, this);
		membershipRequestLink.setPrimary(true);
	}
	
	private void doRequestMembership() {
		boolean emailOk = true;
		for (Identity owner : repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.owner.name())) {
			boolean requestMembershipSent = sendRequestMembership(owner);
			if (!requestMembershipSent) {
				emailOk = false;
			}
		}
		
		if (emailOk) {
			showInfo("membership.request.success");
		} else {
			showError("membership.request.failed");
		}
	}
	
	private boolean sendRequestMembership(Identity owner) {
		MailContext context = new MailContextImpl("[RepositoryEntry:" + entry.getKey() + "]");
		String businessPath = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();
		
		User user = owner.getUser();
		Locale ownerLocale = I18nManager.getInstance().getLocaleOrDefault(user.getPreferences().getLanguage());
		Translator ownerTranslator = Util.createPackageTranslator(RepositoryService.class, ownerLocale);
		
		String subject = ownerTranslator.translate("membership.request.subject", entry.getDisplayname());
		String body = ownerTranslator.translate("membership.request.body", userManager.getUserDisplayName(owner),
				userManager.getUserDisplayName(getIdentity()), getIdentity().getUser().getNickName(),
				entry.getDisplayname(), businessPath);
		
		MailBundle bundle = new MailBundle();
		bundle.setContext(context);
		bundle.setFromId(getIdentity());
		bundle.setToId(owner);
		bundle.setContent(subject, body);
		
		return  mailManager.sendMessage(bundle).isSuccessful();
	}

	public void enableGoToLogin() {
		goToLoginLink = LinkFactory.createButton("go.to.login", mainVC, this);
		goToLoginLink.setIconLeftCSS("o_icon o_icon_login");
		goToLoginLink.setPrimary(true);
	}

	private void doGoToLogin(UserRequest ureq) {
		AuthHelper.doLogout(ureq);
	}

}
