/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.manager.LTI13ToolDeploymentDAO;
import org.olat.ims.lti13.model.LTI13ToolDeploymentImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_17_2_2 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_17_2_2.class);

	private static final String VERSION = "OLAT_17.2.2";
	private static final String LTI_RESOURCE_IDS = "LTI 1.3 RESOURCE IDS";

	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13ToolDeploymentDAO toolDeploymentDao;


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

		allOk &= setResourceIds(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_17_2_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_17_2_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean setResourceIds(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(LTI_RESOURCE_IDS)) {
			try {
				log.info("Start setting legacy resource ids to LTI 1.3 deployment tool.");
				List<LTI13ToolDeploymentImpl> deployments = getToolDeploymentWithoutResourceIds();
				for (LTI13ToolDeploymentImpl deployment : deployments) {
					if(deployment.getBusinessGroup() != null) {
						deployment.setDeploymentResourceId(deployment.getBusinessGroup().getKey().toString());
					} else if(deployment.getEntry() != null) {
						if(StringHelper.containsNonWhitespace(deployment.getSubIdent())) {
							// this reproduce for backwards compatibility reasons the bug OO-6805
							deployment.setDeploymentResourceId(deployment.getEntry().getKey() + "_" + StringHelper.containsNonWhitespace(deployment.getSubIdent()));
						} else {
							deployment.setDeploymentResourceId(deployment.getEntry().getKey().toString());
						}
					}
					toolDeploymentDao.updateToolDeployment(deployment);
				}
				dbInstance.commitAndCloseSession();
				log.info("LTI 1.3 deployment tools updated: {}", deployments.size());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(LTI_RESOURCE_IDS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private List<LTI13ToolDeploymentImpl> getToolDeploymentWithoutResourceIds() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltitooldeployment as deployment")
		  .append(" left join fetch deployment.entry as v")
		  .append(" left join fetch deployment.businessGroup as businessGroup")
		  .append(" where deployment.deploymentResourceId is null");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LTI13ToolDeploymentImpl.class)
				.getResultList();
	}
}
