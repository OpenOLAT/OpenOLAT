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
package org.olat.modules.lecture.ui.blockimport;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.util.i18n.I18nModule;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: 15 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BlockConverterTest extends OlatTestCase {
	
	@Test
	public void testDateFormat() throws Exception {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-lec");
		
		BlockConverter converter = new BlockConverter(entry, new ArrayList<>(), I18nModule.getDefaultLocale());
		
		String input = "L1	Mathematik	4	10/8/18	8:00	12:00	yes	" + teacher.getUser().getEmail() +"	COURSE	Basel, HS76	This is about this and that	Make your HP ready	Teacher identified via Email address, participants from course";
		converter.parse(input);
		
		List<ImportedLectureBlock> importedBlocks = converter.getImportedLectureBlocks();
		Assert.assertNotNull(importedBlocks);
		Assert.assertEquals(1, importedBlocks.size());
		
		ImportedLectureBlock importedBlock = importedBlocks.get(0);
		Assert.assertNotNull(importedBlock);
		Assert.assertNotNull(importedBlock.getGroupMapping());
		
		GroupMapping mapping = importedBlock.getGroupMapping();
		Assert.assertEquals(GroupMapping.Type.course, mapping.type());
		Assert.assertNotNull(mapping.getGroup());
		
		List<Identity> teachers = importedBlock.getTeachers();
		Assert.assertEquals(1, teachers.size());
		Assert.assertEquals(teacher, teachers.get(0));
		
		// the lecture block itself
		LectureBlock lectureBlock = importedBlock.getLectureBlock();
		Assert.assertNotNull(lectureBlock);
		Assert.assertEquals("L1", lectureBlock.getExternalId());
		Assert.assertEquals("Mathematik", lectureBlock.getTitle());
		Assert.assertNotNull(lectureBlock.getStartDate());
		Assert.assertNotNull(lectureBlock.getEndDate());
		Assert.assertTrue(lectureBlock.isCompulsory());
		Assert.assertEquals("Basel, HS76", lectureBlock.getLocation());
		Assert.assertEquals("This is about this and that", lectureBlock.getDescription());
		Assert.assertEquals("Make your HP ready", lectureBlock.getPreparation());
		Assert.assertEquals("Teacher identified via Email address, participants from course", lectureBlock.getComment());
	}
}
