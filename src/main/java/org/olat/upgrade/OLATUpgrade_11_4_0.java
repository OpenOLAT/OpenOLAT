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

import org.olat.basesecurity.GroupRoles;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.commons.info.model.InfoMessageImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.properties.Property;
import org.olat.repository.RepositoryDeletionModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 16.03.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class OLATUpgrade_11_4_0 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_11.4.0";
	private static final String GROUP_INFO_MSG = "GROUP INFO MESSAGE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private	BusinessGroupService groupService;
	@Autowired
	private CollaborationToolsFactory toolsF;
	@Autowired
	private InfoMessageFrontendManager infoMessageManager;
	@Autowired
	private RepositoryDeletionModule deletionManager;

	
	public OLATUpgrade_11_4_0() {
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
		allOk &= upgradeGroupInfoMessage(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_11_4_0 successfully!");
		} else {
			log.audit("OLATUpgrade_11_4_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeGroupInfoMessage(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(GROUP_INFO_MSG)) {
			
			List<BusinessGroup> allBusinessGroups = groupService.loadAllBusinessGroups();
			for (BusinessGroup businessGroup : allBusinessGroups) {
				if(businessGroup == null) continue;

				allOk &= processInfoMessage(businessGroup);
				dbInstance.commitAndCloseSession();
			}
			
			uhd.setBooleanDataValue(GROUP_INFO_MSG, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	/**
	 * @param business group
	 * @return true if upgrade went well
	 */
	private boolean processInfoMessage(BusinessGroup businessGroup) {
		// iterate all groups and translate their singular info message to the new standard
		try {
			String businessPath = "[BusinessGroup:" + businessGroup.getKey() + "][toolmsg:0]";
			int messageCount = infoMessageManager.countInfoMessageByResource(businessGroup.getResource(),
					InfoMessageFrontendManager.businessGroupResSubPath, businessPath, null, null);
			// only upgrade if business group has not any info messages of the new kind yet
			if (1 > messageCount) {
				CollaborationTools collabTools = toolsF.getOrCreateCollaborationTools(businessGroup);
				Property property = collabTools.lookupNewsDBEntry();
				if (property != null) {
					String oldNews = property.getTextValue();//collabTools.lookupNews();			
					Identity author;
					List<Identity> members = groupService.getMembers(businessGroup, GroupRoles.owner.name(), GroupRoles.coach.name());
					if (members == null || (members != null && members.isEmpty())) {
						author = deletionManager.getAdminUserIdentity();
					} else {
						author = members.get(0);
					}
					InfoMessageImpl infoMessage = (InfoMessageImpl)infoMessageManager.createInfoMessage(businessGroup.getResource(),
							InfoMessageFrontendManager.businessGroupResSubPath, businessPath, author);
					Translator trans = Util.createPackageTranslator(CollaborationTools.class, I18nModule.getDefaultLocale());
					String title = trans.translate("news.content");
					infoMessage.setTitle(title);
					infoMessage.setMessage(oldNews);
					infoMessage.setCreationDate(property.getCreationDate());
					infoMessageManager.saveInfoMessage(infoMessage);
				} else {
					log.warn("The group " + businessGroup.getName() + " does not have an info message");
				}
			}
			return true;
		} catch (Exception e) {
			log.warn("Update InfoMessage for " + businessGroup.getName() + " failed", e);
			return false;	
		}
	}
	
	
	

}
