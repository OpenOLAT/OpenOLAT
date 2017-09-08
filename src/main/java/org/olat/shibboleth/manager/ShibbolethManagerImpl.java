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
package org.olat.shibboleth.manager;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.provider.auto.AutoAccessManager;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.shibboleth.ShibbolethManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 19.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ShibbolethManagerImpl implements ShibbolethManager {

	private BaseSecurity securityManager;

	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AutoAccessManager autoAccessManager;

	public ShibbolethManagerImpl() {
		securityManager = BaseSecurityManager.getInstance();
	}

	@Override
	public Identity createUser(String username, String shibbolethUniqueID, String language, ShibbolethAttributes shibbolethAttributes) {
		if (shibbolethAttributes == null) return null;

		Identity identity = createUserAndPersist(username, shibbolethUniqueID, language, shibbolethAttributes);
		addToUsersGroup(identity);
		addToAuthorsGroup(identity, shibbolethAttributes);
		createAndBookAdvanceOrders(identity, shibbolethAttributes);

		return identity;
	}

	private Identity createUserAndPersist(String username, String shibbolethUniqueID, String language, ShibbolethAttributes shibbolethAttributes) {
		User user = userManager.createUser(null, null, null);
		user = shibbolethAttributes.syncUser(user);
		user.getPreferences().setLanguage(language);
		return securityManager.createAndPersistIdentityAndUser(username, null, user, ShibbolethDispatcher.PROVIDER_SHIB, shibbolethUniqueID);
	}

	private void addToUsersGroup(Identity identity) {
		securityManager.addIdentityToSecurityGroup(identity, getUsersSecurityGroup());
	}

	private void addToAuthorsGroup(Identity identity, ShibbolethAttributes shibbolethAttributes) {
		if (shibbolethAttributes.isAuthor() && isNotInAuthorsGroup(identity)) {
			securityManager.addIdentityToSecurityGroup(identity, getAuthorsSecurityGroup());
		}
	}

	private boolean isNotInAuthorsGroup(Identity identity) {
		return !securityManager.isIdentityInSecurityGroup(identity, getAuthorsSecurityGroup());
	}

	private SecurityGroup getUsersSecurityGroup() {
		return securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
	}

	private SecurityGroup getAuthorsSecurityGroup() {
		return securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
	}

	private void createAndBookAdvanceOrders(Identity identity, ShibbolethAttributes shibbolethAttributes) {
		if (acModule.isAutoEnabled()) {
			createAdvanceOr(identity, shibbolethAttributes);
			autoAccessManager.grantAccessToCourse(identity);
		}
	}

	private void createAdvanceOr(Identity identity, ShibbolethAttributes shibbolethAttributes) {
		ShibbolethAdvanceOrderInput input = getShibbolethAdvanceOrderInput();
		input.setIdentity(identity);
		String rawValues = shibbolethAttributes.getAcRawValues();
		input.setRawValues(rawValues);
		autoAccessManager.createAdvanceOrders(input);
	}

	@Override
	public void syncUser(Identity identity, ShibbolethAttributes shibbolethAttributes) {
		if (identity == null || shibbolethAttributes == null) {
			return;
		}

		User user = identity.getUser();
		syncAndPersistUser(user, shibbolethAttributes);
		addToAuthorsGroup(identity, shibbolethAttributes);
		createAndBookAdvanceOrders(identity, shibbolethAttributes);
	}

	private void syncAndPersistUser(User user, ShibbolethAttributes shibbolethAttributes) {
		if (shibbolethAttributes.hasDifference(user)) {
			User syncedUser = shibbolethAttributes.syncUser(user);
			userManager.updateUser(syncedUser);
		}
	}

	/**
	 * Because the static method of the CoreSpringFactory can not be mocked.
	 */
	protected ShibbolethAdvanceOrderInput getShibbolethAdvanceOrderInput() {
		return CoreSpringFactory.getImpl(ShibbolethAdvanceOrderInput.class);
	}

}
