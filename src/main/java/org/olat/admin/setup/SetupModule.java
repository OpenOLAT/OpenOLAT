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
package org.olat.admin.setup;

import java.util.ArrayList;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.user.DefaultUser;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 
 * Initial date: 14.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SetupModule extends AbstractSpringModule {
	
	private static final OLog log = Tracing.createLoggerFor(SetupModule.class);

	@Value("${user.generateTestUsers}")
	private boolean hasTestUsers;
	@Value("${default.auth.provider.identifier}")
	private String authenticationProviderConstant;

	@Autowired @Qualifier("defaultUsers")
	private ArrayList<DefaultUser> defaultUsers;
	@Autowired @Qualifier("testUsers")
	private ArrayList<DefaultUser> testUsers;

	@Autowired
	protected DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	

	@Autowired
	public SetupModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, FrameworkStartupEventChannel.getStartupEventChannel());
	}

	@Override
	public void init() {
		//
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	/**
	 * Courses are deployed after the startup has completed.
	 * 
	 */
	@Override
	public void event(org.olat.core.gui.control.Event event) {
		setup();
	}
	
	protected void setup() {
		createDefaultUsers();
		DBFactory.getInstance().intermediateCommit();
	}
	
	private void createDefaultUsers() {
		// read user editable fields configuration
		if (defaultUsers != null) {
			for (DefaultUser user:defaultUsers) {
				createUser(user);
			}
		}
		if (hasTestUsers) {
			// read user editable fields configuration
			if (testUsers != null) {
				for (DefaultUser user :testUsers) {
					createUser(user);
				}
			}
		}
		// Cleanup, otherwhise this subjects will have problems in normal OLAT
		// operation
		dbInstance.commitAndCloseSession();
	}

	/**
	 * Method to create a user with the given configuration
	 * 
	 * @return Identity or null
	 */
	protected Identity createUser(DefaultUser user) {
		Identity identity;
		identity = securityManager.findIdentityByName(user.getUserName());
		if (identity == null) {
			// Create new user and subject
			UserImpl newUser = new UserImpl();
			newUser.setFirstName(user.getFirstName());
			newUser.setLastName(user.getLastName());
			newUser.setEmail(user.getEmail());
			
			newUser.getPreferences().setLanguage(user.getLanguage());
			newUser.getPreferences().setInformSessionTimeout(true);

			if (!StringUtils.hasText(authenticationProviderConstant)){
				throw new OLATRuntimeException(this.getClass(), "Auth token not set! Please fix! " + authenticationProviderConstant, null);
			}

			// Now finally create that user thing on the database with all
			// credentials, person etc. in one transation context!
			identity = securityManager.createAndPersistIdentityAndUser(user.getUserName(), null, newUser, authenticationProviderConstant,
					user.getUserName(), user.getPassword());
			if (identity == null) {
				throw new OLATRuntimeException(this.getClass(), "Error, could not create  user and subject with name " + user.getUserName(), null);
			} else {
				
				if (user.isGuest()) {
					SecurityGroup anonymousGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
					securityManager.addIdentityToSecurityGroup(identity, anonymousGroup);
					log .info("Created anonymous user " + user.getUserName());
				} else {
					SecurityGroup olatuserGroup = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
					
					if (user.isAdmin()) {
						SecurityGroup adminGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
						securityManager.addIdentityToSecurityGroup(identity, adminGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created admin user " + user.getUserName());
					}  else if (user.isAuthor()) {
						SecurityGroup authorGroup = securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
						securityManager.addIdentityToSecurityGroup(identity, authorGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log.info("Created author user " + user.getUserName());
					} else if (user.isUserManager()) {
						SecurityGroup usermanagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_USERMANAGERS);
						securityManager.addIdentityToSecurityGroup(identity, usermanagerGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created userManager user " + user.getUserName());
					} else if (user.isGroupManager()) {
						SecurityGroup groupmanagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
						securityManager.addIdentityToSecurityGroup(identity, groupmanagerGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created groupManager user " + user.getUserName());
					} else {
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created user " + user.getUserName());
					}
				}
			}
		}
		return identity;
	}
}

