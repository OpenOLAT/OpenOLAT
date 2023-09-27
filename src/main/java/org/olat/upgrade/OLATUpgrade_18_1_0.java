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

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.commons.calendar.CalendarModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.manager.ProjectServiceImpl;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Aug 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_18_1_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_18_1_0.class);

	private static final int BATCH_SIZE = 1000;

	private static final String VERSION = "OLAT_18.1.0";

	private static final String MIGRATE_PROJ_ARTEFACT_EDITORS = "MIGRATE PROJ ARTEFACT EDITORS";
	private static final String UPDATE_MANAGED_CONFIGS = "UPDATED MANAGED CONFIGS";
	private static final String UPDATE_PASSWORD_USER_TOOL = "UPDATED PASSWORD USER TOOL";
	
	@Autowired
	private DB dbInstance;
 	@Autowired
 	private UserToolsModule userToolsModule;
 	@Autowired
	private ProjectServiceImpl projectService;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private RepositoryModule repositoryModule;

	public OLATUpgrade_18_1_0() {
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
		allOk &= migrateProjectArtefactEditors(upgradeManager, uhd);
		allOk &= enablaManagedCalendars(upgradeManager, uhd);
		allOk &= updatePasswordUserTool(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_18_1_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_18_1_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean migrateProjectArtefactEditors(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATE_PROJ_ARTEFACT_EDITORS)) {
			try {
				log.info("Start project artefact editors migration");
				
				int counter = 0;
				List<ProjActivity.Action> fileActions = List.of(
						ProjActivity.Action.fileCreate,
						ProjActivity.Action.fileUpload,
						ProjActivity.Action.fileEdit,
						ProjActivity.Action.fileContentUpdate);
				List<ProjFile> files;
				do {
					files = getProjectFiles(counter, BATCH_SIZE);
					for (ProjFile file:files) {
						getProjectEditor(file.getArtefact(), fileActions)
								.forEach(editor -> projectService.addMember(editor, file.getArtefact(), editor));
					}
					counter += files.size();
					log.info(Tracing.M_AUDIT, "Project files editors migration: {} total processed ({})", files.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (files.size() == BATCH_SIZE);
				
				counter = 0;
				List<ProjActivity.Action> noteActions = List.of(
						ProjActivity.Action.noteCreate,
						ProjActivity.Action.noteContentUpdate);
				List<ProjNote> notes;
				do {
					notes = getProjectNotes(counter, BATCH_SIZE);
					for (ProjNote note:notes) {
						getProjectEditor(note.getArtefact(), noteActions)
								.forEach(editor -> projectService.addMember(editor, note.getArtefact(), editor));
					}
					counter += notes.size();
					log.info(Tracing.M_AUDIT, "Project notes editors migration: {} total processed ({})", notes.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (files.size() == BATCH_SIZE);
				
				log.info("Project artefact editors migration finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_PROJ_ARTEFACT_EDITORS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private List<ProjFile> getProjectFiles(int firstResult, int maxResults) {
		String query = """
				select file
				  from projfile as file
				       join fetch file.artefact as artefact
				 order by file.key
				""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, ProjFile.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();
	}
	
	private List<ProjNote> getProjectNotes(int firstResult, int maxResults) {
		String query = """
				select note
				  from projnote as note
				       join fetch note.artefact as artefact
				 order by note.key
				""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, ProjNote.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();
	}
	
	private List<Identity> getProjectEditor(ProjArtefact artefact, List<ProjActivity.Action> actions) {
		String query = """
				select distinct activity.doer
				  from projactivity as activity
				 where activity.artefact.key = :artefactKey
				   and activity.action in :actions
				""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Identity.class)
				.setParameter("artefactKey", artefact.getKey())
				.setParameter("actions", actions)
				.getResultList();
	}
	
	private boolean enablaManagedCalendars(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(UPDATE_MANAGED_CONFIGS)) {
			calendarModule.setManagedCalendars(true);
			log.info("Managed calendars enabled");
			
			if (repositoryModule.isManagedRepositoryEntries()) {
				lectureModule.setLecturesManaged(true);
				log.info("Managed lectures enabled");
			} else {
				lectureModule.setLecturesManaged(false);
				log.info("Managed lectures disabled");
			}
			
			uhd.setBooleanDataValue(UPDATE_MANAGED_CONFIGS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}
	
	
	private boolean updatePasswordUserTool(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UPDATE_PASSWORD_USER_TOOL)) {
			try {
				log.info("Enable media center.");
				
				String availableTools = userToolsModule.getAvailableUserTools();
				if(StringHelper.containsNonWhitespace(availableTools)
						&& availableTools.contains("org.olat.home.HomeMainController:org.olat.user.ChangePasswordController")) {
					availableTools = availableTools.replace("org.olat.home.HomeMainController:org.olat.user.ChangePasswordController",
							"org.olat.home.HomeMainController:org.olat.user.ui.identity.UserAuthenticationsController");
				}
				userToolsModule.setAvailableUserTools(availableTools);
				log.info("Update password user tool.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(UPDATE_PASSWORD_USER_TOOL, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
}
