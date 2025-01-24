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
package org.olat.modules.curriculum.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParentBasedIdentifierGeneratorTest extends OlatTestCase {
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private ParentBasedIdentifierGenerator externalRefGenerator;
	
	@Test
	public void generatorImplementationExternalRefFromCurriculum() {
		Curriculum curriculum = curriculumService.createCurriculum("GEN-1", "Generator 1", "Short desc.", false, null);
		String identifier = externalRefGenerator.generate(curriculum, null, null);
		Assert.assertEquals("GEN-1", identifier);
	}
	
	@Test
	public void generatorImplementationExternalRefFromImplementation() {
		Curriculum curriculum = curriculumService.createCurriculum("GEN-2", "Generator 2", "Short desc.", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("GEN-CC-2-1", "Element for generator",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		String identifier = externalRefGenerator.generate(curriculum, element, element);
		Assert.assertEquals("GEN-CC-2-1", identifier);
	}

}
