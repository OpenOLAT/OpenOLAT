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
package org.olat.modules.curriculum.manager;

import java.io.File;
import java.net.URL;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementToRepositoryEntryRefs;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumXStreamTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	
	@Test
	public void toStream() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-XSTREAM-1", "My Curriculum 1", "Short desc.", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		Curriculum loadedCurriculum = curriculumService.getCurriculum(curriculum);
		String xml = CurriculumXStream.toXml(loadedCurriculum);
		Assert.assertTrue(xml.contains(curriculum.getDisplayName()));
		Assert.assertTrue(xml.contains(element.getDisplayName()));
		
		Curriculum streamedCurriculum = CurriculumXStream.fromXml(xml);
		Assert.assertNotNull(streamedCurriculum);
		Assert.assertEquals(loadedCurriculum.getKey(), streamedCurriculum.getKey());
	}
	
	@Test
	public void curriculumFromXml_version14x() throws Exception {
		URL input = CurriculumXStreamTest.class.getResource("curriculum.xml");
		File inputFile = new File(input.getFile());
		Curriculum export = CurriculumXStream.curriculumFromPath(inputFile.toPath());
		Assert.assertNotNull(export);
	}
	
	@Test
	public void entriesFromXml_version14x() throws Exception {
		URL input = CurriculumXStreamTest.class.getResource("curriculum_entries.xml");
		File inputFile = new File(input.getFile());
		CurriculumElementToRepositoryEntryRefs export = CurriculumXStream.entryRefsFromPath(inputFile.toPath());
		Assert.assertNotNull(export);
	}
}
