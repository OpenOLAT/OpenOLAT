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
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupMembershipInfos;
import org.olat.repository.RepositoryDeletionModule;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BusinessGroupUserDataManager implements UserDataDeletable, UserDataExportable {
	
	private static final OLog log = Tracing.createLoggerFor(BusinessGroupUserDataManager.class);
	
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryDeletionModule deletionManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDAO;
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		// remove as Participant 
		List<BusinessGroup> attendedGroups = businessGroupService.findBusinessGroupsAttendedBy(identity);
		for (Iterator<BusinessGroup> iter = attendedGroups.iterator(); iter.hasNext();) {
			businessGroupRelationDAO.removeRole(identity, iter.next(), GroupRoles.participant.name());
		}
		log.debug("Remove partipiciant identity=" + identity + " from " + attendedGroups.size() + " groups");
		// remove from waitinglist 
		List<BusinessGroup> waitingGroups = businessGroupService.findBusinessGroupsWithWaitingListAttendedBy(identity, null);
		for (Iterator<BusinessGroup> iter = waitingGroups.iterator(); iter.hasNext();) {
			businessGroupRelationDAO.removeRole(identity, iter.next(), GroupRoles.waiting.name());
		}
		log.debug("Remove from waiting-list identity=" + identity + " in " + waitingGroups.size() + " groups");

		// remove as owner
		List<BusinessGroup> ownerGroups = businessGroupService.findBusinessGroupsOwnedBy(identity);
		for (Iterator<BusinessGroup> iter = ownerGroups.iterator(); iter.hasNext();) {
			BusinessGroup businessGroup = iter.next();
			businessGroupRelationDAO.removeRole(identity, businessGroup, GroupRoles.coach.name());
			if (businessGroupRelationDAO.countRoles(businessGroup, GroupRoles.coach.name()) == 0) {
				Identity admin = deletionManager.getAdminUserIdentity();
				if(admin == null) {
					log.info("Delete user-data, cannot add Administrator-identity as owner of businessGroup=" + businessGroup.getName() + ". Businesgroup is orphan");
				} else {
					businessGroupRelationDAO.addRole(admin, businessGroup, GroupRoles.coach.name());
					log.info("Delete user-data, add Administrator-identity as owner of businessGroup=" + businessGroup.getName());
				}
			}
		}
		log.debug("Remove owner identity=" + identity + " from " + ownerGroups.size() + " groups");
		log.debug("All entries in groups deleted for identity=" + identity);
	}

	@Override
	public String getExporterID() {
		return "group.memberships";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File membershipsArchive = new File(archiveDirectory, "GroupMemberships.xlsx");
		try(OutputStream out = new FileOutputStream(membershipsArchive);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			
			Row header = sheet.newRow();
			header.addCell(0, "Type");
			header.addCell(1, "Group");
			header.addCell(2, "Registration");
			header.addCell(3, "Last visit");

			List<BusinessGroupMembershipInfos> memberships = businessGroupDao.getMemberships(identity);
			for(BusinessGroupMembershipInfos membership:memberships) {
				Row row = sheet.newRow();
				row.addCell(0, membership.getRole());
				row.addCell(1, membership.getBusinessGroupName());
				row.addCell(2, membership.getCreationDate(), workbook.getStyles().getDateTimeStyle());
				row.addCell(3, membership.getLastModified(), workbook.getStyles().getDateTimeStyle());
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		manifest.appendFile(membershipsArchive.getName());
	}
}
