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
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.user.DefaultUser;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SetupModule extends AbstractSpringModule {
	
	private static final Logger log = Tracing.createLoggerFor(SetupModule.class);

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
	private OrganisationService organisationService;
	

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
		if (hasTestUsers && testUsers != null) {
			// read user editable fields configuration
			for (DefaultUser user :testUsers) {
				createUser(user);
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
	private Identity createUser(DefaultUser user) {
		Identity identity;
		identity = securityManager.findIdentityByLogin(user.getUserName());
		if (identity == null) {
			// Create new user and subject
			UserImpl newUser = new UserImpl();
			newUser.setFirstName(user.getFirstName());
			newUser.setLastName(user.getLastName());
			newUser.setEmail(user.getEmail());
			newUser.getPreferences().setLanguage(user.getLanguage());
			newUser.getPreferences().setInformSessionTimeout(true);

			if (!StringHelper.containsNonWhitespace(authenticationProviderConstant)){
				throw new OLATRuntimeException(this.getClass(), "Auth token not set! Please fix! " + authenticationProviderConstant, null);
			}

			// Now finally create that user thing on the database with all
			// credentials, person etc. in one transaction context!
			String legacyName = null;
			if(user.isAdmin() || user.isSysAdmin() || user.isGuest()) {
				legacyName = user.getUserName();
			}
			identity = securityManager.createAndPersistIdentityAndUser(legacyName, user.getUserName(), null, newUser,
					authenticationProviderConstant, BaseSecurity.DEFAULT_ISSUER,
					user.getUserName(), user.getPassword(), null);

			if (identity == null) {
				throw new OLATRuntimeException(this.getClass(), "Error, could not create  user and subject with name " + user.getUserName(), null);
			} else if (user.isGuest()) {
				organisationService.addMember(identity, OrganisationRoles.guest);
				log .info("Created anonymous user {}", user.getUserName());
			} else {
				organisationService.addMember(identity, OrganisationRoles.user);
				log .info("Created user {}", user.getUserName());
				if (user.isAdmin()) {
					organisationService.addMember(identity, OrganisationRoles.administrator);
					log .info("Created administrator user {}", user.getUserName());
				}
				if (user.isSysAdmin()) {
					organisationService.addMember(identity, OrganisationRoles.sysadmin);
					log .info("Created admin user {}", user.getUserName());
				}
				if (user.isAuthor()) {
					organisationService.addMember(identity, OrganisationRoles.author);
					log.info("Created author user {}", user.getUserName());
				}
				if (user.isUserManager()) {
					organisationService.addMember(identity, OrganisationRoles.usermanager);
					log .info("Created userManager user {}", user.getUserName());
				}
				if (user.isGroupManager()) {
					organisationService.addMember(identity, OrganisationRoles.groupmanager);
					log .info("Created groupManager user {}", user.getUserName());
				}
			}
		}
		return identity;
	}
}

