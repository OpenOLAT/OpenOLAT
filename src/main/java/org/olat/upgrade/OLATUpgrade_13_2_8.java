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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.condition.additionalconditions.AdditionalConditionAnswerContainer;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_2_8 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_13.2.8";
	private static final String GUEST_COURSE_NODES_PASSWORDS = "GUEST COURSE NODES PASSWORDS";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private PropertyManager propertyManager;
	
	public OLATUpgrade_13_2_8() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
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
		allOk &= deleteCourseNodePassworsForGuests(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_13_2_8 successfully!");
		} else {
			log.audit("OLATUpgrade_13_2_8 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	/**
	 * Find identities without organizations but not deleted.
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean deleteCourseNodePassworsForGuests(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(GUEST_COURSE_NODES_PASSWORDS)) {
			SearchIdentityParams params = new SearchIdentityParams();
			params.setRoles(new OrganisationRoles[] { OrganisationRoles.guest });
			List<Identity> guests = securityManager.getIdentitiesByPowerSearch(params, 0, -1);
			for(Identity guest:guests) {
				List<Property> properties = propertyManager.listProperties(guest, null, AdditionalConditionAnswerContainer.RESOURCE_NAME,
						null, null, AdditionalConditionAnswerContainer.RESOURCE_NAME);
				for(Property property:properties) {
					propertyManager.deleteProperty(property);
				}
				dbInstance.commitAndCloseSession();
			}
			uhd.setBooleanDataValue(GUEST_COURSE_NODES_PASSWORDS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
}
