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
package org.olat.modules.curriculum.ui.importwizard;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.DefaultStepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.CurriculumStatus;
import org.olat.modules.curriculum.manager.CurriculumElementTypeDAO;
import org.olat.modules.curriculum.manager.CurriculumElementTypeToTypeDAO;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsFileReader.Import;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 25 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsFinishStepCallbackTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumElementTypeDAO curriculumElementTypeDao;
	@Autowired
	private CurriculumElementTypeToTypeDAO curriculumElementTypeToTypeDao;
	
	/**
	 * This is an end to end test. It will create the elements and users only the first time.
	 * It creates a curriculum, an implementation, 2 elements, 2 users and 3 memberships.
	 * 
	 * @param translator
	 * @throws URISyntaxException
	 */
	@Test
	public void finishWizard() throws URISyntaxException {
		Identity actor = JunitTestHelper.getDefaultAdministrator();
		Roles roles = securityManager.getRoles(actor);
		// Setup the mandatory element types
		setupTypes();
		
		URL url = ImportCurriculumsFileReaderTest.class.getResource("products-finish-test.xlsx");
		File file = new File(url.toURI());

		// Read the Excel file
		ImportCurriculumsFileReader reader = new ImportCurriculumsFileReader(roles);
		Import data = reader.loadFile(file);

		// Load the data
		Translator translator = Util.createPackageTranslator(ImportCurriculumsObjectsLoader.class, Locale.ENGLISH);
		ImportCurriculumsObjectsLoader loader = new ImportCurriculumsObjectsLoader(translator);
		loader.loadCurrentCurriculums(data.curriculumsRows());
		loader.loadCurrentElements(data.elementsRows(), data.curriculumsRows());
		loader.loadUsers(data.usersRows());
		loader.loadMemberships(data.membershipsRows(), data.curriculumsRows(), data.elementsRows(), data.usersRows());
		
		// Validate the data
		ImportCurriculumsValidator validator = new ImportCurriculumsValidator(actor, roles, translator);
		for(ImportedRow row:data.curriculumsRows()) {
			validator.validate(row);
		}
		for(ImportedRow row:data.elementsRows()) {
			validator.validate(row);
		}
		for(ImportedUserRow row:data.usersRows()) {
			validator.validate(row);
		}
		for(ImportedMembershipRow row:data.membershipsRows()) {
			validator.validate(row);
		}
		
		// Check validation
		Assertions.assertThat(data.curriculumsRows())
			.hasSize(1)
			.allMatch(r -> r.getValidationStatistics().errors() == 0);
		Assertions.assertThat(data.elementsRows())
			.hasSize(4)
			.allMatch(r -> r.getValidationStatistics().errors() == 0);
		Assertions.assertThat(data.usersRows())
			.hasSize(2)
			.allMatch(r -> r.getValidationStatistics().errors() == 0);
		Assertions.assertThat(data.membershipsRows())
			.hasSize(3)
			.allMatch(r -> r.getValidationStatistics().errors() == 0);
		
		// Finish
		ImportCurriculumsContext context = new ImportCurriculumsContext(actor, roles, translator);
		context.setImportedCurriculumsRows(data.curriculumsRows());
		context.setImportedElementsRows(data.elementsRows());
		context.setImportedUsersRows(data.usersRows());
		context.setImportedMembershipsRows(data.membershipsRows());
		
		ImportCurriculumsFinishStepCallback finishCallback = new ImportCurriculumsFinishStepCallback(context);
		WindowControl wControl = new WindowControlMocker();
		UserRequest ureq = new SyntheticUserRequest(actor, Locale.ENGLISH);
		finishCallback.execute(ureq, wControl, new DefaultStepsRunContext());
		
		// Check curriculum
		List<Curriculum> curriculums = curriculumService.getCurriculumsByIdentifier("P_FR", CurriculumStatus.active);
		Assertions.assertThat(curriculums)
			.hasSize(1);
		
		Curriculum curriculum = curriculums.get(0);
		Assert.assertEquals("French", curriculum.getDisplayName());
		
		// Check implementation
		List<CurriculumElement> implementations = curriculumService.getImplementations(curriculum, CurriculumElementStatus.notDeleted());
		Assertions.assertThat(implementations)
			.hasSize(1);

		CurriculumElement implementation = implementations.get(0);
		Assert.assertEquals("French - Français pour les d\u00E9butants", implementation.getDisplayName());
		
		List<CurriculumElement> elements = curriculumService.searchCurriculumElements(curriculum, implementation, null,
				"P_FR-IMPL_1_EPP_SP26-M_1_ID", null, CurriculumElementStatus.notDeleted());
		Assertions.assertThat(elements)
			.hasSize(1);
		CurriculumElement element = elements.get(0);
		
		// Check user
		Identity user = securityManager.findIdentityByNickName("ablue");
		Assert.assertNotNull(user);
		Assert.assertEquals("Blue", user.getUser().getLastName());
		
		// Check memberships
		List<Identity> participants = curriculumService.getMembersIdentity(element, CurriculumRoles.participant);
		Assertions.assertThat(participants)
			.hasSize(1)
			.containsExactly(user);
	}
	
	private void setupTypes() {
		Map<String,CurriculumElementType> types = curriculumElementTypeDao.load().stream()
				.collect(Collectors.toMap(CurriculumElementType::getIdentifier, t -> t, (u, v) -> u));
		
		CurriculumElementType type = types.get("P-SEMESTER-IMPORT-TEST");
		if(type == null) {
			type = curriculumElementTypeDao.createCurriculumElementType("P-SEMESTER-IMPORT-TEST", "Root type for import test", null, null);
		}
		
		CurriculumElementType subType1 = types.get("P-MODULE-IMPORT-TEST");
		if(subType1 == null) {
			subType1 = curriculumElementTypeDao.createCurriculumElementType("P-MODULE-IMPORT-TEST", "Sub-type module for import test", null, null);
			curriculumElementTypeToTypeDao.addAllowedSubType(type, subType1);
		}
		
		CurriculumElementType subType2 = types.get("P-COURSE-IMPORT-TEST");
		if(subType2 == null) {
			subType2 = curriculumElementTypeDao.createCurriculumElementType("P-COURSE-IMPORT-TEST", "Sub-sub-type course for import test", null, null);
			curriculumElementTypeToTypeDao.addAllowedSubType(subType1, subType2);
		}
		dbInstance.commitAndCloseSession();
	}

}
