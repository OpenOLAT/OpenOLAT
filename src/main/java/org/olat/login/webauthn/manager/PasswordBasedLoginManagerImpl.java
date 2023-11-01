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
package org.olat.login.webauthn.manager;

import java.util.List;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.id.Identity;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.PasswordBasedLoginManager;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.performx.PerformXAuthManager;
import org.olat.login.tocco.ToccoAuthManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PasswordBasedLoginManagerImpl implements PasswordBasedLoginManager {
	
	@Autowired
	private LDAPLoginModule ldapModule;
	@Autowired
	private LDAPLoginManager ldapLoginManager;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private PerformXAuthManager performXAuthManager;
	@Autowired
	private ToccoAuthManager toccoAuthManager;
	@Autowired
	private OLATAuthManager olatAuthManager;
	
	@Override
	public Identity authenticate(String login, String pwd, AuthenticationStatus status) {
		List<String> providers = authenticationDao.getAuthenticationsProvidersByAuthusername(login);
		Identity id = null;
		status.setStatus(AuthHelper.LOGIN_NOTAVAILABLE);
		if(ldapModule.isLDAPEnabled() && (ldapModule.isCreateUsersOnLogin()
				|| providers.contains(LDAPAuthenticationController.PROVIDER_LDAP))) {
			id = ldapLoginManager.authenticate(login, pwd, status);
		}
		if(status.getStatus() != AuthHelper.LOGIN_OK && performXAuthManager.isEnabled()) {
			id = performXAuthManager.authenticate(login, pwd, status);
		}
		if(status.getStatus() != AuthHelper.LOGIN_OK && toccoAuthManager.isEnabled()) {
			id = toccoAuthManager.authenticate(login, pwd, status);
		}
		if(status.getStatus() != AuthHelper.LOGIN_OK
				&& providers.contains("OLAT")) {
			id = olatAuthManager.authenticate(login, pwd, status);
		}
		return id;
	}

}
