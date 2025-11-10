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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.core.CourseElement;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.UserDisplayIdentifierGenerator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 nov 2025<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_19_1_26 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_19_1_26.class);

	private static final String VERSION = "OLAT_19.1.26";

	private static final String AE_USER_DISPLAY_IDENTIFIER = "AE USER DISPLAY_IDENTIFIER";

	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;

	public OLATUpgrade_19_1_26() {
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
		allOk &= createUserDisplayIdentifiers(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_19_1_26 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_19_1_26 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean createUserDisplayIdentifiers(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(AE_USER_DISPLAY_IDENTIFIER)) {
			try {
				log.info("Start creating user display identifier.");
				
				List<CourseElement> courseElements = getTestCourseElements();
				for(int i=0; i<courseElements.size(); i++) {
					CourseElement courseElement = courseElements.get(i);
					createUserDisplayIdentifiers(courseElement);
					dbInstance.commitAndCloseSession();
					if(i % 25 == 0) {
						log.info(Tracing.M_AUDIT, "User display identifiers created: {} / {}", i , courseElements.size());
					}
				}
				log.info("End creating user display identifier.");
				
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(AE_USER_DISPLAY_IDENTIFIER, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}
	
	private void createUserDisplayIdentifiers(CourseElement courseElement) {
		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesBySubIdent(
				() -> courseElement.getRepositoryEntry().getKey(), courseElement.getSubIdent());
		
		List<AssessmentEntry> assessmentEntriesWithoutIdentifier = new ArrayList<>();
		Set<String> identifiers = new HashSet<>();
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			if (StringHelper.containsNonWhitespace(assessmentEntry.getUserDisplayIdentifier())) {
				identifiers.add(assessmentEntry.getUserDisplayIdentifier());
			} else {
				assessmentEntriesWithoutIdentifier.add(assessmentEntry);
			}
		}
		
		for(int i=0; i<assessmentEntriesWithoutIdentifier.size(); i++) {
			AssessmentEntry assessmentEntry = assessmentEntriesWithoutIdentifier.get(i);
			
			String userDisplayIdentifier = null;
			while (userDisplayIdentifier == null) {
				userDisplayIdentifier = UserDisplayIdentifierGenerator.generate();
				
				if (identifiers.contains(userDisplayIdentifier)) {
					userDisplayIdentifier = null;
				}
			}
			
			assessmentEntry.setUserDisplayIdentifier(userDisplayIdentifier);
			dbInstance.getCurrentEntityManager().merge(assessmentEntry);
			identifiers.add(userDisplayIdentifier);
			
			if(i % 25 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private List<CourseElement> getTestCourseElements() {
		String sb = """
			select ce from courseelement ce
			where ce.type = 'iqtest'
			order by ce.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, CourseElement.class)
				.getResultList();
	}
}
