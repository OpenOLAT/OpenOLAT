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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.PersistenceException;

import org.olat.basesecurity.Policy;
import org.olat.basesecurity.PolicyImpl;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRightsRole;
import org.olat.upgrade.model.BusinessGroupUpgrade;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_10_0_3 extends OLATUpgrade {
	
	private static final int BATCH_SIZE = 50;
	private static final String TASK_BUSINESS_GROUPS = "Upgrade rights groups";
	private static final String VERSION = "OLAT_10.0.3";

	@Autowired
	private DB dbInstance;
	@Autowired
	private BGRightManager bgRightManager;
	
	public OLATUpgrade_10_0_3() {
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
		try {
			allOk &= upgradeBusinessGroups(upgradeManager, uhd);
		} catch (PersistenceException e) {
			String msg = e.getMessage();
			//old database schema, cannot update
			if(msg.contains("could not extract ResultSet")) {
				dbInstance.rollbackAndCloseSession();
				allOk &= true;
			} else {
				log.error("", e);
				allOk &= false;
			}
		}
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_10_0_3 successfully!");
		} else {
			log.audit("OLATUpgrade_10_0_3 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeBusinessGroups(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_BUSINESS_GROUPS)) {
			int counter = 0;
			List<BusinessGroupUpgrade> businessGroups;
			do {
				businessGroups = findBusinessGroups(counter, BATCH_SIZE);
				for(BusinessGroupUpgrade businessGroup:businessGroups) {
					processRightGroup(businessGroup); 
				}
				counter += businessGroups.size();
				log.audit("Rights groups processed: " + businessGroups.size() + ", total processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(businessGroups.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(TASK_BUSINESS_GROUPS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}

	private void processRightGroup(BusinessGroupUpgrade businessGroup) {
		boolean commit = false;

		List<String> tutorRights = findBGRights(businessGroup.getOwnerGroup());
		List<String> currentTutorRights = bgRightManager.findBGRights(businessGroup, BGRightsRole.tutor);
		tutorRights.removeAll(currentTutorRights);
		for(String right:tutorRights) {
			bgRightManager.addBGRight(right, businessGroup, BGRightsRole.tutor);
			commit = true;
		}
		
		List<String> participantsRights = findBGRights(businessGroup.getPartipiciantGroup());
		List<String> currentParticipantsRights = bgRightManager.findBGRights(businessGroup, BGRightsRole.participant);
		participantsRights.removeAll(currentParticipantsRights);
		for(String right:participantsRights) {
			bgRightManager.addBGRight(right, businessGroup, BGRightsRole.participant);
			commit = true;
		}
		
		if(commit) {
			dbInstance.commit();
		}
	}
	
	private List<String> findBGRights(SecurityGroup secGroup) {
		List<Policy> results = getPoliciesOfSecurityGroup(secGroup);
		// filter all business group rights permissions. group right permissions
		// start with bgr.
		List<String> rights = new ArrayList<>();
		for (Policy rightPolicy:results) {
			String right = rightPolicy.getPermission();
			if (right.indexOf("bgr.") == 0) rights.add(right);
		}
		return rights;
	}
	
	private List<BusinessGroupUpgrade> findBusinessGroups(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();	
		sb.append("select businessgroup from ").append(BusinessGroupUpgrade.class.getName()).append(" businessgroup")
		  .append(" left join fetch businessgroup.baseGroup as baseGroup")
		  .append(" left join fetch businessgroup.ownerGroup as ownerGroup")
		  .append(" left join fetch businessgroup.partipiciantGroup as partipiciantGroup")
		  .append(" left join fetch businessgroup.waitingGroup as waitingGroup")
		  .append(" left join fetch businessgroup.resource as resource")
		  .append(" order by businessgroup.key");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BusinessGroupUpgrade.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}

	private List<Policy> getPoliciesOfSecurityGroup(SecurityGroup secGroup) {
		if(secGroup == null ) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(128);
		sb.append("select poi from ").append(PolicyImpl.class.getName()).append(" as poi where poi.securityGroup.key=:secGroupKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Policy.class)
				.setParameter("secGroupKey", secGroup.getKey())
				.getResultList();
	}
}
