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
package org.olat.upgrade;

import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertiesConfigImpl;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;
import org.olat.user.propertyhandlers.ui.UsrPropCfgManager;
import org.olat.user.propertyhandlers.ui.UsrPropCfgObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_2_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_2_0.class);

	private static final String VERSION = "OLAT_15.2.0";
	private static final String MIGRATE_USER_PROPERTY = "MIGRATE USER PROPERTY";
	private static final String MIGRATE_USER_NAME = "MIGRATE USER NAME";

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserPropertiesConfigImpl userPropertiesConfig;
	@Autowired
	private UsrPropCfgManager userPropertiesCongfigManager;

	public OLATUpgrade_15_2_0() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= migrateUserProperty(upgradeManager, uhd);
		allOk &= migrateUsernames(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_2_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_2_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private static final int BATCH_SIZE = 1000;
	

	private boolean migrateUsernames(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_USER_NAME)) {
			
			int counter = 0;
			List<Identity> identities;
			do {
				identities = getIdentity(counter, BATCH_SIZE);
				migrateUsernames(identities);
				counter += identities.size();
				log.info(Tracing.M_AUDIT, "Migrate user names: {}, total processed ({})", identities.size(), counter);
				dbInstance.commitAndCloseSession();
			} while(identities.size() == BATCH_SIZE);
	
			uhd.setBooleanDataValue(MIGRATE_USER_NAME, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateUsernames(List<Identity> identities) {
		int count = 0;
		for(Identity identity:identities) {
			String name = identity.getName();
			if(StringHelper.containsNonWhitespace(identity.getUser().getProperty(UserConstants.NICKNAME, null))) {
				continue;
			}
			identity.getUser().setProperty(UserConstants.NICKNAME, name);
			userManager.updateUser(identity, identity.getUser());
			
			if(count++ % 25 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private List<Identity> getIdentity(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" order by ident.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
		
	}
	
	private boolean migrateUserProperty(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_USER_PROPERTY)) {	
			UserPropertyHandler nickNameHandler = (UserPropertyHandler)CoreSpringFactory.getBean("userPropertyUserNickName");
			UsrPropCfgObject cfgObject = userPropertiesCongfigManager.getUserPropertiesConfigObject();
			
			boolean changed = false;
			for (Entry<String, UserPropertyUsageContext> ctxEntry:cfgObject.getUsageContexts().entrySet()) {
				UserPropertyUsageContext defaultContext = userPropertiesConfig
						.getDefaultUserPropertyUsageContexts().get(ctxEntry.getKey());
				if(defaultContext == null) {
					continue;
				}

				List<UserPropertyHandler> defaultHandlers = defaultContext.getPropertyHandlers();
				if(defaultHandlers.contains(nickNameHandler)) {
					UserPropertyUsageContext context = ctxEntry.getValue();
					boolean contains = context.getPropertyHandlers().contains(nickNameHandler);
					boolean admin = context.isForAdministrativeUserOnly(nickNameHandler);
					if(!contains && !admin) {
						context.addPropertyHandler(0, nickNameHandler);
						context.setAsAdminstrativeUserOnly(nickNameHandler, true);
						if(defaultContext.isMandatoryUserProperty(nickNameHandler)) {
							context.setAsMandatoryUserProperty(nickNameHandler, true);
						}
						changed = true;
					}
				}
			}
			
			if(changed) {
				userPropertiesCongfigManager.saveUserPropertiesConfig();
			}
			
			uhd.setBooleanDataValue(MIGRATE_USER_PROPERTY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
}
