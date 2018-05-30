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
package org.olat.modules.forms.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormStorageTest extends OlatTestCase {

	@Autowired
	private EvaluationFormStorage sut;
	
	@Before
	public void clean() throws IOException {
		Path responsesDir = sut.getResponsesRoot();
		// Delete all files and directories
		Files.walk(responsesDir)
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.forEach(File::delete);
	}
	
	@Test
	public void shouldSaveNewFile() throws Exception {
		File file = getExistingFile();
		
		Path relativePath = sut.save(file, getRandomFilename());
		
		assertThat(relativePath).isNotNull();
	}

	@Test
	public void shouldLoadFile() throws Exception {
		Path relativePath = sut.save(getExistingFile(), getRandomFilename());
		
		File loadedFile = sut.load(relativePath);
		
		assertThat(loadedFile).exists();
	}
	
	@Test
	public void shouldDeleteFile() throws Exception {
		assertThat(getExistingFile()).exists();
		Path relativePath = sut.save(getExistingFile(), getRandomFilename());
		File loadedFile = sut.load(relativePath);
		assertThat(loadedFile).exists();
		
		sut.delete(relativePath);
		
		assertThat(loadedFile).doesNotExist();
	}

	private File getExistingFile() throws URISyntaxException {
		URL url = JunitTestHelper.class.getResource("file_resources/house.jpg");
		return Paths.get(url.toURI()).toFile();
	}
	
	private String getRandomFilename() {
		return UUID.randomUUID().toString();
	}

}
