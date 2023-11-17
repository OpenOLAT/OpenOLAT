/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolDeploymentType;
import org.olat.ims.lti13.manager.LTI13ContextDAO;
import org.olat.ims.lti13.manager.LTI13ToolDeploymentDAO;
import org.olat.ims.lti13.model.LTI13ContextImpl;
import org.olat.ims.lti13.model.LTI13ToolDeploymentImpl;
import org.olat.modules.jupyterhub.JupyterDeployment;
import org.olat.modules.jupyterhub.model.JupyterDeploymentImpl;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.model.ZoomConfigImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.upgrade.model.UpgradeLTI13ToolDeployment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_18_1_3 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_18_1_3.class);

	private static final String VERSION = "OLAT_18.1.3";

	private static final String MIGRATE_LTI_TOOL_DEPLOYMENT = "MIGRATE LTI TOOL DEPLOYMENT";
	private static final String MIGRATE_LTI_JUPYTER_DEPLOYMENT = "MIGRATE LTI JUPYTER DEPLOYMENT";
	private static final String MIGRATE_LTI_ZOOM_DEPLOYMENT = "MIGRATE LTI ZOOM DEPLOYMENT";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13ContextDAO ltiContextDao;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private LTI13ToolDeploymentDAO ltiToolDeploymentDao;

	public OLATUpgrade_18_1_3() {
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
		allOk &= migrateToolDeployments(upgradeManager, uhd);
		// Jupyter and Zoom migrations need the tools migration
		allOk &= migrateJupyterDeployments(upgradeManager, uhd);
		allOk &= migrateZoomConfigs(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_18_1_3 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_18_1_3 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean migrateZoomConfigs(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_LTI_ZOOM_DEPLOYMENT)) {
			try {
				log.info("Start migration of LTI Zoom configurations");
				
				List<ZoomConfig> zoomConfigs = getZoomConfigWithoutContext();
				for(ZoomConfig zoomConfig:zoomConfigs) {
					migrateZoomConfig(zoomConfig);
				}
				dbInstance.commitAndCloseSession();
				
				log.info("LTI Zoom configurations migration finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_LTI_ZOOM_DEPLOYMENT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private boolean migrateZoomConfig(ZoomConfig zoomConfig) {
		LTI13ToolDeployment deployment = ((ZoomConfigImpl)zoomConfig).getLtiToolDeployment();
		if(deployment != null && deployment.getDeploymentType() == LTI13ToolDeploymentType.SINGLE_CONTEXT) {
			List<LTI13Context> ltiContexts = ltiContextDao.loadContextsByDeploymentKey(deployment.getKey());
			if(ltiContexts.size() == 1) {
				LTI13Context ltiContext = ltiContexts.get(0);
				updateZoomConfigWithContext(zoomConfig.getKey(), ltiContext.getKey());
			}
		}
		return true;
	}
	
	private void updateZoomConfigWithContext(Long zoomConfigKey, Long contextKey) {
		String query = "update zoomconfig as c set c.ltiContext.key=:contextKey where c.key=:zoomConfigKey";
		dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("contextKey", contextKey)
				.setParameter("zoomConfigKey", zoomConfigKey)
				.executeUpdate();
	}
	
	private List<ZoomConfig> getZoomConfigWithoutContext() {
		String query = """
				select c from zoomconfig c
				inner join fetch c.ltiToolDeployment as td
				where c.ltiContext.key is null""";
		return dbInstance.getCurrentEntityManager().createQuery(query, ZoomConfig.class)
				.getResultList();
	}

	private boolean migrateJupyterDeployments(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_LTI_JUPYTER_DEPLOYMENT)) {
			try {
				log.info("Start migration of LTI Jupyter deployments");
				
				List<JupyterDeployment> jupyterDeployments = getJupyterDeploymentsWithoutContext();
				for(JupyterDeployment jupyterDeployment:jupyterDeployments) {
					migrateJupyterDeployment(jupyterDeployment);
				}
				dbInstance.commitAndCloseSession();
				
				log.info("LTI Jupyter deployments migration finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_LTI_JUPYTER_DEPLOYMENT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateJupyterDeployment(JupyterDeployment jupyterDeployment) {
		LTI13ToolDeployment deployment = ((JupyterDeploymentImpl)jupyterDeployment).getLtiToolDeployment();
		if(deployment != null && deployment.getDeploymentType() == LTI13ToolDeploymentType.SINGLE_CONTEXT) {
			List<LTI13Context> ltiContexts = ltiContextDao.loadContextsByDeploymentKey(deployment.getKey());
			if(ltiContexts.size() == 1) {
				LTI13Context ltiContext = ltiContexts.get(0);
				updateJupyterDeploymentWithContext(jupyterDeployment.getKey(), ltiContext.getKey());
			}
		}
	}
	
	private void updateJupyterDeploymentWithContext(Long jupyterDeploymentKey, Long contextKey) {
		String query = "update jupyterdeployment as jdeployment set jdeployment.ltiContext.key=:contextKey where jdeployment.key=:jupyterDeploymentKey";
		dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("contextKey", contextKey)
				.setParameter("jupyterDeploymentKey", jupyterDeploymentKey)
				.executeUpdate();
	}
	
	private List<JupyterDeployment> getJupyterDeploymentsWithoutContext() {
		String query = """
				select d from jupyterdeployment d
				inner join fetch d.ltiToolDeployment as td
				where d.ltiContext.key is null""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, JupyterDeployment.class)
				.getResultList();
	}
	
	private boolean migrateToolDeployments(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATE_LTI_TOOL_DEPLOYMENT)) {
			try {
				log.info("Start migration of LTI tool deployments");
				List<UpgradeLTI13ToolDeployment> deploymentsToUpgrade = getToolsDeploymentToUpgrade();
				for(UpgradeLTI13ToolDeployment deploymentToUpgrade:deploymentsToUpgrade) {
					allOk &= migrateToolDeployment(deploymentToUpgrade);
				}
				log.info("LTI tool deployments migration finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_LTI_TOOL_DEPLOYMENT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private boolean migrateToolDeployment(UpgradeLTI13ToolDeployment deploymentToUpgrade) {
		final Long deploymentKey = deploymentToUpgrade.getKey();
		LTI13ToolDeployment deployment = ltiToolDeploymentDao.loadDeploymentByKey(deploymentKey);
		if(deployment.getDeploymentType() == LTI13ToolDeploymentType.MULTIPLE_CONTEXTS) {
			return true;
		}
		
		LTI13Context ltiContext = null;
		List<LTI13Context> contexts = ltiContextDao.loadContextsByDeploymentKey(deploymentKey);
		if(contexts.isEmpty()) {
			RepositoryEntry entry = null;
			BusinessGroup businessGroup = null;
			if(deploymentToUpgrade.getEntryKey() != null) {
				entry = repositoryEntryDao.loadByKey(deploymentToUpgrade.getEntryKey());
			}
			if(deploymentToUpgrade.getBusinessGroupKey() != null) {
				businessGroup = businessGroupDao.load(deploymentToUpgrade.getBusinessGroupKey());
			}
			ltiContext = createContext(deploymentToUpgrade, deployment, entry, deploymentToUpgrade.getSubIdent(), businessGroup);
			((LTI13ToolDeploymentImpl)deployment).setDeploymentType(LTI13ToolDeploymentType.SINGLE_CONTEXT);
			deployment = ltiToolDeploymentDao.updateToolDeployment(deployment);
			dbInstance.commitAndCloseSession();
		} else if(contexts.size() == 1) {
			ltiContext = contexts.get(0);
		}
		
		List<LTI13ContentItem> contentItems = getContentItemWithoutContext(deploymentKey);
		if(ltiContext != null && !contentItems.isEmpty()) {
			for(LTI13ContentItem contentItem:contentItems) {
				updateContentItemWithContext(contentItem.getKey(), ltiContext.getKey());
			}
			dbInstance.commitAndCloseSession();
		}
		return true;
	}
	
	public LTI13Context createContext(UpgradeLTI13ToolDeployment deploymentToUpgrade, LTI13ToolDeployment deployment,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		LTI13ContextImpl context = new LTI13ContextImpl();
		context.setCreationDate(new Date());
		context.setLastModified(context.getCreationDate());
		context.setContextId(deploymentToUpgrade.getContextId());
		context.setTargetUrl(deploymentToUpgrade.getTargetUrl());
		context.setResourceId(deploymentToUpgrade.getDeploymentResourceId());
		context.setDeployment(deployment);
		context.setEntry(entry);
		context.setSubIdent(subIdent);
		context.setBusinessGroup(businessGroup);
		//
		context.setSendUserAttributesList(deploymentToUpgrade.getSendUserAttributesList());
		context.setSendCustomAttributes(deploymentToUpgrade.getSendCustomAttributes());
		//
		context.setAuthorRoles(deploymentToUpgrade.getAuthorRoles());
		context.setCoachRoles(deploymentToUpgrade.getCoachRoles());
		context.setParticipantRoles(deploymentToUpgrade.getParticipantRoles());
		context.setAssessable(deploymentToUpgrade.isAssessable());
		context.setNameAndRolesProvisioningServicesEnabled(deploymentToUpgrade.isNameAndRolesProvisioningServicesEnabled());
		//
		context.setDisplay(deploymentToUpgrade.getDisplay());
		context.setDisplayHeight(deploymentToUpgrade.getDisplayHeight());
		context.setDisplayWidth(deploymentToUpgrade.getDisplayWidth());
		context.setSkipLaunchPage(deploymentToUpgrade.isSkipLaunchPage());
		
		dbInstance.getCurrentEntityManager().persist(context);
		return context;
	}
	
	private void updateContentItemWithContext(Long contentItemKey, Long contextKey) {
		String query = "update lticontentitem as item set item.context.key=:contextKey where item.key=:contentItemKey";
		dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("contextKey", contextKey)
				.setParameter("contentItemKey", contentItemKey)
				.executeUpdate();
	}
	
	private List<LTI13ContentItem> getContentItemWithoutContext(Long deploymentKey) {
		String query = """
				select item from lticontentitem as item
				inner join fetch item.deployment as deployment
				where item.context.key is null and deployment.key=:deploymentKey""";
		return dbInstance.getCurrentEntityManager().createQuery(query, LTI13ContentItem.class)
				.setParameter("deploymentKey", deploymentKey)
				.getResultList();
	}
	
	private List<UpgradeLTI13ToolDeployment> getToolsDeploymentToUpgrade() {
		String query = "select deployment from upgradetooldeployment as deployment";
		return dbInstance.getCurrentEntityManager().createQuery(query, UpgradeLTI13ToolDeployment.class)
				.getResultList();
	}
}
