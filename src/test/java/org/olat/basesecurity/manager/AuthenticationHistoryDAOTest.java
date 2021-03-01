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
package org.olat.basesecurity.manager;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationHistory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.Encoder;
import org.olat.login.LoginModule;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthenticationHistoryDAOTest extends OlatTestCase {
	
	private int historySetting;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private AuthenticationHistoryDAO authenticationHistoryDao;
	
	@Before
	public void setUp() {
		historySetting = loginModule.getPasswordHistory();
	}
	
	@After
	public void unsetUp() {
		loginModule.setPasswordHistory(historySetting);
	}

	@Test
	public void createAuthenticationHistory() {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("authdao-1-");
		dbInstance.commitAndCloseSession();
		
		Authentication authentication = securityManager.findAuthentication(ident, BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER);
		authenticationHistoryDao.createHistory(authentication, ident);
		dbInstance.commit();
	}
	
	@Test
	public void loadHistory() {
		
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("authdao-1-");
		Authentication auth = securityManager
				.createAndPersistAuthentication(ident, BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER,
						ident.getName(), "secret", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);
		
		//check if the new token was saved
		List<AuthenticationHistory> history = authenticationHistoryDao.loadHistory(ident,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), 0, 10);
		Assert.assertNotNull(history);
		Assert.assertEquals(1, history.size());
		
	}
	
	@Test
	public void updateCredential() {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("authdao-1-");
		Authentication auth = securityManager
				.createAndPersistAuthentication(ident, BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER,
						ident.getName(), "secret", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);
		
		//check if the new token was saved
		int historyLength = authenticationHistoryDao.historyLength(ident,
				BaseSecurityModule.getDefaultAuthProviderIdentifier());
		Assert.assertEquals(1, historyLength);
	}
	
	@Test
	public void deleteAuthenticationHistory_identity() {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("authdao-3-");
		Authentication auth = securityManager
				.createAndPersistAuthentication(ident, BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER,
						ident.getName(), "secret", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);
		
		//check if the new token was saved
		int historyLength = authenticationHistoryDao.deleteAuthenticationHistory(ident);
		Assert.assertEquals(1, historyLength);
		dbInstance.commit();
	}

}
