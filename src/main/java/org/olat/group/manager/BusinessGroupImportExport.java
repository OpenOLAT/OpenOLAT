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
package org.olat.group.manager;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.properties.BusinessGroupPropertyManager;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("businessGroupImportExport")
public class BusinessGroupImportExport {
	
	private final OLog log = Tracing.createLoggerFor(BusinessGroupImportExport.class);

	private GroupXStream xstream = new GroupXStream();
	
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	


	public void importGroups(OLATResource resource, File fGroupExportXML) {
		if (!fGroupExportXML.exists())
			return;

		OLATGroupExport groupConfig = null;
		try {
			groupConfig = xstream.fromXML(fGroupExportXML);
		} catch (Exception ce) {
			throw new OLATRuntimeException("Error importing group config.", ce);
		}
		if (groupConfig == null) {
			throw new AssertException(
					"Invalid group export file. Root does not match.");
		}

		// get areas
		if (groupConfig.getAreas() != null && groupConfig.getAreas().getGroups() != null) {
			for (Area area : groupConfig.getAreas().getGroups()) {
				String areaName = area.name;
				String areaDesc = (area.description != null && !area.description.isEmpty()) ? area.description.get(0) : "";
				areaManager.createAndPersistBGAreaIfNotExists(areaName, areaDesc, resource);
			}
		}

		// get groups
		if (groupConfig.getGroups() != null && groupConfig.getGroups().getGroups() != null) {
			for (Group group : groupConfig.getGroups().getGroups()) {
				// create group
				String groupName = group.name;
				String groupDesc = (group.description != null && !group.description.isEmpty()) ? group.description.get(0) : "";

				// get min/max participants
				Integer groupMinParticipants = group.minParticipants;
				Integer groupMaxParticipants = group.maxParticipants;

				// waiting list configuration
				Boolean waitingList = group.waitingList;
				if (waitingList == null) {
					waitingList = Boolean.FALSE;
				}
				Boolean enableAutoCloseRanks = group.autoCloseRanks;
				if (enableAutoCloseRanks == null) {
					enableAutoCloseRanks = Boolean.FALSE;
				}
				
				String type = BusinessGroup.TYPE_LEARNINGROUP;//TODO gm
				BusinessGroup newGroup = businessGroupService.createBusinessGroup(null, groupName, groupDesc, type, groupMinParticipants, groupMaxParticipants, waitingList, enableAutoCloseRanks, resource);

				// get tools config
				CollabTools toolsConfig = group.tools;
				CollaborationTools ct = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(newGroup);
				for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
					try {
						Field field = toolsConfig.getClass().getField(CollaborationTools.TOOLS[i]);
						Boolean val = field.getBoolean(toolsConfig);
						if (val != null) {
							ct.setToolEnabled(CollaborationTools.TOOLS[i], val);
						}
					} catch (Exception e) {
						log.error("", e);
					}
				}
				if (group.calendarAccess != null) {
					Long calendarAccess = group.calendarAccess;
					ct.saveCalendarAccess(calendarAccess);
				}
				//fxdiff VCRP-8: collaboration tools folder access control
				if(group.folderAccess != null) {
				  ct.saveFolderAccess(group.folderAccess);				  
				}
				if (group.info != null) {
					ct.saveNews(group.info);
				}

				// get memberships
				List<String> memberships = group.areaRelations;
				if(memberships != null) {
					for (String membership : memberships) {
						BGArea area = areaManager.findBGArea(membership, resource);
						if (area == null) {
							throw new AssertException("Group-Area-Relationship in export, but area was not created during import.");
						}
						areaManager.addBGToBGArea(newGroup, area);
					}
				}

				// get properties
				boolean showOwners = true;
				boolean showParticipants = true;
				boolean showWaitingList = true;
				if (group.showOwners != null) {
					showOwners = group.showOwners;
				}
				if (group.showParticipants != null) {
					showParticipants = group.showParticipants;
				}
				if (group.showWaitingList != null) {
					showWaitingList = group.showWaitingList;
				}
				BusinessGroupPropertyManager bgPropertyManager = new BusinessGroupPropertyManager(newGroup);
				bgPropertyManager.updateDisplayMembers(showOwners, showParticipants, showWaitingList);
			}
		}
	}

}
