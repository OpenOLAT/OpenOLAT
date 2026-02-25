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
import java.util.Locale;

import org.junit.Test;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsFileReader.Import;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: 24 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsObjectsLoaderTest extends OlatTestCase {
	
	@Test
	public void loaderSmokeTest() throws URISyntaxException {
		URL url = ImportCurriculumsFileReaderTest.class.getResource("products-reader-test.xlsx");
		File file = new File(url.toURI());

		ImportCurriculumsFileReader reader = new ImportCurriculumsFileReader(Roles.administratorRoles());
		Import data = reader.loadFile(file);
		
		Translator translator = Util.createPackageTranslator(ImportCurriculumsObjectsLoader.class, Locale.ENGLISH);
		ImportCurriculumsObjectsLoader loader = new ImportCurriculumsObjectsLoader(translator);
		loader.loadCurrentCurriculums(data.curriculumsRows());
		loader.loadCurrentElements(data.elementsRows(), data.curriculumsRows());
		loader.loadUsers(data.usersRows());
		loader.loadMemberships(data.membershipsRows(), data.curriculumsRows(), data.elementsRows(), data.usersRows());
	}
}
