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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.04.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_17_2_9 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_17_2_9.class);

	private static final String VERSION = "OLAT_17.2.9";
	private static final String MIGRATE_LTI_13_DEPLOYMENTS = "MIGRATE LTI 13 DEPLOYMENTS";

	@Autowired
	private DB dbInstance;
	
	public OLATUpgrade_17_2_9() {
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
		
		allOk &= migrateLTI13ToolDeployment(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_17_2_9 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_17_2_9 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	public boolean migrateLTI13ToolDeployment(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_LTI_13_DEPLOYMENTS)) {
			try {
				log.info("Migrate LTI 1.3 deployment context ids.");
				
				int counter = 0;
				List<LTI13ToolDeployment> deployments = getToolDeployment();
				for(LTI13ToolDeployment deployment:deployments) {
					updateToolDeployment(deployment);
					if(++counter % 25 == 0) {
						dbInstance.commitAndCloseSession();
					} else {
						dbInstance.commit();
					}
				}
				dbInstance.commitAndCloseSession();
				log.info("End migration LTI 1.3 deployment context ids: {}", deployments.size());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_LTI_13_DEPLOYMENTS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void updateToolDeployment(LTI13ToolDeployment deployment) {
		if(StringHelper.containsNonWhitespace(deployment.getContextId())) {
			return;
		}
		
		String contextId = null;
		if(deployment.getEntry() != null) {
			contextId = deployment.getEntry().getKey().toString();
		} else if(deployment.getBusinessGroup() != null) {
			contextId = deployment.getBusinessGroup().getKey().toString();
		} else {
			return;
		}

		QueryBuilder sb = new QueryBuilder();
		sb.append("update ltitooldeployment deployment set deployment.contextId=:contextId")
		  .append(" where deployment.key=:deploymentKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("contextId", contextId)
				.setParameter("deploymentKey", deployment.getKey())
				.executeUpdate();
	}
	
	private List<LTI13ToolDeployment> getToolDeployment() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltitooldeployment as deployment")
		  .append(" inner join fetch deployment.tool tool")
		  .append(" left join fetch deployment.entry re")
		  .append(" left join fetch deployment.businessGroup grp")
		  .append(" where deployment.contextId is null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LTI13ToolDeployment.class)
				.getResultList();
	}
}
