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
package org.olat.course.archiver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 16 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseMembershipsExportTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void export() throws IOException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("memb-exp-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(owner);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("memb-exp-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("memb-exp-3");
		Identity groupParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("memb-exp-4");
		Identity cplParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("memb-exp-5");
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
		
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Memberships-Group-Blue", "tg",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, entry);
		businessGroupRelationDao.addRole(groupParticipant, group, GroupRoles.participant.name());
		
		Curriculum curriculum = curriculumService.createCurriculum("Memberships-Curriculum", "Memberships Curriculum", "Description", false,
				organisationService.getDefaultOrganisation());
		CurriculumElement element = curriculumService.createCurriculumElement("Memberships-Element", "Memberships Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(element, entry, false);
		curriculumService.addMember(element, cplParticipant, CurriculumRoles.participant, owner);
		dbInstance.commitAndCloseSession();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new CourseMembershipsExport(entry, owner, Locale.ENGLISH).export(out);
		
		Map<String, String> files = unzip(out.toByteArray());
		assertThat(files).containsKeys("xl/worksheets/sheet1.xml", "xl/worksheets/sheet2.xml",
				"xl/worksheets/sheet3.xml", "xl/worksheets/sheet4.xml");
		
		String workbook = files.get("xl/workbook.xml");
		assertThat(workbook).contains("Participants", "Coaches", "Owners", "Export information");
		
		String strings = files.get("xl/sharedStrings.xml");
		assertThat(strings).contains("Memberships-Group-Blue", "Memberships Element", "Group participant", "CPL participant",
				"Owner", "Coach", "Participant", "Course title", "Course reference", "EN");
		assertThat(strings).contains(participant.getUser().getLastName(), coach.getUser().getLastName(),
				owner.getUser().getLastName(), groupParticipant.getUser().getLastName(), cplParticipant.getUser().getLastName());
	}
	
	private Map<String, String> unzip(byte[] content) throws IOException {
		Map<String, String> files = new HashMap<>();
		try(ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(content))) {
			ZipEntry entry;
			while((entry = zin.getNextEntry()) != null) {
				files.put(entry.getName(), new String(zin.readAllBytes(), StandardCharsets.UTF_8));
			}
		}
		return files;
	}
}
