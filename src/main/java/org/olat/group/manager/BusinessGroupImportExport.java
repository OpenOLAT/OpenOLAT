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
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.BGAreaReference;
import org.olat.group.model.BusinessGroupEnvironment;
import org.olat.group.model.BusinessGroupReference;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupImportExport {
	
	private static final Logger log = Tracing.createLoggerFor(BusinessGroupImportExport.class);

	private final GroupXStream xstream = new GroupXStream();

	private final DB dbInstance;
	private final BGAreaManager areaManager;
	private final BusinessGroupService businessGroupService;
	private final BusinessGroupModule groupModule;
	
	protected BusinessGroupImportExport(DB dbInstance, BGAreaManager areaManager,
			BusinessGroupService businessGroupService, BusinessGroupModule groupModule) {
		this.dbInstance = dbInstance;
		this.areaManager = areaManager;
		this.businessGroupService = businessGroupService;
		this.groupModule = groupModule;
	}
	
	public void exportGroups(List<BusinessGroup> groups, List<BGArea> areas, File fExportFile, boolean runtimeDatas) {
		if (groups == null || groups.isEmpty()) {
			return; // nothing to do... says Florian.
		}

		OLATGroupExport root = new OLATGroupExport();
		// export areas
		root.setAreas(new AreaCollection());
		root.getAreas().setGroups(new ArrayList<Area>());
		for (BGArea area : areas) {
			Area newArea = new Area();
			newArea.key = area.getKey();
			newArea.name = area.getName();
			newArea.description = Collections.singletonList(area.getDescription());
			root.getAreas().getGroups().add(newArea);
		}

		// export groups
		root.setGroups(new GroupCollection());
		root.getGroups().setGroups(new ArrayList<Group>());
		for (BusinessGroup group : groups) {
			String groupName = null;
			Group newGroup = exportGroup(fExportFile, group, groupName, runtimeDatas);
			root.getGroups().getGroups().add(newGroup);
		}
		saveGroupConfiguration(fExportFile, root);
	}
	
	private Group exportGroup(File fExportFile, BusinessGroup group, String groupName, boolean runtimeDatas) {
		Group newGroup = new Group();
		newGroup.key = group.getKey();
		newGroup.name = StringHelper.containsNonWhitespace(groupName) ? groupName : group.getName();
		if (group.getMinParticipants() != null) {
			newGroup.minParticipants = group.getMinParticipants();
		}
		if (group.getMaxParticipants() != null) {
			newGroup.maxParticipants = group.getMaxParticipants();
		}
		if (group.getWaitingListEnabled() != null) {
			newGroup.waitingList = group.getWaitingListEnabled();
		}
		if (group.getAutoCloseRanksEnabled() != null) {
			newGroup.autoCloseRanks = group.getAutoCloseRanksEnabled();
		}
		if(StringHelper.containsNonWhitespace(group.getDescription())) {
			newGroup.description = Collections.singletonList(group.getDescription());
		}
		// collab tools

		String[] availableTools = CollaborationToolsFactory.getInstance().getAvailableTools().clone();
		CollabTools toolsConfig = new CollabTools();
		CollaborationTools ct = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
		for (int i = 0; i < availableTools.length; i++) {
			try {
				Field field = toolsConfig.getClass().getField(availableTools[i]);
				field.setBoolean(toolsConfig, ct.isToolEnabled(availableTools[i]));
			} catch(NoSuchFieldException e) {
				//no field to fill (hasOpenMeetings is not set for backwards compatibility)
			} catch (Exception e) {
				log.error("", e);
			}
		}
		newGroup.tools = toolsConfig;

		Long calendarAccess = ct.lookupCalendarAccess();
		if (calendarAccess != null) {
			newGroup.calendarAccess = calendarAccess;
		}
		//fxdiff VCRP-8: collaboration tools folder access control
		Long folderAccess = ct.lookupFolderAccess();
		if(folderAccess != null) {
			newGroup.folderAccess = folderAccess;
		}
		String info = ct.lookupNews();
		if (info != null && !info.trim().equals("")) {
			newGroup.info = info.trim();
		}

		log.debug("fExportFile.getParent()=" + fExportFile.getParent());
		if(runtimeDatas) {
			ct.archive(fExportFile.getParent());
		}
		// export membership
		List<BGArea> bgAreas = areaManager.findBGAreasOfBusinessGroup(group);
		newGroup.areaRelations = new ArrayList<String>();
		for (BGArea areaRelation : bgAreas) {
			newGroup.areaRelations.add(areaRelation.getName());
		}
		// export properties
		boolean showOwners = group.isOwnersVisibleIntern();
		boolean showParticipants = group.isParticipantsVisibleIntern();
		boolean showWaitingList = group.isWaitingListVisibleIntern();

		newGroup.showOwners = showOwners;
		newGroup.showParticipants = showParticipants;
		newGroup.showWaitingList = showWaitingList;
		return newGroup;
	}
	
	private void saveGroupConfiguration(File fExportFile, OLATGroupExport root) {
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(fExportFile);
			xstream.toXML(root, fOut);
		} catch (IOException ioe) {
			throw new OLATRuntimeException(
					"Error writing group configuration during group export.",
					ioe);
		} catch (Exception cfe) {
			throw new OLATRuntimeException(
					"Error writing group configuration during group export.",
					cfe);
		} finally {
			FileUtils.closeSafely(fOut);
		}
	}

	public BusinessGroupEnvironment importGroups(RepositoryEntry re, File fGroupExportXML) {
		if (!fGroupExportXML.exists())
			return new BusinessGroupEnvironment();

		//start with a new connection
		dbInstance.commitAndCloseSession();
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
		
		BusinessGroupEnvironment env = new BusinessGroupEnvironment();
		Set<BGArea> areaSet = new HashSet<>();
		// get areas
		
		int dbCount = 0;
		if (groupConfig.getAreas() != null && groupConfig.getAreas().getGroups() != null) {
			for (Area area : groupConfig.getAreas().getGroups()) {
				String areaName = area.name;
				String areaDesc = (area.description != null && !area.description.isEmpty()) ? area.description.get(0) : "";
				BGArea newArea = areaManager.createAndPersistBGArea(areaName, areaDesc, re.getOlatResource());
				if(areaSet.add(newArea)) {
					env.getAreas().add(new BGAreaReference(newArea, area.key, area.name));
				}
				
				if(dbCount++ % 25 == 0) {
					dbInstance.commitAndCloseSession();
				}
			}
		}

		// get groups
		if (groupConfig.getGroups() != null && groupConfig.getGroups().getGroups() != null) {
			for (Group group : groupConfig.getGroups().getGroups()) {
				// create group
				String groupName = group.name;
				String groupDesc = (group.description != null && !group.description.isEmpty()) ? group.description.get(0) : "";

				// get min/max participants
				int groupMinParticipants = group.minParticipants == null ? -1 : group.minParticipants.intValue();
				int groupMaxParticipants = group.maxParticipants == null ? -1 : group.maxParticipants.intValue();

				// waiting list configuration
				boolean waitingList = false;
				if (group.waitingList != null) {
					waitingList = group.waitingList.booleanValue();
				}
				boolean enableAutoCloseRanks = false;
				if (group.autoCloseRanks != null) {
					enableAutoCloseRanks = group.autoCloseRanks.booleanValue();
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
				
				BusinessGroup newGroup = businessGroupService.createBusinessGroup(null, groupName, groupDesc, groupMinParticipants, groupMaxParticipants, waitingList, enableAutoCloseRanks, re);
				//map the group
				env.getGroups().add(new BusinessGroupReference(newGroup, group.key, group.name));
				// get tools config
				String[] availableTools = CollaborationToolsFactory.getInstance().getAvailableTools().clone();
				CollabTools toolsConfig = group.tools;
				CollaborationTools ct = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(newGroup);
				for (int i = 0; i < availableTools.length; i++) {
					try {
						Field field = toolsConfig.getClass().getField(availableTools[i]);
						Boolean val = field.getBoolean(toolsConfig);
						if (val != null) {
							ct.setToolEnabled(availableTools[i], val);
						}
					} catch(NoSuchFieldException e) {
						// hasOpenMeetings compatibility
					} catch (Exception e) {
						log.error("", e);
					}
				}
				if (group.calendarAccess != null) {
					Long calendarAccess = group.calendarAccess;
					ct.saveCalendarAccess(calendarAccess);
				}
				if(group.folderAccess != null) {
				  ct.saveFolderAccess(group.folderAccess);				  
				}
				if (group.info != null) {
					ct.saveNews(group.info);
				}

				// get memberships
				List<String> memberships = group.areaRelations;
				if(memberships != null && memberships.size() > 0) {
					Set<String> uniqueMemberships = new HashSet<>(memberships);
					for (String membership : uniqueMemberships) {
						BGArea area = areaManager.findBGArea(membership, re.getOlatResource());
						if (area != null) {
							areaManager.addBGToBGArea(newGroup, area);
						} else {
							log.error("Area not found");
						}
					}
				}
				
				boolean download = groupModule.isUserListDownloadDefaultAllowed();
				newGroup = businessGroupService.updateDisplayMembers(newGroup, showOwners, showParticipants, showWaitingList, false, false, false, download);
			
				if(dbCount++ % 3 == 0) {
					dbInstance.commitAndCloseSession();
				}
			}
		}
		dbInstance.commitAndCloseSession();
		return env;
	}

}
