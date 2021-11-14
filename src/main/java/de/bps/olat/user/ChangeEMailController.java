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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.user;

import java.util.Calendar;
import java.util.Date;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.Util;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.olat.user.ProfileAndHomePageEditController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This controller do change the email of a user after he has clicked the appropriate activation-link.
 * 
 * <P>
 * Initial Date:  27.04.2009 <br>
 * @author bja
 */
public class ChangeEMailController extends DefaultController {

	protected static final String CHANGE_EMAIL_ENTRY = "change.email.login";
	
	public static final int TIME_OUT = 30;
	
	protected Translator pT;
	protected String emKey;
	protected TemporaryKey tempKey;
	protected UserRequest userRequest;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	protected RegistrationManager rm;
	@Autowired
	private BaseSecurity securityManager;

	/**
	 * executed after click the link in email
	 * @param ureq
	 * @param wControl
	 */
	public ChangeEMailController(UserRequest ureq, WindowControl wControl) {
		super(wControl);
		this.userRequest = ureq;
		pT = Util.createPackageTranslator(ProfileAndHomePageEditController.class, userRequest.getLocale());
		pT = userManager.getPropertyHandlerTranslator(pT);
		emKey = userRequest.getHttpReq().getParameter("key");
		if ((emKey == null) && (userRequest.getUserSession().getEntry(CHANGE_EMAIL_ENTRY) != null)) {
			emKey = userRequest.getIdentity().getUser().getProperty("emchangeKey", null);
		}
		if (emKey != null) {
			// key exist
			// we check if given key is a valid temporary key
			tempKey = rm.loadTemporaryKeyByRegistrationKey(emKey);
		}
		if (emKey != null) {
			// if key is not valid we redirect to first page
			if (tempKey == null) {
				// registration key not available
				userRequest.getUserSession().putEntryInNonClearedStore("error.change.email", pT.translate("error.change.email"));
				DispatcherModule.redirectToDefaultDispatcher(userRequest.getHttpResp());
				return;
			} else {
				if (!isLinkTimeUp()) {
					try {
						if ((userRequest.getUserSession().getEntry(CHANGE_EMAIL_ENTRY) == null) 
								|| (!userRequest.getUserSession().getEntry(CHANGE_EMAIL_ENTRY).equals(CHANGE_EMAIL_ENTRY))) {
							userRequest.getUserSession().putEntryInNonClearedStore(CHANGE_EMAIL_ENTRY, CHANGE_EMAIL_ENTRY);
							DispatcherModule.redirectToDefaultDispatcher(userRequest.getHttpResp());
							return;
						} else {
							if (userRequest.getIdentity() == null) {
								DispatcherModule.redirectToDefaultDispatcher(userRequest.getHttpResp());
								return;
							}
						}
					} catch (ClassCastException e) {
						DispatcherModule.redirectToDefaultDispatcher(userRequest.getHttpResp());
						return;
					}
				} else {
					// link time is up
					userRequest.getUserSession().putEntryInNonClearedStore("error.change.email.time", pT.translate("error.change.email.time"));
					Long identityKey = tempKey.getIdentityKey();
					Identity ident = securityManager.loadIdentityByKey(identityKey);
					if (ident != null) {
						// remove keys
						ident.getUser().setProperty("emchangeKey", null);
					}
					// delete registration key
					rm.deleteTemporaryKeyWithId(tempKey.getRegistrationKey());
					DispatcherModule.redirectToDefaultDispatcher(userRequest.getHttpResp());
					return;
				}
			}
		}
	}

	/**
	 * check if the link time up
	 * @return
	 */
	public boolean isLinkTimeUp() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_WEEK, TIME_OUT * -1);
		
		if (tempKey == null) {
			// the database entry was deleted
			return true;
		}
		
		if (!tempKey.getCreationDate().after(cal.getTime())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * delete registration key, 'change.email.login' entry and set the userproperty emchangeKey to null
	 */
	public void deleteRegistrationKey() {
		User user = userRequest.getIdentity().getUser();
		// remove keys
		user.setProperty("emchangeKey", null);
		userRequest.getUserSession().removeEntryFromNonClearedStore(CHANGE_EMAIL_ENTRY);
		userRequest.getUserSession().removeEntryFromNonClearedStore("error.change.email.time");
		// delete registration key
		if (tempKey != null) rm.deleteTemporaryKeyWithId(tempKey.getRegistrationKey());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}
}
