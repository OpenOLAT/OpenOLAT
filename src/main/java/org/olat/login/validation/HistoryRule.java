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
package org.olat.login.validation;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.ui.LDAPAuthenticationController;

/**
 * 
 * Initial date: 21 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class HistoryRule extends DescriptionRule {
	
	private int historyLength;

	HistoryRule(ValidationDescription description, int historyLength) {
		super(description);
		this.historyLength = historyLength;
	}

	@Override
	public boolean validate(String value, Identity identity) {
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		
		boolean ok = true;
		// Is this the right place to check if the user is logged in with LDAP?
		// What happens if someone changes the password of someone else?
		// Further, the description is shown even if the user is logged in with LDAP,
		// although this rule is ignored in that case.
		if (notAutenticatedWithLDAP(identity)) {
			ok = securityManager.checkCredentialHistory(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(),
					value, historyLength);
		}
		return ok;
	}

	private boolean notAutenticatedWithLDAP(Identity identity) {
		LDAPLoginModule ldapLoginModule = CoreSpringFactory.getImpl(LDAPLoginModule.class);
		AuthenticationDAO authenticationDao = CoreSpringFactory.getImpl(AuthenticationDAO.class);
		return !ldapLoginModule.isLDAPEnabled()
				|| !authenticationDao.hasAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP);
	}

	@Override
	public boolean isIdentityRule() {
		return true;
	}

}
