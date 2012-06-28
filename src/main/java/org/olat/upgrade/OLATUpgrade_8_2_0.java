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

import java.util.Collections;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupService;
import org.olat.group.context.BGContext2Resource;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * upgrade code for OLAT 7.1.0 -> OLAT 7.1.1
 * - fixing invalid structures being built by synchronisation, see OLAT-6316 and OLAT-6306
 * - merges all yet found data to last valid node 
 * 
 * <P>
 * Initial Date: 24.03.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_8_2_0 extends OLATUpgrade {

	private static final String TASK_CONTEXTS = "Upgrade contexts";
	private static final int REPO_ENTRIES_BATCH_SIZE = 20;
	private static final String VERSION = "OLAT_8.2.0";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;

	public OLATUpgrade_8_2_0() {
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
		} else {
			if (uhd.isInstallationComplete()) {
				return false;
			}
		}
		
		boolean allOk = upgradeAreas(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_8_2_0 successfully!");
		} else {
			log.audit("OLATUpgrade_8_2_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeAreas(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_CONTEXTS)) {

			int counter = 0;
			List<BusinessGroup> groups;
			do {
				groups = findBusinessGroups(counter, REPO_ENTRIES_BATCH_SIZE);
				for(BusinessGroup group:groups) {
					processBusinessGroup(group);
				}
				counter += groups.size();
				log.audit("Processed context: " + groups.size());
			} while(groups.size() == REPO_ENTRIES_BATCH_SIZE);
			
			uhd.setBooleanDataValue(TASK_CONTEXTS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
			return false;
		}
		return false;
	}
	
	private void processBusinessGroup(BusinessGroup group) {
		List<OLATResource> resources = findOLATResourcesForBusinessGroup(group);
		List<OLATResource> currentList = businessGroupService.findResources(Collections.singletonList(group), 0, -1);
		
		int count = 0;
		for	(OLATResource resource:resources) {
			if(!currentList.contains(resource)) {
				businessGroupService.addResourceTo(group, resource);
				count++;
			}
		}
		dbInstance.commitAndCloseSession();
		System.out.println("Processed: " + group.getName() + " add " + count + " resources");
	}
	
	private List<BusinessGroup> findBusinessGroups(int firstResult, int maxResults) {
		StringBuilder q = new StringBuilder();
		q.append("select bg from ").append(BusinessGroupImpl.class.getName()).append(" bg ")
		 .append(" left join fetch bg.ownerGroup onwerGroup")
		 .append(" left join fetch bg.partipiciantGroup participantGroup")
		 .append(" left join fetch bg.waitingGroup waitingGroup")
		 .append(" order by bg.key");

		List<BusinessGroup> resources = dbInstance.getCurrentEntityManager().createQuery(q.toString(), BusinessGroup.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
		return resources;
	}
	
	private List<OLATResource> findOLATResourcesForBusinessGroup(BusinessGroup group) {
		StringBuilder q = new StringBuilder();
		q.append("select bgcr.resource from ").append(BGContext2Resource.class.getName()).append(" as bgcr where bgcr.groupContext.key=:contextKey");

		List<OLATResource> resources = dbInstance.getCurrentEntityManager().createQuery(q.toString(), OLATResource.class)
				.setParameter("contextKey", ((BusinessGroupImpl)group).getGroupContextKey())
				.getResultList();
		return resources;
	}
}
