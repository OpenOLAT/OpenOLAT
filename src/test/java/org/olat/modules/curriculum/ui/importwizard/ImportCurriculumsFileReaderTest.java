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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.id.Roles;
import org.olat.modules.curriculum.ui.CurriculumExportType;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsFileReader.Import;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: 9 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsFileReaderTest extends OlatTestCase {
	
	@Test
	public void loadFileCurriculum() throws URISyntaxException {
		URL url = ImportCurriculumsFileReaderTest.class.getResource("products-reader-test.xlsx");
		File file = new File(url.toURI());

		ImportCurriculumsFileReader reader = new ImportCurriculumsFileReader(Roles.administratorRoles());
		Import data = reader.loadFile(file);
		List<ImportedRow> curriculumsRows = data.curriculumsRows();
		Assertions.assertThat(curriculumsRows)
			.hasSize(1)
			.map(ImportedRow::getCurriculumIdentifier)
			.contains("P_ESP");
		
		ImportedRow row = curriculumsRows.get(0);
		Assert.assertEquals("Spanish", row.getDisplayName());
	}

	@Test
	public void loadFileElements() throws URISyntaxException {
		URL url = ImportCurriculumsFileReaderTest.class.getResource("products-reader-test.xlsx");
		File file = new File(url.toURI());

		ImportCurriculumsFileReader reader = new ImportCurriculumsFileReader(Roles.administratorRoles());
		Import data = reader.loadFile(file);
		List<ImportedRow> elementsRows = data.elementsRows();
		Assertions.assertThat(elementsRows)
			.hasSize(38)
			.map(ImportedRow::type)
			.contains(CurriculumExportType.IMPL, CurriculumExportType.ELEM, CurriculumExportType.COURSE, CurriculumExportType.TMPL, CurriculumExportType.EVENT)
			.doesNotContainSequence(CurriculumExportType.CUR);
		
		ImportedRow row = elementsRows.get(0);
		Assert.assertEquals(CurriculumExportType.IMPL, row.type());
		Assert.assertEquals("P_ESP", row.getCurriculumIdentifier());
		Assert.assertEquals("P_ESP-IMPL_1_EPP_SP26", row.getImplementationIdentifier());
		Assert.assertEquals("Spanish - Español para principiantes", row.getDisplayName());
		Assert.assertEquals("P_ESP-IMPL_1_EPP_SP26", row.getIdentifier());
		Assert.assertEquals("CONFIRMED", row.getElementStatus());
		Assert.assertNotNull(row.getStartDate());
		Assert.assertNull(row.getStartTime());
		Assert.assertNotNull(row.getEndDate());
		Assert.assertNull(row.getEndTime());
	}
	
	@Test
	public void loadFileUsers() throws URISyntaxException {
		URL url = ImportCurriculumsFileReaderTest.class.getResource("products-reader-test.xlsx");
		File file = new File(url.toURI());

		ImportCurriculumsFileReader reader = new ImportCurriculumsFileReader(Roles.administratorRoles());
		Import data = reader.loadFile(file);
		List<ImportedUserRow> usersRows = data.usersRows();
		Assertions.assertThat(usersRows)
			.hasSize(7)
			.map(ImportedUserRow::getUsername)
			.contains("slangenegger", "knotter", "mdelacruz", "cwieser", "akieser", "equastmann_lwitschi", "cmueller");
		
		ImportedUserRow row = usersRows.get(0);
		Assert.assertEquals("slangenegger", row.getUsername());
		Assert.assertEquals("Simone", row.getIdentityProp(1));
		Assert.assertEquals("Langenegger", row.getIdentityProp(2));
		Assert.assertEquals("simone.langenegger@openolat.com", row.getIdentityProp(3));
		Assert.assertEquals("default-org", row.getOrganisationIdentifier());
	}
	
	@Test
	public void loadFileMembers() throws URISyntaxException {
		URL url = ImportCurriculumsFileReaderTest.class.getResource("products-reader-test.xlsx");
		File file = new File(url.toURI());

		ImportCurriculumsFileReader reader = new ImportCurriculumsFileReader(Roles.administratorRoles());
		Import data = reader.loadFile(file);
		List<ImportedMembershipRow> membersRows = data.membershipsRows();
		Assertions.assertThat(membersRows)
			.hasSize(26)
			.map(ImportedMembershipRow::getUsername)
			.contains("slangenegger", "knotter", "mdelacruz", "cwieser", "akieser", "cmueller")
			.doesNotContain("equastmann_lwitschi");
	
		
	}

}
