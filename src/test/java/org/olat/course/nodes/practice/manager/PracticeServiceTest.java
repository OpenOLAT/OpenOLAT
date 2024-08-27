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
package org.olat.course.nodes.practice.manager;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.practice.PlayMode;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.ims.qti21.repository.handlers.QTI21AssessmentTestHandler;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExportLinkEnum;
import org.olat.test.ArquillianDeployments;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeServiceTest extends OlatTestCase {
	
	private static RepositoryEntry courseEntry;
	private static RepositoryEntry testEntry;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private QTI21AssessmentTestHandler testHandler;
	
	@Before
	public void setup() throws Exception {
		if(courseEntry == null) {
			Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-1");
			courseEntry = JunitTestHelper.deployBasicCourse(author);
			
			URL testUrl = ArquillianDeployments.class.getResource("file_resources/qti21/test_15_questions.zip");
			File testFile = new File(testUrl.toURI());
			testEntry = testHandler.importResource(author, null, "Test - 15 questions", "",
					RepositoryEntryImportExportLinkEnum.NONE, null, Locale.ENGLISH, testFile, testFile.getName());
		}
	}
	
	@Test
	public void generateItems() {
		String subIdent = UUID.randomUUID().toString();
		PracticeResource resource = practiceService.createResource(courseEntry, subIdent, testEntry);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("practice-1");
		dbInstance.commitAndCloseSession();
		
		List<PracticeResource> resources = practiceService.getResources(courseEntry, subIdent);
		// Close need to check query fetching of OLAT resources
		dbInstance.commitAndCloseSession();
		Assertions.assertThat(resources)
			.hasSize(1)
			.contains(resource);
		
		SearchPracticeItemParameters searchParams = new SearchPracticeItemParameters();
		searchParams.setPlayMode(PlayMode.all);
		searchParams.setIdentity(participant);
		searchParams.setCourseEntry(courseEntry);
		searchParams.setSubIdent(subIdent);
		
		List<PracticeItem> items = practiceService.generateItems(resources, searchParams, 10, Locale.ENGLISH);
		Assertions.assertThat(items)
			.isNotNull()
			.hasSize(10);
	}
}
